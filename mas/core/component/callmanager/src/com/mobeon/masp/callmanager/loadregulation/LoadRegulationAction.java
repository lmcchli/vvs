/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.loadregulation;

/**
 * Represents the action necessary to control the current load situation in a
 * good way.
 *
 * @author Malin Flodin
 */
public enum LoadRegulationAction {
    START_TRAFFIC,
    STOP_TRAFFIC,
    UNCHANGED_TRAFFIC,
    REDIRECT_TRAFFIC
}
