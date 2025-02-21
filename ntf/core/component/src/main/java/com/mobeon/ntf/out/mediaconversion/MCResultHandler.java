/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.mediaconversion;

import com.mobeon.common.xmp.client.XmpResultHandler;
import com.mobeon.common.xmp.client.XmpClient;
import com.mobeon.common.xmp.client.XmpProtocol;
import com.mobeon.common.xmp.client.XmpResult;
import com.mobeon.common.xmp.XmpAttachment;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.mail.EmailListHandler;
import com.mobeon.ntf.util.Logger;

import java.util.Properties;
import java.util.ArrayList;

/**
 * This class is used for callbacks from XMP client. It is used by EsiOut for synchron handling.
 */
public class MCResultHandler implements XmpResultHandler {
   private final static Logger log = Logger.getLogger(MCResultHandler.class); 
   private MCData mcData; 
   private boolean shouldBreak = false;
   private boolean returned = false;
   
   private int waitTime = (Config.getXmpValidity() + 10) * 1000;
    
    public MCResultHandler() {
        
    }
    
    
    
    /**
     * Simply waits for the answer to come in.
     */    
    public synchronized void waitForResult() {
        try {
            if( mcData == null ) {
                wait(waitTime);
            }
            returned = true;
        } catch( Exception e ) {
            log.logMessage( "Exception in waiting for answer " + e.toString(), log.L_ERROR );
        }
    }
   
    /**
     * Call this after waitForAnswer has returned.
     * @return The MCData or null.
     */    
    public MCData getMCData() {
        shouldBreak = true;
        return mcData;
    }
   
    /**
     * Takes care of the Xmp result.
     * @param result
     */    
    public synchronized void handleResult(XmpResult result) {
        if( returned ) {
            shouldBreak = true;
            return;
        }
        
        Properties props = result.getProperties();
        mcData = new MCData();
        
        // set status ok, temporary or permanent error.
        mcData.setStatus(result.getStatusCode()/100 );
        // resource limit exceeded should be temporary
        if( result.getStatusCode() == 502 )
            mcData.setStatus( 4 );
        if( result.getStatusCode() == 200 && props != null ) {
            ArrayList attachments = result.getAttachments();
            if( attachments != null && attachments.size() > 0 ) {
                XmpAttachment attachment = (XmpAttachment) attachments.get(0);
                if( attachment != null ) {
                    mcData.setAttachment(attachment);
                }
            }
            
            String lengthStr = props.getProperty("length");
            if( lengthStr != null && lengthStr.length() > 0 ) {
                try {
                    mcData.setLength(Integer.parseInt(lengthStr));
                } catch( NumberFormatException nfe) {
                    log.logMessage("Invalid length parameter " + lengthStr + " returned", log.L_ERROR );
                }
            }
            
        }
           
        notify();
        
    }
    
}
