/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.runtime.ExecutionResult;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.TransferValue;
import com.mobeon.masp.execution_engine.voicexml.runtime.TransferState;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.CCXMLEvent;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.factory.DialogEventFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

import org.mozilla.javascript.ScriptableObject;

/**
 * @author David Looberger
 */
public class SendTransferEvent_T extends VXMLOperationBase {
    private static ILogger logger = ILoggerFactory.getILogger(SendTransferEvent_T.class);
    DebugInfo debugInfo;
    final private String type;
    final private String maxtime;
    final private String connecttimeout;
    final private String aai;
    final private String aaiexpr;

    /**
     * Send a TransferEvent if all parameters are correct and enter waiting state, waiting for the resonse event to return
     * @param type
     * @param maxtime
     * @param connecttimeout
     * @param aai
     * @param aaiexpr
     */
    public SendTransferEvent_T(DebugInfo debugInfo, String type, String maxtime, String connecttimeout, String aai, String aaiexpr) {
        super();
        this.debugInfo = debugInfo;
        this.type = type;
        this.maxtime = maxtime;
        this.connecttimeout = connecttimeout;
        this.aai = aai;
        this.aaiexpr = aaiexpr;
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {

        ValueStack stack = ex.getValueStack();
        String uriExpr = stack.popAsString(ex);

        if(ex.getTransferState().getCallState() == TransferState.CallState.FINALIZING){
            if(logger.isDebugEnabled())
                logger.debug("Transfer is terminated, will not start the transfer");
            return;
        }

        URI destURI = null;
        String actualAii = aai;

            try {
                destURI = new URI(uriExpr);
            } catch (URISyntaxException e) {
                String message = "The calculated value of the destination URI in the transfer is not valid";
                if (logger.isInfoEnabled()) logger.info(message, e);
                ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC, message, DebugInfo.getInstance());
                return;
            }
        if (aaiexpr != null) {
            actualAii = (String) ex.getCurrentScope().evaluate(aaiexpr);
        }

        DialogEventFactory factory = new DialogEventFactory();
        CCXMLEvent event = factory.create(
                ex,
                Constants.Event.DIALOG_TRANSFER,
                "Not found",
                ex.getCurrentConnection(),
                ex.getDialog(),
                DebugInfo.getInstance());
        if(type != null){
            event.defineProperty(Constants.CCXML.TYPE, type, ScriptableObject.READONLY);
        }
        if(maxtime == null){
            event.defineProperty(Constants.CCXML.MAXTIME,
                    ex.getProperty(Constants.PlatformProperties.PLATFORM_TRANSFER_MAXTIME),
                    ScriptableObject.READONLY);
        } else {
            event.defineProperty(Constants.CCXML.MAXTIME, maxtime, ScriptableObject.READONLY);
        }

        String uri = destURI.toASCIIString();
        if(uri != null){
            event.defineProperty(Constants.CCXML.URI, uri, ScriptableObject.READONLY);
        }
        if(connecttimeout == null){
            event.defineProperty(Constants.CCXML.CONNECTTIMEOUT,
                    ex.getProperty(Constants.PlatformProperties.PLATFORM_TRANSFER_CONNECTTIMEOUT),
                    ScriptableObject.READONLY);
        } else {
            event.defineProperty(Constants.CCXML.CONNECTTIMEOUT, connecttimeout, ScriptableObject.READONLY);
        }
        // handle the "values" property
        String transferLocalPI = ex.getProperty(Constants.PlatformProperties.PLATFORM_TRANSFER_LOCAL_PI);
        String transferANI = ex.getProperty(Constants.PlatformProperties.PLATFORM_TRANSFER_ANI);

        TransferValue transferValue = new TransferValue(transferLocalPI, transferANI);

        ex.getTransferState().setCallState(TransferState.CallState.ONGOING);

        event.defineProperty(Constants.CCXML.VALUES, transferValue, ScriptableObject.READONLY);
        setTransferProperties(ex, transferValue);

        ex.getEventHub().fireEvent(event);
        if(logger.isDebugEnabled())logger.debug("Setting engine state to "+ExecutionResult.EVENT_WAIT);        
        ex.setExecutionResult(ExecutionResult.EVENT_WAIT);
    }

    private void setTransferProperties(VXMLExecutionContext ex, TransferValue transferValue) {
        String transferProp = ex.getProperty(Constants.PlatformProperties.TRANSFER_PROPERTIES);

        if(transferProp == null) {
            if(logger.isDebugEnabled())
                logger.debug("Transfer property was null");
        } else {
            Object[] propertyIds = ex.getCurrentScope().getPropertyIds(transferProp);
            for (Object id : propertyIds) {
                String s = (String) id;
                String ecmaScript = transferProp + "." + s;
                Object v = ex.getCurrentScope().evaluate(ecmaScript);
                if(logger.isDebugEnabled())
                    logger.debug("Transfer property "+id+" has value "+v);
                transferValue.defineProperty(s, v, ScriptableObject.READONLY);
            }
        }
    }

    public String arguments() {
        return "";
    }
}
