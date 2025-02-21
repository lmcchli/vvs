package com.mobeon.masp.profilemanager.distributionlists;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Vector;

import com.abcxyz.messaging.common.mcd.KeyValues;
import com.abcxyz.messaging.common.mcd.MCDConstants;
import com.abcxyz.messaging.common.mcd.Modification;
import com.abcxyz.messaging.common.mcd.Profile;
import com.abcxyz.messaging.common.mcd.ProfileContainer;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryAccessException;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryUpdater;
import com.abcxyz.services.moip.distributionlist.DistributionList;
import com.abcxyz.services.moip.distributionlist.DistributionListException;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.mobeon.common.logging.HostedServiceLogger;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.profilemanager.IMultimediaDistributionList;
import com.mobeon.masp.profilemanager.mediafile.AbstractMediaFile;

import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.abcxyz.messaging.identityformatter.IdentityFormatterInvalidIdentityException;

public class MultimediaDistributionList extends AbstractMediaFile implements IMultimediaDistributionList {

	public static final String INVALID_TEL = "tel:1111111111";

	public static final int MAX_DISTRIBUTIONLISTS = 100;
	private String userMsid = null;
	private String userTel = null;
	private String userCosIdentity = null;
	private int currentNumberOfLists = 0;

	private int id = -1;
	private String listmsid=null;
	private URI shortlistUri=null;
	private IDirectoryUpdater directoryUpdater = null;
	private List<String> members = new Vector<String>();
	private static final ILogger logg = ILoggerFactory.getILogger(MultimediaDistributionList.class);
    private static final HostedServiceLogger log = new HostedServiceLogger(logg);

	public MultimediaDistributionList(String listmsid, int listid, String aUserMsid, IDirectoryUpdater anUpdater) throws Exception {
		super("", "3gp");

		this.listmsid=listmsid;
		initialize(anUpdater, aUserMsid, listid);
		try {
			id = Integer.valueOf(listid);
		}
		catch (Exception e){
		    log.error("MultimediaDistributionList invalid list id",e);
		}
		initializeMembers(listid);
		if (log.isDebugEnabled()) {
    		log.debug("MultimediaDistributionList instance is constructed with id: " + id);
        }
	}

	/**
	 * This constructor is typically used when we intend to create or remove a list.
	 *
	 * @param aUserMsid
	 * @param anUpdater
	 * @throws IOException
	 */
	public MultimediaDistributionList(String aUserMsid, IDirectoryUpdater anUpdater) throws Exception {
		super("");
		initialize(anUpdater, aUserMsid, 0);
	}

