/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.operateandmaintainmanager;

import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;

import com.abcxyz.messaging.cdrgen.CDRRecord;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;

import java.util.Hashtable;
import java.util.Observable;
import java.io.IOException;
import java.io.Serializable;


/**
 * This class is threadsafe.
 */

public class ServiceEnabler extends Observable implements ServiceEnablerInfo, Serializable {
    private static ServiceEnablerStatus defaultAdminState;
    private static OperateMAS operateMAS;
    // Necessary to protect argument (reason) as multiple threads call notifyObservers()
    private Object observableLock = new Object();
    
    public enum ServiceEnablerStatus {
        CLOSE ("closed"),
        OPEN ("opened");

        private String info;

        ServiceEnablerStatus(String info){
            this.info = info;
        }

        String getInfo(){
            return info;
        }

    }
    //public enum operStatus {ENABLED, DISABLED,PENDING}
    public enum ServiceEnablerStatusInfo {
        SERVICE_DOWN ("Provided service is down"),
        NO_REG_SERVICE ("No provided service registered"),
        ZERO_THRESHOLD ("Threshold is zero"),
        COM_ERROR ("Communication error"),
        NO_HEARTBEAT ("No heartbeat from provided service"),
        NO_MEMORY ("Amount of free memmory is to small"),
        OK ("Ok"),
        UNKNOWN ("Unkown");

        private String info;

        // Constructor
        ServiceEnablerStatusInfo(String info){
            this.info = info;
        }

        String getInfo(){
            return info;
        }

    }
    private transient ServiceEnablerOperate serviceEnablerInterface;
    private Hashtable<String,ProvidedServiceEntry> lstProvidedServices;      // list  of provided servises for this service enabler.
    private String serviceEnablerName;       // name of this service enabler.
    private Integer maxConnections = 0;          // max connections (threshold).
    private DataSet conCurrentCounter;       // Current connections counter
    private DataSet conTotalCounter;         // Total connections counter
    private DataSet conAccumulatedCounter;   // Accumulated connections counter
    private transient ServiceEnablerStatus serviceEnablerStatus;
    private String protocol;                 // what protocol this service enabler uses.
    private transient ServiceEnablerStatusInfo statusInfo;

    private transient static ILogger log;

    public void setDefaultState(ServiceEnablerStatus state){
    	     log.debug("Default adminstate set to "+state);
             defaultAdminState = state;
    }

    /**
     * Constructor to create a ServiceEnabler.
     * @param opMas CallManagerControllerImpl 
     * @param serviceEnablerName String
     */
    public ServiceEnabler(OperateMAS opMas ,String serviceEnablerName){
        operateMAS = opMas;
        conTotalCounter = new DataSet();
        conCurrentCounter = new DataSet();
        conAccumulatedCounter = new DataSet();
        log = ILoggerFactory.getILogger(ServiceEnabler.class);
        lstProvidedServices = new Hashtable<String,ProvidedServiceEntry>(20);
        this.serviceEnablerName = serviceEnablerName;
        log.debug("Service enabler created ["+serviceEnablerName +"]");
    }

    public void init() {
        serviceEnablerStatus = defaultAdminState;   // Default as locked
        synchronized (observableLock) {
            setChanged();
            notifyObservers("serviceenabler:init");
        }
    }

    /**
     * Calculate the operational state of the ServiceEnabler.
     * @return status
     */
    public Status getAccessStatus(){

        Status status;

        status = Status.UP;
        statusInfo = ServiceEnablerStatusInfo.OK;

        if(lstProvidedServices.size() <= 0 ) {
            status = Status.DOWN;
            statusInfo = ServiceEnablerStatusInfo.NO_REG_SERVICE;
        } else if(maxConnections <= 0) {
            status = Status.DOWN;
            statusInfo = ServiceEnablerStatusInfo.ZERO_THRESHOLD;
        }
        else
        {
            for (ProvidedServiceEntry ps : lstProvidedServices.values() ) {
                if (ps.getStatus().equals(Status.DOWN)) {
                    status = Status.DOWN;
                    statusInfo = ServiceEnablerStatusInfo.SERVICE_DOWN ;
                }
            }
        }
        return status;
    }

    public ServiceEnablerStatus getServiceEnablerStatus(){
        return serviceEnablerStatus;
    }

    public ServiceEnablerStatusInfo getOperationalStateInfo(){
        return statusInfo;
    }

    public void setServiceEnablerOperate(ServiceEnablerOperate serviceEnablerOperate){
        this.serviceEnablerInterface = serviceEnablerOperate;
    }


    /**
     * Sets the threshold for the ServiceEnabler.
     * @param highWaterMark Integer
     * @param lowWaterMark Integer
     * @param threshold Integer
     */
    public void setThreshold(Integer highWaterMark, Integer lowWaterMark ,Integer threshold) throws Exception {

        if (serviceEnablerInterface != null){
            serviceEnablerInterface.updateThreshold(highWaterMark,lowWaterMark,threshold);
            log.debug("Threshold set to "+threshold + " for ["+ this.getName()+"]");
            log.debug("HigWaterMark set to "+highWaterMark + " for ["+ this.getName()+"]");
            log.debug("LowWaterMArk set to "+lowWaterMark + " for ["+ this.getName()+"]");

            if (threshold == 0)
                log.warn("Threshold set to "+threshold + " for ["+ this.getName()+"]");

            //this.threshold = threshold;
        }
        else {
            log.error("Cannot set Threshold (values " + threshold + ", " + highWaterMark + ", " + lowWaterMark +
                    ") as serviceEnablerInterface is null; throwing Exception");
            throw new Exception("No service enabler registered to this entry.");
        }

        // notify change.
        synchronized (observableLock) {
            setChanged();
            notifyObservers("operate:status");
        }
    }

