/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef JNIMEDIASTREAMREF_H_
#define JNIMEDIASTREAMREF_H_

#include "jni.h"

extern "C" {
extern void JNI_MediaStreamOnLoad(void*);
}

class JNIMediaStream
{
public:
    static const char* REQUEST_IFRAME_METHOD;
    static const char* GET_CALL_SESSION_ID_METHOD;

    static const char* RELEASE_PORTS_METHOD;

    inline static jmethodID getIframeMID()
    {
        return iframeMID;
    }
    ;
    inline static jmethodID getCallSessionIdMID()
    {
        return callSessionIdMID;
    }
    ;

    inline static jmethodID getReleasePortsMID()
    {
        return releasePortsMID;
    }
    ;

    static void MediaStreamOnLoad(void *reserved);

private:

    static const char* MEDIA_STREAM_JAVA_CLASSNAME;
    static const char* REQUEST_IFRAME_METHOD_SIGNATURE;
    static jmethodID iframeMID;

    static const char* MEDIA_STREAM_SUPPORT_JAVA_CLASSNAME;
    static const char* GET_CALL_SESSION_ID_METHOD_SIGNATURE;
    static const char* RELEASE_PORTS_METHOD_SIGNATURE;

    static jmethodID callSessionIdMID;
    static jmethodID releasePortsMID;
};

#endif /* JNIMEDIAOSTREAMREF_H_ */
