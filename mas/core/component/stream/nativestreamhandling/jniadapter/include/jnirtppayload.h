/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef JNIRTPPAYLOADREF_H_
#define JNIRTPPAYLOADREF_H_

#include "jni.h"

extern "C" {
extern void JNI_RtpPayloadOnLoad(void*);
}

class JNIRtpPayload
{
public:
    static const char* GET_PAYLOAD_METHOD;
    static const char* GET_CHANNELS_METHOD;
    static const char* GET_CLOCKRATE_METHOD;
    static const char* GET_ENCODING_METHOD;
    static const char* GET_BWSENDER_METHOD;
    static const char* GET_BWRECEIVER_METHOD;
    static const char* GET_MEDIA_FORMAT_PARAMAMETERS_METHOD;

    inline static jmethodID getGetPayloadTypeMID()
    {
        return getPayloadTypeMID;
    }
    ;
    inline static jmethodID getGetChannelsMID()
    {
        return getChannelsMID;
    }
    ;
    inline static jmethodID getGetClockRateMID()
    {
        return getClockRateMID;
    }
    ;
    inline static jmethodID getGetEncodingMID()
    {
        return getEncodingMID;
    }
    ;
    inline static jmethodID getGetBwSenderMID()
    {
        return getBwSenderMID;
    }
    ;
    inline static jmethodID getGetBwReceiverMID()
    {
        return getBwReceiverMID;
    }
    ;
    inline static jmethodID getGetMediaFormatParametersMID()
    {
        return getMediaFormatParametersMID;
    }
    ;

    static void RtpPayloadOnLoad(void *reserved);

private:

    static const char* RTP_PAYLOAD_CLASSNAME;
    static const char* GET_PAYLOAD_METHOD_SIGNATURE;
    static jmethodID getPayloadTypeMID;
    static const char* GET_CHANNELS_METHOD_SIGNATURE;
    static jmethodID getChannelsMID;
    static const char* GET_CLOCKRATE_METHOD_SIGNATURE;
    static jmethodID getClockRateMID;
    static const char* GET_ENCODING_METHOD_SIGNATURE;
    static jmethodID getEncodingMID;
    static const char* GET_BWSENDER_METHOD_SIGNATURE;
    static jmethodID getBwSenderMID;
    static const char* GET_BWRECEIVER_METHOD_SIGNATURE;
    static jmethodID getBwReceiverMID;
    static const char* GET_MEDIA_FORMAT_PARAMAMETERS_METHOD_SIGNATURE;
    static jmethodID getMediaFormatParametersMID;
};

#endif /* JNIRTPPAYLOADREF_H_ */
