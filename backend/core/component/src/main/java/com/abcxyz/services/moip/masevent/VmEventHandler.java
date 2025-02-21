package com.abcxyz.services.moip.masevent;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.mrd.data.InformEventResult;
import com.abcxyz.messaging.mrd.data.InformEventType;
import com.abcxyz.messaging.mrd.operation.InformEventReq;
import com.abcxyz.messaging.mrd.operation.InformEventResp;
import com.abcxyz.messaging.mrd.operation.MsgServerOperations;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.ICommonMessagingAccess;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;


public class VmEventHandler extends MsgServerOperations {

	static private final ILogger logger = ILoggerFactory.getILogger(VmEventHandler.class);
    private ICommonMessagingAccess cms;    

    public VmEventHandler() {
    	this.cms = CommonMessagingAccess.getInstance();
    }
    
    public InformEventResp informEvent (InformEventReq req){
    	
    	InformEventResp resp = null ;
    	
    	try
    	{
	    	MessageInfo msgInfo = new MessageInfo(new MSA(req.oMsa.getValue()),new MSA(req.rMsa.getValue()),req.oMsgID.getValue(), req.rMsgID.getValue());
	    	StateFile stateFile = cms.getStateFile(msgInfo);
	    	String subscriber = stateFile.getC1Attribute(Container1.To);
	    	
	    	if(req.informEventType.value.equals(InformEventType.MSG_READ))
	    	{
	    		IDirectoryAccessSubscriber subscriberProfile = CommonMessagingAccess.getInstance().getMcd().lookupSubscriber(subscriber);
	        	int retentionValue = 14; // Default value for saved and read messages
	        	String[] retention = subscriberProfile.getStringAttributes(DAConstants.ATTR_MSG_RETENTION_READ_VOICE);
	        	
	        	try 
	        	{
	            	if(retention != null && retention.length > 0) 
	            	{
	            		retentionValue = Integer.valueOf(retention[0]).intValue();
	            		if(logger.isDebugEnabled()) {
	            			logger.debug("VmEventHandler.informEvent: Found retention time " + retentionValue + " for subscriber " + subscriber + " in state NEW");
	            		}
	            	}else {
	            		logger.warn("VmEventHandler.informEvent: Did not find the retention time of type NEW for " + subscriber);
	            	}
	    		} 
	        	catch (NumberFormatException nme) 
	        	{
	    			logger.warn("VmEventHandler::saveState Unable to parse the retention time NEW for subscriber " + subscriber);
	    		}
	        	
	    		setMessageExpiry(retentionValue, stateFile);
				cms.updateState(stateFile);
	
				resp = new InformEventResp(req.transID.value, InformEventResult.OK);
	    	}
	    	else if(req.informEventType.value.equals(InformEventType.MSG_DELETED))
	    	{
	        	cms.cancelScheduledEvent(stateFile);
	        	cms.deleteMessage(stateFile);
	        	
	        	resp = new InformEventResp(req.transID.value, InformEventResult.OK);
	    	}
    	}
    	catch(MsgStoreException e)
    	{
    		logger.error("VmEventHandler.informEvent: Could not save message change to state " + InformEventType.MSG_READ, e);
    		resp = new InformEventResp(req.transID.value, InformEventResult.TEMPFAIL, "Exception when trying to access the Message Store");
    	}
    	
    	if(resp == null)
    	{
    		logger.warn("VmEventHandler.informEvent: " + req.informEventType.value + " not a valid event type" );
    		resp = new InformEventResp(req.transID.value, InformEventResult.PERMFAIL, req.informEventType.value + " not a valid event type");
    	}
    	
        return resp;
    }

    
    /**
     * Sets the expiry date of the current message and schedules an expiry event.
     * 
     * @param expiryInDays Number of days when the message must expire
     * @param stateFile State file of the message.
     * @throws MsgStoreException Thrown on error.
     */
    private void setMessageExpiry(int expiryInDays, StateFile stateFile) throws MsgStoreException {
    	cms.setMessageExpiry(expiryInDays, stateFile);
    }
}
