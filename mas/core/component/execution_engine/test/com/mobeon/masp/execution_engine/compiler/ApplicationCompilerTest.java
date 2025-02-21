/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.*;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.util.url.TestHandlerFactory;
import com.mobeon.masp.execution_engine.compiler.ApplicationCompilerImpl;
import com.mobeon.masp.execution_engine.ApplicationImpl;
import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.Case;
import com.mobeon.masp.execution_engine.ModuleCollection;
import com.mobeon.masp.execution_engine.runtime.RuntimeCase;

import java.net.URI;

/**
 * ApplicationCompilerImpl Tester.
 *
 * @author Mikael Andersson
 * @since <pre>09/22/2005</pre>
 */
public class ApplicationCompilerTest extends RuntimeCase {
    static ILogger logger = ILoggerFactory.getILogger(ApplicationCompilerTest.class);

    public ApplicationCompilerTest(String name) {
        super(name);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCompileApplication() throws Exception {
        try {
            TestHandlerFactory.initialize();
            URI uri = new URI("test:///applications/default.xml");
            ApplicationCompilerImpl applicationCompiler = new ApplicationCompilerImpl();
            applicationCompiler.setConfigurationManager(super.configurationManager);
            ApplicationImpl app = applicationCompiler.compileApplication(uri);
            logger.debug(app.getRoot().getEntry().getProduct().toMnemonic());
        } catch (Exception e) {
            die("Exception while parsing vxml doc:" + e.toString());
            throw e;
        }
    }

    public void testCompileDocumentVXML() throws Exception {
        try {
            TestHandlerFactory.initialize();
            URI uri = new URI("test:///vxml/hello_world.vxml");
            ApplicationCompilerImpl applicationCompiler = new ApplicationCompilerImpl();
            applicationCompiler.setConfigurationManager(super.configurationManager);
            Module mod = applicationCompiler.compileDocument(uri, new ModuleCollection(null,uri, "application/xml+vxm"));
            logger.debug(mod.getProduct().toMnemonic());
        } catch (Exception e) {
            die("Exception while parsing vxml doc:" + e.toString());
            throw e;
        }

    }

    public void testCompileDocument() throws Exception {
        try {
            TestHandlerFactory.initialize();
            ApplicationCompilerImpl appc = new ApplicationCompilerImpl();
            appc.setConfigurationManager(super.configurationManager);
            URI uri = new URI("test:///ccxml/hello_world.ccxml");
            Module compiledDoc = appc.compileDocument(uri, new ModuleCollection(null,uri, "application/xml+ccxml" ));
            if (compiledDoc == null) {
                die("compileDocument returned a null Module");
            }

            System.out.println(compiledDoc.getProduct().toMnemonic());
        } catch (Exception e) {
            die("Exception while parsing ccxmxl doc");
            throw e;
        }
    }

    public static Test suite() {
        return new TestSuite(ApplicationCompilerTest.class);
    }
}
