/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.loadregulation.states;

import com.mobeon.masp.callmanager.loadregulation.LoadRegulator;
import com.mobeon.masp.callmanager.loadregulation.LoadRegulationAction;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * This class represents a high load situation, i.e. overload is about to occur.
 * <p>
 * This state is active while the number of calls has reached the high
 * water mark (HWM). When the number of calls reaches Max calls
 * the state is set to {@link com.mobeon.masp.callmanager.loadregulation.states.MaxLoadState}. The number of calls then has to go
 * below Max calls before this state becomes active again. When the number
 * of calls reaches the low water mark (LWM) the state is set to
 * {@link com.mobeon.masp.callmanager.loadregulation.states.NormalLoadState}.
 * <p>
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public class HighLoadState implements LoadState {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private final LoadRegulator loadRegulator;

    public HighLoadState(LoadRegulator loadRegulator) {
        this.loadRegulator = loadRegulator;
    }

    public synchronized LoadRegulationAction addCall() {
        LoadRegulationAction action = LoadRegulationAction.UNCHANGED_TRAFFIC;

        int callAmount = loadRegulator.getCurrentCalls();

        if (callAmount >= loadRegulator.getCurrentMax()) {

            if (log.isDebugEnabled())
                log.debug("Adding call results in max calls reached. " +
                        loadRegulator.getDebugInfo());

            loadRegulator.setMaxLoadState();

        } else {
            if (log.isDebugEnabled())
                log.debug("Adding call does not affect load situation. " +
                        loadRegulator.getDebugInfo());
        }

        return action;
    }

    public synchronized LoadRegulationAction removeCall() {
        LoadRegulationAction action = LoadRegulationAction.UNCHANGED_TRAFFIC;

        int callAmount = loadRegulator.getCurrentCalls();

        if (callAmount <= loadRegulator.getCurrentLWM() || callAmount == 0) {

            if (log.isDebugEnabled())
                log.debug("Removing call results in lower water mark reached. " +
                        loadRegulator.getDebugInfo());

            loadRegulator.recalculateCurrentHWM();

            action = LoadRegulationAction.START_TRAFFIC;
            loadRegulator.setNormalLoadState();

        } else {
            if (log.isDebugEnabled())
                log.debug("Removing call does not affect load situation. " +
                        loadRegulator.getDebugInfo());
        }

        return action;
    }

    public synchronized LoadRegulationAction updateThreshold(
    ) {
        LoadRegulationAction action = LoadRegulationAction.UNCHANGED_TRAFFIC;

        int callAmount = loadRegulator.getCurrentCalls();

        if (callAmount >= loadRegulator.getCurrentMax()) {

            if (log.isDebugEnabled())
                log.debug("New threshold results in max calls reached. " +
                        loadRegulator.getDebugInfo());

            loadRegulator.setMaxLoadState();

        } else if (callAmount <= loadRegulator.getCurrentLWM()) {

            if (log.isDebugEnabled())
                log.debug("New threshold results in lower water mark reached. " +
                        loadRegulator.getDebugInfo());

            loadRegulator.recalculateCurrentHWM();

            action = LoadRegulationAction.START_TRAFFIC;
            loadRegulator.setNormalLoadState();

        } else {
            if (log.isDebugEnabled())
                log.debug("New threshold does not affect load situation. " +
                        loadRegulator.getDebugInfo());
        }

        return action;
    }

    public String toString() {
        return "HighLoad";
    }
}
