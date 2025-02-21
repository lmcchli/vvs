/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.compiler.products.PredicateImpl;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;

/**
 * Implementation class for input items that may contain prompts
 *
 * @author David Looberger
 */
public class InputItemImpl extends PredicateImpl implements InputItem {
    private boolean isRepromptPoint;
    private Product catches;
    private Product properties;

    public InputItemImpl(Product parent, String localName, DebugInfo debugInfo) {
        super(parent, localName, debugInfo);
    }

    public void setIsRepromptPoint(boolean value) {
        isRepromptPoint = value;
    }

    public InputItemImpl(Product parent, DebugInfo debugInfo) {
        super(parent, debugInfo);
    }

    public boolean eval(ExecutionContext ex) {
        if (super.eval(ex)) {
            if (isRepromptPoint)
                ((VXMLExecutionContext) ex).setRepromptPoint(this);
            return true;
        }
        else {
            return false;
        }
    }

    public Product getCatches() {
        return catches;
    }

    public void setCatches(Product catches) {
        this.catches = catches;
    }

    public Product getProperties() {
        return properties;
    }

    public void setProperties(Product properties) {
        this.properties = properties;
    }
}
