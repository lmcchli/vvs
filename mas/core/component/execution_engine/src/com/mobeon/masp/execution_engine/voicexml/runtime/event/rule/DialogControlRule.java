/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.runtime.event.rule;

import com.mobeon.masp.execution_engine.ccxml.runtime.event.DialogStartEvent;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.DialogTerminateEvent;
import com.mobeon.masp.execution_engine.runtime.event.rule.ClassRule;
import com.mobeon.masp.execution_engine.runtime.event.DialogTransferEvent;
import com.mobeon.masp.execution_engine.runtime.event.DialogTransferCompleteEvent;

/**
 * Selects events controlling dialog start and termination.
 *
 * @author Mikael Andersson
 */
public class DialogControlRule extends ClassRule {


    public DialogControlRule() {
        super(DialogTerminateEvent.class,
                DialogTransferCompleteEvent.class);
    }

    public String toString() {
        return "isDialogControl()";
    }
}
