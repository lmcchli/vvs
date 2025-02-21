/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.xml;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediacontentmanager.IMediaContentResource;
import com.mobeon.masp.mediacontentmanager.MediaContent;
import com.mobeon.masp.mediacontentmanager.MediaContentResource;
import com.mobeon.masp.mediacontentmanager.MediaObjectSource;
import com.mobeon.masp.mediacontentmanager.grammar.RulesRecord;
import org.xml.sax.Attributes;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * This class is a {@link SaxMapper} that maps a XML
 * to a {@link com.mobeon.masp.mediacontentmanager.MediaContentResource}.
 * <p/>
 * The root direcotry of the package must be set before parsing. This is done
 * with the setPackageDirectory method.
 */
public class MediaContentResourceMapper extends SaxMapper<MediaContentResource> {
    /**
     * The mapped object. Yes this is the implementation {@link MediaContentResource}
     * instead of the public interface {@link IMediaContentResource} as some internal
     * methods are used.
     */
    private MediaContentResource mediaContentResource;
    /**
     * Logger used.
     */
    private static final ILogger LOGGER =
            ILoggerFactory.getILogger(MediaContentResourceMapper.class);

    /**
     * Creates a <code>MediaContentResourceMapper</code>.
     */
    public MediaContentResourceMapper() {
    }

    /**
     * Returns the from XML mapped MediaContentResource object.
     *
     * @return The IMediaContentResource.
     */
    public MediaContentResource getMappedObject() {
        return mediaContentResource;
    }

