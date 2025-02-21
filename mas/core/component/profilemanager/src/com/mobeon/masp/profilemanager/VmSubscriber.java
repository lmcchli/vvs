package com.mobeon.masp.profilemanager;

import java.net.URI;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import com.abcxyz.messaging.common.mcd.Modification;
import com.abcxyz.messaging.mfs.message.MfsFileFolder;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryAccess;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryAccessException;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryAccessSubscriber;
import com.abcxyz.services.moip.distributionlist.DistributionListException;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.mobeon.common.cmnaccess.oam.CommonOamManager;
import com.mobeon.common.logging.HostedServiceLogger;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.common.trafficeventsender.mfs.MfsEventManager;
import com.mobeon.masp.mailbox.IMailbox;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.MailboxProfile;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.profilemanager.distributionlists.MultimediaDistributionListFactory;
import com.mobeon.masp.profilemanager.distributionlists.MultimediaDistributionListFactoryImpl;
import com.mobeon.masp.profilemanager.distributionlists.MultimediaDistributionListManager;
import com.mobeon.masp.profilemanager.greetings.GreetingFormat;
import com.mobeon.masp.profilemanager.greetings.GreetingManager;
import com.mobeon.masp.profilemanager.greetings.GreetingManagerFactory;
import com.mobeon.masp.profilemanager.greetings.GreetingManagerFactoryImpl;
import com.mobeon.masp.profilemanager.greetings.GreetingSpecification;
import com.mobeon.masp.profilemanager.greetings.GreetingType;
import com.mobeon.masp.profilemanager.greetings.SpokenNameSpecification;

public class VmSubscriber implements IProfile
{

	private static final ILogger logg = ILoggerFactory.getILogger(VmSubscriber.class);
    private static final HostedServiceLogger log = new HostedServiceLogger(logg);

	private Map<String, IMailbox> mailBoxMap;

	private String phoneNumber;
    private IDirectoryAccessSubscriber subscriber;
	private GreetingManagerFactory greetingManagerFactory;
    protected BaseContext context;

    private MultimediaDistributionListFactory distributionListFactory;


	/**
	 * Constructs a subscriber from a phone number, a context and a IDirectoryAccess object.
	 * This constructor has the package class because it not for use directly. It is used by the
	 * public constructor or the unit tests that changes the DirectoryAccess object.
	 *
	 * @param phoneNumber Phone number.
	 * @param context Environment context.
	 * @param subscriber the DirectoryAccessSubscriber
	 */
	protected VmSubscriber(String phoneNumber, BaseContext context, IDirectoryAccessSubscriber subscriber) {
		this.context = context;

		mailBoxMap = new HashMap<String, IMailbox>();
        this.phoneNumber = phoneNumber;
		this.subscriber = subscriber;
		greetingManagerFactory = new GreetingManagerFactoryImpl();
		String subscriberMsid = subscriber.getSubscriberIdentity("msid");
		distributionListFactory = new MultimediaDistributionListFactoryImpl(subscriberMsid);
    }

	public boolean getBooleanAttribute(String applicationName) throws UnknownAttributeException
	{
		boolean[] b = getBooleanAttributes(applicationName);
		if(b != null && b.length > 0) {
			return b[0];
		}
		 // This should not happen.
        String errMsg = "Attribute <" + applicationName + "> has size 0";
        throw new UnknownAttributeException(errMsg);
	}

	public IDirectoryAccessSubscriber getSubscriber(){
	    return subscriber;
	}

