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
 * This class enables a plug-in to register itself to be informed of a cancel in progress from
 * NTF for a given subscriber.
 * 
 * Extend this class and implement process in order to provide feedback to NTF about what
 * to cancel from the plug-in perspective.
 * 
 * Note: if the plug-in is overriding MWI-OFF and or update events depending on which event 
 * NTF is configured to inititiate a cancel on , this will not be called and
 * the plug-in instead will have to send the cancel directly if required.
 * 
 * In order to register your class see: @see #ICancelSmsEventRouter
 * 
 * You will receive a IcancelRequest  @see #ICancelRequest which will inform you of the subscriber that a cancel
 * has been  received.
 * You must return a CancelFeedback @see #CancelFeedBack or null informing of what you wish
 * to cancel.
 * 
 */
public abstract class ANotifierCancelRequestProcessor {
    /**
     * Handles a phone on event.
     * @param cancelRequest - the ICancelRequest object containing the information about the cancel request.
     */

    public abstract CancelFeedBack process(ICancelRequest cancelRequest);
   
}