	protected void initializeMembers(int anId){
		if (userTel == null){
			// No point initializing the members
			if (log.isDebugEnabled()) {
	    		log.debug("initializeMembers() Could not initialize list members for sub " + userMsid + " because can't find his tel");
	        }
			return;
		}
		try {


			if (log.isDebugEnabled()) {
	    		log.debug("initializeMembers() Getting list profile "  +  listmsid + " from MCD.");
	        }
			if(listmsid!=null){
			    Profile theShortlist = directoryUpdater.lookup("shortlist", new URI(listmsid));

	            if (theShortlist != null){
	                URI identitiesUriArray[] = theShortlist.getIdentities(MCDConstants.IDENTITY_SCHEME_SHORTLIST);
	                if (identitiesUriArray != null && identitiesUriArray.length > 0 ) {
	                    this.shortlistUri = identitiesUriArray[0];
	                }
	                
	                List<String> listEntryValues = theShortlist.getAttributeValues("MOIPListEntry");

	                if (listEntryValues != null) {
	                    if (log.isDebugEnabled()) {
	                        log.debug("initializeMembers() MOIPListEntry attribute has " + listEntryValues.size() + " value(s).");
	                    }



	                    for (int i = 0; i < listEntryValues.size(); i++) {
	                        String listEntry = listEntryValues.get(i);
	                        String[] entryParts= listEntry.split(";");

	                        if (log.isDebugEnabled()) {
	                            log.debug("initializeMembers() listEntry=" + listEntry);
	                        }

	                        String nickname=null;
	                        String tel=null;
	                        String msid=null;

	                        for(int j=0;j<entryParts.length;j++)
	                        {
	                            String[] partUri = entryParts[j].split(":");
	                            if(partUri.length != 2){

	                                log.error("initializeMembers()  Skiping uri because uri is malformed uri: "+partUri+" entry: "+listEntry+ " msid "+msid+" tel: "+tel);
	                                continue;
	                            }
	                            if("tel".equalsIgnoreCase(partUri[0])){
	                                tel=partUri[1];
	                            }
	                            else if("nickname".equalsIgnoreCase(partUri[0])){
	                                nickname=partUri[1];
	                            }
	                            else if("msid".equalsIgnoreCase(partUri[0])){
	                                msid=partUri[1];
	                            }
	                            else{
	                                log.error("initializeMembers()  Skiping uri because uri has unknown scheme uri: "+partUri+" entry: "+listEntry+ " msid "+msid+" tel: "+tel);
	                            }
	                        }

	                        if(tel==null)
	                        {
	                            log.error("initializeMembers()  Skiping invalid entry because mandatory tel number is not present  entry: "+listEntry+ " msid "+msid+" tel: "+tel);
	                            continue;
	                        }
	                        log.debug("initializeMembers member done tel: "+tel+" msid: "+msid+" nickname: "+nickname);
	                        members.add(tel);
	                    }
	                }
	                else {
	                    if (log.isDebugEnabled()) {
	                        log.debug("initializeMembers() List " + listmsid + " had no list entries, adding no members");
	                    }
	                }
	            }

			}
			else
			{
                if (log.isDebugEnabled()) {
                    log.debug("initializeMembers()  List No list associated with id "+anId);
                }

			}

		}
		catch (URISyntaxException e) {
            log.error("initializeMembers exception: "+e.toString(),e);
		}
	}



	public int getId(){
		return id;
	}

	protected String mediaFileName() {
		return getName() + ".3gp";
	}

	public String[] getMembers() throws DistributionListException {
		String arrayOfMembers[] = new String[members.size()];

		if (log.isDebugEnabled()) {
			log.debug("getMembers() members has length " + members.size());
		}
		for (int i=0;i < members.size(); i++) {
			String currentMember = members.get(i);
			if (log.isDebugEnabled()) {
				log.debug("getMembers()  processing member " + currentMember);
			}
			arrayOfMembers[i] = currentMember;
		}
		return arrayOfMembers;
	}

