/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.releasecausemapping;

/**
 * This class represents a pair of Q.850 cause/location values.
 * <p>
 * Location may be null which indicates that no location information was
 * available when creating the pair.
 * <p>
 * Value range for Q.850 cause: 0-127
 * Value range for Q.850 location: 0-15
 * {@link IllegalArgumentException} is thrown if creating a Q.850 cause/location
 * pair where cause or location is out-of-range.
 * <p>
 * This class is immutable.
 *
 * @author Malin Nyfeldt
 */
public class Q850CauseLocationPair {
    private final int cause;
    private final Integer location;

    public Q850CauseLocationPair(int cause, Integer location)
            throws IllegalArgumentException {

        if ((cause < ReleaseCauseConstants.Q850_CAUSE_MIN) ||
                (cause > ReleaseCauseConstants.Q850_CAUSE_MAX))
            throw new IllegalArgumentException("Illegal Q.850 cause = " +
                    cause + ". Valid range is: " +
                    ReleaseCauseConstants.Q850_CAUSE_MIN + "-" +
                    ReleaseCauseConstants.Q850_CAUSE_MAX);

        if (location != null)
            if ((location < ReleaseCauseConstants.Q850_LOCATION_MIN) ||
                    (location > ReleaseCauseConstants.Q850_LOCATION_MAX))
                throw new IllegalArgumentException("Illegal Q.850 location = " +
                        location + ". Valid range is: " +
                        ReleaseCauseConstants.Q850_LOCATION_MIN + "-" +
                        ReleaseCauseConstants.Q850_LOCATION_MAX);

        this.cause = cause;
        this.location = location;
    }

    public int getCause() {
        return cause;
    }

    public Integer getLocation() {
        return location;
    }

    public String toString() {
        return "Q.850 cause = " + cause + ", Q.850 location = " + location;
    }
}
