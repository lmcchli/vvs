package com.mobeon.masp.mediacontentmanager;

/**
 * Interface for action element.
 * <p/>
 * An action element has a type defined in enumeration {@link ActionType}.
 *
 * @author mmawi
 */
public interface IActionElement {
    /**
     * Enumeration of different types of action elements.
     * <p/>
     * The types are:
     * <ul>
     * <li>mediafile</li>
     * <li>swap</li>
     * <li>skip</li>
     * <li>select</li>
     * </ul>
     */
    public enum ActionType {
        mediafile,
        swap,
        skip,
        select
    }

    /**
     * Returns the type of this action element. See the {@link ActionType}
     * enumeration for possible types.
     *
     * @return The type of this action element.
     */
    ActionType getType();

    /**
     * Returns this action element's swap value.
     * @return The swap value.
     * @throws UnsupportedOperationException If the action element's type
     *                                       is not <code>ActionType.swap</code>.
     */
    int getSwapValue() throws UnsupportedOperationException;

    /**
     * Returns this action element's media file name.
     *
     * @return The action element's media file name.
     * @throws UnsupportedOperationException If the action element's type
     *                                       is not <code>ActionType.mediafile</code>.
     */
    String getMediaFileName() throws UnsupportedOperationException;

    /**
     * Set this action element's swap value.
     * @param swapValue The swap value.
     * @throws UnsupportedOperationException If the action element's type
     *                                       is not <code>ActionType.swap</code>.
     */
    void setSwapValue(int swapValue) throws UnsupportedOperationException;

    /**
     * Set the name of this action's media file.
     *
     * @param ref   The media file's reference.
     * @throws UnsupportedOperationException If the action element's type
     *                                       is not <code>ActionType.mediafile</code>.
     */
    void setMediaFileName(String ref) throws UnsupportedOperationException;

    /**
     * @return A string representation of this action element
     */
    String toString();
}
