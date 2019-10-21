#include <stdlib.h>
#include <stdio.h>
#include <errno.h>
#include <unistd.h>
#include <limits.h>

#include <cyaml/cyaml.h>
#include <gpiod.h>

struct config {
    char *chip_name;
    unsigned int pin_offset;
};

static const cyaml_schema_field_t top_mapping_schema[] = {
    CYAML_FIELD_STRING_PTR("chip_name", CYAML_FLAG_POINTER, struct config, chip_name, 0, CYAML_UNLIMITED),
    CYAML_FIELD_UINT("pin_offset", CYAML_FLAG_DEFAULT, struct config, pin_offset),
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
    struct gpiod_chip *chip;
    struct gpiod_line *line;

    cyerr = cyaml_load_file("/etc/reset-rm.cfg", &cyaml_config, &top_schema,
        (cyaml_data_t**)&cfg, NULL);
    if (cyerr != CYAML_OK) {
        fprintf(stderr, "ERROR: %s\n", cyaml_strerror(cyerr));
        goto out_ret;
    }

    chip = gpiod_chip_open_by_name(cfg->chip_name);
    if (!chip) {
        fprintf(stderr, "chip %s not found: %d\n", cfg->chip_name, errno);
        goto out_free_cfg;
    }

    line = gpiod_chip_get_line(chip, cfg->pin_offset);
    if (!line) {
        fprintf(stderr, "line %u not found: %d\n", cfg->pin_offset, errno);
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