	public void manipulateListMember(Modification.Operation op, String listMember) throws DistributionListException {

		if (log.isDebugEnabled()) {
			log.debug("manipulateListMember() operation " + op + " member " + listMember);
		}
        if (listmsid==null || this.shortlistUri==null) 
        {
            log.error("manipulateListMember() list msid not initialized userTel: "+userTel+" id: "+id);
            throw new DistributionListException("manipulateListMember() list msid not initialized");
        }

		String member = listMember;
		try {
		       member = CommonMessagingAccess.getInstance().normalizeAddressField(member);
               if (log.isDebugEnabled()){
                   log.debug("manipulateListMember() Normalized member is now " + member);
              }
				Profile shortlist = lookupShortlist(new URI(listmsid));

				List<String> allEntries = shortlist.getAttributeValues(DAConstants.ATTR_SHORTLIST_ENTRY);
				int numberOfEntries = 0;
				if (allEntries != null){
					numberOfEntries = allEntries.size();
				}


				// Now that we know the member's msid, we can update the list
				List<Modification> mods = new Vector<Modification>();
				String values[] = { member };
				KeyValues kv = new KeyValues("MOIPListEntry", values);
				Modification mod = new Modification(op, kv);
				mods.add(mod);
				if (log.isDebugEnabled()){
					log.debug("manipulateListMember() Now that we have the msid of the member, lets update the list profile via CAI3G");
				}
				switch (op) {
				case ADD:
					if (log.isDebugEnabled()){
						log.debug("manipulateListMember() add member " + listMember + " to list.");
					}
                    Profile theSubscriber = directoryUpdater.lookup("subscriber", new URI(member));
                    if (theSubscriber != null){

                        int maxEntries = getMaxAttributeFromSubscriberCos(DAConstants.ATTR_DISTRIBUTIONLIST_MAXENTRIES);
                        if ((numberOfEntries + 1) <= maxEntries){
                            members.add(listMember);
                            numberOfEntries++;
                            directoryUpdater.updateProfile(MCDConstants.PROFILECLASS_SHORTLIST, this.shortlistUri, mods);
                        }
                        else {
                            log.debug("manipulateListMember() add member " + listMember + " is prevented because max number of entries is reached.");
                            throw new DistributionListException("Addition of new entry is not allowed because cos limit of " + maxEntries + " is reached.");
                        }
                    }
                    else {
                        log.error("manipulateListMember() Subscriber " + listMember + " is not found !");
                        throw new DistributionListException("Subscriber " + listMember + " is not found !");
                    }
					break;
				case REMOVE:
					if (log.isDebugEnabled()){
						log.debug("manipulateListMember() remove member " + listMember + " from list.");
					}
	                directoryUpdater.updateProfile(MCDConstants.PROFILECLASS_SHORTLIST, this.shortlistUri, mods);
					members.remove(listMember);
					numberOfEntries--;
					break;

				default:
					if (log.isDebugEnabled()){
						log.debug("manipulateListMember() unsupported operation " + op);
					}
	                break;
				}

				if (log.isDebugEnabled()){
					log.debug("manipulateListMember() " + op + " member to/from list successful !");
				}

		}
		catch (URISyntaxException e) {
			log.error("manipulateListMember() URISyntaxException",e);
			throw new DistributionListException(e.getMessage());
		} catch (DirectoryAccessException e) {
			log.error("manipulateListMember() DirectoryAccessException,e");
			throw new DistributionListException(e.getMessage());
		}
                catch (IdentityFormatterInvalidIdentityException e){
                        log.error("manipulateListMember() IdentityFormatterInvalidIdentityException,e");
                        throw new DistributionListException(e.getMessage());
                }

	}

	protected void initialize(IDirectoryUpdater anUpdater, String aUserMsid, int anId){
		userMsid = aUserMsid;
		directoryUpdater = anUpdater;
		DistributionList list = new DistributionList(userMsid);
		File path = null;
		try {
			path = list.getDistributionListDirectory(anId);
		} catch (DistributionListException e1) {
            log.error("initialize exception: "+e1.toString(),e1);
		}

		if(path==null)
		{
		    log.error("initialize DistributionList Directory is null");

		}
		else
		{
	        setName(path.getAbsolutePath() + File.separator + "listname");
		}

		try {
			Profile theSubscriber = directoryUpdater.lookup("subscriber", new URI(userMsid));
			if (theSubscriber != null){
				URI telIdentities[] = theSubscriber.getIdentities("tel");
				if (telIdentities.length > 0){
					userTel = telIdentities[0].toString();
					userCosIdentity = theSubscriber.getAttributeValues(DAConstants.ATTR_COS_IDENTITY).get(0);
					List<String> allLists = theSubscriber.getAttributeValues(DAConstants.ATTR_DISTRIBUTIONLIST);
					if (allLists != null){
						currentNumberOfLists = allLists.size();
					}
					else {
						currentNumberOfLists =0;
					}

				}
			}
		}
		catch (URISyntaxException e) {
            log.error("initialize exception: "+e.toString(),e);
		}
	}

