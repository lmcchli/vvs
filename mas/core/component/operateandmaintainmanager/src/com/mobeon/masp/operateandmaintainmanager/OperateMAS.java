/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

package com.mobeon.masp.operateandmaintainmanager;

import static com.mobeon.masp.operateandmaintainmanager.ServiceEnabler.*;

import com.abcxyz.messaging.common.oam.FaultManager;
import com.abcxyz.messaging.common.oam.PerformanceEvent;
import com.abcxyz.messaging.common.oam.PerformanceEvent.PerfDataType;
import com.abcxyz.messaging.common.oam.PerformanceManager;
import com.abcxyz.messaging.common.oam.impl.GenericPerformanceEvent;
import com.abcxyz.services.moip.alarms.MoipAlarmEvent;
import com.abcxyz.services.moip.alarms.MoipAlarmFactory;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.configuration.IConfigurationManager;
import com.mobeon.common.configuration.ConfigurationException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is the operating class of OMM. It handles all actions that should be executed by OMManager.
 *
 */
public class OperateMAS implements Observer{
        //private GeneralAttributes genttributes;
    private boolean mas_started = false;  // gets true when MAS is started
    private ILogger log;
    private PeakConnectionCounter[] peakCounterArray= new PeakConnectionCounter[CallType.values().length];
    private LoadRegulation loadRegulation;
    private static ConcurrentHashMap<String,ConsumedServiceEntry> lstConsumedServices; // holds list of registered consumed services
    private static ConcurrentHashMap<String,ServiceEnabler> lstServiceEnablers;        // holds list of registered service enablers
    private FaultManager faultManager;
    private IConfigurationManager configManager;
    private volatile AdminState adminState = AdminState.UNLOCK;
    private volatile AdminState pendingState = adminState;
    private volatile boolean stateChangeInProgress = false;
   //only allow one state change thread at a time
    static private ReentrantLock  oneAtAtime  = new ReentrantLock(); 

    private SessionInfoFactoryImpl sessionInfoFactoryImpl;        // create factory to be able to keep track of session data
    private boolean configAlarmRaised = false;
    private static Date masStartTime;
    private ShutdownHook hook=null;

    public enum AdminState {
        LOCK ("locked"),
        UNLOCK ("unlocked"),
        SHUTDOWN ("shutdown"),
        PENDING_SHUTDOWN ("pending shutdown");

        private String info;

        AdminState(String info){
            this.info = info;
        }

        String getInfo(){
            return info;
        }
    }


    public enum ConnectionCounterNamePrefix{
        CURRENT_CONNECTION ("masCurrentConnections"),
        TOTAL_CONNECTION ("masTotalConnections");

        private String info;

        ConnectionCounterNamePrefix(String info){
            this.info = info;
        }

        String getInfo(){
            return info;
        }
    }


    public final static String PEAKCONNECTION = "masPeakConnections";
    public final static String PEAKTIMECONNECTION = "masPeakTimeConnections";
    private static final int FORCED_SHUTDOWN_TIME = 5; //Maximum time to wait when forcing a shutdown.


    // called when MAS have all functions initilized.
    public void masStarted() {
        mas_started = true;
        log.debug("MAS started, Start diagnose of services");
        this.updateStatus("operate:status");  // this forces OM to determine status of MAS

    }
    
    /* check all mas services have closed/shutdown */
    protected boolean hasMasShutdown() {
        for (ServiceEnabler se : lstServiceEnablers.values() ) {
            if (se.getServiceEnablerStatus() != ServiceEnablerStatus.CLOSE) {
                return false;
            }
        }
        return true;
    }

    class PeakConnectionCounter
    {
    	private String counterName;
    	private long counter;
    	private long peakValue;

    	public PeakConnectionCounter(String counterName)
    	{

    		this.counterName=counterName;
    		counter=0;
    		peakValue=0;
    	    setCounterValue(PEAKCONNECTION+counterName, 0);
    	    setCounterValue(PEAKTIMECONNECTION+counterName, masStartTime.getTime());
    	}


        /**
         * Increments counter by 1
         */
    	synchronized public void incrementCounter(){
            counter++;
            if (counter > peakValue) {
                peakValue = counter;
        	    setCounterValue(PEAKCONNECTION+counterName, peakValue);
        	    setCounterValue(PEAKTIMECONNECTION+counterName, Calendar.getInstance().getTime().getTime());

            }
        }