    /**
     * Create TagTracker for the contents in the resource.
     *
     * @logs.warning "Failed to create URL to ContentFile, referenced from package content file:(file reference). The MediaContentResource with id: (resrource id) is broken"
     * - An invalid media content file is specified.
     *
     * @logs.warning "Failed to create URL to MediaObject File, referenced from package content file:(file reference). The MediaContentResource with id: (resrource id) is broken"
     * - An invalid media object file is specified.
     *
     * @logs.warning "Failed to create URL to Grammar File, referenced from package content file:(file reference). The MediaContentResource with id: (resrource id) is broken"
     * - An invalid grammar file is specified.
     *
     * @return The tracker
     */
    public TagTracker createTagTrackerNetwork() {
        TagTracker root = new TagTracker() {
            public void onDeactivate() {
                // The root will be deactivated when
                // parsing a new docuemt begins
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Parsing new document"
                            + parsedXMLUrl.toString()
                            + ", creating new MediaContentResource!!");
                }
                try {
                    mediaContentResource = new MediaContentResource(
                            parsedXMLUrl.toURI());
                } catch (URISyntaxException e) {
                    throw new SaxMapperException("Mapping of resource from package file:"
                            + parsedXMLUrl + ", failed "
                            +"as the URL " + parsedXMLUrl
                            +" could not be converted to a URI", e);

                }

            }
        };

        // -- create action for /mediacontentpackage
        TagTracker mediaContentPackageTracker = new TagTracker() {
            public void onStart(String namespaceURI,
                                String localname,
                                String qName,
                                Attributes attr) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("<" + localname + ">");
                }

                String id = attr.getValue("id");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\tid=" + id);
                }
                mediaContentResource.setID(id);
                String priorityString = attr.getValue("priority");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\tpriority=" + priorityString);
                }
                mediaContentResource.setPriority(Integer.parseInt(priorityString));

                String language = attr.getValue("language");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\tlanguage=" + language);
                }
                mediaContentResource.getMediaContentResourceProperties().
                        setLanguage(language);
                String type = attr.getValue("type");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\ttype=" + type);
                }
                mediaContentResource.getMediaContentResourceProperties().
                        setType(type);

            }

        };
        root.track("mediacontentpackage", mediaContentPackageTracker);

        // -- create action for /mediacontentpackage/voice
        TagTracker voiceTracker = new TagTracker() {
            public void onStart(String namespaceURI,
                                String localname,
                                String qName,
                                Attributes attr) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("<" + localname + ">");
                }
                String variant = attr.getValue("variant");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\tvariant=" + variant);
                }
                mediaContentResource.getMediaContentResourceProperties().
                        setVoiceVariant(variant);
            }

        };
        mediaContentPackageTracker.track("voice", voiceTracker);

        // -- create action for /mediacontentpackage/video
        TagTracker videoTracker = new TagTracker() {
            public void onStart(String namespaceURI,
                                String localname,
                                String qName,
                                Attributes attr) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("found localname" + localname);
                }
                String variant = attr.getValue("variant");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\tvariant=" + variant);
                }
                mediaContentResource.getMediaContentResourceProperties().
                        setVideoVariant(variant);
            }

        };
        mediaContentPackageTracker.track("video", videoTracker);

        // -- create action for /mediacontentpackage/codecs/codec
        TagTracker codecTracker = new TagTracker() {
            public void onStart(String namespaceURI,
                                String localname,
                                String qName,
                                Attributes attr) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("<" + localname + ">");
                }
                String mimeTypeString = attr.getValue("mimetype");

                try {
                    MimeType codecMimeType = new MimeType(mimeTypeString.toLowerCase());
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("\tMimeType:" + codecMimeType.getBaseType());
                    }
                    mediaContentResource.getMediaContentResourceProperties().
                        addCodec(codecMimeType);
                } catch (MimeTypeParseException e) {
                    throw new SaxMapperException("Mapping of resource from package file:"
                            + parsedXMLUrl + ", failed "
                            +"as the mimetype " + mimeTypeString
                            +" could not be parsed into a MimeType", e);
                } catch (NullPointerException e) {
                    throw new SaxMapperException("Mapping of resource from package file:"
                            + parsedXMLUrl + ", failed as a NullPointerException was thrown."
                            , e);
                }

            }

        };
        mediaContentPackageTracker.track("codecs/codec", codecTracker);

        // -- create action for /mediacontentpackage/contentfile
        TagTracker contentFileTracker = new TagTracker() {
            public void onStart(String namespaceURI,
                                String localname,
                                String qName,
                                Attributes attr) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("found localname" + localname);
                }
                String path = attr.getValue("path");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\tpath=" + path);
                }

                URL mediaContentUrl;
                try {
                    mediaContentUrl = new URL(parsedXMLUrl, path);

                } catch (MalformedURLException e) {
                    LOGGER.warn("Failed to create URL to ContentFile, referenced from " +
                            "package content file:" + parsedXMLUrl.toString() +
                            ". The MediaContentResource with id:" + mediaContentResource.getID() +
                            " is broken");
                    throw new SaxMapperException(
                            "Failed to create URL to ContentFile, referenced from " +
                            "package content file:" + parsedXMLUrl.toString() +
                            ". The MediaContentResource with id:" + mediaContentResource.getID() +
                            " is not created.", e);
                }
                // Create a MediaContentMapper and retrieve list
                // of mapped <code>MediaContent</code>s.
                MediaContentMapper mediaContentMapper =
                        new MediaContentMapper();
                List<MediaContent> mediaContentList =
                        mediaContentMapper.fromXML(mediaContentUrl);
                for (MediaContent mediaContent : mediaContentList) {
                    mediaContentResource.addMediaContent(mediaContent);
                }

            }

        };
        mediaContentPackageTracker.track("contentfile", contentFileTracker);

        // -- create action for /mediacontentpackage/objectfile
        TagTracker objectFileTracker = new TagTracker() {
            public void onStart(String namespaceURI,
                                String localname,
                                String qName,
                                Attributes attr) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("found localname" + localname);
                }
                String path = attr.getValue("path");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\tpath=" + path);
                }

                URL mediaObjectUrl;
                try {
                    mediaObjectUrl = new URL(parsedXMLUrl, path);

                } catch (MalformedURLException e) {
                    LOGGER.warn("Failed to create URL to MediaObject File, referenced from " +
                            "package content file:" + parsedXMLUrl.toString() +
                            ". The MediaContentResource with id:" + mediaContentResource.getID() +
                            " is broken");
                    throw new SaxMapperException(
                            "Failed to create URL to MediaObject file, referenced from " +
                            "package content file:" + parsedXMLUrl.toString() +
                            ". The MediaContentResource with id:" + mediaContentResource.getID() +
                            " is not created.", e);
                }
                // Create a MediaObjectSourceMapper and retrieve list
                // of mapped <code>MediaObjectSource</code>s.
                MediaObjectSourceMapper mediaObjectSourceMapper =
                        new MediaObjectSourceMapper();
                List<MediaObjectSource> mediaObjectSourceList =
                        mediaObjectSourceMapper.fromXML(mediaObjectUrl);
                for (MediaObjectSource mediaSource : mediaObjectSourceList) {
                    mediaContentResource.addMediaObjectSource(mediaSource);
                }
            }

        };
        mediaContentPackageTracker.track("objectfile", objectFileTracker);

        // -- create action for /mediacontentpackage/grammarfile
        TagTracker grammarFileTracker = new TagTracker() {
            public void onStart(String namespaceURI,
                                String localname,
                                String qName,
                                Attributes attr) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("found localname" + localname);
                }
                String path = attr.getValue("path");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\tpath=" + path);
                }

                URL grammarUrl;
                try {
                    grammarUrl = new URL(parsedXMLUrl, path);

                } catch (MalformedURLException e) {
                    LOGGER.warn("Failed to create URL to Grammar File, referenced from " +
                            "package content file:" + parsedXMLUrl.toString() +
                            ". The MediaContentResource with id:" + mediaContentResource.getID() +
                            " is broken");
                    throw new SaxMapperException(
                            "Failed to create URL to Grammar file, referenced from " +
                            "package content file:" + parsedXMLUrl.toString() +
                            ". The MediaContentResource with id:" + mediaContentResource.getID() +
                            " is not created.", e);
                }
                // Create a GrammarMapper and retrieve list
                // of mapped <code>RulesRecord</code>s.
                GrammarMapper grammarMapper = new GrammarMapper();
                List<RulesRecord> rulesRecordList =
                        grammarMapper.fromXML(grammarUrl);
                mediaContentResource.addRulesRecords(rulesRecordList);
            }

        };
        mediaContentPackageTracker.track("grammarfile", grammarFileTracker);

        return root;
    }


}
