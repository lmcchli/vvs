/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.loadregulation;

import com.abcxyz.services.moip.alarms.MoipAlarmEvent;
import com.abcxyz.services.moip.alarms.MoipAlarmFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.loadregulation.states.HighLoadState;
import com.mobeon.masp.callmanager.loadregulation.states.LoadState;
import com.mobeon.masp.callmanager.loadregulation.states.MaxLoadState;
import com.mobeon.masp.callmanager.loadregulation.states.NormalLoadState;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.operateandmaintainmanager.OMManager;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Date;

/**
 * This class handles load regulation of the Call Manager.
 * <p>
 * When a new call is added or removed in Call Manager or when the threshold
 * parameters has changed, this class should be informed. As a result, it then
 * suggests whether traffic should be stopped, started or unchanged.
 * <p>
 * This class do not affect any traffic situation. It merely calculates the
 * load situation and returns a suggested action using the
 * {@link LoadRegulationAction}.
 *  The load regulator maintains a state that illustrates the current load
 * situation. Three states exists: {@link NormalLoadState},
 * {@link com.mobeon.masp.callmanager.loadregulation.states.HighLoadState}, and {@link MaxLoadState}.
 * <p>
 * The load situation is calculated based on the threshold parameters. Three
 * threshold parameters exists: max, high water mark and low water mark.
 * <p>
 * This class is thread-safe. All public methods are synchronized for this
 * purpose.
 *
 * @author Malin Flodin
 */
public class LoadRegulator {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    // Load states
    // Thread-safe due to immutable, i.e set at construction and never changed
    private final NormalLoadState normalLoadState;
    private final HighLoadState highLoadState;
    private final MaxLoadState maxLoadState;

    // Thread-safe since only set or get in synchronized methods addCall,
    // removeCall , updateThreshold and getCurrentState
    private AtomicReference<LoadState> currentState = new AtomicReference<LoadState>();
    private AtomicInteger currentCalls = new AtomicInteger(0);

    // Configured threshold values
    private AtomicInteger configuredMax = new AtomicInteger(0);
    private AtomicInteger configuredHWM = new AtomicInteger(0);
    private AtomicInteger configuredLWM = new AtomicInteger(0);

    // CurrentHWM is increased towards the configured HWM over time. 0 = disabled.
    private AtomicInteger currentHWM = new AtomicInteger(0);

    // currentHWM is increased by this value when NormalStateLoad occurs.
    private double rampFactor;

    // Kenneth Selin added these variables to be of interest when TR 30881 happens next time.
    // The purpose is to log the use of updateThreshold.
    private AtomicBoolean updateThresholdWasCalled = new AtomicBoolean(false);
    private AtomicLong timeUpdateThresholdWasCalled = new AtomicLong(0);
    private AtomicInteger highWaterMarkRaw = new AtomicInteger();
    private AtomicInteger lowWaterMarkRaw = new AtomicInteger();
    private AtomicInteger maxRaw = new AtomicInteger();

    private boolean alarmRaised = false;

    public LoadRegulator() {
        // Create states
        normalLoadState = new NormalLoadState(this);
        highLoadState = new HighLoadState(this);
        maxLoadState = new MaxLoadState(this);

        // Initialize
        currentState.set(maxLoadState);
        currentCalls.set(0);
        configuredMax.set(0);
        configuredHWM.set(0);
        configuredLWM.set(0);

        rampFactor = ConfigurationReader.getInstance().getConfig().getRampFactor();
        currentHWM.set(ConfigurationReader.getInstance().getConfig().getInitialRampHWM());
        if (log.isInfoEnabled()) {
            if (currentHWM.get() == 0) {
                log.info("Initial ramp HighWaterMark is 0, ramping is disabled.");
            } else {
                log.info("Initial ramp HighWaterMark is " + currentHWM.get());
            }
        }
    }

    /**
     * Each time this is called the Current HWM is increased by the ramp
     * factor, if ramping is enabled.
     */
    public synchronized void recalculateCurrentHWM() {
        int current = currentHWM.get();
        if (current > 0) {
            current += rampFactor;
            currentHWM.set(current);
        }
    }

