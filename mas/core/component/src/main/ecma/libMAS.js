/**
 * #DisplayName Get Call Type
 * #Description Returns the type of incoming call based on incoming call parameters (ANI, DNIS and RDNIS) and configuration parameters. The type of call can be Retrieval, Deposit, DepositOrRetrieval or EnterDepositNumber. 
 */
 function Call_GetTypeOfCall ()
{
}

/**
 * #DisplayName Is Admin Greeting
 * #Description Returns true if the Called Number (DNIS) is equal to the phone number that is specified for greeting administration.
 */
function Call_Is_AdminGreeting ()
{
	return true;
}

/**
 * #DisplayName Is Fax
 * #Description Returns true if the call is destinated to the Get Fax service, that is, the DNIS equals the number that is configured for the Get Fax service.
 */
function Call_Is_GetFax()
{
}

/**
 * #DisplayName Is IVRtoSMS
 * #Description Returns true if the call is destinated to the IVR to SMS service, that is, the DNIS equals one of the numbers that has been configured for the IVR to SMS service. 
 */
function Call_Is_IVRtoSMS()
{
}

/**
 * #DisplayName Is OutdialNotificationAni
 * #Description Returns true if the ANI of the incoming call is equal to the ANI that is configured for outdial notification calls.
 */
function Call_Is_OutdialNotificationAni()
{
}

/**
 * #DisplayName Is Redirected
 * #Description Returns true if the call has been redirected (RDNIS exist).
 */
function Call_Is_Redirected()
{
}

/**
 * #DisplayName Is RedirectReasonBusy
 * #Description Returns true if the call has been redirected due to that the subscriber is already talking on the phone (busy).
 */
function Call_Is_RedirectReasonBusy()
{
}

/**
 * #DisplayName Is RedirectReasonNoAnswer
 * #Description Returns true if the call has been redirected due to that the subscriber did not answer the phone (noAnswer).
 */
function Call_Is_RedirectReasonNoAnswer()
{
}

/**
 * #DisplayName Is Video
 * #Description Returns true if the call is a video call.
 */
function Call_Is_Video()
{
}

/**
 * #DisplayName Is Voice
 * #Description Returns true if the call is a voice call.
 */
function Call_Is_Voice()
{
}

/**
 * #DisplayName GetDistributionList
 * #Description Retrieves a distribution list.
 * #param listNumber ListNumber The number of the distribution list to retrieve.
 */
function DistributionList_Get_List(listNumber)
{
	//alert("This is the \"Get List\"-method.\nThank you for requesting list \"" + listNumber + "\"");
}

/**
 * #DisplayName GetDistributionListMember
 * #Description Retrieves a distribution list member.
 * #param memberNumber memberNumber The number of the distribution list member to retrieve.
 */
function DistributionList_Get_Member(memberNumber)
{
}

/**
 * #DisplayName Distribution Has Name Recorded
 * #Description
 * #param memberNumber memberNumber The number of the distribution list member.
 */
function DistributionList_Has_NameRecorded(listNumber)
{
}

/**
 * #DisplayName Distribution List Is Empty
 * #Description Returns true if the distribution list is empty.
 * #param listNumber ListNumber The number of the distribution list to check.
 */
