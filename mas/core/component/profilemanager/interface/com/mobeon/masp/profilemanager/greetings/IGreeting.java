package com.mobeon.masp.profilemanager.greetings;

import com.mobeon.masp.profilemanager.mediafile.IMediaFile;

/**
 * <p>
 * IGreeting provides an interface to manipulate greeting messages at a high level.
 * </p>
 * <p>
 * A greeting is the composition of a media stream and properties related to the greeting.
 * </p>
 * <p>
 * A IGreeting implementation has to manage the storage and retrieval of a media stream and its associated
 * set of properties.
 * </p>
 * @author egeobli
 *
 */
public interface IGreeting extends IMediaFile {
	
	
}
