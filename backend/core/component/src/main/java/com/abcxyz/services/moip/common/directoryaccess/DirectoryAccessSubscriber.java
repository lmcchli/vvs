package com.abcxyz.services.moip.common.directoryaccess;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.abcxyz.messaging.common.mcd.MCDConstants;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.common.complexattributes.ComplexAttributeException;
import com.abcxyz.services.moip.common.complexattributes.ComplexAttributeHelperFactory;
import com.abcxyz.services.moip.common.complexattributes.ComplexAttributes;
import com.abcxyz.services.moip.common.complexattributes.IComplexAttributeHelper;
import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;
import com.abcxyz.services.moip.provisioning.businessrule.ProvisioningConstants;

/**
 * A DirectoryAccessSubscriber is composed of a subscriber profile and cos profile in MCD.
 * If an attribute is not present in the subscriber, then it is fetched from the cos
 * of the subscriber.
 *
 * If a subscriber belongs to a COS with multiline service, the multiline specific attributes
 * are fetched from the multiline profile. If an attribute is not present in the multiline profile,
 * then it is fetched from the COS of the subscriber.
 */
public class DirectoryAccessSubscriber implements IDirectoryAccessSubscriber{

	private MoipProfile subscriberProfile;
	private MoipProfile cosProfile;
	private MoipProfile multilineProfile;
	private LogAgent logAgent;

	public DirectoryAccessSubscriber(MoipProfile subscriberProfile, MoipProfile cos, MoipProfile multiline, LogAgent logAgent) {
		this.subscriberProfile = subscriberProfile;
		this.cosProfile = cos;
		this.multilineProfile = multiline;
		this.logAgent = logAgent;
	}

	public MoipProfile getSubscriberProfile(){
		return subscriberProfile;
	}

	public MoipProfile getCosProfile(){
		return cosProfile;
	}

	public MoipProfile getMultilineProfile(){
		return multilineProfile;
	}

	public String[] getStringAttributes(String attrName) {

		if(logAgent.isDebugEnabled()){
			logAgent.debug("DirectoryAccessSubscriber: getStringAttributes Getting attrName: " + attrName);
		}
		attrName = AttributeNameMapper.getInstance().map(attrName);

		if(ComplexAttributes.members.containsKey(attrName)){

			//This is a complex attribute.
			//Extra logic is required.
			return getComplexStringAttributes(attrName);

		}
		else {
			return getSimpleStringAttributes(attrName);
		}
	}


	public String[] getComplexStringAttributes(String attrName) {

		//obtain the list of MCD attributes required to build the complex attribute
		HashMap<String, String[]> simpleAttributes = new HashMap<String, String[]>();
		Iterator<String> itr = ComplexAttributes.members.get(attrName).iterator();
		while(itr.hasNext()){
			String simpleAttribute = itr.next();
			String[] value = getSimpleStringAttributes(simpleAttribute);
			if(value != null && value.length > 0){
				simpleAttributes.put(simpleAttribute, value);
			}
		}

		//Now get the helper and build the content
		if(simpleAttributes.size() > 0) {
			List<HashMap<String, String[]>> list = new ArrayList<HashMap<String, String[]>>();
			list.add(simpleAttributes);

			String[] result = null;
			try {

				IComplexAttributeHelper helper = ComplexAttributeHelperFactory.getInstance().createHelper(attrName);

				result = helper.assembleComplexAttribute(list);

			} catch (ComplexAttributeException e) {
				logAgent.debug("DirectoryAccessSubscriber: getStringAttributes exception while assembling complex attribute" + e.getMessage());
			}

			if(result != null && result.length > 0) {
				if(logAgent.isDebugEnabled()){
					logAgent.debug("DirectoryAccessSubscriber: getStringAttributes attrName = " + result);
				}
				return result;
			}
		}
		String[] defaultStr = {""};
		if(logAgent.isDebugEnabled()){
			logAgent.debug("DirectoryAccessSubscriber.handleComplexAttribute " + attrName + "= empty string");
		}
		return defaultStr;
	}

