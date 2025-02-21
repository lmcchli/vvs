/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine;

import com.mobeon.masp.execution_engine.compiler.IApplication;
import com.mobeon.masp.execution_engine.components.ApplicationComponent;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.File;
import java.io.Serializable;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URI;

/**
 * @author David Looberger
 */
public class ApplicationImpl implements ApplicationComponent, Serializable {
    // TODO: user separate Module types when, and if those exist
    private static final long serialVersionUID = 0;

    transient static ILogger log = ILoggerFactory.getILogger(ApplicationImpl.class);
    private Map<String, List<ModuleCollection>> collectionByMime = new HashMap<String,List<ModuleCollection>>();
    private ModuleCollection root = null;
    private URI applicationURI;

    public ApplicationImpl(URI applicationURI) {
        this.applicationURI = applicationURI;
    }

    public ModuleCollection getRoot() {
        return root;
    }

    /**
     * @logs.error "Serialization of <URI> failed because of an I/O error <message>" - It was impossible to serialize <URI>, possibly due to full disk, or wrong file permissions, or similar I/O problems. <message> should give more information about the problem.
     * @return
     */
    public InputStream serialize() {
        try {
            PipedInputStream sink = new PipedInputStream();
            ObjectOutputStream oos = new ObjectOutputStream(new PipedOutputStream(sink));
            oos.writeObject(this);
            return sink;
        } catch (IOException e) {
            log.error("Serialization of "+getApplicationURI()+"failed because of an I/O error",e);
        }
        return null;
    }

    private URI getApplicationURI() {
        return applicationURI;
    }

    public static ApplicationImpl deserialize(File application) {
        // TODO: Not implemented yet
        return null;
    }

    public List<ModuleCollection> getModuleCollectionListByMimeType(String mimeType) {
        List<ModuleCollection> result = collectionByMime.get(mimeType);
        if(result == null) {
            result = new ArrayList<ModuleCollection>();
            collectionByMime.put(mimeType,result);
        }
        return result;
    }

    public void add(ModuleCollection collection) {
        if(collection.isRoot())
            root = collection;
        getModuleCollectionListByMimeType(collection.getMimeType()).add(collection);
    }
}
