package com.mobeon.masp.execution_engine.voicexml.runtime;

import org.mozilla.javascript.ScriptableObject;

/**
 * @author David Looberger
 */
public class TransferShadowVars  extends ScriptableObject {
    public long duration;
    public String utterance;
    public String inputmode;

    public TransferShadowVars() {
        super();
    }

     public String getClassName() {
        return "TransferShadowVars";
    }
}
