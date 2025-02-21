package com.mobeon.ntf.reminder;

import com.mobeon.ntf.out.sms.SMSOut;
import com.mobeon.ntf.userinfo.SmsFilterInfo;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.common.smscom.SMSAddress;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: mnify
 * Date: 2007-apr-11
 * Time: 14:37:54
 * To change this template use File | Settings | File Templates.
 */
public class ReminderCallerImpl implements Constants, ReminderCaller {
    private static SMSOut smsOut;

    static {
        smsOut = SMSOut.get();
    }

    public void send(ReminderInfo info, UserInfo user, UserMailbox inbox ) {
         new CallHandler().send(info, user, inbox);
    }

    private class CallHandler {

        private void send(ReminderInfo info, UserInfo user, UserMailbox inbox) {


            // skapa smsfilterinfo med nummer, type av sms och content.
            Properties props = new Properties();
            SmsFilterInfo filterInfo = null;
            String[] numbers = user.getFilter().getNotifNumbers("SMS",info.getUserNotifEmail());
            if( Config.isUnreadMessageReminderFlash() ) {
                numbers = user.getFilter().getNotifNumbers("FLS",info.getUserNotifEmail());
                props.put("FLS", REMINDERSMSNAME);
                filterInfo = new SmsFilterInfo(props, null, null, numbers);
            } else {
                props.put("SMS", REMINDERSMSNAME);
                filterInfo = new SmsFilterInfo(props, numbers, null, null);
            }

            SMSAddress source = Config.getSourceAddress(REMINDERSMSNAME, user.getCosName());
            int validity = user.getValidity(REMINDERSMSNAME);
            NotificationEmail storedMail = null;
            if( inbox.getNewTotalCount() == 1 ) {
                //HUZH 20090309 redesign this
                //storedMail = inbox.getFirstUnseenMail();

            }
            smsOut.handleSMS(user, filterInfo, null, storedMail, inbox, source, validity, 0);

        }


    }
}
