/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import com.mobeon.common.configuration.*;
import com.mobeon.common.eventnotifier.IEventDispatcher;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.ContentTypeMapper;
import com.mobeon.masp.mediatranslationmanager.MediaTranslationManager;

import javax.naming.ConfigurationException;

/**
 * Factory for creation of Media Streams. Note that no connections are
 * established when the stream objects are created. Use the streams
 * <code>create</code>-method to establish a connection.
 * 
 * @author Jörgen Terner
 */
public final class StreamFactoryImpl implements IStreamFactory {
    private static final ILogger LOGGER =
        ILoggerFactory.getILogger(StreamFactoryImpl.class);
    
    /**
     * Used to map MIME-types to content type and file extension.
     * Needed in all inbound streams.
     */
    private ContentTypeMapper mContentTypeMapper;
    
    /** Used to convert media objects. */
    private MediaTranslationManager mMTM;
    
    
    /** <code>true</code> if method {@link #init} has been called. */
    private boolean mIsInitiated;

    /**
     * The default session factory.
     */
    RTPSessionFactory sessionFactory = new CCRTPSessionFactory();
    
    /** 
     * Protects the initiated-flag. Synchronizing with this lock 
     * makes sure that if a stream is requested during the initiation
     * process, it is made to wait instead of receiving an exception.
     */
    private final Object LOCK = new Object();
    
    /* Javadoc in interface. */
    public IOutboundMediaStream getOutboundMediaStream() {
        synchronized(LOCK) {
            if (!mIsInitiated) {
                throw new IllegalStateException("Method init must be called " +
                        "before getOutboundMediaStream()");
            }
        }
        OutboundMediaStreamImpl stream =
                    new OutboundMediaStreamImpl(sessionFactory.createOutboundRTPSession());
        // XXX this test should not be needed when MTM is fully implemented
        if (mMTM != null) {
            stream.setMTM(mMTM);
        }
        else {
            LOGGER.debug("No media translation manager set in stream " +
                    " factory, no conversions can be made.");
        }
        return stream;
    }

    /* Javadoc in interface. */
    public IInboundMediaStream getInboundMediaStream() {
        synchronized(LOCK) {
            if (!mIsInitiated) {
                throw new IllegalStateException("Method init must be called " +
                        "before getInboundMediaStream()");
            }
        }
        InboundMediaStreamImpl stream =
                new InboundMediaStreamImpl(sessionFactory.createInboundRTPSession());
        stream.setContentTypeMapper(mContentTypeMapper);
        return stream;
    }
    
    /**
     * Sets the mapper used to map MIME-types to content type and file 
     * extension. Needed in all inbound streams.
     * 
     * @param mapper May not be <code>null</code>.
     * 
     * @throws IllegalArgumentException If <code>mapper</code> is 
     *         <code>null</code>.
     */
    public void setContentTypeMapper(ContentTypeMapper mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException(
                    "Parameter mapper may not be null");
        }
        mContentTypeMapper = mapper;
    }
    
    /**
     * Sets the current configuration instance.
     * 
     * @param config Current configuration instance. 
     *               May not be <code>null</code>.
     * 
     * @throws IllegalArgumentException  If <code>config</code> is 
     *         <code>null</code>.
     * @throws UnknownGroupException             If the group was not found.
     * @throws MissingConfigurationFileException
     * @throws ConfigurationLoadException
     * @throws ParameterTypeException
     */
    public void setConfiguration(IConfiguration config) 
        throws GroupCardinalityException, UnknownGroupException, 
               ParameterTypeException, MissingConfigurationFileException,
               ConfigurationLoadException, ConfigurationException {

        StreamConfiguration.getInstance().setInitialConfiguration(config);
        StreamConfiguration.getInstance().update();
    }    
    
    /**
     * Sets the event dispatcher that should be used to receive events
     * from other components.
     * 
     * @param eventDispatcher The event dispatcher. May not be 
     *                        <code>null</code>.
     *                        
     * @throws IllegalArgumentException If <code>eventDispatcher</code>
     *         is <code>null</code>.
     */
     public void setEventDispatcher(IEventDispatcher eventDispatcher) {
        if (eventDispatcher == null) {
            throw new IllegalArgumentException(
                    "The event dispatcher may not be null!");
        }
        eventDispatcher.addEventReceiver(
                MediaStreamSupport.getEventReceiver());
    }
    
     /**
      * Sets the MTM that shall be used for media conversions.
      * 
      * @param mtm MTM, may not be <code>null</code>.
      */
     public void setMediaTranslationManager(MediaTranslationManager mtm) {
         if (mtm == null) {
             throw new IllegalArgumentException(
                     "The MTM may not be null!");
         }
         mMTM = mtm;
     }

     /* Javadoc in interface. */
    public void init() {
        synchronized(LOCK) {
            MediaStreamSupport.init();
            // Initializing the the FreePortHandler.
            StreamConfiguration configuration = StreamConfiguration.getInstance();
            FreePortHandler.getInstance().setBase(configuration.getPortPoolBase());
            FreePortHandler.getInstance().setSize(configuration.getPortPoolSize());
            FreePortHandler.getInstance().initilialize();
            sessionFactory.init();
            mIsInitiated = true;
        }
    }

    /**
     * Setter for the RTP Session Factory.
     * This replaces the default RTP session Factory.
     * @param sessionFactory an RTP Session Factory.
     */
    void setSessionFactory(RTPSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}
