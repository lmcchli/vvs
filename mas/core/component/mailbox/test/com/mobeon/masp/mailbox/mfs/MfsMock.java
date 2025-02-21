package com.mobeon.masp.mailbox.mfs;

import java.util.Date;
import java.util.Properties;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.Container2;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.message.Message;
import com.abcxyz.messaging.common.message.MessageStreamingResult;
import com.abcxyz.messaging.common.message.MsgBodyPart;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.message.MfsStateFolderType;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mfs.statefile.StateAttributesFilter;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.messaging.scheduler.handling.AppliEventOperations;
import com.abcxyz.services.moip.masevent.EventTypes;
import com.mobeon.common.cmnaccess.ICommonMessagingAccess;

class MfsMock implements ICommonMessagingAccess {

	private Container1 c1;
	private Container2 c2;
	private MsgBodyPart[] parts;
	private StateAttributes attributes;

	private int storeReturnValue;
	private StateFile state;


	public MfsMock() {
		storeReturnValue = MessageStreamingResult.streamingOK;
	}


	public int countMessages(MSA msa, StateAttributesFilter filter)
			throws MsgStoreException {
		return 0;
	}

	public void deleteMessage(StateFile state) throws MsgStoreException {
	}

	public Message readMessage(MessageInfo msgInfo)
			throws MsgStoreException {
		return null;
	}

	public Message[] searchMessages(MSA msa, StateAttributesFilter filter)
			throws MsgStoreException {
		return null;
	}

	public int storeMessage(Container1 c1, Container2 c2,
			MsgBodyPart[] parts, StateAttributes attributes)
			throws Exception {

		this.c1 = c1;
		this.c2 = c2;
		this.parts = parts;
		this.attributes = attributes;
		return storeReturnValue;
	}



    public boolean isStorageOperationsAvailable(MSA originator, MSA recipient){
        return true;
    }


    public void updateState(StateFile state) throws MsgStoreException {
		this.state = state;
	}

	public Container1 getContainer1() {
		return c1;
	}

	public Container2 getContainer2() {
		return c2;
	}

	public MsgBodyPart[] getContainer3Parts() {
		return parts;
	}

	public StateAttributes getStateAttributes() {
		return attributes;
	}

	public int getStoreReturnValue() {
		return storeReturnValue;
	}

	public void setStoreReturnValue(int storeReturnValue) {
		this.storeReturnValue = storeReturnValue;
	}

	/* (non-Javadoc)
	 * @see com.mobeon.common.cmnaccess.ICommonMessagingAccess#getStateFile(com.abcxyz.messaging.mfs.data.MessageInfo)
	 */
	public StateFile getStateFile(MessageInfo msgInfo) throws MsgStoreException {

		if (state == null) {
			state = new StateFile(msgInfo.omsa, msgInfo.rmsa, msgInfo.omsgid, msgInfo.rmsgid);
			state.setMsgState(Constants.READ);
		}
		return state;
	}

	/* (non-Javadoc)
     * @see com.mobeon.common.cmnaccess.ICommonMessagingAccess#getStateFile(com.abcxyz.messaging.mfs.data.MessageInfo, String)
     */
    public StateFile getStateFile(MessageInfo msgInfo, String folder) throws MsgStoreException {
        if("trash".equals(folder.toLowerCase())){
            if (state == null) {
                state = new StateFile(msgInfo.omsa, msgInfo.rmsa, msgInfo.omsgid, msgInfo.rmsgid, MfsStateFolderType.TRASH);
                state.setMsgState(Constants.READ);
            }
            return state;
        }

        return getStateFile(msgInfo);
    }


	/* (non-Javadoc)
	 * @see com.mobeon.common.cmnaccess.ICommonMessagingAccess#searchMessageInfos(com.abcxyz.messaging.common.message.MSA, com.abcxyz.messaging.mfs.statefile.StateAttributesFilter)
	 */
	public MessageInfo[] searchMessageInfos(MSA msa,
			StateAttributesFilter filter) throws MsgStoreException {

		return new MessageInfo[0];
	}


    @Override
    public void cancelEvent(String eventId) {
        // TODO Auto-generated method stub

    }


    @Override
    public AppliEventOperations getMasEventOpeartor() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public String getMoipPrivateFolder(String msid, boolean internal) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void notifyNtf(EventTypes eventType, MessageInfo msgInfo,
            String recipientId, Properties properties) {
        // TODO Auto-generated method stub

    }


    @Override
    public String scheduleEvent(long when, MessageInfo msgInfo,
            EventTypes eventType, Properties props) {
        // TODO Auto-generated method stub
        return null;
    }


	@Override
	public StateFile[] searchStateFiles(MSA mas, StateAttributesFilter filter)
			throws MsgStoreException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void cancelScheduledEvent(StateFile stateFile) {
		// TODO Auto-generated method stub

	}


	@Override
	public void setMessageExpiry(int expiryInDays, StateFile stateFile)
			throws MsgStoreException {
		// TODO Auto-generated method stub

	}


	@Override
	public void setMessageExpiry(Date expiryDate, StateFile stateFile)
			throws MsgStoreException {
		// TODO Auto-generated method stub

	}
	@Override
    public String denormalizeNumber(String number)
	{
		return "";
	}


    @Override
    public boolean isStorageOperationsAvailable(String originator, String recipient) {
        return true;
    }



}
