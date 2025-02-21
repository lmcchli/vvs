/**
/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2012.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */
package com.mobeon.ntf.management;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.abcxyz.messaging.cdrgen.CDRRecord;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.services.moip.ntf.coremgmt.NtfCmnManager;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.common.xmp.client.XmpClient;
import com.mobeon.common.xmp.client.XmpManagementHandler;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.util.NtfUtil;
import com.mobeon.common.sms.SMSClient;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.abcxyz.messaging.vvs.ntf.notifier.NotifierPluginHandler;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierStateManagement;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState.AdministrativeState;

public class ManagementInfo implements XmpManagementHandler, INotifierStateManagement{
    /* A map to store all the Consumed Service in */
    private HashMap<String, ConsumedService> d_consumedServices = null;

    /* Static final object of this class, used for singleton usage */
    private static final ManagementInfo c_inst = new ManagementInfo();

    /* Used to store data for MIB attribute ntfAdministrativeState */
    private volatile AdministrativeState d_admState = AdministrativeState.UNLOCKED;
    /* This variable is set true when NTF should exit */
    private static boolean doExit = false;

    private static LogAgent logger = NtfCmnLogger.getLogAgent(ManagementInfo.class);

    private OAMManager oamManager = null;

    private ConcurrentHashMap<Thread,Thread> threadsToManage = new ConcurrentHashMap<Thread, Thread>();
    private long checkTime = 3000; // max time to wait before waking up to check change of state

    private Executor myExecutor = Executors.newSingleThreadExecutor();

    /* Constructor - private in order to ensure SINGLETON pattern */
    private ManagementInfo() {
        try {
            d_consumedServices = new HashMap<String, ConsumedService>();

            //register SIGHUP and SIGTERM, INT to shutdown via internal management interface.
            //should really be shutdown via snmp but this is for legacy reasons.
            handleTermSignal.install("TERM");
            handleTermSignal.install("HUP");
            handleTermSignal.install("INT");

        } catch (NullPointerException ne) {
            logger.error("Null Pointer Exception: " + NtfUtil.stackTrace(ne));
        } catch (Exception e) {
            logger.error("Unknown exception creating MIB: " + e.getMessage());
        }

    }

    /**
     * Returns the one and only object of class ManagementInfo
     *@return ManagementInfo object
     */
    public static ManagementInfo get() {
        return c_inst;
    }

    /* (non-Javadoc)
     * @see com.mobeon.ntf.management.INotifierStateManagement#isAdministrativeStateUnlocked()
     */
    @Override
    public boolean isAdministrativeStateUnlocked() {
        return d_admState == AdministrativeState.UNLOCKED;
    }

    /* (non-Javadoc)
     * @see com.mobeon.ntf.management.INotifierStateManagement#isAdministrativeStateLocked()
     */
    @Override
    public boolean isAdministrativeStateLocked() { return d_admState == AdministrativeState.LOCKED; }

    /* (non-Javadoc)
     * @see com.mobeon.ntf.management.INotifierStateManagement#isAdministrativeStateShutdown()
     */
    @Override
    public boolean isAdministrativeStateShutdown() { return d_admState == AdministrativeState.SHUTDOWN || d_admState == AdministrativeState.EXIT; }


    /* (non-Javadoc)
     * @see com.mobeon.ntf.management.INotifierStateManagement#isAdministrativeStateExit()
     */
    @Override
    public boolean isAdministrativeStateExit() { return d_admState == AdministrativeState.EXIT; }

    /* (non-Javadoc)
     * @see com.mobeon.ntf.management.INotifierStateManagement#getNtfAdministrativeState()
     */
    @Override
    public AdministrativeState getNtfAdministrativeState() { return d_admState; }

