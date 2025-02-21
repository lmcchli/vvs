/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.servicerequestmanager.IServiceRequestManager;
import com.mobeon.masp.stream.IStreamFactory;
import com.mobeon.common.externalcomponentregister.ILocateService;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.configuration.IConfigurationManager;

import java.util.Map;
import java.util.Collection;

/**
 * This is a facade for the Media Translation Manager.
 * MediaTranslationManagerFacade implements the public interface MediaTranslationManager.
 * Translators and recognizers are obtained from the MediaTranslationManager(Facade).
 */
public class MediaTranslationManagerFacade implements MediaTranslationManager {
    private MediaTranslationFactory factory = MediaTranslationFactory.getInstance();
    private IServiceRequestManager serviceRequestManager = null;
    private ILocateService serviceLocator = null;

    /**
     * Default constructor.
     */
    public MediaTranslationManagerFacade() {
        factory.setMediaTranslationManager(this);
    }

    /**
     * Initializes and checks validity of object memebers.
     */
    void init() {
    }

    public SpeechRecognizer getSpeechRecognizer(ISession session, Map<String, String> grammar) {
        return factory.createSpeechRecognizer(session, grammar);
    }

    public SpeechRecognizer getSpeechRecognizer(ISession session, SpeechRecognizer prototype) {
        return factory.createSpeechRecognizer(session, prototype);
    }

    /**
     * Creates an instance of a text to speech translator.
     * The type of text to speech translator depends upon configuration.
     * @return a text to speech translator.
     */
    public TextToSpeech getTextToSpeech(ISession session) {
        return factory.createTextToSpeech(session);
    }

    /**
     * Returns the languages available for TTS.
     *
     * @return a collection of strings (eg. "se-SV", "en-GB")
     */
    public Collection<String> getTextToSpeechLanguages() {
        return MediaTranslationConfiguration.getInstance().getTextToSpeechLanguages();
    }

    /**
     * This is a setter for the service locator.
     * @param serviceLocator
     */
    public void setServiceLocator(ILocateService serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    /**
     * This is a getter for the service locator.
     * @return a service locator.
     */
    public ILocateService getServiceLocator() {
        return this.serviceLocator;
    }

    /**
     * Service request manager getter.
     * @return a service request manager (or null).
     */
    public IServiceRequestManager getServiceRequestManager() {
        return serviceRequestManager;
    }

    /**
     * Service request manager setter.
     * @param serviceRequestManager a service request manager.
     */
    public void setServiceRequestManager(IServiceRequestManager serviceRequestManager) {
        this.serviceRequestManager = serviceRequestManager;
    }

    /**
     * This a setter for the stream factory.
     * Since this is a facade the factory is transferred to the {@link MediaTranslationFactory}
     * @param streamFactory
     */
    public void setStreamFactory(IStreamFactory streamFactory) {
        factory.setStreamFactory(streamFactory);
    }

    /**
     * This is a setter for the media object factory.
     * @param mediaObjectFactory
     */
    public void setMediaObjectFactory(IMediaObjectFactory mediaObjectFactory) {
        factory.setMediaObjectFactory(mediaObjectFactory);
    }

    public void setConfigurationManager(IConfigurationManager configurationManager) {
        factory.setConfigurationManager(configurationManager);
    }
}