	public boolean[] getBooleanAttributes(String applicationName) throws UnknownAttributeException
	{
		 if (subscriber != null)
		 {
		        boolean[] attributes = subscriber.getBooleanAttributes(applicationName);
		        if(attributes != null) {
	                if (log.isDebugEnabled()) {
	                    log.debug("In getBooleanAttributes, phoneNumber=" + phoneNumber + ", value for " + applicationName +
	                            " " + Arrays.toString(attributes));
	                }
		            return attributes;
		        } else {
		        	// This should not happen.
		            String errMsg = "Attribute <" + applicationName + "> has size 0";
		            throw new UnknownAttributeException(errMsg);
		        }
	        }

		 // This should not happen.
         String errMsg = "Attribute <" + applicationName + "> has size 0";
         throw new UnknownAttributeException(errMsg);

	}


	public int getIntegerAttribute(String applicationName) throws UnknownAttributeException
	{
		int[] i = getIntegerAttributes(applicationName);
		if(i != null && i.length > 0) {
			return i[0];
		}
		 // This should not happen.
        String errMsg = "Attribute <" + applicationName + "> has size 0";
        throw new UnknownAttributeException(errMsg);
	}


	public int[] getIntegerAttributes(String applicationName) throws UnknownAttributeException
	{
		 if (subscriber != null) {
	        	int[] attributes = subscriber.getIntegerAttributes(applicationName);
	        	if(attributes != null) {
		            if (log.isDebugEnabled()) {
		                log.debug("In subscriberGetIntegerAttribute, phoneNumber=" + phoneNumber + ", value for " + applicationName +
		                        " " + Arrays.toString(attributes));
		            }
		            return attributes;
		        } else {
		        	// This should not happen.
		            String errMsg = "Attribute <" + applicationName + "> has size 0";
		            throw new UnknownAttributeException(errMsg);
		        }
	        }
		// This should not happen.
         String errMsg = "Attribute <" + applicationName + "> has size 0";
         throw new UnknownAttributeException(errMsg);

	}


	public String getStringAttribute(String applicationName) throws UnknownAttributeException
	{
		String[] s = getStringAttributes(applicationName);
		if(s != null && s.length > 0) {
			return s[0];
		}
		 // This should not happen.
        String errMsg = "Attribute <" + applicationName + "> has size 0";
        throw new UnknownAttributeException(errMsg);
	}

	public String[] getStringAttributes(String applicationName) throws UnknownAttributeException
	{
		if (subscriber != null) {

	    	String[] attributes = subscriber.getStringAttributes(applicationName);
	    	if(attributes != null){
	        	if (log.isDebugEnabled()) {
	        		log.debug("In subscriberGetStringAttribute, phoneNumber=" + phoneNumber + ", value for " + applicationName +
	                            " " + Arrays.toString(attributes));
	                }
	            return attributes;
	    	}
	    	else {
	            // This should not happen.
	            String errMsg = "Attribute <" + applicationName + "> has size 0";
	            throw new UnknownAttributeException(errMsg);
	        }

		 }
        // This should not happen.
        String errMsg = "Attribute <" + applicationName + "> has size 0";
        throw new UnknownAttributeException(errMsg);

	}



	public void createDistributionList(String ID)
			throws ProfileManagerException {
		int idAsInt = -1;
		try {
			if (log.isDebugEnabled()) {
        		log.debug("In createDistributionList, ID=" + ID);
            }
			idAsInt = Integer.valueOf(ID).intValue();
			String msid = this.getIdentity("msid");
			MultimediaDistributionListManager mgr = distributionListFactory.getDistributionListManager(msid);
			mgr.createDistributionList(idAsInt);
			if (log.isDebugEnabled()) {
        		log.debug("In createDistributionList, createDistributionList has been performed successfully!");
            }
		}
		catch (DistributionListException e){
        	log.error("In createDistributionList, DistributionListException " + e,e);
			throw new ProfileManagerException("Exception caught (" + e.getMessage() + ") while creating distribution list.");
		}
		catch (Exception e){
        	log.error("In createDistributionList, Exception " + e,e);
			throw new ProfileManagerException("ID " + ID + " is invalid for a distribution list. Use an integer.");
		}
	}

