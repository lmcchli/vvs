#include <stdexcept>
#include <iostream>

#include "jnistreamconfiguration.h"
#include "jniutil.h"
#include "jlogger.h"

using namespace std;

static const char* CLASSNAME = "masjni.jniadapter.JNIStreamConfiguration";

const char* JNIStreamConfiguration::GET_THREADPOOL_SIZE_METHOD = "getThreadPoolSize";
const char* JNIStreamConfiguration::GET_THREADPOOL_SIZE_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamConfiguration::getThreadPoolSizeMID;

const char* JNIStreamConfiguration::GET_PACKETPEND_TIMEOUT_METHOD = "getPacketPendTimeout";
const char* JNIStreamConfiguration::GET_PACKETPEND_TIMEOUT_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamConfiguration::getPacketEndTimeoutMID;

const char* JNIStreamConfiguration::GET_SEND_PACKETS_AHEAD_METHOD = "getSendPacketsAhead";
const char* JNIStreamConfiguration::GET_SEND_PACKETS_AHEAD_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamConfiguration::getSendPacketsAheadMID;

const char* JNIStreamConfiguration::GET_EXPIRE_TIMEOUT_METHOD = "getExpireTimeout";
const char* JNIStreamConfiguration::GET_EXPIRE_TIMEOUT_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamConfiguration::getExpireTimeoutMID;

const char* JNIStreamConfiguration::GET_MTU_METHOD = "getMaximumTransmissionUnit";
const char* JNIStreamConfiguration::GET_MTU_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamConfiguration::getMtuMID;

const char* JNIStreamConfiguration::GET_ABANDONED_STREAM_TIMEOUT_METHOD = "getAbandonedStreamDetectedTimeout";
const char* JNIStreamConfiguration::GET_ABANDONED_STREAM_TIMEOUT_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamConfiguration::getAbandonedStreamTimeoutMID;

const char* JNIStreamConfiguration::GET_SENDERS_CONTROL_FRACTION_METHOD = "getSendersControlFraction";
const char* JNIStreamConfiguration::GET_SENDERS_CONTROL_FRACTION_METHOD_SIGNATURE = "()F";
jmethodID JNIStreamConfiguration::getSendersControlFractionMID;

const char* JNIStreamConfiguration::GET_AUDIO_SKIP_METHOD = "getAudioSkip";
const char* JNIStreamConfiguration::GET_AUDIO_SKIP_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamConfiguration::getAudioSkipMID;

const char* JNIStreamConfiguration::GET_SKEW_METHOD = "getSkew";
const char* JNIStreamConfiguration::GET_SKEW_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamConfiguration::getSkewMID;

const char* JNIStreamConfiguration::GET_SKEW_INTREP_METHOD = "getSkewMethodIntRep";
const char* JNIStreamConfiguration::GET_SKEW_INTREP_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamConfiguration::getSkewIntRepMID;

const char* JNIStreamConfiguration::GET_AUDIO_REPLACE_WITH_SILENCE_METHOD = "getAudioReplaceWithSilence";
const char* JNIStreamConfiguration::GET_AUDIO_REPLACE_WITH_SILENCE_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamConfiguration::getAudioReplaceWithSilenceMID;

const char* JNIStreamConfiguration::GET_THREADPOOL_MAX_WAIT_TIME_METHOD = "getThreadPoolMaxWaitTime";
const char* JNIStreamConfiguration::GET_THREADPOOL_MAX_WAIT_TIME_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamConfiguration::getThreadPoolMaxWithTimeMID;

const char* JNIStreamConfiguration::GET_MAX_WAIT_FOR_IFRAME_METHOD = "getMaxWaitForIFrameTimeout";
const char* JNIStreamConfiguration::GET_MAX_WAIT_FOR_IFRAME_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamConfiguration::getMaxWaitForIframeMID;

const char* JNIStreamConfiguration::IS_DISPATCH_DTMF_ON_KEY_DOWN_METHOD = "isDispatchDTMFOnKeyDown";
const char* JNIStreamConfiguration::IS_DISPATCH_DTMF_ON_KEY_DOWN_METHOD_SIGNATURE = "()Z";
jmethodID JNIStreamConfiguration::isDispatchDtmfOnKeyDownMID;

