/* Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef STREAMCONFIGURATION_H_
#define STREAMCONFIGURATION_H_

#include "jni.h"
#include <base_std.h>
#include <base_include.h>

/**
 * Wrapper class for a Java Configuration class that hides the JNI details.
 * <p>
 * This class holds the current, last updated, configuration. All sessions
 * should request their own copy of the current configuration by calling the
 * <code>copy</code>-method.
 * 
 * @author J�rgen Terner
 */
namespace java {
class StreamConfiguration
{
private:
    /** Number of threads in the threadpool. */
    int mThreadPoolSize;

    /** 
     * The number of microseconds a session waits for data to arrive
     * on a socket 
     */
    long mPacketPendTimeoutMicrosec;

    /**
     * The number of milliseconds packets will be dispatched ahead
     * of their timestamp.
     */
    long mSendPacketsAheadMs;

    /** Timeout to expire unsent packets in milliseconds. */
    long mExpireTimeoutMs;

    /** 
     * Maximum time in milliseconds a record will wait for the
     * first I-frame before starting to record. 
     */
    long mMaxWaitForIFrameTimeoutMs;

    /** 
     * If no RTP-packets are received during this timeout period (milliseconds)
     * an AbandonedStreamDetected-event is fired.
     */
    long mAbandonedStreamDetectedTimeoutMs;

    /** Maximum payload segment size before fragmenting sends. */
    size_t mMaximumTransmissionUnit;

    /** The current configuration instance. */
    static StreamConfiguration* mInstance;

    /**
     * Fraction of the total control bandwith to be dedicated 
     * to senders reports (0 - 1).
     */
    float mSendersControlFraction;

    /**
     * Amount of audio to skip while recording (milliseconds). This is
     * used to avoid hearing the "beep" first in the recording.
     */
    long mAudioSkipMs;

    /**
     * Amount of audio to replace with silence while recording video 
     * (milliseconds). This is used to avoid hearing the "beep" first
     * in the recording.
     */
    long mAudioReplaceWithSilenceMs;

    /** Maximum wait time for threads in the threadpool. */
    long mThreadPoolMaxWaitTimeSec;

    /**
     * If <code>true</code> DTMF events are dispatched on key down, if 
     * <code>false</code> DTMF events are dispatched on key up.
     */
    bool mDispatchDTMFOnKeyDown;

    /**
     * If <code>true</code>, all RTP session is served by pooled threads,
     * if <code>false</code>, all RTP session creates its own thread.
     */
    bool mUsePoolForRTPSessions;

    /** Name of local host, for example "0.0.0.0" or "127.0.0.1". */
    base::String mLocalHostName;

    /**
     * Number of milliseconds the audio should be sent ahead of the video. 
     * If the video should be sent ahead of the audio, this should be a 
     * negative value.
     */
    long mSkew;

    /** Integer representation of the skew method. */
    int mSkewMethod;

    /** 0 == MVAS (MDAT first), 1 == MAS (MOOV first) **/
    int mMovFileVersion;

    int mSilenceDetectionMode;
    int mThreshold;
    int mInitialSilenceFrames;
    int mDetectionFrames;
    int mSilenceDeadband;
    int mSignalDeadband;
    int mSilenceDetectionDebugLevel;

    /**
     * Creates a new empty configuration.
     */
    StreamConfiguration();

    static void copy(StreamConfiguration &src, StreamConfiguration& copy);

public:

    StreamConfiguration(StreamConfiguration &rhs);

    static StreamConfiguration& instance();
    /**
     * Initiates the configuration.
     * <p>
     * Note that this method should only be called once at system startup.
     */
    static void init();

    /**
     * Destroys the current configuration instance.
     * <p>
     * Note that this method should only be called once at system shutdown.
     */
    static void cleanUp();

    /**
     * Copies all current configuration values into the given
     * configuration instance.
     * 
     * @param copy Configuration destination.
     */
    static std::auto_ptr<StreamConfiguration> clone();

    /**
     * Updates the current configuration.
     * 
     * @param configuration Java object containing the configuration info.
     * @param env           Reference to Java environment.
     * 
     * @throws StackException If an error occured.
     */
    static void update(jobject configuration, JNIEnv* env);

    /**
     * Destructor.
     */
    ~StreamConfiguration();

    /** Gets the threadpool size. */
    int getThreadPoolSize();

    /** 
     * @return The number of microseconds a session waits for data to arrive
     *         on a socket
     */
    long getPacketPendTimeout();

    /**
     * @return The number of milliseconds packets will be dispatched ahead
     *         of their timestamp.
     */
    long getSendPacketsAhead();

    /** Gets the timeout to expire unsent packets in milliseconds. */
    long getExpireTimeout();

    /** Maximum payload segment size before fragmenting sends. */
    size_t getMaximumTransmissionUnit();

    /**
     * @return Maximum silent period in milliseconds before a stream
     *         is considered as abandoned (silence = no RTP-packets).
     */
    long getAbandonedStreamDetectedTimeout();

    /**
     * Gets the fraction of the total control bandwith to be dedicated 
     * to senders reports.
     * <p>
     * Of course, 1 - fraction will be dedicated to receivers reports.
     * 
     * @return fraction Fraction of bandwidth, between 0 an 1.
     */
    float getSendersControlFraction();

    /**
     * Gets the amount of audio to skip while recording. 
     * This is used to avoid hearing the "beep" first in the recording.
     * 
     * @return Amount of audio to skip in milliseconds.
     */
    long getAudioSkip();

    /**
     * Gets the amount of audio data that is replaced by silence
     * in a video stream during record. This is used to avoid hearing 
     * the "beep" first in the recording.
     * 
     * @return Amount of audio to replace in milliseconds.
     */
    long getAudioReplaceWithSilence();

    /**
     * @return Maximum wait time in seconds for the threads in the threadpool.
     */
    long getThreadPoolMaxWaitTime();

    /**
     * @return <code>true</code> if DTMF events are dispatched on key down.
     */
    bool isDispatchDTMFOnKeyDown();

    /**
     * @return <code>true</code> if all RTP session is served by pooled 
     *         threads, <code>false</code> if all RTP session creates its 
     *         own thread.
     */
    bool isUsePoolForRTPSessions();

    /**
     * Assigns the local host name to the given parameter.
     * 
     * @param name Name of local host, for example "0.0.0.0" or "127.0.0.1".
     */
    void getLocalHostName(base::String& name);

    /**
     * @return Number of milliseconds the audio should be sent ahead of the 
     *         video. If the video should be sent ahead of the audio, this 
     *         should be a negative value.
     */
    long getSkew();

    /**
     * @return The integer representation of the skew method.
     */
    int getSkewMethod();

    /**
     * @return Maximum time in milliseconds a record will wait for the
     *         first I-frame before starting to record. 
     */
    long getMaxWaitForIFrameTimeout();

    /**
     * @return the version of MOV file which is to be used (currently the order of
     *         MOOV and MDAT depends on this version number).
     */
    int getMovFileVersion();

    int getSilenceDetectionMode();
    int getThreshold();
    int getInitialSilenceFrames();
    int getDetectionFrames();
    int getSilenceDeadband();
    int getSignalDeadband();
    int getSilenceDetectionDebugLevel();
};
}
;
#endif /*STREAMCONFIGURATION_H_*/
