/**
 * Copyright (c) 2007 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.email;

/**
 * ConnectionStateListener specifies an interface for clients that want to know
 * the state of an SMTP connection.
 * <P>
 * There are two states; up and down. Up means that the connection is fully
 * functional and down means that either login or connecting has failed.
 */
public interface ConnectionStateListener {
  /**
   * Tells that the connection is OK.
   * @param name - the name of the connection.
   */
  public void connectionUp(String name);
  
  /**
   * Tells that the connection can not be used.
   * @param name - the name of the connection
   */
  public void connectionDown(String name);
  
}
