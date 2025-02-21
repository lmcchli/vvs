/*
 * Copyright (c) 2008 Abcxyz AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip;

import com.abcxyz.services.moip.migration.profilemanager.moip.cache.TimedCache;
import com.mobeon.common.logging.HostedServiceLogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.profilemanager.ProfileManagerException;

import javax.naming.directory.SearchControls;

import java.lang.reflect.Array;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Representation of a segmented cos containing clusters (compound services), 
 * and a base configuration.
 *
 * @author qtommlu
 */
public class SegmentedCos {

    private Set<ServiceCluster> clusters = new HashSet<ServiceCluster>();
    private ProfileAttributes basicSettings = null;
    private Set<ServiceCluster>userClusters = new HashSet<ServiceCluster>();
    private Set<String>userServiceTypes = new HashSet<String>();
    private HashMap<String, Integer>usedTypePriorities = new HashMap<String,Integer>();
    private HashMap<String, Integer>userUsedTypePriorities = new HashMap<String,Integer>();
    private static final String OBJECT_CLASS = "(objectclass=*)";
    private static final String EM_COMPOUND_SERVICE_TYPE = "emcompoundservicetype";
    private static final String EM_COMPOUND_SERVICE_NAME = "emcompoundservicename";
    private static final String EM_USER_NTD = "emuserntd";
    private static final String EM_SERVICE_DN = "emservicedn";    
    private static final String TTS_EMAIL_ENABLED = "ttsemailenabled";
    private static final String FAX_ENABLED = "faxenabled";
    private static final String TTS_EMAIL = "ttsemail";
    private static final String FAX = "fax";
    
    private TreeSet<String> accumulatedUserNtd = new TreeSet<String>();    
    private TreeSet<String> emServiceDns = new TreeSet<String>();
    private Set <String> translations = new HashSet<String>();
    private HashMap<String, String> emServiceDnTranslations = new HashMap<String, String>();
    private BaseContext baseContext = null;
    private static final HostedServiceLogger log = 
        new HostedServiceLogger(ILoggerFactory.getILogger(SegmentedCos.class));

    /* Special handling translations to be back/forwards compatible.
     * Static from schema ref, no use configuring.
     */
    private void init() {
        emServiceDnTranslations.put("emviaextprovider", "prerecordedgreeting_email");
        emServiceDnTranslations.put("emviatelephony", "prerecordedgreeting_tui");
        emServiceDnTranslations.put("emmessagedeletedrecovery", "message_recovery");
        emServiceDnTranslations.put("emmessageforward", "forwardmessagewithoutcomment");
        emServiceDnTranslations.put("emmessageforwardcomment", "forwardmessagewithcomment");
        emServiceDnTranslations.put("emmessagesend", "sendvoicemessage");
    }
    
