/*
 * SOAPBody.java
 * Created on March 26, 2004, 3:26 PM
 */
package com.mobeon.common.soap;
import java.util.*;

/**
 * Represents a SOAP Body, which contains a number of different elements (recipients,
 * Date, version, etc). A special tag named method is set to indicate what kind of
 * SOAP info that is sent/received
 * @author  ermmaha
 */
public class SOAPBody {
    
    private String _nameSpaceUri;
    private String _method; //SubmitReq
    private HashMap _elements = new HashMap();
    
    public SOAPBody(String method, String nameSpaceUri) {
        _method = method;
        _nameSpaceUri = nameSpaceUri;
    }
    
    public String toString() {
        
        StringBuffer elementBuf = new StringBuffer();
        
        Iterator it = _elements.keySet().iterator();
        while(it.hasNext()) {
            String key = (String) it.next();
            Object o = _elements.get(key);
            if(o instanceof SOAPTag) {
                elementBuf.append(((SOAPTag) o).toString());
            }
        }
        
        SOAPQTag methodTag = new SOAPQTag(_method, _nameSpaceUri, elementBuf.toString());
        
        SOAPTag bodyTag = new SOAPTag("env:Body", methodTag.toString());
        
        return bodyTag.toString();
    }
    
    public void addElement(String key, SOAPTag value) {
        _elements.put(key, value);
    }
    
    public SOAPTag getElement(String key) {
        Object o = _elements.get(key);
        return (SOAPTag) o;
    }
    
    /**
     * Set the methodtag
     * @param String
     */
    public void setSOAPMethod(String method) {
        _method = method;
    }
    
    /**
     * @return String the methodtag
     */
    public String getSOAPMethod() {
        return _method;
    }
}
