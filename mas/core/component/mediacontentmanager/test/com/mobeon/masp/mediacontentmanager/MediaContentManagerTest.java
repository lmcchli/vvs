/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

import com.mobeon.masp.mediacontentmanager.qualifier.NumberQualifier;
import com.mobeon.masp.mediaobject.FileMediaObject;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MultiThreadedTeztCase;
import com.mobeon.masp.execution_engine.platformaccess.util.MediaUtil;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.io.IOException;

/**
 * JUnit test for the {@link com.mobeon.masp.mediacontentmanager.MediaContentManager} class.
 *
 * @author Mats Egland
 */
public class MediaContentManagerTest extends MultiThreadedTeztCase {
    /**
     * Number of concurrent clints that operates on
     * a <code>MediaContentManager</code> in the
     * concurrent test.
     */
    private static final int NR_OF_CLIENTS = 100;
    /**
     * The tested object.
     */
    private MediaContentManager mediaContentManager;


    /**
     * The number of packages in application/mediacontentpackages
     */
    private static final int NR_OF_PACKAGES = 5;

    /**
     * Creates the tested MediaContentManager from
     * spring factory.
     *
     * @throws Exception If error occurrs.
     */
    protected void setUp() throws Exception {
        super.setUp();

        XmlBeanFactory bf = new XmlBeanFactory(new ClassPathResource(
                "mediacontentmanager_beans.xml", getClass()));
        mediaContentManager = (MediaContentManager)bf.getBean("IMediaContentManager");
        assertNotNull(mediaContentManager);

    }

