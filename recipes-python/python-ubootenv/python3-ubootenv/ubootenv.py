# coding=utf-8

# Copyright (c) 2021 GARDENA GmbH

from _ubootenv import ffi
from _ubootenv.lib import libuboot_read_config, \
    libuboot_initialize, \
    libuboot_exit, \
    libuboot_open, \
    libuboot_close, \
    libuboot_get_env


class UBootEnv:
    """ubootenv wrapper class usable standalone and with contextmanager.
    After open() the ubootenv is blocked for other processes until close() was called!
    """
    def __init__(self, config_file: str):
        self.config_file = config_file

    def open(self):
        """Open the ubootenv context."""
        self.__pointer_to_ctx = ffi.new("void **")
        if libuboot_initialize(ffi.cast("struct uboot_ctx **", self.__pointer_to_ctx), ffi.NULL) < 0:
            raise RuntimeError
        self.__ctx = self.__pointer_to_ctx[0]
        if libuboot_read_config(self.__ctx, self.config_file.encode()) < 0:
            raise RuntimeError
        if libuboot_open(self.__ctx) < 0:
            raise RuntimeError

    def read(self, variable: str) -> bytes:
        """Read ubootenv variable."""
        handle = libuboot_get_env(self.__ctx, variable.encode())
        if handle:
            return ffi.string(handle)

    def close(self):
        """Close the ubootenv context.
        This is important! Opened contexts will block other accessing processes.
        """

        # Release the environment, other access is possible again from here on
        libuboot_close(self.__ctx)

        # Release all resources and exit the library
        libuboot_exit(self.__ctx)

    def __enter__(self):
        self.open()
        return self

    def __exit__(self, exc_type, exc_value, exc_traceback):
        self.close()


if __name__ == "__main__":
    with UBootEnv("/etc/fw_env.config") as uboot_handle:
        uboot_handle.read("gatewayid")
