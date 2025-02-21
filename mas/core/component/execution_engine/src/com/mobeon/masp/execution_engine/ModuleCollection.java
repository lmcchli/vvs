/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;

public class ModuleCollection {
    private URI applicationURI;
    private URI baseURI;
    private String mimeType;
    private boolean root = false;
    private HashMap<URI, Module> moduleByURI = new HashMap<URI, Module>();
    private Module entry;

    public URI getApplicationURI() {
        return applicationURI;
    }

    public ModuleCollection(URI applicationURI, URI baseURI, String mimeType) {
        this.applicationURI = applicationURI;
        this.baseURI = baseURI;
        this.mimeType = mimeType;
    }

    public ModuleCollection(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isRoot() {
        return root;
    }

    public void setIsRoot() {
        root = true;
    }

    public void put(URI documentURI, Module module) {
        moduleByURI.put(documentURI, module);
    }

    public URI getBaseURI() {
        return baseURI;
    }

    public Module getEntry() {
        return entry;

    }

    public void setEntry(Module module) {
        entry = module;
    }

    public Module get(String src) {
        return moduleByURI.get(getBaseURI().resolve(src));
    }

    public Collection<Module> getModules() {
        return moduleByURI.values();
    }

    public void addModule(URI documentURI, Module m) {
        moduleByURI.put(documentURI, m);
    }
}
