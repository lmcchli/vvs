/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.runtime.event.factory.CCXMLEventFactory;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.factory.EventFactory;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.runtime.event.DialogTransferCompleteEvent;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEventFactory;
import com.mobeon.masp.execution_engine.runtime.values.Visitors;
import com.mobeon.masp.util.Tools;
import com.mobeon.masp.execution_engine.voicexml.runtime.TransferShadowVars;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SendEvent_TMT4 extends OperationBase {

    private static Map<String, EventFactory> eventFactoryByTarget = new HashMap<String, EventFactory>();
    private static final ILogger log = ILoggerFactory.getILogger(SendEvent_TMT4.class);

    private DebugInfo debugInfo;

    public SendEvent_TMT4(DebugInfo debugInfo) {
        this.debugInfo = debugInfo;
    }

    static {
        eventFactoryByTarget.put("ccxml", new CCXMLEventFactory());
        eventFactoryByTarget.put("dialog", new SimpleEventFactory());
        eventFactoryByTarget.put("basichttp", new CCXMLEventFactory());
    }

    public String arguments() {
        return "";
    }

    public void execute(ExecutionContext context) throws InterruptedException {

        ValueStack stack = context.getValueStack();

        // On top of stack we find namelist variables.

        List<Value> values = stack.popToMark();
        List<String> nameList = new LinkedList<String>();

        for (int i = values.size() - 1; i >= 0; i--) {
            Value v = values.get(i);
            String value =
                    (String) v.accept(context, Visitors.getAsStringVisitor());
            nameList.add(value);
        }


        String delayValue = stack.popAsString(context);
        Long delayI = Tools.parseCSS2Time(delayValue);
        if (delayI == null) {
            context.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC, "Value of delay was invalid. Was: " + delayValue, DebugInfo.getInstance());
            return;
        }
        String event = stack.popAsString(context);
        String targetType = stack.popAsString(context);
        String target = stack.popAsString(context);

        EventFactory factory = eventFactoryByTarget.get(targetType);
        if (factory != null) {
            if (event != null && event.equals(Constants.Event.DIALOG_TRANSFER_COMPLETE)) {
                TransferShadowVars shadow = new TransferShadowVars();
                if (nameList.size() != 1) {
                    String message = "Size of namelist was " + nameList.size() + ", expected 1";
                    context.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC, message, DebugInfo.getInstance());
                    return;
                }
                String reason = nameList.get(0);
                if (log.isDebugEnabled())
                    log.debug("Reason=" + reason);
                DialogTransferCompleteEvent e = new DialogTransferCompleteEvent("DialogTransferCompleteEvent",
                        shadow, reason);
                e.setTargetType(targetType);
                e.setTarget(target);
                context.getEventHub().fireEvent(e);


            } else {
                SimpleEvent ev = factory.create(context, event, "Application event", null, null, null, null);
                ev.defineTarget(targetType, target);
                context.getEventHub().fireEvent(ev, (int)(long)delayI);
            }
        } else {
            context.getEventHub().fireEvent(Constants.Event.ERROR_SEND_TARGETTYPE_INVALID, "Invalid target type: " + target, debugInfo);
        }
    }
}
