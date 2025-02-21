/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip;

import com.abcxyz.services.moip.migration.profilemanager.moip.greetings.*;
import com.abcxyz.services.moip.migration.profilemanager.moip.search.ProfileStringCriteria;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.NoServiceFoundException;

import com.mobeon.common.logging.HostedServiceLogger;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mailbox.IMailbox;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.MailboxProfile;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.profilemanager.HostException;
import com.mobeon.masp.profilemanager.ProfileManagerException;
import com.mobeon.masp.profilemanager.UnknownAttributeException;
import com.mobeon.masp.profilemanager.UserProvisioningException;
import com.mobeon.masp.profilemanager.greetings.GreetingFormat;
import com.mobeon.masp.profilemanager.greetings.GreetingNotFoundException;
import com.mobeon.masp.profilemanager.greetings.GreetingSpecification;
import com.mobeon.masp.profilemanager.greetings.GreetingType;
import com.mobeon.common.util.check.Check;
import com.mobeon.common.util.executor.RetryException;
import com.mobeon.common.util.executor.TimeoutRetrier;

import javax.naming.CommunicationException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

/**
 * Subscriber class implementing the IProfile interface.
 *
 * @author mande
 */
public class Subscriber extends ProfileSettings implements IProfile {
    private static final ILogger logg = ILoggerFactory.getILogger(Subscriber.class);
    private static final HostedServiceLogger log = new HostedServiceLogger(logg);

    private Map<IServiceInstance, IMailbox> mailBoxMap;
    private Map<String, ProfileLevel> attributeLevels = new HashMap<String, ProfileLevel>(); 
    private Map<ProfileLevel, String> distinguishedNameMap = new EnumMap<ProfileLevel, String>(ProfileLevel.class);

    // Fields used for toString method
    private String uid;
    private String telephonenumber;
    private ProfileAttributes subscriberCos;

    /**
     * Default GreetingManagerFactory. This can be changed by setGreetingManagerFactory (for testing purposes).
     */
    private GreetingManagerFactory greetingManagerFactory = new GreetingManagerFactoryImpl();
    private static final String DIST_LIST_OBJECT_CLASS = "(objectclass=distributionlist)";

    public Subscriber(BaseContext context) {
        super(context);
        mailBoxMap = new HashMap<IServiceInstance, IMailbox>();
    }

    /**
     * Sets subscriber string value attribute
     *
     * @param applicationName the attribute to set
     * @param value           the value to set for the supplied attribute, if null the attribute is deleted
     * @throws HostException             if no DirContext could be created or if writing failed
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    public void setStringAttribute(String applicationName, String value) throws ProfileManagerException {
        if (log.isInfoEnabled())
            log.info("setStringAttribute(applicationName=" + applicationName + ", value=" + value + ")");
        String[] values = (value == null) ? null : new String[]{value};
        setStringAttributesWorker(applicationName, values);
        if (log.isInfoEnabled()) log.info("setStringAttribute(String, String) returns void");
    }

    /**
     * Sets subscriber string values attribute
     *
     * @param applicationName the attribute to set
     * @param values          the values to set for the supplied attribute, if null the attribute is deleted
     * @throws HostException             if no DirContext could be created or if writing failed
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    public void setStringAttributes(String applicationName, String[] values)
            throws ProfileManagerException {
        if (log.isInfoEnabled())
            log.info("setStringAttributes(applicationName=" + applicationName + ", value=" + Arrays.toString(values) + ")");
        setStringAttributesWorker(applicationName, values);
        if (log.isInfoEnabled()) log.info("setStringAttributes(String, String[]) returns void");
    }

    public void setStringAttributesWorker(String applicationName, String[] values)
            throws ProfileManagerException {
        // Find metadata
        ProfileMetaData profileMetaData = getMetaData(applicationName);
        // Is applicationName a string attribute?
        if (!stringTypesSet.contains(profileMetaData.getType())) {
            String errmsg = applicationName + " is not of type string";
            if (log.isDebugEnabled()) log.debug(errmsg);
            throw new UnknownAttributeException(errmsg);
        }
        checkReadOnly(profileMetaData);

        if (values != null) {
            // Check syntax on the values
            checkAttributeValue(profileMetaData, values);
        }

        String[] strValues = null;
        if (values != null) {
            // Make copy of data
            strValues = values.clone();
            if (profileMetaData.getType() == AttributeType.XSTRING) {
                // Encode strings
                Check check = new Check();
                for (int i = 0; i < strValues.length; i++) {
                    String value = check.checkin(strValues[i]);
                    if (value == null) {
                        String errmsg = "Could not encode attribute." + check.getErrorMessage();
                        if (log.isDebugEnabled()) log.debug(errmsg);
                        throw new ProfileManagerException(errmsg);
                    } else {
                        strValues[i] = value;
                    }
                }
            }
        }
        modifyAttributes(applicationName, strValues, profileMetaData);
    }

    private void checkReadOnly(ProfileMetaData profileMetaData) throws ProfileManagerException {
        // Is applicationName readonly?
        if (profileMetaData.isReadOnly()) {
            throw new ProfileManagerException(profileMetaData.getApplicationName() + " is readonly");
        }
    }

    private void checkAttributeValue(ProfileMetaData profileMetaData, String[] values)
            throws InvalidAttributeValueException {

        AttributeValueControl attributeValueControl = profileMetaData.getAttributeValueControl();
        if (attributeValueControl != null) {

            for (int i = 0; i < values.length; i++) {
                attributeValueControl.checkValue(values[i], profileMetaData.getApplicationName());
            }
        }
    }

    /**
     * Retrieves the distinguished name for a subscriber's different entry levels
     *
     * @param level the level to get the distinguished name for
     * @return the distinguished name for the submitted level
     * @throws UserProvisioningException if the name for the level could not be found
     */
    protected String getDistinguishedName(ProfileLevel level) throws UserProvisioningException {
        if (distinguishedNameMap.containsKey(level)) {
            return distinguishedNameMap.get(level);
        } else {
            throw new UserProvisioningException("Could not find distinguished name for level " + level.toString());
        }
    }

