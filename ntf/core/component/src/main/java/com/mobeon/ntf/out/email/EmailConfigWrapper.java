/**
 * Copyright (c) 2007 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.email;


import com.mobeon.common.email.EmailConfig;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.event.EventRouter;

import java.io.FileInputStream;
import java.util.*;


public class EmailConfigWrapper implements EmailConfig {

  public int getEmailQueueSize() {
    return 128; // hard coded
  }

  public int getEmailPollInterval() {
    return 30; // hard coded
  }

  public int getEmailMaxConnections() {
    return Config.getSmsMaxConn(); // Use sms setting (10 usually)
  }

  public int getEmailTimeout() {
    return Config.getImapTimeout() * 3; // use impatimeout*3 (15 sec usually)
  }

}
