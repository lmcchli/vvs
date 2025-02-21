/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.wap.papapplication;

import com.mobeon.ntf.out.wap.papapi.*;
import com.mobeon.ntf.out.wap.papclients.*;
import com.mobeon.ntf.util.Logger;
import java.io.*;

/**
 * This class is the working thread of the PI server. It intercepts the push
 * request from the PPGManager, asks PAPMsg to create the PAP message needed,
 * asks the HTTPClient to create an HTTP connnection to a PPG gateway and sends
 * the PAP message to the PPG gateway.
 */
public class WapPushControl //implements Runnable
{
    private static final Logger logger = Logger.getLogger(WapPushControl.class);
    private boolean debug = false;
    private PapMsg papMsg = null;
    private WapClient ppgClient = null;
    private PapParser resultParser = null;
    private com.mobeon.ntf.out.wap.papapi.WapPerson person = null;
    private Thread pushThr = null;
    
    //IPMS Logger used for logging

    
    // is only used for test purposes
    void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    /**
     * The Pap manager calls this method to request a WAP push. The parameter list
     * must include all needed information  to submit a push message.
     * This method does not return any  value. Instead it logs the result in an event file.
     * @roseuid 3A9FC48C021D
     */
    public boolean pushRequest(com.mobeon.ntf.out.wap.papapi.WapPerson person) {
        logger.logMessage("Starting pushRequest", logger.L_DEBUG);
        
        int rsltCode = -1;
        PapMsg pushMsg = new PapMsg();
        String message = pushMsg.createPushMsg(person);
        logger.logMessage("push message = " + message, logger.L_DEBUG);
        WapClient ppgClient = new WapClient();
        PapParser resultParser = new PapParser();
        
        try {
            InputStream is = ppgClient.pushMessage(pushMsg, person);
            if (is == null) {
                logger.logMessage("result = " + IWapPush.PAP_INTERNAL_SERVER_ERROR, logger.L_ERROR);
                return false;
            }
            
            
            rsltCode = resultParser.parsePushResult(is);
            logger.logMessage("\tdeviceID = " + person.getWapDeviceID() +
            "\n\tpushID = " + person.getPushID() +
            "\n\tURI sent = " + person.getPushData() +
            "\n\tresultCode received = " + rsltCode, logger.L_DEBUG);
            is.close();
        }
        catch (IOException ioe) {
            return false;
        }
        return true;
    }
    
    /**
     * @roseuid 3A9FC48C021F
     */
    private WapPushControl() {
        String deb = System.getProperty("debug");
        if ((deb != null) && (deb.equals("true"))) {
            this.setDebug(true);
        }
        
    }
    
    public static WapPushControl createWapPushControl() {
        return new WapPushControl();
    }
    
    /**
     * main is only used as test engine for basic test of WapPushControl
     * @roseuid 3A9FC48C0220
     */
    public static void main(String args[]) {
        
    }
}
