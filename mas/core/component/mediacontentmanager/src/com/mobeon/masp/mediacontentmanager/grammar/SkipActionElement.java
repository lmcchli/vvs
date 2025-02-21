package com.mobeon.masp.mediacontentmanager.grammar;

/**
 * Implementation if skip action element.
 *
 * @author mmawi
 */
public class SkipActionElement extends AbstractActionElement {
    /**
     * Creates a new <code>SkipActionElement</code>.
     */
    protected SkipActionElement() {
        super(ActionType.skip);
    }

    public String toString() {
        return "<skip>";
    }
}
