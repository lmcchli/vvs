/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediacontentmanager.condition.ConditionInterpreterException;
import com.mobeon.masp.mediacontentmanager.grammar.RulesRecord;

import java.net.URI;
import java.util.*;

/**
 * Default implementation of the {@link IMediaContentResource}
 * interface.
 * <p/>
 * A resource is build from a language package on the file system. The memory
 * structure of the resource is therefore mapped from the underlying XML files
 * that describes the content of the resource.
 * <p/>
 * The <code>mediaContentMap</code> contains the content of the XML file
 * MediaContent.xml which holds all contents keyed with their id.
 * </p>
 * The <code>mediaObjectSourceMap</code> holds all sources that the contents
 * reference. A content may have media elements that reference a file or
 * a text. If so, the reference points to a specific media object in the
 * file MediaObjects.xml.
 * <p/>
 * The <code>rulesRecordList</code> holds all grammar rules from the grammar
 * file grammar.xml. Each RulesRecord contains rules for a number type and one
 * or several genders, e.g. "DateDM" "Male,None".
 * <p/>
 * Note: The compareTo method of this class compares solely on the priority. So different instances
 * of this class with equal priority will be considered equal to the compareTo method BUT
 * unequal with the equals method.
 * By this, this class has a natural ordering that is inconsistent with equals.
 */
public class MediaContentResource implements IMediaContentResource {
    /**
     * The {@link com.mobeon.common.logging.ILogger} logger used for logging purposes.
     */
    protected static final ILogger LOGGER =
            ILoggerFactory.getILogger(MediaContentResource.class);

    /**
     * Map of the {@link MediaContent}s this resource has.
     * Each content is keyed with its' id.
     */
    private Map<String, MediaContent> mediaContentMap =
            new HashMap<String, MediaContent>();
    /**
     * Map of the {@link MediaObjectSource}s this resource has.
     * Each content is keyed with its' src.
     * This is all the <code>MediaObjectSource</code>s that a resource
     * uses.
     */
    private Map<String, MediaObjectSource> mediaObjectSourceMap =
            new HashMap<String, MediaObjectSource>();

    /**
     * List of the {@link RulesRecord}s this resource has.
     */
    private List<RulesRecord> rulesRecordList =
            new ArrayList<RulesRecord>();
    /**
     * The {@link MediaContentResourceProperties} properties of the resource.
     */
    private MediaContentResourceProperties properties;
    /**
     * The unique id of this resource.
     */
    private String id;
    /**
     * The priority of this resource.
     * The default is lowest priority.
     */
    private int priority = Integer.MAX_VALUE;
    /**
     * The {@link MediaObjectBuilder} used to create
     * media objects from <code>MessageElments</code>.
     */
    private MediaObjectBuilder mediaObjectBuilder;
    /**
     * URI to the package definition file
     * that defines this resource.
     */
    private URI packageDefinitionFile;

    /**
     * Empty constructor. Creates A <code>MediaContentResource</code>
     * with an empty <code>MediaContentResourceProperties</code>.
     *
     * @param packageDefinitionFile URI to the package definition file
     *                              that defines this resource.
     * @throws IllegalArgumentException If the packageDefinitionFile is null.
     */
    public MediaContentResource(URI packageDefinitionFile) {
        if (packageDefinitionFile == null) {
            throw new IllegalArgumentException("packageDefinitionFile is null");
        }
        this.packageDefinitionFile = packageDefinitionFile;
        properties = new MediaContentResourceProperties();
    }

    /**
     * Sets the identity for this resource.
     *
     * @param id The id as a string.
     */
    public void setID(String id) {
        this.id = id;
    }

    // Javadoc in interface
    public String getID() {
        return id;
    }

    // javadoc in interface
    public MediaContentResourceProperties getMediaContentResourceProperties() {
        return properties;
    }

    /**
     * Sets the priority this resource.
     *
     * @param prio The priority.
     */
    public void setPriority(int prio) {
        this.priority = prio;
    }

    // Javadoc in interface
    public int getPriority() {
        return priority;
    }

    // javadoc in interface
    public List<String> getMediaContentIDs(IMediaQualifier[] qualifiers)
        throws MediaContentManagerException {
        if (qualifiers == null || qualifiers.length == 0) {
            // return all ids
            List<String> result = new ArrayList<String>(mediaContentMap.keySet());
            Collections.sort(result);
            return result;
        } else {
            List<String> result = new ArrayList<String>();
            MediaContent mediaContent;

            for (String id : mediaContentMap.keySet()) {
                mediaContent = mediaContentMap.get(id);
                // only contents with number of qualifiers as specified should be searched
                if (mediaContent.validateQualifiers(qualifiers)) {
                    if (getMatchingMessage(mediaContent, qualifiers) != null) {
                        result.add(id);
                    }
                }

            }
            Collections.sort(result);
            return result;
        }
    }

