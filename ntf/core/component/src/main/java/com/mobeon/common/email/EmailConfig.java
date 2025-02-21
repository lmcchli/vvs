/**
 * Copyright (c) 2007 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.email;

/**
 * Needed by EmailClient for various static config params
 *
 */
public interface EmailConfig {

  /**
   * Needs to return a queue size to be used
   *
   * @return an <code>int</code> value
   */
  int getEmailQueueSize();

  /**
   * Needs to return a poll intervall in seconds
   *
   * @return an <code>int</code> value
   */
  int getEmailPollInterval();

  /**
   * Needs to return the maximum number of 
   * allowed simultaneous connections
   *
   * @return an <code>int</code> value
   */
  int getEmailMaxConnections();

  /**
   * Needs to return a timeout value in milliseconds (unused so far)
   *
   * @return an <code>int</code> value
   */
  int getEmailTimeout();

}
