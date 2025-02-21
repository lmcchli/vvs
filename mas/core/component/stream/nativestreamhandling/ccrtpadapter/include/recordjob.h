/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef RECORDJOB_H_
#define RECORDJOB_H_

#include <base_std.h>

#include <ccrtp/rtp.h>

#ifndef WIN32
#include <sys/time.h>
#else
#include <time.h>
#endif //WIN32
#include <boost/scoped_ptr.hpp>
#include "stackeventdispatcher.h"
#include "recordingproperties.h"
#include "mediabuilder.h"
#include "videomediadata.h"
#include "jni.h"

namespace java {
class MediaObject;
class RTPPayload;
class StreamContentInfo;
class StreamConfiguration;
};

class AudioMediaData;
class StreamRTPSession;
class JavaMediaStream;
class InboundSession;

/**
 * Records media from the given rtp session into the given media object.
 * 
 * @author Jorgen Terner
 */
class RecordJob
{
private:
    RecordJob(RecordJob &rhs);
    RecordJob& operator=(const RecordJob& rhs);

    /** Used to send events to Java-space. */
    jobject mEventNotifier;

    InboundSession & mSession;

    bool mIsRecording;

    /** Identifies the call that issued this play. */
    jobject mCallId;

    /** Describes how the recording should be done. */
    boost::scoped_ptr<RecordingProperties> mProperties;

    /** Current number of received audio packets. */
    uint32 mNumberOfReceivedAudioPackets;

    /** Total amount of recorded audio in milli seconds. */
    unsigned mTotalAmountOfAudioTime;

    /** Audio data received so far. */
    boost::ptr_list<AudioMediaData> mAudioData;

    /** Video data received so far. */
    boost::ptr_list<boost::ptr_list<VideoMediaData> > mVideoData;

    /** Video packets in the current frame received so far. */
    boost::ptr_list<VideoMediaData> mCurrentFrameData;

    /** 
     * <code>true</code> if the media is video, <code>false</code> if only 
     * audio. 
     */
    bool mRecordVideo;

    /** 
     * Media destination. This object stores the data in a Java instance which
     * is not destroyed when this instance is deleted, thus the auto-pointer
     * is ok here. 
     */
    boost::scoped_ptr<java::MediaObject> mMediaObject;

    /** Used to build the resulting media. */
    std::auto_ptr<MediaBuilder> mBuilder;

    bool storeValidRecording(bool removeSilence);

    /**
     * Store the audio header and audio data into the Java MediaObject this 
     * instance wraps. All <code>AppDataUnit</code>s in the list will be 
     * removed from the list and deleted.
     */
    void storeAndReleaseAudio();

    /**
     * Store the audio and video data into the Java MediaObject this 
     * instance wraps. All elements in the lists will be removed 
     * and deleted.
     */
    void storeAndReleaseVideo();

    /**
     * Checks whether the maxRecordingDuration limit has been exceeded.
     * 
     * @return <code>true</code> if the limit has been exceeded.
     */
    bool maxRecordingDurationExceeded();

    /**
     * Checks whether the recording is to be aborted due to
     * silence detection limit exceeded.
     * 
     * @return <code>true</code> if the limit has been exceeded.
     */
    bool /*RecordJob::*/maxSilenceDurationExceeded();

    /**
     * Checks whether the minRecordingDuration limit has been reached
     * or not.
     * 
     * @return <code>true</code> if the limit has not been reached.
     */
    bool lessThanMinRecordingDuration();

    /**
     * Skips the first part of audio data according to configuration.
     * 
     * @param mediaData      Delete the first part of this media data.
     * @param removeSilence  Indicate if silence should be stripped 
     * from end of media data     
     *      
     */
    void trimAudioFile(boost::ptr_list<AudioMediaData>& mediaData, bool removeSilence);

