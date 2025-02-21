/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
//#include "threads_map.h"
#include "streamconfiguration.h"
#include "formathandlerfactory.h"
#include "jlogger.h"
#include "jniutil.h"
#include "jnistreamconfiguration.h"

#include <base_std.h> 

using namespace std;

//TODO: Add more Silence Detection parameters here 
static const char* CLASSNAME = "masjni.ccrtpadapter.StreamConfiguration";

using namespace java;

StreamConfiguration* StreamConfiguration::mInstance = NULL;

StreamConfiguration::StreamConfiguration(StreamConfiguration &rhs)
{
    copy(rhs, *this);
}

StreamConfiguration::StreamConfiguration() :
        mThreadPoolSize(0), mPacketPendTimeoutMicrosec(0), mExpireTimeoutMs(0), mAbandonedStreamDetectedTimeoutMs(0),
        mMaximumTransmissionUnit(0), mSendersControlFraction(0.0f), mAudioSkipMs(0), mAudioReplaceWithSilenceMs(0),
        mThreadPoolMaxWaitTimeSec(0), mDispatchDTMFOnKeyDown(true), mLocalHostName(""), mSkew(0), mSilenceDetectionMode(0),
        mThreshold(0), mInitialSilenceFrames(40), mDetectionFrames(10), mSilenceDeadband(150), mSignalDeadband(10),
        mSilenceDetectionDebugLevel(0)
{
}

