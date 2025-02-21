/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.Dialog;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.DialogStartEvent;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.runtime.values.NotAValue;

/**
 * @author Mikael Andersson
 */
public class CreateDialogStartBySrcTypeNamelist_T4P extends CCXMLOperationBase {

    public void execute(CCXMLExecutionContext ex) throws InterruptedException {
        ValueStack stack = ex.getValueStack();
        String connectionID = ex.getValueStack().popAsString(ex);
        String[] nameList = (String[]) stack.pop().getValue();

        String mimeType = stack.pop().toString(ex);
        String src = stack.pop().toString(ex);

        CCXMLEvent ev = ex.getEventVar();


        Connection connection = null;
        if(connectionID == null){
            if(! ev.has(Constants.Prefix.CONNECTION, ev)){
                stack.push(new NotAValue());
                ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC,
                        "There is no connection defined on the current event", DebugInfo.getInstance());
                return;
            }
            Object o = ev.get(Constants.Prefix.CONNECTION, ev);
            connection = (Connection) o;
        } else {
            connection = ex.getEventSourceManager().findConnection(connectionID);
            if(connection == null){
                ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC,
                        "No connection associated with connectionID:"+ connectionID, DebugInfo.getInstance());
                return;
            }
        }

        Dialog dialog = ex.getEventSourceManager().createDialog(src,mimeType);

        DialogStartEvent dse = new DialogStartEvent(dialog,connection);

        if (nameList != null)
            for (String name : nameList) {
                dialog.addParameter(name, ex.getCurrentScope().getValue(name));
            }
        stack.push(dse);
    }

    public String arguments() {
        return "";
    }
}
