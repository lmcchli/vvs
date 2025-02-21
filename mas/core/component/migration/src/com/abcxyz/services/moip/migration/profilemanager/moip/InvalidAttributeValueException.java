package com.abcxyz.services.moip.migration.profilemanager.moip;

import com.mobeon.masp.profilemanager.ProfileManagerException;

/**
 * This exception is thrown when invalid values on an attribute has been set into ProfileManager
 *
 * @author emahagl
 */
public class InvalidAttributeValueException extends ProfileManagerException {

    public InvalidAttributeValueException(String message) {
        super(message);
    }
}
