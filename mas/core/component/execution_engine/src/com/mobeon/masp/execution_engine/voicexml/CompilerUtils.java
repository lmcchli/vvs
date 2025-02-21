/*
 * Copyright (c) 2006 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml;

import com.mobeon.masp.execution_engine.compiler.Constants;

public class CompilerUtils {

    /**
     * @param tag_name the name of the tag
     * @return returns true if tag_name is the name of an input item tag in VoiceXML
     */
    public static boolean isInputItem(String tag_name) {
        if(tag_name.equals("field")) return true;
        else if(tag_name.equals("record") ) return true;
        else if(tag_name.equals("object") ) return true;
        else if(tag_name.equals("subdialog")) return true;
        else if(tag_name.equals(Constants.VoiceXML.TRANSFER)) return true;


        return false;
    }
}
