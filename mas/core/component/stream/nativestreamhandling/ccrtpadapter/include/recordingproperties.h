/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef RECORDINGPROPERTIES_H_
#define RECORDINGPROPERTIES_H_

#include <config.h> // For att __EXPORT ska vara definierad (Pointer.h)
#include "jni.h"
#include <ccrtp/rtp.h>

/**
 * Wrapper class for a Java RecordingProperties instance that hides the JNI 
 * details.
 * 
 * @author Jorgen Terner
 */
class RecordingProperties
{
private:
    /**
     * If <code>true</code>, recording will start when silence stops.
     * If <code>false</code>, recording will start immediately.
     */
    bool mSilenceDetectionForStart;

    /**
     * If <code>true</code>, recording will stop when silence is detected.
     */
    bool mSilenceDetectionForStop;

    /**
     * Maximum wait time in milliseconds before recording has started.
     */
    int mMaxWaitBeforeRecord;

    /**
     * Maximum recording duration in milliseconds. If the recording duration
     * exceeded the maximum specified, the recording will stop and only
     * the media recording up to the maximum duration will be saved.
     */
    int mMaxRecordingDuration;

    /**
     * Minimum recording duration in milliseconds. If the recording duration
     * is less than specified, the result of the recording is considered
     * being "No Recording".
     */
    int mMinRecordingDuration;

    /**
     * Maximum silence time in milliseconds before recording automatically stops.
     */
    int mMaxSilence;

    int mTimeout;

public:
    /**
     * Creates a new RecordingProperties and reads necessary info 
     * from the given Java object.
     * 
     * @param env         Reference to Java environment.
     * @param properties  Java object containing the properties info.
     */
    RecordingProperties(JNIEnv* env, jobject properties);

    /**
     * Destructor.
     */
    ~RecordingProperties();

    bool getSilenceDetectionForStart();
    bool getSilenceDetectionForStop();
    int getMaxWaitBeforeRecord();
    int getMaxRecordingDuration();
    int getMinRecordingDuration();
    int getMaxSilence();
    int getTimeout();
};

inline bool RecordingProperties::getSilenceDetectionForStart()
{
    return mSilenceDetectionForStart;
}

inline bool RecordingProperties::getSilenceDetectionForStop()
{
    return mSilenceDetectionForStop;
}

inline int RecordingProperties::getMaxWaitBeforeRecord()
{
    return mMaxWaitBeforeRecord;
}

inline int RecordingProperties::getMaxRecordingDuration()
{
    return mMaxRecordingDuration;
}

inline int RecordingProperties::getMinRecordingDuration()
{
    return mMinRecordingDuration;
}

inline int RecordingProperties::getMaxSilence()
{
    return mMaxSilence;
}

inline int RecordingProperties::getTimeout()
{
    return mTimeout;
}
#endif /*RECORDINGPROPERTIES_H_*/
