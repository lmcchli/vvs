/**
 * #DisplayName Analyze Dnis 
 * #Description Analyze Dnis with VoiceSmsPrefix
 * #param dnis dnis The dialled number before number analysis.
 */
function Analyze_Dnis(dnis) {
    var depositPrefix = mas.systemGetConfigurationParameter("vva.voicesms", "depositprefix"); 
    var depositPrefixPostPaid = mas.systemGetConfigurationParameter("vva.voicesms", "depositprefixpostpaid");   
    var depositPrefixPrePaid = mas.systemGetConfigurationParameter("vva.voicesms", "depositprefixprepaid"); 

    	if ((dnis.substr(0, depositPrefix.length) == depositPrefix)&& (depositPrefix.length > 0)){
		Utility_SetGlobalObject('Subscriber', PhoneNumber_GetAnalyzedNumber(System_GetConfig('vva.numberanalysis', 'callednumberrule'), 
									dnis.substr(System_GetConfig('vva.voicesms', 'depositprefix').length), ''));
		return true;
		}

		else if ((dnis.substr(0, depositPrefixPostPaid.length) == depositPrefixPostPaid)&& (depositPrefixPostPaid.length > 0)){
		Utility_SetGlobalObject('Subscriber', PhoneNumber_GetAnalyzedNumber(System_GetConfig('vva.numberanalysis', 'callednumberrule'), 
										dnis.substr(System_GetConfig('vva.voicesms', 'depositprefixpostpaid').length), ''));   
		return true; 
		}

		else if ((dnis.substr(0, depositPrefixPrePaid.length) == depositPrefixPrePaid)&& (depositPrefixPrePaid.length > 0)){
		Utility_SetGlobalObject('Subscriber', PhoneNumber_GetAnalyzedNumber(System_GetConfig('vva.numberanalysis', 'callednumberrule'), 
										dnis.substr(System_GetConfig('vva.voicesms', 'depositprefixprepaid').length), '')); 
		return true;
		}
	else
		return false;
}

/**
 * #DisplayName Analyze only Prefix
 * #Description Sets GlobalObject.Subscriber.
 * #param dnis dnis The dialled number before number analysis.
 */
function Analyze_OnlyPrefix(dnis) {
	if ((System_GetConfig("vva.voicesms", "depositprefix").length > 0) && (Call_GetDnis() == System_GetConfig("vva.voicesms", "depositprefix")) &&
 		(System_GetConfig("vva.voicesms", "retrievalnew").length == 0) && (System_GetConfig("vva.voicesms", "retrievalold").length == 0))
		return true;


	else if ((System_GetConfig("vva.voicesms", "depositprefixpostpaid").length > 0) && (Call_GetDnis() == System_GetConfig("vva.voicesms", "depositprefixpostpaid")) &&
 		(System_GetConfig("vva.voicesms", "retrievalnewpostpaid").length == 0) && (System_GetConfig("vva.voicesms", "retrievaloldpostpaid").length == 0))
		return true;

	else if ((System_GetConfig("vva.voicesms", "depositprefixprepaid").length > 0) && (Call_GetDnis() == System_GetConfig("vva.voicesms", "depositprefixprepaid")) &&
 		(System_GetConfig("vva.voicesms", "retrievalnewprepaid").length == 0) && (System_GetConfig("vva.voicesms", "retrievaloldprepaid").length == 0))
		return true;

	else
		return false;
}


/**
 * #DisplayName Call Get ANI
 * #Description Returns the ANI of an incoming call (the calling party number).
 */
function Call_GetAni(){
	if (session.connection.remote.number != undefined)
    	return session.connection.remote.number;
    else
        return "";
}

/**
 * #DisplayName Call Get ANI PI
 * #Description Returns the PI for ANI of an incoming call (the presentation indicator for calling party number).
 */
function Call_GetAniPi(){
	if (session.connection.remote.pi != undefined)
    	return session.connection.remote.pi;
    else
    	return 1;
}

/**
 * #DisplayName Call Get DNIS
 * #Description Returns the DNIS of an incoming call (the called party number).
 */
function Call_GetDnis(){
	if (session.connection.local.number != undefined)
		return session.connection.local.number;
	else
        return "";
}

/**
 * #DisplayName Call Get Media Type
 * #Description Returns the type of media, "voice" or "video".
 */
function Call_GetMediaType(){
	if (session.connection.calltype != undefined)
	    return session.connection.calltype;
	else
		return "";	
}

/**
 * #DisplayName Call Get RDNIS
 * #Description Returns the RDNIS of an incoming call (the redirecting party number).
 */
function Call_GetRdnis(){
    if ((session.connection.redirect != undefined) && (session.connection.redirect.length > 0))
        return session.connection.redirect[0].number;
    else
        return "";
}

/**
 * #DisplayName Call Get RDNIS PI
 * #Description Returns the PI for RDNIS of an incoming call (the presentation indicator for redirecting party number).
 */
function Call_GetRdnisPi(){
    if ((session.connection.redirect != undefined) && (session.connection.redirect.length > 0))
    	return session.connection.redirect[0].pi;
    else
    	return 1;
}

/**
 * #DisplayName Call Get RDNIS Reason
 * #Description Returns the reason for redirection of an incoming call. Can be "unknown", "user busy", "no reply", 
 * "deflection during alerting", "deflection immediate response" or "mobile subscriber not reachable".
 */
function Call_GetRdnisReason(){
    if ((session.connection.redirect != undefined) && (session.connection.redirect.length > 0))
    	return session.connection.redirect[0].reason;
    else
    	return "unknown";
}

/**
 * #DisplayName Call Is Video
 * #Description Returns true if the call is a video call.
 */
function Call_IsVideo() {
   if (session != undefined)
      if (session.connection != undefined)
         if (session.connection.calltype != undefined)
            return (session.connection.calltype == "video");
   return false;
}

/**
 * #DisplayName Call Is Voice
 * #Description Returns true if the call is a voice call.
 */
function Call_IsVoice() {
   if (session != undefined)
      if (session.connection != undefined)
         if (session.connection.calltype != undefined)
            return (session.connection.calltype == "voice");
   return false;
}

/**
 * #DisplayName Call Is VoiceSMS or VideoSMS
 * #Description Returns true if the call is a VoiceSMS call or a VideoSMS call.
 * #param dnis dnis The dialled number before number analysis.
 */
function Call_IsVoiceSMS(dnis) {
    var method = mas.systemGetConfigurationParameter("vva.voicesms", "accountingmethod");  
    if (method == "disabled"){
        return false;  
	}  
    if (dnis == undefined){
	  return false;
	}
    if (dnis.length == 0){
        return false;
	}   
    
      

    if (method == "always"){
	var depositPrefix = mas.systemGetConfigurationParameter("vva.voicesms", "depositprefix"); 
		
		// Only prefix dialled. Only accepted if retrieval numbers are not configured.
		if ((dnis == depositPrefix) && (depositPrefix.length > 0)) {	
			if ((mas.systemGetConfigurationParameter("vva.voicesms", "retrievalnew").length == 0) && 
                		(mas.systemGetConfigurationParameter("vva.voicesms", "retrievalold").length == 0)){
			    return true;
			}
        }
	  // Prefix + number dialled
        else if ((dnis.substr(0, depositPrefix.length) == depositPrefix) && (depositPrefix.length > 0)){ 
            return true;
    	  }
    	if (dnis == mas.systemGetConfigurationParameter("vva.voicesms", "retrievalnew"))
        return true;
    	if (dnis == mas.systemGetConfigurationParameter("vva.voicesms", "retrievalold"))
        return true;

    return false;          
    }

    if (method == "access"){
	var depositPrefixPostPaid = mas.systemGetConfigurationParameter("vva.voicesms", "depositprefixpostpaid");   
   	var depositPrefixPrePaid = mas.systemGetConfigurationParameter("vva.voicesms", "depositprefixprepaid"); 

		// Only prefix dialled. Only accepted if retrieval numbers are not configured.
    		if ((dnis == depositPrefixPostPaid) && (depositPrefixPostPaid.length > 0)) {	
				if ((mas.systemGetConfigurationParameter("vva.voicesms", "retrievalnewpostpaid").length == 0) &&
                	(mas.systemGetConfigurationParameter("vva.voicesms", "retrievaloldpostpaid").length == 0)){
			  		return true;
           		}
       		}

			else if ((dnis == depositPrefixPrePaid) && (depositPrefixPrePaid.length > 0)) {	
				if ((mas.systemGetConfigurationParameter("vva.voicesms", "retrievalnewprepaid").length == 0) &&
		    		(mas.systemGetConfigurationParameter("vva.voicesms", "retrievaloldprepaid").length == 0)){
			  		return true;
           		}
       		}       	
	
	  // Prefix + number dialled
        else if (((dnis.substr(0, depositPrefixPostPaid.length) == depositPrefixPostPaid) && (depositPrefixPostPaid.length > 0)) || 
		     ((dnis.substr(0, depositPrefixPrePaid.length) == depositPrefixPrePaid )&& (depositPrefixPrePaid.length > 0))){   
            return true;
    	  }
    	  
    if ((dnis == mas.systemGetConfigurationParameter("vva.voicesms", "retrievalnewpostpaid"))||
	  (dnis == mas.systemGetConfigurationParameter("vva.voicesms", "retrievalnewprepaid")))
        return true;
    if ((dnis == mas.systemGetConfigurationParameter("vva.voicesms", "retrievaloldpostpaid"))||
	  (dnis == mas.systemGetConfigurationParameter("vva.voicesms", "retrievaloldprepaid")))
        return true;

    return false;          
    }
}

/**
 * #DisplayName DistributionList Get List
 * #Description Retrieves a distribution list.
 * #param listNumber ListNumber The number of the distribution list to retrieve.
 */
function DistributionList_GetList(listNumber) {
}

/**
 * #DisplayName DistributionList Get Member
 * #Description Retrieves a distribution list member.
 * #param listId listId The Id of the list.
 * #param memberNumber memberNumber The number of the distribution list member to retrieve.
 */
function DistributionList_GetMember(listId, memberNumber) {
}

/**
 * #DisplayName DistributionList Has Name Recorded
 * #Description
 * #param memberNumber memberNumber The number of the distribution list member.
 */
function DistributionList_HasNameRecorded(memberNumber)
{
}

/**
 * #DisplayName DistributionList Is Empty
 * #Description Returns true if the distribution list is empty.
 * #param listNumber ListNumber The number of the distribution list to check.
 */
function DistributionList_IsEmpty(listNumber)
{
}
function DistributionList_IsFull(listNumber)
{
}
function DistributionList_IsMemberAlreadyInTheList(phoneNumber)
{
}
function DistributionList_IsNumberAnExistingList(listNumber)
{
}
function DistributionList_IsNumberAValidList(listNumber)
{
}
function DistributionList_AddMember(listId, phoneNumber)
{
}
function DistributionList_CreateNew()
{
}
function DistributionList_DeleteMemberToDeleted(listId, phoneNumber)
{
}
function DistributionList_SetName(listId, name)
{
}
function DistributionList_Delete(listId)
{
}

/**
 * #DisplayName Event Get Message Size
 * #Description Returns a string with the size of the given message type.
 * #param messageId messageId The Id of the current message.
 */
function Event_GetMessageSize(messageId) {
	var contentArray = [];
	var size = 0;	
	var type = mas.messageGetStoredProperty(messageId, 'type')[0];	
	var sizeOK = true;
	
	if (type == "voice") {
		contentArray = mas.messageGetContent(messageId);				
		for (var i = 0; i < contentArray.length; i++) {		
			if (mas.messageGetMediaProperties(contentArray[i]).search("audio") != -1) {
			    if (mas.messageContentLength(contentArray[i], "milliseconds") != -1) {
				    size = size + mas.messageContentLength(contentArray[i], "milliseconds");
				} 
				else {
				    sizeOK = false;
				}
			}
		}
		if (sizeOK) {
		    size = Math.round(size/1000); // convert from milliseconds to seconds;
		}
		else {
		    size = -1;
		}
		return size.toString();
	}
	if (type == "video") {
		contentArray = mas.messageGetContent(messageId);				
		for (var i = 0; i < contentArray.length; i++) {		
			if (mas.messageGetMediaProperties(contentArray[i]).search("video") != -1) {
			    if (mas.messageContentLength(contentArray[i], "milliseconds") != -1) {
				    size = size + mas.messageContentLength(contentArray[i], "milliseconds");
				} 
				else {
				    sizeOK = false;
				}
			}
		}
		if (sizeOK) {
		    size = Math.round(size/1000); // convert from milliseconds to seconds;
		}
		else {
		    size = -1;
		}
		return size.toString();
	}
	if (type == "fax") {
		contentArray = mas.messageGetContent(messageId);				
		for (var i = 0; i < contentArray.length; i++) {		
			if (mas.messageGetMediaProperties(contentArray[i]).search("image/tiff") != -1) {
			    if (mas.messageContentLength(contentArray[i], "pages") != -1) {
  				    size = size + mas.messageContentLength(contentArray[i], "pages");
				} 
				else {
				    sizeOK = false;
				}
			}
		}
		if (!sizeOK) {
		    size = -1;
        }		
		return size.toString();
	}
	if (type == "email") {
		contentArray = mas.messageGetContent(messageId);				
		for (var i = 0; i < contentArray.length; i++) {
			if (mas.messageContentSize(contentArray[i]) != -1) {
			    size = size + mas.messageContentSize(contentArray[i]);
			} 
			else {
			    sizeOK = false;
			}
		}
		if (!sizeOK) {
		    size = -1;
        }
        return size.toString();
	}
	return "";
}

/**
 * #DisplayName Event Send Message State Event
 * #Description Sends event related to changing of a message state.
 * #param typeOfEvent typeOfEvent Valid values: 'read','reread','delete','save'.
 * #param subscriber subscriber The subscriber.
 * #param messageId messageId The Id of the current message.
 * #param ani ani The ANI of the inbound call.
 */
function Event_SendMessageStateEvent(typeOfEvent, subscriber, messageId, ani) {
	var eventName = "";
	var eventType = "";
	var messageType = "";
	var messageEncoding = "";
	var objectType = "1" //Message
	var messageSize = -1;
	var eventString;
	var type = "";
	
	if (typeOfEvent != "quotafullinformation") {
		messageSize = Event_GetMessageSize(messageId);
		type = mas.messageGetStoredProperty(messageId, "type")[0];
	} else {
		type = "voice";	
	}
		
	if (type == "voice") {
		if (typeOfEvent == "read") {
			eventName = "VoiceRead";
			eventType = "3"; //Retrieve
		}
		if (typeOfEvent == "reread") {
			eventName = "VoiceReRead";
			eventType = "3"; //Retrieve
		}
		if (typeOfEvent == "delete") {
			eventName = "VoiceDeleted";
			eventType = "6"; //Delete
		}
		if (typeOfEvent == "quotafullinformation") {
			eventName = "quotafullinformation";
		}
		
		messageType = "10"; //Voice
		messageEncoding = "10"; //wav
	}
	if (type == "video") {
		if (typeOfEvent == "read") {
			eventName = "VideoRead";
			eventType = "3"; //Retrieve
		}
		if (typeOfEvent == "reread") {
			eventName = "VideoReRead";
			eventType = "3"; //Retrieve
		}
		if (typeOfEvent == "delete") {
			eventName = "VideoDeleted";
			eventType = "6"; //Delete
		}
		messageType = "20"; //Video
		messageEncoding = "40"; //mov
	}
	if (type == "fax") {
		if (typeOfEvent == "read") {
			eventName = "FaxRead";
			eventType = "3"; //Retrieve
		}
		if (typeOfEvent == "reread") {
			eventName = "FaxReRead";
			eventType = "3"; //Retrieve
		}
		if (typeOfEvent == "delete") {
			eventName = "FaxDeleted";
			eventType = "6"; //Delete
		}
		messageType = "30"; //Image
		messageEncoding = "32"; //tiff
	}
	if (type == "email") {
		if (typeOfEvent == "read") {
			eventName = "EmailRead";
			eventType = "3"; //Retrieve
		}
		if (typeOfEvent == "reread") {
			eventName = "EmailReRead";
			eventType = "3"; //Retrieve
		}
		if (typeOfEvent == "delete") {
			eventName = "EmailDeleted";
			eventType = "6"; //Delete
		}
		messageType = "1"; //Text
		messageEncoding = "1"; //MIME
	}
	
	if (messageSize != -1) {
	    eventString = "username="        + subscriber +
	                  ",description="     + eventName +
	                  ",operation="       + eventType +
	                  ",objecttype="      + objectType + 
	                  ",messagetype="     + messageType + 
	                  ",messageencoding=" + messageEncoding +
	                  ",messagesize="     + messageSize + 
	                  ",callingnumber="   + ani;
	}
	else {
		eventString = "username="        + subscriber +
	                  ",description="     + eventName +
	                  ",operation="       + eventType +
	                  ",objecttype="      + objectType + 
	                  ",messagetype="     + messageType + 
	                  ",messageencoding=" + messageEncoding +
	                  ",callingnumber="   + ani;
	}

	System_SendTrafficEvent(eventName.toLowerCase(), eventString);
	return eventString;
}

/**
 * #DisplayName Event SendVoiceSmsReadEvent
 * #Description Sends event related to read or reread of a message.
 * #param typeOfEvent typeOfEvent Valid values: 'read','reread'.
 * #param subscriber subscriber The subscriber.
 * #param messageId messageId The Id of the current message.
 * #param ani ani The ANI of the inbound call.
 * #param port port The sssporttype.
 * #param accountingType accountingType The accountingType.
 */
