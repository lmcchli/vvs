/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.common.externalcomponentregister.ILocateService;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.externalcomponentregister.NoServiceFoundException;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediatranslationmanager.configuration.SpeechRecognizerConfiguration;
import com.mobeon.masp.mediatranslationmanager.configuration.TextToSpeechConfiguration;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.configuration.UnknownParameterException;
import com.mobeon.common.configuration.GroupCardinalityException;
import com.mobeon.common.configuration.UnknownGroupException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * This Singleton class is responsible for the configuration of the MTM.
 * MediaTranslationConfiguration provides getters for eac
 */
public class MediaTranslationConfiguration {
    private static final ILogger logger =
        ILoggerFactory.getILogger(MediaTranslationConfiguration.class);
    // The singleton instance.
    private static MediaTranslationConfiguration singletonInstance = null;

    /** Group name in configuration file. */
    private static final String CONFIGURATION_GROUP = "mediatranslationmanager";
    private static final String TEXT_TO_SPEECH_LANGUAGES = "texttospeechlanguages";

    private ILocateService serviceLocator;

    /**
     * Singleton instance getter.
     * @return the singleton instance of MediaTranslationConfiguration.
     */
    public static MediaTranslationConfiguration getInstance() {
        if (singletonInstance == null) singletonInstance = new MediaTranslationConfiguration();
        return singletonInstance;
    }

    /**
     * Private default constructor.
     */
    private MediaTranslationConfiguration() {
        MediaTranslationManagerFacade mtm = MediaTranslationFactory.getInstance().getMediaTranslationManager();
        serviceLocator = mtm.getServiceLocator();
    }

    /**
     * Retrieves an instance of a Service entry from MCR via ECR.
     * logs.error "Failed to locate service [<serviceName>]" - if failing to
     * locate the service this message is logged and the method returns null.
     * @param serviceName the name of the service.
     * @return the service instance or null if not found.
     */
    protected IServiceInstance getService(String serviceName) {
        IServiceInstance service = null;
        try {
            service = serviceLocator.locateService(serviceName);
        } catch (NoServiceFoundException e) {
            logger.error("Failed to locate service [" +  serviceName + "]");
            e.printStackTrace(System.out);
        }
        return service;
    }

    /**
     * This method locates the service texttospeech and returns an
     * instance of {@link TextToSpeechConfiguration}.
     * @return a valid or invalid instance (must be checked further).
     */
    public TextToSpeechConfiguration getTextToSpeechConfiguration() {
        IServiceInstance service = getService(IServiceName.TEXT_TO_SPEECH);

        return new TextToSpeechConfiguration(service);
    }

  /**
     * This method locates the service speechrecognition and returns an
     * instance of {@link SpeechRecognizerConfiguration}.
     * @return a valid or invalid instance (must be checked further).
     */
    public SpeechRecognizerConfiguration getSpeechRecognizerConfiguration() {
        IServiceInstance service = getService(IServiceName.SPEECH_RECOGNITION);

        return new SpeechRecognizerConfiguration(service);
    }

    /**
     * Returns a list of available TTS languages.
     * @return a collection of strings.
     */
    public Collection<String> getTextToSpeechLanguages() {
	// TODO: optimize the implementation here
	// TODO: implement subscribtion for configuration updates
        Collection<String> textToSpeechLanguages = new LinkedList<String>();
        String value = "";

        IConfiguration configuration =
                MediaTranslationFactory.getInstance().getConfigurationManager().getConfiguration();
        try {
            value = "en,se"; //Hardcode because it is outscoped in MIO for now and we do not want to show this feature in the configuration
        } catch (Exception e) {
            logger.warn("Failed to get parameter '" + TEXT_TO_SPEECH_LANGUAGES +
                    "' from group '" + CONFIGURATION_GROUP, e);
        }
        if (value.length() > 0) {
            StringTokenizer tokens = new StringTokenizer(value, ",");
            while (tokens.hasMoreTokens()) {
                textToSpeechLanguages.add(tokens.nextToken());
            }
            if (logger.isDebugEnabled()) logger.debug("Found " + textToSpeechLanguages.size() + " languages");
        } else {
           logger.warn("No languages defined for TTS");
        }
        return textToSpeechLanguages;
    }
}
