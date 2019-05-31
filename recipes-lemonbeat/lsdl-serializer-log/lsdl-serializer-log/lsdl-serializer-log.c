#define _GNU_SOURCE // What we do here (using RTLD_NEXT) is not
                    // standard-compliant
#include <dlfcn.h>
#include <sys/syscall.h>
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

typedef enum {
  direction_send,
  direction_receive,
} direction_t;

static void reportXML(const direction_t direction, unsigned short port,
                      unsigned char *xmlBuf, unsigned long xmlLen,
                      unsigned long exiLen) {
  syslog(LOG_INFO, "%s %.*s (thread_id=%lu;port=%u;xml_len=%lu;exi_len=%lu)",
         direction == direction_send ? "-->" : "<--", (int)xmlLen, xmlBuf,
         syscall(__NR_gettid), port, xmlLen, exiLen);
}

unsigned long compressXML(unsigned short port, unsigned char *xmlBuf,
                          unsigned long xmlBufLen, unsigned char *exiBuf,
                          unsigned long exiBufLen, unsigned long bitOffset) {
  if (!real_compressXML) {
    real_compressXML = dlsym(RTLD_NEXT, "compressXML");
  }
  if (real_compressXML) {
    const unsigned long result =
        real_compressXML(port, xmlBuf, xmlBufLen, exiBuf, exiBufLen, bitOffset);
    reportXML(direction_send, port, xmlBuf, xmlBufLen, result);
    return result;
  }
  syslog(LOG_ERR, "Failed to call the real compressXML function");
  return 0;
}

unsigned long decompressEXI(unsigned short port, unsigned char *exiBuf,
                            unsigned long exiBufLen, unsigned char *xmlBuf,
                            unsigned long xmlBufLen, unsigned long bitOffset) {
  if (!real_decompressEXI) {
    real_decompressEXI = dlsym(RTLD_NEXT, "decompressEXI");
  }
  if (real_decompressEXI) {
    const unsigned long result = real_decompressEXI(
        port, exiBuf, exiBufLen, xmlBuf, xmlBufLen, bitOffset);
    reportXML(direction_receive, port, xmlBuf, result, exiBufLen);
    return result;
  }
  syslog(LOG_ERR, "Failed to call the real decompressEXI function");
  return 0;
}

const char *lsdlconv_getVersion(void) {
  if (!real_lsdlconv_getVersion) {
    real_lsdlconv_getVersion = dlsym(RTLD_NEXT, "lsdlconv_getVersion");
  }
  if (real_lsdlconv_getVersion) {
    const char *const result = real_lsdlconv_getVersion();
    syslog(LOG_INFO, "lsdl-serializer version: %s", result);
    return result;
  }
  syslog(LOG_ERR, "Failed to call the real lsdlconv_getVersion function");
  return 0;
}
