/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.runtime.event;

import com.mobeon.masp.execution_engine.ccxml.BridgeParty;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.Dialog;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;
import com.mobeon.common.eventnotifier.Event;

/**
 * @author Mikael Andersson
 */
public class DialogStartEvent implements Event {

    public Dialog dialog;
    private Connection connection;


    public DialogStartEvent(Dialog dialog, Connection connection) {
        this.dialog = dialog;
        this.connection = connection;
    }


    public Connection getConnection() {
        return connection;
    }


    public String getSrc() {
        return dialog.getSrc();
    }

    public String getMimeType() {
        return dialog.getMimeType();
    }

    public Id<BridgeParty> getDialogId() {
        return dialog.getDialogId();
    }


    public String toString() {
        return "DialogStartEvent{" +
                dialog +
                '}';
    }

    public Dialog getDialog() {
        return dialog;
    }
}
