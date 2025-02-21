/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef JNIEVENTDISPATCHERREF_H_
#define JNIEVENTDISPATCHERREF_H_

#include "jni.h"

extern "C" {
extern void JNI_EvtDispatcherOnLoad(void*);
}

class JNIEvtDispatcher
{
public:
    static const char* EVENT_PLAY_FINISHED_METHOD;
    static const char* EVENT_RECORD_FINISHED_METHOD;
    static const char* EVENT_STREAM_ABANDONED_METHOD;
    static const char* EVENT_PLAY_FAILED_METHOD;
    static const char* EVENT_RECORD_FAILED_METHOD;
    static const char* EVENT_RETURN_FROM_CALL_METHOD;
    static const char* EVENT_SEND_TOKEN_METHOD;

    inline static jmethodID getPlayFinishedMID()
    {
        return playFinishedMID;
    }
    ;
    inline static jmethodID getRecordFinishedMID()
    {
        return recordFinishedMID;
    }
    ;
    inline static jmethodID getStreamAbandonedMID()
    {
        return streamAbandonedMID;
    }
    ;
    inline static jmethodID getPlayFailedMID()
    {
        return playFailedMID;
    }
    ;
    inline static jmethodID getRecordFailedMID()
    {
        return recordFailedMID;
    }
    ;
    inline static jmethodID getRetFromCallMID()
    {
        return retFromCallMID;
    }
    ;
    inline static jmethodID getSendTokenMID()
    {
        return sendTokenMID;
    }
    ;

    static void EvtDispatcherOnLoad(void *reserved);

private:

    static const char* STACK_EVENT_NOTIFIER_CLASSNAME;
    static const char* EVENT_PLAY_FINISHED_METHOD_SIGNATURE;
    static jmethodID playFinishedMID;
    static const char* EVENT_RECORD_FINISHED_METHOD_SIGNATURE;
    static jmethodID recordFinishedMID;
    static const char* EVENT_STREAM_ABANDONED_METHOD_SIGNATURE;
    static jmethodID streamAbandonedMID;
    static const char* EVENT_PLAY_FAILED_METHOD_SIGNATURE;
    static jmethodID playFailedMID;
    static const char* EVENT_RECORD_FAILED_METHOD_SIGNATURE;
    static jmethodID recordFailedMID;
    static const char* EVENT_RETURN_FROM_CALL_METHOD_SIGNATURE;
    static jmethodID retFromCallMID;
    static const char* EVENT_SEND_TOKEN_METHOD_SIGNATURE;
    static jmethodID sendTokenMID;
};

#endif /* JNIEVENTDISPATCHERREF_H_ */
