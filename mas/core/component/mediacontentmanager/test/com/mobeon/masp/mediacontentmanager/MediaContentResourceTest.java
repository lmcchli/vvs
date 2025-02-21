/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

import com.mobeon.masp.mediacontentmanager.qualifier.*;
import com.mobeon.masp.mediaobject.*;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import jakarta.activation.MimeType;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Unit tests for class {@link MediaContentResource}.
 *
 * @author Mats Egland
 */
public class MediaContentResourceTest extends MultiThreadedTeztCase {

    /**
     * The MediaContentPackage.xml file to parse.
     */
    private static final String FILE_NAME =
            "applications/mediacontentpackages/en_audio_1/beep.wav";
    /**
     * The size of beep.wav
     */
    private static final int BEEP_FILE_SIZE = 1420;
    /**
     * The size of test.wav
     */
    private static final int TEST_FILE_SIZE = 38568;

    /**
     * Tested resource with the following properties:
     * <p/>
     * id               "sv_audio_1"
     * Language         "sv"
     * type             "prompt"
     * priority         "1"
     * voice variant    "male"
     * codecs           {"audio/pcmu"}
     */
    private IMediaContentResource sv_audio_1;
    /**
     * Tested resource with the following properties:
     * <p/>
     * id               "en_audio_1"
     * Language         "en"
     * type             "prompt"
     * priority         "2"
     * voice variant    "male"
     * codecs           {"audio/pcmu"}
     */
    private IMediaContentResource en_audio_1;
    /**
     * Tested resource with the following properties:
     * <p/>
     * id               "en_video_1"
     * Language         "en"
     * type             "prompt"
     * priority         "2"
     * video variant    "blue"
     * codecs           {"video/h263", "audio/pcmu"}
     */
    private IMediaContentResource en_video_1;

    /**
     * Tested resource with the following properties:
     * <p/>
     * id               "de_audio_fungreetings"
     * Language         "en"
     * type             "fun"
     * priority         "1"
     * voice variant    "male"
     * codecs           {"audio/pcmu"}
     */
    private IMediaContentResource de_audio_fungreetings;

    private IMediaQualifierFactory mediaQualifierFactory;
    private MediaObjectFactory mediaObjectFactory;

