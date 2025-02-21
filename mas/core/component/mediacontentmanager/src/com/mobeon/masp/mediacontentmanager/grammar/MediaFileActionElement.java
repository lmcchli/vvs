package com.mobeon.masp.mediacontentmanager.grammar;

/**
 * Implementation of mediafile action element.
 *
 * @author mmawi
 */
public class MediaFileActionElement extends AbstractActionElement {
    /**
     * This <code>MediaFileActionElement</code>s message element.
     */
    private String mediaFileName;

    /**
     * Creates a new <code>MediaFileActionElement</code>.
     */
    protected MediaFileActionElement() {
        super(ActionType.mediafile);
    }

    //javadoc in interface
    public String getMediaFileName() throws UnsupportedOperationException {
        return mediaFileName;
    }

    //javadoc in interface
    public void setMediaFileName(String ref) throws UnsupportedOperationException {
        mediaFileName = ref;
    }

    
    public String toString() {
        return mediaFileName;
    }
}
