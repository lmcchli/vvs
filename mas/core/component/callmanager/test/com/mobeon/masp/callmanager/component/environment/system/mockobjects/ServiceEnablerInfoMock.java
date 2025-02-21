/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.environment.system.mockobjects;

import com.mobeon.masp.operateandmaintainmanager.ServiceEnablerInfo;
import com.mobeon.masp.operateandmaintainmanager.CallType;
import com.mobeon.masp.operateandmaintainmanager.CallDirection;
import com.mobeon.masp.operateandmaintainmanager.CallResult;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A mock of ServiceEnablerInfo used for testing.
 * This class is immutable.
 * @author Malin Flodin
 */
public class ServiceEnablerInfoMock implements ServiceEnablerInfo {

    private ILogger log = ILoggerFactory.getILogger(getClass());

    private final static String CURRENT = "current";
    private final static String TOTAL = "total";
    private AtomicInteger maxConnections = new AtomicInteger();
    private AtomicBoolean isClosed = new AtomicBoolean(false);

    private final ConcurrentHashMap<String, Integer> statsMap =
            new ConcurrentHashMap<String, Integer>();

    public ServiceEnablerInfoMock() {
        clear();
    }

    public synchronized void clear() {
        maxConnections.set(0);
        isClosed.set(false);
        statsMap.clear();
    }

    public void setProtocol(String protocol) {
    }

    public synchronized void setMaxConnections(Integer connections) {
        maxConnections.set(connections);
    }

    public synchronized void incrementCurrentConnections(CallType type, CallDirection direction) {
        log.debug("Incremented current connections for " + type + " " + direction);

        String key = CURRENT + type.toString() + direction.toString();
        increaseStats(key);

//        key = TOTAL + type.toString() + direction.toString();
//        increaseStats(key);
    }

    private void increaseStats(String key) {
        Integer stats = statsMap.get(key);
        if (stats == null)
            statsMap.put(key, 1);
        else
            statsMap.put(key, stats + 1);
    }

    private void increaseStats(String key, int amount) {
        Integer stats = statsMap.get(key);
        if (stats == null)
            statsMap.put(key, amount);
        else
            statsMap.put(key, stats + amount);
    }

    private void decreaseStats(String key) {
        Integer stats = statsMap.get(key);
        if ((stats != null) && (stats >= 1))
            statsMap.put(key, stats - 1);
    }

    public synchronized void decrementCurrentConnections(
            CallType type, CallDirection direction) {
        log.debug("Decremented current connections for " + type + " " + direction);

        String key = CURRENT + type.toString() + direction.toString();
        decreaseStats(key);
    }

    public synchronized void incrementNumberOfConnections(
            CallType type, CallResult result, CallDirection direction) {
        log.debug("Incremented statistics with 1 for " + type + " " + result +
                " " + direction);

        String key = result.toString() + type.toString() + direction.toString();
        increaseStats(key);

        if (result == CallResult.CONNECTED
                || result == CallResult.FAILED
                || result == CallResult.ABANDONED_REJECTED) {
            key = TOTAL + type.toString() + direction.toString();
            increaseStats(key);
        }
    }

    public synchronized void incrementNumberOfConnections(
            CallType type, CallResult result, CallDirection direction,
            Integer incrementBy) {
        log.debug("Incremented statistics with "  + incrementBy +
                " for " + type + " " + result + " " + direction);

        String key = result.toString() + type.toString() + direction.toString();
        increaseStats(key, incrementBy);

        if (result == CallResult.CONNECTED
                || result == CallResult.FAILED
                || result == CallResult.ABANDONED_REJECTED) {
            key = TOTAL + type.toString() + direction.toString();
            increaseStats(key, incrementBy);
        }
    }

    public synchronized void closed() {
        isClosed.set(true);
    }

    public void opened() {
        isClosed.set(false);
    }

    // ================== Methods used for polling statistics ==============

    public Integer getMaxConnections() {
        return maxConnections.get();
    }

    public boolean isCloseComplete() {
        return isClosed.get();
    }

    public void waitForCurrentConnections(
            CallType type, CallDirection direction,
            int expected, int milliSeconds) {

        if (expected <= 0)
            return;

        String key = CURRENT + type.toString() + direction.toString();

        long startTime = System.currentTimeMillis();

        Integer stats = statsMap.get(key);
        while (((stats == null) || (stats != expected)) &&
                (System.currentTimeMillis() < (startTime + milliSeconds))) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                log.debug("Test case interrupted while waiting for " +
                        "statistics " + stats + ".", e);
                return;
            }
            stats = statsMap.get(key);
        }

        if ((stats == null) || (stats != expected)) {
            throw new IllegalStateException(
                    "Timed out when waiting for stats " + expected +
                            ". Current stats is " + stats);
        }
    }

    public void waitForTotalConnections(
            CallType type, CallDirection direction,
            int expected, int milliSeconds) {

        if (expected <= 0)
            return;

        String key = TOTAL + type.toString() + direction.toString();

        long startTime = System.currentTimeMillis();

        Integer stats = statsMap.get(key);
        while (((stats == null) || (stats != expected)) &&
                (System.currentTimeMillis() < (startTime + milliSeconds))) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                log.debug("Test case interrupted while waiting for " +
                        "statistics " + stats + ".", e);
                return;
            }
            stats = statsMap.get(key);
        }

        if ((stats == null) || (stats != expected)) {
            throw new IllegalStateException(
                    "Timed out when waiting for stats " + expected +
                            ". Current stats is " + stats);
        }
    }

    public void waitForNumberOfConnections(
            CallType type, CallResult result, CallDirection direction,
            int expected, int milliSeconds) {

        if (expected <= 0)
            return;

        String key = result.toString() + type.toString() + direction.toString();

        long startTime = System.currentTimeMillis();

        Integer stats = statsMap.get(key);
        while (((stats == null) || (stats != expected)) &&
                (System.currentTimeMillis() < (startTime + milliSeconds))) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                log.debug("Test case interrupted while waiting for " +
                        "statistics " + stats + ".", e);
                return;
            }
            stats = statsMap.get(key);
        }

        if ((stats == null) || (stats != expected)) {
            throw new IllegalStateException(
                    "Timed out when waiting for stats " + expected +
                            ". Current stats is " + stats);
        }
    }

}
