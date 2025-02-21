/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine;

import junit.framework.*;

import com.mobeon.masp.execution_engine.ApplicationImpl;
import com.mobeon.masp.execution_engine.compiler.Constants;

import java.util.List;
import java.net.URI;
import java.io.InputStream;

/**
 * @author Mikael Andersson
 */
public class ApplicationTest extends Case {

    public static Test suite() {
        return new TestSuite(ApplicationTest.class);
    }

    public ApplicationTest(String event) {
        super(event);
    }

    /**
     * Validates that serilize() returns a valid stream and
     * that we can read it.
     *
     * @throws Exception
     */
    public void testSerialize() throws Exception {
        ApplicationImpl app = new ApplicationImpl(new URI("file:///"));
        InputStream is = app.serialize();
        if(is == null)
            die("Serialize should never return null");
        int i = 0;
        byte[] buf = new byte[128];
        while(is.available() != 0 && (i= is.read(buf))  > 0) {
            log.debug("Read "+i+" bytes:"+ new String(buf,0,i,"ISO-8859-1").replaceAll("[\\x00-\\x19]","."));
        }
    }

    /**
     * Validates that adding and retriving module collections as
     * expected. Adding a collection with add makes it available
     * through getModuleCollectionListByMimeType.
     *
     * @throws Exception
     */
    public void testGetModuleCollectionListByMimeType() throws Exception {
        ApplicationImpl app = new ApplicationImpl(new URI("file:///"));

        ModuleCollection collection = new ModuleCollection(null,new URI("file:///"), Constants.MimeType.CCXML_MIMETYPE);
        app.add(collection);

        List<ModuleCollection> actual = app.getModuleCollectionListByMimeType(Constants.MimeType.CCXML_MIMETYPE);
        if(actual == null)
            die("getModuleCollectionListByMimeType should never return null");
        if(actual.size() != 1)
            die("Expected one element in list, got: "+actual.size());
        if(actual.get(0) != collection)
            die("Element in list wasn't the collection we added !");
    }
}