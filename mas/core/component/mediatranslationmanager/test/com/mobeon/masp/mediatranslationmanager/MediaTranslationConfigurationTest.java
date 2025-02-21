/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.masp.mediatranslationmanager.configuration.SpeechRecognizerConfiguration;
import com.mobeon.masp.mediatranslationmanager.configuration.TextToSpeechConfiguration;
import junit.framework.TestCase;

import java.util.Collection;

/**
 * This class implements the testing of {@link MediaTranslationConfiguration}.
 */
public class MediaTranslationConfigurationTest extends TestCase {
    private MediaTranslationManagerFacade mtm = null;

    public void setUp() {
        Utility.getSingleton().initialize("test/TestComponentConfig.xml");
        mtm = Utility.getSingleton().getMediaTranslationManager(Utility.getSingleton().getSession());
    }

    /**
     * Verifying that the MediaTranslationConfiguration is Singleton.
     */
    public void testGetInstance() {
        MediaTranslationConfiguration
                instance1 = MediaTranslationConfiguration.getInstance();
        MediaTranslationConfiguration
                instance2 = MediaTranslationConfiguration.getInstance();
        assertNotNull("Got null singleton", instance1);
        assertEquals("Should have been same instance", instance1, instance2);
    }

    /**
     * Verifying that the MediaTranslationConfiguration is Singleton.
     */
    public void testGetParameter() {
        MediaTranslationConfiguration
                instance = MediaTranslationConfiguration.getInstance();
        TextToSpeechConfiguration tts = instance.getTextToSpeechConfiguration();
        SpeechRecognizerConfiguration asr = instance.getSpeechRecognizerConfiguration();
        assertEquals("Wrong value", "mrcp", tts.getProtocol());
        assertEquals("Wrong value", "mrcp", asr.getProtocol());
        String noSuchParameter = asr.getParameter("kalle", "anka");
        assertEquals("No such parameter", "anka", noSuchParameter);
    }

    public void testGetMrcpTextToSpeech() {
        MediaTranslationConfiguration
                instance = MediaTranslationConfiguration.getInstance();
        TextToSpeechConfiguration configuration = instance.getTextToSpeechConfiguration();
        assertNotNull(configuration.getHost());
        assertFalse("127.0.0.1".equals(configuration.getHost()));
        assertEquals(4900, configuration.getPort());
    }

    public void testGetMccTextToSpeech() {
        MediaTranslationConfiguration
                instance = MediaTranslationConfiguration.getInstance();
        TextToSpeechConfiguration configuration = instance.getTextToSpeechConfiguration();
        assertEquals("10.11.0.111", configuration.getHost());
    }

    public void testGetMrcpSpeechRecognizer() {
        MediaTranslationConfiguration
                instance = MediaTranslationConfiguration.getInstance();
        SpeechRecognizerConfiguration configuration = instance.getSpeechRecognizerConfiguration();
        assertNotNull(configuration.getHost());
        assertFalse("127.0.0.1".equals(configuration.getHost()));
        assertEquals(4900, configuration.getPort());
    }

    public void testGetTextToSpeechLanguages() {
        MediaTranslationConfiguration
                instance = MediaTranslationConfiguration.getInstance();
        Collection<String> languages = instance.getTextToSpeechLanguages();
        assertEquals(2, languages.size());
        String[] language = new String[languages.size()];
        languages.toArray(language);
        assertEquals("en-GB", language[0]);
        assertEquals("se-SV", language[1]);
    }
}
