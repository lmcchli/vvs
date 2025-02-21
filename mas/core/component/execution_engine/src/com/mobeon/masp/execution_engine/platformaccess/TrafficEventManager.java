/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess;

import com.abcxyz.services.moip.common.mdr.MdrConstants;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import com.mobeon.common.trafficeventsender.ITrafficEventSender;
import com.mobeon.common.trafficeventsender.Restrictions;
import com.mobeon.common.trafficeventsender.TrafficEvent;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;


import java.util.Arrays;

/**
 * Handles the trafficEventSend method
 * 
 * @author ermmaha
 */
public class TrafficEventManager {
    private static final String SESSIONID = "sessionid";
    private static ILogger log = ILoggerFactory.getILogger(TrafficEventManager.class);

    private ExecutionContext executionContext;
    private ITrafficEventSender iTrafficEventSender;

    TrafficEventManager(ExecutionContext executionContext, ITrafficEventSender iTrafficEventSender) {
        this.executionContext = executionContext;
        this.iTrafficEventSender = iTrafficEventSender;
    }
    
    /**
     * Sends a traffic event with the specified data
     *
     * @param eventName     the name to set for the traffic event
     * @param propertyName  a list with the property names for the event, the propertyValue with the same index corresponds to this name
     * @param propertyValue a list with the property values for the event, the propertyName with the same index corresponds to this value
     */
    void trafficEventSend(String eventName, String[] propertyName, String[] propertyValue, boolean restrictEndUsers) {

        if (log.isDebugEnabled()) {
            log.debug("In trafficEventSend, eventName=" + eventName + ", propertyName=" + Arrays.asList(propertyName) +
                    ", propertyValue=" + Arrays.asList(propertyValue) + ", restrictEndUsers=" + restrictEndUsers);
        }

        if (propertyName.length != propertyValue.length) {
            throw new PlatformAccessException(
                    EventType.SYSTEMERROR, "trafficEventSend:propertyName.length != propertyValue.length");
        }
        try {
            Restrictions restrictions = null;
            if (restrictEndUsers) {
                restrictions = new Restrictions();
                restrictions.setEnduserRestricted(true);
            }

            iTrafficEventSender.reportTrafficEvent(makeEvent(eventName, propertyName, propertyValue), restrictions);
        } catch (TrafficEventSenderException e) {
            throw new PlatformAccessException(
                    EventType.SYSTEMERROR, "trafficEventSend:eventName=" + eventName, e);
        }
    }

    private TrafficEvent makeEvent(String eventName, String[] propertyName, String[] propertyValue) {
        TrafficEvent trEvent = new TrafficEvent(eventName);

        // HT95859 - First set the properties
        for (int i = 0; i < propertyName.length; i++) {
            trEvent.setProperty(propertyName[i].trim(), propertyValue[i]);
        }
       
        // Then do not addSessionId for TUI generated MDRs for message status change
        if(trEvent.getProperties().get("objecttype")!=null && 
                !(trEvent.getProperties().get("objecttype").equals(MdrConstants.MESSAGE))){
            addSessionId(trEvent);
        }
        
        return trEvent;
    }

	private void addSessionId(TrafficEvent trEvent) {
        String sessionId = executionContext.getSession().getId();
        if (log.isDebugEnabled()) {
            log.debug("In addSessionId, sessionId=" + sessionId);
        }
        trEvent.setProperty(SESSIONID, sessionId);
    }
}
