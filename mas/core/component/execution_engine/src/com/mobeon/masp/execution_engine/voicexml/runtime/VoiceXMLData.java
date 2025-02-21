/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.runtime;

import com.mobeon.masp.execution_engine.compiler.Executable;
import com.mobeon.masp.execution_engine.runtime.Data;

import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

/**
 * VXML specific data instance stored in a StackFrame.
 * <p>
 * <b>Threading: </b>This class is <em>not</em> safe for concurrent access by multiple threads
 * @author Mikael Andersson
 */
public class VoiceXMLData extends Data {

    public VoiceXMLData() {}

    private VoiceXMLData(int depth, List<Executable> destructors, int constructorProgress) {
        this.depth = depth;
        this.destructors = destructors == null?null: Collections.unmodifiableList(new ArrayList<Executable>(destructors));
        this.constructorProgress = constructorProgress;
    }

    public Data duplicate() {
        return new VoiceXMLData(depth,destructors,constructorProgress);
    }

    public String getClassName() {
        return "VoiceXMLData";
    }
}
