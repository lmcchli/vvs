/**
 * 
 */
package com.mobeon.ntf.out.outdial;

/**
 * Object factory. 
 * @author egeobli
 */
public abstract class OdlFactory {
	
	/** Factory instance */
	private static OdlFactory instance;
	
	/**
	 * Returns the instance factory.
	 * @return Factory instance.
	 */
	public synchronized static OdlFactory getInstance() {
		if (instance == null) {
			instance = new DefaultOdlFactory();
		}
		return instance;
	}
	
	/**
	 * Sets the factory instance.
	 * @param factory Factory instance.
	 */
	protected static synchronized void setInstance(OdlFactory factory) {
		instance = factory;
	}

	/**
	 * Creates a new event store.
	 * @return Event store.
	 */
	public abstract IEventStore createEventStore();
}
