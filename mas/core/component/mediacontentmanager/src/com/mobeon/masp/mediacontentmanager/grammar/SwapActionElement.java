package com.mobeon.masp.mediacontentmanager.grammar;

/**
 * Implementation of swap action element.
 *
 * @author mmawi
 */
public class SwapActionElement extends AbstractActionElement {
    private int swapValue;

    /**
     * Create a new <code>SwapActionElement</code>.
     */
    protected SwapActionElement() {
        super(ActionType.swap);
    }

    //javadoc in interface
    public int getSwapValue() throws UnsupportedOperationException {
        return swapValue;
    }

    //javadoc in interface
    public void setSwapValue(int swapValue) throws UnsupportedOperationException {
        this.swapValue = swapValue;
    }

    public String toString() {
        return "<swap " + swapValue + ">";
    }
}
