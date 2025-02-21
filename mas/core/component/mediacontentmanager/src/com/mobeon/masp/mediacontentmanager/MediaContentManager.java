/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediacontentmanager.xml.MediaContentResourceMapper;
import com.mobeon.masp.mediacontentmanager.xml.SaxMapperException;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.mediaobject.ContentTypeMapper;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.*;

import jakarta.activation.MimeType;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.MissingResourceException;
import java.net.MalformedURLException;

/**
 * Implementation of the {@link IMediaContentManager} interface.
 * <p/>
 * The class implements the {@link MediaResourceFilterer} interface to retreive a
 * {@link MediaResourceFilter} with the setMediaResourceFilter method.
 * <p/>
 * This class uses the following resources that should be injected.
 * <ul>
 * <li>
 *  Media Object Factory:   A factory of type <code>IMediaObjectFactory</code> used to
 *                          create <code>IMediaObject</code>s.
 * </li>
 * <li>
 *  Resource Path:          Path to the language resource repository, i.e.
 *                          the path to the root directory on the
 *                          file system where the packages resides.
 * </li>
 *  Media Object Cache:     (Optional) If injected, used to cache created
 *                          MediaObjects.
 * </ul>
 *
 * T
 */
public class MediaContentManager implements IMediaContentManager, MediaResourceFilterer {
    /**
     * The {@link ILogger} logger used for logging purposes.
     */
    protected static final ILogger LOGGER = ILoggerFactory.getILogger(MediaContentManager.class);

    private static final String PACKAGE_FILE = "MediaContentPackage.xml";

    public static final String RESOURCE_PATH_PARAM = "mediaContentManagerResourcePath";

    public static final String CACHE_POLICY = "mediaContentManagerCachePolicy";
    public static final String CACHE_MAX_SIZE = "mediaContentCacheMaxSize";
    public static final String CACHE_ELEMENT_TIMEOUT = "mediaContentCacheElementTimeout";
    public static final String CACHE_MEMORY_SENSITIVE = "mediaContentCacheMemorySensitive";



