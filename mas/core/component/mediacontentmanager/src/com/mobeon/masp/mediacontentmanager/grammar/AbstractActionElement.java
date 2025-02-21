package com.mobeon.masp.mediacontentmanager.grammar;

import com.mobeon.masp.mediacontentmanager.IActionElement;
import com.mobeon.masp.mediacontentmanager.MessageElement;

/**
 * Abstract implementation of {@link IActionElement}.
 *
 * @author mmawi
 */
public abstract class AbstractActionElement implements IActionElement {
    /**
     * This action element's type.
     */
    IActionElement.ActionType type;

    /**
     * Creates an <code>AbstractActionElement</code> with the specified type.
     *
     * @param type The type of action element.
     */
    protected AbstractActionElement(ActionType type) {
        this.type = type;
    }

    //javadoc in interface
    public ActionType getType() {
        return type;
    }

    //javadoc in interface
    public int getSwapValue() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Only action elements of type " +
                "swap have a swap value.");
    }

    //javadoc in interface
    public String getMediaFileName() throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Only action elements of type " +
                "mediafile have a message element.");
    }

    //javadoc in interface
    public void setSwapValue(int swapValue) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Only action elements of type " +
                "swap can have a swap value.");
    }

    //javadoc in interface
    public void setMediaFileName(String ref) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Only action elements of type " +
                "mediafile can have a message element.");
    }

    public abstract String toString();
}
