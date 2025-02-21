#include <stdexcept>
#include <iostream>

#include "jnistreamcontentinfo.h"
#include "jniutil.h"
#include "jlogger.h"

using namespace std;

static const char* CLASSNAME = "masjni.jniadapter.JNIStreamContentInfo";

const char* JNIStreamContentInfo::GET_AUDIO_PAYLOAD_METHOD = "getAudioPayload";
const char* JNIStreamContentInfo::GET_AUDIO_PAYLOAD_METHOD_SIGNATURE = "()Lcom/mobeon/masp/stream/RTPPayload;";
jmethodID JNIStreamContentInfo::getAudioPayloadMID;

const char* JNIStreamContentInfo::GET_DTMF_PAYLOAD_METHOD = "getDTMFPayload";
const char* JNIStreamContentInfo::GET_DTMF_PAYLOAD_METHOD_SIGNATURE = "()Lcom/mobeon/masp/stream/RTPPayload;";
jmethodID JNIStreamContentInfo::getDtmfPayloadMID;

const char* JNIStreamContentInfo::GET_CN_PAYLOAD_METHOD = "getCNPayload";
const char* JNIStreamContentInfo::GET_CN_PAYLOAD_METHOD_SIGNATURE = "()Lcom/mobeon/masp/stream/RTPPayload;";
jmethodID JNIStreamContentInfo::getCNPayloadMID;

const char* JNIStreamContentInfo::GET_VIDEO_PAYLOAD_METHOD = "getVideoPayload";
const char* JNIStreamContentInfo::GET_VIDEO_PAYLOAD_METHOD_SIGNATURE = "()Lcom/mobeon/masp/stream/RTPPayload;";
jmethodID JNIStreamContentInfo::getVideoPayloadMID;

const char* JNIStreamContentInfo::GET_CONTENT_TYPE_METHOD = "getContentType";
const char* JNIStreamContentInfo::GET_CONTENT_TYPE_METHOD_SIGNATURE = "()Ljakarta/activation/MimeType;";
jmethodID JNIStreamContentInfo::getContentTypeMID;

const char* JNIStreamContentInfo::GET_FILE_EXTENSION_METHOD = "getFileExtension";
const char* JNIStreamContentInfo::GET_FILE_EXTENSION_METHOD_SIGNATURE = "()Ljava/lang/String;";
jmethodID JNIStreamContentInfo::getFileExtensionMID;

const char* JNIStreamContentInfo::GET_CNAME_METHOD = "getCNAME";
const char* JNIStreamContentInfo::GET_CNAME_METHOD_SIGNATURE = "()Ljava/lang/String;";
jmethodID JNIStreamContentInfo::getCnameMID;

const char* JNIStreamContentInfo::GET_PAYLOADS_METHOD = "getPayloads";
const char* JNIStreamContentInfo::GET_PAYLOADS_METHOD_SIGNATURE = "()[Lcom/mobeon/masp/stream/RTPPayload;";
jmethodID JNIStreamContentInfo::getPayloadsMID;

const char* JNIStreamContentInfo::GET_PTIME_METHOD = "getPTime";
const char* JNIStreamContentInfo::GET_PTIME_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamContentInfo::getPtimeMID;

const char* JNIStreamContentInfo::GET_MAXPTIME_METHOD = "getMaxPTime";
const char* JNIStreamContentInfo::GET_MAXPTIME_METHOD_SIGNATURE = "()I";
jmethodID JNIStreamContentInfo::getMaxPtimeMID;

const char* JNIStreamContentInfo::IS_VIDEO_METHOD = "isVideo";
const char* JNIStreamContentInfo::IS_VIDEO_METHOD_SIGNATURE = "()Z";
jmethodID JNIStreamContentInfo::isVideoMID;

const char* JNIStreamContentInfo::STREAM_CONTENT_INFO_CLASSNAME = "com/mobeon/masp/stream/StreamContentInfo";

// Since the global references are deleted right away
// it is no longer required to have an unload method
void JNI_StreamContentInfoOnLoad(void *reserved)
{
    JNIStreamContentInfo::RtpPayloadOnLoad(reserved);
}

