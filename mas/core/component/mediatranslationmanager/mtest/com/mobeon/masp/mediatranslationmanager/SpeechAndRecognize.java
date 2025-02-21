/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.logging.ILogger;
import com.mobeon.masp.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.stream.IInboundMediaStream;
import com.mobeon.masp.stream.IOutboundMediaStream;
import com.mobeon.masp.stream.StackException;
import com.mobeon.masp.util.xml.NlsmlDocument;
import com.mobeon.masp.util.xml.SsmlDocument;
import org.apache.log4j.xml.DOMConfigurator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;

public class SpeechAndRecognize {
    private static ILogger logger = ILoggerFactory.getILogger(SpeechAndRecognize.class);
    private int outboundPort = 4712;
    String[] predefinedGrammars = {"grammarA", "grammarB", "grammarC"};

    private MockEventDispatcher mockEventDispatcher = new MockEventDispatcher();
    private TextToSpeech translator;
    private SpeechRecognizer recognizer;
    private IOutboundMediaStream outboundStream;
    private IInboundMediaStream inboundStream;
    private Utility utility = Utility.getSingleton();

    /**
     * This is a test program for elaborating with the ScanSoft TTS/ASR engine(s).
     * The program plays a prompt and attempts to recognize speech (according to
     * a list of grammar references).
     *
     * Example: 10.2.16.84 mtest/promptA.xml grammarA grammarB
     *
     * @param args <host> <prompt> <grammar1> ... <grammarN>
     */
    public static void main(String[] args) {
        Utility.getSingleton().initialize("test/MediaTranslationManagerConfig.xml");
        // Creates a test class object ...
        SpeechAndRecognize numberQuest = new SpeechAndRecognize();
        // Parsing command line arguments
        String[] scope;
        if (args.length > 2) {
            String hostName = args[0];
            String speechFileName = args[1];
            scope = new String[args.length-2];
            String speech = numberQuest.getDocument(speechFileName);

            for (int i = 2; i < args.length; i++) {
                scope[i-2] = args[i];
            }
            // Initializing the test class object ...
            numberQuest.intialize(hostName, scope);
            // Playing a prompt
            numberQuest.translate(speech);
            // Recognizing speech
            String nlsml = numberQuest.recognize(scope);
            // Parsing the result
            String input = numberQuest.parse(nlsml);
            if (input == null) {
                numberQuest.prompt("Something went terrebly wrong",
                        "parse returned null");
            } else {
                numberQuest.prompt("You said", input);
            }
        numberQuest.close();
        } else {
            System.err.println("Wrong number of arguments");
            System.err.println("<hostname> <prompt> <grammarid> ... <grammarid>");
        }
    }

    public SpeechAndRecognize() {
        // Configuration of the logging
    }

    public void intialize(String hostname, String[] grammars) {
        // We need an event dispatcher
        utility.setEventDispatcher(mockEventDispatcher);
        MediaTranslationManagerFacade mtm = MediaTranslationFactory.getInstance().getMediaTranslationManager();

        // Creating a stream for the TTS
        try {
            outboundStream = utility.createCallingStream(hostname, outboundPort);
        } catch (StackException e) {
            e.printStackTrace();
        }

        // Creating a stream for the ASR
        try {
            inboundStream = utility.createReceivingStream();
        } catch (StackException e) {
            e.printStackTrace();
        }
        // This information is useful when setting up JMStudio
        System.out.println("***** Connection settings *****");
        System.out.println("Outbound host: " + hostname);
        System.out.println("Outbound port: " + outboundPort);
        System.out.println("Inbound port:  " + inboundStream.getAudioPort());
        System.out.println("*******************************");

        // Ensuring that the streams are properly created ...
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Initializing the ASR and TTS sessions
        Map<String, String> grammarMap = new HashMap<String, String>();
        for (String grammar : grammars) {
            String srgs = getDocument("mtest/" + grammar + ".xml");
            grammarMap.put(grammar, srgs);
        }
        recognizer = mtm.getSpeechRecognizer(Utility.getSingleton().getSession(), grammarMap);
        translator = mtm.getTextToSpeech(Utility.getSingleton().getSession());
        translator.open(outboundStream);
        recognizer.recognize(inboundStream);
    }

    private void translate(String ssmlXmlText) {
        // Creating a media object containng the speech
        IMediaObject mediaObject = MediaObjectUtility.getInstance().createMediaObject(ssmlXmlText);
        // Preparing for event receival
        mockEventDispatcher.clearEventFlag();
        // Issuing the speech
        translator.translate(mediaObject);
        // Pending for event
        while (!mockEventDispatcher.isEventFlag()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void prompt(String ... sentences) {
        // Creating an SSML from a list of strings.
        // The SSML is translated to speech.
        logger.debug("--> prompt()");
        SsmlDocument ssml = new SsmlDocument();
        logger.debug("initialize ...");
        ssml.initialize();
        logger.debug("add sentences");
        ssml.addSentences(sentences);
        String ssmlXmlText = ssml.getXmlText();
        logger.debug("The prompt : [" + ssmlXmlText + "]");
        translate(ssmlXmlText);
        logger.debug("<-- prompt()");
    }

    private String recognize(String ... grammars) {
        // Recognizing speech based upon a list of grammar references
        logger.debug("--> recognize() : [" + grammars + "]");
        String input = null;
        // Perparing for event receieval
        mockEventDispatcher.clearEventFlag();
        // Issuing recognize
        recognizer.recognize(inboundStream);
        // Pending on an event
        while (!mockEventDispatcher.isEventFlag()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // The event should contain the result (NLSML)
        Event event = mockEventDispatcher.getEvent();
        if (event != null && event instanceof RecognitionCompleteEvent) {
            input = ((RecognitionCompleteEvent)event).getNlsmlDocument();
        }
        // TODO: check input, not always NLSML
        logger.debug("<-- recognize()");
        return input;
    }

    private String parse(String nslmlXmlText) {
        // Extracting the input/utterance from the NLSML
        logger.debug("--> parse() : [" + nslmlXmlText + "]");
        if (nslmlXmlText != null) {
            NlsmlDocument nlsml = new NlsmlDocument();
            nlsml.parse(nslmlXmlText);
            if (nlsml.isOk()) {
                return nlsml.getInput();
            }
        }
        logger.debug("<-- parse()");
        return null;
    }

    public String getDocument(String fileName) {
        // Reading a file, returning the contens as a String
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

    public void close() {
        // Terminating the sessions
        translator.close();
        recognizer.cancel();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Deleting the streams
        inboundStream.delete();
        outboundStream.delete();
    }
}
