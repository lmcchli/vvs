/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2015.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */

package com.mobeon.ntf.out.delayedevent;

import java.util.Properties;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.DelayedEvent;
import com.abcxyz.services.moip.ntf.event.DelayedSMSReminder;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NotificationGroup;
import com.mobeon.ntf.NotificationHandler;
import com.mobeon.ntf.NtfCompletedListener;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.text.TemplateMessageGenerationException;
import com.mobeon.ntf.userinfo.SmsFilterInfo;
import com.mobeon.ntf.userinfo.UserFactory;
import com.mobeon.ntf.userinfo.UserInfo;


/**
 * This class handles delayed event of type delayedsmsreminder by generating the SMS text based on
 * the ntfContent, and then sending the SMS to SMS-C.
 *
 * @author ewenxie
 * @author lmcmajo
 * @since vfe_nl33_mfd02  2015-07-14
 */
public class DelayedSmsReminderHandler implements Constants, NtfCompletedListener {
    private static LogAgent log =  NtfCmnLogger.getLogAgent(DelayedSmsReminderHandler.class);

    private SMSOut smsOut; //the smsOut handler for sending SMS.
    private DelayedEventHandler dlydEvtHandler;
    private MerAgent mer;

    private static DelayedSmsReminderHandler inst;

    /**
     * @return the single instance of DelayedSmsReminderHandler
     */
    public static DelayedSmsReminderHandler get() {
        if (inst != null) {
            return inst;
        }
        inst = new DelayedSmsReminderHandler();
        return inst;
    }


    private DelayedSmsReminderHandler() {
        smsOut = SMSOut.get();
        dlydEvtHandler = DelayedEventHandler.get();

        String compName = Config.getInstanceComponentName();
        if ((compName != null) && (compName.length() != 0)) {
            mer = MerAgent.get(compName);
        } else {
            mer = MerAgent.get();
        }
    }

    /**
     * Fetches the configured sender address for delayed SMS reminder, generates the SMS text according to the ntfContent
     * attribute of the event, and then sends the SMS to SMS-C, the results of sending will be handled by {@link #notifCompleted(NtfEvent)},
     * {@link #notifRetry(NtfEvent)} or {@link #notifFailed(NtfEvent)}.
     *
     * @param delayedSMSReminder the delayed event of type delayedsmsreminder.
     */
    public void process(DelayedSMSReminder delayedSMSReminder) {

        log.debug("Processing delayedsmsreminder: subscriberNumber=" + delayedSMSReminder.getSubscriberNumber() + ", ntfContent="
                + delayedSMSReminder.getNtfContent());

        NotificationEmail email = new NotificationEmail(delayedSMSReminder);
        try {
            email.init();
        } catch (MsgStoreException e1) {
            log.warn("Unable to initialise email due to exception+ ", e1);
            dlydEvtHandler.cancelDelayedEvent(delayedSMSReminder);
        }
        UserInfo user = UserFactory.findUserByTelephoneNumber(delayedSMSReminder.getSubscriberNumber());

        String[] smsNumbers = user.getFilter().getMatchingDeliveryProfileNumbers("SMS", 0, false);
        if (smsNumbers == null) {
            String notifNumber = user.getNotifNumber();
            if (notifNumber != null && !notifNumber.isEmpty()) {
                smsNumbers = new String[] { notifNumber };
            } else {
                smsNumbers = new String[] { email.getReceiverPhoneNumber() };
            }
        }

        Properties filterProp = new Properties();

        filterProp.put("SMS", delayedSMSReminder.getNtfContent());

        SmsFilterInfo filterInfo = new SmsFilterInfo(filterProp, smsNumbers, null);

        //set the depositType so that the sender address for this type of SMS can be fetched, if it's configured in the notification.conf file.
        email.setDepositType(Constants.depositType.DELAYED_SMS_REMINDER);
        SMSAddress src = NotificationHandler.getSourceAddressEmail(email, user.getCosName());
        log.debug("DelayedSmsReminderHandler.process(), source SMSAddress is: " + src);

        NotificationGroup ng = new NotificationGroup(this, email, log, mer);
        ng.addUser(user);

        int deliveryCount = 0;

        try {
            deliveryCount += smsOut.handleSendSMS(user, filterInfo, ng, email, email.getUserMailbox(), src, user.getNotifExpTime(),
                    DEFAULT_DELAY, NTF_DELAYED_NOTIFY);
            log.debug("DelayedSmsReminderHandler.process(), deliveryCount=" + deliveryCount);
            ng.setOutCount(user, deliveryCount);
            ng.noMoreUsers();
        } catch (TemplateMessageGenerationException e) {
            log.error("TemplateMessageGenerationException received in DelayedSmsReminderHandler.process().");
        }
    }

    @Override
    public void notifCompleted(NtfEvent event) {
        if (event instanceof DelayedSMSReminder) {
            DelayedEvent delayedEvent = (DelayedSMSReminder) event;
            if (log.isDebugEnabled()) {
                log.debug("DelayedSmsReminderHandler.notifCompleted(): successfully handled DelayedEvent: " + event.getEventProperties());
            }
            dlydEvtHandler.cancelDelayedEvent(delayedEvent);
        } else {
            log.error("Unhandled Event: " + event.getPersistentProperties());
        }


    }

    @Override
    public void notifRetry(NtfEvent event) {
        if (event instanceof DelayedSMSReminder) {
            DelayedEvent delayedEvent = (DelayedSMSReminder) event;
            if (log.isDebugEnabled()) {
                log.debug("DelayedSmsReminderHandler.notifRetry(): Will retry DelayedEvent according to schema: " + delayedEvent.getEventProperties());
            }
        }  else {
            log.error("Unhandled Event: " + event.getPersistentProperties());
        }

    }

    @Override
    public void notifFailed(NtfEvent event) {
        if (event instanceof DelayedEvent) {
            DelayedEvent delayedEvent = (DelayedEvent) event;
            if (log.isDebugEnabled()) {
                log.debug("DelayedSmsReminderHandler.notifFailed(): Failed to handle DelayedEvent and won't retry: " + delayedEvent.getEventProperties());
                dlydEvtHandler.cancelDelayedEvent(delayedEvent);
            } else {
                log.error("Unhandled Event: " + event.getPersistentProperties());
            }
        }
    }

}
