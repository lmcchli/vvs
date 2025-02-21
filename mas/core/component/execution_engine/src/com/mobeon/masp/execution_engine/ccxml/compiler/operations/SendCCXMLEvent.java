/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.factory.CCXMLEventFactory;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.factory.EventFactory;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author David Looberger
 */
public class SendCCXMLEvent extends CCXMLOperationBase {

        ILogger logger = ILoggerFactory.getILogger(SendCCXMLEvent.class);

        private final String event;
        private final String message;
    private DebugInfo info;
    private final EventFactory factory;
        private static final Map<String, EventFactory> factories = new HashMap<String, EventFactory>();


        static {
            factories.put(Constants.Event.CCXML_EXIT, new CCXMLEventFactory());
        }

        public SendCCXMLEvent(String event, String message, DebugInfo info) {
            this.event = event;
            this.message = message;
            this.info = info;
            factory = factories.get(event);
        }

        public String arguments() {
            return textArgument(event) + ", " +textArgument(message);
        }

        public void execute(CCXMLExecutionContext ex) throws InterruptedException {
            if(factory == null){
                SimpleEvent ev = CCXMLEvent.create(event,message,ex,null,null, info,null);
                ex.getEventHub().fireContextEvent(ev);
            } else {
                ex.getEventHub().fireEvent(factory.create(ex, event, message, null,null, info,null));
            }
        }

}
