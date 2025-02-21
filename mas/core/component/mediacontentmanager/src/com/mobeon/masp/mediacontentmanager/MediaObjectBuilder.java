/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.*;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.mediacontentmanager.grammar.RulesRecord;
import com.mobeon.masp.mediacontentmanager.grammar.GenericNumberBuilder;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import java.io.File;
import java.net.URI;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * This class builds {@link com.mobeon.masp.mediaobject.IMediaObject}s from
 * {@link MessageElement}s. A <code>MessageElement</code> of the type
 * quantifier may result in several <code>IMediaObject</code>s. A
 * mediafile <code>MessageElement</code> is always mapped to only one
 * <code>IMediaObject</code>. Therefore the build() method returns a list of
 * <code>IMediaObject</code>s, but the list may contain only a single
 * <code>IMediaObject</code>.
 * <p/>
 * A <code>MediaObjectBuilder</code> creates mediaobjects on
 * behalf of one <code>MediaContentResource</code>.
 * <p/>
 * A <code>MediaObjectBuilder</code> uses a {@link MediaObjectCache} to
 * cache created <code>IMediaObject</code>s, keyed on the <code>MediaObjectSource</code>s
 * they are created from. The cache may be null, in which case to cache is used
 * and the media objects are always created.
 *
 * @author Mats Egland
 */
public class MediaObjectBuilder {
    /**
     * The {@link com.mobeon.common.logging.ILogger} logger used for logging purposes.
     */
    protected static final ILogger LOGGER =
            ILoggerFactory.getILogger(MediaObjectBuilder.class);

    private static final String TEXT_PLAIN_MIME = "text/plain";
    /**
     * The factory used for creating <code>IMediaObject</code>s.
     */
    private IMediaObjectFactory mediaObjectFactory;
    /**
     * The <code>MediaContentResource</code> the
     * builder works on behalf for.
     */
    private MediaContentResource mediaContentResource;
    /**
     * Used for mapping codecs to content-types
     * and content-types to file-extendions.
     */
    private ContentTypeMapper contentTypeMapper;
    /**
     * Used to cache created mediaobjects. Can be null in
     * case no cache is used.
     */
    private MediaObjectCache mediaObjectCache;

    /**
     * The {@link GenericNumberBuilder} used by this
     * <code>MediaObjectBuilder</code> to decompose
     * qualifier values into <code>MessageElement</code>s.
     */
    private IGenericNumberBuilder genericNumberBuilder = new GenericNumberBuilder();

    /**
     * Creates a <code>MediaObjectBuilder</code> that
     * works on behalf of the specified <code>MediaContentResource</code>.
     * Uses the specified factory for creating new <code>IMediaObject</code>s.
     *
     * @param resource           The <code>MediaContentResource</code> the
     *                           builder works on behalf for.
     * @param cache              Optional, can be null. Cache for
     *                           created media objects.
     * @param mediaObjectFactory The media object factory.
     * @param contentTypeMapper  Used for mapping codecs to content-types
     *                           and content-types to file-extendions.
     */
    public MediaObjectBuilder(MediaContentResource resource,
                              IMediaObjectFactory mediaObjectFactory,
                              MediaObjectCache cache,
                              ContentTypeMapper contentTypeMapper) {
        this.mediaContentResource = resource;
        this.mediaObjectFactory = mediaObjectFactory;
        this.contentTypeMapper = contentTypeMapper;
        this.mediaObjectCache = cache;

    }

    /**
     * Builds a <code>IMediaObject</code> from the specified
     * <code>MessageElement</code> and sets the properties
     * on it based on the specified <code>IMediaContentResourceProperties</code>.
     *
     * @param messageElement    The message-element that the media-object
     *                          is created from.
     * @return The created media-objects.
     * @throws MediaContentManagerException If the creation of the mediaobject fails.
     * @throws IllegalArgumentException     If messageElement argument is null.
     */
    public IMediaObject build(MessageElement messageElement)
        throws MediaContentManagerException {
        IMediaObject result = null;
        List<IMediaObject> mediaObjects = build(messageElement, null);
        if (mediaObjects != null) {
            result = mediaObjects.get(0);
        }
        return result;
    }

