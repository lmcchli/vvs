/*
 * Copyright (c) 2008 Abcxyz AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip;

import javax.naming.directory.SearchControls;

import com.abcxyz.services.moip.migration.profilemanager.moip.cache.TimedCache;
import com.mobeon.common.logging.HostedServiceLogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.profilemanager.ProfileManagerException;

import java.util.TreeSet;

/**
 * Service instance representation. 
 * Reads its configuration at instantiation.
 *
 * @author qtommlu
 */
public class ServiceInstance {

	private ProfileAttributes instanceAttributes = null;
	private int prio = 0;
	private String type = null;
        private static final String OBJECT_CLASS = "(objectclass=*)";
        private static final String EM_END_USER_SERVICE_PRIORITY = "emenduserservicepriority";
        
        private static final HostedServiceLogger log = 
            new HostedServiceLogger(ILoggerFactory.getILogger(ServiceInstance.class));
	/**
	 * Constructs a service instance using searcher to populate 
	 * the instance settings.
	 * 
	 * @param emServiceInstanceDn instance dn
	 * @param s searcher to use
	 * @throws ProfileManagerException
	 */
	public ServiceInstance(String emServiceInstanceDn, Searcher s, TimedCache<String, ProfileAttributes> instanceCache) 
	throws ProfileManagerException 
	{
		/* This is used to construct the "type" of instance
		 * represented by a unique combination of sorted emconf-
		 * objectclasses.
		 */
		TreeSet<String> tmpTreeSet = new TreeSet<String>();
		if (emServiceInstanceDn != null) {
			instanceAttributes = instanceCache.get(emServiceInstanceDn);
			if (instanceAttributes == null) { // Cache miss
				try {
					instanceAttributes = s.retriedSearch(emServiceInstanceDn, 
							OBJECT_CLASS, 
							SearchControls.OBJECT_SCOPE);
				} catch (ProfileManagerException e) {
					; // Ignore the bad instance
				}
				if (instanceAttributes == null) { // LDAP miss
					if (log.isInfoEnabled())
						log.info("Service instance not found: " + emServiceInstanceDn);
				} else {
					instanceCache.put(emServiceInstanceDn, instanceAttributes);
				}
			}
			if (instanceAttributes != null) {
				ProfileAttribute p = instanceAttributes.get(EM_END_USER_SERVICE_PRIORITY);
				if (p != null){
					try {
						prio = Integer.parseInt(p.getData()[0]);
					} catch (NumberFormatException ne) {
						prio = 0;
					}
				}
				ProfileAttribute o = instanceAttributes.get("objectclass");
				if (o != null){
					String [] classes = o.getData();
					for (int i = 0; i < classes.length; i++) {
						if (classes[i].toLowerCase().matches("emconf(.*)")) {
							tmpTreeSet.add(classes[i].toLowerCase());
						}
					}
				}
				type = tmpTreeSet.toString();
				tmpTreeSet = null;
			} else {
				// Bad instance - the prio = 0, type=""
				prio=0;
				type="";
			}
		}
	}
	/**
	 * Return a string array reprecenting the instance attribute used.
	 * @param attributeName attribute
	 * @return value of the given attribute
	 */
	public String [] getInstnaceAttribute(String attributeName) {
		ProfileAttribute tmp = instanceAttributes.get(attributeName);
		if (tmp != null) {
			return tmp.getData();
		} else {
			return null;
		}
	}
	
	/**
	 * @return the ServiceInstance InstanceAttributes
	 */
	public ProfileAttributes getInstanceAttributes() {
		return instanceAttributes;
	}
	/**
	 * 0 means no priority. 1 before 2.
	 * @return priority
	 */
	public int getPrio() {
		return prio;  
	}

	/**
	 * @return a set of emConf* strings representing an instance type
	 */
	public String getType() {
		return type;
	}
}