function Event_SendVoiceSmsReadEvent(typeOfEvent, subscriber, messageId, ani, port, accountingType) {
	var eventName = "";
	var eventType = "3"; //Retrieve
	var messageType = "";
	var messageEncoding = "";
	var objectType = "1" //Message
	var accountType = "";
	var messageSize = Event_GetMessageSize(messageId);
	var eventString;
	
	var type = mas.messageGetStoredProperty(messageId, "type")[0];
	
	if (type == "voice") {
		if (typeOfEvent == "read") {
			eventName = "VoiceRead";
		}
		if (typeOfEvent == "reread") {
			eventName = "VoiceReRead";
		}
		messageType = "10"; //Voice
		messageEncoding = "10"; //wav

	}

	if (type == "video") {
		if (typeOfEvent == "read") {
			eventName = "VideoRead";
		}
		if (typeOfEvent == "reread") {
			eventName = "VideoReRead";
		}
		messageType = "20"; //Video
		messageEncoding = "40"; //mov
	}
	
	accountType = accountingType; 

	eventString = "username="         + subscriber +
	              ",description="     + eventName +
	              ",operation="       + eventType +
	              ",objecttype="      + objectType + 
	              ",messagetype="     + messageType + 
	              ",messageencoding=" + messageEncoding +
	              ",callingnumber="   + ani +
		        ",sssporttype="	    + port +
			  ",accounttype="	    + accountType;
		
		if (messageSize != -1) {
			eventString = eventString + ",messagesize="     + messageSize;
 		}

	System_SendTrafficEvent(eventName.toLowerCase(), eventString);
	
}


/**
 * #DisplayName Event Send Changed Greeting
 * #Description Sends event related to changing of a greeting.
 * #param subscriber subscriber The subscriber.
 * #param cdgNumber cdgNumber The number for which a caller dependent greeting is changed.
 * #param greetingType greetingType The type of greeting.
 * #param fun fun True if a fun greeting is selected.
 */
function Event_SendChangedGreeting(subscriber, cdgNumber, greetingType, fun) {
	var description = "ChangedGreeting";
	var typeOfOperation = "13";
	var messageType = "0";
	var objectType = "2" 
	var eventString;
	
	var portType;
	if (Call_IsVideo()) {
	    portType = 10;
	}
	else {
	    portType = 7;
	}
	
	if (fun) {
	    switch (greetingType) {
            case "Busy":
               messageType = 87; break;
            case "AllCalls":
               messageType = 88; break;
            case "OutOfHours":
               messageType = 89; break;
            case "Temporary":
               messageType = 90; break;
            case "NoAnswer":
               messageType = 91; break;
            case "CallerDependent":
               messageType = 92; break;
            case "Extended_Absence":
               messageType = 93; break;
        }
    }
    else {
	    switch (greetingType) {
            case "Busy":
               messageType = 80; break;
            case "AllCalls":
               messageType = 81; break;
            case "OutOfHours":
               messageType = 82; break;
            case "Temporary":
               messageType = 83; break;
            case "NoAnswer":
               messageType = 84; break;
            case "CallerDependent":
               messageType = 85; break;
            case "Extended_Absence":
               messageType = 86; break;
        }
    }         
	
	if (cdgNumber == null) {
	    eventString = "username="       + subscriber  +
	                  ",description=ChangedGreeting"  + 
	                  ",operation=13"                 +
	                  ",objecttype=2"                 + 
	                  ",messagetype="   + messageType;
	}
	else {
	    eventString = "username="       + subscriber  +
	                  ",description=ChangedGreeting"  + 
	                  ",operation=13"                 +
	                  ",objecttype=2"                 + 
	                  ",messagetype="   + messageType + 
	                  ",callingnumber=" + cdgNumber;
	}

	System_SendTrafficEvent("changedgreeting", eventString);
	return true;
}

/**
 * #DisplayName Family Mailbox Get extensions
 * #Description This function returns a list of all family mailbox extensions.
 * #param phoneNumber phoneNumber The subscribers (group owner) phonenumber.
 */
function FamilyMailbox_GetExtensions(phoneNumber) 
{
	/* Check if FM is active.*/
	var hasService = Subscriber_HasService(phoneNumber, "family_mailbox");
	if (hasService != true)
	   return [false];

	var members = mas.subscriberGetStringAttribute(phoneNumber, "emgroupmember");
	/* Check if groupowner.*/
	if (members[0] == "")
	   return [false];
	
	/* Assign array with extensions (1,2,3 etc.) */
	var i;
	var length = members.length;
	var extensionNr = new Array();
	var currExtension;
 	var stringResult;
	var j=0;
	for(i=0; i < length; i++) {
      	   currExtension = members[i];
           stringResult = parseInt(currExtension.substring(currExtension.indexOf("-")+1, currExtension.indexOf(",")), 10);
	   /*Check that extension is a number*/
           if ((isNaN(stringResult) == false) && (stringResult > 0) && (stringResult < 10)) {
		extensionNr[j] = stringResult;
		j++
	   }
        }
	/* sort the extension array*/
	extensionNr.sort();
     
        return extensionNr;   		
}

/**
 * #DisplayName FTL Set First Time Login
 * #Description If the subscriber has UserLevel (deprecated attribute), the current value of userlevel is converted to MOIPFtl.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 */
function FTL_SetFirstTimeLogin(phoneNumber) {
   if (PhoneNumber_IsASubscriber(phoneNumber) == false)
   	  return;
   if (Subscriber_GetSingleStringAttribute(phoneNumber, "MOIPFtl") != '')
      return;
   else {
         mas.subscriberSetStringAttribute(phoneNumber, "MOIPFtl", [Subscriber_GetSingleStringAttribute(phoneNumber, "MOIPFtlFunctions")]);
   }
}

/**
 * #DisplayName FTL Decrease And Set The FTLArray Counter
 * #Description Decreases the FTL counter with one and updates the profile with the new string.
 * #param subscriber subscriber The subscriber.
 * #param fTLString fTLString the FTL string.
 */
function FTL_DecreaseAndSetTheFTLArrayCounter(subscriber, fTLString) {
	var i = parseInt(fTLString.substr(0,2), 10) - 1;
	if (( i < 10) && (i > -1)) {
		fTLString = "0" + i.toString() + fTLString.substr(2, fTLString.length - 2);
	}
	else {
		fTLString = i.toString() + fTLString.substr(2, fTLString.length - 2);
	}
	
    mas.subscriberSetStringAttribute(subscriber, "MOIPFtl", [fTLString])
	return fTLString;
}

/**
 * #DisplayName FTL Set Part As Performed
 * #Description Stores information that the specified part of the First Time login procedure has been performed.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 * #param part part The first tiem login part. Can be "pin", "spokenname", "greeting", "language" or "notification"
 */
function FTL_SetPartAsPerformed(phoneNumber, part) {
	var fTLStringTmp = Subscriber_GetSingleStringAttribute(phoneNumber, "MOIPFtl");
	var fTLString = fTLStringTmp;
	
	if(part == "language") {
	    //Subscriber_SetSingleStringAttribute(phoneNumber, 'MOIPFtl', fTLString);
	    Subscriber_SetSingleStringAttribute(phoneNumber, 'MOIPFtlPreferredLanguageCompleted', "yes");
		
		// We still return fTLString but we do not need it.
	    fTLString = fTLString.substring(0,3) + "T" + fTLString.substring(4);
	    return fTLString;
	}

	var re ="";
	if(part == "pin")
		re = /,PIN:./;
	if(part == "spokenname")
		re = /,SPO:./;
	if(part == "greeting")
		re = /,ACG:./;
	if(part == "notification")
		re = /,NOT:./;
	
	fTLString = fTLString.replace(re, "");
	Subscriber_SetSingleStringAttribute(phoneNumber, 'MOIPFtl', fTLString);
	return fTLString;
}


/**
 * #DisplayName Greeting Activate
 * #Description Activates the greeting for the specified subscriber.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 * #param greetingType greetingType The type of greeting, eg AllCalls, Busy, NoAnswer, OutOfHours, Extended_Absence, Temporary, SpokenName.
 * #param mediaType mediaType The media type of the greeting, eg voice/video. FOR FUTURE USE!
 * #param phoneNumberCaller phoneNumberCaller The phone number of caller. Only used when greetingType is CallerDependent. Otherwise set it to null.
 */
function Greeting_Activate(phoneNumber, greetingType, mediaType, phoneNumberCaller) {

    var activeGreetingsTmp = mas.subscriberGetStringAttribute(phoneNumber, "MOIPActiveGreetingId")[0];
    var activeGreetings = activeGreetingsTmp;
    if (greetingType == "Temporary") {
        var tmpgrt=new Array(";");
        mas.subscriberSetStringAttribute(phoneNumber, "MOIPTmpGrt", tmpgrt);
    }      
    else if (greetingType == "CallerDependent") {
	var activeCdg = mas.subscriberGetStringAttribute(phoneNumber, "emactivecdg")[0];
	if (activeCdg.search("#" + phoneNumberCaller + "#") == -1) {
	   if (activeCdg.length > 0)
		mas.subscriberSetStringAttribute(phoneNumber, "emactivecdg", [activeCdg + ",#" + phoneNumberCaller + "#"]);
	   else
		mas.subscriberSetStringAttribute(phoneNumber, "emactivecdg", ["#" + phoneNumberCaller + "#"]);
	}	   
    }
    else if (activeGreetings.indexOf(greetingType) == -1) {
	    activeGreetings = activeGreetings.replace(/Extended_Absence/, "");
        activeGreetings = activeGreetings.replace(/[\s,]*$/, ""); //remove commas at the end
        activeGreetings = activeGreetings.replace(/^[\s,]*/, ""); //remove commas at the beginning
        if(activeGreetings.length > 0)
            activeGreetings = activeGreetings + "," + greetingType;
        else
            activeGreetings = greetingType;

        mas.subscriberSetStringAttribute(phoneNumber, "MOIPActiveGreetingId", [activeGreetings]);
    }
}
/**
 * #DisplayName Greeting Deactivate
 * #Description Deactivates the greeting for the specified subscriber.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 * #param greetingType greetingType The type of greeting, eg AllCalls, Busy, NoAnswer, OutOfHours, Extended_Absence, Temporary, SpokenName.
 * #param mediaType mediaType The media type of the greeting, eg voice/video. FOR FUTURE USE!
 * #param phoneNumberCaller phoneNumberCaller The phone number of caller. Only used when greetingType is CallerDependent. Otherwise set it to null.
 */
function Greeting_Deactivate(phoneNumber, greetingType, mediaType, phoneNumberCaller) {

    var activeGreetingsTmp = mas.subscriberGetStringAttribute(phoneNumber, "MOIPActiveGreetingId")[0];
    var activeGreetings = activeGreetingsTmp;
    if (greetingType == "Temporary") {
        mas.subscriberSetStringAttribute(phoneNumber, "MOIPTmpGrt", null);
    }   
    else if (greetingType == "CallerDependent") {
	var activeCdg = mas.subscriberGetStringAttribute(phoneNumber, "emactivecdg")[0];
  	var s1 = new String(activeCdg);
  	var s2 = s1.replace("#" + phoneNumberCaller + "#", "");
	s2 = s2.replace(/^[\s,]*/, "");
	s2 = s2.replace(/[\s,]*$/, "");
	s2 = s2.replace(/,{2,}/, ",");
	mas.subscriberSetStringAttribute(phoneNumber, "emactivecdg", [s2]);	   
    }
    else if (activeGreetings.indexOf(greetingType) != -1) {
        var regexp = new RegExp(greetingType);
        activeGreetings = activeGreetings.replace(regexp, "");
        activeGreetings = activeGreetings.replace(/[\s,]*$/, ""); //remove commas at the end
        activeGreetings = activeGreetings.replace(/^[\s,]*/, ""); //remove commas at the beginning
        activeGreetings = activeGreetings.replace(/,,/, ","); //remove double commas by a single comma
        mas.subscriberSetStringAttribute(phoneNumber, "MOIPActiveGreetingId", [activeGreetings]);
    }
}
/**
 * #DisplayName Greeting Delete
 * #Description Deletes the greeting for the specified subscriber.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 * #param greetingType greetingType The type of greeting, eg AllCalls, Busy, NoAnswer, OutOfHours, Extended_Absence, Temporary, SpokenName, CallerDependent.
 * #param mediaType mediaType The media type of the greeting, eg voice/video.
 * #param phoneNumberCaller phoneNumberCaller The phone number of caller. Only used when greetingType is CallerDependent. Otherwise set it to null.
 */
function Greeting_Delete(phoneNumber, greetingType, mediaType, phoneNumberCaller) {
    if (greetingType == "SpokenName")
	mas.subscriberSetSpokenName(phoneNumber, mediaType, null);
    else if (greetingType == "CallerDependent")
	mas.subscriberSetGreeting(phoneNumber, "cdg", mediaType, phoneNumberCaller, null);
    else
	mas.subscriberSetGreeting(phoneNumber, greetingType.toLowerCase(), mediaType, phoneNumberCaller, null);

    Greeting_Deactivate(phoneNumber, greetingType, mediaType, phoneNumberCaller);
}
/**
 * #DisplayName Greeting Get Fun Greeting
 * #Description Returns the fun greeting specified.
 * #param mediaContentId mediaContentId The id of the fun greeting.
 */
function Greeting_GetFunGreeting(mediaContentId) {
    return mas.systemGetMediaContent("fungreeting", mediaContentId, null);
}
/**
 * #DisplayName Greeting Get Fun Greeting List
 * #Description Returns a list of available fun greetings.
 */
function Greeting_GetFunGreetingList() {
    return mas.systemGetMediaContentIds("fungreeting", null);
}
/**
 * #DisplayName Greeting Get Greeting
 * #Description Returns the greeting file specified.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 * #param greetingType greetingType The type of greeting, eg AllCalls, Busy, NoAnswer, OutOfHours, Extended_Absence, Temporary, SpokenName, CallerDependent.
 * #param phoneNumberCaller phoneNumberCaller The phone number of caller. Only used when greetingType is CallerDependent. Otherwise set it to null.
 * #param mediaType mediaType The media type of greeting. voice/video
 */
function Greeting_GetGreeting(phoneNumber, greetingType, mediaType, phoneNumberCaller){
    greetingType = greetingType.toLowerCase();
    if (greetingType == "callerdependent")
	greetingType = "cdg";

    if (greetingType == "spokenname")
	return mas.subscriberGetSpokenName(phoneNumber, mediaType);
    else
	return mas.subscriberGetGreeting(phoneNumber, greetingType, mediaType, phoneNumberCaller);
}
/**
 * #DisplayName Greeting Has Temporary Greeting Activation Time Passed
 * #Description Returns true if the activation time for temporary greeting has passed for the subscriber with the specified phone number.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 */
function Greeting_HasTemporaryActivationTimePassed(phoneNumber) {
    var tmpGreeting = mas.subscriberGetStringAttribute(phoneNumber, "MOIPTmpGrt")[0];
    var index = tmpGreeting.indexOf(";");
    var activationTime = tmpGreeting.substring(0, index);

    //Expected time format is: YYYY-MM-DD HH:MM:SS
    var currentTime = util.getCurrentTime(mas.subscriberGetStringAttribute(phoneNumber, 'CNTimeZone')[0]);
    currentTime = util.formatTime(currentTime, 'yyyy-MM-dd HH:mm:ss');
    if (currentTime > activationTime)
	return true;
    else
	return false;
}
/**
 * #DisplayName Greeting Has Temporary Greeting Deactivation Time Passed
 * #Description Returns true if the deactivation time for temporary greeting has passed for the subscriber with the specified phone number.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 */
function Greeting_HasTemporaryDeactivationTimePassed(phoneNumber) {
    var tmpGreeting = mas.subscriberGetStringAttribute(phoneNumber, "MOIPTmpGrt")[0];
    var index = tmpGreeting.indexOf(";");
    var deactivationTime = tmpGreeting.substring(index+1, tmpGreeting.length-1);
    if (deactivationTime.length == 0)
        return false;

    //Expected time format is: YYYY-MM-DD HH:MM:SS
    var currentTime = util.getCurrentTime(mas.subscriberGetStringAttribute(phoneNumber, "CNTimeZone")[0]);
    currentTime = util.formatTime(currentTime, 'yyyy-MM-dd HH:mm:ss');

    if (currentTime > deactivationTime)
	return true;
    else
	return false;
}
/**
 * #DisplayName Greeting Is Active
 * #Description Returns true if the specified greeting is active.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 * #param greetingType greetingType The type of greeting, eg AllCalls, Busy, NoAnswer, OutOfHours, Extended_Absence, Temporary, SpokenName, CallerDependent.
 * #param mediaType mediaType The media type of the greeting, eg voice/video. FOR FUTURE USE!
 */
function Greeting_IsActive(phoneNumber, greetingType, mediaType) {
    var activeGreetings = mas.subscriberGetStringAttribute(phoneNumber, "MOIPActiveGreetingId")[0];
 
    if (greetingType == "Temporary") {
        if (mas.subscriberGetStringAttribute(phoneNumber, "MOIPTmpGrt")[0] == null)
            return false;
        else if (mas.subscriberGetStringAttribute(phoneNumber, "MOIPTmpGrt")[0].indexOf(";") != -1)
            return true;   
        else
            return false;
    }
    if (greetingType == "CallerDependent") {
        if (mas.subscriberGetStringAttribute(phoneNumber, "emactivecdg")[0].indexOf("#") != -1)
            return true;
        else
            return false;   
    }
    else if (activeGreetings.indexOf(greetingType) != -1)
        return true;
    else
        return false;
}

/**
 * #DisplayName Greeting Is Outside Business Hours
 * #Description Returns true if the current time is outside the businesss hours for the specified subscriber.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 */
function Greeting_IsOutsideBusinessHours(phoneNumber) {
    var businessDays = mas.subscriberGetStringAttribute(phoneNumber, "MOIPInHoursDOW")[0];
    var startTime = mas.subscriberGetStringAttribute(phoneNumber, "MOIPInHoursStart")[0];
    var endTime = mas.subscriberGetStringAttribute(phoneNumber, "MOIPInHoursEnd")[0];
    
    //Expected time format is: YYYY-MM-DD HH:MM:SS
    var currentTime = util.getCurrentTime(mas.subscriberGetStringAttribute(phoneNumber, "CNTimeZone")[0]);
    currentTime = util.formatTime(currentTime, 'yyyy-MM-dd HH:mm:ss');

    var time = new Date(currentTime.substr(0,4),parseInt(currentTime.substr(5,2)-1),currentTime.substr(8,2),
                        currentTime.substr(11,2),currentTime.substr(14,2),currentTime.substr(17,2));

    if (businessDays.indexOf(time.getDay()+1) < 0)
        return true;
    else if ((parseInt(startTime,10) > parseInt(time.getHours().toString() + time.getMinutes().toString(), 10) || 
            parseInt(endTime,10) < parseInt(time.getHours().toString() + time.getMinutes().toString(), 10)))
        return true;
    else
        return false;
}