    /**
     * Creates a new <code>SegmentedCos</code> instance.
     * Note: userServiceClusterDn must be null for emServiceClusterDn to be read.
     * 
     * @param oldCosSettings the old settings used to locate basic info
     * @param emServiceClusterDn the list of clusters, or null
     * @param s the searcher to use
     * @param userServiceClusterDn one or more references users override Dns. Null allowed.
     */
    public SegmentedCos(
            BaseContext baseContext,
            ProfileAttributes oldCosSettings, 
            String [] emServiceClusterDn, Searcher s,
            String [] userServiceClusterDn,
            TimedCache<String, ProfileAttributes> clusterCache,
            TimedCache<String, ProfileAttributes> instanceCache) 
    throws ProfileManagerException 
    {
    	if (log.isInfoEnabled())
            log.info("SegmentedCos() with" + (emServiceClusterDn==null ? "no" : "") + " and " + (userServiceClusterDn == null ? "no" : "") + " user clusters.");
    	
        this.baseContext = baseContext;
        init();
        if (oldCosSettings != null) {
            this.basicSettings = oldCosSettings;            
        }
        /* Get the Set of user clusters (if any) */
        if (userServiceClusterDn != null) {
            for (int i=0; i< userServiceClusterDn.length; i++) {

                ProfileAttributes userClusterResult = clusterCache.get(userServiceClusterDn[i]); 
                if (userClusterResult == null) {
                    // Cache miss
                    try {
                        userClusterResult = s.retriedSearch(userServiceClusterDn[i], OBJECT_CLASS, SearchControls.OBJECT_SCOPE);
                    } catch (ProfileManagerException e) {
                        ; // Ignore the bad cluster
                    }
                    if (userClusterResult != null) {
                        clusterCache.put(userServiceClusterDn[i],userClusterResult);
                    } else {
                        // Not found in cache or LDAP search
                        if (log.isInfoEnabled())
                            log.info("Ignoring Compound Service override (not found): " + userServiceClusterDn[i]);
                        continue;
                    }
                }
                /* All clusters must have emcompoundservicetype! */
                ProfileAttribute type = userClusterResult.get(EM_COMPOUND_SERVICE_TYPE);
                if (type != null && type.getData()[0] != null) {	  
                    userServiceTypes.add(type.getData()[0].toLowerCase());
                } else {
                    log.warn("Ignoring Compound Service without service type");
                    continue;
                }
                if (log.isDebugEnabled()) {
                    ProfileAttribute name = userClusterResult.get(EM_COMPOUND_SERVICE_NAME);
                    if (name != null && type != null) {
                        if (name.getData() != null && type.getData() != null) {
                            log.debug("Found USER ServiceCluster named <" + 
                                    name.getData()[0] + "> of type <" +  
                                    type.getData()[0] + ">");
                        }
                    }
                }
                userClusters.add(new ServiceCluster(userClusterResult, s, instanceCache));
            }
        }

        /* Get the ordinary CoS clusters - TR31883 - don't read these if there are userClusters!*/
        if (emServiceClusterDn != null && userServiceClusterDn == null) {
            /* This is where the object is instansiated, through LDAP searches */
            for (int i=0; i< emServiceClusterDn.length; i++) {
                ProfileAttributes clusterResult = clusterCache.get(emServiceClusterDn[i]);
                if (clusterResult == null) {
                    // Cache miss
                    try {
                        clusterResult = s.retriedSearch(emServiceClusterDn[i], 
                                OBJECT_CLASS, 
                                SearchControls.OBJECT_SCOPE);
                    } catch (ProfileManagerException e) {
                        ; // Ignore the bad cluster
                    }
                    if (clusterResult != null) {
                        clusterCache.put(emServiceClusterDn[i],clusterResult);
                    } else {
                        if (log.isInfoEnabled())
                            log.info("Ignoring Compound Service (not found): " + emServiceClusterDn[i]);   
                    }
                }
                if (clusterResult != null) {
                    if (log.isDebugEnabled()) {
                        ProfileAttribute type = clusterResult.get(EM_COMPOUND_SERVICE_TYPE);
                        ProfileAttribute name = clusterResult.get(EM_COMPOUND_SERVICE_NAME);
                        if (name != null && type != null) {
                            if (name.getData() != null && type.getData() != null) {
                                log.debug("Found ServiceCluster named <" + 
                                        name.getData()[0] + "> of type <" +  
                                        type.getData()[0] + ">");
                            }
                        }
                    }
                    clusters.add(new ServiceCluster(clusterResult, s, instanceCache));
                }
            } /* end-multivalue-clusters-for */
        } 
    }

    /**
     * Gets the "flat" representation of a CoS using a base context.
     * 
     * @return a Class Of Service representation (ProfileAttributes) on "old" format.
     */
    public ProfileAttributes getCos()
    {
        return getCos(false);
    }

    /**
     * Merge any count of Array.
     *
     * @param arrays manay arrays
     * @return merged array
     */
    @SuppressWarnings ("unchecked")
    public static <T> T[] arrayMerge(T[]... arrays) {
        int count = 0;
        for (T[] array : arrays) {
            count += array.length;
        }
        // create new array
        T[] rv = (T[]) Array.newInstance(arrays[0][0].getClass(),count);
        int start = 0;
        for (T[] array : arrays) {
            System.arraycopy(array,0,rv,start,array.length);
            start += array.length;
        }
        return (T[]) rv;
    }
    
