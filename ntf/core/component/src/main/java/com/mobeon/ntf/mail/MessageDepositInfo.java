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

package com.mobeon.ntf.mail;

import java.util.StringTokenizer;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.mfs.statefile.StateFile;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.abcxyz.services.moip.ntf.event.NtfEvent;


/**
 * NTF internal representation of an MFS message. 
 * 
 * This class is used to validate if NotificationFilter are still valid on level-3 retry. 
 *
 * @author ejulgri
 */
public class MessageDepositInfo extends AMessageDepositInfo {

    private StateFile stateFile = null;
    private String recipient = null;
    
    /**
     * Constructor 
     * @param state StateFile of the message deposit
     * @param recipient user recipient number
     */
    public MessageDepositInfo(StateFile state, String recipient) {
        log = NtfCmnLogger.getLogAgent(MessageDepositInfo.class);
        this.stateFile = state;
        this.recipient = recipient;
    }
    
    
    /**
     * Initialize the NotificationMessage with the give state file. 
     * This method does not initialize information about the receiver since it is not required for NotificationFilter validation.
     */
    public void init() {
        setSenderVisible(stateFile);
        setFrom(stateFile);
        setReceiver(stateFile); 
        setSubject(stateFile);
        setReceivedDate(stateFile);
        //setMessageSize(); 
        setUrgent(stateFile);
        setConfidential(stateFile);
        setDefaultEmailType(stateFile);
        setUid(stateFile);
        setSenderPhoneNumber();
        if (receiver == null) {
            receiver = recipient;
        }
        setReceiverPhoneNumber();
    }

    public String getReceiver() {
        return receiver;
    }
    
    private void setReceiver(StateFile stateFile) {
        String to = stateFile.getC1Attribute(Container1.To);

        receiver = to;
        
        String rcip = recipient;
        
        //rcip can be null, needed for the distribution list feature
        if(rcip != null){
            rcip = rcip.replaceFirst("^0*", "");
            if (to.contains(";") && to.contains(rcip)) {
                StringTokenizer tokenizer=new StringTokenizer(to,";");
                while (tokenizer.hasMoreTokens()){
                    String theRcip=tokenizer.nextToken();
                    if (theRcip.contains(rcip)){
                        receiver=theRcip;
                        break;
                    }
                }
            }
        }
    }

    
    @Override
    public NtfEvent getNtfEvent() {

        return null;
    }


}