    public IMediaObject getSpokenNameForDistributionList(String ID) throws ProfileManagerException{
    	int idAsInt = -1;
		try {
			if (log.isDebugEnabled()) {
        		log.debug("In getSpokenNameForDistributionList, ID=" + ID);
            }
			idAsInt = Integer.valueOf(ID).intValue();
			String msid = this.getIdentity("msid");
			MultimediaDistributionListManager mgr = distributionListFactory.getDistributionListManager(msid);
			return mgr.getSpokenName(idAsInt);
		}
		catch (DistributionListException e){
        	log.error("In getSpokenNameForDistributionList, DistributionListException " + e,e);
			throw new ProfileManagerException("Exception caught (" + e.getMessage() + ") while creating distribution list.");
		}
		catch (Exception e){
        	log.error("In getSpokenNameForDistributionList, Exception " + e,e);
			throw new ProfileManagerException("ID " + ID + " is invalid for a distribution list. Use an integer.");
		}
    }

    public void setSpokenNameForDistributionList(String ID, IMediaObject aSpokenName) throws ProfileManagerException {
    	int idAsInt = -1;
		try {
			if (log.isDebugEnabled()) {
        		log.debug("In setSpokenNameForDistributionList, ID=" + ID + ", aSpokenName=" + aSpokenName);
            }
			idAsInt = Integer.valueOf(ID).intValue();
			String msid = this.getIdentity("msid");
			MultimediaDistributionListManager mgr = distributionListFactory.getDistributionListManager(msid);
			mgr.setSpokenName(idAsInt, aSpokenName);
			if (log.isDebugEnabled()) {
        		log.debug("In setSpokenNameForDistributionList, setSpokenNameForDistributionList has been performed successfully!");
            }
		}
		catch (DistributionListException e){
        	log.error("In setSpokenNameForDistributionList, DistributionListException " + e,e);
			e.printStackTrace();
			throw new ProfileManagerException("Exception caught (" + e.getMessage() + ") while creating distribution list.");
		}
		catch (Exception e){
        	log.debug("In setSpokenNameForDistributionList, Exception " + e,e);
			e.printStackTrace();
			throw new ProfileManagerException("ID " + ID + " is invalid for a distribution list. Use an integer.");
		}
    }

	public void deleteDistributionList(String ID)
			throws ProfileManagerException {
		int idAsInt = -1;
		try {
			if (log.isDebugEnabled()) {
        		log.debug("VmSubscriber.deleteDistributionList>>remove dlist " + ID);
            }
			idAsInt = Integer.valueOf(ID).intValue();
			String msid = this.getIdentity("msid");
			MultimediaDistributionListManager mgr = distributionListFactory.getDistributionListManager(msid);
			mgr.removeDistributionList(idAsInt);
			if (log.isDebugEnabled()) {
        		log.debug("VmSubscriber.deleteDistributionList>>Back from remove list call");
            }
		}
		catch (DistributionListException e){
	          log.error("In deleteDistributionList, DistributionListException " + e,e);

			throw new ProfileManagerException("Exception caught (" + e.getMessage() + ") while deleting distribution list.");
		}
		catch (Exception e){
            log.error("In deleteDistributionList, Exception " + e,e);
			throw new ProfileManagerException("ID " + ID + " is invalid for a distribution list. Use an integer.");
		}
	}

	public void addMemberToDistributionList(String distListNumber, String distListMember) throws ProfileManagerException {

		try {
			if (log.isDebugEnabled()) {
        		log.debug("VmSubscriber.addMemberToDistributionList>>distListNumber " + distListNumber + ", distListMember " + distListMember);
            }
			IMultimediaDistributionList list = getDistributionList(distListNumber);
			list.manipulateListMember(Modification.Operation.ADD, distListMember);
			if (log.isDebugEnabled()) {
        		log.debug("VmSubscriber.addMemberToDistributionList successfull !");
            }
		} catch (Exception ex) {
			log.error("VmSubscriber.addMemberToDistributionList>>got exception: "+ex,ex);
			if (log.isDebugEnabled()) {
			}
			throw new ProfileManagerException("Error adding member " + distListMember + " to list " + distListNumber);
		}
	}