    /**
     * Locks the ServiceEnabler
     * @param forced boolean
     */
    public void close(boolean forced) throws Exception {
        log.info("Trying to Close service enabler. ["+this.serviceEnablerName+"]");

        if (serviceEnablerInterface != null){
            log.info("Closing service enabler. ["+this.serviceEnablerName+"]");
            serviceEnablerInterface.close(forced);
            try
            {
                CDRRecord cdrRecord = new CDRRecord(CommonOamManager.getInstance().getCdrGenOam());
                cdrRecord.flushAllPendingRecords();
            }
            catch (IOException ioe)
            {
                log.warn ("Could not flush CDRs during shutdown / lock operation.");
            }
        }
        else {
            log.error("Cannot close service enabler as serviceEnablerInterface is null; throwing Exception");
            throw new Exception("No service enabler registered to this entry.");
        }

    }

    /**
     * Unlocks the ServiceEnabler
     */
    public void open() throws Exception {
        log.info("Try to Open service enabler. ["+this.serviceEnablerName+"]");
        if (serviceEnablerInterface != null){
            // if the intention is to unlock MAS, Then a open is allowed
            if(operateMAS.getAdminState().equals(OperateMAS.AdminState.UNLOCK) ) {
                log.info("Open service enabler. ["+this.serviceEnablerName+"]");
                serviceEnablerInterface.open();
            }
        }
        else {
            log.error("Cannot open service enabler as serviceEnablerInterface is null; throwing Exception");
            throw new Exception("No service enabler registered to this entry.");
        }


    }

    public void opened() {
        log.info("Opened. ["+serviceEnablerName+"]");
        serviceEnablerStatus = ServiceEnablerStatus.OPEN;
        synchronized (observableLock) {
            setChanged();
            notifyObservers("operate:status");
        }
    }

    public void closed() {
        log.info("Closed. ["+serviceEnablerName+"]");
        serviceEnablerStatus = ServiceEnablerStatus.CLOSE;
        synchronized (observableLock) {
            setChanged();
            notifyObservers("operate:status");
        }
    }


    /**
     * Creates a ProvidedService entry in OMM. If the entry already exsist then the already
     * registered ProvidedSerevice is reused.
     * @param serviceName String
     * @param host String
     * @param port Integer
     * @param protocol String
     * @return ProvidedServiceEntry registered in this ServiceEnabler.
     */
    public ProvidedServiceEntry createProvidedServiceEntry(String serviceName, String host, Integer port,String protocol) {

         ProvidedServiceEntry ps;

         String mapName = serviceName+"-"+host+"-"+port.toString();

        if ( ! lstProvidedServices.containsKey(mapName) ) {
            ps = new ProvidedServiceEntry(this, serviceName,host,port,protocol);
            lstProvidedServices.put(mapName, ps);
            log.debug("Provided service map entry added ["+mapName+"]");
        }
        else {
            ps = lstProvidedServices.get(mapName);
        }

        return ps;

    }

    /**
     * Returns all provided servises registered on this service enabler
     * @return provided serviece
     */
    public Hashtable<String,ProvidedServiceEntry> getProvidedServices() {
        return lstProvidedServices;
    }

    /**
     * Returns max connections that this ServiceEnabler can use.
     * @return maxConnections
     */
    public Integer getMaxConnections(){
        return maxConnections;
    }

    /**
     * Sets protocol for this ProvidedService.
     * @param protocol String
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Returns the protocol used for this ServiceEnabler.
     * @return protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Set max numbers of connections that this SE can use. (threshold)
     * @param connections Integer
     */
    public void setMaxConnections(Integer connections) {
        maxConnections = connections;
        synchronized (observableLock) {
            setChanged();
            notifyObservers("operate:status");
        }
    }

    /**
     * Set current connections currently used in this ProvidedService.
     * @param type CallType
     * @param direction CallDirection
     */
    public void incrementCurrentConnections(CallType type, CallDirection direction) {
        operateMAS.incrementCurrentConnectionCounterValue(OperateMAS.ConnectionCounterNamePrefix.CURRENT_CONNECTION, type, direction);
    }

    public void decrementCurrentConnections(CallType type, CallDirection direction) {
        operateMAS.decrementCurrentConnectionCounterValue(OperateMAS.ConnectionCounterNamePrefix.CURRENT_CONNECTION, type, direction);
    }

    /**
     * Increments connections defined by type, result and direction.
     * @param type CallType
     * @param result CallResult
     * @param direction CallDirection
     */
    public void incrementNumberOfConnections(CallType type, CallResult result, CallDirection direction) {
        operateMAS.incrementTotalConnectionCounterValue(OperateMAS.ConnectionCounterNamePrefix.TOTAL_CONNECTION, type,direction, result);
    }


    public void incrementNumberOfConnections(CallType type, CallResult result, CallDirection direction,Integer incrementBy) {
        operateMAS.incrementTotalConnectionCounterValue(OperateMAS.ConnectionCounterNamePrefix.TOTAL_CONNECTION, type,direction, result,incrementBy);
    }



    /**
     * Returns current number of connections used right now.
     * @return DataSet containing all current counters
     */
    public DataSet getCurrentCounter() {
        return conCurrentCounter;
    }

    /**
     * Returns accumulated number of connections used sinse install.
     * @return DataSet containing all accumulated counters
     */
    public DataSet getAccumulatedCounter() {
        return conAccumulatedCounter;
    }

    /**
     * Returns total number of connections used sinse last restart.
     * @return DataSet containing all total counters
     */
    public DataSet getTotalCounter() {
        return conTotalCounter;
    }

    /**
     * Returns the name of the ServiceEnabler.
     * @return name of the ServiceEnabler.
     */
    public String getName() {
        return serviceEnablerName;
    }


}