/**
 * #DisplayName Greeting Store Recording
 * #Description Stores the recorded greeting for the specified subscriber.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 * #param greetingType greetingType The type of greeting, eg AllCalls, Busy, NoAnswer, OutOfHours, Extended_Absence, Temporary, SpokenName.
 * #param mediaType mediaType The media type of the greeting, eg voice/video.
 * #param phoneNumberCaller phoneNumberCaller The phone number of caller. Only used when greetingType is CallerDependent. Otherwise set it to null.
 * #param greetingRef greetingRef The file that contains the recording.
 */
function Greeting_StoreRecording(phoneNumber, greetingType, mediaType, phoneNumberCaller, greetingRef) {
    if (greetingType == "SpokenName")
	mas.subscriberSetSpokenName(phoneNumber, mediaType, greetingRef);
    else if (greetingType == "CallerDependent") {
	mas.subscriberSetGreeting(phoneNumber, "cdg", mediaType, phoneNumberCaller, greetingRef);
	Greeting_Activate(phoneNumber, greetingType, mediaType, phoneNumberCaller);
    }
    else
	mas.subscriberSetGreeting(phoneNumber, greetingType.toLowerCase(), mediaType, phoneNumberCaller, greetingRef);
}
function Greeting_SetTemporaryAutomaticDeactivation(phoneNumber, days) {
}

/*--------------------------------------------------------
 * This script is run by the simulator when the simulation
 * is restarted. This function should clear or initialize
 * any variables declared in this script file.
 *-------------------------------------------------------*/
function initScriptLibrary() {
	MenuArray = new Array();
	GlobalObject = { ANI:'', Subscriber:'', LoggedIn:false, LoginType:'retrieval', MainMenuValid:false, PreviousMenuValid:false, CallState:'init' };
}

function Login_GetCosWelcomeGreeting(phoneNumber)
{
}

/**
 * #DisplayName Login Is Own Phone Check OK
 * #Description Returns true if Calling from own phone or initAnyPhone=yes.
 * #param subscriber subscriber The phone number of the subscriber.
 * #param ani ani The phone number of the caller.
 * #param loginType loginType The login type, e.g. retrieval or outdialnotification.
 */
function Login_IsOwnPhoneCheckOk(subscriber, ani, loginType){
    if ((subscriber == ani) && (loginType != 'outdialnotification'))
	    return true;
	else if ((subscriber == PhoneNumber_GetAnalyzedNumber(System_GetConfig('vva.numberanalysis', 'callednumberrule'), Call_GetDnis(), '')) && (loginType == 'outdialnotification'))
	    return true;
	else if (mas.systemGetConfigurationParameter("vva.dialogbehaviour", "initpinanyphone") == "yes")
	    return true;
	else {
	    var ftlString = mas.subscriberGetStringAttribute(subscriber, "MOIPFtl")[0];
	    if (ftlString.substring(0,2) == '-1')
	        return true;
	    else if (ftlString.indexOf('PIN') == -1)
            return true;
        else
            return false;
    }
}

/**
 * #DisplayName Login Get Phone Number
 * #Description Returns true if the calling party must enter the phone number during the login procedure.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 */
function Login_GetPhoneNumber(phoneNumber) {
    if (mas.subscriberExist(phoneNumber)) {
        if ((mas.subscriberGetBooleanAttribute(phoneNumber, "MOIPFastLoginAvailable")[0] == true) && 
            (mas.subscriberGetBooleanAttribute(phoneNumber, "MOIPFastLoginEnabled")[0] == true)) { 
            return false;
        } 
        else {
            return true;
        }
    }
    else { 
        return true;
    }
}

/**
 * #DisplayName Login Shall Preferred Language Be Set
 * #Description Returns true if the preferred language needs to be set during login, that is, LoginSetLanguages==Yes and MOIPFtl contains 'T'. 
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 */
function Login_ShallPreferredLanguageBeSet(phoneNumber) {
	if (mas.systemGetConfigurationParameter("vva.dialogbehaviour", "loginsetlanguages") == "no")
		return false;
	else if (!mas.subscriberExist(phoneNumber))
		return true;
	else {
		
		// MULTILANG:   we no longer extract the preferredlanguage setting from MOIPFtl.  We get it directly
		var prefLangCompleted = mas.subscriberGetStringAttribute(phoneNumber, "MOIPFtlPreferredLanguageCompleted")[0];
		var ftlString = mas.subscriberGetStringAttribute(phoneNumber, "MOIPFtl")[0];
		System_Log(4,"MultiLang: Login_ShallPreferredLanguageBeSet() : MOIPFtlPreferredLanguageCompleted = " +prefLangCompleted + ", ftlString = " +ftlString);
		if (ftlString.substring(0,2) == '-1')
			return false;
		else if (prefLangCompleted == 'no'){
			return true;
		}
		else{
			return false;
		}
	}
}

/**
 * #DisplayName Mailbox Get Folder
 * #Description Returns a folder id.
 * #param mailboxId mailboxId The Id of the mailbox.
 * #param folder folder The folder name.
 */
function Mailbox_GetFolder(mailboxId, folder) {
	return mas.mailboxGetFolder(mailboxId, folder);
}

/**
 * #DisplayName Mailbox Delete Messages for Voice SMS
 * #Description Deletes the oldest read messages in the list that makes the list exceed the given limit.
 * If maxNoOfMessages is 10, the following apply: If there are 12 new, no messages are deleted. If there are 8 new and
 * 4 old, 2 old will be deleted. 
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 * #param messageListId messageListId The Id of the message list.
 */
function Mailbox_DeleteMessagesVoiceSms(phoneNumber, messageListId) {
    // Fix to solve problem with FolderClosedException. Folder must be opened but criterias are chosen so no messages are fetched.
    var folderId = mas.mailboxGetFolder(mas.subscriberGetMailbox(phoneNumber), "inbox");        // Fix for FolderClosedException
	var msgListId = mas.mailboxGetMessageList(folderId, "fax" , "saved", "urgent", "", "fifo"); // Fix for FolderClosedException
	
    var messageList = mas.mailboxGetMessages(messageListId);
    var noOfMessages = Utility_GetArrayLength(messageList);
    var noOfMessagesQuota = mas.subscriberGetIntegerAttribute(phoneNumber, "MOIPNoOfmailquota")[0];
    System_Log(4,"VVA: noOfMessages :" + noOfMessages );
    System_Log(4,"VVA: noOfMessagesQuota :" + noOfMessagesQuota );
    if ((noOfMessagesQuota != -1) && (noOfMessages > noOfMessagesQuota)) {	// Total number of messages exceeds limit
        var oldMessageListId = mas.mailboxGetMessageSubList(messageListId, "voice,video", "read", "urgent,nonurgent", "", "fifo");
        var messageList = mas.mailboxGetMessages(oldMessageListId);
        var noOfOld = Utility_GetArrayLength(messageList);
        if (noOfMessages - noOfOld < noOfMessagesQuota) {		// Delete old messages down to noOfMessagesQuota
            for (var i = 0; i < noOfMessages - noOfMessagesQuota; i++) {
                System_Log(4,"VVA: New<Max: Deleting i :" + i );
                mas.messageSetStoredProperty(messageList[i], "state", ["deleted"], -1, -1);
            }
        }
        else {	// Delete all old messages
            for (var i = 0; i < noOfOld; i++) {
                System_Log(4,"VVA: New>=Max: Deleting i :" + i );
                mas.messageSetStoredProperty(messageList[i], "state", ["deleted"], -1, -1);
            }
        }

    }
}


/**
 * #DisplayName Mailbox Get Max Recording Length
 * #Description Returns the number of seconds available for message recording that based on the quota limit. 0 is returned if the mailbox is full.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 * #param callerPhoneNumber callerPhoneNumber The phone number of the caller (ANI).
 * #param readOnly readOnly A boolean parameter, true means open mailbox in readOnly mode, false means read-write mode.
 */
function Mailbox_GetMaxRecordingLength(phoneNumber, callerPhoneNumber, readOnly) {
	var mboxId = mas.subscriberGetMailbox(phoneNumber);
	if (readOnly)
	    mas.mailboxSetReadonly(mboxId);
	var usageId = mas.mailboxUsage(mboxId);
    var noOfMessagesUsed = mas.mailboxGetMessageUsage(usageId); 
    var noOfMessagesQuota = mas.subscriberGetIntegerAttribute(phoneNumber, "MOIPNoOfmailquota")[0];

    //Check if amount of messages is over quota
    if (noOfMessagesQuota != -1 && noOfMessagesUsed >= noOfMessagesQuota)
        return 0;

    var maxLengthOfMsg;
    if (Call_IsVideo())
	   maxLengthOfMsg = mas.subscriberGetIntegerAttribute(phoneNumber, "emmsglenmaxvideo")[0];
    else
	   maxLengthOfMsg = mas.subscriberGetIntegerAttribute(phoneNumber, "msglenmaxvoice")[0];
    
   return maxLengthOfMsg;
}

/**
 * #DisplayName Mailbox Get Max Recording Length for VoiceSMS
 * #Description Returns the number of seconds available for message recording in VoiceSMS based on the quota limit.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 * #param readOnly readOnly A boolean parameter, true means open mailbox in readOnly mode, false means read-write mode.
 */
function Mailbox_GetMaxRecordingLengthVoiceSms(phoneNumber, readOnly) {
    var maxLengthOfMsg;
    System_Log(4,"VVA: phonenumber is:" + phoneNumber );

    if (Call_IsVideo())
	   maxLengthOfMsg = mas.subscriberGetIntegerAttribute(phoneNumber, "emmsglenmaxvideo")[0];
    else
	   maxLengthOfMsg = mas.subscriberGetIntegerAttribute(phoneNumber, "msglenmaxvoice")[0];
    
   return maxLengthOfMsg;
}

/**
 * #DisplayName Mailbox Get Message List
 * #Description Returns an id of a list of messageId's filtered and sorted acording to the parameters.
 * #param folderId folderId The folder name.
 * #param types types A string containing the type of messages to get, separated by comma. Valid values are "voice", "video", "fax" and "email".
 * #param states states A string containing the state for the messages to get, separated by comma. Valid values are "new", "read" and "deleted".
 * #param priorities priorities A string containing the priority for the messages to get, separated by comma. Valid values are "urgent" and "nonurgent".
 * #param orders orders A string containing the order for the messages to get, separated by comma. Valid values are "priority", "type" and "state".
 * #param timeOrder timeOrder The time order for the messages to get. Valid values are "fifo" and "lifo".
 */
function Mailbox_GetMessageList(folderId, types, states, priorities, orders, timeOrder) {
	return mas.mailboxGetMessageList(folderId, types, states, priorities, orders, timeOrder);
}

/**
 * #DisplayName Mailbox Get Messages
 * #Description Returns an array of messageId's given a message list id.
 * #param messageListId messageListId An id of an already fetched message list.
 */
function Mailbox_GetMessages(messageListId) {
	return mas.mailboxGetMessages(messageListId);
}

/**
 * #DisplayName Mailbox Get Message List Id for VoiceSMS
 * #Description Returns an id of a list of messageId's filtered and sorted acording to the parameters. (messageListId)
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 * #param states states A string containing the state for the messages to get, separated by comma. Valid values are "new", "read" and "deleted".
 * #param messageListId messageListId The messageListId of the list to get the new message list from. The value shall be null if the message list shall be fetched from the mail server.
 */
function Mailbox_GetMessageListIdVoiceSms(phoneNumber, states, messageListId) {
    var  types = "";
	if (Subscriber_HasService(phoneNumber, "voice") == true)
		 types = types.concat("voice,");
	if (Subscriber_HasService(phoneNumber, "video"))
		 types = types.concat("video,");
	types = types.replace(/,$/,''); //Removes the last character if it is a comma.
    var timeOrder;
    if (states.indexOf('new') != -1)   
        timeOrder = Utility_ConvertToLowerCase(Utility_GetSubString(Subscriber_GetSingleStringAttribute(phoneNumber, 'MOIPMsgPlayOrder'), 0,4))
    else
        timeOrder = Utility_ConvertToLowerCase(Utility_GetSubString(Subscriber_GetSingleStringAttribute(phoneNumber, 'MOIPMsgPlayOrder'), 5,9))

    if (messageListId == null) {	// Fetch message list from the mail server.
        var folderId = Mailbox_GetFolder(Subscriber_GetMailbox(phoneNumber), 'inbox');
	    return mas.mailboxGetMessageList(folderId, types, states, "urgent,nonurgent", "", timeOrder);
	}
	else {	// Fetch the message list from the list with id messageListId
	    return mas.mailboxGetMessageSubList(messageListId, types, states, "urgent,nonurgent", "", timeOrder);
	}
}

/**
 * #DisplayName Mailbox Get Message Sub List
 * #Description Returns an id of a list of messageId's filtered and sorted from a given list id.
 * #param messageListId messageListId An id of an already fetched message list.
 * #param types types A string containing the type of messages to get, separated by comma. Valid values are "voice", "video", "fax" and "email".
 * #param states states A string containing the state for the messages to get, separated by comma. Valid values are "new", "read" and "deleted".
 * #param priorities priorities A string containing the priority for the messages to get, separated by comma. Valid values are "urgent" and "nonurgent".
 * #param orders orders A string containing the order for the messages to get, separated by comma. Valid values are "priority", "type" and "state".
 * #param timeOrder timeOrder The time order for the messages to get. Valid values are "fifo" and "lifo".
 */
function Mailbox_GetMessageSubList(messageListId, types, states, priorities, orders, timeOrder) {
	return mas.mailboxGetMessageSubList(messageListId, types, states, priorities, orders, timeOrder);
}


/**
 * #DisplayName Mailbox Get Number Of Messages
 * #Description Returns a list of messageId's filtered and sorted acording to the parameters.
 * #param messageListId messageListId The Id of the message.
 * #param types types A string containing the type of messages to get, separated by comma. Valid values are "voice", "video", "fax" and "email".
 * #param states states A string containing the state for the messages to get, separated by comma. Valid values are "new", "read" and "deleted".
 * #param priorities priorities A string containing the priority for the messages to get, separated by comma. Valid values are "urgent" and "nonurgent".
 */
function Mailbox_GetNumberOfMessages(messageListId, types, states, priorities) {
	return mas.mailboxGetNumberOfMessages(messageListId, types, states, priorities);
}
/**
 * #DisplayName Mailbox Is Filled Over Warning Level
 * #Description Returns true if the quota is over warning level.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 */
function Mailbox_IsFilledOverWarningLevel(phoneNumber) {
	var mboxId = mas.subscriberGetMailbox(phoneNumber);
	mas.mailboxSetReadwrite(mboxId);
	var usageId = mas.mailboxUsage(mboxId);
    var noOfMessagesUsed = mas.mailboxGetMessageUsage(usageId);
    var noOfMessagesQuota = mas.subscriberGetIntegerAttribute(phoneNumber, "MOIPNoOfmailquota")[0];
    var warningLevel = mas.subscriberGetStringAttribute(phoneNumber, "MOIPDiskSpaceRemainingWarningLevel")[0];

    //Check if amount of messages is over warning level
    if (noOfMessagesQuota != -1 && (noOfMessagesUsed >= (1 - warningLevel) * noOfMessagesQuota))
        return true;

   return false;
}
/**
 * #DisplayName Mailbox Is Full
 * #Description Returns true if the mailbox is filled over the quota limit.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 */
function Mailbox_IsFull(phoneNumber) {
	var mboxId = mas.subscriberGetMailbox(phoneNumber);
	mas.mailboxSetReadwrite(mboxId);
	var usageId = mas.mailboxUsage(mboxId);
    var noOfMessagesUsed = mas.mailboxGetMessageUsage(usageId);
    var noOfMessagesQuota = mas.subscriberGetIntegerAttribute(phoneNumber, "MOIPNoOfmailquota")[0];
    System_Log(4,"VVA: noOfMessagesUsed=" + noOfMessagesUsed.toString() );
    
    //Check if amount of messages is over quota
    if (noOfMessagesQuota != -1 && noOfMessagesUsed >= noOfMessagesQuota)
        return true;

    return false;
}

function Mailbox_Clear()
{
}

/*
 * Local function
 * Returns the last element in MenuArray.
 * If '#' is included in the last element, only the string from the '#' and to the end is returned.
 */
function Menu_GetCurrent() {
	var pos = MenuArray[MenuArray.length-1].search("#");
	if ( pos != -1) {
		return MenuArray[MenuArray.length-1].substring(pos, MenuArray[MenuArray.length-1].length);
	}
    return MenuArray[MenuArray.length-1];
}

/**
 * #DisplayName Menu Get Previous
 * #Description Removes the last element in MenuArray and returns the element that then is the last element.
 * Returns an empty string if there is zero or one element in MenuArray.
 * Returns only the string from # and onwards if the document of current and previous are equal.
 */
function Menu_GetPrevious() {
    var len = MenuArray.length;
    var doc1, doc2;
    if (len == 0)
        return "";
    else if (len == 1) {
        MenuArray.splice(len-1, 1);
        return "";
    }
    else {
        doc1 = MenuArray[len-1].substring(0, MenuArray[len-1].indexOf("#"));
        doc2 = MenuArray[len-2].substring(0, MenuArray[len-2].indexOf("#"));
        MenuArray.splice(len-1, 1);
        if (doc1 == doc2)
            return MenuArray[len-2].substring(MenuArray[len-2].indexOf("#"));
        else
            return MenuArray[len-2];
    }
}

/**
 * #DisplayName Menu Is Allowed
 * #Description Returns true if the menu is allowed, otherwise false.
 * #param phoneNumber phoneNumber The phonenumber.
 * #param menu menu The menu to check.
 */
