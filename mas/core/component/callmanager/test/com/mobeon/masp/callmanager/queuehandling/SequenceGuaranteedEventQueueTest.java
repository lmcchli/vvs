package com.mobeon.masp.callmanager.queuehandling;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mobeon.masp.callmanager.CallManagerTestContants;
import com.mobeon.masp.callmanager.events.EventObject;
import com.mobeon.masp.util.executor.ExecutorServiceManager;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * SequenceGuaranteedEventQueue Tester.
 *
 * @author Malin Flodin
 */
public class SequenceGuaranteedEventQueueTest extends TestCase
        implements CommandExecutor
{
    static AtomicInteger receivedSequenceNumber = new AtomicInteger(1);
    QueueWithSequenceCheck eventQueue = new QueueWithSequenceCheck(this);
    private final int amountOfEvents = 250;

    AtomicBoolean hasFailed = new AtomicBoolean(false);
    AtomicReference<String> errorMessage = new AtomicReference<String>("");

    public SequenceGuaranteedEventQueueTest(String name) {
        super(name);

        ILoggerFactory.configureAndWatch(CallManagerTestContants.MOBEON_LOG_XML);
        ExecutorServiceManager.getInstance().
                addCachedThreadPool("TestSequenceGuaranteedEventQueue",
                        10, 20);
    }

    public void setUp() throws Exception {
        super.setUp();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Verifies that the events queued by different threads in an
     * SequenceGuaranteedEventQueue are handled one at a time and in the same
     * order as they arrived in the queue. 250 events are processed.
     *
     * When the single-threaded, sequence guarantee is removed from the
     * SequenceGuaranteedEventQueue class, this test case will fail.
     *
     * @throws Exception if the test case failed.
     */
    public void testQueue() throws Exception {
        for (int i = 1; i <= amountOfEvents; i++) {
            terminateIfFailure();

            ExecutorServiceManager.getInstance().
                    getExecutorService("TestSequenceGuaranteedEventQueue").
                    execute(
                    new Runnable()
                    {
                        public void run()
                        {
                            eventQueue.queue(new EventWithSequenceNumber());
                        }
                    }
            );
        }

        while (receivedSequenceNumber.get() < amountOfEvents) {
            terminateIfFailure();
            Thread.sleep(10);
        }

        terminateIfFailure();
    }

    private void terminateIfFailure() {
        if (hasFailed.get()) {
            ExecutorServiceManager.getInstance().
                    getExecutorService("TestSequenceGuaranteedEventQueue").
                    shutdown();
            fail(errorMessage.get());
        }
    }

    /**
     * This method is called by the event queue when the command event shall be
     * executed. It is an implementation of the CommandExecutor interface.
     *
     * First a busy wait is performed to simulate that "work" is done. The
     * amount of time is randomized (between 0 and 50 ms) to simulate that
     * events take different amount of time to process.
     *
     * After the busy wait, a check is made to see if the events sequence number
     * are as expected. The expected sequence number are increased by one for
     * each event that has been processed. If there is no match in sequence
     * numbers, this method signals a failure to the testing process using a
     * boolean of name hasFailed.
     *
     * @param eventObject
     */
    public void doCommand(EventObject eventObject) {

        // Simulate that the event takes time to execute.
        // Random is used to let the process time vary over events
        busyWait((int)(Math.random()*50));

        // After the "execution time" is done, check that the events sequence
        // number is as expected, i.e. one more than the previously execution
        // number. If the sequence number is not as expected this means that
        // an event queued after this event has completed execution before this
        // event, i.e. the events are not executed in sequence.
        int received = receivedSequenceNumber.getAndIncrement();
        int expected =
                ((EventWithSequenceNumber)eventObject).getSequenceNumber();

        if ((received != expected) && (!hasFailed.get()) ) {
            hasFailed.set(true);
            errorMessage.set("Failed due to mismatch in sequence numbers. " +
                    "Expected: " + expected + ", Received: " + received);
        }
    }

    /**
     * Generates a busy wait.
     * @param waitTime The time to busy wait in milliseconds.
     */
    private void busyWait(long waitTime) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < (startTime + waitTime)) {
            // Busy wait
        }
    }

    public static Test suite() {
        return new TestSuite(SequenceGuaranteedEventQueueTest.class);
    }

    /**
     * This class extends the SequenceGuaranteedEventQueue to introduce a
     * sequence number check on top of the queue.
     * It is used for testing purposes to verify that events are handled in
     * the order they are queued.
     */
    class QueueWithSequenceCheck extends SequenceGuaranteedEventQueue {
        private int queuedSequenceNumber = 1;
        private Lock queueLock = new ReentrantLock();

        public QueueWithSequenceCheck(CommandExecutor commandExecutor) {
            super(commandExecutor, null);
        }

        public void queue(EventWithSequenceNumber eventObject) {
            queueLock.lock();
            try {
                eventObject.setSequenceNumber(queuedSequenceNumber++);
                super.queue(eventObject);
            } finally {
                queueLock.unlock();
            }
        }
    }

    /**
     * This class extends EventObject in order to store a sequence number
     * together with the event.
     */
    class EventWithSequenceNumber implements EventObject {
        private int sequenceNumber;

        public int getSequenceNumber() {
            return sequenceNumber;
        }

        public void setSequenceNumber(int sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
        }
    }

}