	public String create(int anId) throws DistributionListException {
		if (userTel == null){
			String m = "Could not find subscriber " + userMsid + " in order to find his tel to build a list name, so returning error immediately";
			log.error("create()  " + m);
			throw new DistributionListException(m);
		}
		if ( (anId < 1) || (anId > MAX_DISTRIBUTIONLISTS)){
			String m = "Could not find subscriber " + userMsid + " in order to find his tel to build a list name, so returning error immediately";
			log.error("create()  " + m);
			throw new DistributionListException(m);
		}
		if (log.isDebugEnabled()){
			log.debug("create() Creating a list for subscriber " + userMsid + " (tel: "+ userTel + " with msid " + listmsid);
		}

		String listMuid = buildListMuid(userTel, anId);
		String listIdentity = buildListIdentity(userTel, anId);
		
		if (log.isDebugEnabled()){
		    log.debug("create() Creating a list for subscriber " + userMsid + " (tel: "+ userTel + " with muid " + listMuid);
		}

		int maxLists = getMaxAttributeFromSubscriberCos(DAConstants.ATTR_DISTRIBUTIONLIST_MAXLISTS);
		if ((currentNumberOfLists + 1) > maxLists){
			// Oups, busted
			String m = "create() Current subscriber " + userTel + " has " + currentNumberOfLists + "  lists but cos only allows "+ maxLists + " . Rejecting creation attempt.";
			log.debug(m);
			throw new DistributionListException(m);
		}

		URI keyId = null;

		try {


            keyId = new URI(listMuid);
            createShortlist(keyId, new URI(listIdentity));
			Profile listProfile = lookupShortlist(keyId);
			String shortlistMsid = listProfile.getIdentities("msid")[0].toString();
			if (log.isDebugEnabled()){
				log.debug("create() msid of the list we just created is " + shortlistMsid);
			}
			URI telURI = new URI(this.userTel);
			updateSubscriber(telURI, shortlistMsid, anId);
			listmsid=shortlistMsid;
			currentNumberOfLists++;
		}
		catch (URISyntaxException e1) {
			log.error("List msid is not an URI: " + listmsid,e1);
		}
		catch (DirectoryAccessException e) {
			// If we make it here, the PA is probably unavailable.
			// So we shouldn't worry about the fact that two outcomes could both end up here
			// (i.e. list creation, sub update)
			log.error("create() Unexpected DirectoryAccessException: " + e.getMessage(),e);
			throw new DistributionListException(e.getMessage());
		}
		return "";
	}

	public void remove(int anId) throws DistributionListException {
		try {
			removeListFromSubscriber(listmsid, anId);
			currentNumberOfLists--;
		}
		catch (DirectoryAccessException e){
	          log.error("MultimediaDistributionList Exception: " + e.getMessage(),e);

			throw new DistributionListException(e.getMessage());
		}
	}


    private void removeListFromSubscriber(String listMsid, int anId) throws DirectoryAccessException {
    	// Now that we know the list's msid, we can update the subscriber
		String fullListName = null;
    	URI subscriberTelURI = null;
    	try {
			subscriberTelURI = new URI(this.userTel);
			List<Modification> mods = new Vector<Modification>();
			fullListName = listMsid + ";" + String.valueOf(anId);
			String values[] = { fullListName } ;
			KeyValues kv = new KeyValues("MOIPDistributionList", values);
			Modification mod = new Modification(Modification.Operation.REMOVE,kv);
			mods.add(mod);
			if (log.isDebugEnabled()){
				log.debug("removeListFromSubscriber() Going to remove MOIPDistributionList " + fullListName + " from subscriber " +  userMsid + " MCD.");
			}
			directoryUpdater.updateProfile(MCDConstants.PROFILECLASS_SUBSCRIBER, subscriberTelURI, mods);
			if (log.isDebugEnabled()){
				log.debug("create() Success! Update worked");
			}
		}
    	catch (URISyntaxException e) {
    		log.error("URI Syntax Error for msid " + userMsid+" Exception: " + e.getMessage(),e);
		}
	}

	protected void createShortlist(URI keyId, URI listIdentityUri) throws DistributionListException{
    	try {
    		Profile myShortlist = new ProfileContainer();
            myShortlist.addIdentity(listIdentityUri);
    		myShortlist.addAttributeValue("MOIPListOwner", userMsid);
    		if (log.isDebugEnabled()){
    			log.debug("create() Creating MOIPShortlist with msid " + keyId.toString());
    		}
    		directoryUpdater.createProfile("shortlist", keyId , myShortlist);
    	}
    	catch (DirectoryAccessException e) {
    		// If we make it here, the PA is probably unavailable.
    		// So we shouldn't worry about the fact that two outcomes could both end up here
    		// (i.e. list creation, sub update)
    		log.error("create() Unexpected DirectoryAccessException: " + e.getMessage(),e);
    		throw new DistributionListException(e.getMessage());
        }
    }

