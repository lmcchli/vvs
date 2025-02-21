/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.masp.logging.ILogger;
import com.mobeon.masp.logging.ILoggerFactory;
import com.mobeon.masp.stream.StackException;
import com.mobeon.masp.stream.IInboundMediaStream;
import com.mobeon.masp.util.xml.NlsmlDocument;
import com.mobeon.masp.util.component.IComponentManager;
import com.mobeon.masp.util.component.SpringComponentManager;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

import edu.net.RTPReceiver;
import edu.net.RTPSender;

/**
 * This is a test class for MRCP Text To Speech.
 * The purpose is to play one or more SSML files.
 */
public class MrcpClientTest {
    private static ILogger logger = ILoggerFactory.getILogger(MrcpClientTest.class);
    private MediaTranslationManagerFacade mtm;
    private MockEventDispatcher eventDispatcher = new MockEventDispatcher();
    private static final String ttsSpeech = "test/testTTS.au";
    private static final String speechEN = "test/reference_TTS_en.au";
    private static final String ssml_en = "<?xml version=\"1.0\"?>" +
            "<speak xml:lang=\"en-GB\">" +
            "<sentence>" + "Hello world!" + "</sentence>" +
            "</speak>";

    private static final String speechSE = "test/reference_TTS_se.au";
    private static final String ssml_sv = "<?xml version=\"1.0\"?>" +
            "<speak xml:lang=\"sv-SE\">" +
            "<sentence>" + "Jag talar utomordentligt bra svenska" + "</sentence>" +
            "</speak>";

    private static final String oneEN = "test/one_en.au";
    private static final String oneSE = "test/one_se.au";
    private static final String expectedInputOneEN = "one";
    private static final String expectedInputOneSE = "ett";

    private static final String grammarEN = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>" +
            "<grammar xml:lang=\"en-US\" version=\"1.0\" root=\"ROOT\">" +
            "<rule id=\"ROOT\" scope=\"public\">" +
            "<item><ruleref uri=\"#words\"/></item>" +
            "</rule>" +
            "<rule id=\"words\">" +
            "<one-of>" +
            "<item> one </item>" +
            "<item> two </item>" +
            "<item> three </item>" +
            "<item> invalid </item>" +
            "<item> nice </item>" +
            "</one-of>" +
            "</rule>" +
            "</grammar>";

    private static final String grammarSE = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>" +
            "<grammar xml:lang=\"sv-SE\" version=\"1.0\" root=\"ROOT\">" +
            "<rule id=\"ROOT\" scope=\"public\">" +
            "<item><ruleref uri=\"#words\"/></item>" +
            "</rule>" +
            "<rule id=\"words\">" +
            "<one-of>" +
            "<item> ett </item>" +
            "<item> tvaa </item>" +
            "<item> tre </item>" +
            "<item> ogiltig </item>" +
            "<item> skoen </item>" +
            "</one-of>" +
            "</rule>" +
            "</grammar>";

    public MrcpClientTest() {
    }

