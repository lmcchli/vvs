/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef JNISTREAMCONTENTINFOREF_H_
#define JNISTREAMCONTENTINFOREF_H_

#include "jni.h"

extern "C" {
extern void JNI_StreamContentInfoOnLoad(void*);
}

class JNIStreamContentInfo
{
public:
    static const char* GET_AUDIO_PAYLOAD_METHOD;
    static const char* GET_DTMF_PAYLOAD_METHOD;
    static const char* GET_CN_PAYLOAD_METHOD;
    static const char* GET_VIDEO_PAYLOAD_METHOD;
    static const char* GET_CONTENT_TYPE_METHOD;
    static const char* GET_FILE_EXTENSION_METHOD;
    static const char* GET_CNAME_METHOD;
    static const char* GET_PAYLOADS_METHOD;
    static const char* GET_PTIME_METHOD;
    static const char* GET_MAXPTIME_METHOD;
    static const char* IS_VIDEO_METHOD;

    inline static jmethodID getGetAudioPayloadMID()
    {
        return getAudioPayloadMID;
    }
    ;
    inline static jmethodID getGetDtmfPayloadMID()
    {
        return getDtmfPayloadMID;
    }
    ;
    inline static jmethodID getGetCNPayloadMID()
    {
        return getCNPayloadMID;
    }
    ;
    inline static jmethodID getGetVideoPayloadMID()
    {
        return getVideoPayloadMID;
    }
    ;
    inline static jmethodID getGetContentTypeMID()
    {
        return getContentTypeMID;
    }
    ;
    inline static jmethodID getGetFileExtensionMID()
    {
        return getFileExtensionMID;
    }
    ;
    inline static jmethodID getGetCnameMID()
    {
        return getCnameMID;
    }
    ;
    inline static jmethodID getGetPayloadsMID()
    {
        return getPayloadsMID;
    }
    ;
    inline static jmethodID getGetPtimeMID()
    {
        return getPtimeMID;
    }
    ;
    inline static jmethodID getGetMaxPtimeMID()
    {
        return getMaxPtimeMID;
    }
    ;
    inline static jmethodID getIsVideoMID()
    {
        return isVideoMID;
    }
    ;

    static void RtpPayloadOnLoad(void *reserved);

private:
    static const char* STREAM_CONTENT_INFO_CLASSNAME;
    static const char* GET_AUDIO_PAYLOAD_METHOD_SIGNATURE;
    static jmethodID getAudioPayloadMID;
    static const char* GET_DTMF_PAYLOAD_METHOD_SIGNATURE;
    static jmethodID getDtmfPayloadMID;
    static const char* GET_CN_PAYLOAD_METHOD_SIGNATURE;
    static jmethodID getCNPayloadMID;
    static const char* GET_VIDEO_PAYLOAD_METHOD_SIGNATURE;
    static jmethodID getVideoPayloadMID;
    static const char* GET_CONTENT_TYPE_METHOD_SIGNATURE;
    static jmethodID getContentTypeMID;
    static const char* GET_FILE_EXTENSION_METHOD_SIGNATURE;
    static jmethodID getFileExtensionMID;
    static const char* GET_CNAME_METHOD_SIGNATURE;
    static jmethodID getCnameMID;
    static const char* GET_PAYLOADS_METHOD_SIGNATURE;
    static jmethodID getPayloadsMID;
    static const char* GET_PTIME_METHOD_SIGNATURE;
    static jmethodID getPtimeMID;
    static const char* GET_MAXPTIME_METHOD_SIGNATURE;
    static jmethodID getMaxPtimeMID;
    static const char* IS_VIDEO_METHOD_SIGNATURE;
    static jmethodID isVideoMID;
};

#endif /* JNISTREAMCONTENTINFOREF_H_ */
