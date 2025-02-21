/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess;

import com.mobeon.masp.callmanager.CallMediaTypes;
import com.mobeon.masp.callmanager.InboundCall;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.Disconnecter;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.ModuleCollection;
import com.mobeon.masp.mediacontentmanager.IMediaContentResource;
import com.mobeon.masp.mediacontentmanager.MediaContentManagerException;
import com.mobeon.masp.mediacontentmanager.MediaContentResourceProperties;
import com.mobeon.masp.mediacontentmanager.IMediaQualifier;
import com.mobeon.masp.mediahandler.MediaHandler;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaMimeTypes;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;
import org.jmock.core.Constraint;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;

/**
 * Tests the Media related functions in PlatformAccess
 *
 * @author ermmaha
 */
public class PlatformAccessMediaTest extends PlatformAccessTest {
    protected String language = "sv";
    protected String voiceVariant = "voice";
    protected String videoVariant = "video";

    // Media Resource Types
    protected final String PROMPT = "prompt";
    protected final String FUNGREETING = "fungreeting";
    protected final String SWA = "SWA";

    // MediaObjects
    protected Mock jmockMediaPrompt0;
    protected Mock jmockMediaPrompt1;
    protected Mock jmockMediaFunGreeting0;
    protected Mock jmockMediaFunGreeting1;
    protected Mock jmockMediaFunGreeting2;
    protected Mock jmockMediaFunGreeting3;
    protected Mock jmockMediaSWA0;
    protected Mock jmockMediaSWA1;

    // MediaContentResource
    protected Mock jmockMediaResourcePromptVoice;
    protected Mock jmockMediaResourcePromptVideo;
    protected Mock jmockMediaResourceFunGreetingVoice;
    protected Mock jmockMediaResourceFunGreetingVideo;
    protected Mock jmockMediaResourceFunGreetingVideoAmr;
    protected Mock jmockMediaResourceSWA;

    // IMediaQualifiers
    protected IMediaQualifier[] qualifiers;
    
    protected Mock jmockMediaHandler;

    public PlatformAccessMediaTest(String name) {
        super(name);

        jmockMediaPrompt0 = mock(IMediaObject.class);
        jmockMediaPrompt1 = mock(IMediaObject.class);
        jmockMediaFunGreeting0 = mock(IMediaObject.class);
        jmockMediaFunGreeting1 = mock(IMediaObject.class);
        jmockMediaFunGreeting2 = mock(IMediaObject.class);
        jmockMediaFunGreeting3 = mock(IMediaObject.class);
        jmockMediaSWA0 = mock(IMediaObject.class);
        jmockMediaSWA1 = mock(IMediaObject.class);

        jmockMediaResourcePromptVoice = mock(IMediaContentResource.class);
        jmockMediaResourcePromptVideo = mock(IMediaContentResource.class);
        jmockMediaResourceFunGreetingVoice = mock(IMediaContentResource.class);
        jmockMediaResourceFunGreetingVideo = mock(IMediaContentResource.class);
        jmockMediaResourceFunGreetingVideoAmr = mock(IMediaContentResource.class);
        jmockMediaResourceSWA = mock(IMediaContentResource.class);

        Mock jmockQualifier = mock(IMediaQualifier.class);
        qualifiers = new IMediaQualifier[]{(IMediaQualifier) jmockQualifier.proxy()};

        jmockMediaHandler = mock(MediaHandler.class);
        
        setupMedias();
    }

