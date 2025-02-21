package com.abcxyz.services.moip.ntf.coremgmt;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;

import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.oam.FaultManager;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.common.oam.PerformanceManager;
import com.abcxyz.messaging.mfs.MFSFactory;
import com.abcxyz.messaging.mfs.MfsConfiguration;
import com.abcxyz.messaging.mrd.client.service.MrdSocketHandler;
import com.abcxyz.messaging.mrd.oamplugin.DispatcherConfigMgr;
import com.abcxyz.messaging.mrd.operation.MsgServerOperations;
import com.abcxyz.messaging.scheduler.SchedulerConfigMgr;
import com.abcxyz.messaging.scheduler.SchedulerFactory;
import com.abcxyz.messaging.scheduler.SchedulerManager;
import com.abcxyz.messaging.scheduler.SchedulerStartFailureException;
import com.abcxyz.messaging.scheduler.handling.AppliEventHandler;
import com.abcxyz.messaging.scheduler.handling.RetryEventInfo;
import com.abcxyz.messaging.scheduler.impl.SchedulerAgentFactory;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.services.moip.ntf.coremgmt.fallback.FallbackEventHandler;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfFaultManager;
import com.abcxyz.services.moip.ntf.event.NtfEventTypes;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.slamdown.SlamdownEventHandler;
import com.mobeon.ntf.slamdown.SlamdownEventHandlerSmsInfo;
import com.mobeon.ntf.slamdown.SlamdownEventHandlerSmsType0;
import com.mobeon.ntf.slamdown.SlamdownEventHandlerSmsUnit;
import com.mobeon.ntf.out.vvm.VvmEventHandler;
import com.mobeon.ntf.out.vvm.VvmEventHandlerActivityDetected;
import com.mobeon.ntf.out.vvm.VvmEventHandlerDeactivator;
import com.mobeon.ntf.out.vvm.VvmEventHandlerSendingUnitPhoneOn;
import com.mobeon.ntf.out.vvm.VvmEventHandlerSmsInfo;
import com.mobeon.ntf.out.vvm.VvmEventHandlerWaitingPhoneOn;

/**
 * Utility class for managing common features
 *
 * @author lmchuzh
 */

public class NtfCmnManager {

    private static LogAgent logger = NtfCmnLogger.getLogAgent(NtfCmnManager.class);

    protected SchedulerManager scheduler;

    protected MrdSocketHandler mrdListener;

    protected boolean alreadyStarted;

    protected boolean mrdListenerStarted;

    protected boolean MRD_Sched_locked = false; //indicate if sched and mrd is locked or not.

    protected boolean MRD_Sched_started = false;

    protected static String ntf_scheduler_blocks = System.getProperty("abcxyz.services.moip.ntf.scheduler_blocks");

    static protected NtfCmnManager instance = new NtfCmnManager();
    static private OAMManager mrdOam;
    static private NtfFaultManager fm = new NtfFaultManager();
    static private PerformanceManager pm;

    public static NtfCmnManager getInstance() {
        return instance;
    }

    public void setFaultManager(FaultManager faultManager){
        fm.setFaultManager(faultManager);
    }

    public NtfFaultManager getFaultManager(){
        return fm;
    }

    public void setPerformanceManager(PerformanceManager performanceManager) {
        pm = performanceManager;
    }

    public PerformanceManager getPerformanceManager() {
        return pm;
    }

    /**
     * OAM already set in common OAM manager
     * @return
     */
    public OAMManager getMrdOamManager() {
        if (mrdOam == null) {
            mrdOam = CommonOamManager.getInstance().getMrdOam();
        }
        return mrdOam;
    }

    public static void resetOamManager() {
        mrdOam = null;
    }

