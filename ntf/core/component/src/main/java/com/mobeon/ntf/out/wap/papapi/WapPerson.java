/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 **/

package com.mobeon.ntf.out.wap.papapi;

import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.util.Logger;
import java.io.IOException;
import com.mobeon.ntf.out.wap.papapplication.WapPushControl;



/**
 * This class contains the information needed for a person to be notified by WAP
 * push.
 * @author Ahmad Mahmoudi
 * @version 1.0
 */
public class WapPerson extends PPGServer implements IWapPush
{
    private static final Logger logger = Logger.getLogger(WapPerson.class);
    private int depositType = -1;
    private UserInfo user = null;
    private NotificationEmail email = null;
    private UserMailbox inbox = null;

  /**
   * Is the message to be submitted to the WAP terminal.
   */
  private String pushData = "";

  /**
   * PushID is a unique string identfying a push request. This value is used to get
   * the push result notify.
   * pushID can only be set internally by the WapPerson object
   */
  private String pushID;
  private WapPushControl wapPushCtrl;


  /**
   * @roseuid 3A89AA0C02A0
   */
    public WapPerson(UserInfo user,
                     NotificationEmail email,
                     UserMailbox inbox)
  {
      this.user = user;
      this.email = email;
      this.inbox = inbox;
      pushID = Long.toHexString(System.currentTimeMillis());
      wapPushCtrl = WapPushControl.createWapPushControl();
  }

    public UserInfo getUser() { return user; }
    public NotificationEmail getEmail() { return email; }
    public UserMailbox getInbox() { return inbox; }

    public String getNotifNumber()//-ermahen
	{
	    return user.getNotifNumber();
	}


  /**
   * Access method for the wapDeviceID property.
   *
   * @return   the current value of the wapDeviceID property
   * @roseuid 3A89AA0C02BE
   */
  public String getWapDeviceID()
  {
    return user.getTelephoneNumber();
  }

  /**
   * Access method for the pushData property.
   *
   * @return   the current value of the pushData property
   * @roseuid 3A89AA0C02D2
   */
  public String getPushData()
  {
    return pushData;
  }

  /**
   * Sets the value of the pushData property.
   *
   * @param aPushData the new value of the pushData property
   * @roseuid 3A89AA0C02DC
   */
  public void setPushData(String aPushData)
  {
    pushData = aPushData;
  }

  /**
   * Access method for the pushID property.
   *
   * @return   the current value of the pushID property
   * @roseuid 3A89AA0C02E6
   */
  public String getPushID()
  {
    return pushID;
  }

  /**
   * @roseuid 3AC9E376031D
   */
  public boolean pushNotifyRequest()
  {
    int result = parseReqParms();
    if (result == PAP_OK)
    {
	if(wapPushCtrl.pushRequest(this))
	    return true;
	else
	    return false;
    }
    else
	return false;
  }

  /**
   * This method parses the person and the ppg server data before requesting the
   * push submission from wapPushControl.
   * @roseuid 3AC9E37700EE
   */
  private int parseReqParms()
  {
    if ((this.getPushID() == null) ||
        (this.getPushData() == null) ||
        (this.getPushData() == null) ||
        (this.getWapDeviceID() == null) ||
        (this.getHostName() == null) ||
        (this.getProtocol() == null))
    {
      logger.logMessage("Error in wap push request parms: " + this, logger.L_ERROR);
      return PAP_BAD_REQUEST;
    }
    return PAP_OK;
  }

  /**
   * Access method for the depositType property.
   *
   * @return   the current value of the depositType property
   * @roseuid
   */
  public int getMessageType() {
    return depositType;}

  /**
   * Sets the value of the depositType property.
   *
   * @param aMessageType the new value of the depositType property
   * @roseuid
   */
  public void setMessageType(int aMessageType) {
    depositType = aMessageType;}
}
