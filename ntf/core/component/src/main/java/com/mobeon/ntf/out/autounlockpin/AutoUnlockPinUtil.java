/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2013.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */

package com.mobeon.ntf.out.autounlockpin;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;


/**
 * Utility methods used for the AutoUnlockPin feature.
 *
 */
public class AutoUnlockPinUtil {

    public static long parseEventTime(String timeAsString) throws ParseException {
        
        Date date = MfsEventManager.dateFormat.get().parse(timeAsString);
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.set(Calendar.MILLISECOND, 0); //remove millisecond since MOIPUserLastPinLockout in user profile does not contain millisecond
        return c.getTimeInMillis();
    }
    
    /**
     * 
     * @param event NTF Event being processed
     * @return true if this Auto Unlock Pin event is for the Unlocking state
     */
    public static boolean isUnlockNeeded(NtfEvent event) {
        
        String locked = event.getProperty(NtfEvent.AUTO_UNLOCK_PIN_LOCKED);
        
        return "1".equals(locked);
    }
    
}