    public static OAMManager initMrdOamManager() {
        mrdOam = CommonOamManager.getInstance().getMrdOam();

        //set NTF specific scheduler configurations
        String param = Config.getNtfEventsRootPath();
        File path = new File(param);
        if (!path.exists()) {
            path.mkdirs();
        }
        try {
            mrdOam.getConfigManager().setParameter(SchedulerConfigMgr.EventsRootPath, param);

            String instanceId;
            if (SchedulerAgentFactory.getConfiguredType() == SchedulerAgentFactory.TYPE.NOSQL) {
                instanceId = MoipMessageEntities.MESSAGE_SERVICE_NTF;
                String ntfSchedulerHttpListeningPort = mrdOam.getConfigManager().getParameter(DispatcherConfigMgr.NtfSchedulerHttpListeningPort);
                // Set the generic listening port with the value that is taken from the NTF specific listening port.
                mrdOam.getConfigManager().setParameter(DispatcherConfigMgr.SchedulerHttpListeningPort, ntfSchedulerHttpListeningPort);
            } else {
                instanceId = CommonOamManager.getInstance().getLocalInstanceIdFromTopology(MoipMessageEntities.MESSAGE_SERVICE_NTF);
            }

            mrdOam.getConfigManager().setParameter(SchedulerConfigMgr.SchedulerID, instanceId);

            String opco = CommonOamManager.getInstance().getLocalOpcoFromTopology(MoipMessageEntities.MESSAGE_SERVICE_NTF);

            ArrayList<String> backupInstanceIdList = CommonOamManager.getInstance().getBackupSchedulerList(opco, MoipMessageEntities.MESSAGE_SERVICE_NTF);

            if (backupInstanceIdList != null) {
                mrdOam.getConfigManager().setList(DispatcherConfigMgr.SchedulerList, backupInstanceIdList);
            }

        } catch (ConfigurationDataException e) {
            e.printStackTrace();
        }

        return mrdOam;
    }


    /**
     * start MRD remote listener and scheduler
     */
    public void start() throws SchedulerStartFailureException{

        if (MRD_Sched_started)
            return;

        // set NTF fault manager
        CommonOamManager.getInstance().setFaultManager(fm);

        CommonMessagingAccess.getInstance().reInitializeMfs(MfsConfiguration.getInstance());
        logger.debug("MFS is using a fault manager of type "  + MFSFactory.getOamManager().getFaultManager().getClass().getName());

        OAMManager oam = initMrdOamManager();

        startScheduler(oam);

        registerHandlers();

        MRD_Sched_started=true;
    }

    public void startMrdListener() {
        startMrdListener(CommonOamManager.getInstance().getMrdOam());
     }


    public void unlock() {
        if (MRD_Sched_locked == true ) {
            resumeMrdListener();
            resumeScheduler();
            MRD_Sched_locked = false;
        }
    }

    public void lock() {
        if ( MRD_Sched_locked == false ) {
            pauseMrdListener();
            pauseScheduler();
            MRD_Sched_locked = true;
        }
    }


    public void stop() {

        stopMrdListener();
        stopScheduler();
    }

    /**
     * @return scheduler manager
     */
    public SchedulerManager getSchedulerManager() {
        return scheduler;
    }

    /**
     * return scheduler ID
     *
     * @return
     */
    public String getSchedulerID() {
        String id = getMrdOamManager().getConfigManager().getParameter(SchedulerConfigMgr.SchedulerID);
        return id;
    }

    public boolean isMrdListenerStarted() {
        return mrdListenerStarted;
    }

    protected void startMrdListener(OAMManager oam) {

        //instantiate message service for handling MRD remote requests
        NtfMessageService msgSerivce = NtfMessageService.get();

        //retrieve configuration from NTF configuration
        int corePoolSize = Config.getServiceListenerCorePoolSize();
        int maxPoolSize = Config.getServiceListenerMaxPoolSize();
        int port = MoipMessageEntities.NTF_SERVICE_DEFAULT_PORT;

        try {
            String portAsString = oam.getConfigManager().getParameter(MoipMessageEntities.NtfMrdServicePort);
            port = Integer.valueOf(portAsString);
        } catch (NumberFormatException e) {
            logger.warn("NTF invalid messaging service port configured, set to default port <" +  MoipMessageEntities.NTF_SERVICE_DEFAULT_PORT + ">");
        }

        Hashtable<String,MsgServerOperations> myMsgServers = new Hashtable<String,MsgServerOperations>();
        myMsgServers.put(MoipMessageEntities.MESSAGE_SERVICE_NTF, msgSerivce);

        mrdListener = new MrdSocketHandler(oam, corePoolSize, maxPoolSize);
        mrdListenerStarted = mrdListener.startMultiple(myMsgServers, port, null);

    }

    protected void pauseMrdListener() {
        if (mrdListener != null) {
            mrdListener.stop();
            logger.info("NtfCmnManager, MRD locked.");
        } else
        {
            logger.error("NtfCmnManager, MRD never started, when attempting to suspend.");
        }

    }

