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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel.ANotifierCancelRequestProcessor;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel.CancelFeedBack;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel.ICancelRequest;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel.ICancelSmsEventRouter;



/**
 * 
 * This class allows a notifier plug-in to register with NTF's internal
 * cancel handler.
 * 
 * When an MWI or update event is received any registered plug-in will
 * be called to collect information about any source/destination/serviceType
 * that the plug-in wants to cancel at the same time.
 * 
 *  This allows a more efficient way to send cancel as duplicate cancel
 *  SMS will be filtered via the plug-in.
 *
 * @author lmcmajo
 */
public class CancelSmsPluginDistributor implements ICancelSmsEventRouter {


    Set<ANotifierCancelRequestProcessor> receivers = Collections.newSetFromMap(new ConcurrentHashMap<ANotifierCancelRequestProcessor, Boolean>());
    
    private static CancelSmsPluginDistributor _inst = null;
    
    public static CancelSmsPluginDistributor get() {
        if (_inst  == null) {
            _inst = new CancelSmsPluginDistributor();
        }
        return _inst;
    }
    
    
    private CancelSmsPluginDistributor() {
        //private as singleton class.
    }
    
    public void register(ANotifierCancelRequestProcessor cancelProcessor) {
        receivers.add(cancelProcessor);
    }
    
    public CancelFeedBack distributeCancel(ICancelRequest request) {
        Iterator<ANotifierCancelRequestProcessor> recIter = receivers.iterator();
        CancelFeedBack cancelFeedBack = new CancelFeedBack();
        while (recIter.hasNext()) {
            CancelFeedBack fb = recIter.next().process(request);
            if (fb != null ) {
                cancelFeedBack.add(fb);
            }
        }        
        return cancelFeedBack;        
    }
    
    public boolean isActive() {
        return !receivers.isEmpty();
    }
}
