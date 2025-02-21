package com.abcxyz.services.moip.migration.profilemanager.moip;

import com.mobeon.masp.profilemanager.HostException;
import com.mobeon.masp.profilemanager.ProfileManagerException;
import com.mobeon.masp.profilemanager.UnknownAttributeException;

/**
 * Interface for getting and setting attributes
 *
 * @author mande
 */
public interface SubscriberSetting extends SubscriberSettingRead {

    /**
     * Sets a string attribute's value
     *
     * @param attribute the attribute to set
     * @param value     the value to set for the supplied attribute, if null the attribute will be removed
     * @throws HostException             If no DirContext could be created or if writing failed
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    void setStringAttribute(String attribute, String value) throws ProfileManagerException;

    /**
     * Sets a string attribute's values
     *
     * @param attribute the attribute to set
     * @param values    the values to set for the supplied attribute, if null the attribute will be removed
     * @throws HostException             If no DirContext could be created or if writing failed
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    void setStringAttributes(String attribute, String[] values) throws ProfileManagerException;

    /**
     * Sets an integer attribute's value
     *
     * @param attribute the attribute to set
     * @param value     the value to set for the supplied attribute
     * @throws HostException             If no DirContext could be created or if writing failed
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    void setIntegerAttribute(String attribute, int value) throws ProfileManagerException;

    /**
     * Sets an integer attribute's values
     *
     * @param attribute the attribute to set
     * @param value     the values to set for the supplied attribute, if null the attribute will be removed
     * @throws HostException             If no DirContext could be created or if writing failed
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    void setIntegerAttributes(String attribute, int[] value) throws ProfileManagerException;

    /**
     * Sets a boolean attribute's value
     *
     * @param attribute the attribute to set
     * @param value     the value to set for the supplied attribute
     * @throws HostException             If no DirContext could be created or if writing failed
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    void setBooleanAttribute(String attribute, boolean value) throws ProfileManagerException;

    /**
     * Sets a boolean attribute's values
     *
     * @param attribute the attribute to set
     * @param value     the values to set for the supplied attribute, if null the attribute will be removed
     * @throws HostException             If no DirContext could be created or if writing failed
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    void setBooleanAttributes(String attribute, boolean[] value) throws ProfileManagerException;
}
