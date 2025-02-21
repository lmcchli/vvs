package com.mobeon.masp.mediahandler;

import jakarta.activation.MimeType;

import com.mobeon.masp.mediahandler.MediaHandler;
import com.mobeon.masp.mediahandler.MediaHandlerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;

public interface MediaHandlerFactory {
	
	/**
	 * Returns a media handler for the given media object.
	 * Currently only handles media objects with Content-Type="audio/wav"
	 * 
	 * @param mo MediaObject to get a handler for.
	 * @return a media handler or null if no matching media handler could be found. 
	 */
	public MediaHandler getMediaHandler(IMediaObject mo);

	/**
	 * Returns a media handler for the given mime-type.
	 * Currently the following mime-types are handled:
	 * 	- "audio/wav"
	 * 
	 * @param mimeType MimeType to get a handler for.
	 * @return a media handler or null if no matching media handler could be found. 
	 */
	public MediaHandler getMediaHandler(MimeType mimeType);
}
