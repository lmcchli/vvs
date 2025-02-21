/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2010.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */

package com.mobeon.ntf.phonedetection;


import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Random;

import com.abcxyz.messaging.common.hlr.HlrAccessManager;
import com.abcxyz.messaging.common.mnr.SubscriberInfo;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.oam.OAMManager;
import com.abcxyz.messaging.common.ssmg.AnyTimeInterrogationResult;
import com.abcxyz.messaging.common.ssmg.interfaces.AlertSCHandler;
import com.abcxyz.messaging.common.util.cache.LifeCache;
import com.abcxyz.services.moip.common.ss7.ISs7Manager;
import com.abcxyz.services.moip.common.ss7.Ss7Exception;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.cmnaccess.oam.ConfigParam;
import com.mobeon.common.cmnaccess.oam.MoipOamManager;
import com.mobeon.common.configuration.ConfigurationException;
import com.mobeon.common.configuration.ConfigurationImpl;
import com.mobeon.common.configuration.IConfiguration;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.phonedetection.PhoneStatus;
import com.mobeon.ntf.util.NtfUtil;



/**
 * This class maintains the phone status of a subscribers.
 * It contains a cache of 1000 subs with a life period of 60 seconds 
 *
 * @author lmcmmaz
 */
public class PhoneStatus {
    
    private static final int CACHE_SIZE = 1000;
    private static final int CACHE_LIFE_SECONDS = 60;
    
  
   
    //Parameters used to simulate SS7/HLR access when not available in lab.
    private static String simulateHLRMethod=System.getProperties().getProperty("com.mobeon.ntf.phonedetection.PhoneStatus.simulateHLRMethod");
    private static boolean simulateHLR = (simulateHLRMethod!=null && !simulateHLRMethod.equals(""));
    private static final int SIMULATE_RAMDOMEERR_FREQ = 5; // 1 in FREQ simulate an error if using RANDOMERROR with simulate HLR.
    private static final int SIMULATE_RAMDOM_FREQ=5; //1 in 5 frequency to generate phone off and or roaming
    private static final long SIMULATE_DELAY=50; //50 ms delay doing the query..
    private static Random rand = new Random(System.currentTimeMillis());
    
    private static HashMap<String, Boolean> simulatedOnce = new LinkedHashMap<String, Boolean>(); //first time this number is queried when simulating HLR.
         
    private static LogAgent log = NtfCmnLogger.getLogAgent(PhoneStatus.class);
    
    //own version of lie cache that extends the life of the cached object by a short guard time after a fetch,
//TODO need to change base class to have itemContainer protected.
//    private class PsLifeCache extends LifeCache {
//        
//        private static final int CACHE_GUARD_MS = 10;
//        
//        public PsLifeCache(int initialCapacity) {
//            super(initialCapacity);
//        }
//
//        public PsLifeCache(int initialCapacity, int itemsLifeTime) {
//            super(initialCapacity,itemsLifeTime);
//        }
//
//        public synchronized Object get(Object key) {
//            Object o = super.get(key);
//            ItemContainer itemContainer = (ItemContainer) o;
//
//            if (itemContainer != null) {
//                if (itemContainer.expiryTime < System.currentTimeMillis()) {
//                    super.remove(key);
//                    return null;
//                } else  {
//                    //allow a short additional guard time so if another fetch of this item it gets the same item.
//                    //Minimises the chance of a race condition.
//                    itemContainer.expiryTime+=CACHE_GUARD_MS;
//                    return itemContainer.item;
//                }
//            }
//
//            return null;
//        }
//    }
    
//    static private PsLifeCache cache = new PsLifeCache(
//            CACHE_SIZE, //initialCapacity
//            CACHE_LIFE_SECONDS //seconds
//            );
    
    static private LifeCache cache = new LifeCache(
            CACHE_SIZE, //initialCapacity
            CACHE_LIFE_SECONDS //seconds
            );
    
    public enum State {
        YES,  // Means positive answer: is roaming or phone is on 
        NO,   // Means negative answer: not roaming or phone is off
        NONE, // Means not initialised or unknown (some types of HLR query do not support all types)
        ERROR // Means unable to query as an error occurred.
        };
    
    private String phoneNumber;
    private State roaming = State.NONE;
    private State phoneOn = State.NONE;
   
    

    

