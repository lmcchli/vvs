/*
 * PhoneOnRequester.java
 *
 * Created on den 19 september 2004, 19:02
 */

package com.mobeon.ntf.out.outdial;

import com.mobeon.ntf.event.PhoneOnEventListener;
import com.mobeon.ntf.userinfo.UserInfo;

/**
 * Interface to request info for when a phone is on.
 */
public interface PhoneOnRequester
{

    /**
     * Set the listener that handles answers for this requester.
     * The listener should be called when a request could
     * not be made.
     * @param listener the new listener to use
     */
    public void setListener(PhoneOnEventListener listener);


    /**
     * Request informaion for when a phone is on.
     * The result will be delivered to a listener. If the request
     * cannot be sent the associated listener should be notifiec
     * immediately.
     * @param userInfo UserInfo for user that we are checking for
     * @param requestNumber Number we are interested in.
     * @param userEmail mail address for user we are checking for
     */
    public void request(UserInfo userInfo, String requestNumber,
                        String userEmail);

    /**
     *Clears one number.
     *@param requestNumber - the number to clear
     */
    public void clear(String requestNumber);

}