    /**
     * Sets the administrative state of NTF, i.e. unlocked(1), locked(2) or shutdown(3)
     */
    public void setNtfAdministrativeState(AdministrativeState admState) {
        synchronized (this) {
            if (admState == AdministrativeState.UNLOCKED && isAdministrativeStateLocked()) {
                NtfCmnManager.getInstance().unlock(); // unlock scheduler and mrd.
                setInternalState(admState);
                setInternalState(admState);
                this.notifyAll();
            } else if (admState == AdministrativeState.SHUTDOWN && !isAdministrativeStateShutdown()) {
                setExit(); // does notify if locked.
                setInternalState(admState);
            } else if (admState == AdministrativeState.LOCKED && isAdministrativeStateUnlocked()) {
                NtfCmnManager.getInstance().lock(); // lock scheduler and mrd.
                setInternalState(admState);
            }
        }
    }

    private void setInternalState(AdministrativeState admState) {
        if (d_admState != admState) { //if state really changed..
            d_admState = admState;
            informPlugInStateChange();  
        }    
    }

    private void informPlugInStateChange() {
        Runnable inform = new Runnable() {

            @Override
            public void run(){
                INotifierNtfAdminState state =  new NotifierNtfAdminStateImpl(d_admState);
                NotifierPluginHandler.get().stateChange(state);    
            }                          
        };
        
        myExecutor.execute(inform); //run in thread so we don't delay any threads if plug-in does not return fast.
    }


    public String toString() {
        return "{ManagementInfo: " + d_admState +  " }";
    }

    /**
     * Creates a new ConsumedService based on key serviceName if not already created
     * @param serviceName - is the ConsumedService name
     */
    private ConsumedService getCreateConsumedService(String serviceName) {
        ConsumedService cService = null;

        synchronized(d_consumedServices) {
            cService = d_consumedServices.get(serviceName);
            if (cService == null) {
                cService = new ConsumedService(serviceName);
                d_consumedServices.put(serviceName, cService);
            }
        }

        return cService;
    }

    /**
     * Returns a ManagementCounter object for either successful or not successful
     *@param serviceName - is the ConsumedService name
     *@param counterType - is the type of the ManagementCounter object, either SUCCESS or FAIL
     *@return a reference to a ManagementCounter object
     */
    public ManagementCounter getCounter(String serviceName, ManagementCounter.CounterType counterType) {
        ConsumedService cService = getCreateConsumedService(serviceName);

        if (cService != null) {
            if(counterType == ManagementCounter.CounterType.SUCCESS) {
               return cService.getSuccessCounter();
            } else {
               return cService.getFailCounter();
            }
        } else {
            return null;
        }
    }

    public ManagementStatus getStatus(String serviceName, String statusName) {
        ConsumedService cService = getCreateConsumedService(serviceName);

        if (cService != null) {
            return cService.getStatus(statusName);
        } else {
            return null;
        }
    }

    /** Returns a reference to a ConsumedService object
     *@param serviceName - name of the service, e.g sms
     *@return ConsumedService object if object with serviceName exists, otherwise null;
     */
    public ConsumedService getConsumedService(String serviceName) {
        ConsumedService cService;
        synchronized(d_consumedServices) {
           cService = d_consumedServices.get(serviceName);
        }

        return cService;
    }

