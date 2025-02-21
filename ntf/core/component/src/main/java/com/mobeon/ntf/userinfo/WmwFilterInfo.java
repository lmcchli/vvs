/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo;


import com.mobeon.ntf.util.Logger;
import java.util.*;

public class WmwFilterInfo {

    private boolean count = false;
    
    private String[] numbers = null;
    
    /**
     * Constructor
     */
    public WmwFilterInfo(Properties filterInfo, String[] numbers) {
        if ("c".equals(filterInfo.getProperty("MWI"))) {
            count = true;
        }
        
        this.numbers = numbers;
    }
         
    public String[] getNumbers() {
        return numbers;
    }

    public boolean hasCount(){
        return count;
    }
    public String toString() {
	return "{WmwFilterInfo}";
    }
}
