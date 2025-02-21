/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.mail.imap;


/**
 * @author Ahmad Mahmoudi
 * @version 1.0
 */
public class ImapServer
{
  /**
   * the host name of the IMAP server
   */
  private String hostName = null;

  /**
   * the port number that the IMAP server listen to
   */
  private int portNumber = -1;

  /**
   * the user name to authenticate with the server
   */
  private String userName = null;

  /**
   * the user's password to authenticate with the server
   */
  private String passWd = null;

  /**
   * the protocol used to communicate with the server
   */
  private String protocol = "imap";


  public ImapServer()
  {
  }

  /**
   * Access method for the hostName property.
   *
   * @return   the current value of the hostName property
   * @roseuid
   */
  public String getHostName()
  {
    return hostName;
  }

  /**
   * Sets the value of the hostName property.
   *
   * @param aHostName the new value of the hostName property
   * @roseuid
   */
  public void setHostName(String aHostName)
  {
    hostName = aHostName;
  }

  /**
   * Access method for the portNumber property.
   *
   * @return   the current value of the portNumber property
   * @roseuid
   */
  public int getPortNumber()
  {
    return portNumber;
  }

  /**
   * Sets the value of the portNumber property.
   *
   * @param aPortNumber the new value of the portNumber property
   * @roseuid
   */
  public void setPortNumber(int aPortNumber)
  {
    portNumber = aPortNumber;
  }

  /**
   * Access method for the userName property.
   *
   * @return   the current value of the userName property
   * @roseuid
   */
  public String getUserName()
  {
    return userName;
  }

  /**
   * Sets the value of the userName property.
   *
   * @param aUserName the new value of the userName property
   * @roseuid
   */
  public void setUserName(String aUserName)
  {
    userName = aUserName;
  }

  /**
   * Access method for the passWd property.
   *
   * @return   the current value of the passWd property
   * @roseuid
   */
  public String getPassWd()
  {
    return passWd;
  }

  /**
   * Sets the value of the passWd property.
   *
   * @param aPassWd the new value of the passWd property
   * @roseuid
   */
  public void setPassWd(String aPassWd)
  {
    passWd = aPassWd;
  }

  /**
   * Access method for the protocol property.
   *
   * @return   the current value of the protocol property
   * @roseuid
   */
  public String getProtocol() {
    return protocol;}

  /**
   * Sets the value of the protocol property.
   *
   * @param aProtocol the new value of the protocol property
   * @roseuid
   */
  public void setProtocol(String aProtocol) {
    protocol = aProtocol;}
}
