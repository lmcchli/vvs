/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack;

import junit.framework.TestCase;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.*;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.states.IdleState;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.states.RecognizingState;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.states.RecognizedState;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.mock.*;
import com.mobeon.sdp.SessionDescription;
import com.mobeon.sdp.SdpFactory;
import com.mobeon.sdp.SdpException;

public class RecognizeSessionTest extends TestCase {
    protected static ILogger logger = ILoggerFactory.getILogger(RecognizeSessionTest.class);
    private RecognizeSession recognizeSession = null;
    private RtspSessionMock rtspSessionMock = null;
    private RtspConnectionMock rtspConnection = null;

    private void setUp(ServerMock server) {
        rtspConnection = new RtspConnectionMock();
        rtspConnection.setMessageHandler(server);
        rtspSessionMock = new RtspSessionMock(rtspConnection);
        recognizeSession = new RecognizeSession(rtspSessionMock, 4711);
    }

    public void setUp() throws Exception {
        MrcpRequest.reset();
        RtspRequest.reset();
    }

    public void testSetupFail() {
        RtspServerMock messageHandler = new RtspServerMock();
        setUp(messageHandler);
        SetupRequest request = new SetupRequest(true, 4712);
        request.setUrl("mockingbird.mobeon.com", 4711);
        RtspResponse response = new RtspResponse(400, "Error");
        SdpFactory.setPathName("gov.nist");
        SdpFactory sdpFactory = null;
        SessionDescription sessionDescription = null;
        String content = "m=audio 4714 RTP/AVP 0";
        try {
            sdpFactory = SdpFactory.getInstance();
            sessionDescription = sdpFactory.createSessionDescription(content);
        } catch (SdpException e) {
            e.printStackTrace();
        }
        messageHandler.setExpectedMessage(request);
        messageHandler.setSentMessage(response);
        RtspRequest.reset();
        MrcpRequest.reset();
        response = new RtspResponse(200, "OK");
        response.setSDP(sessionDescription);
        RtspRequest.reset();
        MrcpRequest.reset();
        messageHandler.setExpectedMessage(request);
        messageHandler.setSentMessage(response);
        recognizeSession.setup();
        assertFalse(recognizeSession.isOk());
        RtspRequest.reset();
        MrcpRequest.reset();
        recognizeSession.setup();
        assertFalse(recognizeSession.isOk());
    }
/*
 *  Testing the following state transitions:
 *
 *     Idle                     Recognizing               Recognized
 *     State                    State                     State
 *      |                         |                          |
 *      |-------1:RECOGNIZE------>|                          |
 *      |                         |                          |
 *      |<-------2:STOP-----------|                          |
 */
    public void testRecognizeStop() throws Exception {
        setUp(new DummyServerMock());
        recognizeSession.recognize("appication/grammar+xml", "</grammar>", "test@mobeon.com");
        assertRecognizingState(recognizeSession);
        recognizeSession.stop();
        assertIdleState(recognizeSession);
    }

/*
 *  Testing the following state transitions:
 *
 *     Idle                     Recognizing               Recognized
 *     State                    State                     State
 *      |                         |                          |
 *      |------1:RECOGNIZE------->|---3:RECOGNIZE-COMPLETE-->|
 *      |                         |                          |
 *      |             |-----------|                          |
 *      |     2:START-OF-SPEECH   |                          |
 *      |             |---------->|                          |
 *      |                         |                          |
 */
    public void testRecognizeRecognizeComplete() throws Exception {
        setUp(new DummyServerMock());
        recognizeSession.recognize("appication/grammar+xml", "</grammar>", "test@mobeon.com");
        assertRecognizingState(recognizeSession);
        MrcpEvent event = new MrcpEvent("START-OF-SPEECH", 0, "IN-PROGRESS");
        rtspSessionMock.receive(event);
        assertRecognizingState(recognizeSession);
        event = new MrcpEvent("RECOGNITION-COMPLETE", 0, "COMPLETE");
        event.setHeaderField("Completion-Cause", "000 success");
        event.setContent("application/x-nlsml", "</result>");
        rtspSessionMock.receive(event);
        assertRecognizedState(recognizeSession);
    }

/*
 *  Testing the following state transitions:
 *
 *     Idle                     Recognizing               Recognized
 *     State                    State                     State
 *      |                         |                          |
 *      |------1:RECOGNIZE------->|---2:RECOGNIZE-COMPLETE-->|
 *      |                         |                          |
 *      |                         |<------3:RECOGNIZE--------|
 */
    public void testRecognizeRecognizeCompleteRecognize() throws Exception {
        setUp(new DummyServerMock());
        recognizeSession.recognize("appication/grammar+xml", "</grammar>", "test@mobeon.com");
        assertRecognizingState(recognizeSession);
        MrcpEvent event = new MrcpEvent("RECOGNITION-COMPLETE", 0, "COMPLETE");
        event.setHeaderField("Completion-Cause", "000 success");
        event.setContent("application/x-nlsml", "</result>");
        rtspSessionMock.receive(event);
        assertRecognizedState(recognizeSession);
        recognizeSession.recognize("appication/grammar+xml", "</grammar>", "test@mobeon.com");
        assertRecognizingState(recognizeSession);
    }

/*
 *  Testing the following state transitions:
 *
 *     Idle                     Recognizing               Recognized
 *     State                    State                     State
 *      |                         |                          |
 *      |------1:RECOGNIZE------->|---2:RECOGNIZE-COMPLETE-->|
 *      |                         |                          |
 *      |<---------------------3:STOP------------------------|
 */
    public void testRecognizeRecognizeCompleteStop() throws Exception {
        setUp(new DummyServerMock());
        recognizeSession.recognize("appication/grammar+xml", "</grammar>", "test@mobeon.com");
        assertRecognizingState(recognizeSession);
        MrcpEvent event = new MrcpEvent("RECOGNITION-COMPLETE", 0, "COMPLETE");
        event.setHeaderField("Completion-Cause", "000 success");
        event.setContent("application/x-nlsml", "</result>");
        rtspSessionMock.receive(event);
        assertRecognizedState(recognizeSession);
        recognizeSession.stop();
        assertIdleState(recognizeSession);
    }

/*
 *  Testing the following state transitions:
 *
 *     Idle                     Recognizing               Recognized
 *     State                    State                     State
 *      |                         |                          |
 *      |-------------|           |                          |
 *      |     1:DEFINE-GRAMMAR    |                          |
 *      |<------------|           |                          |
 *      |                         |                          |
 */
    public void testDefineGrammarInIdle() {
        setUp(new DummyServerMock());
        assertIdleState(recognizeSession);
        recognizeSession.defineGrammar("appication/grammar+xml", "</grammar>", "test@mobeon.com");
        assertIdleState(recognizeSession);
        // TODO: this response is faulty
        MrcpEvent event = new MrcpEvent("RECOGNITION-COMPLETE", 0, "COMPLETE");
        event.setHeaderField("Completion-Cause", "000 success");
        rtspSessionMock.receive(event);
        assertIdleState(recognizeSession);
    }
/*
 *  Testing the following state transitions:
 *
 *     Idle                     Recognizing               Recognized
 *     State                    State                     State
 *      |                         |                          |
 *      |                         |            |-------------|
 *      |                         |     1:DEFINE-GRAMMAR     |
 *      |                         |            |------------>|
 *      |                         |                          |
 */
    public void testDefineGrammarInRecognized() {
        setUp(new DummyServerMock());
        recognizeSession.recognize("appication/grammar+xml", "</grammar>", "test@mobeon.com");
        assertRecognizingState(recognizeSession);
        MrcpEvent event = new MrcpEvent("RECOGNITION-COMPLETE", 0, "COMPLETE");
        event.setHeaderField("Completion-Cause", "000 success");
        event.setContent("application/x-nlsml", "</result>");
        rtspSessionMock.receive(event);
        assertRecognizedState(recognizeSession);
        recognizeSession.defineGrammar("appication/grammar+xml", "</grammar>", "test@mobeon.com");
        assertRecognizedState(recognizeSession);
        // TODO: here is another unexpected thing
        rtspSessionMock.receive(event);
    }

    private void assertRecognizedState(RecognizeSession mrcpSession) {
        assertEquals(RecognizedState.class, mrcpSession.getCurrentState().getClass());
    }

    private void assertRecognizingState(RecognizeSession mrcpSession) {
        assertEquals(RecognizingState.class, mrcpSession.getCurrentState().getClass());
    }

    private void assertIdleState(RecognizeSession mrcpSession) {
        assertEquals(IdleState.class, mrcpSession.getCurrentState().getClass());
    }

}
