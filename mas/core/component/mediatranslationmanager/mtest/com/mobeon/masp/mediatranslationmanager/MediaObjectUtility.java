/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.mediatranslationmanager;

import com.mobeon.masp.logging.ILogger;
import com.mobeon.masp.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.IMediaObject;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.LinkedList;

public class MediaObjectUtility {
    private static ILogger logger = ILoggerFactory.getILogger(MediaObjectUtility.class);
    private static MediaObjectUtility singletonInstance;
    private MediaObjectFactory mediaObjectFactory = null;

    public static MediaObjectUtility getInstance() {
        if (singletonInstance == null) singletonInstance = new MediaObjectUtility();
        return singletonInstance;
    }

    public void initialize() {
        mediaObjectFactory = new MediaObjectFactory();
    }

    public IMediaObject createMediaObject(String ssmlDocument) {
        // Transferring the speech to an array of direct ByteBuffers in
        // order to create a proper media object.
        List<ByteBuffer> textBufferList = new LinkedList<ByteBuffer>();
        ByteBuffer textBuffer = ByteBuffer.allocateDirect(ssmlDocument.length());
        textBuffer.put(ssmlDocument.getBytes());
        textBufferList.add(textBuffer);
        MediaTranslationFactory factory = MediaTranslationFactory.getInstance();
        // Creating a media object
        MediaProperties mediaProperties =
                new MediaProperties(factory.getSsmlMimeType());
        IMediaObject mediaObject =
                factory.getMediaObjectFactory().create(textBufferList, mediaProperties);
        return mediaObject;
    }

    public IMediaObjectFactory getMediaObjectFactory() {
        return mediaObjectFactory;
    }
}