    /**
     * Builds a list of <code>IMediaObject</code>s from the specified
     * <code>MessageElement</code> and sets the properties
     * on it based on the specified <code>IMediaContentResourceProperties</code>.
     *
     * @param messageElement    The message-element that the media-object
     *                          is created from.
     * @param qualifiers        The qualifiers for this message.
     * @return The created media-objects.
     * @throws MediaContentManagerException If the creation of the mediaobject fails.
     * @throws IllegalArgumentException     If messageElement argument is null.
     */
    public List<IMediaObject> build(MessageElement messageElement, IMediaQualifier[] qualifiers)
            throws MediaContentManagerException {
        if (messageElement == null) {
            throw new IllegalArgumentException("Argument messageElement is null");
        }

        List<IMediaObject> resultList = null;
        MessageElement.MessageElementType type =
                messageElement.getType();
        switch (type) {
            case qualifier:
                resultList = buildFromQualifier(messageElement, qualifiers);
                break;
            case mediafile:
                resultList = new ArrayList<IMediaObject>();
                IMediaObject mediaFileObject = buildFromMediaFile(messageElement);
                if (mediaFileObject != null) {
                    resultList.add(mediaFileObject);
                }
                break;
            case text:
                resultList = new ArrayList<IMediaObject>();
                IMediaObject textObject = buildFromText(messageElement);
                if (textObject != null) {
                    resultList.add(textObject);
                }
                break;
        }
        return resultList;
    }

    /**
     * Builds a <code>IMediaObject</code> from a
     * <code>MessageElement</code> of type text.
     *
     * @param messageElement The messageElement to build from.
     * @return The created media-object.
     * @throws MediaContentManagerException If the creation of the mediaobject fails.
     * @throws IllegalArgumentException     If the messageElement is null, if no matching
     *                                      <code>MediaObjectSource</code> exist for the
     *                                      message element.
     */
    private IMediaObject buildFromText(MessageElement messageElement)
            throws MediaContentManagerException {
        // Retreives the MediaObjectSource that the MessageElement
        // is referencing
        MediaObjectSource mediaObjectSource =
                mediaContentResource.getMediaObjectSource(messageElement.getReference());
        if (mediaObjectSource == null) {
            throw new MediaContentManagerException(
                    "Failed to create IMediaObject from reference "
                            + messageElement.getReference()
                            + " for resource "
                            + mediaContentResource.getID() + " as a MediaObjectSource is not" +
                            " found for the reference. This may indicate an error in the language package");

        }
        // check if cached
        IMediaObject mediaObject;
        if (mediaObjectCache != null) {
            mediaObject = mediaObjectCache.get(mediaObjectSource);
            if (mediaObject != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Returning cached IMediaObject for src=" + mediaObjectSource.getSrc());
                }
                return mediaObject;
            }
        }
        MediaProperties mediaProperties =
            new MediaProperties();

        // Map the codecs of the resource to a contentType
        MimeType contentType = null;
        try {
            contentType = new MimeType(TEXT_PLAIN_MIME);
            mediaProperties.setContentType(contentType);
            String ext = contentTypeMapper.mapToFileExtension(new MediaMimeTypes(contentType));
            validateFileExtension(ext);
            mediaProperties.setFileExtension(ext);
        } catch (MimeTypeParseException e) {
            String message = "Failed to create mime type of " + TEXT_PLAIN_MIME;
            LOGGER.error(message,e);
           throw new MediaContentManagerException(
                   message, e);
        }

