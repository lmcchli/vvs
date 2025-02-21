/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tests.sipunit;

import com.mobeon.masp.callmanager.events.ConnectedEvent;
import com.mobeon.masp.callmanager.events.FailedEvent;
import com.mobeon.masp.callmanager.events.DisconnectedEvent;
import com.mobeon.masp.callmanager.sip.events.SipRequestEventImpl;
import com.mobeon.masp.callmanager.sip.events.SipEvent;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;
import com.mobeon.masp.callmanager.sip.events.SipEventImpl;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.CallingParty;
import com.mobeon.masp.callmanager.CallPartyDefinitions;
import com.mobeon.masp.callmanager.CallProperties;
import com.mobeon.masp.callmanager.Call;
import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.masp.callmanager.InboundCall;
import com.mobeon.masp.callmanager.OutboundCall;
import com.mobeon.masp.callmanager.RedirectingParty;
import com.mobeon.masp.callmanager.callhandling.CallDispatcher;
import com.mobeon.masp.callmanager.callhandling.CallImpl;
import com.mobeon.masp.callmanager.callhandling.InboundCallImpl;
import com.mobeon.masp.callmanager.CallManagerImpl;
import com.mobeon.masp.callmanager.callhandling.InboundCallInternal;
import com.mobeon.masp.callmanager.sip.SipMessageSender;
import com.mobeon.masp.callmanager.CalledParty;
import com.mobeon.masp.callmanager.sip.SipMessageSenderImpl;
import com.mobeon.masp.callmanager.queuehandling.CommandExecutor;
import com.mobeon.masp.callmanager.queuehandling.SequenceGuaranteedEventQueue;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingAcceptingInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.AlertingNewCallInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.ConnectedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedCompletedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.InboundCallState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedLingeringByeInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.IdleInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.CallCompletedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.ErrorInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.FailedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.FailedCompletedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.FailedWaitingForAckInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.FailedLingeringByeInboundState;
import com.mobeon.masp.callmanager.callhandling.states.inbound.DisconnectedInboundState;
import com.mobeon.masp.callmanager.callhandling.states.CallState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.OutboundCallState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ConnectedOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.DisconnectedCompletedOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.IdleOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.DisconnectedLingeringByeOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingCallingOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ProgressingProceedingOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.FailedOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.FailedLingeringByeOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ErrorLingeringByeOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.DisconnectedOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ErrorCompletedOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ErrorLingeringCancelOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.FailedCompletedOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.ErrorOutboundState;
import com.mobeon.masp.callmanager.callhandling.states.outbound.CallCompletedOutboundState;
import com.mobeon.masp.callmanager.callhandling.events.AcceptEvent;
import com.mobeon.masp.callmanager.callhandling.events.CallCommandEvent;
import com.mobeon.masp.callmanager.callhandling.events.RejectEvent;
import com.mobeon.masp.callmanager.callhandling.events.DisconnectEvent;
import com.mobeon.masp.callmanager.callhandling.events.SendTokenEvent;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.masp.execution_engine.IApplicationManagment;
import com.mobeon.masp.execution_engine.IApplicationExecution;
import com.mobeon.masp.execution_engine.ServiceEnablerException;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.stream.IMediaStream;
import com.mobeon.masp.stream.IInboundMediaStream;
import com.mobeon.masp.stream.IOutboundMediaStream;
import com.mobeon.masp.stream.IStreamFactory;
import com.mobeon.masp.stream.ConnectionProperties;
import com.mobeon.masp.stream.RTPPayload;
import com.mobeon.masp.stream.StackException;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import com.mobeon.masp.util.executor.ExecutorServiceManager;

import javax.sip.ServerTransaction;
import javax.sip.Dialog;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import javax.sip.Timeout;
import javax.sip.RequestEvent;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.Transaction;
import javax.sip.message.Request;
import javax.sip.message.Response;
import javax.sip.message.Message;
import javax.sip.message.MessageFactory;

/**
 * Loads a bunch of classes. This is done in order to make sure that all classes
 * are loaded when a test case runs. Otherwise, a test case might take too long
 * time thus inroducing unexpected SIP retransmission.
 */
public class ClassLoadingCase {

    // TODO: Phase 2! Update with new classes.
    private static ClassLoadingCase anInstance = new ClassLoadingCase();

    /**
     * Return the singleton instance of ClassLoader
     * @return the singleton object instance
     */
    public static ClassLoadingCase getInstance() {
        return anInstance;
    }

    public void loadClasses() throws ClassNotFoundException {

        loadApplicationRelated();
        loadCallCommands();
        loadCallRelated();
        loadCallManager();
        loadEvents();
        loadCallEvents();
        loadExportedInterfaces();
        loadLogging();
        loadQueueHandling();
        loadSdpHandling();
        loadSipHandling();
        loadStates();
        loadStreamRelated();
        loadUtilities();

        loadSipStack();
    }

