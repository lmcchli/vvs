package com.mobeon.masp.stream.jni;

import org.jmock.MockObjectTestCase;
import com.mobeon.common.configuration.*;
import com.mobeon.masp.stream.StreamConfiguration;

import javax.naming.ConfigurationException;

public class NativeStreamHandlingTest extends MockObjectTestCase {

    public void setUp() throws UnknownGroupException, GroupCardinalityException, ConfigurationException, ParameterTypeException, ConfigurationLoadException, MissingConfigurationFileException {
        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
        cm.setConfigFile("cfg/mas_stream.xml");
        StreamConfiguration.getInstance().setInitialConfiguration(cm.getConfiguration());
        StreamConfiguration.getInstance().update();
    }

    public void testCallback() throws InterruptedException {
        CallbackReceiver callbackReceiver = new CallbackReceiver();
        System.loadLibrary("ccrtpadapter");
        NativeStreamHandling.initialize();
        Object callSessionId = new Object();
        CallbackDispatcher dispatcher = CallbackDispatcher.getSingleton();
        int requestId;

        dispatcher.setRequestIdCounter(0x000f);
        requestId = dispatcher.addRequest(callSessionId, callbackReceiver);
        NativeStreamHandling.postDummyCallback(0, requestId);
        pend(callbackReceiver, 10);
        assertNotNull(callbackReceiver.callback);
        assertEquals(requestId, callbackReceiver.callback.requestId);
        assertEquals(0xf, callbackReceiver.callback.command);
        assertEquals(0x123, callbackReceiver.callback.status);
        assertEquals(4711, callbackReceiver.callback.data);

        dispatcher.setRequestIdCounter(0x00ff);
        requestId = dispatcher.addRequest(callSessionId, callbackReceiver);
        NativeStreamHandling.postDummyCallback(0, requestId);
        pend(callbackReceiver, 10);
        assertNotNull(callbackReceiver.callback);
        assertEquals(requestId, callbackReceiver.callback.requestId);
        assertEquals(0xf, callbackReceiver.callback.command);
        assertEquals(0x123, callbackReceiver.callback.status);
        assertEquals(4711, callbackReceiver.callback.data);

        dispatcher.setRequestIdCounter(0x0fff);
        requestId = dispatcher.addRequest(callSessionId, callbackReceiver);
        NativeStreamHandling.postDummyCallback(0, requestId);
        pend(callbackReceiver, 10);
        assertNotNull(callbackReceiver.callback);
        assertEquals(requestId, callbackReceiver.callback.requestId);
        assertEquals(0xf, callbackReceiver.callback.command);
        assertEquals(0x123, callbackReceiver.callback.status);
        assertEquals(4711, callbackReceiver.callback.data);

        dispatcher.setRequestIdCounter(0xcfff);
        requestId = dispatcher.addRequest(callSessionId, callbackReceiver);
        NativeStreamHandling.postDummyCallback(0, requestId);
        pend(callbackReceiver, 10);
        assertNotNull(callbackReceiver.callback);
        assertEquals(requestId, callbackReceiver.callback.requestId);
        assertEquals(0xf, callbackReceiver.callback.command);
        assertEquals(0x123, callbackReceiver.callback.status);
        assertEquals(4711, callbackReceiver.callback.data);
     }

    class CallbackReceiver implements AbstractCallbackReceiver {
        boolean called = false;
        Callback callback = null;

        public void notify(Object requestId, Callback callback) {
            called = true;
            this.callback = callback;
        }
    }

    void pend(CallbackReceiver callbackReceiver, int maxPoll) {
        int pollCount = 0;
        while (!callbackReceiver.called && pollCount < maxPoll) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            pollCount++;
        }
        if (callbackReceiver.called) callbackReceiver.called = false;
    }
}
