package com.mobeon.common.util;

import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;

/**
 * Provides a basic class for fax numbers.
 * 
 *
 * @author lmchemc
 */
public class FaxNumber {

    private String number;
    private boolean hasPrefix;
    
    private FaxNumber(String number) 
            throws DataException { 
        if (number == null) {
            throw new NullPointerException("number cannot be null");
        }
        
        number = number.trim();
        
        if (number.startsWith("+")) {
            hasPrefix = true;
            number = number.substring(1);
        }
        
        // no specific requirements, but no expectations that fax number should
        // be either smaller or greater than these values
        // NOTE: prov. business rule allows between 4 and 15 digits, VVA number analysis for manual 
        // print number is more flexible
        if (number.length() < 4 || number.length() > 24) {
            throw new DataException("number [" + number + "] must be between 4 and 24 digits"); 
        }

        try {
            Long.parseLong(number);
        } catch (NumberFormatException e) {
            throw new DataException("number[" + number + "] must be numerical");
        }
        
        this.number = number;
    }
    
    /**
     * Creates a new fax number using the given <code>number</code>.
     * 
     * @param number  represents the fax number
     * @return  an object that represents a valid fax number
     * @throws DataException  if the fax number does not have the correct format
     */
    public static FaxNumber parse(String number) 
            throws DataException {
        return new FaxNumber(number);
    }
    
    /**
     * Creates a new fax number using the subscriber's default
     * fax print number attribute in MCD database.
     * 
     * @param profile  the subscriber's profile to be queried
     * @return  an object that represents a valid default fax number as found in the subscriber's profile
     * @throws DataException  if the fax number does not have the correct format
     * @throws NotFoundException  if the subscriber's profile does not have a default fax number
     */
    public static FaxNumber create(IDirectoryAccessSubscriber profile) 
            throws DataException, NotFoundException {
        if (profile == null) {
            throw new NullPointerException("profile is null");
        }
        
        FaxNumber faxNumber = null;
        String[] numbers = profile.getStringAttributes(DAConstants.ATTR_FAX_PRINT_NUMBER);
        
        if (numbers != null && numbers.length >= 1) {
            faxNumber = new FaxNumber(numbers[0]);
        } else {
            throw new NotFoundException("Unable to find fax number in subscriber's profile: " + DAConstants.ATTR_FAX_PRINT_NUMBER);
        }
        
        return faxNumber;
    }
    
    /**
     * Returns the fax number with no modifications.
     * 
     * If fax number included the international prefix "+" then
     * that will be included in the returned fax number. 
     * If the fax number did not include the international prefix "+" then
     * that will not be included in the returned fax number. 
     * 
     * @return  the same fax number that was used to initialize the object
     */
    public String toString() {
        if (hasPrefix) {
            return (new StringBuilder("+").append(number)).toString();
        } else {
            return number;
        }
    }
    
    /**
     * Returns the fax number with the international prefix "+" removed
     * from the start of the fax number.
     * 
     * @return  the fax number with international prefix removed
     */
    public String toStringPrefixStripped() {
        return number;
    }    
}