        /**
         * Decrements counter by 1
         */
    	synchronized public void decrementCounter(){
            if (counter > 0) {
                counter--;
            }
        }
    }


    /**
     * Constructor
     */
    OperateMAS(){
        log =  ILoggerFactory.getILogger(OperateMAS.class);
        lstConsumedServices = new ConcurrentHashMap<String, ConsumedServiceEntry>(20);
        lstServiceEnablers = new ConcurrentHashMap<String, ServiceEnabler>(5);
        sessionInfoFactoryImpl = new SessionInfoFactoryImpl();
        masStartTime = Calendar.getInstance().getTime();      // Time when this process starts
        
        // Register shutdown hook
        hook = new ShutdownHook(this,FORCED_SHUTDOWN_TIME);
        Runtime runtime = Runtime.getRuntime();
        runtime.addShutdownHook(hook);
        
     
        handleTermSignal.setOperateMAS(this);
        //register SIGHUP and SIGTERM, INT to shutdown via internal management interface.
        //should really be shutdown via snmp but this is for legacy reasons.
        handleTermSignal.install("TERM");
        handleTermSignal.install("HUP");
        handleTermSignal.install("INT");
              
        initializeConnectionCounters();
    }


    /**
     * Returns the sessionInfo factory
     * @return sessionInfoFactory
     */
    public SessionInfoFactory getConnectionMonitorFactory() {
        return sessionInfoFactoryImpl;
    }



    public void setLoadRegulation(LoadRegulation loadRegulation){
        this.loadRegulation = loadRegulation;
        this.loadRegulation.setOperate(this);
    }



    /**
     * sets the admin state for MAS
     * @param command
     * Can be one of:
     *<dl>
     *<dt>Locked</dt>
     *  <dd>The service is Locked</dd>
     *<dt>Unlocked</dt>
     *  <dd>The service is Unlocked. </dd>
     *<dt>Shutdown </dt>
     *  <dd>The service is not accepting new calls and is waiting for all calls to end. Then the service will go to adminstate 'Locked'</dd>
     * </dl>
     */
    public boolean setAdminState(AdminState state)
    {
        StateChanger stateChanger;
        
        if (adminState == AdminState.SHUTDOWN) {
            return true; //already shut down ignore any other state changes.
        }
        
        if (adminState == AdminState.PENDING_SHUTDOWN && state != AdminState.SHUTDOWN) {
            return true; //already shutting down ignore any other state change  except forced shutdown.
        }
        
        if (state == AdminState.PENDING_SHUTDOWN || ( state == AdminState.SHUTDOWN)) //for shutting down execute immediately.
        {
            for (ServiceEnabler se : lstServiceEnablers.values() ) {
                try {
                    switch(state)
                    {
                        case PENDING_SHUTDOWN: se.close(false); break;
                        case SHUTDOWN: se.close(true); break;
                    }
                } catch (Throwable t)
                {
                    log.error("Unable to change state of ServiceEnabler: "+se.getName()+ " to "+ state);

                }
            }
        
            adminState=state;
            return true;
        } else {
                        
            if (oneAtAtime.getHoldCount() > 1) {
                log.warn("To many pending state changes: " + oneAtAtime.getHoldCount() +", will wait for them to complete.");
                try {
                    oneAtAtime.lockInterruptibly();
                } catch (InterruptedException e) {
                    return false; // we are being forced to exit.
                } finally {
                    oneAtAtime.unlock();
                }               
            }

            synchronized (this) {
                if (stateChangeInProgress ) {
                    if (state == pendingState) {
                        log.info("MAS recieved change to state: " + state +" When already changing to that state, ignoring.");  
                        return true; //already changing
                    }                     
                } else
                {
                    if (state == adminState) {
                        log.info("MAS already in state:" + state +" when asked to change to it, ignoring.");  
                        return true;
                    }
                }
                
                stateChanger = new StateChanger(state);
                stateChangeInProgress = true;
                log.info("MAS initiated change from state: " + adminState + " to: "+ state);
                pendingState = state;
            }
            stateChanger.start();
        }

      

                 
        return true;
    }
    
    //Class to execute a state change without holding up OM..
    private class StateChanger extends Thread { 
        private AdminState toState;
        private ILogger logger = ILoggerFactory.getILogger(StateChanger.class);

