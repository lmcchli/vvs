package com.mobeon.masp.profilemanager.greetings;

import java.io.IOException;

import com.mobeon.masp.profilemanager.mediafile.AbstractMediaFile;


/**
 * The Greeting class manages greeting information on disk.
 * <p>
 * Greetings are managed using two files: a media file that contains the sound or video data for a greeting
 * and a property file that contains additional information. When  a Greeting object is created it, loads
 * the property file and the media file into memory if they exists. When a greeting is created with
 * non-existing media and property files, it has no media stream and no property. Its setter methods can
 * be used to fill the greeting. When it is ready, it can be stored on disk using the {@link #store()}
 * method.
 * </p>
 * @author egeobli
 *
 */
class Greeting extends AbstractMediaFile implements IGreeting {
	static final String DEFAULT_GREETING_FNAME = "greeting";

	/**
	 * Creates a new Greeting object using file and a media extension.
	 * @param pathName Path name of the greeting with no extension.
	 * @throws IOException If File IO errors.
	 */
	Greeting(String pathName) throws Exception {
		super(pathName);
		if (pathName == null){
			setName(DEFAULT_GREETING_FNAME);
		}
	}

	public Greeting(String pathName, String extension) throws Exception {
		super(pathName, extension);
	}


}
