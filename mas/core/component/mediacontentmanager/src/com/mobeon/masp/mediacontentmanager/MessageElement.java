/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

/**
 * A <tag>MessageElement</tag> represents one atomic media-message. A message element
 * may be part of a sequence of elements, together making up a whole message.
 *
 * <p/>
 * A message element have a type, which is one of the enumeration
 * {@link MessageElementType} and a reference to the message.
 * The reference may point to a file or be the value
 * itself represented as a string.
 *
 * @author Mats Egland
 */
public class MessageElement implements IMessageElement {

    /**
     * The type of this message element.
     */
    private MessageElementType messageElementType;

    /**
     * Reference to the message-element.
     * The reference may point to a file or be the value
     * itself represented as a string.
     */
    private String reference;

    /**
     * Source text of the element.
     */
    private String sourceText;
    /**
     * Spoken text of the element.
     */
    private String spokenText;

    /**
     * Creates a <code>MessageElement</code> with the type,
     * reference and parent <code>Message</code> specified.
     *
     * @param type The type of message.
     * @param ref  Reference to the message source.
     *
     * @throws IllegalArgumentException If argument parent or ref is null.
     */
    public MessageElement(MessageElementType type, String ref) {
        if (ref == null) {
            throw new IllegalArgumentException("Argument ref is null");
        }
        this.messageElementType = type;
        this.reference = ref;
    }

    public MessageElementType getType() {
        return messageElementType;
    }

    /**
     * Returns the reference to the actual message-element.
     * For example a file reference if the type of
     * this message-element is mediafile.
     *
     * @return Reference to the message-element.
     */
    public String getReference() {
        return reference;
    }

    /**
     * Sets the source text of the message element.
     * @param sourceText the source text.
     */
    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    /**
     * Sets the spoken text of the message element.
     * @param spokenText the spoken text.
     */
    public void setSpokenText(String spokenText) {
        this.spokenText = spokenText;
    }

    /**
     *
     * @return the source text of the message element.
     */
    public String getSourceText() {
        return sourceText;
    }

    /**
     *
     * @return the spoken text of the message element.
     */
    public String getSpokenText() {
        return spokenText;
    }
}
