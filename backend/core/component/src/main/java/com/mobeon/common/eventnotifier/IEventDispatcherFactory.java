package com.mobeon.common.eventnotifier;

/**
 * The factory used to instantiate IEventDispatcher objects.
 *
 * @author Torsten Eriksson
 */
public class IEventDispatcherFactory {
	/*
	 * Returns a new MulticastDispatcher.
	 */
	public static IEventDispatcher getEventDispatcher(){
		return new MulticastDispatcher();
	}
}
