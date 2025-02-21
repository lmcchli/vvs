/**
 * Copyright (c) Abcxyz
 * All Rights Reserved
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import java.util.Properties;

import com.abcxyz.messaging.scheduler.handling.AppliEventInfo;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.schedule.INotifierEventInfo;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;


public class NotifierEventInfo implements INotifierEventInfo {
    
    private AppliEventInfo appliEventInfo = null;
    
    public NotifierEventInfo(AppliEventInfo appliEventInfo) {
        this.appliEventInfo = appliEventInfo;
    }
    
    @Override
    public String getEventId() {
        return appliEventInfo.getEventId();
    }
    
    @Override
    public String getEventType() {
        return appliEventInfo.getEventType();
    }

    @Override
    public Properties getEventProperties() {
        return appliEventInfo.getEventProperties();
    }
    
    @Override
    public boolean isExpire() {
        return appliEventInfo.isExpire();
    }
    
    @Override
    public boolean isLastExpire() {
        return appliEventInfo.isLastExpire();
    }
    
    @Override
    public boolean isNextRetryScheduled() {
        return (appliEventInfo.getNextEventInfo() != null);
    }    
    
    @Override
    public String getNextEventId() {
        return (appliEventInfo.getNextEventInfo() != null ? appliEventInfo.getNextEventInfo().getEventId() : null);
    }
    
    @Override
    public int getNumberOfTried() {
        return appliEventInfo.getNumberOfTried();
    }

    @Override
    public boolean isEventIdsMatching(String eventId) {
        //return CommonMessagingAccess.getInstance().compareEventIds(appliEventInfo, eventId);
        /*** 
         * HS80034: 
         * This method is named isEventIdsMatching() and it is calling CommonMessagingAccess.getInstance().compareEventIds(appliEventInfo, eventId);
         * However, if we exam the caller (NTF SMS notification related functions), their real intention is not to compare eventId, event key 
         * (which is a subset of eventId). Example:
         * 
         *  firedEvent:  tn3002/20141204-15h30/150_10307001401-1240c5fd7bef4b3983dd945324990af1-3002.UpdateSMS@sending-Expir;
         *               template_name=updatesms;sender=;sendonlyifunreadmessages=FALSE;nnb=10307001401;rmsg=3003;exp=1417725150046;
         *               rmsa=msid:b44701d99d0830a6;senderdname=;try=0;rnb=10307001401;accesstype=10;sender_visibility=true;
         *               RecipientId=10307001401;asf=true;cphrType=mailboxSubscriber;sendNotif=true;service_type=updateSMS;
         *               omsg=rdc7be368e44f3003;date=1417725029754;omsa=msid:b44701d99d0830a6;sendmultipleifretry=FALSE;
         *               callednumber=10307001401;ntn=UpdateSMS
         *  storedEvent: tn3002/20141204-15h30/90_10307001401-1240c5fd7bef4b3983dd945324990af1-3002.UpdateSMS@sending-UpdateSMS@sending;
         *               template_name=updatesms;sender=;sendonlyifunreadmessages=FALSE;nnb=10307001401;rmsg=3003;exp=1417725150046;
         *               rmsa=msid:b44701d99d0830a6;senderdname=;try=1;rnb=10307001401;accesstype=10;sender_visibility=true;
         *               RecipientId=10307001401;asf=true;cphrType=mailboxSubscriber;sendNotif=true;service_type=updateSMS;
         *               omsg=rdc7be368e44f3003;date=1417725029754;omsa=msid:b44701d99d0830a6;sendmultipleifretry=FALSE;
         *               callednumber=10307001401;ntn=UpdateSMS
         *   
         * These 2 events have different eventIds (eventId is the whole string) - in fact they are 2 different events, but they have the same eventkey
         * which is "10307001401-1240c5fd7bef4b3983dd945324990af1"
         * 
         * Before HS80034, the method CommonMessagingAccess.compareEventIds(appliEventInfo, eventId) was implemented really in a way to compare
         * the eventKeys instead of eventIds, which suite the purpose of this method's callers (NTF SMS notification related functions), but it
         * doesn't reflect the method name properly and, more importantly, it causes other functions which really meant to compare eventIds to fail
         * - hence TR HS80034
         * 
         * So now since we fix HS80034 to make CommonMessagingAccess.compareEventIds(appliEventInfo, eventId) to really compare event IDs (not Keys),
         * we must fix here to call the newly added method: CommonMessagingAccess.compareEventKeys(appliEventInfo, eventId)    
         * 
         * Ideally, we shall change the INotifierEventInfo interface by adding a method isEventKeysMatching(String eventId), and add the overwriting
         * impl here, and change the relevant callers (NTF SMS notification related functions) to call the new method 
         * INotifierEventInfo.isEventKeysMatching(String eventId)
         * But the problem with that ideal way of change is that one of the callers is inside the KDDI plugin (mio_plugin/kddi), which means if we
         * change INotifierEventInfo by adding isEventKeysMatching(String eventId), we have to change kddi plugin, which result in having to deliver
         * plugin package to kddi. To avoid that, we now go with this middle ground to not changing the interface but only change the impl here
         * (which is not ideal, and which makes the name of the method and its content a bit mismatching)  
         *  
         */
        return CommonMessagingAccess.getInstance().compareEventKeys(appliEventInfo, eventId);
    }

}
