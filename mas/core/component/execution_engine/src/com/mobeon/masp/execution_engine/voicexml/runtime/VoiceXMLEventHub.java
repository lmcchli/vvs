/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.runtime;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.event.EventHubImpl;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEventImpl;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.NoInputSender;
import com.mobeon.masp.util.TimeValue;

import java.util.concurrent.TimeUnit;

public class VoiceXMLEventHub extends EventHubImpl {

    public void fireEvent(String event, String message, DebugInfo debugInfo) {
        SimpleEventImpl ev = new SimpleEventImpl(event, debugInfo);
        if (message != null) ev.setMessage(message);
        ev.defineTarget("dialog", getExecutionContext().getDialog().getDialogId().toString());
        ev.setExecutingURI(getExecutionContext().getExecutingModule().getDocumentURI());
        fireEvent(ev);
    }

    public VXMLExecutionContext getExecutionContext() {
        return (VXMLExecutionContext) super.getExecutionContext();
    }

    public void fireNoInputEvent(String message, DebugInfo debugInfo) {
        fireNoInputEvent(message, null, debugInfo);
    }

    public void fireNoInputEvent(String message, String fieldId, DebugInfo debugInfo) {
        final FIAState state = getExecutionContext().getFIAState();
        if (state.isInitialFiaState()) {
            log.warn("<CHECKOUT>Tried to fire noinput event, but apparently no FIA state exists !");
        } else {
            if (fieldId == null)
                fieldId = state.getFieldId();
            final int delay = (int) state.readRecordNoInputDelay();
            SimpleEventImpl ev = new SimpleEventImpl(Constants.VoiceXML.NOINPUT, message, debugInfo);
            if (delay > 0) {
                final NoInputSender noInputSender = getExecutionContext().getNoInputSender();
                noInputSender.start(new TimeValue(delay, TimeUnit.MILLISECONDS), fieldId);
                getExecutionContext().waitForEvents();
            } else {
                ev.defineTarget(getExecutionContext().getFieldTargetType(), fieldId);
                fireContextEvent(ev);
                TestEventGenerator.generateEvent(TestEvent.EVENT_NOINPUT_SENT);
            }
        }
    }
}
