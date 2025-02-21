/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import com.mobeon.common.configuration.ConfigurationManagerImpl;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.mediaobject.ContentTypeMapperImpl;
import org.apache.log4j.xml.DOMConfigurator;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Testclass for {@link StackEventNotifier}.
 * 
 * @author J�rgen Terner
 */
public class StackEventNotifierTest extends MockObjectTestCase {

    protected Mock mEventDispatcher;
    protected StreamFactoryImpl mStreamFactory;


    /**
     * Creates the test.
     * 
     * @param name Name of this test.
     */
    public StackEventNotifierTest(String name)
    {
       super(name);
    }
    
    /* JavaDoc in base class. */
    public void setUp() {
        mEventDispatcher = mock(IEventDispatcher.class);
        mStreamFactory = new StreamFactoryImpl();
        try {
            mStreamFactory.setContentTypeMapper(new ContentTypeMapperImpl());
            ConfigurationManagerImpl cm = new ConfigurationManagerImpl();
            cm.setConfigFile("cfg/mas_stream.xml");
            mStreamFactory.setConfiguration(cm.getConfiguration());
            mStreamFactory.init();
        }
        catch (Exception e) {
            fail("Failed to initiate stream factory: " + e);
        }
    }

    /* JavaDoc in base class. */
    public void tearDown() {
    }
    
    /**
     * <pre>
     * Constructor test.
     * 
     * Wrong arguments
     * ---------------
     * Action: Pass null as argument.
     * Result: IllegalArgumentException.
     * 
     * Correct arguments
     * -----------------
     * Action: Pass a non-null dispatcher as argument
     * Result: No exception is thrown.
     * </pre>
     */
    public void testConstructor1() {
        // Wrong arguments
        try {
            new StackEventNotifier(null);
            fail("Wrong arguments should cause an exception.");
        }
        catch (Exception e) {
            JUnitUtil.assertException("Wrong arguments: " +
                    "Unexpected exception",
                    IllegalArgumentException.class, e);            
        }
        
        // Correct arguments
        try {
            new StackEventNotifier((IEventDispatcher)mEventDispatcher.proxy());
        }
        catch (Exception e) {
            fail("Correct arguments: Unexpected exception: " + e);
        }
    }
    
    /**
     * <pre>
     * Constructor test.
     * 
     * Wrong arguments
     * ---------------
     * Action: Arguments:
     *     1. null, 10
     *     2. null, 0
     *     3. non-null dispatcher, 0
     *     4. non-null dispatcher, -1
     * Result: IllegalArgumentException.
     * 
     * Correct arguments
     * -----------------
     * Action: Arguments: non-null dispatcher, 10
     * Result: No exception is thrown.
     * </pre>
     */
    public void testConstructor2() {
        final IEventDispatcher[] OBJARGS = new IEventDispatcher[] {
            null, 
            null, 
            (IEventDispatcher)mEventDispatcher.proxy(), 
            (IEventDispatcher)mEventDispatcher.proxy()
        };
        final long[] LONGARGS = new long[] {
            10, 0, 0, -1
        };
        for (int i = 0; i < OBJARGS.length; i++) {
            // Wrong arguments
            try {
                new StackEventNotifier(OBJARGS[i], LONGARGS[i]);
                fail("Wrong arguments " + i + " should cause an exception.");
            }
            catch (Exception e) {
                JUnitUtil.assertException("Wrong arguments " + i +
                        ": Unexpected exception",
                        IllegalArgumentException.class, e);            
            }
        }
        
        // Correct arguments
        try {
            new StackEventNotifier((IEventDispatcher)mEventDispatcher.proxy(), 10);
        }
        catch (Exception e) {
            fail("Correct arguments: Unexpected exception: " + e);
        }
    }