function Menu_IsAllowed(phoneNumber, menu){
 var blockedMenusTmp = mas.subscriberGetStringAttribute(phoneNumber, "MOIPTuiBlockedMenu");
 var blockedMenus = new Array(blockedMenusTmp.length);
 menu = '/' + menu + '/';
 for (var i = 0; i < blockedMenusTmp.length; i++) {
     blockedMenus[i] = '/' + blockedMenusTmp[i] + '/';
     if (blockedMenus[i].toLowerCase().search(menu.toLowerCase()) != -1)
      return false;
 }
 return true;
}

/*
 * Local function
 * Stores the uri in MenuArray.
 * If the uri already exist in MenuArray, the elements after that position are removed.
 */
function Menu_SetUri(uri) {
    var len = MenuArray.length;
    for (var i=0; i<len; i++) {
        if (uri == MenuArray[i]) {
            MenuArray.splice(i, len-i);
            break;
        }
    }
    MenuArray[MenuArray.length] = uri;
}

/**
 * #DisplayName Message Check Recipients
 * #Description Returns an array which contains the emailaddresses of the subscribers that can receive the specified mediaType.
 * #param emailArray emailArray The array containing the emailaddresses.
 * #param mediaType mediaType The media type of the message, eg voice/video.
 */
function Message_CheckRecepients(emailArray, mediaType) {
    var phoneNumber;
    var address;
    var newEmailArray = [];
    
    System_Log(4,"VVA: mediaType:" + mediaType);
    //remove recipients that cannot recive the specified type.
    for (var i=0; i<emailArray.length; i++) {
       System_Log(4,"VVA: emailArray[i]:" + emailArray[i]);
       address = Utility_GetEmailAddressFromString(emailArray[i])
       System_Log(4,"VVA: address:" + address );
       //phoneNumber = mas.systemGetSubscribers("mail", address);
       System_Log(4,"VVA: phoneNumber:" + address );
       if (Subscriber_HasService(address, mediaType)) {
          newEmailArray.push(address);
       }
    }
    return newEmailArray;
}

/**
 * #DisplayName Message Content Length
 * #Description Returns the length of the specified MessageContent. Length can be retrieved in milliseconds or number of pages.
 * #param messageContentId messageContentId The identity of the message content.
 * #param type type can be one of "milliseconds" or "pages".
 */
function MessageContentLength(messageContentId, type) {
	return mas.messageContentLength(messageContentId, type);
}

/**
 * #DisplayName Message Create New
 * #Description Returns the identity of the message.
 * #param sender sender The phone number of the sender.
 * #param toList toList An array of emailaddresses of the subscriber(s) that receives the message. In deposit it contains the phone number of the receiver.
 * #param ccList ccList A list of email-addresses of recipients used as secondary recipients. Only used in retrieval.
 * #param mediaObject mediaObject The message that was recoreded.
 * #param mediaObjectDuration mediaObjectDuration The length of the message that was recoreded in milliseconds.
 * #param mediaType mediaType The type of the message, ie voice/video
 * #param spokenName spokenName The spoken name of the caller that will be attached to the message. Empty if no spoken name should be attached.
 * #param typeOfSend typeOfSend Can be deposit/send/replytosender/replytoall/forward
 * #param confidential confidential True if the message shall be confidential.
 * #param urgent urgent True if the message shall be urgent.
 * #param callbacknr callbacknr The number to make callback to if other than "sender".
 * #param orginalMessageId orginalMessageId Only used if typeOfSend is 'forward'.
 */
function Message_CreateNew(sender, toList, ccList, mediaObject, mediaObjectDuration, mediaType, spokenName, typeOfSend, confidential, urgent, callbacknr, orginalMessageId) {
    var from;
    var subject;
    var subjectQualifier;
    var senderEmailAddress = "";
    var senderName;
    var includeSpokenName = false;
    var preferredLanguage;
    var descriptionMessage;
    var descriptionSpokenName;
    var filenameMessage;
    var filenameSpokenName;
    var subjectQualifierForward;
    var subjectQualifierReply;
    var subjectQualifierConfidential;
    var fromSubject;
    var messageId;
    var replyTo;
    var senderVisibility;
    
    System_Log(4,"VVA: sender: " + sender + ", callbacknr: " + callbacknr);
    System_Log(4,"VVA: Message_CreateNew:  typeOfSend: " + typeOfSend);

    if (typeOfSend == "forward")
       messageId = mas.messageForward(orginalMessageId);
    else
       messageId = mas.messageCreateNew();
        
    // get preferred language or default if toList is empty
    if (typeOfSend == "deposit") 
	   preferredLanguage = mas.subscriberGetStringAttribute(toList[0], "preferredlanguage")[0];
    else if (toList.length > 0)
	   preferredLanguage = mas.subscriberGetStringAttribute(toList[0], "preferredlanguage")[0];
    else
	   preferredLanguage = System_GetConfig("vva.media", "defaultlanguage");

    //fetch language dependant information to subject
    if (typeOfSend == "forward") {
       subjectQualifierForward = "true";
	   subjectQualifierReply = "false";
    }
    else if (typeOfSend.search("reply") != -1) {
	   subjectQualifierForward = "false";
       subjectQualifierReply = "true";
    }
    else {
	   subjectQualifierForward = "false";
	   subjectQualifierReply = "false";
    }

    if (confidential == true)
       subjectQualifierConfidential = "true";
    else
	   subjectQualifierConfidential = "false";

    //caller is a subscriber
    System_SetPartitionRestriction (true);
    if (mas.subscriberExist(sender)) {
	
	senderName = mas.subscriberGetStringAttribute(sender, "MOIPCn")[0];
	if (senderName == "") {
		System_Log(4,"VVA: no senderName found");
		senderName = "unknown";
	}
	
	subject = util.convertMediaObjectsToString(mas.systemGetMediaContent("prompt", "subject", new Array(
		      util.getMediaQualifier("String", subjectQualifierForward),
		      util.getMediaQualifier("String", subjectQualifierReply),
		      util.getMediaQualifier("String", subjectQualifierConfidential),
		      util.getMediaQualifier("String", mediaType),
		      util.getMediaQualifier("String", "person"))));
	subject = subject + senderName;

	if (Call_GetAniPi() == 0) {
		// ANI is not restricted
		// Call_GetAniPi() == 0 means call_privacy=off
		senderVisibility = 1; // MFS.senderVisibility=1 means 'SHOW'
	} else {
		senderVisibility = 0; // MFS.senderVisibility=0 means 'HIDE'
	}
	from = senderName + " <tel:" + sender + ">";
	System_Log(4,"VVA: Message_CreateNew: Subscriber: " + senderName + ", from: " + from + ", senderVisibility: " + senderVisibility);
	
    } else {
	// caller is NOT a subscriber
	if (Call_GetAni() == "") {
		// ANI does not exist
		from = util.convertMediaObjectsToString(mas.systemGetMediaContent("prompt", "from", new Array(
			util.getMediaQualifier("String", "unknown"))));
		fromSubject = util.convertMediaObjectsToString(mas.systemGetMediaContent("prompt", "fromSubject", new Array(
			util.getMediaQualifier("String", "unknown"))));
		subject = util.convertMediaObjectsToString(mas.systemGetMediaContent("prompt", "subject", new Array(
			util.getMediaQualifier("String", subjectQualifierForward),
			util.getMediaQualifier("String", subjectQualifierReply),
			util.getMediaQualifier("String", subjectQualifierConfidential),
			util.getMediaQualifier("String", mediaType),
			util.getMediaQualifier("String", "person"))));
		subject = subject + fromSubject;
		senderVisibility = 0; // MFS.senderVisibility=0 means 'HIDE'
	} else if (Call_GetAniPi() == 1) {
		// ANI is restricted
		from = util.convertMediaObjectsToString(mas.systemGetMediaContent("prompt", "from", new Array(
			util.getMediaQualifier("String", "restricted"))));
		fromSubject = util.convertMediaObjectsToString(mas.systemGetMediaContent("prompt", "fromSubject", new Array(
			util.getMediaQualifier("String", "restricted"))));
		subject = util.convertMediaObjectsToString(mas.systemGetMediaContent("prompt", "subject", new Array(
			util.getMediaQualifier("String", subjectQualifierForward),
			util.getMediaQualifier("String", subjectQualifierReply),
			util.getMediaQualifier("String", subjectQualifierConfidential),
			util.getMediaQualifier("String", mediaType),
			util.getMediaQualifier("String", "person"))));
		subject = subject + fromSubject;
		senderVisibility = 0; // MFS.senderVisibility=0 means 'HIDE'
	} else {
		//  ANI exists and is not restricted
		from = "tel:"+sender;
		subject = util.convertMediaObjectsToString(mas.systemGetMediaContent("prompt", "subject", new Array(
			util.getMediaQualifier("String", subjectQualifierForward),
			util.getMediaQualifier("String", subjectQualifierReply),
			util.getMediaQualifier("String", subjectQualifierConfidential),
			util.getMediaQualifier("String", mediaType),
			util.getMediaQualifier("String", "person"))));
		subject = subject + sender;
		senderVisibility = 1; // MFS.senderVisibility=1 means 'SHOW'
	}
	//System_Log(4,"VVA: Message_CreateNew: Non-subscriber: " + senderName + ", from: " + from + ", senderVisibility: " + senderVisibility);
    }
    
    System_SetPartitionRestriction (false);

    //Attach spoken name to message if it is defined
    if (spokenName != "") {
        descriptionSpokenName = util.convertMediaObjectsToString(mas.systemGetMediaContent("prompt", "descriptionSpokenname"));

        filenameSpokenName = util.convertMediaObjectsToString(mas.systemGetMediaContent("prompt", "filename", new Array(
			    util.getMediaQualifier("String", "spokenname"))));

        mas.messageSetSpokenNameOfSender(messageId, spokenName, descriptionSpokenName, filenameSpokenName, preferredLanguage);
    }

    // Fetch language specific content description and filenames
    descriptionMessage = util.convertMediaObjectsToString(mas.systemGetMediaContent("prompt", "descriptionMessage", new Array(
			 util.getMediaQualifier("String", mediaType),
			 util.getMediaQualifier(util.getMediaObject(Time_GetSeconds(mediaObjectDuration))))));

    filenameMessage = util.convertMediaObjectsToString(mas.systemGetMediaContent("prompt", "filename", new Array(
		      util.getMediaQualifier("String", mediaType))));
      
    //Attach message to the message id
    if (mediaObject != null)  // mediaObject is null if forward without comment.
       mas.messageAddMediaObject(messageId, mediaObject, descriptionMessage, filenameMessage, preferredLanguage);

    // Set properties of the message
    Message_SetStorableProperty(messageId, "sender", [from]);
    if (typeOfSend == "deposit")
        Message_SetStorableProperty(messageId, "recipientsSubscriberId", toList);    
    else
        Message_SetStorableProperty(messageId, "recipients", toList);
    Message_SetStorableProperty(messageId, "subject", [subject]); 
    Message_SetStorableProperty(messageId, "type", [mediaType]);
    Message_SetStorableProperty(messageId, "language", [preferredLanguage]);    
    if (ccList.length != 0)
        Message_SetStorableProperty(messageId, "secondaryrecipients", ccList);
    Message_SetStorableProperty(messageId, "confidential", [confidential]);
    Message_SetStorableProperty(messageId, "urgent", [urgent]);
    // Add Reply To if applicable
    System_SetPartitionRestriction (true);	
    
    if (callbacknr != '') {
	System_Log(4, "VVA: callbacknr is not null: " + callbacknr);  
	replyTo = callbacknr;
    } else {
 	System_Log(4, "VVA: callbacknr is null");
	replyTo = sender;
    }
    System_Log(4, "VVA: replyTo: " + replyTo);
    Message_SetStorableProperty(messageId, "replytoaddr", [replyTo]);
    Message_SetStorableProperty(messageId, "sendervisibility", [senderVisibility]);

    System_SetPartitionRestriction (false);	

    return messageId;
}

/**
 * #DisplayName Message Days Since Delivery
 * #Description Returns the number of days between the delivery date and current time.
 * #param mailboxId mailboxId The maibox id.
 * #param messageId messageId The message id.
 */
function Message_DaysSinceDelivery(mailboxId, messageId) {
	var currentTime = util.getCurrentTime(mas.subscriberGetStringAttribute(mailboxId, "CNTimeZone")[0]);
	System_Log(4,"VVA: currentTime=" + currentTime);
	var receivedDate = mas.messageGetStoredProperty(messageId, "receiveddate")[0];
	System_Log(4,"VVA: receivedDate=" + receivedDate);
	var deliveryDateInSeconds =  parseInt(util.formatTime(receivedDate, null), 10)/1000;
	var secondsAtMidnigth = parseInt(util.formatTime(currentTime.substr(0,10) + " 23:59:59", null), 10)/1000;	
	return Math.floor((secondsAtMidnigth-deliveryDateInSeconds)/3600/24);
}

/**
 * #DisplayName Message Get ContentLength
 * #Description Returns the length of the media object of the given messageContentId. 
 * #param messageId messageId The Id of the message.
 * #param type type The type: milliseconds or pages.
 */
function Message_GetContentLength(messageId, type) {
	return mas.messageContentLength(mas.messageGetContent(messageId)[0], type);
}
/**
 * #DisplayName Message Get Media Object
 * #Description Returns the media object of the given messageContentId.
 * #param messageContentId messageContentId The Id of the message content.
 */
function Message_GetMediaObject(messageContentId) {
	return mas.messageGetMediaObject(messageContentId);
}

/**
 * #DisplayName Message Get Message State Event String
 * #Description Returns a string that can be used if sending an event that relates to change message state.
 * #param subscriber subscriber The logged in subscriber.
 * #param eventType eventType Valid values: 'retrieve', 'delete', 'store'.
 * #param messageId messageId The Id of the current message.
 * #param ani ani The ANI of the call.
 */
function Message_GetMessageStateEventString(subscriber, eventType, messageId, ani) {
	var contentArray = [];
	var size = 0;
	var resultString = "";
	var sizeOK = true;
	
	// Add subscriber
	resultString.concat("username=", subscriber);
	
	// Add eventType
	if (eventType == "retrieve")
	resultString.concat(",operation=1")
	if (eventType == "delete")
	resultString.concat(",operation=6")
	if (eventType == "store")
	resultString.concat(",operation=13")
	
	//Get Type
	var type = mas.messageGetStoredProperty(messageId, 'type')[0];
	
	// Add Message Type, Encoding and size
	if (type == "voice") {
	
		// Add Message Type
		resultString.concat(",messagetype=10");
		
		// Add Message Encoding
		resultString.concat(",messageencoding=10");
		
		// Add Size
		contentArray = mas.messageGetContent(messageId);
		for (var i = 0; i < contentArray.length; i++) {
			if (mas.messageGetMediaProperties(contentArray[i]).search("audio") != -1) {
			    if (mas.messageContentLength(contentArray[i], "milliseconds") != -1) {
				    size = size + mas.messageContentLength(contentArray[i], "milliseconds");
				} 
				else {
				    sizeOK = false;
				}
			}
		}
		if (sizeOK) {
		    size = Math.round(size/1000); // convert from milliseconds to seconds;
		    resultString.concat(",messagesize=", size.toString());
		}
	}
	if (type == "video") {
	
		// Add Message Type
		resultString.concat(",messagetype=20");
		
		// Add Message Encoding
		resultString.concat(",messageencoding=40");
		
		// Add Size
		contentArray = mas.messageGetContent(messageId);
		for (var i = 0; i < contentArray.length; i++) {
			if (mas.messageGetMediaProperties(contentArray[i]).search("video") != -1) {
			    if (mas.messageContentLength(contentArray[i], "milliseconds") != -1) {
				    size = size + mas.messageContentLength(contentArray[i], "milliseconds");
				} 
				else {
				    sizeOK = false;
				}
			}
		}
		if (sizeOK) {
		    size = Math.round(size/1000); // convert from milliseconds to seconds;
		    resultString.concat(",messagesize=", size.toString());
		}
	}
	
	return resultString;
}

function Message_GetNumberOfAttachments()
{
}

/**
 * #DisplayName Message Get Number Of Fax Pages
 * #Description Returns the number of fax pages of the given messageContentId array.
 * #param messageId messageId The Id of the message.
 */
function Message_GetNumberOfFaxPages(messageId) {
	var contentArray = mas.messageGetContent(messageId);	
	var pages = 0;
	var sizeOK = true;
	
	for (var i = 0; i < contentArray.length; i++) {
		if (mas.messageGetMediaProperties(contentArray[i]).search("image/tiff") != -1) {
			if (mas.messageContentLength(contentArray[i], "pages") != -1) {
			    pages = pages + mas.messageContentLength(contentArray[i], "pages");
			} 
			else {
			    sizeOK = false;
			}
		}
	}
	if (sizeOK) {
	    return pages;
	}
	else {
	    return -1;
	}
}

/**
 * #DisplayName Message Get Playable Parts
 * #Description Returns an array with the playable body parts.
 * #param mailboxId mailboxId The Id of the mailbox.
 * #param messageId messageId The Id of the message.
 */
function Message_GetPlayableParts(mailboxId, messageId) {
	var orginalContentArray = mas.messageGetContent(messageId);	
	var playableContentArray = [];

	for (var i = 0; i < orginalContentArray.length; i++) {
		if (mas.messageGetMediaProperties(orginalContentArray[i]).search("audio") != -1) {
			playableContentArray.push(orginalContentArray[i]);
			continue;
		}
		if (mas.messageGetMediaProperties(orginalContentArray[i]).search("video") != -1) {
			playableContentArray.push(orginalContentArray[i]);
			continue;
		}
	 	if ((mas.messageGetMediaProperties(orginalContentArray[i]).search("text") != -1) && ( Subscriber_HasService(mailboxId, "tts"  ))) {
			playableContentArray.push(orginalContentArray[i]);
			continue;
		}
	}
	return playableContentArray;
}

/**
 * #DisplayName Message Get Storable Property
 * #Description Returns the property of the given property name.
 * #param messageId messageId The Id of the message.
 * #param property property The property name. Valid values: confidential, deliverydate, language, recipients, replytoaddr, secondaryrecipients, sender, subject, type and urgent.
 */
