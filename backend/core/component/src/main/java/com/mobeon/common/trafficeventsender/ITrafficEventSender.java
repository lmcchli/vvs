/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.trafficeventsender;

/**
 * This interface is used to report that a Traffic Event has occurred.
 *
 * @author ermmaha
 */
public interface ITrafficEventSender {

    /**
     * Reports a Traffic Event to interested parties.
     *
     * @param trafficEvent
     * @throws TrafficEventSenderException
     */
    public void reportTrafficEvent(TrafficEvent trafficEvent) throws TrafficEventSenderException;

    /**
     * Reports a Traffic Event to interested parties. The restrictions parameter is used to list receiving parties that
     * should not receive the Traffic Event
     *
     * @param trafficEvent
     * @param restrictions
     * @throws TrafficEventSenderException
     */
    public void reportTrafficEvent(TrafficEvent trafficEvent, Restrictions restrictions) throws TrafficEventSenderException;
}
