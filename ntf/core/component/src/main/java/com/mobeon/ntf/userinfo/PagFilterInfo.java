/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo;

import com.mobeon.ntf.Constants;
import com.mobeon.ntf.userinfo.UserInfo.NotifState;
import com.mobeon.ntf.util.Logger;

import java.util.*;

public class PagFilterInfo implements Constants{

    private String content;
    private boolean hangup = false;
    private String number;
    
    
    /**
     * Constructor
     */
    public PagFilterInfo(UserInfo user) {
        //not currently supported.
        if(user.getFilter().isNotifTypeDisabledOnUser(Constants.NTF_PAG, user.getTelephoneNumber()) == NotifState.ENABLED) { 
            String pnc = user.getPnc();
            if(pnc != null) {
                int contentStart = pnc.indexOf("+");
                int contentEnd = pnc.length();
                number = pnc.substring(0, contentStart);

                if("h".equalsIgnoreCase(pnc.substring(contentEnd - 1))) {
                    hangup = true;
                    content = pnc.substring(contentStart, contentEnd - 1);
                } else {
                    content = pnc.substring(contentStart, contentEnd);
                }
            }
        }
    }
         
    public String getNumber(){
        return number;
    }

    public String getContent(){
        return content;
    }
    
    public boolean getHangup() {
        return hangup;
    }
    
    public String toString() {
    	return "{PagerFilterInfo}";
    }
}
