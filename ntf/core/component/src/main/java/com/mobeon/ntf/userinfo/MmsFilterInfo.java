/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo;


import java.util.*;

public class MmsFilterInfo {
        
    //protected UserDevice[] notifDevices   = null;
    private String[] numbers;
    
    
    
    public MmsFilterInfo( String[] numbers ) {
        this.numbers = numbers;
    }
    
    public String[] getNumbers() {
        return numbers;
    }
    

    public String toString() {
	return "{MmsFilterInfo}";
    }
}
