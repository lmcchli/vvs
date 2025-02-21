package com.mobeon.masp.mediatranslationmanager;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import org.apache.log4j.xml.DOMConfigurator;
import com.mobeon.common.configuration.*;

import java.util.LinkedList;
import java.util.Collection;

public class MediaTranslationManagerTest extends MockObjectTestCase {
    MediaTranslationManager mtm;

    Mock mockConfigurationGroup;
    Mock mockConfiguration;
    Mock mockConfigurationManager;

    public void setUp() {
        MediaTranslationFactory factory = MediaTranslationFactory.getInstance();
        mtm = new MediaTranslationManagerFacade();
        mockConfigurationGroup = mock(IGroup.class);
        mockConfiguration = mock(IConfiguration.class);
        mockConfigurationManager = mock(IConfigurationManager.class);
        factory.setConfigurationManager((IConfigurationManager)mockConfigurationManager.proxy());
    }

    public void testGetTextToSpeechLanguages() {
        mockConfigurationManager.stubs().method("getConfiguration").will(returnValue(mockConfiguration.proxy()));
        mockConfiguration.stubs().method("getGroup").will(returnValue(mockConfigurationGroup.proxy()));
        mockConfigurationGroup.stubs().method("getString").will(returnValue("apa,gris"));
        assertNotNull(mtm.getTextToSpeechLanguages());
        assertEquals(2, mtm.getTextToSpeechLanguages().size());
        assertTrue(mtm.getTextToSpeechLanguages().contains("apa"));
        assertTrue(mtm.getTextToSpeechLanguages().contains("gris"));
    }

    public void testFailOfGetTextToSpeechLanguages() {
        Collection<String> languages;
        // Failing to get undefined group
        mockConfigurationManager.stubs().method("getConfiguration").will(returnValue(mockConfiguration.proxy()));
        mockConfiguration.stubs().method("getGroup").will(throwException(new UnknownGroupException("Mock", null)));
        languages = mtm.getTextToSpeechLanguages();
        assertNotNull(languages);
        assertEquals(0, languages.size());

        // Failing to get group due to abiguity
        mockConfiguration.stubs().method("getGroup").will(throwException(new GroupCardinalityException("Mock")));
        languages = mtm.getTextToSpeechLanguages();
        assertNotNull(languages);
        assertEquals(0, languages.size());

        // Failing to get parameter from group
        mockConfiguration.stubs().method("getGroup").will(returnValue(mockConfigurationGroup.proxy()));
        mockConfigurationGroup.stubs().method("getFullName").will(returnValue("Mock"));
        mockConfigurationGroup.stubs().method("getString")
                .will(throwException(new UnknownParameterException("Mock", (IGroup)mockConfigurationGroup.proxy())));
        languages = mtm.getTextToSpeechLanguages();
        assertNotNull(languages);
        assertEquals(0, languages.size());

        // Got an empty parameter value
        mockConfigurationGroup.stubs().method("getString").will(returnValue(""));
        languages = mtm.getTextToSpeechLanguages();
        assertNotNull(languages);
        assertEquals(0, languages.size());
    }
}
