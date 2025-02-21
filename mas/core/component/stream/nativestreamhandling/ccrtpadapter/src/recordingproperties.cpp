/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#include <base_std.h> 

#include "recordingproperties.h"
#include "jniutil.h"
#include "jlogger.h"
#include "jnirecordingproperties.h"

using namespace std;
using namespace ost;

static const char* CLASSNAME = "masjni.ccrtpadapter.RecordingProperties";

RecordingProperties::RecordingProperties(JNIEnv* env, jobject properties) :
        mMaxRecordingDuration(-1), mMaxSilence(-1), mTimeout(-1)
{
    try {
        // isSilenceDetectionForStart
        mSilenceDetectionForStart = JNIUtil::callBooleanMethod(env, properties, JNIRecordingProperties::getIsSilenceDetectionForStartMID()) == JNI_TRUE;
        JNIUtil::checkException(env, JNIRecordingProperties::IS_SILENCE_DETECTION_FOR_START_METHOD, true);

        // isSilenceDetectionForStop
        mSilenceDetectionForStop = JNIUtil::callBooleanMethod(env, properties, JNIRecordingProperties::getIsSilenceDetectionForStopMID()) == JNI_TRUE;
        JNIUtil::checkException(env, JNIRecordingProperties::IS_SILENCE_DETECTION_FOR_STOP_METHOD, true);

        // getMaxRecordingDuration
        mMaxWaitBeforeRecord = (int) JNIUtil::callIntMethod(env, properties, JNIRecordingProperties::getMaxWaitBeforeRecordMID());
        JNIUtil::checkException(env, JNIRecordingProperties::GET_MAX_WAIT_BEFORE_RECORD_METHOD, true);

        // getMaxRecordingDuration
        mMaxRecordingDuration = (int) JNIUtil::callIntMethod(env, properties, JNIRecordingProperties::getMaxRecordingDurationMID());
        JNIUtil::checkException(env, JNIRecordingProperties::GET_MAX_RECORDING_DURATION_METHOD, true);
        JLogger::jniLogTrace(env, CLASSNAME, "mMaxRecordingDuration=%d", mMaxRecordingDuration);

        // getMinRecordingDuration
        mMinRecordingDuration = (int) JNIUtil::callIntMethod(env, properties, JNIRecordingProperties::getMinRecordingDurationMID());
        JNIUtil::checkException(env, JNIRecordingProperties::GET_MIN_RECORDING_DURATION_METHOD, true);

        // getMaxSilence
        mMaxSilence = (int) JNIUtil::callIntMethod(env, properties, JNIRecordingProperties::getMaxSilenceMID());
        JNIUtil::checkException(env, JNIRecordingProperties::GET_MAX_SILENCE_METHOD, true);

        mTimeout = (int) JNIUtil::callIntMethod(env, properties, JNIRecordingProperties::getTimeoutMID());
        JNIUtil::checkException(env, JNIRecordingProperties::GET_TIMEOUT_METHOD, true);

        JLogger::jniLogDebug(env, CLASSNAME, "RecordingProperties - create at %#x", this);
    } catch (exception& e) {
        JLogger::jniLogError(env, CLASSNAME, "%s", e.what());
        throw;
    }
}

RecordingProperties::~RecordingProperties()
{
    JLogger::jniLogDebug(JNIUtil::getJavaEnvironment(), CLASSNAME, "~RecordingProperties - delete at %#x", this);
}
