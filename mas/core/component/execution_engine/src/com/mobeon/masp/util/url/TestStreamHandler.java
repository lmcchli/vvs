/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.url;

import com.mobeon.masp.util.test.MASTestSwitches;

import java.net.URLStreamHandler;
import java.net.URLConnection;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.File;

public class TestStreamHandler extends URLStreamHandler {
    protected URLConnection openConnection(URL u) throws IOException {
        String file=u.getPath();
        File f  = MASTestSwitches.currentMasDir();
        String path = f.getPath()+File.separator;
        URL newURL = toFileURL(path, file);
        return newURL.openConnection();
    }

    private static URL toFileURL(String path, String file) throws MalformedURLException {
        if("\\".equals(File.separator)) {
            path = path.replaceAll("\\\\","/");
        }
        return new URL("file:///"+path+file);
    }
}
