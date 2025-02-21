package com.mobeon.masp.operateandmaintainmanager;
/*
 * Copyright (c) $today.year Mobeon AB. All Rights Reserved.
 */

public class ServiceEnablerOperateImplMock implements ServiceEnablerOperate {

    String name;
    ServiceEnabler.ServiceEnablerStatus state;
    ServiceEnablerInfo serviceInfo;
    Integer threshold;
    String protocol;

    private Supervision supervision;
    private String host;
    private String port;

    public ServiceEnablerOperateImplMock(String name){
        this.name = name;
    }

    public void setServiceInfo(ServiceEnablerInfo info) {
        serviceInfo = info;
    }

    public void setSupervision(Supervision supervision){
        this.supervision = supervision;
    }

    public synchronized void open() {
        state = ServiceEnabler.ServiceEnablerStatus.OPEN;
        serviceInfo.opened();
    }

    public void close(boolean forced) {
        // TODO make a timer that handles unforced calls.
        // On a unforced call. start a timer that sends closed after some time.
        // on forced call, send closed directly
        state = ServiceEnabler.ServiceEnablerStatus.CLOSE ;
        serviceInfo.closed();
    }

/*    public void shutdown() {
        state="Shutdown";
        try {
            ServiceEnablerInfo seInfo = supervision.getServiceEnablerStatistics(this);
            seInfo.closed(); //.shutdownComplete();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }   */

    public void updateThreshold(int highWaterMark, int lowWaterMark, int threshold) {
        this.threshold=threshold;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getProtocol() {
        return this.protocol;
    }

    public String getName() {
        return this.name;
    }

    public String toString(){
        //return "x" ;
        return name +"-"+host+"-"+port ;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
