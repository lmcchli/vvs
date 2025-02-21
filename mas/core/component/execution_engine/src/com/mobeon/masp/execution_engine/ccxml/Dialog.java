/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml;

import com.mobeon.masp.execution_engine.ApplicationWatchdog;
import com.mobeon.masp.execution_engine.ccxml.runtime.Bridge;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.runtime.DialogExecutionContext;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.util.NamedValue;
import com.mobeon.masp.execution_engine.voicexml.runtime.TransferState;
import com.mobeon.masp.util.Ignore;
import org.mozilla.javascript.ScriptableObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Mikael Andersson
 */
public class Dialog extends ScriptableObject implements Cloneable, BridgeParty {


    private Id<BridgeParty> dialogIdRef;
    private String dialogId;
    private String src;
    private String mimeType;
    private List<NamedValue<String, Object>> parameters = new ArrayList<NamedValue<String, Object>>();
    private boolean shutdown = false;
    private DialogExecutionContext executionContext = null;

    //This is an event-sender used by bridges to be able to send error-messages
    //to this dialog if the dialog was the bridge initiator
    private EventSender sender;
    private CCXMLExecutionContext ccxmlContext;
    private EventSourceManager eventSourceManager;


    public Dialog(Id<BridgeParty> id, String src, String mimeType, CCXMLExecutionContext originatingContext) {
        sender = ConnectionImpl.createEventSender(originatingContext);
        eventSourceManager = originatingContext.getEventSourceManager();
        ccxmlContext = originatingContext;
        dialogIdRef = id;
        dialogId = dialogIdRef.toString();

        this.src = src;
        this.mimeType = mimeType;
    }

    public String getClassName() {
        return "Dialog";
    }

    public Id<BridgeParty> getDialogId() {
        return dialogIdRef;
    }

    public String getBridgePartyId() {
        return dialogId;
    }

    public String getSessionId() {
        return ccxmlContext.getSessionId();
    }

    public void sendEvent(CCXMLEvent event, EventTarget target) {
        switch (target) {
            case CONTEXT:
                break;
            case CCXML:
                event.defineTarget("ccxml", ccxmlContext.getContextId());
                break;
        }
        sender.sendEvent(event);
    }

    public void sendEvent(CCXMLEvent event) {
        sender.sendEvent(event);
    }

    public String getSrc() {
        return src;
    }

    public String getMimeType() {
        return mimeType;

    }

    public boolean join(BridgeParty otherParty, boolean fullDuplex, boolean implicit) {

        Bridge bridge = new Bridge(getEventSourceManager(), sender, this, otherParty, fullDuplex, implicit);
        bridge.join(implicit);

        return true;
    }

    public List<Bridge> bridgesOf(Set<? super Connection> conns, Set<? super Dialog> dlgs) {
        return eventSourceManager.bridgesOf(conns, dlgs);
    }

    private EventSourceManager getEventSourceManager() {
        return eventSourceManager;
    }

    public String getConnectionString () {
        if (eventSourceManager != null) {
            return eventSourceManager.getConnectionByLegAsString ();
        }
        return null;
    }
    
    public boolean unjoin(BridgeParty otherParty) {
        Bridge bridge = getEventSourceManager().findBridge(this, otherParty);
        if (bridge != null) {
            bridge.unjoin(false);
            return true;
        }
        return false;
    }

    public synchronized void setExecutionContext(DialogExecutionContext ec) {
        if (shutdown && ec.isAlive()) {
            ec.shutdown(true);
        } else {
            executionContext = ec;
        }
    }

    public synchronized void shutdown() {
        ExecutionContext context;
        if (! shutdown) {
            shutdown = true;
            if (executionContext != null) {
                if (executionContext.isAlive())
                    executionContext.shutdown(true);
                context = executionContext;
                ApplicationWatchdog.instance().signalDialogShutdown(context.getSession().getIdentity(), this);
            }
            executionContext = null;
        }
    }

    public void addParameter(String name, Object value) {
        parameters.add(new NamedValue<String, Object>(getSrc(), getMimeType()));
    }

    public boolean isLive() {
        return executionContext != null;
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public boolean isInTransfer() {
        return executionContext.getTransferState().getCallState() != TransferState.CallState.IDLE;
    }

    public static class Identity {
        private String src;
        private String mimeType;

        public Identity(String src, String mimeType) {
            this.src = src;
            this.mimeType = mimeType;
        }

        public String getSrc() {
            return src;
        }

        public String getMimeType() {
            return mimeType;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            final Identity identity = (Identity) o;

            if (!mimeType.equals(identity.mimeType)) return false;
            if (!src.equals(identity.src)) return false;

            return true;
        }

        public int hashCode() {
            int result;
            result = src.hashCode();
            result = 29 * result + mimeType.hashCode();
            return result;
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Dialog dialog = (Dialog) o;

        if (dialogIdRef != null ? !dialogIdRef.equals(dialog.dialogIdRef) : dialog.dialogIdRef != null) return false;
        if (mimeType != null ? !mimeType.equals(dialog.mimeType) : dialog.mimeType != null) return false;
        if (src != null ? !src.equals(dialog.src) : dialog.src != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (dialogIdRef != null ? dialogIdRef.hashCode() : 0);
        result = 29 * result + (src != null ? src.hashCode() : 0);
        result = 29 * result + (mimeType != null ? mimeType.hashCode() : 0);
        return result;
    }


    public String toString() {
        return "Dialog{" +
                "dialogId=" + dialogIdRef +
                ", src=" + src +
                ", mimeType=" + mimeType +
                '}';
    }

    public Dialog clone() {
        try {
            return (Dialog) super.clone();
        } catch (CloneNotSupportedException e) {
            Ignore.cloneNotSupportedException(e);
            return null;
        }
    }
}
