/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <base_std.h>

#include "streamcontentinfo.h"
#include "jniutil.h"
#include "jlogger.h"
#include "jnistreamcontentinfo.h"
#include "rtppayload.h"

const char* GET_ARRAY_LENGTH = "GetArrayLength";
const char* GET_OBJECT_ARRAY_ELEMENT = "GetObjectArrayElement";

static const char* CLASSNAME = "masjni.ccrtpadapter.StreamContentInfo";

using namespace java;

std::auto_ptr<StreamContentInfo> StreamContentInfo::getInbound(jobject contentInfo, JNIEnv* env)
{
    return std::auto_ptr<StreamContentInfo>(new StreamContentInfo(contentInfo, env, true));
}

std::auto_ptr<StreamContentInfo> StreamContentInfo::getOutbound(jobject contentInfo, JNIEnv* env)
{
    return std::auto_ptr<StreamContentInfo>(new StreamContentInfo(contentInfo, env));
}

StreamContentInfo::StreamContentInfo(jobject contentInfo, JNIEnv* env, bool createInbound) :
        mAudioPayload(NULL), mVideoPayload(NULL), mDTMFPayload(NULL), mContentType(NULL), mFileExtension(""), mPayloads(),
        mPayloadSize(0), mPTime(0), mIsVideo(false)
{
    try {
        // getAudioPayload
        jobject audioPayload = JNIUtil::callObjectMethod(env, contentInfo,
                JNIStreamContentInfo::getGetAudioPayloadMID());
        JNIUtil::checkException(env, JNIStreamContentInfo::GET_AUDIO_PAYLOAD_METHOD, true);
        mAudioPayload.reset(new RTPPayload(audioPayload, env));
        env->DeleteLocalRef(audioPayload);

        // getVideoPayload
        jobject videoPayload = JNIUtil::callObjectMethod(env, contentInfo,
                JNIStreamContentInfo::getGetVideoPayloadMID());
        JNIUtil::checkException(env, JNIStreamContentInfo::GET_VIDEO_PAYLOAD_METHOD, true);

        if (videoPayload != NULL) {
            mVideoPayload.reset(new RTPPayload(videoPayload, env));
            env->DeleteLocalRef(videoPayload);
        }

        // getDTMFPayload
        jobject dtmfPayload = JNIUtil::callObjectMethod(env, contentInfo, JNIStreamContentInfo::getGetDtmfPayloadMID());
        JNIUtil::checkException(env, JNIStreamContentInfo::GET_DTMF_PAYLOAD_METHOD, true);
        if (dtmfPayload != NULL) {
            mDTMFPayload.reset(new RTPPayload(dtmfPayload, env));
            env->DeleteLocalRef(dtmfPayload);
        }

        // getCNPayload
        jobject cnPayload = JNIUtil::callObjectMethod(env, contentInfo, JNIStreamContentInfo::getGetCNPayloadMID());
        JNIUtil::checkException(env, JNIStreamContentInfo::GET_CN_PAYLOAD_METHOD, true);
        if (cnPayload != NULL) {
            mCNPayload.reset(new RTPPayload(cnPayload, env));
            env->DeleteLocalRef(cnPayload);
        } else {
            JLogger::jniLogTrace(env, CLASSNAME, "-----NO CN PAYLOAD SET------");
        }

        // getContentType
        jobject contentType = JNIUtil::callObjectMethod(env, contentInfo, JNIStreamContentInfo::getGetContentTypeMID());
        JNIUtil::checkException(env, JNIStreamContentInfo::GET_CONTENT_TYPE_METHOD, true);

        // This global reference must be deleted after the stream 
        // has been deleted!
        mContentType = env->NewGlobalRef(contentType);
        env->DeleteLocalRef(contentType);

        // isVideo
        mIsVideo = JNIUtil::callBooleanMethod(env, contentInfo, JNIStreamContentInfo::getIsVideoMID()) == 1;
        JNIUtil::checkException(env, JNIStreamContentInfo::IS_VIDEO_METHOD, true);

        // getFileExtension
        jstring fileExtension = (jstring) JNIUtil::callObjectMethod(env, contentInfo,
                JNIStreamContentInfo::getGetFileExtensionMID());
        JNIUtil::checkException(env, JNIStreamContentInfo::GET_FILE_EXTENSION_METHOD, true);

        if (fileExtension != NULL) {
            const char* ext(env->GetStringUTFChars(fileExtension, 0));
            mFileExtension = ext;
            env->ReleaseStringUTFChars(fileExtension, ext);
            env->DeleteLocalRef((jobject) fileExtension);
        } else {
            JLogger::jniLogTrace(env, CLASSNAME, "-----NO File Extension-----");
        }

        // getCNAME
        jstring cname = (jstring) JNIUtil::callObjectMethod(env, contentInfo, JNIStreamContentInfo::getGetCnameMID());
        JNIUtil::checkException(env, JNIStreamContentInfo::GET_CNAME_METHOD, true);

        if (cname != NULL) {
            const char* cnameStr(env->GetStringUTFChars(cname, 0));
            mCNAME = cnameStr;
            env->ReleaseStringUTFChars(cname, cnameStr);
            env->DeleteLocalRef((jobject) cname);
        }

        // getPTime
        mPTime = (int) JNIUtil::callIntMethod(env, contentInfo, JNIStreamContentInfo::getGetPtimeMID());
        JNIUtil::checkException(env, JNIStreamContentInfo::GET_PTIME_METHOD, true);

        // getMaxPTime
        mMaxPTime = (int) JNIUtil::callIntMethod(env, contentInfo, JNIStreamContentInfo::getGetMaxPtimeMID());
        JNIUtil::checkException(env, JNIStreamContentInfo::GET_MAXPTIME_METHOD, true);
    } catch (std::exception& e) {
        JLogger::jniLogError(env, CLASSNAME, "%s", e.what());
        throw;
    }

    JLogger::jniLogDebug(env, CLASSNAME, "StreamContentInfo - created at %#x", this);
}

