/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.outdial.test;

import com.mobeon.ntf.Config;
import com.mobeon.common.externalcomponentregister.ExternalComponentRegister;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.externalcomponentregister.NoServiceFoundException;
import com.mobeon.ntf.userinfo.UserInfoAdapter;
import com.mobeon.ntf.out.outdial.*;

/**
 * Implementation of userinfo that is based on info in and OdlInfo
 */
public class TestUserInfo extends UserInfoAdapter {

    private String preferredLanguage;
    private String[] emFilter = new String[1];
    private String userMail;
    private String userDN;
    private String outdialSchema ;
    private static String[] attrs;
    private static String[] services;
    private static int nextSmsc = 0;

    static {
        attrs =  new String[3];
        attrs[0] = "X-Ipms-User-Attribute-" + "telephonenumber";
        attrs[1] = "X-Ipms-User-Attribute-" + "preferredLanguage";
        attrs[2] = "X-Ipms-User-Attribute-" + "emFilter";

        services = new String[2];
        services[0] = "emservicename=msgtype_email,ou=services,o=non-mur";
        services[1] = "emservicename=sms_notification,ou=services,o=non-mur";
    }

    public TestUserInfo(OdlInfo oInfo) {
        super();
        telephoneNumber = oInfo.getDialNumber();
        preferredLanguage = "en";
        userMail = oInfo.getUserEmail();
        emFilter[0] = "";
        userDN = oInfo.getUserDN();
        if (telephoneNumber.startsWith("1")) {
            outdialSchema = "other";
        } else if (telephoneNumber.startsWith("2")) {
            outdialSchema ="yetanother";
        } else {
            outdialSchema = "default";
        }
    }

    public String toString()
    {
        return "TestUserInfo{ " + userDN + "," + userMail + "," + telephoneNumber + "}";
    }

    /** @return a full identification of the user, e.g. the distinguished name
     * in an LDAP user directory. */
    public String getFullId() {
        return userDN;
    }

    /** @return the users mail address. */
    public String getMail() {
        return userMail;
    }

    /**
     * Find the next SMSC to use. If the allowed SMSCs are in the configuration file,
     * the SMSC is selected from one of those, otherwise it is selected from one of all
     * ShortMessage components in MCR.
     *@return the id of the users SMS center.
     */
    public String getSmscId() {
        String smsc;
        if (Config.getAllowedSmsc() == null
            || Config.getAllowedSmsc().length == 0) {
        	IServiceInstance[] insts;
        	try {
        		insts = ExternalComponentRegister.getInstance().getServiceInstances(IServiceName.SHORT_MESSAGE).toArray(new IServiceInstance[0]);
        	}
        	catch (NoServiceFoundException e) {
        		return null;
        	}
            synchronized(attrs) { //attrs has nothing to do with this function,
                                  //but we can not synchronize on nextSmsc
                                  //(it is not an object)
                if (nextSmsc >= insts.length) {
                    nextSmsc = 0;
                }
                smsc = insts[nextSmsc].getProperty(IServiceInstance.COMPONENT_NAME);
                nextSmsc++;
            }
        } else {
            synchronized(attrs) {
                if (nextSmsc >= Config.getAllowedSmsc().length) {
                    nextSmsc = 0;
                }
                smsc = Config.getAllowedSmsc()[nextSmsc];
                nextSmsc++;
            }
        }
        return smsc;
    }


    /** @return returns a string denoting the users preferred language. */
    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    /** @return Name of outdial schema. */
    public String getOutdialSchema()
    {
        return outdialSchema;
    }

    /** @return a list of the services available to the user. */
    public String[] getServices() {
        return services;
    }
}
