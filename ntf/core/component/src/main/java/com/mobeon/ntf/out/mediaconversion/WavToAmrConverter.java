package com.mobeon.ntf.out.mediaconversion;
/*
* COPYRIGHT Abcxyz Communication Inc. Montreal 2010
* The copyright to the computer program(s) herein is the property
* of ABCXYZ Communication Inc. Canada. The program(s) may be used
* and/or copied only with the written permission from ABCXYZ
* Communication Inc. or in accordance with the terms and conditions
* stipulated in the agreement/contact under which the program(s)
* have been supplied.
*---------------------------------------------------------------------
* Created on 7-Apr-2010
*/

/**
 * Converts wav-files to 3gp-files. Copied from MCC component
 * @deprecated
 */
public class WavToAmrConverter extends ProgramCallingConverter {


    /**
     * Constructor.
     */
    private WavToAmrConverter() {
        super("wav", "amr", "audio/amr", "/opt/moip/ntf/bin/wavtoamr");
    }

    public static WavToAmrConverter get() {
        if (inst == null) {
            synchronized (instLock) {
                if (inst == null) {
                    inst = new WavToAmrConverter();
                }
            }
        }

        return inst;
    }

    static Object instLock = new Object();
    static WavToAmrConverter inst = null;
}
