
/**
 * #DisplayName Call Get ANI
 * #Description Returns the ANI of an incoming call (the calling party number).
 */
function Call_GetAni(){
    // return session.connection.remote.number
	return "999";
}

/**
 * #DisplayName Call Get ANI PI
 * #Description Returns the PI for ANI of an incoming call (the presentation indicator for calling party number).
 */
function Call_GetAniPi(){
    // return session.connection.remote.pi
	return 0;
}

/**
 * #DisplayName Call Get DNIS
 * #Description Returns the DNIS of an incoming call (the called party number).
 */
function Call_GetDnis(){
    // return session.connection.local.number
	return "222222";
}

/**
 * #DisplayName Call Get Media Type
 * #Description Returns the type of media, "voice" or "video".
 */
function Call_GetMediaType(){
    // return session.connection.calltype
	return "voice";
}

/**
 * #DisplayName Call Get RDNIS
 * #Description Returns the RDNIS of an incoming call (the redirecting party number).
 */
function Call_GetRdnis(){
    // return session.connection.redirect[0].number
	return "";
}

/**
 * #DisplayName Call Get RDNIS PI
 * #Description Returns the PI for RDNIS of an incoming call (the presentation indicator for redirecting party number).
 */
function Call_GetRdnisPi(){
    // return session.connection.redirect[0].pi
	return 0;
}

/**
 * #DisplayName Call Get RDNIS Reason
 * #Description Returns the reason for redirection of an incoming call. Can be "unknown", "user busy", "no reply", 
 * "deflection during alerting", "deflection immediate response" or "mobile subscriber not reachable".
 */
function Call_GetRdnisReason(){
    // return session.connection.redirect[0].reason
	return "unknown";
}

/**
 * #DisplayName Call Get Session Type
 * #Description Returns the type of session, e.g. "incoming_call" or "outdial_notification".
 */
function Call_GetSessionType(){
    // return session.connection.ccxml.namelist
	return "incoming_call";
}

/**
 * #DisplayName Call Is Video
 * #Description Returns true if the call is a video call.
 */
function Call_IsVideo() {
    // return (session.connection.calltype == "video")
	return false;
}

/**
 * #DisplayName Call Is Voice
 * #Description Returns true if the call is a voice call.
 */
