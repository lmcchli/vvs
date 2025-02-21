/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.mediaobject.ContentTypeMapper;
import com.mobeon.masp.mediacontentmanager.*;
import com.mobeon.common.configuration.*;

import java.util.List;
import java.util.ArrayList;
import java.util.MissingResourceException;
import java.util.LinkedList;
import java.net.URI;


/**
 * The MediaContentManagerMock object
 */
public class MediaContentManagerMock extends BaseMock implements IMediaContentManager, MediaResourceFilterer {

    private static final String PACKAGE_FILE = "MediaContentPackage.xml";

    private static final String RESOURCE_PATH_PARAM = "resourcepath";
    /**
     * The factory used to create media objects
     */
    private IMediaObjectFactory mediaObjectFactory;
    /**
     * The filter used when fetching IMediaContentResource's with the
     * getMediaContentResource method.
     *
     * @see {@link com.mobeon.masp.mediacontentmanager.MediaResourceFilter)
     */
    private MediaResourceFilter mediaResourceFilter;
    /**
     * The list of {@link com.mobeon.masp.mediacontentmanager.IMediaContentResource} that is loaded from
     * file system during creation.
     */
    private List<IMediaContentResource> mediaContentResourceList =
            new ArrayList<IMediaContentResource>();

    /**
     * The path to the package repository directory. This is the
     * root directory of Media Content Packages that are read during startup.
     * Each package has a directory under the root directory named after
     * the id of the package.
     */
    protected String resourcePath;

    /**
     * Used to map codecs to content-types and content-types
     * to file-extensions.
     */
    private ContentTypeMapper contentTypeMapper;

    /**
     * The media object cache  used by this manager to cache
     * created media objects.
     */
    private MediaObjectCache mediaObjectCache;

    /**
     * The config.
     */
    private IConfiguration configuration;

    /**
     * Empty constructor that creates a MediaContentManager.
     */
    public MediaContentManagerMock() {
        super ();
        log.info ("MOCK: MediaContentManagerMock.MediaContentManagerMock: Created");
        if (mediaResourceFilter == null) {
            this.mediaResourceFilter = new MediaResourceFilter();
        }
    }


    /**
     * Initializes the Media Content Manager object. Loads the Media Content Packages
     * from disk.
     */
    private void init() throws ConfigurationLoadException,
            MissingConfigurationFileException,
            UnknownGroupException,
            GroupCardinalityException,
            UnknownParameterException {
        log.info ("MOCK: MediaContentManagerMock.init: Not implemented!");
    }

    /**
     * This implementation will throw a MissingResourceException if the required
     * MediaResourceFilter is missing.
     * <p/>
     * For javadoc in interface see {@link IMediaContentManager#getMediaContentResource(MediaContentResourceProperties)}}.
     * <p/>
     * This method is not synchronized as the list of resources is initilized at creation
     * only (the init method.)
     *
     * @throws java.util.MissingResourceException If the MediaContentManager has no MediaResourceFilter set.
     */
    public List<IMediaContentResource> getMediaContentResource(
            MediaContentResourceProperties mediaContentResourceProperties) {

        log.info ("MOCK: MediaContentManagerMock.getMediaContentResource");
        URI uri = URI.create("");
        MediaContentResourceMock mcr = new MediaContentResourceMock(uri);
        List<IMediaContentResource> lst = new LinkedList<IMediaContentResource>();
        lst.add(mcr);
        return lst;
    }

    /**
     * Injects the required <code>IMediaObjectFactory</code>.
     *
     * @param mediaObjectFactory
     */
    public void setMediaObjectFactory(IMediaObjectFactory mediaObjectFactory) {
        this.mediaObjectFactory = mediaObjectFactory;
    }

    /**
     * Injects the required <code>MediaResourceFilter</code>, used to
     * select <code>MediaContentResource</code>s.
     * <p/>
     * The filter is used by the <tag>getMediaContentResource</tag> method.
     *
     * @param filter The filter to select matching resources.
     */
    public void setMediaResourceFilter(MediaResourceFilter filter) {
        this.mediaResourceFilter = filter;
    }

    /**
     * Sets the {@link ContentTypeMapper} that this class
     * uses to map codecs to content-types, and content-types
     * to file-extensions.
     *
     * @param contentTypeMapper The content-type mapper.
     */
    public void setContentTypeMapper(ContentTypeMapper contentTypeMapper) {
        this.contentTypeMapper = contentTypeMapper;
    }


    /**
     * Sets the {@link MediaObjectCache} used to cache created
     * media objects.
     *
     * @param cache The media object cache used by this manager.
     */
    public void setMediaObjectCache(MediaObjectCache cache) {
        this.mediaObjectCache = cache;
    }

    /**
     * Sets the current configuration instance.
     *
     * @param config Current configuration instance.
     *               May not be <code>null</code>.
     *
     * @throws IllegalArgumentException  If <code>config</code> is
     *         <code>null</code>.
     * @throws com.mobeon.common.configuration.UnknownGroupException             If the group was not found.
     * @throws com.mobeon.common.configuration.MissingConfigurationFileException
     * @throws com.mobeon.common.configuration.ConfigurationLoadException
     * @throws com.mobeon.common.configuration.ParameterTypeException
     */
    public void setConfiguration(IConfiguration config)
            throws GroupCardinalityException, UnknownGroupException,
            ParameterTypeException, MissingConfigurationFileException,
            ConfigurationLoadException, UnknownParameterException {

        this.configuration = config;
    }

    /**
     * Verifies that all resources needed by the content manager
     * is injected.
     *
     * @throws MissingResourceException If a resource is missing.
     */
    private void verifyResources() {
        if (contentTypeMapper == null) {
            throw new MissingResourceException("ContentTypeMapper is null. Must be injected.",
                    "ContentTypeMapper", "ContentTypeMapper");
        } else if (mediaResourceFilter == null) {
            throw new MissingResourceException("MediaResourceFilter is null. Must be injected.",
                    "MediaResourceFilter", "MediaResourceFilter");
        } else if (mediaResourceFilter == null) {
            throw new MissingResourceException("IMediaObjectFactory is null. Must be injected.",
                    "IMediaObjectFactory", "IMediaObjectFactory");
        } else if (mediaObjectCache == null) {
            throw new MissingResourceException("MediaObjectCache is null. Must be injected.",
                    "MediaObjectCache", "MediaObjecdtCache");
        } else if (configuration == null) {
            throw new MissingResourceException("Configuration is null. Must be injected.",
                    "Configuration", "Configuration");
        }

    }

}
