/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.util;

/**
 * An interface to provide access to the MDR generation for customized notification or PhoneOn
 */
public interface INotifierMdrGenerator {

    /**
     * Sends information about a successful notification information delivery to MER.
     * @param receiver the receiver of the notification (the UserName attribute).
     * @param portType the type of delivery interface (the SAS-Port-Type attribute).
     * @param reasonDetail the detailed reason for the generation of an MDR (the Event-Reason-Detail attribute).
     */
    public void generateMdrDelivered(String receiver, int portType, String reasonDetail);
    
    /**
     * Sends information about an expired notification information delivery to MER.
     * @param receiver the receiver of the notification (the UserName attribute).
     * @param portType the type of delivery interface (the SAS-Port-Type attribute).
     * @param reasonDetail the detailed reason for the generation of an MDR (the Event-Reason-Detail attribute).
     */
    public void generateMdrExpired(String receiver, int portType, String reasonDetail);
    
    /**
     * Sends information about a failed notification information delivery to MER.
     * @param receiver the receiver of the notification (the UserName attribute).
     * @param portType the type of delivery interface (the SAS-Port-Type attribute).
     * @param reasonDetail the detailed reason for the generation of an MDR (the Event-Reason-Detail attribute).
     * @param message message describing the reason for failure, or null (the Event-Description attribute).
     */
    public void generateMdrFailed(String receiver, int portType, String reasonDetail, String message);
    
    /**
     * Sends information about a successful notification information delivery to MER.
     * @param receiver the receiver of the notification (the UserName attribute).
     * @param portType the type of delivery interface (the SAS-Port-Type attribute).
     * @param reasonDetail the detailed reason for the generation of an MDR (the Event-Reason-Detail attribute).
     * @param message message describing the reason for failure, or null (the Event-Description attribute).
     */
    public void generateMdrDiscarded(String receiver, int portType, String reasonDetail, String message);

    /**
     * Sends information about successful phone on requests.
     * @param receiver the receiver of the notification (the UserName attribute).
     */
    public void generateMdrPhoneOnDelivered(String receiver);
    
}
