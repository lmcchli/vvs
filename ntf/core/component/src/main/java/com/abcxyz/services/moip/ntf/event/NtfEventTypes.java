package com.abcxyz.services.moip.ntf.event;

import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;

/**
 * listing all NTF supported service types and event types
 * <br>
 * <B> IWD service types have to be defined in common and be referred from the list
 */
public enum NtfEventTypes {
	// Level 2 service event types, have to use same same as defined in back-end. Since they are used for IWD
    DEFAULT_NTF(MoipMessageEntities.MESSAGE_SERVICE_NTF),
    EVENT_TYPE_NOTIF("Notif"),
    SLAMDOWN(MoipMessageEntities.SERVICE_TYPE_SLAMDOWN),
    MCN(MoipMessageEntities.SERVICE_TYPE_MCN),
    MWI_OFF(MoipMessageEntities.SERVICE_TYPE_MWI_OFF),
    MWI_OFF_UNSUBSCRIBED(MoipMessageEntities.SERVICE_TYPE_MWI_OFF_UNSUBSCRIBED),
    ALERT_SC(MoipMessageEntities.SERVICE_TYPE_ALERT_SC),
    ROAMING(MoipMessageEntities.SERVICE_TYPE_ROAMING),
    MAILBOX_UPDATE(MoipMessageEntities.SERVICE_TYPE_MAILBOX_UPDATE),
    FAX_RECEIPT(MoipMessageEntities.SERVICE_TYPE_FAX_RECEIPT),
    FAX_PRINT(MoipMessageEntities.SERVICE_TYPE_FAX),
    VVA_SMS(MoipMessageEntities.SERVICE_TYPE_VVA_SMS),
    AUTO_UNLOCK_PIN(MoipMessageEntities.SERVICE_TYPE_AUTO_UNLOCK_PIN),
    SUBSCRIBER_VVM_SYSTEM_DEACTIVATED("subVvmSystemDeactivated"),
    SUBSCRIBER_VVM_SYSTEM_ACTIVITY_DETECTED("subVvmSystemActivityDetected"),
    SUBSCRIBER_VVM_IMAP_FIRST_DETECTED("subVvmImapFirstDetected"),
    SMS_REMINDER("smsreminder"),
    EVENT_TYPE_REMINDER("reminder"),
    AUTO_UNLOCK_PIN_L2("aupl2"),
    EVENT_AUTO_UNLOCK_PIN_L2("evtaupl2"),
    DELAYED_EVENT(MoipMessageEntities.SERVICE_TYPE_DELAYED_EVENT),//this type of events should be scheduled to happen in the future at the triggertime if the action is start

    // Level 3 service event types - Visual Voice Mail (VVM)
    VVM_L3("vvml3"),
    VVM_SMS_INFO("vvminfo"),
    VVM_SENDING_PHONEON("vvmunit"),
    VVM_WAIT_PHONEON("vvmwait"),
    VVM_DEACTIVATOR("vvmdeact"),
    VVM_ACTIVATOR("vvmactiv"),
    VVM_GREETING(MoipMessageEntities.SERVICE_TYPE_GREETING_UPDATED),
    VVM_EXPIRY(MoipMessageEntities.SERVICE_TYPE_MSG_EXPIRY),
    VVM_LOGOUT(MoipMessageEntities.SERVICE_TYPE_LOGOUT_SUBSCRIBER),

    // Level 3 service event types - Slamdown
    SLAMDOWN_L3("slml3"),
    SLAMDOWN_AGGREGATION("aggr"),
    SLAMDOWN_SMS_UNIT("smsunit"),
    SLAMDOWN_SMS_TYPE_0("smstype0"),
    SLAMDOWN_SMS_INFO("smsinfo"),


    FAX_L3("faxl3"),

    // Level 3 service event types - Outdial notification
    OUTDIAL("outdl"),
    EVENT_TYPE_ODL_START("Odlst"),
    EVENT_TYPE_ODL_LOGIN("Odllg"),
    EVENT_TYPE_ODL_CALL("Odlcl"),
    EVENT_TYPE_ODL_WAIT("Odlwt"),
    EVENT_TYPE_ODL_WAITON("Odlwton"),
    EVENT_TYPE_ODL_REMINDER("Odlreminder"),

    // Level 3 service event types - SIP MWI notification
    SIPMWI("sipmwi"),
    SIPMWI_REMINDER("sipmwireminder"),

    // Level 3 service event types - Fallback
    FALLBACK_L3("fallbackl3"),
    ;

	private String name;

	NtfEventTypes (String _name) {
		this.name = _name;
	}

	public String getName() {
		return name;
	}
}