    /**
     * Retrieves media content with the specified id
     * as a list of {@link IMediaObject}s.
     *
     * The optional <code>qualifiers</code> argument has two purposes:
     * <ul>
     * <li>To select only the media content,
     * which condition matches the qualifier list</li>
     * <li>If the content has qualifiers: To be converted to their
     * Media Object representation and be part of the returned list.</li>
     * </ul>select only
     *
     * @logs.info "The requested media content with id=(contentid) was not found in the resource (resourceid)"
     * - A request for a content with an id not defined in the media contents has been made.
     *
     * @param id             The id of the content.
     * @param qualifiers     Optional list of qualifiers for the
     *                       content. (i.e. parameter can be null).
     * @return List of {@link IMediaObject}s that represents
     *         the specified id, and matches the qualifiers.
     *         Empty list if no match, never returns null.
     *
     * @throws MediaContentManagerException If error occurs when creating the
     *                                      media objects or if a condition for a
     *                                      specific message failed to be interpreted.
     *
     * @throws IllegalArgumentException     If id is null or if the qualifiers specified
     *                                      is not valid input to the content.
     *                                      The number of qualifiers specified must match
     *                                      the number of qualifiers of the content and
     *                                      also be of correct types.
     *                                      This exception is also thrown if the requested
     *                                      id is not defined in the media content package.
     */
    public IMediaObject[] getMediaContent(String id, IMediaQualifier[] qualifiers)
            throws MediaContentManagerException, IllegalArgumentException {

        if (id == null) {
            throw new IllegalArgumentException("Argument id is null");
        }
        MediaContent matchingContent;
        matchingContent = mediaContentMap.get(id);
        if (matchingContent != null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Found requested media content with id=" + id
                        + ", resource id=" + this.id);
            }