const char* JNIStreamConfiguration::IS_USE_POOL_FOR_SESSIONS_METHOD = "isUsePoolForRTPSessions";
const char* JNIStreamConfiguration::IS_USE_POOL_FOR_SESSIONS_METHOD_SIGNATURE = "()Z";
jmethodID JNIStreamConfiguration::isUsePoolForSessionsMID;

const char* JNIStreamConfiguration::GET_LOCAL_HOST_METHOD = "getLocalHostName";
const char* JNIStreamConfiguration::GET_LOCAL_HOST_METHOD_SIGNATURE = "()Ljava/lang/String;";
jmethodID JNIStreamConfiguration::getLocalHostMID;

const char* JNIStreamConfiguration::GET_MOV_FILE_VERSION_METHOD = "getMovFileVersion";
const char* JNIStreamConfiguration::GET_MOV_FILE_VERSION_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamConfiguration::getMovFileVersionMID;

const char* JNIStreamConfiguration::GET_SILENCE_DETECTION_MODE_METHOD = "getSilenceDetectionMode";
const char* JNIStreamConfiguration::GET_SILENCE_DETECTION_MODE_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamConfiguration::getSilenceDetectionModeMID;

const char* JNIStreamConfiguration::GET_THRESHOLD_METHOD = "getSilenceThreshold";
const char* JNIStreamConfiguration::GET_THRESHOLD_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamConfiguration::getThresholdMID;

const char* JNIStreamConfiguration::GET_INITIAL_SILENCE_FRAMES_METHOD = "getInitialSilenceFrames";
const char* JNIStreamConfiguration::GET_INITIAL_SILENCE_FRAMES_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamConfiguration::getInitialSilenceFramesMID;

const char* JNIStreamConfiguration::GET_DETECTION_FRAMES_METHOD = "getDetectionFrames";
const char* JNIStreamConfiguration::GET_DETECTION_FRAMES_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamConfiguration::getDetectionFramesMID;

const char* JNIStreamConfiguration::GET_SILENCE_DEADBAND_METHOD = "getSilenceDeadband";
const char* JNIStreamConfiguration::GET_SILENCE_DEADBAND_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamConfiguration::getSilenceDeadbandMID;

const char* JNIStreamConfiguration::GET_SIGNAL_DEADBAND_METHOD = "getSignalDeadband";
const char* JNIStreamConfiguration::GET_SIGNAL_DEADBAND_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamConfiguration::getSignalDeadbandMID;

const char* JNIStreamConfiguration::GET_SILENCE_DETECTION_DEBUG_LEVEL_METHOD = "getSilenceDetectionDebugLevel";
const char* JNIStreamConfiguration::GET_SILENCE_DETECTION_DEBUG_LEVEL_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamConfiguration::getSilenceDetectionDebugLevelMID;

const char* JNIStreamConfiguration::STREAM_CONFIGURATION_CLASSNAME = "com/mobeon/masp/stream/StreamConfiguration";

// Since the global references are deleted right away
// it is no longer required to have an unload method
void JNI_StreamConfigurationOnLoad(void *reserved)
{
    JNIStreamConfiguration::StreamConfigurationOnLoad(reserved);
}

