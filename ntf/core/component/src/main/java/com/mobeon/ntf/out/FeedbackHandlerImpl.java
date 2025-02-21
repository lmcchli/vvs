package com.mobeon.ntf.out;

import com.mobeon.ntf.Constants;
import com.mobeon.ntf.userinfo.UserInfo;

/**
 * Created by IntelliJ IDEA.
 * User: mnify
 * Date: 2007-maj-10
 * Time: 14:38:57
 */
public class FeedbackHandlerImpl implements FeedbackHandler, Constants {
    
    int status = FEEDBACK_STATUS_OK;

    public FeedbackHandlerImpl() {
        status = FEEDBACK_STATUS_OK;
    }

    public int getStatus() {
        return status;
    }

    public void ok(UserInfo user, int notifType) {
        status = FEEDBACK_STATUS_OK;
    }

    public void ok(UserInfo user, int notifType, boolean sendToMer) {
        status = FEEDBACK_STATUS_OK;
    }

    public void failed(UserInfo user, int notifType, String msg) {
        status = FEEDBACK_STATUS_FAILED;
    }

    public void expired(UserInfo user, int notifType) {
        status = FEEDBACK_STATUS_EXPIRED;
    }

    public void retry(UserInfo user, int notifType, String msg) {
        status = FEEDBACK_STATUS_RETRY;            
    }
}
