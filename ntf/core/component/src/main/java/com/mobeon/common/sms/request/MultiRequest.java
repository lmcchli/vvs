/**
 * Copyright (c) 2003, 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.sms.request;

import com.mobeon.common.smscom.SMSMessage;
import com.mobeon.common.sms.SMSUnit;
import java.util.LinkedList;

/**
 *The Request class stores the request that is sent to the smsc. It is responsible to 
 *make an SMSMessage depending on the protocol and type of message.
 *Each type of message must extend this class and implement the getSMSMessage method.
 *
 * 
 */
public class MultiRequest extends Request { 
    private LinkedList<Request> requests;

    public MultiRequest() { 
        super(null, null, 0, null, 0);  
        requests = new LinkedList<Request>();
        this.delay = 0;
    }

    public MultiRequest(int delay) {
        super(null, null, 0, null, 0);
        requests = new LinkedList<Request>();
        this.delay = delay;
    }
       
    public SMSMessage getSMSMessage(SMSUnit unit) {
        return null;
    }
        
    public void addRequest( Request request ) {
        requests.addLast(request);
    }
    
    public Request getNextRequest() {
        if( !requests.isEmpty() ) {
            return requests.getFirst();
        } else {
            return null;
        }
    }
    
    public void requestDone() {
        if( !requests.isEmpty() ) {
            requests.removeFirst();
        }
        
    }
    
    public int getCount() {
        return requests.size();
    }
}