StreamContentInfo::StreamContentInfo(jobject contentInfo, JNIEnv* env) :
        mAudioPayload(NULL), mVideoPayload(NULL), mDTMFPayload(NULL), mCNPayload(NULL), mContentType(NULL), mFileExtension(""),
        mPayloads(), mPayloadSize(0)
{
    try {
        // getPTime
        mPTime = (int) JNIUtil::callIntMethod(env, contentInfo, JNIStreamContentInfo::getGetPtimeMID());
        JNIUtil::checkException(env, JNIStreamContentInfo::GET_PTIME_METHOD, true);

        // getMaxPTime
        mMaxPTime = (int) JNIUtil::callIntMethod(env, contentInfo, JNIStreamContentInfo::getGetMaxPtimeMID());
        JNIUtil::checkException(env, JNIStreamContentInfo::GET_MAXPTIME_METHOD, true);

        // isVideo
        mIsVideo = JNIUtil::callBooleanMethod(env, contentInfo, JNIStreamContentInfo::getIsVideoMID()) == 1;
        JNIUtil::checkException(env, JNIStreamContentInfo::IS_VIDEO_METHOD, true);

        // getCNAME
        jstring cname = (jstring) JNIUtil::callObjectMethod(env, contentInfo, JNIStreamContentInfo::getGetCnameMID());
        JNIUtil::checkException(env, JNIStreamContentInfo::GET_CNAME_METHOD, true);

        if (cname != NULL) {
            const char* cnameStr(env->GetStringUTFChars(cname, 0));
            mCNAME = cnameStr;
            env->ReleaseStringUTFChars(cname, cnameStr);
            env->DeleteLocalRef((jobject) cname);
        }

        // getAudioPayload
        jobject audioPayload = JNIUtil::callObjectMethod(env, contentInfo,
                JNIStreamContentInfo::getGetAudioPayloadMID());
        JNIUtil::checkException(env, JNIStreamContentInfo::GET_AUDIO_PAYLOAD_METHOD, true);
        if (audioPayload != NULL) {
            mAudioPayload.reset(new RTPPayload(audioPayload, env));
            env->DeleteLocalRef(audioPayload);
        }

        // getVideoPayload
        jobject videoPayload = JNIUtil::callObjectMethod(env, contentInfo,
                JNIStreamContentInfo::getGetVideoPayloadMID());
        JNIUtil::checkException(env, JNIStreamContentInfo::GET_VIDEO_PAYLOAD_METHOD, true);

        if (videoPayload != NULL) {
            mVideoPayload.reset(new RTPPayload(videoPayload, env));
            env->DeleteLocalRef(videoPayload);
        }

        // getDTMFPayload
        jobject dtmfPayload = JNIUtil::callObjectMethod(env, contentInfo, JNIStreamContentInfo::getGetDtmfPayloadMID());
        JNIUtil::checkException(env, JNIStreamContentInfo::GET_DTMF_PAYLOAD_METHOD, true);

        if (dtmfPayload != NULL) {
            mDTMFPayload.reset(new RTPPayload(dtmfPayload, env));
            env->DeleteLocalRef(dtmfPayload);
        }

        //getPayloads
        jobjectArray payloads = (jobjectArray) JNIUtil::callObjectMethod(env, contentInfo,
                JNIStreamContentInfo::getGetPayloadsMID());
        JNIUtil::checkException(env, JNIStreamContentInfo::GET_PAYLOADS_METHOD, true);

        if (payloads == NULL) {
            JLogger::jniLogTrace(env, CLASSNAME, "NO PAYLOADS SENT");
        } else {
            mPayloadSize = env->GetArrayLength((jarray) payloads);
            JNIUtil::checkException(env, GET_ARRAY_LENGTH, true);
            if (mPayloadSize == 0) {
                JLogger::jniLogTrace(env, CLASSNAME, "NO PAYLOADS SENT, SIZE=0");
            } else {
                for (int i = 0; i < mPayloadSize; i++) {
                    jobject payload(env->GetObjectArrayElement(payloads, i));
                    JNIUtil::checkException(env, GET_OBJECT_ARRAY_ELEMENT, true);
                    mPayloads.push_back(new RTPPayload(payload, env));

                    env->DeleteLocalRef(payload);
                }
            }
            env->DeleteLocalRef(payloads);
        }
    } catch (std::exception& e) {
        JLogger::jniLogError(env, CLASSNAME, "%s", e.what());
        throw;
    }

    JLogger::jniLogDebug(env, CLASSNAME, "StreamContentInfo - created at %#x", this);
}

