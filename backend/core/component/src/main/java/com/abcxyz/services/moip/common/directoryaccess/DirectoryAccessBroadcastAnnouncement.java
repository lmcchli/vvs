package com.abcxyz.services.moip.common.directoryaccess;

import java.net.URI;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.provisioning.businessrule.ProvisioningConstants;

/**
 * A DirectoryAccessBroadcastAnnouncement is a profile in MCD to configure broadcast announcements.
 */
public class DirectoryAccessBroadcastAnnouncement implements IDirectoryAccessBroadcastAnnouncement{

	private MoipProfile profile;
	private LogAgent logAgent;
	private String name = null;

	public DirectoryAccessBroadcastAnnouncement(String name, MoipProfile profile, LogAgent logAgent) {
		this.profile = profile;
		this.logAgent = logAgent;
		this.name = name;

		if(logAgent.isDebugEnabled()) {
			logAgent.debug("DirectoryAccessBroadcastAnnouncement(): Finished creating " + name);
		}

	}


	/**
	 * Return the name of broadcast announcement.
	 * @return String - the name
	 */
	public String getName() {
		return name;
	}

	/**
	 *  Get the BA Profile
	 *  @return Profile - the BA profile that was provided at instance creation
	 */
	public MoipProfile getProfile(){
		return profile;
	}

	/**
	 * Fetch all values for a specified attribute
	 * @param attrName  name of attribute to be fetched
	 * @return String[] - array of values for this attribute - empty string if no values exist.
	 */
	public String[] getStringAttributes(String attrName) {

		String[] defaultStr = {""};
		String[] attrs = null;

		if(logAgent.isDebugEnabled()){
			logAgent.debug("DirectoryAccessSubscriber: getStringAttributes Getting attrName: " + attrName);
		}

		attrs = profile.getStringAttributes(attrName);
		if(attrs != null && attrs.length > 0){
			if(logAgent.isDebugEnabled()) {
				logAgent.debug("DirectoryAccessBroadcastAnnouncement: getStringAttributes attrName[0] = " + attrs[0]);
			}
		} else {
			if(logAgent.isDebugEnabled()) {
				logAgent.debug("DirectoryAccessSubscriber: getSimpleStringAttributes " + attrName + "= empty string");
			}
			attrs = defaultStr;
		}

		return attrs;
	}

	/**
	 * Fetch all values for a specified attribute and return them as integers
	 * @param attrName  name of attribute to be fetched
	 * @return int[] - array of values for this attribute - null returned if attribute is empty.
	 */
	public int[] getIntegerAttributes(String attrName) {
		int[] result = null;

		String[] stringAttributes = getStringAttributes(attrName);

		if(stringAttributes != null) {

			result = new int[stringAttributes.length];
			try {
				for (int i = 0; i < stringAttributes.length; i++) {
					result[i] = Integer.parseInt(stringAttributes[i]);
				}
			} catch (NumberFormatException e) {
				logAgent.warn("DirectoryAccessBroadcastAnnouncement.getIntegerAttributes(): Integer attribute <"
						+ attrName + "> could not be parsed",e);
				result = null;
			}
		}
		return result;
	}

	/**
	 * Fetch all values for a specified attribute and return them as array of boolean
	 * @param attrName  name of attribute to be fetched
	 * @return int[] - array of values for this attribute - null returned if attribute is empty.
	 */
	public boolean[] getBooleanAttributes(String attrName) {

		String[] stringAttributes = getStringAttributes(attrName);
		boolean[] result = null;

		if(stringAttributes != null) {
			result = new boolean[stringAttributes.length];
			for (int i = 0; i < stringAttributes.length; i++) {
				result[i] = stringAttributes[i].equalsIgnoreCase(ProvisioningConstants.YES)
				|| stringAttributes[i].equalsIgnoreCase(ProvisioningConstants.TRUE);
			}
		}

        return result;
	}

	/**
	 * Fetch identity and return as a String
	 * @return String[] - the identity as an array of String
	 */
	public String[] getIdentity(String identityTag) {

		URI[] uris = profile.getIdentities();
		String[] ids = null;

		// Create a string array the same size as the identities.
		// Retrieve all identities that are of type identityTag.
		String[] tmpids = new String[uris.length];
		int identityCnt = 0;
		for(URI uri: uris){
			String identity = uri.toString();
			if(identity.startsWith(identityTag)){
				tmpids[identityCnt++] = identity.substring(identity.indexOf(":")+1, identity.length());
			}
		}

		// now reduce the size of the array to fit the number of identities that matched identityTag, if required.
		if (identityCnt == uris.length){
			ids = tmpids;
		}else {
			ids = new String[identityCnt];
			System.arraycopy(tmpids, 0, ids, 0, identityCnt);
		}

		if(logAgent.isDebugEnabled()) {
			logAgent.debug("DirectoryAccessBroadcastAnnouncement.getIdentity(): Retrieved " + identityCnt +" Identities :" + ids);
		}

		return ids;
	}

	public static void main(String args[]){
		String language = "en_GB";
		language = language.substring(0, language.indexOf('_'));
		System.out.println("language: " + language);


	}
}