        public StateChanger(AdminState state) {                
            toState=state;
        }
        public void run() {

            if (oneAtAtime.getHoldCount() > 0) {
                logger.info("Waiting for another state change to finish, before change to: " + toState);
            }
             
            try {
                oneAtAtime.lock();
                if (!toState.equals(adminState)) {
                    stateChangeInProgress = true;
                    adminState=toState;
                    for (ServiceEnabler se : lstServiceEnablers.values() ) {
                        try {
                            log.info("Changing state ServiceEnabler: "+se.getName()+ " to "+adminState);
                            switch(toState)
                            {
                                case LOCK: se.close(false); break;
                                case UNLOCK: se.open(); break;
                                case PENDING_SHUTDOWN: se.close(false); break;
                                case SHUTDOWN: se.close(true); break;
                            }
                        } catch (Exception e) {
                            log.error("Unable to change state of ServiceEnabler: "+se.getName()+ " to "+toState);
                        }
                    }
                }                
                log.info("State change executed: " +adminState);
            }finally {                   
                stateChangeInProgress = false;
                oneAtAtime.unlock(); //let any pending state changes start.                                       
            }
        }
       
    }


    /**
     * Creates a ConsumedService entry in OMM.
     * Returning the registered ConsumedService to be able to increment counters.
     * (incrementFailedOperations,incrementSuccessOperations)
     * @param serviceName
     * @param host
     * @param port
     * @return ConsumedService
     */
    public synchronized ConsumedService createConsumedServiceEntry(String serviceName, String host, Integer port) {
        ConsumedServiceEntry cs;
        String mapName = serviceName+"-"+host+"-"+port.toString();
        log.debug("register consumed service");

        if ( ! lstConsumedServices.containsKey(mapName) ) {
            cs = new ConsumedServiceEntry(serviceName,host,port);
            lstConsumedServices.put(mapName, cs);
        }
        else {
            cs = lstConsumedServices.get(mapName);
        }

        return cs;
    }

    /**
     * returns the current adminstate
     */
    public AdminState getAdminState() {
        return adminState;
    }
    

    

    /**
     * Creates a ServiceEnabler entry in OMM to make it possible for OMM to communicate with the ServiceEnabler. (start,stop,shutdown..)
     * Returns a ServiceEnablerInfo for the ServiceEnabler to be able to communicate with OMM. (Set counters..)
     * @param serviceEnablerRef
     * @return ServiceEnablerInfo
     */
    private synchronized ServiceEnablerInfo createServiceEnablerStatistics( ServiceEnablerOperate serviceEnablerRef){
        log.debug("register service enabler ["+serviceEnablerRef.toString()+"]");
        ServiceEnabler se;

        if ( ! lstServiceEnablers.containsKey(serviceEnablerRef.toString()) ) {
            se = new ServiceEnabler(this,serviceEnablerRef.toString());
            switch (adminState) {
                case LOCK:      se.setDefaultState(ServiceEnablerStatus.CLOSE); break;
                case UNLOCK:    se.setDefaultState(ServiceEnablerStatus.OPEN); break;
                case SHUTDOWN:  se.setDefaultState(ServiceEnablerStatus.CLOSE); break;
                case PENDING_SHUTDOWN:  se.setDefaultState(ServiceEnablerStatus.CLOSE); break;
            }

            se.addObserver(this);
            se.addObserver(loadRegulation);
            se.setServiceEnablerOperate(serviceEnablerRef);

            lstServiceEnablers.put(serviceEnablerRef.toString(), se);
        }
        else {
            se = lstServiceEnablers.get(serviceEnablerRef.toString());
            se.setServiceEnablerOperate(serviceEnablerRef);
        }
        return se;
    }