    /**
     * Gets the "flat" representation of a CoS (Service Profile) using a base context.
     * 
     * @param withOverrides If this is specified the user attributes is considered, i.e
     *        a correct CoS representation for the user is returned. The reason to
     *        ever set this to false is only when caching the CoS-only settings.
     *        
     * @return a Class Of Service representation (ProfileAttributes) on "old" format.
     */
    public ProfileAttributes getCos(boolean withOverrides) {
        /* Add settings from basic attributes */
        ProfileAttributes result = new ProfileAttributes(basicSettings.getContext());
        String [] keys = basicSettings.getContext().getConfig().getBasicAttributes();
        for (int i=0; i < keys.length; i++){
            if (log.isDebugEnabled()) {
                log.debug("Adding COS key :" + keys[i]);
            }
            ProfileAttribute basicAttribute = basicSettings.get(keys[i]); 
            if (basicAttribute != null) {
                String [] values = basicAttribute.getData(); 
                if (values != null) {
                    result.put(keys[i], new ProfileAttribute(values));
                } else {
                    log.info("Basic setting attribute " + keys[i] + " not found.");
                }
            } else {
                log.info("Basic setting attribute " + keys[i] + " not found.");
            }
        }
        populatePriorityMap(withOverrides);

        HashMap<String, ProfileAttribute> accumulatedAttributesMap = new HashMap<String, ProfileAttribute>();
        
        /* Add attributes from each cluster/instance */
        Iterator<ServiceCluster> c = clusters.iterator();
        while (c.hasNext()) {
            ServiceCluster next = c.next();
            
            // Add cluster attributes.. TR 31870
            for (Map.Entry<String, ProfileAttribute> e : next.getClusterAttributes()) {
                if (e != null && e.getKey() != null && e.getValue() != null) { 
                    // Should not need to check tihs, since all these are required..
                	ProfileAttribute p = accumulatedAttributesMap.get(e.getKey());
                	if (p == null) {
                		accumulatedAttributesMap.put(e.getKey(), e.getValue());
                	} else {
                		String [] tmp = arrayMerge(p.getData(), e.getValue().getData());
                		accumulatedAttributesMap.remove(p);
                		accumulatedAttributesMap.put(e.getKey(), new ProfileAttribute(tmp));
                	}
                }
            }
            for (Map.Entry<String, ProfileAttribute> entry : accumulatedAttributesMap.entrySet()) {            	
            	result.put(entry.getKey(), entry.getValue());
            }
            
            if (!withOverrides || !isOverriden(next.getType())) { // Don't add it if it is an override!
                Set<ServiceInstance> instanceSet = next.getSetOfInstances();
                Iterator<ServiceInstance> i = instanceSet.iterator();
                while (i.hasNext()){				  
                    ServiceInstance instance = i.next();
                    String key = instance.getType();
                    /* Only add instance settings of same type if prio is higher than already added */
                    if (priorityCheck(key, instance.getPrio(), withOverrides)) {					
                        ProfileAttributes instanceAttrs = instance.getInstanceAttributes();
                        if (instanceAttrs != null) {
                        	Set<Map.Entry<String, ProfileAttribute>> instanceMap = instanceAttrs.entrySet();
                        	Iterator<Map.Entry<String, ProfileAttribute>> im = instanceMap.iterator();
                        	while (im.hasNext()) {
                        		Map.Entry<String, ProfileAttribute> nextEntry = im.next();
                        		if (!specialHandling(nextEntry)) {
                        			result.put(nextEntry.getKey(), nextEntry.getValue());
                        		}
                        		if (log.isDebugEnabled()) {
                        			log.debug("Adding key :" + nextEntry.getKey());
                        		}
                        	}
                        }
                    } 
                }
            }
        }
        if (userClusters != null && withOverrides) {
            /* Add attributes from each USER cluster/instance */
        	accumulatedAttributesMap = new HashMap<String, ProfileAttribute>();
            Iterator<ServiceCluster> uc = userClusters.iterator();
            while (uc.hasNext()){
                ServiceCluster next = uc.next();
                // Add cluster results.. TR 31870
                for (Map.Entry<String, ProfileAttribute> e : next.getClusterAttributes()) {
                    if (e != null && e.getKey() != null && e.getValue() != null) { 
                        // Should not need to check tihs, since all these are required..
                    	// Accumulate these!? In schema guide these are single value.. assume only last is of relevance..
                    	ProfileAttribute p = accumulatedAttributesMap.get(e.getKey());
                    	if (p == null) {
                    		accumulatedAttributesMap.put(e.getKey(), e.getValue());
                    	} else {
                    		String [] tmp = arrayMerge(p.getData(), e.getValue().getData());
                    		accumulatedAttributesMap.remove(p);
                    		accumulatedAttributesMap.put(e.getKey(), new ProfileAttribute(tmp));
                    	}
                    }
                }
                for (Map.Entry<String, ProfileAttribute> entry : accumulatedAttributesMap.entrySet()) {
                	result.put(entry.getKey(), entry.getValue());
                }
                
                Set<ServiceInstance> instanceSet = next.getSetOfInstances();
                Iterator<ServiceInstance> ui = instanceSet.iterator();
                while (ui.hasNext()){
                    ServiceInstance instance = ui.next();
                    String key = instance.getType();
                    if (priorityCheck(key, instance.getPrio(), withOverrides)) {
                        ProfileAttributes instanceAttrs = instance.getInstanceAttributes();
                        if (instanceAttrs != null ) {
                        	Set<Map.Entry<String, ProfileAttribute>> instanceMap = instanceAttrs.entrySet();
                        	Iterator<Map.Entry<String, ProfileAttribute>> im = instanceMap.iterator();
                        	while (im.hasNext()) {
                        		Map.Entry<String, ProfileAttribute> nextEntry = im.next();
                        		if (!specialHandling(nextEntry)) {
                        			result.put(nextEntry.getKey(), nextEntry.getValue());
                        		}
                        		if (log.isDebugEnabled()) {
                        			log.debug("Adding USER key :" + nextEntry.getKey());
                        		}
                        	}
                        } 
                    }
                }
            }
        }
        specialHandling(result);
        return result;
    }
    /**
     * Checks if it is ok to add the instance of type "key"
     * @param key instance type
     * @param prio instance priority
     * @return true if ok to add the instance settings
     */
    private boolean priorityCheck(String key, int prio, boolean withOverrides) {
        boolean ret = false;
        if ((usedTypePriorities == null) ||
                (!usedTypePriorities.containsKey(key))){
            ret = true; /* null or no match, ok to add */
        } else {
            /* Match */
            if (prio == 0) { 
                /* We only store prio != 0, 0 is unprioritized */
                ret = false;
            } else {
                if (prio <= usedTypePriorities.get(key)){
                    /* Only if prio is the lowest, i.e equals the
                     * stored prio, the instnace should be added
                     */
                    ret = true;
                }
            }
        }

        boolean userRet = false;
        if ((userUsedTypePriorities == null) ||
                (!userUsedTypePriorities.containsKey(key))){
            userRet = true; /* null or no match, ok to add */
        } else {
            /* Match */
            if (prio == 0) { 
                /* We only store prio != 0, 0 is unprioritized */
                userRet = false;
            } else {
                if (prio <= userUsedTypePriorities.get(key)){
                    /* Only if prio is the lowest, i.e equals the
                     * stored prio, the instnace should be added
                     */
                    userRet = true;
                }
            }
        }
        if (withOverrides) {
            ret = userRet && ret;
        }

        if (log.isDebugEnabled()) {
            log.debug("Prio check key/value: " + key + "/" + prio + " returns " + ret);
        }
        return ret;
    }

