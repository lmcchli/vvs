package com.mobeon.masp.execution_engine.runapp;

import com.mobeon.masp.execution_engine.runapp.mock.MailboxMock;
import com.mobeon.masp.execution_engine.runapp.mock.StoredMessageMock;
import com.mobeon.masp.execution_engine.runapp.mock.MessageContentMock;
import com.mobeon.masp.mailbox.StoredMessageState;
import com.mobeon.masp.mailbox.MailboxMessageType;
import com.mobeon.masp.mailbox.IMessageContent;
import com.mobeon.masp.mailbox.MessageContentProperties;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.masp.util.xml.SsmlDocument;
import junit.framework.Test;

import jakarta.activation.MimeType;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: 2007-jan-23
 * Time: 20:07:39
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationVXMLTTSTest extends ApplicationBasicTestCase<ApplicationVXMLTTSTest> {

    static {
        enableAutomaticXML();
        testLanguage("vxml");
        testSubdir("tts");
        ApplicationBasicTestCase.testCases(
                testCase("tts_1")
        );
        store(ApplicationVXMLTTSTest.class);
    }
    /**
     * Creates this test case
     */
    public ApplicationVXMLTTSTest(String event) {
        //super(event, "info_log.xml");

        super(event);
    }

    /**
     * Defines the test suite for the test, make sure we do a one time setp
     * only !!!
     *
     * @return a testsuite
     */
    public static Test suite() {
        return genericSuite(ApplicationVXMLTTSTest.class);
    }

    protected void setUp() throws Exception {
        genericSetUp(ApplicationVXMLTTSTest.class);
    }

    public void testTTS1() throws Exception {
        setTestCaseTimeout(10000);

        StoredMessageMock message = new StoredMessageMock();
        message.setSubject("Hockey on Sunda");
        message.setState(StoredMessageState.NEW);
        message.setType(MailboxMessageType.EMAIL);
        message.setUrgent(true);

        MediaProperties mediaProperties = new MediaProperties(new MimeType("text/plain"));
        MediaObjectFactory mediaObjectFactory = new MediaObjectFactory();
        SsmlDocument ssmlDocument = new SsmlDocument();
        ssmlDocument.initialize();
        ssmlDocument.setParameter("language", "en");
        ssmlDocument.addSentence("har du kissat på dig?");

        IMediaObject mediaObjectForBody = mediaObjectFactory.create( ssmlDocument.getXmlText(), mediaProperties);


        mediaObjectForBody.setImmutable();
        MessageContentProperties messageContentProperties = new MessageContentProperties();

        MessageContentMock messageContentMock = new MessageContentMock(mediaProperties, mediaObjectForBody, messageContentProperties);
        ArrayList<IMessageContent> messageContent = new ArrayList<IMessageContent>();
        messageContent.add(messageContentMock);
        message.setMessageContent(messageContent);

        MailboxMock.storedMessages.add(message);

        LogFileExaminer lfe = runSimpleTest("tts_1", false);
        lfe.add2LevelRequired(".*Supported TTS languages are: en sv.*");

        lfe.failOnUndefinedErrors();
        validateTest(lfe);
    }

}
