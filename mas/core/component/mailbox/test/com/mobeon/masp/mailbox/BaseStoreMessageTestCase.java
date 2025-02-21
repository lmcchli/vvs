/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.masp.mediaobject.IMediaObject;
import static com.mobeon.masp.mediaobject.MediaLength.LengthUnit.MILLISECONDS;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;


/**
 * @author QHAST
 */
public abstract class BaseStoreMessageTestCase extends BaseMailboxTestCase {

    public static final MimeType AUDIO_WAV_MIMETYPE;
    public static final String AUDIO_WAV_FILE_EXTENSION ="wav";

    public static final MessageContentProperties SPOKEN_NAME_CONTENT_PROPERTIES = new MessageContentProperties("spokenname","Originator's spoken name","en");

    //Audio wav Spoken name
    public static final String AUDIO_WAV_SPOKEN_NAME_DATA ="AudioSpokenData";
    public static final long AUDIO_WAV_SPOKEN_NAME_MILLISEC_LENGTH = 1234;
    public static final IMediaObject AUDIO_WAV_SPOKEN_NAME;

    //Audio wav Message
    public static final String AUDIO_WAV_MESSAGE_DATA ="AudioMessageData";
    public static final long AUDIO_WAV_MESSAGE_MILLISEC_LENGTH = 4567;
    public static final IMediaObject AUDIO_WAV_MESSAGE;


    static {
        try {
            AUDIO_WAV_MIMETYPE = new MimeType("audio/wav");
        } catch (MimeTypeParseException e) {
            throw new IllegalArgumentException("Incorrect mime type!",e);
        }

        AUDIO_WAV_SPOKEN_NAME = createMediaObject(AUDIO_WAV_SPOKEN_NAME_DATA,AUDIO_WAV_MIMETYPE);
        AUDIO_WAV_SPOKEN_NAME.getMediaProperties().setFileExtension(AUDIO_WAV_FILE_EXTENSION);
        AUDIO_WAV_SPOKEN_NAME.getMediaProperties().addLengthInUnit(MILLISECONDS,AUDIO_WAV_SPOKEN_NAME_MILLISEC_LENGTH);

        AUDIO_WAV_MESSAGE = createMediaObject(AUDIO_WAV_MESSAGE_DATA,AUDIO_WAV_MIMETYPE);
        AUDIO_WAV_MESSAGE.getMediaProperties().setFileExtension(AUDIO_WAV_FILE_EXTENSION);
        AUDIO_WAV_MESSAGE.getMediaProperties().addLengthInUnit(MILLISECONDS,AUDIO_WAV_MESSAGE_MILLISEC_LENGTH);
    }

    protected IStorableMessageFactory storableMessageFactory;


    public BaseStoreMessageTestCase(String name) {
        super(name);
    }

    protected abstract IStorableMessageFactory createStorableMessageFactory()  throws Exception;

    public void setUp() throws Exception
    {
        super.setUp();
        storableMessageFactory = createStorableMessageFactory();
    }

    public void testStoreVoiceMessage() throws Exception
    {
        IStorableMessage m = storableMessageFactory.create();
        m.setType(MailboxMessageType.VOICE);
        m.setSubject("Voice Messagec Test");
        m.setSender("<hakan.stolt@mobeon.com>");
        m.setRecipients("primary@dummy.mobeon.com","priiiiiimAAaaarRRy@dUMmY.mobeon.com");
        m.setSecondaryRecipients("secondary@dummy.mobeon.com","seeeeCooondaaaryyyy@DuMmY.mobeon.com");

        m.addContent(AUDIO_WAV_MESSAGE,new MessageContentProperties("message","Voice message","en"));
        m.setSpokenNameOfSender(AUDIO_WAV_SPOKEN_NAME,SPOKEN_NAME_CONTENT_PROPERTIES);

        storeAndAssert(m);

    }

    protected abstract void storeAndAssert(IStorableMessage m) throws Exception;



}