    /**
     * Tests for the getMediaContentResource method.
     *
     * <pre>
     * 1. Null argument
     *  Conditions:
     *      A MediaContentManager is created with resources parsed
     *      from applications/mediacontentpackages which holds
     *      the following packages:
     *          - "en_audio_1"
     *          - "en_video_1"
     *  Actions:
     *      filterProperties=null
     *  Result:
     *      All packages is returned. I.e NR_OF_PACKAGES.
     *
     *
     * 2. Empty list is returned if no match
     *  Conditions:
     *      A MediaContentManager is created with resources parsed
     *      from applications/mediacontentpackages which holds
     *      the following packages:
     *          - "en_audio_1" (language=en, type=prompt, voice(male), audio/pcmu)
     *          - "sv_audio_1" (language=sv, type=prompt, voice(male), audio/pcmu)
     *          - "en_video_1" (language=en, type=prompt, video(blue, video/h263))
     *  Actions:
     *      filterProperties=(language=sv)
     *  Result:
     *      No match, i.e. empty list is returned.
     *
     * 3. All match if empty but non-null filterProperties
     *  Conditions:
     *      A MediaContentManager is created with resources parsed
     *      from applications/mediacontentpackages which holds
     *      the following packages:
     *          - "en_audio_1" (language=en, type=prompt, voice(male), audio/pcmu)
     *          - "sv_audio_1" (language=sv, type=prompt, voice(male), audio/pcmu)
     *          - "en_video_1" (language=en, type=prompt, video(blue, video/h263))
     *  Actions:
     *      filterProperties=empty
     *  Result:
     *      All resources is returned.
     *
     * 4. Filter on language only.
     *  Conditions:
     *      A MediaContentManager is created with resources parsed
     *      from applications/mediacontentpackages which holds
     *      the following packages:
     *          - "en_audio_1" (language=en, type=prompt, voice(male), audio/pcmu)
     *          - "sv_audio_1" (language=sv, type=prompt, voice(male), audio/pcmu)
     *          - "en_video_1" (language=en, type=prompt, video(blue, video/h263))
     *  Actions:
     *      1. filterProperties=(language=sv, null...)
     *      2. filterProperties=(language=xx, null...)
     *  Result:
     *      1. Resource with id "sv_audio_1" is returned.
     *      2. Empty list returned
     *
     * 5. Filter on variant .
     *  Conditions:
     *      A MediaContentManager is created with resources parsed
     *      from applications/mediacontentpackages which holds
     *      the following packages:
     *          - "en_audio_1" (language=en, type=prompt, voice(male), audio/pcmu, prio=1)
     *          - "en_audio_2" (language=en, type=prompt, voice(female), audio/pcmu, prio=2)
     *          - "sv_audio_1" (language=sv, type=prompt, voice(male), audio/pcmu, prio=2)
     *          - "en_video_1" (language=en, type=prompt, video(blue, video/h263))
     *  Actions:
     *      1. filterProperties=(voicevariant=male, otherwise null...)
     *      2. filterProperties=(voicevariant=neutral, otherwise null...)
     *      3. filterProperties=(voicevariant=male, videovariant=blue)
     *      4. filterProperties=(videovariant=blue, otherwise null)
     *  Result:
     *      1. Both voice resources is returned, "sv_audio_1" and "en_audio_1"
     *         and the first in list is "en_audio_1" as it has highest prio.
     *      2. Empty list returned
     *      3. All three resources is returned.
     *      4. en_video_1 is returned
     *
     * </pre>
     *
     */
    public void testGetMediaContentResource() throws MimeTypeParseException {
        // 1
        List<IMediaContentResource> resources =
                mediaContentManager.getMediaContentResource(null);
        assertEquals("All resources should be returned if null is passed",
                NR_OF_PACKAGES, resources.size());
        // 2
        MediaContentResourceProperties filterProperties =
                new MediaContentResourceProperties();
        filterProperties.setLanguage("xx");
        resources =
                mediaContentManager.getMediaContentResource(filterProperties);
        assertNotNull("Empty list should be returned if no match", resources);
        assertEquals("Empty list should be returned if no match",
                0, resources.size());
        // 3
        filterProperties =
                new MediaContentResourceProperties();
        resources =
                mediaContentManager.getMediaContentResource(filterProperties);
        assertEquals("All resources should be returned if empty filter",
                NR_OF_PACKAGES, resources.size());

        // 4
        filterProperties.setLanguage("sv");
        resources =
                mediaContentManager.getMediaContentResource(filterProperties);
        assertEquals("One resource should be returned",
                1, resources.size());
        assertEquals("Id of resource should be sv_audio_1",
                "sv_audio_1", resources.get(0).getID());
        filterProperties.setLanguage("xx");
        resources =
                mediaContentManager.getMediaContentResource(filterProperties);
        assertTrue(resources.size() == 0);

        // 5
        filterProperties.setLanguage("");
        filterProperties.setVoiceVariant("male");
        filterProperties.setVideoVariant(null);
        resources =
                mediaContentManager.getMediaContentResource(filterProperties);
        assertEquals("Two resource should be returned",
                2, resources.size());
        assertEquals("Id of first resource should be en_audio_1",
                "en_audio_1", resources.get(0).getID());
        assertEquals("Id of second resource should be sv_audio_1",
                "sv_audio_1", resources.get(1).getID());
        filterProperties.setVoiceVariant("neutral");
        resources =
                mediaContentManager.getMediaContentResource(filterProperties);
        assertEquals("No resource should be returned",
                0, resources.size());
        filterProperties.setVideoVariant("blue");
        filterProperties.setVoiceVariant("male");
        resources =
                mediaContentManager.getMediaContentResource(filterProperties);
        assertEquals("Three resources should be returned",
                3, resources.size());
        filterProperties.setVideoVariant("blue");
        filterProperties.setVoiceVariant(null);
        resources =
                mediaContentManager.getMediaContentResource(filterProperties);
        assertEquals("One resource should be returned",
                1, resources.size());
        assertEquals("Id of first resource should be en_video_1",
                "en_video_1", resources.get(0).getID());

        // Perfect match
        filterProperties.setLanguage("en");
        filterProperties.addCodec(
                new MimeType("audio", "pcmu"));
        filterProperties.setType("prompt");
        filterProperties.setVoiceVariant("male");
        resources =
                mediaContentManager.getMediaContentResource(filterProperties);
        assertEquals("There should be exactly one matching resource",
                1, resources.size());
        assertEquals("Id of resource should be en_audio_1",
                "en_audio_1", resources.get(0).getID());

        // voice variant differs
        filterProperties.setVoiceVariant("neutral");
        resources =
                mediaContentManager.getMediaContentResource(filterProperties);
        assertEquals("There should be no matching resource",
                0, resources.size());

        // Codecs
        filterProperties.setVideoVariant("blue");
        filterProperties.setVoiceVariant("male");
        filterProperties.addCodec(
                new MimeType("video", "h263"));
        resources =
                mediaContentManager.getMediaContentResource(filterProperties);
        assertEquals("There should be one matching resource",
                1, resources.size());

        filterProperties =
                new MediaContentResourceProperties();
        filterProperties.addCodec(new MimeType("video/h263"));
        resources =
                mediaContentManager.getMediaContentResource(filterProperties);
        assertEquals("There should be no matching resource",
                0, resources.size());

        filterProperties.addCodec(new MimeType("audio/pcmu"));

        resources =
                mediaContentManager.getMediaContentResource(filterProperties);
        assertEquals("There should be one matching resource",
                1, resources.size());
    }

