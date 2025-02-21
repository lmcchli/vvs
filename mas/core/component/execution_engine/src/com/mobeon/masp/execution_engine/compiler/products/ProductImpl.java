/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.products;

import com.mobeon.masp.util.test.MASTestSwitches;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Product;

/**
 * @author Mikael Andersson
 */
public class ProductImpl extends ProductBase {

    public ProductImpl(Product parent, String localName, DebugInfo debugInfo) {
        super(parent, localName, debugInfo);
        setName(localName);
        ensureValidName(localName);
        ensureValidDebugInfo(debugInfo);
    }

    private void ensureValidDebugInfo(DebugInfo debugInfo) {
        if (MASTestSwitches.isCompilerTesting() || MASTestSwitches.isUnitTesting()) {
            if(debugInfo == null)
            throw new NullPointerException("Product instance of class " +
                    this.getClass().getSimpleName() +
                    "+allocated with null DebugInfo, this is not allowed !");
        }
    }

    private void ensureValidName(String localName) {
        if (MASTestSwitches.isCompilerTesting()) {
            if (localName == null) {
                this.setName("__internal_" + "UnitTest");
            }
        } else {
            if (localName == null) {
                this.setName("__internal_" + getId());
            }
        }
    }

    public ProductImpl(Product parent, DebugInfo debugInfo) {
        super(parent, debugInfo);
        ensureValidName(null);
        ensureValidDebugInfo(debugInfo);
    }

    public Class getCanonicalClass() {
        return Product.class;
    }

}
