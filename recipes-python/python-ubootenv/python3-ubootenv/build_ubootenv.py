from cffi import FFI

ffibuilder = FFI()

ffibuilder.cdef("""
    struct uboot_ctx;

    int libuboot_read_config(struct uboot_ctx *ctx, const char *config);
    int libuboot_initialize(struct uboot_ctx **out, struct uboot_env_device *envdevs);
    void libuboot_exit(struct uboot_ctx *ctx);
    int libuboot_open(struct uboot_ctx *ctx);
    void libuboot_close(struct uboot_ctx *ctx);
    char *libuboot_get_env(struct uboot_ctx *ctx, const char *varname);
 """)

ffibuilder.set_source("_ubootenv",
                      """
                       #include "libuboot.h"
                      """,
                      # library_dirs=[ 'my/custom/library/directory'],
                      # include_dirs=[ 'my/custom/include/directory' ],
                      libraries=['ubootenv']
                      )

if __name__ == "__main__":
    ffibuilder.compile(verbose=True)
