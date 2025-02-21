/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf.userinfo.mcd;

public interface McdUserFinder {

    /****************************************************************
    * The following public methods are documented in the UserInfo
    * documentation.
    */
    public boolean findByMail(String mail);
    public boolean findByTelephoneNumber(String nbr);
    public boolean readByIdentity(String dn);

}
