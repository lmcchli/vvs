package com.mobeon.ntf.slamdown;

import com.mobeon.ntf.userinfo.UserInfo;

public class SlamdownListNoSQL extends SlamdownListAbstract {

	public SlamdownListNoSQL(String subscriberNumber, String notificationNumber, UserInfo userInfo, int validity, String cosName, boolean internal, int notificationType) {
		super(subscriberNumber, notificationNumber, userInfo, validity, cosName, internal, notificationType);
	}
}
