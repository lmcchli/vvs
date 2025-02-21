/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.mfs;

import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mfs.statefile.StateAttributesFilter;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.QuotaName;

import static com.mobeon.masp.mailbox.QuotaName.TOTAL;
import com.mobeon.masp.mailbox.QuotaUsageInventory;
import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.mrd.data.ServiceName;

/**
 *
 */
public class MfsQuotaUsageInventory extends QuotaUsageInventory<MfsStoreAdapter,MfsContext> {

    private static final ILogger LOGGER = ILoggerFactory.getILogger(MfsQuotaUsageInventory.class);
    private MfsStoreAdapter mfsStoreAdapter;

    MfsQuotaUsageInventory(MfsStoreAdapter storeAdapter, MfsContext context) {    	
        super(storeAdapter,context);
        this.mfsStoreAdapter = storeAdapter;
    }

    protected void init() throws MailboxException {
    	Object perf = null;
    	try {
	    	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
	            perf = CommonOamManager.profilerAgent.enterCheckpoint("MfsQuotaUsageInventory.init()");
	        }
	    	StateAttributesFilter filter = new StateAttributesFilter();
	    	String[] msgType = new String[] {ServiceName.FAX, ServiceName.VOICE, ServiceName.VIDEO};
	    	filter.setAttributeValue(StateAttributes.getC1StateName(Container1.Message_class), msgType);
	    	MSA msa = new MSA(mfsStoreAdapter.getMsId(), true);
	    	try {
				setMessageUsage(TOTAL, CommonMessagingAccess.getInstance().countMessages(msa, filter));
                                filter.setAttributeValue(StateAttributes.getC1StateName(Container1.Message_class),  new String[] {ServiceName.FAX});
                                setMessageUsage(QuotaName.FAX, CommonMessagingAccess.getInstance().countMessages(msa, filter));
                                filter.setAttributeValue(StateAttributes.getC1StateName(Container1.Message_class),  new String[] {ServiceName.VOICE});
                               setMessageUsage(QuotaName.VOICE, CommonMessagingAccess.getInstance().countMessages(msa, filter));
                                filter.setAttributeValue(StateAttributes.getC1StateName(Container1.Message_class),  new String[] {ServiceName.VIDEO});
                              setMessageUsage(QuotaName.VIDEO, CommonMessagingAccess.getInstance().countMessages(msa, filter));
			} catch (MsgStoreException e) {
				throw new MailboxException("Cannot count messages for MsID " + mfsStoreAdapter.getMsId() , e);
			}   
    	 } finally {
         	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
 				CommonOamManager.profilerAgent.exitCheckpoint(perf);
 			}
         }
    }
    
}