    private PhoneStatus(String phoneNumber){
        this.phoneNumber = phoneNumber;
        cache.put(phoneNumber, this);
    }
    
    public static PhoneStatus getPhoneStatus(String phoneNumber){
        log.debug("PhoneStatus.getPhoneStatus() for " + phoneNumber);
        PhoneStatus aPhoneStatus = (PhoneStatus)cache.get(phoneNumber);
        if(aPhoneStatus != null){
            log.debug("PhoneStatus.getPhoneStatus() " + phoneNumber + " found in cache: " + aPhoneStatus.toString());
        } else {
            log.debug("PhoneStatus.getPhoneStatus() " + phoneNumber + " not in cache. Create a new");
            aPhoneStatus = new PhoneStatus(phoneNumber);
        }
        return aPhoneStatus;
    }
    
    protected static void clear(){
        cache.clear();
    }

    protected static int size(){
        return cache.size();
    }
    
    public State isRoaming(){
        log.debug("PhoneStatus.isRoaming(): " + roaming.toString());
        if(roaming  == PhoneStatus.State.NONE) {  //if never queried
            if (CommonMessagingAccess.getInstance().getSs7Manager().useHlr() == true) {
                log.debug("PhoneStatus.isRoaming() - getting the status from HLR");
                queryForPhoneStatus();
                log.debug("PhoneStatus.isRoaming() set to " + roaming.toString());
            }
        }
        return roaming;
    }

    public void setRoaming(State state){
        log.debug("PhoneStatus.setRoaming(): " + state.toString());
        roaming = state;
    }


    public State isPhoneOn(){
        log.debug("PhoneStatus.isPhoneOn(): " + phoneOn.toString());
        if(phoneOn  == PhoneStatus.State.NONE)  { //if never queried
            // Check for phoneOn via SMS type 0 if not set, then query HLR.
            if(Config.getPhoneOnMethod().equalsIgnoreCase(Config.PHONE_ON_SMS_TYPE_0)){
                // Do nothing, return State.NONE. This method should should be used for status check to hlr
            } else if(Config.getPhoneOnMethod().equalsIgnoreCase(Config.PHONE_ON_ALERT_SC)) { 
                log.debug("PhoneStatus.isPhoneOn() - getting the status from SS7Manager");
                queryForPhoneStatus();
                log.debug("PhoneStatus.isPhoneOn() set to " + phoneOn.toString());                
            } else {
                // well, we have a serious problem we need to know the phone status but 
                // the configuration does not specify the way to do it...
                log.debug("PhoneStatus.isPhoneOn() set to " + phoneOn.toString() + 
                                     " since Cm.phoneOnMethod does not specify a method to use.");
            }
        }
        return phoneOn;
    }

    public void setPhoneOn(State state){
        log.debug("PhoneStatus.setPhoneOn(): " + state.toString());
        phoneOn = state;
    }
    
