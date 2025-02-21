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

package com.abcxyz.services.moip.ntf.event;

import java.util.Properties;


/**
 *
 *
 * @author ewenxie
 */
public class DelayedEventFactory {
    public static DelayedEvent createDelayedEvent(Properties eventProperties){
        if(eventProperties != null){
            String delayedEventType = eventProperties.getProperty(DelayedEvent.DELAYED_EVENT_TYPE);
            if(delayedEventType != null){
                if(delayedEventType.equals(DelayedEvent.DelayedEventType.DELAYEDSMSREMINDER.type())){
                    DelayedEvent event = new DelayedSMSReminder(eventProperties);
                    if (event.isValid()) {
                        return event;
                    }
                }
            }
        }

        return null;
    }

}
