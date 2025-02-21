package com.abcxyz.services.moip.migration.profilemanager.moip;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.util.check.Check;
import com.mobeon.masp.profilemanager.UnknownAttributeException;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Iterator;
/**
 * Documentation
 *
 * @author mande
 */
public class ProfileSettings implements ICommunity, ICos {
    private static final ILogger LOG = ILoggerFactory.getILogger(ProfileSettings.class);

    /**
     * Maps application attribute names to data for that attribute
     */
    protected ProfileAttributes profileAttributes;
    protected static final EnumSet<AttributeType> stringTypesSet = EnumSet.of(AttributeType.STRING, AttributeType.XSTRING);
    protected BaseContext context;

    public ProfileSettings(BaseContext context) {
        this.context = context;
        profileAttributes = new ProfileAttributes(context);
    }

    public ProfileSettings(BaseContext context, ProfileAttributes profileAttributes) {
        this.context = context;
        this.profileAttributes = profileAttributes;
    }

    /**
     * Retrieves subscriber string attribute. If the attribute is multivalue, the first entry is returned.
     *
     * @param applicationName the attribute to retrieve
     * @return the value of the retrieved attribute
     * @throws com.mobeon.masp.profilemanager.UnknownAttributeException
     *          if an invalid attribute was supplied
     */
    public String getStringAttribute(String applicationName) throws UnknownAttributeException {
        if (LOG.isInfoEnabled()) LOG.info("getStringAttribute(applicationName=" + applicationName + ")");
        String[] attributes = getStringAttributesWorker(applicationName);
        if (attributes.length > 0) {
            if (LOG.isInfoEnabled()) LOG.info("getStringAttribute(String) returns " + attributes[0]);
            return attributes[0];
        } else {
            // This should not happen.
            String errMsg = "Attribute <" + applicationName + "> has size 0";
            throw new UnknownAttributeException(errMsg);
        }
    }

    /**
     * Retrieves subscriber attributes
     *
     * @param applicationName the application name of the attribute to retrieve
     * @return the values of the retrieved attribute
     * @throws com.mobeon.masp.profilemanager.UnknownAttributeException
     *          if an invalid application attribute name was supplied
     */
    public String[] getStringAttributes(String applicationName) throws UnknownAttributeException {
        if (LOG.isInfoEnabled()) LOG.info("getStringAttributes(applicationName=" + applicationName + ")");
        String[] values = getStringAttributesWorker(applicationName);
        if (LOG.isInfoEnabled()) LOG.info("getStringAttributes(String) returns " + Arrays.toString(values));
        return values;
    }

    public String[] getStringAttributesWorker(String applicationName) throws UnknownAttributeException {
        // Find metadata
        ProfileMetaData profileMetaData = getMetaData(applicationName);
        // Is applicationName a string attribute?
        if (!stringTypesSet.contains(profileMetaData.getType())) {
            String errMsg = "Attribute <" + applicationName + "> is not of type string/xstring";
            if (LOG.isDebugEnabled()) LOG.debug(errMsg);
            throw new UnknownAttributeException(errMsg);
        }
        String[] userRegisterData = getUserRegisterData(applicationName, profileMetaData);
        if (profileMetaData.getType() == AttributeType.XSTRING) {
            Check check = new Check();
            for (int i = 0; i < userRegisterData.length; i++) {
                String unchecked = check.checkout(userRegisterData[i]);
                if (unchecked == null) {
                    // This should not happen.
                    String errMsg = check.getErrorMessage() + " Attribute <" + applicationName + "> could not be decoded";
                    throw new UnknownAttributeException(errMsg);
                } else {
                    userRegisterData[i] = unchecked;
                }
            }
        }
        return userRegisterData;
    }

    /**
     * Returns the metadata for the application attribute
     *
     * @param attribute application attribute to get metadata for
     * @return metadata for the attribute
     * @throws UnknownAttributeException
     *          if metadata for attribute can not be found
     */
    protected ProfileMetaData getMetaData(String attribute) throws UnknownAttributeException {
        Map<String, ProfileMetaData> applicationAttributeMap = getContext().getConfig().getApplicationAttributeMap();
        if (applicationAttributeMap.containsKey(attribute)) {
            return applicationAttributeMap.get(attribute);
        } else {
            String errMsg = "Could not find metadata for " + attribute;
            if (LOG.isDebugEnabled()) LOG.debug(errMsg);
            throw new UnknownAttributeException(errMsg);
        }
    }

    /**
     * Gets the cached data from the user register. The data is stored as a string array. If no data is cached,
     * the configured default data is returned. If no default data exists, UnknownAttributeException is thrown
     *
     * @param applicationName
     * @param metaData        the meta data for the attribute
     * @return the user registerService data for the attribute, or default data if user register data does not exist
     * @throws UnknownAttributeException
     *          - if no data exists for the attribute
     */
    private String[] getUserRegisterData(String applicationName, ProfileMetaData metaData) throws UnknownAttributeException {
        if (profileAttributes.containsKey(applicationName)) {
            ProfileAttribute profileAttribute = profileAttributes.get(applicationName);
            return profileAttribute.getData();
        } else {
            // Check for default value
            String[] defaultValue = metaData.getDefaultValue();
            if (defaultValue != null) {
                if (LOG.isDebugEnabled()) {
                    if (LOG.isDebugEnabled()) LOG.debug("Using default value " + Arrays.deepToString(defaultValue) + " for attribute " + applicationName);
                }
                return defaultValue;
            } else {
                if (LOG.isDebugEnabled()) LOG.debug("No value found for attribute " + applicationName);
                throw new UnknownAttributeException(applicationName);
            }
        }
    }

