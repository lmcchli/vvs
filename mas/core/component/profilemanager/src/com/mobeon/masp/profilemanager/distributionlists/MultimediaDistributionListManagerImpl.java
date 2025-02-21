package com.mobeon.masp.profilemanager.distributionlists;


import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Vector;

import com.abcxyz.messaging.common.mcd.Profile;
import com.abcxyz.services.moip.common.directoryaccess.DirectoryUpdater;
import com.abcxyz.services.moip.common.directoryaccess.IDirectoryUpdater;
import com.abcxyz.services.moip.distributionlist.DistributionList;
import com.abcxyz.services.moip.distributionlist.DistributionListException;
import com.abcxyz.services.moip.distributionlist.DistributionListManagerImpl;
import com.mobeon.common.logging.HostedServiceLogger;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaObjectException;
import com.mobeon.masp.mediaobject.MediaProperties;
import com.mobeon.masp.mediaobject.factory.MediaObjectFactory;
import com.mobeon.masp.profilemanager.IMultimediaDistributionList;


/**
 * This class extends the DistributionListManager of backend by adding the functionality of
 * updating the MCD via CAI3G.
 * That original class just manages the directory structure in private, because that is the service
 * common to both VM and PA.
 *
 * @author lmcantl
 *
 */
public class MultimediaDistributionListManagerImpl extends DistributionListManagerImpl implements MultimediaDistributionListManager {

	private static final ILogger logg = ILoggerFactory.getILogger(MultimediaDistributionListManagerImpl.class);
	private static final HostedServiceLogger log = new HostedServiceLogger(logg);
	private IDirectoryUpdater updater = null;

	public MultimediaDistributionListManagerImpl(String aUserMsid) {
		super(aUserMsid);
	}

	/**
	 * Updates the MCD via CAI3G, and then calls the super class to have the directory created.
	 * Creating a list via this method means creating the shortlist via CAI3G,
	 * updating the subscriber's MOIPDistributionList attribute with the msid returned,
	 * and finally creating the structure.
	 *
	 * @TODO this method will eventually accept the mediaobject that is to be stored to disk,
	 * or more likely a "specification" that will contain metadata to be put in a property file and
	 * also the binary.
	 */
	public String createDistributionList(int anId) throws DistributionListException {
		// Lets hardcode a limit for now
		updater = getDirectoryUpdater();
		try {
			if (log.isDebugEnabled()) {
				log.debug("createDistributionList() Creating list " + anId);
			}

			IMultimediaDistributionList theMMDList = isNoSQL() ?
					new MultimediaDistributionListNoSQL(null,anId, userMsid, updater) :
					new MultimediaDistributionList(null,anId, userMsid, updater);

			String returned = theMMDList.create(anId);
			if (log.isDebugEnabled()) {
				log.debug("createDistributionList() Back from creating list " + anId + " in MCD. Now doing MFS");
			}
			return returned;
		} catch (Exception e) {
			log.error("createDistributionList() Unexpected exception: " + e.getMessage(),e);
			throw new DistributionListException("Problem creating list");
		}
	}

	public String getDistributionListsMsid(int anId) throws DistributionListException {
		updater = getDirectoryUpdater();
		try {
			Profile theSubscriber = updater.lookup("subscriber", new URI(userMsid));
			List<String> values = theSubscriber.getAttributeValues("MOIPDistributionList");
			if (values != null) {
				if (log.isDebugEnabled()){
					log.debug("getDistributionLists() MOIPDistributionList attribute has " + values.size() + " value(s)");
				}
				for (int i=0; i < values.size(); i++) {
					String currentListMsid = values.get(i);
					if (log.isDebugEnabled()) {
						log.debug("getDistributionLists() currentListMsid=" + currentListMsid);
					}
					String tokens[] = currentListMsid.split(";");
					if (tokens.length != 2) {
						if (log.isDebugEnabled()) {
							log.debug("getDistributionLists() Problem. A list id in a subscriber doesn't have an id (msid;id): " + currentListMsid);
						}
					} else {
						String msid = tokens[0];
						String id = tokens[1];
						if(Integer.valueOf(id).intValue()==anId) {
							return msid;
						}
					}
				}
			}
		} catch (URISyntaxException e) {
			log.error("getDistributionLists() URI syntax error when handling list members. Exiting now and returning null",e);
		} catch (Exception e) {
			log.error("getDistributionLists() Exception " + e.toString(),e);
		}
		throw new DistributionListException("Problem removing list");
	}

