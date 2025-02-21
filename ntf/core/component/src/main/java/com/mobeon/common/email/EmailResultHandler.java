/**
 * Copyright (c) 2007 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.email;


public interface EmailResultHandler {
    
    public void ok(int id);
    
    public void failed(int id, String errorText);
    
    public void retry(int id, String errorText);
    
}
