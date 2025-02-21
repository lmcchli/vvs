/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.grammar;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.execution_engine.xml.CompilerElement;

public abstract class Grammar {
    protected final ILogger logger = ILoggerFactory.getILogger(DTMFGrammar.class);
    protected SCOPE scope;
    private InputMode mode = InputMode.DTMF; // TODO: Voice is defalt in standard but for us?
    private String grammar_id;

    public enum InputMode {
        VOICE, DTMF;

        public String toString() {
            switch (this) {
                case VOICE:
                    return "voice";
                case DTMF:
                    return "dtmf";

            }
            return "invalid";
        }
    }

    public Grammar(InputMode mode, SCOPE scope) {
        this.mode = mode;
        this.scope = scope;
    }

    public SCOPE getScope() {
        return scope;
    }


    public void setScope(SCOPE scope) {
        this.scope = scope;
    }

    public String getGrammar_id() {
        return grammar_id;
    }

    public void setGrammar_id(String grammar_id) {
        this.grammar_id = grammar_id;
    }

    public static enum SCOPE {
        APPLICATION, DOCUMENT, LOCAL
    }
}
