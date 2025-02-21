package com.mobeon.masp.execution_engine.util;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.util.TimeValue;
import com.mobeon.masp.util.Tools;
import com.mobeon.masp.util.test.MASTestSwitches;
import junit.framework.Assert;

import java.util.EnumMap;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by IntelliJ IDEA.
 * User: QMIAN
 * Date: 2007-jan-31
 * Time: 19:14:53
 * To change this template use File | Settings | File Templates.
 */
public class TestEventGenerator {

    private static ILogger logger = ILoggerFactory.getILogger(TestEventGenerator.class);

    public static void generateEvent(TestEvent event) {
        if (logger.isInfoEnabled() && MASTestSwitches.isUnitTesting()) {
            lockAndStep(event, null);
            logger.info(event);
        }
    }

    public static void generateEvent(TestEvent event, Object associatedInfo) {
        if (logger.isInfoEnabled() && MASTestSwitches.isUnitTesting()) {
            lockAndStep(event, associatedInfo);
            logger.info(event + "=" + associatedInfo);
        }
    }

    public static boolean isActive() {
        return logger.isInfoEnabled() && MASTestSwitches.isUnitTesting();
    }

    public static void generateEvent(TestEvent event, String name, String value) {
        if (logger.isInfoEnabled() && MASTestSwitches.isUnitTesting()) {
            lockAndStep(event, name);
            logger.info(event + "={" + name + ":" + value + "}");
        }
    }

    private static class LockSteppedExchange {
        public final Lock lock = new ReentrantLock();
        public final Condition condition = lock.newCondition();
        private Object info;
        private boolean isInLock;
        private boolean isInStep;
        private boolean retired = false;
        private TestEvent event;
        private Semaphore oneStepAtATime = new Semaphore(1);
        private Semaphore oneLockAtATime = new Semaphore(1);

        public LockSteppedExchange(TestEvent event) {

            this.event = event;
        }

        public Object stepWhenLocked(long timeout) throws InterruptedException, TimeoutException {
            Object result;
            try {
                //Wait until we can grab lock to get a clean copy
                //of the retired flag.
                while (!lock.tryLock(100, TimeUnit.MILLISECONDS)) {
                }
                if (retired)
                    return null;
                lock.unlock();

                //Don't hold lock while aquiring
                oneStepAtATime.acquire();
                lock.lock();

                isInStep = true;
                while (!isInLock && !retired) {
                    boolean succeded = true;
                    if (timeout != -1)
                        succeded = condition.await(timeout, TimeUnit.MILLISECONDS);
                    else
                        condition.await();
                    if (!succeded) {
                        retire();
                        throw new TimeoutException("Maximum timeout of " + timeout + " milliseconds exceeded");
                    }
                }
                result = info;
                if (!retired) condition.signal();
            } finally {
                isInLock = false;
                lock.unlock();
            }
            return retired ? null : result;
        }

        public void lockAndExchange(Object associatedInfo) throws InterruptedException {
            try {
                oneLockAtATime.acquire();
                lock.lock();

                this.info = associatedInfo;
                isInLock = true;
                if (!retired) condition.signal();
                while (!isInStep && !retired) {
                    condition.await();
                }
            } finally {
                isInStep = false;
                if (oneStepAtATime.availablePermits() == 0)
                    oneStepAtATime.release();
                oneLockAtATime.release();
                lock.unlock();
            }
        }

        public void retire() {
            try {
                lock.lock();
                retired = true;
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }

        public Object stepWhenLocked() throws InterruptedException {
            try {
                return stepWhenLocked(-1);
            } catch (TimeoutException e) {
                return null;
            }
        }

        public void activate() {
            try {
                lock.lock();
                retired = false;
            } finally {
                lock.unlock();
            }
        }
    }

    static final EnumMap<TestEvent, LockSteppedExchange> lockStepExchangeMap = new EnumMap<TestEvent, LockSteppedExchange>(TestEvent.class);

