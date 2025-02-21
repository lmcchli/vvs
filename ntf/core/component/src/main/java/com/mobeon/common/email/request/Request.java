/**
 * Copyright (c) 2007 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.email.request;

import com.mobeon.common.email.EmailResultHandler;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Session;

/**
 * The Request class stores the request that is sent to a mailserver..
 * Each type of message must extend this class and implement the getEmailMessage method.
 * 
 */
public abstract class Request {
  protected EmailResultHandler resultHandler; 
  protected int validity;
  protected int id;
  protected MimeContainer msg;

  /**
   * Creates a new <code>Request</code> instance.
   *
   * @param msg a <code>MimeContainer</code> value (lightweight mime message)
   * @param validity an <code>int</code> value
   * @param resultHandler an <code>EmailResultHandler</code> for feedback
   * @param id an <code>int</code> value identifier for the request
   */
  public Request(MimeContainer msg, 
                 int validity, 
                 EmailResultHandler resultHandler, 
                 int id) {
    this.msg = msg;
    this.resultHandler = resultHandler;
    this.validity = validity;
    this.id = id;
  }

  /**
   * Gets the result (feedback handler) for this request
   *
   * @return an <code>EmailResultHandler</code>
   */
  public EmailResultHandler getResultHandler() {
    return resultHandler;
  }
   
  /**
   * Gets the validity of the request
   *
   * @return validity
   */
  public int getValidity() {
    return validity;
  }
    
  /**
   * Gets the request's id
   * @return id
   */
  public int getId() {
    return id;
  }
    
  /**
   * This method needs to be implemented by all requests. 
   * It should construct the actual mime message with the
   * session.
   *
   * @param session a <code>Session</code> value
   * @return a <code>MimeMessage</code> value
   * @exception jakarta.mail.MessagingException if an error occurs
   */
  public abstract MimeMessage getEmailMessage(Session session) throws jakarta.mail.MessagingException;
        
}

