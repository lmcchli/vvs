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
 * Informs information needed about a Cancel request.
 * This is simply the subscriber number, so a database lookup can
 * be made in order to assemble destination number(s) etc.
 */
public  interface ICancelRequest {
    
    /**
     * @return the Subscriber number that the cancel concerns
     */
    public String getSubscriberNumber();
    
}
