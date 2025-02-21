package com.mobeon.common.cmnaccess;

import java.util.HashSet;
import java.util.Set;

public class MsgAccessingException extends Exception {
    static final long serialVersionUID = 0L;
    
    Set<String> failedRecipientsList = new HashSet<String>();
    
    public MsgAccessingException(String _msg) {
        super(_msg);
    }
    public MsgAccessingException(String _msg, Exception e) {
        super(_msg, e);
    }
    
    public void addFailedRecipient(String nonNormalizedTo) {
        failedRecipientsList.add(nonNormalizedTo);
    }
    
    public String[] getFailedRecipients() {
        return failedRecipientsList.toArray(new String[failedRecipientsList.size()]);
    }

}
