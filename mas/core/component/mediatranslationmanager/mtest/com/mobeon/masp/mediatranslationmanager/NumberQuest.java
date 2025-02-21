/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.logging.ILogger;
import com.mobeon.masp.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.stream.IInboundMediaStream;
import com.mobeon.masp.stream.IOutboundMediaStream;
import com.mobeon.masp.stream.StackException;
import com.mobeon.masp.util.xml.NlsmlDocument;
import com.mobeon.masp.util.xml.SsmlDocument;

import java.util.Map;
import java.util.HashMap;

public class NumberQuest {
    private static ILogger logger = ILoggerFactory.getILogger(NumberQuest.class);

    private static final String grammarId = "guess";
    private static final String grammar = "<?xml version=\"1.0\"?>" +
            "<grammar xml:lang=\"en-GB\" version=\"1.0\" root=\"ROOT\">" +
            "<rule id=\"ROOT\" scope=\"public\">" +
            "<item><ruleref uri=\"#words\"/></item>" +
            "</rule>" +
            "<rule id=\"words\">" +
            "<one-of>" +
            "<item> one </item>" +
            "<item> two </item>" +
            "<item> three </item>" +
            "<item> four </item>" +
            "<item> five </item>" +
            "<item> six </item>" +
            "<item> seven </item>" +
            "<item> eight </item>" +
            "<item> nine </item>" +
            "<item> ten </item>" +
            "<item> yes </item>" +
            "<item> no </item>" +
            "<item> quit </item>" +
            "</one-of>" +
            "</rule>" +
            "</grammar>";
    private String outboundHost = "10.16.2.45";
    private int outboundPort = 4712;

    private MockEventDispatcher mockEventDispatcher = new MockEventDispatcher();
    private TextToSpeech translator;
    private SpeechRecognizer recognizer;
    private IOutboundMediaStream outboundStream;
    private IInboundMediaStream inboundStream;

    public NumberQuest() {
    }

    public void intialize() {
        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
        MediaTranslationManagerFacade mtm = new MediaTranslationManagerFacade();

        // Initializing the stream utility
        StreamUtility.getInstance().initialize(mtm);
        StreamUtility.getInstance().setEventDispatcher(mockEventDispatcher);

        // Initializing the media object utility
        MediaObjectUtility.getInstance().initialize();

        // Initializing the Media Translation Manager Facade
        mtm.setMediaObjectFactory(MediaObjectUtility.getInstance().getMediaObjectFactory());
        mtm.setStreamFactory(StreamUtility.getInstance().getStreamFactory());

        // Initializing the Media Translation Factory
//        cm.setConfigFile("test/data/configuration/mrcpConfiguration.xml");
//        MediaTranslationConfiguration.getInstance().setConfiguration(cm.getConfiguration());
        MediaTranslationFactory.getInstance().setMediaTranslationManager(mtm);

        try {
            outboundStream = StreamUtility.getInstance().createCallingStream(outboundHost, outboundPort);
        } catch (StackException e) {
            e.printStackTrace();
        }

        try {
            inboundStream = StreamUtility.getInstance().createInboundStream();
        } catch (StackException e) {
            e.printStackTrace();
        }
        logger.info("***** Connection settings *****");
        logger.info("Outbound host: " + outboundHost);
        logger.info("Outbound port: " + outboundPort);
        logger.info("Inbound port:  " + inboundStream.getAudioPort());
        logger.info("*******************************");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Map<String, String> grammars = new HashMap<String, String>();
        grammars.put(grammarId, grammar);
        recognizer = mtm.getSpeechRecognizer(Utility.getSingleton().getSession(), grammars);
        translator = mtm.getTextToSpeech(Utility.getSingleton().getSession());
        translator.open(outboundStream);
        recognizer.prepare();
    }

    public static void main(String[] args) {
        NumberQuest numberQuest = new NumberQuest();

        logger.info("Initializing ...");
        numberQuest.intialize();
        numberQuest.prompt("Welcome to number quest",
                "Our objective is to guess a number between one and three",
                "To quit, just say quit");
        String input;
        do {
            numberQuest.prompt("Please make your guess");
            String nlsml = numberQuest.recognize();
            input = numberQuest.parse(nlsml);
            if (input == null) {
                numberQuest.prompt("Something went terrebly wrong",
                        "parse returned null");
                break;
            }
            numberQuest.prompt("You said", input);
        } while (input != null && !input.equals("two") && !input.equals("quit"));

        numberQuest.translator.close();
        numberQuest.recognizer.cancel();
    }

    private void prompt(String ... sentences) {
        logger.info("--> prompt()");
        SsmlDocument ssml = new SsmlDocument();
        logger.info("initialize ...");
        ssml.initialize();
        logger.info("add sentences");
        ssml.addSentences(sentences);
        String ssmlXmlText = ssml.getXmlText();
        logger.info("The prompt : [" + ssmlXmlText + "]");
        IMediaObject mediaObject = MediaObjectUtility.getInstance().createMediaObject(ssmlXmlText);
        mockEventDispatcher.clearEventFlag();
        translator.translate(mediaObject);
        while (!mockEventDispatcher.isEventFlag()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.info("<-- prompt()");
    }

    private String recognize() {
        logger.info("--> recognize() : [" + grammarId + "]");
        String input = null;
        mockEventDispatcher.clearEventFlag();
        recognizer.recognize(inboundStream);
        while (!mockEventDispatcher.isEventFlag()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Event event = mockEventDispatcher.getEvent();
        if (event != null && event instanceof RecognitionCompleteEvent) {
            input = ((RecognitionCompleteEvent)event).getNlsmlDocument();
        }
        // TODO: check input not always NLSML
        logger.info("<-- recognize()");
        return input;
    }

    private String parse(String nslmlXmlText) {
        logger.info("--> parse() : [" + nslmlXmlText + "]");
        if (nslmlXmlText != null) {
            NlsmlDocument nlsml = new NlsmlDocument();
            nlsml.parse(nslmlXmlText);
            if (nlsml.isOk()) {
                return nlsml.getInput();
            }
        }
        logger.info("<-- parse()");
        return null;
    }
}
