/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event;

import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;
import com.mobeon.masp.execution_engine.ccxml.runtime.IdGeneratorImpl;

import java.net.URI;
import java.util.concurrent.Callable;

/**
 * @author Mikael Andersson
 */
public class SimpleEventImpl implements SimpleEvent {
    private String event = null;
    private URI executingURI;
    private DebugInfo debugInfo;
    private String message;
    private int priority = 0;
    private String targetId;
    private String targetType;
    private Event related;

    private Id<Event> sendIdRef;
    private String sendId;
    private Callable callable;

    public SimpleEventImpl(String event, DebugInfo debugInfo) {
        this.debugInfo = debugInfo;
        this.event = event;
        generateSendId();
    }

    public SimpleEventImpl(String event, String message, DebugInfo debugInfo) {
        this.debugInfo = debugInfo;
        this.event = event;
        this.message = message;
        generateSendId();
    }

    public SimpleEventImpl(String event, String message, String targetId, String targetType) {
        this.event = event;
        this.message = message;
        this.targetId = targetId;
        this.targetType = targetType;
        generateSendId();
    }

    private String prefixOf(String event) {
        int i = event.indexOf('.');
        if (i >= 0) return event.substring(0, i);
        else
            return event;
    }


    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }


    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("SimpleEventImpl{ event=").append(getEvent());
        if (getExecutingURI() != null) {
            buf.append(", uri=").append(getExecutingURI());
        } else {
            buf.append(", uri=<unknown>");

        }
        buf.append(", from=").append(getDebugInfo());
        if (message != null) {
            buf.append(", message=").append(message);
        }
        if (targetType != null) {
            buf.append(", targettype=").append(targetType);
        }
        if (targetId != null) {
            buf.append(", targetid=").append(targetId);
        }
        if (sendId != null) {
            buf.append(", sendid=").append(sendId);
        }
        buf.append(", priority=").append(priority());
        buf.append(" }");
        return buf.toString();
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final SimpleEvent event1 = (SimpleEvent) o;

        return event.equals(event1.getEvent());

    }

    public int priority() {
        return priority;
    }

    public int hashCode() {
        return event.hashCode();
    }

    public URI getExecutingURI() {
        return executingURI;
    }

    public DebugInfo getDebugInfo() {
        return debugInfo;
    }

    public String getMessage() {
        return message;
    }

    protected void setPriority(int priority) {
        this.priority = priority;
    }

    public void setExecutingURI(URI uri) {
        executingURI = uri;
    }

    public String getSendId() {
        return sendId;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void defineTarget(String targetType, String targetId) {
        this.targetType = targetType;
        this.targetId = targetId;
    }

    public String getTargetType() {
        return targetType;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setRelated(Event related) {
        this.related = related;
    }

    public Event getRelated() {
        return related;
    }

    private void generateSendId() {
        sendIdRef = IdGeneratorImpl.EVENT_GENERATOR.generateId();
        sendId = sendIdRef.toString();
    }
    public void setCallable(Callable callable) {
        this.callable = callable;
    }
    public Object call() throws Exception {
        return callable != null?callable.call():null;
    }
}
