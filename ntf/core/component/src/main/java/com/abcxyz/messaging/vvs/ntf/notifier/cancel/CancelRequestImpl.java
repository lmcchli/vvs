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

package com.abcxyz.messaging.vvs.ntf.notifier.cancel;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel.ICancelRequest;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseSubscriberProfile;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;


/**
 * 
 *
 * @author lmcmajo
 */
public class CancelRequestImpl implements ICancelRequest {
    
    
    private String subscriber;

    public CancelRequestImpl(String subscriber) {
        this.subscriber=subscriber;
    }

    /* (non-Javadoc)
     * @see com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel.ICancelRequest#getSubscriberNumber()
     */
    @Override
    public String getSubscriberNumber() {
        return subscriber;
    }

}