function Call_IsVoice() {
    // return (session.connection.calltype == "voice")
	return true;
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
 * #DisplayName Event Get Message Type
 * #Description Returns a string with the radius-ma type value for the given message type.
 * #param messageId messageId The Id of the current message.
 */
function Event_GetMessageType(messageId) {
	var type = mas.messageGetProperty(messageId, 'type');	
	if (type == "voice")
		return "10";
	if (type == "video")
		return "20";
	if (type == "fax")
		return "40";
	if (type == "email")
		return "1";
	return "";
}

/**
 * #DisplayName Event Get Message Encoding
 * #Description Returns a string with the radius-ma encoding value for the given message type.
 * #param messageId messageId The Id of the current message.
 */
function Event_GetMessageEncoding(messageId) {
	var type = mas.messageGetProperty(messageId, 'type');	
	if (type == "voice")
		return "10"; //wav
	if (type == "video")
		return "40"; //mov
	if (type == "fax")
		return "32"; //tiff
	if (type == "email")
		return "1"; //mime
	return "";
}

/**
 * #DisplayName Event Get Message Size
 * #Description Returns a string with the size of the given message type.
 * #param messageId messageId The Id of the current message.
 */
function Event_GetMessageSize(messageId) {
	var contentArray = [];
	var size = 0;	
	var type = mas.messageGetProperty(messageId, 'type');	
	
	if (type == "voice") {
		contentArray = mas.messageGetContent(messageId);				
		for (var i = 0; i < contentArray.length; i++) {
		
			if (mas.messageGetMediaProperties(contentArray[i]).search("audio") != -1) {
				size = size + mas.messageContentLength(contentArray[i], "milliseconds");
			}
		}
		size = Math.round(size/1000); // convert from milliseconds to seconds;
		return size.toString();
	}
	if (type == "video") {
		contentArray = mas.messageGetContent(messageId);				
		for (var i = 0; i < contentArray.length; i++) {		
			if (mas.messageGetMediaProperties(contentArray[i]).search("video") != -1) {
				size = size + mas.messageContentLength(contentArray[i], "milliseconds");
			}
		}
		size = Math.round(size/1000); // convert from milliseconds to seconds;
		return size.toString();
	}
	if (type == "fax") {
		contentArray = mas.messageGetContent(messageId);				
		for (var i = 0; i < contentArray.length; i++) {		
			if (mas.messageGetMediaProperties(contentArray[i]).search("image/tiff") != -1) {
				size = size + mas.messageContentLength(contentArray[i], "pages");
			}
		}
		return size.toString();
	}
	if (type == "email") {
		contentArray = mas.messageGetContent(messageId);				
		for (var i = 0; i < contentArray.length; i++) {
			size = size + mas.messageContentSize(contentArray[i]);
		}
		return size.toString();
	}
	return "";
}

function FamilyMailbox_GetListOfMembers(phoneNumber)
{
}
function FamilyMailbox_GetListWithNumberOfMessages(phoneNumber)
{
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
	
    mas.subscriberSetStringAttribute(subscriber, "emftl", fTLString)
	return fTLString;
}

function FTL_DoesMandatoryFunctionsExist(phoneNumber)
{
}
function FTL_DoesOptionalFunctionsExist(phoneNumber)
{
}
function FTL_GetFirstFunction(phoneNumber)
{
}
function FTL_GetNextFunction(phoneNumber)
{
}
function FTL_IsCompleted(phoneNumber)
{
}
function FTL_IsCurrentFunctionGreeting(phoneNumber)
{
}
function FTL_IsCurrentFunctionMandatory(phoneNumber)
{
}
function FTL_IsCurrentFunctionName(phoneNumber)
{
}
function FTL_IsCurrentFunctionNotification(phoneNumber)
{
}
function FTL_IsCurrentFunctionOptional(phoneNumber)
{
}
function FTL_IsCurrentFunctionPIN(phoneNumber)
{
}
function FTL_IsCurrentFunctionTheLast(phoneNumber)
{
}
function FTL_IsNotUsingOwnPhone(phoneNumber)
{
}

/**
 * #DisplayName FTL Set Part As Performed
 * #Description Stores information that the specified part of the First Time login procedure has been performed.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 * #param part part The first tiem login part. Can be "pin", "spokenname", "greeting", "language" or "notification"
 */
function FTL_SetPartAsPerformed(phoneNumber, part) {
	var fTLString = Subscriber_GetSingleStringAttribute(phoneNumber, "emftl");
	
	if(part == "language") {
		fTLString = fTLString.substring(0,3) + "F" + fTLString.substring(4);
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
	Subscriber_SetSingleStringAttribute(phoneNumber, 'emFTL', fTLString);
	return fTLString;
}

/**
 * #DisplayName Greeting Activate
 * #Description Activates the greeting for the specified subscriber.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 * #param greetingType greetingType The type of greeting, eg AllCalls, Busy, NoAnswer, OutOfHours, Extended_Absence, Temporary, SpokenName.
 * #param mediaType mediaType The media type of the greeting, eg voice/video. FOR FUTURE USE!
 */
function Greeting_Activate(phoneNumber, greetingType, mediaType) {
    var activeGreetings = mas.subscriberGetStringAttribute(phoneNumber, "activegreetingid")[0];

    if (activeGreetings.indexOf(greetingType) == -1) {
	activeGreetings = activeGreetings.replace(/Extended_Absence/, "");
	activeGreetings = activeGreetings.replace(/[\s,]*$/, "");
	activeGreetings = activeGreetings + "," + greetingType;	
        mas.subscriberSetStringAttribute(phoneNumber, "activegreetingid", new Array(activeGreetings));
    }
}
/**
 * #DisplayName Greeting Deactivate
 * #Description Deactivates the greeting for the specified subscriber.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 * #param greetingType greetingType The type of greeting, eg AllCalls, Busy, NoAnswer, OutOfHours, Extended_Absence, Temporary, SpokenName.
 * #param mediaType mediaType The media type of the greeting, eg voice/video. FOR FUTURE USE!
 */
function Greeting_Deactivate(phoneNumber, greetingType, mediaType) {
    var activeGreetings = mas.subscriberGetStringAttribute(phoneNumber, "activegreetingid")[0];

    if (activeGreetings.indexOf(greetingType) != -1) {
	var regexp = "/" + greetingType + "/";
	activeGreetings = activeGreetings.replace(/[\s,]*$/, "");
	activeGreetings = activeGreetings.replace(regexp, "");
        mas.subscriberSetStringAttribute(phoneNumber, "activegreetingid", new Array(activeGreetings));
    }
}
/**
 * #DisplayName Greeting Delete
 * #Description Deletes the greeting for the specified subscriber.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 * #param greetingType greetingType The type of greeting, eg AllCalls, Busy, NoAnswer, OutOfHours, Extended_Absence, Temporary, SpokenName.
 * #param mediaType mediaType The media type of the greeting, eg voice/video.
 */
function Greeting_Delete(phoneNumber, greetingType, mediaType) {
    if (greetingType == "SpokenName")
	mas.subscriberSetSpokenName(phoneNumber, mediaType, null);
    else
	mas.subscriberSetGreeting(phoneNumber, greetingType, mediaType, null);

    Greeting_Deactivate(phoneNumber, greetingType, mediaType);
}
/**
 * #DisplayName Greeting Get Fun Greeting
 * #Description Returns the fun greeting specified.
 * #param mediaContentId mediaContentId The id of the fun greeting.
 */
function Greeting_GetFunGreeting(mediaContentId) {
    return mas.systemGetMediaContent("FunGreeting", mediaContentId, null);
}
/**
 * #DisplayName Greeting Get Fun Greeting List
 * #Description Returns a list of available fun greetings.
 */
function Greeting_GetFunGreetingList() {
    return mas.systemGetMediaContentIds("FunGreeting", null);
}
/**
 * #DisplayName Greeting Get Greeting
 * #Description Returns the greeting file specified.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 * #param greetingType greetingType The type of greeting, eg AllCalls, Busy, NoAnswer, OutOfHours, Extended_Absence, Temporary, SpokenName.
 * #param mediaType mediaType The media type of greeting. voice/video
 */
function Greeting_GetGreeting(phoneNumber, greetingType, mediaType){
    if (greetingType == "SpokenName")
	return mas.subscriberGetSpokenName(phoneNumber, greetingType, mediaType);
    else
	return mas.subscriberGetGreeting(phoneNumber, greetingType, mediaType, "");
}
/**
 * #DisplayName Greeting Has Temporary Greeting Activation Time Passed
 * #Description Returns true if the activation time for temporary greeting has passed for the subscriber with the specified phone number.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 */
function Greeting_HasTemporaryActivationTimePassed(phoneNumber) {
    var tmpGreeting = mas.subscriberGetStringAttribute(phoneNumber, "emtmpgrt")[0];
    var index = tmpGreeting.indexOf(";");
    var activationTime = tmpGreeting.substring(0, index);

    //Expected time format is: YYYY-MM-DD HH:MM:SS
    var currentTime = util.getCurrentTime(mas.subscriberGetStringAttribute(phoneNumber, 'SubscriberTimeZone')[0]);
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
    var tmpGreeting = mas.subscriberGetStringAttribute(phoneNumber, "emtmpgrt")[0];
    var index = tmpGreeting.indexOf(";");
    var deactivationTime = tmpGreeting.substring(index+1, tmpGreeting.length);

    //Expected time format is: YYYY-MM-DD HH:MM:SS
    var currentTime = util.getCurrentTime(mas.subscriberGetStringAttribute(phoneNumber, "subscribertimezone")[0]);
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
 * #param greetingType greetingType The type of greeting, eg AllCalls, Busy, NoAnswer, OutOfHours, Extended_Absence, Temporary, SpokenName.
 * #param mediaType mediaType The media type of the greeting, eg voice/video. FOR FUTURE USE!
 */
function Greeting_IsActive(phoneNumber, greetingType, mediaType) {
    var activeGreetings = mas.subscriberGetStringAttribute(phoneNumber, "activegreetingid")[0];

    if ((greetingType == "Temporary") && mas.subscriberGetStringAttribute(phoneNumber, "emtmpgrt")[0].indexOf(";") != -1)
        return true;   
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
    var businessDays = mas.subscriberGetStringAttribute(phoneNumber, "inhoursdOW")[0];
    var startTime = mas.subscriberGetStringAttribute(phoneNumber, "inhoursstart")[0];
    var endTime = mas.subscriberGetStringAttribute(phoneNumber, "inhoursend")[0];
    
    //Expected time format is: YYYY-MM-DD HH:MM:SS
    var currentTime = util.getCurrentTime(mas.subscriberGetStringAttribute(phoneNumber, "subscribertimezone")[0]);
    currentTime = util.formatTime(currentTime, 'yyyy-MM-dd HH:mm:ss');
	
    var time = new Date(currentTime.substr(0,4),parseInt(currentTime.substr(5,2)-1),currentTime.substr(8,2),
    			currentTime.substr(11,2),currentTime.substr(14,2),currentTime.substr(17,2));
	
    if (businessDays.indexOf(time.getDay()) < 0)
	return true;
    else if ((parseInt(startTime,10) > parseInt(time.getHours() + time.getMinutes(), 10) || 
	    parseInt(endTime,10) < parseInt(time.getHours() + time.getMinutes(), 10)))
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
 * #param greetingRef greetingRef The file that contains the recording.
 */
function Greeting_StoreRecording(phoneNumber, greetingType, mediaType, greetingRef) {
    if (greetingType == "SpokenName")
	mas.subscriberSetSpokenName(phoneNumber, mediaType, greetingRef);
    else
	mas.subscriberSetGreeting(phoneNumber, greetingType, mediaType, null, greetingRef);
}
function Greeting_SetTemporaryAutomaticDeactivation(phoneNumber, days) {
}
function Login_GetCosWelcomeGreeting(phoneNumber)
{
}

/**
 * #DisplayName Login Is Own Phone Check OK
 * #Description Returns true if Calling from own phone or initAnyPhone=yes or UserLevel>10. Note that error.com.mobeon.platform.datanotfound is
 * thrown if Userlevel does not exist and that in such a case it must be checked that the attribute emFTL does not contain PIN.
 * #param subscriber subscriber The phone number of the subscriber.
 * #param ani ani The phone number of the caller.
 */
function Login_IsOwnPhoneCheckOk(subscriber, ani){
    if (subscriber == ani)
	    return true;
	else if (mas.systemGetConfigurationParameter("vva.dialogbehaviour", "initpinanyphone") == "yes")
	    return true;
    else if (mas.subscriberGetIntegerAttribute(subscriber, "userlevel") >= 10)
	    return true;
    else
        return false;
}

/**
 * #DisplayName Login Get Phone Number
 * #Description Returns true if the calling party must enter the phone number during the login procedure.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 */
function Login_GetPhoneNumber(phoneNumber) {
    if (mas.subscriberExist(phoneNumber)) {
        if ((mas.subscriberGetBooleanAttribute(phoneNumber, "fastloginavailable")[0] == true) && 
            (mas.subscriberGetBooleanAttribute(phoneNumber, "fastloginenabled")[0] == true)) { 
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
 * #Description Returns true if the preferred language shall be set during login, that is, LoginSetLanguages==Yes and UserLevel<5.
 * Note that error.com.mobeon.platform.datanotfound is thrown if Userlevel does not exist and that in such a case the attribute emFTL must be checked.
 */
function Login_ShallPreferredLanguageBeSet(phoneNumber) {
    if (mas.systemGetConfigurationParameter("vva.dialogbehaviour", "loginsetlanguages") == "no")
        return false;
    else if (phoneNumber == "")
        return true;
    else if (mas.subscriberGetIntegerAttribute(phoneNumber, "userlevel") >= 5)
        return false;
    else
        return true;
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
 * #Description Returns true if the quota is over warning level, otherwise return false.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 */
function Mailbox_IsFilledOverWarningLevel(phoneNumber) {
    var mailboxId = mas.subscriberGetMailbox(phoneNumber);
    var noOfMessagesUsed = mas.mailboxGetMessageUsage(mailboxId); 
    var noOfMessagesQuota = mas.subscriberGetIntegerAttribute(phoneNumber, "emnoOfmailquota")[0];
    var warningLevel = mas.subscriberGetStringAttribute(phoneNumber, "diskSpaceremainingwarninglevel")[0];

    //Check if amount of messages is over warning level
    if (noOfMessagesQuota != -1 && ((1 - warningLevel) * noOfMessagesQuota) <= noOfMessagesUsed)
        return true;

    var byteQuota = mas.subscriberGetIntegerAttribute(phoneNumber, "mailquota")[0];
    var bytesUsed = mas.mailboxGetByteUsage(mailboxId);
    
    //Check if the disk usage is over byte quota warning
    if (byteQuota != -1 && ((1 - warningLevel) * byteQuota) <= bytesUsed)
	    return true;
    else
	    return false;
}
/**
 * #DisplayName Mailbox Is Full Retrieval
 * #Description Returns true if the mailbox is filled over the quota limit, otherwise return false.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 */
function Mailbox_IsFullRetrieval(phoneNumber) {
    var mailboxId = mas.subscriberGetMailbox(phoneNumber);
    var noOfMessagesUsed = mas.mailboxGetMessageUsage(mailboxId); 
    var noOfMessagesQuota = mas.subscriberGetIntegerAttribute(phoneNumber, "emnoOfmailquota")[0];

    //Check if amount of messages is over quota
    if (noOfMessagesQuota != -1 && noOfMessagesUsed >= noOfMessagesQuota)
        return true;

    var secondsOverhead = 3;
    var maxLengthOfMsg;
    var byteQuota = mas.subscriberGetIntegerAttribute(phoneNumber, "mailquota")[0];
    var endOfMsgWarning = mas.subscriberGetIntegerAttribute(phoneNumber, "eomsgwarning")[0];

    var secondsLeft = Utility_SecondsLeftOfMessageRecording(phoneNumber, "", mailboxId, byteQuota);

    if (Call_IsVideo())
	    maxLengthOfMsg = mas.subscriberGetIntegerAttribute(phoneNumber, "emmsglenmaxvideo")[0];
    else
	    maxLengthOfMsg = mas.subscriberGetIntegerAttribute(phoneNumber, "msglenmaxvoice")[0];
    
    //Check if the disk usage is over byte quota
    if (byteQuota != -1 && secondsLeft <= endOfMsgWarning+secondsOverhead && secondsLeft <=  maxLengthOfMsg+secondsOverhead)
	    return false;
    else
	    return true;
}
/**
 * #DisplayName Mailbox Is Full Deposit
 * #Description Returns true if the mailbox is filled over the quota limit, otherwise return false.
 * #param phoneNumber phoneNumber The phone number of the subscriber.
 * #param callerPhoneNumber callerPhoneNumber The phone number of the caller (ANI).
 */
function Mailbox_IsFullDeposit(phoneNumber, callerPhoneNumber) {
    var mailboxId = mas.subscriberGetMailbox(phoneNumber);
    var noOfMessagesUsed = mas.mailboxGetMessageUsage(mailboxId); 
    var noOfMessagesQuota = mas.subscriberGetIntegerAttribute(phoneNumber, "emnoofmailquota")[0];

    //Check if amount of messages is over quota
    if (noOfMessagesQuota != -1 && noOfMessagesUsed >= noOfMessagesQuota)
        return true;

    var secondsOverhead = 3;
    var maxLengthOfMsg;
    var byteQuota = mas.subscriberGetIntegerAttribute(phoneNumber, "mailquota")[0];
    var endOfMsgWarning = mas.subscriberGetIntegerAttribute(phoneNumber, "eomsgwarning")[0];

    var secondsLeft = Utility_SecondsLeftOfMessageRecording(phoneNumber, callerPhoneNumber, mailboxId, byteQuota);

    if (Call_IsVideo())
	    maxLengthOfMsg = mas.subscriberGetIntegerAttribute(phoneNumber, "emmsgLenmaxvideo")[0];
    else
	    maxLengthOfMsg = mas.subscriberGetIntegerAttribute(phoneNumber, "msglenmaxvoice")[0];
    
    //Check if the disk usage is over byte quota
    if (byteQuota != -1 && secondsLeft <= endOfMsgWarning+secondsOverhead && secondsLeft <=  maxLengthOfMsg+secondsOverhead)
	return false;
    else
	return true;
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

/*
 * Local function
 * Removes the last element in MenuArray and returns the element that then is the last element.
 * Returns an empty string if there is zero or one element in MenuArray.
 */
function Menu_GetPrevious() {
    var len = MenuArray.length;
    if (len == 0)
        return "";
    else if (len == 1) {
        MenuArray.splice(len-1, 1);
        return "";
    }
    else {
        MenuArray.splice(len-1, 1);
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
	var blockedMenus = mas.subscriberGetStringAttribute(phoneNumber, "emtuiblockedmenu");
	for (var i = 0; i < blockedMenus.length; i++) {
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

function Message_AreNoRecepientsOK()
{
}
function Message_AreSomeOfTheRecepientsSubscribers()
{
}
function Message_AreSomeRecepientsOK()
{
}
/**
 * #DisplayName Message Check Recipients
 * #Description Returns an array which contains the emailaddresses of the subscribers that can recieve the specified mediaType.
 * #param emailArray emailArray The array containing the emailaddresses.
 * #param mediaType mediaType The media type of the message, eg voice/video.
 */
function Message_CheckRecepients(emailArray, mediaType) {
    var i=0;
    var j=0;
    var phoneNumber;
    var newEmailArray = [];

    //remove recipients that cannot recive the specified type.
    for (i; i<emailArray.length; i++) {
	phoneNumber = mas.systemGetSubscribers("mail", emailArray[i]);

    	if (Subscriber_HasService(phoneNumber, mediaType)) {
	    newEmailArray[j] = emailArray[i];	    
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
 * #DisplayName Message Days Since Delivery
 * #Description Returns the number of days between the delivery date and current time.
 * #param mailboxId mailboxId The maibox id.
 * #param messageId messageId The message id.
 */
function Message_DaysSinceDelivery(mailboxId, messageId) {
	var currentTime = getCurrentTime(subscriberGetStringAttribute(mailboxId, "subscribertimezone")[0]);
	var deliveryDateInSeconds =  util.formatTime(messageGetProperty(messageId, "deliverydate"), null);
	var secondsAtMidnigth = util.formatTime(currentTime.substr(0,10) + "23:59:59", null);
	return Math.floor(secondsAtMidnigth/deliveryDateInSeconds);
}

function Message_GetCallbackNumber()
{
}

/**
 * #DisplayName Message Get Attachment Text
 * #Description Returns the text that describes the containing attachment.
 * #param messageId messageId The Id of the message.
 */
function Message_GetAttachmentText(messageId) {
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

	var result = "This message contains ";
	result = result.concat(total.toString());
	if (total == 1)
		result = result.concat(" attachment. ");
	else
		result = result.concat(" attachments. ");	
	if (text > 0)
		result = result.concat(text.toString(), " Plain text, ");
	if (audio > 0)
		result= result.concat(audio.toString(), " Audio message, ");
	if (video > 0)
		result= result.concat(video.toString(), " Video message, ");
	if (word > 0)
		result= result.concat(word.toString(), " Microsoft Word, ");
	if (excel > 0)
		result= result.concat(excel.toString(), " Microsoft Excel, ");
	if (ppt > 0)
		result= result.concat(ppt.toString(), " Microsoft PowerPoint, ");
	if (fax > 0)
		result= result.concat(fax.toString(), " Fax image, ");
	if (other > 0)
		result= result.concat(other.toString(), " Other, ");

	return result;
}


function Message_GetFaxNumber()
{
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
	var type = mas.messageGetProperty(messageId, 'type');
	
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
				size = size + mas.messageContentLength(contentArray[i], "milliseconds");
				}
			}
		size = Math.round(size/1000); // convert from milliseconds to seconds;
		resultString.concat(",messagesize=", size.toString());
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
				size = size + mas.messageContentLength(contentArray[i], "milliseconds");
				}
			}
		size = Math.round(size/1000); // convert from milliseconds to seconds;
		resultString.concat(",messagesize=", size.toString());
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
	
	for (var i = 0; i < contentArray.length; i++) {
		if (mas.messageGetMediaProperties(contentArray[i]).search("image/tiff") != -1) {
			pages = pages + mas.messageContentLength(contentArray[i], "pages");
		}
	}
	return pages;
}

function Message_GetNumberOfRecipients() {
}
/**
 * #DisplayName Message Get Playable Parts
 * #Description Returns an array with the playable body parts.
 * #param mailboxId mailboxId The Id of the mailbox.
 * #param messageId messageId The Id of the message.
 */
function Message_GetPlayableParts(mailboxId, messageId)
{
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
		if ((mas.messageGetMediaProperties(orginalContentArray[i]).search("image/tiff") != -1) && ( Subscriber_HasService(mailboxId, "fax"))) {
			playableContentArray.push(orginalContentArray[i]);
			continue;
		}
	 	if ((mas.messageGetMediaProperties(orginalContentArray[i]).search("text") != -1) && ( Subscriber_HasService(mailboxId, "TTS"  ))) {
			playableContentArray.push(orginalContentArray[i]);
			continue;
		}
	}
	return playableContentArray;
}

/**
 * #DisplayName Message Get Property
 * #Description Returns the property of the given property name.
 * #param messageId messageId The Id of the message.
 * #param property property The property name.
 */
function Message_GetProperty(messageId, property) {
    return mas.messageGetProperty(messageId, property);
}


/**
 * #DisplayName Message Get Respond Addresses
 * #Description Returns an array the respond addresses.
 * #param mailboxId mailboxId The identity of the mailbox.
 * #param messageId messageId The identity of the message.
 * #param respondAddresses respondAddresses The type of respond addresses. Valid values: "allTo", "allCc" or "senderTo".
 */
function Message_GetRespondAddresses(mailboxId, messageId, respondAddresses) {
	var ownAddress = "";
	
	//If respondAdresses == "allCc", generate the respond to all cc addresses.
	if (respondAddresses == "allCc") {
		ownAddress  = mas.subscriberGetStringAttribute(mailboxId, "mail");	
		var ccAddresses = mas.messageGetProperty(messageId, "secondaryrecipients");
		var ccAddressesModifyed = [];
	
		//Remove own address from cc array.
		for (var i=0; i<ccAddresses.length; i++)
			if (ccAddresses[i] != ownAddress)
				ccAddressesModifyed.push(ccAddresses[i]);
		return ccAddressesModifyed;
	}
	
	//If ReplyToEmailAddr exist, use that address. Otherwise, use Sender
	var replyAddress = mas.messageGetProperty(messageId, "replytoaddr");
	if (replyAddress.length == 0)
		replyAddress = mas.messageGetProperty(messageId, "sender");

	//If respondAdresses == "senderTo", return the replyAddress.
	if (respondAddresses == "senderTo") {
		return replyAddress;
	}

	//If respondType == "allTo", generate the respond to all To addresses.
	ownAddress  = mas.subscriberGetStringAttribute(mailboxId, "mail");
	var toAddresses = mas.messageGetProperty(messageId, "recipients");
	var toAddressesModifyed = [];
	
	//Remove own address from to array.
	for (var i=0; i<toAddresses.length; i++)
		if (toAddresses[i] != ownAddress)
			toAddressesModifyed.push(toAddresses[i]);
						
	return replyAddress.concat(toAddressesModifyed);
}

/**
 * #DisplayName Message Get Senders PhoneNumber
 * #Description Fetch the phone number from the given string. Returns an empty string if no number was found.
 * #param senderString senderString The string to fetch the phone number from.
 */
function Message_GetSendersPhoneNumber(senderString) {
	
	/* Test if deposit by a Subscriber*/
	var number = (senderString.substring(senderString.indexOf("(") +1, senderString.indexOf(")")));
	if ( parseInt(number, 10)) {
		return number;
	}
	else {
		/* Test if Deposit by Non-Subscriber */
		number = (senderString.substring(0, senderString.indexOf("<") - 1 ) );    
		if ( parseInt(number, 10)) {
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

function Message_GetSubject()
{
}
function Message_HasDistributionListRecipients()
{
}
function Message_HasTooManyRecipients()
{
}

function Message_IsFutureDeliverySet()
{
}
function Message_IsMaxLengthRecorded()
{
}
function Message_IsNoneOfTheRecepientsSubscribers()
{
}
/**
 * #DisplayName Message Is Printable
 * #Description Determine if any of the content part of a message is printeble.
 * #param contentArray contentArray An array containing the contents. 
 */
function Message_IsPrintable(contentArray)
{
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
	if (Subscriber_HasService(mailboxId, 'sendvoicemessage') == false)
		return false;
		
	// Check if only one recipients
	if ((mas.messageGetProperty(messageId, "recipients").length + mas.messageGetProperty(messageId, "secondaryrecipients").length) == 1)
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
	if (Subscriber_HasService(mailboxId, "sendvoicemessage") == false)
		return false;
		
	// Check if Undelivered
	if (mas.messageGetProperty(messageId, "deliverystatus")[0] != "success")
		return false;

	// Check if fax that is not forwarded
	if ((mas.messageGetProperty(messageId, "type")[0] == "fax") && (messageGetProperty(messageId, "forwarded")[0] == "false"))
		return false;

	return true;
}

/**
 * #DisplayName Message Print
 * #Description Prints the specified message to the specified destination.
 * #param messageId messageId The ID of the message.
 * #param faxNumber faxNumber The fax number to print the message to.  
 */
function Message_Print(messageId, faxNumber) {
	mas.messagePrint(messageId, faxNumber);
}

function Message_SendToFax()
{
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
/**
 * #DisplayName Message Create New
 * #Description Returns the identity of the message.
 * #param phoneNumber phoneNumber The phonenumber of the subscriber.
 * #param callerPhoneNumber callerPhoneNumber The phonenumber of the calling party.
 * #param mediaObject mediaObject The message that was recoreded.
 * #param messageType messageType The type of the message, ie voice/video/fax/email
 */
function Message_CreateNew(phoneNumber, callerPhoneNumber, mediaObject, messageType) {
    var sender;
    var subject;
    var callerEmailAddress = "";
    var callerName = "";
    var includeSpokenName = false;
    var restrictedNumber;

    var messageId = mas.messageCreateNew();

    //subcriber data
    var emailAddress = mas.subscriberGetStringAttribute(phoneNumber, "mail")[0];
    var preferredLanguage = mas.subscriberGetStringAttribute(phoneNumber, "preferredlanguage")[0];

    //caller data

    if (mas.subscriberExist(callerPhoneNumber)) {
	callerEmailAddress = mas.subscriberGetStringAttribute(callerPhoneNumber, "mail")[0];
	callerName = mas.subscriberGetStringAttribute(callerPhoneNumber, "cn")[0];
	includeSpokenName = mas.subscriberGetBooleanAttribute(callerPhoneNumber, "includespokenname")[0];
	sender = callerName + " (" + callerPhoneNumber + ")" + "<" + callerEmailAddress + ">";

	var spokenName;
	var depositNoQuotaCheck = "no";

	if(Mailbox_IsFullRetrieval(phoneNumber)) {
	    if (!(spokenName = Greeting_GetGreeting(callerPhoneNumber, "SpokenName", messageType)))
		depositNoQuotaCheck = mas.systemGetConfigurationParameter("vva.dialogbehaviour", "depositnoquotacheck");
	}
	else
	    spokenName = Greeting_GetGreeting(callerPhoneNumber, "SpokenName", messageType)

	if (Greeting_IsActive(callerPhoneNumber, "SpokenName", messageType) && includeSpokenName == true && depositNoQuotaCheck == "no")
	    mas.messageSetSpokenNameOfSender(messageId, spokenName);
    }
    else {
	    sender = callerPhoneNumber + "<>";
	    callerName = "John Doe";
    }

    subject = "Voice message from " + callerName;

    //INFO: Both subject and sender shall be handled differently (using systemGetMediaContent). How, will be decided later in the project.

    Message_SetProperty(messageId, "sender", sender);
    Message_SetProperty(messageId, "recipients", emailAddress);
    Message_SetProperty(messageId, "subject", subject); 
    Message_SetProperty(messageId, "type", messageType);
    Message_SetProperty(messageId, "language", preferredLanguage);

    mas.messageAddMediaObject(messageId, mediaObject);

    return messageId;
}
function Message_SetDeliveryTime()
{
}
function Message_Clear()
{
}
/**
 * #DisplayName Message Set Property
 * #Description Sets the specified property of the message. 
 * #param messageId messageId The ID of the message.
 * #param property property The property name.
 * #param value value An array with the values of the property.
 */
function Message_SetProperty(messageId, property, value) {
    return mas.messageSetProperty(messageId, property, value);
}

/**
 * #DisplayName PhoneNumber Get Analyzed Number
 * #Description This function will return the analyzed phone number.
 * #param rule rule The rule to use for the analysis.
 * #param phoneNumber phoneNumber The phone number to analyze.
 * #param callerPhoneNumber callerPhoneNumber The callers phone number.
 */
function PhoneNumber_GetAnalyzedNumber(rule, phoneNumber, callerPhoneNumber) {
   return mas.systemAnalyzeNumber(rule, phoneNumber, callerPhoneNumber);
}
/**
 * #DisplayName PhoneNumber Get SpokenName
 * #Description This function will check if a given phone number belongs to a subscriber and return its spoken name if it is.
 * #param phoneNumber phoneNumber The phone number to check.
 */
function PhoneNumber_GetSpokenName(phoneNumber)
{
}

/**
 * #DisplayName PhoneNumber Get Email Addresses
 * #Description This function will return the email addresses of the phone numbers specified in the array.
 * #param phoneNumberArray phoneNumberArray The array which contains phone numbers.
 */
function PhoneNumber_GetEmailAddresses(phoneNumberArray) {
    var emailArray = new Array();
    var i=0;

    for (i; i<phoneNumberArray.length; i++) {
	emailArray[i] = Subscriber_GetSingleStringAttribute(phoneNumberArray[i], "mail");
    }
    return emailArray;
}
/**
 * #DisplayName PhoneNumber Is A Subscriber
 * #Description This function will return true if the specified phone number belongs to a subscriebr in the system.
 * #param phoneNumber phoneNumber The phone number to check.
 */
function PhoneNumber_IsASubscriber(phoneNumber) {
	 return true; /*return mas.subscriberExist(phoneNumber);*/
}
function PhoneNumber_IsASubscriberThatCanReceiveMessage(phoneNumber)
{
}

/**
 * #DisplayName PhoneNumber Is Family Mailbox Member
 * #Description Returns true the specified phoneNumber is a family mailbox member.
 * #param phoneNumber phoneNumber The phone number to check.
 */
function PhoneNumber_IsFamilyMailboxMember(phoneNumber) {
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
 * #DisplayName PhoneNumber Is IVRtoSMS
 * #Description Returns true the specified phoneNumber equals one of the numbers that has been configured for the IVR to SMS service. 
 * #param phoneNumber phoneNumber The phone number to check.
 */
function PhoneNumber_IsIVRtoSMS(phoneNumber) {
	var ivrNumbers = mas.systemGetConfigurationParameter("vva.incomingcall", "ivrnumbers");
	var number;
	var indexOf = ivrNumbers.indexOf(":");
	while (indexOf != -1) {
		number = ivrNumbers.replace(/[\D]*([\d]*).*/, "$1");
		if (number == phoneNumber) 
			return true;
		else {
			ivrNumbers = ivrNumbers.substring(indexOf+1);
			indexOf = ivrNumbers.indexOf(":");
		}
	}
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
    if (phoneNumber.indexOf(retrievalPrefix) == 0)
	    return true;
	else
	    return false;	
}

function SMS_SendSlamdownInformation(phoneNumber)
{
}
function SMS_SendTemporaryGreetingReminder(phoneNumber, days)
{
}
function SMS_SendToPhoneNumber(phoneNumber, content)
{
}
function Subscriber_GetActiveNotifications(phoneNumber)
{
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
 * #DisplayName Subscriber Get Multi String Attribute
 * #Description This function returns the value of the attribute.
 * #param phoneNumber phoneNumber The phone number.
 * #param attributeName attributeName The name of the attribute to get.
 */
function Subscriber_GetMultiStringAttribute(phoneNumber, attributeName) {
	return mas.subscriberGetStringAttribute(phoneNumber, attributeName);
}

function Subscriber_GetCallerDependentGreetingCaller(phoneNumber)
{
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
function Subscriber_GetNextCallerDependentGreetingCaller(phoneNumber)
{
}
function Subscriber_GetNumberOfDistributionLists(phoneNumber)
{
}
function Subscriber_GetSpokenName(phoneNumber)
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
	if (service == "fax")
	    return mas.subscriberGetBooleanAttribute(phoneNumber, "faxenabled")[0];
		
	if (service == "outdial") {
	    var subXfer = mas.subscriberGetBooleanAttribute(phoneNumber, "subscriberxfer")[0];	
	    var serviceDn = mas.subscriberGetStringAttribute(phoneNumber, "emservicedn");
	    for (var i = 0; i < serviceDn.length; i++) {
		if (subXfer == true && serviceDn[i].search("call_handling") != -1)
    		    return true;
	    }
	}

	if (service == "callednumberlogin")
	    return mas.subscriberGetBooleanAttribute(phoneNumber, "emcnl")[0];	

	if (service == "temporarygreeting")
	    return mas.subscriberGetBooleanAttribute(phoneNumber, "emtmpgrtavailable")[0];	

	var serviceDn = mas.subscriberGetStringAttribute(phoneNumber, "emservicedn");
	for (var i = 0; i < serviceDn.length; i++)
	{
		if ((service == "voice") && (serviceDn[i].search("msgtype_voice") != -1))
    			return true;
       		if ((service == "video") && (serviceDn[i].search("msgtype_video") != -1))
    			return true;
		if ((service == "email") && (serviceDn[i].search("msgtype_email") != -1))
    			return true;
    		if ((service == "sendvoicemessage") && (serviceDn[i].search("sendvoicemessage") != -1))
    			return true;
    		if ((service == "forwardwithcomment") && (serviceDn[i].search("forwardwithcomment") != -1))
    			return true;
		if ((service == "forwardwocomment") && (serviceDn[i].search("forwardwocomment") != -1))
    			return true;
		if ((service == "prerecordedgreeting_tui") && (serviceDn[i].search("prerecordedgreeting_tui") != -1))
    			return true;
        	if (service == "notification") {
            		// Return true if any notification service is active
    	    		if ((serviceDn[i].search("sms_notification") != -1))
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
        	}
	}
	return false;
}
	
function Subscriber_HasValidEmailAddress(phoneNumber)
{
}
/**
 * #Displayname Subscriber Set Single Boolean Attribute
 * #Description Sets an attribute to a value.
 * #param phoneNumber phoneNumber The phone number to check.
 * #param attributeName attributeName The name of the attribute.
 * #param value Value The value for the attribute.
 */
function Subscriber_SetSingleBooleanAttribute(phoneNumber, attributeName, value) {
    mas.subscriberSetBooleanAttribute(phoneNumber, attributeName, new Array(value));
}

/**
 * #Displayname Subscriber Set Single Integer Attribute
 * #Description Sets an attribute to a value.
 * #param phoneNumber phoneNumber The phone number to check.
 * #param attributeName attributeName The name of the attribute.
 * #param value Value The value for the attribute.
 */
function Subscriber_SetSingleIntegerAttribute(phoneNumber, attributeName, value) {
    mas.subscriberSetIntegerAttribute(phoneNumber, attributeName, new Array(value));
}

/**
 * #Displayname Subscriber Set Single String Attribute
 * #Description Sets an attribute to a value.
 * #param phoneNumber phoneNumber The phone number to check.
 * #param attributeName attributeName The name of the attribute.
 * #param value Value The value for the attribute.
 */
function Subscriber_SetSingleStringAttribute(phoneNumber, attributeName, value) {
    mas.subscriberSetStringAttribute(phoneNumber, attributeName, new Array(value));
}

function Subscriber_SetNotification(phoneNumber, notificationName, value)
{
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
 * #Displayname System Get Event message
 * #Description Returns the value of the platform variable _message
 */
function System_GetEventMessage() {
	return "";
}

/**
 * #Displayname System Get Language Code
 * #Description Returns the language code for the element at position index in the configuration parameter LanguageNumbers.
 * #param index index The index in the configuration array LanguageNumbers.
 */
function System_GetLanguageCode(index)
{
    var languageNumbers = mas.systemGetConfigurationParameter("vva.dialogbehaviour", "languagenumbers").replace(/\[| |\]/g, '').split(/,/);
    if (index < languageNumbers.length)
        return languageNumbers[index].substring(0, languageNumbers[index].indexOf(':'));
    else
        return "";
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
function System_GetMediaQualifier(type, value) {
    return util.getMediaQualifier(type, value)
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
    return mas.systemGetServiceRequestParameter;
}

function System_IsIvrToSmsEnabled()
{
}
function System_IsTtsEnabled()
{
}
function System_IsVpimEnabled()
{
}

/**
 * #Displayname System Set Prompt Language
 * #Description Set the specified language as prompt language.
 * #param language Language The language to set.
 * #param voiceVariant VoiceVariant The voice variant for the language.
 * #param videoVariant VideoVariant The video variant for the language.
 */
function System_SetPromptLanguage(language, voiceVariant, videoVariant) {
    mas.systemSetMediaResources(language, voiceVariant, videoVariant);
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
 * #Displayname System Send Service Response
 * #Description Sends a Service Response with the specified status code and status text.
 * #param statusCode statusCode The status code.
 * #param statusText statusText The status text.
 */
function System_SendServiceResponse(statusCode, statusText) {
    return mas.systemSendServiceResponse(statusCode, statusText)
}

/**
 * #Displayname System Send Traffic Event
 * #Description Sends the specifies traffic event.
 * #param eventName eventName The name of the event.
 * #param properties properties The properties for the event specified as <name>=<value>,<name>=<value>, etc.
 */
function System_SendTrafficEvent(eventName, properties) {
return properties;
    var propertyName = new Array();
    var propertyValue = new Array();
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
    
    mas.trafficEventSend(eventName, propertyName, propertyValue);
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
	return now.getUTCSeconds();
}

/**
 * #displayname Time Is Afternoon
 * #description Returns true if the actual time is within the time limits that are specified for an afternoon.
 * #phoneNumber phoneNumber The phone number to check time for, regarding timezone.
 */
function Time_IsAfternoon(phoneNumber) {
    var hhmm;
    if (mas.subscriberExist(phoneNumber)) {
        var now = util.getCurrentTime(mas.subscriberGetStringAttribute(phoneNumber, "subscribertimezone")[0]);
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
        var now = util.getCurrentTime(mas.subscriberGetStringAttribute(phoneNumber, 'subscribertimezone')[0]);
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
        var now = util.getCurrentTime(mas.subscriberGetStringAttribute(phoneNumber, 'subscribertimezone')[0]);
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
        mas.systemGetConfigurationParameter('vva.timesettings', 'morningStart')) {
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
 * #displayname Utility Concat Array
 * #description Returns the concatinated array.
 * #param array1 array1 The first array.
 * #param array2 array2 The second array.
 */
function Utility_ConcatArray(array1, array2) {
  return array1.concat(array2);
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
 * #displayname Utility Create Array
 * #description Creates and returns an empty array.
*/
function Utility_CreateArray() {
  return new Array();
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
 * #displayname Utility Get Array Length
 * #description Returns the length of the array.
 * #param list list The list.
 */
function Utility_GetArrayLength(list) {
  return list.length;
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
 * Internal function. 
 * Takes two arguments, subscribers phone number and callers phonenumber. 
 * Last one is optional and is used in deposit.
*/
function Utility_SecondsLeftOfMessageRecording(phoneNumber, callerPhoneNumber, mailboxId, byteQuota)  {
    var bytesUsed = mas.mailboxGetByteUsage(mailboxId);

    var includeSpokenName = false;
    var maxLengthOfGreeting = "";
    var secondsLeft;
    var bitrateVoice = 64;
    var bitrateVideo = 112;

    if (callerPhoneNumber != "") {
	includeSpokenName = mas.subscriberGetBooleanAttribute(callerPhoneNumber, "includespokenname")[0];
	maxLengthOfGreeting = mas.subscriberGetIntegerAttribute(callerPhoneNumber, "greetingsecmax")[0];
    } 

    if (Call_IsVideo()) {
	if (includeSpokenName == true) {
	    //Video, spoken name included
	    secondsLeft = (((byteQuota - bytesUsed -50000)*8.0/1000) / bitrateVideo/4*3.0) - (maxLengthOfGreeting/3*4.0) + 0.5;
	}
	else {
	    //Video, spoken name not included
	    secondsLeft = (((byteQuota - bytesUsed -50000)*8.0/1000) / bitrateVideo/4*3.0) + 0.5;
	}
    }
    else {
	if (includeSpokenName == true) {
	    //Voice, spoken name included
	    secondsLeft = (((byteQuota - bytesUsed -50000)*8.0/1000) / bitrateVoice/4*3.0) - (maxLengthOfGreeting/3*4.0) + 0.5;
	}
	else {
	    //Voice spoken name not included
	    secondsLeft = (((byteQuota - bytesUsed -50000)*8.0/1000) / bitrateVoice/4*3.0) + 0.5;
	}
    }
    return secondsLeft;
}

/**
 * #displayname Utility Set Global Object
 * #description Sets the specified global object variable to the specified value.
 * #param name name The name of the variable in GlobalObject.
 * #param value value The value for the specified variable.
*/
function Utility_SetGlobalObject(name, value) {
    if (name == "Subscriber") 
        GlobalObject.Subscriber = value;
    if (name == "LoggedIn") 
        GlobalObject.LoggedIn = value;
    if (name == "MainMenuValid") 
        GlobalObject.MainMenuValid = value;
    if (name == "PreviousMenuValid") 
        GlobalObject.PreviousMenuValid = value;
    if (name == "WorkWithVoiceGreetings") 
        GlobalObject.WorkWithVoiceGreetings = value;
    if (name == "UtcSecondsAtConnect") 
        GlobalObject.UtcSecondsAtConnect = value;
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
 * #displayname Utility String To Array
 * #description Converts the input string to an array.
 * #param str str The string to be converted.
 * #param separator separator The caracter that separates the array into separate elements.
*/
function Utility_StringToArray(str, separator) {
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

function XMP_GetPagingString()
{
}
function XMP_GetPhoneNumber()
{
}
function XMP_GetSubscriber()
{
}

var MenuArray = new Array();
var GlobalObject = { Subscriber:'', LoggedIn:false, MainMenuValid:false, PreviousMenuValid:false, WorkWithVoiceGreetings:false, UtcSecondsAtConnect:0 };

// ********** MasInterface stubs *********************
var numberMedia = new NumberMediaQualifier();
function NumberMediaQualifier() {
}

var stringMedia = new StringMediaQualifier();
function StringMediaQualifier() {
}

var time12Media = new Time12MediaQualifier();
function Time12MediaQualifier() {
}

var time24Media = new Time24MediaQualifier();
function Time24MediaQualifier() {
}

var mediaObjectMedia = new MediaObjectMediaQualifier();
function MediaObjectMediaQualifier() {
}

var mas = new PlatformAccess();

function PlatformAccess() {
    this.mailboxGetFolder = mailboxGetFolder;
    this.mailboxGetMessageUsage = mailboxGetMessageUsage;
    this.mailboxGetByteUsage = mailboxGetByteUsage;
    this.mailboxGetMessages = mailboxGetMessages;
    this.mailboxGetMessageList = mailboxGetMessageList;
    this.mailboxGetMessageSubList = mailboxGetMessageSubList;
    this.mailboxGetNumberOfMessages = mailboxGetNumberOfMessages;
    this.messageAddMediaObject = messageAddMediaObject;
    this.messageContentLength = messageContentLength;
    this.messageContentSize = messageContentSize;
    this.messageCreateNew = messageCreateNew;
    this.messageGetContent = messageGetContent;
    this.messageGetMediaObject = messageGetMediaObject;
    this.messageGetMediaProperties = messageGetMediaProperties;
    this.messageGetProperty = messageGetProperty;
    this.messageGetSpokenNameOfSender = messageGetSpokenNameOfSender;
    this.messagePrint = messagePrint;
    this.messageSetProperty = messageSetProperty;
    this.messageStore = messageStore;
    this.subscriberExist = subscriberExist;
    this.subscriberGetGreeting= subscriberGetGreeting;
    this.subscriberGetMailbox = subscriberGetMailbox;
    this.subscriberGetBooleanAttribute = subscriberGetBooleanAttribute;
    this.subscriberGetIntegerAttribute = subscriberGetIntegerAttribute;
    this.subscriberGetStringAttribute = subscriberGetStringAttribute;
    this.subscriberGetSpokenName = subscriberGetSpokenName;
    this.subscriberSetBooleanAttribute = subscriberSetBooleanAttribute;
    this.subscriberSetIntegerAttribute = subscriberSetIntegerAttribute;
    this.subscriberSetStringAttribute = subscriberSetStringAttribute;
    this.subscriberSetSpokenName = subscriberSetSpokenName;
    this.subscriberSetGreeting = subscriberSetGreeting;
    this.systemAnalyzeNumber = systemAnalyzeNumber;
    this.systemGetConfigurationParameter = systemGetConfigurationParameter;
    this.systemGetMediaContent = systemGetMediaContent;
    this.systemGetMediaContentIds = systemGetMediaContentIds;
    this.systemGetServiceRequestParameter = systemGetServiceRequestParameter;
    this.systemGetSubscribers = systemGetSubscribers;
    this.systemSendServiceResponse = systemSendServiceResponse;   
    this.systemSetMediaResources = systemSetMediaResources;
    this.systemSetProperty = systemSetProperty;
    this.trafficEventSend = trafficEventSend;
}

function mailboxGetFolder(mailboxId, folderName) {
    return 2;
}

function mailboxGetMessageUsage(mailboxId) {
    return 2;
}

function mailboxGetByteUsage(mailboxId) {
    return 1024;
}


function mailboxGetMessageList(folderId, types, states, priorities, orders, timeOrder) {
	if ((types == "voice") && (states == "new"))
		return 1;
	if ((types == "voice") && (states == "read"))
		return 2;
	if ((types == "fax") && (states == "new"))
		return 3;

	return 3;
}

function mailboxGetMessageSubList(messageListId, types, states, priorities, orders, timeOrder) {
	if ((types == "voice") && (states == "new"))
		return 1;
	if ((types == "voice") && (states == "read"))
		return 2;
	if ((types == "video") && (states == "new"))
		return 3;
	if ((types == "video") && (states == "read"))
		return 4;
	if ((types == "fax") && (states == "new"))
		return 5;
	if ((types == "fax") && (states == "read"))
		return 6;
	if (types == "email")
		return 7;

	return 8;
}

function mailboxGetMessages(messageListId) {
	if (messageListId == 1)
		return [1]; //new voice
	if (messageListId == 2)
		return [3]; // old voice
	if (messageListId == 3)
		return [2]; //new video
	if (messageListId == 4)
		return []; // old video
	if (messageListId == 5)
		return [4]; // new fax
	if (messageListId == 6)
		return []; // old fax
	if (messageListId == 7)
		return [1]; // email
	
	return [1,2,3,4];
}

function mailboxGetNumberOfMessages(messageListId, types, states, priorities) {
	if (states == 'new'){
		var result = 0;
		var voice = 1;
		var video = 0;
		var fax = 0;
		var email = 0;
		if (types.search('voice') != -1)
			result = result + voice;
		if (types.search('video') != -1)
			result = result + video;
		if (types.search('fax') != -1)
			result = result + fax;
		if (types.search('email') != -1)
			result = result + email;
		return result;
	}
	if (states == 'old')
		return 3;
	if (states == 'deleted')
		return 3;
	return 4;
}
function messageAddMediaObject(messageId, mediaObject) {
    return true;
}
function messageCreateNew(){
    return 1;
}

function messageGetContent(messageId){
	if (messageId == 1)
		return [11,12];
	if (messageId == 2)
		return [21];
	if (messageId == 3)
		return [31];
	if (messageId == 4)
		return [41];
	if (messageId == 5)
		return [51,52,53];
	return 0;
}

function messageContentLength(messageContentId, type) {
	if (messageContentId == 11)
		return 4000;
	if (messageContentId == 12)
		return 7000;

		return 3000;
}

function messageContentSize(messageContentId) {
	return 7000;
}


function messageGetMediaObject(messageContentId){
	
	if (messageContentId == 11)
		return "Messages/voice1_att1.wav";
	if (messageContentId == 12)
		return "Messages/voice1_att2.wav";
	if (messageContentId == 21)
		return "Messages/video1.wav";
	if (messageContentId == 31)
		return "Messages/voice2_urgent.wav";
	if (messageContentId == 41)
		return "Messages/fax1.wav";

	return "Messages/unknown.wav";
}

function messageGetMediaProperties(messageContentId){
	if ((messageContentId == 11) || (messageContentId == 12) || (messageContentId == 31))
		return "audio/wav";
	if (messageContentId == 21)
		return "video/quicktime";
	if (messageContentId == 41)
		return "image/tiff";
	if (messageContentId == 51)
		return "audio/wav";
	if (messageContentId == 52)
		return "audio/wav";
	if (messageContentId == 53)
		return "audio/wav";

	
	return "";
}

function messageGetProperty(messageId, property){
	if (property == "state")
		return ["old"];
		
	if (property == "deliveryreport")
		return ["true"];
	
	if (property == "deliverystatus")
		return ["printfailed"];

	if (property == "forwarded")
		return ["false"];

	if (property == "urgent") {
		if (messageId == 3)
			return ["true"];
		return ["false"];
	}
		
	if (property == "confidential")
		return ["false"];
		
	if (property == "sender")
		return ["Tomas Andersen (1022) <tomas@mobeon.com>"];
		//return ["1022 <>"];
		//return ["Internet Mail Delivery <postmaster@skiroule.mvas.su.erm.abcxyz.se>"];
		//return ["Tomas A <tomas@mvas.su.erm.abcxyz.se>"];
		//return ["FAX=1780100010@faxe.telephony.su.erm.abcxyz.se"];
		//return ["Number Withheld <>"];

	if (property == "type"){
		if (messageId == 1)
			return ["voice"];
		if (messageId == 2)
			return ["video"];
		if (messageId == 3)
			return ["voice"];
		if (messageId == 4)
			return ["fax"];
		if (messageId == 5)
			return ["email"];
		return ["unknown message Id. (property = type)"];
	}
		
	if (property == "recipients"){
		//return ["b@x.com", "c@x.com", "martin.ringbert@mobeon.com"];
		return ["b@x.com"];
	}
	
	if (property == "secondaryrecipients"){
		//return ["d@x.com", "martin.ringbert@mobeon.com", "e@x.com"];
		return [];
	}
	
	if (property == "replytoaddr"){
		return [];
		//return ["d@x.com"];
	}

	return ["unknown property"];
}

function messageGetSpokenNameOfSender(messageId) {
	if (messageId == 1)
		return "Messages/tomas.wav";
	return "Messages/spokenname.wav";
}

function messagePrint(messageId, destination) {
	return;
}

function messageSetProperty(messageId, property, value) {
	return true;
}

function messageStore(messageId) {
	return false;
}

function subscriberExist(phoneNumber) {
    if (phoneNumber.match(/060999.*/))
        return true;
    else
        return false;
}

function subscriberGetBooleanAttribute(phoneNumber, attrName) {
    if(attrName == "autoplay")
		return [true];
    if(attrName == "callerxfer")
    	return [true];
    if(attrName == "callerxfertocoverage")
    	return [true];
    if(attrName == "emcnl")
    	return [true];
    if(attrName == "emtmpgrtavailable")
    	return [true];
    if(attrName == "fastloginavailable")
    	return [true];
    if(attrName == "fastloginenabled")
    	return [true];
    if(attrName == "faxenabled")
		return [true];
    if(attrName == "includespokenname")
    	return [false];
    if(attrName == "passwordskipavailable")
    	return [true];
    if(attrName == "passwordskipenabled")
    	return [true];
    if(attrName == "subscriberxfer")
		return [true];
    if(attrName == "urgentMsgplay")
		return [true];
	   
    return [false];
}

function subscriberGetMailbox(phoneNumber) {
    return 1;
}

function subscriberGetGreeting(phoneNumber, greetingType, mediaType, cdgNumber) {
    if(greetingType == "AllCalls")
	return "Media/English/voice/AllCalls.wav";
    if(greetingType == "NoAnswer")
	return "Media/English/voice/NoAnswer.wav";
    if(greetingType == "Busy")
    	return "Media/English/voice/Busy.wav";
    if(greetingType == "OutOfHours")
    	return "Media/English/voice/OutOfHours.wav";
    if(greetingType == "Extended_Absence")
    	return "Media/English/voice/ExtendedAbsence.wav";
    if(greetingType == "CDG")
	return "Media/English/voice/CDG.wav";
    if(greetingType == "Temporary")
	return "Media/English/voice/Temporary.wav";
    else
	return "";
}

function subscriberGetStringAttribute(phoneNumber, attrName) {
	if(attrName == "activegreetingid")
    	return ["AllCalls, SpokenName"];
    if(attrName == "cn")
    	return ["Martin"];
    if(attrName == "emftl")
    	//return ["-1,F,SPO:M,ACG:O,NOT:O,PIN:M"];
		//return ["03,F,SPO:O,ACG:O"];
		//return ["00,F,SPO:O,ACG:O"];
		//return ["01,F,SPO:O,ACG:O"];
		//return ["03,F"];
    	//return ["03,F,PIN:O,ACG:O,NOT:O,PIN:M"];
    	return ["04,F,PIN:O,NOT:O"];
	if(attrName == "emgreetinginfo")
    	return ["bu:R,sn:12R,na:4Fnoanswergreeting.wav,ac:4AR,oh:5R,ea:8R,te:45R"];
	if(attrName == "emmsgplayorder")
		return ["FIFO,LIFO"];
	if(attrName == "emtmpgrt")
    	return ["2005-10-01 10:00:00;2005-11-01 10:00:00"];
	if (attrName == "emservicedn")
		return ["x msgtype_email x", "x msgtype_voice x", "x msgtype_video x", "sendvoicemessage", "forwardwithcomment", "forwardwocomment",
			"prerecordedgreeting_tui", "call_handling"];
	if (attrName == "emtuiaccess")
    	return ["std"];
	if (attrName == "emtuiblockedmenu")
		return ["SendVoiceMessage", "NoAnswerGreeting", "BusyGreeting", 
		"OutOfHoursGreeting", "distributionlist", "attcallcoverage", "notification", "messageplayoptions", "timeformat", "defaultfaxnumber", "setpreferredlanguagemenu" ];
	if (attrName == "emvuiaccess")
    	return ["none"];
    if(attrName == "inhoursdow")
    	return ["1"];
    if(attrName == "inhoursend")
    	return ["1700"];
    if(attrName == "inhoursstart")
    	return ["0800"];
    if(attrName == "mail")
    	return ["martin.ringbert@mobeon.com"];
    if(attrName == "mailhost")
    	return ["mobeon.com"];
	if(attrName == "mailuserstatus")
		return ["active"];
	if(attrName == "messageplayvoice")
		return ["both"];
	if(attrName == "messageplayvideo")
		return ["both"];
	if(attrName == "messageplayemail")
		return ["body"];
	if(attrName == "preferredlanguage")
    	return ["en"];
    if(attrName == "umpassword")
    	return ["1111"];
	
	return [""];
}

function subscriberGetIntegerAttribute(phonenumber, attrName) {
	if(attrName == "badlogincount")
    	return [0];
    if(attrName == "dlmax")
    	return [3];
    if(attrName == "emnoOfmailquota")
    	return [-1];
    if(attrName == "emreadlevel")
    	return [-1];
    if(attrName == "eomsgwarning")
    	return [15];
    if(attrName == "greetingsecmax")
    	return [60];
    if(attrName == "includespokenname")
    	return [0];
    if(attrName == "mailquota")
    	return [1000];
    if(attrName == "maxloginlockout")
    	return [6];    
    if(attrName == "msglenmaxvoice")
    	return [180];   
    if(attrName == "passwdlenmax")
    	return [8];     
    if(attrName == "passwdlenmin")
    	return [4];
    if(attrName == "userlevel")
    	return [70];
     	
	return [-1];  
}

function subscriberGetSpokenName(phonenumber, mediaType) {
    return "Media/English/voice/SpokenName.wav";
}
function subscriberSetStringAttribute(phoneNumber, attrName, attrValues) {
}

function subscriberSetIntegerAttribute(phoneNumber, attrName, attrValues) {
}

function subscriberSetBooleanAttribute(phoneNumber, attrName, attrValues) {
}

function subscriberSetSpokenName(phoneNumber, greetingRef) {
}

function subscriberSetGreeting(phoneNumber, greetingName, callType, callerId, greetingRef) {
}

function systemAnalyzeNumber(rule, phoneNumber, callerPhoneNumber) {
    if (rule == "INBOUNDCALL") {
        if (phoneNumber.match(/0077.*/))
            return phoneNumber;
        else if (phoneNumber == '')
            return phoneNumber;        
        else
	        return "060" + phoneNumber;
	}
    else if (rule == "RETRIEVALPREFIXRULE")
        return phoneNumber.replace(/0077(.*)/, "$1");
    else
	    return phoneNumber;
}

function systemGetConfigurationParameter(group, parameterName) {
	if (parameterName == "systemunpausenykey")
		return "no";		
	else if (parameterName == "languagenumbers")
		return "[en:1, sv:2, es:3]";		
	else if (parameterName == "timeouttomainmenu")
		return "no";		
	else if (parameterName == "initpinanyphone")
		return "yes";
	else if (parameterName == "loginsetlanguages")
		return "no";
	else if (parameterName == "maxloginattempts")
		return "3";
	else if (parameterName == "callednumberretrprefixrule")
		return "RETRIEVALPREFIXRULE";
	else if (parameterName == "callednumberrule")
		return "INBOUNDCALL";
	else if (parameterName == "callingnumberrule")
		return "INBOUNDCALL";
	else if (parameterName == "clitosmsrule")
		return "CLITOSMSCALL";
	else if (parameterName == "cutthroughpagingnumberrule")
		return "CUTTHROUGHPAGING";
	else if (parameterName == "echonumberfromdtmfrule")
		return "ECHONUMBER";
	else if (parameterName == "echonumberfrommurrule")
		return "ECHONUMBER";
	else if (parameterName == "ivrtosmsdestinationnumberrule")
		return "IVRTOSMS";
	else if (parameterName == "ivrtosmscallbacknumberrule")
		return "IVRTOSMS";
	else if (parameterName == "redirectingnumberrule")
		return "INBOUNDCALL";
	else if (parameterName == "slamdownnumberrule")
		return "SLAMDOWNCALL";
	else if (parameterName == "subscribernumberrule")
		return "INBOUNDCALL";
	else if (parameterName == "outdialnotificationani")
		return "0601610220";
	else if (parameterName == "messagepartsplayorder")
		return "status,from,date,lll,body";
	else if (parameterName == "admingreetingdnis")
		return "0604444";
	else if (parameterName == "aniequalsdepositid")
		return "retrieval";
	else if (parameterName == "depositdnis")
		return "0605555";
	else if (parameterName == "directdepositdnis")
		return "0605151";		
	else if (parameterName == "faxprintdnis")
		return "0606666";
	else if (parameterName == "ivrnumbers")
		return "[0601111:en, 0602222:sv]";
	else if (parameterName == "retrievaldnis")
		return "0607777";
	else if (parameterName == "retrievalprefix")
		return "0077";
	else if (parameterName == "retrievalallowedatdepositdnis")
		return "no";
	else if (parameterName == "autoplayofsaved")
		return "true";
	else if (parameterName == "autoplaydelay")
		return "3";
	else if (parameterName == "morningstart")
		return "0200";
	else if (parameterName == "afternoonstart")
		return "1200";
	else if (parameterName == "eveningstart")
		return "1800";
	else if (parameterName == "forwardtime")
		return "3";
	else if (parameterName == "rewindtime")
		return "3";		
	else
		return "unknown configuration parameter";
}

function systemGetMediaContent(type, mediaContentId, qualifier) {
	if (type == "FunGreeting") {
	    if (mediaContentId == 1)
		return "Media/English/voice/fun1.wav";
	    if (mediaContentId == 2)
		return "Media/English/voice/fun2.wav";
	    if (mediaContentId == 3)
		return "Media/English/voice/fun3.wav";
	}
	if (mediaContentId == 2000)
		return "Media/English/voice/VVA_0009.wav";
	if (mediaContentId == 2001)
		return "Media/English/voice/VVA_0008.wav";
	if (mediaContentId == 2002)
		return "Media/English/voice/VVA_0007.wav";
	if (mediaContentId == 2003)
		return "Media/English/voice/VVA_0091.wav";
	if (mediaContentId == 2004)
		return "Media/English/voice/VVA_0127.wav";
	if (mediaContentId == 2005)
		return "Media/English/voice/VVA_0137.wav";
	if (mediaContentId == 2006)
		return "Media/English/voice/VVA_0137.wav";
	if (mediaContentId == 2100)
		return "Media/English/voice/VVA_0277.wav";
	if (mediaContentId == 2101)
		return "Media/English/voice/VVA_0286.wav";
	if (mediaContentId == 2102)
		return "Media/English/voice/VVA_0297.wav";
	if (mediaContentId == 2103)
		return "Media/English/voice/VVA_0298.wav";
	if (mediaContentId == 2104)
		return "Media/English/voice/VVA_0303.wav";
	if (mediaContentId == 2105)
		return "Media/English/voice/VVA_0305.wav";
	if (mediaContentId == 2106)
		return "Media/English/voice/VVA_0307.wav";
	if (mediaContentId == 2123)
		return "Media/English/voice/VVA_0364.wav";
	if (mediaContentId == 2124)
		return "Media/English/voice/VVA_0365.wav";
	if (mediaContentId == 2125)
		return "Media/English/voice/VVA_0366.wav";
	if (mediaContentId == 2126)
		return "Media/English/voice/VVA_0367.wav";
	if (mediaContentId == 2127)
		return "Media/English/voice/VVA_0379.wav";
	if (mediaContentId == 2128)
		return "Media/English/voice/VVA_0379.wav";
	if (mediaContentId == 2200)
		return "Media/English/voice/VVA_1000.wav";
	if (mediaContentId == 2201)
		return "Media/English/voice/VVA_1001.wav";
	if (mediaContentId == 2202)
		return "Media/English/voice/VVA_1002.wav";
	if (mediaContentId == 2203)
		return "Media/English/voice/VVA_1003.wav";
	if (mediaContentId == 2204)
		return "Media/English/voice/VVA_1004.wav";
	if (mediaContentId == 2207)
		return "Media/English/voice/VVA_1007.wav";


	if (mediaContentId.toString().length == 1)
		return "Media/English/voice/VVA_000" + mediaContentId.toString() + ".wav";
	if (mediaContentId.toString().length == 2)
		return "Media/English/voice/VVA_00" + mediaContentId.toString() + ".wav";
	if (mediaContentId.toString().length == 3)
		return "Media/English/voice/VVA_0" + mediaContentId.toString() + ".wav";
	if (mediaContentId.toString().length == 4)
		return "Media/English/voice/VVA_" + mediaContentId.toString() + ".wav";

	return "Media/English/voice/Unkown.wav";
}

function systemGetMediaContentIds(type, qualifier) {
    return [1,2,3];
}
function systemGetServiceRequestParameter(name) {
}
function systemGetSubscribers(attribute, value) {
    return new Array("1922");
}
function systemSendServiceResponse(statusCode, statusText) {
}     

function systemSetProperty(name, value) {
}
function systemSetMediaResources(language, voiceVariant, videoVariant) {
}

function trafficEventSend(eventName, propertyName, propertyValue) {
}


var util = new PlatformAccessUtil();

function PlatformAccessUtil() {
	this.formatTime = formatTime;
	this.getCurrentTime = getCurrentTime;
	this.getMediaObject = getMediaObject;
	this.getMediaQualifier = getMediaQualifier;
	this.systemSetOffset = systemSetOffset;
}

function getCurrentTime(timezone) {
    return "2005-11-18 13:30:00";
}

function formatTime(vvaTime, pattern) {
	if (pattern == null)
		return 10000000000;
    if (pattern == "HHmm")
        return "1330";
    else
        return vvaTime;
}

function getMediaObject(value) {
    return new Object();
}

function getMediaQualifier(type, value) {
    return new Object();
}

function systemSetOffset(mediaObject, offset) {
	
}
