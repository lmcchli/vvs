package com.mobeon.masp.stream.jni;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import com.mobeon.common.configuration.*;
import com.mobeon.masp.stream.StreamConfiguration;

import javax.naming.ConfigurationException;
import java.util.Vector;

public class CallbackDispatcherTest extends MockObjectTestCase {
    Mock callbackReceiver = mock(AbstractCallbackReceiver.class);
    Mock streamHandlingMock = mock(AbstractStreamHandling.class);
    AbstractStreamHandling streamHandling;

    public void setUp() {
       streamHandling = (AbstractStreamHandling)streamHandlingMock.proxy();
    }

    public void testSingleton() {
        assertNotNull(CallbackDispatcher.getSingleton());
    }

    public void testInitialization() {
        streamHandlingMock.expects(atLeastOnce())
                .method("getCallback")
                .will(returnValue(0));
        CallbackDispatcher.getSingleton().initialize(streamHandling, 7);
        Thread.yield();
        assertEquals(7, CallbackDispatcher.getSingleton().getDispatchQueueCount());
    }

    public void testConfiguration() throws UnknownGroupException, GroupCardinalityException, ConfigurationException, ParameterTypeException, ConfigurationLoadException, MissingConfigurationFileException {
        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
        cm.setConfigFile("cfg/mas_stream.xml");
        StreamConfiguration.getInstance().setInitialConfiguration(cm.getConfiguration());
        StreamConfiguration.getInstance().update();

        StreamConfiguration configuration = StreamConfiguration.getInstance();

        assertEquals(1, configuration.getOutputProcessors());
        assertEquals(1, configuration.getInputProcessors());
    }

    public void testRequestIdWrap() {
        final int nOfWraps = 3;
        final int idMax = 0xffff;
        final int nOfCalls = idMax*nOfWraps+1;
        int wrapCount = 0;
        Object callSessionId = new Object();

        for (int counter = 0; counter < nOfCalls; counter++) {
            int requestId = CallbackDispatcher.getSingleton().addRequest(callSessionId,
                    (AbstractCallbackReceiver)callbackReceiver.proxy());
            assertTrue(requestId >= 0);
            assertTrue(requestId <= idMax);
            if (requestId == 0) wrapCount++;
        }
        assertEquals(nOfWraps, wrapCount);
    }

    public void testConcurrency() throws InterruptedException {
        Vector<CallbackRequester> crv = new Vector<CallbackRequester>();
        for (int i = 0; i < 10; i++) {
            CallbackRequester cr = new CallbackRequester(i, 100);
            cr.start();
            crv.add(cr);
        }
        Thread.sleep(1000);
        for (CallbackRequester cr : crv) {
            for (CallbackReceiver r : cr.crv) {
                assert(r.notified);
            }
        }
    }

    class CallbackRequester extends Thread {
        int id;
        int iterations;
        Vector<CallbackReceiver> crv = new Vector<CallbackReceiver>();

        public CallbackRequester(int id, int iterations) {
            this.id = id;
            this.iterations = iterations;
        }

        public void run() {
            for (int i = 0; i < iterations; i++)  {
                CallbackReceiver cr = new CallbackReceiver();
                crv.add(cr);
                cr.requestId = CallbackDispatcher.getSingleton().addRequest(null, cr);
                System.out.println("#" + cr.requestId);
                sleep();
            }
            for (CallbackReceiver cr : crv)  {
                Callback cb = new Callback(cr.requestId, -1, -1);
                CallbackDispatcher.getSingleton().notifyCaller(cb);
                sleep();
            }
        }

        void sleep() {
            try {
                Thread.sleep(id);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    class CallbackReceiver implements AbstractCallbackReceiver {
        int requestId = -1;
        int callbackId = -1;
        boolean notified = false;

        public void notify(Object requestId, Callback callback) {
            this.callbackId = callback.requestId;
            notified = true;
        }
    }
}
