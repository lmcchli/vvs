package com.mobeon.masp.mediahandler;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;

import junit.framework.TestCase;

public class MediaHandlerFactoryTest extends TestCase {

	MediaObjectFactory mediaObjectFactory;
	MediaHandlerFactory mediaHandlerFactory;
	
	public void setUp() throws MimeTypeParseException {
		Logger rootLog = Logger.getRootLogger();
		rootLog.setPriority(Priority.INFO);
		
		mediaObjectFactory = new MediaObjectFactory();
		mediaHandlerFactory = new MediaHandlerFactoryImpl();
	}

	
	public void testGetMediaHandler() throws Exception {
		
		MediaProperties mp;
		IMediaObject mo;
		MediaHandler mh;

		mp = new MediaProperties(new MimeType("audio/wav"));
		mo = mediaObjectFactory.create(mp);
		mh = mediaHandlerFactory.getMediaHandler(mo);
		assertTrue(mh instanceof WavMediaHandler);

		mp = new MediaProperties(new MimeType("audio/3gp"));
		mo = mediaObjectFactory.create(mp);
		mh = mediaHandlerFactory.getMediaHandler(mo);
		assertNull(mh);

		mp = new MediaProperties(new MimeType("video/wav"));
		mo = mediaObjectFactory.create(mp);
		mh = mediaHandlerFactory.getMediaHandler(mo);
		assertNull(mh);

		mh = mediaHandlerFactory.getMediaHandler((IMediaObject)null);
		assertNull(mh);

		
		mp = new MediaProperties();
		mo = mediaObjectFactory.create(mp);
		mh = mediaHandlerFactory.getMediaHandler(mo);
		assertNull(mh);
		
		mh = mediaHandlerFactory.getMediaHandler(new MimeType("audio/wav"));
		assertTrue(mh instanceof WavMediaHandler);

		mh = mediaHandlerFactory.getMediaHandler(new MimeType("audio/abc"));
		assertNull(mh);

		mh = mediaHandlerFactory.getMediaHandler((MimeType)null);
		assertNull(mh);
		
	}
	
	
}
