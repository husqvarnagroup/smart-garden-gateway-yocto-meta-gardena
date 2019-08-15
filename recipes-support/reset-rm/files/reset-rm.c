#include <stdlib.h>
#include <stdio.h>
#include <errno.h>
#include <unistd.h>
#include <limits.h>

#include <cyaml/cyaml.h>
#include <gpiod.h>

struct config {
    char *pin;
};

static const cyaml_schema_field_t top_mapping_schema[] = {
    CYAML_FIELD_STRING_PTR("pin", CYAML_FLAG_POINTER, struct config, pin, 0, CYAML_UNLIMITED),
    CYAML_FIELD_END
};

static const cyaml_schema_value_t top_schema = {
    CYAML_VALUE_MAPPING(CYAML_FLAG_POINTER, struct config, top_mapping_schema),
};

static const cyaml_config_t cyaml_config = {
    .log_level = CYAML_LOG_WARNING,
    .log_fn = cyaml_log,
    .mem_fn = cyaml_mem,
};

static const struct gpiod_line_request_config line_config = {
    .consumer = "reset-rm",
    .request_type = GPIOD_LINE_REQUEST_DIRECTION_OUTPUT,
    .flags = 0,
};

int main(void) {
    int rc;
    int ret = EXIT_FAILURE;
    cyaml_err_t cyerr;
    struct config *cfg;
    char chipname[PATH_MAX];
    unsigned int offset;
    struct gpiod_chip *chip;
    struct gpiod_line *line;

    cyerr = cyaml_load_file("/etc/reset-rm.cfg", &cyaml_config, &top_schema,
        (cyaml_data_t**)&cfg, NULL);
    if (cyerr != CYAML_OK) {
        fprintf(stderr, "ERROR: %s\n", cyaml_strerror(cyerr));
        goto out_ret;
    }

    rc = gpiod_ctxless_find_line(cfg->pin, chipname, sizeof(chipname), &offset);
    if (rc < 0 || rc > 1) {
        fprintf(stderr, "unknown error during find_line: %d. errno=%d\n", rc, errno);
        goto out_free_cfg;
    }
    if (rc == 0) {
        fprintf(stderr, "pin %s not found: %d\n", cfg->pin, errno);
        goto out_free_cfg;
    }
    if (rc != 1) {
        fprintf(stderr, "BUG: %d\n", errno);
        goto out_free_cfg;
    }

    chip = gpiod_chip_open_by_name(chipname);
    if (!chip) {
        fprintf(stderr, "chip %s not found: %d\n", chipname, errno);
        goto out_free_cfg;
    }

    line = gpiod_chip_get_line(chip, offset);
    if (!line) {
        fprintf(stderr, "line %u not found: %d\n", offset, errno);
        goto out_close_chip;
    }

    rc = gpiod_line_request(line, &line_config, 0);
    if (rc) {
        fprintf(stderr, "can't request line: %d\n", errno);
        goto out_close_chip;
    }

    usleep(100000);

    rc = gpiod_line_set_value(line, 1);
    if (rc) {
        fprintf(stderr, "can't set gpio value to 1: %d\n", errno);
        goto out_release_line;
    }

    ret = EXIT_SUCCESS;

out_release_line:
    gpiod_line_release(line);
out_close_chip:
    gpiod_chip_close(chip);
out_free_cfg:
    cyaml_free(&cyaml_config, &top_schema, cfg, 0);
out_ret:
    return ret;
}

