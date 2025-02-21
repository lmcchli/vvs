package com.abcxyz.services.moip.common.cmnaccess;

import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;

/******************************************************************************
 * COPYRIGHT (C) ABCXYZ INTERNET APPLICATIONS INC.
 * THIS SOFTWARE IS FURNISHED UNDER A LICENSE ONLY AND IS
 * PROPRIETARY TO ABCXYZ INTERNET APPLICATIONS INC. IT MAY NOT BE COPIED
 * EXCEPT WITH THE PRIOR WRITTEN PERMISSION OF ABCXYZ INTERNET APPLICATIONS
 * INC.  ANY COPY MUST INCLUDE THE ABOVE COPYRIGHT NOTICE AS
 * WELL AS THIS PARAGRAPH.  THIS SOFTWARE OR ANY OTHER COPIES
 * THEREOF, MAY NOT BE PROVIDED OR OTHERWISE MADE AVAILABLE
 * TO ANY OTHER PERSON OR ENTITY.
 * TITLE TO AND OWNERSHIP OF THIS SOFTWARE SHALL AT ALL
 * TIMES REMAIN WITH ABCXYZ INTERNET APPLICATIONS INC.
 ********************************************************************************/

/**
 * class defines MOIP message ID's generation algorithm
 *
 */
public class MoipMessageIDGen {
	private static char RECEIPIENT_PREFIX = 'r';
	private static char SENDER_PREFIX = 's';

	/**
	 * short message ID is composed with 2 message IDs
	 *
	 * @param msgInfo
	 * @param serverId
	 * @return
	 */
	public static String getShortRecipientMessageID(MessageInfo msgInfo, String serverId) {
		String id = RECEIPIENT_PREFIX + msgInfo.omsgid + msgInfo.rmsgid + serverId;
		return id;
	}

	public static String getShortSenderMessageID(MessageInfo msgInfo, String serverId) {
		String id = SENDER_PREFIX + msgInfo.omsgid + msgInfo.rmsgid + serverId;
		return id;
	}
	/**
	 * MOIP message's internal recipient id is always available, recipient is kept for external case only;
	 *
	 * @param msginfo
	 * @return
	 */
	public static String getRecipientMessageID(MessageInfo msgInfo, String serverId) {
        String id = null;

		if (msgInfo.rmsa.isInternal()) {
			id = getShortRecipientMessageID(msgInfo, serverId);
			return id;
		}

		try {
			id = IDBase64Gen.encodeRecipientId(msgInfo.rmsa, msgInfo.rmsgid, msgInfo.omsgid, serverId, RECEIPIENT_PREFIX);
		} catch (IDBase64FormatException e) {
			CommonOamManager.getInstance().getLogger().warn("MoipMessageIDGen exception for: " + msgInfo.toString(), e);
		}

		return id;
	}

	public static String getSenderMessageID(MessageInfo msgInfo, String serverId) {
        String id = null;

		if (msgInfo.omsa.isInternal()) {
			id = getShortSenderMessageID(msgInfo, serverId);
			return id;
		}

		try {
			id = IDBase64Gen.encodeRecipientId(msgInfo.omsa, msgInfo.rmsgid, msgInfo.omsgid, serverId, SENDER_PREFIX);
		} catch (IDBase64FormatException e) {
			CommonOamManager.getInstance().getLogger().warn("MoipMessageIDGen exception for: " + msgInfo.toString(), e);
		}

		return id;
	}
}