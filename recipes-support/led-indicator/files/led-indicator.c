#include <stdio.h>
#include <string.h>

#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>

#include <stdbool.h>
#include <unistd.h>

const char* led_green = "smartgw:internet:green";
const char* led_red = "smartgw:internet:red";

static void usage(FILE *stream, char const *program_name) {
    fprintf(stream, "USAGE: %s identify OR <LED name> <on|off|flash>\n", program_name);
}

static void led_flash(const char *led) {
    char path[100];
    snprintf(path, sizeof(path), "/sys/class/leds/%s/trigger", led);

    const int fd = open(path, O_WRONLY);
    write(fd, "timer", 5);
    close(fd);
}

static void led_on(const char *led) {
    char path[100];
    snprintf(path, sizeof(path), "/sys/class/leds/%s/trigger", led);

    const int fd_trigger = open(path, O_WRONLY);
    write(fd_trigger, "oneshot", 7);
    close(fd_trigger);

    snprintf(path, sizeof(path), "/sys/class/leds/%s/brightness", led);
    const int fd_brightness = open(path, O_WRONLY);
    write(fd_brightness, "1", 1);
    close(fd_brightness);
}

static void led_off(const char *led) {
    char path[50];
    snprintf(path, sizeof(path), "/sys/class/leds/%s/trigger", led);

    const int fd_trigger = open(path, O_WRONLY);
    write(fd_trigger, "oneshot", 7);
    close(fd_trigger);

    snprintf(path, sizeof(path),  "/sys/class/leds/%s/brightness", led);
    const int fd_brightness = open(path, O_WRONLY);
    write(fd_brightness, "0", 1);
    close(fd_brightness);
}

// Controls the On/Off period of a flashing LED
static void flash(const char *led, const int ms) {
    static char path[100], path2[100], ms_buf[20];
    snprintf(path, sizeof(path), "/sys/class/leds/%s/delay_on", led);
    snprintf(path2, sizeof(path2), "/sys/class/leds/%s/delay_off", led);

    const int fd = open(path, O_WRONLY);
    const int fd2 = open(path2, O_WRONLY);

    snprintf(ms_buf, sizeof(ms_buf), "%d", ms);
    write(fd, ms_buf, strlen(ms_buf));
    write(fd2, ms_buf, strlen(ms_buf));

    close(fd);
    close(fd2);
}

static void identify(void) {
    // Fork the process, so it does not block until finished
    if (fork() == 0) {
        const int time0 = 250;
        const int time1 = 200;
        const int time2 = 141;
        const int time3 = 100;
        const int time4 = 71;
        const int time5 = 50;

        led_on(led_green);
        led_on(led_red);

        // Make sure LEDs are on before starting the procedure
        usleep(1000 * 10);

        led_flash(led_green);
        led_flash(led_red);

        flash(led_green, time1);
        flash(led_red, time1);
        usleep(time0 * 1000);

        flash(led_green, time2);
        flash(led_red, time2);
        usleep(time0 + 4 * (time1)*1000);

        flash(led_green, time3);
        flash(led_red, time3);
        usleep(time0 + 4 * (time1 + time2) * 1000);

        flash(led_green, time4);
        flash(led_red, time4);
        usleep(time0 + 4 * (time1 + time2 + time3) * 1000);

        flash(led_green, time5);
        flash(led_red, time5);
        usleep(time0 + 4 * (time1 + time2 + time3 + time4) * 1000);

        led_off(led_red);
        led_on(led_green);
    }
}

int main(int argc, const char *argv[]) {

    if (argc == 2 && strcmp(argv[1], "identify") == 0) {
        identify();
        return 0;
    }

    if (argc < 3) {
        usage(stderr, argv[0]);
        return 1;
    }

    const char *led = argv[1];
    const char *cmd = argv[2];

    // Check if LED exists
    {
        char buf[100];
        struct stat st;
        snprintf(buf, sizeof(buf), "/sys/class/leds/%s", led);
        stat(buf, &st);
        if (!S_ISDIR(st.st_mode)) {
            fprintf(stderr, "LED %s not found!\n", led);
            return 1;
        }
    }

    // Check what command we have received
    if (strcmp(cmd, "on") == 0) {
        led_on(led);
    } else if (strcmp(cmd, "off") == 0) {
        led_off(led);
    } else if (strcmp(cmd, "flash") == 0) {
        led_flash(led);
    } else {
        // Unrecognised command
        usage(stderr, argv[0]);
        return 1;
    }

    return 0;
}
