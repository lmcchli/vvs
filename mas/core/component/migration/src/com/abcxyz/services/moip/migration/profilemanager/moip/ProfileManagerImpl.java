/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip;

import com.abcxyz.services.moip.migration.profilemanager.moip.cache.TimedCache;
import com.abcxyz.services.moip.migration.profilemanager.moip.search.LdapFilterFactory;
import com.abcxyz.services.moip.migration.profilemanager.moip.search.ProfileCriteriaVisitor;
import com.abcxyz.services.moip.migration.profilemanager.moip.subscription.Subscription;
import com.mobeon.common.logging.HostedServiceLogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.provisionmanager.ProvisioningException;
import com.mobeon.common.util.criteria.Criteria;
import com.mobeon.common.util.executor.RetryException;
import com.mobeon.common.util.executor.TimeoutRetrier;
import com.mobeon.masp.profilemanager.HostException;
import com.mobeon.masp.profilemanager.ProfileManagerException;
import com.mobeon.masp.profilemanager.UnknownAttributeException;
import com.mobeon.masp.profilemanager.UserProvisioningException;

import javax.naming.*;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapName;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * Documentation
 *
 * @author mande
 */
public class ProfileManagerImpl implements IProfileManager {

    private static final HostedServiceLogger log = new HostedServiceLogger(ILoggerFactory.getILogger(ProfileManagerImpl.class));

    private String BILLINGPREFIX = "billingnumber";
    private String USERPREFIX = "uniqueidentifier";
    private static final IProfile[] EMPTY_PROFILE_LIST = new IProfile[0];
    private static final String[] LIMITSCOPE = new String[]{"limitscope"};

    private BaseContext context;
    private TimedCache<String, ProfileAttributes> cosCache;
    private TimedCache<String, ProfileAttributes> communityCache;
    private TimedCache<String, ProfileAttributes> userAdminCache;
    private TimedCache<String, ProfileAttributes> compoundServiceCache; /* cluster cache */
    private TimedCache<String, ProfileAttributes> endUserServiceCache;  /* instance cache */
    private static final String OBJECT_CLASS = "(objectclass=*)";
    private static final String OBJECT_CLASS_SEGMENTED_CONF_COS = "(objectclass=emCompoundService)";
    private String BILLING_NUMBER = "("+BILLINGPREFIX+"=*)";
    private static final String EM_COMPOUND_SERVICE_DN = "emcompoundservicedn";
    private static final String EM_COMPOUND_SERVICE_ID = "emcompoundserviceid"; 
    private Searcher searcher = null;
    
    public ProfileManagerImpl() {
    }

    public BaseContext getContext() {
        return context;
    }

    public void setContext(BaseContext context) throws ProfileManagerException {
        this.context = context;
        this.context.setProfileManager(this);
        this.BILLINGPREFIX = context.getConfig().getBillingPrefix();
        this.USERPREFIX = context.getConfig().getUserPrefix();
        this.BILLING_NUMBER = "("+BILLINGPREFIX+"=*)";
        
    }

    
    public IProfile getProfile(String dn)
	throws UnknownAttributeException, HostException {
        if (log.isInfoEnabled()) log.info("getProfile(dn=" + dn + ")");
        SearchControls ctls = getReadSearchControl();
        ctls.setSearchScope(SearchControls.OBJECT_SCOPE);
	IProfile[] profiles = getProfileWorker(
	    new GetProfileTask(dn, OBJECT_CLASS, ctls));
	if (profiles.length == 1) {
	    if (log.isInfoEnabled()) 
		log.info(
		    "getProfile(dn=" + dn + ") returns " 
		    + Arrays.toString(profiles)); 
	    return profiles[0];
	} else if (profiles.length > 1) {
	    // "shouldn't happen"
	    throw new HostException(
		"Multiple profiles with this dn; " + dn);
	}
	if (log.isInfoEnabled()) 
	    log.info(
		"getProfile(dn=" + dn + ") returns null"); 
	return null;
    }

    public void init() {
        int timeout = getContext().getConfig().getCosCacheTimeout();
        cosCache = new TimedCache<String, ProfileAttributes>(timeout);
        communityCache = new TimedCache<String, ProfileAttributes>(timeout);
        userAdminCache = new TimedCache<String, ProfileAttributes>(timeout);
        //segmentedCosCache = new TimedCache<String, ProfileAttributes>(timeout);
        compoundServiceCache = new TimedCache<String, ProfileAttributes>(timeout);
        endUserServiceCache = new TimedCache<String, ProfileAttributes>(timeout);
        searcher = new Searcher(context);
    }

    public IProfile[] getProfile(Criteria<ProfileCriteriaVisitor> criteria) throws UnknownAttributeException, HostException {
        if (log.isInfoEnabled()) log.info("getProfile(criteria=" + criteria + ")");
        IProfile[] profiles = getProfileWorker(null, criteria, false);
        if (log.isInfoEnabled())
            log.info("getProfile(Criteria<ProfileCriteriaVisitor>) returns " + Arrays.toString(profiles));
        return profiles;
    }

    public IProfile[] getProfile(String searchBase, Criteria<ProfileCriteriaVisitor> criteria)
            throws UnknownAttributeException, HostException {
        if (log.isInfoEnabled()) log.info("getProfile(searchBase=" + searchBase + ", criteria=" + criteria + ")");
        IProfile[] profiles = getProfileWorker(searchBase, criteria, false);
        if (log.isInfoEnabled())
            log.info("getProfile(String, Criteria<ProfileCriteriaVisitor>) returns " + Arrays.toString(profiles));
        return profiles;
    }

