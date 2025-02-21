package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.mediacontentmanager.*;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaObject;
import com.mobeon.masp.mediaobject.MediaProperties;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.net.URI;

/**
 * Mock object for the media content reouserce.
 */
public class MediaContentResourceMock  extends BaseMock implements IMediaContentResource {

    /**
     * Map of the {@link com.mobeon.masp.mediacontentmanager.MediaContent}s this resource has.
     * Each content is keyed with its' id.
     */
    private Map<String, MediaContent> mediaContentMap =
            new HashMap<String, MediaContent>();
    /**
     * Map of the {@link com.mobeon.masp.mediacontentmanager.MediaObjectSource}s this resource has.
     * Each content is keyed with its' src.
     * This is all the <code>MediaObjectSource</code>s that a resource
     * uses.
     */
    private Map<String, MediaObjectSource> mediaObjectSourceMap =
            new HashMap<String, MediaObjectSource>();
    /**
     * The {@link com.mobeon.masp.mediacontentmanager.MediaContentResourceProperties} properties of the resource.
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
     * The {@link com.mobeon.masp.mediacontentmanager.MediaObjectBuilder} used to create
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
    public MediaContentResourceMock(URI packageDefinitionFile) {
        super ();
        log.info ("MOCK: MediaContentResourceMock.MediaContentResourceMock");
        if (packageDefinitionFile == null) {
            throw new IllegalArgumentException("packageDefinitionFile is null");
        }
        this.packageDefinitionFile = packageDefinitionFile;
        properties = new MediaContentResourceProperties();
        try {
            properties.addCodec(new MimeType("audio/pcmu"));
        } catch (MimeTypeParseException e) {
            log.error("Exception");
        }
        this.id = "mock";
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
    public List<String> getMediaContentIDs(IMediaQualifier[] qualifiers) {
        // todo use the qualifiers
        return new ArrayList<String>(mediaContentMap.keySet());
    }

    public IMediaObject[] getMediaContent(String id, IMediaQualifier[] qualifiers)
            throws MediaContentManagerException {

        IMediaObject[] iml = null;

        if (id == null) {
            throw new IllegalArgumentException("Argument id is null");
        }
        log.info ("MOCK: MediaContentResourceMock.getMediaContent "+id);

        if (id.equals("1")) {
            log.info ("MOCK: MediaContentResourceMock.getMediaContent Returning with an array of one object");
            MediaObject mo = new MediaObject (new MediaProperties(null, "wav", 4711));

            IMediaObject im = mo;
            iml = new IMediaObject[1];
            iml[0] = im;
        }

        if (id.equals("2")) {
            log.info ("MOCK: MediaContentResourceMock.getMediaContent Returning null");
            iml = null;
        }

        if (id.equals("3")) {
            log.info ("MOCK: MediaContentResourceMock.getMediaContent Returning with an empty array");
            iml = new IMediaObject[0];
        }

        if (id.equals("4")) {
            log.info ("MOCK: MediaContentResourceMock.getMediaContent id is 4. Returning with an array of one object");
            MediaObject mo = new MediaObject (new MediaProperties(null, "wav", 10*4711));

            IMediaObject im = mo;
            iml = new IMediaObject[1];
            iml[0] = im;
        }

        // No match return empty list
        return iml;
    }

    public List<String> getAllMediaContentIDs() {
        return new ArrayList<String>(mediaContentMap.keySet());
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
        if (this.id == compared.getID()) {
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
     * Overridden to be consisten with equals.
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
}
