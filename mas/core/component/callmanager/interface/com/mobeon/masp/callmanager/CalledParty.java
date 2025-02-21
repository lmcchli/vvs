/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager;

/**
 * A container of CalledParty related information.
 * It extends {@link com.mobeon.masp.callmanager.CallPartyDefinitions}
 * with information specific for a CalledParty.
 *
 * As a container, it provides only setters and getters.
 *
 * This class is thread-safe.
 *
 * @see com.mobeon.masp.callmanager.CallPartyDefinitions
 *
 * @author Malin Flodin
 */

public class CalledParty extends CallPartyDefinitions {
    public String toString() {
        return super.toString();
    }
}