    private void loadSipStack() throws ClassNotFoundException {
        ServerTransaction.class.getClassLoader().
                loadClass("javax.sip.ServerTransaction");
        Dialog.class.getClassLoader().
                loadClass("javax.sip.Dialog");
        RequestEvent.class.getClassLoader().
                loadClass("javax.sip.RequestEvent");
        ResponseEvent.class.getClassLoader().
                loadClass("javax.sip.ResponseEvent");
        SipException.class.getClassLoader().
                loadClass("javax.sip.SipException");
        SipFactory.class.getClassLoader().
                loadClass("javax.sip.SipFactory");
        SipListener.class.getClassLoader().
                loadClass("javax.sip.SipListener");
        SipStack.class.getClassLoader().
                loadClass("javax.sip.SipStack");
        Timeout.class.getClassLoader().
                loadClass("javax.sip.Timeout");
        TimeoutEvent.class.getClassLoader().
                loadClass("javax.sip.TimeoutEvent");
        Transaction.class.getClassLoader().
                loadClass("javax.sip.Transaction");
        Message.class.getClassLoader().
                loadClass("javax.sip.message.Message");
        MessageFactory.class.getClassLoader().
                loadClass("javax.sip.message.MessageFactory");
        Request.class.getClassLoader().
                loadClass("javax.sip.message.Request");
        Response.class.getClassLoader().
                loadClass("javax.sip.message.Response");
    }

    private void loadApplicationRelated() throws ClassNotFoundException {
        IApplicationManagment.class.getClassLoader().
                loadClass("com.mobeon.masp.execution_engine." +
                        "IApplicationManagment");
        IApplicationExecution.class.getClassLoader().
                loadClass("com.mobeon.masp.execution_engine." +
                        "IApplicationExecution");
        IEventDispatcher.class.getClassLoader().
                loadClass("com.mobeon.common.eventnotifier." +
                        "IEventDispatcher");
        ISession.class.getClassLoader().
                loadClass("com.mobeon.masp.execution_engine.session.ISession");
    }