    /**
     * Creates a provided service entry in OMM.
     * Returning ProvidedService to be able to set status on the ProvidedService entry.
     * @param serviceName
     * @param host
     * @param port
     * @param serviceEnablerOperate
     * @return ProvidedService
     */
    public synchronized ProvidedService createProvidedServiceEntry(String serviceName, String host, int port, ServiceEnablerOperate serviceEnablerOperate) {
        ProvidedService ps;
        ServiceEnabler se;

        log.debug("Try to register provided service ["+serviceEnablerOperate.toString()+"."+serviceName+"]");
        if ( ! lstServiceEnablers.containsKey(serviceEnablerOperate.toString()) ) {
            se = new ServiceEnabler(this,serviceEnablerOperate.toString());
            switch (adminState) {
                case LOCK:      se.setDefaultState(ServiceEnablerStatus.CLOSE); break;
                case UNLOCK:    se.setDefaultState(ServiceEnablerStatus.OPEN); break;
                case SHUTDOWN:  se.setDefaultState(ServiceEnablerStatus.CLOSE); break;
                case PENDING_SHUTDOWN:  se.setDefaultState(ServiceEnablerStatus.CLOSE); break;
            }

            se.setServiceEnablerOperate(serviceEnablerOperate); // set the ServiceEnablerOperate to current ServiceEnabler
            se.addObserver(this);               // add OperateMas to listen if service enabler changes
            se.addObserver(loadRegulation);     // add LoadRegulation to listen if service enabler changes
            lstServiceEnablers.put(serviceEnablerOperate.toString(), se);
            //se.init();      // init service enabler after the setup of the service is completed.
        }
        else {
            se = lstServiceEnablers.get(serviceEnablerOperate.toString());
        }

        try
        {
            if(adminState.equals(AdminState.UNLOCK)){
                log.info("Opening service enabler: "+serviceName);
                se.open();
            }
            else
            {
                log.info("Closing service enabler: "+serviceName);
                se.close(false);
            }
        }
        catch(Exception e)
        {
            log.error("createProvidedServiceEntry unable to close or open Service enabler: "+se.getName() + " : " + e.getMessage(),e);
        }

        ps = se.createProvidedServiceEntry(serviceName,host,port,serviceEnablerOperate.getProtocol()); // Register the provided servicewithin the service enabler
        se.init(); // init the servce enabler. calls setThreshold().


        ProvidedServiceEntry pse = (ProvidedServiceEntry)ps;
        pse.addObserver(this);                  // add OperateMas to listen if provided service changes
        pse.addObserver(loadRegulation);

        return ps;
    }





     public ServiceEnablerInfo getServiceEnablerStatistics(ServiceEnablerOperate serviceEnablerRef)  {
        log.debug("get service enabler statistic ["+serviceEnablerRef.toString()+"]");
        ServiceEnablerInfo se;

        if ( lstServiceEnablers.containsKey(serviceEnablerRef.toString()) ) {
            se = lstServiceEnablers.get(serviceEnablerRef.toString());
        } else {

            se = createServiceEnablerStatistics(serviceEnablerRef);
            //throw new Exception("No Service Enabler registered with name :" +serviceEnablerRef.toString() );
        }

        // Gets the name of the protocol.
        se.setProtocol(serviceEnablerRef.getProtocol() );
        return se;
    }

     synchronized public void shutdown() {
         if (getAdminState().equals(AdminState.SHUTDOWN) || getAdminState().equals(AdminState.PENDING_SHUTDOWN)){
             return; //only initiate once..
         }

         int shutdownGracePeriod=300;
         try{
             shutdownGracePeriod = configManager.getConfiguration().getGroup(CommonOamManager.MAS_SPECIFIC_CONF).getInteger("shutdownGracePeriod",300);
         }
         catch(ConfigurationException e)
         {
             log.error("Exception when reading configuration for "+shutdownGracePeriod+ " Exception: "+e,e);
         }
         log.warn("MAS Shutdown in progress.");

         setAdminState(AdminState.PENDING_SHUTDOWN);
         log.info("will Wait shutDownGracePeriod: "+shutdownGracePeriod +" seconds for all service enablers to close and active calls to terminate, before forcing shutdown.");

         ShutDownSupervisor sds = new ShutDownSupervisor(shutdownGracePeriod,FORCED_SHUTDOWN_TIME,this);
         sds.start();
     }


    public void initializeConnectionCounters()
    {
    	setCounterValue("masStartTime",masStartTime.getTime());

        for (ConnectionCounterNamePrefix name : ConnectionCounterNamePrefix.values()) {
            for (CallType calltype : CallType.values()) {
            	setConnectionCounterValue(name,calltype,0);
            	if(name.equals(ConnectionCounterNamePrefix.TOTAL_CONNECTION))
            	{
            		setCounterValue(name.getInfo()+calltype.getCounterName()+CallResult.FAILED.getCounterName(),0);
            		setCounterValue(name.getInfo()+calltype.getCounterName()+CallResult.CONNECTED.getCounterName(),0);

            	}
                for (CallDirection calldirection : CallDirection.values()) {
                	if(!calldirection.equals(CallDirection.UNKNOWN))
                	{
                    	setConnectionCounterValue(name,calltype,calldirection,0);
                    	if(name.equals(ConnectionCounterNamePrefix.TOTAL_CONNECTION))
                    	{
                    		setCounterValue(name.getInfo()+calltype.getCounterName()+calldirection.getCounterName()+CallResult.FAILED.getCounterName(),0);
                    		setCounterValue(name.getInfo()+calltype.getCounterName()+calldirection.getCounterName()+CallResult.CONNECTED.getCounterName(),0);
                    	}
                	}

                }
            }
        }
        for (CallType calltype : CallType.values())
        {
        	peakCounterArray[calltype.ordinal()]= new PeakConnectionCounter(calltype.getCounterName());
        }
    }