function Message_GetStorableProperty(messageId, property) {
    return mas.messageGetStorableProperty(messageId, property);
}

/**
 * #DisplayName Message Get Stored Property
 * #Description Returns the property of the given property name.
 * #param messageId messageId The Id of the message.
 * #param property property The property name. Valid values: confidential, deliveryreport, deliverystatus, forwarded, language, receiveddate, recipients, replytoaddr, secondaryrecipients, sender, state, subject, type and urgent.
 */
function Message_GetStoredProperty(messageId, property) {
    return mas.messageGetStoredProperty(messageId, property);
}


/**
 * #DisplayName Message Get Respond Addresses
 * #Description Returns an array of the respond addresses.
 * #param mailboxId mailboxId The identity of the mailbox.
 * #param messageId messageId The identity of the message.
 * #param respondAddresses respondAddresses The type of respond addresses. Valid values: "allTo", "allCc" or "senderTo".
 */
function Message_GetRespondAddresses(mailboxId, messageId, respondAddresses) {
	var ownAddress = "";
	var emailAddress = "";
	
	//If respondAdresses == "allCc", generate the respond to all cc addresses.
	if (respondAddresses == "allCc") {
		//ownAddress  = mas.subscriberGetStringAttribute(mailboxId, "mail")[0];	
		ownAddress  = mailboxId;	

		var ccAddressesModifyed = [];

		try {
			var ccAddresses = mas.messageGetStoredProperty(messageId, "secondaryrecipients");	
			//Remove own address from cc array.
			for (var i=0; i<ccAddresses.length; i++) {
		    		emailAddress = Utility_GetEmailAddressFromString(ccAddresses[i]);
				if (emailAddress != ownAddress)
				ccAddressesModifyed.push(emailAddress);
			}
		}
		catch(err) {}
			
		return ccAddressesModifyed;
	}
	
	//If ReplyToEmailAddr exist, use that address. Otherwise, use Sender
	var replyAddress = mas.messageGetStoredProperty(messageId, "replytoaddr");
	if (replyAddress.length == 0)
		replyAddress = mas.messageGetStoredProperty(messageId, "sender");
	replyAddress[0] = Utility_GetEmailAddressFromString(replyAddress[0]);

	//If respondAdresses == "senderTo", return the replyAddress.
	if (respondAddresses == "senderTo") {
		return replyAddress;
	}

	//If respondType == "allTo", generate the respond to all To addresses.
	//ownAddress  = mas.subscriberGetStringAttribute(mailboxId, "mail")[0];
	ownAddress = mailboxId;
	var toAddresses = mas.messageGetStoredProperty(messageId, "recipients");
	var toAddressesModifyed = [];
	
	//Remove own address from to array.
	for (var i=0; i<toAddresses.length; i++) {
	    emailAddress = Utility_GetEmailAddressFromString(toAddresses[i]);
		if (emailAddress != ownAddress)
			toAddressesModifyed.push(emailAddress);
	}				
	return replyAddress.concat(toAddressesModifyed);
}

/**
 * #DisplayName Message Get Senders PhoneNumber
 * #Description Fetch the phone number from the given string. Returns an empty string if no number was found.
 * #param senderString senderString The string to fetch the phone number from.
 */
function Message_GetSendersPhoneNumber(senderString) {

	System_Log(4, "VVA: From field:" + senderString);

	/* Test if deposit by a Subscriber*/
	
	// Case 1: senderName + <tel:+140000>
	var number = (senderString.substring(senderString.indexOf("<") +1, senderString.indexOf(">")));
	System_Log(4, "VVA: senderString.substring: " + number);
	var number2 = (number.substring(number.indexOf("tel:")+4, number.length));
	if (parseInt(number2, 10)) {
		System_Log(4, "VVA: Message_GetSendersPhoneNumber_Case1_parseInt: " + number2);
		return number2;
	}

	// Case 2: tel:+40000 (no "<>")
	if (senderString.search("tel:") != -1) {
		number = (senderString.substring(senderString.indexOf("tel:")+4, senderString.length));    
		if (parseInt(number, 10)) {
			System_Log(4, "VVA: Message_GetSendersPhoneNumber_Case2_parseInt: " + number);
			return number;
		}
	}

	// Case3: 40000 (non-normalized number)
 	if (parseInt(senderString, 10)) {
		System_Log(4, "VVA: Message_GetSendersPhoneNumber_Case3_parseInt: " + senderString);
		return senderString;
	}

	/* Test if Deposit by Non-Subscriber */
	
	// Case 4: non-subscriber case
	// TODO: To be analysed

	/* Check if fax number exists */
	var re1 = new RegExp(".*FAX=.*@.*");
	if (senderString.search(re1) != -1) {
		number = senderString.substring( senderString.indexOf("=") +1, senderString.indexOf("@"));  
		number = parseInt(number,10);
		if (isNaN(number)){ 
			return "";
		} else {
			return number;
		}
	}

	/* No number available */
	return "";
}

/**
 * #DisplayName Message Get Spoken Name Of Sender
 * #Description Returns the spoken name of the sender of the specified message.
 * #param messageId messageId The identity of the message.
 */
function Message_GetSpokenNameOfSender(messageId) {
	return mas.messageGetSpokenNameOfSender(messageId);
}

function Message_HasDistributionListRecipients()
{
}

function Message_IsFutureDeliverySet()
{
}

/**
 * #DisplayName Message Is Part Text
 * #Description Determine if the current message part is text.
 * #param messageContentId messageContentId The Id of the message content. 
 */
function Message_IsPartText(messageContentId) {
	if (mas.messageGetMediaProperties(messageContentId).search("text") != -1) {
		return true
	}
	return false
}

/**
 * #DisplayName Message Is Printable
 * #Description Determine if any of the content part of a message is printeble.
 * #param messageId messageId The Id of the message. 
 */
function Message_IsPrintable(messageId)
{
	var contentArray = mas.messageGetContent(messageId);
	for (var i=0; i<contentArray.length; i++) {
		if ((mas.messageGetMediaProperties(contentArray[i]).search("image/tiff") != -1) || (mas.messageGetMediaProperties(contentArray[i]).search("text") != -1)) {
			return true;
		}
	}
	return false;
}

/**
 * #DisplayName Message Is Respond To All Allowed
 * #Description Returns true if allowed and false if not. Respond to all is not allowed if: Subscriber does not have SendMessage service or only one recipients of the message.
 * #param mailboxId mailboxId The identity of the mailbox.
 * #param messageId messageId The identity of the message.
 */
function Message_IsRespondToAllAllowed(mailboxId, messageId) {

	// Check if SendMessage service is disabled
	if (Subscriber_HasService(mailboxId, 'sendmessage') == false)
		return false;
	
	var numOfSecondaryRecipients = 0;
	
	try {	
		var secondaryRecipientsArray = mas.messageGetStoredProperty(messageId, "secondaryrecipients");
	
		if(secondaryRecipientsArray == null) {
			numOfSecondaryRecipients = 0;
		}
		else {
			numOfSecondaryRecipients = secondaryRecipientsArray.length
		}
	} catch(err) {
		numOfSecondaryRecipients = 0;
	}
	
		
	// Check if only one recipients
	if ((mas.messageGetStoredProperty(messageId, "recipients").length + numOfSecondaryRecipients) == 1)
		return false;

	return true;
}

/**
 * #DisplayName Message Is Respond To Sender Allowed
 * #Description Returns true if allowed and false if not. Respond to sender is not allowed if: Subscriber does not have SendMessage service or Undelivered message or Fax message that is not forwarded.
 * #param mailboxId mailboxId The identity of the mailbox.
 * #param messageId messageId The identity of the message.
 */
function Message_IsRespondToSenderAllowed(mailboxId, messageId) {

	// Check if SendMessage service is disabled
	if (Subscriber_HasService(mailboxId, "sendmessage") == false)
		return false;
		
	// Check if Undelivered
	if (mas.messageGetStoredProperty(messageId, "deliveryreport")[0] == "true")
		return false;

	// Check if fax that is not forwarded
	if ((mas.messageGetStoredProperty(messageId, "type")[0] == "fax") && (mas.messageGetStoredProperty(messageId, "forwarded")[0] == "false"))
		return false;

	return true;
}

/**
 * #DisplayName Message Play Email Attachments
 * #Description Returns the MediaObjects that plays the Email attachment.
 * #param messageId messageId The ID of the message.
 */
function Message_PlayEmailAttachment(messageId) {
	var contentArray = mas.messageGetContent(messageId);
	var text = 0;
	var audio = 0;
	var video = 0;
	var word = 0;
	var excel = 0;
	var ppt = 0;
	var fax = 0;
	var other = 0;
	var total = 0;
	
	//Get number of different attachments
	for (var i = 0; i < contentArray.length; i++) {
		
		if (mas.messageGetMediaProperties(contentArray[i]).search("text") != -1) {
			text = text + 1;
			total = total + 1;
		}		 
		else if ( mas.messageGetMediaProperties(contentArray[i]).search("audio") != -1) {
			audio = audio + 1;
			total = total + 1;
		}
		else if ( mas.messageGetMediaProperties(contentArray[i]).search("video") != -1) {
			video = video + 1;
			total = total + 1;
		}
		else if ( mas.messageGetMediaProperties(contentArray[i]).search("msword") != -1) {
			word = word + 1;
			total = total + 1;
		}
		else if ( mas.messageGetMediaProperties(contentArray[i]).search("excel") != -1) {
			excel = excel + 1;
			total = total + 1;
		}
		else if ( mas.messageGetMediaProperties(contentArray[i]).search("powerpoint") != -1) {
			ppt = ppt + 1;
			total = total + 1;
		}
		else if ( mas.messageGetMediaProperties(contentArray[i]).search("image/tiff") != -1) {
			fax = fax + 1;
			total = total + 1;
		}
		else {
			other = other + 1;
			total = total + 1;
		}
	}
	if (text != 0) {
		text = text - 1;
		total = total - 1;
	}

	// Build the MediaObject array to return
	var objectArray = [];
	objectArray.push(mas.systemGetMediaContent("prompt", "tts_contains"));          //"This message contains..."
	if (total == 1) {
		objectArray.push(mas.systemGetMediaContent("prompt", "tts_oneattachment")); //"one attachment."
	}
	else {
		objectArray.push(util.getMediaObject(total.toString()));                     //<number of attachments>
		objectArray.push(mas.systemGetMediaContent("prompt", "tts_attachments"));   //"attachments."
	}
	if (text > 0) {
		objectArray.push(util.getMediaObject(text.toString()));                      //<number of text attachments>
		objectArray.push(mas.systemGetMediaContent("prompt", "tts_text"));          //"plain text,"
	}
	if (audio > 0) {
		objectArray.push(util.getMediaObject(audio.toString()));                     //<number of audio attachments>
		objectArray.push(mas.systemGetMediaContent("prompt", "tts_audio"));         //"audio message,"
	}
	if (video > 0) {
		objectArray.push(util.getMediaObject(video.toString()));                      //<number of video attachments>
		objectArray.push(mas.systemGetMediaContent("prompt", "tts_video"));          //"video message,"
	}
	if (word > 0) {
		objectArray.push(util.getMediaObject(word.toString()));                      //<number of word attachments>
		objectArray.push(mas.systemGetMediaContent("prompt", "tts_word"));          //"Microsoft Word,"
	}
	if (excel > 0) {
		objectArray.push(util.getMediaObject(excel.toString()));                      //<number of exel attachments>
		objectArray.push(mas.systemGetMediaContent("prompt", "tts_excel"));          //"Microsoft Excel,"
	}
	if (ppt > 0) {
		objectArray.push(util.getMediaObject(ppt.toString()));                       //<number of exel attachments>
		objectArray.push(mas.systemGetMediaContent("prompt", "tts_ppt"));           //"Microsoft PowerPoint,"
	}
	if (fax > 0) {
		objectArray.push(util.getMediaObject(fax.toString()));                       //<number of exel attachments>
		objectArray.push(mas.systemGetMediaContent("prompt", "tts_fax"));           //"Fax image,"
	}
	if (other > 0) {
		objectArray.push(util.getMediaObject(other.toString()));                     //<number of exel attachments>
		objectArray.push(mas.systemGetMediaContent("prompt", "tts_other"));         //"Other."
	}
	return objectArray;	
}

/**
 * #DisplayName Message Play Email Header
 * #Description Returns the MediaObjects that plays the Email header.
 * #param messageId messageId The ID of the message.
 */
function Message_PlayEmailHeader(messageId) {
	var objectArray = [];
	objectArray.push(mas.systemGetMediaContent("prompt", "tts_regarding"));           //"Regarding..."
	objectArray.push(util.getMediaObject(Message_GetStoredProperty(messageId, "subject")[0])); //<Subject>
	return objectArray;	
}

/**
 * #DisplayName Message Play Email Length
 * #Description Returns the MediaObjects that plays the Email length.
 * #param messageId messageId The ID of the message.
 */
function Message_PlayEmailLength(messageId) {
	var objectArray = [];
	objectArray.push(mas.systemGetMediaContent("prompt", "tts_approxlen"));       //"The approximate length of the text message is..."
	objectArray.push(util.getMediaObject(Event_GetMessageSize(messageId)));       //<seconds>
	return objectArray;	
}

/**
 * #DisplayName Message Print
 * #Description Prints the specified message to the specified destination.
 * #param messageId messageId The ID of the message.
 * #param faxNumber faxNumber The fax number to print the message to.  
 */
function Message_Print(messageId, faxNumber, sender) {
	var from = "\""+ Subscriber_GetSingleStringAttribute(sender, "cn") + " (" + sender + ")\" <" + Subscriber_GetSingleStringAttribute(sender, "mail") + ">";
	mas.messagePrint(messageId, faxNumber, from);
}

/**
 * #DisplayName Message Send To Mailbox
 * #Description Returns true if the message was sent successfully.
 * #param messageId messageId The ID of the message.
 */
function Message_SendToMailbox(messageId) {
    mas.messageStore(messageId);
}
function Message_SetCallbackNumber()
{
}

function Message_SetDeliveryTime()
{
}

/**
 * #DisplayName Message Set TTS Language
 * #Description Sets the correct language of the MediaObject depending on installed TTS languages and the Subscribers preferred language.
 * #param subscriber subscriber The subscriber.
 * #param mediaObject mediaObject The MediaObject to set TTS language on.
 */
function Message_SetTTSLanguage(subscriber, mediaObject) {
	var installedLang = util.getSupportedTTSLanguages();
	// If no TTS languages are installed, return false.
	if (installedLang.length == 0)
		return false;
		
	var objectLang = util.getMediaObjectProperty(mediaObject, "language");	
	// Check if the MediaObjects language is installed.
	for (var i=0;i<installedLang.length;i++) {
		if (objectLang == installedLang[i])
			return true;
	}
	// Check if the Subscribers preferred language is installed as TTS language.
	var prefLang = Subscriber_GetSingleIntegerAttribute(subscriber, "preferredlanguage");
	for (var i=0;i<installedLang.length;i++) {
		if (prefLang == installedLang[i]) {
			util.setMediaObjectProperty(mediaObject, ["language"], [prefLang]);
			return true;
		}
	}
	// Use the default TTS language.
	objectLang = System_GetConfig("vva.media", "defaultttslanguage");
	util.setMediaObjectProperty(mediaObject, ["language"], [objectLang]);
	return true;
}

function Message_Clear()
{
}
/**
 * #DisplayName Message Set Storable Property
 * #Description Sets the specified property of the message. 
 * #param messageId messageId The ID of the message.
 * #param property property The property name. Valid values: confidential, deliverydate, language, recipients, replytoaddr, secondaryrecipients, sender, subject, type and urgent.
 * #param value value An array with the values of the property.
 */
function Message_SetStorableProperty(messageId, property, value) {
	    mas.messageSetStorableProperty(messageId, property, value);
    return;
}

/**
 * #DisplayName Message Set Stored Property
 * #Description Sets the specified property of the message. 
 * #param messageId messageId The ID of the message.
 * #param property property The property name. Valid value: state.
 * #param value value An array with the values of the property.
 * #param savedRetentionDays savedRetentionDays Number of retention days for saved state - DEPRECATED
 * #param readRetentionDays readRetentionDays Number of retention days for read state - DEPRECATED 
 */
function Message_SetStoredProperty(messageId, property, value, savedRetentionDays, readRetentionDays) {
	    mas.messageSetStoredProperty(messageId, property, value);
    return;
}


/**
 * #Displayname Message Set Offset
 * #Description Sets the offset to a given IMediaObject. Returns the updated IMediaObject
 * #param mediaObject mediaObject An IMediaObject.
 * #param offset offset The offset in milliseconds.
 */
function Message_SetOffset(mediaObject, offset) {
	return util.setOffset(mediaObject, offset);
}

/**
 * #DisplayName PhoneNumber Get Analyzed Number
 * #Description This function will return the analyzed phone number.
 * #param rule rule The rule to use for the analysis.
 * #param phoneNumber phoneNumber The phone number to analyze.
 * #param callerPhoneNumber callerPhoneNumber The callers phone number.
 */
function PhoneNumber_GetAnalyzedNumber(rule, phoneNumber, callerPhoneNumber) {
    if (phoneNumber == undefined)
        return "";
    if (callerPhoneNumber == '') {
        if ((phoneNumber == "")) {
            return "";
        }
        else {
            return mas.systemAnalyzeNumber(rule, phoneNumber, null);
        }
    }
    else
       return mas.systemAnalyzeNumber(rule, phoneNumber, callerPhoneNumber); 
}

/**
 * #DisplayName PhoneNumber Get Email Addresses
 * #Description This function will return the email addresses of the phone numbers specified in the array.
 * #param phoneNumberArray phoneNumberArray The array which contains phone numbers.
 */
function PhoneNumber_GetEmailAddresses(phoneNumberArray) {
    //var emailArray = [];

    //for (var i=0; i<phoneNumberArray.length; i++) {
    //	  emailArray[i] = Subscriber_GetSingleStringAttribute(phoneNumberArray[i], "mail");
    //}
    //return emailArray;
    return phoneNumberArray;
}
/**
 * #DisplayName PhoneNumber Is A Subscriber
 * #Description This function will return true if the specified phone number belongs to a subscriber in the system.
 * #param phoneNumber phoneNumber The phone number to check.
 */