            Message matchingMessage = getMatchingMessage(matchingContent, qualifiers);
            if (matchingMessage == null) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("No matching message for the media content with id="
                            + id + " was found in the resource " + this.id);
                }
                return new IMediaObject[]{};
            }

            List<MessageElement> messageElementList =
                    matchingMessage.getMessageElements();
            List<IMediaObject> result =
                    new ArrayList<IMediaObject>();

            for (MessageElement messageElement : messageElementList) {
                List<IMediaObject> mediaObjects;
                try {
                    mediaObjects = mediaObjectBuilder.build(messageElement, qualifiers);
                } catch (MediaContentManagerException e) {
                    String message = "Failed to create an IMediaObject from MessageElement with "
                        + " type=" + messageElement.getType()
                        + " and reference=" + messageElement.getReference()
                        + " resource id=" + this.id;

                    LOGGER.error(message,e);
                    throw new MediaContentManagerException(message, e);
                }
                if (mediaObjects != null) {
                    result.addAll(mediaObjects);
                }
            }
            return result.toArray(new IMediaObject[result.size()]);

        } else {
            // Invalid content id
            String message = "The requested media content with id="
                    + id + " was not found in the resource " + this.id;
            if (LOGGER.isInfoEnabled())
                LOGGER.info(message);
            //return new IMediaObject[]{};
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Retreives the first message in the specified content that matches
     * the qualifiers.
     *
     * @param matchingContent The content where the messages resides.
     * @param qualifiers      The input values to the message-conditions.
     * @return                The matching Message, or null if no matching message.
     *
     * @throws MediaContentManagerException If a message-condition failed to be interpreted.
     */
    private Message getMatchingMessage(MediaContent matchingContent, IMediaQualifier[] qualifiers)
            throws MediaContentManagerException {
        Message matchingMessage;
        try {
            matchingMessage = matchingContent.getMatchingMessage(qualifiers);
        } catch (ConditionInterpreterException e) {
            throw new MediaContentManagerException("Failed to interpret a condition for " +
                    "a message in content with id " +
                    matchingContent.getId(), e);
        }
        return matchingMessage;
    }

    public List<String> getAllMediaContentIDs() {
        List<String> result = new ArrayList<String>(mediaContentMap.keySet());
        Collections.sort(result);
        return result;
    }

    /**
     * Adds the specified <code>MediaContent</code> to this resource.
     * Note: The mediaContentMap is not synchronized as this method
     * is called only at creation.
     *
     * @param mediaContent The content that is added.
     * @throws IllegalArgumentException If argument is null or
     *                                  if a MediaContent with same id
     *                                  already exist.
     */
    public void addMediaContent(MediaContent mediaContent) {
        if (mediaContent == null) {
            throw new IllegalArgumentException("mediaContent parameter is null");

        }
        String id = mediaContent.getId();

        if (mediaContentMap.containsKey(id)) {
            throw new IllegalArgumentException("A MediaContent with id:" + id
                    + " already exist in MediaContentResource " + this.id);
        }
        mediaContentMap.put(id, mediaContent);

    }

    /**
     * Adds the specified <code>MediaObjectSource</code> to this resource.
     * Note: The mediaObject is not synchronized as this method
     * is called only at creation.
     *
     * @param mediaObjectSource The source that is added.
     * @throws IllegalArgumentException If argument is null or
     *                                  if a MediaObjectSource with same src
     *                                  already exist.
     */
    public void addMediaObjectSource(MediaObjectSource mediaObjectSource) {
        if (mediaObjectSource == null) {
            throw new IllegalArgumentException("mediaObjectSource parameter is null");

        }
        String src = mediaObjectSource.getSrc();

        if (mediaObjectSourceMap.containsKey(src)) {
            throw new IllegalArgumentException("A MediaObjectSource with src=" + src
                    + " already exist in MediaContentResource " + this.id +
                    ". No duplicates are allowed.");
        }
        mediaObjectSourceMap.put(src, mediaObjectSource);

    }

    /**
     * Add a list of <code>RulesRecord</code>s to this resource.
     * Note: The rulesRecordList is not synchronized as this method
     * is called only at creation.
     *
     * @param rulesRecordList   The list that is added.
     * @throws IllegalArgumentException If argument is null.
     */
    public void addRulesRecords(List<RulesRecord> rulesRecordList) {
        if (rulesRecordList == null) {
            throw new IllegalArgumentException("rulesRecordList parameter is null");
        }

        this.rulesRecordList = rulesRecordList;
    }

    /**
     * Returns the {@link MediaObjectSource} with the specified src.
     *
     * @param src The src of the MediaObjectSource (id).
     * @return The <code>MediaObjectSource</code> with the specified
     *         id, or null if no such source exist.
     * @throws NullPointerException if the src is <tt>null</tt>.
     */
    public MediaObjectSource getMediaObjectSource(String src) {
        return mediaObjectSourceMap.get(src);
    }

    /**
     * Sets the builder used to build media objects
     * from <code>MessageElement</code>s.
     *
     * @param mediaObjectBuilder The media object factory.
     */
    public void setMediaObjectBuilder(MediaObjectBuilder mediaObjectBuilder) {
        this.mediaObjectBuilder = mediaObjectBuilder;
    }

    /**
     * Compares this instance with another IMediaContentResource.
     * If id is same the resources are same.
     * <p/>
     * Otherwise, the priority determines the comparison, highest priority
     * (i.e. lowest integer) is considered ordered before.
     * <p/>
     * If id is not equal but priority is equal, the result of
     * compareTo of the id's is returned.
     *
     * @param compared The IMediaContentResource that this instance is compared against.
     * @return a negative integer, zero, or a positive integer as the first argument is less
     *         than, equal to, or greater than the second.
     */
    public int compareTo(IMediaContentResource compared) {
        if (this.id.equals(compared.getID())) {
            return 0;
        } else {
            if (this.priority <
                    compared.getPriority()) {
                return -1;
            } else if (this.priority >
                    compared.getPriority()) {
                return 1;
            }
        }
        // The id is not same, but priority is same, sort on the id then.
        return this.id.compareTo(compared.getID());
    }

    /**
     * Overridden to provide consistence with compareTo.
     * Returns true if id of specified resource is same
     * as this id.
     *
     * @param obj The object with which to compare to.
     * @return If the id of the passed resource is same
     *         as is of this resource.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MediaContentResource)) {
            return false;
        }
        MediaContentResource resource = (MediaContentResource) obj;
        return resource.getID().equals(this.id);
    }

    /**
     * Overridden to be consistent with equals.
     *
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * Returns URI to the top-level package definition file
     * that defines this resource.
     *
     * @return The root directory for this resource.
     */
    public URI getPackageDefinitionFile() {
        return packageDefinitionFile;
    }

    /**
     * Returns this resource's list of <code>RulesRecord</code>s.
     * @return This resource's list of <code>RulesRecord</code>s
     */
    public List<RulesRecord> getRulesRecords() {
        return rulesRecordList;
    }
}
