/*
 * @(#)CallManagerLicensing 1.0 21/01/2011
 *
 * COPYRIGHT (C) ABCXYZ INTERNET APPLICATIONS INC.
 *
 * THIS SOFTWARE IS FURNISHED UNDER A LICENSE ONLY AND IS
 * PROPRIETARY TO ABCXYZ INTERNET APPLICATIONS INC. IT MAY NOT BE COPIED
 * EXCEPT WITH THE PRIOR WRITTEN PERMISSION OF ABCXYZ INTERNET APPLICATIONS
 * INC.  ANY COPY MUST INCLUDE THE ABOVE COPYRIGHT NOTICE AS
 * WELL AS THIS PARAGRAPH.  THIS SOFTWARE OR ANY OTHER COPIES
 * THEREOF, MAY NOT BE PROVIDED OR OTHERWISE MADE AVAILABLE
 * TO ANY OTHER PERSON OR ENTITY.
 * TITLE TO AND OWNERSHIP OF THIS SOFTWARE SHALL AT ALL
 * TIMES REMAIN WITH ABCXYZ INTERNET APPLICATIONS INC.
 *
 *
 */
package com.mobeon.masp.callmanager;


import java.util.Vector;

import com.abcxyz.messaging.common.data.LicenseStatus;
import com.abcxyz.messaging.common.oam.ConfigManager;
import com.abcxyz.messaging.common.oam.ConfigurationDataException;
import com.abcxyz.messaging.common.oam.FaultManager;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.oam.LogManager;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.common.oam.PerformanceManager;
import com.abcxyz.messaging.common.oam.ProfilerAgent;
import com.abcxyz.messaging.common.oam.StateManager;
import com.abcxyz.messaging.common.oam.TrafficLogAgent;
import com.abcxyz.messaging.common.udp.InitializationException;
import com.abcxyz.messaging.licensemonitor.LicenseMonitor;
import com.abcxyz.messaging.nodestatusmonitor.NodeStatusManager;
import com.abcxyz.messaging.oe.common.topology.ComponentInfo;
import com.abcxyz.messaging.oe.common.topology.SystemTopology;
import com.abcxyz.messaging.oe.common.topology.SystemTopologyInfo;
import com.abcxyz.messaging.oe.impl.bpmanagement.utils.ComponentSAUtils;
import com.abcxyz.messaging.oe.lib.OEManager;
import com.abcxyz.messaging.throttling.DistributedSizeThrottler;
import com.abcxyz.messaging.throttling.LimitCrossingHandler;
import com.mobeon.common.cmnaccess.SystemTopologyHelper;
import com.mobeon.common.cmnaccess.TopologyException;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.logging.LogAgentFactory;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.operateandmaintainmanager.OMManager;

public class CallManagerLicensingImpl implements CallManagerLicensing
{

    private class CallManagerLicensingOam implements OAMManager
    {
        OAMManager omManager;
        private final LogAgent logger =   LogAgentFactory.getLogAgent(CallManagerLicensingImpl.class);

        public CallManagerLicensingOam(OAMManager omManager)
        {
            this.omManager=omManager;
        }
        public FaultManager getFaultManager(){
            return omManager.getFaultManager();
        }

        public ConfigManager getConfigManager(){
            return omManager.getConfigManager();
        }


        public PerformanceManager getPerformanceManager(){
            return omManager.getPerformanceManager();
        }


        public LogManager getLoggingManager(){
            return omManager.getLoggingManager();
        }


        public void setLoggingManager(LogManager loggingManager){

        }

        public void setConfigManager(ConfigManager _configMgr){

        }

        public LogAgent getLogAgent(){
            return logger;
        }


        public void setLogAgent(LogAgent logger){

        }

        public void setTrafficLogAgent(TrafficLogAgent trafficLogAgent){
        }

        public TrafficLogAgent getTrafficLogAgent(){
            return omManager.getTrafficLogAgent();
        }

        public void setPerformanceManager(PerformanceManager _performanceManager){

        }

        public void setFaultManager(FaultManager _faultManager){

        }

        public void setProfilerAgent(ProfilerAgent _profiler){

        }

        public ProfilerAgent getProfilerAgent(){
            return omManager.getProfilerAgent();
        }

        public StateManager getStateManager(){
            return omManager.getStateManager();
        }

        public void setStateManager(StateManager stateManager){

        }
    }


