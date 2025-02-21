/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.events;

/**
 * This class is an event that carries all information regarding a request
 * from the Call Manager operator to update threshold values.
 * <P>
 * This class is immutable.
 *
 * @author Malin Flodin
 */
public final class UpdateThresholdEvent implements EventObject {

    private final int highWaterMark;
    private final int lowWaterMark;
    private final int threshold;

    public UpdateThresholdEvent(
            int highWaterMark, int lowWaterMark, int threshold) {
        this.highWaterMark = highWaterMark;
        this.lowWaterMark = lowWaterMark;
        this.threshold = threshold;
    }

    public int getHighWaterMark() {
        return highWaterMark;
    }

    public int getLowWaterMark() {
        return lowWaterMark;
    }

    public int getThreshold() {
        return threshold;
    }

    public String toString() {
        return "UpdateThresholdEvent: <HWM = " + highWaterMark + ">, <LWM = " +
                    lowWaterMark + ">, <Max = " + threshold + ">";
    }
}
