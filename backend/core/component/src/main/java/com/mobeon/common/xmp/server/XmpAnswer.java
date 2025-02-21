/*
 * XmpAnswer.java
 *
 * Created on den 7 december 2005, 11:24
 */

package com.mobeon.common.xmp.server;

import com.mobeon.common.xmp.XmpAttachment;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Enumeration;



public class XmpAnswer implements IXmpAnswer {
    private int statusCode = 0;
    private String statusText;
    private Integer transactionId = new Integer(0);
    
    private Properties parameters;
    private ArrayList attachments = null;
    
    /** Creates a new instance of XmpAnswer */
    public XmpAnswer(int statusCode, String statusText) {
        this();
        this.statusCode = statusCode;
        this.statusText = statusText;
        
    }
    
     public XmpAnswer() {
        
        parameters = new Properties();
    }
    
    public ArrayList getAttachments() {
        return attachments;
    }
    
    public String getRspAsXml() {
	String xmlText = "";
	
	xmlText += "<status-code>" + statusCode + "</status-code>\n" +
	    "<status-text>" + statusText + "</status-text>\n";

        Enumeration parameterNames = parameters.propertyNames(); 
        while( parameterNames.hasMoreElements() ) {
            String name = (String) parameterNames.nextElement();
            String value = parameters.getProperty(name);
            if( value != null ) {
               xmlText +=  "<parameter name=\"" + name + "\">" + value + "</parameter>\n";
            }
        }
	
	return xmlText;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getStatusText() {
        return statusText;
    }
    
    public Integer getTransactionId() {
        return transactionId;
    }
    
    public void addAttachment(XmpAttachment att) {
        if( attachments == null ) {
            attachments = new ArrayList();
        }
        attachments.add(att);
    }
    
    public void addParameter(String name, String value) {
        parameters.setProperty(name.trim(), value.trim());
    }
    
    public void setTransactionId(Integer id) {
        this.transactionId = id;
    }
    
    public void setStatusCode(int code) {
        this.statusCode = code;
    }
    
    public void setStatusText(String text) {
        this.statusText = text;
    }
    
    
}
