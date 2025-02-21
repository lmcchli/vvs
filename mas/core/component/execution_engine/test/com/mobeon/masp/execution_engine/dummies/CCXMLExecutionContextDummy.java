package com.mobeon.masp.execution_engine.dummies;

import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.masp.execution_engine.ccxml.Dialog;
import com.mobeon.masp.execution_engine.ccxml.EventSourceManager;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.DialogStartEvent;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.execution_engine.mock.MockAction;
import static com.mobeon.masp.execution_engine.mock.MockAction.Action.DELEGATE;
import com.mobeon.masp.operateandmaintainmanager.SessionInfoFactory;

import java.util.concurrent.Callable;

/**
 * @author Mikael Andersson
 */
public class CCXMLExecutionContextDummy extends ExecutionContextDummy implements CCXMLExecutionContext {

    public CCXMLExecutionContextDummy(DefaultExpectTarget defaultExpectTarget, CCXMLRuntimeData data) {
        super(defaultExpectTarget,data);
    }

    public CCXMLRuntimeData getData() {
        return (CCXMLRuntimeData)super.getData();
    }

    public DialogStartEvent fetchDialogEvent(String s) {
        return null;
    }

    public void bindStateTo(String varname) {
    }

    public String getStateVarName() {
        return null;
    }

    public String getEventVarName() {
        return null;
    }

    public void setEventVarName(String name) {
    }

    public void setCallManager(CallManager callManager) {
    }

    @MockAction(DELEGATE)
    public EventSourceManager getEventSourceManager() {
        return getData().getConnectionManager();
    }

    public CCXMLEvent getEventVar() {
        return null;
    }

    public void addDialog(Dialog dialog) {
    }

    public void setEventDispatcher(IEventDispatcher eventDispatcher) {
    }

    public IEventDispatcher getEventDispatcher() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void waitForEvent(String eventToFire, String messageForFiredEvent, int waitTime, Callable toInvokeWhenDelivered, Connection connection, String ... eventNames) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public CallManager getCallManager() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public SessionInfoFactory getSessionInfoFactory() {
        return null;
    }


}