    /**
     *  Populate prio-map (one for user & one for cos). 
     *  No 0 in map 1 is prioritized over 2.
     *  Example map with three types:
     *  [emconfone, emconftwo] 1
     *  [emconfone] 2
     *  [emconfone, emconfthree] 1
     *  
     *  @param withOverrides true if overrides are used
     */
    private void populatePriorityMap(boolean withOverrides) {
        /* Add priorities from each cluster/instance */
        Iterator<ServiceCluster> c = clusters.iterator();
        while (c.hasNext()){
            ServiceCluster next = c.next();
            if (!withOverrides || !isOverriden(next.getType())) { // Don't add it if it is an override!
                Set<ServiceInstance> instanceSet = next.getSetOfInstances();
                Iterator<ServiceInstance> i = instanceSet.iterator();
                while (i.hasNext()){
                    ServiceInstance instance = i.next();
                    String key = instance.getType();			
                    int prio = instance.getPrio();
                    /* Only add if prio is higher than already in! 0-prio is useless to add */
                    if (prio != 0 && // it can not be 0
                            (!usedTypePriorities.containsKey(key) || // non-zero and not already in
                                    (prio < usedTypePriorities.get(key)))) // already in but lower prio
                    {
                        usedTypePriorities.put(key, prio);
                        log.debug("Priomap.add(" + key + "," + prio + ")");
                    }
                }
            }
        }	  
        /* Then add user cluster prios */
        Iterator<ServiceCluster> uc = userClusters.iterator();
        while (uc.hasNext()){
            ServiceCluster next = uc.next();
            Set<ServiceInstance> instanceSet = next.getSetOfInstances();
            Iterator<ServiceInstance> ui = instanceSet.iterator();
            if (!withOverrides || !isOverriden(next.getType())) { // Don't add it if it is an override!
                while (ui.hasNext()){
                    ServiceInstance instance = ui.next();
                    String key = instance.getType();
                    int prio = instance.getPrio();
                    if (prio != 0 && // it can not be 0
                            (!usedTypePriorities.containsKey(key) || // non-zero and not already in
                                    (prio < usedTypePriorities.get(key)))) // already in but lower prio
                    {
                        userUsedTypePriorities.put(key, prio);
                        log.debug("Priomap.add(" + key + "," + prio + ")");
                    }
                }
            }
        }
    }