    private static void lockAndStep(TestEvent event, Object associatedInfo) {
        LockSteppedExchange lse;
        synchronized (lockStepExchangeMap) {
            lse = lockStepExchangeMap.get(event);
        }
        if (lse != null) {
            try {
                lse.lockAndExchange(associatedInfo);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static void declareWait(TestEvent ... events) {
        synchronized (lockStepExchangeMap) {
            for (TestEvent event : events)
                summon(event).activate();
        }
    }

    public static void declareNoWait(TestEvent ... events) {
        synchronized (lockStepExchangeMap) {
            for (TestEvent event : events) {
                LockSteppedExchange lse = lockStepExchangeMap.remove(event);
                if (lse != null) {
                    lse.retire();
                }
            }
        }
    }

    public static void declareNoWait() {
        synchronized (lockStepExchangeMap) {
            for (LockSteppedExchange barrier : lockStepExchangeMap.values()) {
                barrier.retire();
            }
            lockStepExchangeMap.clear();
        }
    }


    public static class CheckPoint {
        private TestEvent startEvent;
        private long start;
        private long end;
        private Object associatedInfo;
        private TestEvent endEvent;

        public CheckPoint(TestEvent event) {
            this.startEvent = event;
            start = System.currentTimeMillis();
        }

        public Object waitFor(TestEvent event, long timeout) throws BrokenBarrierException {
            this.endEvent = event;
            Object result = TestEventGenerator.waitFor(event, timeout);
            end = System.currentTimeMillis();
            return result;
        }

        public void atLeastMillis(long delta) {
            if (delta > end - start) {
                Assert.fail("Time between " + startEvent + " and " + endEvent + " was " + (end - start) + " which is less than the minimum " + new TimeValue(delta) + " required.");
            }
        }

        public void atMostMillis(long delta) {
            if (delta < end - start) {
                Assert.fail("Time between " + startEvent + " and " + endEvent + " was " + (end - start) + " which is more than the maximum " + new TimeValue(delta) + " required.");
            }
        }

        public Object getInfo() {
            return associatedInfo;
        }

        public void setInfo(Object associatedInfo) {
            this.associatedInfo = associatedInfo;
        }
    }


    public static CheckPoint waitAndCheckpoint(TestEvent event, long timeout) throws BrokenBarrierException {
        long start = System.currentTimeMillis();
        CheckPoint result = new CheckPoint(event);
        try {
            final LockSteppedExchange lse = summon(event);
            if (lse != null) {
                if (MASTestSwitches.canTestTimeout()) {
                    try {
                        result.setInfo(lse.stepWhenLocked(timeout));
                        logger.info("AWAIT succeded for " + event);
                    } catch (TimeoutException te) {
                        Assert.fail("Test case failed ( TCFAIL ) because of timeout waiting for event " + event + ". Waited " + Tools.millisFrom(start));
                    }
                } else {
                    result.setInfo(lse.stepWhenLocked());
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Assert.fail("Test case failed ( TCFAIL ) because it was interrupted");
        }
        return result;
    }

    public static Object waitFor(TestEvent event, long timeout)  {
        long start = System.currentTimeMillis();
        Object result = null;
        try {
            final LockSteppedExchange lse = summon(event);
            if (lse != null) {
                if (MASTestSwitches.canTestTimeout()) {
                    try {
                        result = lse.stepWhenLocked(timeout);
                        logger.info("AWAIT succeded for " + event);
                    } catch (TimeoutException te) {
                        Assert.fail("Test case failed ( TCFAIL ) because of timeout waiting for event " + event + ". Waited " + Tools.millisFrom(start));
                    }
                } else {
                    result = lse.stepWhenLocked();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Assert.fail("Test case failed ( TCFAIL ) because it was interrupted");
        }
        return result;
    }

    private static LockSteppedExchange summon(TestEvent event) {
        LockSteppedExchange lse;
        synchronized (lockStepExchangeMap) {
            lse = lockStepExchangeMap.get(event);
            if (lse == null) {
                lockStepExchangeMap.put(event, new LockSteppedExchange(event));
                lse = lockStepExchangeMap.get(event);
            }
        }
        return lse;
    }

}
