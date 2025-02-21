/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.executor;
import com.mobeon.masp.util.component.SpringComponentManager;
import com.mobeon.masp.util.component.IComponentManager;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Test cases for the ExecutorServiceManager
 */
public class ExecutorServiceManagerTest extends MockObjectTestCase {
//    Mock execRoot;
    Mock exec1;
    Mock exec2;
    Mock exec3;
    Mock exec4;
    Mock exec5;


    /**
     * Setup a default set of ExecutorServices
     */
    public void setUp() {

 //       execRoot = mock(ExecutorService.class);
        exec1 =  mock(ExecutorService.class);
        exec2 =  mock(ExecutorService.class);
        exec3 =  mock(ExecutorService.class);
        exec4 =  mock(ExecutorService.class);
        exec5 =  mock(ExecutorService.class);

        ExecutorServiceManager.getInstance().clear();
//        ExecutorServiceManager.getInstance().addExecutor("root", (ExecutorService) execRoot.proxy());
        ExecutorServiceManager.getInstance().addExecutor("com.mobeon.masp.executionengine", (ExecutorService) exec1.proxy());
        ExecutorServiceManager.getInstance().addExecutor("com.mobeon", (ExecutorService) exec2.proxy());
        ExecutorServiceManager.getInstance().addExecutor("com.mobeon.masp.util", (ExecutorService) exec3.proxy());
        ExecutorServiceManager.getInstance().addExecutor("com.mobeon.masp.executionengine.compiler.CompileCCXML", (ExecutorService) exec4.proxy());
        ExecutorServiceManager.getInstance().addExecutor("com.mobeon.masp.executionengine.compiler.Compile", (ExecutorService) exec5.proxy());
    }

    /**
     * Try to find a ExecutorService for each of the supplied categories. Expect according to the mappings made in the
     * setUp() method.
     * @throws Exception
     */
    public void testGetExecutorService() throws Exception {
        Executor e = ExecutorServiceManager.getInstance().getExecutorService("com.mobeon.masp.executionengine.compiler.Compile");
        assertEquals(e, exec5.proxy());
        e = ExecutorServiceManager.getInstance().getExecutorService("com.mobeon.masp.util.Pool");
        assertEquals(e, exec3.proxy());
        e = ExecutorServiceManager.getInstance().getExecutorService("com.mobeon.masp.callmgr.Call");
        assertEquals(e, exec2.proxy());
        e = ExecutorServiceManager.getInstance().getExecutorService("java.lang.String");
        assertNotNull(e);
        e = ExecutorServiceManager.getInstance().getExecutorService("com.mobeon.masp.executionengine.executor.Traverser");
        assertEquals(e, exec1.proxy());
    }

    /**
     * Perform 100000 request to the ExecutorServiceManager within 500ms
     * TODO: What is a reasonable value to exepect? Does this TC provide any benefit?
     * @throws Exception
     */
    public void testGet100000Executors() throws Exception {
        long tStart, tStop;
        tStart = System.currentTimeMillis();
        Executor e = null;
        for (int i = 0; i < 20000; i++) {
            ExecutorServiceManager.getInstance().getExecutorService("com.mobeon.masp.ee.compiler.Compile");
            ExecutorServiceManager.getInstance().getExecutorService("com.mobeon.masp.util.Pool");
            ExecutorServiceManager.getInstance().getExecutorService("com.mobeon.masp.callmgr.Call");
            ExecutorServiceManager.getInstance().getExecutorService("java.lang.String");
            ExecutorServiceManager.getInstance().getExecutorService("com.mobeon.masp.ee.executor.Traverser");
        }
        tStop = System.currentTimeMillis();
        long elapsed = tStop - tStart;
        System.out.println("elapsed = " + elapsed);
        assertTrue(elapsed < 500);
    }

    public void testBeanConfiguration() {
        ExecutorServiceManager.getInstance().clear();
        String componentConfigXML = "test/TestComponentConfig.xml";
        IComponentManager compManager = null;
        ExecutorServiceManager esm = null;

        try {
            // Create our context
            FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(componentConfigXML);
            SpringComponentManager.initialApplicationContext(ctx);
            compManager = SpringComponentManager.getInstance();

            Executor rootExecutor = ExecutorServiceManager.getInstance().getExecutorService("com.mobeon.masp");
            assertNotNull(rootExecutor);
            assertTrue(rootExecutor instanceof ThreadPoolExecutorService);

            esm = (ExecutorServiceManager)compManager.create("ExecutorServiceManager");
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }

        // This key does not exist, ensuring that we will get an executor anyway.
        Executor undefinedExecutor = esm.getExecutorService("java.lang.String");
        assertNotNull(undefinedExecutor);
        assertTrue(undefinedExecutor instanceof ThreadPoolExecutorService);

        // This key exist as a sub key ("com.mobeon.masp")
        Executor mobeonExecutor = esm.getExecutorService("com.mobeon.masp.servicerequestmanager.diagnoseservice.DiagnoseServiceImpl");
        assertNotNull(mobeonExecutor);
        assertTrue(mobeonExecutor instanceof ThreadPoolExecutorService);
        assertNotSame(undefinedExecutor, mobeonExecutor);

        // This is key exist as a full key
        Executor anExecutor = esm.getExecutorService("com.mobeon.masp.mediatranslationmanager");
        assertNotNull(anExecutor);
        assertTrue(anExecutor instanceof ThreadPoolExecutorService);
        assertNotSame(mobeonExecutor, anExecutor);
        assertNotSame(undefinedExecutor, anExecutor);

        // This key exist as a sub key ("com.mobeon.masp.aPackage")
        Executor anyExecutor = esm.getExecutorService("com.mobeon.masp.mediatranslationmanager.any");
        assertEquals(anExecutor, anyExecutor);

        // Just ensuring that it works fine for singleton access to
        esm = ExecutorServiceManager.getInstance();
        Executor singletonExecutor = esm.getExecutorService("com.mobeon.masp.mediatranslationmanager");
        assertEquals(anExecutor, singletonExecutor);
    }

    public void testGetThreadPoolExecutorServices() throws Exception {
        ExecutorServiceManager esm = ExecutorServiceManager.getInstance();
        esm.clear();
        esm.addCachedThreadPool("root", 128, 128);
        esm.addCachedThreadPool("com.mobeon.masp", 3, 3);
        Executor e;
        for (int i = 0; i < 10; i++) {
            e = esm.getExecutorService("com.mobeon.masp");
            assertTrue(e instanceof ThreadPoolExecutorService);
            e.execute(new Runnable() {
                public void run(){
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }});
            e = esm.getExecutorService("you.will.never.find.this");
            assertTrue(e instanceof ThreadPoolExecutorService);
            e.execute(new Runnable() {
                public void run(){
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }});
        }
    }

//    public void testGlobalBeanConfiguration() {
//        ExecutorServiceManager.getInstance().clear();
//        String componentConfigXML = "etc/ComponentConfig.xml";
//
//        try {
//            // Create our context
//            FileSystemXmlApplicationContext ctx = new FileSystemXmlApplicationContext(componentConfigXML);
//            SpringComponentManager.initialApplicationContext(ctx);
//            SpringComponentManager.getInstance();
//
//            Executor rootExecutor = ExecutorServiceManager.getInstance().getExecutorService("com.mobeon.masp");
//            assertNotNull(rootExecutor);
//            assertTrue(rootExecutor instanceof ThreadPoolExecutorService);
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail();
//        }
//    }
}


