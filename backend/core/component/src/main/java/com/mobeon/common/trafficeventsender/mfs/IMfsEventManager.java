/**
 * 
 */
package com.mobeon.common.trafficeventsender.mfs;

import java.io.File;
import java.io.FileFilter;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;

import com.mobeon.common.trafficeventsender.MfsConfiguration;
import com.mobeon.common.trafficeventsender.TrafficEvent;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;

/**
 * This interface is used to store and retrieve events to and from the MFS file system.
 * 
 * @author egeobli
 */
public interface IMfsEventManager {

	/**
	 * Stores an event to a user's MFS private folder.
	 * 
	 * @param telephoneNumber Phone number.
	 * @param event Event.
	 * @throws TrafficEventSenderException
	 */
	public void storeEvent(String telephoneNumber, TrafficEvent event) throws TrafficEventSenderException;

	/**
     * Retrieves events for a specific file path.
     * 
     *  @param File path
     *  @throws TrafficEventSenderException Throws TrafficEventSenderException on error.
     */
    public TrafficEvent[] retrieveEvents(String filePath, boolean internal) throws TrafficEventSenderException;

	/**
	 * Retrieves events for a specific user.
	 * 
	 *  @param phoneNumber User's phone number.
	 *  @param name Event file name.
	 *  @throws TrafficEventSenderException Throws TrafficEventSenderException on error.
	 */
	public TrafficEvent[] retrieveEvents(String phoneNumber, String name, boolean internal) throws TrafficEventSenderException;

	/**
	 * Retrieves events for a specific telephone number from the specified file.
	 * @param phoneNumber Telephone number for which events are retrieved
	 * @param fileName Event file name
	 * @return Events from the file
	 * @throws TrafficEventSenderException on error.
	 */
	public TrafficEvent[] retrieveEvents(String phoneNumber, String fileName) throws TrafficEventSenderException;
	
	/**
     * Creates the login file, indicating the user has logged in
     * @param telephoneNumber Telephone number of the user
	 * @throws TrafficEventSenderException if any error occurs
	 */
    public void createLoginFile(String telephoneNumber) throws TrafficEventSenderException;
    
    /**
     * Removes the login file, indicating that the user has logged out
     * @param telephoneNumber Telephone number of the user
     * @throws TrafficEventSenderException if any error occurs
     */
    public void removeLoginFile(String telephoneNumber) throws TrafficEventSenderException;
    
    /**
     * Checks if the login file exists in the users events folder
     * @param telephoneNumber
     * @return true if the file exists, or false otherwise
     */
    public boolean loginFileExists(String telephoneNumber);

    /**
     * Checks if the login file exists in the users events folder 
     * and if the file has been created within a certain period of time.  
     * @param telephoneNumber
     * @param validityPeriod (minutes) 
     * @return true if the file exists, or false otherwise
     */
    public boolean loginFileExistsAndValidDate(String telephoneNumber, int validityPeriod);
    
	/**
     * Creates an empty file in the user's events folder.
     * If it already exists, it does nothing.
     * 
     * @param telephoneNumber Telephone number of the subscriber
     * @param fileName Filename of the file to create
     * @throws TrafficEventSenderException If something goes wrong in the creation of the directories or the file
     */
    public void createEmptyFile(String telephoneNumber, String fileName) throws TrafficEventSenderException;
    /**
     * Creates a empty file with properties in the user's events folder.
     *
     *
     * @param telephoneNumber Telephone number of the subscriber
     * @param prefix prefix of the file to create
     * @throws TrafficEventSenderException If something goes wrong in the creation of the directories or the file
     */
    public void createPropertiesFile(String telephoneNumber, String prefix, Map<String, String> properties) throws TrafficEventSenderException;
    /**
     * Return file name of the given extension for the given subscriber
     * @param telephoneNumber
     * @param extension Name of the file extension
     * @return filename if found, null otherwise. 
     */
    public String getFileNameByExtension(String telephoneNumber, String eventName, String extension);

    /**
     * Return file names starting with given characters for the given subscriber
     * @param telephoneNumber
     * @param startingWith Name of the beginning of the file
     * @return filenames if found, null otherwise. 
     */
    public String[] getFilesNameStartingWith(String telephoneNumber, final String startingWith);
    
    /**
     * Returns names of files (including or not the path) selected by the specified filter. 
     * @param telephoneNumber Phone number
     * @param fileFilter Filter that is applied on the query.
     * @param fullPath Full path with file name or file name only
     * @return filenames if found, null otherwise.
     */
    public String[] getEventFileNames(String telephoneNumber, FileFilter fileFilter, boolean fullPath);

    /**
     * Returns an array of file objects selected using the specified filter. 
     * @param telephoneNumber Phone number
     * @param fileFilter Filter that is applied on the query.
     * @return Array of files if found, null otherwise.
     */
    public File[] getEventFiles(String telephoneNumber, FileFilter fileFilter);

    /**
     * Return file path names starting with given characters for the given subscriber
     * @param telephoneNumber
     * @param startingWith Name of the beginning of the file
     * @return file path names if found, null otherwise. 
     */
    public String[] getFilePathsNameStartingWith(String telephoneNumber, final String startingWith);

    /**
     * Checks if the file exists in the users events folder
     * @param telephoneNumber
     * @param fileName Name of the file to check
     * @param internal Defines if the telephoneNumber relates to a subscriber (internal) or non-subscriber (external)
     * @return true if the file exists, or false otherwise
     */
    public boolean fileExists(String telephoneNumber, String fileName, boolean internal);

