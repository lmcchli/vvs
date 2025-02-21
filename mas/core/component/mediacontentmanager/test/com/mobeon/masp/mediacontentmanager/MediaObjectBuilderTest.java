/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

import com.mobeon.masp.mediaobject.*;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.masp.mediacontentmanager.condition.Condition;
import com.mobeon.masp.mediacontentmanager.qualifier.IMediaObjectQualifier;
import com.mobeon.masp.mediacontentmanager.qualifier.MediaQualifierFactory;
import com.mobeon.masp.mediacontentmanager.grammar.*;
import junit.framework.TestCase;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;

/**
 * Unit tests for class {@link MediaObjectBuilder}.
 *
 * Todo the builder uses a media object cache, should be tested.
 *
 * @author Mats Egland
 */
public class MediaObjectBuilderTest extends TestCase {
    private static final String PACKAGE_ROOT =  "applications/mediacontentpackages/en_audio_1/";
    private static final String PACKAGE_DEF  = PACKAGE_ROOT + "MediaContentPackage.xml";
    private static final String CONTENT_DEF  = PACKAGE_ROOT + "MediaContent.xml";
    private static final String OBJECT_DEF  = PACKAGE_ROOT + "MediaObjects.xml";
    private static final String FILENAME = "beep.wav";
    private static final int FILESIZE = 1420;

    private static final String SUBJECT = "Voice message from Nisse";
    /**
     * MessageElement of type mediafile that has a
     * reference to the file "beep.wav"
     */
    private MessageElement beep_wav;
    /**
     * MessageElement of type text that holds the text is
     * member SUBJECT.
     */
    private MessageElement subject;

    /**
     * Tested builder.
     */
    private MediaObjectBuilder builder;
    /**
     * The resource that the above builder will work for.
     * Has the following properties:
     *
     * Language         "en"
     * type             "prompt"
     * voice variant    "male"
     * codecs           {"audio/pcmu"}
     *
     */
    MediaContentResource mediaContentResource;
    private MediaQualifierFactory mediaQualifierFactory =
            new MediaQualifierFactory();
    private IMediaQualifier qNumberOfMessages;
    private MessageElement numberOfMessages;
    private IMediaQualifier qSpokenName;
    private MessageElement spokenName;
    private IMediaQualifier qDate;
    private MessageElement dateElement;
    // TodO new
    private IMediaQualifier qCompleteDate;
    private MessageElement completeDateElement;
    // Todo end new

    private IMediaQualifier qTime24;
    private MessageElement time24Element;
    private IMediaQualifier qTime12;
    private MessageElement time12Element;
    private IMediaQualifier qTime12b;
    private MessageElement time12Elementb;
    private IMediaQualifier qString;
    private MessageElement stringElement;
    private IMediaQualifier qWeekDay;
    private MessageElement weekdayElement;
    private MessageElement invalidURIElement;

    private IActionElementFactory actionElementFactory
            = new ActionElementFactory();
    private List<RulesRecord> rulesRecordList = new ArrayList<RulesRecord>();

