/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database;

import java.util.Iterator;


/**
 * The ANotifierDatabaseCosProfile abstract class defines the methods that can be invoked to retrieve information
 * stored in a class of service database profile.
 * <p>
 * Both the NTF component and Notifier plug-in can implement a concrete class extending this abstract class.
 * <p>
 * The Notifier plug-in can obtain a {@link ANotifierDatabaseSubscriberProfile} by performing a MiO database subscriber lookup.
 * It can then get the ANotifierDatabaseCosProfile object associated to the subscriber by invoking {@link ANotifierDatabaseSubscriberProfile#getCosProfile()}.  
 * In this case, the ANotifierDatabaseCosProfile object is an instance of the NTF component concrete class.
 * <p> 
 * Alternatively, the Notifier plug-in can create its own concrete class that extends ANotifierDatabaseCosProfile.
 * Instances of this Notifier plug-in concrete class can be populated as needed and used wherever a ANotifierDatabaseCosProfile object is needed.
 * This can be useful when processing notifications for users who are not provisioned in the MiO database.
 */
public abstract class ANotifierDatabaseCosProfile {

    /**
     * Gets the scheme-specific-part of all the subscriber identities that start with the specified scheme.  
     * This method will return <code>null</code> if no identities are found.
     * <p>
     * For example, the scheme is "msid" and the scheme-specific-part is "9914b3e658dd74bc" in the following msid identity URI:
     * <PRE>msid:9914b3e658dd74bc</PRE>
     * 
     * @param scheme the scheme of the desired identities 
     * @return the scheme-specific-part of all the cos identities that start with the specified scheme, 
     *         or <code>null</code> if no identities are found.
     */
    public String[] getIdentities(String scheme) {
        return null;
    }

    /**
     * Gets the string values for the specified attribute name.
     * This method will return null if no attribute values are found.
     * @param attributeName the name of the attribute for which to retrieve the attribute string values
     * @return the string values for the specified attribute name, or null if no attribute values are found
     */
    public String[] getStringAttributes(String attributeName) {
        return null;
    }

    /**
     * Gets the integer values for the specified attribute name.
     * This method will return null if no attribute values are found or at least one of the attribute values could not be parsed into an integer.
     * <p>
     * In the NTF component concrete class, this method is a convenience method; 
     * the result obtained from invoking {@link ANotifierDatabaseCosProfile#getStringAttributes(String)} is parsed as integers and returned.
     * 
     * @param attributeName the name of the attribute for which to retrieve the attribute integer values
     * @return the attribute integer values for the specified attribute name, 
     *         or null if no attribute values are found or if at least one of the attribute values could not be parsed into an integer
     */
    public int[] getIntegerAttributes(String attributeName) {
        return null;
    }

    /**
     * Gets the boolean values for the specified attribute name.
     * This method will return null if no attribute values are found.
     * <p>
     * In the NTF component concrete class, this method is a convenience method; 
     * the result obtained from invoking {@link ANotifierDatabaseCosProfile#getStringAttributes(String)} is converted into boolean values and returned.
     * The string values "yes" and "true" are converted to true; all other values are converted to false.
     * 
     * @param attributeName the name of the attribute for which to retrieve the attribute boolean values
     * @return the attribute boolean values for the specified attribute name, or null if no attribute boolean values are found
     */
    public boolean[] getBooleanAttributes(String attributeName) {
        return null;
    }

    /**
     * Gets an iterator on all attribute names in this cos profile.
     * The order in which this iterator returns the attribute names is not guaranteed. 
     * @return an iterator on all attribute names in this profile
     */
    public Iterator<String> getAttributeNameIterator() {
        return null;
    }

}
