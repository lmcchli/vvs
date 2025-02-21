package com.mobeon.ntf.slamdown;

import com.mobeon.ntf.userinfo.UserInfo;

public class SlamdownListFS extends SlamdownListAbstract {

	public SlamdownListFS(String subscriberNumber, String notificationNumber, UserInfo userInfo, int validity, String cosName, boolean internal, int notificationType) {
		super(subscriberNumber, notificationNumber, userInfo, validity, cosName, internal, notificationType);
	}
}