    /**
     * Creates a <code>MediaObjectBuilder</code> with a
     * <code>MediaContentResource</code> a
     * <code>MediaObjectFactory</code> and a
     * <code>MediaObjectCache</code>.
     * BUT No cache!.
     * <pre>
     * The resource has:
     *
     * - A content with id "beep" that has one message with
     *   the messageelement <code>beep_wav</code>.
     * - Language         "en"
     * - type             "prompt"
     * - voice variant    "male"
     * - codecs           {"audio/pcmu"}
     * </pre>
     *
     * @throws Exception
     */
    protected void setUp() throws Exception {
        XmlBeanFactory bf = new XmlBeanFactory(new ClassPathResource(
                "mediacontentmanager_beans.xml", getClass()));
        ContentTypeMapper contentTypeMapper = (ContentTypeMapperImpl)bf.getBean("ContentTypeMapper");
        assertNotNull(contentTypeMapper);

        mediaContentResource =
                new MediaContentResource(new File(
                        PACKAGE_DEF).toURI());
        mediaContentResource.getMediaContentResourceProperties().setLanguage("en");
        mediaContentResource.getMediaContentResourceProperties().setType("prompt");
        mediaContentResource.getMediaContentResourceProperties().setVoiceVariant("male");
        mediaContentResource.getMediaContentResourceProperties().addCodec(new MimeType("audio/pcmu"));

        // Build content for beep
        MediaContent beepContent = new MediaContent("beep",
                    new File(CONTENT_DEF).toURI());
        MediaObjectSource beepSource = new MediaObjectSource(
                MediaObjectSource.Type.MEDIAFILE,
                FILENAME,
                new File(OBJECT_DEF).toURI());
        Message message = new Message(new Condition("true"), beepContent);
        beep_wav = new MessageElement(MessageElement.MessageElementType.mediafile,
                "beep.wav");
        message.appendMessageElement(beep_wav);
        beepContent.addMessage(message);
        mediaContentResource.addMediaContent(beepContent);
        mediaContentResource.addMediaObjectSource(beepSource);

        // Build content for subject
        MediaContent subjectContent = new MediaContent("subject",
                    new File(CONTENT_DEF).toURI());
        MediaObjectSource subjectSource = new MediaObjectSource(
                MediaObjectSource.Type.TEXT,
                "subject",
                new File(OBJECT_DEF).toURI());
        subjectSource.setSourceText(SUBJECT);
        subjectSource.addLength(new MediaLength(MediaLength.LengthUnit.PAGES, 1));
        Message subjectMessage = new Message(new Condition("true"), subjectContent);
        subject = new MessageElement(MessageElement.MessageElementType.text,
                "subject");
        subjectMessage.appendMessageElement(subject);
        subjectContent.addMessage(subjectMessage);
        mediaContentResource.addMediaContent(subjectContent);
        mediaContentResource.addMediaObjectSource(subjectSource);

        // condition for 100
        NumberRuleCondition condition100 = new NumberRuleCondition(false, 1, 99, 0, 99, false, true);
        IActionElement actionElement100 = actionElementFactory.create(IActionElement.ActionType.mediafile);
        actionElement100.setMediaFileName("100");
        condition100.addActionElement(actionElement100);
        NumberRule numberRule100 = new NumberRule(100);
        numberRule100.addCondition(condition100);
        // condition for 1
        NumberRuleCondition condition1 = new NumberRuleCondition(true, 1, 1, 0, 1, true, true);
        IActionElement actionElement1 = actionElementFactory.create(IActionElement.ActionType.mediafile);
        actionElement1.setMediaFileName("1");
        condition1.addActionElement(actionElement1);
        NumberRule numberRule1 = new NumberRule(1);
        numberRule1.addCondition(condition1);
        // condition for dom1
        NumberRuleCondition conditionDom1 = new NumberRuleCondition(false, 1, 1, 0, 999999, true, true);
        IActionElement actionElementDom1 = actionElementFactory.create(IActionElement.ActionType.mediafile);
        actionElementDom1.setMediaFileName("dom1");
        conditionDom1.addActionElement(actionElementDom1);
        NumberRule numberRuleDom1 = new NumberRule(10000);
        numberRuleDom1.addCondition(conditionDom1);
        // condition for feb
        NumberRuleCondition conditionFeb = new NumberRuleCondition(false, 2, 2, 0, 999999, true, true);
        IActionElement actionElementFeb = actionElementFactory.create(IActionElement.ActionType.mediafile);
        actionElementFeb.setMediaFileName("feb");
        IActionElement actionElementSwap = actionElementFactory.create(IActionElement.ActionType.swap);
        actionElementSwap.setSwapValue(-1);
        conditionFeb.addActionElement(actionElementSwap);
        conditionFeb.addActionElement(actionElementFeb);
        NumberRule numberRuleFeb = new NumberRule(100);
        numberRuleFeb.addCondition(conditionFeb);
        // conditions for hours24: 21
        IActionElement actionElement20 = actionElementFactory.create(IActionElement.ActionType.mediafile);
        actionElement20.setMediaFileName("20");
        NumberRuleCondition conditionHours24 = new NumberRuleCondition(false, 1, 1, 0, 5900, true, true);
        conditionHours24.addActionElement(actionElement20);
        conditionHours24.addActionElement(actionElement1);
        NumberRule numberRuleHours24 = new NumberRule(210000);
        numberRuleHours24.addCondition(conditionHours24);
        // condition for minutes24: 34
        IActionElement actionElement30 = actionElementFactory.create(IActionElement.ActionType.mediafile);
        actionElement30.setMediaFileName("30");
        IActionElement actionElement4 = actionElementFactory.create(IActionElement.ActionType.mediafile);
        actionElement4.setMediaFileName("4");
        NumberRuleCondition conditionMinutes24 = new NumberRuleCondition(true, 1, 1, 0, 0, true, true);
        conditionMinutes24.addActionElement(actionElement30);
        conditionMinutes24.addActionElement(actionElement4);
        NumberRule numberRuleMinutes24 = new NumberRule(3400);
        numberRuleMinutes24.addCondition(conditionMinutes24);
        // condition for hours12: 4, reuse actionElement 4
        NumberRuleCondition conditionHours12 = new NumberRuleCondition(false, 1, 1, 0, 597000, true, true);
        conditionHours12.addActionElement(actionElement4);
        NumberRule numberRuleHours12 = new NumberRule(4000000);
        numberRuleHours12.addCondition(conditionHours12);
        // condition for hours12: 12
        IActionElement actionElement12 = actionElementFactory.create(IActionElement.ActionType.mediafile);
        actionElement12.setMediaFileName("12");
        NumberRuleCondition conditionHours12b = new NumberRuleCondition(false, 1, 1, 0, 597000, true, true);
        conditionHours12b.addActionElement(actionElement12);
        NumberRule numberRuleHours12b = new NumberRule(12000000);
        numberRuleHours12b.addCondition(conditionHours12b);
        // condition for minutes12: 31, reuse actionElements 30 and 1
        NumberRuleCondition conditionMinutes12 = new NumberRuleCondition(false, 1, 1, 0, 7000, true, true);
        conditionMinutes12.addActionElement(actionElement30);
        conditionMinutes12.addActionElement(actionElement1);
        NumberRule numberRuleMinutes12 = new NumberRule(310000);
        numberRuleMinutes12.addCondition(conditionMinutes12);
        // condition for AM
        IActionElement actionElementAM = actionElementFactory.create(IActionElement.ActionType.mediafile);
        actionElementAM.setMediaFileName("pm");
        NumberRuleCondition conditionAM = new NumberRuleCondition(true, 0, 0, 0, 0, false, true);
        conditionAM.addActionElement(actionElementAM);
        NumberRule numberRuleAM = new NumberRule(100);
        numberRuleAM.addCondition(conditionAM);
        // condition for PM
        IActionElement actionElementPM = actionElementFactory.create(IActionElement.ActionType.mediafile);
        actionElementPM.setMediaFileName("pm");
        NumberRuleCondition conditionPM = new NumberRuleCondition(true, 1, 1, 0, 0, true, true);
        conditionPM.addActionElement(actionElementPM);
        NumberRule numberRulePM = new NumberRule(7000);
        numberRulePM.addCondition(conditionPM);

        // create rules list
        RulesRecord rulesRecordNumber = new RulesRecord(IMediaQualifier.QualiferType.Number);
        rulesRecordNumber.addGender(IMediaQualifier.Gender.None);
        rulesRecordNumber.addNumberRule(numberRule100);
        rulesRecordNumber.addNumberRule(numberRule1);
        RulesRecord rulesRecordDateDM = new RulesRecord(IMediaQualifier.QualiferType.DateDM);
        rulesRecordDateDM.addGender(IMediaQualifier.Gender.None);
        rulesRecordDateDM.addNumberRule(numberRuleDom1);
        rulesRecordDateDM.addNumberRule(numberRuleFeb);
        RulesRecord rulesRecordTime24 = new RulesRecord(IMediaQualifier.QualiferType.Time24);
        rulesRecordTime24.addGender(IMediaQualifier.Gender.None);
        rulesRecordTime24.addNumberRule(numberRuleHours24);
        rulesRecordTime24.addNumberRule(numberRuleMinutes24);
        RulesRecord rulesRecordTime12 = new RulesRecord(IMediaQualifier.QualiferType.Time12);
        rulesRecordTime12.addGender(IMediaQualifier.Gender.None);
        rulesRecordTime12.addNumberRule(numberRuleHours12);
        rulesRecordTime12.addNumberRule(numberRuleHours12b);
        rulesRecordTime12.addNumberRule(numberRuleMinutes12);
        rulesRecordTime12.addNumberRule(numberRuleAM);
        rulesRecordTime12.addNumberRule(numberRulePM);

         // ************ NEW ***********
        RulesRecord rulesRecordCompleteDate = new RulesRecord(IMediaQualifier.QualiferType.CompleteDate);
        NumberRuleCondition condition2006 = new NumberRuleCondition(false, 2006, 2006, 0, 999999, true, true);
        IActionElement actionElement2006 = actionElementFactory.create(IActionElement.ActionType.mediafile);
        actionElement2006.setMediaFileName("year2006");
        //IActionElement actionElementSwap = actionElementFactory.create(IActionElement.ActionType.swap);
        //actionElementSwap.setSwapValue(-1);
        //conditionFeb.addActionElement(actionElementSwap);
        //conditionFeb.addActionElement(actionElement2006);
        NumberRule numberRule2006 = new NumberRule(1000000);
        numberRule2006.addCondition(condition2006);
        rulesRecordCompleteDate.addNumberRule(numberRule2006);
        // ************ END NEW ***********

        rulesRecordList.add(rulesRecordNumber);
        rulesRecordList.add(rulesRecordDateDM);
        rulesRecordList.add(rulesRecordTime24);
        rulesRecordList.add(rulesRecordTime12);
        rulesRecordList.add(rulesRecordCompleteDate);
        mediaContentResource.addRulesRecords(rulesRecordList);



        // Add 0.wav object source
        mediaContentResource.addMediaObjectSource(new MediaObjectSource(
                MediaObjectSource.Type.MEDIAFILE,
                "0.wav",
                new File(OBJECT_DEF).toURI()));
        // Add 1.wav object source
        mediaContentResource.addMediaObjectSource(new MediaObjectSource(
                MediaObjectSource.Type.MEDIAFILE,
                "1.wav",
                new File(OBJECT_DEF).toURI()));
        // Add 2.wav object source
        mediaContentResource.addMediaObjectSource(new MediaObjectSource(
                MediaObjectSource.Type.MEDIAFILE,
                "2.wav",
                new File(OBJECT_DEF).toURI()));
        // Add 3.wav object source
        mediaContentResource.addMediaObjectSource(new MediaObjectSource(
                MediaObjectSource.Type.MEDIAFILE,
                "3.wav",
                new File(OBJECT_DEF).toURI()));
        // Add 4.wav object source
        mediaContentResource.addMediaObjectSource(new MediaObjectSource(
                MediaObjectSource.Type.MEDIAFILE,
                "4.wav",
                new File(OBJECT_DEF).toURI()));
        // Add 7.wav object source
        mediaContentResource.addMediaObjectSource(new MediaObjectSource(
                MediaObjectSource.Type.MEDIAFILE,
                "7.wav",
                new File(OBJECT_DEF).toURI()));
        // Add 12.wav object source
        mediaContentResource.addMediaObjectSource(new MediaObjectSource(
                MediaObjectSource.Type.MEDIAFILE,
                "12.wav",
                new File(OBJECT_DEF).toURI()));
        // Add 20.wav object source
        mediaContentResource.addMediaObjectSource(new MediaObjectSource(
                MediaObjectSource.Type.MEDIAFILE,
                "20.wav",
                new File(OBJECT_DEF).toURI()));
        // Add 30.wav object source
        mediaContentResource.addMediaObjectSource(new MediaObjectSource(
                MediaObjectSource.Type.MEDIAFILE,
                "30.wav",
                new File(OBJECT_DEF).toURI()));
        // Add 100.wav object source
        mediaContentResource.addMediaObjectSource(new MediaObjectSource(
                MediaObjectSource.Type.MEDIAFILE,
                "100.wav",
                new File(OBJECT_DEF).toURI()));
        // Add dom1.wav object source
        mediaContentResource.addMediaObjectSource(new MediaObjectSource(
                MediaObjectSource.Type.MEDIAFILE,
                "dom1.wav",
                new File(OBJECT_DEF).toURI()));
        // Add feb.wav object source
        mediaContentResource.addMediaObjectSource(new MediaObjectSource(
                MediaObjectSource.Type.MEDIAFILE,
                "feb.wav",
                new File(OBJECT_DEF).toURI()));
        // Add am.wav object source
        mediaContentResource.addMediaObjectSource(new MediaObjectSource(
                MediaObjectSource.Type.MEDIAFILE,
                "am.wav",
                new File(OBJECT_DEF).toURI()));
        // Add pm.wav object source
        mediaContentResource.addMediaObjectSource(new MediaObjectSource(
                MediaObjectSource.Type.MEDIAFILE,
                "pm.wav",
                new File(OBJECT_DEF).toURI()));
        // Add mon.wav object source
        mediaContentResource.addMediaObjectSource(new MediaObjectSource(
                MediaObjectSource.Type.MEDIAFILE,
                "mon.wav",
                new File(OBJECT_DEF).toURI()));
        // Add invalidURI.wav object source
        mediaContentResource.addMediaObjectSource(new MediaObjectSource(
                MediaObjectSource.Type.MEDIAFILE,
                "invalidURI.wav ",
                new File(OBJECT_DEF).toURI()));

        MediaObjectFactory mediaObjectFactory =
                new MediaObjectFactory();
        builder = new MediaObjectBuilder(
                mediaContentResource,
                mediaObjectFactory,
                null,
                contentTypeMapper);

        // Number qualifier, 101
        qNumberOfMessages = mediaQualifierFactory.create(IMediaQualifier.QualiferType.Number,
                "numberOfMessages", "101", IMediaQualifier.Gender.None);
        numberOfMessages = new MessageElement(MessageElement.MessageElementType.qualifier,
                "numberOfMessages:Number:None");
        // MediaObject qualifier, spokenName
        IMediaObject spokenNameMediaObject = mediaObjectFactory.create(new File(PACKAGE_ROOT + FILENAME));
        qSpokenName = new IMediaObjectQualifier("spokenName", spokenNameMediaObject,
                IMediaQualifier.Gender.None);
        //qSpokenName = mediaQualifierFactory.create(IMediaQualifier.QualiferType.MediaObject,
        //        "spokenName", spokenNameMediaObject, IMediaQualifier.Gender.None);
        spokenName = new MessageElement(MessageElement.MessageElementType.qualifier,
                "spokenName:MediaObject:None");
        // DateDM qualifier, February 1st
        qDate = mediaQualifierFactory.create(IMediaQualifier.QualiferType.DateDM,
                "date", "2006-02-01", IMediaQualifier.Gender.None);
        dateElement = new MessageElement(MessageElement.MessageElementType.qualifier,
                "date:DateDM:None");
        // Time24 qualifier, 21:34:00
        qTime24 = mediaQualifierFactory.create(IMediaQualifier.QualiferType.Time24,
                "time24", "21:34:00", IMediaQualifier.Gender.None);
        time24Element = new MessageElement(MessageElement.MessageElementType.qualifier,
                "time24:Time24:None");
        // Time12 qualifier, 16:31:00
        qTime12 = mediaQualifierFactory.create(IMediaQualifier.QualiferType.Time12,
                "time12", "16:31:00", IMediaQualifier.Gender.None);
        time12Element = new MessageElement(MessageElement.MessageElementType.qualifier,
                "time12:Time12:None");
        // Time12 qualifier, 00:31:00
        qTime12b = mediaQualifierFactory.create(IMediaQualifier.QualiferType.Time12,
                "time12b", "00:31:00", IMediaQualifier.Gender.None);
        time12Elementb = new MessageElement(MessageElement.MessageElementType.qualifier,
                "time12b:Time12:None");
        // String qualifier, 0701112233
        qString = mediaQualifierFactory.create(IMediaQualifier.QualiferType.String,
                "telephoneNumber", "070 1112233", IMediaQualifier.Gender.None);
        stringElement = new MessageElement(MessageElement.MessageElementType.qualifier,
                "telephoneNumber:String:None");
        qWeekDay = mediaQualifierFactory.create(IMediaQualifier.QualiferType.WeekDay,
                "day", "2006-02-27", IMediaQualifier.Gender.None);
        weekdayElement = new MessageElement(MessageElement.MessageElementType.qualifier,
                "day:WeekDay:None");
        invalidURIElement = new MessageElement(MessageElement.MessageElementType.mediafile,
                "invalidURI.wav ");

        // Todo new
        // CompleteDate qualifier, February 1st 2008
        qCompleteDate = mediaQualifierFactory.create(IMediaQualifier.QualiferType.CompleteDate,
                "completeDate", "2008-02-01", IMediaQualifier.Gender.None);
        completeDateElement = new MessageElement(MessageElement.MessageElementType.qualifier,
                "date:CompleteDate:None");

    }

