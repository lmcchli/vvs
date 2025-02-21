/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.outdial.test;

import java.util.*;

import com.mobeon.common.storedelay.SDLogger;
import com.mobeon.ntf.out.outdial.*;


import com.mobeon.ntf.userinfo.UserInfo;

public class TestOdlCaller implements OdlCallSpec
{


    private HashMap resultMap = new HashMap(); // User -> List(Integer)
    /** Calls made via the outcaller */
    private List calls = new ArrayList(); // Phone numbers
    private int count = 0;
    private boolean countOnly;
    private long callTime = 1000; // How long to make a call take

    public TestOdlCaller()
    {
    }


    public void setCountOnly(boolean countOnly)
    {
        this.countOnly = countOnly;
    }

    public void setCallTime(long callTime)
    {
        this.callTime = callTime;
    }

    public List getCalls()
    {
        return calls;
    }

    public void clearCalls()
    {
        calls = new ArrayList();
        count = 0;
    }

    public int getCount()
    {
        return count;
    }

    public void setResponseCode(Object user, int code)
    {
        List results = (List) resultMap.get(user);
        if (results == null) {
            results = new LinkedList();
        }
        results.add(new Integer(code));
        resultMap.put(user, results);
    }

    private int getCode(Object user, int defValue)
    {
        List results = (List) resultMap.get(user);
        if ((results == null) || (results.size() == 0)) return defValue;
        Integer nextRes = (Integer) results.get(0);
        results.remove(0);
        return nextRes.intValue();
    }


    /**
     *Sends a call to OutdialNotification server via XMP. Returns at once and result is handled in
     *the listener.
     *@param number - The Number to call.
     *@param user - The user to handle.
     *@param listener - Where to put callbacks.
     */
    public void sendCall(String number, UserInfo user, OdlCallListener listener)
    {
        SDLogger.log(SDLogger.DEBUG, "OdlCaller - Calling " + number + " for " + user);
        count++;
        if (!countOnly) calls.add(number);
        // get esi data
        int code = getCode(number, 200);
        SDLogger.log(SDLogger.DEBUG, "OdlCaller - Using Code = " + code);
        if (code >= 900) {
            listener.handleResult(number, user, code);
        } else {
            // Pretend to call first
            try {
                Thread.sleep(callTime);
            } catch (Exception ignore) {
            }
            ;
            listener.handleResult(number, user, code);
        }


    }

}
