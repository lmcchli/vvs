/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager;

/**
 * Enumreation of possible operational states experienced by the Call Manager.
 * The Call Manager is {@link DOWN} if the administrative state is locked,
 * pending lock (i.e. locking) or shutdown.
 * The Call Manager is {@link IMPAIRED} if there is no active remote party, i.e.
 * no active registrations towards the SSPs.
 * Otherwise, the Call Manager is {@link UP}.
 */
public enum ExperiencedOperationalStatus {
    UP("up"),
    DOWN("down"),
    IMPAIRED("impaired");

    private final String name;

    ExperiencedOperationalStatus(String name) {
       this.name = name;
   }

    public String getName() {
        return name;
    }
}