void JNIStreamContentInfo::RtpPayloadOnLoad(void *reserved)
{
    JNIEnv* env = NULL;
    (void) JNIUtil::getJavaEnvironment((void**) &env, true);

    // ControlToken - MID's
    jclass streamContentInfoClass = (jclass) env->NewGlobalRef(env->FindClass(STREAM_CONTENT_INFO_CLASSNAME));
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load class %s", STREAM_CONTENT_INFO_CLASSNAME);
        abort();
    }

    getAudioPayloadMID = env->GetMethodID(streamContentInfoClass, GET_AUDIO_PAYLOAD_METHOD,
            GET_AUDIO_PAYLOAD_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_AUDIO_PAYLOAD_METHOD,
                GET_AUDIO_PAYLOAD_METHOD_SIGNATURE, STREAM_CONTENT_INFO_CLASSNAME);
        abort();
    }

    getDtmfPayloadMID = env->GetMethodID(streamContentInfoClass, GET_DTMF_PAYLOAD_METHOD,
            GET_DTMF_PAYLOAD_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_DTMF_PAYLOAD_METHOD,
                GET_DTMF_PAYLOAD_METHOD_SIGNATURE, STREAM_CONTENT_INFO_CLASSNAME);
        abort();
    }

    getCNPayloadMID = env->GetMethodID(streamContentInfoClass, GET_CN_PAYLOAD_METHOD, GET_CN_PAYLOAD_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_CN_PAYLOAD_METHOD,
                GET_CN_PAYLOAD_METHOD_SIGNATURE, STREAM_CONTENT_INFO_CLASSNAME);
        abort();
    }

    getVideoPayloadMID = env->GetMethodID(streamContentInfoClass, GET_VIDEO_PAYLOAD_METHOD,
            GET_VIDEO_PAYLOAD_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_VIDEO_PAYLOAD_METHOD,
                GET_VIDEO_PAYLOAD_METHOD_SIGNATURE, STREAM_CONTENT_INFO_CLASSNAME);
        abort();
    }

    getContentTypeMID = env->GetMethodID(streamContentInfoClass, GET_CONTENT_TYPE_METHOD,
            GET_CONTENT_TYPE_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_CONTENT_TYPE_METHOD,
                GET_CONTENT_TYPE_METHOD_SIGNATURE, STREAM_CONTENT_INFO_CLASSNAME);
        abort();
    }

    getFileExtensionMID = env->GetMethodID(streamContentInfoClass, GET_FILE_EXTENSION_METHOD,
            GET_FILE_EXTENSION_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_FILE_EXTENSION_METHOD,
                GET_FILE_EXTENSION_METHOD_SIGNATURE, STREAM_CONTENT_INFO_CLASSNAME);
        abort();
    }

    getCnameMID = env->GetMethodID(streamContentInfoClass, GET_CNAME_METHOD, GET_CNAME_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_CNAME_METHOD,
                GET_CNAME_METHOD_SIGNATURE, STREAM_CONTENT_INFO_CLASSNAME);
        abort();
    }

    getPayloadsMID = env->GetMethodID(streamContentInfoClass, GET_PAYLOADS_METHOD, GET_PAYLOADS_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_PAYLOADS_METHOD,
                GET_PAYLOADS_METHOD_SIGNATURE, STREAM_CONTENT_INFO_CLASSNAME);
        abort();
    }

    getPtimeMID = env->GetMethodID(streamContentInfoClass, GET_PTIME_METHOD, GET_PTIME_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_PTIME_METHOD,
                GET_PTIME_METHOD_SIGNATURE, STREAM_CONTENT_INFO_CLASSNAME);
        abort();
    }

    getMaxPtimeMID = env->GetMethodID(streamContentInfoClass, GET_MAXPTIME_METHOD, GET_MAXPTIME_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_MAXPTIME_METHOD,
                GET_MAXPTIME_METHOD_SIGNATURE, STREAM_CONTENT_INFO_CLASSNAME);
        abort();
    }

    isVideoMID = env->GetMethodID(streamContentInfoClass, IS_VIDEO_METHOD, IS_VIDEO_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", IS_VIDEO_METHOD,
                IS_VIDEO_METHOD_SIGNATURE, STREAM_CONTENT_INFO_CLASSNAME);
        abort();
    }
    // Do not need the global ref - the mid's are sufficient
    env->DeleteGlobalRef(streamContentInfoClass);
}
