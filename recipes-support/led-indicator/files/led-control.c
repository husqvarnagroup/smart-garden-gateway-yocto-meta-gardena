#include <stdio.h>
#include <string.h>

#include <sys/stat.h>
#include <sys/types.h>
#include <fcntl.h>

#include <unistd.h>
#include <stdbool.h>

void usage(char const *program_name)
{
    printf("USAGE: %s identify OR <LED name> <on|off|flash>\n", program_name);
}

void led_flash(char *led)
{
    static int fd;
    static char path[50];
    sprintf(path, "/sys/class/leds/%s/trigger", led);

    fd = open(path, O_WRONLY);
    write(fd, "timer", 5);
    close(fd);
}

void led_on(char *led)
{
    static int fd;
    static char path[50];
    sprintf(path, "/sys/class/leds/%s/trigger", led);

    fd = open(path, O_WRONLY);
    write(fd, "oneshot", 7);
    close(fd);

    sprintf(path, "/sys/class/leds/%s/brightness", led);
    fd = open(path, O_WRONLY);
    write(fd, "1", 1);
    close(fd);
}

void led_off(char *led)
{
    static int fd;
    static char path[50];
    sprintf(path, "/sys/class/leds/%s/trigger", led);

    fd = open(path, O_WRONLY);
    write(fd, "oneshot", 7);
    close(fd);

    sprintf(path, "/sys/class/leds/%s/brightness", led);
    fd = open(path, O_WRONLY);
    write(fd, "0", 1);
    close(fd);
}

// Controls the On/Off period of a flashing LED
void flash(char *led, int ms)
{
    static int fd, fd2;
    static char path[50], path2[50], ms_buf[20];
    sprintf(path, "/sys/class/leds/%s/delay_on", led);
    sprintf(path2, "/sys/class/leds/%s/delay_off", led);

    fd = open(path, O_WRONLY);
    fd2 = open(path2, O_WRONLY);

    sprintf(ms_buf, "%d", ms);
    write(fd, ms_buf, strlen(ms_buf));
    write(fd2, ms_buf, strlen(ms_buf));

    close(fd);
    close(fd2);
}

void identify(void)
{
    // Fork the process, so it does not block until finished
    if(fork() == 0)
    {
        const int time0 = 250;
        const int time1 = 200;
        const int time2 = 141;
        const int time3 = 100;
        const int time4 = 71;
        const int time5 = 50;

        led_on("green3");
        led_on("red3");

        // Make sure LEDs are on before starting the procedure
        usleep(1000*10);

        led_flash("green3");
        led_flash("red3");

        flash("green3", time1);
        flash("red3", time1);
        usleep(time0 * 1000);

        flash("green3", time2);
        flash("red3", time2);
        usleep(time0 + 4 * (time1) * 1000);

        flash("green3", time3);
        flash("red3", time3);
        usleep(time0 + 4 * (time1+time2) * 1000);

        flash("green3", time4);
        flash("red3", time4);
        usleep(time0 + 4 * (time1+time2+time3) * 1000);

        flash("green3", time5);
        flash("red3", time5);
        usleep(time0 + 4 * (time1+time2+time3+time4) * 1000);

        led_off("red3");
        led_on("green3");
    }
}

int main(int argc, char const *argv[])
{
    unsigned int usecs;
    char *led, *cmd;
    char buf[50];
    bool isdir;
    struct stat st;

    led = (char *)argv[1];
    cmd = (char *)argv[2];

    // led should not be null before string comaprison
    if(!led)
    {
        usage(argv[0]);
        return 1;
    }

    // Special case, identify command
    if(strcmp(led, "identify") == 0)
    {
        identify();
        return 0;
    }

    // Do not accept null values
    if(!led || !cmd)
    {
        usage(argv[0]);
        return 1;
    }

    // Check if folder exists
    sprintf(buf, "/sys/class/leds/%s", led);
    stat(buf, &st);
    isdir = S_ISDIR(st.st_mode);
    if(!isdir){
        printf("Folder %s not found!\n", buf);
        return 1;
    }

    // Check what command we have received
    if(strcmp(cmd, "on") == 0)
    {
        led_on(led);
    }
    else if(strcmp(cmd, "off") == 0)
    {
        led_off(led);
    }
    else if(strcmp(cmd, "flash") == 0)
    {
        led_flash(led);
    }
    else
    {
        // Unrecognised command
        usage(argv[0]);
        return 1;
    }

    return 0;
}