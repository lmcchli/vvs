/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo;


import java.util.*;

public class OdlFilterInfo {

    private boolean count = false;        
    
    private String[] numbers = null;
    
   
    public OdlFilterInfo(Properties filterInfo, String[] numbers) {
        if ("c".equals(filterInfo.getProperty("ODL"))) {
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
	return "{OdlFilterInfo}";
    }
}
