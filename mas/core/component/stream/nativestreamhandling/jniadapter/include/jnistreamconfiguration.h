/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef JNISTREAMCONFIGURATIONREF_H_
#define JNISTREAMCONFIGURATIONREF_H_

#include "jni.h"

extern "C" {
extern void JNI_StreamConfigurationOnLoad(void*);
}

class JNIStreamConfiguration
{
public:
    static const char* GET_THREADPOOL_SIZE_METHOD;
    static const char* GET_PACKETPEND_TIMEOUT_METHOD;
    static const char* GET_SEND_PACKETS_AHEAD_METHOD;
    static const char* GET_EXPIRE_TIMEOUT_METHOD;
    static const char* GET_MTU_METHOD;
    static const char* GET_ABANDONED_STREAM_TIMEOUT_METHOD;
    static const char* GET_SENDERS_CONTROL_FRACTION_METHOD;
    static const char* GET_AUDIO_SKIP_METHOD;
    static const char* GET_SKEW_METHOD;
    static const char* GET_SKEW_INTREP_METHOD;
    static const char* GET_AUDIO_REPLACE_WITH_SILENCE_METHOD;
    static const char* GET_THREADPOOL_MAX_WAIT_TIME_METHOD;
    static const char* GET_MAX_WAIT_FOR_IFRAME_METHOD;
    static const char* IS_DISPATCH_DTMF_ON_KEY_DOWN_METHOD;
    static const char* IS_USE_POOL_FOR_SESSIONS_METHOD;
    static const char* GET_LOCAL_HOST_METHOD;
    static const char* GET_MOV_FILE_VERSION_METHOD;
    static const char* GET_SILENCE_DETECTION_MODE_METHOD;
    static const char* GET_THRESHOLD_METHOD;
    static const char* GET_INITIAL_SILENCE_FRAMES_METHOD;
    static const char* GET_DETECTION_FRAMES_METHOD;
    static const char* GET_SILENCE_DEADBAND_METHOD;
    static const char* GET_SIGNAL_DEADBAND_METHOD;
    static const char* GET_SILENCE_DETECTION_DEBUG_LEVEL_METHOD;

    inline static jmethodID getGetThreadPoolSizeMID()
    {
        return getThreadPoolSizeMID;
    }
    ;
    inline static jmethodID getGetPacketEndTimeoutMID()
    {
        return getPacketEndTimeoutMID;
    }
    ;
    inline static jmethodID getGetSendPacketsAheadMID()
    {
        return getSendPacketsAheadMID;
    }
    ;
    inline static jmethodID getGetExpireTimeoutMID()
    {
        return getExpireTimeoutMID;
    }
    ;
    inline static jmethodID getGetMtuMID()
    {
        return getMtuMID;
    }
    ;
    inline static jmethodID getGetAbandonedStreamTimeoutMID()
    {
        return getAbandonedStreamTimeoutMID;
    }
    ;
    inline static jmethodID getGetSendersControlFractionMID()
    {
        return getSendersControlFractionMID;
    }
    ;
    inline static jmethodID getGetAudioSkipMID()
    {
        return getAudioSkipMID;
    }
    ;
    inline static jmethodID getGetSkewMID()
    {
        return getSkewMID;
    }
    ;
    inline static jmethodID getGetSkewIntRepMID()
    {
        return getSkewIntRepMID;
    }
    ;
    inline static jmethodID getGetAudioReplaceWithSilenceMID()
    {
        return getAudioReplaceWithSilenceMID;
    }
    ;
    inline static jmethodID getGetThreadPoolMaxWithTimeMID()
    {
        return getThreadPoolMaxWithTimeMID;
    }
    ;
    inline static jmethodID getGetMaxWaitForIframeMID()
    {
        return getMaxWaitForIframeMID;
    }
    ;
    inline static jmethodID getIsDispatchDtmfOnKeyDownMID()
    {
        return isDispatchDtmfOnKeyDownMID;
    }
    ;
    inline static jmethodID getIsUsePoolForSessionsMID()
    {
        return isUsePoolForSessionsMID;
    }
    ;
    inline static jmethodID getGetLocalHostMID()
    {
        return getLocalHostMID;
    }
    ;
    inline static jmethodID getGetMovFileVersionMID()
    {
        return getMovFileVersionMID;
    }
    ;
    inline static jmethodID getGetSilenceDetectionModeMID()
    {
        return getSilenceDetectionModeMID;
    }
    ;
    inline static jmethodID getGetThresholdMID()
    {
        return getThresholdMID;
    }
    ;
    inline static jmethodID getGetInitialSilenceFramesMID()
    {
        return getInitialSilenceFramesMID;
    }
    ;
    inline static jmethodID getGetDetectionFramesMID()
    {
        return getDetectionFramesMID;
    }
    ;
    inline static jmethodID getGetSilenceDeadbandMID()
    {
        return getSilenceDeadbandMID;
    }
    ;
    inline static jmethodID getGetSignalDeadbandMID()
    {
        return getSignalDeadbandMID;
    }
    ;
    inline static jmethodID getGetSilenceDetectionDebugLevelMID()
    {
        return getSilenceDetectionDebugLevelMID;
    }
    ;

