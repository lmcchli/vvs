#include <stdexcept>
#include <iostream>

#include "jnicontroltoken.h"
#include "jniutil.h"
#include "jlogger.h"

using namespace std;

static const char* CLASSNAME = "masjni.jniadapter.JNIControlToken";

const char* JNIControlToken::GET_TOKEN_DIGIT_METHOD = "getTokenDigit";
const char* JNIControlToken::GET_TOKEN_DIGIT_METHOD_SIGNATURE = "()I";
jmethodID JNIControlToken::getTokenDigitMID;

const char* JNIControlToken::GET_VOLUME_METHOD = "getVolume";
const char* JNIControlToken::GET_VOLUME_METHOD_SIGNATURE = "()I";
jmethodID JNIControlToken::getVolumeMID;

const char* JNIControlToken::GET_DURATION_METHOD = "getDuration";
const char* JNIControlToken::GET_DURATION_METHOD_SIGNATURE = "()I";
jmethodID JNIControlToken::getDurationMID;

const char* JNIControlToken::CONTROL_TOKEN_CLASSNAME = "com/mobeon/masp/stream/ControlToken";

// Since the global references are deleted right away
// it is no longer required to have an unload method
void JNI_ControlTokenOnLoad(void *reserved)
{
    JNIControlToken::ControlTokenOnLoad(reserved);
}

void JNIControlToken::ControlTokenOnLoad(void *reserved)
{
    JNIEnv* env = NULL;
    (void) JNIUtil::getJavaEnvironment((void**) &env, true);

    // ControlToken - MID's
    jclass controlTokenClass = (jclass) env->NewGlobalRef(env->FindClass(CONTROL_TOKEN_CLASSNAME));
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load class %s", CONTROL_TOKEN_CLASSNAME);
        abort();
    }

    getTokenDigitMID = env->GetMethodID(controlTokenClass, GET_TOKEN_DIGIT_METHOD, GET_TOKEN_DIGIT_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_TOKEN_DIGIT_METHOD,
                GET_TOKEN_DIGIT_METHOD_SIGNATURE, CONTROL_TOKEN_CLASSNAME);
        abort();
    }

    getVolumeMID = env->GetMethodID(controlTokenClass, GET_VOLUME_METHOD, GET_VOLUME_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_VOLUME_METHOD,
                GET_VOLUME_METHOD_SIGNATURE, CONTROL_TOKEN_CLASSNAME);
        abort();
    }

    getDurationMID = env->GetMethodID(controlTokenClass, GET_DURATION_METHOD, GET_DURATION_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_DURATION_METHOD,
                GET_DURATION_METHOD_SIGNATURE, CONTROL_TOKEN_CLASSNAME);
        abort();
    }

    // Do not need the global ref - the mid's are sufficient
    env->DeleteGlobalRef(controlTokenClass);
}
