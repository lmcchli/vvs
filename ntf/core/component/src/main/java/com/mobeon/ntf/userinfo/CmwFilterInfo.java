/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo;

import com.mobeon.ntf.Constants;
import com.mobeon.ntf.util.Logger;
import java.util.*;


public class CmwFilterInfo {
    
    //protected UserDevice[] notifDevices   = null;
    private String[] numbers;

    /**
     * Constructor
     */
    /*
    public CmwFilterInfo(Vector devices) {
        Vector dev = new Vector();
        for (Iterator it = devices.iterator(); it.hasNext(); ) {
            UserDevice ud = (UserDevice) it.next();
            if (ud.hasCMW()) {
                dev.add(ud);
            }
        }
        notifDevices = new UserDevice[dev.size()];
        notifDevices = ((UserDevice[]) dev.toArray(notifDevices));
    }
    */
    public CmwFilterInfo(String[] numbers) {
        this.numbers = numbers;
    }
         
    public String[] getNumbers() {
        return numbers;
    }
    /*
    public UserDevice[] getDevices(){
        return notifDevices;
    }*/
         
    public String toString() {
    	return "{CmwFilterInfo}";
    }
}