        List<MediaLength> lengths = mediaObjectSource.getAllMediaLengths();
        for (MediaLength mediaLength : lengths) {
            mediaProperties.addLength(mediaLength);
        }
        try {
            mediaObject = mediaObjectFactory.create(mediaObjectSource.getSourceText(),
                    mediaProperties);
        } catch (MediaObjectException e) {
            String message = "Failed to create text mediaobject. ";
            LOGGER.error(message ,e);

            throw new MediaContentManagerException(
                    message + e);        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Created new IMediaObject for src=" + mediaObjectSource.getSrc());
        }
        if (mediaObjectCache != null) {
            mediaObjectCache.add(mediaObjectSource, mediaObject);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Added IMediaObject to cache, key is MediaObjectSource with src="
                        + mediaObjectSource.getSrc()
                        + ". Current size of cache is " + mediaObjectCache.size());
            }
        }
        return mediaObject;
    }

    /**
     * Builds a <code>IMediaObject</code> from a
     * <code>MessageElement</code> of type mediafile.
     *
     * @param messageElement The messageElement to build from.
     * @return The created media-object.
     * @throws MediaContentManagerException If the creation of the mediaobject fails.
     * @throws IllegalArgumentException     If the messageElement is null or not of type
     *                                      <code>mediafile</code>.
     */
    private IMediaObject buildFromMediaFile(IMessageElement messageElement)
            throws MediaContentManagerException {

        if (messageElement == null) {
            throw new IllegalArgumentException("Argument messageElement is null");
        } else if (messageElement.getType() != MessageElement.MessageElementType.mediafile) {
            throw new IllegalArgumentException("Argument messageElement is not of type mediafile");
        }
        // Retreives the MediaObjectSource that the MessageElement
        // is referencing
        MediaObjectSource mediaObjectSource =
                mediaContentResource.getMediaObjectSource(messageElement.getReference());
        if (mediaObjectSource == null) {
            throw new MediaContentManagerException(
                    "Failed to create IMediaObject from reference "
                            + messageElement.getReference()
                            + " for resource "
                            + mediaContentResource.getID() + " as a MediaObjectSource is not" +
                            " found for the reference. This may indicate an error in the language package");

        }
        // check if cached
        IMediaObject mediaObject = null;
        if (mediaObjectCache != null) {
            mediaObject = mediaObjectCache.get(mediaObjectSource);
            if (mediaObject != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Returning cached IMediaObject for src=" + mediaObjectSource.getSrc());
                }
            }
        }
        if (mediaObject != null) {
            return mediaObject;
        } else {
            // Create a file from the relative fileReference
            // The URI to the content definition file
            mediaObject = buildFromMediaFile(mediaObjectSource);
            if (mediaObject != null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Created new IMediaObject for src=" + mediaObjectSource.getSrc());
                }
                if (mediaObjectCache != null) {
                    mediaObjectCache.add(mediaObjectSource, mediaObject);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Added IMediaObject to cache, key is MediaObjectSource with src="
                                + mediaObjectSource.getSrc()
                                + ". Current size of cache is " + mediaObjectCache.size());
                    }
                }
            }
            return mediaObject;
        }
    }

    /**
     * Builds a <code>IMediaObject</code> from the
     * <code>MediaObjectSource</code> specified.
     *
     * @param mediaObjectSource
     * @return
     * @throws MediaContentManagerException
     */
    private IMediaObject buildFromMediaFile(
            MediaObjectSource mediaObjectSource)
            throws MediaContentManagerException {

        IMediaObject mediaObject;
        URI contentDefFile =
                mediaObjectSource.getDefinitionFile();
        URI mediaFileURI;
        try {
            mediaFileURI = contentDefFile.resolve(mediaObjectSource.getSrc());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Failed to create media object. " +
                    "Could not create URI from mediaObjectSource: <" +
                    mediaObjectSource.getSrc() +
                    "> mediaContentResource: " + mediaContentResource.getID() +
                    ". Check media content package config files.",e);
            return null;
        }

        File mediaFile = new File(mediaFileURI);
        if (mediaFile.exists()) {
            MediaProperties mediaProperties =
                    new MediaProperties();

            // Map the codecs of the resource to a contentType
            MimeType contentType = getContentType(mediaObjectSource);

            mediaProperties.setContentType(contentType);

            String ext = mediaObjectSource.getFileExtension();
            mediaProperties.setFileExtension(ext);
            List<MediaLength> lengths = mediaObjectSource.getAllMediaLengths();
            for (MediaLength mediaLength : lengths) {
                mediaProperties.addLength(mediaLength);
            }
            try {
                mediaObject =
                        mediaObjectFactory.create(mediaFile, mediaProperties);
            } catch (MediaObjectException e) {
                String message = "Failed to create IMediaObject";
                LOGGER.error(message, e);
                throw new MediaContentManagerException(message, e);
            }
        } else {
            throw new MediaContentManagerException("Failed to create IMediaObject" +
                    " from MessageElement as the file="
                    + mediaFile.getAbsolutePath()
                    + " does not exist. Impossible to create a IMediaObject from it.");
        }
        return mediaObject;
    }

    /**
     * Validates that the file-extension is valid.
     *
     * @param ext         The file-extension
     *
     *
     * @throws MediaContentManagerException If the extension is null.
     */
    private void validateFileExtension(String ext)
                throws MediaContentManagerException {
        if (ext == null || ext.length() == 0) {
            throw new MediaContentManagerException("Failed to create IMediaObject" +
                    " from MessageElement as the file extension could not be mapped.");
        }
    }

    /**
     * Maps the codecs of the resource to a contentType and
     * returns it.
     *
     * @return The mapped content-type.
     * @throws MediaContentManagerException If the mapping from codecs
     *                                      to content-type fails.
     */
    private MimeType getContentType(MediaObjectSource source) throws MediaContentManagerException {
        MimeType contentType = null;
        String fileExtension = source.getFileExtension();
        if(fileExtension != null)
            contentType = contentTypeMapper.mapToContentType(fileExtension);
        if (contentType == null) {
            throw new MediaContentManagerException("Could not map the file extension:" + fileExtension
                    + " to a content-type");
        }
        return contentType;
    }

    /**
     * Sets the cache for this builder. If no cache is set
     * the builder will create a new IMediaObject each time.
     *
     * @param mediaObjectCache The cache to use.
     */
    public void setMediaObjectCache(MediaObjectCache mediaObjectCache) {
        this.mediaObjectCache = mediaObjectCache;
    }


    /**
     * Builds a list of <code>IMediaObject</code>s from a
     * <code>MessageElement</code> of type qualifier.
     *
     * @logs.warning "No qualifier matching the string (qualifier) was found in the content. Resource id: (resource)"
     * - Check if the qualifiers are correct in the media content file in the resource.
     *
     * @param messageElement    The qualifier message-element that the media-object
     *                          is created from.
     * @param qualifiers        The qualifiers for this message.
     * @return The created media-objects.
     * @throws MediaContentManagerException If the creation of the mediaobject fails.
     * @throws IllegalArgumentException     If messageElement argument is null or is
     *                                      not of type qualifier or if argument
     *                                      qualifiers is null.
     */
    private List<IMediaObject> buildFromQualifier(MessageElement messageElement, IMediaQualifier[] qualifiers)
            throws MediaContentManagerException {

        if (messageElement == null) {
            throw new IllegalArgumentException("Argument messageElement is null");
        } else if (messageElement.getType() != MessageElement.MessageElementType.qualifier) {
            throw new IllegalArgumentException("Argument messageElement is not of type qualifier");
        } else if (qualifiers == null) {
            throw new IllegalArgumentException("Argument qualifiers is null");
        }

        String messageElementQualifierRef = messageElement.getReference();
        String messageElementQualifierName;
        String qualifierTypeStr;
        String qualifierGenderStr;
        try {
            StringTokenizer st = new StringTokenizer(messageElementQualifierRef, ":");
            messageElementQualifierName = st.nextToken();
            qualifierTypeStr = st.nextToken();
            qualifierGenderStr = st.nextToken();
        } catch (NullPointerException e) {
            String message = "Could not get reference from " +
            "qualifier MessageElement.";
            LOGGER.error(message, e);

            throw new MediaContentManagerException(message, e);
        } catch (NoSuchElementException e) {
            String message = "The MessageElement's reference " +
            "is not formatted correctly: " + messageElementQualifierRef;
            LOGGER.error(message, e);
            throw new MediaContentManagerException(message, e);
        }

        IMediaQualifier.QualiferType messageElementQualifierType;
        IMediaQualifier.Gender messageElementQualifierGender;
        try {
            messageElementQualifierType = IMediaQualifier.QualiferType.valueOf(qualifierTypeStr);
        }
        catch (IllegalArgumentException e) {
            String message = "Failed to parse from String to "
                + "IMediaQualifer.QualifierType. The type:"
                + qualifierTypeStr + " is not a member of the IMediaQualifier.QualiferType "
                + "enumeration. ";
            LOGGER.error(message, e);
            throw new MediaContentManagerException(message, e);
        }
        try {
            messageElementQualifierGender = IMediaQualifier.Gender.valueOf(qualifierGenderStr);
        }
        catch (IllegalArgumentException e) {
            String message = "Failed to parse from String to "
                + "IMediaQualifer.QualifierType. The type:"
                + qualifierTypeStr + " is not a member of the IMediaQualifier.QualiferType "
                + "enumeration. ";
            LOGGER.error(message, e);
            throw new MediaContentManagerException(message, e);
        }

        for (IMediaQualifier qualifier : qualifiers) {
            if (qualifier.getName().equals(messageElementQualifierName) &&
                    qualifier.getGender().equals(messageElementQualifierGender) &&
                    qualifier.getType().equals(messageElementQualifierType))
            {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Qualifier name: " + qualifier.getName()
                            + ", gender:" + qualifier.getGender()
                            + ", type:" + qualifier.getType()
                            + ", value:" + qualifier.getValue());
                }

                List<IMediaObject> resultList = new ArrayList<IMediaObject>();

                // If the qualifier is of type MediaObject, we just return that MediaObject.
                if (qualifier.getType() == IMediaQualifier.QualiferType.MediaObject) {
                    resultList.add((IMediaObject) qualifier.getValue());
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Returning IMediaObject from qualifier: " + qualifier.getName());
                    }
                } else {
                    // The qualifier should be decomposed into MessageElements.
                    List<IMessageElement> qualifierMessageElements = decomposeQualifier(qualifier);
                    // Build a IMediaObject for each MessageElement.
                    if (qualifierMessageElements != null) {
                        for (IMessageElement element : qualifierMessageElements) {
                            resultList.add(buildFromMediaFile(element));
                        }
                    }
                }
                return resultList;
            }
        }
        LOGGER.warn("No qualifier matching the string " + messageElementQualifierRef
                + " was found in the content. Resource id: " + mediaContentResource.getID());
        return null;
    }

    /**
     * Decompose a <code>IMediaQualifier</code>'s value into
     * <code>MessageElement</code>s.
     *
     * @logs.warning "No rules records found in the MediaContentResource with id: (resource id)"
     * - Check that the grammar rules file for this resource is correct.
     *
     * @logs.warning "No matching rule found for this qualifier type: (qualifier type) gender: (gender) MediaContentResource: (resource id)"
     * - Check that the grammar rules file for this resource is correct. A rule set for the
     * qualifier type and gender is required.
     *
     * @logs.info "Unsupported IMediaQualifier.QualifierType: (qualifier type)"
     * - An invalid qualifier type is used.
     *
     * @param qualifier The IMediaQualifier to decompose.
     * @return A list of MessageElements representing the qualifier's value.
     * @throws MediaContentManagerException If the qualifier is of wrong type.
     */
    private List<IMessageElement> decomposeQualifier(IMediaQualifier qualifier)
            throws MediaContentManagerException {
        if (qualifier.getType() == IMediaQualifier.QualiferType.String) {
            // The qualifier is a String, return a list of message elements,
            // one element for each character in the string.
            return decomposeString((String) qualifier.getValue());
        } else if (qualifier.getType() == IMediaQualifier.QualiferType.WeekDay) {
            // The qualifier is a WeekDay, return a message element
            // representing the day.
            IMessageElement weekDayElement;
            weekDayElement = decomposeWeekDay((Date) qualifier.getValue());
            List<IMessageElement> qualifierMessageElements =
                    new ArrayList<IMessageElement>();
            qualifierMessageElements.add(weekDayElement);
            return qualifierMessageElements;
        } else {
            List<IMessageElement> qualifierMessageElements;
            DateFormat dateFormat;
            List<RulesRecord> rulesRecordList = mediaContentResource.getRulesRecords();
            if (rulesRecordList.size() == 0) {
                LOGGER.warn("No rules records found in the MediaContentResource with id: " +
                        mediaContentResource.getID());
                return null;
            }
            for (RulesRecord rulesRecord : rulesRecordList) {
                if (rulesRecord.compareRule(qualifier.getGender(), qualifier.getType())) {
                    switch (qualifier.getType()) {
                        case DateDM:
                            dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            qualifierMessageElements = decomposeDateDM(rulesRecord,
                                    dateFormat.format(qualifier.getValue()));
                            break;
                        case CompleteDate:
                            dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            qualifierMessageElements = decomposeCompleteDate(rulesRecord,
                                    dateFormat.format(qualifier.getValue()));
                            break;
                        case Number:
                            qualifierMessageElements = decomposeNumber(rulesRecord,
                                    (Integer) qualifier.getValue());
                            break;
                        case Time12:
                            dateFormat = new SimpleDateFormat("HH:mm:ss");
                            qualifierMessageElements = decomposeTime12(rulesRecord,
                                    dateFormat.format(qualifier.getValue()));
                            break;
                        case Time24:
                            dateFormat = new SimpleDateFormat("HH:mm:ss");
                            qualifierMessageElements = decomposeTime24(rulesRecord,
                                    dateFormat.format(qualifier.getValue()));
                            break;
                        default:
                            if (LOGGER.isInfoEnabled())
                                LOGGER.info("Unsupported IMediaQualifier.QualifierType:"
                                            + qualifier.getType());
                            return null;
                    }
                    return qualifierMessageElements;
                }
            }
            LOGGER.warn("No matching rule found for this qualifier type: "
                    + qualifier.getType() + " gender: " +qualifier.getGender()
                    + ", MediaContentResource: " + mediaContentResource.getID());
        }
        return null;
    }

    /**
     * Return a <code>MessageElement</code> with a reference to a media object
     * representing a week day for a date. e.g. 2006-02-27 would be "mon"
     *
     * @param value The date
     * @return A <code>MessageElement</code> representing the day of week for
     * the date.
     */
    private IMessageElement decomposeWeekDay(Date value) {
        DateFormat dateFormat = new SimpleDateFormat("EEE", Locale.ENGLISH);
        String weekDay = dateFormat.format(value);
        List<MimeType> mimeTypes =
                mediaContentResource.getMediaContentResourceProperties().getMediaCodecs();
        String fileExt = contentTypeMapper.mapToFileExtension(new MediaMimeTypes(mimeTypes));
        return new MessageElement(MessageElement.MessageElementType.mediafile,
                weekDay.toLowerCase() + "." + fileExt);
    }

    /**
     * Build a list of <code>MessageElement</code>s from a String qualifier.
     *
     * @logs.info "No valid characters found in qualifier value:(string)"
     * - Only characters a-z,A-Z,0-9 can be used. Other characters are filtered
     * and it appears that no characters are left after filtering.
     *
     * @param value     The value of the qualifier. Only characters a-z, A-Z
     *                  and digits 0-9 is supported, all other characters
     *                  is removed from the string before it is converted.
     * @return  A list of <code>MessageElement</code>s representing the string,
     * one element for each character.
     */
    private List<IMessageElement> decomposeString(String value) {
        List<String> resultList = null;
        value = value.replaceAll("[^a-zA-Z0-9]", "");
        if (value.length() > 0) {
            char[] characters = value.toCharArray();
            resultList = new ArrayList<String>();
            for (char ch : characters) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Character: " + ch);
                }
                resultList.add(String.valueOf(ch));
            }
        } else {
            if (LOGGER.isInfoEnabled())
                LOGGER.info("No valid characters found in qualifier value:<" + value + ">");
            return null;
        }
        return addFileExtension(resultList);
    }

    /**
     * Build a list of <code>MessageElement</code>s from a DateDM qualifier.
     *
     * @param rule              The <code>RulesRecord</code> containing the number
     *                          rules for this qualifier.
     * @param qualifierValue    The value of the qualifier (the date in form "yyyy-MM-dd")
     * @return A list of <code>MessageElement</code>s representing the qualifier.
     */
    private List<IMessageElement> decomposeDateDM(RulesRecord rule, String qualifierValue)
            throws MediaContentManagerException {
        // Date in format "yyyy-MM-dd"
        StringTokenizer st = new StringTokenizer(qualifierValue, "-");
        String year = st.nextToken(); //Year not used.
        Long month = Long.valueOf(st.nextToken());
        Long day = Long.valueOf(st.nextToken());

        // NumberBuilder expects input in the form ddMM00
        return addFileExtension(genericNumberBuilder.buildNumber(rule, day * 10000 + month * 100));
    }

    /**
     * Build a list of <code>MessageElement</code>s from a DateDMY qualifier.
     *
     * @param rule              The <code>RulesRecord</code> containing the number
     *                          rules for this qualifier.
     * @param qualifierValue    The value of the qualifier (the date in form "yyyy-MM-dd")
     * @return A list of <code>MessageElement</code>s representing the qualifier.
     */
    private List<IMessageElement> decomposeCompleteDate(RulesRecord rule, String qualifierValue)
            throws MediaContentManagerException {
        // Date in format "yyyy-MM-dd"
        StringTokenizer st = new StringTokenizer(qualifierValue, "-");
        Long year = Long.valueOf(st.nextToken());
        Long month = Long.valueOf(st.nextToken());
        Long day = Long.valueOf(st.nextToken());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("year=" + year);
            LOGGER.debug("month=" + month);
            LOGGER.debug("day=" + day);
            LOGGER.debug("NUMBER=" + (year * 1000000 + day * 10000 + month * 100));
        }

        // NumberBuilder expects input in the form yyyyddMM00
        return addFileExtension(genericNumberBuilder.buildNumber(rule, year * 1000000 + day * 10000 + month * 100));
    }

    /**
     * Build a list of <code>MessageElement</code>s from a Number qualifier.
     *
     * @param rule              The <code>RulesRecord</code> containing the number
     *                          rules for this qualifier.
     * @param qualifierValue    The value of the qualifier
     * @return A list of <code>MessageElement</code>s representing the qualifier.
     */
    private List<IMessageElement> decomposeNumber(RulesRecord rule, Integer qualifierValue) {
        return addFileExtension(genericNumberBuilder.buildNumber(rule, Long.valueOf(qualifierValue)));
    }

    /**
     * Build a list of <code>MessageElement</code>s from a Time12 qualifier.
     *
     * @param rule              The <code>RulesRecord</code> containing the number
     *                          rules for this qualifier.
     * @param qualifierValue    The value of the qualifier (the time in form "HH:mm:ss")
     * @return A list of <code>MessageElement</code>s representing the qualifier.
     */
    private List<IMessageElement> decomposeTime12(RulesRecord rule, String qualifierValue) {
        // Time in format "HH:mm:ss"
        // postfix: 0 = AM, 7000 = PM
        Long postfix = (long) 0;
        StringTokenizer st = new StringTokenizer(qualifierValue, ":");
        Long hours = Long.valueOf(st.nextToken());
        Long minutes = Long.valueOf(st.nextToken());

        if (hours >= 12) {
            if (hours > 12) {
                hours = hours - 12;
            }
            postfix = (long) 7000;
        } else if (hours == 0) {
            hours = (long) 12;
        }
        // NumberBuilder expects input in the form HHmm0000 for AM and HHmm7000 for PM.
        return addFileExtension(genericNumberBuilder.buildNumber(rule, hours * 1000000 + minutes * 10000 + postfix));
    }

    /**
     * Build a list of <code>MessageElement</code>s from a Time24 qualifier.
     *
     * @param rule              The <code>RulesRecord</code> containing the number
     *                          rules for this qualifier.
     * @param qualifierValue    The value of the qualifier (the time in form "HH:mm:ss")
     * @return A list of <code>MessageElement</code>s representing the qualifier.
     */
    private List<IMessageElement> decomposeTime24(RulesRecord rule, String qualifierValue) {
        // Time in format "HH:mm:ss"
        StringTokenizer st = new StringTokenizer(qualifierValue, ":");
        Long hours = Long.valueOf(st.nextToken());
        Long minutes = Long.valueOf(st.nextToken());
        // NumberBuilder expects input in the form HHmm00
        return addFileExtension(genericNumberBuilder.buildNumber(rule, hours * 10000 + minutes * 100));
    }

    /**
     * Adds a file extension on each of the files in the list of file names.
     * The file extension is determined from the ContentTypeMapper.
     *
     * @param fileList The list of incomplete file names.
     * @return A list of <code>IMessageElements</code> with references to the
     * files.
     */
    private List<IMessageElement> addFileExtension(List<String> fileList) {
        List<IMessageElement> resultList = new ArrayList<IMessageElement>();
        // get file extension for this media content resource's codecs.
        List<MimeType> mimeTypes =
                mediaContentResource.getMediaContentResourceProperties().getMediaCodecs();
        String fileExt = contentTypeMapper.mapToFileExtension(new MediaMimeTypes(mimeTypes));
        for (String file : fileList) {
            resultList.add(new MessageElement(
                    IMessageElement.MessageElementType.mediafile,
                    file + "." + fileExt));
        }
        return resultList;
    }
}