    /**
     * Method queryForPhoneStatus will set both the roaming and phoneOn statuses of the subscriber
     * with a single request to the HLR if the information is available.
     * Depending on the configuration, the query will be made with either an AnyTimeInterrogation,
     * SendRoutingInfoForShortMessage (SRIforSM) or custom (plug-in) 
     * 
     * Takes no argument and return nothing since it directly updates internal variables of the object.
     */
    private void queryForPhoneStatus() {

        
        //for testing in lab when no HLR
        if (simulateHLR==true) {
            simulateQueryForPhoneStatus();return;
        }
        
        String method = CommonMessagingAccess.getInstance().getSs7Manager().getSubStatusHlrInterrogationMethod();

        if (method == null) {
            log.error("queryForPhoneStatus: cannot dertermine  StatusHlrInterrogationMethod ");
            return;
        }
        if(method.equalsIgnoreCase(ConfigParam.SUB_STATUS_HLR_METHOD_ENUM_ATI)) {
            // Perform an Any Time Interrogation to get subscriber roaming and phoneOn status
            AnyTimeInterrogationResult result;
            try {
                result = CommonMessagingAccess.getInstance().getSs7Manager().requestATI(phoneNumber);

                if (result.isError()) {
                    // Should the statuses be defaulted to roaming=yes and phoneon=no to be safe?
                    // This is a cache after all and if there is a connection problem, we do not want
                    // to bombard the HLR with potentially many retries in the next minute.

                    log.debug("PhoneStatus ATI request received an error response. Error code: " + 
                                          result.getATIErrorCode() + ", Error msg: " + result.getATIErrorMessage());
                    roaming = State.ERROR;
                    phoneOn = State.ERROR;
                    return;
                }

                roaming = CommonMessagingAccess.getInstance().getSs7Manager().isRoaming_ATI(result) ?  State.YES : State.NO;

                phoneOn = (result.getSubscriberState() == AnyTimeInterrogationResult.SUBSCRIBER_STATE.NET_DET_NOT_REACHABLE
                           && result.getNotReachableReason().equals(AnyTimeInterrogationResult.NOT_REACHABLE_REASON.IMSI_DETACHED)) ?
                           State.NO : State.YES;
            } catch (Ss7Exception e) {
                log.warn("queryForPhoneStatus: Ss7Exception while processing ATI request for " + phoneNumber, e);
                roaming = State.ERROR;
                phoneOn = State.ERROR;
            }

            // Perform a Send Routing Information for Short Message (SRIforSM) to get subscriber roaming and phoneOn status
        } else if(method.equalsIgnoreCase(ConfigParam.SUB_STATUS_HLR_METHOD_ENUM_SRIFORSM)) {
            SubscriberInfo subInfo;
            try {
                subInfo = CommonMessagingAccess.getInstance().getSs7Manager().getSubscriberInfo(phoneNumber);

                if (subInfo != null) {
                    roaming = (subInfo.getRoamingStatus() == SubscriberInfo.ROAMING) ? State.YES : State.NO;
                    phoneOn = subInfo.getSubscriberStatus() ? State.YES : State.NO;
                } else {
                    roaming = State.ERROR;
                    phoneOn = State.ERROR;
                }
            } catch (Ss7Exception e) {
                log.warn("queryForPhoneStatus: Ss7Exception while processing SRI-for-SM request for " + phoneNumber, e);
                roaming = State.ERROR;
                phoneOn = State.ERROR;
            }
        } else if (method.equalsIgnoreCase(ConfigParam.SUB_STATUS_HLR_METHOD_ENUM_CUSTOM)) { // VFE_NL MFD
            //currently only supports roaming check.
            log.debug("PhoneStatus.queryForPhoneStatus() HLR Access method = custom; to use HlrAccessManager");
            HlrAccessManager ham = CommonMessagingAccess.getInstance().getHlrAccessManager();
            if (ham != null) {
                try {
                    int roamingStat = ham.getSubscriberRoamingStatus(phoneNumber);                                
                    roaming = (roamingStat == HlrAccessManager.SUBSCRIBER_ROAMING_STATUS_ROAMING) ? State.YES : State.NO;
                    if(log.isDebugEnabled()) log.debug("Info from HlrAccessManager roaming = " + roaming);
                } catch (Exception e) {
                    log.error("Exception calling HlrAccessManager.getSubscriberRoamingStatus() for " + phoneNumber + " ",e);
                    roaming = State.ERROR;
                }
            } else {
                log.error("CommonMessagingAccess.getInstance().getHlrAccessManager() returned null for " + phoneNumber);
                roaming = State.ERROR;
            }
        }
    }
    
    /*
     * Function to simulate HLR status for phone
     * NOTE: in backend configure subscriberStatusHlrMethod to CUSTOM and
     * Cm.enableHlrAccess to true.
     * use -Dcom.mobeon.ntf.phonedetection.PhoneStatus.simulateHLRMethod="VALUE,.. on java command line."
     * where VALUE=
     * RANDOM=<freq> - return randomly phone on off roaming yes no (exclusive) - 1 in freq
     * HOME - roaming = no (home network)
     * ROAM - roaming = yes (foreign network)
     * PHONEON - Phone on 
     * PHONEOFF = Phone off.
     * ERROR = FAILS to fetch HLR status. 
     * FIRSTERR = First time only for this cached item simulate an error.
     * RANDERR=<freq>  Simulate an error randomly 1 in freq
     * DELAY=<ms> Simulate a delay on network of ms.
     * 
     */
    private void simulateQueryForPhoneStatus() {        
        
        //default if nothing set to phone on and not roaming.
        roaming=State.NO;
        phoneOn=State.YES;
        
        simulateHLRMethod = simulateHLRMethod.toUpperCase();
        
        if (simulateHLRMethod.contains("DELAY")) {
            int idx=simulateHLRMethod.indexOf("DELAY=");
            long delay = SIMULATE_DELAY;
            if (idx != -1) {
                String value = simulateHLRMethod.substring(idx).trim();            
                idx = value.indexOf(",");
                if (idx !=-1 )
                {
                    value=value.substring(0, idx).trim();                                                        
                }
                String[] values = value.split("=");
                value=values[1];

                try {
                    delay=Long.valueOf(value);
                }
                catch (NumberFormatException n) {
                    //ignore
                }

                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    //ignore..
                }
            }
        }
        
