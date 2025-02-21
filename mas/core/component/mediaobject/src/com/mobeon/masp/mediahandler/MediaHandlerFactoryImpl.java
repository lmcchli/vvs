package com.mobeon.masp.mediahandler;

import jakarta.activation.MimeType;

import com.mobeon.masp.mediahandler.MediaHandler;
import com.mobeon.masp.mediahandler.MediaHandlerFactory;
import com.mobeon.masp.mediahandler.WavMediaHandler;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;

public class MediaHandlerFactoryImpl implements MediaHandlerFactory {

	private static ILogger log = ILoggerFactory.getILogger(MediaHandlerFactoryImpl.class);

	final IMediaObjectFactory mediaObjectFactory = 
		new MediaObjectFactory();
	
	public MediaHandlerFactoryImpl() {
	}

	/**
	 * Returns a media handler for the given media object.
	 * Currently only handles media objects with Content-Type="audio/wav"
	 * 
	 * @param mo MediaObject to get a handler for.
	 * @return a media handler or null if no matching media handler could be found. 
	 */
	public MediaHandler getMediaHandler(IMediaObject mo) {
		if (mo != null) {
			MimeType mimeType = mo.getMediaProperties().getContentType();
			return getMediaHandler(mimeType);
		} else {
			log.warn("getMediahandler(): MediaObject may be null");
			return null;
		}
	}

	/**
	 * Returns a media handler for the given mime-type.
	 * Currently the following mime-types are handled:
	 * 	- "audio/wav"
	 *  - "audio.3gpp
	 * 
	 * @param mimeType MimeType to get a handler for.
	 * @return a media handler or null if no matching media handler could be found. 
	 * #FIXME AMR-WB?
	 */
	public MediaHandler getMediaHandler(MimeType mimeType) {
		
		MediaHandler mediaHandler = null;
		
		if (mimeType != null && "audio/wav".equals(mimeType.getBaseType())) {
			mediaHandler = new WavMediaHandler(mediaObjectFactory, mimeType);
		} else if (mimeType != null && "audio/3gpp".equals(mimeType.getBaseType())) {
			mediaHandler = new AmrMediaHandler(mediaObjectFactory, mimeType);
		} else {
			if (log.isInfoEnabled())
				log.info("getMediahandler(): Cannot find media handler for " + 
						mimeType);
		}
		
		return mediaHandler;
	}

}
