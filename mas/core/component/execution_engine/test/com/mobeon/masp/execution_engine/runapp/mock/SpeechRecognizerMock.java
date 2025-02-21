package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediatranslationmanager.*;
import com.mobeon.masp.stream.IInboundMediaStream;
import com.mobeon.masp.execution_engine.Case;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.masp.execution_engine.util.TestEvent;

import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * @author David Looberger
 */
public class SpeechRecognizerMock extends BaseMock implements SpeechRecognizer {
    private static ILogger log = ILoggerFactory.getILogger(SpeechRecognizerMock.class);


    public void recognizeSucceded(CallMock call, String grammar, String value, String meaning) {
        call.fireEvent(new RecognitionCompleteEvent(constructResponse(grammar, value, meaning)));
    }

    public static String constructResponse
            (String grammarId,
             String value,
             String meaning) {
        return "<?xml version='1.0'?>\n" +
                "<result>\n" +
                "<interpretation grammar=\"session:" + grammarId + "\" confidence=\"97\">\n" +
                "<input mode=\"speech\">" + value + "</input>\n" +
                "<instance>\n" +
                "<SWI_literal>" + meaning + "</SWI_literal>\n" +
                "<SWI_grammarName>session:" + grammarId + "</SWI_grammarName>\n" +
                "<SWI_meaning>{SWI_literal:" + meaning + "}</SWI_meaning>\n" +
                "</instance>\n" +
                "</interpretation>\n" +
                "</result>";
    }

    public void prepare() {
        log.info("MOCK: SpeechRecognizer.prepare() called");
        TestEventGenerator.generateEvent(TestEvent.RECOGNIZER_PREPARE);
    }

    public void recognize(IInboundMediaStream inboundStream) {
        log.info("MOCK: SpeechRecognizer.recognize() called");
        TestEventGenerator.generateEvent(TestEvent.RECOGNIZER_RECOGNIZE);
    }

    public void cancel() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Deprecated
    public void recognize(IInboundMediaStream inbound, Map<String, String> grammars) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Deprecated
    public void recognize(IInboundMediaStream inbound, String grammar) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Deprecated
    public void control(String action, String data) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Deprecated
    public void open(IInboundMediaStream inbound) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Deprecated
    public void setGrammar(String grammar, String grammarId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Deprecated
    public void recognize(String ... grammarIds) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Deprecated
    public void close() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Deprecated
    public String getState() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void recognizeFailed(InboundCallMock call) {
        call.fireEvent(new RecognitionFailedEvent("The testcase said so ;)"));
    }

    public void recognizeNoInput(InboundCallMock call) {
        call.fireEvent(new RecognitionNoInputEvent());
    }

    public void recognizeNoMatch(InboundCallMock call) {
        call.fireEvent(new RecognitionNoMatchEvent());
    }
}
