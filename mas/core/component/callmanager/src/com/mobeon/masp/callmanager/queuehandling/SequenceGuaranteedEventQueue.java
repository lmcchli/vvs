/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.queuehandling;

import com.mobeon.masp.callmanager.events.EventObject;

import com.mobeon.masp.util.executor.ExecutorServiceManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An event queue that executes one command event at a time by sending it to the
 * registered command executor. The command executor must fulfill the
 * CommandExecutor interface. The SequenceGuaranteedEventQueue guarantees that
 * all events are handled in the same order as they are queued and that only
 * one event at a time is handled.
 *
 * This class uses a thread pool to pick an available thread to execute commands.
 * At most one thread is used from the thread pool at a time.
 *
 * When an event is queued, a check is made to see if there already is a thread
 * processing events on the queue. If a thread is active, the event is simply
 * added to the queue. If no thread is active, a thread from the
 * thread pool is started to process events. After the newly allocated thread
 * is done processing the event, it checks if there are more events to handle
 * in the queue before it is returned to the thread pool.
 *
 * It is required that the CommandExecutor.doCommand method never throws an
 * exception. If it should do this any way, the exception is simply logged as
 * an error and ignored.
 *
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public class SequenceGuaranteedEventQueue {

    private final ILogger log = ILoggerFactory.getILogger(getClass());
    private final CommandExecutor commandExecutor;
    private final ConcurrentLinkedQueue<EventObject> eventQueue =
            new ConcurrentLinkedQueue<EventObject>();
    private final ReentrantLock lock = new ReentrantLock();

    private Class poolName = null;
    private AtomicBoolean threadIsRunning = new AtomicBoolean(false);

    // Used for debug logging
    private AtomicInteger counter = new AtomicInteger(0);

    public SequenceGuaranteedEventQueue(CommandExecutor commandExecutor,
                                        Class poolName) {
        this.commandExecutor = commandExecutor;
        this.poolName = poolName;
    }

    /**
     * This method adds an event to the queue.
     * If a thread processing events already is running it simply returns.
     * Otherwise a new thread is allocated from a thread pool and is started
     * processing events.
     *
     * @param eventObject The event to queue.
     */
    public void queue(EventObject eventObject) {
        // Adding the event to the queue, checking if a thread is running and
        // indicating if a new thread is started should be an atomic event.
        // A lock is used for this purpose.
        lock.lock();
        try {
            eventQueue.add(eventObject);
            if (log.isDebugEnabled()) {
                // Log every 500th event added to the queue.
                if (counter.incrementAndGet() > 500) {                    
                    counter.set(0);
                    log.debug("PoolName: " + poolName + ", QueueSize: " + eventQueue.size());
                }
            }
            if (!threadIsRunning.get()) {
                threadIsRunning.set(true);
                if (poolName == null) {
                    poolName = SequenceGuaranteedEventQueue.class;
                }
                ExecutorServiceManager.getInstance().
                        getExecutorService(poolName).
                        execute(new CommandExecution());
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * This method executes the first command event on the queue.
     * After the event has been executed, if there are no more events in the
     * queue, the thread is returned to the pool otherwise it executes the next
     * event in the queue.
     */
    private void processQueueEvent() {
        executeCommand();

        // Checking if there are more command events to process and indicating
        // if the thread has been returned to the pool should be atomic.
        // A lock is used for this purpose.
        lock.lock();
        try {
            while (!eventQueue.isEmpty()) {
                // Be sure to unlock before executing the command since it
                // might take a while for the execution to finish.
                lock.unlock();
                executeCommand();
                lock.lock();
            }
        } finally {
            threadIsRunning.set(false);
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void executeCommand() {
        try {
            // Polling the event queue does not need to be protected by the lock,
            // since only one thread will poll the queue at a time and we are
            // sure that there exists an event on the queue before retrieving it.
            commandExecutor.doCommand(eventQueue.poll());
        } catch (Throwable e) {
            // Cannot do anything more than log the error and ignore the problem.
            // Method doCommand must never throw an exception of any kind.
            log.error("Internal Error: Exception catched when processing " +
                    "event. The procesEvent method must never throw an " +
                    "exception. ", e);
        }
    }

    /**
     * This class is used only to simplify the code. It is used to execute a
     * command from a thread pool. Implements Runnable.
     *
     * @author Malin Flodin
     */
    public class CommandExecution implements Runnable {
        public void run() {
            processQueueEvent();
        }
    }
}
