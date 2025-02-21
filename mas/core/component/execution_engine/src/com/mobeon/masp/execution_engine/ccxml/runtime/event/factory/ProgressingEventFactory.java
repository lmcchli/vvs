package com.mobeon.masp.execution_engine.ccxml.runtime.event.factory;

import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.ccxml.Dialog;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.callmanager.events.ProgressingEvent;
import org.mozilla.javascript.ScriptableObject;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: May 4, 2006
 * Time: 3:40:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProgressingEventFactory extends ConnectionEventFactory {

    private class Info extends ScriptableObject {

        public String getClassName() {
            return "Info";
        }
    }

    public SimpleEvent create(        ExecutionContext ec, String eventName, String message, Connection conn, Dialog dialog, DebugInfo debugInfo, Event related) {

        // Add "_earlymedia" to the proprietary info property

        CCXMLEvent event = (CCXMLEvent) super.create(ec, eventName, message, conn, dialog, debugInfo, related);
        Info info = new Info();
        ProgressingEvent e = (ProgressingEvent) related;
        info.defineProperty(Constants.CCXML._EARLYMEDIA, e.isEarlyMedia(), ScriptableObject.READONLY);
        event.defineProperty(Constants.CCXML.INFO, info, ScriptableObject.READONLY);
        return event;
    }

}
