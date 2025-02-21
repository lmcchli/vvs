/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.wap.papapi;


/**
 * @author Ahmad Mahmoudi
 * @version 1.0
 */
public class HttpServer
{

  /**
   * The port number the PPG server listens to.
   * -1 to use the default port (80 for HTTP).
   */
  private int portNumber = 80;

  /**
   * the path to the PPG servlet on the server
   */
  private String urlSuffix = "";

  /**
   * The host name of the PPG server.
   */
  private String hostName = "localhost";

  /**
   * The user name is used if authentication is needed
   */
  private String userName = "user";

  /**
   * The passWd is used if authentication is needed
   */
  private String passWd = "password";

  /**
   * @roseuid 3A8D53BA0014
   */
  public HttpServer()
  {
  }

  /**
   * Access method for the hostName property.
   *
   * @return   the current value of the hostName property
   * @roseuid 3A8D53BA00A0
   */
  public String getHostName()
  {
    return hostName;
  }

  /**
   * Sets the value of the hostName property.
   *
   * @param aHostName the new value of the hostName property
   * @roseuid 3A8D53BA014B
   */
  public void setHostName(String aHostName)
  {
    hostName = aHostName;
    return;
  }

  /**
   * Access method for the portNumber property.
   *
   * @return   the current value of the portNumber property
   * @roseuid 3A8D53BA02E5
   */
  public int getPortNumber()
  {
    return portNumber;
  }

  /**
   * Sets the value of the portNumber property.
   *
   * @param aPortNumber the new value of the portNumber property
   * @roseuid 3A8D53BA032B
   */
  public void setPortNumber(int aPortNumber)
  {
    portNumber = aPortNumber;
    return;
  }

  /**
   * Access method for the urlSuffix property.
   *
   * @return   the current value of the urlSuffix property
   * @roseuid 3A8D53BB019C
   */
  public String getUrlSuffix()
  {
    return urlSuffix;
  }

  /**
   * Sets the value of the urlSuffix property.
   *
   * @param aUrlSuffix the new value of the urlSuffix property
   * @roseuid 3A8D53BB0200
   */
  public void setUrlSuffix(String aUrlSuffix)
  {
    urlSuffix = aUrlSuffix;
    return;
  }

  /**
   * Access method for the userName property.
   *
   * @return   the current value of the userName property
   * @roseuid 3A9676FB00B2
   */
  public String getUserName()
  {
    return userName;
  }

  /**
   * Sets the value of the userName property.
   *
   * @param aUserName the new value of the userName property
   * @roseuid 3A9676FB00BC
   */
  public void setUserName(String aUserName)
  {
    userName = aUserName;
  }

  /**
   * Access method for the passWd property.
   *
   * @return   the current value of the passWd property
   * @roseuid 3A9676FB00DA
   */
  public String getPassWd()
  {
    return passWd;
  }

  /**
   * Sets the value of the passWd property.
   *
   * @param aPassWd the new value of the passWd property
   * @roseuid 3A9676FB00E4
   */
  public void setPassWd(String aPassWd)
  {
    passWd = aPassWd;
  }

  public String toString()
  {

    String s = "host name = " + this.getHostName() + ", " +
               "port number = " + this.getPortNumber() + ", " +
               "urn = " + this.getUrlSuffix() + ", " +
               "user name = " + this.getUserName() + ", " +
               "passwd = " + this.getPassWd() + ", ";
    return s;
  }
}
