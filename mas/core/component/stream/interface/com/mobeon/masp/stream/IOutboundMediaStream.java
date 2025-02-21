/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import java.util.Collection;

import com.mobeon.masp.mediaobject.IMediaObject;

/**
 * Used to create and control outbound media streams.
 *
 * @author Jörgen Terner
 */
public interface IOutboundMediaStream extends IMediaStream {

     public enum PlayOption {
        WAIT_FOR_AUDIO(0), WAIT_FOR_VIDEO(1),
        WAIT_FOR_AUDIO_AND_VIDEO(2), DO_NOT_WAIT(3);

         /** The corresponding decimal definition. */
         private final int decimalDef;

        PlayOption(int decimalDef) {
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
     * Creates and connects a stream to its endpoint. Note that
     * <code>connectionProperties</code> must at least contain information
     * for audiohost and audioport. If a videohost is specified, the
     * videoport must be specified as well.
     *
     * @param payloads             RTP payload mappings.
     * @param connectionProperties Properties necessary to establish a
     *                             connection to the endpoint.
     *
     * @throws IllegalArgumentException If <code>payloads</code> or
     *                                  <code>connectionProperties</code> is
     *                                  <code>null</code> or if
     *                                  <code>connectionProperties</code> does
     *                                  not contain enough information.
     * @throws IllegalStateException    If this method has already been called
     *                                  for this stream instance or if the
     *                                  stream has been deleted.
     * @throws CreateSessionException   If the local session could not be
     *                                  created.
     * @throws StackException           If some other error occured.
     */
    public void create(Collection<RTPPayload> payloads,
                       ConnectionProperties connectionProperties) throws StackException;

    /**
     * Creates and connects a stream to its endpoint. Note that
     * <code>connectionProperties</code> must at least contain information
     * for audiohost and audioport. If a videohost is specified, the 
     * videoport must be specified as well. 
     *
     * @param payloads             RTP payload mappings.
     * @param connectionProperties Properties necessary to establish a
     *                             connection to the endpoint.
     *
     * @param rtcpFeedback         RTCP Feedback to use in the stream
     * @throws IllegalArgumentException If <code>payloads</code> or
     *                                  <code>connectionProperties</code> is 
     *                                  <code>null</code> or if 
     *                                  <code>connectionProperties</code> does
     *                                  not contain enough information.
     * @throws IllegalStateException    If this method has already been called
     *                                  for this stream instance or if the 
     *                                  stream has been deleted.
     * @throws CreateSessionException   If the local session could not be 
     *                                  created.
     * @throws StackException           If some other error occured.
     */

    public void create(Collection<RTPPayload> payloads,
                       ConnectionProperties connectionProperties, RTCPFeedback rtcpFeedback,
		IInboundMediaStream inboundStream) throws StackException;

    /**
     * Tells that an inbound stream has been joined and that 
     * data will arrive from that inbound stream.
     */
    public void joined(IInboundMediaStream inboundStream);

    /**
     * @return <code>true</code> if this outbound stream is currently
     *         joined, <code>false</code> if not joined.
     */
    public boolean isJoined();

    /**
     * Tells that a joined inbound stream has been removed and no more
     * data will arrive from that inbound stream.
     */
    public void unjoined();

    /**
     * Sends a translated media object to the endpoint.
     * When a translator, TextToSpeech, has translated the media object
     * received through the TextToSpeech method translate, the translated
     * media object will be "returned" through this method.
     * This method is responsible for playing the translated media object.
     *
     * @param mediaObject contains data to send.
     */
    public void translationDone(IMediaObject mediaObject);

    /**
     * A translation has failed. When this occurs, the ongoing play-
     * operation will end with a PlayFailed-event.
     *
     * @param cause Description of the cause of failure.
     */
    public void translationFailed(String cause);

    /**
     * Notifies the outbound stream that no more translated data will be 
     * sent from a joined stream.
     * <p>
     * <strong>Note</strong> that this method should NOT be used together 
     * with the <code>translationDone(IMediaObject)</code>-method.
     */
    public void translationDone();

    /**
     * Sends the tokens on the RTP stream. DTMF is only supported so far as
     * as DTMF RTP payload.
     *
     * @param tokens The tokens. May not be <code>null</code>.
     *
     * @throws IllegalArgumentException if tokens is <code>null</code>.
     * @throws IllegalStateException    If this method is called before 
     *                                  {@link #create}
     *                                  or if <code>delete</code> has been 
     *                                  called on this stream.
     * @throws StackException           If some other error occured.
     */
    public void send(ControlToken[] tokens) throws StackException;

    /**
     * Plays the media object on the stream using the <code>playOption</code>
     * and starts from <code>cursor</code>. For a video media object, the media
     * stream locates the closest intra frame after <code>cursor</code> to
     * start streaming from.
     * <p>
     * If the media object is the same as a previously stopped media object,
     * any internal data such as packetization is reused.
     * <p>
     * <strong>Note</strong> that support for the <code>playOption</code> 
     * argument is not implemented yet.
     *
     * @param callId      Identifies this call. Is included in all events
     *                    originating from this call. May not be 
     *                    <code>null</code>.
     * @param mediaObject Media source. May not be <code>null</code>.
     * @param playOption  Tells how the playing shall be done. May not be 
     *                    <code>null</code>.
     * @param cursor      Start location in milliseconds in the media object
     *                    for playing. Must be &gt= 0.
     *                    
     * @throws IllegalStateException         If this method is called before 
     *                    <code>create(MediaMimeTypes, ConnectionProperties)</code>,
     *                                       if <code>delete</code> has been 
     *                                       called on this stream or if this stream
     *                                       is joined.
     * @throws IllegalArgumentException      If <code>callId</code>, 
     *                                       <code>mediaObject</code> or
     *                                       <code>playOption</code> is 
     *                                       <code>null</code>, if 
     *                                       <code>cursor</code> is &lt 0
     *                                       or if <code>mediaObject</code>
     *                                       is mutable.
     * @throws UnsupportedOperationException If a new play is issued when the
     *                                       stream is already playing.
     * @throws StackException                If some other error occured.
     */
    public void play(Object callId, IMediaObject mediaObject,
                     PlayOption playOption, long cursor) throws StackException;

    /**
     * Plays the media objects on the stream using the <code>playOption</code>
     * and starts from <code>cursor</code>. For a video media object, the media
     * stream locates the closest intra frame after <code>cursor</code> to
     * start streaming from.
     * <p>
     * If the media object is the same as a previously stopped media object,
     * any internal data such as packetization is reused.
     * <p>
     * <strong>Note</strong> that support for the <code>playOption</code>
     * argument is not implemented yet.
     *
     * @param callId       Identifies this call. Is included in all events
     *                     originating from this call. May not be
     *                     <code>null</code>.
     * @param mediaObjects Media source. May not be <code>null</code>.
     * @param playOption   Tells how the playing shall be done. May not be
     *                     <code>null</code>.
     * @param cursor       Start location in milliseconds in the media object
     *                     for playing. Must be &gt= 0.
     *
     * @throws IllegalStateException         If this method is called before
     *                    <code>create(MediaMimeTypes, ConnectionProperties)</code>
     *                                       or if <code>delete</code> has been
     *                                       called on this stream.
     * @throws IllegalArgumentException      If <code>callId</code>,
     *                                       <code>mediaObjects</code> or
     *                                       <code>playOption</code> is
     *                                       <code>null</code>, if
     *                                       <code>cursor</code> is &lt 0
     *                                       or if one or more 
     *                                       <code>mediaObjects</code>
     *                                       is mutable.
     * @throws UnsupportedOperationException If a new play is issued when the
     *                                       stream is already playing.
     * @throws StackException                If some other error occured.
     */
    public void play(Object callId, IMediaObject mediaObjects[],
                     PlayOption playOption, long cursor) throws StackException;
    /**
     * Any ongoing streaming is canceled.
     * 
     * @throws IllegalStateException If this method is called before 
     * <code>create(MediaMimeTypes, ConnectionProperties)</code>
     *                               or if <code>delete</code> has been 
     *                               called on this stream.
     */
    public void cancel();
    
    /**
     * @return An array of all supported payload mappings for this stream.
     *         Can never be <code>null</code>.
     */
    public RTPPayload[] getSupportedPayloads();

    /**
     * If this outbound stream should be used for RTCP picture fast update
     * requests
     *
     * @return true if this stream should be used for RTCP picture fast update
     */
    public boolean usesRTCPPictureFastUpdate();

}
