/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess.util;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Date: 2005-dec-21
 *
 * @author ermmaha
 */
public class MediaUtil {
    private static ILogger log = ILoggerFactory.getILogger(MediaUtil.class);
    private static String PROPERTIES_FILE_NAME = "properties.cfg";

    public static String convertMediaObjectToString(IMediaObject iMediaObject) throws IOException {
        InputStream inputStream = iMediaObject.getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        int ch;
        byte[] tmp = new byte[1024]; //Read/write in batches to minimize systemcalls
        while ((ch = inputStream.read(tmp)) != -1) {
            os.write(tmp, 0, ch);
        }
        return os.toString("UTF-8");
    }

    /**
     * Loads a Map of mediapath strings keyed by a language string.
     *
     * @param baseURI
     * @return the Map
     */
    public static Map<String, String> getMediaPathMap(String baseURI) {
        HashMap<String, String> pathMap = new HashMap<String, String>();

        int pos = baseURI.indexOf("applications");
        String mediaPackagesPath = baseURI.substring(5, pos) + "applications/mediacontentpackages/";

        File dir = new File(mediaPackagesPath);
        if (dir.isDirectory()) {
            File[] filez = dir.listFiles(new ContentDirectoryFileFilter());
            for (int i = 0; i < filez.length; i++) {
                readPropFile(filez[i], pathMap, mediaPackagesPath);
            }
        } else {
            log.debug("mediaPackagesPath is not a directory");
        }

        return pathMap;
    }

    private static void readPropFile(File file, HashMap<String, String> pathMap, String mediaPackagesPath) {
        File propFile = new File(file, PROPERTIES_FILE_NAME);

        if (propFile.isFile()) {
            Properties prop = new Properties();
            FileInputStream in = null;
            try {
                in = new FileInputStream(propFile);
                prop.load(in);

                if (log.isDebugEnabled()) {
                    log.debug("Propfile read: " + prop);
                }

                pathMap.put(prop.getProperty("lang"), getMediaPath(mediaPackagesPath, prop.getProperty("productid")));

            } catch (FileNotFoundException e) {
                log.error("Exception in readPropFile " + e);
            } catch (IOException e) {
                log.error("Exception in readPropFile " + e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.error("Exception in readPropFile " + e);
                    }
                }
            }
        }
    }

    private static String getMediaPath(String mediaPackagesPath, String packageId) {
        return mediaPackagesPath + packageId + "/";
    }

    static class ContentDirectoryFileFilter implements FileFilter {
        public boolean accept(File pathname) {
            return pathname.getName().startsWith("mcp") && pathname.isDirectory();
        }
    }
}