    /**
     * <pre>
     * Test for methods:
     *    playFinished
     *    recordFinished
     *    playFailed
     * 
     * Wrong arguments
     * ---------------
     * Condition: A StackEventNotifier instance is created.
     * Action: Call event method with callId = null as argument.
     * Result: IllegalArgumentException.
     * 
     * Correct arguments
     * -----------------
     * Condition: A StackEventNotifier instance is created.
     * Action: Call event method with a non-null callId argument.
     * Result: 
     *     No exceptions are thrown. The correct event is sent once to the 
     *     event dispatcher: 
     *     playFinished: PlayFinishedEvent
     *     recordFinished: RecordFinishedEvent
     *     playFailed: PlayFailedEvent
     * </pre>
     */
    public void testSendEvents() {
        
        final String[] METHOD_NAMES = new String[] {
            "playFinished", "recordFinished", 
            "playFailed", "playFailed", "recordFailed",
            "streamAbandoned", "recordStarted", "control"
        };
        final Class[] EVENT_TYPES = new Class[] {
                PlayFinishedEvent.class, RecordFinishedEvent.class,
                PlayFailedEvent.class, PlayFailedEvent.class,
                RecordFailedEvent.class, StreamAbandonedEvent.class,
                Event.class, ControlTokenEvent.class
            };

        final Class[][] METHOD_ARGS = new Class[][] {
            new Class[] {Object.class, PlayFinishedEvent.CAUSE.class, long.class},
            new Class[] {Object.class, int.class, String.class},
            new Class[] {Object.class, int.class, String.class},
            new Class[] {Object.class, int.class, String.class},
            new Class[] {Object.class, int.class, String.class},
            new Class[] {IMediaStream.class},
            new Class[] {Object.class},
            new Class[] {int.class, int.class, int.class}
        };        
        final Object[][] WRONG_ARGUMENTS = new Object[][] {
            new Object[] {null, PlayFinishedEvent.CAUSE.PLAY_FINISHED, 0}, // XXX more cases now...
            new Object[] {null, 0, null},
            new Object[] {null, -1, null},
            new Object[] {null, -1, "Test"},
            new Object[] {null, -1, "Test"},
            new Object[] {null},
            new Object[] {null},
            null
        };
        final Object[][] CORRECT_ARGUMENTS = new Object[][] {
            new Object[] {new Object(), PlayFinishedEvent.CAUSE.PLAY_FINISHED, 0}, 
            new Object[] {new Object(), 0, null},
            new Object[] {new Object(), 0, null},
            new Object[] {new Object(), 0, "Test"},
            new Object[] {new Object(), 0, "Test"},
            new Object[] {mStreamFactory.getOutboundMediaStream()},
            new Object[] {new Object()},
            new Integer[] {0, 10, 10}
        };

        for (int i = 0; i < METHOD_NAMES.length; i++) {
            // Wrong arguments
            try {
                if (WRONG_ARGUMENTS[i] != null) {
                    StackEventNotifier notifier =
                        new StackEventNotifier((IEventDispatcher)mEventDispatcher.proxy());
                    Method m = 
                        StackEventNotifier.class.getMethod(METHOD_NAMES[i], METHOD_ARGS[i]);
                    m.invoke(notifier, WRONG_ARGUMENTS[i]);
                    fail("Wrong arguments " + i + " should cause an exception.");
                }
            }
            catch (InvocationTargetException e) {
                JUnitUtil.assertException("Wrong arguments " + i + 
                        ": Unexpected exception.",
                        IllegalArgumentException.class, (Exception)e.getCause());                
            }
            catch (Exception e) {
                fail("Wrong arguments " + i + ": Unexpected exception: " + e);
            }

            tearDown();
            setUp();
            
            
            // Correct arguments
            try {
                StackEventNotifier notifier =
                    new StackEventNotifier((IEventDispatcher)mEventDispatcher.proxy());
                mEventDispatcher.expects(once()).method("fireEvent").with(isA(EVENT_TYPES[i]));
                Method m = 
                    StackEventNotifier.class.getMethod(METHOD_NAMES[i], METHOD_ARGS[i]);
                m.invoke(notifier, CORRECT_ARGUMENTS[i]);
            }
            catch (InvocationTargetException e) {
                fail("Correct arguments " + i + ": Unexpected exception: " + 
                    e.getCause());
            }
            catch (Exception e) {
                fail("Correct arguments " + i + ": Unexpected exception: " + e);
            }

            tearDown();
            setUp();
        }
    }