	public IMultimediaDistributionList[] getDistributionLists() throws DistributionListException {
		if (log.isDebugEnabled()) {
			log.debug("getDistributionLists() Entering...");
		}
		List<IMultimediaDistributionList> allLists = new Vector<IMultimediaDistributionList>();
		updater = getDirectoryUpdater();
		try {
			Profile theSubscriber = updater.lookup("subscriber", new URI(userMsid));
			List<String> values = theSubscriber.getAttributeValues("MOIPDistributionList");
			if (values != null){
				if (log.isDebugEnabled()) {
					log.debug("getDistributionLists() MOIPDistributionList attribute has " + values.size() + " value(s)");
				}
				for (int i=0; i < values.size(); i++) {
					String currentListMsid = values.get(i);
					if (log.isDebugEnabled()){
						log.debug("getDistributionLists() currentListMsid=" + currentListMsid);
					}
					String tokens[] = currentListMsid.split(";");
					if (tokens.length != 2) {
						if (log.isDebugEnabled()) {
							log.debug("getDistributionLists() Problem. A list id in a subscriber doesn't have an id (msid;id): " + currentListMsid);
						}
					} else {
						String msid = tokens[0];
						String id = tokens[1];

						IMultimediaDistributionList theList = isNoSQL() ?
								new MultimediaDistributionListNoSQL(msid, Integer.valueOf(id).intValue(), userMsid, updater) :
								new MultimediaDistributionList(msid, Integer.valueOf(id).intValue(), userMsid, updater);

						allLists.add(theList);
					}
				}
			}
		} catch (URISyntaxException e) {
			log.error("getDistributionLists() URI syntax error when handling list members. Exiting now and returning null",e);
		} catch (IOException e) {
			log.error("getDistributionLists() IOException " + e.toString(),e);
		} catch (Exception e) {
			log.error("getDistributionLists() Exception " + e.toString(),e);
		}
		IMultimediaDistributionList arrayOfLists[] = new IMultimediaDistributionList[allLists.size()];
		return allLists.toArray(arrayOfLists);
	}



	@Override
	public void removeDistributionList(int anId) throws DistributionListException {
		updater = getDirectoryUpdater();
		try {
			if (log.isDebugEnabled()) {
				log.debug("removeDistributionList() Remove list " + anId);
			}

			IMultimediaDistributionList theMMDList = isNoSQL() ?
					new MultimediaDistributionListNoSQL(getDistributionListsMsid(anId), anId, userMsid, updater) :
					new MultimediaDistributionList(getDistributionListsMsid(anId), anId, userMsid, updater);

			theMMDList.remove(anId);
			if (log.isDebugEnabled()){
				log.debug("removeDistributionList() Back from removing list " + anId + " in MCD. Now doing MFS");
			}
		} catch (Exception e) {
			log.error("removeDistributionList() Unexpected exception removing list: " + e.getMessage(),e);
			throw new DistributionListException("Problem removing list");
		}
	}

	public IDirectoryUpdater getDirectoryUpdater(){
		if (updater == null){
			updater = DirectoryUpdater.getInstance();
		}
		return updater;
	}