    static void StreamConfigurationOnLoad(void *reserved);

private:
    static const char* STREAM_CONFIGURATION_CLASSNAME;
    static const char* GET_THREADPOOL_SIZE_METHOD_SIGNATURE;
    static jmethodID getThreadPoolSizeMID;
    static const char* GET_PACKETPEND_TIMEOUT_METHOD_SIGNATURE;
    static jmethodID getPacketEndTimeoutMID;
    static const char* GET_SEND_PACKETS_AHEAD_METHOD_SIGNATURE;
    static jmethodID getSendPacketsAheadMID;
    static const char* GET_EXPIRE_TIMEOUT_METHOD_SIGNATURE;
    static jmethodID getExpireTimeoutMID;
    static const char* GET_MTU_METHOD_SIGNATURE;
    static jmethodID getMtuMID;
    static const char* GET_ABANDONED_STREAM_TIMEOUT_METHOD_SIGNATURE;
    static jmethodID getAbandonedStreamTimeoutMID;
    static const char* GET_SENDERS_CONTROL_FRACTION_METHOD_SIGNATURE;
    static jmethodID getSendersControlFractionMID;
    static const char* GET_AUDIO_SKIP_METHOD_SIGNATURE;
    static jmethodID getAudioSkipMID;
    static const char* GET_SKEW_METHOD_SIGNATURE;
    static jmethodID getSkewMID;
    static const char* GET_SKEW_INTREP_METHOD_SIGNATURE;
    static jmethodID getSkewIntRepMID;
    static const char* GET_AUDIO_REPLACE_WITH_SILENCE_METHOD_SIGNATURE;
    static jmethodID getAudioReplaceWithSilenceMID;
    static const char* GET_THREADPOOL_MAX_WAIT_TIME_METHOD_SIGNATURE;
    static jmethodID getThreadPoolMaxWithTimeMID;
    static const char* GET_MAX_WAIT_FOR_IFRAME_METHOD_SIGNATURE;
    static jmethodID getMaxWaitForIframeMID;
    static const char* IS_DISPATCH_DTMF_ON_KEY_DOWN_METHOD_SIGNATURE;
    static jmethodID isDispatchDtmfOnKeyDownMID;
    static const char* IS_USE_POOL_FOR_SESSIONS_METHOD_SIGNATURE;
    static jmethodID isUsePoolForSessionsMID;
    static const char* GET_LOCAL_HOST_METHOD_SIGNATURE;
    static jmethodID getLocalHostMID;
    static const char* GET_MOV_FILE_VERSION_METHOD_SIGNATURE;
    static jmethodID getMovFileVersionMID;
    static const char* GET_SILENCE_DETECTION_MODE_METHOD_SIGNATURE;
    static jmethodID getSilenceDetectionModeMID;
    static const char* GET_THRESHOLD_METHOD_SIGNATURE;
    static jmethodID getThresholdMID;
    static const char* GET_INITIAL_SILENCE_FRAMES_METHOD_SIGNATURE;
    static jmethodID getInitialSilenceFramesMID;
    static const char* GET_DETECTION_FRAMES_METHOD_SIGNATURE;
    static jmethodID getDetectionFramesMID;
    static const char* GET_SILENCE_DEADBAND_METHOD_SIGNATURE;
    static jmethodID getSilenceDeadbandMID;
    static const char* GET_SIGNAL_DEADBAND_METHOD_SIGNATURE;
    static jmethodID getSignalDeadbandMID;
    static const char* GET_SILENCE_DETECTION_DEBUG_LEVEL_METHOD_SIGNATURE;
    static jmethodID getSilenceDetectionDebugLevelMID;
};

#endif /* JNISTREAMCONFIGURATIONREF_H_ */
