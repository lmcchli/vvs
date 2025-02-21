/**
 * 
 */
package com.mobeon.ntf.out.outdial;

import java.util.List;

import com.abcxyz.services.moip.ntf.event.OdlEvent;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager.FileStatusEnum;

/**
 * Objects of type IEventStore store OdlEvent objects associated with
 * telephone numbers.
 * <p>
 * Implementation of IEventStore should be thread safe since event stores
 * are used in a multi-threading environment.
 * </p>
 * <p>
 * The returned list elements should also be synchronised in order to iterate
 * through elements without being disturbed by other threads.
 * </p>
 * @author egeobli
 */
public interface IEventStore {

	/**
	 * Stores an event and associate it with a telephone number.
	 * 
	 * @param number Number to which the event is associated.
	 * @param event Event.
	 */
	public void put(String number, OdlEvent event);
	
	/**
	 * Stores a list of events and associate it to the specified phone number.
	 * 
	 * @param number Number to which the events are associated.
	 * @param eventList List of events.
	 */
	public void put(String number, List<OdlEvent> eventList);
	
	/**
	 * Returns the events associated to the specified telephone number.
	 * 
	 * @param number Number for the event.
	 * @return Returns the associated events or null if none exists.
	 */
	public List<OdlEvent> get(String number);
	
	/**
	 * Return the event for the given subscriber and notification number 
	 * @param subscriberNumber subscriber
	 * @param notificationNumber notification
	 * @return OdlEvent, null if not found
	 */
	public OdlEvent get(String subscriberNumber, String notificationNumber);

	/**
	 * Removes the events associated to a telephone number.
	 * 
	 * @param number Telephone number.
	 */
	public void remove(String number);
	
	/**
	 * Removes the event associated with a telephone number.
	 * The phone number is retrieved from the event.
	 * 
	 * @param event Event to remove.
	 */
	public void remove(OdlEvent event);

	/**
	 * Check if the status file is obsolete (if exists) 
     * @param odlEvent OdlEvent
	 * @param validityInMin In minutes
	 * @return FileStatusEnum if the status file exists, validation performed (valid or not) or not
	 */
	public FileStatusEnum fileExistsValidation(OdlEvent odlEvent, int validityInMin);
}
