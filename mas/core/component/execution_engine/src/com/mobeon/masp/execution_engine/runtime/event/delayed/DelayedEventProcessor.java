package com.mobeon.masp.execution_engine.runtime.event.delayed;

import com.mobeon.masp.execution_engine.runtime.event.EventHubImpl;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;

/**
 * User: QMIAN
 * Date: 2006-okt-20
 * Time: 13:55:57
 */
public class DelayedEventProcessor implements Runnable {

    private static final ILogger log = ILoggerFactory.getILogger(EventHubImpl.class);

    private static final DelayedEventProcessor processor = new DelayedEventProcessor();

    private final DelayQueue<DelayedEvent> wakeUpQueue = new DelayQueue<DelayedEvent>();
    private final ConcurrentHashMap<String, Long> cancelled = new ConcurrentHashMap<String, Long>();
    private static final long CANCEL_PURGE_DELAY = 1000 * 60 * 2; //Cancel will be active for 2 minutes after cancelling

    public static DelayedEventProcessor start() {
        Thread t = new Thread(processor, "DelayedEventSender");
        t.setDaemon(true);
        t.start();
        return processor;
    }

    private static void registerInLogger(DelayedEvent delayed) {
        ISession session = delayed.getSession();
        if (session != null) {
            delayed.getSession().registerSessionInLogger();
        }
    }


    public void run() {
        try {
            if (log.isInfoEnabled())
                log.info("DelayedEventProcessor started");
            int oldSize = cancelled.size();
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    if(log.isDebugEnabled())
                        log.debug("Waiting for take");
                    DelayedEvent delayed = wakeUpQueue.take();
                    if(log.isDebugEnabled())
                        log.debug("Take returned, processing "+delayed);
                    registerInLogger(delayed);
                    if (delayed.sendId() != null && cancelled.containsKey(delayed.sendId())) {
                        if (log.isDebugEnabled()) {
                            log.debug("Cancelled event " + delayed.sendId());
                        }
                        cancelled.remove(delayed.sendId());
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Firing delayed event with sendid " + delayed.sendId());
                        }
                        delayed.fireEvent();
                    }
                    if (Math.abs(cancelled.size() - oldSize) > 4) {
                        oldSize = cancelled.size();
                        Iterator<Map.Entry<String, Long>> entryIterator = cancelled.entrySet().iterator();
                        while (entryIterator.hasNext()) {
                            Map.Entry<String, Long> entry = entryIterator.next();
                            if (entry.getValue() <= System.currentTimeMillis()) {
                                entryIterator.remove();
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    log.info("DelayedEventProcessor was interrupted !", e);
                } catch (Throwable t) {
                    log.warn("<CHECKOUT>Unknown error/exception: " + t.getMessage(), t);
                }
            }
        } finally {
            if (log.isInfoEnabled())
                log.info("DelayedEventProcessor exited");
        }

    }

    public void sendDelayedEvent(DelayedEvent delayedEvent) {
        wakeUpQueue.put(delayedEvent);
        if (log.isDebugEnabled())
            log.debug("Added " + delayedEvent + " to delayed event sender");
    }

    public void logState() {
        log.debug("Number of delayed events is now " + wakeUpQueue.size());
        log.debug("Number of cancelled events is now " + cancelled.size());
    }

    public void cancel(final String sendid) {
        cancelled.put(sendid, System.currentTimeMillis() + CANCEL_PURGE_DELAY);
    }

    public static void reset() {
        processor.wakeUpQueue.clear();
        processor.cancelled.clear();
    }

}