    protected Profile lookupShortlist(URI keyId){
    	if (log.isDebugEnabled()){
			log.debug("lookupShortlist() List retrieve the msid of the list we just created");
		}

		Profile listProfile = directoryUpdater.lookup("shortlist", keyId);

		return listProfile;
    }

    protected int getMaxAttributeFromSubscriberCos(String anAttributeName){
    	int maxLists = 0;
    	try {
    		if ((userCosIdentity == null) || (userCosIdentity.length() == 0)){
    			if (log.isDebugEnabled()){
    				log.debug("getMaxAttributeFromSubscriberCos() Cannot find cos to find max lists; return 0 as a max");
    			}
    		}
    		else {
    			Profile cosProfile = lookupClassofService(new URI(userCosIdentity));
    			if (cosProfile != null){
    				List<String> maxListsArray = cosProfile.getAttributeValues(anAttributeName);
    				if (maxListsArray.size() > 0){
    					String maxListsAsString = maxListsArray.get(0);
    					try {
    						maxLists= Integer.parseInt(maxListsAsString);
    					}
    					catch (Exception e){
        					if (log.isDebugEnabled()){
        						log.debug("getMaxAttributeFromSubscriberCos() Could not parse value of " + anAttributeName + " as an integer. Value is " + maxListsAsString + " . Returning 0");
        					}
    					}
    				}
    				else {
    					if (log.isDebugEnabled()){
    						log.debug("getMaxAttributeFromSubscriberCos() Cannot find attribute " + anAttributeName + " is cos. Returning 0");
    					}
    				}
    			}
    			else {
    				if (log.isDebugEnabled()){
    					log.debug("getMaxAttributeFromSubscriberCos() Cannot find cos to find max lists; return 0 as a max");
    				}
    			}
    		}
    	} catch (URISyntaxException e) {
            log.error("getMaxAttributeFromSubscriberCos() Unexpected Exception: " + e.getMessage(),e);
    	}
    	return maxLists;
    }


    protected Profile lookupClassofService(URI keyId){
    	if (log.isDebugEnabled()){
			log.debug("lookupClassofService() Retrieve the cos " + keyId.toString());
		}

		Profile cosProfile = directoryUpdater.lookup("classofservice", keyId);
		return cosProfile;
    }

    protected void updateSubscriber(URI subscriberTelURI, String listMsid, int anId) throws DirectoryAccessException {
    	// Now that we know the list's msid, we can update the subscriber
		List<Modification> mods = new Vector<Modification>();
		String values[] = { listMsid + ";" + String.valueOf(anId) } ;
		KeyValues kv = new KeyValues("MOIPDistributionList", values);
		Modification mod = new Modification(Modification.Operation.ADD,kv);
		mods.add(mod);
		if (log.isDebugEnabled()){
			log.debug("create() Now that we have the msid of the list, lets update the subscriber profile via CAI3G");
		}
		directoryUpdater.updateProfile(MCDConstants.PROFILECLASS_SUBSCRIBER, subscriberTelURI, mods);
		if (log.isDebugEnabled()){
			log.debug("create() Success! Update worked");
		}
    }


    public static String buildListMuid(String aUserTel, int aListId) {
        String userTel = aUserTel;
        StringBuilder sb = new StringBuilder();
        sb.append("muid:");
        if (userTel.startsWith("tel:")){
            userTel = userTel.substring(4,userTel.length());
        }
        sb.append(userTel);
        sb.append("_");
        sb.append(String.valueOf(aListId));
        return sb.toString();
    }

    public static String buildListIdentity(String aUserTel, int aListId) {
        String userTel = aUserTel;
        StringBuilder sb = new StringBuilder();
        sb.append("shortlist:");
        if (userTel.startsWith("tel:")){
            userTel = userTel.substring(4,userTel.length());
        }
        sb.append(userTel);
        sb.append("_");
        sb.append(String.valueOf(aListId));
        return sb.toString();
    }
}
