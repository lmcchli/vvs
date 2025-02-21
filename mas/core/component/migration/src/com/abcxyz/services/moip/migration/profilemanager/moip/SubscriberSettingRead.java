package com.abcxyz.services.moip.migration.profilemanager.moip;

import com.mobeon.masp.profilemanager.UnknownAttributeException;

/**
 * Interface for getting attributes
 *
 * @author mande
 */
public interface SubscriberSettingRead {
    /**
     * Retrieves a string attribute's value. If the attribute is multivalue, the first entry is returned.
     *
     * @param attribute the attribute to retrieve
     * @return the value of the retrieved attribute
     * @throws UnknownAttributeException
     *          if an invalid attribute was supplied
     */
    String getStringAttribute(String attribute) throws UnknownAttributeException;

    /**
     * Retrieves a string attribute's values
     *
     * @param attribute the attribute to retrieve
     * @return the values of the retrieved attribute
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    String[] getStringAttributes(String attribute) throws UnknownAttributeException;

    /**
     * Retrieves an integer attribute's value. If the attribute is multivalue, the first entry is returned.
     *
     * @param attribute the attribute to retrieve
     * @return the values of the retrieved attribute
     * @throws UnknownAttributeException
     *          if an invalid attribute was supplied
     */
    int getIntegerAttribute(String attribute) throws UnknownAttributeException;

    /**
     * Retrieves an integer attribute's values
     *
     * @param attribute the attribute to retrieve
     * @return the values of the retrieved attribute
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    int[] getIntegerAttributes(String attribute) throws UnknownAttributeException;

    /**
     * Retrieves a boolean attribute's value. If the attribute is multivalue, the first entry is returned.
     *
     * @param attribute the attribute to retrieve
     * @return the values of the retrieved attribute
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    boolean getBooleanAttribute(String attribute) throws UnknownAttributeException;

    /**
     * Retrieves a boolean attribute's values
     *
     * @param attribute the attribute to retrieve
     * @return the values of the retrieved attribute
     * @throws UnknownAttributeException if an invalid attribute was supplied
     */
    boolean[] getBooleanAttributes(String attribute) throws UnknownAttributeException;
}