    public void setUp() {
        try {
            initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        MrcpClientTest mrcpClient = new MrcpClientTest();
        logger.info("Start ...");
        try {
            logger.info("Initialize ...");
            mrcpClient.initialize();
            if (args.length >= 1) {
                if ("TTS".equalsIgnoreCase(args[0])) {
                    logger.info("Speak-IT ...");
                    if (args.length < 2) {
                        for (int i=0; i < 2; i++) {
                            mrcpClient.testTranslate(ssml_en);
                            if (!compareFile(speechEN, ttsSpeech)) {
                                System.out.println("Fail: english ttsSpeech differs!");
                            } else {
                                System.out.println("Pass: english ttsSpeech");
                            }
                            mrcpClient.testTranslate(ssml_sv);
                            if (!compareFile(speechSE, ttsSpeech)) {
                                System.out.println("Fail: swedish ttsSpeech differs!");
                            } else {
                                System.out.println("Pass: swedish ttsSpeech");
                            }
                        }
                    }
                    else for (int i = 1; i < args.length; i++) {
                        mrcpClient.testTranslate(getDocument(args[i]));
                    }
                } else if ("ASR".equalsIgnoreCase(args[0])) {
                    logger.info("Recognize-IT ...");
                    mrcpClient.testRecognize(grammarSE, oneSE, expectedInputOneSE);
                }
            } else logger.error("Syntax error ...");
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Ensuring that threads are stopped ...
        System.exit(1);
    }

    private static boolean compareFile(String reference, String result) {
        FileInputStream referenceFile;
        FileInputStream resultFile;
        try {
            referenceFile = new FileInputStream(reference);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        try {
            resultFile = new FileInputStream(result);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        try {
            if (referenceFile.available() != resultFile.available()) return false;
            while (referenceFile.available() > 0 && resultFile.available() > 0) {
                if (referenceFile.read() != resultFile.read()) return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static String getDocument(String fileName) {
        String ssmlDocument = null;
        FileInputStream input;
        File file;

        try {
            file = new File(fileName);
            input = new FileInputStream(file);
            int length = (int)file.length();
            byte[] text = new byte[length];
            input.read(text, 0, length);
            ssmlDocument = new String(text);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ssmlDocument;
    }

    public void initialize() throws Exception {
        String componentConfigXML = "test/TestComponentConfig.xml";
        IComponentManager compManager = null;
        try {
            // Create our context
            FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(componentConfigXML);
            SpringComponentManager.initialApplicationContext(ctx);
            compManager = SpringComponentManager.getInstance();

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            assert compManager != null;
            mtm = (MediaTranslationManagerFacade)compManager.create("MediaTranslationManager",
                    MediaTranslationManagerFacade.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
 //       mtm = new MediaTranslationManagerFacade();

        // Initializing the stream utility
        StreamUtility.getInstance().initialize(mtm);
        StreamUtility.getInstance().setEventDispatcher(eventDispatcher);

        Utility.getSingleton().initialize("test/MediaTranslationManagerConfig.xml");
        Utility.getSingleton().setEventDispatcher(eventDispatcher);

        // Initializing the media object utility
        MediaObjectUtility.getInstance().initialize();

        // Initializing the Media Translation Manager Facade
        mtm.setMediaObjectFactory(MediaObjectUtility.getInstance().getMediaObjectFactory());
        mtm.setStreamFactory(StreamUtility.getInstance().getStreamFactory());

        // Initializing the Media Translation Factory
//        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
//        cm.setConfigFile("test/data/configuration/mrcpConfiguration.xml");
//        MediaTranslationConfiguration.getInstance().setConfiguration(cm.getConfiguration());
//        MediaTranslationFactory.getInstance().setMediaTranslationManager(mtm);
    }

    public void testTranslate() {
        testTranslate(ssml_sv);
    }

    public void testRecognize() {
        testRecognize(grammarEN, null, null);
    }

    public void testTranslate(String ssmlDocument) {
        TextToSpeech translator = mtm.getTextToSpeech(Utility.getSingleton().getSession());
        MediaTranslationFactory factory = MediaTranslationFactory.getInstance();
        RTPReceiver rtpReceiver = new RTPReceiver();
        String remoteHost = "localhost";
        int remotePort = 4712;
        rtpReceiver.setRemoteHost(remoteHost);
        rtpReceiver.setRemotePort(remotePort);
//        assertNotNull("The translator must not be null", translator);
//        assertTrue("The translator should be an instance of MrcpTextToSpeech",
//                translator instanceof MrcpTextToSpeech);

        // Ok, now that the expectations are defined we can perform the translation
        try {
            rtpReceiver.initialize();
            rtpReceiver.open(ttsSpeech);
            translator.translate(MediaObjectUtility.getInstance().createMediaObject(ssmlDocument),
                                 StreamUtility.getInstance().createCallingStream(remoteHost, remotePort));
            logger.debug("  *** Pending upon test timeout ... ***");
            Thread.sleep(5000);
            // This is supposed to be done by the calling stream when receiving translationDone();
            logger.debug("  *** The time is out, terminating session ... ***");
            translator.control("teardown", "");
            rtpReceiver.close();
        } catch (StackException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void testRecognize(String grammar, String speechFileName, String expectedInput) {
        Map<String, String> grammars = new HashMap<String, String>();
        grammars.put("mobeon.com", grammar);
        SpeechRecognizer recognizer = mtm.getSpeechRecognizer(Utility.getSingleton().getSession(), grammars);
        RTPSender rtpSender = new RTPSender();
        rtpSender.setFileName(speechFileName);
        eventDispatcher.clearEventFlag();
        try {
            IInboundMediaStream stream = Utility.getSingleton().createReceivingStream();
            System.out.println("Streaming to: " + stream.getAudioPort());
            System.out.println("Streaming to: " + stream.getHost());
            rtpSender.setRemotePort(stream.getAudioPort());
            rtpSender.setRemoteHost(stream.getHost());
            rtpSender.initialize();
            rtpSender.open();
            recognizer.recognize(stream);
            MrcpSpeechRecognizer rec = (MrcpSpeechRecognizer)recognizer;

            System.out.println("  *** Pending on state transition " + MrcpSpeechRecognizer.ServiceState.RECOGNIZING + " ***");
            while (rec.getServiceState() != MrcpSpeechRecognizer.ServiceState.RECOGNIZING) {
                Thread.sleep(1);
            }

            System.out.println("  *** Issuing audio ***");
            rtpSender.send();
            System.out.println("  *** Pending on event ... ***");
            while (!eventDispatcher.isEventFlag()) {
                Thread.sleep(10);
            }
            if (eventDispatcher.getEvent() instanceof RecognitionCompleteEvent) {
                RecognitionCompleteEvent event = (RecognitionCompleteEvent)eventDispatcher.getEvent();
                System.out.println("  *** Got recognition complete event ***");
                NlsmlDocument nlsml = new NlsmlDocument();
                nlsml.parse(event.getNlsmlDocument());
                if (expectedInput.equals(nlsml.getInput())) {
                    System.out.println("  *** ASR test: passed ***");
                }
            }
            rtpSender.close();
            recognizer.cancel();
        } catch (StackException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void testRecognizeSynchronous(String grammar, String speechFileName) {
        Map<String, String> grammars = new HashMap<String, String>();
        SpeechRecognizer recognizer = mtm.getSpeechRecognizer(Utility.getSingleton().getSession(), grammars);
        RTPSender rtpSender = new RTPSender();
        rtpSender.setFileName(speechFileName);
        grammars.put("mobeon.com", grammar);
        try {
            IInboundMediaStream stream = Utility.getSingleton().createReceivingStream();
            System.out.println("Streaming to: " + stream.getAudioPort());
            System.out.println("Streaming to: " + stream.getHost());
            rtpSender.setRemotePort(stream.getAudioPort());
            rtpSender.setRemoteHost(stream.getHost());
            rtpSender.initialize();
            // rtpSender.open();
            recognizer.recognize(stream);
        } catch (StackException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