    /**
     * Test that the cache returns the same object when requeste two times, but
     * different objects for different resources.
     *
     */
    public void testCache() throws Exception {
        //Initialize
        MediaContentResourceProperties properties = new MediaContentResourceProperties();
        properties.setLanguage("en");
        properties.setType("prompt");
        properties.addCodec(new MimeType("audio/pcmu"));
        List<IMediaContentResource> resources = mediaContentManager.getMediaContentResource(properties);
        assertTrue("No en audio resource found.", resources.size() > 0);
        IMediaContentResource enResource = resources.get(0);
        assertNotNull(enResource);

        properties.setLanguage("sv");
        properties.setType("prompt");
        properties.addCodec(new MimeType("audio/pcmu"));
        resources = mediaContentManager.getMediaContentResource(properties);
        assertTrue("No sv audio resource found.", resources.size() > 0);
        IMediaContentResource svResource = resources.get(0);
        assertNotNull(svResource);

        // Verify that the en and sv media objects are not the same.
        IMediaObject[] enMediaObject1 = enResource.getMediaContent("beep.wav", null);
        assertTrue(enMediaObject1.length == 1);
        IMediaObject[] svMediaObject1 = svResource.getMediaContent("beep.wav", null);
        assertTrue(svMediaObject1.length == 1);
        assertNotSame(enMediaObject1[0], svMediaObject1[0]);

        // Get the en and sv media object again.
        IMediaObject[] enMediaObject2 = enResource.getMediaContent("beep.wav", null);
        assertTrue(enMediaObject1.length == 1);
        IMediaObject[] svMediaObject2 = svResource.getMediaContent("beep.wav", null);
        assertTrue(svMediaObject1.length == 1);
        assertNotSame(enMediaObject2[0], svMediaObject2[0]);

        // Verify that the en media objects is the same.
        assertSame(enMediaObject1[0], enMediaObject2[0]);
        // Verify that the sv media objects is the same.
        assertSame(svMediaObject1[0], svMediaObject2[0]);
    }

    /**
     * Tests concurrent access of a <code>MediaContentManager</code>.
     *
     */
    public void testConcurrent() throws InterruptedException {

        final MediaContentManagerClient[] clients = new MediaContentManagerClient[NR_OF_CLIENTS];
        for (int i = 0; i < clients.length; i++) {
            clients[i] = new MediaContentManagerClient("MediaContentManagerClient "+i,
                    mediaContentManager);
            //clients[i].start();
        }
        runTestCaseRunnables(clients);
        // Let the clients run for two seconds
        new Timer().schedule(new TimerTask() {
            public void run() {
                for (int i = 0; i < clients.length; i++) {
                    clients[i].setDone();
                }
            }

        }, 2000);
        joinTestCaseRunnables(clients);
//        for (MediaContentManagerClient mediaContentManagerClient : clients) {
//            mediaContentManagerClient.join();
//        }
    }

    /**
     * Thread that works on a <code>MediaContentManager</code>.
     */
    private class MediaContentManagerClient extends TestCaseRunnable {
        private MediaContentManager mediaContentManager;
        private boolean done = false;

        public MediaContentManagerClient(String name, MediaContentManager mediaContentManager) {
            //super(name);
            this.mediaContentManager = mediaContentManager;
        }
        public void setDone() {
            done = true;
        }
        public void runTestCase() {
            while (!done) {
                MediaContentResourceProperties filterProperties =
                    new MediaContentResourceProperties();
                filterProperties.setLanguage("sv");

                List<IMediaContentResource> resources =
                        mediaContentManager.getMediaContentResource(filterProperties);
                assertEquals("Number of resources should be 1", 1, resources.size());

            }
        }
    }

