/**
 * Copyright (c) 2003, 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.email.request;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Session;
import java.util.LinkedList;

/**
 * The Request class stores the request that is sent to the MTA. 
 * This class handles multiple requests (email-addresses) as one request.
 */
public class MultiRequest extends Request { 
  private LinkedList<Request> requests;    

  public MultiRequest() { 
    super(null, 0, null, 0);  
    requests = new LinkedList<Request>();
  }

  public MimeMessage getEmailMessage(Session session) {
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

