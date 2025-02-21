/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.calleventlistener;

import com.mobeon.common.eventnotifier.Event;

/**
 * Interface that shall implemented by classes interested in retrieving call
 * related events, i.e. information that a call for example has been
 * created or rejected.
 * <p>
 * A class that implements this interface can register it self as a call event
 * listener in {@link com.mobeon.masp.callmanager.CMUtils}.
 * <p>
 * The following events is sent to a CallEventListener:
 * <ul>
 * <li>{@link com.mobeon.masp.callmanager.events.ProgressingEvent}</li>
 * <li>{@link com.mobeon.masp.callmanager.events.ConnectedEvent}</li>
 * <li>{@link com.mobeon.masp.callmanager.events.DisconnectedEvent}</li>
 * <li>{@link DroppedPacketsEvent}</li>
 * <li>{@link com.mobeon.masp.callmanager.events.ErrorEvent}</li>
 * <li>{@link com.mobeon.masp.callmanager.events.FailedEvent}</li>
 * <li>{@link com.mobeon.masp.callmanager.events.JoinedEvent}</li>
 * <li>{@link com.mobeon.masp.callmanager.events.JoinErrorEvent}</li>
 * <li>{@link com.mobeon.masp.callmanager.events.NotAllowedEvent}</li>
 * <li>{@link com.mobeon.masp.callmanager.events.SendTokenErrorEvent}</li>
 * <li>{@link com.mobeon.masp.callmanager.events.UnjoinedEvent}</li>
 * <li>{@link com.mobeon.masp.callmanager.events.UnjoinErrorEvent}</li>
 * </ul>
 *
 * @author Malin Flodin
 */
public interface CallEventListener {
    public void processCallEvent(Event event);
}