    /**
     * <pre>
     * Basic tests for methods: 
     *    returnFromCall
     *    waitForCallToFinish
     *    initCall
     *    abortCall
     * 
     * Note that this test-method only tests each method by itself and
     * not the logical relationships between the methods.
     * 
     * Wrong arguments
     * ---------------
     * Condition: A StackEventNotifier instance is created.
     * Action: Pass null as argument.
     * Result: IllegalArgumentException.
     * 
     * Correct arguments
     * -----------------
     * Condition: A StackEventNotifier instance is created.
     * Action: Pass a non-null argument
     * Result: 
     *     No exception is thrown. The notifier contains the correct 
     *     number of stored conditions:
     *     returnFromCall: 0 conditions
     *     waitForCallToFinish: 0 conditions
     *     initCall: 1 conditions
     *     abortCall: 0 conditions
     * </pre>
     */
    public void testStateMethods() {
        final String[] METHOD_NAMES = new String[] {
                "returnFromCall", "waitForCallToFinish", 
                "initCall", "abortCall"
        };
        
        final int[] NR_OF_CONDITIONS = new int[] {
            0, 0, 1, 0
        };

        for (int i = 0; i < METHOD_NAMES.length; i++) {
            // Wrong arguments
            try {
                StackEventNotifier notifier =
                    new StackEventNotifier((IEventDispatcher)mEventDispatcher.proxy());
                Method m = StackEventNotifier.class.getMethod(METHOD_NAMES[i], 
                        new Class[] {Object.class});
                m.invoke(notifier, new Object[] {null});
                fail("Wrong arguments " + i + " should cause an exception.");
            } catch (InvocationTargetException e) {
                JUnitUtil.assertException("Wrong arguments " + i + 
                        ": Unexpected exception.",
                        IllegalArgumentException.class, (Exception)e.getCause());                
            }
            catch (Exception e) {
                fail("Wrong arguments " + i + ": Unexpected exception: " + e);
            }
            
            tearDown();
            setUp();
            
            // Correct arguments
            try {
                StackEventNotifier notifier =
                    new StackEventNotifier((IEventDispatcher)mEventDispatcher.proxy());
                Method m = StackEventNotifier.class.getMethod(METHOD_NAMES[i], 
                        new Class[] {Object.class});
                m.invoke(notifier, new Object[] {new Object()});
                assertEquals("Correct arguments " + i, 
                        NR_OF_CONDITIONS[i], notifier.getNumberOfConditions());
            }
            catch (InvocationTargetException e) {
                fail("Correct arguments " + i + ": Unexpected exception: " + 
                    e.getCause());
            }
            catch (Exception e) {
                fail("Correct arguments " + i + ": Unexpected exception: " + e);
            }
            
            tearDown();
            setUp();
        }
    }
    