    /**
     * Creates a MediaContentManager that reads a language resource packages
     * under the application folder.
     */
    protected void setUp() throws Exception {
        mediaQualifierFactory = new MediaQualifierFactory();
        mediaObjectFactory = new MediaObjectFactory(8*1000);
        XmlBeanFactory bf = new XmlBeanFactory(new ClassPathResource(
                "mediacontentmanager_beans.xml", getClass()));
        IMediaContentManager mediaContentManager =
                (IMediaContentManager) bf.getBean("IMediaContentManager");
        assertNotNull(mediaContentManager);

        MediaContentResourceProperties resourceProperties =
                new MediaContentResourceProperties();

        // get sv_audio_1
        resourceProperties.setLanguage("sv");
        resourceProperties.addCodec(new MimeType("audio/pcmu"));
        List<IMediaContentResource> resources =
                mediaContentManager.getMediaContentResource(resourceProperties);

        assertEquals("There should be only one matching resource, there are:"
                + resources.size(),
                1, resources.size());
        sv_audio_1 = resources.get(0);
        assertEquals("Resource en_audio_1 should be returned",
                "sv_audio_1", sv_audio_1.getID());

        // get en_audio_1
        resourceProperties.setLanguage("en");
        resources =
                mediaContentManager.getMediaContentResource(resourceProperties);
        assertEquals("There should be two matching resource, there are:"
                + resources.size(),
                2, resources.size());
        en_audio_1 = resources.get(0);
        assertEquals("Resource en_audio_1 should be returned",
                "en_audio_1", en_audio_1.getID());

        // get en_video_1
        resourceProperties.setLanguage("en");
        resourceProperties.addCodec(new MimeType("video/h263"));
        assertEquals("There should be two codecs in properties", 2,
                resourceProperties.getMediaCodecs().size());
        resources =
                mediaContentManager.getMediaContentResource(resourceProperties);
        assertEquals("There should be only one matching resource, there are:"
                + resources.size(),
                1, resources.size());
        en_video_1 = resources.get(0);
        assertEquals("Resource en_audio_1 should be returned",
                "en_video_1", en_video_1.getID());

        //get de_audio_fungreetings
        resourceProperties = new MediaContentResourceProperties();
        resourceProperties.setLanguage("de");
        resourceProperties.setType("fun");
        resourceProperties.addCodec(new MimeType("audio/pcmu"));

        resources =
                mediaContentManager.getMediaContentResource(resourceProperties);
        assertEquals("There should be only one matching resource, there are:"
                + resources.size(),
                1, resources.size());
        de_audio_fungreetings = resources.get(0);
        assertEquals("Resource de_audio_fungreetings should be returned",
                "de_audio_fungreetings", de_audio_fungreetings.getID());
    }
    /**
     * Tests for the method
     * {@link MediaContentResource#getMediaContent(String, IMediaQualifier[])} with
     * content loaded from XML.
     * <p/>
     * <pre>
     * 1. IllegalArgument
     *  Condition:
     *      The resource en_audio_1 is created.
     *  Action:
     *      1) Id = null
     *      2) qualifers passed is null but content requires one qualifiers
     *      3) number of qualifers passed matches qualifers in content
     *         but the types is incorrect
     *      4) number of qualifers passed is 2, but content has 1.
     *  Result:
     *      1-4) IllegalArgumentException
     * <p/>
     * 2. Fetch content with id "beep.wav" and no qualifiers.
     *  Condition:
     *      The resource en_audio_1 is created which holds
     *      content with id "beep.wav".
     *  Action:
     *      id = "beep.wav"
     *  Result:
     *      IMediaObject that holds file beep.wav is returned.
     * <p/>
     * 3. Fetch content with id "2100" with qualifiers
     *  Condition:
     *      The resource en_audio_1 is created which holds
     *      content with id "2100" which has the following messages:
     *      <instance cond="((numberOfNewMessages == 1))&#xD;&#xA;">
     *          <element type="mediafile" reference="VVA_0276.wav" />
     *      </instance>
     *     <instance cond="((numberOfNewMessages &gt; 1))&#xD;&#xA;">
     *          <element type="mediafile" reference="VVA_0277.wav" />
     *      </instance>
     *  Action:
     *      1) numberOfNewMessages = 1
     *      2) numberOfNewMessages = 2
     *  Result:
     *      1) MediaObject of VVA_0276.wav is returned
     *      2) MediaObject of VVA_0277.wav is returned
     * <p/>
     * 4. todo
     * <p/>
     * 5. todo
     * <p/>
     * 6. todo
     * <p/>
     * 7. todo
     * <p/>
     * 8. Fetch content with an id that is not defined in the media content
     * package.
     *  Condition:
     *      The resource en_audio_1 is created and does not host a content with
     *      id "finnsinteID".
     *  Action:
     *      Get content with id "finnsinteID".
     *  Result:
     *      IllegalArgumentException is thrown.
     * <p/>
     * 9. Fetch content with id "subject" with qualifiers "forward", "urgent",
     * "confidential", "messageType" and "sender".
     *  Condition:
     *      The resource en_audio_1 is created and holds a content with id
     *      "subject". The content is of type "returnAll" which means that all
     *      instances that has a condition interpreted as true will be returned.
     *  Action:
     *      1)  forward = false
     *          urgent = false
     *          confidential = true
     *          messageType = voice
     *          sender = Kalle Anka
     *      2)  forward = true
     *          urgent = true
     *          confidential = false
     *          messageType = email
     *          sender = Kalle Anka
     *
     *  Result:
     *      1)  The returned MediaObjects are:
     *          "Confidential Voice message from Kalle Anka"
     *      2)  The returned MediaObjects are:
     *          "Forwarded Urgent Email from Kalle Anka"
     * <p/>
     * 10. Test with String qualifier in playlist
     *  Condition:
     *      The resource en_audio_1 is created and holds a content with id
     *      "2200". The message content contains a String qualifier
     *      phoneNumber and the message elements VVA_0405.wav and VVA_0426.wav
     *  Action:
     *      Fetch message id "2200", phoneNumber is "070123"
     *  Result:
     *      8 MediaObjects will be returned:
     *      0.wav, 7.wav, 0.wav, 1.wav, 2.wav, 3.wav, VVA_0405.wav, VVA_0426.wav
     * <p/>
     * 11. Test with Time12 qualifier in playlist
     *  Condition:
     *      The resource en_audio_1 is created and holds a content with id
     *      "timequalifiertest". The message content contains a Time12
     *      qualifier time12Test.
     *  Action:
     *      Fetch message id "timequalifiertest", time12Test is "12:22:34"
     *  Result:
     *      4 MediaObjects will be returned:
     *      12.wav, 20.wav, 2.wav, pm.wav
     *
     * <p/>
     * todo more tests
     * </pre>
     */
    public void testGetMediaContent() throws MediaQualifierException, MediaContentManagerException {
        // 1
        IMediaQualifier mQ = mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.String,
                "numberOfYears",
                "2", null);

