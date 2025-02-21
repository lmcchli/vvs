/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.trafficeventsender;

/**
 * Defines different restrictions for which event receivers that should not receive an event.
 *
 * @author ermmaha
 */
public class Restrictions {

    private boolean enduserRestricted = false;
    private boolean eventsystemRestricted = false;

    /**
     * Enduser is restricted to not receive the event
     */
    public boolean isEnduserRestricted() {
        return enduserRestricted;
    }

    /**
     * Enduser is restricted to not receive the event
     */
    public void setEnduserRestricted(boolean enduserRestricted) {
        this.enduserRestricted = enduserRestricted;
    }

    /**
     * The eventreporting system is restricted to not receive the event
     */
    public boolean isEventsystemRestricted() {
        return eventsystemRestricted;
    }

    /**
     * The eventreporting system is restricted to not receive the event
     */
    public void setEventsystemRestricted(boolean eventsystemRestricted) {
        this.eventsystemRestricted = eventsystemRestricted;
    }
}