    /**
     * <pre>
     * Tests the relationsships between the methods:
     *    returnFromCall
     *    waitForCallToFinish
     *    initCall
     *    abortCall
     * 
     * Abort and return
     * ----------------
     * Description
     *     A call to init should increase the number of conditions if
     *     the callId did not aready exists. A call to abort or returnFromCall
     *     should remove the condition for the given callId.
     * Condition: A StackEventNotifier instance is created.
     * Action: The following steps in sequence:
     *     1. initCall(callId1)
     *     2. initCall(callId1)
     *     3. initCall(callId2)
     *     4. abortCall(callId2)
     *     5. abortCall(callId2)
     *     6. returnFromCall(callId2)
     *     7. returnFromCall(callId1)
     * Result: Number of stored conditions after step:
     *     1. 1
     *     2. 2
     *     3. 2
     *     4. 1
     *     5. 1
     *     6. 1
     *     7. 0
     * 
     * Timeout during wait
     * -------------------
     * Condition: A StackEventNotifier instance is created with
     *                   a timeout = 2 second.
     * Action: Call initCall(id) followed by waitForCallToFinish(id).
     * Result: waitForCallToFinish returns false.
     * 
     * Wait and return
     * ---------------
     * Condition: A StackEventNotifier instance is created with
     *                   a timeout = 60 second.
     * Action: 
     *     Call initCall(id), start a separate thread that calls
     *     waitForCallToFinish(id), from the main thread, wait 1 sec
     *     and call returnFromCall(id).
     * Result: waitForCallToFinish returns true.
     * </pre>
     */
    public void testWaitAndSignal() {
        // Abort and return
        StackEventNotifier notifier =
            new StackEventNotifier((IEventDispatcher)mEventDispatcher.proxy());
        final String[] METHOD_NAMES = new String[] {
                "initCall", "initCall", "initCall", 
                "abortCall", "abortCall",
                "returnFromCall", "returnFromCall"
        };
        final Object CALLID1 = new Object();
        final Object CALLID2 = new Object();
        final Object[] CALLIDS = new Object[] {
                CALLID1, CALLID1, CALLID2, 
                CALLID2, CALLID2, 
                CALLID2, CALLID1
        };
        final int[] NR_OF_CONDITIONS = new int[] {
                1, 1, 2, 
                1, 1, 
                1, 0
        };
        try {
            for (int i = 0; i < METHOD_NAMES.length; i++) {
                Method m = StackEventNotifier.class.getMethod(METHOD_NAMES[i], 
                        new Class[] {Object.class});
                m.invoke(notifier, new Object[] {CALLIDS[i]});
                assertEquals("Unexpected number of conditions after step " + i,
                        NR_OF_CONDITIONS[i], notifier.getNumberOfConditions());
            }
        }
        catch (InvocationTargetException e) {
            fail("Unexpected exception: " + e.getCause());
        }
        catch (Exception e) {
            fail("Unexpected exception: " + e);
        }
    
        // Timeout during wait
        try {
            notifier =
                new StackEventNotifier((IEventDispatcher)mEventDispatcher.proxy(),
                        2);
            Object callId = new Object();
            notifier.initCall(callId);
            assertFalse("Timeout during wait: A timeout should have occured.",
                    notifier.waitForCallToFinish(callId));
        }
        catch (Exception e) {
            fail("Timeout during wait: Unexpected exception: " + e);
        }

        // Wait and return
        try {
            notifier =
                new StackEventNotifier((IEventDispatcher)mEventDispatcher.proxy(),
                        60);
            Object callId = new Object();
            notifier.initCall(callId);
            Runnable r = new WaitForCallToReturn(notifier, callId, 
                    "Wait and return: A timeout should not have occured.");
            Thread t = new Thread(r);
            t.start();
            Thread.sleep(1000);
            notifier.returnFromCall(callId);
        }
        catch (InterruptedException e) {
            fail("InterruptedException during wait: " + e);
        }
        catch (Exception e) {
            fail("Wait and return: Unexpected exception: " + e);
        }
    }
    
    /**
     * Helper class used to wait asyncronously for a call to return.
     */
    private class WaitForCallToReturn implements Runnable {
        private StackEventNotifier notifier;
        private Object callId;
        private String message;
        
        public WaitForCallToReturn(StackEventNotifier notifier, Object callId,
                String message) {
            this.notifier = notifier;
            this.callId = callId;
            this.message = message;
        }
        public void run() {
            assertTrue(message, notifier.waitForCallToFinish(callId));
        }
    }
}
