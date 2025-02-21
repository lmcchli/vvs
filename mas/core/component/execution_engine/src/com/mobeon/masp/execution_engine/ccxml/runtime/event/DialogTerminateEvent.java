/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.runtime.event;

import com.mobeon.common.eventnotifier.Event;

public class DialogTerminateEvent implements Event {
    private String dialogId;

    public DialogTerminateEvent(String dialogId) {
        this.dialogId = dialogId;
    }

    public String getDialogId() {
        return dialogId;
    }
}
