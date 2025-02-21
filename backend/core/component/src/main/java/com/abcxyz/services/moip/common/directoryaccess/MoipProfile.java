package com.abcxyz.services.moip.common.directoryaccess;

import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.abcxyz.messaging.common.mcd.Profile;
import com.abcxyz.messaging.common.mcd.ProfileContainer;
import com.abcxyz.messaging.common.oam.LogAgent;

public class MoipProfile {

	protected Profile profile;
	private static AttributeNameMapper attributeNameMapper = AttributeNameMapper.getInstance();
	private LogAgent logAgent = null;

	public MoipProfile(LogAgent logAgent) {
		profile = new ProfileContainer();
		this.logAgent = logAgent;
	}

	public MoipProfile (Profile aProfile, LogAgent logAgent) {
		profile = aProfile;
		this.logAgent = logAgent;
	}

	public URI[] getIdentities() {
		return profile.getIdentities();
	}

	public URI[] getIdentities(String scheme) {
		return profile.getIdentities(scheme);
	}

	public Profile getProfile() {
		return profile;
	}

	public String[] getStringAttributes(String attrName) {
		if (profile != null) {
			attrName = attributeNameMapper.map(attrName);
			List<String> s = profile.getAttributeValues(attrName);
			
			//We have to do this because of the way core handles multiple value attributes
			if(s != null){
				String[] result = s.toArray(new String[0]);
				for(int i=0; i<result.length; i++){
					result[i] = result[i].replace("&#44;", ",");
				}
				return result;
			}
		}
		
		return null;
	}

	public int[] getIntegerAttributes(String attrName) {
		String[] stringAttributes = getStringAttributes(attrName);

		if(stringAttributes == null) {
			return null;
		}

		int[] result = new int[stringAttributes.length];
        for (int i = 0; i < stringAttributes.length; i++) {
            try {
                result[i] = Integer.parseInt(stringAttributes[i]);
            } catch (NumberFormatException e) {
            	if (logAgent.isDebugEnabled()){
            		logAgent.debug("Profile.getIntegerAttributes() Integer attribute [" + attrName + "] could not be parsed");
            	}
                return null;
            }
        }
        return result;
	}

	public boolean[] getBooleanAttributes(String attrName) {
		String[] stringAttributes = getStringAttributes(attrName);

		if(stringAttributes == null) {
			return null;
		}

		boolean[] result = new boolean[stringAttributes.length];
        for (int i = 0; i < stringAttributes.length; i++) {
        	result[i] = stringAttributes[i].equalsIgnoreCase("yes") || stringAttributes[i].equalsIgnoreCase("true");
        }
        return result;
	}

	/**
	 * Return the list of attributes in Date format.
	 * @param attrName  name of attribute to fetch
	 * @param format the SimpleDateFormat to be used for parsing this date
	 * @return  Date - null will be returned for any entry where ParseException was encountered or attribute has no values.
	 * @throws ParseException
	 *
	 */
	public Date[] getDateAttributes(String attrName, SimpleDateFormat format) throws ParseException {

		String[] attrs = getStringAttributes(attrName);
		Date date[] = new Date [attrs.length];

		if(attrs != null && attrs.length > 0){
			for (int idx = 0; idx < attrs.length; idx ++){
				date[idx] = stringToDate(attrs[idx], format);
			}
		}
		return date;
	}
	/**
	 * Return a date attribute in Date format.
	 * @param attrName  name of attribute to fetch
	 * @param format the SimpleDateFormat to be used for parsing this date	 *
	 * @return  Date - null will be returned if ParseException was encountered or attribute has no values.
	 * @throws ParseException
	 *
	 */
	public Date getDateAttribute(String attributeName, SimpleDateFormat format) throws ParseException {
		Date date = null;

		String[] attrs = getStringAttributes(attributeName);
		if(attrs != null && attrs.length > 0){
			date = stringToDate(attrs[0], format);
		}
		return date;
	}

	private Date stringToDate(String dateStr, SimpleDateFormat format) throws ParseException {
		return format.parse(dateStr);
	}

	public String getStringAttribute(String attributeName) {
		String[] attrs = getStringAttributes(attributeName);
		if(attrs != null && attrs.length > 0){
			return attrs[0];
		}
		return null;
	}

	public int getIntegerAttribute(String attributeName) {
		int[] attrs = getIntegerAttributes(attributeName);
		if(attrs != null && attrs.length > 0){
			return attrs[0];
		}
		return -1;
	}

	public boolean getBooleanAttribute(String attributeName) {
		boolean[] attrs = getBooleanAttributes(attributeName);
		if(attrs != null && attrs.length > 0){
			return attrs[0];
		}
		return false;
	}

	public String toString(){
		return profile.toString();
	}


}
