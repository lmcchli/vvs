/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.outdial;

import com.mobeon.ntf.userinfo.UserInfo;

/**
 * Handles result from OdlCaller.
 * Errorcodes comes from Xmp where 200 is ok.
 * Additional errorCodes that comes from OdlCaller is:
 * 910 - Odl is disabled
 * 920 - CFU is enabled on the user.
 * 925 - Could not decide CFU because of failure in ESI/HLR
 * 930 - General error in OdlCaller
 */
public interface OdlCallListener {

    public void handleResult(String subscriberNumber, String notificationNumber, UserInfo user, int code);


}
