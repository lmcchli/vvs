/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine;

import com.mobeon.masp.callmanager.CallManager;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.components.ApplicationExecutionComponent;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Mikael Andersson
 */
public class ApplicationExecutionTest extends Case {
    private IMediaObjectFactory mediaObjectFactory;
    private Mock mockMediaObjectFactory;
    private CallManager callManager;
    private Mock mockCallManager;

    public ApplicationExecutionTest(String event) {
        super(event);
    }

    public static Test suite() {
        return new TestSuite(ApplicationExecutionTest.class);
    }

    public void testStart() throws Exception {
        setupMediaObjectFactory();
        setupCallManager();

        ApplicationExecutionImpl applicationExecution = setupApplicationExecution();
        try {
            applicationExecution.start();
        } catch (Throwable t) {
            die("start() threw an unexpected exception " + t.getMessage());
            t.printStackTrace();
        }
        if (!applicationExecution.createRuntimes())
            die("Runtimes are not created even though preconditions should be satisfied");
    }

    private ApplicationExecutionImpl setupApplicationExecution() throws URISyntaxException {
        ApplicationImpl application = new ApplicationImpl(new URI("test:/ccxml/hello_world.xml"));
        ModuleCollection collection = new ModuleCollection(Constants.MimeType.CCXML_MIMETYPE);
        Module module = new Module(new URI("test:/ccxml/hello_world.ccxml"));
        collection.setEntry(module);
        collection.put(new URI("test:/ccxml/hello_world.ccxml"), module);
        collection.setIsRoot();
        application.add(collection);


        SessionFactory sessionFactory = new SessionFactory();
        ApplicationExecutionImpl applicationExecution =
            new ApplicationExecutionImpl("hello_world", ApplicationExecutionComponent.Context.testInstance(),application, null, null, null);
        applicationExecution.setSession(sessionFactory.create());

        applicationExecution.getContext().setSessionFactory(sessionFactory);
        if (applicationExecution.createRuntimes())
            die("Insufficient preconditions, createRuntimes should not succeed");
        applicationExecution.getContext().setMediaObjectFactory(mediaObjectFactory);


        return applicationExecution;
    }

    private void setupCallManager() {
        mockCallManager = new Mock(CallManager.class);
        callManager = (CallManager) mockCallManager.proxy();
    }

    private void setupMediaObjectFactory() {
        mockMediaObjectFactory = new Mock(IMediaObjectFactory.class);
        mediaObjectFactory = (IMediaObjectFactory) mockMediaObjectFactory.proxy();
    }

    public void testGetSession() throws Exception {
        ApplicationExecutionImpl applicationExecution =
            new ApplicationExecutionImpl("", ApplicationExecutionComponent.Context.testInstance(),null, null, null, null);
        SessionFactory sessionFactory = new SessionFactory();
        applicationExecution.getContext().setSessionFactory(sessionFactory);
        applicationExecution.setSession(sessionFactory.create());
        ISession session = applicationExecution.getSession();
        if (session == null)
            die("getSession should never return null !");
        ISession nextCall = applicationExecution.getSession();
        if (nextCall != session)
            die("Consecutive calls to getSession should return the same object");
    }
}