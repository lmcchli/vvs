package com.abcxyz.services.moip.common.directoryaccess;

import java.util.HashMap;
import java.util.Map;

import com.abcxyz.services.moip.provisioning.businessrule.DAConstants;

/**
 * This class is intended to map pre-MIAB attribute names to the new MIAB attribute names
 *
 */
public class AttributeNameMapper {

	private Map<String, String> attributeNameMap;
	private static AttributeNameMapper attributeNameMapper = new AttributeNameMapper();

	public static synchronized AttributeNameMapper getInstance(){
		if(attributeNameMapper == null){
			attributeNameMapper = new AttributeNameMapper();
		}
		return attributeNameMapper;
	}

	private AttributeNameMapper(){
		attributeNameMap = new HashMap<String, String>();
		attributeNameMap.put("preferredlanguage", DAConstants.ATTR_PREFERRED_LANGUAGE);
		attributeNameMap.put("badlogincount", DAConstants.ATTR_BAD_LOGIN_COUNT);
		attributeNameMap.put("emnotifnumber", DAConstants.ATTR_NOTIF_NUMBER);
		attributeNameMap.put("empreferreddateformat", DAConstants.ATTR_PREFERRED_DATE_FORMAT);
		attributeNameMap.put("empreferredtimeformat", DAConstants.ATTR_PREFERRED_TIME_FORMAT);
		attributeNameMap.put("subscribertimezone", DAConstants.ATTR_SUBSCRIBER_TIME_ZONE);
		attributeNameMap.put("emservicedn", DAConstants.ATTR_SERVICES);
		attributeNameMap.put("emfilter", DAConstants.ATTR_FILTER);
		attributeNameMap.put("emnotifexptime", DAConstants.ATTR_NOTIF_EXP_TIME);
		attributeNameMap.put("messageplayvoice", DAConstants.ATTR_MESSAGE_PLAY_VOICE);
		attributeNameMap.put("includespokenname", DAConstants.ATTR_INCLUDE_SPOKEN_NAME);
		attributeNameMap.put("greetingsecmax", DAConstants.ATTR_GREETING_SEC_MAX);
		attributeNameMap.put("eomsgwarning", DAConstants.ATTR_EOMSG_WARNING);
		attributeNameMap.put("reinventory", DAConstants.ATTR_REINVENTORY);
		attributeNameMap.put("emoutboundcalltl", DAConstants.ATTR_OUTBOUND_CALL_TL);
		attributeNameMap.put("autoplay", DAConstants.ATTR_AUTOPLAY);
		attributeNameMap.put("emnoofmailquota", DAConstants.ATTR_NO_OF_MAIL_QUOTA);
		attributeNameMap.put("emodlpinskip", DAConstants.ATTR_OUTDIAL_PIN_SKIP);
		attributeNameMap.put("emcnl", DAConstants.ATTR_CNL);
		attributeNameMap.put("passwdlenmin", DAConstants.ATTR_PIN_MIN_LEN);
		attributeNameMap.put("passwdlenmax", DAConstants.ATTR_PIN_MAX_LEN);
		attributeNameMap.put("fastloginavailable", DAConstants.ATTR_FAST_LOGIN_AVAILABLE);
		attributeNameMap.put("fastloginenabled", DAConstants.ATTR_FAST_LOGIN_ENABLED);
		attributeNameMap.put("passwordskipavailable", DAConstants.ATTR_PIN_SKIP_AVAILABLE);
		attributeNameMap.put("maxloginlockout", DAConstants.ATTR_MAX_LOGIN_LOCKOUT);
		attributeNameMap.put("emftlfunctions", DAConstants.ATTR_FTL_FUNCTIONS);
		attributeNameMap.put("inhoursdow", DAConstants.ATTR_BUSINESS_DOW);
		attributeNameMap.put("phonenumbercalleraccesssection", DAConstants.ATTR_PHONE_NUMBER_CALLER_ACCESS_SECTION);
		attributeNameMap.put("phonenumberdialingsection", DAConstants.ATTR_PHONE_NUMBER_DIALING_SECTION);
		attributeNameMap.put("emoutdialsequence", DAConstants.ATTR_OUTDIAL_SEQUENCE);
		attributeNameMap.put("emtuiaccess", DAConstants.ATTR_TUI_ACCESS);
		attributeNameMap.put("emmsgplayorder", DAConstants.ATTR_MSG_PLAY_ORDER);
		attributeNameMap.put("emftl", DAConstants.ATTR_FTL);
		attributeNameMap.put("passwordskipenabled", DAConstants.ATTR_PIN_SKIP_ENABLED);
		attributeNameMap.put("activegreetingid", DAConstants.ATTR_ACTIVE_GREETING_ID);
		attributeNameMap.put("emtmpgrt", DAConstants.ATTR_TMP_GRT);
		attributeNameMap.put("emtmpgrtavailable", DAConstants.ATTR_TMP_GRT_AVAILABLE);
		attributeNameMap.put("cosdn", DAConstants.ATTR_COS_IDENTITY);
		attributeNameMap.put("emtuiblockedmenu", DAConstants.ATTR_TUI_BLOCKED_MENU);
		attributeNameMap.put("umpassword", DAConstants.ATTR_PIN);
		attributeNameMap.put("messageinventory", DAConstants.ATTR_MESSAGE_INVENTORY);
		attributeNameMap.put("emnotifdisabled", DAConstants.ATTR_NOTIF_DISABLED);
		attributeNameMap.put("emreadlevel", DAConstants.ATTR_READ_LEVEL);
		attributeNameMap.put("emservicesetting", DAConstants.ATTR_SERVICE_SETTING);
		attributeNameMap.put("emuserntd", DAConstants.ATTR_USER_NTD);
		attributeNameMap.put("emusersd", DAConstants.ATTR_USER_SD);
		attributeNameMap.put("msglenmaxvoice", DAConstants.ATTR_MSG_LEN_MAX_VOICE);
		attributeNameMap.put("emdeliveryprofile", DAConstants.ATTR_DELIVERY_PROFILE);
		attributeNameMap.put("subscriberxfer", DAConstants.ATTR_SUBSCRIBER_XFER);
		attributeNameMap.put("cn", DAConstants.ATTR_CN);
		attributeNameMap.put("emvuiaccess", DAConstants.ATTR_VUI_ACCESS);
		attributeNameMap.put("emmsglenmaxvideo", DAConstants.ATTR_MSG_LEN_MAX_VIDEO);
		attributeNameMap.put("messageplayvideo", DAConstants.ATTR_MESSAGE_PLAY_VIDEO);
		attributeNameMap.put("facsimileTelephoneNumber", DAConstants.ATTR_FAX_PRINT_NUMBER);
		attributeNameMap.put("dlmax", DAConstants.ATTR_DISTRIBUTIONLIST_MAXLISTS);
		attributeNameMap.put("dlentriesmax", DAConstants.ATTR_DISTRIBUTIONLIST_MAXENTRIES);
	}


	public String map(String attrName) {
		String lowercaseAttrName = attrName.toLowerCase();
		if(!lowercaseAttrName.startsWith("moip")){
			String newName = attributeNameMap.get(lowercaseAttrName);
			if(newName != null){
				return newName;
			}
		}
		return attrName;
	}

}
