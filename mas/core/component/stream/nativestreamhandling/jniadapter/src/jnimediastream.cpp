#include <stdexcept>
#include <iostream>

#include "jnimediastream.h"
#include "jniutil.h"
#include "jlogger.h"

using namespace std;

static const char* CLASSNAME = "masjni.jniadapter.JNIMediaStream";

const char* JNIMediaStream::REQUEST_IFRAME_METHOD = "sendPictureFastUpdateRequest";
const char* JNIMediaStream::REQUEST_IFRAME_METHOD_SIGNATURE = "()V";
jmethodID JNIMediaStream::iframeMID;

const char* JNIMediaStream::GET_CALL_SESSION_ID_METHOD = "getCallSessionId";
const char* JNIMediaStream::GET_CALL_SESSION_ID_METHOD_SIGNATURE = "()Ljava/lang/String;";
jmethodID JNIMediaStream::callSessionIdMID;

const char* JNIMediaStream::MEDIA_STREAM_JAVA_CLASSNAME = "com/mobeon/masp/stream/IInboundMediaStream";
const char* JNIMediaStream::MEDIA_STREAM_SUPPORT_JAVA_CLASSNAME = "com/mobeon/masp/stream/MediaStreamSupport";

const char* JNIMediaStream::RELEASE_PORTS_METHOD = "releasePorts";
const char* JNIMediaStream::RELEASE_PORTS_METHOD_SIGNATURE = "()V";
jmethodID JNIMediaStream::releasePortsMID;

// Since the global references are deleted right away
// it is no longer required to have an unload method
void JNI_MediaStreamOnLoad(void *reserved)
{
    JNIMediaStream::MediaStreamOnLoad(reserved);
}

void JNIMediaStream::MediaStreamOnLoad(void *reserved)
{
    JNIEnv* env = NULL;
    (void) JNIUtil::getJavaEnvironment((void**) &env, true);

    // MediaStream - MID's
    jclass msIFClass = (jclass) env->NewGlobalRef(env->FindClass(MEDIA_STREAM_JAVA_CLASSNAME));
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load class %s", MEDIA_STREAM_JAVA_CLASSNAME);
        abort();
    }

    iframeMID = env->GetMethodID(msIFClass, REQUEST_IFRAME_METHOD, REQUEST_IFRAME_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", REQUEST_IFRAME_METHOD,
                REQUEST_IFRAME_METHOD_SIGNATURE, MEDIA_STREAM_JAVA_CLASSNAME);
        abort();
    }

    jclass msSupportClass = (jclass) env->NewGlobalRef(env->FindClass(MEDIA_STREAM_SUPPORT_JAVA_CLASSNAME));
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load class %s", MEDIA_STREAM_SUPPORT_JAVA_CLASSNAME);
        abort();
    }

    callSessionIdMID = env->GetMethodID(msSupportClass, GET_CALL_SESSION_ID_METHOD,
            GET_CALL_SESSION_ID_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_CALL_SESSION_ID_METHOD,
                GET_CALL_SESSION_ID_METHOD_SIGNATURE, MEDIA_STREAM_SUPPORT_JAVA_CLASSNAME);
        abort();
    }

    releasePortsMID = env->GetMethodID(msSupportClass, RELEASE_PORTS_METHOD, RELEASE_PORTS_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", RELEASE_PORTS_METHOD,
                RELEASE_PORTS_METHOD_SIGNATURE, MEDIA_STREAM_SUPPORT_JAVA_CLASSNAME);
        abort();
    }

    // Do not need the global ref - the mid's are sufficient
    env->DeleteGlobalRef(msIFClass);
    env->DeleteGlobalRef(msSupportClass);
}