	public void deleteMemberFromDistributionList(String distListNumber, String distListMember) throws ProfileManagerException {

		try {
			if (log.isDebugEnabled()) {
        		log.debug("VmSubscriber.deleteMemberFromDistributionList>>distListNumber " + distListNumber + ", distListMember " + distListMember);
            }
			IMultimediaDistributionList list = getDistributionList(distListNumber);
			list.manipulateListMember(Modification.Operation.REMOVE, distListMember);
			if (log.isDebugEnabled()) {
        		log.debug("VmSubscriber.deleteMemberFromDistributionList successfull !");
            }
		} catch (Exception ex) {
			log.error("VmSubscriber.deleteMemberFromDistributionList>>got exception: "+ex,ex);
			throw new ProfileManagerException("Error deleting member " + distListMember + " from list " + distListNumber);
		}
	}

    public String getDistributionListsMsid(String aListNumber)throws ProfileManagerException{
        String msid = this.getIdentity("msid");
        try
        {
            return  distributionListFactory.getDistributionListManager(msid).getDistributionListsMsid(Integer.valueOf(aListNumber));
        }
        catch(Exception e)
        {
            log.error("Could not retrieve distribution msid for list number:  "+aListNumber,e);
            throw new ProfileManagerException("Could not retrieve distribution msid for list number "+ aListNumber+ " msid: "+ msid);
        }


    }
	public IMultimediaDistributionList[] getDistributionLists()
	throws ProfileManagerException {
		if (log.isDebugEnabled()) {
    		log.debug("VmSubscriber.getDistributionLists>>Entered");
        }
		String msid = this.getIdentity("msid");
		MultimediaDistributionListManager mgr = distributionListFactory.getDistributionListManager(msid);
		try {
			if (log.isDebugEnabled()) {
	    		log.debug("VmSubscriber.getDistributionLists>>Get distribution lists for subscriber " + msid);
	        }
			IMultimediaDistributionList[] allLists = mgr.getDistributionLists();
			if (log.isDebugEnabled()) {
	    		log.debug("VmSubscriber.getDistributionLists>>Got " + allLists.length + " for sub " + msid);
	        }
			return allLists;
		}
		catch (DistributionListException e) {

			String m = "Could not retrieve distribution lists for subscriber " + msid;
			log.error(m,e);
			throw new ProfileManagerException("Could not retrieve distribution lists for subscriber " + msid);
		}
		catch (Exception e){

			String m = "Got exception retrieving lists for subscriber " + msid + " Exception: " + e.getMessage();
			log.error(m,e);
			throw new ProfileManagerException("Could not retrieve distribution lists for subscriber " + msid);
		}
	}

	public IMultimediaDistributionList getDistributionList(String aListNumber)
	throws ProfileManagerException {
		if (log.isDebugEnabled()) {
    		log.debug("VmSubscriber.getDistributionList>>Entering for list " + aListNumber);
        }
		String msid = this.getIdentity("msid");
		MultimediaDistributionListManager mgr = distributionListFactory.getDistributionListManager(msid);
		int listId = -1;
		try {
			listId = Integer.valueOf(aListNumber);
			if (log.isDebugEnabled()) {
	    		log.debug("VmSubscriber.getDistributionList>>Get list " + aListNumber + " for sub " + msid);
	        }
			IMultimediaDistributionList theList = mgr.getDistributionList(listId);
			if (log.isDebugEnabled()) {
	    		log.debug("VmSubscriber.getDistributionList>>Returning a list of id " + theList.getId());
	        }
			return theList;
		}
		catch (Exception e){
			String m = "Could not retrieve distribution list " + aListNumber + " for subscriber " + msid;
			log.error(m,e);
	        throw new ProfileManagerException(m);
		}
	}

