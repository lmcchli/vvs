/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <stdexcept>

#include "rtppayload.h"
#include "streamrtpsession.h"
#include "jlogger.h"
#include "jniutil.h"
#include "jnirtppayload.h"

#include <ccrtp/rtp.h>
#include <base_std.h> 

using namespace std;
using namespace ost;

static const char* CLASSNAME = "masjni.ccrtpadapter.RTPPayload";

using namespace java;
/**
 * Each index represens an RTP payload type as defined in RFC3551. 
 */

const PayloadType STATIC_PAYLOAD_MAPPING[] = { sptPCMU, ptINVALID, sptG726_32, sptGSM, sptG723, sptDVI4_8000,
        sptDVI4_16000, sptLPC, sptPCMA, sptG722, sptL16_DUAL, sptL16_MONO, sptQCELP, ptINVALID, sptMPA, sptG728,
        sptDVI4_11025, sptDVI4_22050, sptG729, ptINVALID, ptINVALID, ptINVALID, ptINVALID, ptINVALID, ptINVALID,
        sptCELB, sptJPEG, ptINVALID, sptNV, ptINVALID, ptINVALID, sptH261, sptMPV, sptMP2T, sptH263 };

RTPPayload::RTPPayload(jobject payloadType, JNIEnv* env) :
                mPayloadType(-1), mChannels(0), mCodec(""), mEnv(env)
{
    try {
        // getPayloadType
        mPayloadType = (int) JNIUtil::callIntMethod(env, payloadType, JNIRtpPayload::getGetPayloadTypeMID());
        JNIUtil::checkException(env, JNIRtpPayload::GET_PAYLOAD_METHOD, true);

        // getChannels
        mChannels = (int) JNIUtil::callIntMethod(env, payloadType, JNIRtpPayload::getGetChannelsMID());
        JNIUtil::checkException(env, JNIRtpPayload::GET_CHANNELS_METHOD, true);

        // getClockRate
        mClockRate = (long) JNIUtil::callIntMethod(env, payloadType, JNIRtpPayload::getGetClockRateMID());
        JNIUtil::checkException(env, JNIRtpPayload::GET_CLOCKRATE_METHOD, true);

        // getEncoding
        jstring codec = (jstring) JNIUtil::callObjectMethod(env, payloadType, JNIRtpPayload::getGetEncodingMID());
        JNIUtil::checkException(env, JNIRtpPayload::GET_ENCODING_METHOD, true);

        if (codec != NULL) {
            const char* codecStr(env->GetStringUTFChars(codec, 0));
            mCodec = codecStr;
            env->ReleaseStringUTFChars(codec, codecStr);
            env->DeleteLocalRef((jobject) codec);
        }

        // getMediaFormatParamaters
        jstring mediaFormatParameters = (jstring) JNIUtil::callObjectMethod(env, payloadType,
                JNIRtpPayload::getGetMediaFormatParametersMID());
        JNIUtil::checkException(env, JNIRtpPayload::GET_MEDIA_FORMAT_PARAMAMETERS_METHOD, true);

        if (mediaFormatParameters != NULL) {
            const char* mediaFormatParametersStr(env->GetStringUTFChars(mediaFormatParameters, 0));
            mMediaFormatParameters = mediaFormatParametersStr;
            env->ReleaseStringUTFChars(mediaFormatParameters, mediaFormatParametersStr);
            env->DeleteLocalRef((jobject) mediaFormatParameters);
        }

        if (!isStatic() && !isDynamic()) {
            ostringstream message("The specified RTP payload type is not valid: ");
            message << mPayloadType;
            throw runtime_error(message.str().c_str());
        }

        // getbwSender
        mBwSender = (int) JNIUtil::callIntMethod(env, payloadType, JNIRtpPayload::getGetBwSenderMID());
        JNIUtil::checkException(env, JNIRtpPayload::GET_BWSENDER_METHOD, true);

        // getbwReceiver
        mBwReceiver = (int) JNIUtil::callIntMethod(env, payloadType, JNIRtpPayload::getGetBwReceiverMID());
        JNIUtil::checkException(env, JNIRtpPayload::GET_BWRECEIVER_METHOD, true);

        JLogger::jniLogTrace(env, CLASSNAME, "Payload: %s", toString().c_str());

        JLogger::jniLogDebug(env, CLASSNAME, "RTPPayload - create at %#x", this);
    } catch (exception& e) {
        JLogger::jniLogError(env, CLASSNAME, "%s", e.what());
        throw;
    }
}

RTPPayload::~RTPPayload()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(mEnv), CLASSNAME, "~RTPPayload - delete at %#x", this);
}

void RTPPayload::setPayloadFormat(StreamRTPSession& session)
{
    if (isStatic()) {
        session.setPayloadFormat(StaticPayloadFormat((StaticPayloadType) STATIC_PAYLOAD_MAPPING[mPayloadType]));
    } else {
        session.setPayloadFormat(DynamicPayloadFormat(mPayloadType, mClockRate));
    }
}
/*
 ost::PayloadFormat& RTPPayload::getPayloadFormat()
 {
 if(isStatic(mPayloadType)) {
 return StaticPayloadFormat((StaticPayloadType)STATIC_PAYLOAD_MAPPING[mPayloadType]);
 }
 return DynamicPayloadFormat(mPayloadType, mClockRate);
 }
 */
bool RTPPayload::isStatic()
{
    // lastStaticPayloadType is equal to the last index in the
    // mapping.
    if ((mPayloadType < 0) || (mPayloadType > lastStaticPayloadType)) {
        return false;
    }
    return STATIC_PAYLOAD_MAPPING[mPayloadType] != ptINVALID;
}

bool RTPPayload::isDynamic()
{
    // Does not support redefinition of static payload types.
    return (mPayloadType < ptINVALID) && !isStatic();
}

int RTPPayload::getPayloadType()
{
    return mPayloadType;
}

int RTPPayload::getChannels()
{
    return mChannels;
}

const base::String& RTPPayload::getCodec()
{
    return mCodec;
}

unsigned RTPPayload::getClockRate()
{
    return mClockRate;
}

const base::String& RTPPayload::getMediaFormatParameters() const
{
    return mMediaFormatParameters;
}
int RTPPayload::getBwSender()
{
    return mBwSender;
}

int RTPPayload::getBwReceiver()
{
    return mBwReceiver;
}

base::String RTPPayload::toString()
{
    std::string str;
    str += "Codec:";
    str += mCodec;
    str += ",";
    str += "PT:";
    str += StringOf(mPayloadType);
    str += ",";
    str += "CR:";
    str += StringOf(mClockRate);
    str += ",";
    str += "CH:";
    str += StringOf(mChannels);
    str += ",";
    str += "RS:";
    str += StringOf(mBwSender);
    str += ",";
    str += "RR:";
    str += StringOf(mBwReceiver);
    return str;
}

template<class T>
std::string RTPPayload::StringOf(const T& object)
{
    std::ostringstream os;
    os << object;
    std::string s = os.str();
    return s;
}
