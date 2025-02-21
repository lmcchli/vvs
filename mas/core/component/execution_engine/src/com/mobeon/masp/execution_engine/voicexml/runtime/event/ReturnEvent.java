/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.runtime.event;

import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEventImpl;
import org.mozilla.javascript.Scriptable;

public class ReturnEvent extends  SimpleEventImpl {
    private Scriptable theResult;
    private SimpleEvent event;

    public ReturnEvent(Scriptable theResult) {
        super("com.mobeon.return",null);
        this.theResult = theResult;
    }

    public ReturnEvent(SimpleEvent event) {
        super("com.mobeon.return",null);
        this.event = event;
    }

    public Object getResult() {
        return theResult;
    }

    public SimpleEvent getResultEvent() {
        return event;
    }
}