    /**
     * Checks if the file exists in the users events folder using the given validity period
     * @param telephoneNumber
     * @param fileName Name of the file to check
     * @param validityPeriodInMin Validity period of the file (if it exists)
     * @param internal Defines if the telephoneNumber relates to a subscriber (internal) or non-subscriber (external)
     * @return true if the file exists, or false otherwise
     */
    public boolean fileExists(String telephoneNumber, String fileName, int validityPeriodInMin, boolean internal);

	/**
     * Deletes a file in the user's events folder, if it exists already.
     * If it does not exist, it does nothing.
     * 
     * @param telephoneNumber Telephone number of the subscriber
     * @param fileName Filename of the file to delete
     * @param internal Defines if the telephoneNumber relates to a subscriber (internal) or non-subscriber (external)
     * @throws TrafficEventSenderException If something goes wrong in the deletion of the file
     */
    public boolean removeFile(String telephoneNumber, String fileName, boolean internal) throws TrafficEventSenderException;

    /**
     * Deletes a file
     * 
     * @param absoluteFilePath
     * @throws TrafficEventSenderException If something goes wrong in the deletion of the file
     */
    public boolean removeFile(String absoluteFilePath) throws TrafficEventSenderException;
    
    /**
     * Deletes a file
     * 
     * @param absoluteFilePath
     * @throws TrafficEventSenderException If something goes wrong in the deletion of the file
     */
    public boolean removeFile(String telephoneNumber, String fileName) throws TrafficEventSenderException;

    /**
     * Updates the event manager internal configuration.
     * 
     * @param configuration Configuration information.
     */
    public void updateConfiguration(MfsConfiguration mfsConfiguration);
    
    /**
     * Returns a list of event files for the specified event.
     * <p>
     * This method should be used in the context of retrieving a list of slam down event files;
     * but it can be use for other events as well.
     * It returns only event files that are ready for processing.
     * </p>
     * @param telephoneNumber User phone number.
     * @param eventName Event name.
     * @return Returns an event file list.
     */
    public String[] getEventFiles(String telephoneNumber, String eventName) throws TrafficEventSenderException;

    /**
	 * Stores an event for SMS that was raised directly from VVA.
	 *
	 * @param telephoneNumber Phone number.
	 * @param event Event.
	 * @throws TrafficEventSenderException
	 */
    public void storeVvaSms(String telephoneNumber, TrafficEvent event) throws TrafficEventSenderException;

    /**
     * Stores an event for auto unlock pin event raised from VVA
     * 
     * @param telephoneNumber Phone number.
     * @param event Event.
     * @throws TrafficEventSenderException
     */
    public void storeAutoUnlockPinLockout(String telephoneNumber, TrafficEvent event) throws TrafficEventSenderException;
    
    /**
     * Returns if the phone number is internal or not
     * @param phoneNumber
     * @return if the phone number is internal or not
     */
    boolean isInternal(String phoneNumber);   
    
    /**
     * Returns a list of out-dial events for the specified number.
     * 
     * @param number Telephone number to which the events belong to.
     * @return Returns an array of event keys or null if none exists.
     */
    String[] getOutdialEvents(String number);
    
    /**
     * Stores properties in the given file
     *  
     * @param telephoneNumber Telephone number to which the event belongs to.
     * @param fileName Key representing the event.
     * @param properties file's properties.
     * @throws TrafficEventSenderException Thrown on error.
     */
    void storeProperties(String telephoneNumber, String fileName, Properties properties) throws TrafficEventSenderException;
    
    /**
     * Returns the properties found in a given file
     * 
     * @param telephoneNumber Telephone number to which the event belongs to.
     * @param fileName Key representing the event.
     * @return Returns the properties of the event. Returns null if no event exists.
     */
    Properties getProperties(String telephoneNumber, String fileName);

    String[] getSendStatusEventFiles(String telephoneNumber, String eventName, String order) throws TrafficEventSenderException;

    /**
     * Checks if MFS storage is available. The storage is unavailable during Geo-Redundancy failover.
     * @param originator The A number
     * @param recipient The B number
     * @return true if storage is possible in the Geo Redundant system
     */
    public boolean isStorageOperationsAvailable(String originator, String recipient);

    /**
     * Acquire a lock file (empty file) for a given telephone number.
     * 
     * @param telephoneNumber Subscriber or non-subscriber number
     * @param fileName Lock file name
     * @param validityPeriodInSeconds If 0, no validity period validation (file is always considered valid)
     * @param internal MIO Subscriber or non-MIO subscriber
     * @return long lockId which must be provided when unlocking the file, if lock file is not acquire, 0 is returned
     * @throws TrafficEventSenderException if either filePath creation fails or IOException
     *         Thrown to distinguish between a lock file not acquired and a faulty IO access. 
     */
    public long acquireLockFile(String telephoneNumber, String fileName, int validityPeriodInSeconds) throws TrafficEventSenderException;
    public long acquireLockFile(String telephoneNumber, String fileName, int validityPeriodInSeconds, boolean internal) throws TrafficEventSenderException;

    /**
     * Release a lock file (empty file) for a given telephone number.
     * 
     * @param telephoneNumber Subscriber or non-subscriber number
     * @param fileName Lock file name
     * @param lockId Provided when the lock was acquired.  A lock cannot be removed if the lockId does not match. 
     * @param internal MIO Subscriber or non-MIO subscriber
     * @throws TrafficEventSenderException if either filePath creation fails or IOException
     */
    public void releaseLockFile(String telephoneNumber, String fileName, long lockId) throws TrafficEventSenderException;
    public void releaseLockFile(String telephoneNumber, String fileName, long lockId, boolean internal) throws TrafficEventSenderException;

    
	public long getLastModified(File file) throws TrafficEventSenderException;
	public Reader retrieveEventsAsReader(File file) throws TrafficEventSenderException;
	public boolean renameFile(File orig, File dest) throws TrafficEventSenderException;
}
