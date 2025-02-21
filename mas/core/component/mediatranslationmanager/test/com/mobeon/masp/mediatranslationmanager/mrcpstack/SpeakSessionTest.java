/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager.mrcpstack;

import junit.framework.TestCase;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.*;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.states.PausedState;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.states.SpeakingState;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.states.IdleState;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.mock.*;
import org.apache.log4j.xml.DOMConfigurator;

public class SpeakSessionTest extends TestCase {
    protected static ILogger logger = ILoggerFactory.getILogger(RecognizeSessionTest.class);
    private SpeakSession speakSession = null;
    private RtspSessionMock rtspSessionMock = null;
    private RtspConnectionMock rtspConnection = null;

    private void setUp(ServerMock server) {
        rtspConnection = new RtspConnectionMock();
        rtspConnection.setMessageHandler(server);
        rtspSessionMock = new RtspSessionMock(rtspConnection);
        speakSession = new SpeakSession(rtspSessionMock, 4712);
    }

    public void setUp() {
        MrcpRequest.reset();
        RtspRequest.reset();
    }

    public void testSetupFail() {
        RtspServerMock messageHandler = new RtspServerMock();
        setUp(messageHandler);
        SetupRequest request = new SetupRequest(false, 4712);
        request.setUrl("mockingbird.mobeon.com", 4711);
        RtspResponse response = new RtspResponse(400, "Error");
        messageHandler.setExpectedMessage(request);
        messageHandler.setSentMessage(response);
        RtspRequest.reset();
        MrcpRequest.reset();
        speakSession.setup();
        assertFalse(speakSession.isOk());
        RtspRequest.reset();
        MrcpRequest.reset();
        response = new RtspResponse(200, "OK");
        messageHandler.setSentMessage(response);
        speakSession.setup();
        assertFalse(speakSession.isOk());
    }

/*
*  Testing the following state transitions:
*
*     Idle                     Speaking                  Paused
*     State                    State                     State
*      |                         |                          |
*      |---------1:SPEAK-------->|                          |
*      |                         |                          |
*      |<----2:SPEAK-COMPLETE----|                          |
*/
    public void testSpeakSpeakComplete() {
        setUp(new DummyServerMock());
        assertIdleState(speakSession);
        speakSession.setup();
        assertIdleState(speakSession);
        speakSession.speak("mock", "Don't you mock me boy!");
        assertSpeakingState(speakSession);
        MrcpEvent event = new MrcpEvent("SPEAK-COMPLETE", 0, "COMPLETE");
        event.setHeaderField("Completion-Cause", "000 normal");
        rtspSessionMock.receive(event);
        assertIdleState(speakSession);
        speakSession.teardown();
        assertIdleState(speakSession);
    }

/*
 *  Testing the following state transitions:
 *
 *     Idle                     Speaking                  Paused
 *     State                    State                     State
 *      |                         |                          |
 *      |---------1:SPEAK-------->|                          |
 *      |                         |                          |
 *      |<--------2:STOP----------|                          |
 */
    public void testSpeakStop() {
        setUp(new DummyServerMock());
        assertIdleState(speakSession);
        speakSession.setup();
        assertIdleState(speakSession);
        speakSession.speak("mock", "Don't you mock me boy!");
        assertSpeakingState(speakSession);
        speakSession.stop();
        assertIdleState(speakSession);
        speakSession.teardown();
        assertIdleState(speakSession);
    }

/*
 *  Testing the following state transitions:
 *
 *     Idle                     Speaking                  Paused
 *     State                    State                     State
 *      |                         |                          |
 *      |---------1:SPEAK-------->|---------2:PAUSE--------->|
 *      |                         |                          |
 *      |                         |<--------3:RESUME---------|
 */
    public void testSpeakPauseResume() {
        setUp(new DummyServerMock());
        assertIdleState(speakSession);
        speakSession.setup();
        assertIdleState(speakSession);
        speakSession.speak("mock", "Don't you mock me boy!");
        assertSpeakingState(speakSession);
        speakSession.pause();
        assertPausedState(speakSession);
        speakSession.resume();
        assertSpeakingState(speakSession);
    }

/*
 *  Testing the following state transitions:
 *
 *     Idle                     Speaking                  Paused
 *     State                    State                     State
 *      |                         |                          |
 *      |---------1:SPEAK-------->|---------2:PAUSE--------->|
 *      |                         |                          |
 *      |<---------------------3:STOP------------------------|
 */
    public void testSpeakPauseStop()  {
        setUp(new DummyServerMock());
        assertIdleState(speakSession);
        speakSession.setup();
        assertIdleState(speakSession);
        speakSession.speak("mock", "Don't you mock me boy!");
        assertSpeakingState(speakSession);
        speakSession.pause();
        assertPausedState(speakSession);
        speakSession.stop();
        assertIdleState(speakSession);
    }

/*
 *  Testing the following state transitions:
 *
 *     Idle                     Speaking                  Paused
 *     State                    State                     State
 *      |                         |                          |
 *      |---------1:SPEAK-------->|---------2:PAUSE--------->|
 *      |                         |                          |
 *      |                         |           |--------------|
 *      |                         |       3:SPEAK            |
 *      |                         |           |--------------|
 *      |                         |                          |
 */
    public void testSpeakPauseSpeak()  {
        setUp(new DummyServerMock());
        assertIdleState(speakSession);
        speakSession.setup();
        assertIdleState(speakSession);
        speakSession.speak("mock", "Don't you mock me boy!");
        assertSpeakingState(speakSession);
        speakSession.pause();
        assertPausedState(speakSession);
        speakSession.speak("mock", "Don't you mock me boy!");
        assertPausedState(speakSession);
    }

/*
 *  Testing the following state transitions:
 *
 *     Idle                     Speaking                  Paused
 *     State                    State                     State
 *      |                         |                          |
 *      |---------1:SPEAK-------->|---------2:PAUSE--------->|
 *      |                         |                          |
 *      |                         |           |--------------|
 *      |                         |       3:PAUSE            |
 *      |                         |           |--------------|
 *      |                         |                          |
 */
    public void testSpeakPausePause() {
        setUp(new DummyServerMock());
        assertIdleState(speakSession);
        speakSession.setup();
        assertIdleState(speakSession);
        speakSession.speak("mock", "Don't you mock me boy!");
        assertSpeakingState(speakSession);
        speakSession.pause();
        assertPausedState(speakSession);
        speakSession.pause();
        assertPausedState(speakSession);
    }

    private void assertPausedState(SpeakSession mrcpSession) {
        assertEquals(PausedState.class, mrcpSession.getCurrentState().getClass());
    }

    private void assertSpeakingState(SpeakSession mrcpSession) {
        assertEquals(SpeakingState.class, mrcpSession.getCurrentState().getClass());
    }

    private void assertIdleState(SpeakSession mrcpSession) {
        assertEquals(IdleState.class, mrcpSession.getCurrentState().getClass());
    }
}