    public IProfile[] getProfile(Criteria<ProfileCriteriaVisitor> criteria, boolean limit)
            throws UnknownAttributeException, HostException {
        if (log.isInfoEnabled()) log.info("getProfile(criteria=" + criteria + ", limit=" + limit + ")");
        IProfile[] profiles = getProfileWorker(null, criteria, limit);
        if (log.isInfoEnabled())
            log.info("getProfile(Criteria<ProfileCriteriaVisitor>, boolean) returns " + Arrays.toString(profiles));
        return profiles;
    }

    public IProfile[] getProfile(String searchBase, Criteria<ProfileCriteriaVisitor> criteria, boolean limit)
            throws UnknownAttributeException, HostException {
        if (log.isInfoEnabled())
            log.info("getProfile(searchBase=" + searchBase + ",criteria=" + criteria + ", limit=" + limit + ")");
        IProfile[] profiles = getProfileWorker(searchBase, criteria, limit);
        if (log.isInfoEnabled())
            log.info("getProfile(String, Criteria<ProfileCriteriaVisitor>, boolean) returns " + Arrays.toString(profiles));
        return profiles;
    }

    /**
     * @param searchBase
     * @param criteria
     * @param limit
     * @return iprofile object
     * @throws UnknownAttributeException
     * @throws HostException
     * @logs.error "com.mobeon.common.profilemanager.HostException: Search failed. SearchBase&lt;searchBase&gt;
     * Filter&lt;(billingnumber=*)&gt;" - The search for a subscriber failed. Most probably due to problems with the
     * MUR host.
     */
    public IProfile[] getProfileWorker(
	String searchBase, 
	Criteria<ProfileCriteriaVisitor> criteria, 
	boolean limit)
            throws UnknownAttributeException, HostException {
        if (searchBase == null) {
            searchBase = getContext().getConfig().getDefaultSearchbase();
        }
        SearchControls ctls = getReadSearchControl();
        setLimitScope(ctls, limit);
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String ldapFilter = LdapFilterFactory.getLdapFilter(
	    criteria, getContext().getConfig().getApplicationAttributeMap());
	return getProfileWorker(
	    new GetProfileTask(searchBase, ldapFilter, ctls));
    }

