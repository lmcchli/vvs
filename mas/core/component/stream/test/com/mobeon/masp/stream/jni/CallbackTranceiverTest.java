package com.mobeon.masp.stream.jni;

import org.jmock.MockObjectTestCase;
import org.jmock.Mock;
import org.jmock.core.Invocation;
import org.jmock.core.Stub;
import com.mobeon.masp.stream.StreamConfiguration;
import com.mobeon.common.configuration.*;

import javax.naming.ConfigurationException;

public class CallbackTranceiverTest extends MockObjectTestCase {
    Mock streamHandlingMock = mock(AbstractStreamHandling.class);
    Mock callbackReceiver = mock(AbstractCallbackReceiver.class);

    public void setUp() throws UnknownGroupException, GroupCardinalityException, ConfigurationException, ParameterTypeException, ConfigurationLoadException, MissingConfigurationFileException {
        ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
        cm.setConfigFile("cfg/mas_stream.xml");
        StreamConfiguration.getInstance().setInitialConfiguration(cm.getConfiguration());
        StreamConfiguration.getInstance().update();
    }

    public void testDispatching() throws InterruptedException {
        // Setting up mocks and stubs
        // First we are mocking event retrieval: a number of play finish
        Callback cb1 = new Callback(0, 1, 200);
        Callback cb2 = new Callback(1, 1, 200);
        Callback cb3 = new Callback(2, 1, 200);
        Callback cb4 = new Callback(0, 0, 0);
        streamHandlingMock.expects(atLeastOnce())
                .method("getCallback")
                .with(eq(47))
                .will(onConsecutiveCalls(
                        returnValue(cb1),
                        returnValue(cb2),
                        returnValue(cb3),
                        returnValue(cb4)));
        // In order to verify that play finish is issued properly
        // they are counted through a custom Stub.
        InvocationCounter invocations = new InvocationCounter();
        Object requestId = new Object();
        callbackReceiver.expects(once()).method("notify")
                .with(eq(requestId), eq(cb1)).will(invocations);
        callbackReceiver.expects(once()).method("notify")
                .with(eq(requestId), eq(cb2)).will(invocations);
        callbackReceiver.expects(once()).method("notify")
                .with(eq(requestId), eq(cb3)).will(invocations);
        // Mocking three requests
        cb1.requestId = CallbackDispatcher.getSingleton().addRequest(requestId, (AbstractCallbackReceiver)callbackReceiver.proxy());
        cb2.requestId = CallbackDispatcher.getSingleton().addRequest(requestId, (AbstractCallbackReceiver)callbackReceiver.proxy());
        cb3.requestId = CallbackDispatcher.getSingleton().addRequest(requestId, (AbstractCallbackReceiver)callbackReceiver.proxy());
        // Starting a dispatcher and awaiting its termination
        CallbackTranceiver dispatcher = new CallbackTranceiver((AbstractStreamHandling)streamHandlingMock.proxy(), 47);
        Thread.yield();
        int pollCount = 0;
        int maxPoll = 100;
        while (dispatcher.isRunning() && pollCount < maxPoll) {
            Thread.sleep(100);
            pollCount++;
        }
        assertFalse(pollCount == maxPoll);
        // Verifying the number of play finished
        assertEquals(3, invocations.counter);
    }
}
