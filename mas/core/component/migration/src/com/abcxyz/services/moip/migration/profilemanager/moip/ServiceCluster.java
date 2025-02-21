/*
 * Copyright (c) 2008 Abcxyz AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip;

import com.abcxyz.services.moip.migration.profilemanager.moip.cache.TimedCache;
import com.mobeon.common.logging.HostedServiceLogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.profilemanager.ProfileManagerException;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

/**
 * Representation of a service cluster (Compound Service)
 *
 * @author qtommlu
 */
public class ServiceCluster {

  private static final HostedServiceLogger log = 
    new HostedServiceLogger(ILoggerFactory.getILogger(ServiceCluster.class));
  private ProfileAttributes profileAttributes = null;
  private Set<ServiceInstance> instances = new HashSet<ServiceInstance>();
  private static final String EM_END_USER_SERVICE_DN = "emenduserservicedn";
  private static final String EM_COMPOUND_SERVICE_TYPE = "emcompoundservicetype";
  private static final String EM_COMPOUND_SERVICE_ID = "emcompoundserviceid";
  private static final String EM_COMPOUND_SERVICE_NAME = "emcompoundservicename";
  private static final String EM_COMPOUND_SERVICE_DN = "emcompoundservicedn";
  
  /**
   * Creates a service cluster (compound service) using specified searcher
   * for instance attribute fetch and attributes for initiating the cluster
   * settings (type, emserviceinstancedn etc).
   * 
   * @param attributes cluster attributes
   * @param s Searcher object to use
   * @throws ProfileManagerException
   */
  public ServiceCluster(ProfileAttributes attributes, Searcher s, TimedCache<String, ProfileAttributes> instanceCache) 
  throws ProfileManagerException 
  {
      profileAttributes = attributes;    
      /* Create and store the instances here */
      ProfileAttribute instanceAttributes = attributes.get(EM_END_USER_SERVICE_DN);
      if (instanceAttributes != null) {
          String [] instanceDns = instanceAttributes.getData();
          for (int i=0; i<instanceDns.length; i++) {
              if (instanceDns[i] != null) {
                  if (log.isDebugEnabled()) {
                      log.debug("Found ServiceInstance with dn=" + instanceDns[i]);
                  }
                  instances.add(new ServiceInstance(instanceDns[i], s, instanceCache));
              }        
          }
      }
  }
  
  public Set<Map.Entry<String, ProfileAttribute>>getClusterAttributes() {
      Map<String, ProfileAttribute> al = new HashMap<String, ProfileAttribute>();
      al.put(EM_COMPOUND_SERVICE_TYPE, profileAttributes.get(EM_COMPOUND_SERVICE_TYPE));
      al.put(EM_COMPOUND_SERVICE_ID, profileAttributes.get(EM_COMPOUND_SERVICE_ID));
      al.put(EM_COMPOUND_SERVICE_NAME, profileAttributes.get(EM_COMPOUND_SERVICE_NAME));
      al.put(EM_COMPOUND_SERVICE_DN, new ProfileAttribute(new String [] {profileAttributes.getDistinguishedName()})); // CLPD needs this too
      return al.entrySet();
  }
  
  /**
   * @return an array of service instances contained in this cluster
   */
  public ServiceInstance [] getInstances() {
	  if (instances != null){
		  return (ServiceInstance[])instances.toArray();
	  }	 else {
		  return null;
	  }
  }
  /**
   * @return the set of service instances contained in this cluster
   */
  public Set<ServiceInstance> getSetOfInstances() {
	  	return instances; 
  }

  /**
   * @return cluster type
   */
  public String getType() {
	  ProfileAttribute a = profileAttributes.get(EM_COMPOUND_SERVICE_TYPE);
	  if (a != null) {
		  return a.getData()[0];
	  } else {
		  return null;
	  }
  }
}