	synchronized public void setConnectionCounterValue(ConnectionCounterNamePrefix name, CallType calltype,long value) {
		setCounterValue(name.getInfo()+calltype.getCounterName(),value);
    }


	synchronized public void setConnectionCounterValue(ConnectionCounterNamePrefix name, CallType calltype,CallDirection direction, long value) {

    	if(!direction.equals(CallDirection.UNKNOWN))
    	{
    		setCounterValue(name.getInfo()+calltype.getCounterName()+direction.getCounterName(),value);
    	}
    }

	synchronized public void incrementCurrentConnectionCounterValue(ConnectionCounterNamePrefix name, CallType calltype,CallDirection direction) {

		incrementCounterValue(name.getInfo()+calltype.getCounterName());
    	if(!direction.equals(CallDirection.UNKNOWN))
    	{
    		incrementCounterValue(name.getInfo()+calltype.getCounterName()+direction.getCounterName());
    	}
		peakCounterArray[calltype.ordinal()].incrementCounter();


	}
	synchronized public void decrementCurrentConnectionCounterValue(ConnectionCounterNamePrefix name, CallType calltype,CallDirection direction) {
		decrementCounterValue(name.getInfo()+calltype.getCounterName());
    	if(!direction.equals(CallDirection.UNKNOWN))
    	{
    		decrementCounterValue(name.getInfo()+calltype.getCounterName()+direction.getCounterName());
    	}
		//Check Peak
		peakCounterArray[calltype.ordinal()].decrementCounter();

	}


	synchronized public void incrementTotalConnectionCounterValue(ConnectionCounterNamePrefix name, CallType calltype,CallDirection direction,CallResult result) {

		if(result.equals(CallResult.CONNECTED)|| result.equals(CallResult.FAILED))
		{
			incrementCounterValue(name.getInfo()+calltype.getCounterName());
			incrementCounterValue(name.getInfo()+calltype.getCounterName()+result.getCounterName());
        	if(!direction.equals(CallDirection.UNKNOWN))
        	{
    			incrementCounterValue(name.getInfo()+calltype.getCounterName()+direction.getCounterName()+result.getCounterName());
    			incrementCounterValue(name.getInfo()+calltype.getCounterName()+direction.getCounterName());
        	}

		}
    }

	synchronized public void incrementTotalConnectionCounterValue(ConnectionCounterNamePrefix name, CallType calltype,CallDirection direction,CallResult result, long value) {
		if(result.equals(CallResult.CONNECTED)|| result.equals(CallResult.FAILED))
		{
			incrementCounterValue(name.getInfo()+calltype.getCounterName(),value);
			incrementCounterValue(name.getInfo()+calltype.getCounterName()+result.getCounterName(),value);
        	if(!direction.equals(CallDirection.UNKNOWN))
        	{
    			incrementCounterValue(name.getInfo()+calltype.getCounterName()+direction.getCounterName(),value);
    			incrementCounterValue(name.getInfo()+calltype.getCounterName()+direction.getCounterName()+result.getCounterName(),value);
        	}
		}
	}
	synchronized public void setCounterValue(String counterName, long value) {

        PerformanceEvent event = new GenericPerformanceEvent(counterName,PerfDataType.COUNTER);
        PerformanceManager perfManager = CommonOamManager.getInstance().getPerformanceManager();
        if (perfManager != null) {
            if (log.isDebugEnabled()) log.debug("setCounterValue counterName: "+counterName+ " value: " +value);
            perfManager.setEventValue(event, value);
        } else
            if (log.isDebugEnabled()) log.debug("setCounterValue : Performance Manager not initialized.  Counters won't be incremented.");
    }

