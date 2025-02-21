package com.mobeon.masp.mediacontentmanager.grammar;

/**
 * @author mmawi
 */
public class SelectActionElement extends AbstractActionElement {
    protected SelectActionElement() {
        super(ActionType.select);
    }

    public String toString(){
        return "<select>";
    }
}