    private CallManagerLicensingOam callManagerLicensingOam;


    private static final String VOICE_CALL_LIC = "FAT1020087";
    private static final String VIDEO_CALL_LIC = "FAT1020445";

    private static final String MAS_CURRENT_CHANNEL_LICENSES_VOICE = "masCurrentChannelLicensesVoice";
    private static final String MAS_MAX_CHANNEL_LICENSES_VOICE =     "masMaxChannelLicensesVoice";
    private static final String MAS_CURRENT_CHANNEL_LICENSES_VIDEO = "masCurrentChannelLicensesVideo";
    private static final String MAS_MAX_CHANNEL_LICENSES_VIDEO =     "masMaxChannelLicensesVideo";

    private static final String MAS_TOTAL_FAILED_NO_LICENSES_VOICE = "masTotalFailedNoLicensesVoice";
    private static final String MAS_TOTAL_FAILED_NO_LICENSES_VIDEO = "masTotalFailedNoLicensesVideo";
    private static final String LICENSE_BYPASS_PROPERTTY = "abcxyz.services.vm.mas.mlmworkaround";

    private static final String mioTenantName = System.getenv("TENANT_NAME");  // Ex: VDF1
    
    private final ILogger log = ILoggerFactory.getILogger(getClass());

    // As required for Spring Framework
    private OMManager omManager;

    private static DistributedSizeThrottler voiceCalls = null;
    private static DistributedSizeThrottler videoCalls = null;
    int opcoMaxVoice = 0;
    int opcoMaxVideo = 0;
    Boolean proxyModeEnabled = false;
    Boolean licenseBypassEnable = false;
    NodeStatusManager nodeStatusManager=null;
    LicenseStatus voiceLicStatus = null;
    LicenseMonitor licenseMonitor = null;
    LicenseStatus videoLicStatus = null;
    String multicastAddress ="";
    int serverCountInTopology=0;
    int portNum = 0;
    int remainingVoiceChannelAfterRefresh=0;
    int remainingVideoChannelAfterRefresh=0;
    long serverInstanceNumber=0;
    String operatorName="";




    public CallManagerLicensingImpl()
    {
    }

    public void setOmManager(OMManager omManager)
    {
        this.omManager = omManager;
    }

    public synchronized void init() throws ConfigurationDataException, TopologyException, InitializationException
    {

        callManagerLicensingOam = new CallManagerLicensingOam(omManager.getOamManager());
        ConfigurationReader configReader = ConfigurationReader.getInstance();

        if (configReader.getConfig().getApplicationProxyMode()) {
            log.info("Call manager is in proxy mode");
            proxyModeEnabled = true;
            return;
        }
        String licensingbypass =System.getProperty(LICENSE_BYPASS_PROPERTTY);
        if(licensingbypass!=null && licensingbypass.equalsIgnoreCase("true"))
        {
            log.warn("License Bypass Enable");
            licenseBypassEnable=true;
            return;
        }
        opcoMaxVoice = configReader.getConfig().getOpcoMaxVoiceLicence();
        opcoMaxVideo = configReader.getConfig().getOpcoMaxVideoLicence();
        multicastAddress = configReader.getConfig().getLicensingOpcoMulticastAddress();
        portNum = configReader.getConfig().getLicensingOpcoMulticastPort();
        SystemTopology topology = OEManager.getSystemTopology();
        SystemTopologyInfo topoInfo = topology.getSystemTopologyInfo();

        String componentInstanceName = ComponentSAUtils.getInstance().getComponentName();

        ComponentInfo myInfo = SystemTopologyHelper.getComponentInfo(componentInstanceName);
        serverInstanceNumber = myInfo.getId();
        Vector<ComponentInfo> tns = topoInfo.getComponentInfoPerOpcoAndType(myInfo.getOperatorName(), myInfo.getType());
        serverCountInTopology = tns.size();

        /**
         * Starting in MiO 5.0, the operatorName is taken from the environment variable
         * "TENANT_NAME". If not present, we fallback to operator Name taken from the
         * topology configuration file (which is usually the opco name).
         */
        operatorName = myInfo.getOperatorName();
        if (mioTenantName != null && !mioTenantName.isEmpty()) {
            operatorName = mioTenantName;
        }
        
        licenseMonitor = new LicenseMonitor(callManagerLicensingOam, operatorName);
        
        if(opcoMaxVoice!=0 || opcoMaxVideo!=0)
        {


            log.info("CallManagerLicensing::intconfiguration:" +
                    " serverCountInTopology: "+serverCountInTopology +
                    " operatorName: "+operatorName +
                    " serverInstanceNumber: "+serverInstanceNumber +
                    " multicastAddress: "+multicastAddress+
                    " portNum: "+portNum +
                    " opcoMaxVoice: "+opcoMaxVoice+
                    " opcoMaxVideo: "+opcoMaxVideo);

            nodeStatusManager = new NodeStatusManager(callManagerLicensingOam,
                    operatorName, Long.toString(serverInstanceNumber) ,
                        serverCountInTopology, multicastAddress, portNum);


            createVoiceThrottler(0);

            createVideoThrottler(0);


            OMManager.getOperateMAS().setCounterValue(MAS_CURRENT_CHANNEL_LICENSES_VOICE, 0);
            OMManager.getOperateMAS().setCounterValue(MAS_CURRENT_CHANNEL_LICENSES_VIDEO, 0);
            OMManager.getOperateMAS().setCounterValue(MAS_TOTAL_FAILED_NO_LICENSES_VOICE, 0);
            OMManager.getOperateMAS().setCounterValue(MAS_TOTAL_FAILED_NO_LICENSES_VIDEO, 0);



            if (log.isInfoEnabled())
                log.info("Call Manager Licensing is initialized");

        }
        else
        {
            log.warn("Call Manager licensing initialized with no voice and video license");
        }



        return;
    }