function DistributionList_Is_Empty(listNumber)
{
}
function DistributionList_Is_Full(listNumber)
{
}
function DistributionList_Is_MemberAlreadyInTheList(phoneNumber)
{
}
function istributionList_Is_NumberAnExistingList(listNumber)
{
}
function DistributionList_Is_NumberAValidList(listNumber)
{
}
function DistributionList_Set_AddMember(phoneNumber)
{
}
function DistributionList_Set_CreateNew()
{
}
function DistributionList_Set_MemberToDeleted()
{
}
function DistributionList_Set_Name(name)
{
}
function DistributionList_Set_ToDeleted(listNumber)
{
}
function FamilyMailbox_Get_ListOfMembers(phoneNumber)
{
}
function FamilyMailbox_Get_ListWithNumberOfMessages(phoneNumber)
{
}
function FTL_Does_MandatoryFunctionsExist(phoneNumber)
{
}
function FTL_Does_OptionalFunctionsExist(phoneNumber)
{
}
function FTL_Get_FirstFunction(phoneNumber)
{
}
function FTL_Get_NextFunction(phoneNumber)
{
}
function TFL_Is_Completed(phoneNumber)
{
}
function FTL_Is_CurrentFunctionGreeting(phoneNumber)
{
}
function FTL_Is_CurrentFunctionMandatory(phoneNumber)
{
}
function FTL_Is_CurrentFunctionName(phoneNumber)
{
}
function FTL_Is_CurrentFunctionNotification(phoneNumber)
{
}
function FTL_Is_CurrentFunctionOptional(phoneNumber)
{
}
function FTL_Is_CurrentFunctionPIN(phoneNumber)
{
}
function FTL_Is_CurrentFunctionTheLast(phoneNumber)
{
}
function FTL_Is_NotUsingOwnPhone(phoneNumber)
{
}
function FTL_Set_PartAsPerformed(phoneNumber, part)
{
}
function Greeting_Get_FirstFunGreeting()
{
}
function Greeting_Get_Greeting()
{
}
function Greeting_Get_NextFunGreeting()
{
}
function Greeting_Has_TemporaryActivationTimePassed()
{
}
function Greeting_Has_TemporaryDeactivationTimePassed()
{
}
function Greeting_Is_Active()
{
}
function Greeting_Is_Recorded()
{
}function Greeting_Is_RecordedForCaller()
{
}
function Greeting_Set_StoreRecording()
{
}
function Greeting_Set_TemporaryAutomaticDeactivation()
{
}
function Greeting_Set_ToActive()
{
}
function Greeting_Set_ToDeleted()
{
}
function Greeting_Set_ToInactive()
{
}
function Login_Get_CosWelcomeGreeting()
{
}
function Login_Is_MaxAttemptsReached()
{
}
function Login_Is_VideoCallButNoVideoService()
{
}
function Login_Is_VoiceCallButNoVoiceService()
{
}
function Login_Shall_PhoneNumberBeFetched()
{
}
function Login_Shall_PinBeFetched()
{
}
function Login_Shall_PreferredLanguageBeSet()
{
}
/**
 * #DisplayName Get Message List
 * #Description Returns true if there are any new messages.
 * #param mailboxID mailboxID The ID of the mailbox.
 * #param inbox inbox For example "Inbox".
 * #param type type Valid values: "voice", "video", "fax", "email", "all".
 * #param status status Valid values: "new", "read", "deleted", "all".
 * #param prio prio Valid values: "urgent", "nonurgent", "all".
 * #param order order Valid values: "FIFO", "LIFO".
 */
function Mailbox_Get_MessageList(mailboxID, inbox, type, status, prio, order)
{
}
function Mailbox_Get_Message()
{
}
function Mailbox_Is_FilledOverWarningLevel()
{
}
function Mailbox_Is_Full()
{
}
function Mailbox_Set_ToEmpty()
{
}
function Menu_Get_Current()
{
}
function Menu_Get_Previous()
{
}
function Menu_Is_Allowed()
{
}
function Menu_Reset_Choices()
{
}
function Menu_Set_Current()
{
}
function Menu_Set_Next()
{
}
function Message_Are_AllRecepientsOK()
{
}
function Message_Are_NoRecepientsOK()
{
}
function Message_Are_SomeOfTheRecepientsSubscribers()
{
}
function Message_Are_SomeRecepientsOK()
{
}
function Message_Get_CallbackNumber()
{
}
function Message_Get_FaxNumber()
{
}
function Message_Get_FirstPlayableBodyPart()
{
}
function Message_Get_NextPlayableBodyPart()
{
}
function Message_Get_NumberOfAttachments()
{
}
function Message_Get_NumberOfFaxPages()
{
}
function Message_Get_NumberOfRecipients()
{
}
function Message_Get_PreviousPlayableBodyPart()
{
}
function Message_Get_SenderPhoneNumber()
{
}
function Message_Get_SenderSpokenName()
{
}
function Message_Get_Subject()
{
}
function Message_Get_Type()
{
}
function Message_Has_DistributionListRecipients()
{
}
function Message_Has_TooManyRecipients()
{
}
function Message_Is_Confidential()
{
}
function Message_Is_DateFridayLastWeek()
{
}
function Message_Is_DateMondayLastWeek()
{
}
function Message_Is_DateSaturdayLastWeek()
{
}
function Message_Is_DateSundayLastWeek()
{
}
function Message_Is_DateThursdayLastWeek()
{
}
function Message_Is_DateToday()
{
}
function Message_Is_DateTuesdayLastWeek()
{
}
function Message_Is_DateWednesdayLastWeek()
{
}
function Message_Is_DateYesterday()
{
}
function Message_Is_Deleted()
{
}
function Message_Is_Forwarded()
{
}
function Message_Is_FutureDeliverySet()
{
}
function Message_Is_MaxLengthRecorded()
{
}
function Message_Is_NoneOfTheRecepientsSubscribers()
{
}
function Message_Is_Printable()
{
}
function Message_Is_Undeliverable_PrintFailed()
{
}
function Message_Is_Undeliverable_SendFailed()
{
}
function Message_Is_Urgent()
{
}
function Message_Is_VoiceAttachedToTheFax()
{
}
function Message_Send_ToFax()
{
}
function Message_Send_ToMailbox()
{
}
function Message_Set_CallbackNumber()
{
}
function Message_Set_CreateNew()
{
}
function Message_Set_DeliveryTime()
{
}
function Message_Set_LastRecipientToDeleted()
{
}
function Message_Set_ToCleared()
{
}
function Message_Set_ToConfidential()
{
}
function Message_Set_ToDeleted()
{
}
function Message_Set_ToNew()
{
}
function Message_Set_ToNonConfidential()
{
}
function Message_Set_ToNonUrgent()
{
}
function Message_Set_ToRead()
{
}
function Message_Set_ToRecovered()
{
}
function Message_Set_ToUrgent()
{
}
function PhoneNumber_Get_AnalyzedNumber(rule, phoneNumber, callerPhoneNumber)
{
}
/**
 * #DisplayName GetSpokenName
 * #Description This function will check if a given phone number belongs to a subscriber and return its spoken name if it is.
 * #param phoneNumber PhoneNo The phone number to check.
 */