    /**
     * The factory used to create media objects
     */
    private IMediaObjectFactory mediaObjectFactory;
    /**
     * The filter used when fetching IMediaContentResource's with the
     * getMediaContentResource method.
     *
     * @see {@link MediaResourceFilter)
     */
    private MediaResourceFilter mediaResourceFilter;
    /**
     * The list of {@link IMediaContentResource} that is loaded from
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
    public MediaContentManager() {
        if (mediaResourceFilter == null) {
            this.mediaResourceFilter = new MediaResourceFilter();
        }
    }


    /**
     * Initializes the Media Content Manager object. Loads the Media Content Packages
     * from disk.
     *
     * @logs.error "The package directory does not exist or is not a directory. Will not read any media packages."
     * - The configured resource path is invalid, check the configuration file.
     *
     * @logs.error "Failed to load media package from file: (filepath). Could not create URL to it. "
     * - The given file path is invalid.
     *
     * @logs.error "Failed to load content resource package from file: (resourcefile). The resource with root directory=(path) will not be created."
     * - An error occurred while parsing the xml files in the resource package.
     *
     * @logs.warning "The Media Content Package Directory (path) does not contain a toplevel definition file named MediaContentPackage.xml"
     * - The definition file must use this name.
     */
    private void init() throws ConfigurationLoadException,
            MissingConfigurationFileException,
            UnknownGroupException,
            GroupCardinalityException,
            UnknownParameterException {

        // Check that all resources is injected
        verifyResources();

//        // Create media object cache if configured.
        boolean createCache = false;
        MediaObjectCacheImpl.POLICY policy = null;
        int maxSize = 0;
        long elementTimeout = 0;
        boolean memorySensitive = false;

        try {

        	String policyStr = configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getString(CACHE_POLICY).toUpperCase();
            if (policyStr.equals("LFU")) {
                policy = MediaObjectCacheImpl.POLICY.LFU;
            } else if (policyStr.equals("LRU")) {
                policy = MediaObjectCacheImpl.POLICY.LRU;
            } else if (policyStr.equals("FIFO")) {
                policy = MediaObjectCacheImpl.POLICY.FIFO;
            }

            maxSize = configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getInteger(CACHE_MAX_SIZE);

            elementTimeout = configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getInteger(CACHE_ELEMENT_TIMEOUT);

            memorySensitive = configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getBoolean(CACHE_MEMORY_SENSITIVE);

            createCache = true;

        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Failed to get configuration for MediaObjectCache, will not create cache."
                        + e.getMessage());
            }
            createCache = false;
        }

        if (createCache) {
            if (LOGGER.isInfoEnabled())
                LOGGER.info("Creating MediaObjectCache.");
            try {
                mediaObjectCache = new MediaObjectCacheImpl(policy, maxSize, elementTimeout, memorySensitive);
            } catch (IllegalArgumentException e) {
                mediaObjectCache = null;
            }
        } else {
            if (LOGGER.isInfoEnabled())
                LOGGER.info("No MediaObjectCache is created.");
        }

        resourcePath = configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getString(RESOURCE_PATH_PARAM);

        MediaContentResourceMapper saxResourceMapper = new MediaContentResourceMapper();
        File rootResourceDir = new File(resourcePath);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Loading media content packages from root directory:" + resourcePath);
        }
        if (!rootResourceDir.isDirectory()) {
            LOGGER.error("The package directory does not exist or is not a directory. Will not read any media packages.");
            return;
        }

        File[] packageDirectories = rootResourceDir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                return file.isDirectory();
            }
        });
        for (File packageDirectory : packageDirectories) {
            File packageDefinitionFile = new File(packageDirectory, PACKAGE_FILE);
            if (packageDefinitionFile.exists()) {
                try {
                    MediaContentResource resource =
                            saxResourceMapper.fromXML(packageDefinitionFile.toURL());

                    // set the MediaObjectBuilder on the resource, so it can
                    // build IMediaObjects when requested.
                    MediaObjectBuilder builder = new MediaObjectBuilder(
                            resource, mediaObjectFactory,
                            mediaObjectCache, contentTypeMapper);
                    resource.setMediaObjectBuilder(builder);
                    mediaContentResourceList.add(resource);

                    // get info for logging.
                    MediaContentResourceProperties properties = resource.getMediaContentResourceProperties();
                    List<MimeType> codecList = properties.getMediaCodecs();
                    String codecs = "";
                    for (MimeType c : codecList) {
                        codecs += c.toString() + " ";
                    }
                    String variant = properties.getVoiceVariant();
                    if (variant == null) {
                        variant = properties.getVideoVariant();
                    }
                    String resourceInfo = " Id: " + resource.getID()
                            + ", Language: " + properties.getLanguage()
                            + ", Type: " + properties.getType()
                            + ", Priority: " + resource.getPriority()
                            + ", Variant: " + variant
                            + ", Codecs: " + codecs;

                    if (LOGGER.isInfoEnabled())
                        LOGGER.info("Media Content Package added successfully." + resourceInfo);
                } catch (MalformedURLException e) {
                    LOGGER.error("Failed to load media package from file:"
                            + packageDefinitionFile.getAbsolutePath()
                            + ". Could not create URL to it. ");
                    return;
                } catch (SaxMapperException e) {
                    LOGGER.error(
                            "Failed to load content resource package from file:"
                                    + packageDefinitionFile.getAbsolutePath()
                                    + ". The resource with root directory="
                                    + packageDirectory.getAbsolutePath()
                                    + " will not be created.", e);
                    return;
                }
            } else {
                LOGGER.warn("The Media Content Package Directory " + packageDirectory.getName() +
                        " does not contain a toplevel definition file named " +
                        PACKAGE_FILE);
            }
        }
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
     * @throws MissingResourceException If the MediaContentManager has no MediaResourceFilter set.
     */
    public List<IMediaContentResource> getMediaContentResource(
            MediaContentResourceProperties mediaContentResourceProperties) {

        if (mediaContentResourceProperties == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("getMediaContentResource: No properties given. Will return all resources.");
            }
            return new ArrayList<IMediaContentResource>(mediaContentResourceList);
        } else if (mediaResourceFilter == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("getMediaContentResource: MediaContentManager has no " +
                        "MediaResourceFilter will throw MissingResourceException");
            }
            throw new MissingResourceException("MediaContentManager has no MediaResourceFilter!",
                    getClass().getName(), "mediaResourceFilter");
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getMediaContentResource: Passed properties="
                    + mediaContentResourceProperties.toString());
        }
        List<IMediaContentResource> matchingResources = new ArrayList<IMediaContentResource>();
        for (IMediaContentResource mediaContentResource : mediaContentResourceList) {
            if (mediaResourceFilter.filter(mediaContentResourceProperties, mediaContentResource)) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("getMediaContentResource: The resource with id " + mediaContentResource.getID()
                            + " matched the given properties");
                }
                matchingResources.add(mediaContentResource);
            }
        }
        Collections.sort(matchingResources);
        return matchingResources;
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


//    /**
//     * Sets the {@link MediaObjectCache} used to cache created
//     * media objects.
//     *
//     * @param cache The media object cache used by this manager.
//     */
//    public void setMediaObjectCache(MediaObjectCache cache) {
//        this.mediaObjectCache = cache;
//    }

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
        } else if (configuration == null) {
            throw new MissingResourceException("Configuration is null. Must be injected.",
                    "Configuration", "Configuration");
        } else if (mediaObjectFactory == null) {
            throw new MissingResourceException("MediaObjectFactory is null. Must be injected.",
                    "MediaObjectFactory", "MediaObjectFactory");
        }

    }

}