    // Internal function used for both DN and filter search
    private IProfile[] getProfileWorker(GetProfileTask getProfileTask)
            throws UnknownAttributeException, HostException {
        TimeoutRetrier<IProfile[]> timeoutRetrier = 
	    new TimeoutRetrier<IProfile[]>(
                getProfileTask,
                getContext().getConfig().getTryLimit(),
                getContext().getConfig().getTryTimeLimit(),
                getContext().getConfig().getReadTimeout()
        );
        try {
            return timeoutRetrier.call();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof HostException) {
                // Already logged in call
                throw (HostException) e.getCause();
            } else {
                // This should not happen, TimeoutRetrier should only
                // throw HostExceptions from GetProfileTask Todo:
                // throw other exception? Must change interface in
                // that case...
                throw new HostException(
		    "GetProfileTask threw unexpected exception: " 
		    + e.getCause(), e.getCause());
            }
        } catch (TimeoutException e) {
            throw new HostException("GetProfileTask has timed out", e);
        } catch (InterruptedException e) {
            throw new HostException("GetProfileTask was interrupted", e);
        }
    }

    private StringBuilder getSearchErrorMessage(String searchBase, String filter) {
        StringBuilder errMsg = new StringBuilder();
        errMsg.append("Search");
        if (getContext().getConfig().getLimitScope()) {
            errMsg.append(" with limitScope");
        }
        errMsg.append(" failed. SearchBase<").append(searchBase).append(">");
        errMsg.append(" Filter<").append(filter).append(">");
        return errMsg;
    }

    /**
     * Sets the limitscope attribute for the search if requested and configuration allows it
     *
     * @param ctls  the search control to set the limitscope attribute for
     * @param limit if limitscope should be set or not
     */
    private void setLimitScope(SearchControls ctls, boolean limit) {
        if (getContext().getConfig().getLimitScope() && limit) {
            ctls.setReturningAttributes(LIMITSCOPE);
        }
    }

    SearchControls getReadSearchControl() {
        SearchControls readSearchControls = new SearchControls();
        readSearchControls.setTimeLimit(getContext().getConfig().getReadTimeout());
        return readSearchControls;
    }

    /**
     * Creates a subscriber from a searchresult. This implementation is designed for MoIP 6.0 LDAP schema.
     * Subclass the ProfileManagerImpl and overwrite this method to create and IProfile from the resulting
     * LDAP search.
     *
     * @param searchResult the search result to create a subscriber from
     * @param dirContext   the dir context to use for searching
     * @return a profile object
     * @throws HostException             when problems occur with the directory server
     * @throws UserProvisioningException when subscriber creation fails due to provision errors
     */
    protected IProfile createSubscriber(SearchResult searchResult, DirContext dirContext)
            throws UserProvisioningException, HostException {

        ProfileAttributes profileAttributes;
        try {
            profileAttributes = new ProfileAttributes(getContext(), searchResult);
        } catch (NamingException e) {
            throw new HostException("Could not read search result. " + e, e);
        }
        Subscriber subscriber = newSubscriber();
        ProfileAttributes billingResult;
        ProfileAttributes userResult;
        ProfileAttributes cosResult;
        ProfileAttributes communityResult;
        String name = profileAttributes.getDistinguishedName();
        switch (getLevel(name)) {
            case BILLING:
                billingResult = profileAttributes;
                userResult = getSuperLevelAttributes(billingResult, dirContext);
                cosResult = getCosResult(
                    getCosDn(userResult,billingResult), userResult, dirContext);
                communityResult = getCommunityResult(userResult, dirContext);
                break;
            case USER:
                userResult = profileAttributes;
                billingResult = getBillingResult(userResult, dirContext);
                cosResult = getCosResult(
                    getCosDn(userResult,billingResult), userResult, dirContext);
                communityResult = getCommunityResult(userResult, dirContext);
                break;
            default:
                throw new UserProvisioningException("Search result is not user or billing entry.");
        }
        Map<String, Map<ProfileLevel, ProfileAttribute>> dataMap;
        dataMap = new HashMap<String, Map<ProfileLevel, ProfileAttribute>>(
                getContext().getConfig().getUserRegisterAttributeMap().size()
        );
        addSearchResult(communityResult, ProfileLevel.COMMUNITY, subscriber, dataMap);
        addSearchResult(cosResult, ProfileLevel.COS, subscriber, dataMap);
        addSearchResult(userResult, ProfileLevel.USER, subscriber, dataMap);
        addSearchResult(billingResult, ProfileLevel.BILLING, subscriber, dataMap);
        populateSubscriber(subscriber, dataMap);
        subscriber.setCos(cosResult);
        return subscriber;
    }

    /**
     * If required, overwrite this method so it creates an intance of the class adapted to the VM system from which you want to extract messages.
     * @return a new instance of subscriber or one of its subclass
     */
    protected Subscriber newSubscriber() {
        return new Subscriber(getContext());
    }

    /**
     * Adds search results to a subscriber
     *
     * @param profileAttributes the search result to add
     * @param level
     * @param subscriber        the subscriber to add the result to
     * @param dataMap
     * @logs.warning "Could not retrieve next attribute" - The next method threw a NamingException
     * The NamingException subclass should indicate what the problem is.
     * @logs.warning "Could not check for more attributes" - The hasMore method threw a NamingException.
     * The NamingException subclass should indicate what the problem is.
     */
    private void addSearchResult(ProfileAttributes profileAttributes, ProfileLevel level, Subscriber subscriber, Map<String, Map<ProfileLevel, ProfileAttribute>> dataMap) {
        // This is needed to be able to handle a Greeting Admin as a Subscriber
        // If no result is submitted, just return.
        if (profileAttributes == null) {
            return;
        }
        subscriber.setDistinguishedName(level, profileAttributes.getDistinguishedName());
        Set<Map.Entry<String, ProfileAttribute>> entries = profileAttributes.entrySet();
        for (Map.Entry<String, ProfileAttribute> entry : entries) {
            addUserRegisterAttribute(entry, level, dataMap);
        }
    }

    private void addUserRegisterAttribute(Map.Entry<String, ProfileAttribute> entry, ProfileLevel level,
                                          Map<String, Map<ProfileLevel, ProfileAttribute>> dataMap) {

        String userRegisterName = entry.getKey();
        // Only handle "known" attributes
        if (getContext().getConfig().getUserRegisterAttributeMap().containsKey(userRegisterName)) {
            Map<ProfileLevel, ProfileAttribute> dataEntry;
            if (dataMap.containsKey(userRegisterName)) {
                // Add to existing entry
                dataEntry = dataMap.get(userRegisterName);
            } else {
                // Create new map entry
                dataEntry = new EnumMap<ProfileLevel, ProfileAttribute>(ProfileLevel.class);
            }
            // Add data
            dataEntry.put(level, entry.getValue());
            dataMap.put(userRegisterName, dataEntry);
        }
    }

    private void populateSubscriber(Subscriber subscriber, Map<String, Map<ProfileLevel, ProfileAttribute>> dataMap) {
        Map<String, Set<ProfileMetaData>> userRegisterAttributeMap = getContext().getConfig().getUserRegisterAttributeMap();
        for (Map.Entry<String, Map<ProfileLevel, ProfileAttribute>> entry : dataMap.entrySet()) {
            String userRegisterName = entry.getKey();
            if (userRegisterAttributeMap.containsKey(userRegisterName)) {
                Set<ProfileMetaData> profileMetaDataSet = userRegisterAttributeMap.get(userRegisterName);
                for (ProfileMetaData profileMetaData : profileMetaDataSet) {
                    List<ProfileLevel> searchOrder = profileMetaData.getSearchOrder();
                    ListIterator<ProfileLevel> listIterator = searchOrder.listIterator(searchOrder.size());
                    while (listIterator.hasPrevious()) {
                        ProfileLevel level = listIterator.previous();
                        Map<ProfileLevel, ProfileAttribute> value = entry.getValue();
                        if (value.containsKey(level)) {
                            subscriber.setAttribute(profileMetaData.getApplicationName(), value.get(level), level);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * @param profileAttributes
     * @param dirContext
     * @return
     * @throws UserProvisioningException
     * @throws HostException
     * @logs.warning "Search returned multiple billingentries. SearchBase&lt;searchBase&gt;
     * Filter&lt;(billingnumber=*)&gt;" - The search for billing entry returned multiple results. This could indicate
     * provisioning errors. The first billing entry will be used.
     */
    protected ProfileAttributes getBillingResult(ProfileAttributes profileAttributes, DirContext dirContext)
            throws UserProvisioningException, HostException {

        String searchBase = profileAttributes.getDistinguishedName();
        SearchControls ctls = getReadSearchControl();
        ctls.setSearchScope(SearchControls.ONELEVEL_SCOPE);
        try {
            NamingEnumeration<SearchResult> billingResults = dirContext.search(searchBase, BILLING_NUMBER, ctls);
            if (billingResults.hasMore()) {
                SearchResult billingResult = billingResults.next();
                // Only bother with more results, if emsecnb is defined and does not contain
                // current billingnumber (i.e. always aim to return the primary number entry)
                String billingNr = getBillingNrFromResult(billingResult);
                if (billingNr != null && emsecnbContainsBillingNr(profileAttributes, billingNr)) {
                    // Billingnr is in emsecnb, try to find one that is not..
                    while (billingResults.hasMore()) {
                        SearchResult result = billingResults.next();
                        String nextBillingNr = getBillingNrFromResult(result);
                        if (nextBillingNr != null 
                                && (!emsecnbContainsBillingNr(profileAttributes, nextBillingNr))) {
                            // Got one that is primary!
                            return new ProfileAttributes(getContext(), result);
                        }
                    }
                    // Did not find primary -warn and return first found as before
                    String errMsg = getSearchErrorMessage(
                            "Search returned multiple billingentries",
                            searchBase,
                            BILLING_NUMBER
                    );
                    log.warn(errMsg);
                }
                return new ProfileAttributes(getContext(), billingResult);
            } else {
                String errMsg = getSearchErrorMessage(
                        "Billing level search returned no result",
                        searchBase,
                        BILLING_NUMBER
                );
                throw new UserProvisioningException(errMsg);
            }
        } catch (NamingException e) {
            String errMsg = getSearchErrorMessage("Search failed", searchBase, BILLING_NUMBER);
            throw new HostException(errMsg, e);
        }
    }

    /**
     *  Does profileAttributes.emsecnb contain billingnr?
     * @param profileAttributes
     * @param billingnr
     * @return true if profileAttributes.emsecnb contains billingnr
     */
    private boolean emsecnbContainsBillingNr(ProfileAttributes profileAttributes, String billingnr) {
        if (!profileAttributes.containsKey("emsecnb")) { 
            return false;
        }
        ProfileAttribute p = profileAttributes.get("emsecnb");
        for (String d : p.getData()) {
            if (d != null && d.trim().matches(billingnr.trim())) {
                //log.debug("XXX MATCH: " + d + " | " + billingnr);
                return true;
            }
        }
        return false;
    }
    
    private String getBillingNrFromResult(SearchResult res) throws NamingException {
        ProfileAttributes tmp = new ProfileAttributes(getContext(), res);
        ProfileAttribute firstBillingnr = null;
        String billingNr = null;
        if (tmp != null) {
            firstBillingnr = tmp.get("billingnumber");
            if (firstBillingnr != null && firstBillingnr.getData() != null ){                        
                billingNr = firstBillingnr.getData()[0];
            }
        }
        return billingNr;
    }
    
    /**
     * This implementation is specific to MoIP 6.0 LDAP schema. Subclass ProfileManagerImpl
     * and overwrite this method to obtain specific behaviour.
     * Analyzes a distinguished name to get the entry level: user entry or billing entry.
     * If no level can be found, the level unknown is returned.
     *
     * @param name the distinguished name to analyze
     * @return the entry level of the submitted distinguished name
     */
    protected ProfileLevel getLevel(String name) {
        if (name.startsWith(BILLINGPREFIX)) {
            return ProfileLevel.BILLING;
        } else if (name.startsWith(USERPREFIX)) {
            return ProfileLevel.USER;
        }
        return ProfileLevel.UNKNOWN;
    }

    private boolean attributeMatch(String [] attributes, String key) {
    	if (attributes == null || key == null) {
    		return false;
    	}
    	for (String s : attributes) {
    		if (s.equalsIgnoreCase(key)) {
    			return true;
    		}
    	}
    	return false;
    }
    
    /**
     * Get CoS data for a particular user. Segmented CoS (FE31) with
     * Compound Services overrides are handled here.
     * @param cosdn DN of the base CoS
     * @param userAttributes User level attributed possibly containing
     *	Compound Services overrides.
     * @param dirContext Used for search 
     *	(TODO: Common search code for getCos() and this method)
     */
    protected ProfileAttributes getCosResult(
            String cosdn, ProfileAttributes userAttributes, DirContext dirContext)
    throws UserProvisioningException, HostException {
        boolean overrides = false;
        boolean segmented = false;
        String [] userAttributesCompoundServiceDn = null;

        if (userAttributes != null && userAttributes.containsKey(EM_COMPOUND_SERVICE_DN)) {
            userAttributesCompoundServiceDn = userAttributes.get(EM_COMPOUND_SERVICE_DN).getData();
            // This means there are user overrides.
            overrides = true;
        }
        if (cosdn == null) {
            // No cosdn could be found, may be greeting administrator, return null
            return null;
        }
        ProfileAttributes cosResult = cosCache.get(cosdn);
        if (cosResult == null) { // Cache miss
            cosResult = searcher.search(dirContext, cosdn, OBJECT_CLASS, SearchControls.OBJECT_SCOPE, null);
            if (cosResult != null) {
                cosCache.put(cosdn, cosResult);
            } else {
                // LDAP failure
                log.warn("ProfileManager cosResult null.");
                return null;
            }
        }
        ProfileAttribute objectclass = cosResult.get("objectclass");
        if (objectclass == null) {
        	segmented = false;
        } else { 
        	segmented = attributeMatch(objectclass.getData(), "emsegmentedconfcos");
        }
//        segmented = cosResult.containsKey(EM_COMPOUND_SERVICE_DN);
        /* Segmented cos */
        if (segmented) {
        	ProfileAttribute a = cosResult.get(EM_COMPOUND_SERVICE_DN);
    	    String[] emCompoundServiceDns = null;
    	    if (a != null) {
    	    	emCompoundServiceDns = a.getData();            
    	    }
//    	    if (emCompoundServiceDns[0].length() > 0) {
    	        SegmentedCos segmentedCos = null;
    	        try {
    	            segmentedCos = new SegmentedCos(getContext(),
                            cosResult, 
    	                    emCompoundServiceDns, 
    	                    searcher,
    	                    userAttributesCompoundServiceDn,
                            compoundServiceCache,
                            endUserServiceCache);
    	        } catch (ProfileManagerException pme){
    	            log.warn("ProfileManager Exception (Segmented COS returns old COS):", pme);
    	            return cosResult; // If new setting is broken, return old cos
    	        }
    	        if (segmentedCos != null) {
    	            ProfileAttributes profileSettings = segmentedCos.getCos();
    	            if (overrides) {
    	                return segmentedCos.getCos(true);
    	            } else {
    	                return profileSettings;
    	            }
    	        } else {
    	            log.warn("ProfileManager null (Segmented COS returns old COS)");
    	            return cosResult; /* ordinary old CoS */
    	        }
   //         }
    	}
    	return cosResult;
    }

    /**
     * Retrieves the cosdn from the user or billingnumber search
     * result. The "emservicedn" in the user attributes has precedence
     * over the "cosdn" in the billing attributes.
     *
     * @param userAttributes User Profile attributes
     * @param billingAttributes Billing attributes
     * @return distinguished name for the COS, or null if not found
     * @throws UserProvisioningException if the cosdn is empty.
     */
    private String getCosDn(
        ProfileAttributes userAttributes, ProfileAttributes billingAttributes)
        throws UserProvisioningException {

        String[] data = null;
        if (userAttributes.containsKey("emservicedn")) {
            data = userAttributes.get("emservicedn").getData();
        } else if (billingAttributes.containsKey("cosdn")) {
            data = billingAttributes.get("cosdn").getData();
        }

        if (data != null) {
            if (data.length > 0) {
                return data[0];
            } else {
                throw new UserProvisioningException("cosdn is empty");
            }
        }
            // No cosdn exist, this could be a greeting administrator, return null
            return null;

    }

    /**
     * Retrieves the result from the level above the submitted search result
     *
     * @param profileAttributes the search result for the entry above which a search result should be retrieved
     * @param dirContext        the DirContext where the search should be made
     * @return the search result for the entry above the entry in profileAttributes
     * @throws UserProvisioningException if no or multiple results are found
     */
    private ProfileAttributes getSuperLevelAttributes(ProfileAttributes profileAttributes, DirContext dirContext)
            throws UserProvisioningException, HostException {

        String ctxDn = profileAttributes.getDistinguishedName();
        String sup = getSuperLevel(ctxDn);
        return searcher.search(dirContext, sup, OBJECT_CLASS, SearchControls.OBJECT_SCOPE, null);
    }

    private ProfileAttributes search(DirContext dirContext, String searchBase, String filter, int searchScope)
            throws UserProvisioningException, HostException {
        return searcher.search(dirContext, searchBase, filter, searchScope, null);
    }

    private String getSuperLevel(String distinguishedName)
            throws UserProvisioningException {
        try {
            LdapName ldapName = new LdapName(distinguishedName);
            if (ldapName.size() > 0) {
                return ldapName.getPrefix(ldapName.size() - 1).toString();
            } else {
                throw new UserProvisioningException("Unable to retrieve super level for " + distinguishedName);
            }
        } catch (InvalidNameException e) {
            String errmsg = "Unable to retrieve super level for " + distinguishedName + ". " + e;
            throw new UserProvisioningException(errmsg);
        }
    }

    String getSearchErrorMessage(String message, String searchBase, String filter) {
        StringBuilder errMsg;
        errMsg = new StringBuilder(message);
        errMsg.append(". SearchBase<").append(searchBase).append("> ");
        errMsg.append("Filter<").append(filter).append("> ");
        return errMsg.toString();
    }

    protected ProfileAttributes getCommunityResult(ProfileAttributes profileAttributes, DirContext dirContext)
            throws UserProvisioningException, HostException {
        String ctxDn = profileAttributes.getDistinguishedName();
        String sup = getSuperLevel(ctxDn);
        ProfileAttributes result = communityCache.get(sup);
        if (result != null) {
            return result;
        } else {
            result = search(dirContext, sup, OBJECT_CLASS, SearchControls.OBJECT_SCOPE);
            communityCache.put(sup, result);
            return result;
        }
    }


    /**
     * Retrieves subscriber profiles asynchronously
     *
     * @param criteria search criteria used when searching for subscribers
     * @return a Future which can be used to poll status and/or retrieve the result from the
     *         asynchronous operation. Note that exceptions thrown when retrieving the profile are
     *         catched in the Future's ExecutionException.
     * @throws HostException if no host is found or host is experiencing problems, e.g. timeout
     */
    public Future<IProfile[]> getProfileAsync(Criteria<ProfileCriteriaVisitor> criteria)
            throws UnknownAttributeException, HostException {
        if (log.isInfoEnabled()) log.info("getProfileAsync(criteria=" + criteria + ")");
        throw new UnsupportedOperationException();
//        return null;
    }

    /**
     * Retrieves subscriber profiles asynchronously
     *
     * @param searchBase the base from which to search
     * @param criteria   search criteria used when searching for subscribers
     * @return a Future which can be used to poll status and/or retrieve the result from the
     *         asynchronous operation. Note that exceptions thrown when retrieving the profile are
     *         catched in the Future's ExecutionException.
     * @throws HostException if no host is found or host is experiencing problems, e.g. timeout
     */
    public Future<IProfile[]> getProfileAsync(String searchBase, Criteria<ProfileCriteriaVisitor> criteria)
            throws UnknownAttributeException, HostException {
        if (log.isInfoEnabled()) log.info("getProfileAsync(searchBase=" + searchBase + ", criteria=" + criteria + ")");
        throw new UnsupportedOperationException();
//        return null;
    }

    public Future<IProfile[]> getProfileAsync(Criteria<ProfileCriteriaVisitor> criteria, boolean limit) throws UnknownAttributeException, HostException {
        if (log.isInfoEnabled()) log.info("getProfileAsync(criteria=" + criteria + ", limit=" + limit + ")");
        throw new UnsupportedOperationException();
//        return null;
    }

    public Future<IProfile[]> getProfileAsync(String searchBase, Criteria<ProfileCriteriaVisitor> criteria, boolean limit) throws UnknownAttributeException, HostException {
        if (log.isInfoEnabled())
            log.info("getProfileAsync(searchBase=" + searchBase + ", criteria=" + criteria + ", limit=" + limit + ")");
        throw new UnsupportedOperationException();
//        return null;
    }
    
    public ICos getCos(String dn) throws ProfileManagerException {
        if (log.isInfoEnabled()) log.info("getCos(dn=" + dn + ")");
        ICos cos;
        ProfileAttributes result = cosCache.get(dn);
        if (result != null) {
            cos = new ProfileSettings(getContext(), result);
        } else {
            result = searcher.retriedSearch(dn, OBJECT_CLASS, SearchControls.OBJECT_SCOPE);
            cosCache.put(dn, result);
            cos = new ProfileSettings(getContext(), result);
        }
        try {
        	boolean segmented = false;
            ProfileAttribute objectclass = result.get("objectclass");
            if (objectclass == null) {
            	segmented = false;
            } else { 
            	segmented = attributeMatch(objectclass.getData(), "emsegmentedconfcos");
            }
          if (segmented) {
            // Segmented CoS, cache of clusters/instances internal        	
        	  ProfileAttribute a = result.get(EM_COMPOUND_SERVICE_DN);
        	  String [] emCompoundServiceDns = null;
        	  if (a != null) {
        		  emCompoundServiceDns = a.getData();
        	  } 
    		if (log.isDebugEnabled()) {
    			log.debug("UNCACHED Segmented cos found for dn=" + dn );
    		}
    		SegmentedCos segmentedCos = new SegmentedCos(getContext(),
    		        result, 
    		        emCompoundServiceDns, 
    		        searcher,
    		        null,
    		        compoundServiceCache,
    		        endUserServiceCache); 
            ProfileAttributes segmentedCosAttrs = segmentedCos.getCos();
            if (log.isInfoEnabled()) {
            	log.info("getCos(String) returns ICos(" + dn + ")");
            }
            return new ProfileSettings(getContext(), segmentedCosAttrs);
          }
        } catch (UnknownAttributeException uae) {
            log.warn("SegmentedCos: Unknown Attribute (ignored)", uae);
        }
        if (log.isInfoEnabled()) log.info("getCos(String) returns ICos(" + dn + ")");
        return cos;
    }
    
    public ICos getCos(String dn, String [] userCompoundServiceDn) throws ProfileManagerException {
    	if (userCompoundServiceDn == null) {
    		return null;
    	}
    	LdapServiceInstanceDecorator serviceInstance = getContext().getServiceInstance(Direction.READ);
    	if (serviceInstance == null) {
    		return null;
    	}
    	DirContext d = getContext().getDirContext(serviceInstance, Direction.READ);
    	if (d == null) {
    		return null;
    	}
    	ProfileAttributes userAttributes = new ProfileAttributes(getContext());
    	if (userAttributes == null) {
    		return null;
    	}
    	userAttributes.put(EM_COMPOUND_SERVICE_DN, new ProfileAttribute(userCompoundServiceDn));
    	ProfileAttributes newCos = getCosResult(dn, userAttributes, d);
    	if (newCos == null) {
    		return null;
    	}
        // Return the context to the pool TR fix
    	try {
            return new ProfileSettings(getContext(), newCos);
        } finally {
            getContext().returnDirContext(d, false);            
        }
    }

    public ICommunity getCommunity(String dn) throws ProfileManagerException {
        if (log.isInfoEnabled()) log.info("getCommunity(dn=" + dn + ")");
        ICommunity community;
        ProfileAttributes result = communityCache.get(dn);
        if (result != null) {
            community = new ProfileSettings(getContext(), result);
        } else {
            result = searcher.retriedSearch(dn, OBJECT_CLASS, SearchControls.OBJECT_SCOPE);
            communityCache.put(dn, result);
            community = new ProfileSettings(getContext(), result);
        }
        if (log.isInfoEnabled()) log.info("getCommunity(String) returns ICommunity(" + dn + ")");
        return community;
    }

    public void createSubscription(Subscription subscription, String adminUid) throws ProfileManagerException {
        if (log.isInfoEnabled())
            log.info("createSubscription(subscription=" + subscription + ", adminUid=" + adminUid + ")");
        createSubscription(subscription, adminUid, null);
        if (log.isInfoEnabled()) log.info("createSubscription(Subscription, String) returns void");
    }

    public void createSubscription(Subscription subscription, String adminUid, String cosName)
            throws ProfileManagerException {
        if (log.isInfoEnabled())
            log.info("createSubscription(subscription=" + subscription + ", adminUid=" + adminUid + ", cosName=" + cosName + ")");
        com.mobeon.common.provisionmanager.Subscription provision = getSubscription(subscription);
        ProfileAttributes userAdmin = searchAdmin(adminUid);
        String password = getContext().getConfig().getUserAdminPassword();
        if (cosName != null) {
            provision.addAttribute(getProvisioningName("cosdn"), searchCosDn(cosName, userAdmin));
        }
        try {
            getContext().getProvisioning().create(provision, adminUid, password);
        } catch (ProvisioningException e) {
            throw new UserProvisioningException(e.toString(), e);
        }
        if (log.isInfoEnabled()) log.info("createSubscription(Subscription, String, String) returns void");
    }

    public void deleteSubscription(Subscription subscription, String adminUid) throws ProfileManagerException {
        if (log.isInfoEnabled())
            log.info("deleteSubscription(subscription=" + subscription + ", adminUid=" + adminUid + ")");
        com.mobeon.common.provisionmanager.Subscription provision = getSubscription(subscription);
        // The admin has to exist, but we don't use it
        searchAdmin(adminUid);
        String password = getContext().getConfig().getUserAdminPassword();
        try {
            getContext().getProvisioning().delete(provision, adminUid, password);
        } catch (ProvisioningException e) {
            throw new UserProvisioningException(e.toString(), e);
        }
        if (log.isInfoEnabled()) log.info("deleteSubscription(Subscription, String) returns void");
    }
    
    private ProfileSettings getCachedCosByCompoundServiceId(String compoundServiceId) throws ProfileManagerException {
        Set<Map.Entry<String, ProfileAttributes>> s = compoundServiceCache.entrySet();
        Iterator<Map.Entry<String, ProfileAttributes>> i = s.iterator();
        while (i.hasNext()) {
            Map.Entry<String, ProfileAttributes> e = i.next();
            if (e.getValue().containsKey(EM_COMPOUND_SERVICE_ID)) { // All clusters _should_ have this id
                ProfileAttribute p = e.getValue().get(EM_COMPOUND_SERVICE_ID);
                if (p != null) {
                    String [] data = p.getData();
                    if (data != null && data[0].matches(compoundServiceId)){
                        // Cache HIT!
                        // Create a SegmentedCos from this cluster & return
                        // the ProfileSettings
                        SegmentedCos sc = new SegmentedCos(
                                getContext(),
                                new ProfileAttributes(getContext()), //empty oldSettings 
                                new String [] {e.getValue().getDistinguishedName()}, 
                                searcher,
                                null,
                                compoundServiceCache,
                                endUserServiceCache);
                        return new ProfileSettings(getContext(), sc.getCos());
                    }
                }
            }
        }
        return null;
    }
    
    public ICos getCos(int compoundServiceId) throws ProfileManagerException {
        // look in cluster cache, 
        // if not there, search (& chache) all clusters (but not instances) in mur!
        // objectclass=emSegmentedConfCos & attribute emCompoundServiceDn
        String compoundServiceIdString;
        try {
            compoundServiceIdString = Integer.toString(compoundServiceId);            
        } catch (NumberFormatException ne) {
            return null; // Bad argument
        }
        ProfileSettings ps = getCachedCosByCompoundServiceId(compoundServiceIdString);
        if (ps != null) {
            return ps;
        }
        // Cache miss, now we need to search all (but cached) emcompoundServiceDns
        ProfileAttributes [] result = null;
        try {
            result = searcher.retriedMultiSearch(
                    getContext().getConfig().getDefaultSearchbase(),
                    OBJECT_CLASS_SEGMENTED_CONF_COS, SearchControls.SUBTREE_SCOPE);
        } catch (ProfileManagerException pme){
            log.warn("No compound services found duing search ", pme);
        }
        if (result == null) {
            log.warn("No compound services found duing search.");
            return null;
        }
        for (int i = 0; i < result.length; i++) {
            ProfileAttributes tmp = result[i];
            if (tmp != null) {
                compoundServiceCache.put(tmp.getDistinguishedName(), tmp); // Put all!
            }
        }
        ps = getCachedCosByCompoundServiceId(compoundServiceIdString);
        return ps;
    }

    private ProfileAttributes searchAdmin(String adminUid) throws ProfileManagerException {
        ProfileAttributes userAdmin = userAdminCache.get(adminUid);
        if (userAdmin == null) {
            String searchBase = getContext().getConfig().getDefaultSearchbase();
            String filter = "(uid=" + adminUid + ")";
            userAdmin = searcher.retriedSearch(searchBase, filter, SearchControls.SUBTREE_SCOPE);
            userAdminCache.put(adminUid, userAdmin);
        }
        return userAdmin;
    }

    private String searchCosDn(String cosName, ProfileAttributes userAdmin) throws ProfileManagerException {
        //String searchBase = getSuperLevel(userAdmin.getDistinguishedName());
        String distinguishedName = userAdmin.getDistinguishedName();
        String searchBase = "";
        try {
            LdapName ldapName = new LdapName(distinguishedName);
            if (ldapName.size() < 1)
              throw new UserProvisioningException("Unable to get searchbase for empty dn");
            searchBase = ldapName.getPrefix(1).toString();
        } catch (InvalidNameException e) {
            throw new UserProvisioningException("Unable to get searchbase for " + distinguishedName);
        }
        String filter = "(cosname=" + cosName + ")";
        ProfileAttributes cos =
                searcher.retriedEntrySearch(searchBase, filter, SearchControls.SUBTREE_SCOPE, new String[]{});
        return cos.getDistinguishedName();
    }

    private com.mobeon.common.provisionmanager.Subscription getSubscription(Subscription subscription)
            throws UnknownAttributeException {
        com.mobeon.common.provisionmanager.Subscription provision = new com.mobeon.common.provisionmanager.Subscription();
        for (Map.Entry<String, String[]> entry : subscription.getAttributes().entrySet()) {
            provision.addAttribute(getProvisioningName(entry.getKey()), entry.getValue());
        }
        return provision;
    }

    private String getProvisioningName(String applicationName) throws UnknownAttributeException {
        Map<String, String> provisioningMap = getContext().getConfig().getProvisioningMap();
        if (provisioningMap.containsKey(applicationName)) {
            return provisioningMap.get(applicationName);
        } else {
            throw new UnknownAttributeException(applicationName);
        }
    }

    /**
     * @param dirContext
     * @param searchBase
     * @param ldapFilter
     * @param ctls
     * @return
     * @throws HostException
     * @logs.warning "Search [with LimitScope] failed. SearchBase&lt;searchBase&gt; Filter&lt;filter&gt;" -
     * A search result from a call to the profilesSearch method contained no attributes and is therefore ignored.
     * @logs.warning "Couldn't create a subscriber. &lt;Exception&gt;. SearchBase&lt;searchBase&gt;
     * Filter&lt;filter&gt;" - A search result from a call to the profilesSearch method could not be used to create
     * a subscriber object and is therefore ignored.
     */
    private IProfile[] profilesSearch(DirContext dirContext, String searchBase, String ldapFilter,
                                      SearchControls ctls)
            throws HostException {
        List<IProfile> profiles = new ArrayList<IProfile>();
        try {
            NamingEnumeration<SearchResult> result = dirContext.search(searchBase, ldapFilter, ctls);
            while (result.hasMore()) {
                SearchResult searchResult = result.next();
                if (searchResult.getAttributes().size() == 0) {
                    log.warn(getSearchErrorMessage(searchBase, ldapFilter).toString());
                    continue;
                }
                try {
                    IProfile profile = createSubscriber(searchResult, dirContext);
                    profiles.add(profile);
                } catch (UserProvisioningException e) {
                    log.warn(getSearchErrorMessage("Couldn't create a subscriber. " + e, searchBase, ldapFilter));
                }
            }
            return profiles.toArray(EMPTY_PROFILE_LIST);
        } catch (NamingException e) {
            StringBuilder errMsg = getSearchErrorMessage(searchBase, ldapFilter);
            errMsg.append(". ").append(e);
            errMsg.append(" url=").append(getProviderUrlFromException(e));
            log.debug(errMsg.toString());
            throw new HostException(errMsg.toString(), e);
        }
    }

    String getProviderUrlFromException(NamingException e) {
        if (e.getResolvedObj() != null) {
            Object resolvedObj = e.getResolvedObj();
            if (resolvedObj instanceof Context) {
                Context ldapCtx = (Context) resolvedObj;
                try {
                    Hashtable env = ldapCtx.getEnvironment();
                    return (String) env.get("java.naming.provider.url");
                } catch (NamingException e1) {
                    log.error("Could not get ProviderUrl " + e1);
                }
            }
        }
        return "";
    }

    public class GetProfileTask implements Callable<IProfile[]> {
        private String searchBase;
        private String ldapFilter;
        private SearchControls ctls;

        public GetProfileTask(String searchBase, String ldapFilter, SearchControls ctls) {
            this.searchBase = searchBase;
            this.ldapFilter = ldapFilter;
            this.ctls = ctls;
        }

        public IProfile[] call() throws ProfileManagerException, RetryException {
            LdapServiceInstanceDecorator serviceInstance = getContext().getServiceInstance(Direction.READ);
            DirContext dirContext = null;
            boolean release = false;
            try {
                dirContext = getContext().getDirContext(serviceInstance, Direction.READ);
                IProfile[] profiles = profilesSearch(dirContext, searchBase, ldapFilter, ctls);
                log.available(serviceInstance.getProtocol(), serviceInstance.getHost(), serviceInstance.getPort());
                return profiles;
            } catch (HostException e) {
                if (e.getCause() instanceof CommunicationException) {
                    log.notAvailable(serviceInstance.getProtocol(), serviceInstance.getHost(), serviceInstance.getPort(), e.toString());
                    getContext().getServiceLocator().reportServiceError(serviceInstance.getDecoratedServiceInstance());
                    release = true;
                    throw new RetryException(e);
                } else {
                    throw e;
                }
            } finally {
                getContext().returnDirContext(dirContext, release);
            }
        }
    }

 
}