void StreamConfiguration::update(jobject configuration, JNIEnv* env)
{
    try {
        // Fetching configuration

        // getThreadPoolSize
        int threadPoolSize = (int) JNIUtil::callIntMethod(env, configuration,
                JNIStreamConfiguration::getGetThreadPoolSizeMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_THREADPOOL_SIZE_METHOD, true);

        // getPacketPendTimeout
        long packetPendTimeout = (long) JNIUtil::callIntMethod(env, configuration,
                JNIStreamConfiguration::getGetPacketEndTimeoutMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_PACKETPEND_TIMEOUT_METHOD, true);

        // getSendPacketsAhead
        long sendPacketsAheadMs = (long) JNIUtil::callIntMethod(env, configuration,
                JNIStreamConfiguration::getGetSendPacketsAheadMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_SEND_PACKETS_AHEAD_METHOD, true);

        // getExpireTimout
        long expireTimeoutMs = (long) JNIUtil::callIntMethod(env, configuration,
                JNIStreamConfiguration::getGetExpireTimeoutMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_EXPIRE_TIMEOUT_METHOD, true);

        // getMaximumTransmissionUnit
        long maximumTransmissionUnit = (long) JNIUtil::callIntMethod(env, configuration,
                JNIStreamConfiguration::getGetMtuMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_MTU_METHOD, true);

        // getAbandonedStreamDetectedTimeout
        long abandonedStreamDetectedTimeoutMs = (long) JNIUtil::callIntMethod(env, configuration,
                JNIStreamConfiguration::getGetAbandonedStreamTimeoutMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_ABANDONED_STREAM_TIMEOUT_METHOD, true);

        // getSendersControlFraction
        float fraction = (float) JNIUtil::callFloatMethod(env, configuration,
                JNIStreamConfiguration::getGetSendersControlFractionMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_SENDERS_CONTROL_FRACTION_METHOD, true);

        // getAudioSkip
        long audioSkip = (long) JNIUtil::callIntMethod(env, configuration,
                JNIStreamConfiguration::getGetAudioSkipMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_AUDIO_SKIP_METHOD, true);

        // getSkew
        long skew = (long) JNIUtil::callIntMethod(env, configuration, JNIStreamConfiguration::getGetSkewMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_SKEW_METHOD, true);

        // getSkewMethodIntRep
        int skewMethod = (int) JNIUtil::callIntMethod(env, configuration,
                JNIStreamConfiguration::getGetSkewIntRepMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_SKEW_INTREP_METHOD, true);

        // getGetAudioReplaceWithSilence
        long audioReplaceWithSilence = (long) JNIUtil::callIntMethod(env, configuration,
                JNIStreamConfiguration::getGetAudioReplaceWithSilenceMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_AUDIO_REPLACE_WITH_SILENCE_METHOD, true);

        // getThreadPoolMaxWaitTimeSec
        long threadPoolMaxWaitTimeSec = (long) JNIUtil::callIntMethod(env, configuration,
                JNIStreamConfiguration::getGetThreadPoolMaxWithTimeMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_THREADPOOL_MAX_WAIT_TIME_METHOD, true);

        // getMaxWaitForIFrameTimeout
        long maxWaitForIFrameTimeout = (long) JNIUtil::callIntMethod(env, configuration,
                JNIStreamConfiguration::getGetMaxWaitForIframeMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_MAX_WAIT_FOR_IFRAME_METHOD, true);

        // isDispatchDTMFOnKeyDown
        jboolean isDispatchDTMFOnKeyDown = JNIUtil::callBooleanMethod(env, configuration,
                JNIStreamConfiguration::getIsDispatchDtmfOnKeyDownMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::IS_DISPATCH_DTMF_ON_KEY_DOWN_METHOD, true);

        // isUsePoolForRTPSessions
        jboolean isUsePoolForRTPSessions = JNIUtil::callBooleanMethod(env, configuration,
                JNIStreamConfiguration::getIsUsePoolForSessionsMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::IS_USE_POOL_FOR_SESSIONS_METHOD, true);

        // getLocalHostName
        jstring localHostName = (jstring) JNIUtil::callObjectMethod(env, configuration,
                JNIStreamConfiguration::getGetLocalHostMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_LOCAL_HOST_METHOD, true);
        const char* localHostNameStr(env->GetStringUTFChars(localHostName, 0));

        // getMovFileVersion
        int movFileVersion = (int) JNIUtil::callIntMethod(env, configuration,
                JNIStreamConfiguration::getGetMovFileVersionMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_MOV_FILE_VERSION_METHOD, true);

        // getSilenceDetectionMode
        int silenceDetectionMode = (int) JNIUtil::callIntMethod(env, configuration,
                JNIStreamConfiguration::getGetSilenceDetectionModeMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_SILENCE_DETECTION_MODE_METHOD, true);

        // getSilenceThreshold
        int threshold = (int) JNIUtil::callIntMethod(env, configuration, JNIStreamConfiguration::getGetThresholdMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_THRESHOLD_METHOD, true);

        // getInitialSilenceFrames
        int initialSilenceFrames = (int) JNIUtil::callIntMethod(env, configuration,
                JNIStreamConfiguration::getGetInitialSilenceFramesMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_INITIAL_SILENCE_FRAMES_METHOD, true);

        // getDetectionFrames
        int detectionFrames = (int) JNIUtil::callIntMethod(env, configuration,
                JNIStreamConfiguration::getGetDetectionFramesMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_DETECTION_FRAMES_METHOD, true);

        // getSilenceDeadband
        int silenceDeadband = (int) JNIUtil::callIntMethod(env, configuration,
                JNIStreamConfiguration::getGetSignalDeadbandMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_SILENCE_DEADBAND_METHOD, true);

        // getSignalDeadband
        int signalDeadband = (int) JNIUtil::callIntMethod(env, configuration,
                JNIStreamConfiguration::getGetSignalDeadbandMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_SIGNAL_DEADBAND_METHOD, true);

        // getSilenceDetectionDebugLevel
        int silenceDetectionDebugLevel = (int) JNIUtil::callIntMethod(env, configuration,
                JNIStreamConfiguration::getGetSilenceDetectionDebugLevelMID());
        JNIUtil::checkException(env, JNIStreamConfiguration::GET_SILENCE_DETECTION_DEBUG_LEVEL_METHOD, true);

        // Update instance values
        mInstance->mThreadPoolSize = threadPoolSize;
        mInstance->mSendPacketsAheadMs = sendPacketsAheadMs;
        mInstance->mPacketPendTimeoutMicrosec = packetPendTimeout;
        mInstance->mExpireTimeoutMs = expireTimeoutMs;
        mInstance->mMaximumTransmissionUnit = maximumTransmissionUnit;
        mInstance->mAbandonedStreamDetectedTimeoutMs = abandonedStreamDetectedTimeoutMs;
        mInstance->mSendersControlFraction = fraction;
        mInstance->mAudioSkipMs = audioSkip;
        mInstance->mAudioReplaceWithSilenceMs = audioReplaceWithSilence;
        mInstance->mThreadPoolMaxWaitTimeSec = threadPoolMaxWaitTimeSec;
        mInstance->mDispatchDTMFOnKeyDown = isDispatchDTMFOnKeyDown == JNI_TRUE;
        mInstance->mUsePoolForRTPSessions = isUsePoolForRTPSessions == JNI_TRUE;
        mInstance->mLocalHostName = localHostNameStr;
        mInstance->mSkew = skew;
        mInstance->mSkewMethod = skewMethod;
        mInstance->mMaxWaitForIFrameTimeoutMs = maxWaitForIFrameTimeout;
        mInstance->mMovFileVersion = movFileVersion;
        mInstance->mSilenceDetectionMode = silenceDetectionMode;
        mInstance->mThreshold = threshold;
        mInstance->mInitialSilenceFrames = initialSilenceFrames;
        mInstance->mDetectionFrames = detectionFrames;
        mInstance->mSilenceDeadband = silenceDeadband;
        mInstance->mSignalDeadband = signalDeadband;
        mInstance->mSilenceDetectionDebugLevel = silenceDetectionDebugLevel;

        // Update format handler factory
        FormatHandlerFactory::setMovFileVersion(movFileVersion);

        env->ReleaseStringUTFChars(localHostName, localHostNameStr);
        env->DeleteLocalRef((jobject) localHostName);

        JLogger::jniLogTrace(env, CLASSNAME, "Configuration updated.");
        JLogger::jniLogTrace(env, CLASSNAME, "Abandoned stream timeout=%d",
                mInstance->mAbandonedStreamDetectedTimeoutMs);
        JLogger::jniLogTrace(env, CLASSNAME, "MOV file version=%d", mInstance->mMovFileVersion);
    } catch (exception& e) {
        JLogger::jniLogError(env, CLASSNAME, "Failed to update configuration: %s", e.what());
        return;
    }
}