    /**
     * Used to check if service type is also in user profile 
     * @param serviceType compound service (cluster) type to look for
     * @return if the cluster is also in the user profile
     */
    private boolean isOverriden(String serviceType) {
        return ((userServiceTypes != null) && userServiceTypes.contains(serviceType.toLowerCase()));
    }
    
    /**
     * This aggregates userNTD and stores attributes for translation.
     * The void specialHandling(ProfileAttributes) should be called to add the translated/aggregated attributes.
     * 
     * @param entry Entry to check if it is to be handled in a special way
     * @return true if the enty is handled specially (is userNtd or mapped attr).
     */
    private boolean specialHandling(Map.Entry<String, ProfileAttribute> entry) {
        /* userNTD should be composed from all instances */
        if (entry.getKey().toLowerCase() == EM_USER_NTD) {
            String data[] = entry.getValue().getData()[0].split(",");
            for (int i = 0; i < data.length; i++) {
                accumulatedUserNtd.add(data[i].trim());
            }
            return true;
        }
        /* ttsmailenabled should be composed from emServiceDn=ttsemail */
        /* Faxenabled should be composed from emServiceDn=fax */
        if (entry.getKey().toLowerCase().matches(EM_SERVICE_DN)) {            
            String [] entries = entry.getValue().getData();
            for (int i = 0; i < entries.length; i++) {
                String data[] = entry.getValue().getData()[i].split("="); // servicename=foo
                String tmp = data[1].toLowerCase().trim();
                String r = tmp.substring(0, tmp.indexOf(","));
                if (r.matches(TTS_EMAIL) || r.matches(FAX)) {
                    emServiceDns.add(r); // only servicenames ttsemailenabled, faxenabled  
                } else {
                    emServiceDns.add(entry.getValue().getData()[i]);
                    // Add whole thing!
                }
            }
                return true;
        }
        /**
         *  emServiceDn should be composed from emViaRxtProvider, 
         * emViaTelephony, emMessageDeletedRecovery, emMessageForward,
         * emMessageForwardComment, emMessageSend (and all ordinary emServiceDns)
         * (Hard coded, the schema reference is fixed now!)
         */
        if (emServiceDnTranslations.containsKey(entry.getKey().toLowerCase().trim())) {
            // This is an entry that should be converted to a emServiceDn
        	// IF and only IF it is set to TRUE (TR 31973)
        	if (entry.getValue().getData()[0].toLowerCase().trim().matches("true")) {
        		translations.add(entry.getKey().toLowerCase().trim());
        	}
            return true;
        }
        
        return false;
    }

