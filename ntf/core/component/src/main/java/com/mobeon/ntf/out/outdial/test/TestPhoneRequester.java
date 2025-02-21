/*
 * TestPhoneRequester.java
 *
 * Created on den 27 september 2004, 11:18
 */

package com.mobeon.ntf.out.outdial.test;

import com.mobeon.common.smscom.*;
import com.mobeon.ntf.out.outdial.*;
import com.mobeon.common.storedelay.SDLogger;
import com.mobeon.ntf.userinfo.UserInfo;

import java.util.*;

/**
 * A class that pretends handling phone on checking.
 * For now; always report that phone is on immediately.
 */
public class TestPhoneRequester
    implements PhoneOnRequester
{

    private PhoneOnMap phoneMap;
    private PhoneOnEventListener listener;
    private List requestList = new ArrayList();
    private int count = 0;
    private boolean countOnly = false;

    /** Creates a new instance of TestPhoneRequester */
    public TestPhoneRequester(PhoneOnMap phoneMap)
    {
        this.phoneMap = phoneMap;
    }

    public void setCountOnly(boolean countOnly)
    {
        this.countOnly = countOnly;
    }

    public List getRequestList()
    {
        return requestList;
    }

    public void clearRequestList()
    {
        requestList = new ArrayList();
        count = 0;
    }

    public int getCount()
    {
        return count;
    }


    public void request(UserInfo userInfo,
                        String requestNumber,
                        String userEmail)
    {
        SDLogger.log(SDLogger.DEBUG, "PhoneOn - Got request for " + requestNumber);
        phoneMap.add(requestNumber, userEmail);
        count++;
        if (!countOnly) {
            requestList.add(requestNumber + "," + userEmail);
        }
        SMSAddress smsAdress = new SMSAddress(0, 0, requestNumber);
        PhoneOnEvent ev = new PhoneOnEvent(this, smsAdress, true, "OK");
        listener.phoneOn(ev);
    }

    public void setListener(PhoneOnEventListener listener)
    {
        this.listener = listener;
    }

    public void clear(String requestNumber) {
    }
    
}