StreamConfiguration::~StreamConfiguration()
{
}

StreamConfiguration& StreamConfiguration::instance()
{
    return *mInstance;
}

void StreamConfiguration::copy(StreamConfiguration &src, StreamConfiguration& copy)
{
    copy.mThreadPoolSize = src.mThreadPoolSize;
    copy.mPacketPendTimeoutMicrosec = src.mPacketPendTimeoutMicrosec;
    copy.mSendPacketsAheadMs = src.mSendPacketsAheadMs;
    copy.mExpireTimeoutMs = src.mExpireTimeoutMs;
    copy.mMaximumTransmissionUnit = src.mMaximumTransmissionUnit;
    copy.mAbandonedStreamDetectedTimeoutMs = src.mAbandonedStreamDetectedTimeoutMs;
    copy.mSendersControlFraction = src.mSendersControlFraction;
    copy.mAudioSkipMs = src.mAudioSkipMs;
    copy.mAudioReplaceWithSilenceMs = src.mAudioReplaceWithSilenceMs;
    copy.mThreadPoolMaxWaitTimeSec = src.mThreadPoolMaxWaitTimeSec;
    copy.mDispatchDTMFOnKeyDown = src.mDispatchDTMFOnKeyDown;
    copy.mUsePoolForRTPSessions = src.mUsePoolForRTPSessions;
    copy.mLocalHostName = src.mLocalHostName;
    copy.mSkew = src.mSkew;
    copy.mSkewMethod = src.mSkewMethod;
    copy.mMaxWaitForIFrameTimeoutMs = src.mMaxWaitForIFrameTimeoutMs;
    copy.mMovFileVersion = src.mMovFileVersion;
    copy.mSilenceDetectionMode = src.mSilenceDetectionMode;
    copy.mThreshold = src.mThreshold;
    copy.mInitialSilenceFrames = src.mInitialSilenceFrames;
    copy.mDetectionFrames = src.mDetectionFrames;
    copy.mSilenceDeadband = src.mSilenceDeadband;
    copy.mSignalDeadband = src.mSignalDeadband;
    copy.mSilenceDetectionDebugLevel = src.mSilenceDetectionDebugLevel;
}

