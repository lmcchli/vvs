/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.events;

import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.stream.RecordingProperties;

/**
 * A record event contains all information regarding a request to record the
 * inbound media stream of a call.
 * It is used internally in the Call Manager to carry information regarding
 * the event until the event is handled.
 * <p>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public class RecordEvent extends CallCommandEvent {

    Object id;
    IMediaObject playMediaObject;
    IMediaObject recordMediaObject;
    RecordingProperties properties;

    public RecordEvent(Object id,
                       IMediaObject recordMediaObject,
                       RecordingProperties properties) {
        this.id = id;
        this.playMediaObject = null;
        this.recordMediaObject = recordMediaObject;
        this.properties = properties;
    }

    public RecordEvent(Object id,
                       IMediaObject recordMediaObject,
                       RecordingProperties properties,
                       IMediaObject playMediaObject) {
        this.id = id;
        this.playMediaObject = playMediaObject;
        this.recordMediaObject = recordMediaObject;
        this.properties = properties;
    }

    public Object getId() {
        return id;
    }

    public IMediaObject getPlayMediaObject() {
        return playMediaObject;
    }

    public IMediaObject getRecordMediaObject() {
        return recordMediaObject;
    }

    public RecordingProperties getProperties() {
        return properties;
    }

    public String toString() {
        return "RecordEvent (id = " + id + ")";
    }
}
