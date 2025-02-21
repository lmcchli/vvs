/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Ops;
import com.mobeon.masp.execution_engine.compiler.Predicate;
import com.mobeon.masp.execution_engine.compiler.products.PredicateImpl;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.event.Selector;
import com.mobeon.masp.execution_engine.runtime.event.VoiceXMLSelector;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.net.URI;
import java.net.URISyntaxException;

/** A factory for creating default event handlers for all the standard events of VoiceXML.
 * Each handler is returned as a {@link com.mobeon.masp.execution_engine.compiler.Predicate}
 * @author David Looberger
 */
public class DefaultEventHandlerFactory {
    public static final Map<String, Predicate> handlerMap = new HashMap<String, Predicate>();
    private static ILogger log = ILoggerFactory.getILogger(DefaultEventHandlerFactory.class);

    private static final String DEFAULT = "default";

    static {
        handlerMap.put("cancel", cancelHandler());
        handlerMap.put("error", errorHandler());
        handlerMap.put("exit", exitHandler());
        handlerMap.put("help", helpHandler());
        handlerMap.put("noinput", noinputHandler());
        handlerMap.put("nomatch", nomatchHandler());
        handlerMap.put("maxspeachtimeout", maxspeachtimeout());
        handlerMap.put("connection.disconnect", connectionHangupHandler());
        handlerMap.put(DEFAULT, defaultHandler());
    }

    public static Predicate getHandler(String event) {
        Predicate handler = handlerMap.get(event);
        if (handler == null) {
            handler = handlerMap.get(DEFAULT);
        }
        return handler;
    }

    private static Predicate defaultHandler() {
        Predicate handler = new PredicateImpl(null, DebugInfo.getInstance());
        try {
            handler.add(Ops.evaluateECMA_P("false",new URI("Defaulteventhandler"),1));
        } catch (URISyntaxException e) {
            log.error("Failed to parse URI:", e);
        }
        handler.add(Ops.mark_P());
        handler.add(Ops.text_P("Unhandled event caught."));
        handler.add(Ops.queuePlayableObject_TM());
        // Play any remaining, queued prompt
        handler.add(Ops.playQueuedPrompts());
        handler.add(Ops.sendDialogEvent(Constants.Event.DIALOG_EXIT, "Default event handler called."));
        handler.add(Ops.engineShutdown(true));
        return handler;    }

    private static Predicate connectionHangupHandler() {
        Predicate handler = new PredicateImpl(null, DebugInfo.getInstance());
        // Enter FinalProcessingState
        handler.add(Ops.enterFinalProcessingState(true));
        // Play any remaining, queued prompt
        handler.add(Ops.setIsExiting());
        handler.add(Ops.playQueuedPrompts());
        handler.add(Ops.sendDialogEvent(Constants.Event.DIALOG_EXIT, "Default connection.hangup handler called."));
        handler.add(Ops.engineShutdown(true));
        return handler;    }

    private static Predicate maxspeachtimeout() {
        Predicate handler = new PredicateImpl(null, DebugInfo.getInstance());
        try {
            handler.add(Ops.evaluateECMA_P("false", new URI("Defaulteventhandler"),1));
        } catch (URISyntaxException e) {
            log.error("Failed to parse URI:", e);
        }
        handler.add(Ops.mark_P());
        handler.add(Ops.text_P("The input has reached its maximum length."));
        handler.add(Ops.queuePlayableObject_TM());
        handler.add(Ops.unwindToRepromptPoint());
        return handler;
    }

    private static Predicate nomatchHandler() {
        Predicate handler = new PredicateImpl(null, DebugInfo.getInstance());
        try {
            handler.add(Ops.evaluateECMA_P("false", new URI("Defaulteventhandler"),1));
        } catch (URISyntaxException e) {
            log.error("Failed to parse URI:", e);
        }
        handler.add(Ops.mark_P());
        handler.add(Ops.text_P("The input is not valid in this contex."));
        handler.add(Ops.queuePlayableObject_TM());
        handler.add(Ops.unwindToRepromptPoint());
        return handler;
    }

    private static Predicate noinputHandler() {
        Predicate handler = new PredicateImpl(null, DebugInfo.getInstance());
        handler.add(Ops.unwindToRepromptPoint());
        return handler;
    }

    private static Predicate helpHandler() {
        Predicate handler = new PredicateImpl(null, DebugInfo.getInstance());
        try {
        handler.add(Ops.evaluateECMA_P("false", new URI("Defaulteventhandler"),1));
        } catch (URISyntaxException e) {
            log.error("Failed to parse URI:", e);
        }
        handler.add(Ops.mark_P());
        handler.add(Ops.text_P("Unhandled Help event caught."));
        handler.add(Ops.queuePlayableObject_TM());
        handler.add(Ops.unwindToRepromptPoint());
        return handler;
    }

    private static Predicate exitHandler() {
        Predicate handler = new PredicateImpl(null, DebugInfo.getInstance());
        // Play any remaining, queued prompt
        handler.add(Ops.playQueuedPrompts());
        handler.add(Ops.sendDialogEvent(Constants.Event.DIALOG_EXIT, "Default EXIT handler called."));
        handler.add(Ops.engineShutdown(true));
        return handler;
    }

    private static Predicate errorHandler() {
        Predicate handler = new PredicateImpl(null, DebugInfo.getInstance());
        try {
            handler.add(Ops.evaluateECMA_P("false", new URI("Defaulteventhandler"),1));
        } catch (URISyntaxException e) {
            log.error("Failed to parse URI:", e);
        }
        handler.add(Ops.mark_P());
        handler.add(Ops.text_P("Unhandled Error event caught."));
        handler.add(Ops.queuePlayableObject_TM());
        // Play any remaining, queued prompt
        handler.add(Ops.playQueuedPrompts());
        handler.add(Ops.sendDialogEvent(Constants.Event.DIALOG_EXIT, "Default EXIT handler called"));
        handler.add(Ops.engineShutdown(true));
        return handler;
    }


    private static Predicate cancelHandler() {
        // TODO: What exactly should the default cancel handler do?
        Predicate handler = new PredicateImpl(null, DebugInfo.getInstance());
        try {
        handler.add(Ops.evaluateECMA_P("false", new URI("Defaulteventhandler"),1));
        } catch (URISyntaxException e) {
            log.error("Failed to parse URI:", e);
        }
        handler.add(Ops.mark_P());
        handler.add(Ops.text_P("Default Cancel handler called."));
        handler.add(Ops.log_TM());
        return handler;
    }

    public static void registerDefaultHandlers(ExecutionContext ex) {
        Selector sel = null;

        for (String event : handlerMap.keySet() ) {
            if (!event.equals(DEFAULT)) {
                sel = VoiceXMLSelector.parse(event);
            } else {
                sel = VoiceXMLSelector.parse("*");
            }
            ex.registerHandler(null, sel, handlerMap.get(event));
        }
        ex.getEventProcessor().setEnabled(true);
    }
}
