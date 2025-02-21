/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.loadregulation.states;

import com.mobeon.masp.callmanager.loadregulation.LoadRegulationAction;

/**
 * Interface class represents a LoadState and contains all events that should
 * be handled by the load regulation.
 * 
 * @author Malin Flodin
 */
public interface LoadState {
    public LoadRegulationAction addCall();
    public LoadRegulationAction removeCall();
    public LoadRegulationAction updateThreshold();
}
