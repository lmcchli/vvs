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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;



/**
 * This class represents a collection of CancelInfo
 * @see com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.cancel.CancelInfo
 * 
 * It is used to send all CancelInfo back to NTF to combine with other standard
 * cancel.  It is simply a convenient container to allow a plug-in to return multiple
 * cancel SMS.
 *
 */
public class CancelFeedBack {
       
    //Each CancelInfo represents one Cancel SMS that needs to be sent.
    protected Collection<CancelInfo> cancelInfoSet = new HashSet<CancelInfo>();
    
    public CancelFeedBack() {
    }
    
    /**
     * Create a new feedback based on one cancelInfo.
     * @param cancelInfo the cancelInfo to add.
     */
    public CancelFeedBack(CancelInfo cancelInfo) {
        if (cancelInfo != null) {
            cancelInfoSet.add(cancelInfo);
        }
    }
       
    /**
     * Add another collection of CancelInfo to this one.
     * @param infoSet - the collection to add.
     */
    public CancelFeedBack(Collection<CancelInfo> infoSet) {
        if (infoSet != null) {
            cancelInfoSet.addAll(infoSet);
        }
    }
    
    /**
     * add another CancelInfo to this CancelFeedBack
     * @param cancelInfo the cancelInfo to add
     */
    public void addCancelInfo(CancelInfo cancelInfo) {
        cancelInfoSet.add(cancelInfo);
        
    }
    
    /**
     * add a collection of cancelInfo to this CancelFeedBack
     * @param cancelInfoCollection the collection to add.
     */
    public void addCancelInfo(Collection<CancelInfo> cancelInfoCollection) {
        cancelInfoSet.addAll(cancelInfoCollection);
    }
    
    /**
     * Add another instance of CancelFeedBack to this one.
     * Combines the two.
     * @param feedback another CancelFeedBackInstance.
     */
    public void add(CancelFeedBack feedback) {
       if (feedback != this) { //don't add self to self
           cancelInfoSet.addAll(feedback.cancelInfoSet); 
       }
    }
    
    
   /**
    * Get a Collection of CancelInfo that the feedback contains.
    * @return a Collection containing all CancelInfo.
     */
    public  Collection<CancelInfo> getAll() {
        return cancelInfoSet;
    }
    
    /**
     * checks if any CancelInfo in this Container.
     * @return true if no elements.
     */
    public boolean isEmpty() {
        return cancelInfoSet.isEmpty();
    }
    
    /**
     * Gets an iterator to the collection of CancelInfo.
     * @return iterator 
     */
    public Iterator<CancelInfo> getIter() {
        return cancelInfoSet.iterator(); 
    }   
}
