/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager;

import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.stream.IOutboundMediaStream.PlayOption;
import com.mobeon.masp.stream.RecordingProperties;
import com.mobeon.masp.stream.IInboundMediaStream;
import com.mobeon.masp.stream.IOutboundMediaStream;
import com.mobeon.masp.execution_engine.session.ISession;

import java.util.Set;

/**
 * Provides call management related to an active inbound or outbound call.
 * <p>
 * Also a container of properties related to a call.
 * All getters return null if values have not been previously set.
 *
 * @author Malin Flodin
 */
public interface Call {

    /**
     * The call type is determined during the media negotiation and is therefore
     * not available until a
     * {@link com.mobeon.masp.callmanager.events.ConnectedEvent} or a
     * {@link com.mobeon.masp.callmanager.events.EarlyMediaAvailableEvent} (for
     * inbound calls) is generated.
     * @return the call type or null if the call type has not been determined yet.
     */
    public CallProperties.CallType getCallType();

    public CallingParty getCallingParty();
    public CalledParty getCalledParty();

    public String getProtocolName();
    public String getProtocolVersion();

    public ISession getSession();

    /**
     * Plays a media object on the outbound media stream of this call using the
     * <code>playOption</code> and starts from <code>cursor</code>.
     * For a video media object, the media stream locates the closest intra
     * frame after <code>cursor</code> to start streaming from.
     * <p>
     * If the media object is the same as a previously stopped media object,
     * any internal data such as packetization is reused.
     * <p>
     * <strong>Note</strong> that support for the <code>playOption</code>
     * argument is not implemented yet.
     * <p>
     * If the play request could not be performed, a
     * {@link com.mobeon.masp.stream.PlayFailedEvent} is generated.
     * Otherwise, a {@link com.mobeon.masp.stream.PlayFinishedEvent}
     * is generated when playing the media object is completed.
     *
     * @param id          Identifies this play request instance. Is included in
     *                    all events originating from this play request.
     *                    May not be <code>null</code>.
     * @param mediaObject Media source. May not be <code>null</code>.
     * @param playOption  Tells how the playing shall be done. May not be
     *                    <code>null</code>.
     * @param cursor      Start location in milliseconds in the media object
     *                    for playing. Must be &gt= 0.
     *
     * @throws IllegalArgumentException      If <code>id</code>,
     *                                       <code>mediaObject</code> or
     *                                       <code>playOption</code> is
     *                                       <code>null</code>, if
     *                                       <code>cursor</code> is &lt 0
     *                                       or if <code>mediaObject</code>
     *                                       is mutable.
     */
    public void play(Object id, IMediaObject mediaObject,
                     PlayOption playOption, long cursor)
            throws IllegalArgumentException;

    /**
     * Plays media objects on the outbound media stream of this call using the
     * <code>playOption</code> and starts from <code>cursor</code>.
     * For a video media object, the media stream locates the closest intra
     * frame after <code>cursor</code> to start streaming from.
     * <p>
     * If the media object is the same as a previously stopped media object,
     * any internal data such as packetization is reused.
     * <p>
     * <strong>Note</strong> that support for the <code>playOption</code>
     * argument is not implemented yet.
     * <p>
     * If the play request could not be performed, a
     * {@link com.mobeon.masp.stream.PlayFailedEvent} is generated.
     * Otherwise, a {@link com.mobeon.masp.stream.PlayFinishedEvent}
     * is generated when playing the media object is completed.
     *
     * @param id           Identifies this play request instance. Is included in
     *                     all events originating from this play request.
     *                     May not be <code>null</code>.
     * @param mediaObjects Media source. May not be <code>null</code>.
     * @param playOption   Tells how the playing shall be done. May not be
     *                     <code>null</code>.
     * @param cursor       Start location in milliseconds in the media object
     *                     for playing. Must be &gt= 0.
     *
     * @throws IllegalArgumentException      If <code>id</code>,
     *                                       <code>mediaObjects</code> or
     *                                       <code>playOption</code> is
     *                                       <code>null</code>, if
     *                                       <code>cursor</code> is &lt 0
     *                                       or if one or more
     *                                       <code>mediaObjects</code>
     *                                       is mutable.
     */
    public void play(Object id, IMediaObject[] mediaObjects,
                     PlayOption playOption, long cursor)
            throws IllegalArgumentException;

