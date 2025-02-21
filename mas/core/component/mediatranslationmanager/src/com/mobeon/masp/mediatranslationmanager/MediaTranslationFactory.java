/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.servicerequestmanager.ServiceRequest;
import com.mobeon.masp.stream.IStreamFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediatranslationmanager.configuration.TextToSpeechConfiguration;
import com.mobeon.masp.mediatranslationmanager.configuration.SpeechRecognizerConfiguration;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.RtspConnectionFactory;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.RtspSession;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.RtspConnection;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.RtspConnectionFactoryImpl;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.configuration.IConfigurationManager;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import java.util.Map;

/**
 * A factory for creating instances needed by Media Translation.
 * The factory also provides means to create instances of streams and
 * media objects.
 */
public class MediaTranslationFactory {
    private static ILogger logger = ILoggerFactory.getILogger(MediaTranslationFactory.class);
    // Singleton instance
    private static MediaTranslationFactory instance = null;
    private MediaTranslationManagerFacade mediaTranslationManager = null;
    private IStreamFactory streamFactory = null;
    private IMediaObjectFactory mediaObjectFactory = null;
    private RtspConnectionFactory rtspConnectionFactory = new RtspConnectionFactoryImpl();
    private IConfigurationManager configurationManager = null;

    private static final String MRCP = "mrcp";
    private static final String MCC = "xmp";

    private MimeType ssmlMimeType;
    private MimeType plainMimeType;
    private MimeType htmlMimeType;
    private MimeType pcmuMimeType;
    private MimeType pcmaMimeType;

    /**
     * Default constructor
     */
    private MediaTranslationFactory() {
        try {
            ssmlMimeType = new MimeType("application/ssml");
            plainMimeType = new MimeType("text/plain");
            htmlMimeType = new MimeType("text/html");
            pcmuMimeType = new MimeType("audio/pcmu");
            pcmaMimeType = new MimeType("audio/pcma");
        } catch (MimeTypeParseException e) {
            // This should not happen since the texts above are already validated.
            logger.error("Unknown mime type! " + e);
        }
    }

    /**
     * Initializes and checks validity of object memebers.
     */
    void init() {
        // Ensuring proper initialization
        boolean isInitialized = true;
        String message = "Not properly initialized since: ";
        if (mediaObjectFactory == null) {
            isInitialized = false;
            message += "The MediaObjectFactory is not set. ";
        }
        if (streamFactory == null) {
            isInitialized = false;
            message += "The StreamFactory is not set. ";
        }
        if (mediaTranslationManager == null) {
            isInitialized = false;
            message += "The MediaTranslationManagerFacade is not set. ";
        }
        if (!isInitialized) throw new IllegalAccessError(message);
    }

    /**
     * Singleton instance getter.
     * @return the singleton instance of MediaTranslationFactory.
     */
    public static MediaTranslationFactory getInstance() {
        if (instance == null) instance = new MediaTranslationFactory();
        return instance;
    }

    /**
     * Creates an instance of a text to speech translator.
     * @return a text to speech translator (or null).
     * @throws IllegalStateException
     */
    public TextToSpeech createTextToSpeech(ISession session) {
        if (logger.isDebugEnabled()) logger.debug("--> createTextToSpeech()");
        TextToSpeechConfiguration configuration =
                MediaTranslationConfiguration.getInstance().getTextToSpeechConfiguration();
        String protocol = configuration.getProtocol();
        if (logger.isDebugEnabled()) logger.debug("Protocol type: " + protocol);
        TextToSpeech translator = null;

        if (MCC.equals(protocol)) {
            translator = new MccTextToSpeech(session);

            ((MccTextToSpeech)translator).setRequestManager(mediaTranslationManager.getServiceRequestManager());
        } else if (MRCP.equals(protocol)) {
            translator = new MrcpTextToSpeech(session);
        } else {
            String message = "Unknown TTS engine protocol: [" + protocol + "]";
            logger.error(message);
            throw new IllegalStateException(message);
        }
        if (logger.isDebugEnabled()) logger.debug("<-- createTextToSpeech()");
        return translator;
    }

    public SpeechRecognizer createSpeechRecognizer(ISession session, Map<String, String> grammar) {
        if (logger.isDebugEnabled()) logger.debug("--> createSpeechRecognizer()");
        SpeechRecognizerConfiguration configuration =
                MediaTranslationConfiguration.getInstance().getSpeechRecognizerConfiguration();
        String protocol = configuration.getProtocol();
        if (logger.isDebugEnabled()) logger.debug("Protocol type: " + protocol);
        SpeechRecognizer recognizer = null;
        if (MRCP.equals(protocol)) {
            recognizer = new MrcpSpeechRecognizer(grammar);
            ((MrcpSpeechRecognizer)recognizer).setSession(session);
        } else {
            String message = "Unknown ASR engine protocol: [" + protocol + "]";
            logger.error(message);
            throw new IllegalStateException(message);
        }
        if (logger.isDebugEnabled()) logger.debug("<-- createSpeechRecognizer()");
        return recognizer;
    }

