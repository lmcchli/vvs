package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.mediacontentmanager.IMediaQualifierFactory;
import com.mobeon.masp.mediacontentmanager.IMediaQualifier;
import com.mobeon.masp.mediacontentmanager.MediaQualifierException;
import com.mobeon.masp.mediaobject.IMediaObject;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Feb 12, 2006
 * Time: 4:22:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class MediaQualifierFactoryMock implements IMediaQualifierFactory {
    public IMediaQualifier create(IMediaQualifier.QualiferType type, String name, String value, IMediaQualifier.Gender gender) throws MediaQualifierException {
        return new MediaQualifierMock("a_mediaqualifer");
    }

    public IMediaQualifier create(String name, IMediaObject mediaObject, IMediaQualifier.Gender gender) {
        return new MediaQualifierMock("a_mediaqualifer");
    }
}
