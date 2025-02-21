/**
 * Copyright (c) 2007 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.email.request;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Session;

/**
 * Check alive request
 */
public class PollRequest extends Request {

  /**
   * Creates a new <code>PollRequest</code> instance.
   */
  public PollRequest() {
    super(null, 0, null, 0); 
  }

  /**
   * Returns null always
   *
   * @param session unused
   * @return Null
   */
  public MimeMessage getEmailMessage(Session session) {
    return null;
  }

}
