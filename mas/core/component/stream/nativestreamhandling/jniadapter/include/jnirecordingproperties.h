/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef JNIRECORDINGPROPERTIESREF_H_
#define JNIRECORDINGPROPERTIESREF_H_

#include "jni.h"

extern "C" {
extern void JNI_RecordingPropertiesOnLoad(void*);
}

class JNIRecordingProperties
{
public:
    static const char* IS_SILENCE_DETECTION_FOR_START_METHOD;
    static const char* IS_SILENCE_DETECTION_FOR_STOP_METHOD;
    static const char* GET_MAX_WAIT_BEFORE_RECORD_METHOD;
    static const char* GET_MAX_RECORDING_DURATION_METHOD;
    static const char* GET_MIN_RECORDING_DURATION_METHOD;
    static const char* GET_MAX_SILENCE_METHOD;
    static const char* GET_TIMEOUT_METHOD;

    inline static jmethodID getIsSilenceDetectionForStartMID()
    {
        return isSilenceDetectionForStartMID;
    }
    ;
    inline static jmethodID getIsSilenceDetectionForStopMID()
    {
        return isSilenceDetectionForStopMID;
    }
    ;
    inline static jmethodID getMaxWaitBeforeRecordMID()
    {
        return maxWaitBeforeRecordMID;
    }
    ;
    inline static jmethodID getMaxRecordingDurationMID()
    {
        return maxRecordingDurationMID;
    }
    ;
    inline static jmethodID getMinRecordingDurationMID()
    {
        return minRecordingDurationMID;
    }
    ;
    inline static jmethodID getMaxSilenceMID()
    {
        return maxSilenceMID;
    }
    ;
    inline static jmethodID getTimeoutMID()
    {
        return timeoutMID;
    }
    ;

    static void ControlTokenOnLoad(void *reserved);

private:

    static const char* RECORDING_PROPERTIES_CLASSNAME;
    static const char* IS_SILENCE_DETECTION_FOR_START_METHOD_SIGNATURE;
    static jmethodID isSilenceDetectionForStartMID;
    static const char* IS_SILENCE_DETECTION_FOR_STOP_METHOD_SIGNATURE;
    static jmethodID isSilenceDetectionForStopMID;
    static const char* GET_MAX_WAIT_BEFORE_RECORD_METHOD_SIGNATURE;
    static jmethodID maxWaitBeforeRecordMID;
    static const char* GET_MAX_RECORDING_DURATION_METHOD_SIGNATURE;
    static jmethodID maxRecordingDurationMID;
    static const char* GET_MIN_RECORDING_DURATION_METHOD_SIGNATURE;
    static jmethodID minRecordingDurationMID;
    static const char* GET_MAX_SILENCE_METHOD_SIGNATURE;
    static jmethodID maxSilenceMID;
    static const char* GET_TIMEOUT_METHOD_SIGNATURE;
    static jmethodID timeoutMID;
};

#endif /* JNIRECORDINGPROPERTIESREF_H_ */
