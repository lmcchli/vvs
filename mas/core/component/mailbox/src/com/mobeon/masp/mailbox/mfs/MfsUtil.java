/**
 * 
 */
package com.mobeon.masp.mailbox.mfs;

import java.util.Hashtable;
import java.util.Map;

import com.abcxyz.messaging.mrd.data.ServiceName;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.mobeon.masp.mailbox.MailboxMessageType;
import com.mobeon.masp.mailbox.StoredMessageState;

/**
 * @author egeobli
 *
 */
public final class MfsUtil {

	// Map for converting MAS message types to MFS message types
	private static final Map<MailboxMessageType, String> messageTypeMap = new Hashtable<MailboxMessageType, String>();

	// Map for converting MFS message types to MAS message types
	private static final Map<String, MailboxMessageType> masMessageTypeMap = new Hashtable<String, MailboxMessageType>();

	// Map for converting StateCriteria enumeration to their string counterpart.
	private static final Hashtable<StoredMessageState, String> storedMessageStateMap = new Hashtable<StoredMessageState, String>();

	// Map for converting MFS state criteria to their StateCriteria enumeration counterpart.
	private static final Hashtable<String, StoredMessageState> masStoredMessageStateMap = new Hashtable<String, StoredMessageState>();

	static {
		messageTypeMap.put(MailboxMessageType.EMAIL, ServiceName.EMAIL);
		messageTypeMap.put(MailboxMessageType.FAX, ServiceName.FAX);
		messageTypeMap.put(MailboxMessageType.VIDEO, ServiceName.VIDEO);
		messageTypeMap.put(MailboxMessageType.VOICE, ServiceName.VOICE);
		assert(messageTypeMap.size() == MailboxMessageType.values().length);
		
		masMessageTypeMap.put(ServiceName.EMAIL, MailboxMessageType.EMAIL);
		masMessageTypeMap.put(ServiceName.FAX, MailboxMessageType.FAX);
		masMessageTypeMap.put(ServiceName.VIDEO, MailboxMessageType.VIDEO);
		masMessageTypeMap.put(ServiceName.VOICE, MailboxMessageType.VOICE);
		assert(masMessageTypeMap.size() == MailboxMessageType.values().length);

		storedMessageStateMap.put(StoredMessageState.DELETED, Constants.DELETED);
		storedMessageStateMap.put(StoredMessageState.NEW, Constants.NEW);
		storedMessageStateMap.put(StoredMessageState.READ, Constants.READ);
		storedMessageStateMap.put(StoredMessageState.SAVED, Constants.SAVED);
		assert(storedMessageStateMap.size() == StoredMessageState.values().length);

		masStoredMessageStateMap.put(Constants.DELETED, StoredMessageState.DELETED);
		masStoredMessageStateMap.put(Constants.NEW, StoredMessageState.NEW);
		masStoredMessageStateMap.put(Constants.READ, StoredMessageState.READ);
		masStoredMessageStateMap.put(Constants.SAVED, StoredMessageState.SAVED);
		assert(masStoredMessageStateMap.size() == StoredMessageState.values().length);
}

	private MfsUtil() {
	}
	
	/**
	 * Returns the string representation of a mailbox message type.
	 * @param messageType Message type value.
	 * @return String name for the message type value.
	 */
	public static String toMfsMessageType(MailboxMessageType messageType) {
		return messageTypeMap.get(messageType);
	}
	
	/**
	 * Converts a MFS message type to a MAS message type.
	 * @param messageType MFS Message type value.
	 * @return String name for the message type value. 
	 *         null is returned if the message type does not exists.
	 */
	public static MailboxMessageType toMasMessageType(String messageType) {
		return masMessageTypeMap.get(messageType);
	}
	
	/**
	 * Returns the string representation of a state criteria.
	 * @param stateCriteria State criteria enumeration value.
	 * @return String name for a state criteria.
	 */
	public static String toMfsMessageState(StoredMessageState storedMessageState) {
		return storedMessageStateMap.get(storedMessageState);
	}
	
	/**
	 * Returns a StoreMessageState state from MFS message state string.
	 * @param messageState MFS Message State.
	 * @return StoredMessageState value. null is returned if the message state
	 *         does not exists.
	 */
	public static StoredMessageState toMasMessageState(String messageState) {
		return masStoredMessageStateMap.get(messageState);
	}
	
	/**
	 * Returns a priority value for MFS according to MAS message state.
	 * 
	 * @param isUrgent true when the message is urgent.
	 * @return Priority value. The value is an integer from 0 to 4 where 0 is the highest priority. See
	 * 		   the MFS SEC for more information.
	 */
	public static int toMfsPriority(boolean isUrgent) {
		return isUrgent ? MoipMessageEntities.MFS_URGENT_PRIORITY : MoipMessageEntities.MFS_NORMAL_PRIORITY;
	}
	
	/**
	 * Returns the urgent state from a MFS priority value.
	 * 
	 * @param priority MFS priority value.
	 * @throws IllegalArgumentException if the priority is not in the range of 0 to 4.
	 */
	public static boolean toMasPriority(int priority) {
		if (priority < 0 || priority > 4) {
			throw new IllegalArgumentException("MFS priority must range from 0 to 4");
		}
		return priority < MoipMessageEntities.MFS_NORMAL_PRIORITY;
	}
	
	/**
	 * Returns the MFS string representation of the confidentiality state.
	 * 
	 * @param confidential Confidentiality state.
	 * @return MFS confidentiality state.
	 */
	public static String toMfsConfidentialState(boolean confidential) {
		return confidential ? Constants.MFS_PRIVATE : Constants.MFS_NONPRIVATE;
	}
	
	/**
	 * Returns the MAS confidentiality state representation from an MFS confidentiality state.
	 * @param confidential State
	 * @return true if state is confidential, false otherwise.
	 * @throws IllegalArgumentException if the confidential parameter is not "Private" or "NonPrivate".
	 */
	public static boolean toMasConfidentialState(String confidential) {
		if (confidential.equalsIgnoreCase(Constants.MFS_PRIVATE)) {
			return true;
		} else if (confidential.equalsIgnoreCase(Constants.MFS_NONPRIVATE)) {
			return false;
		} else {
			throw new IllegalArgumentException("Parameter confidential can only have \"" + 
					Constants.MFS_PRIVATE + "\" or \"" +
					Constants.MFS_NONPRIVATE + "\" " +
					"as valid values.");
		}
	}
}
