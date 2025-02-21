/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.wap.papapi;


/**
 * @author Ahmad Mahmoudi
 * @version 1.0
 */
public class PPGServer extends HttpServer
{

  /**
   * The protocol used to tunnel the push message through. Currently only
   * synchronous push submission through HTTP is supported.
   */
  private String protocol = "http";

  /**
   * @roseuid 3A89AA0C0119
   */
  public PPGServer()
  {
  }

  /**
   * Access method for the protocol property.
   *
   * @return   the current value of the protocol property
   * @roseuid 3A89AA0C0173
   */
  public String getProtocol()
  {
    return protocol;
  }

  /**
   * Sets the value of the protocol property.
   *
   * @param aProtocol the new value of the protocol property
   * @roseuid 3A89AA0C0174
   */
  public void setProtocol(String protocol)
  {
    this.protocol = protocol;
    return;
  }

  /**
   * returns the contents of the ppg server object as a string.
   */
  public String toString()
  {
    return "protocol = " + this.getProtocol() + ", " + super.toString();
  }
}
