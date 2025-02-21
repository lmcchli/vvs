/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

/**
 * Represents the length of a Media (for example audio/video/text) 
 * in a specific unit.
 *
 * The type of units is listed in the {@link LengthUnit} enumeration.
 *
 * @author Mats Egland
 */
public final class MediaLength {
    /**
     * Allowed length-units.
     */
    public enum LengthUnit {MILLISECONDS, PAGES}

    /**
     * The length-value.
     */
    private long value;
    /**
     * The unit of this instance's length value.
     */
    private LengthUnit unit;

    /**
     * Constructor that constructs a <code>MediaLength</code> instance
     * with given length and unit.
     *
     * @param unit      The LengthUnit of the value.
     * @param value     The value of the lenght.
     * 
     * @throws IllegalArgumentException If unit argument is null, or if
     * 								    value is less than 0.
     */
    public MediaLength(LengthUnit unit, long value) {
    	if (unit == null) {
    		throw new IllegalArgumentException(
    				"Null argument is not allowed");
    	} else if (value < 0) {
    		throw new IllegalArgumentException(
				"Value of MediaLength must be positive");
    	}
    	this.value = value;
        this.unit = unit;
    }

    /**
     * Returns the length.
     *
     * @return The length in the specified unit.
     */
    public final long getValue() {
        return value;
    }

    /**
     * Returns the unit of the length value.
     * @return The unit of this length.
     */
    public final LengthUnit getUnit() {
        return unit;
    }

    /**
     * Returns a string representation of the object.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "{unit="+unit+",value="+value+"}";
    }
}
