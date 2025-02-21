/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.runtime.event;

import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.Dialog;
import com.mobeon.masp.execution_engine.ccxml.runtime.Bridge;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;
import com.mobeon.masp.execution_engine.ccxml.runtime.IdGeneratorImpl;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.factory.*;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.util.Ignore;
import org.mozilla.javascript.ScriptableObject;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class CCXMLEvent extends ScriptableObject implements SimpleEvent {

    public static final Map<String, EventFactory> factories = new HashMap<String, EventFactory>();
    private Id<Event> sendIdRef;
    private URI executingURI;


    public static final EventFactory CONNECTION = new ConnectionEventFactory();
    public static final EventFactory DIALOG = new DialogEventFactory();
    public static final EventFactory ERROR_NOTALLOWED = new ErrorNotAllowedFactory();
    public static final EventFactory ERROR_SEMANTIC = new ErrorSemanticFactory();
    public static final EventFactory ERROR_CONNECTION = new ErrorConnectionFactory();
    public static final EventFactory FAILED = new FailedFactory();
    public static final EventFactory LOADED = new LoadedFactory();
    public static final EventFactory KILLED = new KilledFactory();
    public static final EventFactory EARLY_MEDIA = new EarlyMediaAvailableFactory();
    public static final EventFactory PROGRESSING = new ProgressingEventFactory();
    public static final EventFactory SIPMESSAGERESPONSEFACTORY = new SipMessageResponseFactory();


    private DebugInfo debugInfo;
    private int priority;

    public static final String EVENT_SOURCE = "eventsource";
    public static final String EVENT_SOURCE_TYPE = "eventsourcetype";
    public static final String SESSION_ID = "sessionid";
    private static final String ID1 = "id1";
    private static final String ID2 = "id2";
    private static final String EVENT_ID = "eventid";
    private static final String REASON = "reason";

    private String targetType;
    private String targetId;
    private Event related;
    private String message;
    private boolean isSourceRelatedDefined;
    private String sendId;
    private Callable toInvoke;


    static {
        factories.put(Constants.Event.CONNECTION_ALERTING, CONNECTION);
        factories.put(Constants.Event.CONNECTION_CONNECTED, CONNECTION);
        factories.put(Constants.Event.CONNECTION_DISCONNECTED, CONNECTION);
        factories.put(Constants.Event.CONNECTION_PROGRESSING, PROGRESSING);
        factories.put(Constants.Event.CONNECTION_FAILED, FAILED);
        factories.put(Constants.Event.CONNECTION_SIGNAL, CONNECTION);
        factories.put(Constants.Event.CONNECTION_PROXIED, CONNECTION);
        factories.put(Constants.Event.CONNECTION_REDIRECTED, CONNECTION);
        factories.put(Constants.Event.DIALOG_EXIT, DIALOG);
        factories.put(Constants.Event.RECORD_FAILED, DIALOG);
        factories.put(Constants.Event.RECORD_FINISHED, DIALOG);
        factories.put(Constants.Event.ERROR_SEMANTIC, ERROR_SEMANTIC);
        factories.put(Constants.Event.ERROR_NOTALLOWED, ERROR_NOTALLOWED);
        factories.put(Constants.Event.ERROR_CONNECTION, ERROR_CONNECTION);
        factories.put(Constants.Event.CCXML_LOADED, LOADED);
        factories.put(Constants.Event.CCXML_KILL, KILLED);
        factories.put(Constants.Event.MOBEON_PlATFORM_EARLYMEDIARESOURCEAVAILABLE, EARLY_MEDIA);
        factories.put(Constants.Event.MOBEON_PLATFORM_SIPMESSAGERESPONSEEVENT, SIPMESSAGERESPONSEFACTORY);
    }

    public CCXMLEvent(DebugInfo debugInfo) {
        this.debugInfo = debugInfo;
        sendIdRef = IdGeneratorImpl.EVENT_GENERATOR.generateId();
        sendId = sendIdRef.toString();
        defineEventId(sendId);
    }

    public int priority() {
        return priority;
    }

    public static CCXMLEvent create(String event, String message, CCXMLExecutionContext ec, Connection conn, Dialog dialog, DebugInfo info, Event related) {

        EventFactory factory = factories.get(event);
        if (factory != null) {
            return (CCXMLEvent)factory.create(ec, event , message, conn, dialog, info, related);
        } else {
            return CCXMLEvent.createDefault(ec,event,message,info);
        }
    }

    public static void defineDefault(ExecutionContext ec,CCXMLEvent event, String eventName,String message,DebugInfo info) {
        event.defineName(eventName);
        event.defineSourceRelated(ec);
        event.defineMessage(message);
        event.defineReason(message);
    }

    private static CCXMLEvent createDefault(CCXMLExecutionContext ec,String eventName,String message,DebugInfo info) {
        CCXMLEvent event = new CCXMLEvent(info);
        event.defineName(eventName);
        event.defineSourceRelated(ec);
        event.defineMessage(message);
        event.defineReason(message);
        return event;
    }

    public void defineMessage(String message) {
        this.message = message;
    }

    public void defineConnectionRelated(String name, Connection conn, CCXMLExecutionContext ec) {
        defineName(name);
        defineMessage(message);
        defineConnectionId(conn);
        defineConnection(conn);
        defineSourceRelated(ec);
    }

    public void defineSourceRelated(ExecutionContext ec) {
        isSourceRelatedDefined = true;
        defineEventSource(ec.getContextId());
        defineEventSourceType(ec.getContextType());
    }

    public void defineName(String name) {
        determinePriority(name);
        defineProperty(Constants.VoiceXML.NAME, name, ScriptableObject.READONLY);
    }

    private void determinePriority(String name) {
        if (name.startsWith(Constants.VoiceXML.ERROR) || name.equals(Constants.VoiceXML.ERROR)) priority = 1;
        else if (name.startsWith(Constants.Event.CCXML_KILL) || name.equals(Constants.Event.CCXML_KILL)) {
            priority = 2;
        }
    }

    public void defineConnectionId(Connection conn) {
        if(conn != null){
            defineProperty(Constants.CCXML.CONNECTION_ID, conn.getBridgePartyId(), ScriptableObject.READONLY);
        }
    }

    private void defineConnection(Connection conn) {
        defineProperty(Constants.Prefix.CONNECTION, conn, ScriptableObject.READONLY);
    }

    public void defineEventId(String eventId) {
        defineProperty(EVENT_ID, eventId, ScriptableObject.READONLY);
    }

    public void defineEventSource(String contextId) {
        defineProperty(EVENT_SOURCE, contextId, ScriptableObject.READONLY);
    }


    public void defineEventSourceType(String contextType) {
        defineProperty(EVENT_SOURCE_TYPE, contextType, ScriptableObject.READONLY);
    }

    public void defineSessionId(String sessionId) {
        defineProperty(SESSION_ID,sessionId, ScriptableObject.READONLY);
    }

    public String getClassName() {
        return "Event";
    }

    public String getSendId() {
        return sendId;
    }

    public String getEvent() {
        return getProperty(this, Constants.VoiceXML.NAME).toString();
    }

    public String getMessage() {
        return message;
    }

    public void setEvent(String event) {
        putProperty(this, Constants.VoiceXML.NAME,event);
    }

    public String toString() {
        return "CCXMLEvent{ event=" + getEvent() + ", uri=" + getExecutingURI() + ", from=" + getDebugInfo() +
        ", priority=" + priority() + ", message=" + getMessage() +" }";
    }

    private DebugInfo getDebugInfo() {
        return debugInfo;
    }

    public Connection getConnection() {
        Object value = getProperty(this, Constants.Prefix.CONNECTION);
        if (value instanceof Connection) return (Connection) value;
        else
            return null;
    }

    public URI getExecutingURI() {
        return executingURI;
    }

    public void setExecutingURI(URI executingURI) {
        this.executingURI = executingURI;
    }

    public static CCXMLEvent create(String event, CCXMLExecutionContext ec, Connection connection, DebugInfo debugInfo, Event related) {
        return create(event, event, ec, connection, null, debugInfo, related);

    }

    public static CCXMLEvent create(String event, String message, CCXMLExecutionContext ec, Connection connection, DebugInfo debugInfo) {
        return create(event, message, ec, connection, null, debugInfo, null);

    }

    public static CCXMLEvent create(String event, String message, CCXMLExecutionContext ec, Connection connection, DebugInfo debugInfo, Callable toInvoke) {
        CCXMLEvent e = create(event, message, ec, connection, null, debugInfo, null);
        e.setToInvoke(toInvoke);
        return e;
    }

    public void setToInvoke(Callable toInvoke) {
        this.toInvoke = toInvoke;
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

    public void defineJoinRelated(Bridge bridge) {
        defineProperty(ID1, bridge.getOut().getBridgePartyId(), ScriptableObject.READONLY);
        defineProperty(ID2, bridge.getIn().getBridgePartyId(), ScriptableObject.READONLY);
    }

    public void defineReason(String reason) {
        defineProperty(REASON, reason, ScriptableObject.READONLY);
    }

    public boolean isSourceRelatedDefined() {
        return isSourceRelatedDefined;
    }

    public CCXMLEvent clone()  {
        try {
            return (CCXMLEvent)super.clone();
        } catch (CloneNotSupportedException e) {
            Ignore.cloneNotSupportedException(e);
        }
        return null;
    }

    public Object call() throws Exception {
        if(toInvoke != null){
            return toInvoke.call();
        } else {
            return null;
        }
    }
}