    protected void resumeMrdListener() {
        if (mrdOam !=null) {
            startMrdListener(mrdOam);
            logger.info("NtfCmnManager, MRD unlocked.");
        } else
        {
            logger.error("NtfCmnManager, MRD never started, when attempting to resume");
        }
    }


    /**
     * start scheduler, if not already started
     */
    protected void startScheduler(final OAMManager oam) throws SchedulerStartFailureException {


        //check scheduler storage path
        String param = oam.getConfigManager().getParameter(SchedulerConfigMgr.EventsRootPath);

        if (param == null) {
            logger.error("NtfCmnManager scheduler path not defined");
            throw new SchedulerStartFailureException("scheduler events root path not defined.");
        } else {
            File path = new File(param);
            if (!path.exists()) {
                logger.info("NtfCmnManager scheduler path <" + param + "> doesn't exist, to be created");
                if (path.mkdirs() == false) {
                    if (!Boolean.getBoolean("abcxyz.messaging.scheduler.memory")) {
                        logger.error("NtfCmnManager scheduler path can not be created");
                        throw new SchedulerStartFailureException("scheduler events root path does not exist.");
                    }
                }
            }
        }

        //set open timers
        ArrayList<Long> openTimers = new ArrayList<Long>();
        if(ntf_scheduler_blocks != null) {
            String[] blocks = ntf_scheduler_blocks.split(",");

            for(String block: blocks) {
                try {
                    openTimers.add(new Long(block)*60*1000);
                } catch( NumberFormatException nfe ) {
                    logger.warn("NtfCmnManager ntf scheduler block cannot be parsed and will be ignored: " + block +
                                " [abcxyz.services.moip.ntf.scheduler_blocks = " + ntf_scheduler_blocks + "]", nfe);
                }
            }
        }

        if(openTimers.isEmpty()) {
            //use default timers
            openTimers.add(new Long(5*60*1000)); //5 minutes
            openTimers.add(new Long(60*60*1000)); //60 minutes
        }



        //check scheduler instance number
        String id = getMrdOamManager().getConfigManager().getParameter(SchedulerConfigMgr.SchedulerID);

        if (logger.isDebugEnabled()) {
            logger.debug("NtfSchedulerManager scheduler ID: " + id);
        }
        // retrieve scheduler's instance from the factory
        scheduler = SchedulerFactory.getSchedulerManager();

        try {
            scheduler.init(oam, openTimers);
            scheduler.start();
            if (logger.isDebugEnabled()) {
                logger.debug("NtfSchedulerManager scheduler start.");
            }
        }
        catch(final SchedulerStartFailureException e) {
            logger.error("NtfSchedulerManager scheduler start failed");
            if(logger.isDebugEnabled()) {
                logger.debug("NtfSchedulerManager scheduler start failed", e);
            }
            scheduler = null;
            throw e;
        }

    }

    protected void pauseScheduler() {
        if (scheduler != null) {
            scheduler.suspendAll();
            logger.info("NtfCmnManager, scheduler locked.");
        } else
        {
            logger.error("NtfCmnManager scheduler never started when attempting to suspend.");
        }
    }

    protected void  resumeScheduler() {
        if (scheduler != null) {
            scheduler.resumeAll();
            logger.info("NtfCmnManager, scheduler unlocked.");
        } else
        {
            logger.error("NtfCmnManager scheduler never started when attempting to resume.");
        }
    }

    /**
     * Stop the MRD listener.
     */
    protected void stopMrdListener() {
        if (mrdListener != null) {
            mrdListener.stop();
            logger.info("NtfCmnManager, MRD stopped.");
        }
        mrdListenerStarted = false;
    }

    /**
     * Stop the scheduler.
     */
    protected void stopScheduler() {
        if(scheduler != null) {
            scheduler.stop();
        }
    }