    /**
     * Tests the systemGetMediaContentIds function. Uses multiple instances of PlatformAccess to access the function.
     *
     * @throws Exception if testcase fails.
     */
    public void testSystemGetMediaContentIds() throws Exception {
        //setupMediaContentManager(jmockMediaResourcePromptVoice);
        String[] mediaContentIds;

        PlatformAccess platformAccess1 = createPlatformAccess();
        PlatformAccess platformAccess2 = createPlatformAccess();

        //Test empty prompt resource
        try {
            platformAccess1.systemGetMediaContentIds("prompt", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
            System.out.println(e.getDescription());
        }

        platformAccess1.systemSetMediaResources(language, voiceVariant, null);

        //test prompt
        mediaContentIds = platformAccess1.systemGetMediaContentIds("Prompt", qualifiers);
        assertArray(mediaContentIds, new String[]{"prompt0", "prompt1"});
        mediaContentIds = platformAccess2.systemGetMediaContentIds("Prompt", null);
        assertArray(mediaContentIds, new String[]{"prompt0", "prompt1"});

        //test fungreeting
        //setupMediaContentManager(jmockMediaResourceFunGreetingVoice);
        platformAccess1.systemSetMediaResource(FUNGREETING, language, voiceVariant, null);
        mediaContentIds = platformAccess1.systemGetMediaContentIds("fungreeting", null);
        assertArray(mediaContentIds, new String[]{"fun0"});

        //test swa
        //setupMediaContentManager(jmockMediaResourceSWA);
        platformAccess1.systemSetMediaResource(SWA, language, voiceVariant, null);
        mediaContentIds = platformAccess1.systemGetMediaContentIds("SWA", null);
        assertArray(mediaContentIds, new String[]{"swa0"});

        //test exceptions
        jmockMediaResourcePromptVoice.expects(once()).method("getMediaContentIDs").
                will(throwException(new MediaContentManagerException("Bad qualifier")));
        try {
            platformAccess1.systemGetMediaContentIds("Prompt", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // test empty ids
        jmockMediaResourcePromptVoice.expects(once()).method("getMediaContentIDs").will(returnValue(new ArrayList<String>()));
        mediaContentIds = platformAccess1.systemGetMediaContentIds("Prompt", null);
        assertTrue(mediaContentIds.length == 0);
    }

    /**
     * Tests the systemGetMediaContent function. Uses multiple instances of PlatformAccess to access the function.
     *
     * @throws Exception if testcase fails.
     */
    public void testSystemGetMediaContent() throws Exception {
        //setupMediaContentManager(jmockMediaResourcePromptVoice);

        PlatformAccess platformAccess1 = createPlatformAccess();
        PlatformAccess platformAccess2 = createPlatformAccess();

        platformAccess2.systemSetMediaResources(language, voiceVariant, null);

        IMediaObject[] iMediaObjects = platformAccess1.systemGetMediaContent("prompt", "beep");
        assertEquals(iMediaObjects[0], jmockMediaPrompt0.proxy());
        iMediaObjects = platformAccess2.systemGetMediaContent("prompt", "beep");
        assertEquals(iMediaObjects[0], jmockMediaPrompt0.proxy());

        // test MediaContentManagerException
        jmockMediaResourcePromptVoice.expects(once()).method("getMediaContent").
                will(throwException(new MediaContentManagerException("Could not get mediacontent")));
        try {
            platformAccess1.systemGetMediaContent("prompt", "beep");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // test IllegalArgumentException
        jmockMediaResourcePromptVoice.expects(once()).method("getMediaContent").
                will(throwException(new IllegalArgumentException("Could not get mediacontent")));
        try {
            platformAccess1.systemGetMediaContent("prompt", "beep");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        // test empty content
        jmockMediaResourcePromptVoice.expects(once()).method("getMediaContent").will(returnValue(null));
        iMediaObjects = platformAccess1.systemGetMediaContent("prompt", "beep", null);
        assertNull(iMediaObjects);
    }

    /**
     * Test calling the systemGetMediaContent and systemGetMediaContentIds
     * functions but no selected content is put in the session by the callmanager
     *
     * @throws Exception if testcase fails.
     */
    public void testSystemGetMediaError() throws Exception {
        //setupMediaContentManager(jmockMediaResourcePromptVoice);
        // clear the object that was set in the session
        stubSession.setData("selectedcallmediatypes", null);

        //platformAccess.systemSetMediaResources(language, voiceVariant, videoVariant);

        try {
            platformAccess.systemGetMediaContentIds("prompt", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        try {
            platformAccess.systemGetMediaContent("prompt", "beep");
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
    }

    /**
     * Test calling the systemGetMediaContent and systemGetMediaContentIds functions with invalid type
     *
     * @throws Exception if testcase fails.
     */
    public void testInvalidResourceType() throws Exception {
        try {
            platformAccess.systemGetMediaContentIds("invalidtype", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }

        try {
            platformAccess.systemGetMediaContent("invalidtype", "beep", null);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
    }

    /**
     * Test the systemSetMediaResources function. Another language is set after the first was set.
     *
     * @throws Exception if testcase fails.
     */
    public void testSystemSetMediaResources() throws Exception {
        //setupMediaContentManager(jmockMediaResourcePromptVoice);

        PlatformAccess platformAccess1 = createPlatformAccess();

        platformAccess1.systemSetMediaResources(language, voiceVariant, null);

        // test prompt
        String[] mediaContentIds = platformAccess1.systemGetMediaContentIds("Prompt", null);
        assertArray(mediaContentIds, new String[]{"prompt0", "prompt1"});

        // Set video call
        fakeEarlySelection(new MimeType("audio/pcmu"), new MimeType("video/h263"));

        platformAccess1.systemSetMediaResources(language, null, videoVariant);

        // The mocked MediaContentResource returns the same as before
        mediaContentIds = platformAccess1.systemGetMediaContentIds("Prompt", null);
        assertArray(mediaContentIds, new String[]{"prompt0", "prompt1"});

        // test empty resources
        List<IMediaContentResource> resources = new ArrayList<IMediaContentResource>();
        jmockMediaContentManager.stubs().method("getMediaContentResource").will(returnValue(resources));
        try {
            platformAccess1.systemSetMediaResources(language, voiceVariant, videoVariant);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
    }

    public void testSystemSetMediaResource() throws Exception {
        PlatformAccess platformAccess = createPlatformAccess();

        // 1
        stubSession.setData("selectedcallmediatypes", null);
        platformAccess.systemSetMediaResource(FUNGREETING, language, voiceVariant, null);

        Object data = stubSession.getData("selectedcallmediatypes");
        assertNull("selectedcallmediatypes should not have been set.", data);
        data = stubSession.getData("callmediatypesarray");
        assertNotNull("callmediatypesarray should not be null.", data);
        if (data instanceof CallMediaTypes[]) {
            CallMediaTypes[] callMediaTypes = (CallMediaTypes[])data;
            MediaMimeTypes expectedTypes =
                    new MediaMimeTypes(new MimeType("audio/pcmu"));
            assertTrue("Wrong number of call media types.",
                    callMediaTypes.length == 1);
            assertTrue("Not correct types in array",
                    callMediaTypes[0].getOutboundMediaTypes().compareTo(expectedTypes));
        } else {
            fail("callmediatypesarray should be a CallMediaTypes[]");
        }


        //2
        stubSession.setData("selectedcallmediatypes", null);
        platformAccess.systemSetMediaResource(FUNGREETING, language, null, videoVariant);

        data = stubSession.getData("selectedcallmediatypes");
        assertNull("selectedcallmediatypes should not have been set.", data);
        data = stubSession.getData("callmediatypesarray");
        assertNotNull("callmediatypesarray should not be null.", data);
        if (data instanceof CallMediaTypes[]) {
            CallMediaTypes[] callMediaTypes = (CallMediaTypes[])data;
            assertTrue("Wrong number of call media types.",
                    callMediaTypes.length == 2);
            MediaMimeTypes expectedTypes =
                    new MediaMimeTypes(new MimeType("audio/pcmu"), new MimeType("video/h263"));
            MediaMimeTypes expectedTypes2 =
                    new MediaMimeTypes(new MimeType("audio/amr"), new MimeType("video/h263"));
            assertTrue("Not correct types in array",
                    callMediaTypes[0].getOutboundMediaTypes().compareTo(expectedTypes2));
            assertTrue("Not correct types in array",
                    callMediaTypes[1].getOutboundMediaTypes().compareTo(expectedTypes));
        } else {
            fail("callmediatypesarray should be a CallMediaTypes[]");
        }


        //3
        stubSession.setData("selectedcallmediatypes", null);
        platformAccess.systemSetMediaResource(PROMPT, language, null, videoVariant);

        data = stubSession.getData("selectedcallmediatypes");
        assertNull("selectedcallmediatypes should not have been set.", data);
        data = stubSession.getData("callmediatypesarray");
        assertNotNull("callmediatypesarray should not be null.", data);
        if (data instanceof CallMediaTypes[]) {
            CallMediaTypes[] callMediaTypes = (CallMediaTypes[])data;
            MediaMimeTypes expectedTypes =
                    new MediaMimeTypes(new MimeType("audio/pcmu"), new MimeType("video/h263"));
            assertTrue("Wrong number of call media types.",
                    callMediaTypes.length == 1);
            assertTrue("Not correct types in array",
                    callMediaTypes[0].getOutboundMediaTypes().compareTo(expectedTypes));
        } else {
            fail("callmediatypesarray should be a CallMediaTypes[]");
        }

        //4
        stubSession.setData("selectedcallmediatypes", null);
        platformAccess.systemSetMediaResource(PROMPT, language, voiceVariant, null);

        data = stubSession.getData("selectedcallmediatypes");
        assertNull("selectedcallmediatypes should not have been set.", data);
        data = stubSession.getData("callmediatypesarray");
        assertNotNull("callmediatypesarray should not be null.", data);
        if (data instanceof CallMediaTypes[]) {
            CallMediaTypes[] callMediaTypes = (CallMediaTypes[])data;
            MediaMimeTypes expectedTypes =
                    new MediaMimeTypes(new MimeType("audio/pcmu"));
            assertTrue("Wrong number of call media types.",
                    callMediaTypes.length == 1);
            assertTrue("Not correct types in array",
                    callMediaTypes[0].getOutboundMediaTypes().compareTo(expectedTypes));
        } else {
            fail("callmediatypesarray should be a CallMediaTypes[]");
        }

        //5
        MediaMimeTypes mediaTypes =
                new MediaMimeTypes(new MimeType("audio/pcmu"), new MimeType("video/h263"));
        stubSession.setData("selectedcallmediatypes",
                new CallMediaTypes(mediaTypes, jmockMediaResourcePromptVoice.proxy()));
        stubSession.setData("callmediatypesarray", null);

        platformAccess.systemSetMediaResource(FUNGREETING, language, voiceVariant, null);

        //6
        mediaTypes = new MediaMimeTypes(new MimeType("audio/amr"), new MimeType("video/h263"));
        stubSession.setData("selectedcallmediatypes",
                new CallMediaTypes(mediaTypes, jmockMediaResourcePromptVoice.proxy()));
        stubSession.setData("callmediatypesarray", null);

        platformAccess.systemSetMediaResource(FUNGREETING, language, voiceVariant, null);
    }

    /**
     * Test the systemSetEarlyMediaResource function.
     *
     * @throws Exception if testcase fails.
     */
    public void testSystemSetEarlyMediaResource() throws Exception {
        setupMediaContentManager(jmockMediaResourcePromptVoice);

        Mock jmockConnection = mock(Connection.class);
        Mock jmockCall = mock(InboundCall.class);
        Mock jmockCCXMLExecutionContext = mock(CCXMLExecutionContext.class);

        jmockCall.expects(once()).method("negotiateEarlyMediaTypes");
        jmockConnection.stubs().method("getCall").will(returnValue(jmockCall.proxy()));
        int timeout = 5;
        jmockConnection.expects(once()).method("getCallManagerWaitTimeout").will(returnValue(timeout));
        jmockCCXMLExecutionContext.stubs().method("getCurrentConnection").will(returnValue(jmockConnection.proxy()));
        jmockCCXMLExecutionContext.stubs().method("getSession").will(returnValue(stubSession));
        jmockCCXMLExecutionContext.expects(once()).method("waitForEvent").with(new Constraint[] {
                eq(Constants.Event.ERROR_CONNECTION),
                isA(String.class),
                eq(timeout),
                isA(Disconnecter.class),
                eq(jmockConnection.proxy()),
                isA(String[].class)
        });
        stubSession.setData("MediaManager", null);
        PlatformAccess platformAccess1 = createPlatformAccess((ExecutionContext)jmockCCXMLExecutionContext.proxy());

        platformAccess1.systemSetEarlyMediaResource(language, voiceVariant, videoVariant);

        // Test empty resources
        List<IMediaContentResource> resources = new ArrayList<IMediaContentResource>();
        jmockMediaContentManager.stubs().method("getMediaContentResource").will(returnValue(resources));
        try {
            platformAccess1.systemSetEarlyMediaResource(language, voiceVariant, videoVariant);
            fail("Expected PlatformAccessException");
        } catch (PlatformAccessException e) {
            assertEquals(EventType.SYSTEMERROR, e.getMessage());
        }
    }

    /**
     * Setup stubs for the jmockMediaContentManager
     *
     * @param jmockMediaResource to return when the getMediaContentResource is called
     */
    private void setupMediaContentManager(Mock jmockMediaResource) {
        List<IMediaContentResource> resources = new ArrayList<IMediaContentResource>();
        resources.add((IMediaContentResource) jmockMediaResource.proxy());
        jmockMediaContentManager.stubs().method("getMediaContentResource").will(returnValue(resources));
    }

    private void setupMediaContentManager() {
        MediaContentResourceProperties resourcePropertiesVoicePrompt = new MediaContentResourceProperties();
        resourcePropertiesVoicePrompt.setLanguage(language);
        resourcePropertiesVoicePrompt.setVoiceVariant(voiceVariant);
        resourcePropertiesVoicePrompt.setVideoVariant(null);
        resourcePropertiesVoicePrompt.setType(PROMPT);

        MediaContentResourceProperties resourcePropertiesVideoPrompt = new MediaContentResourceProperties();
        resourcePropertiesVideoPrompt.setLanguage(language);
        resourcePropertiesVideoPrompt.setVoiceVariant(null);
        resourcePropertiesVideoPrompt.setVideoVariant(videoVariant);
        resourcePropertiesVideoPrompt.setType(PROMPT);

        MediaContentResourceProperties resourcePropertiesVoiceFunGreeting = new MediaContentResourceProperties();
        resourcePropertiesVoiceFunGreeting.setLanguage(language);
        resourcePropertiesVoiceFunGreeting.setVoiceVariant(voiceVariant);
        resourcePropertiesVoiceFunGreeting.setVideoVariant(null);
        resourcePropertiesVoiceFunGreeting.setType(FUNGREETING);

        MediaContentResourceProperties resourcePropertiesVideoFunGreeting = new MediaContentResourceProperties();
        resourcePropertiesVideoFunGreeting.setLanguage(language);
        resourcePropertiesVideoFunGreeting.setVoiceVariant(null);
        resourcePropertiesVideoFunGreeting.setVideoVariant(videoVariant);
        resourcePropertiesVideoFunGreeting.setType(FUNGREETING);

        MediaContentResourceProperties resourcePropertiesSWA = new MediaContentResourceProperties();
        resourcePropertiesSWA.setLanguage(language);
        resourcePropertiesSWA.setVoiceVariant(voiceVariant);
        resourcePropertiesSWA.setVideoVariant(null);
        resourcePropertiesSWA.setType(SWA);

        jmockMediaContentManager.stubs().method("getMediaContentResource")
                .will(returnValue(new ArrayList<IMediaContentResource>()));

        List<IMediaContentResource> voicePromptResources = new ArrayList<IMediaContentResource>();
        voicePromptResources.add((IMediaContentResource) jmockMediaResourcePromptVoice.proxy());
        jmockMediaContentManager.stubs().method("getMediaContentResource")
                .with(eq(resourcePropertiesVoicePrompt))
                .will(returnValue(voicePromptResources));

        List<IMediaContentResource> videoPromptResources = new ArrayList<IMediaContentResource>();
        videoPromptResources.add((IMediaContentResource) jmockMediaResourcePromptVideo.proxy());
        jmockMediaContentManager.stubs().method("getMediaContentResource")
                .with(eq(resourcePropertiesVideoPrompt))
                .will(returnValue(videoPromptResources));

        List<IMediaContentResource> voiceFunGreetingResources = new ArrayList<IMediaContentResource>();
        voiceFunGreetingResources.add((IMediaContentResource) jmockMediaResourceFunGreetingVoice.proxy());
        jmockMediaContentManager.stubs().method("getMediaContentResource")
                .with(eq(resourcePropertiesVoiceFunGreeting))
                .will(returnValue(voiceFunGreetingResources));

        List<IMediaContentResource> videoFunGreetingResources = new ArrayList<IMediaContentResource>();
        videoFunGreetingResources.add((IMediaContentResource) jmockMediaResourceFunGreetingVideoAmr.proxy());
        videoFunGreetingResources.add((IMediaContentResource) jmockMediaResourceFunGreetingVideo.proxy());
        jmockMediaContentManager.stubs().method("getMediaContentResource")
                .with(eq(resourcePropertiesVideoFunGreeting))
                .will(returnValue(videoFunGreetingResources));

        List<IMediaContentResource> voiceSWAResources = new ArrayList<IMediaContentResource>();
        voiceSWAResources.add((IMediaContentResource) jmockMediaResourceSWA.proxy());
        jmockMediaContentManager.stubs().method("getMediaContentResource")
                .with(eq(resourcePropertiesSWA))
                .will(returnValue(voiceSWAResources));
    }

    private void setupMedias() {
        // setup the prompt-mediaresource
        setupMediaPrompt();

        // setup the swa-mediaresource
        setupMediaSWA();

        // setup the swa-mediaresource
        setupMediaFunGreeting();

        setupMediaContentManager();
    }

    /**
     * Mocks a prompt mediaresource
     */
    private void setupMediaPrompt() {
        IMediaObject[] mockMediaObjectPrompts0 = new IMediaObject[]{(IMediaObject) jmockMediaPrompt0.proxy()};
        jmockMediaResourcePromptVoice.stubs().method("getMediaContent").with(eq("beep"), eq(null)).
                will(returnValue(mockMediaObjectPrompts0));
        jmockMediaResourcePromptVoice.stubs().method("getID").will(returnValue("promptVoice"));

        // setup mediaobjectids for the prompt-mediaresource
        ArrayList<String> promptIds = new ArrayList<String>();
        promptIds.add("prompt0");
        promptIds.add("prompt1");
        jmockMediaResourcePromptVoice.stubs().method("getMediaContentIDs").will(returnValue(promptIds));

        // setup MediaContentResourceProperties for the voice prompt-mediaresource
        MediaContentResourceProperties prop = new MediaContentResourceProperties();
        MimeType voice = null;
        MimeType video = null;
        try {
            voice = new MimeType("audio/pcmu");
            video = new MimeType("video/h263");
        } catch (MimeTypeParseException e) {
            fail("Could not create MimeTypes for mocking MediaProperties " + e);
        }
        prop.addCodec(voice);
        prop.setLanguage(language);
        prop.setVoiceVariant(voiceVariant);
        prop.setVideoVariant(null);
        prop.setType(PROMPT);

        jmockMediaResourcePromptVoice.stubs().method("getMediaContentResourceProperties").will(returnValue(prop));


        // Set up a video prompt resource, using the same media objects as voice.
        jmockMediaResourcePromptVideo.stubs().method("getMediaContent").with(eq("beep"), eq(null)).
                will(returnValue(mockMediaObjectPrompts0));
        jmockMediaResourcePromptVideo.stubs().method("getID").will(returnValue("promptVideo"));
        jmockMediaResourcePromptVideo.stubs().method("getMediaContentIDs").will(returnValue(promptIds));

        // setup MediaContentResourceProperties for the voice prompt-mediaresource
        MediaContentResourceProperties prop2 = new MediaContentResourceProperties();
        prop2.addCodec(voice);
        prop2.addCodec(video);
        prop2.setLanguage(language);
        prop2.setVoiceVariant(null);
        prop2.setVideoVariant(videoVariant);
        prop2.setType(PROMPT);

        jmockMediaResourcePromptVideo.stubs().method("getMediaContentResourceProperties").will(returnValue(prop2));

        fakeEarlySelection(voice);
    }

    /**
     * Mocks a SWA mediaresource
     */
    private void setupMediaSWA() {
        IMediaObject[] mockMediaObject = new IMediaObject[]{(IMediaObject) jmockMediaSWA0.proxy()};
        jmockMediaResourceSWA.stubs().method("getMediaContent").with(eq("swa"), eq(null)).
                will(returnValue(mockMediaObject));
        jmockMediaResourceSWA.stubs().method("getID").will(returnValue("swa0"));

        // setup mediaobjectids for the SWA-mediaresource
        ArrayList<String> ids = new ArrayList<String>();
        ids.add("swa0");
        jmockMediaResourceSWA.stubs().method("getMediaContentIDs").will(returnValue(ids));

        // setup MediaContentResourceProperties for the prompt-mediaresource
        MediaContentResourceProperties prop = new MediaContentResourceProperties();
        MimeType voice = null;
        try {
            voice = new MimeType("audio/pcmu");
        } catch (MimeTypeParseException e) {
            fail("Could not create MimeTypes for mocking MediaProperties " + e);
        }
        prop.addCodec(voice);

        prop.setLanguage(language);
        prop.setVoiceVariant(voiceVariant);
        prop.setVideoVariant(null);
        prop.setType(SWA);

        jmockMediaResourceSWA.stubs().method("getMediaContentResourceProperties").will(returnValue(prop));
    }

    /**
     * Mocks a fungreeting mediaresource
     */
    private void setupMediaFunGreeting() {
        MediaContentResourceProperties propertiesVoice = new MediaContentResourceProperties();
        MediaContentResourceProperties propertiesVideo = new MediaContentResourceProperties();
        MediaContentResourceProperties propertiesVideoAmr = new MediaContentResourceProperties();
        MimeType voiceCodec = null;
        MimeType videoCodec = null;
        MimeType amrCodec = null;
        try {
            voiceCodec = new MimeType("audio/pcmu");
            videoCodec = new MimeType("video/h263");
            amrCodec = new MimeType("audio/amr");
        } catch (MimeTypeParseException e) {
            fail("Could not create MimeTypes for mocking MediaProperties " + e);
        }
        propertiesVoice.addCodec(voiceCodec);
        propertiesVoice.setLanguage(language);
        propertiesVoice.setType(FUNGREETING);
        propertiesVoice.setVoiceVariant(voiceVariant);
        propertiesVoice.setVideoVariant(null);

        propertiesVideo.addCodec(voiceCodec);
        propertiesVideo.addCodec(videoCodec);
        propertiesVideo.setLanguage(language);
        propertiesVideo.setType(FUNGREETING);
        propertiesVideo.setVoiceVariant(null);
        propertiesVideo.setVideoVariant(videoVariant);

        propertiesVideoAmr.addCodec(amrCodec);
        propertiesVideoAmr.addCodec(videoCodec);
        propertiesVideoAmr.setLanguage(language);
        propertiesVideoAmr.setType(FUNGREETING);
        propertiesVideoAmr.setVoiceVariant(null);
        propertiesVideoAmr.setVideoVariant(videoVariant);

        IMediaObject[] mockMediaObject0 = new IMediaObject[]{(IMediaObject) jmockMediaFunGreeting0.proxy()};
        jmockMediaResourceFunGreetingVoice.stubs().method("getMediaContent").with(eq("fun"), eq(null)).
                will(returnValue(mockMediaObject0));
        jmockMediaResourceFunGreetingVoice.stubs().method("getID").will(returnValue("funVoice"));

        // setup mediaobjectids for the fungreeting-mediaresource
        ArrayList<String> idsVoice = new ArrayList<String>();
        idsVoice.add("fun0");
        jmockMediaResourceFunGreetingVoice.stubs().method("getMediaContentIDs").will(returnValue(idsVoice));
        jmockMediaResourceFunGreetingVoice.stubs().method("getMediaContentResourceProperties").will(returnValue(propertiesVoice));


        IMediaObject[] mockMediaObject2 = new IMediaObject[]{(IMediaObject) jmockMediaFunGreeting2.proxy()};
        jmockMediaResourceFunGreetingVideo.stubs().method("getMediaContent").with(eq("fun"), eq(null)).
                will(returnValue(mockMediaObject2));
        jmockMediaResourceFunGreetingVideo.stubs().method("getID").will(returnValue("funVideo"));

        // setup mediaobjectids for the fungreeting-mediaresource
        ArrayList<String> idsVideo = new ArrayList<String>();
        idsVideo.add("fun0");
        jmockMediaResourceFunGreetingVideo.stubs().method("getMediaContentIDs").will(returnValue(idsVideo));
        jmockMediaResourceFunGreetingVideo.stubs().method("getMediaContentResourceProperties").will(returnValue(propertiesVideo));

        //AMR video
        jmockMediaResourceFunGreetingVideoAmr.stubs().method("getMediaContent").with(eq("fun"), eq(null)).
                will(returnValue(mockMediaObject2));
        jmockMediaResourceFunGreetingVideoAmr.stubs().method("getID").will(returnValue("funVideoAmr"));

        // setup mediaobjectids for the fungreeting-mediaresource
        ArrayList<String> idsAmr = new ArrayList<String>();
        idsAmr.add("fun0");
        jmockMediaResourceFunGreetingVideoAmr.stubs().method("getMediaContentIDs").will(returnValue(idsAmr));
        jmockMediaResourceFunGreetingVideoAmr.stubs().method("getMediaContentResourceProperties").will(returnValue(propertiesVideoAmr));
    }

    private void fakeEarlySelection(MimeType... voice) {
        // setup the selected CallMediaTypes that is put in the session to fake the CallManagers early media selection
        MediaMimeTypes outboundMediaTypes = new MediaMimeTypes(voice);
        CallMediaTypes callMediaTypes = new CallMediaTypes(outboundMediaTypes, jmockMediaResourcePromptVoice.proxy());
        stubSession.setData("selectedcallmediatypes", callMediaTypes); // the SELECTEDCALL_MEDIATYPES_KEY constant in MediaManager
    }
    
    public void testIsAppendSupported() throws Exception {
    	
        PlatformAccess platformAccess1 = createPlatformAccess();
        platformAccess1.systemSetMediaResources(language, voiceVariant, null);
        
        jmockContentTypeMapper.expects(once()).method("mapToContentType").
        will(returnValue(new MimeType("audio/wav")));
        jmockMediaHandler.expects(once()).method("hasConcatenate").will(returnValue(true));
        jmockMediaHandlerFactory.expects(once()).method("getMediaHandler").
        will(returnValue(jmockMediaHandler.proxy()));
        assertTrue(platformAccess1.systemIsAppendSupported("prompt"));

                
        jmockContentTypeMapper.expects(once()).method("mapToContentType").
        will(returnValue(new MimeType("audio/wav")));
        jmockMediaHandler.expects(once()).method("hasConcatenate").will(returnValue(false));
        jmockMediaHandlerFactory.expects(once()).method("getMediaHandler").
        will(returnValue(jmockMediaHandler.proxy()));
        assertFalse(platformAccess1.systemIsAppendSupported("prompt"));

        
        jmockContentTypeMapper.expects(once()).method("mapToContentType").
        will(returnValue(new MimeType("audio/xyz")));
        jmockMediaHandlerFactory.expects(once()).method("getMediaHandler").
        will(returnValue(null));
        assertFalse(platformAccess1.systemIsAppendSupported("prompt"));
        
    }


    public static Test suite() {
        return new TestSuite(PlatformAccessMediaTest.class);
    }
}
