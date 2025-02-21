/*
 * XmpAnswerHandler.java
 *
 * Created on den 7 december 2005, 11:22
 */

package com.mobeon.common.xmp.server;

import java.util.ArrayList;

/**
 *
 * @author  MNIFY
 */
public interface IXmpAnswer {
    
    public int getStatusCode();
    
    public String getStatusText();
    
    public Integer getTransactionId();
    
    public String getRspAsXml();
    
    public ArrayList getAttachments();
    
}