    /**
     * Applies the accumulated attributes handled in a special way in specialHandling(entry).
     * @param result profile attributes to modify
     */
    private void specialHandling(ProfileAttributes result) {

        // Handle userNTD
        Iterator<String> i = accumulatedUserNtd.iterator();        
        String ntd = "";
        while( i.hasNext()) {
            ntd = ntd.concat(i.next());
            if (i.hasNext()) {
                ntd = ntd.concat(",");
            }
        }
        accumulatedUserNtd.clear();
        
        if (ntd.length() > 0) {
            result.put(EM_USER_NTD, new ProfileAttribute(new String [] {ntd}));
        }
        
        if (emServiceDns.contains(TTS_EMAIL)) {
            result.put(TTS_EMAIL_ENABLED, new ProfileAttribute(new String[] {"yes"})); 
            emServiceDns.remove(TTS_EMAIL);
        }
        
        if (emServiceDns.contains(FAX)) {
            result.put(FAX_ENABLED, new ProfileAttribute(new String[] {"yes"})); 
            emServiceDns.remove(FAX);
        }
        
        
        String [] serviceDnArray = emServiceDns.toArray(new String[emServiceDns.size()]);
        // The above needs to be added later

        String [] generated = null;
        if (translations != null) {
            generated = translations.toArray(new String[translations.size()]);
            for (int g=0; g<generated.length; g++) {
                generated[g] = buildServiceDn(generated[g]);
            }
        }
        int serviceDnArrayLength = 0;
        if (serviceDnArray != null)
            serviceDnArrayLength = serviceDnArray.length;
        int generatedLength = 0;
        if (generated != null) {
            generatedLength = generated.length;
        }
        String [] res = new String[generatedLength + serviceDnArrayLength] ;
        if (serviceDnArray != null) {
            System.arraycopy(serviceDnArray, 0, res, 0, serviceDnArrayLength);
        }
        if (generated != null) {
            System.arraycopy(generated, 0, res, serviceDnArrayLength, generatedLength);
        }
        if (res != null && res.length != 0) {
            ProfileAttribute p = new ProfileAttribute(res);
            result.put(EM_SERVICE_DN, p);
        }        
        translations.clear();
        emServiceDns.clear();  
    }
    
    /**
     * Do the translation of emServiceDn names
     * @param s attribute name
     * @return emservicename=<translated name>, ou=services, o=<default searchbase>
     */
    private String buildServiceDn(String s) {        
        return "emservicename=" + emServiceDnTranslations.get(s) + ", ou=services, " + baseContext.getConfig().getDefaultSearchbase();
    }
    
    /**
     * Gets an array of all Service Clusters
     *
     * @return ServiceCluster array unordered. Null if no clusters.
     */
    public ServiceCluster []  getServiceClusters() {
        /* The list of all Clusters in type/priority order */
        if (clusters != null) {
            return (ServiceCluster[]) clusters.toArray();
        } else {
            return null;		  	
        }
    }
}
