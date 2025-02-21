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

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.ntf.text.Phrases;


/**
 * This class defines the delayed event of type {@link com.abcxyz.services.moip.ntf.event.DelayedEvent.DelayedEventType#DELAYEDSMSREMINDER}.
 *
 * @author ewenxie
 * @since vfe_nl33_mfd02  2015-07-14
 */
public class DelayedSMSReminder extends DelayedEvent {
    //CONSTANTS
    public static final String NTF_CONTENT = "ntf_content";
    public static final String STATUS_FILE_EVT_NAME= "SmsReminder_" ;
    public static final String DEFAULT_CONTENT="DelayedSmsReminder";


    //VARIABLES
    private static LogAgent log = NtfCmnLogger.getLogAgent(DelayedSMSReminder.class);
    protected String ntfContent; //the content phrase for this type of SMS, which is configured in the *.cphr file.

    public DelayedSMSReminder(Properties eventProperties) {
        super(eventProperties);
        delayedEventType = DelayedEventType.DELAYEDSMSREMINDER;
        ntfContent = eventProperties.getProperty(NTF_CONTENT);

        if (ntfContent == null) {
            ntfContent = DEFAULT_CONTENT; //default.
        } else if (!Phrases.isCphrPhraseFound("en", ntfContent, null)) {
            log.warn(ntfContent + " is not found in en.cphr file. Will abort processing this delayedevent");
            valid = false;
            return;
        }

        StringBuilder statusFileNameBuilder = new StringBuilder();

        statusFileNameBuilder.append(STATUS_FILE_PREFIX)
                             .append(STATUS_FILE_EVT_NAME)
                             .append(ntfContent)
                             .append(STATUS_FILE_EXTENSION);

        statusFileName = statusFileNameBuilder.toString();
    }

    public String getNtfContent() {
        return ntfContent;
    }

    public String getStatusFileName() {
        return statusFileName;
    }

}
