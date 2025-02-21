/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs;

import java.io.File;
import java.io.FileFilter;
import java.util.Properties;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.ANotifierNotificationInfo;


/**
 * The INotifierMfsEventManager interface defines the methods that the Notifier plug-in can invoke to manipulate
 * files in the MiO file system.
 */
public interface INotifierMfsEventManager {
    
    /**
     * The NotifierFileStatusEnum contains the possible statuses of a file.
     */
    public static enum NotifierFileStatusEnum {
        /**
         * The file does not exist.
         */
        FILE_DOES_NOT_EXIST,
        
        /**
         * The file exists and no check was done regarding its validity.
         */
        FILE_EXISTS_NO_VALIDATION,
        
        /**
         * The file exists and is valid.
         */
        FILE_EXISTS_AND_VALID,
        
        /**
         * The file exists but is no longer valid.
         */
        FILE_EXISTS_AND_INVALID,
        
        /**
         * The status of the file could not be determined.
         */
        UNABLE_TO_DETERMINE_STATUS
    }

    /**
     * Gets the status of a file for the specified telephone number.
     * This method is used to check the existence and validity of phone-number-specific files.
     * @param telephoneNumber the telephone number under which to check
     * @param fileName the name of the file for which to check the status
     * @param validityPeriodInMin the time period in minutes used to determine if the existing file is still valid
     * @return {@link NotifierFileStatusEnum} constant that corresponds to the status of the specified file
     */
    public NotifierFileStatusEnum fileExistsValidation(String telephoneNumber, String fileName, int validityPeriodInMin);

    /**
     * Stores the given properties in the specified file for the specified telephone number.
     * This method is used to store phone-number-specific properties.
     * @param telephoneNumber the telephone number under which the file should be created or found if already created
     * @param fileName the name of the file in which to store the properties
     * @param properties the name-value pairs to store
     * @return true if the properties was successfully stored; false otherwise
     */
    public boolean storeProperties(String telephoneNumber, String fileName, Properties properties);
    
    /**
     * Gets the properties stored in the specified file under the specified telephone number.
     * This method is used to retrieve phone-number-specific properties.
     * @param telephoneNumber the telephone number under which the file can be found
     * @param fileName the name of the file containing the properties
     * @return the properties from the specified file, or null if an error occurred while retrieving the properties
     */
    public Properties getProperties(String telephoneNumber, String fileName);
    
    /**
     * Removes the phone-number-specific file from the MiO file system.
     * @param telephoneNumber the telephone number under which the file is stored
     * @param fileName the name of the file to remove
     * @return true if the file is successfully removed or does not exist; false otherwise
     */
    public boolean removeFile(String telephoneNumber, String fileName);
    
    /**
     * Acquires the lock file for the specified telephone number.
     * <p>
     * Reasons for failure to acquire the lock file include:
     * <ul>
     * <li>A valid lock file with the specified file name already exists
     * <li>Failure to access the file system directory
     * <li>Failure to create the new lock file
     * </ul>
     * @param telephoneNumber the telephone number for which the lock file is acquired
     * @param lockFileName the name of the lock file to acquire 
     * @param validityPeriodInSeconds the time period used to determine if an existing lock file is still valid
     * @return lock id; zero if the lock file was not acquired
     */
    public long acquireLockFile(String telephoneNumber, String lockFileName, int validityPeriodInSeconds);
    
    /**
     * Releases the lock file with the specified lock id.
     * <p>
     * Reasons for failure to release the lock file include:
     * <ul>
     * <li>Failure to access the file system directory
     * <li>Failure to delete the lock file
     * </ul>
     * @param telephoneNumber the telephone number for which the lock file is released
     * @param lockFileName the name of the lock file to release
     * @param lockId identity of the lock to be released
     * @return true if the lock file was released or the specified lock id was zero or the specified lock id did not match the id of the current lock file; 
     *         false otherwise
     */
    public boolean releaseLockFile(String telephoneNumber, String lockFileName, long lockId);
    
    /**
     * Gets the event files for the specified telephone number that match the specified filter.
     * @param telephoneNumber the telephone number under which the files are stored
     * @param fileFilter the file filter to use to get the subset of files under the telephone number
     * @return the event files for the specified telephone number that match the specified filter, 
     *         or null if an error occurred retrieving the files or no files matched the specified filter
     */
    public File[] getEventFiles(String telephoneNumber, FileFilter fileFilter);
    
    /**
     * Gets the content of the specified file as a string value.
     * @param telephoneNumber the telephone number under which the file is stored
     * @param fileName the name of the file from which to get the content
     * @return the content of the specified file as a string value
     * @throws NotifierMfsException if an error occurs while trying to get the file content
     */
    public String getFileContentAsString (String telephoneNumber, String fileName) throws NotifierMfsException;

    /**
     * Gets the content of the specified file as an array of bytes.
     * @param telephoneNumber the telephone number under which the file is stored
     * @param fileName the name of the file from which to get the content
     * @return the content the specified file as an array of bytes
     * @throws NotifierMfsException if an error occurs while trying to get the file content
     */
    public byte[] getFileContentAsBytes (String telephoneNumber, String fileName) throws NotifierMfsException;
    
    /**
     * Retrieves the slam-down call information from the specified file.
     * @param telephoneNumber the telephone number under which the file is stored
     * @param fileName the name of the file containing the information
     * @return information for all slam-down calls in the file
     * @throws NotifierMfsException if an error occurs while retrieving the information (for example: file not found, parsing error)
     */
    public ANotifierSlamdownCallInfo[] getSlamdownCallInfo(String telephoneNumber, String fileName) throws NotifierMfsException;

    /**
     * Gets an {@link INotifierNewMessageCallInfo} object that contains the information regarding the new message deposit 
     * which triggered the current notification.
     * <p>
     * The properties obtained by calling {@link ANotifierNotificationInfo#getProperties()} will be used to find the message in the file system.  
     * Hence, the properties should include the original properties received by NTF.
     * @param notificationInfo the ANotifierNotificationInfo object containing the information regarding the current notification
     * @return information for the new message deposit triggering the current notification
     * @throws NotifierMfsException if an error occurs while retrieving the information (for example: file not found, reading error)
     */
    public INotifierNewMessageCallInfo getNewMessageCallInfo(ANotifierNotificationInfo notificationInfo) throws NotifierMfsException;
}
