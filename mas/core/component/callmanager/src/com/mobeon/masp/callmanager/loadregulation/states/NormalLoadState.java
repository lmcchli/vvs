/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.loadregulation.states;

import com.mobeon.masp.callmanager.loadregulation.LoadRegulator;
import com.mobeon.masp.callmanager.loadregulation.LoadRegulationAction;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * This class represents a normal load situation, i.e. no overload is currently
 * detected.
 * <p>
 * This state is active while the number of calls is lower than the high
 * water mark (HWM). When the number of calls becomes higher than the HWM the
 * state is set to {@link com.mobeon.masp.callmanager.loadregulation.states.HighLoadState}. The number of calls then has to go
 * below the lower water mark (LWM) before this state becomes active again.
 * <p>
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public class NormalLoadState implements LoadState {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private final LoadRegulator loadRegulator;

    public NormalLoadState(LoadRegulator loadRegulator) {
        this.loadRegulator = loadRegulator;
    }

    public synchronized LoadRegulationAction addCall() {
        LoadRegulationAction action = LoadRegulationAction.UNCHANGED_TRAFFIC;

        int callAmount = loadRegulator.getCurrentCalls();

        if (callAmount >= loadRegulator.getCurrentMax()) {

            if (log.isDebugEnabled())
                log.debug("Adding call results in max calls reached. " +
                        loadRegulator.getDebugInfo());

            action = LoadRegulationAction.STOP_TRAFFIC;
            loadRegulator.setMaxLoadState();

        } else if (callAmount >= loadRegulator.getCurrentHWM()) {

            if (log.isDebugEnabled())
                log.debug("Adding call results in high water mark reached. " +
                        loadRegulator.getDebugInfo());

            action = LoadRegulationAction.STOP_TRAFFIC;
            loadRegulator.setHighLoadState();

        } else if (callAmount < loadRegulator.getCurrentLWM()) {
            if (log.isDebugEnabled())
                log.debug("Adding call does not affect load situation. " +
                        loadRegulator.getDebugInfo());
        } else {
            if (log.isDebugEnabled())
                log.debug("Adding call does not affect load situation. " +
                        loadRegulator.getDebugInfo());
        }

        return action;
    }

    public synchronized LoadRegulationAction removeCall() {
        return LoadRegulationAction.UNCHANGED_TRAFFIC;
    }

    public synchronized LoadRegulationAction updateThreshold(
    ) {

        LoadRegulationAction action = LoadRegulationAction.UNCHANGED_TRAFFIC;

        int callAmount = loadRegulator.getCurrentCalls();

        if (callAmount >= loadRegulator.getCurrentMax()) {

            if (log.isDebugEnabled())
                log.debug("New threshold results in max calls reached. " +
                        loadRegulator.getDebugInfo());
            action = LoadRegulationAction.STOP_TRAFFIC;
            loadRegulator.setMaxLoadState();

        } else if (callAmount >= loadRegulator.getCurrentHWM()) {

            if (log.isDebugEnabled())
                log.debug("New threshold results in high water mark reached. " +
                        loadRegulator.getDebugInfo());
            action = LoadRegulationAction.STOP_TRAFFIC;
            loadRegulator.setHighLoadState();

        } else if (callAmount < loadRegulator.getCurrentLWM()) {
            if (log.isDebugEnabled())
                log.debug("New threshold does not affect load situation. " +
                        loadRegulator.getDebugInfo());

            loadRegulator.recalculateCurrentHWM();

        } else {
            if (log.isDebugEnabled())
                log.debug("New threshold does not affect load situation. " +
                        loadRegulator.getDebugInfo());
        }

        return action;
    }

    public String toString() {
        return "NormalLoad";
    }
}
