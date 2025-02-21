/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.xml;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediacontentmanager.MediaObjectSource;
import com.mobeon.masp.mediaobject.MediaLength;
import org.xml.sax.Attributes;

import java.io.CharArrayWriter;
import java.util.ArrayList;
import java.util.List;
import java.net.URISyntaxException;

/**
 * XML-to-object mapper that maps a MediaObjects-XML to
 * a list of {@link MediaObjectSource} objects.
 *
 * @author Mats Egland
 */
public class MediaObjectSourceMapper extends SaxMapper<List<MediaObjectSource>> {
    /**
     * List of mapped <code>MediaObjectSource</code>s.
     */
    private List<MediaObjectSource> mediaObjectSourceList;

    /**
     * The <code>MediaObjectSource</code> currently under construction.
     */
    private MediaObjectSource currentMediaObjectSource;


    /**
     * Logger used.
     */
    private static final ILogger LOGGER =
            ILoggerFactory.getILogger(MediaObjectSourceMapper.class);

    /**
     * Creates a <code>MediaContentResourceMapper</code>.
     */
    public MediaObjectSourceMapper() {
    }

    /**
     * Returns the from XML mapped list of
     * {@link MediaObjectSource} objects.
     *
     * @return The mapped object.
     */
    public List<MediaObjectSource> getMappedObject() {
        return mediaObjectSourceList;
    }

    public TagTracker createTagTrackerNetwork() {
        TagTracker root = new TagTracker() {
            public void onDeactivate() {
                // The root will be deactivated when
                // parsing a new docuemt begins
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("parsing new document, creating new list of MediaObjectSource's" +
                            " from " + parsedXMLUrl);
                }
                mediaObjectSourceList = new ArrayList<MediaObjectSource>();
            }
        };

        // -- create action for /mediaobjects/mediaobject
        TagTracker mediaObjectTracker = createMediaObjectTracker(root);
        // -- create action for /mediaobjects/mediaobject/lengths/length
        createLengthTracker(mediaObjectTracker);
        // -- create action for /mediaobjects/mediaobject/sourcetext
        createSourceTextTracker(mediaObjectTracker);

        return root;
    }

    /**
     * Creates action for /mediaobjects/mediaobjet/sourcetext
     *
     * @param parent The parent tracker
     */
    private void createSourceTextTracker(TagTracker parent) {
        TagTracker sourceTextTracker = new TagTracker() {
            public void onEnd(String namespaceURI,
                              String localName,
                              String qName,
                              CharArrayWriter contents) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("<" + localName + ">");
                }
                char[] characters = contents.toCharArray();

                if (characters.length > 0) {
                    String sourceText = new String(characters);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("\tsourceText:" + sourceText);
                    }
                    currentMediaObjectSource.setSourceText(sourceText);
                }
            }
        };
        parent.track("sourcetext", sourceTextTracker);
    }
    /**
     * Creates action for /mediaobjects/mediaobject/lengths/length
     *
     * @param parent The parent tracker
     */
    private void createLengthTracker(TagTracker parent) {
        TagTracker lengthTracker = new TagTracker() {
            public void onStart(String namespaceURI,
                                String localname,
                                String qName,
                                Attributes attr) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("<" + localname + ">");
                }
                String unit = attr.getValue("unit");
                String value = attr.getValue("value");
                // add length in this unit
                MediaLength length = new MediaLength(
                        MediaLength.LengthUnit.valueOf(unit.toUpperCase()),
                        Integer.parseInt(value));

                currentMediaObjectSource.addLength(length);
            }
        };
        parent.track("lengths/length", lengthTracker);
    }
    /**
     * Creates action for tag /mediaobjects/mediaobject
     *
     * @param root The parent tracker.
     * @return The tracker created for the action.
     */
    private TagTracker createMediaObjectTracker(TagTracker root) {
        // -- create action for /contents/content
        TagTracker mediaObjectTracker = new TagTracker() {
            public void onStart(String namespaceURI,
                                String localname,
                                String qName,
                                Attributes attr) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("<" + localname + ">");
                }

                // Create a new MediaContent and add it to list
                String type = attr.getValue("type");
                String src = attr.getValue("src");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\ttype=" + type);
                    LOGGER.debug("\tsrc=" + src);
                }


                try {

                    currentMediaObjectSource = new MediaObjectSource(
                            MediaObjectSource.Type.valueOf(type.toUpperCase()),
                            src, parsedXMLUrl.toURI());
                    mediaObjectSourceList.add(currentMediaObjectSource);
                } catch (IllegalArgumentException e) {
                    throw new SaxMapperException("Failed to create MediaObjectSource with type:"
                            + type
                            + " and src "
                            + src
                            + "as the type is illegal"
                            + parsedXMLUrl, e);
                } catch (URISyntaxException e) {
                    throw new SaxMapperException(
                            "Failed to create MediaObjectSource with src:"
                                + src
                                + " as the URL to the definition file:"
                                + parsedXMLUrl
                                + " could not be converted to a URI.", e);
                } catch (NullPointerException e) {
                    throw new SaxMapperException(
                            "Failed to create MediaObjectSource with src:"
                                + src
                                + " as type :"
                                + type
                                + " could not be converted to a MediaObjectSource type.", e);
                }

            }
        };
        root.track("mediaobjects/mediaobject", mediaObjectTracker);
        return mediaObjectTracker;
    }


}
