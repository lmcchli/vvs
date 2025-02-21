/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.mobeon.masp.callmanager;

/**
 * A container of DiversionParty related information.
 * It extends {@link com.mobeon.masp.callmanager.CallPartyDefinitions}
 * with information specific for a DiversionParty (RFC 5806).
 *
 * As a container, it provides only setters and getters.
 *
 * This class is thread safe.
 */
public class DiversionParty extends CallPartyDefinitions {

    private String hostIp = CMUtils.getInstance().getSipStackWrapper().getHost();
    private String reason = null;
    private String counter = null;
    private String limit = null;
    private String privacy = null;
    private String screen = null;
    private String extension = null;

    public DiversionParty() {
        super();
    }

    public synchronized String getHostIp() {
        return this.hostIp;
    }

    public synchronized void setHostIp(String hostIp) {
        this.hostIp = hostIp;
    }

    public synchronized String getReason() {
        return this.reason;
    }

    public synchronized void setReason(String reason) {
        this.reason = reason;
    }

    public synchronized String getCounter() {
        return this.counter;
    }

    public synchronized void setCounter(String counter) {
        this.counter = counter;
    }

    public synchronized String getLimit() {
        return this.limit;
    }

    public synchronized void setLimit(String limit) {
        this.limit = limit;
    }

    public synchronized String getPrivacy() {
        return this.privacy;
    }

    public synchronized void setPrivacy(String privacy) {
        this.privacy = privacy;
    }

    public synchronized String getScreen() {
        return this.screen;
    }

    public synchronized void setScreen(String screen) {
        this.screen = screen;
    }

    public synchronized String getExtension() {
        return this.extension;
    }

    public synchronized void setExtension(String extension) {
        this.extension = extension;
    }

    public String toString() {
        return super.toString() +
            ", hostIp: " + hostIp +  
            ", reason : " + reason +  
            ", counter : " + counter +  
            ", limit : " + limit +  
            ", privacy : " + privacy +  
            ", screen: " + screen +
            ", extention: " + extension;
    }
}
