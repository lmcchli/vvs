/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo;


import java.util.*;

public class WapFilterInfo {

    
    private String[] numbers;
    
    
       
    public WapFilterInfo(String[] numbers) {
        this.numbers = numbers;
    }
    
    public String[] getNumbers() {
        return numbers;
    }
    
    
    public String toString() {
	return "{WapFilterInfo}";
    }
}
