/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.trafficeventsender;

import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Singleton-class for the configuration of the TrafficEventsender. Contains a RadiusConfiguration and an
 * MfsConfiguration.
 *
 * @author ermmaha
 */
public class TrafficEventSenderConfiguration {
    private static final String ENABLED_ATTR = "enabled";
    private static final String TYPE_ATTR = "type";
    private static final String RADIUS_TYPE = "radius";

    private List<EventElement> eventElements = new ArrayList<EventElement>();

    private IConfiguration configuration;
    private RadiusConfiguration radiusConfiguration;
    private MfsConfiguration mfsConfiguration;

    private static TrafficEventSenderConfiguration instance = new TrafficEventSenderConfiguration();

    private TrafficEventSenderConfiguration() {
        radiusConfiguration = new RadiusConfiguration();
        mfsConfiguration = new MfsConfiguration();
    }

    /**
     * Returns the singleton instance of this class
     *
     * @return the singleton instance
     */
    public static TrafficEventSenderConfiguration getInstance() {
        return instance;
    }

    /**
     * @return the RadiusConfiguration instance
     */
    public RadiusConfiguration getRadiusConfiguration() {
        return radiusConfiguration;
    }

    /**
     * @return the MfsConfiguration instance
     */
    public MfsConfiguration getMfsConfiguration() {
        return mfsConfiguration;
    }

    /**
     * Sets the configuration. This method should only be called once when the Provisoning Manager is initiated.
     *
     * @param configuration The configuration instance.
     * @throws IllegalArgumentException If <code>config</code> is <code>null</code>.
     */
    void setConfiguration(IConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("Parameter configuration is null");
        }
        this.configuration = configuration;
    }

    /**
     * Reads configuration parameters.
     *
     * @throws ConfigurationException if configuration could not be read.
     */
    void update() throws ConfigurationException {
        mfsConfiguration.readConfiguration(configuration.getGroup(CommonOamManager.TRAFFIC_EVENTS_CONF));  //read slamdown and mcn info
        radiusConfiguration.readConfiguration(configuration);  //From backend
    	readConfiguration(); //read TrafficEventSender traffic events
    }

    /**
     * Retrieves the EventElement list.
     *
     * @return the EventElement list
     */
    List<EventElement> getEventElements() {
        return eventElements;
    }

    /**
     * Retrieves the EventElement that corresponds to the name
     *
     * @param name
     * @return the EventElement object, null if not found
     */
    EventElement getEventElement(String name) {
        for (EventElement element : eventElements) {
            if (element.getName().equals(name)) return element;
        }
        return null;
    }


    protected void readConfiguration() throws ConfigurationException {
        List<EventElement> newEventElements = new ArrayList<EventElement>();

        //read from trafficevents.conf
        IGroup trafficEventsGroup = configuration.getGroup(CommonOamManager.TRAFFIC_EVENTS_CONF);
		Map<String, Map<String, String>> trafficEventsTable = trafficEventsGroup.getTable("TrafficEvents.Table");

		Set<String> trafficEventsKeySet = trafficEventsTable.keySet();
		Iterator<String> trafficEventsIterator = trafficEventsKeySet.iterator();

		while(trafficEventsIterator.hasNext()){
			String name = trafficEventsIterator.next();
			EventElement e = new EventElement(name);

			Map<String, String> trafficEventMap = trafficEventsTable.get(name);
			e.setEnabled(Boolean.parseBoolean(trafficEventMap.get(ENABLED_ATTR)));
			e.setType(trafficEventMap.get(TYPE_ATTR));
			newEventElements.add(e);

		}

        eventElements = newEventElements;
    }

    enum EventType {
        RADIUS, MFS
    }

    class EventElement {
        private String name;
        private boolean enabled;
        private EventType type = EventType.RADIUS;

        EventElement(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }

        void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        boolean isEnabled() {
            return enabled;
        }

        void setType(String typeStr) {
            if (typeStr != null) {
                if (typeStr.equalsIgnoreCase(RADIUS_TYPE)) {
                    type = EventType.RADIUS;
                } else {
                    type = EventType.MFS;
                }
            }
        }

        EventType getType() {
            return type;
        }

    }
}
