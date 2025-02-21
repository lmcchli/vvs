package com.mobeon.masp.mediacontentmanager.grammar;

import com.mobeon.masp.mediacontentmanager.IActionElementFactory;
import com.mobeon.masp.mediacontentmanager.IActionElement;

/**
 * Default implementation of the {@link IActionElementFactory} interface.
 *
 * @author mmawi
 */
public class ActionElementFactory implements IActionElementFactory {
    //javadoc in interface
    public IActionElement create(IActionElement.ActionType type) {
        IActionElement actionElement = null;
        switch (type) {
            case mediafile:
                actionElement = new MediaFileActionElement();
                break;
            case select:
                actionElement = new SelectActionElement();
                break;
            case skip:
                actionElement = new SkipActionElement();
                break;
            case swap:
                actionElement = new SwapActionElement();
                break;
        }
        return actionElement;
    }
}
