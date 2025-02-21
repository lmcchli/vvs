/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaMimeTypes;

import java.util.Collection;

/**
 * Used to create and control inbound media streams.
 *
 * @author Jörgen Terner
 */
public interface IInboundMediaStream extends IMediaStream {

    /**
     * Creates an inbound media stream.
     *
     * @param mediaMimeTypes         Describes the media of the stream.
     *                             
     * @throws IllegalArgumentException If <code>mediaMimeTypes</code> is
     *                                  <code>null</code>.
     * @throws IllegalStateException    If this method has already been called
     *                                  for this stream instance or if the 
     *                                  stream has been deleted.
     * @throws CreateSessionException   If the local session could not be 
     *                                  created.
     * @throws StackException           If some other error occured.
     */
    public void create(MediaMimeTypes mediaMimeTypes) throws StackException;

    public void create(VideoFastUpdater videoFastUpdater,
                       MediaMimeTypes mediaMimeTypes) throws StackException;
    
    public void create(Collection<RTPPayload> rtpPayloads) throws StackException;

    public void create(VideoFastUpdater videoFastUpdater,
                       Collection<RTPPayload> rtpPayloads) throws StackException;

    
    /**
     * Joins this inbound stream with the given outbound stream. This causes
     * the media that comes on the inbound stream to be redirected to the
     * outbound stream.
     *
     * @param outboundStream The outbound stream.
     * 
     * @throws IllegalArgumentException If <code>outboundStream</code>
     *                                  is <code>null</code>.
     * @throws IllegalStateException    If <code>outboundStream</code> is 
     *                                  already joined.
     * @throws StackException           If some other error occured.
     */
    public void join(IOutboundMediaStream outboundStream)
        throws StackException, IllegalStateException;
    public void join(boolean handleDtmfAtInbound, IOutboundMediaStream outboundStream,
                     boolean forwardDtmfToOutbound)
        throws StackException, IllegalStateException;

    /**
     * Removes the redirection of the inbound stream to the given outbound
     * stream. When unjoin is done, all data received so far from the endpoint
     * of the inbound stream is sent to the outbound stream and the outbound
     * streams <code>unjoined()</code>-function is called.
     *
     * @param outboundStream The outbound stream.
     * 
     * @throws IllegalArgumentException If <code>outboundStream</code>
     *                                  is <code>null</code>.
     */
    public void unjoin(IOutboundMediaStream outboundStream)
        throws StackException;

    /**
     * Records the media that arrives on the inbound stream and stores it in
     * the provided media object.
     *
     * @param callId      Identifies this call. Is included in all events
     *                    originating from this call. May not be 
     *                    <code>null</code>.
     * @param mediaObject Destination for incoming media.
     * @param properties  Tells how the recording is to be done.
     * 
     * @throws IllegalStateException         If this method is called before 
     *                                       {@link #create(com.mobeon.masp.mediaobject.MediaMimeTypes)}
     *                                       or if <code>delete</code> has been 
     *                                       called on this stream.
     * @throws IllegalArgumentException      If <code>callId</code>, 
     *                                       <code>mediaObject</code> or
     *                                       <code>properties</code> is 
     *                                       <code>null</code> or if 
     *                                       <code>mediaObject</code> is
     *                                       immutable.
     * @throws UnsupportedOperationException If a new record is issued when the
     *                                       stream is already recording.
     * @throws StackException                If some other error occured.
     */
    public void record(Object callId, IMediaObject mediaObject,
                       RecordingProperties properties) throws StackException;

    /**
     * Records the media that arrives on the inbound stream and stores it in
     * the provided media object.
     *
     * @param callId      Identifies this method call. Is included in all 
     *                    events originating from this method call. May not be 
     *                    <code>null</code>.
     * @param playMediaObject   If not <code>null</code>, this mediaobject will
     *                          be played on the given outbound stream until
     *                          the recording starts.
     * @param outboundStream    If <code>playMediaObject</code> is not 
     *                          <code>null</code>, it will be played on this
     *                          stream until the recording starts.
     * @param recordMediaObject Destination for incoming media.
     * @param properties  Tells how the recording is to be done.
     * 
     * @throws IllegalStateException         If this method is called before 
     *              {@link #create(com.mobeon.masp.mediaobject.MediaMimeTypes)}
     *                                       or if <code>delete</code> has been 
     *                                       called on this stream.
     * @throws IllegalArgumentException      If <code>callId</code>, 
     *                                       <code>recordMediaObject</code> or
     *                                       <code>properties</code> is 
     *                                       <code>null</code>, if 
     *                                       <code>recordMediaObject</code> is
     *                                       immutable or if 
     *                                       <code>playMediaObject</code> is
     *                                       not <code>null</code> but
     *                                       <code>outboundStream</code> is
     *                                       <code>null</code>.
     * @throws UnsupportedOperationException If a new record is issued when the
     *                                       stream is already recording.
     * @throws StackException                If some other error occured.
     */
    public void record(Object callId, IMediaObject playMediaObject,
                       IOutboundMediaStream outboundStream,
                       IMediaObject recordMediaObject,
                       RecordingProperties properties) throws StackException;
                       
    /**
     * ReNegotiatedSDP.  These(this) are/is the new type as provided
     * the remote party at outdial in the SDP of 200 OK.
     *
     * @param dtmfPayLoad      DTMF payload type.
     *                                             
     * @throws StackException                If some other error occured.
     */
                       
    public void reNegotiatedSdp(RTPPayload dtmfPayLoad) throws StackException;



    /**
     * Gets the length of time in milliseconds represented by the media 
     * in a packet 
     *
     * @return <code>pTime</code> for the media on this stream.
     * 
     * @throws IllegalStateException If this method is called before 
     *         {@link #create(com.mobeon.masp.mediaobject.MediaMimeTypes)}.
     */
    public int getPTime();

    /**
     * Gets the maximum amount of media that can be encapsulated in
     * each packet expressed in milliseconds. Should be an integer multiple of ptime.
     *
     * @return <code>maxPTime</code> for the media on this stream.
     *
     * @throws IllegalStateException If this method is called before
     *         {@link #create(com.mobeon.masp.mediaobject.MediaMimeTypes)}.
     */
    public int getMaxPTime();


    /**
     * Get the address of the local host.
     * 
     * @return Address of local host, can never be <code>null</code>.
     */
    public String getHost();

    /**
     * @return Current number of lost packets.
     * 
     * @throws IllegalStateException If this method is called before 
     *       {@link #create(com.mobeon.masp.mediaobject.MediaMimeTypes)}
     *                               or if <code>delete</code> has been 
     *                               called on this stream.
     * @throws StackException        If some error occured.
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
     * @throws IllegalStateException If this method is called before 
     *       {@link #create(com.mobeon.masp.mediaobject.MediaMimeTypes)}
     *                               or if <code>delete</code> has been 
     *                               called on this stream.
     * @throws StackException        If some error occured.
     */
    public short getFractionLost() throws StackException;

    /**
     * Sends a Picture Fast Update request which will result in an I-frame on
     * the video stream as soon as possible.
     * <p>
     * Note that this method is meant to be called from within the stream
     * component. It is needed in the interface so that the part that makes
     * the call (might be native code) only have to know about the interface
     * class and not the implementation class.
     */
    void sendPictureFastUpdateRequest();

    /**
     * Returns the total bit rate (for all media types, for example voice+video if it is a stream
     * with both voice and video media), in bits/second.
     * @return the bit rate
     */
    public int getInboundBitRate();
}
