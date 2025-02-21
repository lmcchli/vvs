/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.runtime;

import com.mobeon.masp.execution_engine.compiler.Executable;
import com.mobeon.masp.execution_engine.runtime.Data;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * CCXML specific data instance stored in a StackFrame.
 * <p>
 * <b>Threading: </b>This class is <em>not</em> safe for concurrent access by multiple threads
 * @author Mikael Andersson
 */
public class CCXMLData extends Data {

    public CCXMLData(){}

    private CCXMLData(int depth, List<Executable> destructors, int constructorProgress) {
        this.depth = depth;
        this.destructors = destructors == null?null: Collections.unmodifiableList(new ArrayList<Executable>(destructors));
        this.constructorProgress = constructorProgress;
    }

    public Data duplicate() {
        return new CCXMLData(depth,destructors,constructorProgress);
    }
}