function PhoneNumber_IsASubscriber(phoneNumber) {
              System_Log(4, "PhoneNumber_IsASubscriber: " + phoneNumber);				
	return mas.subscriberExist(phoneNumber);
}



/**
 * #DisplayName PhoneNumber can Subscribe MWI to Mailbox
 * #Description This function will return true if the specified phone number is allowed to subscribe to SIP MWI notifications for the given mailbox.
 * #param userAgentNumber The phone number to check.
 * #param mailboxId The mailbox id the User Agents wishes to subscribe to
 * #return true if the user agent is allowed to subscribe to the designated mailbox
 */
function PhoneNumber_canSubscribeToMWIForMailbox(userAgentNumber,mailboxId){
	mas.phoneNumberCanSubscribeToMWIForMailbox(userAgentNumber,mailboxId);
}

/**
 * #DisplayName PhoneNumber Is Family Mailbox Member
 * #Description Returns true the specified phoneNumber is a family mailbox member.
 * #param phoneNumber phoneNumber The phone number to check.
 */
function PhoneNumber_IsFamilyMailboxMember(phoneNumber) {
	if (phoneNumber.indexOf('-') != -1)
	   return true;
	else
	   return false;
}

/**
 * #DisplayName PhoneNumber Is Family Mailbox Owner
 * #Description Returns true the specified phoneNumber is a family mailbox owner.
 * #param phoneNumber phoneNumber The phone number to check.
 */
function PhoneNumber_IsFamilyMailboxOwner(phoneNumber) {
	   return false;
}

/**
 * #DisplayName PhoneNumber Is MissedCallNotification
 * #Description Returns true the specified phoneNumber equals one of the numbers that has been configured for the Missed Call Notification service. 
 * #param phoneNumber phoneNumber The phone number to check.
 */
function PhoneNumber_IsMissedCallNotification(phoneNumber) {
	var mCNNumbers = mas.systemGetConfigurationParameter("vva.incomingcall", "missedcallnotifnumbers");
	var number;
	var language;
	var indexOf = mCNNumbers.indexOf(":");
	while (indexOf != -1) {
		number = mCNNumbers.replace(/[\D]*([\d]*).*/, "$1");
		if (number == phoneNumber)
			return true;
		else {
			mCNNumbers = mCNNumbers.substring(indexOf+1);
			indexOf = mCNNumbers.indexOf(":");
		}
	}
	number = mCNNumbers.replace(/[\D]*([\d]*).*/, "$1");
	if (number == phoneNumber && phoneNumber > "")
		return true;
	else
		return false;		
}

/**
 * #DisplayName PhoneNumber StartsWith RetrievalPrefix
 * #Description Returns true if phoneNumber starts with the digits that are specified as RetrievalPrefix. 
 * #param phoneNumber phoneNumber The phone number to check.
 */
function PhoneNumber_StartsWithRetrievalPrefix(phoneNumber)
{
    var retrievalPrefix = mas.systemGetConfigurationParameter("vva.incomingcall", "retrievalprefix");
    if (retrievalPrefix == "")
        return false;
    else if (phoneNumber.indexOf(retrievalPrefix) == 0)
        return true;
    else
	    return false;	
}

/**
 * #DisplayName SMS Send Slamdown Information
 * #Description Sends a slamdown notification the the subscriber.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 * #param ani ani The analyzed ANI of the inbound call.
 */
function SMS_SendSlamdownInformation(phoneNumber, ani) {
    // send no slamdown if no ANI. or restricted number or subscriber does not have the service.	
	if (Call_GetAni() == "") {
		System_Log(4, "No slamdown is sent. Reason: ANI does not exist");
		return;
	}
	
	if (Call_GetAniPi() == 1) {
		System_Log(4, "No slamdown is sent. Reason: ANI is restricted");
		return;
	}
	
	if (Subscriber_HasService(phoneNumber, "slamdown_notification") == false ||
	    Subscriber_IsNotificationActive(phoneNumber, 'slamdown_notification') == false) {
		System_Log(4, "No slamdown is sent. Reason: Slamdown Notification service is not enabled");
		return;
	}

    //Can be: "unknown", "user busy", "no reply", "deflection during alerting", "deflection immediate response", "mobile subscriber not reachable"
    var noSlamdown = mas.systemGetConfigurationParameter("vva.dialogbehaviour", "redirectcausenoslamdown");

    // send no slamdown if redirection reason exists in config param "redirectcausenoslamdown"
    if (noSlamdown.search(Call_GetRdnisReason()) != -1) {
		System_Log(4, "No slamdown is sent. Reason: Slamdown is disabled for redirect reason: " + Call_GetRdnisReason());
		return;
	}

    var rule = mas.systemGetConfigurationParameter("vva.numberanalysis", "slamdownnumberrule");
    var number = PhoneNumber_GetAnalyzedNumber(rule, ani, null);

    var slamdownstring = "sender=" + number + ",callingnumber=" + number + ",callednumber=" + phoneNumber;
	    
    System_SendTrafficEvent("slamdowninformation", slamdownstring);
    System_Log(4, "Slamdown sent: " + slamdownstring);	
}
 
function SMS_SendTemporaryGreetingReminder(phoneNumber, days)
{
}
function SMS_SendToPhoneNumber(phoneNumber, content)
{
}

/**
 * #DisplayName SMS Send Quota Information
 * #Description Sends a quota full notification the the subscriber.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 * #param ani The analyzed ANI of the inbound call.
 */
function SMS_SendQuotaFullInformation(phoneNumber, ani) {

    var quotafullstring = "sender=" + ani + ",callingnumber=" + ani + ",callednumber=" + phoneNumber;
	    
    System_SendTrafficEvent("quotafullinformation", quotafullstring);
    System_Log(4, "Quota full sent: " + quotafullstring);	
}

/**
 * #DisplayName SMS Get MissedCallNotifLanguage
 * #Description Sets the prompt language depending on the configuration. Also returns the language.
 * #param calledNumber calledNumber The called number.
 */
function SMS_Get_MissedCallNotifLanguage(calledNumber) {
	var notifNumbers = mas.systemGetConfigurationParameter("vva.incomingcall", "missedcallnotifnumbers");
	var pos1 = 0;
	var pos2 = 0;
	var commaPos = 0;
	var language = "";
	var finished = false;
	notifNumbers = notifNumbers.replace(/ /, ""); //Remove spaces from string
	while (finished == false) {
		pos2 = notifNumbers.indexOf(":",pos1);
		
		commaPos = notifNumbers.indexOf(",",pos2);
		
		if (commaPos == -1) {
			commaPos = notifNumbers.indexOf("]",pos2);
		}
		
		if (pos2 == -1) { 
			language = mas.systemGetConfigurationParameter("vva.media", "defaultlanguage");
			finished = true;
		}
		else if (calledNumber == notifNumbers.substring(pos1 + 1, pos2)) {
			language = notifNumbers.substring(pos2 + 1, commaPos);
			finished = true;
		}
		pos1 = commaPos;
	}
	return language;
}

/**
 * #DisplayName Subscriber Create
 * #Description Creates a new subscriber in the user directory.
 * #param attrNames  attrNames  List of attribute names for the subscriber, the attrValues with the same index corresponds to this name.
 *                              The names should be the attributenames defined in CAI IWD
 * #param attrValues attrValues List of attribute values for the subscriber, the attrNames with the same index corresponds to this name.
 * #param adminUid   adminUid   Uid for an useradmin in the user directory.
 * #param cosName    cosName    Name on CoS to use for the new subscriber (optional, use null if not included)
 */
function Subscriber_Create(attrNames, attrValues, adminUid, cosName) {
	mas.subscriberCreate(attrNames, attrValues, adminUid, cosName);
}

/**
 * #Displayname Subscriber Is Notification Active
 * #Description Returns true if the subscriber has the specified notification type active
 * #param phoneNumber phoneNumber The phone number to work on.
 * #param notificationType notificationType The type of notification. E.g. SMS, ODL, MWI
 */
function Subscriber_IsNotificationActive(phoneNumber, notificationType) {
	if (notificationType == "slamdown_notification") {
		if (mas.subscriberGetStringAttribute(phoneNumber, "MOIPUserSD")[0].indexOf('slamdown_notification') == -1)
			return true;
	}
	else if (notificationType == "PAG") {
	    	// Return true if  PAG is not disabled in MOIPUserNTD. emntd is checked for backwords compability. Also, empnc must exist.
                if (mas.subscriberGetStringAttribute(phoneNumber, "MOIPUserNTD")[0].indexOf('PAG') == -1 &&
		    mas.subscriberGetStringAttribute(phoneNumber, "emntd")[0].indexOf('PAG') == -1 &&
		    mas.subscriberGetStringAttribute(phoneNumber, "empnc")[0] != '')
			return true;	   
    	}
	else {
        	if (mas.subscriberGetStringAttribute(phoneNumber, "MOIPUserNTD")[0].indexOf(notificationType.toUpperCase()) == -1 &&
		    mas.subscriberGetStringAttribute(phoneNumber, "emntd")[0].indexOf(notificationType.toUpperCase()) == -1) 
			return true; 
    	}
	return false;
}


/**
 * #DisplayName Subscriber Get Single Boolean Attribute
 * #Description This function returns the value of the attribute.
 * #param phoneNumber phoneNumber The phone number.
 * #param attributeName attributeName The name of the attribute to get.
 */
function Subscriber_GetSingleBooleanAttribute(phoneNumber, attributeName) {
	return mas.subscriberGetBooleanAttribute(phoneNumber, attributeName)[0];
}

/**
 * #DisplayName Subscriber Get Single Integer Attribute
 * #Description This function returns the value of the attribute.
 * #param phoneNumber phoneNumber The phone number.
 * #param attributeName attributeName The name of the attribute to get.
 */
function Subscriber_GetSingleIntegerAttribute(phoneNumber, attributeName) {
	return mas.subscriberGetIntegerAttribute(phoneNumber, attributeName)[0];
}

/**
 * #DisplayName Subscriber Get Single String Attribute
 * #Description This function returns the value of the attribute.
 * #param phoneNumber phoneNumber The phone number.
 * #param attributeName attributeName The name of the attribute to get.
 */
function Subscriber_GetSingleStringAttribute(phoneNumber, attributeName) {
	return mas.subscriberGetStringAttribute(phoneNumber, attributeName)[0];
}

/**
 * #DisplayName Subscriber Get Mailbox
 * #Description This function returns the mailbox id of the subscribers default mailbox.
 * #param phoneNumber phoneNumber The phone number.
 */
function Subscriber_GetMailbox(phoneNumber) {
	return mas.subscriberGetMailbox(phoneNumber);
}

/**
 * #DisplayName Subscriber Get Multi String Attribute
 * #Description This function returns the value of the attribute.
 * #param phoneNumber phoneNumber The phone number.
 * #param attributeName attributeName The name of the attribute to get.
 */
function Subscriber_GetMultiStringAttribute(phoneNumber, attributeName) {
	return mas.subscriberGetStringAttribute(phoneNumber, attributeName);
}
/**
 * #DisplayName Subscriber Get Caller Dependent Greeting
 * #Description Returns an array of callers that have specified greetings. If no callers are found, an empty array is returned.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 */
