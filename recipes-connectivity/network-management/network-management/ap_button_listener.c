#define _GNU_SOURCE
#include <stdio.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <string.h>
#include <stdlib.h>
#include <time.h>
#include <stdbool.h>
#include <sys/wait.h>
#include <libevdev/libevdev.h>

#define EVENT_FILE "/dev/input/event0"

static bool timespec_lt(struct timespec *a, struct timespec *b) {
    if (a->tv_sec == b->tv_sec)
        return a->tv_nsec < b->tv_nsec;
    else
        return a->tv_sec < b->tv_sec;
}

static int start_ap(void) {
    pid_t pid;
    int status;

    pid = vfork();
    if (pid < 0) {
        perror("vfork");
        return -1;
    }

    if (pid == 0) {
        static const char *args[] = {
            "/usr/bin/network_management",
            "start_ap",
            NULL
        };
        static const char *env[] = {
            NULL
        };

        execve(args[0], (char**)args, (char**)env);
        perror("execve");
        exit(1);
    }

    do {
        pid = waitpid(pid, &status, 0);
    } while (pid == -1 && errno == EINTR);

    if (pid < 0 && errno != ECHILD) {
        perror("waitpid");
        return -1;
    }

    if (!WIFEXITED(status) || WEXITSTATUS(status) != 0) {
        fprintf(stderr, "Command exited with error status=%d\n", status);
        return -1;
    }

    return 0;
}

int main(void) {
    int fd;
    int rc;
    int ret = -1;
    struct libevdev *dev = NULL;

    fd = open(EVENT_FILE, O_RDONLY);
    if (fd < 0) {
        fprintf(stderr, "can't open "EVENT_FILE": %d\n", fd);
        return -1;
    }

    rc = libevdev_new_from_fd(fd, &dev);
    if (rc < 0) {
        fprintf(stderr, "Failed to init libevdev: %d\n", rc);
        goto ret_close_fd;
    }

    rc = libevdev_set_clock_id(dev, CLOCK_MONOTONIC);
    if (rc) {
        fprintf(stderr, "Failed to set clock id: %d\n", rc);
        goto ret_free_dev;
    }

    printf("Input device name: \"%s\"\n", libevdev_get_name(dev));
    printf("Input device ID: bus %#x vendor %#x product %#x\n",
        libevdev_get_id_bustype(dev),
        libevdev_get_id_vendor(dev),
        libevdev_get_id_product(dev));

    struct timespec mints = { 0 };
    do {
        struct input_event ev;
        struct timespec evts;

        rc = libevdev_next_event(dev, LIBEVDEV_READ_FLAG_NORMAL|LIBEVDEV_READ_FLAG_BLOCKING, &ev);
        if (rc != LIBEVDEV_READ_STATUS_SUCCESS) {
            continue;
        }
        TIMEVAL_TO_TIMESPEC(&ev.time, &evts);

        if (timespec_lt(&evts, &mints)) {
            continue;
        }

        if (ev.type == EV_KEY && ev.code == KEY_PROG1 && ev.value == 0) {
            printf("Button pressed\n");
            start_ap();

            rc = clock_gettime(CLOCK_MONOTONIC, &mints);
            if (rc) {
                fprintf(stderr, "can't get current time: %d\n", rc);
                goto ret_free_dev;
            }
        }

    } while (rc == LIBEVDEV_READ_STATUS_SYNC || rc == LIBEVDEV_READ_STATUS_SUCCESS || rc == -EAGAIN);

    if (rc) {
        fprintf(stderr, "libevdev_next_event error: %d\n", rc);
        goto ret_free_dev;
    }

    ret = 0;

ret_free_dev:
    libevdev_free(dev);
ret_close_fd:
    close(fd);

    return ret;
}