StreamContentInfo::~StreamContentInfo()
{
    JNIEnv* env = JNIUtil::getJavaEnvironment();

    JNIUtil::deleteGlobalRef(env, mContentType);

    JLogger::jniLogDebug(env, CLASSNAME, "~StreamContentInfo - deleted at %#x", this);
}

void foldToLower(base::String & str)
{
#if 0 // ost::String
    char * text = str.getText();
    for(unsigned int i=0;i<str.length();i++) {
        text[i] = tolower(text[i]);
    }
#else
    std::transform(str.begin(), str.end(), str.begin(), std::ptr_fun((int (*)(int))std::tolower));
#endif
}RTPPayload* StreamContentInfo::getPayload(const base::String& codec)
{
    for (int i = 0; i < mPayloadSize; i++) {
        base::String moCodec(codec);
        foldToLower(moCodec);

        base::String payloadCodec(mPayloads[i].getCodec());
        foldToLower(payloadCodec);

        if (moCodec == payloadCodec) {
            return &mPayloads[i];
        }
    }
    return NULL;
}

RTPPayload* StreamContentInfo::getAudioPayload()
{
    return mAudioPayload.get();
}

bool StreamContentInfo::isVideo()
{
    return mIsVideo;
}

RTPPayload* StreamContentInfo::getVideoPayload()
{
    return mVideoPayload.get();
}

RTPPayload& StreamContentInfo::getDTMFPayload()
{
    return *mDTMFPayload;
}

RTPPayload* StreamContentInfo::getCNPayload()
{
    return mCNPayload.get();
}

jobject StreamContentInfo::getContentType()
{
    return mContentType;
}

base::String& StreamContentInfo::getFileExtension()
{
    return mFileExtension;
}

int StreamContentInfo::getPTime()
{
    return mPTime;
}

int StreamContentInfo::getMaxPTime()
{
    return mMaxPTime;
}

base::String& StreamContentInfo::getCNAME()
{
    return mCNAME;
}
