/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.greetings;

/**
 * Greeting type enumeration
 *
 * @author mande
 */
public enum GreetingType {
    ALL_CALLS,
    NO_ANSWER,
    BUSY,
    OUT_OF_HOURS,
    EXTENDED_ABSENCE,
    CDG,
    TEMPORARY,
    OWN_RECORDED,
    SPOKEN_NAME,
    DIST_LIST_SPOKEN_NAME;

    /**
     * Returns a GreetingType matching an application greeting name
     * @param value a string representing a GreetingType in the application
     * @return the GreetingType corresponding to value
     */
    public static GreetingType getValueOf(String value) {
        if (value.compareTo("allcalls") == 0) {
            return GreetingType.ALL_CALLS;
        }
        if (value.compareTo("noanswer") == 0) {
            return GreetingType.NO_ANSWER;
        }
        if (value.compareTo("busy") == 0) {
            return GreetingType.BUSY;
        }
        if (value.compareTo("outofhours") == 0) {
            return GreetingType.OUT_OF_HOURS;
        }
        if (value.compareTo("extended_absence") == 0) {
            return GreetingType.EXTENDED_ABSENCE;
        }
        if (value.compareTo("cdg") == 0) {
            return GreetingType.CDG;
        }
        if (value.compareTo("temporary") == 0) {
            return GreetingType.TEMPORARY;
        }
        if (value.compareTo("ownrecorded") == 0) {
            return GreetingType.OWN_RECORDED;
        }
        throw new IllegalArgumentException(value);
    }
}
