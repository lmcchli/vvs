/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject.jaf;

import com.mobeon.masp.mediaobject.IMediaObject;

import jakarta.activation.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Adapts a Media object to {@link jakarta.activation.DataSource}
 * @author QHAST
 */
public class DataSourceAdapter implements DataSource {


    /**
     * Adapted object.
     */
    private IMediaObject mediaObject;

    /**
     * The name of this object where the name of the object is dependant on the
     * nature of the underlying object. (i.e. file name)
     */
    private String name;

    /**
     * Contructs with media object to be adapted and a name.
     * Null values not allowed.
     * @param mediaObject adapted media object.
     * @param name data source name.
     */
    public DataSourceAdapter(IMediaObject mediaObject, String name) {
        if(mediaObject == null)
            throw new IllegalArgumentException("mediaObject cannot be null!");
        if(name == null)
            throw new IllegalArgumentException("name cannot be null!");
        this.mediaObject = mediaObject;
        this.name = name;
    }


    /**
     * This method returns the MIME type of the data in the form of a string.
     * The method delegates the call to {@link com.mobeon.masp.mediaobject.MediaProperties#getContentType()}
     * @return the content MIME Type as a String
     */
    public String getContentType() {
        return mediaObject.getMediaProperties().getContentType().toString();
    }

    /**
     * This method returns an InputStream representing the the content data.
     * The method delegates the call to {@link com.mobeon.masp.mediaobject.IMediaObject#getInputStream()}
     * @return an InputStream
     * @throws java.io.IOException
     */
    public InputStream getInputStream() throws IOException {
        return mediaObject.getInputStream();
    }

    /**
     * Return the name of this object where the name of the object is dependant on the nature
     * of the underlying objects.
     * @return the name of the object.
     */
    public String getName() {
        return name;
    }

    /**
     * Not supported.
     * @return
     * @throws java.io.IOException
     * @throws UnsupportedOperationException everytime.
     */
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException("getOutputStream() not supported!");
    }

}
