/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ModuleCollection;

import java.io.File;
import java.io.InputStream;
import java.net.URI;

import org.dom4j.Document;

/**
 * The ApplicationCompilerImpl interface provides methods for compiling documents and applications
 * implemented in VoiceXML/CCXML into a format that is executable by the IApplicationExecutor
 * <p/>
 * The ApplicationCompilerImpl is implemented as a singleton
 */
public interface IApplicationCompiler {
    /**
     * Compiles an application defined in VoiceXML/CCXML into an executable format.
     * Each document found in the directory (and its sub directories) of the root application
     * will be compiled.
     * <p/>
     *
     * @param applicationURI The root document of the application
     * @return The application
     */
    public IApplication compileApplication(URI applicationURI);


    /**
     * Serialize the compiled application
     *
     * @param application
     * @return A serialized stream
     */
    public InputStream serialize(URI application); //TODO: should this be an URI?

    /**
     * Re-creates an executable application from a serialized file
     *
     * @param application a file containing the serserializedplication
     * @return The application
     */
    public IApplication deserialize(File application);

    /**
     * Compiles a document defined in VoiceXML/CCXML into an executable format.
     *
     * @param documentURI the uri of the document
     * @param collection
     * @return the module
     */
    Module compileDocument(URI documentURI, ModuleCollection collection);

    /**
     * Compiles a document defined in VoiceXML/CCXML into an executable format.
     *
     * @param doc parsed dom4j document object
     * @param collection
     * @return the module
     */
    Module compileDocument(Document doc, URI documentURI, ModuleCollection collection);
}
