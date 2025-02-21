/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.runtime;

import com.mobeon.masp.execution_engine.runtime.Statics;
import com.mobeon.masp.execution_engine.runtime.values.Pair;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class VoiceXMLStatics extends Statics {
    private Map<String,Object> params = new HashMap<String, Object>();

    public void setParams(List<Pair> params) {
        if(params != null && params.size() > 0) {
            for (Pair param : params) {
                this.params.put(param.getName(),param.getValue());
            }
        }
    }

    public Map<String,Object> getParams() {
        return params;
    }
}
