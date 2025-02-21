package com.abcxyz.services.moip.ntf.coremgmt.fallback;

/**
 * Container class for all the info that can be attached to a Fallback event at run-time.
 */
public class FallbackInfo {
    private String content;
        
    public FallbackInfo(String content) {       
        this.content = content;
    }
    
    public String getContent() {
        return content;
    }    
    
}
