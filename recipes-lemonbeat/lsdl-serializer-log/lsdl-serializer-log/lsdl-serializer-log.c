#define _GNU_SOURCE // What we do here is non-standard-compliant
#include <dlfcn.h>
#include <limits.h>
#include <stddef.h>
#include <stdio.h>
#include <string.h>
#include <sys/syscall.h>
#include <sys/types.h>
#include <syslog.h>
#include <unistd.h>

static unsigned long (*real_compressXML)(unsigned short port,
                                         unsigned char *xmlBuf,
                                         unsigned long xmlBufLen,
                                         unsigned char *exiBuf,
                                         unsigned long exiBufLen,
                                         unsigned long bitOffset) = NULL;

static unsigned long (*real_decompressEXI)(unsigned short port,
                                           unsigned char *exiBuf,
                                           unsigned long exiBufLen,
                                           unsigned char *xmlBuf,
                                           unsigned long xmlBufLen,
                                           unsigned long bitOffset) = NULL;

static const char *(*real_lsdlconv_getVersion)(void) = NULL;

extern char *__progname;

static void reportXML(const char *function, unsigned short port,
                      unsigned char *buf, unsigned long len) {
  syslog(LOG_INFO, "pid=%lu;tid=%d;port=%u;function=%s: %.*s",
         syscall(__NR_gettid), getpid(), port, function, (int)len, buf);
}

static void reportEXI(const char *function, unsigned short port,
                      unsigned char *buf, unsigned long len) {
  char hex_buffer[len * 2 + 1];
  memset(hex_buffer, 0, sizeof(hex_buffer));
  for (unsigned long i = 0; i < len; i++) {
    sprintf(&hex_buffer[i * 2], "%02X", buf[i]);
  }
  syslog(LOG_INFO, "pid=%lu;tid=%d;port=%u;function=%s: %.*s",
         syscall(__NR_gettid), getpid(), port, function,
         (int)sizeof(hex_buffer), hex_buffer);
}

unsigned long compressXML(unsigned short port, unsigned char *xmlBuf,
                          unsigned long xmlBufLen, unsigned char *exiBuf,
                          unsigned long exiBufLen, unsigned long bitOffset) {
  if (!real_compressXML) {
    real_compressXML = dlsym(RTLD_NEXT, "compressXML");
  }
  if (real_compressXML) {
    reportXML(__func__, port, xmlBuf, xmlBufLen);
    const unsigned long result =
        real_compressXML(port, xmlBuf, xmlBufLen, exiBuf, exiBufLen, bitOffset);
    reportEXI(__func__, port, exiBuf, result);
    return result;
  }
  return 0;
}

unsigned long decompressEXI(unsigned short port, unsigned char *exiBuf,
                            unsigned long exiBufLen, unsigned char *xmlBuf,
                            unsigned long xmlBufLen, unsigned long bitOffset) {
  if (!real_decompressEXI) {
    real_decompressEXI = dlsym(RTLD_NEXT, "decompressEXI");
  }
  if (real_decompressEXI) {
    reportEXI(__func__, port, exiBuf, exiBufLen);
    const unsigned long result = real_decompressEXI(
        port, exiBuf, exiBufLen, xmlBuf, xmlBufLen, bitOffset);
    reportXML(__func__, port, xmlBuf, result);
    return result;
  }
  return 0;
}

const char *lsdlconv_getVersion(void) {
  if (!real_lsdlconv_getVersion) {
    real_lsdlconv_getVersion = dlsym(RTLD_NEXT, "lsdlconv_getVersion");
  }
  if (real_lsdlconv_getVersion) {
    const char *const result = real_lsdlconv_getVersion();
    return result;
  }
  return 0;
}
