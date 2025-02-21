/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef JNICONTROLTOCKENREF_H_
#define JNICONTROLTOCKENREF_H_

#include "jni.h"

extern "C" {
extern void JNI_ControlTokenOnLoad(void*);
}

class JNIControlToken
{
public:
    static const char* GET_TOKEN_DIGIT_METHOD;
    static const char* GET_VOLUME_METHOD;
    static const char* GET_DURATION_METHOD;

    inline static jmethodID getGetTokenDigitMID()
    {
        return getTokenDigitMID;
    }
    ;
    inline static jmethodID getGetVolumeMID()
    {
        return getVolumeMID;
    }
    ;
    inline static jmethodID getGetDurationMID()
    {
        return getDurationMID;
    }
    ;

    static void ControlTokenOnLoad(void *reserved);

private:

    static const char* CONTROL_TOKEN_CLASSNAME;
    static const char* GET_TOKEN_DIGIT_METHOD_SIGNATURE;
    static jmethodID getTokenDigitMID;
    static const char* GET_VOLUME_METHOD_SIGNATURE;
    static jmethodID getVolumeMID;
    static const char* GET_DURATION_METHOD_SIGNATURE;
    static jmethodID getDurationMID;
};

#endif /* JNICONTROLTOCKENREF_H_ */