    public synchronized void refresh()
    {

        ConfigurationReader configReader = ConfigurationReader.getInstance();


        if (configReader.getConfig().getApplicationProxyMode()) {
            log.info("CallManagerLicensing::refresh Call manager is in proxy mode");
            proxyModeEnabled = true;
            return;
        }
        if(licenseBypassEnable)
        {
            log.warn("CallManagerLicensing::refresh License Bypass Enable");
            licenseBypassEnable=true;
            return;
        }
        log.info("CallManagerLicensing::refresh trying to refresh config");

        //Read new config
        int opcoMaxVoiceNew = configReader.getConfig().getOpcoMaxVoiceLicence();
        int opcoMaxVideoNew = configReader.getConfig().getOpcoMaxVideoLicence();

        //Keep current connection count
        int currentVoiceCount = 0;
        if(voiceCalls!=null)currentVoiceCount=voiceCalls.getCount();
        int currentVideoCount = 0;
        if(videoCalls!=null)currentVideoCount=videoCalls.getCount();


        if(opcoMaxVoiceNew!=0 || opcoMaxVideoNew!=0)
        {
            try
            {
                String multicastAddressNew = configReader.getConfig().getLicensingOpcoMulticastAddress();
                int portNumNew = configReader.getConfig().getLicensingOpcoMulticastPort();
                SystemTopology topology = OEManager.getSystemTopology();
                SystemTopologyInfo topoInfo = topology.getSystemTopologyInfo();

                String componentInstanceName = ComponentSAUtils.getInstance().getComponentName();

                ComponentInfo myInfo = SystemTopologyHelper.getComponentInfo(componentInstanceName);

                Vector<ComponentInfo> tns = topoInfo.getComponentInfoPerOpcoAndType(myInfo.getOperatorName(), myInfo.getType());
                int serverCountInTopologyNew = tns.size();

                /**
                 * Starting in MiO 5.0, the operatorName is taken from the environment variable
                 * "TENANT_NAME". If not present, we fallback to operator Name taken from the
                 * topology configuration file (which is usually the opco name).
                 */
                String operatorNameNew = myInfo.getOperatorName();
                if (mioTenantName != null && !mioTenantName.isEmpty()) {
                    operatorNameNew = mioTenantName;
                }
                
                log.info("CallManagerLicensing::refresh comparing configuration:" +
                        " serverCountInTopology old: "+serverCountInTopology + " new: "+serverCountInTopologyNew+
                        " operatorName old: "+operatorName + " new: "+operatorNameNew+
                        " serverInstanceNumber old: "+serverInstanceNumber + " new: "+myInfo.getId()+
                        " multicastAddress old: "+multicastAddress + " new: "+multicastAddressNew+
                        " portNum old: "+portNum + " new: "+portNumNew+
                        " opcoMaxVoice old: "+opcoMaxVoice + " new: "+opcoMaxVoiceNew+
                        " opcoMaxVideo old: "+opcoMaxVideo + " new: "+opcoMaxVideoNew+" currentVoiceCount: "+currentVoiceCount+" currentVideoCount: "+currentVideoCount);


                //Check if configuration changed
                if(serverCountInTopologyNew!=serverCountInTopology || !operatorNameNew.equals(operatorName)||
                        serverInstanceNumber!=myInfo.getId()|| multicastAddress!=multicastAddressNew ||
                        portNum!=portNumNew|| opcoMaxVoiceNew!=opcoMaxVoice || opcoMaxVideoNew!=opcoMaxVideo)
                {
                    log.info("CallManagerLicensing::refresh Congiguration changed Licensing need to be resfresh");
                    if(nodeStatusManager!=null)nodeStatusManager.shutdown();
                    licenseMonitor.releaseLicense(VOICE_CALL_LIC);
                    licenseMonitor.releaseLicense(VIDEO_CALL_LIC);



                    //Update configuration parameters
                    serverCountInTopology=serverCountInTopologyNew;
                    operatorName = operatorNameNew;
                    serverInstanceNumber=myInfo.getId();
                    multicastAddress=multicastAddressNew;
                    portNum=portNumNew;
                    opcoMaxVoice=opcoMaxVoiceNew;
                    opcoMaxVideo=opcoMaxVideoNew;
                    nodeStatusManager = new NodeStatusManager(callManagerLicensingOam,
                            operatorName, Long.toString(serverInstanceNumber) ,
                            serverCountInTopology, multicastAddress, portNum);


                    licenseMonitor.setShareId(operatorName);


                    //Create Voice Throttler
                    createVoiceThrottler(currentVoiceCount+remainingVoiceChannelAfterRefresh);

                    //Create Video Throttler
                    createVideoThrottler(currentVideoCount+remainingVideoChannelAfterRefresh);
                }
                else
                {
                    log.info("CallManagerLicensing::refresh no configuration changes detected");

                }
            }
            catch(Exception e)
            {
                OMManager.getOperateMAS().setCounterValue(MAS_MAX_CHANNEL_LICENSES_VOICE, 0);
                OMManager.getOperateMAS().setCounterValue(MAS_CURRENT_CHANNEL_LICENSES_VOICE, 0);
                OMManager.getOperateMAS().setCounterValue(MAS_MAX_CHANNEL_LICENSES_VIDEO, 0);
                OMManager.getOperateMAS().setCounterValue(MAS_CURRENT_CHANNEL_LICENSES_VIDEO, 0);


                log.error("CallManagerLicensing:refresh failled to updated callmanager licensing "+e.getMessage(),e);
                reset();
            }
        }
        else
        {
            OMManager.getOperateMAS().setCounterValue(MAS_MAX_CHANNEL_LICENSES_VOICE, 0);
            OMManager.getOperateMAS().setCounterValue(MAS_CURRENT_CHANNEL_LICENSES_VOICE, 0);
            OMManager.getOperateMAS().setCounterValue(MAS_MAX_CHANNEL_LICENSES_VIDEO, 0);
            OMManager.getOperateMAS().setCounterValue(MAS_CURRENT_CHANNEL_LICENSES_VIDEO, 0);
            log.error("CallManagerLicensing:refresh No voice or video license configured");
            reset();
        }

        return;

    }

