/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.stream.IMediaStream.SkewMethod;

import static com.mobeon.masp.stream.IOutboundMediaStream.PlayOption;

/**
 * Defines the needed functionality of an RTP-stack implementation.
 */
public interface RTPSession {

    /**
     * Initializes the RTP Session (pre create).
     * @param stream
     */
    public void init(IMediaStream stream);

    /**
     * Plays the media object on the stream using the <code>playOption</code>
     * and starts from <code>cursor</code>. For a video media object, the
     * media stream locates the closest intra frame after <code>cursor</code>
     * to start streaming from.
     * <p>
     * If the media object is the same as a previously stopped media object, 
     * any internal data such as packetization is reused.
     * 
     * @param requestId   Identifies this call. Used when sending events
     *                    originating from this call.
     * @param mediaObject Media source.
     * @param playOption  Tells how the playing shall be done.
     * @param cursor      Start location in milliseconds in the media object 
     *                    for playing.
     * 
     * @throws IllegalStateException         If this method is called before 
     *                                       <code>create</code> or if 
     *                                       <code>delete</code> has been 
     *                                       called on this stream.
     * @throws IllegalArgumentException      If <code>callId</code>, 
     *                                       <code>mediaObject</code> or
     *                                       <code>playOption</code> is 
     *                                       <code>null</code> or if 
     *                                       <code>cursor</code> is &lt 0.
     * @throws UnsupportedOperationException If a new play is issued when the
     *                                       stream is already playing.
     * @throws StackException                If some other error occured.
     */
    public void play(int requestId, IMediaObject mediaObject,
            PlayOption playOption, long cursor) throws StackException ;

    /**
     * Records the media that arrives on the inbound stream and stores it in
     * the provided media object.
     *
     * @param callId            Identifies this call. Used when sending events 
     *                          originating from this method call.
     * @param playMediaObject   If not <code>null</code>, this mediaobject will
     *                          be played on the given outbound stream until
     *                          the recording starts.
     * @param outboundStream    If <code>playMediaObject</code> is not 
     *                          <code>null</code>, it will be played on this
     *                          stream until the recording starts.
     * @param recordMediaObject Destination for incoming media.
     * @param properties        Tells how the recording is to be done.
     * 
     * @throws IllegalStateException         If this method is called before 
     *                                       <code>create</code> or if 
     *                                       <code>delete</code> has been 
     *                                       called on this stream.
     * @throws IllegalArgumentException      If <code>callId</code>, 
     *                                       <code>mediaObject</code> or
     *                                       <code>properties</code> is 
     *                                       <code>null</code> or if 
     *                                       <code>playMediaObject</code> is
     *                                       not <code>null</code> but
     *                                       <code>outboundStream</code> is
     *                                       <code>null</code>.
     * @throws UnsupportedOperationException If a new record is issued when the
     *                                       stream is already recording.
     * @throws StackException                If some other error occured.
     */
    public void record(Object callId, 
            IMediaObject playMediaObject,
            IOutboundMediaStream outboundStream, 
            IMediaObject recordMediaObject,
            RecordingProperties properties) throws StackException;
            
    public void reNegotiatedSdp(RTPPayload dtmfPayLoad) throws StackException; 

    /**
     * Creates and connects a stream to its endpoint.
     * 
     * @param contentInfo            Information about payload for the media of
     *                               the stream. For inbound streams this also
     *                               includes information needed when saving
     *                               media to a MediaObject.
     * @param connectionProperties   Properties necessary to establish a
     *                               connection to the endpoint. Is 
     *                               <code>null</code> for inbound streams.
     * @param eventNotifier          Handles stack events.
     * @param localAudioPort         Local port for RTP-traffic over an audio 
     *                               session. The portnumber 
     *                               <code>audioPort+1</code> will be used for
     *                               RTCP-traffic.
     * @param localVideoPort         Local port for RTP-traffic over a video 
     *                               session. The portnumber 
     *                               <code>videoPort+1</code> will be used for
     *                               RTCP-traffic.
     *                               
     * @throws CreateSessionException If the local session could not be
     *                                created.
     * @throws IllegalStateException  If the stream has been deleted or if
     *                                create has already been called.
     * @throws StackException         If some other error occured.
     */
    public void create(StreamContentInfo contentInfo,
            ConnectionProperties connectionProperties,
            StackEventNotifier eventNotifier, 
            int localAudioPort, int localVideoPort,
            IInboundMediaStream inboundStream) throws StackException ;
    