	synchronized public void incrementCounterValue(String counterName) {

        PerformanceEvent event = new GenericPerformanceEvent(counterName,PerfDataType.COUNTER);
        PerformanceManager perfManager = CommonOamManager.getInstance().getPerformanceManager();
        if (perfManager != null) {
            if (log.isDebugEnabled()) log.debug("incrementCounterValue counterName: "+counterName);
            perfManager.markEvent(event, 1);
        } else
            if (log.isDebugEnabled()) log.debug("incrementCounterValue : Performance Manager not initialized.  Counters won't be incremented.");
    }
	synchronized public void incrementCounterValue(String counterName,long incrementBy) {

        PerformanceEvent event = new GenericPerformanceEvent(counterName,PerfDataType.COUNTER);
        PerformanceManager perfManager = CommonOamManager.getInstance().getPerformanceManager();
        if (perfManager != null) {
            if (log.isDebugEnabled()) log.debug("incrementCounterValue counterName: "+counterName+ " incrementBy: "+incrementBy);
            perfManager.markEvent(event, incrementBy);
        } else
            if (log.isDebugEnabled()) log.debug("incrementCounterValue : Performance Manager not initialized.  Counters won't be incremented.");
    }
	synchronized public void decrementCounterValue(String counterName) {

        PerformanceEvent event = new GenericPerformanceEvent(counterName,PerfDataType.COUNTER);
        PerformanceManager perfManager = CommonOamManager.getInstance().getPerformanceManager();
        if (perfManager != null) {
            if (log.isDebugEnabled()) log.debug("decrementCounterValue counterName: "+counterName);
            perfManager.markEvent(event, -1);
        } else
            if (log.isDebugEnabled()) log.debug("decrementCounterValue : Performance Manager not initialized.  Counters won't be incremented.");
    }




    public void update(Observable o, Object arg) {
        updateStatus(arg);
    }

    //private void updateStatus(Object arg, Observable o) {
    private void updateStatus(Object arg) {
        String reason = (String)arg;
        int nbServiceEnablerClose=0;
        int nbServiceEnablerOpen=0;

        // something has happend, Check mas status..
        if (mas_started)
        {
            if (reason.equals("operate:status") ) {
                log.info("Detected service enabler status changed ");
                for (ServiceEnabler se : lstServiceEnablers.values() ) {
                    /*
                     if mas is in shutdown state, monitor and go to locked when all
                     service enablers is closed
                    */
                    log.info("Service Enabler "+ se.getName()+ " status "+se.getServiceEnablerStatus());
                    switch(se.getServiceEnablerStatus())
                    {
                        case OPEN: nbServiceEnablerOpen++; break;
                        case CLOSE: nbServiceEnablerClose++; break;
                    }

                    if (log.isInfoEnabled()) log.info( se.getName()+" is " +se.getAccessStatus());
                }
                log.info("updateStatus adminState: "+adminState+ " nbServiceEnablerOpen: "+nbServiceEnablerOpen+ " nbServiceEnablerClose: "+nbServiceEnablerClose);

                if(adminState.equals(AdminState.PENDING_SHUTDOWN))
                {
                    if(nbServiceEnablerOpen==0)adminState=AdminState.SHUTDOWN;
                }
            }
        }
    }

    public void setConfigManager(IConfigurationManager configManager){
        this.configManager = configManager;
    }

    public void sendReloadConfiguration() {

        try {

            boolean result = configManager.reload();
            if (result){
                log.debug("Reload of configuration ok");
                if(configAlarmRaised) {
                	MoipAlarmEvent alarm = MoipAlarmFactory.getInstance().getAlarm(MoipAlarmFactory.MoipAlarm.CONFIG_LOADING_FAILED);
                	faultManager.clearAlarm(alarm);
                	configAlarmRaised = false;
                }
            }
            else
            {
                log.debug("Reload of configuration not ok, using backup configuration");
            }

        } catch (ConfigurationException e) {
            log.error("Reload of configuration failed");
            if(!configAlarmRaised) {
            	MoipAlarmEvent alarm = MoipAlarmFactory.getInstance().getAlarm(MoipAlarmFactory.MoipAlarm.CONFIG_LOADING_FAILED);
            	faultManager.raiseAlarm(alarm);
            	configAlarmRaised = true;
            }
        }

    }

    // when a global even ocures .
    public void reloadConfig (){
        OMMConfiguration.reloadConfig();
    }



	public void setMasFaultManager(MasFaultManager faultManager) {
		this.faultManager = faultManager;
	}


	public FaultManager getFaultManager() {
		return faultManager;
	}
}