    /**
     * Request that NTF should exit as soon as possible.
     */
    private void setExit() {
        synchronized (this) {
            if (doExit == false) {
                doExit = true;
                if (isAdministrativeStateLocked()) {
                    NtfCmnManager.getInstance().unlock();
                    this.notifyAll();
                } // wake up threads waiting for unlock if waiting...
                setInternalState(AdministrativeState.SHUTDOWN);
                try {
                    startExitCountDown();
                } catch (Throwable i) {
                    setInternalState(AdministrativeState.EXIT);
                    try {
                        logger.error("Failed to initiate gracefull shutdown due to: ", i);
                    } catch (Throwable i2) {
                        ;
                    } // ignore...
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        // ignore;
                    }
                    System.exit(-1);
                }
            } else {
                return;
            }
        }
    }

    private synchronized void exit() {
        setInternalState(AdministrativeState.EXIT);
    }

    private void startExitCountDown() {
        new Thread("shutdown-supervisor-thread") {

            public void run() {

                long time = 30;
                try {
                    try {
                        time = Config.getShutdownTime();
                    } catch (Throwable t) {
                        logger.error("Unable to fetch Shutdown time, assuming " + time + " seconds, due to: ",t);
                    }
                    boolean interrupted = false;
                    logger.info("Waiting for max: " + time + " seconds, for all registered Threads to shutdown");
                    while (time-- > 0 && !ManagementInfo.get().isAdministrativeStateExit())
                    {
                        try
                        {
                            sleep(1000);
                            if (time % 10 == 0)
                            {
                                logger.info("Waiting for max: " + time + " seconds, for all registered Threads to shutdown");
                                ManagementInfo.get().auditRegisteredThreads();
                            }
                            if (time <= 5 && !interrupted) //when only five seconds left.
                            {
                                ManagementInfo.get().auditRegisteredThreads();
                                logger.info("Forcing shutdown, interupting remaining threads!");
                                ManagementInfo.get().interruptAllRegisteredThreads(); // force threads to start exit, timeup for clean shutdown..
                                logger.info("Waiting for max: " + time + " seconds, for all registered Threads to shutdown");
                                interrupted = true; //sent interrupt to all alive registered threads.
                            }
                        } catch (InterruptedException i) 
                        {; /*ignore interrupt as we need to monitor the shutdown, should never happen...*/}

                    }

                    NtfCmnManager.getInstance().stop(); //shutdown MRD and scheduler.

                    try {
                        XmpClient.get().stop();
                    } catch (Throwable ioe)
                    {
                        logger.warn ("ManagementInfo Could not shut down XMP client during shutdown due to ",ioe);
                    }
                    
                    logger.info("ShutdownHook() Disconnecting from SMSCs.");
                    try {
                        SMSClient smsClient = SMSClient.get();
                        if (smsClient != null) {
                            smsClient.disconnectSmsUnits();
                            sleep(5000);
                        }
                    } catch (Exception e) {
                        logger.warn ("ManagementInfo Could not disconnect from SMSCs during shutdown.");
                    }

                    logger.info("Flushing CDR's at shutdown");

                    try
                    {
                        CDRRecord cdrRecord = new CDRRecord(CommonOamManager.getInstance().getCdrGenOam());
                        cdrRecord.flushAllPendingRecords();
                    } catch (Throwable ioe)
                    {
                        logger.warn ("ManagementInfo Could not flush CDRs during shutdown due to ",ioe);
                    }

                    if (time <= 0 ) {
                        logger.error("Not all Threads shut down cleanly, exiting anyway due to timeout.");
                        ManagementInfo.get().exit();
                        sleep(1000); //Allow time for plug-in(s) to receive an EXIT event..
                        System.exit(-1);
                    }


                    //If possible allow 2 seconds for final shutdown for all threads to exit cleanly....
                    //If not sleep for a minimum half seconds to allow final exiting thread to complete...
                    if (time > 0) {
                        if (time > 2) {
                            try {sleep (2000);} catch (InterruptedException i) {;}
                            time-=2;
                        } else
                        {
                            try {sleep(time*1000);} catch (InterruptedException i) {;}
                            time --;
                        }
                    } else
                    {
                        try {sleep (500);} catch (InterruptedException i) {;}
                    }

                    logger.info("ManagmentInfo NTF shutdown execution completed with " + time + " seconds remaining.");
                    ManagementInfo.get().exit();
                    System.exit(0);

                } catch (Throwable t) {
                    try {
                        logger.error("Throwable caught during gracefull shutdown! ",t);
                        if (t instanceof Error) {
                            logger.error("Throwable is fatal, exit now...");
                            System.exit(-1); //fatal error exit now..
                        } else
                        {
                            logger.error("Non fatal trying to continue...");
                        }
                    } catch (Throwable t2)
                    { 
                        //If cannot even log just exit...
                        System.exit(-1);
                    }
                }
            }
        }.start();
    }



    /* (non-Javadoc)
     * @see com.mobeon.ntf.management.INotifierStateManagement#isExit()
     */
    @Override
    public boolean isExit() { return doExit; }

    /* (non-Javadoc)
     * @see com.mobeon.ntf.management.INotifierStateManagement#registerThread()
     */
    @Override
    public void registerThreadforShutdown() {
        Thread t = Thread.currentThread();
        synchronized (threadsToManage) {
            if ( threadsToManage.containsKey(t) == false) {
                threadsToManage.put(t, t);
            }
        }
    }

    /* (non-Javadoc)
     * @see com.mobeon.ntf.management.INotifierStateManagement#threadDone()
     */
    @Override
    public void threadDone() {
        Thread t = Thread.currentThread();
        synchronized (threadsToManage) {
            if ( threadsToManage.containsKey(t) == true) {
                threadsToManage.remove(t);
                if (threadsToManage.size() == 0 && isAdministrativeStateShutdown()) {
                    setInternalState(AdministrativeState.EXIT);
                }
            }
        }
    }


    public void statusDown(String service, String instanceName) {
        getStatus(service, instanceName).down();
    }

    public void statusUp(String service, String instanceName) {
        getStatus(service, instanceName).up();
    }

    public void sendFailed(String service) {
        getCounter(service, ManagementCounter.CounterType.FAIL).incr();
    }

    public void sendOk(String service) {
        getCounter(service, ManagementCounter.CounterType.SUCCESS).incr();
    }

    public synchronized void setOamManager(OAMManager oamManager) {
        if (this.oamManager == null) {
           this.oamManager = oamManager;
        }
    }

    public OAMManager getOamManager() {
        return this.oamManager;
    }

    protected void interruptAllRegisteredThreads() {

        try {
            synchronized (threadsToManage) {
                Set<Thread> allThreads = threadsToManage.keySet();
                logger.info("Interrupting: " + allThreads.size() + " threads.");
                synchronized (this) {
                    this.notifyAll();
                }
                for (Thread thread : allThreads) {
                    try {
                        logger.info("Interrupting: " + thread.getName());
                        thread.interrupt();
                    } catch (Throwable e2) {
                        logger.info("received exception while interrupting thread: ", e2);
                    }
                }
            }
        } catch (Throwable e) {
            logger.error("Unexpected Throwable: ", e);
        }
    }


    /**
     * Function checks if threads are alive and prunes any that have not been removed.
     * Threads should remove them self's when shutdown, but if they have crashed 
     * maybe not removed, so this is just a precaution.
     */
    protected void auditRegisteredThreads( ) {
        synchronized (threadsToManage) {
            try {
            Set<Thread> allThreads = threadsToManage.keySet();
            Iterator<Thread> iter = allThreads.iterator();
            //logger.info("Interrupting: " + allThreads.size() + " threads.");
            while ( iter.hasNext() ) {
                Thread t =  iter.next();

                if (!t.isAlive());
                {
                    logger.debug("Removing dead Thread from Managed Threads, during audit: " + t.getName());
                }
                    iter.remove();
                }
            } catch (Throwable t) {
                logger.error("Unexpected Throwable while auding threads: ",t);
            }

            if (threadsToManage.size() == 0 && isAdministrativeStateShutdown()) {
                setInternalState(AdministrativeState.EXIT);
            }

        }
    }

    /* (non-Javadoc)
     * @see com.mobeon.ntf.management.INotifierStateManagement#waitOnLock()
     */
    @Override
    public void waitOnLock() throws InterruptedException {
        synchronized  (this) {
            while (isAdministrativeStateLocked())
            {
                /* Can miss the state change due to race condition so wake up and check occasionally.
                 * Normally Should be woken on state change...
                 */
                wait(checkTime);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see com.mobeon.ntf.management.INotifierStateManagement#waitOnLock(long)
     */
    @Override
    public void waitOnLock(long millis) throws InterruptedException {
        synchronized  (this) {
            long millisRem = millis;            
            long waitTime = 0;
            if (isAdministrativeStateLocked() && (millisRem > 0)) {
                if (millisRem <= checkTime ) //check administrative state at least every check time.
                {
                    waitTime = millisRem;
                }
                else
                {
                    waitTime = checkTime;
                }   
                
                wait(millisRem); //should be woken on state change but to be sure timeout and check..
                millisRem-=waitTime;
            }       
        }
    }

    @Override
    public void shutdownNtf() {
        setNtfAdministrativeState(AdministrativeState.SHUTDOWN);       
    }
}
