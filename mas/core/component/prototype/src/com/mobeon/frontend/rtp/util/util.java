/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.frontend.rtp.util;

import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.FileTypeDescriptor;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-jan-10
 * Time: 17:36:53
 * To change this template use File | Settings | File Templates.
 */
public class util {
    /**
     * Convert a file name to a content type.  The extension is parsed
     * to determine the content type.
     */
    public static ContentDescriptor fileExtToCD(String name) {
        String ext;
        int p;

        // Extract the file extension.
        if ((p = name.lastIndexOf('.')) < 0)
            return null;

        ext = (name.substring(p + 1)).toLowerCase();

        String type;

        // Use the MimeManager to get the mime type from the file extension.
        if ( ext.equals("mp3")) {
            type = FileTypeDescriptor.MPEG_AUDIO;
        } else {
            if ((type = com.sun.media.MimeManager.getMimeType(ext)) == null)
            return null;
            type = ContentDescriptor.mimeTypeToPackageName(type);
        }
        return new FileTypeDescriptor(type);
    }

}
