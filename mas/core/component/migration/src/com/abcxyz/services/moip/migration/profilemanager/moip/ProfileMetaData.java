/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.abcxyz.services.moip.migration.profilemanager.moip;

import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.IGroup;
import com.mobeon.common.configuration.UnknownParameterException;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Class containing information about UserRegister data: type, default values, constraints, etc.
 *
 * @author emahagl
 */
public class ProfileMetaData {
    private static final ILogger LOG = ILoggerFactory.getILogger(ProfileMetaData.class);
    /**
     * The application name of the attribute
     */
    private final String applicationName;
    /**
     * The User Register name of the attribute
     */
    private final String userRegisterName;
    /**
     * The application data type of the attribute
     */
    private final AttributeType type;
    /**
     * The string representing the true value of a boolean attribute in the user register
     */
    private String truestring;
    /**
     * The string representing the false value of a boolean attribute in the user register
     */
    private String falsestring;
    /**
     * The level to write attribute data to in the user register
     */
    private ProfileLevel writeLevel;
    /**
     * If the attribute is readonly or not
     */
    private boolean readOnly;
    /**
     * Default value of the attribute. No default value is represented by null.
     */
    private String[] defaultValue;
    private List<ProfileLevel> searchOrder;

    /**
     * Contains information about configured syntax rules for this attribute
     */
    private AttributeValueControl attributeValueControl;

    /**
     * Creates meta data for one application parameter
     *
     * @param metaData configuration group for the parameter
     * @throws MetaDataException if a required configuration parameter is missing
     */
    public ProfileMetaData(IGroup metaData, List<ProfileLevel> defaultSearchOrder) throws MetaDataException {
        try {
            applicationName = metaData.getName();
            userRegisterName = metaData.getString("userregistername");
            type = AttributeType.valueOf(metaData.getString("type").toUpperCase());
            switch (type) {
                case STRING:
                    break; // Nothing stringspecific yet
                case XSTRING:
                    break; // Nothing stringspecific yet
                case INTEGER:
                    break; // Nothing integerspecific yet
                case BOOLEAN:
                    truestring = metaData.getString("true");
                    falsestring = metaData.getString("false");
                    break;
            }

            try {
                writeLevel = ProfileLevel.valueOf(metaData.getString("writelevel", "unknown").toUpperCase());
            } catch (IllegalArgumentException e) {
                writeLevel = ProfileLevel.UNKNOWN;
            }
            readOnly = (writeLevel == ProfileLevel.UNKNOWN);
            try {
                defaultValue = new String[]{metaData.getString("default")};
                if (LOG.isDebugEnabled()) {
                    if (LOG.isDebugEnabled()) LOG.debug("Default value " + Arrays.deepToString(defaultValue) + " found for attribute " + applicationName);
                }
            } catch (UnknownParameterException e) {
                if (LOG.isDebugEnabled()) LOG.debug("Default value not found for attribute " + applicationName);
            }
            try {
                String searchOrderString = metaData.getString("searchorder");
                searchOrder = getSearchOrder(searchOrderString);
            } catch (UnknownParameterException e) {
                if (LOG.isDebugEnabled()) LOG.debug("Using default search order <" + defaultSearchOrder + "> for attribute " + applicationName);
                searchOrder = defaultSearchOrder;
            }

            if (!readOnly && (type != AttributeType.BOOLEAN)) createAttributeValueControl(metaData);

        } catch (UnknownParameterException e) {
            throw new MetaDataException(e.getMessage());
        }
        checkValidity();
    }

    /**
     * Checks that the metadata class is valid
     *
     * @throws MetaDataException if any requried data is empty or null
     */
    private void checkValidity() throws MetaDataException {
        // No strings should be empty
        if (applicationName.length() == 0) {
            throw new MetaDataException("ApplicationName is empty");
        }
        if (userRegisterName.length() == 0) {
            throw new MetaDataException("UserRegisterName is empty");
        }
        switch (type) {
            case STRING:
                break; // Nothing stringspecific yet
            case XSTRING:
                break; // Nothing stringspecific yet
            case INTEGER:
                break; // Nothing integerspecific yet
            case BOOLEAN:
                if (truestring.length() == 0) {
                    throw new MetaDataException("TrueString is empty");
                }
                if (falsestring.length() == 0) {
                    throw new MetaDataException("FalseString is empty");
                }
                break;
        }
        // Searchorder should not be empty
        if (searchOrder.isEmpty()) {
            throw new MetaDataException("SearchOrder is empty");
        }
    }

