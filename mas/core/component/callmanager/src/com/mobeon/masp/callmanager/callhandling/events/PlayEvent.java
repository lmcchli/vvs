/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.events;

import com.mobeon.masp.stream.IOutboundMediaStream;
import com.mobeon.masp.mediaobject.IMediaObject;

/**
 * A play event contains all information regarding a request to play media on
 * the outbound media stream of a call.
 * It is used internally in the Call Manager to carry information regarding
 * the event until the event is handled.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class PlayEvent extends CallCommandEvent {

    private final Object id;
    private final IMediaObject mediaObject;
    private final IMediaObject[] mediaObjects;
    private final IOutboundMediaStream.PlayOption playOption;
    private final long cursor;

    public PlayEvent(Object id, IMediaObject mediaObject,
                     IOutboundMediaStream.PlayOption playOption, long cursor) {
        this.id = id;
        this.mediaObject = mediaObject;
        this.mediaObjects = null;
        this.playOption = playOption;
        this.cursor = cursor;
    }

    public PlayEvent(Object id, IMediaObject[] mediaObjects,
                     IOutboundMediaStream.PlayOption playOption, long cursor) {
        this.id = id;
        this.mediaObject = null;
        this.mediaObjects = mediaObjects;
        this.playOption = playOption;
        this.cursor = cursor;
    }

    public Object getId() {
        return id;
    }

    public IMediaObject getMediaObject() {
        return mediaObject;
    }

    public IMediaObject[] getMediaObjects() {
        return mediaObjects;
    }

    public IOutboundMediaStream.PlayOption getPlayOption() {
        return playOption;
    }

    public long getCursor() {
        return cursor;
    }

    public String toString() {
        return "PlayEvent (id = " + id + ")";
    }
}