    protected void registerHandlers() {
        //retry schema from NTF
        RetryEventInfo retryInfo =  new RetryEventInfo(NtfEventTypes.DEFAULT_NTF.getName());
        retryInfo.setEventRetrySchema(Config.getNotifRetrySchema());
        retryInfo.setExpireTimeInMinute(Config.getNotifExpireTimeInMin());
        NtfRetryEventHandler defaultHandler = new NtfRetryEventHandler(retryInfo);

        //register event handler
        NtfEventHandlerRegistry.registerDefaultHandler(defaultHandler);  //register as persistence handler
        NtfEventHandlerRegistry.registerDefaultListener(defaultHandler); //register as event sent listener

        // Register event handlers for Outdial
        OdlEventHandler odlHandler = new OdlEventHandler();
        NtfEventHandlerRegistry.registerEventHandler(odlHandler);  //register as persistence handler
        NtfEventHandlerRegistry.registerEventSentListener(odlHandler.getEventServiceName(), odlHandler); //register as event sent listener

        // Register event handlers for Outdial - Start
        OdlEventHandlerStart odlEventHandlerStart = new OdlEventHandlerStart();
        NtfEventHandlerRegistry.registerEventHandler(odlEventHandlerStart);  //register as persistence handler
        NtfEventHandlerRegistry.registerEventSentListener(odlEventHandlerStart.getEventServiceName(), odlEventHandlerStart); //register as event sent listener

        // Register event handlers for Outdial - Login
        OdlEventHandlerLogin odlEventHandlerLogin = new OdlEventHandlerLogin();
        NtfEventHandlerRegistry.registerEventHandler(odlEventHandlerLogin);  //register as persistence handler
        NtfEventHandlerRegistry.registerEventSentListener(odlEventHandlerLogin.getEventServiceName(), odlEventHandlerLogin); //register as event sent listener

        // Register event handlers for Outdial - WaitOn
        OdlEventHandlerWaitOn odlEventHandlerWaitOn = new OdlEventHandlerWaitOn();
        NtfEventHandlerRegistry.registerEventHandler(odlEventHandlerWaitOn);  //register as persistence handler
        NtfEventHandlerRegistry.registerEventSentListener(odlEventHandlerWaitOn.getEventServiceName(), odlEventHandlerWaitOn); //register as event sent listener

        // Register event handlers for Outdial - Wait
        OdlEventHandlerWait odlEventHandlerWait = new OdlEventHandlerWait();
        NtfEventHandlerRegistry.registerEventHandler(odlEventHandlerWait);  //register as persistence handler
        NtfEventHandlerRegistry.registerEventSentListener(odlEventHandlerWait.getEventServiceName(), odlEventHandlerWait); //register as event sent listener

        // Register event handlers for Outdial - Call
        OdlEventHandlerCall odlEventHandlerCall = new OdlEventHandlerCall();
        NtfEventHandlerRegistry.registerEventHandler(odlEventHandlerCall);  //register as persistence handler
        NtfEventHandlerRegistry.registerEventSentListener(odlEventHandlerCall.getEventServiceName(), odlEventHandlerCall); //register as event sent listener

        // Register event handlers for SipMwi
        SipMwiEventHandler sipMwiHandler = new SipMwiEventHandler();
        NtfEventHandlerRegistry.registerEventHandler(sipMwiHandler);  //register as persistence handler
        NtfEventHandlerRegistry.registerEventSentListener(sipMwiHandler.getEventServiceName(), sipMwiHandler); //register as event sent listener

        // Register event handler for Fax Print
        FaxPrintEventHandler faxPrintHandler = new FaxPrintEventHandler();
        NtfEventHandlerRegistry.registerEventHandler(faxPrintHandler);  //register as persistence handler
        NtfEventHandlerRegistry.registerEventSentListener(faxPrintHandler.getEventServiceName(), faxPrintHandler); //register as event sent listener

        // Register event handler for Slamdown
        SlamdownEventHandler slamdownHandler = new SlamdownEventHandler();
        NtfEventHandlerRegistry.registerEventHandler(slamdownHandler);  //register as persistence handler
        NtfEventHandlerRegistry.registerEventSentListener(slamdownHandler.getEventServiceName(), slamdownHandler); //register as event sent listener

        // Register event handler for Slamdown SmsUnit
        SlamdownEventHandlerSmsUnit slamdownHandlerSmsUnit = new SlamdownEventHandlerSmsUnit();
        NtfEventHandlerRegistry.registerEventHandler(slamdownHandlerSmsUnit);  //register as persistence handler
        NtfEventHandlerRegistry.registerEventSentListener(slamdownHandlerSmsUnit.getEventServiceName(), slamdownHandlerSmsUnit); //register as event sent listener

        // Register event handler for Slamdown SmsType0
        SlamdownEventHandlerSmsType0 slamdownHandlerSmsType0 = new SlamdownEventHandlerSmsType0();
        NtfEventHandlerRegistry.registerEventHandler(slamdownHandlerSmsType0);  //register as persistence handler
        NtfEventHandlerRegistry.registerEventSentListener(slamdownHandlerSmsType0.getEventServiceName(), slamdownHandlerSmsType0); //register as event sent listener

        // Register event handler for Slamdown SmsInfo
        SlamdownEventHandlerSmsInfo slamdownHandlerSmsInfo = new SlamdownEventHandlerSmsInfo();
        NtfEventHandlerRegistry.registerEventHandler(slamdownHandlerSmsInfo);  //register as persistence handler
        NtfEventHandlerRegistry.registerEventSentListener(slamdownHandlerSmsInfo.getEventServiceName(), slamdownHandlerSmsInfo); //register as event sent listener

        // Register event handlers for Visual Voice Mail (VVM)
        VvmEventHandler vvmEventHandler = new VvmEventHandler();
        NtfEventHandlerRegistry.registerEventHandler(vvmEventHandler);  //register as persistence handler
        NtfEventHandlerRegistry.registerEventSentListener(vvmEventHandler.getEventServiceName(), vvmEventHandler); //register as event sent listener

        // Register event handlers for Visual Voice Mail (VVM) SmsInfo
        VvmEventHandlerSmsInfo vvmEventHandlerSmsInfo = new VvmEventHandlerSmsInfo();
        NtfEventHandlerRegistry.registerEventHandler(vvmEventHandlerSmsInfo);  //register as persistence handler
        NtfEventHandlerRegistry.registerEventSentListener(vvmEventHandlerSmsInfo.getEventServiceName(), vvmEventHandlerSmsInfo); //register as event sent listener

        // Register event handlers for Visual Voice Mail (VVM) PhoneOn SendingUnit
        VvmEventHandlerSendingUnitPhoneOn vvmEventHandlerSendingUnitPhoneOn = new VvmEventHandlerSendingUnitPhoneOn();
        NtfEventHandlerRegistry.registerEventHandler(vvmEventHandlerSendingUnitPhoneOn);  //register as persistence handler
        NtfEventHandlerRegistry.registerEventSentListener(vvmEventHandlerSendingUnitPhoneOn.getEventServiceName(), vvmEventHandlerSendingUnitPhoneOn); //register as event sent listener

        // Register event handlers for Visual Voice Mail (VVM) Waiting PhoneOn
        VvmEventHandlerWaitingPhoneOn vvmEventHandlerWaitingPhoneOn = new VvmEventHandlerWaitingPhoneOn();
        NtfEventHandlerRegistry.registerEventHandler(vvmEventHandlerWaitingPhoneOn);  //register as persistence handler
        NtfEventHandlerRegistry.registerEventSentListener(vvmEventHandlerWaitingPhoneOn.getEventServiceName(), vvmEventHandlerWaitingPhoneOn); //register as event sent listener

        // Register event handlers for Visual Voice Mail (VVM) Deactivator
        VvmEventHandlerDeactivator vvmEventHandlerDeactivator = new VvmEventHandlerDeactivator();
        NtfEventHandlerRegistry.registerEventHandler(vvmEventHandlerDeactivator);  //register as persistence handler
        NtfEventHandlerRegistry.registerEventSentListener(vvmEventHandlerDeactivator.getEventServiceName(), vvmEventHandlerDeactivator); //register as event sent listener

        // Register event handlers for Visual Voice Mail (VVM) Activity Detected
        VvmEventHandlerActivityDetected vvmEventHandlerActivityDetected = new VvmEventHandlerActivityDetected();
        NtfEventHandlerRegistry.registerEventHandler(vvmEventHandlerActivityDetected);  //register as persistence handler
        NtfEventHandlerRegistry.registerEventSentListener(vvmEventHandlerActivityDetected.getEventServiceName(), vvmEventHandlerActivityDetected); //register as event sent listener


        // Register event handlers for Fallbak mechanism
        FallbackEventHandler fallbackEventHandler = new FallbackEventHandler();
        NtfEventHandlerRegistry.registerEventHandler(fallbackEventHandler);  //register as persistence handler
        NtfEventHandlerRegistry.registerEventSentListener(fallbackEventHandler.getEventServiceName(), fallbackEventHandler); //register as event sent listener

    }
}
