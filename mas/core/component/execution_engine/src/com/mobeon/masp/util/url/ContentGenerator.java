package com.mobeon.masp.util.url;

import java.io.InputStream;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: QMIAN
 * Date: 2007-jan-11
 * Time: 13:38:45
 * To change this template use File | Settings | File Templates.
 */
public abstract class ContentGenerator {
    private URL url;
    public abstract InputStream getInputStream();

    public ContentGenerator(URL url) {
        this.url = url;
    }


    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

}
