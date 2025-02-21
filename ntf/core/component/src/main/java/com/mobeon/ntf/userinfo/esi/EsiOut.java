/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo.esi;

import com.mobeon.common.xmp.client.XmpResultHandler;
import com.mobeon.common.xmp.client.XmpClient;
import com.mobeon.common.xmp.client.XmpProtocol;
import com.mobeon.ntf.mail.EmailListHandler;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.Constants;
import java.util.Properties;

/**
 * This class contacts ESI.
 **/
public class EsiOut implements Constants{
    private final static Logger log = Logger.getLogger(EsiOut.class); 
    private static final String service = "ExternalSubscriberInformation";    
    
    private static EsiOut inst;
    
    private XmpClient client = null;


    /** Private constructor */
    private EsiOut() {
        client = XmpClient.get();
    }
    
    /**
     * Returns a reference to the one and only EsiOut object.
     * @return The EsiOut object.
     */    
    public synchronized static EsiOut get() {
        if( inst == null )
            inst = new EsiOut();
        return inst;
    }
    
    

    
    /**
     * Changes ESI attributes for a subscriber(msisdn).
     * Enter null for values that shouldnt be changed. "" means that it will be forwarded to "".
     * @param msisdn The subscriber.
     * @param cfNotReachable The number to forward not reachable calls to.
     * @param cfBusy Where to transfer busy calls.
     * @param cfNoReply Where to transfer no reply calls.
     * @param cfDivertAll Where to transfer all calls.
     * @return The status of the sending to ESI.
     */    
    public ExternalSubscriberInformation modifyEsiData(String msisdn, String cfNotReachable, String cfBusy, String cfNoReply, String cfUnconditional) {
        Properties props = new Properties();
        props.setProperty("operation", "MODIFY" );
        props.setProperty(MSISDN, msisdn );
        
        if( cfNotReachable != null )
            props.setProperty(CFNRC, cfNotReachable );
        if( cfNoReply != null )
            props.setProperty(CFNRY, cfNoReply);
        if( cfBusy != null )
            props.setProperty(CFB, cfBusy );
        if( cfUnconditional != null )
            props.setProperty(CFU, cfUnconditional );
        
        int transId = client.nextTransId();
        String request = new XmpProtocol().makeRequest(transId, service, props );
        
        EsiResultHandler resultHandler = new EsiResultHandler();
        
        
        boolean sendOk = client.sendRequest(transId, request, service, resultHandler );
        ExternalSubscriberInformation esiData = null;
        if( sendOk ) {
            resultHandler.waitForResult();
            esiData = resultHandler.getEsiData();
        } else {
            log.logMessage( "Send failed in getEsiData", log.L_DEBUG );
            esiData = new ExternalSubscriberInformation();
            esiData.setStatus( 4 );
        }
        
        return esiData;
    }
    
    /**
     * Deletes ESI parameters for a subscriber.
     * @param msisdn The subscriber
     * @param cfNotReachable Should not reachable be deleted.
     * @param cfBusy SHould busy be deleted
     * @param cfNoReply Should no reply be deleted.
     * @param cfDivertAll Should divert all be deleted.
     * @return The status of the send call.
     */    
    public ExternalSubscriberInformation deleteEsiData(String msisdn, boolean cfNotReachable, boolean cfBusy, boolean cfNoReply, boolean cfUnconditional ) {
        Properties props = new Properties();
        props.setProperty("operation", "DELETE" );
        props.setProperty(MSISDN, msisdn );
        
        if( cfNotReachable  )
            props.setProperty(CFNRC, "");
        if( cfNoReply  )
            props.setProperty(CFNRY, "");
        if( cfBusy  )
            props.setProperty(CFB, "" );
        if( cfUnconditional  )
            props.setProperty(CFU, "" );
        
        int transId = client.nextTransId();
        String request = new XmpProtocol().makeRequest(transId, service, props );
        
        EsiResultHandler resultHandler = new EsiResultHandler();
        
        boolean sendOk = client.sendRequest(transId, request, service, resultHandler );
        ExternalSubscriberInformation esiData = null;
        if( sendOk ) {
            resultHandler.waitForResult();
            esiData = resultHandler.getEsiData();
        } else {
            log.logMessage( "Send failed in getEsiData", log.L_DEBUG );
            esiData = new ExternalSubscriberInformation();
            esiData.setStatus( 4 );
        }
        
        return esiData;
     
    }
    
    
    /**
     * Same as boolean getEsiData, except it waits for the result from ESI.
     * @param msisdn The subscriber
     * @return Returns the information stored for the subscriber in HLR.
     */    
    public ExternalSubscriberInformation getEsiData(String msisdn) {
        
        Properties props = new Properties();
        props.setProperty("operation", "READ" );
        props.setProperty(MSISDN, msisdn );
        props.setProperty(CFNRC, "");
        props.setProperty(CFNRY, "");
        props.setProperty(CFB, "");
        props.setProperty(CFU, "");
        props.setProperty(LOCATION, "");
        props.setProperty(SEP, "");
        props.setProperty(HASMWI, "");
        props.setProperty(HASFLASH, "");
        props.setProperty(HASREPLACE, "");
        int transId = client.nextTransId();
            
        String request = new XmpProtocol().makeRequest(transId, service, props );
        EsiResultHandler resultHandler = new EsiResultHandler();
        
        boolean sendOk = client.sendRequest(transId, request, service, resultHandler );
        
        ExternalSubscriberInformation esiData = null;
        if( sendOk ) {
            resultHandler.waitForResult();
            esiData = resultHandler.getEsiData();
            if( esiData == null ) {
                esiData = new ExternalSubscriberInformation();
                esiData.setStatus( 4 );
            }
        } else {
            log.logMessage( "Send failed in getEsiData", log.L_DEBUG );
            esiData = new ExternalSubscriberInformation();
            esiData.setStatus( 4 );
        }
        
        return esiData;
          
    }
}
