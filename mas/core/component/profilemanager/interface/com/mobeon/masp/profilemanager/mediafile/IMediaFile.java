package com.mobeon.masp.profilemanager.mediafile;

import java.io.IOException;
import java.io.InputStream;

public interface IMediaFile {
	
	/**
	 * Returns a property value.
	 * @param key Property name.
	 * @return Property value. Returns null if the property does not exists.
	 */
	public String getProperty(String key);
	
	/**
	 * Sets a property value.
	 * @param key Property name.
	 * @param value Property value.
	 */
	public void setProperty(String key, String value);
	
	/**
	 * Returns an input binary stream to the media.
	 * @return Binary stream. Returns null if the the file has no media.
	 */
	public InputStream getMedia();
	
	/**
	 * Sets the input stream of this file.
	 * @param media Media input stream.
	 */
	public void setMedia(InputStream media);
	
	/**
	 * Stores the file. This method can be used when the file already has an associated
	 * name. If no name is associated, a default one should be created.
	 *
	 * @throws Exception if the file cannot be stored.
	 */
	public void store() throws Exception;
	
	/**
	 * Sets the file name
	 * @param aName
	 */
	public void setName(String aName);
	/**
	 * Returns the file name.
	 * 
	 * @return Greeting name.
	 */
	public String getName();
	
	/**
	 * @return true If the file has already been created (saved). 
	 */
	public boolean exists();
	
	/**
	 * Deletes the file from the system.
	 */
	public void delete();
}