    private void loadCallCommands() throws ClassNotFoundException {
        AcceptEvent.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.events." +
                        "AcceptEvent");
        CallCommandEvent.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.events." +
                        "CallCommandEvent");
        RejectEvent.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.events." +
                        "RejectEvent");
        DisconnectEvent.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.events." +
                        "DisconnectEvent");
        SendTokenEvent.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.events." +
                        "SendTokenEvent");
    }

    private void loadEvents() throws ClassNotFoundException {
        SipRequestEvent.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.sip.events.SipRequestEvent");
        SipRequestEventImpl.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.sip.events.SipRequestEventImpl");
        SipEvent.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.sip.events.SipEvent");
        SipEventImpl.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.sip.events.SipEventImpl");
        SipResponseEvent.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.sip.events.SipResponseEvent");
    }

    private void loadCallRelated() throws ClassNotFoundException {
        CallDispatcher.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.CallDispatcher");
        CallImpl.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.CallImpl");
        InboundCallInternal.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.InboundCallInternal");
        InboundCallImpl.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.InboundCallImpl");

    }

    private void loadCallManager() throws ClassNotFoundException {
        CallManagerImpl.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.CallManagerImpl");
        ServiceEnablerException.class.getClassLoader().
                loadClass("com.mobeon.masp.execution_engine.ServiceEnablerException");
    }

    private void loadCallEvents() throws ClassNotFoundException {
        ConnectedEvent.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.events.ConnectedEvent");
        FailedEvent.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.events.FailedEvent");
        DisconnectedEvent.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.events.DisconnectedEvent");
    }

    private void loadExportedInterfaces() throws ClassNotFoundException {
        CallingParty.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.CallingParty");
        CallPartyDefinitions.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.CallPartyDefinitions");
        CallProperties.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.CallProperties");
        CalledParty.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.CalledParty");
        Call.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.Call");
        CallManager.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.CallManager");
        InboundCall.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.InboundCall");
        OutboundCall.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.OutboundCall");
        RedirectingParty.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.RedirectingParty");
    }

    private void loadLogging() throws ClassNotFoundException {
        ILoggerFactory.class.getClassLoader().
                loadClass("com.mobeon.common.logging.ILoggerFactory");
        ILogger.class.getClassLoader().
                loadClass("com.mobeon.common.logging.ILogger");
    }

    private void loadQueueHandling() throws ClassNotFoundException {
        CommandExecutor.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.queuehandling." +
                        "CommandExecutor");
        SequenceGuaranteedEventQueue.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.queuehandling." +
                        "SequenceGuaranteedEventQueue");

        SequenceGuaranteedEventQueue.CommandExecution.class.getClassLoader();
    }

    private void loadSdpHandling() throws ClassNotFoundException {
        // TODO: Phase 2! Update with new classes.
    }

    private void loadSipHandling() throws ClassNotFoundException {
        SipMessageSender.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.sip.SipMessageSender");
        SipMessageSenderImpl.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.sip.SipMessageSenderImpl");
    }

    private void loadStates() throws ClassNotFoundException {
        CallState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states.CallState");

        // Inbound states
        AlertingAcceptingInboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "inbound.AlertingAcceptingInboundState");
        AlertingNewCallInboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "inbound.AlertingNewCallInboundState");
        CallCompletedInboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "inbound.CallCompletedInboundState");
        ConnectedInboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "inbound.ConnectedInboundState");
        DisconnectedCompletedInboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "inbound.DisconnectedInboundState");
        DisconnectedInboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "inbound.DisconnectedInboundState");
        DisconnectedLingeringByeInboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "inbound.DisconnectedLingeringByeInboundState");
        ErrorInboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "inbound.ErrorInboundState");
        FailedCompletedInboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "inbound.FailedCompletedInboundState");
        FailedInboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "inbound.FailedInboundState");
        FailedLingeringByeInboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "inbound.FailedLingeringByeInboundState");
        FailedWaitingForAckInboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "inbound.FailedWaitingForAckInboundState");
        IdleInboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "inbound.IdleInboundState");
        InboundCallState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "inbound.InboundCallState");


        // Outbound states
        CallCompletedOutboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "outbound.CallCompletedOutboundState");
        ConnectedOutboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "outbound.ConnectedOutboundState");
        DisconnectedCompletedOutboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "outbound.DisconnectedOutboundState");
        DisconnectedLingeringByeOutboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "outbound.DisconnectedLingeringByeOutboundState");
        DisconnectedOutboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "outbound.DisconnectedOutboundState");
        ErrorCompletedOutboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "outbound.ErrorCompletedOutboundState");
        ErrorLingeringByeOutboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "outbound.ErrorLingeringByeOutboundState");
        ErrorLingeringCancelOutboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "outbound.ErrorLingeringCancelOutboundState");
        ErrorOutboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "outbound.ErrorOutboundState");
        FailedCompletedOutboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "outbound.FailedCompletedOutboundState");
        FailedLingeringByeOutboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "outbound.FailedLingeringByeOutboundState");
        FailedOutboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "outbound.FailedOutboundState");
        IdleOutboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "outbound.IdleOutboundState");
        OutboundCallState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "outbound.OutboundCallState");
        ProgressingCallingOutboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "outbound.ProgressingCallingOutboundState");
        ProgressingOutboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "outbound.ProgressingOutboundState");
        ProgressingProceedingOutboundState.class.getClassLoader().
                loadClass("com.mobeon.masp.callmanager.callhandling.states." +
                        "outbound.ProgressingProceedingOutboundState");
    }

    private void loadStreamRelated() throws ClassNotFoundException {
        IMediaStream.class.getClassLoader().
                loadClass("com.mobeon.masp.stream.IMediaStream");
        IInboundMediaStream.class.getClassLoader().
                loadClass("com.mobeon.masp.stream.IInboundMediaStream");
        IOutboundMediaStream.class.getClassLoader().
                loadClass("com.mobeon.masp.stream.IOutboundMediaStream");
        IStreamFactory.class.getClassLoader().
                loadClass("com.mobeon.masp.stream.IStreamFactory");
        ConnectionProperties.class.getClassLoader().
                loadClass("com.mobeon.masp.stream.ConnectionProperties");
        RTPPayload.class.getClassLoader().
                loadClass("com.mobeon.masp.stream.RTPPayload");
        StackException.class.getClassLoader().
                loadClass("com.mobeon.masp.stream.StackException");
        MediaMimeTypes.class.getClassLoader().
                loadClass("com.mobeon.masp.mediaobject.MediaMimeTypes");
    }

    private void loadUtilities() throws ClassNotFoundException {
        ExecutorServiceManager.class.getClassLoader().
                loadClass("com.mobeon.masp.util.executor." +
                        "ExecutorServiceManager");
        ExecutorServiceManager.getInstance().
                getExecutorService(SequenceGuaranteedEventQueue.class);
        ExecutorServiceManager.class.getClassLoader().
                loadClass("com.mobeon.masp.util.executor." +
                        "ExecutorServiceManager");
    }
}
