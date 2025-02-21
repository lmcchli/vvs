#include <stdexcept>
#include <iostream>

#include "jnirecordingproperties.h"
#include "jniutil.h"
#include "jlogger.h"

using namespace std;

static const char* CLASSNAME = "masjni.jniadapter.JNIRecordingProperties";

const char* JNIRecordingProperties::IS_SILENCE_DETECTION_FOR_START_METHOD = "isSilenceDetectionForStart";
const char* JNIRecordingProperties::IS_SILENCE_DETECTION_FOR_START_METHOD_SIGNATURE = "()Z";
jmethodID JNIRecordingProperties::isSilenceDetectionForStartMID;

const char* JNIRecordingProperties::IS_SILENCE_DETECTION_FOR_STOP_METHOD = "isSilenceDetectionForStop";
const char* JNIRecordingProperties::IS_SILENCE_DETECTION_FOR_STOP_METHOD_SIGNATURE = "()Z";
jmethodID JNIRecordingProperties::isSilenceDetectionForStopMID;

const char* JNIRecordingProperties::GET_MAX_WAIT_BEFORE_RECORD_METHOD = "getMaxWaitBeforeRecord";
const char* JNIRecordingProperties::GET_MAX_WAIT_BEFORE_RECORD_METHOD_SIGNATURE = "()I";
jmethodID JNIRecordingProperties::maxWaitBeforeRecordMID;

const char* JNIRecordingProperties::GET_MAX_RECORDING_DURATION_METHOD = "getMaxRecordingDuration";
const char* JNIRecordingProperties::GET_MAX_RECORDING_DURATION_METHOD_SIGNATURE = "()I";
jmethodID JNIRecordingProperties::maxRecordingDurationMID;

const char* JNIRecordingProperties::GET_MIN_RECORDING_DURATION_METHOD = "getMinRecordingDuration";
const char* JNIRecordingProperties::GET_MIN_RECORDING_DURATION_METHOD_SIGNATURE = "()I";
jmethodID JNIRecordingProperties::minRecordingDurationMID;

const char* JNIRecordingProperties::GET_MAX_SILENCE_METHOD = "getMaxSilence";
const char* JNIRecordingProperties::GET_MAX_SILENCE_METHOD_SIGNATURE = "()I";
jmethodID JNIRecordingProperties::maxSilenceMID;

const char* JNIRecordingProperties::GET_TIMEOUT_METHOD = "getTimeout";
const char* JNIRecordingProperties::GET_TIMEOUT_METHOD_SIGNATURE = "()I";
jmethodID JNIRecordingProperties::timeoutMID;

const char* JNIRecordingProperties::RECORDING_PROPERTIES_CLASSNAME = "com/mobeon/masp/stream/RecordingProperties";

// Since the global references are deleted right away
// it is no longer required to have an unload method
void JNI_RecordingPropertiesOnLoad(void *reserved)
{
    JNIRecordingProperties::ControlTokenOnLoad(reserved);
}

void JNIRecordingProperties::ControlTokenOnLoad(void *reserved)
{
    JNIEnv* env = NULL;
    (void) JNIUtil::getJavaEnvironment((void**) &env, true);

    // ControlToken - MID's
    jclass recordigPropertiesClass = (jclass) env->NewGlobalRef(env->FindClass(RECORDING_PROPERTIES_CLASSNAME));
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load class %s", RECORDING_PROPERTIES_CLASSNAME);
        abort();
    }

    isSilenceDetectionForStartMID = env->GetMethodID(recordigPropertiesClass, IS_SILENCE_DETECTION_FOR_START_METHOD,
            IS_SILENCE_DETECTION_FOR_START_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s",
                IS_SILENCE_DETECTION_FOR_START_METHOD, IS_SILENCE_DETECTION_FOR_START_METHOD_SIGNATURE,
                RECORDING_PROPERTIES_CLASSNAME);
        abort();
    }

    isSilenceDetectionForStopMID = env->GetMethodID(recordigPropertiesClass, IS_SILENCE_DETECTION_FOR_STOP_METHOD,
            IS_SILENCE_DETECTION_FOR_STOP_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s",
                IS_SILENCE_DETECTION_FOR_STOP_METHOD, IS_SILENCE_DETECTION_FOR_STOP_METHOD_SIGNATURE,
                RECORDING_PROPERTIES_CLASSNAME);
        abort();
    }

    maxWaitBeforeRecordMID = env->GetMethodID(recordigPropertiesClass, GET_MAX_WAIT_BEFORE_RECORD_METHOD,
            GET_MAX_WAIT_BEFORE_RECORD_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s",
                GET_MAX_WAIT_BEFORE_RECORD_METHOD, GET_MAX_WAIT_BEFORE_RECORD_METHOD_SIGNATURE,
                RECORDING_PROPERTIES_CLASSNAME);
        abort();
    }

    maxRecordingDurationMID = env->GetMethodID(recordigPropertiesClass, GET_MAX_RECORDING_DURATION_METHOD,
            GET_MAX_RECORDING_DURATION_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s",
                GET_MAX_RECORDING_DURATION_METHOD, GET_MAX_RECORDING_DURATION_METHOD_SIGNATURE,
                RECORDING_PROPERTIES_CLASSNAME);
        abort();
    }

    minRecordingDurationMID = env->GetMethodID(recordigPropertiesClass, GET_MIN_RECORDING_DURATION_METHOD,
            GET_MIN_RECORDING_DURATION_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s",
                GET_MIN_RECORDING_DURATION_METHOD, GET_MIN_RECORDING_DURATION_METHOD_SIGNATURE,
                RECORDING_PROPERTIES_CLASSNAME);
        abort();
    }

    maxSilenceMID = env->GetMethodID(recordigPropertiesClass, GET_MAX_SILENCE_METHOD, GET_MAX_SILENCE_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_MAX_SILENCE_METHOD,
                GET_MAX_SILENCE_METHOD_SIGNATURE, RECORDING_PROPERTIES_CLASSNAME);
        abort();
    }

    timeoutMID = env->GetMethodID(recordigPropertiesClass, GET_TIMEOUT_METHOD, GET_TIMEOUT_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_TIMEOUT_METHOD,
                GET_TIMEOUT_METHOD_SIGNATURE, RECORDING_PROPERTIES_CLASSNAME);
        abort();
    }

    // Do not need the global ref - the mid's are sufficient
    env->DeleteGlobalRef(recordigPropertiesClass);
}
