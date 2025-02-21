/**
 * 
 */
package com.mobeon.masp.mailbox.mfs;

import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;

/**
 * @author egeobli
 *
 */
public final class Constants {
	
	// Boolean value string representation
	public static final String YES = "yes";
	public static final String NO = "no";
	
	// Importance values for MFS
	public static final String MFS_URGENT = "Urgent";
	public static final String MFS_NORMAL = "Normal";
	
	// Confidential values for MFS
	public static final String  MFS_NONPRIVATE = "NonPrivate";
	public static final String  MFS_PRIVATE = "Private";
	
	// state
	public static final String DELETED = MoipMessageEntities.MESSAGE_DELETED;
	public static final String NEW = MoipMessageEntities.MESSAGE_NEW;
	public static final String SAVED = MoipMessageEntities.MESSAGE_SAVED;
	public static final String READ = MoipMessageEntities.MESSAGE_READ;

	
	// Parts content headers
	public static final String CONTENT_DURATION = "Content-Duration";
	public static final String CONTENT_DESCRIPTION = "Content-Description";
	public static final String CONTENT_LANGUAGE = "Content-Language";
	public static final String CONTENT_DISPOSITION = "Content-Disposition";
//	public static final String HEADERNAME_CONTENT_DISPOSITION = "Content-Disposition";
	public static final String FILENAME_PARAMETER_NAME = "filename";
	public static final String ORIGINATOR_SPOKEN_NAME_STRING = "Originator-Spoken-Name";

	// Message Expiry Event Id
	public static final String EXPIRY_EVENT_ID = MoipMessageEntities.EXPIRY_EVENT_ID;
}
