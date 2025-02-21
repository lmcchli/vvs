/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.values;

import com.mobeon.masp.execution_engine.runtime.ValueVisitor;

public class Visitors {

    private static ValueVisitor onlyTextVisitor = new OnlyTextVisitor();
    private static ValueVisitor asBooleanVisitor = new AsBooleanVisitor();
    private static ValueVisitor asStringVisitor = new AsStringVisitor();
    private static ValueVisitor asECMAVisitor = new AsECMAVisitor();
    private static ValueVisitor asObjectVisitor = new AsObjectVisitor();

    public static ValueVisitor getAsBooleanVisitor() {
        return asBooleanVisitor;
    }

    public static ValueVisitor getOnlyTextVisitor() {
        return onlyTextVisitor;
    }

    public static ValueVisitor getAsStringVisitor() {
        return asStringVisitor;
    }

    public static ValueVisitor getAsECMAVisitor() {
        return asECMAVisitor;
    }

    public static ValueVisitor getAsObjectVisitor() {
        return asObjectVisitor;
    }
}
