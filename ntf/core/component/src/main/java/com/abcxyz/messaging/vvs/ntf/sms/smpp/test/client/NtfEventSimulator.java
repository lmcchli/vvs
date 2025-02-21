package com.abcxyz.messaging.vvs.ntf.sms.smpp.test.client;

import java.util.Properties;

import com.abcxyz.messaging.scheduler.EventID;
import com.abcxyz.messaging.scheduler.InvalidEventIDException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.generic.NotifierEvent;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.generic.NotifierType.NotifierTypeState;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.text.TestUser;
import com.mobeon.ntf.userinfo.SmsFilterInfo;

public class NtfEventSimulator {
	private NotifierEvent textMailNotifierEvent = null;
	private NotifierEvent incomingCallNotificationNotifierEvent = null;
	private NotifierEvent newMessageDepositNotifierEvent = null;
	private NotifierEvent readMessageNotificationNotifierEvent = null;
	private NotifierEvent fmcNotifierEvent = null;
	private NotificationEmail notificationEmail = null;
	private TestUser testUser = null;
	private SmsFilterInfo smsFilterInfo = null;
	private UserMailbox userMailbox = null;
	private SMSAddress sourceSMSAddress = null;
	private int validity = 24;
	private int type = 0;
	private boolean isRoamingFilterUsed = false;

	NtfEventSimulator() {
		this.textMailNotifierEvent = new NotifierEvent(getNtfEventPropertiesForGivenNotifierType("tmail"));
		this.incomingCallNotificationNotifierEvent = new NotifierEvent(getNtfEventPropertiesForGivenNotifierType("slmdwn"));
		this.newMessageDepositNotifierEvent = new NotifierEvent(getNtfEventPropertiesForGivenNotifierType("nmd"));
		this.readMessageNotificationNotifierEvent = new NotifierEvent(getNtfEventPropertiesForGivenNotifierType("rmn"));
		this.fmcNotifierEvent = new NotifierEvent(getNtfEventPropertiesForGivenNotifierType("fmc"));
		this.textMailNotifierEvent.setNotifierTypeState(NotifierTypeState.STATE_INITIAL);
		
		//TODO: Need to fix since NotifierEvent no longer inherits from NtfEvent.
		//this.notificationEmail = new NotificationEmail(textMailNotifierEvent);
        this.notificationEmail = new NotificationEmail(new NtfEvent(""));
		
        notificationEmail.setEmailType(Constants.NTF_VOICE);
        notificationEmail.setSenderPhoneNumberTest("PAYPHONE");

		// "To" subscriber
		this.testUser = new TestUser();
		customizeUserInfo();
		// SMPP.Destination_addr
		this.smsFilterInfo = new SmsFilterInfo(getSmsFilterProperties(),
				new String[] { this.testUser.getTelephoneNumber() }, null, null);
		this.userMailbox = new UserMailbox(1, 0, 0, 0, 0, 0, 0, 0, true);
		this.sourceSMSAddress = new SMSAddress(4, 0, "819037281111");
		// SMPP.From
		this.validity = 24;
		this.type = 0;
		this.isRoamingFilterUsed = false;
	}

	NotifierEvent getTextMailNotifierEvent() {
		return this.textMailNotifierEvent;
	}
	
	NotifierEvent getIncomingCallNotifcationNotifierEvent(){
	    return this.incomingCallNotificationNotifierEvent;
	}
	
	NotifierEvent getNewMessageDepositNotifierEvent(){
	    return this.newMessageDepositNotifierEvent;
	}
	
	NotifierEvent getReadMessageNotificationNotifierEvent(){
	    return this.readMessageNotificationNotifierEvent;
	}
	
	NotifierEvent getFMCNotiferEvent(){
	    return this.fmcNotifierEvent;
	}
	
	NotificationEmail getNotificationEmail() {
		return this.notificationEmail;
	}

	TestUser getUserInfo() {
		return this.testUser;
	}

	SmsFilterInfo getSmsFilterInfo() {
		return this.smsFilterInfo;
	}

	UserMailbox getUserMailbox() {
		return this.userMailbox;
	}

	SMSAddress getSourceSMSAddress() {
		return this.sourceSMSAddress;
	}

	int getValidity() {
		return this.validity;
	}

	int getType() {
		return this.type;
	}

	boolean isRoamingFilterUsed() {
		return this.isRoamingFilterUsed;
	}
	
	Properties getNtfEventPropertiesForGivenNotifierType(String serviceType){
	    Properties properties = null;
        try {
            EventID eventId = new EventID(
                    "tn3003/20120718-12h00/100_10204000602-3a516598d5354296b6355e71bd79a087-3003.nmd@sending-nmd@sending;omsg=043c7e;try=1;exp=1342543300920;nnb=10204000602;rmsg=ecee05;snb=10204000602;omsa=msid:7de5522716982450;rmsa=msid:eeef3aba414954c4;ntft="+serviceType+";notifiertypestate=initial");
            properties = eventId.getEventProperties();
            SMPPTestClientLogger
                    .writeLogMessageToFile("NtfEventSimulator.getNtfEventProperties(): The event id to be used for the NTFEvent is: tn3003/20120718-12h00/100_10204000602-3a516598d5354296b6355e71bd79a087-3003.nmd@sending-nmd@sending;omsg=043c7e;try=1;exp=1342543300920;nnb=10204000602;rmsg=ecee05;snb=10204000602;omsa=msid:7de5522716982450;rmsa=msid:eeef3aba414954c4;ntft="+serviceType+";notifiertypestate=initial.\n");
        } catch (InvalidEventIDException e) {
            SMPPTestClientLogger.writeLogMessageToFile(e.getMessage());
        }
        return properties;
	}
	
	Properties getSmsFilterProperties() {
		String phraseToUse = "s";
		Properties properties = new Properties();
		properties.setProperty("SMS", phraseToUse);
		SMPPTestClientLogger
		.writeLogMessageToFile("NtfEventSimulator.getNtfEventProperties(): The prase that will be used from the template file is the one corresponding to "+phraseToUse+".\n");
		return properties;
	}

	void customizeUserInfo() {
		this.testUser.setTelephoneNumber("819037282222");
		this.testUser.setTON(4);
		this.testUser.setNPI(0);
		// Subscriber To: language (en/ja)
		this.testUser.setPreferredLanguage("ja");
		SMPPTestClientLogger
				.writeLogMessageToFile("NtfEventSimulator.getNtfEventProperties(): Created a user with the following information: {telephone=9037282222,TON=4,NPI=1,LAN=ja}.\n");
	}
}
