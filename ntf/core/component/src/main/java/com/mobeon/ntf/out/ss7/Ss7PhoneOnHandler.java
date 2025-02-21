package com.mobeon.ntf.out.ss7;

import com.abcxyz.messaging.common.mnr.SubscriberInfo;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.common.ssmg.AnyTimeInterrogationResult;
import com.abcxyz.messaging.common.ssmg.ReportDeliveryStatusException;
import com.abcxyz.services.moip.common.ss7.Ss7Exception;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;

import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.ConfigParam;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.event.EventRouter;
import com.mobeon.ntf.event.PhoneOnEvent;

public class Ss7PhoneOnHandler {
	private static Ss7PhoneOnHandler instance = new Ss7PhoneOnHandler();
	static LogAgent log = NtfCmnLogger.getLogAgent(Ss7PhoneOnHandler.class);

	public static Ss7PhoneOnHandler getInstance() {
        if (instance == null) {
            instance = new Ss7PhoneOnHandler();
        }
        return instance;
      }

    

	public void requestPhoneOn(String address) {
		try {
			if(!CommonMessagingAccess.getInstance().getSs7Manager().useHlr()){
		        PhoneOnEvent phoneOnEvent = new PhoneOnEvent(this, address, PhoneOnEvent.PHONEON_OK, "Phone is on");
				EventRouter.get().phoneOn(phoneOnEvent);
				return;
			}
			
			String method = CommonMessagingAccess.getInstance().getSs7Manager().getSubStatusHlrInterrogationMethod();
			
			if(method.equalsIgnoreCase(ConfigParam.SUB_STATUS_HLR_METHOD_ENUM_ATI)) {
			
			Object perf = null;
			AnyTimeInterrogationResult result = null;
            try {
                if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
                    perf = CommonOamManager.profilerAgent.enterCheckpoint("SS7PhoneOnHandler.requestATI");
                }
                result = CommonMessagingAccess.getInstance().getSs7Manager().requestATI(address);
            } finally {
                if (perf != null) {
                    CommonOamManager.profilerAgent.exitCheckpoint(perf);
                }
            }
			
    			AnyTimeInterrogationResult.SUBSCRIBER_STATE state = result.getSubscriberState();
    			AnyTimeInterrogationResult.NOT_REACHABLE_REASON reason = result.getNotReachableReason();
			
					
    			if (result.isError()) {
    				if(log.isDebugEnabled()) {
    					log.debug("Ss7Handler.requestPhoneOn: ATI result error for " + address);
    				}
    				handleError(address);
    			} //BUSY
    			else if(state.equals(AnyTimeInterrogationResult.SUBSCRIBER_STATE.CAMELBUSY)){
    				if(log.isDebugEnabled()) {
    					log.debug("Ss7Handler.requestPhoneOn: subscriber is busy " + address);
    				}
    				if(Config.getCheckBusy()) {
    					handleBusy(address);
    				} else {
    					if(log.isDebugEnabled()) {
    						log.debug("Ss7Handler.requestPhoneOn: subscriber is busy but config for check busy is disabled");
    					}
    					handleAssumedIdle(address);
    				}
    			}
    
    			//IMSIDETACHED or RESTRICTED AREA
    			else if(state.equals(AnyTimeInterrogationResult.SUBSCRIBER_STATE.NET_DET_NOT_REACHABLE)
    					&& (reason.equals(AnyTimeInterrogationResult.NOT_REACHABLE_REASON.IMSI_DETACHED)
    							|| reason.equals(AnyTimeInterrogationResult.NOT_REACHABLE_REASON.RESTRICTED_AREA))){
    				if(log.isDebugEnabled()){
    					log.debug("Ss7Handler.requestPhoneOn: subscriber is imsi detached or restricted area " + address);
    				}
    				handleImsiDetachedOrRestrictedArea(address, result);
    
    			}
    
    			//ASSUMED IDLE
    			else if(state.equals(AnyTimeInterrogationResult.SUBSCRIBER_STATE.ASSUMED_IDLE)){
    				if(log.isDebugEnabled()) {
    					log.debug("Ss7Handler.requestPhoneOn: subscriber is assumed idle " + address);
    				}
    				handleAssumedIdle(address);
    			}
                        //FIX for VDF CR - bad requirement - this case should not lead to a ReportSM-DeliveryStatus message
                        else if(state.equals(AnyTimeInterrogationResult.SUBSCRIBER_STATE.NOT_PROVIDED_FROM_VLR) ||
                                 result.getMscNumber() == null){
                                 if (!Config.getSendDetachOnAssumedUnavailable()){   //normal case
                                        if(log.isDebugEnabled()){
                                        log.debug("Ss7Handler.requestPhoneOn: subscriber_state not provide by VLR or MscNumber is null.  Assumed idle " + address);}
                                        handleAssumedIdle(address);
                                 } else {                                           //Only for VDF obsolete requirement to detach sub if subscriber state not provided from VLR.
                                        if(log.isDebugEnabled()){                   
                                        log.debug("Ss7Handler.requestPhoneOn: subscriber_state not provide by VLR or MscNumber is null.  send reportSM-DeliveryStatus to HLR" + address);}
                                        handleOtherCases(address); 
                                        }
                        }

    			//NOTREGISTERED or MSPURGED or Without MSC number or NOT_PROVIDED_FROM_VLR
    			else if((state.equals(AnyTimeInterrogationResult.SUBSCRIBER_STATE.NET_DET_NOT_REACHABLE)
    					&& (reason.equals(AnyTimeInterrogationResult.NOT_REACHABLE_REASON.NOT_REGISTERED)
    							|| reason.equals(AnyTimeInterrogationResult.NOT_REACHABLE_REASON.MS_PURGED)))
    					) {
    				if(log.isDebugEnabled()){
    					log.debug("Ss7Handler.requestPhoneOn: subscriber is notRegistered or mspurged or without msc number " + address);
    				}
    				handleOtherCases(address);
    
    			} else {
    				if(log.isDebugEnabled()){
    					log.debug("Ss7Handler.requestPhoneOn: other ati result status" + address);
    				}
    				handleAssumedIdle(address);
                                //Changed for VDF - bad requirement
    			}
			} else if(method.equalsIgnoreCase(ConfigParam.SUB_STATUS_HLR_METHOD_ENUM_SRIFORSM)) {
                SubscriberInfo subInfo;
                subInfo = CommonMessagingAccess.getInstance().getSs7Manager().getSubscriberInfo(address);

                if (subInfo != null) {
                    if(subInfo.getSubscriberStatus()) {
                        handleAssumedIdle(address); // PhoneOn is true, assume it is ready to receive a call
                    } else {
                        handleOtherCases(address); // Phone is false, meaning the phone is not reachable.
                    }
                }
            } else if (method.equalsIgnoreCase(ConfigParam.SUB_STATUS_HLR_METHOD_ENUM_CUSTOM)) { // VFE_NL MFD
                log.debug("Ss7PhoneOnHandler.requestPhoneOn(): HLR Access method = custom; shall use HlrAccessManager; but isphoneOn() is not part of the scope; so do do nothing");
            }
		} catch(ReportDeliveryStatusException e) {
			log.error("Ss7Handler.requestPhoneOn: ReportDeliveryStatusException occurred: " + e.getMessage());
			PhoneOnEvent phoneOnEvent = new PhoneOnEvent(this, address, PhoneOnEvent.PHONEON_CLIENT_FAILED_TEMPORARY, "Could not send request to HLR");
			EventRouter.get().phoneOn(phoneOnEvent);
		} catch (Ss7Exception e) {
			log.error("Ss7Handler.requestPhoneOn: ss7exception occurred: " + e.getMessage());
			PhoneOnEvent phoneOnEvent = new PhoneOnEvent(this, address, PhoneOnEvent.PHONEON_CLIENT_FAILED_TEMPORARY, "Could not send request to HLR");
			EventRouter.get().phoneOn(phoneOnEvent);
		}
	}

	/**
	 * phone is busy, we will retry later on according to outdial state machine
	 * @param address
	 */
	public void handleBusy(String address) {
        PhoneOnEvent phoneOnEvent = new PhoneOnEvent(this, address, PhoneOnEvent.PHONEON_BUSY, "Phone is busy");
		EventRouter.get().phoneOn(phoneOnEvent);
	}

	/**
	 * phone is on, we can send the outdial
	 * @param address
	 */
	public void handleAssumedIdle(String address){
        PhoneOnEvent phoneOnEvent = new PhoneOnEvent(this, address, PhoneOnEvent.PHONEON_OK, "Phone is on");
		EventRouter.get().phoneOn(phoneOnEvent);
	}

	/**
	 * phone is absent with reason imsi detached or restricted area
	 * send MT-FORWARD_SM and REPORT_SM_DELIVERY_STATUS
	 * @param address
	 * @throws Ss7Exception
	 */
	public void handleImsiDetachedOrRestrictedArea(String address, AnyTimeInterrogationResult atiResult) throws Ss7Exception {

		PhoneOnEvent phoneOnEvent = null;
		if(atiResult.getAtiSubscriberInfoExt() != null) {

			String mscAddress = null;
			if(atiResult.getMscNumber()!= null) {
				mscAddress = atiResult.getMscNumber().getAddress();
			}
			CommonMessagingAccess.getInstance().getSs7Manager().sendMtForwardSM(address, atiResult.getAtiSubscriberInfoExt().getActiveImsi(), mscAddress);
			CommonMessagingAccess.getInstance().getSs7Manager().sentReportSMDeliveryStatus(address);
			phoneOnEvent = new PhoneOnEvent(this, address, PhoneOnEvent.PHONEON_CLIENT_SENT_SUCCESSFULLY, "Phone on request sent");

		} else {
		    phoneOnEvent = new PhoneOnEvent(this, address, PhoneOnEvent.PHONEON_CLIENT_FAILED, "Failed to get IMSI");
		}

		EventRouter.get().phoneOn(phoneOnEvent);
	}

	/**
	 * send REPORT_SM_DELIVERY_STATUS
	 * @param address
	 * @throws Ss7Exception
	 * @throws ReportDeliveryStatusException
	 */
	public void handleOtherCases(String address) throws ReportDeliveryStatusException, Ss7Exception{

		CommonMessagingAccess.getInstance().getSs7Manager().sentReportSMDeliveryStatus(address);

		PhoneOnEvent phoneOnEvent = new PhoneOnEvent(this, address, PhoneOnEvent.PHONEON_CLIENT_SENT_SUCCESSFULLY, "Phone on request sent");
		EventRouter.get().phoneOn(phoneOnEvent);
	}
	/**
	 * If there is error from SS7 ATI, do retry
	 * @param address
	 * @throws Ss7Exception
	 */
	public void handleError(String address) throws Ss7Exception {
        PhoneOnEvent phoneOnEvent = new PhoneOnEvent(this, address, PhoneOnEvent.PHONEON_SS7_ERROR, "Ss7 error");
		EventRouter.get().phoneOn(phoneOnEvent);
	}


}
