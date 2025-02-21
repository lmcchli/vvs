/* COPYRIGHT (c) Abcxyz Communications Inc. Canada (EMC), 2014.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property
 * of Abcxyz Communications Inc. Canada (EMC). The program(s) may
 * be used and/or copied only with the written permission from
 * Abcxyz Communications Inc. Canada (EMC) or in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 */

package com.mobeon.ntf.management;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState;


/**
 * 
 * This class is used to send state change information to notifier plugin's
 *
 * @author lmcmajo
 */
public class NotifierNtfAdminStateImpl implements INotifierNtfAdminState {

    private AdministrativeState admState;
    
    public NotifierNtfAdminStateImpl(AdministrativeState admState) {
        this.admState=admState;
    }
    //Don't allow default constructor.
    @SuppressWarnings("unused")
    private NotifierNtfAdminStateImpl() {};

    /* (non-Javadoc)
     * @see com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.INotifierNtfAdminState#get()
     */
    @Override
    public AdministrativeState get() {
        return admState;
    }
}