function Subscriber_GetCallerDependentCaller(phoneNumber){
	var activeCdg = mas.subscriberGetStringAttribute(phoneNumber, "emactivecdg")[0];
	if (activeCdg == "")
	   return [];

	activeCdg = activeCdg.split(',');
	for (var i=0; i<activeCdg.length; i++)
		activeCdg[i] = activeCdg[i].replace(/#/g, "");
	return activeCdg;
}

/**
 * #DisplayName Subscriber Get Message Types Allowed
 * #Description This function returns a string containing the valid message types for a subscriber.
 * #phoneNumber phoneNumber The subscribers phonenumber.
 */
function Subscriber_GetMessageTypesAllowed(phoneNumber) {
	var resultstring = "";
	if (Subscriber_HasService(phoneNumber, "voice") == true)
		resultstring = resultstring.concat("voice,");
	if (Subscriber_HasService(phoneNumber, "video"))
		resultstring = resultstring.concat("video,");
	if (Subscriber_HasService(phoneNumber, "fax"))
		resultstring = resultstring.concat("fax,");
	if (Subscriber_HasService(phoneNumber, "email"))
		resultstring = resultstring.concat("email,");
	resultstring = resultstring.replace(/,$/,''); //Removes the last character if it is a comma.
	return resultstring;
}

function Subscriber_GetNumberOfDistributionLists(phoneNumber)
{
}

/**
 * #DisplayName Subscriber Has Service
 * #Description Returns true if the subscriber has the specified service. 
 * #phoneNumber phoneNumber The phone number to check service for.
 * #service service The service to check.
 */
function Subscriber_HasService(phoneNumber, service)
{
    // Fax feature not available for Mio MOIP 1.0
    //if (service == "fax")
    //    return mas.subscriberGetBooleanAttribute(phoneNumber, "faxenabled")[0];
		
    if (service == "outdial") {
        var subXfer = mas.subscriberGetBooleanAttribute(phoneNumber, "MOIPSubscriberXfer")[0];	
        var serviceDn = mas.subscriberGetStringAttribute(phoneNumber, "MOIPServices");

		for (var i = 0; i < serviceDn.length; i++) {
	    	if (subXfer == true && serviceDn[i].search("call_handling") != -1)
				return true;
		}
    }
	
    if (service == "tts")
        return mas.subscriberGetBooleanAttribute(phoneNumber, "ttsemailenabled")[0];

    if (service == "callednumberlogin")
        return mas.subscriberGetBooleanAttribute(phoneNumber, "MOIPCnl")[0];	

    if (service == "temporarygreeting")
        return mas.subscriberGetBooleanAttribute(phoneNumber, "MOIPTmpGrtAvailable")[0];	

    var serviceDn = mas.subscriberGetStringAttribute(phoneNumber, "MOIPServices");
    for (var i = 0; i < serviceDn.length; i++) {
	if ((service == "voice") && (serviceDn[i].search("msgtype_voice") != -1))
		return true;
       	if ((service == "video") && (serviceDn[i].search("msgtype_video") != -1))
	    	return true;
	if ((service == "email") && (serviceDn[i].search("msgtype_email") != -1))
	   		return true;
    	if ((service == "sendmessage") && (serviceDn[i].search("sendmessage") != -1))
	   		return true;
    	if ((service == "forwardmessagewithcomment") && (serviceDn[i].search("forwardmessagewithcomment") != -1))
	   		return true;
		if ((service == "forwardmessagewithoutcomment") && (serviceDn[i].search("forwardmessagewithoutcomment") != -1))
	   		return true;
		if ((service == "prerecordedgreeting_tui") && (serviceDn[i].search("prerecordedgreeting_tui") != -1))
	   		return true;
        if (service == "notification") {
	    // Return true if any notification service is active
    	    if ((serviceDn[i].search("sms_notification") != -1))
    			return true;
	    	else if ((serviceDn[i].search("slamdown_notification") != -1))
    			return true;
    	    else if ((serviceDn[i].search("mwi_notification") != -1))
    			return true;
    	    else if ((serviceDn[i].search("outdial_notification") != -1))
    			return true;
    	    else if ((serviceDn[i].search("wappush_notification") != -1))
    			return true;
    	    else if ((serviceDn[i].search("mms_notification") != -1))
    			return true;
    	    else if ((serviceDn[i].search("pager_notification") != -1))
    			return true;
    	    else if ((serviceDn[i].search("callMWI_notification") != -1))
    			return true;
    		else if ((serviceDn[i].search("email_notification") != -1))
    			return true;	
        }
	if (service == "sms_notification") {
	    // Return true if sms_notification service is active
            if ((serviceDn[i].search("sms_notification") != -1)) {
		return true; 
	   }
    	}
	if (service == "mwi_notification") {
	    // Return true if mwi_notification service is active
            if ((serviceDn[i].search("mwi_notification") != -1)) {
		return true;
	   }
    	}
	if (service == "outdial_notification") {
	    // Return true if outdial_notification service is active
            if ((serviceDn[i].search("outdial_notification") != -1)) {
		return true;
	   }
    	}
	if (service == "wappush_notification") {
	    // Return true if wappush_notification service is active
            if ((serviceDn[i].search("wappush_notification") != -1)) {
		return true;
	   }
    	}
        if (service == "mms_notification") {
	    // Return true if mms_notification service is active
            if ((serviceDn[i].search("mms_notification") != -1)) {
		return true;
	   }
    	}
	if (service == "pager_notification") {
	    // Return true if pager_notification service is active	 
            if ((serviceDn[i].search("pager_notification") != -1)) {
		return true;
	   }
    	}
	if (service == "callmwi_notification") {
	    // Return true if callmwi_notification service is active
            if ((serviceDn[i].search("callMWI_notification") != -1)) {
		return true;
	   }
    	}
        if (service == "slamdown_notification") {
	    // Return true if slamdown_notification service is active
            if ((serviceDn[i].search("slamdown_notification") != -1)) {
		return true;
	   }
    	}
    	if (service == "email_notification") {
	    // Return true if email_notification service is active
            if ((serviceDn[i].search("email_notification") != -1)) {
		return true;
	   }
    	}
	if (service == "caller_dependent_greeting") {
	    // Return true if caller_dependent_greeting is active.
            if ((serviceDn[i].search("caller_dependent_greeting") != -1))
		return true;
    	}
	if (service == "family_mailbox") {
	    // Return true if family mailbox is active.
            if ((serviceDn[i].search("family_mailbox") != -1))
		return true;
	}
    }
    return false;
}
	
function Subscriber_HasValidEmailAddress(phoneNumber)
{
}

/**
 * #Displayname Subscriber Set Notification
 * #Description Sets the notification to active or inactive.
 * #param phoneNumber phoneNumber The phone number to work on.
 * #param notificationType notificationType The type of notification.
 * #param value Value The value to set. Can be enable or disable.
 */
function Subscriber_SetNotification(phoneNumber, notificationType, value){
	var notificationString, attributeName;

	if (notificationType == "slamdown_notification") {
		attributeName = "MOIPUserSD";
		var notificationStringTmp = mas.subscriberGetStringAttribute(phoneNumber, "MOIPUserSD")[0];
		notificationString = notificationStringTmp;
	}
	else {
		attributeName = "MOIPUserNTD";
		var notificationStringTmp2 = mas.subscriberGetStringAttribute(phoneNumber, "MOIPUserNTD")[0];
		notificationString = notificationStringTmp2;
		var notificationStringTmp;
		notificationType = notificationType.toUpperCase();

		notificationStringTmp = mas.subscriberGetStringAttribute(phoneNumber, "emntd")[0];
		if (notificationStringTmp != "") {
			notificationString = notificationString + notificationStringTmp;
		        mas.subscriberSetStringAttribute(phoneNumber, "emntd", null);
			System_Log(4,"VVA: Removing attribute emntd, since it is obsoleted by MOIPUserNTD");	
		}
	}

	if (value == "disable") {
 		notificationString = notificationString + "," + notificationType;
		notificationString = notificationString.replace(/^,/, '');
		Subscriber_SetSingleStringAttribute(phoneNumber, attributeName, notificationString);
		System_Log(4,"VVA: Disable notification=" + notificationType + " notificationString=" + 
			   notificationString + " Subscriber=" + phoneNumber);	
	}	
	else if (value == "enable") {
		notificationString = notificationString.replace(notificationType,'');
	        notificationString = notificationString.replace(/^,/, '');
       		notificationString = notificationString.replace(/,$/, '');
		notificationString = notificationString.replace(/,,/, ',');
		if (notificationType == "slamdown_notification") 
			mas.subscriberSetStringAttribute(phoneNumber, "MOIPUserSD", null);
		else if ( notificationString != '')
			Subscriber_SetSingleStringAttribute(phoneNumber, attributeName, notificationString);
		else
		        mas.subscriberSetStringAttribute(phoneNumber, "MOIPUserNTD", null);

		System_Log(4,"VVA: Enable notification=" + notificationType + " notificationString=" + 
			   notificationString + " Subscriber=" + phoneNumber);	
	}
	else
		System_Log(4,"VVA: Wrong value in Subscriber_SetNotification. Value=" + value + ", Subscriber" + phoneNumber);	
}

/**
 * #Displayname Subscriber Set Single Boolean Attribute
 * #Description Sets an attribute to a value.
 * #param phoneNumber phoneNumber The phone number to check.
 * #param attributeName attributeName The name of the attribute.
 * #param value Value The value for the attribute.
 */
function Subscriber_SetSingleBooleanAttribute(phoneNumber, attributeName, value) {
    mas.subscriberSetBooleanAttribute(phoneNumber, attributeName, [value]);
}

/**
 * #Displayname Subscriber Set Single Integer Attribute
 * #Description Sets an attribute to a value.
 * #param phoneNumber phoneNumber The phone number to check.
 * #param attributeName attributeName The name of the attribute.
 * #param value Value The value for the attribute.
 */
function Subscriber_SetSingleIntegerAttribute(phoneNumber, attributeName, value) {
    mas.subscriberSetIntegerAttribute(phoneNumber, attributeName, [value]);
}

/**
 * #Displayname Subscriber Set Single String Attribute
 * #Description Sets an attribute to a value.
 * #param phoneNumber phoneNumber The phone number to check.
 * #param attributeName attributeName The name of the attribute.
 * #param value Value The value for the attribute.
 */
function Subscriber_SetSingleStringAttribute(phoneNumber, attributeName, value) {
    mas.subscriberSetStringAttribute(phoneNumber, attributeName, [value]);
}


/**
 * #Displayname Subscriber Get Message Retention Time
 * #Description Returns an array with the message retention times, in days, for read and saved messages
 * #param phoneNumber phoneNumber The phone number to check.
 * #param calltype calltype The calltype, 'voice' or 'video'.
 */
function Subscriber_GetMessageRetentionTimes(phoneNumber, calltype) {
    var retentionTimes = [7, 14];

    return retentionTimes;
}

/**
 * #Displayname System Convert Sip To Xmp
 * #Description Converts a SIP code to XMP code by using the configuration file.
 * #param code code The SIP code to be converted.
 */
function System_CheckSubcriptionValidity(mailboxId,number) {
    mas.systemLog(1, "System_CheckSubcriptionValidity NOT IMPLEMENTED!!!!! ");

    //TODO Check for valid subscription file and subsriber service
    return true;

}


/**
 * #Displayname System Convert Sip To Xmp
 * #Description Converts a SIP code to XMP code by using the configuration file.
 * #param code code The SIP code to be converted.
 */
function System_ConvertSipToXmp(code) {
    var configuredCodes = ["failed", "ok", "retry"];
    var i;
    var j;
    var codeString;
    var xmpCode;
    var sipCodes;
    for (i=0; i<configuredCodes.length; i++) {
                codeString = System_GetConfig("vva.siptoxmp", configuredCodes[i]);
                xmpCode = codeString.substring(0,3);
                sipCodes = codeString.substring(4).split(",");
                for (j=0; j<sipCodes.length; j++) {
                   if (sipCodes[j].search("-")) {
                      if ((parseInt(code, 10) >= parseInt(sipCodes[j].substring(0,3),10)) && (parseInt(code, 10) <= parseInt(sipCodes[j].substring(4),10))) {
                         return xmpCode;
                      }
                      if (code == sipCodes[j]) {
                         return xmpCode;
                      }
                   }
                }
        }
        return System_GetConfig("vva.siptoxmp", "default");
}

/**
 * #Displayname System Get Config
 * #Description Returns the value of the specified parameter.
 * #param section section The section for the configuration parameter.
 * #param parameterName parameterName The name of the configuration parameter.
 */
function System_GetConfig(section, parameterName) {
	return mas.systemGetConfigurationParameter(section, parameterName);
}

/**
 * #Displayname System Get Language Code
 * #Description Returns the language code for the element at position index in the LanguageNumbers.
 * #param subLanguageNumbers the languageNumbers for the subscriber.
 * #param index index The index in the configuration array LanguageNumbers.
 */
function System_GetLanguageCode(subLanguageNumbers,index)
{
    
    var languageNumbers;
    
    //If the input subLanguageNumbers is invalid then load the default ones.

    
    if ((subLanguageNumbers == '') || (subLanguageNumbers == null)){
    	subLanguageNumbers= mas.systemGetConfigurationParameter("vva.dialogbehaviour", "languagenumbers");
    }
    
    languageNumbers= subLanguageNumbers.replace(/\[| |\]/g, '').split(/,/);
    mas.systemLog(4, "MultiLang: System_GetLanguageCode(): languageNumbers = " +  languageNumbers + "index = " + index);
       
    if (index < languageNumbers.length){
        return languageNumbers[index].substring(0, languageNumbers[index].indexOf(':'));
    }
    else{
        return "";
    }
}

/**
 * #Displayname System Get Language Number
 * #Description Returns the language number for the element at position index in the configuration parameter LanguageNumbers.
 * #param index index The index in the configuration array LanguageNumbers.
 */
function System_GetLanguageNumber(index)
{
    var languageNumbers = mas.systemGetConfigurationParameter("vva.dialogbehaviour", "languagenumbers").replace(/\[| |\]/g, '').split(/,/);
    if (index < languageNumbers.length)
        return languageNumbers[index].substring(languageNumbers[index].indexOf(':')+1);
    else
        return "";
}

/*
 * Local function
 * Returns a media qualifer object of the specified type with the specified value.
 */
function System_GetMediaQualifier() {
    //return util.getMediaQualifier(type, value)
    if (System_GetMediaQualifier.arguments.length == 1)
	    return util.getMediaQualifier(System_GetMediaQualifier.arguments[0]);
	else
		return util.getMediaQualifier(System_GetMediaQualifier.arguments[0], System_GetMediaQualifier.arguments[1]);
}

function System_GetPrompt()
{
    if (System_GetPrompt.arguments.length == 1)
	    return mas.systemGetMediaContent("prompt", System_GetPrompt.arguments[0]);
	else
		return mas.systemGetMediaContent("prompt", System_GetPrompt.arguments[0], System_GetPrompt.arguments[1]);
}

function System_GetPromptForTtsLanguage()
{
}

/**
 * #Displayname System Get Service Request Parameter
 * #Description Returns the value for the specified parameter in the Service Request.
 * #param name name The name of the parameter.
 */
function System_GetServiceRequestParameter(name) {
    return mas.systemGetServiceRequestParameter(name);
}

/**
 * #Displayname System Get Service Response Header Parameter
 * #Description Returns the value for a header parameter, from the last received Service Response.
 * #param parameterName parameterName The name of the header parameter.
 */
function System_GetServiceResponseHeaderParameter(parameterName) {
    return mas.systemGetServiceResponseHeaderParameter(parameterName);
}

/**
 * #Displayname System Get Service Response Parameter
 * #Description Returns the value for a service response parameter from the response
 * #param parameterName parameterName The name of the parameter.
 */
function System_GetServiceResponseParameter(parameterName) {
    return mas.systemGetServiceResponseParameter(parameterName);
}


/**
 * #Displayname System Get Subscribers
 * #Description Returns a list with phonenumbers to the subscribers that has the given value for the specified attribute.
 * #param name name The name of the attribute.
 * #param value value The value for the attribute.
 */
function System_GetSubscribers(name, value) {
    return mas.systemGetSubscribers(name, value);
}

/**
 * #Displayname System Is Append Supported
 * #Description Returns true if the active Media Content Package supports append of recordings.
 */
 function System_IsAppendSupported()
{
    return mas.systemIsAppendSupported("prompt");
}

function System_IsVpimEnabled()
{
}

/**
 * #Displayname System Log
 * #Description Prints a log message.
 * #param severity severity Valid values: 0-fatal, 1-error, 2-warn, 3-info, 4-debug.
 * #param msg msg The message to log. 
 */
function System_Log(severity, msg) {
	mas.systemLog(severity, msg);
}


/**
 * #Displayname System Set Early Media
 * #Description Set early media resource using language and variant from configuration
 */
function System_SetEarlyMedia() {
    var language = mas.systemGetConfigurationParameter('vva.media', 'defaultlanguage');
    var variantVoice = mas.systemGetConfigurationParameter('vva.media', 'variantvoice');
    var variantVideo = mas.systemGetConfigurationParameter('vva.media', 'variantvideo');
    mas.systemSetEarlyMediaResource(language, variantVoice, variantVideo);
}

/**
 * #Displayname System Set Partition Restriction
 * #Description Set a restriction for searches in User Directory to be only in the local sub/domain for example when using iMux.
 * #param restrict restrict A boolean that defines if searches shall be restricted or not.
 */
function System_SetPartitionRestriction(restrict) {
    mas.systemSetPartitionRestriction(restrict);
}

/**
 * #Displayname System Set Prompt Language
 * #Description Set the specified language as prompt language.
 * #param language Language The language to set.
 * #param extension Extension the brand used for language settings.
 * #param voiceVariant VoiceVariant The voice variant for the language.
 * #param videoVariant VideoVariant The video variant for the language.
 */
function System_SetPromptLanguage(language, extension, voiceVariant, videoVariant) {
	
	
    var brandedLang = Utility_CreateBrandedLanguage(language, extension);
    
    mas.systemLog(4, "MultiLang: calling System_SetPromptLanguage: language = " + language + " brand = " + extension );

    if (Call_IsVoice() == true)
        mas.systemSetMediaResources(brandedLang, voiceVariant, null);
    else if (Call_IsVideo() == true)
        mas.systemSetMediaResources(brandedLang, null, videoVariant);
    else
        mas.systemSetMediaResources(brandedLang, voiceVariant, videoVariant); 
    return true;
}


/**
 * #Displayname System Set Media Content Package
 * #Description Set the media content package for the specified resource type.
 * #param resourceType ResourceType The resource type to set.
 * #param language Language The language to set.
 * #param voiceVariant VoiceVariant The voice variant for the language.
 * #param videoVariant VideoVariant The video variant for the language.
 */
function System_SetMediaContentPackage(resourceType, language, voiceVariant, videoVariant) {
    mas.systemSetMediaResource(resourceType, language, voiceVariant, videoVariant);
    return true;
}

/**
 * #DysplayName System MWI Subscribe
 * #Description Creates an active SIP subscription to "message-information" event for the given mailbox obn behalf of userAgentNumber   
 * @param mailboxId the mailbox the user agent wishes to subscribe to
 * @param userAgentNumber the subscribing user agent number 
 * @param callId the call-id of the SIP subscribe
 * @param fromTag the from tag of the SIP subscribe
 * @param toTag the to tag of the SIP subscribe
 * @param cSeq the CSeq of the SIP subscribe
 * @param expires the value of the expires header of the SIP subscribe
 * @return true of the subscription has succeeded, false if it has failed
 */
function System_MWISubscribe(mailboxId,uuserAgentNumber,callId,fromTag,toTag,cSeq,expires) {
	return mas.systemMWISubscribe(maiboxId,userAgentNumber,callId,fromTag,toTag,cSeq,expires)
	
}

/**
 * Create a language string using <language>_<brand>
 * @param language the base language 
 * @param extension the brand
 * @return Final language string that can be used to reference mediaContentPackage
 */
function Utility_CreateBrandedLanguage(language, extension) {
	
	var brLang = language;
	if ((extension != null)&& (extension != "")) {
		brLang = language + "_" + extension;
	}
	mas.systemLog(4, "MultiLang: Utility_CreateBrandedLanguage() language = " + language + " brand = " + extension + ". Using branded language " + brLang);

	return brLang;
}

/**
 * #Displayname System Get Valid Language List
 * #Description Return the list of valid languages for a subscribers LanguageExtension
 * #param extension Extension Extension language to set.
 */
function System_GetValidLanguageList( extension ) {
  var brLangName =  'languagenumbers';
  
    if ((extension != null)&& (extension != "")) {
        brLangName = extension + '_languagenumbers';
    }
    mas.systemLog(4, "MultiLang: System_GetValidLanguageList(): getting languagelist for " +  brLangName + " = " + System_GetConfig('vva.dialogbehaviour', brLangName));

    return System_GetConfig('vva.dialogbehaviour', brLangName);
}

/**
 * #Displayname System Set Property
 * #Description Set the specified sytem property to the specified value.
 * #param name name The name of the property.
 * #param value value The value to be set.
 */
function System_SetProperty(name, value) {
    mas.systemSetProperty(name, value);
}

/**
 * #Displayname System Send Service Request
 * #Description Sends a Service Request.
 * #param hostName hostName The hostname.
 * #param serviceName serviceName The service name.
 * #param ValidityTime ValidityTime The validity time of the request.
 * #param ReportIndication ReportIndication The report indication.
 * #param parameterNames parameterNames The array with parameter names.
 * #param parameterValues parameterValues The array with parameter values.
 */

function System_SendServiceRequest(hostName, serviceName, ValidityTime, ReportIndication, parameterNames, parameterValues)	{
	mas.systemSendServiceRequest(hostName, serviceName, ValidityTime, ReportIndication, parameterNames, parameterValues);
}

/**
 * #Displayname System Send Service Response
 * #Description Sends a Service Response with the specified status code, status text and parameters.
 * #param statusCode statusCode The status code.
 * #param statusText statusText The status text.
 * #param parameterNames parameterNames The array with parameter names.
 * #param parameterValues parameterValues The array with parameter values.
 */
function System_SendServiceResponse(statusCode, statusText, parameterNames, parameterValues) {
    mas.systemSendServiceResponse(statusCode, statusText, parameterNames, parameterValues);
    return true;
}

/**
 * #Displayname System Send SIP Message
 * #Description Sends a SIP Message with the specified se.
 * #param messageName messageName The name of the message. For Example: "messagewaiting".
 * #param parameterNames parameterNames An array of parameter names. The value corresponding to each parameter name is found at the same position in the paramValues array. For example, paramNames[3] corresponds to paramValues[3]. 
 * #param parameterValues parameterValues An array of parameter values. The name corresponding to each parameter value is found at the same position in the paramNames array. 
 */
function System_SendSIPMessage(messageName, parameterNames, parameterValues){
    mas.systemSendSIPMessage('messagewaiting', parameterNames, parameterValues);
}

/**
 * #Displayname System Send Traffic Event
 * #Description Sends the specifies traffic event.
 * #param eventName eventName The name of the event.
 * #param properties properties The properties for the event specified as <name>=<value>,<name>=<value>, etc.
 */
function System_SendTrafficEvent(eventName, properties) {
    System_SendTrafficEventLocal(eventName, properties, null);
    return true;
}

/**
 * Needed to support sending of events from CCXML
 * Sends the specifies traffic event.
 * param eventName eventName The name of the event.
 * param properties properties The properties for the event specified as <name>=<value>,<name>=<value>, etc.
 * param calltype calltype The calltype, 'voice' or 'video'.
 */
function System_SendTrafficEvent2(eventName, properties, calltype) {
    System_SendTrafficEventLocal(eventName, properties, calltype);
    return true;
}

/**
 * Local function.
 * Sends the specified traffic event.
 */
function System_SendTrafficEventLocal(eventName, properties, calltype) {
    var propertyName = [];
    var propertyValue = [];
    var indexComma;
    var indexEqual;    
    var i = 0;    
    while (properties.length != 0) {
        indexComma = properties.indexOf(',');
        indexEqual = properties.indexOf('=');
        propertyName[i] = properties.substring(0, indexEqual);
        if (indexComma != -1) {
            propertyValue[i] = properties.substring(indexEqual+1, indexComma);
            properties = properties.substring(indexComma + 1);
        }
        else {
            propertyValue[i] = properties.substring(indexEqual+1);
            properties = '';
        }
        i = i + 1;
    }

    // accesstype should not be appended on the following events.
    if (eventName != "cliinformationmessage" && eventName != "mwioff" && eventName != "missedcallnotification" && eventName !="mailboxupdate") {
        propertyName.push("accesstype");

        if (calltype != null) {
	        if (calltype == 'voice') {
	            propertyValue.push("7");
	        }
	        else {
	            propertyValue.push("10");
            }
        }
        else {
	        if (Call_IsVoice()) {
	            propertyValue.push("7");
	        }
	        else {
	            propertyValue.push("10");
	        }
        }
    }    

    mas.trafficEventSend(eventName, propertyName, propertyValue, false);
}

/*
 * Local function
 * Returns true if the first string ends with the second string.
 */
 
function System_StringEndsWith(aString, aSubString) {
    var index = aString.indexOf(aSubString);
    if (index != -1) {
        if (index == aString.length - aSubString.length) {
            return true;
        }
        else {
            return false;
        }
    }
    else {
        return false;
    }    
}

/**
 * #displayname Time Get Seconds
 * #description Returns the milliseconds in seconds.
 * #param milliseconds milliseconds The number of milliseconds.
 */
function Time_GetSeconds(milliseconds) {
    return Math.floor(milliseconds/1000);
}

/**
 * #displayname Time Get UTC Seconds
 * #description Returns the current UTC seconds (seconds since 1 January 1970 00:00:00).
 */
function Time_GetUtcSeconds() {
    var now = new Date();
    return Math.floor(now.valueOf()/1000);
}

/**
 * #displayname Time, current time
 * #description Returns the Date and Time.
 */

function Time_GetNow() {
    var dateobj2 = new Date();
    var date=dateobj2.toString();
    var date2 = date.substr(0,3) + " ";
    date2 += date.substr(7,3);
    date2 += date.substr(3,4);
    date2 += date.substr(10,15);
    date2 += date.substr(28,5);
    return date2;
}

/**
 * #displayname Time Is Afternoon
 * #description Returns true if the actual time is within the time limits that are specified for an afternoon.
 * #phoneNumber phoneNumber The phone number to check time for, regarding timezone.
 */
function Time_IsAfternoon(phoneNumber) {
    var hhmm;
    if (mas.subscriberExist(phoneNumber)) {
        var now = util.getCurrentTime(mas.subscriberGetStringAttribute(phoneNumber, "CNTimeZone")[0]);
        hhmm = util.formatTime(now, 'HHmm');
    }
    else {
        var now = new Date();
        var hours = now.getHours();
        var minutes = now.getMinutes();
        if (hours < 10)
            hours = '0' + hours;
        if (minutes < 10)
            minutes = '0' + minutes;
        hhmm = hours.toString() + minutes.toString();
    }
    
    if (mas.systemGetConfigurationParameter('vva.timesettings', 'eveningstart') >=
        mas.systemGetConfigurationParameter('vva.timesettings', 'afternoonstart')) {
        if ((hhmm >= mas.systemGetConfigurationParameter('vva.timesettings', 'afternoonstart')) &&
            (hhmm < mas.systemGetConfigurationParameter('vva.timesettings', 'eveningstart'))) {
            return true;
        }
        else {
           return false;
        }
    }
    else {
        if ((hhmm >= mas.systemGetConfigurationParameter('vva.timesettings', 'afternoonstart')) ||
            (hhmm < mas.systemGetConfigurationParameter('vva.timesettings', 'eveningstart'))) {
            return true;
        }
        else {
            return false;
        }
    }        
}

/**
 * #displayname Time Is Evening
 * #description Returns true if the actual time is within the time limits that are specified for an evening.
 * #phoneNumber phoneNumber The phone number to check time for, regarding timezone.
 */
function Time_IsEvening(phoneNumber) {
    var hhmm;
    if (mas.subscriberExist(phoneNumber)) {
        var now = util.getCurrentTime(mas.subscriberGetStringAttribute(phoneNumber, 'CNTimeZone')[0]);
        hhmm = util.formatTime(now, 'HHmm');
    }
    else {
        var now = new Date();
        var hours = now.getHours();
        var minutes = now.getMinutes();
        if (hours < 10)
            hours = '0' + hours;
        if (minutes < 10)
            minutes = '0' + minutes;
        hhmm = hours.toString() + minutes.toString();
    }
    
    if (mas.systemGetConfigurationParameter('vva.timesettings', 'morningstart') >=
        mas.systemGetConfigurationParameter('vva.timesettings', 'eveningstart')) {
        if ((hhmm >= mas.systemGetConfigurationParameter('vva.timesettings', 'eveningstart')) &&
            (hhmm < mas.systemGetConfigurationParameter('vva.timesettings', 'morningstart'))) {
            return true;
        }
        else {
           return false;
        }
    }
    else {
        if ((hhmm >= mas.systemGetConfigurationParameter('vva.timesettings', 'eveningstart')) ||
            (hhmm < mas.systemGetConfigurationParameter('vva.timesettings', 'morningstart'))) {
            return true;
        }
        else {
            return false;
        }
    }        
}

/**
 * #displayname Time Is Morning
 * #description Returns true if the actual time is within the time limits that are specified for a morning.
 * #phoneNumber phoneNumber The phone number to check time for, regarding timezone.
 */
function Time_IsMorning(phoneNumber) {
    var hhmm;
    if (mas.subscriberExist(phoneNumber)) {
        var now = util.getCurrentTime(mas.subscriberGetStringAttribute(phoneNumber, 'CNTimeZone')[0]);
        hhmm = util.formatTime(now, 'HHmm');
    }
    else {
        var now = new Date();
        var hours = now.getHours();
        var minutes = now.getMinutes();
        if (hours < 10)
            hours = '0' + hours;
        if (minutes < 10)
            minutes = '0' + minutes;
        hhmm = hours.toString() + minutes.toString();
    }
    
    if (mas.systemGetConfigurationParameter('vva.timesettings', 'afternoonstart') >=
        mas.systemGetConfigurationParameter('vva.timesettings', 'morningstart')) {
        if ((hhmm >= mas.systemGetConfigurationParameter('vva.timesettings', 'morningstart')) &&
            (hhmm < mas.systemGetConfigurationParameter('vva.timesettings', 'afternoonstart'))) {
            return true;
        }
        else {
           return false;
        }
    }
    else {
        if ((hhmm >= mas.systemGetConfigurationParameter('vva.timesettings', 'morningstart')) ||
            (hhmm < mas.systemGetConfigurationParameter('vva.timesettings', 'afternoonstart'))) {
            return true;
        }
        else {
            return false;
        }
    }        
}
function Time_IsValid(year, month, day, hour, minute, hourFormat)
{
}
function Tts_SetLanguage(language)
{
}

/**
 * #DisplayName Utility Append Media Objects
 * #Description Concatinates two media objects, if the first object is equal to false, the second object is returned.
 * #param mo1 mo1 The first media object.
 * #param mo2 mo2 The second media object.
 */
function appendRecording(mo1, mo2) {
   if (mo1 == false)
      return mo2;
   return util.appendMediaObjects(mo1, mo2);
}

/**
 * #DisplayName Utility Append Recorded Media Objects
 * #Description Concatinates two media objects, if the first object is equal to false, the second object is returned.
 * #param mo1 The first media object.
 * #param mo2 The second media object.
 */

function Utility_AppendRecording(mo1, mo2) {
	return appendRecording(mo1,mo2);
}

/**
 * #displayname Utility Concat Array
 * #description Returns the concatinated array.
 * #param array1 array1 The first array.
 * #param array2 array2 The second array.
 */
function Utility_ConcatArray(array1, array2) {
  return array1.concat(array2);
}

/**
 * #displayname Utility Convert Time
 * #description Returns the given vvaTime formatted to the format specified by the pattern, converted to the subscribers time zone.
 * #param phoneNumber phoneNumber The phoneNumber of the subscriber.
 * #param vvaTime vvaTime The time in vva format.
 * #param pattern pattern The pattern to format the time into.
 */
function Utility_ConvertTime(phoneNumber, vvaTime, pattern) {
    var subTimezone = mas.subscriberGetStringAttribute(phoneNumber, 'CNTimeZone')[0];
    var tzTime = util.convertTime(vvaTime, null, subTimezone);
	return util.formatTime(tzTime, pattern);
}

/**
 * #displayname Utility Convert To Lower Case
 * #description Returns the converted string.
 * #param str str The string to convert.
 */
function Utility_ConvertToLowerCase(str) {
	return str.toLowerCase();
}

/**
 * #displayname Utility Create Owner Name
 * #description Creates and Returns the Event Attribute Owner Name
 * #param phoneNumber phoneNumber The phoneNumber of the subscriber.
 */
function Utility_CreateOwnerName(phoneNumber)
{
   var ownerNameString = "";
   return ownerNameString;
}

/**
 * #displayname Utility Delete Array Element
 * #description Deletes the element at the specified position in the list.
 * #param list List The list.
 * #param pos Position The element position.
*/
function Utility_DeleteArrayElement(list, pos) {
    return list.splice(pos,1);
}
/**
 * #displayname Utility Format Time
 * #description Returns the given vvaTime formatted to the format specified by the pattern.
 * #param vvaTime vvaTime The time in vva format.
 * #param pattern pattern The pattern to format the time into.
 */
function Utility_FormatTime(vvaTime, pattern) {
	return util.formatTime(vvaTime, pattern);
}

/**
 * #displayname Utility Get Array Element
 * #description Returns the element at the indicated position.
 * #param list List The list.
 * #param pos Position The element position.
*/
function Utility_GetArrayElement(list, pos) {
  return list[pos];
}

/**
 * #displayname Utility Get Array Position
 * #description Returns the position of the value. Returns -1 if the value is not found.
 * #param list list The list.
 * #param value value The value to search for.
*/
function Utility_GetArrayPosition(list, value) {
    for (var i=0; i<list.length; i++) {
	if (list[i] == value)
	    return i;
    }
    return -1;
}

/**
 * #displayname Utility Get Array Length
 * #description Returns the length of the array.
 * #param list list The list.
 */
function Utility_GetArrayLength(list) {
  return list.length;
}

/**
 * #displayname Utility Get Email Address From String
 * #description Returns the email address from the inputted string.
 * #param str str The string to fetch the email address from.
 */
function Utility_GetEmailAddressFromString(str) {
   var emailAddress;
   if (str.search('>') != -1)
          emailAddress=str.substring(str.indexOf('<')+1,str.indexOf('>'));
       else
          emailAddress=str;
  return emailAddress;
}


/**
 * #displayname Utility Get SubString
 * #description Returns substring.
 * #param str string The orginal string
 * #param from from position to start from.
 * #param to to position to end with.
*/
function Utility_GetSubString(str, from, to) {
  return str.substring(from,to);
}
/**
 * #displayname Utility Remove Spaces
 * #description Returns the input string without spaces.
 * #param str string The orginal string
*/
function Utility_RemoveSpaces(str) {
  return str.replace(/ /g,'');
}

/**
 * #displayname Utility Seconds To HMS
 * #description Returns an array with the seconds converted to [h,m,s].
 * #param seconds seconds The number of seconds to convert.
*/
function Utility_SecondsToHMS(seconds) {
	var h = Math.floor(seconds/3600);
	var m = Math.floor((seconds-(h*3600))/60);
	var s = Math.floor(seconds-(h*3600)-(m*60));
  return [h,m,s];
}


/**
 * #displayname Utility Set Array Element
 * #description Set the element at the specified position in the list.
 * #param list List The list.
 * #param pos Position The element position.
 * #param element Element The element to insert.
*/
function Utility_SetArrayElement(list, pos, element) {
  list[pos] = element;
}


/**
 * #displayname Utility Set Global Object
 * #description Sets the specified global object variable to the specified value.
 * #param name name The name of the variable in GlobalObject.
 * #param value value The value for the specified variable.
*/
function Utility_SetGlobalObject(name, value) {
    if (name == "ANI") 
        GlobalObject.ANI = value;
    if (name == "Subscriber") 
        GlobalObject.Subscriber = value;
    if (name == "LoggedIn") 
        GlobalObject.LoggedIn = value;
    if (name == "LoginType") 
        GlobalObject.LoginType = value;
    if (name == "MainMenuValid") 
        GlobalObject.MainMenuValid = value;
    if (name == "PreviousMenuValid") 
        GlobalObject.PreviousMenuValid = value;
    if (name == "UtcSecondsAtConnect") 
        GlobalObject.UtcSecondsAtConnect = value;
    if (name == "CallState") 
        GlobalObject.CallState = value;

	return;
}

/**
 * #displayname Utility String Search
 * #description Returns true if the subStr is found in str, othewise false.
 * #param str str The string to be searched.
 * #param subStr subStr The sub string to search for inside str.
*/
function Utility_StringSearch(str, subStr) {
	if (str.search(subStr) != -1)
		return true;
	return false;
}

/**
 * #displayname Utility Array Search
 * #description Returns true if the subStr is found in arr, othewise false.
 * #param arr arr The array to be searched.
 * #param subStr subStr The sub string to search for in each position of arr.
*/
function Utility_ArraySearch(arr, subStr) {
        for (var i=0; i < arr.length; i++)
        {
	if (arr[i].search(subStr) != -1)
		return true;
        }
	return false;
}
/**
 * #displayname Utility String To Array
 * #description Converts the input string to an array.
 * #param str str The string to be converted.
 * #param separator separator The caracter that separates the array into separate elements.
*/
function Utility_StringToArray(str, separator) {
	str = str.replace(/\[/,"");
	str = str.replace(/\]/,"");
	str = str.replace(/ /,"");
	return str.split(separator);
}


/**
 * #displayname Utility String To Int
 * #description Converts the input string to an integer.
 * #param str str The string to be converted.
*/
function Utility_StringToInt(str) {
	return parseInt(str, 10);
}



var soapResult = null;

/**
 * #DisplayName  TestSOAP_sendSoap
 * #param soapParameter soapParameter The parameter to the soap call.
 * Returns the error code of the SOAP service call
 *      0 == success
 *      1 == server down
 *      2 == timeout
 *      3 == soap/http error
 */
function TestSOAP_sendSoap(soapParameter) {
    
    var replyResult = 0;
    
    try {	

        proxies.readtimeout = 1000;   // in ms
        proxies.soaptest.func = null; // no hook up function !
        soapResult = proxies.soaptest.GetNumber(soapParameter); // call the server and return the result.
        
        if(proxies.httpstatus == 400 || proxies.httpstatus == 500)
        {
            // SOAP Error response will come in HTTP 400 or 500 response
            System_Log(1,"VVA: TestSOAP_sendSoap SOAP Error Response"); 
            replyResult = 3;       
        }    
        
    } catch (err) {
    
        replyResult = 3;   // default error 
        var errStr = err.toString();
        System_Log(1,"VVA: TestSOAP_sendSoap Error: "+ errStr + " HTTP Status:" + proxies.httpstatus );
    
        if(errStr.search("java.net.SocketTimeoutException") != -1){
            //timeout has occured
            replyResult = 2;
        } else if ( (errStr.search("java.net.ConnectException") != -1) || 
                    (errStr.search("java.net.UnknownHostException") != -1) ||   
                    (errStr.search("java.io.FileNotFoundException") != -1) ) {  //HTTP 404
            //the server does not exist or is down
            replyResult = 1; 
        } else if ( (errStr.search("Failed to parse XML") != -1) ||
                    (errStr.search("Server returned HTTP response code") != -1) ) {  //other HTTP/XML errors
            //other possible error response from the server
            replyResult = 3;
        }        

    }
      
    return replyResult;
}

/**
 * #DisplayName  TestSOAP_getResult
 * Returns the result of the SOAP service call
 *    an integer
 */
function TestSOAP_getResult() {
    var number = soapResult.descendants().number;
    var identifier = soapResult.descendants().identifier;  
    return number;
}


/**
 * #displayname Authentication Check Pin
 * #description Authentication Check Pin
 * #param phoneNumber phoneNumber The phoneNumber of the subscriber.
 * #param newPin newPin the pin the subscriber submitted
 */
function Authentication_CheckPin(phoneNumber, newPin)
{
	return mas.checkPin(phoneNumber, newPin);
}

/**
 * #displayname Authentication Change Pin
 * #description Authentication Change Pin
 * #param phoneNumber phoneNumber The phoneNumber of the subscriber.
 * #param newPin newPin the pin the subscriber submitted
 */
function Authentication_ChangePin(phoneNumber, newPin)
{
	return mas.changePassword(phoneNumber, newPin, newPin, "reason");
}


/**
 * #DisplayName  Menu_testSoapEnabled
 */
function Menu_testSoapEnabled() {
    
    var testsoap_enabled = false;
    try {
        testsoap_enabled = (System_GetConfig("vva.dialogbehaviour", "testsoapenabled") == "true");
    } catch (err) { }
    
    return testsoap_enabled;    
}

var MenuArray = new Array();
var GlobalObject = { ANI:'', Subscriber:'', LoggedIn:false, LoginType:'retrieval', MainMenuValid:false, PreviousMenuValid:false, CallState:'init' };

/**
 * #DysplayName System MWI Subscribe
 * #Description Creates an active SIP subscription to "message-information" event for the given mailbox obn behalf of userAgentNumber   
 * @param mailboxId the mailbox the user agent wishes to subscribe to
 * @param userAgentNumber the subscribing user agent number 
 * @param callId the call-id of the SIP subscribe
 * @param fromTag the from tag of the SIP subscribe
 * @param toTag the to tag of the SIP subscribe
 * @param cSeq the CSeq of the SIP subscribe
 * @param expires the value of the expires header of the SIP subscribe
 * @return true of the subscription has succeeded, false if it has failed
 */
function System_MWISubscribe(mailboxId,userAgentNumber,callId,fromTag,toTag,cSeq,expires) {
	return mas.systemMWISubscribe(mailboxId,userAgentNumber,callId,fromTag,toTag,cSeq,expires)
	
}

/**
 * #DisplayName PhoneNumber can Subscribe MWI to Mailbox
 * #Description This function will return true if the specified phone number is allowed to subscribe to SIP MWI notifications for the given mailbox.
 * #param userAgentNumber The phone number to check.
 * #param mailboxId The mailbox id the User Agents wishes to subscribe to
 * #return true if the user agent is allowed to subscribe to the designated mailbox
 */
function PhoneNumber_canSubscribeToMWIForMailbox(userAgentNumber,mailboxId){
	return mas.phoneNumberCanSubscribeToMWIForMailbox(userAgentNumber,mailboxId);
}