    private void createVoiceThrottler(int currentActiveChannel) {
        if(opcoMaxVoice!=0)
        {
            log.info("Voice License Throttler creating with operatorName: "+operatorName+" opcoMaxVoice: "+opcoMaxVoice+" license: "+VOICE_CALL_LIC);

            // create license status monitoring objects
            voiceLicStatus = licenseMonitor.getLicenseStatus(VOICE_CALL_LIC, opcoMaxVoice);
            voiceCalls = new DistributedSizeThrottler("VoiceCallsThrottler_" + operatorName, voiceLicStatus,
                    nodeStatusManager, new LimitCrossingHandler(), 0,callManagerLicensingOam);
            log.info("Voice License Manager created with "+voiceCalls.getMaxValue()+" max token");
            OMManager.getOperateMAS().setCounterValue(MAS_MAX_CHANNEL_LICENSES_VOICE, voiceCalls.getMaxValue());
            if(voiceCalls.getMaxValue()!=opcoMaxVoice)log.error("Voice License Throttler: obtained "+voiceCalls.getMaxValue()+ " licenses when requested "+opcoMaxVoice+" licenses");

            if(currentActiveChannel!=0)
            {
                //Need to update actual count of the throttler
                int actualCount=0;
                while(true)
                {
                    remainingVoiceChannelAfterRefresh=0;
                    actualCount=currentActiveChannel;
                    if(actualCount>voiceCalls.getRemainingCapacity())
                    {
                        remainingVoiceChannelAfterRefresh=actualCount-voiceCalls.getRemainingCapacity();
                        actualCount=voiceCalls.getRemainingCapacity();
                    }
                    if(voiceCalls.setActualCount(actualCount)){
                        log.info("Voice License Throttler: Actual count set successfully actualCount: "+actualCount+" remainingChannel: "+remainingVoiceChannelAfterRefresh+" RemainingCapacity: "+ voiceCalls.getRemainingCapacity()+" currentActiveChannel: "+currentActiveChannel);
                        break;
                    }
                    else
                    {
                        log.warn("Voice License Throttler: Unable to set Actual count (will retry) actualCount: "+actualCount+" remainingChannel: "+remainingVoiceChannelAfterRefresh+" RemainingCapacity: "+ voiceCalls.getRemainingCapacity()+" currentActiveChannel: "+currentActiveChannel);
                    }
                }
            }
        }
        else
        {
            log.info("Call Manager licensing initialized with no voice license");
            OMManager.getOperateMAS().setCounterValue(MAS_MAX_CHANNEL_LICENSES_VOICE, 0);
            OMManager.getOperateMAS().setCounterValue(MAS_CURRENT_CHANNEL_LICENSES_VOICE, 0);
            voiceLicStatus=null;
            voiceCalls=null;

        }
    }