std::auto_ptr<StreamConfiguration> StreamConfiguration::clone()
{
    std::auto_ptr<StreamConfiguration> result(new StreamConfiguration());
    StreamConfiguration& copy = *result;
    StreamConfiguration::copy(*mInstance, copy);
    return result;
}

int StreamConfiguration::getThreadPoolSize()
{
    return mThreadPoolSize;
}
long StreamConfiguration::getPacketPendTimeout()
{
    return mPacketPendTimeoutMicrosec;
}
long StreamConfiguration::getSendPacketsAhead()
{
    return mSendPacketsAheadMs;
}

long StreamConfiguration::getExpireTimeout()
{
    return mExpireTimeoutMs;
}
size_t StreamConfiguration::getMaximumTransmissionUnit()
{
    return mMaximumTransmissionUnit;
}

float StreamConfiguration::getSendersControlFraction()
{
    return mSendersControlFraction;
}

long StreamConfiguration::getAbandonedStreamDetectedTimeout()
{
    return mAbandonedStreamDetectedTimeoutMs;
}

long StreamConfiguration::getAudioSkip()
{
    return mAudioSkipMs;
}

long StreamConfiguration::getAudioReplaceWithSilence()
{
    return mAudioReplaceWithSilenceMs;
}

long StreamConfiguration::getThreadPoolMaxWaitTime()
{
    return mThreadPoolMaxWaitTimeSec;
}

void StreamConfiguration::init()
{
    mInstance = new StreamConfiguration();
}

void StreamConfiguration::cleanUp()
{
    delete mInstance;
    mInstance = NULL;
}

bool StreamConfiguration::isDispatchDTMFOnKeyDown()
{
    return mDispatchDTMFOnKeyDown;
}

bool StreamConfiguration::isUsePoolForRTPSessions()
{
    return mUsePoolForRTPSessions;
}

void StreamConfiguration::getLocalHostName(base::String& name)
{
    name = mLocalHostName;
}

long StreamConfiguration::getSkew()
{
    return mSkew;
}

int StreamConfiguration::getSkewMethod()
{
    return mSkewMethod;
}

long StreamConfiguration::getMaxWaitForIFrameTimeout()
{
    return mMaxWaitForIFrameTimeoutMs;
}

int StreamConfiguration::getMovFileVersion()
{
    return mMovFileVersion;
}

int StreamConfiguration::getSilenceDetectionMode()
{
    return mSilenceDetectionMode;
}

int StreamConfiguration::getThreshold()
{
    return mThreshold;
}

int StreamConfiguration::getInitialSilenceFrames()
{
    return mInitialSilenceFrames;
}

int StreamConfiguration::getDetectionFrames()
{
    return mDetectionFrames;
}

int StreamConfiguration::getSilenceDeadband()
{
    return mSilenceDeadband;
}

int StreamConfiguration::getSignalDeadband()
{
    return mSignalDeadband;
}

int StreamConfiguration::getSilenceDetectionDebugLevel()
{
    return mSilenceDetectionDebugLevel;
}
