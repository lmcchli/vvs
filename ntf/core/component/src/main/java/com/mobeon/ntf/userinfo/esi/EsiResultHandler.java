/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo.esi;

import com.mobeon.common.xmp.client.XmpResultHandler;
import com.mobeon.common.xmp.client.XmpClient;
import com.mobeon.common.xmp.client.XmpProtocol;
import com.mobeon.common.xmp.client.XmpResult;
import com.mobeon.ntf.mail.EmailListHandler;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.util.threads.NtfThread;

import java.util.Properties;

/**
 * This class is used for callbacks from XMP client. It is used by EsiOut for synchron handling.
 */
public class EsiResultHandler implements XmpResultHandler, Constants {
   private final static Logger log = Logger.getLogger(EsiResultHandler.class); 
   private ExternalSubscriberInformation esiData;
   private boolean returned = false;
    
    public EsiResultHandler() {
        //super( "EsiThreadedResultHandler" );        
    }
   
    /**
     * Simply waits for the answer to come in.
     */    
    public synchronized void waitForResult() {
        try {
            if( esiData == null ) {
                wait(1000*30);
            }
            returned = true;
        } catch( Exception e ) {
            log.logMessage( "Exception in waiting for answer " + e.toString(), log.L_ERROR );
        }
    }
   
    /**
     * Call this after waitForAnswer has returned.
     * @return The ExternalSubscriberInformation.
     */    
    public ExternalSubscriberInformation getEsiData() {
        return esiData;
    }
    
    
    
    /**
     * Takes care of the Xmp result.
     * @param result
     */    
    public synchronized void handleResult(XmpResult result) {
        
        if( returned ) {
            // result for expired request
            return;
        }
        
        Properties props = result.getProperties();
        
        esiData = new ExternalSubscriberInformation();
        
        // set status ok, temporary or permanent error.
        esiData.setStatus(result.getStatusCode()/100 );
        // resource limit exceeded should be temporary
        if( result.getStatusCode() == 502 )
            esiData.setStatus( 4 );
        if( result.getStatusCode() == 200 && props != null ) {
            esiData.setMsisdn(props.getProperty(MSISDN));
            esiData.setRoamingStatus(props.getProperty(LOCATION));
            esiData.setCfUnconditional(props.getProperty(CFU));
            esiData.setCfBusy(props.getProperty(CFB));
            esiData.setCfNoReply(props.getProperty(CFNRY));
            esiData.setCfNotReachable(props.getProperty(CFNRC));
            esiData.setSubscriberExternalPrefix(props.getProperty(SEP));
            esiData.setHasMwi(getBoolean(props,HASMWI, Config.isDefaultUserHasMwi()));
            esiData.setHasFlash(getBoolean(props, HASFLASH, Config.isDefaultUserHasFlash()));
            esiData.setHasReplace(getBoolean(props, HASREPLACE, Config.isDefaultUserHasReplace()));
        }
           
        notify();
        
    }

    private boolean getBoolean(Properties props, String key, boolean defaultValue) {
        String value = props.getProperty(key);
        if( value != null && value.equalsIgnoreCase("false")) {
            return false;
        } else if( value != null && value.equalsIgnoreCase("true")) {
            return true;
        }
        return defaultValue;
    }
}