	/**
	 * Does logic to aggregate info from multiline, cos, sub profiles
	 */
	public String[] getSimpleStringAttributes(String attrName) {

		String[] attrs = null;

		if(multilineProfile != null && DAConstants.multilineAttributes.contains(attrName)){
			attrs = multilineProfile.getStringAttributes(attrName);
			logAgent.debug("DirectoryAccessSubscriber.getSimpleStringAttributes: Getting multiline attribute " + attrName);
		}else {
			attrs = subscriberProfile.getStringAttributes(attrName);
		}

		if(attrs != null && attrs.length > 0){
			logAgent.debug("DirectoryAccessSubscriber: getSimpleStringAttributes sub attrName[0] = " + attrs[0]);
			return attrs;
		}else{
			attrs = cosProfile.getStringAttributes(attrName);
			if(attrs != null && attrs.length > 0){
				logAgent.debug("DirectoryAccessSubscriber: getSimpleStringAttributes cos attrName[0] = " + attrs[0]);
				return attrs;
			}
		}

		String[] defaultStr = {""};
		logAgent.debug("DirectoryAccessSubscriber: getSimpleStringAttributes " + attrName + "= empty string");
		return defaultStr;
	}


	public int[] getIntegerAttributes(String attrName) {
		attrName = AttributeNameMapper.getInstance().map(attrName);
		String[] stringAttributes = getStringAttributes(attrName);
		if(stringAttributes == null) {
			return null;
		}

		int[] result = new int[stringAttributes.length];
        for (int i = 0; i < stringAttributes.length; i++) {
            try {
                result[i] = Integer.parseInt(stringAttributes[i]);
            } catch (NumberFormatException e) {
                //String errMsg = "Integer attribute <" + attrName + "> could not be parsed";
                return new int[] {0};
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


	public String[] subscriberGetCosStringAttribute(String attrName) {
		return cosProfile.getStringAttributes(attrName);
	}

	public int[] subscriberGetCosIntegerAttribute(String attrName) {
		return cosProfile.getIntegerAttributes(attrName);
	}

	public boolean[] subscriberGetCosBooleanAttribute(String attrName) {
		return cosProfile.getBooleanAttributes(attrName);
	}

	//TODO fix to return String[]
	public String getSubscriberIdentity(String identityTag) {
		URI[] uris = subscriberProfile.getIdentities();
		for(URI uri: uris){
			String identity = uri.toString();
			if(identity.startsWith(identityTag)){
				return identity.substring(identity.indexOf(":")+1, identity.length());
			}
		}
		return null;
	}

    @Override
    public String[] getSubscriberIdentities(String identityTag) {
        String[] identityStrings = null;
        URI[] uris = subscriberProfile.getIdentities();
        if(uris != null && uris.length > 0) {
            ArrayList<String> identityArray = new ArrayList<String>();
            for(URI uri: uris){
                if(uri.toString().startsWith(identityTag)){
                    identityArray.add(uri.getSchemeSpecificPart());
                }                
            }
            identityStrings = identityArray.toArray(new String[identityArray.size()]);
        }
        return identityStrings;
    }

	/**
	 * Does this user have the multiline service
	 * @return true if subscriber belongs to a multiline cos
	 */
	public static boolean hasMultilineService(MoipProfile cosProfile){

		boolean isMultiline = false;
		String[] services = cosProfile.getStringAttributes(DAConstants.ATTR_SERVICES);
		if(services != null) {
			for(String s: services) {
				if(s.equalsIgnoreCase(ProvisioningConstants.SERVICES_MULTILINE)){
					isMultiline = true;
					break;
				}
			}
		}

		return isMultiline;
	}

	/**
	 * @return true if this subscriber has the voice mail service enabled, false otherwise.
	 */
	public boolean hasVoiceMailService() {
        boolean hasVoiceMailService = false;
        
        String[] services = subscriberProfile.getStringAttributes(MCDConstants.CNSERVICES_ATTRIBUTE_NAME);
        
        if(services != null) {
            for(String s: services) {
                if(s.equalsIgnoreCase(ProvisioningConstants.MOIP_SERVICE_NAME)){
                    hasVoiceMailService = true;
                    break;
                }
            }
        }
        
        return hasVoiceMailService;
	}

	public static void main(String args[]){
		String language = "en_GB";
		language = language.substring(0, language.indexOf('_'));
		System.out.println("language: " + language);
	}
}

