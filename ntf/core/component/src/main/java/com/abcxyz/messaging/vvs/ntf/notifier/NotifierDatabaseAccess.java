/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.notifier;

import com.abcxyz.messaging.common.mcd.MCDException;
import com.abcxyz.messaging.common.mcd.exceptions.AuthenticationException;
import com.abcxyz.messaging.common.mcd.exceptions.DBUnavailableException;
import com.abcxyz.messaging.common.mcd.exceptions.ProfileNotFoundException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.ANotifierDatabaseSubscriberProfile;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.INotifierDatabaseAccess;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.database.NotifierDatabaseException;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccess;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;


public class NotifierDatabaseAccess implements INotifierDatabaseAccess {

    private static NotifierDatabaseAccess instance = null;
    private IDirectoryAccess da = null;
    
    private NotifierDatabaseAccess() {
        da = CommonMessagingAccess.getInstance().getMcd();
    }

    public static NotifierDatabaseAccess get() {
        if(instance == null) {
            instance = new NotifierDatabaseAccess();
        }
        return instance;
    }

    public ANotifierDatabaseSubscriberProfile getSubscriberProfile(String subscriberNumber) throws NotifierDatabaseException {
        try {
            IDirectoryAccessSubscriber subscriberProfile = da.lookupSubscriber(subscriberNumber, true);
            if(subscriberProfile != null) {
                return new NotifierDatabaseSubscriberProfile(subscriberNumber, subscriberProfile);
            } else {
                return null;
            }
        } catch (ProfileNotFoundException pnfe) {
            return null;
        } catch (AuthenticationException ae) {
            throw new NotifierDatabaseException(ae);
        } catch (DBUnavailableException dbue) {
            throw new NotifierDatabaseException(dbue);
        } catch (MCDException mcd) {
            throw new NotifierDatabaseException(mcd);
        } catch (Exception e) {
            throw new NotifierDatabaseException(e);
        }
    }

}
