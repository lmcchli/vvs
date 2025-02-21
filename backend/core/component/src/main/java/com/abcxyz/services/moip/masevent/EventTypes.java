package com.abcxyz.services.moip.masevent;

import java.util.Hashtable;
import java.util.Map;

import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;

/**
 *
 * ENUM lists MAS event types scheduled through scheduler interfaces.
 * <br>
 * <B>Special event types for IWD with NTF have to be listed in common MoipMessageEntities and redefined
 * in the list using the same name
 *
 */
public enum EventTypes {

    SLAM_DOWN(MoipMessageEntities.SERVICE_TYPE_SLAMDOWN),
    MCN(MoipMessageEntities.SERVICE_TYPE_MCN),
	LOG_OUT(MoipMessageEntities.SERVICE_TYPE_LOGOUT_SUBSCRIBER),
	MWI_OFF(MoipMessageEntities.SERVICE_TYPE_MWI_OFF),
	MAILBOX_UPDATE(MoipMessageEntities.SERVICE_TYPE_MAILBOX_UPDATE),
	QUOTA_WARNING(MoipMessageEntities.SERVICE_TYPE_QUOTA),
	MSG_EXPIRY(MoipMessageEntities.SERVICE_TYPE_MSG_EXPIRY),
	GREETING_CHANGED(MoipMessageEntities.SERVICE_TYPE_GREETING_UPDATED),
	READ_EXPIRE("rexpir"),
	SAVE_EXPIRE("sexpir"),
	INTERNAL_TIMER("timer"),
	ALERT_SC(MoipMessageEntities.SERVICE_TYPE_ALERT_SC),
	DELIVERY("delivery"),
	FAX_PRINT("faxprint"),
	VVA_SMS(MoipMessageEntities.SERVICE_TYPE_VVA_SMS),
	DELAYED_EVENT(MoipMessageEntities.SERVICE_TYPE_DELAYED_EVENT),
	AUTO_UNLOCK_PIN(MoipMessageEntities.SERVICE_TYPE_AUTO_UNLOCK_PIN),
	SUBSCRIBER_ACTIVITY_DETECTED("subact"),
	SUBSCRIBER_VVM_SYSTEM_DEACTIVATED("subVvmSystemDeactivated", "subvvmdact"),
	SUBSCRIBER_VVM_IMAP_ACTIVITY_DETECTED("subImapAct"),
    SEND_NOTIFICATOIN_TYPE("sendNotification", "sndnotif")
	;

	private String name;
    private String performanceProfilingName = null;

	private static final Map<String, EventTypes> lookup = new Hashtable<String, EventTypes>();

	static {
		for (EventTypes type : EventTypes.values()) {
			lookup.put(type.getName().toLowerCase(), type);
		}
	}

	EventTypes(String name) {
		this.name = name;
	}
	
	EventTypes(String name, String performanceProfilingName) {
        this.name = name;
        this.performanceProfilingName = performanceProfilingName;
    }

	public String getName() {
		return name;
	}
	
	public String getPerformanceProfilingName() {
        return (performanceProfilingName == null ? name : performanceProfilingName);
    }

	public static EventTypes get(String name) {
		return lookup.get(name.toLowerCase());
	}
}
