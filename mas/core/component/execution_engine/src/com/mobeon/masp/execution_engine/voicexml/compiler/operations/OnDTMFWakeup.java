package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.values.ECMAObjectValue;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Constants;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Jun 19, 2006
 * Time: 3:35:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class OnDTMFWakeup extends VXMLOperationBase {
    ECMAObjectValue falseObj = new ECMAObjectValue(Boolean.FALSE);
    ECMAObjectValue trueObj = new ECMAObjectValue(Boolean.TRUE);

    public void execute(VXMLExecutionContext ex) throws InterruptedException {

        Event ev = ex.getEventEntry().getEvent();
        if (ev instanceof SimpleEvent) {
            SimpleEvent event = (SimpleEvent) ev;
            if(ex.getFIAState().onDTMFWakeup(event)){
                ex.getValueStack().push(falseObj);
                if(! ex.getFIAState().matchedGrammar()){
                    ex.getEventHub().fireContextEvent(Constants.VoiceXML.NOMATCH, DebugInfo.getInstance());
                }
            } else {
                ex.getValueStack().push(trueObj);
            }
        }
    }

    public String arguments() {
        return "";
    }
}
