package com.mobeon.common.cmnaccess;

import java.net.URI;

import com.abcxyz.messaging.common.mcd.MCDException;
import com.abcxyz.messaging.common.mcd.ProfileContainer;
import com.abcxyz.messaging.common.mcd.exceptions.AuthenticationException;
import com.abcxyz.messaging.common.mcd.exceptions.DBUnavailableException;
import com.abcxyz.messaging.common.oam.impl.StdoutLogger;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryAccessException;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryAccessSubscriber;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccess;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessProfile;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.abcxyz.services.moip.common.directoryaccess.MoipProfile;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;


public class McdStub implements IDirectoryAccess {

    public static final int SUBSCRIBER_PROFILE = 0;
    public static final int COS_PROFILE = 1;

    static private ProfileContainer subscriberProfileContainer = new ProfileContainer();
    static private ProfileContainer cosProfileContainer = new ProfileContainer();

    @Override
    public MoipProfile lookupCos(String cosIdentity)
    {
        return this.getCos();
    }

    @Override
    public DirectoryAccessSubscriber lookupSubscriber(String subscriberIdentity, boolean throwException)
            throws AuthenticationException, DBUnavailableException, MCDException, Exception {
        return lookupSubscriber(subscriberIdentity, false);
    }

    @Override
    public DirectoryAccessSubscriber lookupSubscriber(String subscriberIdentity)
    {
        if(subscriberProfileContainer == null){
            System.out.println("DirectoryAccess.lookupSubscriber: subscriber does not exist");
            return null;
        }

        if (subscriberProfileContainer.attributesSize() == 0) {
            // Add default values
            subscriberProfileContainer.addAttributeValue(DAConstants.ATTR_COS_IDENTITY, "cos:1");
            subscriberProfileContainer.addAttributeValue(DAConstants.ATTR_NOTIF_NUMBER, "123456");
            subscriberProfileContainer.addAttributeValue(DAConstants.ATTR_DELIVERY_PROFILE,
                    "NotifType=SMS,ODL,MWI,EML;MobileNumber=" +
                    "514123" +
                    ";IPNumber=15143457900,888888888;Email=test@abc.com,foo@bar.com");

            subscriberProfileContainer.addIdentity(URI.create("msid:111112462ffff"));
        }

        DirectoryAccessSubscriber sub = new DirAccessSubscriberStub(new MoipProfile(subscriberProfileContainer, new StdoutLogger("McdStub")), this.getCos(), null, new StdoutLogger("McdStub"));
        return sub;
    }

    /**
     * Add subscriber profile attributes.
     * If invoked, the default values provided within this class wont be part of the profile.
     *
     * @param attributeName
     * @param attributeValue
     */
    public void addSubscriberProfileAttribute(String attributeName, String attributeValue) {
    	subscriberProfileContainer.removeAttribute(attributeName);
        subscriberProfileContainer.addAttributeValue(attributeName, attributeValue);
    }

    public void addSubcriberProfileIdentity(URI identity) {
    	subscriberProfileContainer.addIdentity(identity);
    }

    public void addSubscriberProfileIdentity(String identity) {
    	subscriberProfileContainer.addIdentity(identity);
    }

    /**
     * Add cos profile attributes.
     * If invoked, the default values provided within this class wont be part of the profile.
     *
     * @param attributeName
     * @param attributeValue
     */
    public void addCosProfileAttribute(String attributeName, String attributeValue) {
        cosProfileContainer.addAttributeValue(attributeName, attributeValue);
    }

    @Override
    public void updateSubscriber(IDirectoryAccessSubscriber subscriber, String attrName, String value) throws DirectoryAccessException
    {
        // TODO Auto-generated method stub
    }

    private MoipProfile getCos() {

        if (cosProfileContainer.attributesSize() == 0) {
            // Add default values
            cosProfileContainer.addAttributeValue(DAConstants.ATTR_FILTER, "1;y;a;evf;SMS,MWI;s,c;1;;;;;default;;");
            cosProfileContainer.addAttributeValue(DAConstants.ATTR_SERVICES, "mwi_notification");
            cosProfileContainer.addAttributeValue(DAConstants.ATTR_SERVICES, "msgtype_voice");
        }

        MoipProfile cosProfile = new MoipProfile(cosProfileContainer, new StdoutLogger("McdStub"));
        return cosProfile;
    }

	@Override
	public IDirectoryAccessProfile lookupBroadcastAnnouncement(String baName) {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public boolean isProfileUpdatePossible() {
        return true;
    }


}