	public IMediaObject getGreeting(GreetingSpecification specification)
			throws ProfileManagerException {

		if (log.isDebugEnabled()) {
    		log.debug("VmSubscriber.getGreeting>>specification type=" + specification.getType() + ", specification format=" + specification.getFormat());
        }
        GreetingManager greetingManager = greetingManagerFactory.getGreetingManager(
        		context,
        		subscriber.getSubscriberIdentity(DAConstants.IDENTITY_PREFIX_MSID),
        		phoneNumber,
        		getFolder(phoneNumber, specification.getType()));

		return greetingManager.getGreeting(specification);
	}

	public Future<IMediaObject> getGreetingAsync(
			GreetingSpecification specification)
			throws UnknownAttributeException {
		throw new UnsupportedOperationException("Unsupported greeting operation");
	}


	/**
     * Returns the subscriber's M3 mailbox
     * @return a mailbox
     * @throws HostException if no mailbox could be created
     */
    public IMailbox getMailbox() throws HostException  {
        if (log.isInfoEnabled()) log.info("getMailbox()");
        Object perf = null;
        try {
	    	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
	            perf = CommonOamManager.profilerAgent.enterCheckpoint("McdSubscriber.getMailbox()");
	        }
	        IMailbox mailbox;
	        if (mailBoxMap.containsKey(phoneNumber)) {
	            mailbox = mailBoxMap.get(phoneNumber);
	        } else {
	            try {
	            	String accountID = subscriber.getSubscriberIdentity(DAConstants.IDENTITY_PREFIX_MSID);
	                MailboxProfile mailboxProfile = new MailboxProfile(accountID, null, null);
	                mailbox = context.getMailboxAccountManager().getMailbox(null, mailboxProfile);
	                mailBoxMap.put(phoneNumber, mailbox);
	            } catch (MailboxException e) {
	                throw new HostException("Couldn't open mailbox: " + phoneNumber, e);
	            }
	        }
	        if (log.isInfoEnabled()) log.info("getMailbox() returns " + mailbox);
	        return mailbox;
        } finally {
        	if (CommonOamManager.profilerAgent.isProfilerEnabled()) {
				CommonOamManager.profilerAgent.exitCheckpoint(perf);
			}
        }
    }

	public IMailbox getMailbox(String mailHost, String accountID) {
		throw new UnsupportedOperationException("Unsupported mailbox operation");
	}


	public IMailbox getMailbox(String mailHost, String accountID,
			String accountPassword) throws MailboxException {
		throw new UnsupportedOperationException("Unsupported mailbox operation");
	}

	public IMediaObject getSpokenName(GreetingFormat format)
			throws ProfileManagerException {
        IMediaObject greeting = getGreeting(new SpokenNameSpecification(GreetingType.SPOKEN_NAME, format));
        return greeting;
	}

	public Future<IMediaObject> getSpokenNameAsync(GreetingFormat format)
			throws UnknownAttributeException {
		throw new UnsupportedOperationException("Unsupported greeting operation");
	}

	public void setGreeting(GreetingSpecification specification,
			IMediaObject greeting) throws ProfileManagerException {

		String msid = subscriber.getSubscriberIdentity(DAConstants.IDENTITY_PREFIX_MSID);
		GreetingManager greetingManager = greetingManagerFactory.getGreetingManager(
				context,
				msid,
				phoneNumber,
				getFolder(phoneNumber, specification.getType()));
		greetingManager.setGreeting(phoneNumber, specification, greeting);
	}

	public void setSpokenName(GreetingFormat format, IMediaObject spokenName, String duration)
			throws ProfileManagerException {
        if (log.isInfoEnabled()) log.info("setSpokenName(format=" + format + ", spokenName=" + spokenName + ")");
		setGreeting(new SpokenNameSpecification(GreetingType.SPOKEN_NAME, format, null, duration), spokenName);
	}

	public void setBooleanAttribute(String attribute, boolean value) throws ProfileManagerException {
		try {
			if (subscriber != null) {
				DirectoryAccess.getInstance().updateSubscriber(subscriber, attribute, Boolean.toString(value));
			}
			else {
				throw new ProfileManagerException("setBooleanAttribute: subscriber profile does not exist");
			}
		} catch (DirectoryAccessException dae) {
			throw new ProfileManagerException(dae.getMessage());
		}
	}

	public void setBooleanAttributes(String attribute, boolean[] value)	throws ProfileManagerException {
		try {
			if (subscriber != null) {
				String [] strValues = new String[value.length];
				int i=0;
				for (boolean v : value) {
					strValues[i++] = Boolean.toString(v);
				}
				DirectoryAccess.getInstance().updateSubscriber(subscriber, attribute, strValues);
			}
			else {
				throw new ProfileManagerException("setBooleanAttributes: subscriber profile does not exist");
			}
		} catch (DirectoryAccessException dae) {
			throw new ProfileManagerException(dae.getMessage());
		}
	}

	public void setIntegerAttribute(String attribute, int value) throws ProfileManagerException {
		try {
			if (subscriber != null) {
				DirectoryAccess.getInstance().updateSubscriber(subscriber, attribute, Integer.toString(value));
			}
			else {
				throw new ProfileManagerException("setIntegerAttribute: subscriber profile does not exist");
			}
		} catch (DirectoryAccessException dae) {
			throw new ProfileManagerException(dae.getMessage());
		}
	}

	public void setIntegerAttributes(String attribute, int[] value)	throws ProfileManagerException {
		try {
			if (subscriber != null) {
				String [] strValues = new String[value.length];
				int i=0;
				for (int v : value) {
					strValues[i++] = Integer.toString(v);
				}
				DirectoryAccess.getInstance().updateSubscriber(subscriber, attribute, strValues);
			}
			else {
				throw new ProfileManagerException("setIntegerAttributes: subscriber profile does not exist");
			}
		} catch (DirectoryAccessException dae) {
			throw new ProfileManagerException(dae.getMessage());
		}
	}

	public void setStringAttribute(String attribute, String value) throws ProfileManagerException {
		setStringAttributes(attribute, new String[] {value});
	}

	public void setStringAttributes(String attribute, String[] values) throws ProfileManagerException {
		//Default to replace
		setStringAttributes(attribute, values, Modification.Operation.REPLACE);
	}

	public void setStringAttributes(String attribute, String[] values, Modification.Operation op) throws ProfileManagerException {
		try {
			if (subscriber != null) {
				DirectoryAccess.getInstance().updateSubscriber(subscriber, attribute, values, op);
			}
			else {
				throw new ProfileManagerException("setStringAttributes: subscriber profile does not exist");
			}
		} catch (DirectoryAccessException dae) {
			throw new ProfileManagerException(dae.getMessage());
		}
	}

    private String getFolder(String telephoneNumber, GreetingType type) {
        switch (type) {
            case ALL_CALLS:
            case BUSY:
            case EXTENDED_ABSENCE:
            case NO_ANSWER:
            case OUT_OF_HOURS:
            case OWN_RECORDED:
            case TEMPORARY:
            case SPOKEN_NAME:
                //return MfsEventManager.moipFormatedTelephone(telephoneNumber) + "/Greeting";
            	//==> MIO 5.0 MIO5_MFS
            	return MfsFileFolder.PREFIX_MOIP + MfsEventManager.moipFormatedTelephone(telephoneNumber) + "_Greeting";
            case CDG:   // only one list of CDG per multiline inbox so store them under msid only.
                //return "Greeting";
            	//==> MIO 5.0 MIO5_MFS
            	return MfsFileFolder.PREFIX_MOIP + "Greeting";
            case DIST_LIST_SPOKEN_NAME:
                //return MfsEventManager.moipFormatedTelephone(telephoneNumber) + "/DistList";
                //==> MIO 5.0 MIO5_MFS
            	return MfsFileFolder.PREFIX_MOIP + MfsEventManager.moipFormatedTelephone(telephoneNumber) + "_DistList";
            default:
                throw new IllegalArgumentException(type.toString());
        }
    }

    /**
     * Sets the greeting manager factory.
     * <p>
     * <b>Note:</b> <i>This method is for testing purpose only.</i>
     * </p>
     * @param greetingManagerFactory Greeting manager factory object.
     */
    void setGreetingManagerFactory(GreetingManagerFactory greetingManagerFactory) {
    	this.greetingManagerFactory = greetingManagerFactory;
    }

	/**
	 * Return a list of all identities assigned to this subscriber's profile
	 * @param scheme - optional parameter which is used to filter on a specific scheme.
	 * @return an array of identities in String format.
	 * @throws ProfileManagerException
	 */
	@Override
	public String[] getIdentities(String scheme) throws ProfileManagerException{
		if (log.isDebugEnabled()) {
			log.debug("In McdSubscriber.getIdentitiesAsString() called with scheme : " + scheme + " for phoneNumber=" + phoneNumber);
		}

		if (subscriber != null) {

			URI[] identities = subscriber.getSubscriberProfile().getIdentities();

			if( (identities != null)  && (identities.length > 0)){
				List<String> identityList = new ArrayList<String>();
				for (int idx = 0 ; idx < identities.length; idx ++) {
                                        String identity = identities[idx].toString();

					// We include an identity if it matches the scheme or if there is no scheme filter provided.
					if (identities[idx].getScheme().equalsIgnoreCase(scheme) || (scheme == null)){
						identityList.add(identity);
						if (log.isDebugEnabled()) {
							log.debug("In McdSubscriber.getIdentitiesAsString() : processing identity " + identity);
						}
					}else{
						if (log.isDebugEnabled()) {
							log.debug("In McdSubscriber.getIdentitiesAsString() : identity " + identity + " did not match scheme : " + scheme);
						}
					}
				}

                                if (identityList.size() < 1) {
					throw new ProfileManagerException("McdSubscriber.getIdentitiesAsString():No Identities found for this profile :" + phoneNumber);
				}

				String [] identityStr = identityList.toArray(new String [identityList.size()]);

				return identityStr;
			}
			throw new ProfileManagerException("McdSubscriber.getIdentitiesAsString():No Identities found for this profile :" + phoneNumber);
		}
		throw new ProfileManagerException("McdSubscriber.getIdentitiesAsString():subscriber is not set for this profile : " + phoneNumber);
    }

	/**
	 * Return only the first matching identity to a certain scheme.
	 * This method is used because for certain keys, we are guaranteed that there shall
	 * be only one (muid, msid), and the getIdentities method strangely returns a mostly
	 * empty array, and if the desired scheme is not first, code will break.
	 *
	 * @param scheme - optional parameter which is used to filter on a specific scheme.
	 * @return an array of identities in String format.
	 * @throws ProfileManagerException
	 */
	@Override
	public String getIdentity(String scheme) throws ProfileManagerException{

		if ((scheme == null) || (subscriber == null)){
			throw new ProfileManagerException("Scheme or sub is null, cannot find identity");
		}
		if (log.isDebugEnabled()) {
			log.debug("VmSubscriber.getIdentity() called with scheme : " + scheme + " for phoneNumber=" + phoneNumber);
		}

		URI[] identities = subscriber.getSubscriberProfile().getIdentities();
		if( (identities != null)  && (identities.length > 0)){
			for (int idx = 0 ; idx < identities.length; idx ++) {
				if ( identities[idx].getScheme().equalsIgnoreCase(scheme)){
					return identities[idx].toString();
				}
			}
		}
		return null;
	}
}