        try {
            en_audio_1.getMediaContent(null, null);
            fail("IllegalArgumentException should be thrown");
        } catch (MediaContentManagerException e) {
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException e) {/*ok*/}
        try {
            en_audio_1.getMediaContent("beepwithqualifiers", null);
            fail("IllegalArgumentException should be thrown");
        } catch (MediaContentManagerException e) {
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException e) {/*ok*/}
        try {
            en_audio_1.getMediaContent("beepwithqualifiers", new IMediaQualifier[] {mQ});
            fail("IllegalArgumentException should be thrown");
        } catch (MediaContentManagerException e) {
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException e) {/*ok*/}
        mQ = mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                "numberOfYears",
                "2", null);
        IMediaQualifier mQ2 = mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                "numberOfYears",
                "2", null);
        try {
            en_audio_1.getMediaContent("beepwithqualifiers", new IMediaQualifier[] {mQ, mQ2});
            fail("IllegalArgumentException should be thrown");
        } catch (MediaContentManagerException e) {
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException e) {/*ok*/}

        // 2
        try {
            IMediaObject[] moArray = en_audio_1.getMediaContent("beep.wav", null);
            IMediaObject beepMO = moArray[0];
            assertEquals("Size of media object should be " + BEEP_FILE_SIZE,
                    BEEP_FILE_SIZE, beepMO.getSize());
            int readByte;
            FileInputStream fis = new FileInputStream(FILE_NAME);
            InputStream is = beepMO.getInputStream();
            while ((readByte = fis.read()) != -1) {
                assertEquals("Byte mismatch",
                        readByte, is.read());
            }

        } catch (MediaContentManagerException e) {
            fail(e.getMessage());
        } catch (FileNotFoundException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }

        // 3 test with NumberQualifer
        NumberQualifier nQ = new NumberQualifier("numberOfNewMessages", 1, NumberQualifier.Gender.Male);
        try {
            IMediaObject[] moArray = en_audio_1.getMediaContent(
                    "2100", new IMediaQualifier[] {nQ});
            assertEquals("Only one mediaobject should be returned",
                    1, moArray.length);
            if (moArray[0] instanceof FileMediaObject) {
                FileMediaObject fileMO = (FileMediaObject) moArray[0];
                assertEquals("File should be VVA_0276.wav",
                        "VVA_0276.wav", fileMO.getFile().getName());
            }
            assertEquals("The file returned should be VVA_0276.wav",
                    BEEP_FILE_SIZE, moArray[0].getSize());
        } catch (MediaContentManagerException e) {
            fail(e.getMessage());
        }

        mQ = mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                "numberOfNewMessages",
                "2",
                null);
        try {
            IMediaObject[] moArray = en_audio_1.getMediaContent(
                    "2100", new IMediaQualifier[] {mQ});
            assertEquals("Only one mediaobject should be returned",
                    1, moArray.length);
            if (moArray[0] instanceof FileMediaObject) {
                FileMediaObject fileMO = (FileMediaObject) moArray[0];
                assertEquals("File should be VVA_0277.wav",
                        "VVA_0277.wav", fileMO.getFile().getName());
            }
            assertEquals("The file returned should be VVA_0276.wav",
                    BEEP_FILE_SIZE, moArray[0].getSize());
        } catch (MediaContentManagerException e) {
            fail(e.getMessage());
        }

        // 4 test with DateDMQualifer

        IMediaQualifier datum = mediaQualifierFactory.create(IMediaQualifier.QualiferType.DateDM,
                "datum", "2000-01-10", null);
        IMediaQualifier datum2 = mediaQualifierFactory.create(IMediaQualifier.QualiferType.DateDM,
                "datum2", "2000-01-02", null);
        try {
            IMediaObject[] moArray = en_audio_1.getMediaContent(
                    "3000", new IMediaQualifier[] {datum, datum2});
            assertEquals("Only one mediaobject should be returned",
                    1, moArray.length);
            if (moArray[0] instanceof FileMediaObject) {
                FileMediaObject fileMO = (FileMediaObject) moArray[0];
                assertEquals("File should be VVA_0200.wav",
                        "VVA_0200.wav", fileMO.getFile().getName());
            }
        } catch (MediaContentManagerException e) {
            fail(e.getMessage());
        }
        datum = mediaQualifierFactory.create(IMediaQualifier.QualiferType.DateDM,
                "datum", "2000-01-01", null);
        try {
            IMediaObject[] moArray = en_audio_1.getMediaContent(
                    "3000", new IMediaQualifier[] {datum, datum2});
            assertEquals("Only one mediaobject should be returned",
                    1, moArray.length);
            if (moArray[0] instanceof FileMediaObject) {
                FileMediaObject fileMO = (FileMediaObject) moArray[0];
                assertEquals("File should be VVA_0201.wav",
                        "VVA_0201.wav", fileMO.getFile().getName());
            }
        } catch (MediaContentManagerException e) {
            fail(e.getMessage());
        }

        // 5 Test with number qualifier in playlist
        nQ = (NumberQualifier)mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                null, "1", null);
        IMediaObject[] moArray = en_audio_1.getMediaContent(
                "2000", new IMediaQualifier[] {nQ});
        assertEquals("Three mediaobject should be returned",
                3, moArray.length);
        if (moArray[0] instanceof FileMediaObject) {
            FileMediaObject fileMO = (FileMediaObject) moArray[0];
            assertEquals("File should be VVA_0009.wav",
                    "VVA_0009.wav", fileMO.getFile().getName());
        }
        if (moArray[1] instanceof FileMediaObject) {
            FileMediaObject fileMO = (FileMediaObject) moArray[1];
            assertEquals("File should be 1.wav",
                    "1.wav", fileMO.getFile().getName());
        }
        if (moArray[2] instanceof FileMediaObject) {
            FileMediaObject fileMO = (FileMediaObject) moArray[2];
            assertEquals("File should be VVA_0010.wav",
                    "VVA_0010.wav", fileMO.getFile().getName());
        }

        // 6 Fetch content with id 5000, where a string contains 'numberOfNew' which
        // is a qualifierName. MediaObject holding VVA_0307.wav should be returned.
        // "(numberOfNew > 0) && (numberOfNewUrgent == 1000) && (name == 'hello')"

        nQ = new NumberQualifier(null, 1, null);
        NumberQualifier nQ1 = (NumberQualifier)mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                null, "1000", null);
        StringQualifier sQ = (StringQualifier)mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.String,
                null, "hello", null);
        moArray = en_audio_1.getMediaContent(
                "5000", new IMediaQualifier[] {nQ, nQ1, sQ});

        assertEquals("Only one mediaobject should be returned." +
                "The possible error is that the insertion of qualifier has corrupted a string in" +
                " the condition.",
                1, moArray.length);
        if (moArray[0] instanceof FileMediaObject) {
            FileMediaObject fileMO = (FileMediaObject) moArray[0];
            assertEquals("File should be VVA_0307.wav",
                    "VVA_0307.wav", fileMO.getFile().getName());
        }

        // 7 Test with DateDM qualifier in playlist
        datum = mediaQualifierFactory.create(IMediaQualifier.QualiferType.DateDM,
                "dateTest", "2006-02-01", null);
        moArray = en_audio_1.getMediaContent(
                "datequalifiertest", new IMediaQualifier[] {datum});
        assertEquals("Two mediaobject should be returned",
                2, moArray.length);
        if (moArray[0] instanceof FileMediaObject) {
            FileMediaObject fileMO = (FileMediaObject) moArray[0];
            assertEquals("File should be feb.wav",
                    "feb.wav", fileMO.getFile().getName());
        }
        if (moArray[1] instanceof FileMediaObject) {
            FileMediaObject fileMO = (FileMediaObject) moArray[1];
            assertEquals("File should be dom1.wav",
                    "dom1.wav", fileMO.getFile().getName());
        }

        // 8. test getting a content that do not exist
        try {
            en_audio_1.getMediaContent(
                    "finnsinteID", new IMediaQualifier[] {});
            fail("IllegalArgumentException should be thrown");
        } catch (MediaContentManagerException e) {
            fail("IllegalArgumentException should be thrown");
        } catch (IllegalArgumentException e) {/*ok*/}

        // 9.1 test getting a text content.
        IMediaQualifier forwardQ = mediaQualifierFactory.create(IMediaQualifier.QualiferType.String,
                "forward", "false", IMediaQualifier.Gender.None);
        IMediaQualifier urgentQ = mediaQualifierFactory.create(IMediaQualifier.QualiferType.String,
                "urgent", "false", IMediaQualifier.Gender.None);
        IMediaQualifier confQ = mediaQualifierFactory.create(IMediaQualifier.QualiferType.String,
                "confidential", "true", IMediaQualifier.Gender.None);
        IMediaQualifier messageTypeQ = mediaQualifierFactory.create(IMediaQualifier.QualiferType.String,
                "messageType", "voice", IMediaQualifier.Gender.None);
        IMediaObject senderMediaObject =
                null;
        try {
            senderMediaObject = mediaObjectFactory.create("Kalle Anka", new MediaProperties());
        } catch (MediaObjectException e) {
            fail("Text mediaobejct could not be created.");
        }
        IMediaQualifier senderQ =
                mediaQualifierFactory.create("sender", senderMediaObject, IMediaQualifier.Gender.None);
        IMediaQualifier[] subjectQualifiers =
                new IMediaQualifier[] {forwardQ, urgentQ, confQ, messageTypeQ, senderQ};
        moArray = en_audio_1.getMediaContent(
                "subject", subjectQualifiers);
        assertEquals("Four MediaObjects should be returned.",
                4, moArray.length);
        // 9.2
        forwardQ = mediaQualifierFactory.create(IMediaQualifier.QualiferType.String,
                "forward", "true", IMediaQualifier.Gender.None);
        urgentQ = mediaQualifierFactory.create(IMediaQualifier.QualiferType.String,
                "urgent", "true", IMediaQualifier.Gender.None);
        confQ = mediaQualifierFactory.create(IMediaQualifier.QualiferType.String,
                "confidential", "false", IMediaQualifier.Gender.None);
        messageTypeQ = mediaQualifierFactory.create(IMediaQualifier.QualiferType.String,
                "messageType", "email", IMediaQualifier.Gender.None);
        subjectQualifiers =
                new IMediaQualifier[] {forwardQ, urgentQ, confQ, messageTypeQ, senderQ};
        moArray = en_audio_1.getMediaContent(
                "subject", subjectQualifiers);
        assertEquals("Five MediaObjects should be returned.",
                5, moArray.length);

        // 10, test with String qualifier in playlist
        IMediaQualifier phoneNumberQ =
                mediaQualifierFactory.create(IMediaQualifier.QualiferType.String,
                "phoneNumber", "070123", IMediaQualifier.Gender.None);
        moArray = en_audio_1.getMediaContent(
                "2200", new IMediaQualifier[] {phoneNumberQ});
        assertEquals("8 mediaobjects should be returned.",
                8, moArray.length);
        if (moArray[0] instanceof FileMediaObject) {
            FileMediaObject fileMO = (FileMediaObject) moArray[0];
            assertEquals("File should be 0.wav",
                    "0.wav", fileMO.getFile().getName());
        }
        if (moArray[1] instanceof FileMediaObject) {
            FileMediaObject fileMO = (FileMediaObject) moArray[1];
            assertEquals("File should be 7.wav",
                    "7.wav", fileMO.getFile().getName());
        }
        if (moArray[2] instanceof FileMediaObject) {
            FileMediaObject fileMO = (FileMediaObject) moArray[2];
            assertEquals("File should be 0.wav",
                    "0.wav", fileMO.getFile().getName());
        }
        if (moArray[3] instanceof FileMediaObject) {
            FileMediaObject fileMO = (FileMediaObject) moArray[3];
            assertEquals("File should be 1.wav",
                    "1.wav", fileMO.getFile().getName());
        }
        if (moArray[4] instanceof FileMediaObject) {
            FileMediaObject fileMO = (FileMediaObject) moArray[4];
            assertEquals("File should be 2.wav",
                    "2.wav", fileMO.getFile().getName());
        }
        if (moArray[5] instanceof FileMediaObject) {
            FileMediaObject fileMO = (FileMediaObject) moArray[5];
            assertEquals("File should be 3.wav",
                    "3.wav", fileMO.getFile().getName());
        }
        if (moArray[6] instanceof FileMediaObject) {
            FileMediaObject fileMO = (FileMediaObject) moArray[6];
            assertEquals("File should be VVA_0405.wav",
                    "VVA_0405.wav", fileMO.getFile().getName());
        }
        if (moArray[7] instanceof FileMediaObject) {
            FileMediaObject fileMO = (FileMediaObject) moArray[7];
            assertEquals("File should be VVA_0426.wav",
                    "VVA_0426.wav", fileMO.getFile().getName());
        }

        // 11 Test with Time12 qualifier in playlist
        IMediaQualifier time12q =
                mediaQualifierFactory.create(IMediaQualifier.QualiferType.Time12,
                "time12Test", "12:22:34", IMediaQualifier.Gender.None);
       moArray = en_audio_1.getMediaContent(
                "timequalifiertest", new IMediaQualifier[] {time12q});
        assertEquals("Four mediaobject should be returned",
                4, moArray.length);
        if (moArray[0] instanceof FileMediaObject) {
            FileMediaObject fileMO = (FileMediaObject) moArray[0];
            assertEquals("File should be 12.wav",
                    "12.wav", fileMO.getFile().getName());
        }
        if (moArray[1] instanceof FileMediaObject) {
            FileMediaObject fileMO = (FileMediaObject) moArray[1];
            assertEquals("File should be 20.wav",
                    "20.wav", fileMO.getFile().getName());
        }
        if (moArray[2] instanceof FileMediaObject) {
            FileMediaObject fileMO = (FileMediaObject) moArray[2];
            assertEquals("File should be 2.wav",
                    "2.wav", fileMO.getFile().getName());
        }
        if (moArray[3] instanceof FileMediaObject) {
            FileMediaObject fileMO = (FileMediaObject) moArray[3];
            assertEquals("File should be pm.wav",
                    "pm.wav", fileMO.getFile().getName());
        }
    }

    /**
     * Tests that all condition types, i.e. Number, DateDM etc. works. Does that by
     * fetching media from content with id 6000 (en_audio_1).
     *
     */
    public void testAllConditionTypes() throws MediaQualifierException, MediaContentManagerException {
        CompleteDateQualifier cdQ = (CompleteDateQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.CompleteDate,
                null, "2000-01-01 12:12:12 +0100", null);
        DateDMQualifier ddmQ = (DateDMQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.DateDM,
                null, "2000-01-01", null);
        IMediaObjectQualifier moQ = (IMediaObjectQualifier) mediaQualifierFactory.create(
                "mediaObject", null, null);
        NumberQualifier nQ = (NumberQualifier)mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                null, "1000", null);
        StringQualifier sQ = (StringQualifier)mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.String,
                null, "hello", null);
        Time12Qualifier t12Q = (Time12Qualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Time12,
                null, "12:12:12", null);
        Time12Qualifier t24Q = (Time24Qualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Time24,
                null, "12:12:12", null);
        WeekdayQualifier wdQ = (WeekdayQualifier) mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.WeekDay,
                null, "2000-01-01", null);


        IMediaObject[] moArray = en_audio_1.getMediaContent(
                "6000", new IMediaQualifier[] {cdQ, ddmQ,moQ, nQ, sQ, t12Q, t24Q, wdQ});

    }

    /**
     * Test for the method
     * {@link MediaContentResource#getMediaContentIDs(IMediaQualifier[])}.
     * <p/>
     * <p/>
     * <pre>
     * 1. Null argument should return all IDs.
     *  Condition:
     *      The resource en_audio_1 (see above for properties of the resource)
     *      is created and holds the content of file
     *      application/mediacontentpackages/en_audio_1/MediaContent.xml
     *  Action:
     *  Result:
     *      The following ids are part of list: 1, 254, 2204 amd beep
     * <p/>
     * 2. The returned list if IDs is a copy of the internal list.
     *  Condition:
     *  Action:
     *      Alter the returned list.
     *  Result:
     *      The list in the resource is unmodified.
     * 3. Pass one number qualifer with value 2.
     *  Condition:
     *      Resource en_audio_1 has 8 content with one number qualifier
     *      which also has a message that meets the qualifer.
     *  Action:
     *      getMediaContentIds(NumberQualifier("numberOfYears", 2);
     *  Result:
     *      Ids: 2001, 2002, 2102, beepwithqualifiers, 2000, 2100, 2102,
     *      numberqualifiertest
     * 4. Match id 3000 only
     *  Condition:
     *      Resource en_audio_1 has a content with id 3000 wich requires
     *      two qualifiers of type DateDM
     *  Action:
     *      call getMediaContentIds with the following qualifiers
     *       1) DateDM("2000-01-01");
     *       2) DateDM("2000-01-02");
     *  Result:
     *      Ids: 3000 is returned only.
     * 5. No match
     * Condition:
     *      Resource en_audio_1 has no content with three DateDM qualifiers
     *  Action:
     *      call getMediaContentIds with the following qualifiers
     *       1) DateDM("2000-01-01");
     *       2) DateDM("2000-01-02");
     *       3) DateDM("2000-01-03");
     *  Result:
     *      Nothing.
     * 6. Match id 4000 only
     *  Condition:
     *      Resource en_audio_1 has a content with id 4000 wich requires
     *      two qualifiers of type Number. The condition is true if
     *      qualifer1 < 0 AND qualifier2 == 1000
     *  Action:
     *      call getMediaContentIds with the following qualifiers
     *       1) NumberQualifer(-1);
     *       2) NumberQualifer(1000);
     *  Result:
     *      Ids: 4000 is returned only.
     *
     * 7. Test fun greetings order.
     *  Condition:
     *      The resource en_audio_fungreeting contains 3 ids.
     *  Action:
     *      Call method and check the order of the elements in the list.
     *  Result:
     *      The first element is 1, second 2 and third 3. * <p/>
     * </pre>
     */
    public void testGetMediaContentIDs() throws MediaContentManagerException, MediaQualifierException {
        // 1
        List<String> ids = en_audio_1.getMediaContentIDs(null);
        assertNotNull("Ids is null", ids);
        assertTrue("Number of ids should be bigger than 0",
                ids.size() > 0);
        assertTrue("Content with id 1 should be part of resource",
                ids.contains("1"));
        assertTrue("Content with id 254 should be part of resource",
                ids.contains("254"));
        assertTrue("Content with id 2204 should be part of resource",
                ids.contains("2204"));
        assertTrue("Content with id beep.wav should be part of resource",
                ids.contains("beep.wav"));

        // 2
        ids.clear();
        List<String> ids2 = en_audio_1.getMediaContentIDs(null);
        assertNotNull("Ids is null", ids);
        assertTrue("Number of ids should be bigger than 0",
                ids2.size() > 0);
        assertNotSame("List of ids should be a copy",
                ids, ids2);

        // 3
        NumberQualifier nQ = (NumberQualifier)mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                null, "2", null);
        ids = en_audio_1.getMediaContentIDs(new IMediaQualifier[] {nQ});
        assertNotNull("Ids is null", ids);
        assertEquals("Number of ids should be 8",
                8, ids.size());
        assertTrue("beepwithqualifers should be part of ids",
                ids.contains("beepwithqualifiers"));

        // 4
        DateDMQualifier dQ1 = (DateDMQualifier)mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.DateDM,
                null, "2000-01-01", null);
        DateDMQualifier dQ2 = (DateDMQualifier)mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.DateDM,
                null, "2000-01-02", null);
        ids = en_audio_1.getMediaContentIDs(new IMediaQualifier[] {dQ1, dQ2});
        assertNotNull("Ids is null", ids);
        assertEquals("Number of ids should be 1",
                1, ids.size());
        assertEquals("id 3000 should be returned",
                "3000", ids.get(0));

        // 5
        DateDMQualifier dQ3 = (DateDMQualifier)mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.DateDM,
                null, "2000-01-03", null);
        ids = en_audio_1.getMediaContentIDs(new IMediaQualifier[] {dQ1, dQ2, dQ3});
        assertNotNull("Ids is null", ids);
        assertEquals("Number of ids should be 0",
                0, ids.size());

        // 6
        NumberQualifier nQ1 = (NumberQualifier)mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                null, "-1", null);
        NumberQualifier nQ2 = (NumberQualifier)mediaQualifierFactory.create(
                IMediaQualifier.QualiferType.Number,
                null, "1000", null);
        ids = en_audio_1.getMediaContentIDs(new IMediaQualifier[] {nQ1, nQ2});
        assertNotNull("Ids is null", ids);
        assertEquals("Number of ids should be 1",
                1, ids.size());
        assertEquals("id 4000 should be returned",
                "4000", ids.get(0));

        // 7
        ids = de_audio_fungreetings.getMediaContentIDs(null);
        assertNotNull("Fun greetings is null", ids);
        assertEquals("1 should be first", "1", ids.get(0));
        assertEquals("2 should be second", "2", ids.get(1));
        assertEquals("3 should be third", "3", ids.get(2));
    }

    /**
     * Test for the method
     * {@link MediaContentResource#getAllMediaContentIDs()}.
     * <p/>
     * <pre>
     * 1. All IDs.
     *  Condition:
     *      The resource en_audio_1 (see above for properties of the resource)
     *      is created and holds the content of file
     *      application/mediacontentpackages/en_audio_1/MediaContent.xml
     *  Action:
     *      call method.
     *  Result:
     *      The following ids are part of list: 1, 254, 2204 amd beep
     * <p/>
     * 2. The returned list if IDs is a copy of the internal list.
     *  Condition:
     *  Action:
     *      Alter the returned list.
     *  Result:
     *      The list in the resource is unmodified.
     * <p/>
     * 3. Check that the returned list is sorted correctly.
     *  Condition:
     *      The resource en_audio_fungreeting contains 3 ids.
     *  Action:
     *      Call method and check the order of the elements in the list.
     *  Result:
     *      The first element is 1, second 2 and third 3.
     * <p/>
     * </pre>
     */
    public void testGetAllMediaContentIDs() throws Exception {
        // 1
        List<String> ids = en_audio_1.getAllMediaContentIDs();
        assertNotNull("Ids is null", ids);
        assertTrue("Number of ids should be bigger than 0",
                ids.size() > 0);
        assertTrue("Content with id 1 should be part of resource",
                ids.contains("1"));
        assertTrue("Content with id 254 should be part of resource",
                ids.contains("254"));
        assertTrue("Content with id 2204 should be part of resource",
                ids.contains("2204"));
        assertTrue("Content with id beep.wav should be part of resource",
                ids.contains("beep.wav"));

        // 2
        ids.clear();
        List<String> ids2 = en_audio_1.getAllMediaContentIDs();
        assertNotNull("Ids is null", ids);
        assertTrue("Number of ids should be bigger than 0",
                ids2.size() > 0);
        assertNotSame("List of ids should be a copy",
                ids, ids2);

        // 3
        ids = de_audio_fungreetings.getMediaContentIDs(null);
        assertNotNull("Fun greetings is null", ids);
        assertEquals("1 should be first", "1", ids.get(0));
        assertEquals("2 should be second", "2", ids.get(1));
        assertEquals("3 should be third", "3", ids.get(2));
    }

    /**
     * Tests that the content of a MediaContentResource matches
     * the content of the package XMLs.
     * <p/>
     * <pre>
     * 1. Assert id of resources is correct.
     *  Condition:
     *      The following member resources is created
     *      (see properties above)
     *      en_audio_1
     *      sv_audio_1
     *      en_video_1
     *  Action:
     *      getId() on all three resources.
     *  Result:
     *      Id matches.
     * <p/>
     * 2. Assert priority of resources is correct.
     *  Condition:
     *      The member resources en_audio_1, sv_audio_1 and en_video_1 is created
     *      with properties as descibed above.
     *  Action:
     *      getPriority() on all three resources.
     *  Result:
     *      2,1,2
     * <p/>
     * 3. Assert language of resources is correct.
     *  Condition:
     *      The member resources en_audio_1, sv_audio_1 and en_video_1 is created
     *      with properties as descibed above.
     *  Action:
     *      getLanguage on all tree resources.
     *  Result:
     *      en, sv, en
     *
     * 4. Assert type of resources is correct.
     *  Condition:
     *      The member resources en_audio_1, sv_audio_1 and en_video_1 is created
     *      with properties as descibed above.
     *  Action:
     *      getType on all tree resources.
     *  Result:
     *      prompt, prompt, prompt
     *
     * 5. Assert voice variant of resources is correct.
     *  Condition:
     *      The member resources en_audio_1, sv_audio_1 and en_video_1 is created
     *      with properties as descibed above.
     *  Action:
     *      getVoiceVariant on all tree resources.
     *  Result:
     *      male, male, null
     *
     * 6. Assert voice variant of resources is correct.
     *  Condition:
     *      The member resources en_audio_1, sv_audio_1 and en_video_1 is created
     *      with properties as descibed above.
     *  Action:
     *      getVoiceVariant on all tree resources.
     *  Result:
     *      male, male, null
     *
     * 8. Retreive content "beep" without qualifers.
     *  Condition:
     *      package "en_audio_1" has a  with id "beep" which
     *      holds reference is mediafile "beep.wav".
     *  Action:
     *      getMediaContent("beep", null);
     *  Result:
     *      - Returns one IMediaObject that holds the file "beep.wav"
     * <p/>
     * </pre>
     */
    public void testContentMappedFromXML() {
        // 1  - id
        assertEquals("The id of the resource should be en_audio_1",
                "en_audio_1", en_audio_1.getID());
        assertEquals("The id of the resource should be sv_audio_1",
                "sv_audio_1", sv_audio_1.getID());
        assertEquals("The id of the resource should be en_video_1",
                "en_video_1", en_video_1.getID());

        // 2  - prio
        assertEquals("The priority of the resource should be 1",
                1, en_audio_1.getPriority());
        assertEquals("The priority of the resource should be 2",
                2, sv_audio_1.getPriority());
        assertEquals("The priority of the resource should be 1",
                1, en_video_1.getPriority());

        // 3  - lang
        assertEquals("The language of the resource should be en",
                "en", en_audio_1.getMediaContentResourceProperties().getLanguage());
        assertEquals("The language of the resource should be sv",
                "sv", sv_audio_1.getMediaContentResourceProperties().getLanguage());
        assertEquals("The langugage of the resource should be en",
                "en", en_video_1.getMediaContentResourceProperties().getLanguage());

        // 4 - type
        assertEquals("The type of the resource should be prompt",
                "prompt", en_audio_1.getMediaContentResourceProperties().getType());
        assertEquals("The type of the resource should be prompt",
                "prompt", sv_audio_1.getMediaContentResourceProperties().getType());
        assertEquals("The type of the resource should be prompt",
                "prompt", en_video_1.getMediaContentResourceProperties().getType());

        // 5 - voice variant
        assertEquals("The voice variant of the resource should be male",
                "male", en_audio_1.getMediaContentResourceProperties().getVoiceVariant());
        assertEquals("The voice variant of the resource should be male",
                "male", sv_audio_1.getMediaContentResourceProperties().getVoiceVariant());
        assertNull("The voice variant of the resource should be null",
                en_video_1.getMediaContentResourceProperties().getVoiceVariant());
        // todo check  codecs

        // 8
        try {
            IMediaObject[] mediaObjectArray =
                    en_audio_1.getMediaContent("beep.wav", null);
            assertNotNull("The returned array of mediaobjects is null",
                    mediaObjectArray);
            assertEquals("There should be one mediaobject returned",
                    1, mediaObjectArray.length);

        } catch (MediaContentManagerException e) {
            fail("Failed to retreive media content for id 201");
        }


    }





    /**
     * This test lets multiple thread call the
     * {@link MediaContentResource#getMediaContent(String, IMediaQualifier[])}
     * concurrently, asking for the same IMediaObject that represents the
     * file VVA_0276.wav.
     */
    public void testGetMediaContentConcurrent() throws InterruptedException {
        GetMediaContentClient[] clients = new GetMediaContentClient[3];
        NumberQualifier nQ = new NumberQualifier("numberOfNewMessages", 1, NumberQualifier.Gender.Male);
        for (int i = 0; i < clients.length; i++) {
            NumberQualifier numberTestQualifier =
                    new NumberQualifier("numberTest", i, NumberQualifier.Gender.None);
            clients[i] = new GetMediaContentClient(
                    en_audio_1,
                    "2100",
                    new IMediaQualifier[] {nQ},
                    new IMediaQualifier[] {numberTestQualifier},
                    i);

        }

        runTestCaseRunnables(clients);
        joinTestCaseRunnables(clients);

//        for (int i = 0; i < clients.length; i++) {
//            clients[i].start();
//        }
//        for (int i = 0; i < clients.length; i++) {
//            clients[i].join();
//        }
    }

    /**
     * Thread that requests media with a specific id on a resource until
     * someone calls the done() method.
     *
     */
    private class GetMediaContentClient extends TestCaseRunnable {
        private IMediaContentResource resource;
        private String id;
        private boolean done = false;
        private IMediaQualifier[] qualifiers;
        private IMediaQualifier[] qualifiers2;
        private int clientId;

        public GetMediaContentClient(
                IMediaContentResource resource,
                String id,
                IMediaQualifier[] qualifiers,
                IMediaQualifier[] qualifiers2,
                int clientId) {
            this.resource = resource;
            this.id = id;
            this.qualifiers = qualifiers;
            this.qualifiers2 = qualifiers2;
            this.clientId = clientId;
        }

        public void runTestCase() {
            // Test build from qualifier.
            // Get an IMediaObject from a number qualifier with the value
            // from clientId.
            for (int i = 0; i < 100; ++i) {
                IMediaObject[] moArr;
                try {
                    moArr = resource.getMediaContent("numberqualifiertest", qualifiers2);
                    assertEquals("One IMediaObject should have been returned.",
                            1, moArr.length);
                    IMediaObject mo = moArr[0];
                    assertNotNull("MediaObject is null", mo);
                    assertEquals("Fetched mediaobject should be " + clientId + ".wav",
                            clientId + ".wav", ((FileMediaObject)mo).getFile().getName());
                } catch (MediaContentManagerException e) {
                    fail(e.getMessage());
                }
            }
            for (int i = 0; i < 100; i++) {
                try {
                    IMediaObject[] moArr = resource.getMediaContent(id, qualifiers);
                    IMediaObject mo = moArr[0];
                    assertNotNull("MediaObject is null", mo);
                    assertEquals("Fetched mediaobject should be VVA_0276.wav",
                            "VVA_0276.wav", ((FileMediaObject)mo).getFile().getName());
                } catch (MediaContentManagerException e) {
                    fail("Failed to get IMediaObject from resource " +
                    resource.getID());
                }
            }
            for (int i = 0; i < 100; i++) {
                try {
                    IMediaObject[] moArr = resource.getMediaContent(id, qualifiers);
                    IMediaObject mo = moArr[0];
                    assertNotNull("MediaObject is null", mo);
                    assertEquals("Fetched mediaobject should be VVA_0276.wav",
                            "VVA_0276.wav", ((FileMediaObject)mo).getFile().getName());
                } catch (MediaContentManagerException e) {
                    fail("Failed to get IMediaObject from resource " +
                    resource.getID());
                }
            }
            for (int i = 0; i < 100; i++) {
                IMediaObject[] moArr = new IMediaObject[0];
                try {
                    moArr = resource.getMediaContent(id, qualifiers);
                } catch (MediaContentManagerException e) {
                    fail(e.getMessage());
                }
                IMediaObject mo = moArr[0];
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(((FileMediaObject)mo).getFile().getAbsolutePath());
                } catch (FileNotFoundException e) {
                    fail(e.getMessage());
                }
                InputStream is = mo.getInputStream();
                int readByte;
                try {
                    while ((readByte = fis.read()) != -1) {
                        assertEquals("Byte mismatch",
                                readByte, is.read());
                    }
                } catch (IOException e) {
                    fail(e.getMessage());
                }
            }
        }
    }
}
