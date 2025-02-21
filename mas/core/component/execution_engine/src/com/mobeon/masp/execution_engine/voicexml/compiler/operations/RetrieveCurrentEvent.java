/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * @author David Looberger
 */
public class RetrieveCurrentEvent extends VXMLOperationBase {
    private static final ILogger log = ILoggerFactory.getILogger(RetrieveCurrentEvent.class);

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        Event ev = ex.getEventEntry().getEvent();
        if (ev instanceof SimpleEvent) {
            SimpleEvent event = (SimpleEvent) ev;

            Scope currentScope = ex.getCurrentScope();

            currentScope.setValue("_event", event.getEvent());

            // Don't worry if message is null, this will cause _message
            // to be "undefined"
            String message = event.getMessage();

            if (! currentScope.isDeclaredInExactlyThisScope(Constants.VoiceXML._MESSAGE)) {
                currentScope.evaluateAndDeclareVariable(Constants.VoiceXML._MESSAGE, null);
                if (currentScope.lastEvaluationFailed()) {
                    errorSemantic(ex, Constants.VoiceXML._MESSAGE);
                    return;
                }
            }
            if (message != null) {
                currentScope.setValue(Constants.VoiceXML._MESSAGE, message);

                if (currentScope.lastEvaluationFailed()) {
                    errorSemantic(ex, Constants.VoiceXML._MESSAGE);
                }
            }
        }
    }


    public String arguments() {
        return "";  //To change body of implemented methods use File | Settings | File Templates.
    }

    private void errorSemantic(ExecutionContext ex, String name) {
        String msg = "Declaration of " + name + " failed";
        if (log.isDebugEnabled()) log.debug(msg);
        ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC,
                msg, DebugInfo.getInstance());
    }
}
