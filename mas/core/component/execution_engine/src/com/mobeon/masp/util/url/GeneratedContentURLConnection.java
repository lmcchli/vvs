package com.mobeon.masp.util.url;

import java.net.URLConnection;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: QMIAN
 * Date: 2007-jan-11
 * Time: 13:36:25
 * To change this template use File | Settings | File Templates.
 */
public class GeneratedContentURLConnection extends URLConnection {
    private ContentGenerator contentGenerator;

    public GeneratedContentURLConnection(ContentGenerator contentGenerator) {
        super(contentGenerator.getUrl());
        this.contentGenerator = contentGenerator;
    }

    public InputStream getInputStream() throws IOException {
        return contentGenerator.getInputStream();
    }

    public void connect() throws IOException {
    }
}