	@Override
	public IMultimediaDistributionList getDistributionList(int anId) throws DistributionListException {

		IMultimediaDistributionList[] allLists = getDistributionLists();
		IMultimediaDistributionList aList = null;
		for (int i=0; i < allLists.length; i++) {
			if (log.isDebugEnabled()){
				log.debug("getDistributionLists(int)() got " + allLists[i]);
			}
			if (allLists[i].getId() == anId) {
				if (log.isDebugEnabled()){
					log.debug("getDistributionLists(int)() return list " + allLists[i]);
				}
				aList = allLists[i];
				break;
			}
		}

		if (aList == null) {
			throw new DistributionListException("List id " + anId + " does not exist!");
		} else {
			return aList;
		}
	}

	@Override
	public void setSpokenName(int anId, IMediaObject aSpokenName) throws DistributionListException {
		if (log.isDebugEnabled()) {
			log.debug("setSpokenName() Id = " + anId + " with a media object");
		}
		DistributionList list = new DistributionList(userMsid);
		File path = list.getDistributionListDirectory(anId);
		if (log.isDebugEnabled()) {
			log.debug("setSpokenName() Id = " + anId + " means a full path = " + path);
		}
		updater = getDirectoryUpdater();

		try {
			IMultimediaDistributionList mmList = isNoSQL() ?
					new MultimediaDistributionListNoSQL(getDistributionListsMsid(anId),anId, userMsid, updater) :
					new MultimediaDistributionList(getDistributionListsMsid(anId),anId, userMsid, updater);

			mmList.setMedia(aSpokenName.getInputStream());
			String contentType = aSpokenName.getMediaProperties().getContentType().toString();
			String fileExtension =aSpokenName.getMediaProperties().getFileExtension();
			String size = String.valueOf(aSpokenName.getMediaProperties().getSize());
			if (log.isDebugEnabled()) {
				log.debug("setSpokenName() Storing properties Content-Type = " + contentType + " File-Extension = "  + fileExtension + " File-Size " + size);
			}
			mmList.setProperty("Content-Type", contentType);
			mmList.setProperty("File-Extension", fileExtension);
			mmList.setProperty("File-Size", size);
			if (log.isDebugEnabled()) {
				log.debug("setSpokenName() Ready to store!");
			}
			mmList.store();
			if (log.isDebugEnabled()) {
				log.debug("setSpokenName() Back from store!");
			}
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("setSpokenName() IOException caught during store: " + e.getMessage(),e);
			}
		}
	}

	@Override
	public IMediaObject getSpokenName(int anId) throws DistributionListException {
		if (log.isDebugEnabled()) {
			log.debug("getSpokenName() Id = " + anId + " with a media object");
		}

		updater = getDirectoryUpdater();

		try {
			IMultimediaDistributionList mmList = isNoSQL() ?
					new MultimediaDistributionListNoSQL(getDistributionListsMsid(anId),anId, userMsid,updater) :
					new MultimediaDistributionList(getDistributionListsMsid(anId),anId, userMsid,updater);

			MediaProperties mProperties = new MediaProperties();
			InputStream is = mmList.getMedia();
			String contentType = mmList.getProperty("Content-Type");
			if (contentType != null) {
				MimeType theType = new MimeType(contentType);
				mProperties.setContentType(theType);
				String fileSize = mmList.getProperty("File-Size");
				if (fileSize != null) {
					mProperties.setSize(Long.valueOf(fileSize).longValue());
					String fileExtension = mmList.getProperty("File-Extension");
					if (fileExtension != null) {
						mProperties.setFileExtension(fileExtension);
						MediaObjectFactory factory = new MediaObjectFactory();
						IMediaObject mediaObject = factory.create(is, 4096, mProperties);
						return mediaObject;
					}
				}
			}
		} catch (MediaObjectException e) {
			log.error("getSpokenName exception: "+e.toString(),e);
		} catch (MimeTypeParseException e) {
			log.error("getSpokenName exception: "+e.toString(),e);
		} catch (Exception e) {
			log.error("getSpokenName exception: "+e.toString(),e);
		}
		return null;
	}


}