    /**
     * Handles an audio RTP packet.
     * 
     * @param adu The new RTP packet.
     */
    void handleAudioPacket(std::auto_ptr<const ost::AppDataUnit>& adu);

    //    /**
    //     * Handles an audio PCMU RTP packet.
    //     *
    //     * @param adu The new RTP packet.
    //     * @return the audio duration of the packet in ms
    //     */
    //	unsigned handleAudioPcmuPacket(std::auto_ptr<const ost::AppDataUnit>& adu);
    //
    //
    //    /**
    //     * Handles an audio AMR RTP packet.
    //     *
    //     * @param adu The new RTP packet.
    //     * @return the audio duration of the packet in ms
    //     */
    //	unsigned handleAudioAmrPacket(std::auto_ptr<const ost::AppDataUnit>& adu);

    /**
     * Handles a video RTP packet.
     * 
     * @param adu The new RTP packet.
     */
    void handleVideoPacket(std::auto_ptr<const ost::AppDataUnit>& adu);

    /**
     * Caclulates the start offsets in time between the audio and
     * video data. This involves looking att RTCP SR data and specified skew
     * according to specified SkewMethod.
     */
    void calculateStartOffsets(boost::ptr_list<boost::ptr_list<VideoMediaData> >& videoData,
            boost::ptr_list<AudioMediaData>& audioData, uint32& audioStartTimeOffset, uint32& videoStartTimeOffset);

    /**
     * @param Frame payload.
     * 
     * @return <code>true</code> if the payload is an I-frame.
     */
    bool isIFrame(const uint8 *payload);

public:
    /**
     * Creates the job. Note that this Job-instance claims ownership over
     * the given <code>options</code> and <code>mediaObject</code>. They
     * will be deleted when this instance is deleted.
     * 
     * @param audioSession  Rtp session used to stream audio data on.
     * @param videoSession  Rtp session used to stream video data on.
     * @param eventNotifier Used to send events to Java-space.
     * @param callId        Identifies the call that issued this recording.
     * @param options       Describes how the recording should be done.
     * @param mediaObject   Media destination. 
     * @param contentInfo   Includes RTP payload type.
     * @param config        Configuration for the current session.
     * @param javaMediaStream Used as information in some events and to request
     *                        I-frames.
     */
    RecordJob(JNIEnv* env, InboundSession& session, jobject eventNotifier, jobject callId,
            std::auto_ptr<RecordingProperties>& properties, std::auto_ptr<java::MediaObject>& mediaObject);

    /* Documented in baseclass. */
    virtual void init();

    /**
     * Destructs this instance.
     */
    virtual ~RecordJob();

    /**
     * Tells that the job is stopped due to a deletion of the stream.
     * If enough data have been received, it will be stored in the
     * media object.
     */
    void streamDeleted();

    /* Doc in baseclass. */
    void stop();

    bool isRecording();

    /**
     * The stream has been abandoned, set appropriate cause and mark
     * the job as stopped.
     */
    void abandoned();

    /**
     * @return The record failed due to this cause.
     */
    StackEventDispatcher::RecordFailedEventCause getFailedCause();

    /**
     * @return The record finished due to this cause.
     */
    StackEventDispatcher::RecordFinishedEventCause getCause();

    bool succeeded();

    jobject getCallId();

    /* Doc in baseclass. */
    base::String getId();

    /**
     * Handles an RTP packet.
     * 
     * @param adu The new RTP packet.
     * @param isAudioPacket           <code>true</code> if the packet is 
     *                                received from the audio session.
     */
    void handlePacket(std::auto_ptr<const ost::AppDataUnit>& adu, bool isVideoPacket);

    void sendRecordFailed(StackEventDispatcher::RecordFailedEventCause cause, const char * msg, JNIEnv* env);

    void finishRecordingWithEvent(StackEventDispatcher::RecordFinishedEventCause cause, bool removeSilence);

    virtual void onTimerTick(uint64 timeref);
};

#endif /*RECORDJOB_H_*/