void JNIStreamConfiguration::StreamConfigurationOnLoad(void *reserved)
{
    JNIEnv* env = NULL;
    (void) JNIUtil::getJavaEnvironment((void**) &env, true);

    // ControlToken - MID's
    jclass streamConfigurationClass = (jclass) env->NewGlobalRef(env->FindClass(STREAM_CONFIGURATION_CLASSNAME));
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load class %s", STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getThreadPoolSizeMID = env->GetMethodID(streamConfigurationClass, GET_THREADPOOL_SIZE_METHOD,
            GET_THREADPOOL_SIZE_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_THREADPOOL_SIZE_METHOD,
                GET_THREADPOOL_SIZE_METHOD_SIGNATURE, STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getPacketEndTimeoutMID = env->GetMethodID(streamConfigurationClass, GET_PACKETPEND_TIMEOUT_METHOD,
            GET_PACKETPEND_TIMEOUT_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s",
                GET_PACKETPEND_TIMEOUT_METHOD, GET_PACKETPEND_TIMEOUT_METHOD_SIGNATURE, STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getSendPacketsAheadMID = env->GetMethodID(streamConfigurationClass, GET_SEND_PACKETS_AHEAD_METHOD,
            GET_SEND_PACKETS_AHEAD_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s",
                GET_SEND_PACKETS_AHEAD_METHOD, GET_SEND_PACKETS_AHEAD_METHOD_SIGNATURE, STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getExpireTimeoutMID = env->GetMethodID(streamConfigurationClass, GET_EXPIRE_TIMEOUT_METHOD,
            GET_EXPIRE_TIMEOUT_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_EXPIRE_TIMEOUT_METHOD,
                GET_EXPIRE_TIMEOUT_METHOD_SIGNATURE, STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getMtuMID = env->GetMethodID(streamConfigurationClass, GET_MTU_METHOD, GET_MTU_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_MTU_METHOD,
                GET_MTU_METHOD_SIGNATURE, STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getAbandonedStreamTimeoutMID = env->GetMethodID(streamConfigurationClass, GET_ABANDONED_STREAM_TIMEOUT_METHOD,
            GET_ABANDONED_STREAM_TIMEOUT_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s",
                GET_ABANDONED_STREAM_TIMEOUT_METHOD, GET_ABANDONED_STREAM_TIMEOUT_METHOD_SIGNATURE,
                STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getSendersControlFractionMID = env->GetMethodID(streamConfigurationClass, GET_SENDERS_CONTROL_FRACTION_METHOD,
            GET_SENDERS_CONTROL_FRACTION_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s",
                GET_SENDERS_CONTROL_FRACTION_METHOD, GET_SENDERS_CONTROL_FRACTION_METHOD_SIGNATURE,
                STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getAudioSkipMID = env->GetMethodID(streamConfigurationClass, GET_AUDIO_SKIP_METHOD,
            GET_AUDIO_SKIP_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_AUDIO_SKIP_METHOD,
                GET_AUDIO_SKIP_METHOD_SIGNATURE, STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getSkewMID = env->GetMethodID(streamConfigurationClass, GET_SKEW_METHOD, GET_SKEW_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_SKEW_METHOD,
                GET_SKEW_METHOD_SIGNATURE, STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getSkewIntRepMID = env->GetMethodID(streamConfigurationClass, GET_SKEW_INTREP_METHOD,
            GET_SKEW_INTREP_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_SKEW_INTREP_METHOD,
                GET_SKEW_INTREP_METHOD_SIGNATURE, STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getAudioReplaceWithSilenceMID = env->GetMethodID(streamConfigurationClass, GET_AUDIO_REPLACE_WITH_SILENCE_METHOD,
            GET_AUDIO_REPLACE_WITH_SILENCE_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s",
                GET_AUDIO_REPLACE_WITH_SILENCE_METHOD, GET_AUDIO_REPLACE_WITH_SILENCE_METHOD_SIGNATURE,
                STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getThreadPoolMaxWithTimeMID = env->GetMethodID(streamConfigurationClass, GET_THREADPOOL_MAX_WAIT_TIME_METHOD,
            GET_THREADPOOL_MAX_WAIT_TIME_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s",
                GET_THREADPOOL_MAX_WAIT_TIME_METHOD, GET_THREADPOOL_MAX_WAIT_TIME_METHOD_SIGNATURE,
                STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getMaxWaitForIframeMID = env->GetMethodID(streamConfigurationClass, GET_MAX_WAIT_FOR_IFRAME_METHOD,
            GET_MAX_WAIT_FOR_IFRAME_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s",
                GET_MAX_WAIT_FOR_IFRAME_METHOD, GET_MAX_WAIT_FOR_IFRAME_METHOD_SIGNATURE,
                STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    isDispatchDtmfOnKeyDownMID = env->GetMethodID(streamConfigurationClass, IS_DISPATCH_DTMF_ON_KEY_DOWN_METHOD,
            IS_DISPATCH_DTMF_ON_KEY_DOWN_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s",
                IS_DISPATCH_DTMF_ON_KEY_DOWN_METHOD, IS_DISPATCH_DTMF_ON_KEY_DOWN_METHOD_SIGNATURE,
                STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    isUsePoolForSessionsMID = env->GetMethodID(streamConfigurationClass, IS_USE_POOL_FOR_SESSIONS_METHOD,
            IS_USE_POOL_FOR_SESSIONS_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s",
                IS_USE_POOL_FOR_SESSIONS_METHOD, IS_USE_POOL_FOR_SESSIONS_METHOD_SIGNATURE,
                STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getLocalHostMID = env->GetMethodID(streamConfigurationClass, GET_LOCAL_HOST_METHOD,
            GET_LOCAL_HOST_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_LOCAL_HOST_METHOD,
                GET_LOCAL_HOST_METHOD_SIGNATURE, STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getMovFileVersionMID = env->GetMethodID(streamConfigurationClass, GET_MOV_FILE_VERSION_METHOD,
            GET_MOV_FILE_VERSION_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_MOV_FILE_VERSION_METHOD,
                GET_MOV_FILE_VERSION_METHOD_SIGNATURE, STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getSilenceDetectionModeMID = env->GetMethodID(streamConfigurationClass, GET_SILENCE_DETECTION_MODE_METHOD,
            GET_SILENCE_DETECTION_MODE_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s",
                GET_SILENCE_DETECTION_MODE_METHOD, GET_SILENCE_DETECTION_MODE_METHOD_SIGNATURE,
                STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getThresholdMID = env->GetMethodID(streamConfigurationClass, GET_INITIAL_SILENCE_FRAMES_METHOD,
            GET_INITIAL_SILENCE_FRAMES_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s",
                GET_INITIAL_SILENCE_FRAMES_METHOD, GET_INITIAL_SILENCE_FRAMES_METHOD_SIGNATURE,
                STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getInitialSilenceFramesMID = env->GetMethodID(streamConfigurationClass, GET_INITIAL_SILENCE_FRAMES_METHOD,
            GET_INITIAL_SILENCE_FRAMES_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s",
                GET_INITIAL_SILENCE_FRAMES_METHOD, GET_INITIAL_SILENCE_FRAMES_METHOD_SIGNATURE,
                STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getDetectionFramesMID = env->GetMethodID(streamConfigurationClass, GET_DETECTION_FRAMES_METHOD,
            GET_DETECTION_FRAMES_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_DETECTION_FRAMES_METHOD,
                GET_DETECTION_FRAMES_METHOD_SIGNATURE, STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getSilenceDeadbandMID = env->GetMethodID(streamConfigurationClass, GET_SILENCE_DEADBAND_METHOD,
            GET_SILENCE_DEADBAND_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_SILENCE_DEADBAND_METHOD,
                GET_SILENCE_DEADBAND_METHOD_SIGNATURE, STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getSignalDeadbandMID = env->GetMethodID(streamConfigurationClass, GET_SIGNAL_DEADBAND_METHOD,
            GET_SIGNAL_DEADBAND_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_SIGNAL_DEADBAND_METHOD,
                GET_SIGNAL_DEADBAND_METHOD_SIGNATURE, STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    getSilenceDetectionDebugLevelMID = env->GetMethodID(streamConfigurationClass,
            GET_SILENCE_DETECTION_DEBUG_LEVEL_METHOD, GET_SILENCE_DETECTION_DEBUG_LEVEL_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s",
                GET_SILENCE_DETECTION_DEBUG_LEVEL_METHOD, GET_SILENCE_DETECTION_DEBUG_LEVEL_METHOD_SIGNATURE,
                STREAM_CONFIGURATION_CLASSNAME);
        abort();
    }

    // Do not need the global ref - the mid's are sufficient
    env->DeleteGlobalRef(streamConfigurationClass);
}