        if (simulateHLRMethod.contains("ERROR")) {
            roaming=State.ERROR;
            phoneOn=State.ERROR;
            return;
        }
        
        if (simulateHLRMethod.contains("FIRSTERR")) {
            String number=phoneNumber;
            if (number.contains("+") ) {
                number=number.substring(number.indexOf('+')+1); //remove the plus if any.
            } else if (number.contains("tel:")){
                number=number.substring(number.indexOf("tel:")+1);
            }
            if (simulatedOnce.get(number) == null) {
                //only set error on first call, so a retry will use random or other setting.
                simulatedOnce.put(number,true);
                roaming=State.ERROR;
                phoneOn=State.ERROR;
                return;
            }
        }
        
        if (simulateHLRMethod.contains("RANDERR")) {
            int idx=simulateHLRMethod.indexOf("RANDERR=");
            int freq = SIMULATE_RAMDOMEERR_FREQ;
            if (idx != -1) {
                String value = simulateHLRMethod.substring(idx).trim();            
                idx = value.indexOf(",");
                if (idx !=-1 )
                {
                   value=value.substring(0, idx).trim();                                                        
                }
                String[] values = value.split("=");
                value=values[1];
                
                try {
                    freq=Integer.valueOf(value);
                }
                catch (NumberFormatException n) {
                    //ignore
                }
                if (freq < 2) {
                    freq=2; // every other time.
                }
                
            }
            if (rand.nextInt(freq) == 0) {
                //if simulate an error on this check then return true.
                roaming=State.ERROR;
                phoneOn=State.ERROR;
                return;
            }
        }
        
        if (simulateHLRMethod.contains("RANDOM")) {
            int idx=simulateHLRMethod.indexOf("RANDOM=");
            int freq = SIMULATE_RAMDOM_FREQ;
            if (idx != -1) {
                String value = simulateHLRMethod.substring(idx).trim();            
                idx = value.indexOf(",");
                if (idx !=-1 )
                {
                    value=value.substring(0, idx).trim();                                                        
                }
                String[] values = value.split("=");
                value=values[1];

                try {
                    freq=Integer.valueOf(value);
                }
                catch (NumberFormatException n) {
                    //ignore
                }
                if (freq < 2) {
                    freq=2; // every other time
                }

            }
            if (rand.nextInt(freq) == 0) {
                //simulate roaming 1 in frequency
                roaming=State.YES;
            } else {               
                roaming=State.NO;
            }

            if (rand.nextInt(freq) == 0) {
              //simulate phone off 1 in frequency
                phoneOn=State.NO;
            } else {               
                phoneOn=State.YES;
            }    
        }
                        
        if (simulateHLRMethod.contains("HOME")) {
            roaming=State.NO;
        }
        if (simulateHLRMethod.contains("ROAM")) {
            roaming=State.YES;
        }

