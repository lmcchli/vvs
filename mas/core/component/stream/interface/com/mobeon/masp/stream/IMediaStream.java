/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.execution_engine.session.ISession;

/**
 * Baseinterfase used to create and control media streams.
 *
 * @author Jörgen Terner
 */
public interface IMediaStream {
    /**
     * Methods to adjust to skew between the audio and video streams.
     * <ul>
     * <li>LOCAL - The skew is the time the audio stream is sent ahead the
     *     video stream.</li>
     * <li>RTCP - The audio and video streams are sent in "sync" and the skew
     *    is added to the calculated value in RTCP SR.</li>
     * <li>LOCAL_AND_RTCP - The audio stream is sent ahead of the video stream
     *     according to the skew and the skew is added to the calculated value
     *     in RTCP SR</li>
     * </ul>
     */
    public enum SkewMethod {
        LOCAL(0), RTCP(1), LOCAL_AND_RTCP(2);

        /** The corresponding decimal definition. */
        private final int decimalDef;

        SkewMethod(int decimalDef) {
            this.decimalDef = decimalDef;
        }

        /**
         * @return The corresponding decimal definition.
         */
        public int decimalDef() {
            return this.decimalDef;
        }
    }

    /**
     * Sets the skew between the audio and video streams for a stream object.
     * <p>
     * The skew is the number of milliseconds the audio is ahead of the video.
     * If the video is ahead of the audio, this is a negative value.
     *
     * @param method Defines how the skew will be used.
     * @param skew   The number of milliseconds the audio is ahead of the
     *               video.
     */
    public void setSkew(SkewMethod method, long skew);

    /**
     * Closes the endpoints and makes the stream unavailable for playing.
     * If the stream is not created or already deleted, nothing happens.
     */
    public void delete();

    /**
     * Stops the current ongoing operation.
     * <p>
     * In the case of a <code>play</code>-operation, the media stream retains
     * all internal data of the media object in case a new <code>play</code>
     * with the same media object is issued.
     *
     * @param callId Identifies the call that initiated the operation that
     *               shall be stopped. May not be <code>null</code>.
     *                    
     * @return In the case of a <code>play</code>-operation, the current cursor
     *         in milliseconds is returned. In the case of a
     *         <code>recording</code>-operation, <code>0</code> is always
     *         returned.
     *         
     * @throws IllegalArgumentException      If <code>callId</code> is 
     *                                       <code>null</code>.
     * @throws IllegalStateException         If this method is called before 
     *                                       <code>create</code>
     *                                       or if <code>delete</code> has been 
     *                                       called on this stream.
     * @throws StackException                If some other error occured.
     */
    public long stop(Object callId) throws StackException;

    /**
     * Gets the local RTP port for audio traffic. If this portnumber is
     * undefined, <code>-1</code> is returned.
     * <p>
     * This port is always defined after a successful call to 
     * <code>create</code>.
     *
     * @return The local RTP port for audio traffic, <code>-1</code> if this
     *         portnumber is undefined.
     */
    public int getAudioPort();

    /**
     * Gets the local RTCP port for audio traffic. If this portnumber is
     * undefined, <code>-1</code> is returned.
     * <p>
     * This port is always defined after a successful call to 
     * <code>create</code>.
     *
     * @return The local RTCP port for audio traffic, <code>-1</code> if this
     *         portnumber is undefined.
     */
    public int getAudioControlPort();

    /**
     * Gets the local RTP port for video traffic. If this portnumber is
     * undefined, <code>-1</code> is returned.
     * <p>
     * This port is always defined after a successful call to 
     * <code>create</code> when the media properties denotes a video session.
     *
     * @return The local RTP port for video traffic, <code>-1</code> if this
     *         portnumber is undefined.
     */
    public int getVideoPort();

    /**
     * Gets the local RTCP port for video traffic. If this portnumber is
     * undefined, <code>-1</code> is returned.
     * <p>
     * This port is always defined after a successful call to 
     * <code>create</code> when the media properties denotes a video session.
     *
     * @return The local RTCP port for video traffic, <code>-1</code> if this
     *         portnumber is undefined.
     */
    public int getVideoControlPort();

    /**
     * Sets the event dispatcher that should be used to send events
     * to other components.
     * 
     * @param eventDispatcher The event dispatcher. May not be 
     *                        <code>null</code>.
     *                        
     * @throws IllegalArgumentException If <code>eventDispatcher</code>
     *         is <code>null</code>.
     */
    public void setEventDispatcher(IEventDispatcher eventDispatcher);

    /**
     * Gets the event dispatcher that should be used to send events
     * to other components.
     * 
     * @return The event dispatcher. Is <code>null</code> if 
     *         <code>setEventDispatcher</code> has not been called.
     */
    public IEventDispatcher getEventDispatcher();

    /**
     * Sets the canonical name used as identifier in the RTP packets
     * sent by this stream. See RFC 1550 for more information about
     * CNAME.
     * 
     * @param name Canonical name used in this stream.
     */
    public void setCNAME(String name);

    /**
     * Sets the session information {@link ISession} in order to
     * have session information in the logs.
     * This is relevant only when "spawning" threads.
     * @param callSession the session information.
     */
    public void setCallSession(ISession callSession);

    /**
     * Sends a FIR on an outbound stream, requesting a
     * full-intra frame for the inbound video stream with
     * SSRC <param>ssrc</param>
     * @param ssrc SSRC of media sender
     */
    public void sendPictureFastUpdate(int ssrc);

    /**
     * Gets SSRC from the inbound video stream
     * @return SSRC of stream
     */
    public int getSenderSSRC();

}