    private void createAttributeValueControl(IGroup metaData) {
        try {
            IGroup syntaxGroup = metaData.getGroup("syntax");
            if (type == AttributeType.INTEGER)
                attributeValueControl = new AttributeValueControl(syntaxGroup, false);
            else
                attributeValueControl = new AttributeValueControl(syntaxGroup, true);

        } catch (ConfigurationException e) {
            if (LOG.isDebugEnabled()) LOG.debug("Syntax group not found for attribute " + applicationName);
        } catch (MetaDataException e) {
            // ToDo throw or just log?
            LOG.error("Invalid configuration for attribute " + applicationName + " " + e.getMessage());
        }
    }

    /**
     * Returns the application name for the attribute described by this ProfileMetaData object
     *
     * @return the application name
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Returns the user register name for the attribute described by this ProfileMetaData object
     *
     * @return the user register name
     */
    public String getUserRegisterName() {
        return userRegisterName;
    }

    /**
     * Returns the application data type for the attribute described by this ProfileMetaData object
     *
     * @return the application data type
     */
    public AttributeType getType() {
        return type;
    }

    public String getTrueString() throws MetaDataException {
        if (type == AttributeType.BOOLEAN && truestring != null) {
            return truestring;
        } else {
            // Todo: throw exception? Return null?
            throw new MetaDataException("Invalid request of truestring for attribute " + getApplicationName() +
                    " of type " + getType());
        }
    }

    public String getFalseString() throws MetaDataException {
        if (type == AttributeType.BOOLEAN && falsestring != null) {
            return falsestring;
        } else {
            // Todo: throw exception? Return null?
            throw new MetaDataException("Invalid request of falsestring for attribute " + getApplicationName() +
                    " of type " + getType());
        }
    }

    /**
     * Retrieves the user register write level for the profile attribute
     *
     * @return the user register write level for the profile attribute
     * @throws MetaDataException if profile attribute is read only
     */
    public ProfileLevel getWriteLevel() throws MetaDataException {
        if (!isReadOnly()) {
            return writeLevel;
        } else {
            throw new MetaDataException(getApplicationName() + " is read-only");
        }
    }

    /**
     * Returns the UserRegister string representation of the boolean profile attribute
     *
     * @param value the boolean value to return string representation for
     * @return the string representation of the boolean value
     * @throws MetaDataException if profile attribute is not of type boolean
     */
    public String getBooleanString(boolean value) throws MetaDataException {
        if (value) {
            return getTrueString();
        } else {
            return getFalseString();
        }
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Retrieves default value for attribute or null if no default value is specified
     *
     * @return default value for attribute
     */
    public String[] getDefaultValue() {
        return defaultValue;
    }

    /**
     * Retrieves the list of search order for attribute
     *
     * @return list of search order for attribute
     */
    public List<ProfileLevel> getSearchOrder() {
        return searchOrder;
    }

    /**
     * Retrieves the AttributeValueControl object to check the values
     *
     * @return the AttributeValueControl
     */
    public AttributeValueControl getAttributeValueControl() {
        return attributeValueControl;
    }

    /**
     * Creates a list of profile level search order from a search order string
     *
     * @param searchOrderString a string defining the profile level search order
     * @return a list of profile level search order
     * @logs.warn "Could not parse level &lt;level&gt; in search order &lt;searchorderstring&gt;" -
     * if one of the levels in an attributes searchorder string could not be parsed. This is due to
     * an erronous configuration file or schema file. Allowed values should be "community", "cos",
     * "user" and "billing".
     */
    protected static List<ProfileLevel> getSearchOrder(String searchOrderString) {
        List<ProfileLevel> searchOrderList = new ArrayList<ProfileLevel>();
        String[] levels = searchOrderString.split(",");
        for (String level : levels) {
            try {
                searchOrderList.add(ProfileLevel.valueOf(level.toUpperCase()));
            } catch (IllegalArgumentException e) {
                LOG.warn("Could not parse level " + level + " in search order " + searchOrderString);
            }
        }
        return searchOrderList;
    }
}
