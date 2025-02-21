/**
 * Copyright (c) 2009 Abcxyz
 * All Rights Reserved
 */

package com.abcxyz.services.moip.ntf;

import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.util.Logger;

/**
 * Class to simulate connection to a real MER system.
 */
public class TestMerAgent extends MerAgent {

    private Logger log;
    private int delivered = 0;
    private int failed = 0;
    private int expired = 0;
    private String lastResult = "";
    private String lastAdr = "";
    
    /**
     * Constructor.
     *@param mcrHost the host where the component register is.
     */
    public TestMerAgent() {
        log = Logger.getLogger();
    }

    public void notificationDelivered(String mail, int notifType) {
        log.logMessage("MER delivered: " + mail + ", " + notifType);
        delivered++;
        lastAdr= mail;
        lastResult= "delivered";
    }

    public void notificationFailed(String mail, int notifType, String msg) {
        log.logMessage("MER failed: " + mail + ", " + notifType);
        failed++;
        lastAdr= mail;
        lastResult= "failed";
    }

    public void notificationExpired(String mail, int notifType) {
        log.logMessage("MER expired: " + mail + ", " + notifType);
        expired++;
        lastAdr= mail;
        lastResult= "expired";
    }

    public String getLastAddress() {
        return lastAdr;
    }

    public String getLastResult() {
        return lastResult;
    }
    
    public int getDelivered() {
        return delivered;
    }

    public int getFailed() {
        return failed;
    }

    public int getExpired() {
        return expired;
    }
}
