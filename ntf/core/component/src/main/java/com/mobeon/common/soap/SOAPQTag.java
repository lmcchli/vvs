/*
 * SOAPQTag.java
 * Created on April 1, 2004, 3:51 PM
 */
package com.mobeon.common.soap;

/**
 * @author  ermmaha
 */
public class SOAPQTag extends SOAPTag {
    
    private String _uri;
    
    public SOAPQTag(String name) {
        super(name);
    }
    
    public SOAPQTag(String name, String uri, String value) {
        super(name, value);
        addAttribute(uri);
        _uri = uri;
    }
    
    public String getURI() {
        return _uri;
    }
}
