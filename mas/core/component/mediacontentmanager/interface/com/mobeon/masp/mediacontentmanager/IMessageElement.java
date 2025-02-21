package com.mobeon.masp.mediacontentmanager;

/**
 * Interface for a message element. A message element is an atomic media-message
 * and may be part of a sequence of elements, together making up a whole message.
 * <p/>
 * A message element have a type, which is one of the enumeration
 * {@link MessageElementType} and a reference to the message.
 * The reference may point to a file or be the value itself represented as a
 * string.
 *
 * @author mmawi
 */
public interface IMessageElement {
    /**
     * Enumerations of the different types of message-elements.
     */
    public enum MessageElementType {
        qualifier, mediafile, text
    }

    /**
     * Returns the type of the message element.
     * 
     * @return The type of the message element.
     */
    public MessageElementType getType();

    /**
     * Returns the reference to the actual message-element.
     * For example a file reference if the type of
     * this message-element is mediafile.
     *
     * @return Reference to the message-element.
     */
    public String getReference();

    /**
     * Sets the source text of the message element.
     *
     * @param sourceText the source text.
     */
    public void setSourceText(String sourceText);

    /**
     * Sets the spoken text of the message element.
     * @param spokenText the spoken text.
     */
    public void setSpokenText(String spokenText);

    /**
     * Returns the source text of the message element.
     *
     * @return the source text of the message element.
     */
    public String getSourceText();

    /**
     * Returns the spoken text of the message element.
     *
     * @return the spoken text of the message element.
     */
    public String getSpokenText();
}