    /**
     * Registers in the load regulation calculation that a call has been added.
     * <p>
     * This method is synchronized to achieve atomic operation.
     * @return the action needed to regulate the traffic in order to obtain
     * an optimal load situation.
     * @param id This parameter is an id used when logging so that this
     * request to add a call can be connected (when analyzing the log file)
     * to a later request to remove that same call.
     */
    public synchronized LoadRegulationAction addCall(String id) {
        if (log.isDebugEnabled())
            log.debug("Adding call in state: " + currentState.get() +
                    ", id=" + id + " " + getDebugInfo());

        currentCalls.incrementAndGet();

        if (log.isDebugEnabled())
            log.debug("Add call: Current calls incremented to: " +
                    currentCalls.get() + ", id=" + id + " " + getDebugInfo());

        LoadRegulationAction action = currentState.get().addCall();

        //Call should not be added, decrease counter.
        if (action == LoadRegulationAction.REDIRECT_TRAFFIC) {
            currentCalls.decrementAndGet();
            if (log.isDebugEnabled())
                log.debug("Add call: Current calls decremented to: " +
                        currentCalls.get() + ", id=" + id + " " +
                        getDebugInfo());
        }

        if (currentCalls.get() < 0) {
            log.error("Add call: NEGATIVE AMOUNT OF CURRENT CALLS: " +
                    currentCalls.get() + ", id=" + id + " " + getDebugInfo());
        }

        if (log.isDebugEnabled())
            log.debug("Adding call results in load regulation action: " +
                    action + ", id=" + id + " " + getDebugInfo());

        return action;
    }

    /**
     * Check if a new call can be added.
     * <p>
     * NOTE:
     * This method shall be used when an OPTIONS request is received to be able
     * to generate the correct response code for that request.
     * According to RFC 3261, the response to an OPTIONS request should be the
     * same as to an INVITE request. An INVITE request is rejected when the max
     * load state has been reached and that should therefore also be done for a
     * SIP OPTIONS request. However, in order to be able to do better load
     * regulation in combination with a load balancer that uses an OPTIONS
     * request for status check it has been decided that an OPTIONS request
     * should be rejected as soon as high load state has been reached although
     * this is a slight violation to the RFC since INVITEs will still be
     * accepted in that state.
     * @return the action that would be taken if the call was added.
     */
    public synchronized LoadRegulationAction checkCallAction() {
        if ((currentState.get() instanceof HighLoadState) ||
                (currentState.get() instanceof MaxLoadState)) {
            return LoadRegulationAction.REDIRECT_TRAFFIC;
        }

        return LoadRegulationAction.UNCHANGED_TRAFFIC;
    }

    /**
     * Registers in the load regulation calculation that a call has been removed.
     * <p>
     * This method is synchronized to achieve atomic operation.
     * @return the action needed to regulate the traffic in order to obtain
     * an optimal load situation.
     * @param id This parameter is an id used when logging so that this
     * request to remove a call can be connected (when analyzing the log file)
     * to a previous request to add the call.
     */
    public synchronized LoadRegulationAction removeCall(String id) {
        if (log.isDebugEnabled())
            log.debug("Removing call in state: " + currentState.get() +
                    ", id=" + id + " " + getDebugInfo());

        currentCalls.decrementAndGet();

        if (log.isDebugEnabled())
        log.debug("Remove call: Current calls decremented to: " +
                currentCalls.get() + ", id=" + id + " " + getDebugInfo());

        LoadRegulationAction action = currentState.get().removeCall();

        if (currentCalls.get() < 0) {
            log.error("Remove call: NEGATIVE AMOUNT OF CURRENT CALLS: " +
                    currentCalls.get() + ", id=" + id + " " + getDebugInfo());
        }

        if (log.isDebugEnabled())
            log.debug("Removing call results in load regulation action: " +
                    action + ", id=" + id + " " + getDebugInfo());
        return action;
    }

    /**
     * Registers in the load regulation calculation that the threshold has been
     * updated.
     * <p>
     * This method is synchronized to achieve atomic operation.
     * @return the action needed to regulate the traffic in order to obtain
     * an optimal load situation.
     */
    public synchronized LoadRegulationAction updateThreshold(
            int highWaterMark, int lowWatermark, int max) {
        if (log.isDebugEnabled())
            log.debug("Setting threshold in state: " + currentState.get());

        saveUpdateThresholdInfo(highWaterMark, lowWatermark, max);

        configuredMax.set(max);
        configuredHWM.set(highWaterMark);
        configuredLWM.set(lowWatermark);

        if (configuredHWM.get() < 1) {
            log.warn("HighWaterMark value too low (" + configuredHWM.get() +
                    "), using 1 instead.");
            configuredHWM.set(1);
        }
        if (configuredMax.get() <= configuredHWM.get()) {
            configuredMax.set(configuredHWM.get() + 1);
            log.warn("Threshold must be higher than HighWaterMark, using " +
                    configuredMax.get());
        }
        if (configuredLWM.get() >= configuredHWM.get()) {
            configuredLWM.set(configuredHWM.get() - 1);
            log.warn("LowWaterMark must be lower than HighWaterMark, using " +
                    configuredLWM.get());
        } else if (configuredLWM.get() < 0) {
            log.warn("LowWaterMark cannot be negative (" + configuredLWM.get() +
                    "), using 0 instead.");
            configuredLWM.set(0);
        }

        LoadRegulationAction action =
                currentState.get().updateThreshold();

        if (log.isDebugEnabled())
            log.debug("Setting threshold results in load regulation action: " +
                    action);
        return action;
    }

