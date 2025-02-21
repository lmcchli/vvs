/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.operateandmaintainmanager;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;

import java.util.Observable;
import java.util.Observer;
import java.io.Serializable;


/**
 * Handles provided service entry.
 */
public class ProvidedServiceEntry extends Observable implements ProvidedService, Serializable {


    private String applicationName;
    private String applicationVersion;
    private String protocol;
    private Status status;
    private Integer port;
    private String host;
    private String serviceName;
    private ServiceEnabler attatchedToSe;
    private static ILogger log;

    /**
     * Constructor for provided service entry
     * @param serviceName
     * @param host
     * @param port
     */
    public ProvidedServiceEntry(ServiceEnabler serviceEnabler, String serviceName, String host, Integer port,String protocol) {
        log = ILoggerFactory.getILogger(ProvidedServiceEntry.class);

        status = Status.UNKNOWN;
        this.serviceName = serviceName;
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.attatchedToSe = serviceEnabler;

        log.debug("Provided service created ["+serviceName+"]");
    }

    public void addObserverListener(Observer observer){
        addObserver(observer);
    }

    /**
     * Set status for provided service
     * @param status
     */
    public void setStatus(Status status) {
        if(log.isDebugEnabled())
            log.debug("Set provided service to "+ status.toString() +" ["+serviceName+"]");
        if (this.status != status){
            this.status = status;
            if (status.equals(Status.UP)) {
                try {
                    this.attatchedToSe.open();
                } catch (Exception e) {
                    log.error("setStatus Exception: ",e);
                }
            }
            else
            {
                try {
                    this.attatchedToSe.close(false);
                } catch (Exception e) {
                    log.error("setStatus Exception: ",e);
                }
            }
        }
    }

    /**
     * Returns the status for the provided service.
     * If neither setStatus or setCurrentTime() is called before time limit expires,
     * the getAccessStatus() will return DOWN.
     * If setStatus(status) is called after getAccessStatus() returned DOWN,
     * The getAccessStatus will return the same status entered in setStatus(status) method.
     * If setCurrentTime() is called after getAccessStatus() returned DOWN,
     * The getAccessStatus will return the last status set by setStatus(status);
     * @return status of the sprovided service, UP/DOEN/IMPARED.
     */
    public Status getStatus() {
        return status;
    }

    /**
     * set application for provided service
     * @param applicationName
     * @param applicationVersion
     */
    public synchronized void setApplication(String applicationName, String applicationVersion) {
        this.applicationName = applicationName;
        this.applicationVersion  = applicationVersion;
    }




    public synchronized String getServiceName() {
        return serviceName;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getApplicationName(){
        return this.applicationName;
    }

    public String getApplicationVersion(){
        return this.applicationVersion;
    }

    public String toString(){
        return serviceName +"-"+host+"-"+port ;
    }


}