    /**
     * Records the media that arrives on the inbound media stream of this call
     * and stores it in the provided media object.
     * <p>
     * If the record request could not be performed, a
     * {@link com.mobeon.masp.stream.RecordFailedEvent} is generated.
     * Otherwise, a {@link com.mobeon.masp.stream.RecordFinishedEvent}
     * is generated when recording the media object is completed.
     *
     * @param id          Identifies this record request instance. Is included
     *                    in all events originating from this record request.
     *                    May not be <code>null</code>.
     * @param recordMediaObject Destination for incoming media.
     * @param properties  Tells how the recording is to be done.
     *
     * @throws IllegalArgumentException      If <code>id</code>,
     *                                       <code>mediaObject</code> or
     *                                       <code>properties</code> is
     *                                       <code>null</code> or if
     *                                       <code>mediaObject</code> is
     *                                       immutable.
     */
    public void record(Object id, IMediaObject recordMediaObject,
                       RecordingProperties properties)
            throws IllegalArgumentException;

    /**
     * Records the media that arrives on the inbound media stream of this call
     * and stores it in the provided media object.
     * <p>
     * If the record request could not be performed, a
     * {@link com.mobeon.masp.stream.RecordFailedEvent} is generated.
     * Otherwise, a {@link com.mobeon.masp.stream.RecordFinishedEvent}
     * is generated when recording the media object is completed.
     *
     * @param id          Identifies this record request instance. Is included
     *                    in all events originating from this record request.
     *                    May not be <code>null</code>.
     * @param playMediaObject   If not <code>null</code>, this mediaobject will
     *                          be played on the call's outbound media stream
     *                          until the recording starts.
     * @param recordMediaObject Destination for incoming media.
     * @param properties  Tells how the recording is to be done.
     *
     * @throws IllegalArgumentException      If <code>id</code>,
     *                                       <code>recordMediaObject</code> or
     *                                       <code>properties</code> is
     *                                       <code>null</code>, if
     *                                       <code>recordMediaObject</code> is
     *                                       immutable or if
     *                                       <code>playMediaObject</code> is
     *                                       mutable.
     */
    public void record(Object id, IMediaObject playMediaObject,
                       IMediaObject recordMediaObject,
                       RecordingProperties properties)
            throws IllegalArgumentException;

    /**
     * Stops the current ongoing play.
     * <p>
     * The media stream retains all internal data of the media object in case
     * a new <code>play</code> with the same media object is issued.
     *
     * @param id     Identifies the request that initiated the play operation
     *               that shall be stopped. May not be <code>null</code>.
     *
     * @throws IllegalArgumentException      If <code>id</code> is
     *                                       <code>null</code>.
     */
    public void stopPlay(Object id) throws IllegalArgumentException;

    /**
     * Stops the current ongoing record.
     *
     * @param id     Identifies the request that initiated the record operation
     *               that shall be stopped. May not be <code>null</code>.
     *
     * @throws IllegalArgumentException      If <code>id</code> is
     *                                       <code>null</code>.
     */
    public void stopRecord(Object id) throws IllegalArgumentException;

    public IInboundMediaStream getInboundStream();
    public IOutboundMediaStream getOutboundStream();


    /**
     * Retrieves an unmodifiable <code>Set</code> with the far end connections associated with a call.
     * The RTP connections are determined during the media negotiation and is therefore
     * not available until a {@link com.mobeon.masp.callmanager.events.ConnectedEvent} (for inbound
     * or outbound calls), a {@link com.mobeon.masp.callmanager.events.EarlyMediaAvailableEvent} (for
     * inbound calls only) or a {@link com.mobeon.masp.callmanager.events.ProgressingEvent} indicating
     * Early Media (for outbound calls only) is generated.
     * @return the far end connections associated with the call
     */
    public Set<Connection> getFarEndConnections();

    /**
     * Returns the total bit rate (for all media types, for example voice+video if it is a call
     * with both voice and video media) for the inbound stream, in bits/second.
     * @return the bit rate
     */
    public int getInboundBitRate();

}
