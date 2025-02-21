/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.loadregulation.states;

import com.mobeon.masp.callmanager.loadregulation.LoadRegulator;
import com.mobeon.masp.callmanager.loadregulation.LoadRegulationAction;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class represents a maximum load situation, i.e. overload has occurred.
 * <p>
 * This state is active while the number of calls has reached the Max number of
 * allowed calls. When number of calls falls below Max calls again, the state
 * is set to  {@link com.mobeon.masp.callmanager.loadregulation.states.HighLoadState}.
 * <p>
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public class MaxLoadState implements LoadState {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private final LoadRegulator loadRegulator;

    private AtomicBoolean firstUpdateThreshold = new AtomicBoolean(true);

    public MaxLoadState(LoadRegulator loadRegulator) {
        this.loadRegulator = loadRegulator;
    }

    public synchronized LoadRegulationAction addCall() {
        LoadRegulationAction action = LoadRegulationAction.REDIRECT_TRAFFIC;

        if (log.isDebugEnabled())
            log.debug("Adding call does not affect load situation. Redirecting traffic. " +
                    loadRegulator.getDebugInfo());

        return action;
    }

    public synchronized LoadRegulationAction removeCall() {
        LoadRegulationAction action = LoadRegulationAction.UNCHANGED_TRAFFIC;

        int callAmount = loadRegulator.getCurrentCalls();

        if (callAmount >= loadRegulator.getCurrentMax()) {

            if (log.isDebugEnabled())
                log.debug("Removing call does not affect load situation. Redirecting traffic. " +
                        loadRegulator.getDebugInfo());

            action = LoadRegulationAction.REDIRECT_TRAFFIC;

        } else if (callAmount <= loadRegulator.getCurrentLWM() || callAmount == 0) {

            if (log.isDebugEnabled())
                log.debug("Removing call results in lower water mark reached. " +
                        loadRegulator.getDebugInfo());

            action = LoadRegulationAction.START_TRAFFIC;
            loadRegulator.setNormalLoadState();

        } else {

            if (log.isDebugEnabled())
                log.debug("Removing call results in amount below max calls. " +
                        loadRegulator.getDebugInfo());

            loadRegulator.setHighLoadState();
        }

        return action;
    }

    public synchronized LoadRegulationAction updateThreshold(
    ) {
        LoadRegulationAction action = LoadRegulationAction.UNCHANGED_TRAFFIC;

        int callAmount = loadRegulator.getCurrentCalls();

        if (callAmount >= loadRegulator.getCurrentMax()) {

            if (log.isDebugEnabled())
                log.debug("New threshold causes traffic redirection. " +
                        loadRegulator.getDebugInfo());

            action = LoadRegulationAction.REDIRECT_TRAFFIC;

        } else if (callAmount <= loadRegulator.getCurrentLWM()) {

            if (log.isDebugEnabled())
                log.debug("New threshold results in lower water mark reached. " +
                        loadRegulator.getDebugInfo());

            if(firstUpdateThreshold.getAndSet(false)){
                if (log.isDebugEnabled())
                    log.debug("First update threshold, will not recalculate current HWM");
            } else {
                loadRegulator.recalculateCurrentHWM();
            }
            action = LoadRegulationAction.START_TRAFFIC;
            loadRegulator.setNormalLoadState();

        } else {
            if (log.isDebugEnabled())
                log.debug("New threshold results in amount below max calls. " +
                        loadRegulator.getDebugInfo());

            loadRegulator.setHighLoadState();
        }

        return action;
    }

    public String toString() {
        return "MaxLoad";
    }
}
