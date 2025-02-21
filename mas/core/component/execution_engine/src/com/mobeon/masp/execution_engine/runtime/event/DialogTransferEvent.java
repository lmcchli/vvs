/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.event;

import com.mobeon.masp.execution_engine.ccxml.BridgeParty;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.Dialog;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.ScriptableObject;

/**
 * @author David Looberger
 */
public class DialogTransferEvent extends CCXMLEvent {
    String name;
    Id<BridgeParty> dialogid;
    String connectionid;
    String conferenceid;
    String type;
    URI uri;
    List<String> namelist;
    Map<String, Object> values;
    String maxtime;
    String connecttimeout;
    String aai;
    String transferaudio;


    public DialogTransferEvent(DebugInfo debugInfo,
                               Connection conn,
                               VXMLExecutionContext context,
                               Dialog dialog,
                               Id<BridgeParty> dialogid,
                               String type,
                               URI uri,
                               String maxtime,
                               String connecttimeout,
                               String transferaudio) {
        super(debugInfo);
        this.name = Constants.Event.DIALOG_TRANSFER;
        this.dialogid = dialogid;
        this.type = type;
        this.uri = uri;
        this.maxtime = maxtime;
        this.connecttimeout = connecttimeout;
        this.transferaudio = transferaudio;

        defineName(name);
        defineMessage(name);
        defineProperty(Constants.CCXML.DIALOG_ID, dialog.getDialogId().toString(), ScriptableObject.READONLY);
        if (conn != null){
            defineConnectionId(conn);
        }
        defineSourceRelated(context);
    }

    public String getName() {
        return name;
    }

    public Id<BridgeParty> getDialogid() {
        return dialogid;
    }

    public String getConnectionid() {
        return connectionid;
    }

    public String getConferenceid() {
        return conferenceid;
    }

    public String getType() {
        return type;
    }

    public URI getUri() {
        return uri;
    }

    public List<String> getNamelist() {
        return namelist;
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public String getMaxtime() {
        return maxtime;
    }

    public String getConnecttimeout() {
        return connecttimeout;
    }

    public String getAai() {
        return aai;
    }

    public void setConnectionid(String connectionid) {
        this.connectionid = connectionid;
    }

    public void setConferenceid(String conferenceid) {
        this.conferenceid = conferenceid;
    }

    public void setAai(String aai) {
        this.aai = aai;
    }

    public void setNamelist(List<String> namelist) {
        this.namelist = namelist;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    public String getTransferaudio() {
        return transferaudio;
    }

    public void setTransferaudio(String transferaudio) {
        this.transferaudio = transferaudio;
    }
}
