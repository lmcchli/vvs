/**
 *
 */
package com.mobeon.common.trafficeventsender.mfs;

import java.util.Properties;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.trafficeventsender.TrafficEvent;

/**
 * Interface used by NtfNotifierEventHandler for both Slamdown and MCN event manager worker. 
 */
interface IEventManagerWorker {

	/**
	 * Immutable class for storing incoming events.
	 */
	class IncomingEventEntry {
		private String phone;
		private TrafficEvent event;

		IncomingEventEntry(String phoneNumber, TrafficEvent event) {
			this.phone = phoneNumber;
			this.event = event;
		}

		String getPhoneNumber() {
			return phone;
		}

		TrafficEvent getEvent() {
			return event;
		}
	}

	class TimeoutEventEntry {
		private Properties properties;
		private String nextEventId;

		TimeoutEventEntry(Properties properties, String nextEventId) {
			this.properties = properties;
			this.nextEventId = nextEventId;
		}

		Properties getProperties() {
			return properties;
		}

		String getNextEventId() { 
		    return this.nextEventId;
		}
	}

	public void enqueue(IncomingEventEntry entry);

	public void enqueue(TimeoutEventEntry entry);

	ILogger getLogger();
}

