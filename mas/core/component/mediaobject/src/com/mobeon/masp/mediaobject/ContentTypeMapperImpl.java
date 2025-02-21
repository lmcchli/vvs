/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediaobject;

import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.*;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * Default implementation of the {@link ContentTypeMapper}
 * interface.
 *
 * @author Mats Egland
 */
public class ContentTypeMapperImpl implements ContentTypeMapper {
    /**
     * The {@link com.mobeon.common.logging.ILogger} logger used for logging purposes.
     */
    protected static final ILogger LOGGER =
            ILoggerFactory.getILogger(ContentTypeMapperImpl.class);

    public static String CONTENT_TYPE_MAPPER_TABLE = "ContentTypeMapper.Table";
    public static String CODEC_MIME_TYPES = "codecMimeTypes";
    public static String FILE_EXT_NAME = "fileExtName";

    /**
     * The configuration
     */
    private IConfiguration configuration;

    /**
     * Set the configuration.
     * @param configuration The <code>IConfiguration</code> to use.
     */
    public void setConfiguration(IConfiguration configuration) {
        this.configuration = configuration;
    }

    private Map<String, MimeType> fileExtToContentTypeMap;
    private Map<MimeType, String> contentTypeToFileExtMap;
    private Map<MediaMimeTypes, MimeType> codecToContentTypeMap;

    /**
     * Initialize the ContentTypeMapper. The configuration is read.
     *
     * @logs.info "Failed to get configuration group (group)"
     * - An expected group is missing in the configuration file.
     *
     * @logs.info "Failed to get attribute (attribute) in (group)"
     * - A mandatory attribute is missing in the group.
     *
     * @logs.info "Failed to create MimeType from string (string)"
     * - The mimetype specified in the config file is invalid.
     */
    public void init() {
        MimeType currentContentType = null;
        fileExtToContentTypeMap = new HashMap<String, MimeType>();
        contentTypeToFileExtMap = new HashMap<MimeType, String>();
        codecToContentTypeMap = new HashMap<MediaMimeTypes, MimeType>();

        IGroup masSpecificGroup = null;
		try {
			masSpecificGroup = configuration.getGroup(CommonOamManager.MAS_SPECIFIC_CONF);
		} catch (GroupCardinalityException e1) {
			if (LOGGER.isInfoEnabled()) LOGGER.info("Failed to get configuration group <contenttypemapper>", e1);
		} catch (UnknownGroupException e1) {
			if (LOGGER.isInfoEnabled()) LOGGER.info("Failed to get configuration group <contenttypemapper>", e1);
		}

		if(masSpecificGroup != null) {
			Map<String, Map<String, String>> contentTypeMapperTable = masSpecificGroup.getTable(CONTENT_TYPE_MAPPER_TABLE);

			Set<String> contentTypeMapperKeySet = contentTypeMapperTable.keySet();
			Iterator<String> contentTypeMapperIterator = contentTypeMapperKeySet.iterator();


			while(contentTypeMapperIterator.hasNext()){
				String contentTypeStr = contentTypeMapperIterator.next();
                try {
                    currentContentType = new MimeType(contentTypeStr);
                } catch (MimeTypeParseException e) {
                    if (LOGGER.isInfoEnabled())
                        LOGGER.info("Failed to create MimeType from string " + contentTypeStr, e);
                }

                if (currentContentType != null) {
                	//Special case for backward compatibility with pre amr-wb (5.1) config.
                	//HX87943 masSpecific.conf is not backward compatible
                	if (currentContentType.getBaseType().equalsIgnoreCase("audio/3gpp")) {
                		if (currentContentType.getParameter("codec") == null) {
                			/* if no codec is specified for 3gp then force it to amr nb.
                			 * technically this is not correct as can be also mp3 (not supported by stream yet)
                			 * or sawb (amr-wb) but this makes it compatible with old config where the codec was not
                			 * specified for 3gpp.
                			 * 
                			 * This is just to make existing old table work without needing to update
                			 * the config at upgrade to 5.1, for customers already using amr-nb as the code
                			 * now looks for the ;codec=samr
                			 */
                			
                			currentContentType.setParameter("codec", "samr");
                		}
                	}
        			Map<String, String> contentTypeMap = contentTypeMapperTable.get(contentTypeStr);

                    // codecs
                    MediaMimeTypes codecs = new MediaMimeTypes();

                    //Read codecs
                    String codecMimeType = contentTypeMap.get(CODEC_MIME_TYPES);

                    String[] codecList = codecMimeType.split(",");
                    for(String codecStr: codecList){
                    	try {
                    		codecs.addMimeType(new MimeType(codecStr));

                        } catch (MimeTypeParseException e) {
                                if (LOGGER.isInfoEnabled()) LOGGER.info("Failed to create MimeType from string " + codecStr, e);
                        }

                    }

                    // Put codec in map together with content type
                    codecToContentTypeMap.put(codecs, currentContentType);


                    //Read file extensions
                    String fileExtName = contentTypeMap.get(FILE_EXT_NAME);

                    // Put file extension in maps
                    fileExtToContentTypeMap.put(fileExtName, currentContentType);
                    contentTypeToFileExtMap.put(currentContentType, fileExtName);


                }
			}
		}
    }

    public MimeType mapToContentType(MediaMimeTypes codecs) {
        if (codecs == null) {
            throw new IllegalArgumentException("codecs is null");
        }

        Set<MediaMimeTypes> mappedCodecs = codecToContentTypeMap.keySet();
        for (MediaMimeTypes c : mappedCodecs) {
            if (c.compareTo(codecs)) {
                return codecToContentTypeMap.get(c);
            }
        }
        return null;
    }

    public String mapToFileExtension(MediaMimeTypes codecs) {
        if (codecs == null) {
            throw new IllegalArgumentException("codecs is null");
        }

        MimeType contentType = mapToContentType(codecs);
        if (contentTypeToFileExtMap.containsKey(contentType)) {
            return contentTypeToFileExtMap.get(contentType);
        } else {
            return null;
        }
    }

    public MimeType mapToContentType(String fileExtension) {
        if (fileExtension == null) {
            throw new IllegalArgumentException("fileExtension is null");
        } else if (fileExtension.trim().length() == 0) {
            throw new IllegalArgumentException("fileExtension is empty");
        }
        if (fileExtToContentTypeMap.containsKey(fileExtension)) {
            return fileExtToContentTypeMap.get(fileExtension);
        } else {
            return null;
        }
    }
}