    public SpeechRecognizer createSpeechRecognizer(ISession session, SpeechRecognizer template) {
        if (logger.isDebugEnabled()) logger.debug("--> createSpeechRecognizer()");
        SpeechRecognizer recognizer = null;
        if (template instanceof MrcpSpeechRecognizer) {
            recognizer = new MrcpSpeechRecognizer(template);
            ((MrcpSpeechRecognizer)recognizer).setSession(session);
        } else {
            String message = "Unknown ASR class: [" + template.getClass() + "]";
            logger.error(message);
            throw new IllegalStateException(message);
        }
        if (logger.isDebugEnabled()) logger.debug("<-- createSpeechRecognizer()");
        return recognizer;
    }

    /**
     * Creates an instance of a speech recognizer.
     * @return a speech recognizer (or null).
     * @param session
     * @throws IllegalStateException
     * @deprecated
     */
    @Deprecated
    public SpeechRecognizer createSpeechRecognizer(ISession session) {
        if (logger.isDebugEnabled()) logger.debug("--> createSpeechRecognizer()");
        SpeechRecognizerConfiguration configuration =
                MediaTranslationConfiguration.getInstance().getSpeechRecognizerConfiguration();
        String protocol = configuration.getProtocol();
        if (logger.isDebugEnabled()) logger.debug("Protocol type: " + protocol);
        SpeechRecognizer recognizer = null;
        if (MRCP.equals(protocol)) {
            recognizer = new MrcpSpeechRecognizer(session);
        } else {
            String message = "Unknown ASR engine protocol: [" + protocol + "]";
            logger.error(message);
            throw new IllegalStateException(message);
        }
        if (logger.isDebugEnabled()) logger.debug("<-- createSpeechRecognizer()");
        return recognizer;
    }

    /**
     * Creates an instance of a service request.
     * @return a service request (or null)
     */
    public ServiceRequest createServiceRequest() {
        return new ServiceRequest();
    }

    public RtspSession createRtspSession(String host, int port) {
        RtspConnection rtspConnection = rtspConnectionFactory.create(host, port);
        return new RtspSession(rtspConnection);
    }

    /**
     * Stream factory getter.
     * @return a stream factory (or null).
     */
    public IStreamFactory getStreamFactory() {
        return streamFactory;
    }

    /**
     * Stream factory setter.
     * @param streamFactory a stream factory.
     */
    public void setStreamFactory(IStreamFactory streamFactory) {
        this.streamFactory = streamFactory;
    }

    /**
     * Media translation manager facade getter.
     * @return media translation manager face (or null).
     */
    public MediaTranslationManagerFacade getMediaTranslationManager() {
        return mediaTranslationManager;
    }

    /**
     * Media translation manager facade setter.
     * @param mediaTranslationManager a media translation manager facade.
     */
    public void setMediaTranslationManager(MediaTranslationManagerFacade mediaTranslationManager) {
        this.mediaTranslationManager = mediaTranslationManager;
    }

    /**
     * Media object factory getter.
     * @return a media object factory (or null).
     */
    public IMediaObjectFactory getMediaObjectFactory() {
        return mediaObjectFactory;
    }

    /**
     * Media object factory setter.
     * @param mediaObjectFactory a media object factory.
     */
    public void setMediaObjectFactory(IMediaObjectFactory mediaObjectFactory) {
        this.mediaObjectFactory = mediaObjectFactory;
    }

    public RtspConnectionFactory getRtspServerFactory() {
        return rtspConnectionFactory;
    }

    public void setRtspConnectionFactory(RtspConnectionFactory rtspConnectionFactory) {
        this.rtspConnectionFactory = rtspConnectionFactory;
    }

    /**
     * Getter for the SSML mime type.
     * @return a mime type.
     */
    public MimeType getSsmlMimeType() {
        return ssmlMimeType;
    }

    /**
     * Getter for the plain text mime type.
     * @return a mime type.
     */
    public MimeType getPlainMimeType() {
        return plainMimeType;
    }

    /**
     * Getter for the HTML mime type.
     * @return a mime type.
     */
    public MimeType getHtmlMimeType() {
        return htmlMimeType;
    }

    /**
     * Getter for the PCMU mime type.
     * @return a mime type.
     */
    public MimeType getPcmuMimeType() {
        return pcmuMimeType;
    }

    /**
     * Getter for the PCMA mime type.
     * @return a mime type.
     */
    public MimeType getPcmaMimeType() {
        return pcmaMimeType;
    }

    public void setConfigurationManager(IConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }
    public IConfigurationManager getConfigurationManager() {
        return configurationManager;
    }
}