    /**
     * This method tests typical client interactions.
     *
     * <pre>
     * 1. Fetch Prompt consisting of VVA_0009.
     * Condition:
     *  Resource en_audio_1 has a content with id 4000 with a message
     *  consisting of VVA_009.wav with condition:
     *  (numberOfNew > 0) && (numberOfNewUrgent == 10)
     * Action:
     *  Fetch resource en_audio_1, fetch the message from the resource.
     * Result:
     *  One MediaObject containing VVA_0009.wav is returned.
     *
     * 2. Fetch prompts with qualifiers, i.e. "2 new voice messages."
     * Condition:
     *  Resource en_audio_1 has a content with id 2103 with a message
     *  consisting of VVA_0300.wav and with condition:
     *  (numberOfNew > 1) && (numberOfNewUrgent == 0)
     *  Qualifier numberOfNew has value 2 and numberOfNewUrgent is 0.
     * Action:
     *  Fetch resource en_audio_1, fetch the message from the resource.
     * Result:
     *  Two media objects are returnded: "2.wav" and "VVA_0300.wav".
     *
     * </pre>
     */
    public void testTypicalScenario() throws MimeTypeParseException, MediaContentManagerException {
        // 1
        MediaContentResourceProperties filterProperties =
                new MediaContentResourceProperties();
        filterProperties.setLanguage("en");

        filterProperties.setType("prompt");
        filterProperties.setVoiceVariant("male");
        List<IMediaContentResource> resources =
                mediaContentManager.getMediaContentResource(filterProperties);
        assertEquals("There should be exactly one matching resource",
                1, resources.size());
        assertEquals("Id of resource should be en_audio_1",
                "en_audio_1", resources.get(0).getID());
        IMediaContentResource en_audio_1 = resources.get(0);
        NumberQualifier numberOfNew = new NumberQualifier(null, 1, null);
        NumberQualifier numberOfUrgent = new NumberQualifier(null, 10, null);
        IMediaObject[] mediaObjects = en_audio_1.getMediaContent(
                "4000",new IMediaQualifier[] {numberOfNew, numberOfUrgent});
        assertEquals("One mediaObject should be returned",
                1, mediaObjects.length);
        if (mediaObjects[0] instanceof FileMediaObject) {
            FileMediaObject mediaObject = (FileMediaObject) mediaObjects[0];
            assertEquals("The mediaobject should encapsulate VVA_0009.wav",
                    "VVA_0009.wav", mediaObject.getFile().getName());
        }

        // 2
        numberOfNew = new NumberQualifier("numberOfNew", 2, IMediaQualifier.Gender.None);
        numberOfUrgent = new NumberQualifier("numberOfNewUrgent", 0, IMediaQualifier.Gender.None);
        mediaObjects = en_audio_1.getMediaContent(
                "2103",new IMediaQualifier[] {numberOfNew, numberOfUrgent});
        assertEquals("Two mediaObjects should be returned",
                2, mediaObjects.length);
        if (mediaObjects[0] instanceof FileMediaObject) {
            FileMediaObject mediaObject = (FileMediaObject) mediaObjects[0];
            assertEquals("The mediaobject should encapsulate 2.wav",
                    "2.wav", mediaObject.getFile().getName());
        }
        if (mediaObjects[1] instanceof FileMediaObject) {
            FileMediaObject mediaObject = (FileMediaObject) mediaObjects[1];
            assertEquals("The mediaobject should encapsulate VVA_0300.wav",
                    "VVA_0300.wav", mediaObject.getFile().getName());
        }
    }

    public void testUtf8() throws MimeTypeParseException, MediaContentManagerException {
        // 1
        MediaContentResourceProperties filterProperties =
                new MediaContentResourceProperties();
        filterProperties.setLanguage("sv");

        filterProperties.setType("prompt");
        filterProperties.setVoiceVariant("male");
        List<IMediaContentResource> resources =
                mediaContentManager.getMediaContentResource(filterProperties);
        assertEquals("There should be exactly one matching resource",
                1, resources.size());
        assertEquals("Id of resource should be sv_audio_1",
                "sv_audio_1", resources.get(0).getID());
        IMediaContentResource sv_audio_1 = resources.get(0);
        NumberQualifier numberOfNew = new NumberQualifier(null, 1, null);
        NumberQualifier numberOfUrgent = new NumberQualifier(null, 10, null);
        IMediaObject[] mediaObjects = sv_audio_1.getMediaContent(
                "subject",new IMediaQualifier[] {});
        assertEquals("One mediaObject should be returned",
                1, mediaObjects.length);
        try {
            assertEquals("subject: ", "Röstmeddelande från nisse",
                    MediaUtil.convertMediaObjectToString(mediaObjects[0]));
        } catch (IOException e) {
            fail("IOException have been thrown" + e);
        }

    }


}
