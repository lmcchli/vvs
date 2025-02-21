/**
 * 
 */
package com.mobeon.masp.mailbox.mfs;

import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.ICommonMessagingAccess;
import com.mobeon.masp.mailbox.BaseStoreableMessageFactory;
import com.mobeon.masp.mailbox.IStorableMessage;
import com.mobeon.masp.mailbox.IStorableMessageFactory;
import com.mobeon.masp.mailbox.MailboxException;

/**
 * @author egeobli
 *
 */
public class MfsStorableMessageFactory extends
		BaseStoreableMessageFactory<MfsContext> implements IStorableMessageFactory {

	private ICommonMessagingAccess commonMessagingAccess;
	/* (non-Javadoc)
	 * @see com.mobeon.masp.mailbox.BaseStoreableMessageFactory#create()
	 */
	@Override
	public IStorableMessage create() throws MailboxException {
		MfsStorableMessage message = new MfsStorableMessage(
				getContextFactory().create(), 
				commonMessagingAccess);
		
		return message;
	}
	
	public ICommonMessagingAccess getCommonMessagingAccess() {
		return commonMessagingAccess;
	}
	public void setCommonMessagingAccess(ICommonMessagingAccess commonMessagingAccess) {
		this.commonMessagingAccess = commonMessagingAccess;
	}

	
}