    private void saveUpdateThresholdInfo(int highWaterMark, int lowWatermark, int max) {
        updateThresholdWasCalled.set(true);
        timeUpdateThresholdWasCalled.set(System.currentTimeMillis());
        highWaterMarkRaw.set(highWaterMark);
        lowWaterMarkRaw.set(lowWatermark);
        maxRaw.set(max);
    }

    //======= Public methods intended to be used within subpackages  ========

    public int getCurrentCalls() {
        return currentCalls.get();
    }

    public int getCurrentMax() {
        if (currentHWM.get() == 0)
            return configuredMax.get();
        int currentMax = currentHWM.get() + (configuredMax.get() - configuredHWM.get());
        if (currentMax >= configuredMax.get())
            return configuredMax.get();
        if (currentMax < 0)
            return 0;
        return currentMax;
    }

    public int getCurrentHWM() {
        if (currentHWM.get() >= configuredHWM.get() || currentHWM.get() == 0)
            return configuredHWM.get();
        return currentHWM.get();
    }

    public int getCurrentLWM() {
        if (currentHWM.get() >= configuredHWM.get() || currentHWM.get() == 0)
            return configuredLWM.get();
        int currentLWM = currentHWM.get() - (configuredHWM.get() - configuredLWM.get());
        if (currentLWM < 0)
            return 0;
        return currentLWM;
    }

    public synchronized LoadState getCurrentState() {
        return currentState.get();
    }

    public synchronized int getConfiguredMax() {
        return configuredMax.get();
    }

    public void setHighLoadState() {
        currentState.set(highLoadState);
        if(!alarmRaised) {
        	MoipAlarmEvent alarm = MoipAlarmFactory.getInstance().getAlarm(MoipAlarmFactory.MoipAlarm.COMPONENT_HIGH_LOAD);
        	OMManager.getOperateMAS().getFaultManager().raiseAlarm(alarm);
        	alarmRaised = true;
            if (log.isDebugEnabled())
                log.debug("LoadRegulator alarm raised");
        }
        if (log.isDebugEnabled())
            log.debug("Load Regulation state set to: " + currentState.get());
    }

    public void setNormalLoadState() {
        currentState.set(normalLoadState);
        if(alarmRaised) {
        	MoipAlarmEvent alarm = MoipAlarmFactory.getInstance().getAlarm(MoipAlarmFactory.MoipAlarm.COMPONENT_HIGH_LOAD);
        	OMManager.getOperateMAS().getFaultManager().clearAlarm(alarm);
        	alarmRaised = false;
            if (log.isDebugEnabled())
                log.debug("LoadRegulator alarm cleared");
        }
        if (log.isDebugEnabled())
            log.debug("Load Regulation state set to: " + currentState.get());
    }

    public void setMaxLoadState() {
        if(!alarmRaised) {
        	MoipAlarmEvent alarm = MoipAlarmFactory.getInstance().getAlarm(MoipAlarmFactory.MoipAlarm.COMPONENT_HIGH_LOAD);
        	OMManager.getOperateMAS().getFaultManager().raiseAlarm(alarm);
        	alarmRaised = true;
            if (log.isDebugEnabled())
                log.debug("LoadRegulator alarm raised");
        }
    	currentState.set(maxLoadState);
        if (log.isDebugEnabled())
            log.debug("Load Regulation state set to: " + currentState.get());
    }

    public String getDebugInfo() {
        String s = "(State=" + currentState.get() +
                ", Current calls=" + currentCalls.get() +
                ", Max calls=" + getCurrentMax() + ", HWM=" + getCurrentHWM() +
                ", LWM=" + getCurrentLWM();

        if(updateThresholdWasCalled.get()){
            s += ", Update threshold time="+new Date(timeUpdateThresholdWasCalled.get()).toString()+
                    ", Raw HWM="+ highWaterMarkRaw.get() +
                    ", Raw LWM="+lowWaterMarkRaw.get()+
                    ", Raw max="+maxRaw.get();
        } else {
            s += ", updateThreshold has not been called";
        }
        s += ")";
        return s;
    }

}
