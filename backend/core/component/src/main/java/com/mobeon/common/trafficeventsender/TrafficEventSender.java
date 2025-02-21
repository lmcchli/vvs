/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.trafficeventsender;

import com.mobeon.common.configuration.ConfigurationChanged;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.common.eventnotifier.Event;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.eventnotifier.IEventReceiver;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

/**
 * Implements the ITrafficEventSender interface. Has an instance of a RadiusClient and an instance of a EmailClient that
 * are used to handle the radius events and email events.
 *
 * @author ermmaha
 */
public class TrafficEventSender implements ITrafficEventSender, IEventReceiver {
    private static ILogger log = ILoggerFactory.getILogger(TrafficEventSender.class);

    private IConfiguration configuration;
    private boolean mfsClientEnabled = true;
	private boolean configUpdated = false;

    /**
     * Handles the MFS events
     */
    private MfsClient mfsClient;

    /**
     * No-arg constructor
     */
    public TrafficEventSender() {
    }

    /**
     * Constructor used for mocking the tests.
     *
     * @param emailClient
     * @param radiusClient
     */
    public TrafficEventSender(MfsClient mfsClient) {
        this.mfsClient = mfsClient;
    }

    /**
     * Initiates the object. Must be called after construction!
     */
    public void init() {

        configUpdated = true;
        TrafficEventSenderConfiguration trafficEventSenderConfiguration = TrafficEventSenderConfiguration.getInstance();
        trafficEventSenderConfiguration.setConfiguration(configuration);

        try {
            trafficEventSenderConfiguration.update();
        } catch (ConfigurationException e) {
            log.error("Exception in updateConfiguration "+e, e);
        }

        if (mfsClientEnabled) {
            try {
                mfsClient = new MfsClient();
            } catch (Exception e) {
                log.error("Exception create MfsClient: " + e, e);
            }
        }
    }

    /**
     * Set if the MfsClient should be enabled or disabled. (Default is enabled)
     *
     * @param mfsClientEnabled true if enabled, false if disabled
     */
    public void setMfsClientEnabled(boolean mfsClientEnabled) {
        this.mfsClientEnabled = mfsClientEnabled;
    }

    /**
     * Setter for TrafficEventSender's configuration
     *
     * @param configuration The configuration to use
     */
    public void setConfiguration(IConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Sets the event dispatcher that should be used to receive global events.
     *
     * @param eventDispatcher
     */
    public void setEventDispatcher(IEventDispatcher eventDispatcher) {
        eventDispatcher.addEventReceiver(this);
    }

    public void doEvent(Event event) {
        // Do nothing
    }

    /**
     * This method is used to receive global events fired by any event dispatcher in the system.
     * <p/>
     * Traffic Event Sender is only interested in the {@link com.mobeon.common.configuration.ConfigurationChanged}
     * event which is used to reload the configuration.
     */
    public void doGlobalEvent(Event event) {
        if (event instanceof ConfigurationChanged) {
            if (log.isDebugEnabled()) {
                log.debug("In doGlobalEvent: ConfigurationChanged event received, reloading configuration");
            }
            ConfigurationChanged configurationChanged = (ConfigurationChanged) event;
            this.configuration = configurationChanged.getConfiguration();
            updateConfiguration();
        }
    }

    public void reportTrafficEvent(TrafficEvent trafficEvent) throws TrafficEventSenderException {
        reportTrafficEvent(trafficEvent, true, true);
    }

    public void reportTrafficEvent(TrafficEvent trafficEvent, Restrictions restrictions)
            throws TrafficEventSenderException {

        if (restrictions != null) {
            reportTrafficEvent(trafficEvent, !restrictions.isEnduserRestricted(), !restrictions.isEventsystemRestricted());
        } else {
            reportTrafficEvent(trafficEvent, true, true);
        }
    }

    private void reportTrafficEvent(TrafficEvent trafficEvent, boolean sendMfs, boolean sendRadius)
            throws TrafficEventSenderException {

    	if(!configUpdated) {
    		updateConfiguration();
    	}

        String name = trafficEvent.getName();
        if (log.isDebugEnabled()) {
            log.debug("In reportTrafficEvent: name=" + name + ", sendMfs=" + sendMfs + ", sendRadius=" + sendRadius);
        }
        TrafficEventSenderConfiguration.EventElement element = TrafficEventSenderConfiguration.getInstance().getEventElement(name);

        if (element != null && !element.isEnabled()) {
            if (log.isDebugEnabled()) log.debug("In reportTrafficEvent: trafficevent " + name + " is not enabled");
            return;
        }

        if (element != null && element.getType() == TrafficEventSenderConfiguration.EventType.RADIUS) {
            if (sendRadius) {
                TrafficEventMdrHandler.sendTrafficEvent(trafficEvent);
            }
        } else {
            if (element == null) {
                log.debug("No trafficevent " + name + " is found in the configuration");
                throw new TrafficEventSenderException("No trafficevent " + name + " is found in the configuration");
            }
            if (mfsClient != null && sendMfs) {
                mfsClient.sendTrafficEvent(trafficEvent);
            }
        }
    }

    private void updateConfiguration() {
		configUpdated = true;
        TrafficEventSenderConfiguration trafficEventSenderConfiguration = TrafficEventSenderConfiguration.getInstance();
        trafficEventSenderConfiguration.setConfiguration(configuration);

        try {
            trafficEventSenderConfiguration.update();
        } catch (ConfigurationException e) {
            log.error("Exception in updateConfiguration "+e, e);
            // ToDo what to do here ?
            //throw new ServiceEnablerException("Could not configure Traffic event sender.");
        }

        if (mfsClientEnabled) {
        	mfsClient.updateConfiguration();
        }
    }

}
