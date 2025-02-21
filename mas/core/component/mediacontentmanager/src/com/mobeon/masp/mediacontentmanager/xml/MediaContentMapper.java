/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager.xml;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediacontentmanager.*;
import com.mobeon.masp.mediacontentmanager.condition.Condition;
import com.mobeon.masp.mediacontentmanager.qualifier.MediaQualifierFactory;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;
import java.io.CharArrayWriter;
import java.net.URISyntaxException;

/**
 * XML-to-object mapper that maps a MediaContent-XML to
 * a list of {@link MediaContent} objects.
 *
 * @author Mats Egland
 */
public class MediaContentMapper extends SaxMapper<List<MediaContent>> {
    /**
     * List of mapped <code>MediaContent</code>s.
     */
    private List<MediaContent> mediaContentList;

    /**
     * The MediaContent currently under construction.
     */
    private MediaContent currentMediaContent;
    /**
     * The <code>Message</code> under construction.
     */
    private Message currentMessage;
    /**
     * The <code>MessageElement</code> under construction.
     */
    private MessageElement currentMessageElement;

    /**
     * Logger used.
     */
    private static final ILogger LOGGER =
            ILoggerFactory.getILogger(MediaContentMapper.class);

    private IMediaQualifierFactory qualifierFactory =
            new MediaQualifierFactory();

    /**
     * Creates a <code>MediaContentResourceMapper</code>.
     */
    public MediaContentMapper() {
    }

    /**
     * Returns the from XML mapped list of
     * {@link MediaContent} objects.
     *
     * @return The mapped object.
     */
    public List<MediaContent> getMappedObject() {
        return mediaContentList;
    }

    public TagTracker createTagTrackerNetwork() {
        TagTracker root = new TagTracker() {
            public void onDeactivate() {
                // The root will be deactivated when
                // parsing a new docuemt begins
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("parsing new document, creating new list of MediaContents.");
                }
                mediaContentList = new ArrayList<MediaContent>();
            }
        };

        // -- create action for /mediacontents/mediacontent
        TagTracker mediaContentTracker = createMediaContentTracker(root);
        // -- create action for /mediacontents/mediacontent/qualifiers/qualifier
        createQualifierTracker(mediaContentTracker);
        // -- create action for /mediacontents/mediacontent/instance
        TagTracker instanceTracker = createInstanceTracker(mediaContentTracker);
        // -- create action for /mediacontents/mediacontent/instance/element
        TagTracker elementTracker = createElementTracker(instanceTracker);
        // -- create action for /mediacontents/mediacontent/instance/element
        createSourceTextTracker(elementTracker);


