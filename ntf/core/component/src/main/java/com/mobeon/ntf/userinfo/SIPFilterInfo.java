/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo;


import java.util.*;

public class SIPFilterInfo {

   private String[] numbers = null;
    
   
    public SIPFilterInfo(String[] numbers) {
        
        this.numbers = numbers;
    }
    
    
    public String[] getNumbers() {
        return numbers;
    }

  
    public String toString() {
	return "{SIPFilterInfo}";
    }
}
