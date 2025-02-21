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

package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.sms.SMSAddressInfo;


/**
 * 
 * This class is a container to contain information needed for NTF
 * to send a cancel SMS, used for feedback to NTF's cancel Handler
 * about each SMS which needs to be cancelled.
 *
 */
public class CancelInfo {
    
    /**
     *
     * CancelType is the type of SMS Cancel that will be sent depending on the
     * constructor of CancelInfo.
     * 
     * It also depends on NTF's cancel configuration of what is actually used.
     * 
     * The source address must always be set so source Address is implied for other types.
     * 
     * There are really only two types of cancel, with a source address, or with a service_type
     * or both combined.  The other types in the table just tell NTF where to find the service TYPE.
     * in the table (notification.conf) ReplaceNotifications.List.
     * 
     * SOURCEONLY  - only cancel using source address and destination.
     * POSITION    - Type of service type replace but based on the position in list
     * CONTENT     - Use the CPHR content type to look up the service type in the list
     * SERVICETYPE - Use the ServiceType directly.
     */
    public enum CancelType {
        SOURCEONLY,
        POSITION,
        CONTENT,
        SERVICETYPE
    }
    
    protected SMSAddressInfo destination = null;
    protected SMSAddressInfo source = null;
    protected String smppServiceType = null;
    protected int cancelPosition = -1;
    protected String content = null;   
    
    protected CancelType cancelType = CancelType.SOURCEONLY;
    
    
    /**
     * @return CancelType of this CancelInfo see CancelType.
     */
    public CancelType getCancelType() {
        return cancelType;
    }


    /**
     * @return the destinationAddress.
     */
    public SMSAddressInfo getDestination() {
        return destination;
    }

   
    
    /**
     * @return the source address.
     */
    public SMSAddressInfo getSource() {
        return source;
    }

    
    /**
     * @return the service type.
     */
    public String getSmppServiceType() {
        return smppServiceType;
    }
 
    
    /**
     * @return the content name, CPHR phrase.
     */
    public String getContent() {
        return content;
    }

    /**
     * Cancel only by source and destination, sets type to CancelType.SOURCEONLY.
     * If NTF is configured to Cancel only by service Type an error will be seen in logs and
     * no cancel will be sent.
     *  
     * See Cm.cancelSmsMethod in notification.xsd/conf
     * 
     * @param source the source address.
     * @param destination the destination address.
     */
    public CancelInfo(SMSAddressInfo source, SMSAddressInfo destination) {
        this.source=source;
        this.destination=destination;
        cancelType = CancelType.SOURCEONLY;
    }
    
    /**
     * Cancel by serviceType, source and Destination.
     * Depends on NTF cancel Configuration on what is actually used.
     * 
     * See Cm.cancelSmsMethod in notification.xsd/conf
     * 
     * @param source the source address.
     * @param destination the destination address.
     */
    public CancelInfo(SMSAddressInfo source, SMSAddressInfo destination, String smppServiceType) {
        this.source=source;
        this.destination=destination;
        this.smppServiceType=smppServiceType;
        cancelType = CancelType.SERVICETYPE;
    }
    
    /**
     * Cancel by serviceType but lookup using content, source and destination.
     * Depends on NTF cancel Configuration on what is actually used.
     * 
     * See Cm.cancelSmsMethod in notification.xsd/conf
     * 
     * @param source the source address.
     * @param content the cphr content phrase - a lookup will be made in the ReplaceNotifications.List.
     * @param destination the destination address.
     */
    public CancelInfo(SMSAddressInfo source, String content, SMSAddressInfo destination) {
        this.source=source;
        this.destination=destination;
        this.content=content;
        cancelType = CancelType.CONTENT;
    }
    
    /**
     * 
     * Cancel by serviceType but using position to lookup, source and Destination.
     * Depends on NTF cancel Configuration on what is actually used.
     * @param source the source address.
     * @param destination the destination address.
     * @param cancelPosition the position in the ReplaceNotifications.List (notification.conf).
     */
    public CancelInfo(SMSAddressInfo source, SMSAddressInfo destination,int cancelPosition) {
        this.source=source;
        this.destination=destination;
        this.cancelPosition=cancelPosition;
        cancelType = CancelType.POSITION;
    }


    /**
     * @return the position in the 
     */
    public int getCancelPosition() {
        return cancelPosition;
    }
    
    /* (non-Javadoc)
     * Creates a string representation of a CancelInfo
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuilder out = new StringBuilder();
        out
        .append("CancelInfo{ ")
        .append("CancelType:[")
        .append(cancelType)
        .append("]")
        .append(", Source:[")
        .append(source)
        .append("]")
        .append(", Destination:[") 
        .append(destination)
        .append("]");

        switch (cancelType) {
            case CONTENT:
                out
                .append(", content:[")
                .append(content)
                .append("]");
                break;
            case POSITION:
                out
                .append(", position:[ ")
                .append(cancelPosition)
                .append("]");
                break;
            case SERVICETYPE:
                out
                .append(", serviceType:[")
                .append(smppServiceType)
                .append("]");
                break;
        }
        out.append("}");

        return out.toString();
    }
    
}