    public String getDistinguishedName() throws UserProvisioningException {
	return getDistinguishedName(ProfileLevel.USER);
    }

    /**
     * Sets the distinguised name for a subscriber's different entry levels
     *
     * @param level the level to set the distinguished name for
     * @param dn    the distinguished name to set
     */
    protected void setDistinguishedName(ProfileLevel level, String dn) {
        distinguishedNameMap.put(level, dn);
    }

    /**
     * Sets subscriber integer attribute
     *
     * @param applicationName the attribute to set
     * @param value           the value to set for the supplied attribute
     * @throws HostException             if no DirContext could be created or if writing failed
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    public void setIntegerAttribute(String applicationName, int value) throws ProfileManagerException {
        if (log.isInfoEnabled())
            log.info("setIntegerAttribute(applicationName=" + applicationName + ", value=" + value + ")");
        int[] values = new int[]{value};
        setIntegerAttributesWorker(applicationName, values);
        if (log.isInfoEnabled()) log.info("setIntegerAttribute(String, int) returns void");
    }

    /**
     * Sets subscriber integer attribute
     *
     * @param applicationName the attribute to set
     * @param values          the values to set for the supplied attribute
     * @throws HostException             if no DirContext could be created or if writing failed
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    public void setIntegerAttributes(String applicationName, int[] values) throws ProfileManagerException {
        if (log.isInfoEnabled())
            log.info("setIntegerAttributes(applicationName=" + applicationName + ", value=" + Arrays.toString(values) + ")");
        setIntegerAttributesWorker(applicationName, values);
        if (log.isInfoEnabled()) log.info("setIntegerAttributes(String, int[]) returns void");
    }

    public void setIntegerAttributesWorker(String applicationName, int[] values) throws ProfileManagerException {
        // Find metadata
        ProfileMetaData profileMetaData = getMetaData(applicationName);
        // Is applicationName an integer attribute?
        if (profileMetaData.getType() != AttributeType.INTEGER) {
            String errmsg = applicationName + " is not of type integer";
            if (log.isDebugEnabled()) log.debug(errmsg);
            throw new UnknownAttributeException(errmsg);
        }
        checkReadOnly(profileMetaData);

        String[] strValues = null;
        if (values != null) {
            // Convert integers to strings
            strValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                strValues[i] = Integer.toString(values[i]);
            }
            // Check syntax on the values
            checkAttributeValue(profileMetaData, strValues);
        }
        modifyAttributes(applicationName, strValues, profileMetaData);
    }

    /**
     * Modifies attributes in the user register
     *
     * @param applicationName the attribute to modify
     * @param values          the values to set, if null the attribute is deleted
     * @param profileMetaData meta data for the attribute to modify
     * @throws HostException if no DirContext could be created or if writing failed
     * @logs.error "Failed to write" - if unable to establish a connection to the user register, or if the
     * user register returned an error on write. More information should be contained in the submitted exception.
     * @logs.error "WriteLevel does not exist for attribute &lt;attributename&gt;" - if it was not possible to retrieve
     * the level to write the attribute to. This is due to misconfiguration, either WriteLevel is missing or the
     * attribute is read-only.
     */
    private void modifyAttributes(String applicationName, String[] values, ProfileMetaData profileMetaData)
            throws ProfileManagerException {

        BasicAttribute attr = new BasicAttribute(profileMetaData.getUserRegisterName());
        if (values != null) {
            for (String strValue : values) {
                attr.add(strValue);
            }
        }
        ModificationItem[] mods = new ModificationItem[1];
        mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attr);
        String name;
        try {
            name = getDistinguishedName(profileMetaData.getWriteLevel());
        } catch (MetaDataException e) {
            // This should not happen, check for readonly should have been made in Set methods
            String errMsg = "WriteLevel does not exist for attribute " + applicationName;
            log.error(errMsg);
            throw new ProfileManagerException(errMsg);
        }
        retriedModify(name, mods);
        // If write was successful, set the values in the subscriber object also
        setInProfile(applicationName, values);
    }

    private void retriedModify(String name, ModificationItem[] mods) throws ProfileManagerException {
        ModifyTask modifyTask = new ModifyTask(getContext(), name, mods);
        retryTask(modifyTask, getContext().getConfig().getWriteTimeout());
    }

    /**
     * Sets values in the profile cache
     *
     * @param applicationName the attribute name of the values to set
     * @param values          the values to set, if null the attribute is deleted
     */
    private void setInProfile(String applicationName, String[] values) {
        if (values == null) {
            profileAttributes.remove(applicationName);
        } else {
            if (profileAttributes.containsKey(applicationName)) {
                // Replace the old attribute
                ProfileAttribute attribute = profileAttributes.get(applicationName);
                attribute.setData(values);
            } else {
                // This is a new attribute item, create a new ProfileAttribute
                ProfileAttribute attribute = new ProfileAttribute(values);
                profileAttributes.put(applicationName, attribute);
            }
        }
    }

    /**
     * Sets subscriber boolean attribute
     *
     * @param applicationName the attribute to set
     * @param value           the value to set for the supplied attribute
     * @throws HostException             if no DirContext could be created or if writing failed
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    public void setBooleanAttribute(String applicationName, boolean value) throws ProfileManagerException {
        if (log.isInfoEnabled())
            log.info("setBooleanAttribute(applicationName=" + applicationName + ", value=" + value + ")");
        boolean[] values = new boolean[]{value};
        setBooleanAttributes(applicationName, values);
        if (log.isInfoEnabled()) log.info("setBooleanAttribute(String, boolean) returns void");
    }

    /**
     * Sets subscriber boolean attribute
     *
     * @param applicationName the attribute to set
     * @param values          the values to set for the supplied attribute
     * @throws HostException             if no DirContext could be created or if writing failed
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    public void setBooleanAttributes(String applicationName, boolean[] values) throws ProfileManagerException {
        if (log.isInfoEnabled())
            log.info("setBooleanAttributes(applicationName=" + applicationName + ", value=" + Arrays.toString(values) + ")");
        setBooleanAttributesWorker(applicationName, values);
        if (log.isInfoEnabled()) log.info("setBooleanAttributes(String, boolean[]) returns void");
    }

    public void setBooleanAttributesWorker(String applicationName, boolean[] values) throws ProfileManagerException {
        // Find metadata
        ProfileMetaData profileMetaData = getMetaData(applicationName);
        // Is applicationName a boolean attribute?
        if (profileMetaData.getType() != AttributeType.BOOLEAN) {
            String errmsg = applicationName + " is not of type boolean";
            if (log.isDebugEnabled()) log.debug(errmsg);
            throw new UnknownAttributeException(errmsg);
        }
        checkReadOnly(profileMetaData);

        String[] strValues = null;
        if (values != null) {
            // Convert booleans to strings
            strValues = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                strValues[i] = profileMetaData.getBooleanString(values[i]);
            }
        }
        modifyAttributes(applicationName, strValues, profileMetaData);
    }

    /**
     * Retrieves a subscriber's community
     *
     * @return a community
     */
    public ICommunity getCommunity() throws ProfileManagerException {
        if (log.isInfoEnabled()) log.info("getCommunity()");
        String dn = getDistinguishedName(ProfileLevel.COMMUNITY);
        ICommunity community = getContext().getProfileManager().getCommunity(dn);
        if (log.isInfoEnabled()) log.info("getCommunity() returns ICommunity(" + dn + ")");
        return community;
    }

    protected void setCos(ProfileAttributes cos) {
    	subscriberCos = cos;
    }
    
    /**
     * Retrieves a subscriber's class of service
     * (Possible user overrides included)
     * @deprecated It is always better to use getAttribute, 
     *             otherwise search order is bypassed.
     * @return a class of service or null
     */
    public ICos getCos() throws ProfileManagerException {
        if (log.isInfoEnabled()) log.info("getCos()");
        
        if (subscriberCos == null) { // Admin could req. null ret.
        	return null;
        } else {
        	return new ProfileSettings(getContext(), subscriberCos); // Usually
        }
    }

    /**
     * Retrieves a subscriber's greeting
     *
     * @param specification specifies which greeting to retrieve
     * @return the requested greeting
     * @throws GreetingNotFoundException if no greeting was found for supplied specification
     */
    public IMediaObject getGreeting(GreetingSpecification specification) throws ProfileManagerException {
        if (log.isInfoEnabled()) log.info("getGreeting(specification=" + specification + ")");
        GreetingManager greetingManager = getGreetingManager(specification.getType());
        try {
            IMediaObject greeting = greetingManager.getGreeting(specification);
      //      greetingManager.getServiceInstance().setServiceStatus(IServiceInstance.ServiceStatus.UP);
            if (log.isInfoEnabled()) log.info("getGreeting(GreetingSpecification) returns " + greeting);
            return greeting;
        } catch (ProfileManagerException e) {
            if (!(e instanceof GreetingNotFoundException)) {
           //     greetingManager.getServiceInstance().setServiceStatus(IServiceInstance.ServiceStatus.DOWN);
            }
            throw e;
        }
    }

    private IProfile getGreetingAdmin() throws GreetingNotFoundException {
        // Search for a profile.
        String adminInfo;
        try {
            adminInfo = getStringAttribute("admininfo");
        } catch (UnknownAttributeException e) {
            if (log.isDebugEnabled()) log.debug(e.getMessage() + ". Could not get greeting admin.");
            throw new GreetingNotFoundException(e);
        }
        ProfileStringCriteria adminCriteria;
        try {
            adminCriteria = getStringCriteria(adminInfo);
        } catch (UserProvisioningException e) {
            if (log.isDebugEnabled()) log.debug(e.getMessage() + ". Could not create greeting admin search criteria.");
            throw new GreetingNotFoundException(e);
        }
        try {
            IProfile[] profiles = getContext().getProfileManager().getProfile(getDistinguishedName(ProfileLevel.COMMUNITY), adminCriteria);
            if (profiles.length != 1) {
                throw new GreetingNotFoundException("Multiple greeting administrators found.");
            }
            return profiles[0];
        } catch (ProfileManagerException e) {
            throw new GreetingNotFoundException(e.getMessage(), e);
        }
    }

    private ProfileStringCriteria getStringCriteria(String adminInfo) throws UserProvisioningException {
        int i = adminInfo.indexOf("=");
        if (i != -1) {
            String name = adminInfo.substring(0, i);
            String value = adminInfo.substring(i + 1);
            return new ProfileStringCriteria(name, value);
        } else {
            throw new UserProvisioningException("Invalid admininfo attribute value: " + adminInfo);
        }
    }

    /**
     * Retrieves a subscriber's greeting asynchronously
     *
     * @param specification specifies which greeting to retrieve
     * @return a Future which can be used to poll status and/or retrieve the result from the
     *         asynchronous operation. Note that exceptions thrown when retrieving the greeting are
     *         catched in the Future's ExecutionException.
     * @throws UnknownAttributeException if an invalid specification attribute was supplied
     */
    public Future<IMediaObject> getGreetingAsync(GreetingSpecification specification) throws UnknownAttributeException {
        if (log.isInfoEnabled()) log.info("getGreetingAsync(specification=" + specification + ")");
        throw new UnsupportedOperationException("getGreetingAsync");
//        return null;
    }

    /**
     * Sets a subscriber's greeting
     *
     * @param specification specifies which greeting to set
     * @param greeting      the greeting which should be set
     * @throws GreetingNotFoundException if greeting could not be found
     * @throws UnknownAttributeException if an invalid filter attribute was supplied
     */
    public void setGreeting(GreetingSpecification specification, IMediaObject greeting) throws ProfileManagerException {
        if (log.isInfoEnabled())
            log.info("setGreeting(specification=" + specification + ", greeting=" + greeting + ")");
        if (!specification.isValid()) {
            throw new ProfileManagerException("Invalid specification: " + specification);
        }
        GreetingManager greetingManager = getGreetingManager(specification.getType());
        String telephoneNumber = getStringAttribute("billingnumber");
        try {
            greetingManager.setGreeting(telephoneNumber, specification, greeting);
            //greetingManager.getServiceInstance().setServiceStatus(IServiceInstance.UP);
        } catch (ProfileManagerException e) {
            //greetingManager.getServiceInstance().setServiceStatus(IServiceInstance.ServiceStatus.DOWN);
            throw e;
        }
        if (log.isInfoEnabled()) log.info("setGreeting(GreetingSpecification, IMediaObject) returns void");
    }

    /**
     * This method is designed to fetch a GretingManager on the MoIP 6.0 system. To adapt to other system, subclass the subscriber 
     * class and overwrite this method
     * @param type
     * @return
     * @throws GreetingNotFoundException
     * @throws UnknownAttributeException
     */
    protected GreetingManager getGreetingManager(GreetingType type) throws GreetingNotFoundException, UnknownAttributeException {
        IProfile greetingAdmin = getGreetingAdmin();
        String host = greetingAdmin.getStringAttribute("mailhost");
        int port;
        IServiceInstance serviceInstance;
        try {
            serviceInstance = getContext().getServiceLocator().locateService("storage", host);
            port = Integer.parseInt(serviceInstance.getProperty(IServiceInstance.PORT));
        } catch (NoServiceFoundException e) {
        	log.error("no service found for service <storage> at host name: " + host);
        	e.printStackTrace(System.out);
            throw new GreetingNotFoundException(e);
        } catch (NumberFormatException e) {
        	log.error("port is not numeric for service <storage> at host name: " + host);
        	e.printStackTrace(System.out);
            throw new GreetingNotFoundException("Could not parse port property", e);
        }
        String uid = greetingAdmin.getStringAttribute("uid");
        String password = greetingAdmin.getStringAttribute("password");
        String telephoneNumber = getStringAttribute("billingnumber");
        return greetingManagerFactory.getGreetingManager(getContext(), host, port, uid, password, getFolder(telephoneNumber, type), serviceInstance);
    }

    private String getFolder(String telephoneNumber, GreetingType type) {
        switch (type) {
            case ALL_CALLS:
            case BUSY:
            case CDG:
            case EXTENDED_ABSENCE:
            case NO_ANSWER:
            case OUT_OF_HOURS:
            case OWN_RECORDED:
            case TEMPORARY:
            case SPOKEN_NAME:
                return telephoneNumber + "/Greeting";
            case DIST_LIST_SPOKEN_NAME:
                return telephoneNumber + "/DistList";
            default:
                throw new IllegalArgumentException(type.toString());
        }
    }

    /**
     * Gets a subscriber's spoken name
     *
     * @param format
     * @return the requested spoken name
     * @throws GreetingNotFoundException if no greeting was found for supplied specification
     * @throws UnknownAttributeException if an invalid specification attribute was supplied
     */
    public IMediaObject getSpokenName(GreetingFormat format) throws ProfileManagerException {
        if (log.isInfoEnabled()) log.info("getSpokenName(format=" + format + ")");
        IMediaObject greeting = getGreeting(new SpokenNameSpecification(GreetingType.SPOKEN_NAME, format));
        if (log.isInfoEnabled()) log.info("getSpokenName(GreetingFormat) returns " + greeting);
        return greeting;
    }

    /**
     * Gets a subscriber's spoken name asynchronously
     *
     * @param format
     * @return a Future which can be used to poll status and/or retrieve the result from the
     *         asynchronous operation. Note that exceptions thrown when retrieving the spoken name are
     *         catched in the Future's ExecutionException.
     * @throws UnknownAttributeException if an invalid specification attribute was supplied
     */
    public Future<IMediaObject> getSpokenNameAsync(GreetingFormat format) throws UnknownAttributeException {
        if (log.isInfoEnabled()) log.info("getSpokenNameAsync(format=" + format + ")");
        throw new UnsupportedOperationException("getSpokenNameAsync");
//        return null;
    }

    /**
     * Sets a subscriber's spoken name
     *
     * @param format     the format of the spoken name
     * @param spokenName the spoken name which should be set
     * @throws UnknownAttributeException if an invalid filter attribute was supplied
     */
    public void setSpokenName(GreetingFormat format, IMediaObject spokenName) throws ProfileManagerException {
        if (log.isInfoEnabled()) log.info("setSpokenName(format=" + format + ", spokenName=" + spokenName + ")");
        setGreeting(new SpokenNameSpecification(GreetingType.SPOKEN_NAME, format), spokenName);
        if (log.isInfoEnabled()) log.info("setSpokenName(GreetingFormat, IMediaObject) returns void");
    }

    /**
     * Retrieves all the distribution lists that a subscriber has
     *
     * @return all distribution lists for the subscriber
     */
    public IDistributionList[] getDistributionLists() throws ProfileManagerException {
        if (log.isInfoEnabled()) log.info("getDistributionLists()");
        SearchDistributionListsTask searchTask = new SearchDistributionListsTask(
                getContext(),
                getDistinguishedName(ProfileLevel.USER),
                DIST_LIST_OBJECT_CLASS,
                SearchControls.ONELEVEL_SCOPE
        );
        IDistributionList[] distributionLists = retryTask(searchTask, getContext().getConfig().getReadTimeout());
        log.info("getDistributionLists() returns " + Integer.toString(distributionLists.length) + " distribution lists");
        return distributionLists;
    }

    private String getDistributionListSearchBase(String id) throws ProfileManagerException {
        StringBuilder filter = new StringBuilder("mail=");
        filter.append(id).append(getStringAttribute("mail")).append(",").append(getDistinguishedName(ProfileLevel.USER));
        return filter.toString();
    }

    /**
     * Creates a new distribution list for a subscriber
     *
     * @param id the ID of the new distribution list
     * @return the new distribution list
     */
    public IDistributionList createDistributionList(String id) throws ProfileManagerException {
        log.info("createDistributionList(id=" + id + ")");
        DistributionListImpl distributionList = new DistributionListImpl(getContext(), id, this, getDistinguishedName(ProfileLevel.USER));
        log.info("createDistributionList(String) returns " + distributionList);
        return distributionList;
    }

    /**
     * Deletes a distribution list for a subscriber
     *
     * @param distributionList the distribution list to delete for the subscriber
     */
    public void deleteDistributionList(IDistributionList distributionList) throws ProfileManagerException {
        if (log.isInfoEnabled()) log.info("deleteDistributionList(distributionList=" + distributionList + ")");
        // Delete spoken name
        distributionList.setSpokenName(null);
        DestroyTask destroyTask = new DestroyTask(getDistributionListSearchBase(distributionList.getID()));
        retryTask(destroyTask, getContext().getConfig().getWriteTimeout());
        log.info("deleteDistributionList(IDistributionList) returns void");
    }

    /**
     * Returns the subscriber's M3 mailbox
     *
     * @return a mailbox
     * @throws HostException             if no mailbox could be created
     * @throws UserProvisioningException if mail credentials are missing
     */
    public IMailbox getMailbox() throws HostException, UserProvisioningException {
        if (log.isInfoEnabled()) log.info("getMailbox()");
        // Get credentials and create a mailbox
        IServiceInstance serviceInstance;
        String mailHost;
        String accountId;
        String accountPassword;
        String emailAddress;
        try {
            mailHost = getStringAttribute("mailhost"); // Todo: ProfileManager datamodel
            accountId = getStringAttribute("uid"); // Todo: ProfileManager datamodel
            accountPassword = getStringAttribute("password"); // Todo: ProfileManager datamodel
            emailAddress = getStringAttribute("mail"); // Todo: ProfileManager datamodel
        } catch (UnknownAttributeException e) {
            throw new UserProvisioningException("Mail credentials missing for " + toString(), e);
        }
        try {
            serviceInstance = getContext().getServiceLocator().locateService("storage", mailHost);
        } catch (NoServiceFoundException e) {
        	log.error("no service found for service <storage> at host name: " + mailHost);
        	e.printStackTrace(System.out);
            throw new HostException("The external component register cannot find the host <" + mailHost +
                    "> as a provider of the service <storage>", e);
        }

        IMailbox mailbox;
        if (mailBoxMap.containsKey(serviceInstance)) {
            mailbox = mailBoxMap.get(serviceInstance);
        } else {
            try {
                MailboxProfile mailboxProfile = new MailboxProfile(accountId, accountPassword, emailAddress);
                mailbox = getContext().getMailboxAccountManager().getMailbox(serviceInstance, mailboxProfile);
          //      serviceInstance.setServiceStatus(IServiceInstance.ServiceStatus.UP);
                mailBoxMap.put(serviceInstance, mailbox);
            } catch (MailboxException e) {
//                serviceInstance.setServiceStatus(IServiceInstance.ServiceStatus.DOWN);
                throw new HostException("Couldn't open mailbox: " + serviceInstance.toString(), e);
            }
        }
        if (log.isInfoEnabled()) log.info("getMailbox() returns " + mailbox);
        return mailbox;
    }

    public IMailbox getMailbox(String mailHost) {
        if (log.isInfoEnabled()) log.info("getMailbox(mailHost=" + mailHost + ")");
        throw new UnsupportedOperationException("getMailbox");
//        return null;
    }

    public IMailbox getMailbox(String mailHost, String accountID) {
        if (log.isInfoEnabled()) log.info("getMailbox(mailHost=" + mailHost + ", accountID=" + accountID + ")");
        throw new UnsupportedOperationException("getMailbox");
//        return null;
    }

    /**
     * Retrieves a mailbox
     *
     * @param mailHost        the mail host to use
     * @param accountID       the mail account to login to
     * @param accountPassword the password to use
     * @return a mailbox
     */
    public IMailbox getMailbox(String mailHost, String accountID, String accountPassword) throws MailboxException {
        if (log.isInfoEnabled())
            log.info("getMailbox(mailHost=" + mailHost + ", accountID=" + accountID + ", accountPassword=" + accountPassword + ")");
        throw new UnsupportedOperationException("getMailbox");
//        return null;
    }

    /**
     * Closes the resources connected with this profile
     */
    public void close() {
        if (log.isInfoEnabled()) log.info("close()");
        for (Map.Entry<IServiceInstance, IMailbox> entry : mailBoxMap.entrySet()) {
            try {
                entry.getValue().close();
            } catch (MailboxException e) {
                log.warn("Unable to close mailbox. " + e.getMessage());
            }
        }
        mailBoxMap.clear();
        if (log.isInfoEnabled()) log.info("close() returns void");

    }

    protected void setAttribute(String applicationName, ProfileAttribute attribute, ProfileLevel level) {
        profileAttributes.put(applicationName, attribute);
        attributeLevels.put(applicationName, level); // keep track of level
    }

    public String toString() {
        if (uid == null) {
            try {
                uid = getStringAttribute("uid");
            } catch (UnknownAttributeException e) {
                uid = "UNKNOWN";
            }
        }
        if (telephonenumber == null) {
            try {
                telephonenumber = getStringAttribute("billingnumber");
            } catch (UnknownAttributeException e) {
                telephonenumber = "UNKNOWN";
            }
        }
        return "IProfile(uid=" + uid + ", telephonenumber=" + telephonenumber + ")";
    }

    /**
     * Sets a new GreetingManagerFactory. Mainly used for testing purposes
     *
     * @param greetingManagerFactory
     */
    protected void setGreetingManagerFactory(GreetingManagerFactory greetingManagerFactory) {
        this.greetingManagerFactory = greetingManagerFactory;
    }

    private <T> T retryTask(Callable<T> task, int timeout) throws ProfileManagerException {
        TimeoutRetrier<T> timeoutRetrier = new TimeoutRetrier<T>(
                task,
                getContext().getConfig().getTryLimit(),
                getContext().getConfig().getTryTimeLimit(),
                timeout
        );
        try {
            return timeoutRetrier.call();
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ProfileManagerException) {
                throw (ProfileManagerException) e.getCause();
            } else {
                // This should not happen, TimeoutRetrier should only throw ProfileManagerExceptions from tasks
                throw new ProfileManagerException("Task threw unexpected exception: " + e.getMessage(), e.getCause());
            }
        } catch (TimeoutException e) {
            throw new HostException("Task has timed out", e);
        } catch (InterruptedException e) {
            throw new HostException("Task was interrupted", e);
        }
    }

    /**
     * A retryable JNDI search task submittable to a TimedRetrier
     *
     * @author mande
     */
    private class SearchDistributionListsTask implements Callable<IDistributionList[]> {

        String searchBase;
        String filter;
        int scope;
        String[] attrs;
        private BaseContext context;

        /**
         * Constructs a SearchDistributionListsTask
         *
         * @param context
         * @param searchBase
         * @param filter     the search filter
         * @param scope      the search scope
         */
        public SearchDistributionListsTask(BaseContext context, String searchBase, String filter, int scope) {
            this.context = context;
            this.searchBase = searchBase;
            this.filter = filter;
            this.scope = scope;
        }

        public IDistributionList[] call() throws ProfileManagerException, RetryException {
            LdapServiceInstanceDecorator serviceInstance = getContext().getServiceInstance(Direction.READ);
            DirContext dirContext = null;
            boolean release = false;
            try {
                dirContext = getContext().getDirContext(serviceInstance, Direction.READ);
                IDistributionList[] result = search(dirContext);
                log.available(serviceInstance.getProtocol(), serviceInstance.getHost(), serviceInstance.getPort());
                return result;
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

        private IDistributionList[] search(DirContext dirContext) throws ProfileManagerException {
            try {
                SearchControls ctls = getReadSearchControl();
                ctls.setSearchScope(scope);
                ctls.setReturningAttributes(attrs);
                NamingEnumeration<SearchResult> namingEnumeration = dirContext.search(searchBase, filter, ctls);
                List<IDistributionList> distributionLists = new ArrayList<IDistributionList>();
                while (namingEnumeration.hasMore()) {
                    SearchResult searchResult = namingEnumeration.next();
                    distributionLists.add(new DistributionListImpl(getContext(), searchResult, Subscriber.this));
                }
                return distributionLists.toArray(new IDistributionList[distributionLists.size()]);
            } catch (NamingException e) {
                throw new HostException("Search failed: " + e, e);
            }
        }

        private SearchControls getReadSearchControl() {
            SearchControls readSearchControls = new SearchControls();
            readSearchControls.setTimeLimit(getContext().getConfig().getReadTimeout());
            return readSearchControls;
        }

        private BaseContext getContext() {
            return context;
        }
    }

    private class DestroyTask implements Callable<Object> {
        private String dn;

        public DestroyTask(String dn) {
            this.dn = dn;
        }

        public Object call() throws Exception {
            LdapServiceInstanceDecorator serviceInstance = getContext().getServiceInstance(Direction.WRITE);
            DirContext dirContext = null;
            boolean release = false;
            try {
                dirContext = getContext().getDirContext(serviceInstance, Direction.WRITE);
                dirContext.destroySubcontext(dn);
                log.available(serviceInstance.getProtocol(), serviceInstance.getHost(), serviceInstance.getPort());
                return null;
            } catch (CommunicationException e) {
                log.notAvailable(serviceInstance.getProtocol(), serviceInstance.getHost(), serviceInstance.getPort(), e.toString());
                getContext().getServiceLocator().reportServiceError(serviceInstance.getDecoratedServiceInstance());
                release = true;
                throw new RetryException(new HostException("Could not delete distribution list. " + e, e));
            } catch (NamingException e) {
                throw new HostException("Could not delete distribution list. " + e, e);
            } finally {
                getContext().returnDirContext(dirContext, release);
            }
        }
    }
    public Set<Entry<String, String[]>> getAttributes(ProfileLevel profileLevel) {
        if (!attributeLevels.containsValue(profileLevel)) {
            return null; // No attributes on specified level
        }
        Map<String, String[]> result = new HashMap<String, String[]>();
        Set<Entry<String, ProfileLevel>> levels = attributeLevels.entrySet();
        if (levels == null) {
            return null;
        }
        Iterator<Entry<String, ProfileLevel>> i = levels.iterator();
        while (i.hasNext()) {
            Entry<String, ProfileLevel> next = i.next();
            if (next.getValue().equals(profileLevel)) {
                result.put(next.getKey(), profileAttributes.get(next.getKey()).getData());
            }
        }
        return result.entrySet();
    }

	@Override
	/**
	 * Forward compatibility with MIO but old MoIP does noet have message ID for greetings
	 */
	public String getGreetingMessageId(GreetingSpecification specification)
			throws ProfileManagerException {
		// TODO Auto-generated method stub
		return null;
	}
}