function PhoneNumber_Get_SpokenName(phoneNumber)
{
}
function PhoneNumber_Is_aSubscriber()
{
}
function PhoneNumber_Is_aSubscriberThatCanReceiveMessage()
{
}
function PhoneNumber_Is_FamilyMailboxMember()
{
}
function PhoneNumber_Is_FamilyMailboxOwner()
{
}
function SMS_Send_SlamdownInformation()
{
}
function SMS_Send_TemporaryGreetingReminder()
{
}
function SMS_Send_ToPhoneNumber()
{
}
function Subscriber_Get_ActiveNotifications()
{
}
/**
 * #DisplayName Get Attribute
 * #Description This function returns the value of the attribute.
 * #param attributeName attributeName The phone number to check.
 */
function Subscriber_Get_Attribute(attributeName)
{
  /*switch (attributeName)
  {
    case "emMsgPlayOrder": result = "FIFO,LIFO"; break;
    default: "";
  }*/
  return "FIFO,LIFO";
}
function Subscriber_Get_CallerDependentGreetingCaller()
{
}
function Subscriber_Get_MessageTypesAllowed()
{
}
function Subscriber_Get_NextCallerDependentGreetingCaller()
{
}
function Subscriber_Get_NumberOfDistributionLists()
{
}
function Subscriber_Get_SpokenName()
{
}
function Subscriber_Has_Service()
{
}
function Subscriber_Has_ValidEmailAddress()
{
}
function Subscriber_Is_SpokenNameRecorded()
{
}
/**
 * #displayname Set attribute
 * #description Sets an attribute to a value.
 * #param attribute Attribute The name of the attribute to set.
 * #param value Value The value to set the attribute to.
 */
function Subscriber_Set_Attribute(attribute, value)
{
}
function Subscriber_Set_NotificationTypeState()
{
}
function System_Get_Languges()
{
}
/**
 * #displayname Get List Length
 * #description Returns the length of the list.
 * #param list list The list.
 */
function System_Get_ListLength(list)
{
  return list.length;
}
function System_Get_ListElement(list, elem)
{
  return list.length;
}
function System_Get_Prompt()
{
}
function System_Get_PromptForTtsLanguage()
{
}
function System_Is_IvrToSmsEnabled()
{
}
function System_Is_TtsEnabled()
{
}
function System_Is_VpimEnabled()
{
}
function System_Set_PromptLanguage()
{
}
function Time_Is_Afternoon()
{
}
function Time_Is_Evening()
{
}
function Time_Is_Morning()
{
}
function Time_Is_OutsideBusinessHours()
{
}
function Time_Is_Valid()
{
}
function Tts_Set_Language()
{
}
function XMP_Get_PagingString()
{
}
function XMP_Get_PhoneNumber()
{
}
function XMP_Get_Subscriber()
{
}

