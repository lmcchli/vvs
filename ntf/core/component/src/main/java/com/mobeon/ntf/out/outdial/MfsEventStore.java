/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.mobeon.ntf.out.outdial;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Properties;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.InvalidOdlEventException;
import com.abcxyz.services.moip.ntf.event.OdlEvent;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.common.trafficeventsender.mfs.MfsEventFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager.FileStatusEnum;

/**
 * Implements a persistent event store that is connected to MFS.
 * <p>
 * When storing or retrieving events, the phone number is used to locate the base
 * path of the event file on MFS. The event is store in a file that has the event's key as its
 * name. This file is a property file.
 * </p>
 * <p>
 * The event store registers itself as an observer of all loaded or saved events. This
 * way it updates the status of the event when changed.
 * </p>
 */
class MfsEventStore implements IEventStore, Observer {

    /** Prefix for out-dial event files. */
    static final String OUTDIAL_EVENT_FILE_PREFIX = "odl-";
    public static final String OUTDIAL_PHONE_ON_LOCK_FILE = "outdial_phoneon.lock";

    private static LogAgent logger = NtfCmnLogger.getLogAgent(MfsEventStore.class);
	private MfsEventManager mfs = MfsEventFactory.getMfsEvenManager();

	public OdlEvent get(String subscriberNumber, String notificationNumber) {
        String identity = subscriberNumber + " : " + notificationNumber;
        OdlEvent odlEventFound = null;

        List<OdlEvent> odlEventList = this.get(notificationNumber);
        if (odlEventList != null) {
            for (OdlEvent odlEvent : odlEventList) {
                if (odlEvent.getRecipentId().equals(subscriberNumber)) {
                    logger.debug("Outdial persistent file found and matching for " + identity);
                    odlEventFound = odlEvent;
                    break;
                } else {
                    logger.debug("Outdial persistent file found (" + odlEvent.getIdentity() + ") but not matching for " + identity);
                }
            }
        }

        if (odlEventFound == null) {
            logger.debug("No Outdial persistent file found for " + identity);
        }
        return odlEventFound;
    }

    /**
	 * Reads the MFS private directory for the specified number and returns
	 * the list of active out dial events.
	 *
	 * @param number User's telephone number
	 * @return List of active out-dial events.
	 * @see com.mobeon.ntf.out.outdial.IEventStore#get(java.lang.String)
	 */
	@Override
	public List<OdlEvent> get(String number) {
		//
		// Read event list for subscriber.
		//
		String[] eventKeys = mfs.getOutdialEvents(number);
		if (eventKeys == null) {
			return null;
		}

		//
		// Build event list
		//
		ArrayList<OdlEvent> eventList = new ArrayList<OdlEvent>(eventKeys.length);
		for (String key : eventKeys) {
			try {
				Properties props = mfs.getProperties(number, OUTDIAL_EVENT_FILE_PREFIX + key);
				if (props == null) {
					continue;
				}

				OdlEvent event = new OdlEvent(props);
				eventList.add(event);

				// Register as an observer for each event
				event.addObserver(this);

			} catch (InvalidOdlEventException e) {
				// Event file corruption, remove this file
				logger.error("Deleting corrupted out-dial event for subscriber " + number +
						" with key " + key +
						" for the following reason: " + e.getMessage());
				try {
					mfs.removeFile(number, OUTDIAL_EVENT_FILE_PREFIX + key);
				} catch (TrafficEventSenderException e1) {
					logger.error("Out-dial event deletion failed for :<" + number +
							"> and key <" + key +
							"> with the following reason: " + e1.getMessage());
				}
				continue;
			}
		}

		return eventList;
	}

	/* (non-Javadoc)
	 * @see com.mobeon.ntf.out.outdial.IEventStore#put(java.lang.String, com.abcxyz.services.moip.ntf.event.OdlEvent)
	 */
	@Override
	public void put(String number, OdlEvent event) {
		// Save event to MFS
	    try {
	        Properties props = event.getEventProperties();

	        if (event.getReferenceId() != null && event.getReferenceId().length() > 0) {
	            props.put(OdlEvent.SCHEDULER_ID, event.getReferenceId());
	        } else {
	            props.remove(OdlEvent.SCHEDULER_ID);
	        }
	        if (event.getSchedulerIdReminder() != null && event.getSchedulerIdReminder().length() > 0) {
	            props.put(OdlEvent.SCHEDULER_ID_REMINDER, event.getSchedulerIdReminder());
	        } else {
	            props.remove(OdlEvent.SCHEDULER_ID_REMINDER);
	        }

	        mfs.storeProperties(number, OUTDIAL_EVENT_FILE_PREFIX + event.getOdlEventKey(), props);
	    } catch (TrafficEventSenderException e) {
	        logger.error("Cannot save event <" + event.getOdlEventKey() +
	                "> for number <" + number +
	                "> to persistent storage for the following reason: <" + e.getMessage() + ">");
	    }

	    // Register as an observer
	    event.addObserver(this);
	}

	/* (non-Javadoc)
	 * @see com.mobeon.ntf.out.outdial.IEventStore#put(java.lang.String, java.util.List)
	 */
	@Override
	public void put(String number, List<OdlEvent> eventList) {
		// Save event list to MFS
		for (OdlEvent event : eventList) {
			put(number, event);
		}
	}

	/* (non-Javadoc)
	 * @see com.mobeon.ntf.out.outdial.IEventStore#remove(java.lang.String)
	 */
	@Override
	public void remove(String number) {
		// Remove all events from store for this subscriber.
		String[] eventKeys = mfs.getOutdialEvents(number);
		if (eventKeys != null) {
			for (String key : eventKeys) {
				try {
					mfs.removeFile(number, OUTDIAL_EVENT_FILE_PREFIX + key);
				} catch (TrafficEventSenderException e) {
					logger.debug("Cannot remove event <" + key +
							"> for number <" + number +
							"> from persistent storage for the following reason: <" + e.getMessage() + ">");
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.mobeon.ntf.out.outdial.IEventStore#remove(com.abcxyz.services.moip.ntf.event.OdlEvent)
	 */
	@Override
	public void remove(OdlEvent event) {
		// Remove event from store.
		try {
			mfs.removeFile(event.getTelNumber(), OUTDIAL_EVENT_FILE_PREFIX + event.getOdlEventKey());
		} catch (TrafficEventSenderException e) {
			logger.debug("Cannot remove event <" + event.getOdlEventKey() +
					"> for number <" + event.getTelNumber() +
					"> from persistent storage for the following reason: <" + e.getMessage() + ">");
		}
		event.deleteObserver(this);
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		// Update the received event on store.
		OdlEvent event = (OdlEvent) arg;
		put(event.getTelNumber(), event);
	}

	@Override
    public FileStatusEnum fileExistsValidation(OdlEvent odlEvent, int validityInMin) {
	    return mfs.fileExistsValidation(odlEvent.getTelNumber(), OUTDIAL_EVENT_FILE_PREFIX + odlEvent.getOdlEventKey(), validityInMin, true);
    }                
}