    private void createVideoThrottler(int currentActiveChannel) {
        if(opcoMaxVideo!=0)
        {
            log.info("VideoLicense Throttler: creating with operatorName: "+operatorName+" opcoMaxVideo: "+opcoMaxVideo+" license: "+VIDEO_CALL_LIC);

            videoLicStatus = licenseMonitor.getLicenseStatus(VIDEO_CALL_LIC, opcoMaxVideo);
            videoCalls = new DistributedSizeThrottler("VideoCallsThrottler_" + operatorName, videoLicStatus,
                                                        nodeStatusManager, new LimitCrossingHandler(), 0,callManagerLicensingOam);

            if(videoCalls.getMaxValue()!=opcoMaxVideo)log.error("VideoLicense Throttler: obtained "+videoCalls.getMaxValue()+ " licenses when requested "+opcoMaxVideo+" licenses");
            log.info("Video License Manager created with "+videoCalls.getMaxValue()+" max token");
            OMManager.getOperateMAS().setCounterValue(MAS_MAX_CHANNEL_LICENSES_VIDEO, videoCalls.getMaxValue());

            if(currentActiveChannel!=0)
            {
                //Need to update actual count of the throttler
                int actualCount=0;
                while(true)
                {
                    remainingVideoChannelAfterRefresh=0;
                    actualCount=currentActiveChannel;
                    if(actualCount>videoCalls.getRemainingCapacity())
                    {
                        remainingVideoChannelAfterRefresh=actualCount-videoCalls.getRemainingCapacity();
                        actualCount=videoCalls.getRemainingCapacity();
                    }
                    if(videoCalls.setActualCount(actualCount)){
                        log.info("Video License Manager: Actual count set successfully actualCount: "+actualCount+" remainingChannel: "+remainingVideoChannelAfterRefresh+" RemainingCapacity: "+ videoCalls.getRemainingCapacity()+" currentActiveChannel: "+currentActiveChannel);
                        break;
                    }
                    else
                    {
                        log.warn("Video License Manager: Unable to set Actual count (will retry) actualCount: "+actualCount+" remainingChannel: "+remainingVideoChannelAfterRefresh+" RemainingCapacity: "+ videoCalls.getRemainingCapacity()+" currentActiveChannel: "+currentActiveChannel);
                    }
                }
            }
        }
        else
        {
            OMManager.getOperateMAS().setCounterValue(MAS_MAX_CHANNEL_LICENSES_VIDEO, 0);
            OMManager.getOperateMAS().setCounterValue(MAS_CURRENT_CHANNEL_LICENSES_VIDEO, 0);
            log.info("Call Manager licensing initialized with no video license");
            videoLicStatus=null;
            videoCalls=null;


        }
    }