        TagTracker spokenTextTracker = new TagTracker() {
            public void onEnd(String namespaceURI,
                              String localName,
                              String qName,
                              CharArrayWriter contents ){
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("<" + localName + ">");
                }
                char[] characters = contents.toCharArray();

                if (characters.length > 0 ) {
                    String spokenText = new String(characters);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("\tspokentext:" + spokenText);
                    }
                    currentMessageElement.setSpokenText(spokenText);
                }
            }
        };
        elementTracker.track("spokentext", spokenTextTracker);

        return root;
    }
    /**
     * Creates action for /mediacontents/mediacontent/instance/element/sourcetext
     *
     * @param elementTracker The parent tracker
     */
    private void createSourceTextTracker(TagTracker elementTracker) {
        TagTracker sourceTextTracker = new TagTracker() {
            public void onEnd(String namespaceURI,
                              String localName,
                              String qName,
                              CharArrayWriter contents ){
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("<" + localName + ">");
                }
                char[] characters = contents.toCharArray();

                if (characters.length > 0) {
                    String sourceText = new String(characters);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("\tsourceText:"+sourceText);
                    }
                    currentMessageElement.setSourceText(sourceText);
                }
            }
        };
        elementTracker.track("sourcetext", sourceTextTracker);
    }

    /**
     * Creates action for /mediacontents/mediacontent/instance/element
     *
     * @param instanceTracker The parent tracker
     *
     * @return The tracker for the action.
     */
    private TagTracker createElementTracker(TagTracker instanceTracker) {
        TagTracker elementTracker = new TagTracker() {

            public void onStart(String namespaceURI,
                                String localname,
                                String qName,
                                Attributes attr) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("<" + localname + ">");
                }
                String type = attr.getValue("type").toLowerCase();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\ttype=" + type);
                }
                String reference = attr.getValue("reference");
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("\treference=" + reference);
                    }
                // Create MessageElement and add to Message
                MessageElement.MessageElementType messageType;
                try {
                    messageType =
                            MessageElement.MessageElementType.valueOf(type);
                } catch (IllegalArgumentException e) {
                    throw new SaxMapperException(
                            "Failed to map MessageElementType from string="
                            + type + ". "
                            + "MediaContent with id="
                            + currentMediaContent.getId()
                            + " was not created successfully.", e);
                }
                currentMessageElement =
                        new MessageElement(messageType, reference);
                currentMessage.appendMessageElement(currentMessageElement);
            }
        };
        instanceTracker.track("element", elementTracker);
        return elementTracker;
    }

    /**
     * Creates action for /mediacontents/mediacontent/instance
     *
     * @param mediaContentTracker The parent tracker.
     * @return The tracker for the action.
     */
    private TagTracker createInstanceTracker(TagTracker mediaContentTracker) {
        TagTracker instanceTracker = new TagTracker() {
            public void onStart(String namespaceURI,
                                String localname,
                                String qName,
                                Attributes attr) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("<" + localname + ">");
                }

                String cond = attr.getValue("cond");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\tcond=" + cond);
                }

                currentMessage = new Message(new Condition(cond),
                        currentMediaContent);
                currentMediaContent.addMessage(currentMessage);
            }
        };
        mediaContentTracker.track("instance", instanceTracker);
        return instanceTracker;
    }

    /**
     * Creates action for /mediacontents/mediacontent/qualifiers/qualifier.
     *
     * @param mediaContentTracker The parent tracker.
     * @return The tracker for the action.
     */
    private TagTracker createQualifierTracker(TagTracker mediaContentTracker) {
        // -- create action for /contents/content/qualifiers/qualifier
        TagTracker qualifierTracker = new TagTracker() {
            public void onStart(String namespaceURI,
                                String localname,
                                String qName,
                                Attributes attr) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("<" + localname + ">");
                }

                // Create a new MediaContent and add it to list
                String name = attr.getValue("name");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\tname=" + name);
                }
                String type = attr.getValue("type");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\ttype=" + type);
                }
                String gender = attr.getValue("gender");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\tgender=" + gender);
                }
                IMediaQualifier.QualiferType qualifierType;
                try {
                    qualifierType = IMediaQualifier.QualiferType.valueOf(type);
                } catch (IllegalArgumentException e) {
                    throw new SaxMapperException("Failed to create IMediaQualifer with name "
                            + name + " as the type:"
                            + type + " is not a member of the IMediaQualifier.QualiferType "
                            + "enumeration. "
                            + "MediaContent with id="
                            + currentMediaContent.getId()
                            + " was not created successfully.", e);

                }
                IMediaQualifier.Gender genderType;
                try {
                    genderType = IMediaQualifier.Gender.valueOf(gender);
                } catch (IllegalArgumentException e) {
                    throw new SaxMapperException("Failed to create IMediaQualifer as the gender:"
                            + gender + " is not a member of the IMediaQualifier.Gender "
                            + "enumeration. "
                            + "MediaContent with id="
                            + currentMediaContent.getId()
                            + " was not created successfully.", e);
                }
                if (qualifierType == IMediaQualifier.QualiferType.MediaObject) {
                    IMediaQualifier qualifer = qualifierFactory.create(
                            name, null, genderType);
                    currentMediaContent.addQualifier(qualifer);
                } else {
                    try {
                        IMediaQualifier qualifer = qualifierFactory.create(
                                qualifierType,
                                name,
                                null,
                                genderType);
                        currentMediaContent.addQualifier(qualifer);
                    } catch (MediaQualifierException e) {
                        throw new SaxMapperException("Failed to create IMediaQualifer of type:"
                                + type + ". "
                                + "MediaContent with id="
                                + currentMediaContent.getId()
                                + " was not created successfully.", e);
                    }
                }
            }
        };
        mediaContentTracker.track("qualifiers/qualifier", qualifierTracker);
        return qualifierTracker;
    }

    /**
     * Creates action for tag /mediacontents/mediacontent
     *
     * @param root The parent tracker.
     * @return The tracker created for the action.
     */
    private TagTracker createMediaContentTracker(TagTracker root) {
        // -- create action for /contents/content
        TagTracker mediaContentTracker = new TagTracker() {
            public void onStart(String namespaceURI,
                                String localname,
                                String qName,
                                Attributes attr) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("<" + localname + ">");
                }

                // Create a new MediaContent and add it to list
                String id = attr.getValue("id");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\tid=" + id);
                }
                try {
                    currentMediaContent = new MediaContent(id, parsedXMLUrl.toURI());
                } catch (URISyntaxException e) {
                    throw new SaxMapperException("Failed to create MediaContent with id:"
                                + id
                                + " as the URL to the definition file:"
                                + parsedXMLUrl
                                + " could not be converted to a URI.", e);
                }
                String returnAllStr = attr.getValue("returnall");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("\treturnall=" + returnAllStr);
                }
                Boolean returnAll = Boolean.valueOf(returnAllStr);
                if (returnAll) {
                    currentMediaContent.setReturnAll(returnAll);
                }
                mediaContentList.add(currentMediaContent);
            }
        };
        root.track("mediacontents/mediacontent", mediaContentTracker);
        return mediaContentTracker;
    }


}
