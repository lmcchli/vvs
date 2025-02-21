/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
#ifndef PLAYJOB_H_
#define PLAYJOB_H_

#include <base_std.h>
#include <config.h> // F�r att __EXPORT ska vara definierad (Pointer.h)
#include <ccrtp/rtp.h>

#include <boost/scoped_ptr.hpp>
#include <boost/ptr_container/ptr_vector.hpp>

#include <jni.h>

#include "mediaparser.h"
#include "Callback.h"

class RtpBlockHandler;
class StreamRTPSession;
class OutboundSession;
class MediaEnvelope;

/**
 * Plays a media object on the given rtp session.
 * <p>
 * Note the <code>init</code>-method that must be called after the constructor.
 * 
 * @author Jorgen Terner
 */
class PlayJob
{
private:
    PlayJob(PlayJob& rhs);
    PlayJob& operator=(const PlayJob& rhs);

    enum STATE
    {
        PUT_ON_QUEUE, WAIT_FOR_PACKETS
    };

    STATE mState;

    /** Identifies the call that issued this play. */
    unsigned mRequestId;

    /** Describes how the play should be done. */
    int mPlayOption;

    /** 
     * This should normally be the one specified in mContentInfo but
     * the media file might say something else...
     */
    uint32 mPTime;

    /**
     * If this media has been played before and stopped, this is the location
     * in milliseconds where the play was stopped. The play will resume from 
     * this location.
     */
    long mCursor;

    /**
     * Start timestamp for audio.
     */
    uint32 mStartAudioTimeStamp;

    OutboundSession& mSession;
    /** 
     * <code>true</code> if the media is video, <code>false</code> if only 
     * audio. 
     */
    bool mPlayVideo;

    bool mIsPlaying;

    // Indicates if there was an error during initialization or not.
    bool mIsOk;

    boost::scoped_ptr<MediaEnvelope> m_mediaObject;

    /** 
     * The amount of passed play time (in ms).
     * This is the total time accumulated for all played media
     * objects in this job.
     */

    void playAudioOnly();
    void playVideo();

public:
    /**
     * Creates the play job and parses the media object.
     * 
     * @param audioSession  Rtp session used to stream audio data on.
     * @param videoSession  Rtp session used to stream video data on.
     * @param callId        Identifies the call that issued this play.
     * @param playOption    Describes how the play should be done.
     * @param cursor        If this media has been played before and stopped,
     *                      this is the location in milliseconds where the
     *                      play was stopped. The play will resume from this
     *                      location. <strong>NOT IMPLEMENTED YET</strong>.
     * @param mediaObject   Contains the media.
     * @param contentInfo   Includes RTP payload types.
     */
    PlayJob(JNIEnv* env, OutboundSession &session, unsigned requestId, int playOption, long cursor,
            std::auto_ptr<MediaEnvelope>& mediaObject);

    /**
     * Must be called before any other method to initiate the job.
     * This initializes the play job ...
     * 
     * @throws invalid_argument      If the mediaobject given in the 
     *                               constructor is mutable.
     * @throws runtime_error         If the encoding of the media is
     *                               not supported.
     * @throws out_of_range          If the mediaobject given in the
     *                               constructor is not long enough
     *                               to contain required information.
     * @throws MediaLibraryException If the mediaobject given in the 
     *                               constructor could not be parsed.
     */
    virtual void init();

    /**
     * Destructs this instance.
     */
    virtual ~PlayJob();

    /**
     * Gives the timestamp in milliseconds for the next packet that was about
     * to be scheduled when a play was stopped.
     * <p>
     * <strong>Note</strong> that is it only safe to call this method when
     * <code>isDone()==true</code>.
     * <p>
     * Note also that this method only gives relevant data when a play is done
     * due to a call to stop.
     * 
     * @return The timestamp in milliseconds for the next packet.
     */
    long getCursor();

    /**
     * Sets the cursor value which will be used in the next play.
     * 
     * @param cursor The next play will start after this number of 
     *               milliseconds. If the media is video, it will start
     *               at the closest intra frame.
     */
    void setCursor(long cursor);

    /**
     * Tells that the job is stopped due to a deletion of the stream.
     * Fires a PLAY_STREAM_DELETED event.
     */
    void streamDeleted();

    /** Cancels the job and fires a PLAY_CANCELLED event. */
    void cancel();

    /** Stops the job and fires a PLAY_STOPPED event. */
    void stop();

    /** Stops the job and fires a PLAY_STREAM_JOINED event. */
    void joined();

    /**
     * @return The play has ended due to this cause.
     */
    //    int getCause();
    void sendPlayFinished(int cause = Callback::OK);

    void sendPlayFailed(const char * msg);

    /* Doc in baseclass. */
    base::String getId();

    bool isDone();

    bool putPacketsOnQueue();

    void onTick();

    bool isVideo();

    // Determines if job was properly initialized or not.
    bool isOk();
};
#endif /*PLAYJOB_H_*/
