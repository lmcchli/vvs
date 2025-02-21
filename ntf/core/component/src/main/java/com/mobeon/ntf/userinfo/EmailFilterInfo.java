/**
 * Copyright (c) 2007 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo;

import java.util.*;

public class EmailFilterInfo {

  private HashMap addresses = null;
  protected boolean isEML = false;
  protected String notifContent = "e";

  public EmailFilterInfo(Properties filterInfo, String[] emailDestinationAddresses) {
    String content;
    content = filterInfo.getProperty("EML");
    if (content != null) {
      isEML = true;
      if (!content.equals("")) {
        notifContent = content;
      }
    }

    addresses = new HashMap();
    if (emailDestinationAddresses != null) {
      for(int i=0; i<emailDestinationAddresses.length; i++) {
        addresses.put( emailDestinationAddresses[i], "EML");
      }
    }
  }


  /**
   * Returns the string array of addresses
   *
   * @return comma separated list of email addresses
   */
  public String [] getDestinationAddresses(){
    Set set = addresses.keySet();
    String [] result = new String[addresses.size()];
    Iterator it = set.iterator();
    int i = 0;
    while( it.hasNext() ) {
        result[i] = (String)it.next();
        i++;
    }
    return result;
  }


  /**
   * Returns comma separated list of email addresses
   *
   * @return comma separated list of email addresses
   */
  public String getAddresses() {
    Set set = addresses.keySet();
    String result = "";
    Iterator it = set.iterator();
    while( it.hasNext() ) {
      if (result != "") {
        result = result + "," + (String)it.next();
      } else {
        result = (String)it.next();
      }
    }
    return result;
  }

  public String getNotifContent() {
    return notifContent;
  }

  public String toString() {
    return "{EmailFilterInfo}";
  }
  public boolean isFwdMsg() {
      if (notifContent != null && (notifContent.contains("m") || notifContent.contains("FwdMessage"))) {
          return true;
      }
      return false;
  }

}
