package com.mobeon.masp.execution_engine.voicexml.runtime;

import org.mozilla.javascript.ScriptableObject;
import com.mobeon.masp.execution_engine.compiler.Constants;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Apr 5, 2006
 * Time: 4:57:56 PM
 * To change this template use File | Settings | File Templates.
 *
 *
 * This class models the "values" property on a dialog.transfer event.
 *
 */
public class TransferValue  extends ScriptableObject {
    private static final long serialVersionUID = -4776395359638273247L;
    public TransferValue(String presentationIndicator, String ani){
        if(presentationIndicator != null){
            defineProperty(Constants.CCXML.TRANSFER_LOCAL_PI, presentationIndicator, ScriptableObject.READONLY);
        }
        if(ani != null){
            defineProperty(Constants.CCXML.TRANSFER_ANI, ani, ScriptableObject.READONLY);
        }
    }
    public String getClassName() {
        return "TransferValue";
    }
}
