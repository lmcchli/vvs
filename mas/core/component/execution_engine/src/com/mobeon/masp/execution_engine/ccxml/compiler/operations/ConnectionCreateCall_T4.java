/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.Disconnecter;
import com.mobeon.masp.execution_engine.runtime.ValueStack;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.util.TimeValue;
import com.mobeon.masp.util.TimeValueParser;
import com.mobeon.masp.util.Tools;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.callmanager.CallPartyDefinitions;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.CalledParty;
import com.mobeon.masp.callmanager.CallingParty;
import com.mobeon.masp.callmanager.DiversionParty;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class ConnectionCreateCall_T4 extends CCXMLOperationBase {

    static ILogger logger = ILoggerFactory.getILogger(ConnectionCreateCall_T4.class);

    private String messageForFiredEvent = "Expected event for createcall did not arrive in time";
    private String[] eventNames = {Constants.Event.CONNECTION_CONNECTED,
            Constants.Event.CONNECTION_DISCONNECTED,
            Constants.Event.CONNECTION_DISCONNECT_HANGUP,
            Constants.Event.ERROR_CONNECTION,
            Constants.Event.CONNECTION_FAILED,
            Constants.Event.ERROR_NOTALLOWED};

    public void execute(CCXMLExecutionContext ex) throws InterruptedException {
        ValueStack stack = ex.getValueStack();

        String timeout = stack.popAsString(ex);
        String hints = stack.popAsString(ex);
        String destination = stack.popAsString(ex);
        String callerId = stack.popAsString(ex);
        String connectionID = stack.popAsString(ex);

        // deal with presentation indicator, which is set as an ECMA property
        CallPartyDefinitions.PresentationIndicator presentationIndicator =
                CallPartyDefinitions.PresentationIndicator.UNKNOWN;
        if(hints != null && hints.length() > 0){
            String hintsPI = hints + "." + Constants.CCXML.PI;
            Scope currentScope = ex.getCurrentScope();
            Object hintsValue = currentScope.evaluate(hintsPI);
            // If we fail to evaluate, "pi" was not defined by the application, this is ok
            if(! currentScope.lastEvaluationFailed() && hintsValue != currentScope.getUndefined()){
                double piDouble = 0;
                if(hintsValue != null){
                    try {
                        piDouble = Double.parseDouble(hintsValue.toString());
                    } catch (java.lang.NumberFormatException e){
                        ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC,
                                hintsPI + " had invalid value " + hintsValue, DebugInfo.getInstance());
                        return;
                    }
                    int pi = (int) piDouble;
                    if(! isValid(pi)){
                        ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC,
                                hintsPI + " had invalid value " + hintsValue, DebugInfo.getInstance());
                        return;
                    }
                    presentationIndicator = intToPI(pi);
                }
            }
        }

        // deal with calltype, which is set as an ECMA property
        String calltype = null;
        if(hints != null && hints.length() > 0){
            String hintsCalltype = hints + "." + Constants.CCXML.CALLTYPE;
            Scope currentScope = ex.getCurrentScope();
            Object hintsValue = currentScope.evaluate(hintsCalltype);
            // If we fail to evaluate, "calltype" was not defined by the application, this is ok
            if(! currentScope.lastEvaluationFailed() && hintsValue != currentScope.getUndefined()){
                if(hintsValue != null){
                    calltype = hintsValue.toString();
                }
            }
        }

        // deal with outboundCallServerHost, which is set as an ECMA property
        String outboundCallServerHost = doOutboundCallServerHost(hints, ex);
        // deal with outboundCallServerPort, which is set as an ECMA property
        int outboundCallServerPort = doOutboundCallServerPort(hints, ex);

        // Diversion info (optional parameters)
        DiversionParty diversionParty = null;
        if (hints != null && hints.length() > 0) {

            String mailboxId = getValueFromEcma(hints, ex, Constants.CCXML.DIVERSION_MAILBOX);
            /**
             * MailboxId is a mandatory parameter when diversion information is provided,
             * if this parameter is missing, the whole 'diversion' section is skipped.
             */
            if (mailboxId != null) {
                diversionParty = new DiversionParty();

                mailboxId = CommonMessagingAccess.getInstance().denormalizeNumber(mailboxId);
                if (!stringToParty(ex, mailboxId, diversionParty)) {
                    ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC, "invalid mailboxId value", DebugInfo.getInstance());
                    return;
                }

                String hostIp = getValueFromEcma(hints, ex, Constants.CCXML.DIVERSION_HOST_IP);
                if (hostIp != null) {
                    diversionParty.setHostIp(hostIp);
                }
                diversionParty.setReason(getValueFromEcma(hints, ex, Constants.CCXML.DIVERSION_REASON));
                diversionParty.setCounter(getValueFromEcma(hints, ex, Constants.CCXML.DIVERSION_COUNTER));
                diversionParty.setLimit(getValueFromEcma(hints, ex, Constants.CCXML.DIVERSION_LIMIT));
                diversionParty.setPrivacy(getValueFromEcma(hints, ex, Constants.CCXML.DIVERSION_PRIVACY));
                diversionParty.setScreen(getValueFromEcma(hints, ex, Constants.CCXML.DIVERSION_SCREEN));
                diversionParty.setExtension(getValueFromEcma(hints, ex, Constants.CCXML.DIVERSION_EXTENSION));
            }
        }

        // Caller info (optional parameters)
        String callerInfoFromDisplayName = null;
        String callerInfoFromUser = null;
        String callerInfoPaiFirstValue = null;
        String callerInfoPaiSecondValue = null;
        String callerInfoPaiDisplayNameFirstValue = null;
        String callerInfoPaiDisplayNameSecondValue = null;

        if (hints != null && hints.length() > 0) {
            callerInfoFromDisplayName = getValueFromEcma(hints, ex, Constants.CCXML.CALLER_INFO_FROM_DISPLAY_NAME);
            callerInfoFromUser = getValueFromEcma(hints, ex, Constants.CCXML.CALLER_INFO_FROM_USER);
            callerInfoPaiFirstValue = getValueFromEcma(hints, ex, Constants.CCXML.CALLER_INFO_PAI_FIRST_VALUE);
            callerInfoPaiSecondValue = getValueFromEcma(hints, ex, Constants.CCXML.CALLER_INFO_PAI_SECOND_VALUE); 
            callerInfoPaiDisplayNameFirstValue = getValueFromEcma(hints, ex, Constants.CCXML.CALLER_INFO_PAI_DISPLAY_NAME_FIRST_VALUE);
            callerInfoPaiDisplayNameSecondValue = getValueFromEcma(hints, ex, Constants.CCXML.CALLER_INFO_PAI_DISPLAY_NAME_SECOND_VALUE);
        }

        Connection conn = ex.getEventSourceManager().createConnection(null, true);
        if(connectionID != null) {
            ex.getCurrentScope().evaluateAndDeclareVariable(connectionID,conn.getBridgePartyId(),true,false);
        }
        waitForEvent(timeout, ex, conn);
        CallProperties p = makeCallProperties(ex,
                destination,
                callerId,
                timeout,
                presentationIndicator,
                stringToCallType(calltype),
                outboundCallServerHost,
                outboundCallServerPort,
                diversionParty,
                callerInfoFromDisplayName,
                callerInfoFromUser,
                callerInfoPaiFirstValue,
                callerInfoPaiSecondValue,
                callerInfoPaiDisplayNameFirstValue,
                callerInfoPaiDisplayNameSecondValue);
        if (p != null) {
            if (diversionParty != null) {
                p.setPreventLoopback(true);
            }
            conn.createCall(p);
        }
    }

    private String doOutboundCallServerHost(String hints, CCXMLExecutionContext ex) {
        if(hints != null && hints.length() > 0){
            String hintsServer = hints + "." + Constants.CCXML.OUTBOUNDCALLSERVERHOST;
            Scope currentScope = ex.getCurrentScope();
            Object hintsValue = currentScope.evaluate(hintsServer);
            if(! currentScope.lastEvaluationFailed() && hintsValue != currentScope.getUndefined()){
                if(hintsValue != null){
                    return hintsValue.toString();
                }
            }
        }
        return null;
    }

    private int doOutboundCallServerPort(String hints, CCXMLExecutionContext ex) {
        if(hints != null && hints.length() > 0){
            String hintsServer = hints + "." + Constants.CCXML.OUTBOUNDCALLSERVERPORT;
            Scope currentScope = ex.getCurrentScope();
            Object hintsValue = currentScope.evaluate(hintsServer);
            if(! currentScope.lastEvaluationFailed() && hintsValue != currentScope.getUndefined()){
                if(hintsValue != null){
                    return (int) Float.parseFloat(hintsValue.toString());
                }
            }
        }
        return -1;
    }

    private String getValueFromEcma(String hints, CCXMLExecutionContext ex, String constantCcxml) {
        if (hints != null && hints.length() > 0) {
            String param = hints + "." + constantCcxml;
            Scope currentScope = ex.getCurrentScope();
            Object value = currentScope.evaluate(param);
            if (!currentScope.lastEvaluationFailed() && value != currentScope.getUndefined()) {
                if (value != null) {
                    return value.toString();
                }
            }
        }
        return null;
    }

    public static CallProperties.CallType stringToCallType(String calltype) {
            if (calltype == null) {
                return CallProperties.CallType.UNKNOWN;
            } else if (calltype.equals(Constants.CallProperties.VOICE)) {
                return CallProperties.CallType.VOICE;
            } else if (calltype.equals(Constants.CallProperties.VIDEO)) {
                return CallProperties.CallType.VIDEO;
            } else {
                return CallProperties.CallType.UNKNOWN;
            }
        }

    private void waitForEvent(String timeout, CCXMLExecutionContext ex, Connection conn) {
        if(timeout != null){
            TimeValue t = TimeValueParser.getTime(timeout);
            long timeoutMillis = Tools.toMillis(t);
            ex.waitForEvent(Constants.Event.ERROR_CONNECTION,
                    messageForFiredEvent,
                    (int) timeoutMillis + conn.getCreateCallAdditionalTimeout(),
                    new Disconnecter(conn), conn, eventNames);
        }
    }


    public String arguments() {
        return "";
    }

    CallPartyDefinitions.PresentationIndicator intToPI(int pi){
        if(pi == 0){
            return CallPartyDefinitions.PresentationIndicator.ALLOWED;
        } else if(pi == 1){
            return CallPartyDefinitions.PresentationIndicator.RESTRICTED;
        } else {
            return CallPartyDefinitions.PresentationIndicator.UNKNOWN;
        }
    }

    public boolean isValid(int pi){
        return pi==0 || pi == 1;
    }

    private boolean stringToParty(ExecutionContext ex, String numberOrURI, CallPartyDefinitions party) {
        if (numberOrURI.startsWith("tel:") || numberOrURI.startsWith("fax:") || numberOrURI.startsWith("modem:")
            || numberOrURI.startsWith("sip:")) {
            try {
                new URI(numberOrURI);
                party.setUri(numberOrURI);
            } catch (URISyntaxException e) {
                ex.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC,
                        "Invalid telephone / number in createcall, the number in question was " + numberOrURI
                              + ", and it failed because of " + e.getReason(), DebugInfo.getInstance());
                return false;
            }
        } else {
            party.setTelephoneNumber(numberOrURI);
        }
        return true;
    }

    CallProperties makeCallProperties(ExecutionContext ex,
                                      String destination,
                                      String callerId,
                                      String timeout,
                                      CallPartyDefinitions.PresentationIndicator presentationIndicator,
                                      CallProperties.CallType callType,
                                      String outboundCallServerHost,
                                      int outboundCallServerPort,
                                      DiversionParty diversionParty,
                                      String callerInfoFromDisplayName,
                                      String callerInfoFromUser,
                                      String callerInfoPaiFirstValue,
                                      String callerInfoPaiSecondValue,
                                      String callerInfoPaiDisplayNameFirstValue,
                                      String callerInfoPaiDisplayNameSecondValue) {

        CallProperties callProperties = new CallProperties();
        CalledParty calledParty = new CalledParty();
        CallingParty callingParty = new CallingParty();
        callingParty.setPresentationIndicator(presentationIndicator);

        if (!stringToParty(ex, destination, calledParty)) {
            return null;
        }
        callProperties.setCalledParty(calledParty);

        if (!stringToParty(ex, callerId, callingParty)) {
            return null;
        }
        callingParty.setFromDisplayName(callerInfoFromDisplayName);
        callingParty.setFromUser(callerInfoFromUser);
        callingParty.setPAssertedIdentityFirstValue(callerInfoPaiFirstValue);
        callingParty.setPAssertedIdentitySecondValue(callerInfoPaiSecondValue);
        callingParty.setPAssertedIdentityDisplayName(callerInfoPaiDisplayNameFirstValue);
        callingParty.setPAssertedIdentitySecondValueDisplayName(callerInfoPaiDisplayNameSecondValue);
        callProperties.setCallingParty(callingParty);
        callProperties.setDiversionParty(diversionParty);

        if (timeout != null) {
            TimeValue t = TimeValueParser.getTime(timeout);
            long timeoutMillis = Tools.toMillis(t);            
            callProperties.setMaxDurationBeforeConnected((int)timeoutMillis);
        }
        callProperties.setCallType(callType);
        callProperties.setOutboundCallServerHost(outboundCallServerHost);
        if(outboundCallServerPort != -1)
            callProperties.setOutboundCallServerPort(outboundCallServerPort);
        return callProperties;
    }
}