    private void reset()
    {
        if(licenseMonitor!=null)licenseMonitor.releaseLicense(VOICE_CALL_LIC);
        if(licenseMonitor!=null)licenseMonitor.releaseLicense(VIDEO_CALL_LIC);
        voiceCalls = null;
        videoCalls = null;
        opcoMaxVoice = 0;
        opcoMaxVideo = 0;
        if(nodeStatusManager!=null)nodeStatusManager.shutdown();
        voiceLicStatus = null;
        videoLicStatus = null;
        multicastAddress ="";
        operatorName = "";
        serverCountInTopology=0;
        portNum = 0;
    }



    public Boolean isLicensingEnabled()
    {
        return true;
    }

    public synchronized void addOneVoiceCall()  throws CallManagerLicensingException
    {
        if (proxyModeEnabled || licenseBypassEnable) {
            return;
        }
        if(opcoMaxVoice==0 || voiceCalls==null)
        {
            OMManager.getOperateMAS().incrementCounterValue(MAS_TOTAL_FAILED_NO_LICENSES_VOICE);
            log.warn("CallManagerLicensing::addOneVoiceCall No voice licenses configured");
            throw new  CallManagerLicensingException ("No Voice License available");
        }
        boolean ret=false;
        try{
            ret= voiceCalls.increaseOne();
        }
        catch(Exception e)
        {
            log.warn("CallManagerLicensing:addOneVoiceCall Failed to acquire license for new Voice call: "+e.getMessage(),e);
            OMManager.getOperateMAS().incrementCounterValue(MAS_TOTAL_FAILED_NO_LICENSES_VOICE);
            throw new  CallManagerLicensingException ("Failed to acquire license for Voice call");
        }

        if (!ret) {
            OMManager.getOperateMAS().incrementCounterValue(MAS_TOTAL_FAILED_NO_LICENSES_VOICE);
            log.warn("CallManagerLicensing::addOneVoiceCall Failed to acquire license for new Voice call Count=" +
                    voiceCalls.getCount() + " over " + voiceCalls.getMaxValue()+ " Remaining Capacity: "+ voiceCalls.getRemainingCapacity());
            throw new  CallManagerLicensingException ("No Voice License available");
        }
        OMManager.getOperateMAS().setCounterValue(MAS_CURRENT_CHANNEL_LICENSES_VOICE, getCurrentVoiceLicenceUsed());
        OMManager.getOperateMAS().setCounterValue(MAS_MAX_CHANNEL_LICENSES_VOICE, voiceCalls.getMaxValue());

        if (log.isInfoEnabled()) {
            log.info("CallManagerLicensing::addOneVoiceCall() voiceCalls Count=" +
                    voiceCalls.getCount() + " over " + voiceCalls.getMaxValue()+ " Remaining Capacity: "+ voiceCalls.getRemainingCapacity());
        }
        return;
   }

    public synchronized void removeOneVoiceCall()
    {
        if (proxyModeEnabled || licenseBypassEnable) {
            return;
        }
        if(opcoMaxVoice==0 || voiceCalls==null)
        {
            return;
        }

        try{
            if(remainingVoiceChannelAfterRefresh>0)
            {
                //Decrease remaining before the throttler
                remainingVoiceChannelAfterRefresh--;
                log.debug("CallManagerLicensing::removeOneVoiceCall decreasing remainingVoiceChannelAfterRefresh: "+remainingVoiceChannelAfterRefresh);

            }
            else
            {
                if (!voiceCalls.decreaseOne()) {
                    log.error("CallManagerLicensing::removeOneVoiceCall Voice Calls counter got negative value when decrementing");
                }
            }
        }
        catch(Exception e)
        {
            log.error("CallManagerLicensing::removeOneVoiceCall Error: "+e.getMessage(),e);
        }

        OMManager.getOperateMAS().setCounterValue(MAS_CURRENT_CHANNEL_LICENSES_VOICE, getCurrentVoiceLicenceUsed());
        OMManager.getOperateMAS().setCounterValue(MAS_MAX_CHANNEL_LICENSES_VOICE, voiceCalls.getMaxValue());
        if (log.isInfoEnabled()) {
            log.info("CallManagerLicensing::removeOneVoiceCall() voiceCalls Count=" +
                    voiceCalls.getCount() + " over " + voiceCalls.getMaxValue()+ " Remaining Capacity: "+ voiceCalls.getRemainingCapacity());
        }
        return;
    }

