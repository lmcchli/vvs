/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.runtime;

import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.rule.CCXMLToParentRule;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.event.EventHubImpl;
import com.mobeon.masp.execution_engine.runtime.event.EventStream;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.event.rule.EventRules;

public class CCXMLEventHub extends EventHubImpl {

    public CCXMLExecutionContext getExecutionContext() {
        return (CCXMLExecutionContext) super.getExecutionContext();
    }

    protected SimpleEvent createEvent(String event, DebugInfo debugInfo, String message) {
        SimpleEvent ev = CCXMLEvent.create(event,message,getExecutionContext(),null,null, debugInfo,null);
        ev.setExecutingURI(getExecutionContext().getExecutingModule().getDocumentURI());
        return ev;
    }

    public void onOwnerSet() {
        EventStream currentStream = getExecutionContext().getEventStream();

        EventStream.Injector injector = currentStream.new Injector(EventRules.TRUE_RULE, new CCXMLToParentRule());
        getExecutionContext().getEventHub().set(injector);

    }
}
