#include <stdexcept>
#include <iostream>

#include "jnirtppayload.h"
#include "jniutil.h"
#include "jlogger.h"

using namespace std;

static const char* CLASSNAME = "masjni.jniadapter.JNIRtpPayload";

const char* JNIRtpPayload::GET_PAYLOAD_METHOD = "getPayloadType";
const char* JNIRtpPayload::GET_PAYLOAD_METHOD_SIGNATURE = "()I";
jmethodID JNIRtpPayload::getPayloadTypeMID;

const char* JNIRtpPayload::GET_CHANNELS_METHOD = "getChannels";
const char* JNIRtpPayload::GET_CHANNELS_METHOD_SIGNATURE = "()I";
jmethodID JNIRtpPayload::getChannelsMID;

const char* JNIRtpPayload::GET_CLOCKRATE_METHOD = "getClockRate";
const char* JNIRtpPayload::GET_CLOCKRATE_METHOD_SIGNATURE = "()I";
jmethodID JNIRtpPayload::getClockRateMID;

const char* JNIRtpPayload::GET_ENCODING_METHOD = "getEncoding";
const char* JNIRtpPayload::GET_ENCODING_METHOD_SIGNATURE = "()Ljava/lang/String;";
jmethodID JNIRtpPayload::getEncodingMID;

const char* JNIRtpPayload::GET_BWSENDER_METHOD = "getBwSender";
const char* JNIRtpPayload::GET_BWSENDER_METHOD_SIGNATURE = "()I";
jmethodID JNIRtpPayload::getBwSenderMID;

const char* JNIRtpPayload::GET_BWRECEIVER_METHOD = "getBwReceiver";
const char* JNIRtpPayload::GET_BWRECEIVER_METHOD_SIGNATURE = "()I";
jmethodID JNIRtpPayload::getBwReceiverMID;

const char* JNIRtpPayload::GET_MEDIA_FORMAT_PARAMAMETERS_METHOD = "getMediaFormatParameters";
const char* JNIRtpPayload::GET_MEDIA_FORMAT_PARAMAMETERS_METHOD_SIGNATURE = "()Ljava/lang/String;";
jmethodID JNIRtpPayload::getMediaFormatParametersMID;

const char* JNIRtpPayload::RTP_PAYLOAD_CLASSNAME = "com/mobeon/masp/stream/RTPPayload";

// Since the global references are deleted right away
// it is no longer required to have an unload method
void JNI_RtpPayloadOnLoad(void *reserved)
{
    JNIRtpPayload::RtpPayloadOnLoad(reserved);
}

void JNIRtpPayload::RtpPayloadOnLoad(void *reserved)
{
    JNIEnv* env = NULL;
    (void) JNIUtil::getJavaEnvironment((void**) &env, true);

    // ControlToken - MID's
    jclass rtpPayloadClass = (jclass) env->NewGlobalRef(env->FindClass(RTP_PAYLOAD_CLASSNAME));
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load class %s", RTP_PAYLOAD_CLASSNAME);
        abort();
    }

    getPayloadTypeMID = env->GetMethodID(rtpPayloadClass, GET_PAYLOAD_METHOD, GET_PAYLOAD_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_PAYLOAD_METHOD,
                GET_PAYLOAD_METHOD_SIGNATURE, RTP_PAYLOAD_CLASSNAME);
        abort();
    }

    getChannelsMID = env->GetMethodID(rtpPayloadClass, GET_CHANNELS_METHOD, GET_CHANNELS_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_CHANNELS_METHOD,
                GET_CHANNELS_METHOD_SIGNATURE, RTP_PAYLOAD_CLASSNAME);
        abort();
    }

    getClockRateMID = env->GetMethodID(rtpPayloadClass, GET_CLOCKRATE_METHOD, GET_CLOCKRATE_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_CLOCKRATE_METHOD,
                GET_CLOCKRATE_METHOD_SIGNATURE, RTP_PAYLOAD_CLASSNAME);
        abort();
    }

    getEncodingMID = env->GetMethodID(rtpPayloadClass, GET_ENCODING_METHOD, GET_ENCODING_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_ENCODING_METHOD,
                GET_ENCODING_METHOD_SIGNATURE, RTP_PAYLOAD_CLASSNAME);
        abort();
    }

    getBwSenderMID = env->GetMethodID(rtpPayloadClass, GET_BWSENDER_METHOD, GET_BWSENDER_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_BWSENDER_METHOD,
                GET_BWSENDER_METHOD_SIGNATURE, RTP_PAYLOAD_CLASSNAME);
        abort();
    }

    getBwReceiverMID = env->GetMethodID(rtpPayloadClass, GET_BWRECEIVER_METHOD, GET_BWRECEIVER_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s", GET_BWRECEIVER_METHOD,
                GET_BWRECEIVER_METHOD_SIGNATURE, RTP_PAYLOAD_CLASSNAME);
        abort();
    }

    getMediaFormatParametersMID = env->GetMethodID(rtpPayloadClass, GET_MEDIA_FORMAT_PARAMAMETERS_METHOD,
            GET_MEDIA_FORMAT_PARAMAMETERS_METHOD_SIGNATURE);
    if (env->ExceptionCheck() == JNI_TRUE) {
        env->ExceptionDescribe();

        JLogger::jniLogError(env, CLASSNAME, "Unable to load mid %s signature %s from %s",
                GET_MEDIA_FORMAT_PARAMAMETERS_METHOD, GET_MEDIA_FORMAT_PARAMAMETERS_METHOD_SIGNATURE,
                RTP_PAYLOAD_CLASSNAME);
        abort();
    }

    // Do not need the global ref - the mid's are sufficient
    env->DeleteGlobalRef(rtpPayloadClass);
}
