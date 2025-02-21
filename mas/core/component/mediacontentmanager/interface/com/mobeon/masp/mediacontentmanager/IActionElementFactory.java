package com.mobeon.masp.mediacontentmanager;

/**
 * Factory for IActionElements.
 * @author mmawi
 */
public interface IActionElementFactory {
    /**
     * Creates a new <code>IActionElement</code> of the specified type.
     *
     * @param type The action element's type.
     * @return An <code>IActionElement</code> of the specified type.
     */
    IActionElement create(IActionElement.ActionType type);
}
