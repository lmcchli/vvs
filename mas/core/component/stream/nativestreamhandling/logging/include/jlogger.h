#ifndef INCLUDED_JNI_LOGGER
#define INCLUDED_JNI_LOGGER

#include "jni.h"

class JLogger
{
public:
    static jboolean jniLogIsEnabled(JNIEnv* const, const char*, jfieldID);
    static void jniLogFatal(JNIEnv* const, const char*, char const*, ...);
    static void jniLogError(JNIEnv* const, const char*, char const*, ...);
    static void jniLogWarn(JNIEnv* const, const char*, char const*, ...);
    static void jniLogInfo(JNIEnv* const, const char*, char const*, ...);
    static void jniLogDebug(JNIEnv* const, const char*, char const*, ...);
    static void jniLogTrace(JNIEnv* const, const char*, char const*, ...);
};

#endif /* INCLUDED_JNI_LOGGER */
