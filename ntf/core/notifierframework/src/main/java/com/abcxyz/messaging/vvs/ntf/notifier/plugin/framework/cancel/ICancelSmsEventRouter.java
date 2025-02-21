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


/**
 * 
 * The ICancelSmsEvent defines the method that the Notifier plug-in can invoke to obtain
 * access to requests to cancelSM
 *
 * @author lmcmajo
 */
 public interface  ICancelSmsEventRouter {
    /**
     * Registers a processor for cancel Requests received from NTF
     * If you wish your Thread to be able to provide information about
     * an SMS to Cancel upon request.
     * @param CancelReceiver the phone on event receiver 
     */
    public void register(ANotifierCancelRequestProcessor CancelReceiver);
   
}
