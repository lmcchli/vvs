/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.runtime;

import org.mozilla.javascript.ScriptableObject;

/**
 * @author David Looberger
 */
public class ShadowVarBase extends ScriptableObject {
    public String markname;
    public long marktime;
    public String utterance;
    public String inputmode;
    public String interpretation;
    public String confidence;

    public ShadowVarBase(){
        markname = null;
        marktime = 0;
    }
    public String getClassName() {
        return "ShadowVarBase";
    }
}