    /**
     * Closes the endpoints and makes the stream unavailable for playing.
     * If the stream is not created or already deleted, nothing happens.
     */
    void delete(int requestId);

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
     *                                       {@link #create}
     *                                       or if <code>delete</code> has been 
     *                                       called on this stream.
     * @throws StackException                If some other error occured.
     */
    public long stop(Object callId) throws StackException;

    /**
     * @return Current number of lost packets.
     * 
     * @throws StackException If some error occured.
     */
    public int getCumulativePacketLost() throws StackException;
    
    /**
     * The loss fraction is defined as the number of packets lost in the
     * current reportinginterval, divided by the number expected. It is 
     * expressed as the integer part after multiplying the loss fraction
     * by 256. Possible values are 0-255. If duplicates exists and the 
     * number of received packets are greater than the number expected, 
     * the loss fraction is set to zero.
     * <p>
     * Example: If 1/4 of the packets were lost, the loss fraction would be
     * 1/4*256=64.
     * 
     * @return Current loss fraction.
     * 
     * @throws StackException If some error occured.
     */
    public short getFractionLost() throws StackException;

    /**
     * Any ongoing streaming is canceled.
     */
    public void cancel() throws StackException;
    
    /**
     * Sends the tokens on the RTP stream. DTMF is only supported so far as
     * as DTMF RTP payload.
     *
     * @param tokens The tokens. May not be <code>null</code>.
     *
     * @throws IllegalArgumentException If tokens is <code>null</code>.
     * @throws IllegalStateException    If this method is called before 
     *                                  {@link #create}
     *                                  or if <code>delete</code> has been 
     *                                  called on this stream.
     * @throws StackException           If some other error occured.
     */
    public void send(ControlToken[] tokens) throws StackException;

    /**
     * Sets the skew between the audio and video streams for a stream object.
     * <p>
     * The skew is the number of milliseconds the audio is ahead of the video.
     * If the video is ahead of the audio, this is a negative value.
     *
     * @param method Defines how the skew will be used.
     * @param skew   The number of milliseconds the audio is ahead of the
     *               video.
     *
     * @throws StackException If some error occured.
     */
    public void setSkew(SkewMethod method, long skew) throws StackException;
    
    /**
     * Connects a outbound stream to this inbound stream.
     *
     * @param handleDtmfAtInbound
     * @param outboundStream    The outbound stream to connect to this inbound
     *                          stream.
     *
     * @param forwardDtmfToOutbound
     * @throws IllegalStateException         If the outbound stream is
     *                                       already connected.
     * @throws IllegalArgumentException      If <code>outboundStream</code> is
     *                                       <code>null</code>.
     * @throws StackException                If some other error occured.
     */
    public void join(boolean handleDtmfAtInbound, IOutboundMediaStream outboundStream, boolean forwardDtmfToOutbound)
        throws StackException;

    /**
     * Unjoins the given outbound stream from this inbound stream.
     *
     * @param outboundStream    The outbound stream to disconnect from this 
     *                          inbound stream.
     * 
     * @throws IllegalArgumentException      If <code>outboundStream</code> is
     *                                       <code>null</code>.
     * @throws StackException                If some other error occured.
     */
    public void unjoin(IOutboundMediaStream outboundStream) 
        throws StackException;

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