        if (simulateHLRMethod.contains("PHONEON")) {
            phoneOn=State.YES;
        }
        if (simulateHLRMethod.contains("PHONEOFF")) {
            phoneOn=State.NO;
        }
        
    }

    public String getPhoneNumber(){
        return phoneNumber;
    }


    public String toString() {
        return "PhoneInfo:phoneNumber=" + phoneNumber + ";roaming=" + isRoaming().toString() + ";phoneOn=" + isPhoneOn().toString();
    }

    /////////////////////////////////////////////////////// test VFE_NL MFD
    public static void main(String args[]) {
        String aPhoneNumber = args[0];       
        if (aPhoneNumber == null || aPhoneNumber.isEmpty()) {
            System.out.println("\nUsage: must pass a phonenumber as an argument");
            System.exit(1);
        }
        System.out.println("Phone number = " + aPhoneNumber);
        if (!loadBackendOam()) {
            System.out.println("failed calling loadBackendOam()");
            System.exit(1);
        }
        PhoneStatus phoneStatus = new PhoneStatus(aPhoneNumber);
        System.out.println("roaming stat (unkown -1 is considered not roaming (false)) = " + phoneStatus.isRoaming());
        System.exit(0);
    }
    
    private static boolean loadBackendOam() {
        Collection<String> configFilenames = new LinkedList<String>();

        String configFilename = "/opt/moip/config/backend/backend.conf";
        File backendXml = new File(configFilename);
        if (!backendXml.exists()) {
            configFilename = Config.getNtfHome() + "/cfg/backend.conf";
            backendXml = new File(configFilename);

            if (backendXml.exists() == false) {
                System.setProperty("backendConfigDirectory", Config.getNtfHome() + "/../ipms_sys2/backend/cfg");
                configFilename = Config.getNtfHome() + "/../ipms_sys2/backend/cfg/backend.conf";
                backendXml = new File(configFilename);
                if (backendXml.exists() == false) {
                    log.error("NtfMain failed to find backend.conf file. verify \"ntfHome\" is defined correctly.");
                    return false;
                }
            }
        }

        configFilenames.add(configFilename);
        IConfiguration configuration;
        OAMManager oamManager = CommonOamManager.getInstance().getMcdOam();
        try {
            configuration = new ConfigurationImpl(null,configFilenames,false);
            OAMManager hlrAccessOam = new MoipOamManager();
            //OAMManager ss7Oam = new MoipOamManager();
            CommonMessagingAccess.getInstance().setConfiguration(configuration);
            CommonOamManager.getInstance().setHlrAccessOam(hlrAccessOam);
            //CommonOamManager.getInstance().setSs7Oam(ss7Oam);
            CommonOamManager.getInstance().setConfiguration(configuration);
            //CommonMessagingAccess.getInstance().getHlrAccessManager(); // this force the initialisation of HlrAccessMgr config
            ISs7Manager ss7mgrStub = new Ss7mgrStub();
            CommonMessagingAccess.setSs7Manager(ss7mgrStub);
            return true;
        } catch (ConfigurationException e) {
            log.fatal("loadBackendOam, exception: ", e);
            e.printStackTrace();
        }

        return false;
    }

    static class Ss7mgrStub implements ISs7Manager { 
        public SubscriberInfo getSubscriberInfo(String s)
                throws Ss7Exception{return null;}

        public String getImsi(String s)
                throws Ss7Exception{return null;} 

        public  void setUnconditionalDivertInHlr(String s)
                throws Ss7Exception{}

        public void setConditionalDivertInHlr(String s)
                throws Ss7Exception{}

        public  void cancelUnconditionalDivertInHlr(String s)
                throws Ss7Exception{}

        public void cancelConditionalDivertInHlr(String s)
                throws Ss7Exception{}

        public  Boolean getDivertStatusInHlr(String s, String s1)
                throws Ss7Exception{return null;}

        public int getRoamingStatus_ATI(String aPhoneNumber){return -1;}

        public  Boolean isRoaming(String s){return null;}

        public  Boolean isRoaming_ATI(String s){return null;}

        public Boolean isRoaming_ATI(AnyTimeInterrogationResult result){ return null; }


        public  Boolean isRoaming_SRI(String s){return null;}

        public  Boolean isRoamingSRI(String s)
                throws Ss7Exception{return null;}

        public  AnyTimeInterrogationResult requestATI(String s)
                throws Ss7Exception{return null;}

        public  Boolean registerAlertScHandler(AlertSCHandler alertschandler)
                throws Ss7Exception{return true;}

        public Boolean registerAlertScHandlerWithRetry(AlertSCHandler alertSCHandler,int alertSCRegistrationNumOfRetry,int alertSCRegistrationTimeInSecBetweenRetry){return true;}

        public  void sendMtForwardSM(String s, String s1, String s2)
                throws Ss7Exception{}

        public  void sentReportSMDeliveryStatus(String s)
                throws Ss7Exception{}

        public  Boolean useHlr(){return false;}

        public int getSubscriberRoamingStatus(String s) 
                throws Ss7Exception{return 0;}

        public String getSubStatusHlrInterrogationMethod() {return ConfigParam.SUB_STATUS_HLR_METHOD_ENUM_CUSTOM;}
    }
    
}