    /**
     * Retrieves subscriber integer attribute. If the attribute is multivalue, the first entry is returned.
     *
     * @param applicationName the attribute to retrieve
     * @return the values of the retrieved attribute
     * @throws com.mobeon.masp.profilemanager.UnknownAttributeException
     *          if an invalid attribute was supplied
     */
    public int getIntegerAttribute(String applicationName) throws UnknownAttributeException {
        if (LOG.isInfoEnabled()) LOG.info("getIntegerAttribute(applicationName=" + applicationName + ")");
        int[] attributes = getIntegerAttributesWorker(applicationName);
        if (attributes.length > 0) {
            if (LOG.isInfoEnabled()) LOG.info("getIntegerAttribute(String) returns " + attributes[0]);
            return attributes[0];
        } else {
            // This should not happen.
            String errMsg = "Attribute <" + applicationName + "> has size 0";
            throw new UnknownAttributeException(errMsg);
        }
    }

    /**
     * Retrieves subscriber integer attributes
     *
     * @param applicationName the attribute to retrieve
     * @return the values of the retrieved attribute
     * @throws com.mobeon.masp.profilemanager.UnknownAttributeException
     *          if an invalid attribute was supplied
     */
    public int[] getIntegerAttributes(String applicationName) throws UnknownAttributeException {
        if (LOG.isInfoEnabled()) LOG.info("getIntegerAttributes(applicationName=" + applicationName + ")");
        int[] values = getIntegerAttributesWorker(applicationName);
        if (LOG.isInfoEnabled()) LOG.info("getIntegerAttributes(String) returns " + values);
        return values;
    }

    public int[] getIntegerAttributesWorker(String applicationName) throws UnknownAttributeException {
        // Find metadata
        ProfileMetaData profileMetaData = getMetaData(applicationName);
        // Is applicationName an integer attribute?
        if (profileMetaData.getType() != AttributeType.INTEGER) {
            String errMsg = "Attribute <" + applicationName + "> is not of type integer";
            if (LOG.isDebugEnabled()) LOG.debug(errMsg);
            throw new UnknownAttributeException(errMsg);
        }
        String[] userRegisterData = getUserRegisterData(applicationName, profileMetaData);
        int[] result = new int[userRegisterData.length];
        for (int i = 0; i < userRegisterData.length; i++) {
            try {
                result[i] = Integer.parseInt(userRegisterData[i]);
            } catch (NumberFormatException e) {
                // This should not happen.
                String errMsg = "Integer attribute <" + applicationName + "> could not be parsed";
                throw new UnknownAttributeException(errMsg);
            }
        }
        return result;
    }

    /**
     * Retrieves subscriber boolean attribute. If the attribute is multivalue, the first entry is returned.
     *
     * @param applicationName the attribute to retrieve
     * @return the values of the retrieved attribute
     * @throws com.mobeon.masp.profilemanager.UnknownAttributeException
     *          if an invalid attribute was supplied
     */
    public boolean getBooleanAttribute(String applicationName) throws UnknownAttributeException {
        if (LOG.isInfoEnabled()) LOG.info("getBooleanAttribute(applicationName=" + applicationName + ")");
        boolean[] attributes = getBooleanAttributesWorker(applicationName);
        if (attributes.length > 0) {
            if (LOG.isInfoEnabled()) LOG.info("getBooleanAttribute(String) returns " + attributes[0]);
            return attributes[0];
        } else {
            // This should not happen.
            String errMsg = "Attribute <" + applicationName + "> has size 0";
            throw new UnknownAttributeException(errMsg);
        }
    }

    /**
     * Retrieves subscriber boolean attributes
     *
     * @param applicationName the attribute to retrieve
     * @return the values of the retrieved attribute
     * @throws com.mobeon.masp.profilemanager.UnknownAttributeException
     *          if an invalid attribute was supplied
     */
    public boolean[] getBooleanAttributes(String applicationName) throws UnknownAttributeException {
        if (LOG.isInfoEnabled()) LOG.info("getBooleanAttribute(applicationName=" + applicationName + ")");
        boolean[] values = getBooleanAttributesWorker(applicationName);
        if (LOG.isInfoEnabled()) LOG.info("getBooleanAttributes(String) returns " + values);
        return values;
    }

    public boolean[] getBooleanAttributesWorker(String applicationName) throws UnknownAttributeException {
        // Find metadata
        ProfileMetaData profileMetaData = getMetaData(applicationName);
        // Is applicationName an integer attribute?
        if (profileMetaData.getType() != AttributeType.BOOLEAN) {
            String errMsg = "Attribute <" + applicationName + "> is not of type boolean";
            if (LOG.isDebugEnabled()) LOG.debug(errMsg);
            throw new UnknownAttributeException(applicationName);
        }
        String[] userRegisterData = getUserRegisterData(applicationName, profileMetaData);
        boolean[] result = new boolean[userRegisterData.length];
        String trueString;
        try {
            trueString = profileMetaData.getTrueString();
        } catch (MetaDataException e) {
            throw new UnknownAttributeException("Attribute <" + applicationName + "> has no boolean strings");
        }
        for (int i = 0; i < userRegisterData.length; i++) {
            result[i] = userRegisterData[i].compareTo(trueString) == 0;
        }

        return result;
    }

    protected BaseContext getContext() {
        return context;
    }
    /**
     *  @return set of all attributes in the profile treated as strings
     */
    public Set<Entry<String, String []>>getAttributes() {
        Set<Entry<String, ProfileAttribute>> set = profileAttributes.entrySet();
        Iterator<Entry<String, ProfileAttribute>> it = set.iterator();
        Map<String, String []> ret = new HashMap<String, String[]>();
        while (it.hasNext()) {
            Entry<String, ProfileAttribute> e = it.next();
            ret.put(e.getKey(), e.getValue().getData());
        }
        return ret.entrySet();
    }
    
}