    public synchronized void addOneVideoCall() throws CallManagerLicensingException
    {
        if (proxyModeEnabled || licenseBypassEnable) {
            return;
        }
        if(opcoMaxVideo==0 || videoCalls==null)
        {
            OMManager.getOperateMAS().incrementCounterValue(MAS_TOTAL_FAILED_NO_LICENSES_VIDEO);
            log.warn("CallManagerLicensing::addOneVideoCall No video licenses configured");
            throw new  CallManagerLicensingException ("No Video License available");
        }
        boolean ret=false;
        try{
            ret= videoCalls.increaseOne();
        }
        catch(Exception e)
        {
            log.warn("CallManagerLicensing::addOneVideoCall Failed to acquire license for new Video call: "+e.getMessage(),e);
            OMManager.getOperateMAS().incrementCounterValue(MAS_TOTAL_FAILED_NO_LICENSES_VIDEO);
            throw new  CallManagerLicensingException ("Failed to acquire license for Video call");
        }

        if (!ret) {
            OMManager.getOperateMAS().incrementCounterValue(MAS_TOTAL_FAILED_NO_LICENSES_VIDEO);
            log.warn("CallManagerLicensing::addOneVideoCall Failed to acquire license for new Video call videoCalls Count=" +
                    videoCalls.getCount() + " over " + videoCalls.getMaxValue()+ " Remaining Capacity: "+ videoCalls.getRemainingCapacity());
            throw new  CallManagerLicensingException ("No Video License available");
        }

        OMManager.getOperateMAS().setCounterValue(MAS_CURRENT_CHANNEL_LICENSES_VIDEO, getCurrentVideoLicenceUsed());
        OMManager.getOperateMAS().setCounterValue(MAS_MAX_CHANNEL_LICENSES_VIDEO, videoCalls.getMaxValue());


        if (log.isInfoEnabled()) {
            log.info("CallManagerLicensing::addOneVideoCall() videoCalls Count=" +
                    videoCalls.getCount() + " over " + videoCalls.getMaxValue()+ " Remaining Capacity: "+ videoCalls.getRemainingCapacity());
        }
        return;
    }

    private int getCurrentVoiceLicenceUsed()
    {
        return voiceCalls.getCount()+remainingVoiceChannelAfterRefresh;
    }
    private int getCurrentVideoLicenceUsed()
    {
        return videoCalls.getCount()+remainingVideoChannelAfterRefresh;
    }

    public synchronized void removeOneVideoCall()
    {
        if (proxyModeEnabled || licenseBypassEnable) {
            return;
        }
        if(opcoMaxVideo==0 || videoCalls==null)
        {
            return;
        }
        try
        {
            if(remainingVideoChannelAfterRefresh>0){
                //Decrease remaining before the throttler
                remainingVideoChannelAfterRefresh--;
                log.debug("CallManagerLicensing::removeOneVideoCall decreasing remainingVideoChannelAfterRefresh: "+remainingVideoChannelAfterRefresh);
            }
            else
            {
                if (!videoCalls.decreaseOne()) {
                    log.error("CallManagerLicensing::removeOneVideoCall Video Calls counter got negative value when decrementing");
                }
            }
        }
        catch(Exception e)
        {
            log.error("CallManagerLicensing::removeOneVideoCall Error: "+e.getMessage(),e);

        }

        OMManager.getOperateMAS().setCounterValue(MAS_CURRENT_CHANNEL_LICENSES_VIDEO, getCurrentVideoLicenceUsed());
        OMManager.getOperateMAS().setCounterValue(MAS_MAX_CHANNEL_LICENSES_VIDEO, videoCalls.getMaxValue());

        if (log.isInfoEnabled()) {
            log.info("CallManagerLicensing::removeOneVideoCall() videoCalls Count=" +
                    videoCalls.getCount() + " over " + videoCalls.getMaxValue()+ " Remaining Capacity: "+ videoCalls.getRemainingCapacity());
        }
        return;
    }

}