    protected void tearDown() throws Exception {
        super.tearDown();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Test that it is possible to have a wav and move file in the same package, and that
     * the content type for the respective file is correct.
     * TR 29068, TR 27862
     * @throws Exception
     */
    public void testWavAndMovInSamePackage() throws Exception {
        String pathname = "applications/mediacontentpackages/en_video_1/MediaContentPackage.xml";
        MediaContentResource mediaContentResource =  createMediaContentResource(pathname);
        mediaContentResource.getMediaContentResourceProperties().addCodec(new MimeType("video/h263"));

        String PACKAGE_ROOT =  "applications/mediacontentpackages/en_video_1/";
        String CONTENT_DEF  = PACKAGE_ROOT + "MediaContent.xml";
        String OBJECT_DEF  = PACKAGE_ROOT + "MediaObjects.xml";

        checkContentTypeOfMediaObject(mediaContentResource, CONTENT_DEF, OBJECT_DEF, "beep.wav", "beep", "audio/wav");
        checkContentTypeOfMediaObject(mediaContentResource, CONTENT_DEF, OBJECT_DEF, "UM_0010.mov", "UM_0010", "video/quicktime");
    }

    /**
     * Tests for method
     * {@link MediaObjectBuilder#build(MessageElement)} where the messageelements
     * references a file.
     * No cache in builder.
     *
     * 1. Create IMediaObject from file "beep.wav"
     *  Condition:
     *      Created: MessageElement beep_wav with:
     *          type            mediafile
     *          reference       beep.wav
     *  Action:
     *      MessageElement=beep_wav
     *  Result:
     *      IMediaObject was created.
     *
     * 2. Assert size of created mediaobject is correct.
     *  Condition:
     *      Created: MessageElement beep_wav with:
     *          type            mediafile
     *          reference       beep.wav
     *  Action:
     *      MessageElement=beep_wav
     *  Result:
     *      Size of mediaObject is 1420
     *
     * 3. Verify the contents of the mediaobject's properties.
     *  Condition:
     *      Created: MessageElement beep_wav with:
     *          type            mediafile
     *          reference       beep.wav
     *  Action:
     *      Build mediaobject from message-element above and
     *      verify that its properties is good.
     *
     *  Result:
     *      1. The content-type is "audio/wav".
     *      2. The file-extension is "wav".
     */
    public void testBuildFromFile_NoCache() throws MimeTypeParseException {
        // 1
        IMediaObject mediaObject = null;
        try {
            mediaObject = builder.build(beep_wav);
        } catch (MediaContentManagerException e) {
            fail("Failed to call build: " + e.getMessage());
        }
        assertNotNull("Failed to create IMediaObject from beep.wav",
                mediaObject);

        // 2
        IMediaObjectIterator iter = mediaObject.getNativeAccess().iterator();
        int readBytes = 0;
        while (iter.hasNext()) {
            try {
                ByteBuffer byteBuffer = iter.next();

                while (byteBuffer.hasRemaining()) {
                    byteBuffer.get();
                    readBytes++;
                }
            } catch (MediaObjectException e) {
                fail("Failed to iterate over MediaObject");
            }
        }
        assertEquals("Number of bytes in mediaobject does not match with filesize",
                FILESIZE, readBytes);

        // 3
        MediaProperties mediaProperties = mediaObject.getMediaProperties();
        assertNotNull("ContentType is null", mediaProperties.getContentType());
        assertTrue("The contentType should be audio/wav, it is:" +
                mediaProperties.getContentType().getBaseType(),
                mediaProperties.getContentType().match("audio/wav"));
        assertNotNull("FileExtension is null");
        assertEquals("The file-extension should be wav",
                mediaProperties.getFileExtension(),
                "wav");

    }

    /**
     * Tests for method
     * {@link MediaObjectBuilder#build(MessageElement)} where the messageelements
     * references a text.
     * No cache in builder.
     *
     * 1. Create IMediaObject from file text SUBJECT.
     *  Condition:
     *      Created: MessageElement subject with:
     *          type            text
     *          reference       "subject" which references to text SUBJECT.
     *  Action:
     *      MessageElement=subject
     *  Result:
     *      IMediaObject was created.
     *
     * 2. Assert content of created mediaobject is correct.
     *  Condition:
     *      Created: MessageElement subject with:
     *          type            text
     *          text = SUBJECT
     *  Action:
     *      MessageElement=subject
     *  Result:
     *      Content of media object corresponds to text in member SUBJECT.
     *
     * 3. Verify the contents of the mediaobject's properties.
     *  Condition:
     *      Created: MessageElement beep_wav with:
     *          type            mediafile
     *          reference       beep.wav
     *  Action:
     *      Build mediaobject from message-element above and
     *      verify that its properties is good.
     *
     *  Result:
     *      1. The content-type is "audio/wav".
     *      2. The file-extension is "wav".
     */
    public void testBuildFromText_NoCache() throws MimeTypeParseException, IOException {
        // 1
        IMediaObject mediaObject = null;
        try {
            mediaObject = builder.build(subject);
        } catch (MediaContentManagerException e) {
            fail("Failed to call build " + e.getMessage());
        }
        assertNotNull("Failed to create IMediaObject from MessageElement of type text",
                mediaObject);

        // 2
        byte[] bytes = SUBJECT.getBytes("UTF-16");
        byte[] buffer = new byte[bytes.length];
        InputStream is = mediaObject.getInputStream();
        is.read(buffer);
        for (int i = 0; i < bytes.length; i++) {
            assertEquals("Text in mediaobject is wrong",
                    bytes[i], buffer[i]);
        }


        // 3
        MediaProperties mediaProperties = mediaObject.getMediaProperties();
        assertNotNull("ContentType is null", mediaProperties.getContentType());
        assertTrue("The contentType should be text/plain, it is:" +
                mediaProperties.getContentType().getBaseType(),
                mediaProperties.getContentType().match("text/plain"));
        assertNotNull("FileExtension is null");
        assertEquals("The file-extension should be txt",
                mediaProperties.getFileExtension(),
                "txt");
        assertEquals("Length should be 1 pages",
                1, mediaProperties.getLengthInUnit(MediaLength.LengthUnit.PAGES));

    }

    /**
     * Test method for {@link MediaObjectBuilder#build(MessageElement, IMediaQualifier[])}
     * <p/>
     * <pre>
     * 1) Test number qualifier
     *    Contiditions:
     *      RulesRecord with NumberRules for Number,None set up in the
     *      mediaContentResource.
     *      The NumberRule contains contitions for the tested number and has
     *      action elements with the corresponding message elements.
     *      Message element with the Number qualifier and the qualifier is
     *      created.
     *    Action:
     *      Build from message element with qualifier 101
     *    Result:
     *      3 IMediaObjects are returned. (1 100 1)
     *
     * 2) Test MediaObject qualifier
     *    Contiditions:
     *      Message element with the MediaObject qualifier and the qualifier is
     *      created.
     *    Action:
     *      Build from message element with qualifier spokenName
     *    Result:
     *      The IMediaObject spokenName is returned.
     *
     * 3) Test DateDM qualifier
     *    Contiditions:
     *      RulesRecord with NumberRules for DateDM,None set up in the
     *      mediaContentResource.
     *      The NumberRule contains contitions for the tested number and has
     *      action elements with the corresponding message elements.
     *      Message element with the DateDM qualifier and the qualifier is
     *      created.
     *    Action:
     *      Build from message element with qualifier 2006-02-01
     *    Result:
     *      Two IMediaObjects are returned (feb 1st)
     *
     * 4) Test Time24 qualifier
     *    Contiditions:
     *      RulesRecord with NumberRules for Time24,None set up in the
     *      mediaContentResource.
     *      The NumberRule contains contitions for the tested number and has
     *      action elements with the corresponding message elements.
     *      Message element with the Time24 qualifier and the qualifier is
     *      created.
     *    Action:
     *      Build from message element with qualifier 21:34:00
     *    Result:
     *      4 IMediaObjects are returned (20 1 30 4)
     *
     * 5) Test Time12 qualifier
     *    Contiditions:
     *      RulesRecord with NumberRules for Time12,None set up in the
     *      mediaContentResource.
     *      The NumberRule contains contitions for the tested number and has
     *      action elements with the corresponding message elements.
     *      Message element with the Time12 qualifier and the qualifier is
     *      created.
     *    Action:
     *      5.1) Build from message element with qualifier 16:31:00 (4:31 pm)
     *      5.2) Build from message element with qualifier 00:31:00 (12:31 am)
     *    Result:
     *      5.1) 4 IMediaObjects are returned (4 30 1 pm)
     *      5.2) 4 IMediaObjects are returned (12 30 1 am)
     *
     * 6) Test String qualifier
     *    Conditions:
     *      Message element with a String qualifier is created, the String
     *      qualifier is created.
     *    Action:
     *      Build from message element with qualifier 0701112233
     *    Result:
     *      10 IMediaObjects are returned (0, 7, 0, 1, 1, 1, 2, 2, 3, 3).
     *
     * 7) WeedDay qualifier
     *    Conditions:
     *      Message element with a WeekDay qualifier is created, the WeekDay
     *      qualifier is created.
     *    Action:
     *      Build from message element with qualifier value 2006-02-27
     *    Result:
     *      One MediaObject representing Monday is returned.
     *
     * </pre>
     * <p/>
     * @throws MimeTypeParseException
     */
    public void testBuildQualifier() throws MimeTypeParseException {
        // 1 Number qualifier
        List<IMediaObject> mediaObjects = null;
        try {
            mediaObjects = builder.build(numberOfMessages, new IMediaQualifier[] {qNumberOfMessages});
        } catch (Exception e) {
            fail("Failed to call build " + e.getMessage());
            e.printStackTrace();
        }
        assertNotNull("Failed to create IMediaObjects from qualifier 101",
                mediaObjects);

        assertEquals("Did not get 3 IMediaObjects from 101",
                3, mediaObjects.size());

        // 2 MediaObject qualifier
        mediaObjects = null;
        try {
            mediaObjects = builder.build(spokenName, new IMediaQualifier[] {qSpokenName});
        } catch (Exception e) {
            fail("Failed to call build " + e.getMessage());
        }
        assertNotNull("Failed to create IMediaObjects from qualifier spokenName",
                mediaObjects);

        assertEquals("Did not get 1 IMediaObject from spokenName",
                1, mediaObjects.size());

        // 3 DateDM qualifier
        mediaObjects = null;
        try {
            mediaObjects = builder.build(dateElement, new IMediaQualifier[] {qDate});
        } catch (Exception e) {
            fail("Failed to call build " + e.getMessage());
        }
        assertNotNull("Failed to create IMediaObjects from qualifier date",
                mediaObjects);

        assertEquals("Did not get 2 IMediaObjects from date",
                2, mediaObjects.size());

        // TODO
        // 3 CompleteDate qualifier
        /*mediaObjects = null;
        try {
            mediaObjects = builder.build(completeDateElement, new IMediaQualifier[] {qCompleteDate});
        } catch (Exception e) {
            fail("Failed to call build " + e.getMessage());
        }
        assertNotNull("Failed to create IMediaObjects from qualifier CompleteDate",
                mediaObjects);

        assertEquals("Did not get 2 IMediaObjects from CompleteDate",
                2, mediaObjects.size());
        */

        // 4 Time24 qualifier
        mediaObjects = null;
        try {
            mediaObjects = builder.build(time24Element, new IMediaQualifier[] {qTime24});
        } catch (Exception e) {
            fail("Failed to call build " + e.getMessage());
        }
        assertNotNull("Failed to create IMediaObjects from qualifier time24",
                mediaObjects);

        assertEquals("Did not get 4 IMediaObjects from time24",
                4, mediaObjects.size());

        // 5.1 Time12 qualifier
        mediaObjects = null;
        try {
            mediaObjects = builder.build(time12Element, new IMediaQualifier[] {qTime12});
        } catch (Exception e) {
            fail("Failed to call build " + e.getMessage());
        }
        assertNotNull("Failed to create IMediaObjects from qualifier time12",
                mediaObjects);
        assertEquals("Did not get 4 IMediaObjects from time12",
                4, mediaObjects.size());

        // 5.2 Time12 qualifier
        mediaObjects = null;
        try {
            mediaObjects = builder.build(time12Elementb, new IMediaQualifier[] {qTime12b});
        } catch (Exception e) {
            fail("Failed to call build " + e.getMessage());
        }
        assertNotNull("Failed to create IMediaObjects from qualifier time12",
                mediaObjects);
        assertEquals("Did not get 4 IMediaObjects from time12",
                4, mediaObjects.size());

        // 6 String qualifier
        mediaObjects = null;
        try {
            mediaObjects = builder.build(stringElement, new IMediaQualifier[] {qString});
        } catch (Exception e) {
            fail("Failed to call build " + e.getMessage());
        }
        assertNotNull("Failed to create IMediaObjects from qualifier telephoneNumber",
                mediaObjects);

        assertEquals("Did not get 10 IMediaObjects from telephoneNumber",
                10, mediaObjects.size());

        // 7 WeekDay qualifier
        mediaObjects = null;
        try {
            mediaObjects = builder.build(weekdayElement, new IMediaQualifier[] {qWeekDay});
        } catch (Exception e) {
            fail("Failed to call build " + e.getMessage());
        }
        assertNotNull("Failed to create IMediaObjects from qualifier day",
                mediaObjects);

        assertEquals("Did not get 1 IMediaObject from day",
                1, mediaObjects.size());
    }

    public void testBuildWithInvalidInput() throws Exception {
        List<IMediaObject> mediaObjects = null;

        // 1 test with null input
        try {
            mediaObjects = builder.build(null, new IMediaQualifier[] {qNumberOfMessages});
            fail("IllegalArgumentException expected when message element is null!");
        } catch (IllegalArgumentException e) {
            //ok
        }

        // 2 test with unmapped media object source
        MessageElement unmapped = new MessageElement(
                MessageElement.MessageElementType.mediafile,
                "invalid.wav");
        try {
            mediaObjects = builder.build(unmapped, null);
            fail("MediaContentManagerException expected when using an invalid message element!");
        } catch (MediaContentManagerException e) {
            //ok
        }

        // 3 test with a mediaobject with invalid URI.
        mediaObjects = builder.build(invalidURIElement, null);
        assertEquals(0, mediaObjects.size());
    }

    private MediaContentResource createMediaContentResource(String pathname) {
        XmlBeanFactory bf = new XmlBeanFactory(new ClassPathResource(
                "mediacontentmanager_beans.xml", getClass()));
        ContentTypeMapper contentTypeMapper = (ContentTypeMapperImpl)bf.getBean("ContentTypeMapper");
        assertNotNull(contentTypeMapper);

        MediaContentResource mediaContentResource =
                new MediaContentResource(new File(
                        pathname).toURI());
        MediaObjectBuilder builder = new MediaObjectBuilder(
                mediaContentResource, new MediaObjectFactory(),
                new MediaObjectCacheImpl(MediaObjectCacheImpl.POLICY.FIFO, 10, 10, true), contentTypeMapper);

        mediaContentResource.setMediaObjectBuilder(builder);

        mediaContentResource.getMediaContentResourceProperties().setLanguage("en");
        mediaContentResource.getMediaContentResourceProperties().setType("prompt");
        mediaContentResource.getMediaContentResourceProperties().setVoiceVariant("male");
        return mediaContentResource;
    }

    private void checkContentTypeOfMediaObject(MediaContentResource mediaContentResource, String CONTENT_DEF, String OBJECT_DEF,
                                               String src, String id, String expectedContentType) throws MediaContentManagerException {
        IMediaObject[] mediaObjects;


        MediaContent mediaContent = new MediaContent(id,
                    new File(CONTENT_DEF).toURI());
        MediaObjectSource mediaObjectSource = new MediaObjectSource(
                MediaObjectSource.Type.MEDIAFILE,
                src,
                new File(OBJECT_DEF).toURI());
        Message message = new Message(new Condition("true"), mediaContent);
        MessageElement messageElement = new MessageElement(MessageElement.MessageElementType.mediafile,
                src);
        message.appendMessageElement(messageElement);
        mediaContent.addMessage(message);
        mediaContentResource.addMediaContent(mediaContent);
        mediaContentResource.addMediaObjectSource(mediaObjectSource);

        mediaObjects = mediaContentResource.getMediaContent(id, null);
        IMediaObject mediaObject = mediaObjects[0];
        assertTrue(mediaObject.getMediaProperties().getContentType().getBaseType().equals(expectedContentType));
    }

}
