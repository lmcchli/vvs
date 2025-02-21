/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.stream.IOutboundMediaStream.PlayOption;

/**
 * An instance of this class can be used to play a sequence of 
 * MediaObjects. This class keeps track of the next MediaObject that
 * shall be played and when a play should be considered as finished.
 * <p>
 * A play is finished if:
 * <ul>
 * <li>All elements have been played.</li>
 * <li>Play of one element finished with CAUSE != PLAY_FINISHED.</li>
 * <li>Play of one element failed.</li>
 * </ul>
 * <p>
 * Class motivation: By using this class, there is no need to create 
 * a new thread at the Java-side when a sequence of MediaObjects shall
 * be played. Normally the asynchronous part of the operation is handled
 * by threads created at the native side (so that Java threads execute 
 * as little native code as possible). Playing a sequence of MediaObjects
 * requires some way of grouping several asynchronous operations together.
 * One way is to create a new thread that waits for each asynchronous part 
 * to finish. Another way is using a class like this.
 * 
 * @author Jörgen Terner
 */
/* package */ final class MediaObjectPlaySequence {
    private static final ILogger LOGGER =
        ILoggerFactory.getILogger(MediaObjectPlaySequence.class);
    
    /** The OutboundStream that received the play request. */
    private OutboundMediaStreamImpl mOutboundStream;
    
    /** The original request id. */
    private Object mRequestId;
    
    /** <code>true</code> if the play has finished/failed. */
    private boolean mHasFinished = false;
    
    /** The sequence of media objects that shall be played. */
    private IMediaObject[] mSequence;
    
    /** Index of next media object to play. */
    private int mNextMediaObjectIndex = 0;
    
    private PlayOption mPlayOption;
    
    private long mCursor;
    
    MediaObjectPlaySequence(
            OutboundMediaStreamImpl outboundStream,
            Object requestId, 
            IMediaObject[] sequence,
            PlayOption playOption,
            long cursor) {
        if(LOGGER.isDebugEnabled())
            LOGGER.debug("Constructor");
        mOutboundStream = outboundStream;
        mRequestId = requestId;
        mSequence = sequence;
        mPlayOption = playOption;
        mCursor = cursor;
    }
    
    /**
     * Clear references to avoid memory-leaks.
     */
    private void cleanUp() {
        mHasFinished = true;
        mOutboundStream = null;
        mSequence = null;
    }
    
    /**
     * Reduced the cursor according to skipped media length and plays
     * the next media object.
     * <p>
     * Note that this method should not throw any exceptions when it
     * will be called from native code.
     *
     * @param lengthInMs Milliseconds already skipped.
     */
    void skippedMedia(long lengthInMs) {
        mCursor -= lengthInMs;
        try {
            playNext();
        }
        catch (StackException e) {
            // No exceptions may be thrown from this method because
            // it might be executen within a native thread.
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("Exception during play", e);
        }
        catch (Throwable t) {
            // No exceptions may be thrown from this method because
            // it might be executen within a native thread.
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("Exception during play", t);
        }
    }
    
    /**
     * Tells that the cursor has played out its role, that is,
     * all media that shall be skipped has been skipped.
     */
    /* package */ void skippedPartOfMedia() {
        mCursor = 0;
    }

    /**
     * Plays the next MediaObject in the sequence if there is one, 
     * otherwise a <code>PlayFinished</code>-event is fired.
     * 
     * @throws StackException If an error occured.
     */
    void playNext() throws StackException {
        if(LOGGER.isDebugEnabled())
            LOGGER.debug("About to play next media object in sequence: " + mNextMediaObjectIndex);
        if (!mHasFinished) {
            // Check if we have reached end of media
            // or if the stream, for some reason, is not playable anymore (deleted, cancelled etc).
            if (mNextMediaObjectIndex == mSequence.length || !mOutboundStream.isMultiPlay()) {
                if(LOGGER.isDebugEnabled())
                    LOGGER.debug("MediaObjectPlaySequence::playNext: Done.");
                if (mNextMediaObjectIndex == mSequence.length) {
                    if(LOGGER.isDebugEnabled())
                        LOGGER.debug("Cause: End of sequence is reached.");
                }
                if (!mOutboundStream.isMultiPlay()) {
                    if(LOGGER.isDebugEnabled())
                        LOGGER.debug("Cause: No longer multiplay.");
                }
                mOutboundStream.getEventNotifier().playFinished(mRequestId,
                        PlayFinishedEvent.CAUSE.PLAY_FINISHED, 0);
                cleanUp();
            }
            else {
                IMediaObject nextMediaObject = mSequence[mNextMediaObjectIndex++];
                    
                if(LOGGER.isDebugEnabled())
                    LOGGER.debug("MediaObjectPlaySequence::playNext: Playing next " +
                        "in sequence: " + mNextMediaObjectIndex);
                try {
                    mOutboundStream.play(this, nextMediaObject, mPlayOption, mCursor);
                } catch (IllegalStateException e) {
                    if(LOGGER.isDebugEnabled())
                        LOGGER.debug("Caught an exception in play next ...", e);
                    requestFailed("Caught IllegalStateException");
                }
            }
        }
    }
    
    /**
     * This method shall be called instead of dispatching an event
     * whenever the requestId is an instance of this class. This 
     * instance must descide when an event is to be dispatched.
     * <p>
     * Note that this method should not throw any exceptions when it
     * will be called from native code.
     * 
     * @param cause Event cause.
     */
    void requestFinished(PlayFinishedEvent.CAUSE cause, long cursor) {
        if(LOGGER.isDebugEnabled())
            LOGGER.debug("Handling play finish of media object sequence play");
        if (cause != PlayFinishedEvent.CAUSE.PLAY_FINISHED) {
            // The play should only continue with the next element in the
            // sequence if the last element played successfully.
            if(LOGGER.isDebugEnabled())
                LOGGER.debug("MediaObjectPlaySequence::requestFinished: " +
                    "Play of sequence interrupted, cause=" + cause.toString());
            mOutboundStream.getEventNotifier().playFinished(
                    mRequestId, cause, cursor);
            cleanUp();
        }
        else {
            try {
                playNext();
            }
            catch (StackException e) {
                // No exceptions may be thrown from this method because
                // it might be executen within a native thread.
                if(LOGGER.isDebugEnabled())
                    LOGGER.debug("Exception during play", e);
            }
            catch (Throwable t) {
                // No exceptions may be thrown from this method because
                // it might be executen within a native thread.
                if(LOGGER.isDebugEnabled())
                    LOGGER.debug("Exception during play", t);
            }
        }
    }

    /**
     * This method shall be called instead of dispatching an event
     * whenever the requestId is an instance of this class. This 
     * instance must descide when an event is to be dispatched.
     * 
     * @param message Event description.
     */
    void requestFailed(String message) {
        if(LOGGER.isDebugEnabled())
            LOGGER.debug("MediaObjectPlaySequence::requestFailed: " +
                "Play of sequence interrupted, message=" + message);
        mOutboundStream.getEventNotifier().playFailed(mRequestId, message);
        cleanUp();
    }
}
