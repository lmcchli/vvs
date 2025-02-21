/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.userinfo.mail;

import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.mobeon.ntf.Config;
import com.mobeon.common.externalcomponentregister.ExternalComponentRegister;
import com.mobeon.common.externalcomponentregister.IServiceInstance;
import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.externalcomponentregister.NoServiceFoundException;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.userinfo.NotificationFilter;
import com.mobeon.ntf.userinfo.UserInfoAdapter;

/**
 * This interface specifies the API available to NTF for accessing user
 * data. It should be the "perfect" and most convenient view of user data, so it
 * will probably change somewhat with each major version of NTF.
 *
 * The implementations will provide the information requested by this interface,
 * by getting data from different sources and combine, convert or just fill in
 * defaults as well as possible.
 *
 * A new NTF with a new version of this interface that shows the possibilities
 * of a new user directory can still be used with an old version of the user
 * directory by updating the implementation for that user directory to the new
 * UserInfo interface,
 */
public class MailUserInfo extends UserInfoAdapter {

    private String telephoneNumber;
    private String preferredLanguage;
    private String[] emFilter = new String[1];
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

    public MailUserInfo(NotificationEmail ne) {
        super();
        String[] temp = null;
        try {
            temp = ne.getHeaders(attrs);
            telephoneNumber = temp[0];
            preferredLanguage = temp[1];
            emFilter[0] = temp[2];
        } catch (MsgStoreException mse) {
            ;
        }
    }
    /** @return a full identification of the user, e.g. the distinguished name
     * in an LDAP user directory. */
    public String getFullId() {
        return "mail." + telephoneNumber;
    }

    /** @return the users mail address. */
    public String getMail() {
        return "Non-mur-" + telephoneNumber;
    }

    /** @return the users telephone number. */
    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    /** @return the users notification filter. */
    public NotificationFilter getFilter() {
        return new NotificationFilter(emFilter, false, this, null);
    }

    /** @return the users telephone number for notifications. */
    public String getNotifNumber() {
        return telephoneNumber;
    }

    /** @return returns a string denoting the users preferred language. */
    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    /** @return a list of the services available to the user. */
    public String[] getServices() {
        return services;
    }

    public boolean isNotifTypeOnFilter(String type) {
        return getFilter().isNotifTypeOnFilter(type);
    }
    
    /**
     * Find the next SMSC to use. If the allowed SMSCs are in the configuration file,
     * the SMSC is selected from one of those, otherwise it is selected from one of all
     * ShortMessage components in MCR.
     *@return the id of the users SMS center.
     */
    public String getSmscId() {
        return null;
/* TODO: Class not used anymore.  Kept for history purposes.
        String smsc;
        if (Config.getAllowedSmsc() == null
            || Config.getAllowedSmsc().length == 0) {
        	IServiceInstance[] insts;
        	try {
        		insts = ExternalComponentRegister.getInstance().getServiceInstances(IServiceName.SHORT_MESSAGE).toArray(new IServiceInstance[0]);
        	}
        	catch (NoServiceFoundException e) {
        		e.printStackTrace(System.out);
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
*/
    }

}
