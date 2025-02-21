package com.mobeon.masp.execution_engine.platformaccess;


import java.net.URI;

import com.abcxyz.messaging.authentication.api.AuthenticationException;
import com.abcxyz.messaging.authentication.crp.CrpDirectInterface;
import com.abcxyz.messaging.common.mcd.Modification;
import com.abcxyz.messaging.common.mcd.Profile;
import com.mobeon.common.trafficeventsender.TrafficEvent;
import com.mobeon.common.trafficeventsender.TrafficEventSenderException;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediacontentmanager.IMediaQualifier;

/**
 * Approved: Per Berggren</p>
 * No: 3.IWD.MAS0001</p>
 * Author: Marcus Haglund</p>
 * Title: IWD PlatformAccess </p>
 * Version: A</p>
 * </p>
 *
 * The class PlatformAccess is a container for the methods to be used by the ECMAScripts to access the MAS platform methods.
 * <p/>
 * Identities:
 * <br>
 * The interface uses "identities" to communicate with the objects in the MAS platform.
 * An identity is always assigned by the MAS platform and returned as the result of a method,
 * e.g subscriberGetMailbox. The identity can then be used in subsequent methods e.g. mailboxGetByteUsage.
 * The identities are unique within each type, which means that for example a messageId uniquely
 * identifies a message and that subscriber, mailbox and folder are not required as parameters in the
 * methods that access messages. All the identities are integers, the type of identity is defined by the
 * name of the parameters, e.g. mailboxId and messageId.
 * </p>
 * <p/>
 * Error handling:
 * <br>
 * Errors that occur during the execution of the methods are reported through the VoiceXML platform defined
 * events. The following general errors are defined: "error.com.mobeon.platform.profileread", "error.com.mobeon.platform.profilewrite",
 * "error.com.mobeon.platform.mailbox", "error.com.mobeon.platform.numberanalysis", "error.com.mobeon.platform.datanotfound" and
 * "error.com.mobeon.platform.systemerror".
 * <br>
 * "error.com.mobeon.platform.profileread" shall be sent when a read request for the users profile fails. (Some
 * communication error with the user directory) The event shall contain additional error information in the message part.
 * <br>
 * "error.com.mobeon.platform.profilewrite" shall be sent when a write request to update the users profile fails. (Some
 * communication error with the user directory) The event shall contain additional error information in the message part.
 * <br>
 * "error.com.mobeon.platform.mailbox" shall be sent when a request to access the users mailbox fails. The event shall
 * contain additional error information in the message part.
 * <br>
 * "error.com.mobeon.platform.numberanalysis" shall be sent when number analysis fails. See the systemAnalyzeNumber
 * function for more details about the eventdescription.
 * <br>
 * "error.com.mobeon.platform.datanotfound" shall be sent when the requested data was not found. The additional error
 * information in the message part is specified for each method.
 * <br>
 * "error.com.mobeon.platform.system" shall be sent for other types of errors, e.g usage of an identity that does
 * not exist. The event shall contain additional error information in the message part.
 * <br>
 * The event messages can contain an event specific part and an additional part which may contain some extra information
 * about the error. The parts are separated with a "%".
 * <br>
 * Ex. on a message for the error.com.mobeon.platform.numberanalysis event:
 * systemAnalyzeNumber:NOMATCH%No rule matched the number 123456
 * <br>
 * Ex. on a message for the error.com.mobeon.platform.datanotfound event:
 * subscriberGetStringAttribute:attrName=userlevel%Invalid attribute
 * </p>
 */
public interface PlatformAccess {

    /**
     * Adds a member to the specified distribution list.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li>the <code>phoneNumber</code> is not found</li>
     * <li>the distListNumber is not found</li>
     * <p>
     * The event message will contain the item that was not found, that is: "phoneNumber" or "distListNumber".
     * </ul>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT    <br>
     * SSE_PROFILEWRITE_EVENT   <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT          <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "distributionListAddMember:distListNumber=" + distListNumber, e); <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "distributionListAddMember:phoneNumber=" + phoneNumber, "phoneNumber not found");
     * </dd></dl>
     * @param phoneNumber    the phone number of the subscriber
     * @param distListNumber the distribution list number
     * @param distListMember the email address to the member
     */
    public void distributionListAddMember(String phoneNumber, String distListNumber, String distListMember);

    /**
     * Deletes a member from the specified distribution list.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li>the <code>phoneNumber</code> is not found</li>
     * <li>the distListNumber is not found</li>
     * <p>
     * The event message will contain the item that was not found, that is: "phoneNumber" or "distListNumber".
     * </ul>
     * </ul>
     * <p>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_PROFILEWRITE_EVENT <br>
     * SSE_DATANOTFOUND_EVENT<br>
     * SSE_SYSTEM_EVENT <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "distributionListDeleteMember:distListNumber=" + distListNumber, e);<br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "distributionListDeleteMember:phoneNumber=" + phoneNumber, "phoneNumber not found");<br>
     * </dd></dl>
     *
     * @param phoneNumber    the phone number of the subscriber
     * @param distListNumber the distribution list number
     * @param distListMember the email address to the member
     */
    public void distributionListDeleteMember(String phoneNumber, String distListNumber, String distListMember);

    /**
     * Returns a list with the members in the specified distribution list. The list is empty if no members exist in the list.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li>the <code>phoneNumber</code> is not found</li>
     * <li>the distListNumber is not found</li>
     * <p>
     * The event message will contain the item that was not found, that is "phoneNumber" or "distListNumber".
     * </ul>
     * </ul>
     * <p>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value:</b> A string array containing:<br>
     * {"aaa@aaa.com", "bbb@bbb.com", "ccc@ccc.com"}<br>
     * All parameters to this method are ignored.</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "distributionListGetMembers:distListNumber=" + distListNumber, e); <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "distributionListGetMembers:phoneNumber=" + phoneNumber, "phoneNumber not found"); <br>
     * </dd></dl>
     *
     * @param phoneNumber    the phone number of the subscriber
     * @param distListNumber the distribution list number
     * @return a list with the email addresses to the members
     */
    public String[] distributionListGetMembers(String phoneNumber, String distListNumber);

    /**
     * Returns the spoken name for the specified distribution list.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li>the <code>phoneNumber</code>.</li>
     * <li>the distListNumber is not found</li>
     * <li>the distribution list has no spoken name</li>
     * <p>
     * If phoneNumber or distListNumber is not found the event message will contain the item that was not found,
     * that is: "phoneNumber", "distListNumber".
     * If no spokenName exist for distributeion list the message will contain the string "spokenName".
     * </ul>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value:</b> An audio or video media object. The same object is returned regardless of the phoneNumber and distListId parameters.</dd><
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_MAILBOX_EVENT     <br>
     * SSE_DATANOTFOUND_EVENT    <br>
     * SSE_SYSTEM_EVENT              <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "distributionListGetSpokenName:distListNumber=" + distListNumber, e);<br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "distributionListGetSpokenName:phoneNumber=" + phoneNumber, "phoneNumber not found");<br>
     * </dd></dl>
     *
     * @param phoneNumber    the phone number of the subscriber
     * @param distListNumber the identity for the distribution list
     * @return the spoken name
     */
    public IMediaObject distributionListGetSpokenName(String phoneNumber, String distListNumber);

    /**
     * Stores the spoken name for the specified distribution list.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li>the phoneNumber or distListNumber is not found</li>
     * <li>the distListNumber is not found</li>
     * <p>
     * The event message will contain the item that was not found, that is "phoneNumber" or "distListNumber".
     * </ul>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_PROFILEWRITE_EVENT <br>
     * SSE_MAILBOX_EVENT       <br>
     * SSE_DATANOTFOUND_EVENT  <br>
     * SSE_SYSTEM_EVENT <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "distributionListSetSpokenName:distListNumber=" + distListNumber, e); <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "distributionListSetSpokenName:phoneNumber=" + phoneNumber, "phoneNumber not found"); <br>
     * </dd></dl>
     *
     * @param phoneNumber    the phone number of the subscriber
     * @param distListNumber the identity for the distribution list
     * @param spokenName     the spoken name (if null it will be removed)
     */
    public void distributionListSetSpokenName(String phoneNumber, String distListNumber, IMediaObject spokenName);

    /**
     * Adds a new folder on the top level of the subscribers mailbox.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <p>
     * <li>the mailboxId is not found</li>
     * <p>The event message will contain the item that was not found, that is "mailboxId".</p>
     * </ul>
     * <b>error.com.mobeon.platform.mailbox</b> is sent if:
     * <ul>
     * <li>there are problems with the mailbox</li>
     * </ul>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT  <br>
     * SSE_DATANOTFOUND_EVENT  <br>
     * SSE_SYSTEM_EVENT             <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxAddFolder:mailboxId=" + mailboxId, e); <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxAddFolder:mailboxId=" + mailboxId, "Invalid mailboxId"); <br>
     * </dd></dl>
     *
     * @param mailboxId  the mailbox identity
     * @param folderName the name of the new folder
     */
    public void mailboxAddFolder(int mailboxId, String folderName);

    /**
     * Adds a new folder in the specified folder.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the mailboxId is not found</li>
     * <li>the folderId is not found</li>
     * <p>
     * The event message will contain the item that was not found, that is "mailboxId" or "folderId".
     * </ul>
     * <b>error.com.mobeon.platform.mailbox</b> is sent if:
     * <ul>
     * <li>there are problems with the mailbox</li>
     * </ul>
     * </ul>
     * <p>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT  <br>
     * SSE_DATANOTFOUND_EVENT  <br>
     * SSE_SYSTEM_EVENT  <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxAddFolder:mailboxId=" + mailboxId, e);  <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxAddFolder:folderId=" + folderId, "Invalid folderId");  <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxAddFolder:mailboxId=" + mailboxId, "Invalid mailboxId");    <br>
     * <p/>
     * </dd></dl>
     *
     * @param mailboxId  the mailbox identity
     * @param folderId   the identity of the folder where the new folder shall be located
     * @param folderName the name of the new folder
     */
    public void mailboxAddFolder(int mailboxId, int folderId, String folderName);

    /**
     * Retrieves an usageId.
     * The usageId is to be used with method
     * {@link PlatformAccess#mailboxGetMessageUsage(int)}.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the mailboxId is not found</li>
     * <p>
     * The event message will contain the item that was not found, that is "mailboxId".
     * </ul>
     * <b>error.com.mobeon.platform.mailbox</b> is sent if:
     * <ul>
     * <li>there are problems with the mailbox</li>
     * </ul>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Return value:</b> Always return the value "1"</dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT  <br>
     * SSE_DATANOTFOUND_EVENT  <br>
     * SSE_SYSTEM_EVENT  <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "mailBoxUsage:mailboxId=" + mailboxId, e);  <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "mailBoxUsage:mailboxId=" + mailboxId, "Invalid mailboxId");  <br>
     * </dd></dl>
     *
     * @param mailboxId the mailbox identity
     * @return usageId used in the mailboxGetByteUsage and mailboxGetMessageUsage methods.
     */
    public int mailboxUsage(int mailboxId);

    /**
     * Returns the identity of the specified folder.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the mailboxId is not found</li>
     * <p>
     * The event message will contain the item that was not found, that is "mailboxId".
     * </ul>
     * <b>error.com.mobeon.platform.mailbox</b> is sent if:
     * <ul>
     * <li>there are problems with the mailbox, such as folder not found</li>
     * <p>
     * The event message will contain the item that was not found, that is "folderName".
     * </ul>
     * </ul>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Return value:</b> Always returns the number "1".</dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT  <br>
     * SSE_DATANOTFOUND_EVENT  <br>
     * SSE_SYSTEM_EVENT  <br>
     * <p/>
     * throw new PlatformAccessException(EventType.MAILBOX, "mailboxGetFolder:folderName=" + folderName, e); <br>
     * throw new PlatformAccessException(EventType.MAILBOX, "mailboxGetFolder:mailboxId=" + mailboxId, e); <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetFolder:mailboxId=" + mailboxId, "Invalid mailboxId");  <br>
     * </dd></dl>
     *
     * @param mailboxId  the mailbox identity
     * @param folderName the name of the folder
     * @return the folder identity (folderId)
     */
    public int mailboxGetFolder(int mailboxId, String folderName);

    /**
     * Sets the current mailbox <b>inbox folder</b> to readonly mode (the method name is somewhat missleading).
     * Subsequent commands are then unable to update any information in the inbox folder.<br>
     * This command must be used before the mailboxUsage() command and the purpose is to make
     * it possible to check mailbox usage without changing
     * the lastAccessTime flag in MS i.e. IMAP command EXAMINE will be used instead of SELECT.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the mailboxId is not found</li>
     * <p>
     * The event message will contain the item that was not found, that is "mailboxId".
     * </ul>
     * <b>error.com.mobeon.platform.mailbox</b> is sent if:
     * <ul>
     * <li>there are problems with the mailbox</li>
     * </ul>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>  Not implemented
     * </dl>
     *
     * @param mailboxId  the mailbox identity
     */
    public void mailboxSetReadonly(int mailboxId);


    /**
     * Sets the current mailbox <b>inbox folder</b> to readwrite mode.
     * If the mailbox was opened in readonly mode, the mailbox is reopened in readwrite mode.
     * This command must be used before the mailboxUsage() command.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the mailboxId is not found</li>
     * <p>
     * The event message will contain the item that was not found, that is "mailboxId".
     * </ul>
     * <b>error.com.mobeon.platform.mailbox</b> is sent if:
     * <ul>
     * <li>there are problems with the mailbox</li>
     * </ul>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b> Not implemented
     * </dl>
     *
     * @param mailboxId  the mailbox identity
     */
    public void mailboxSetReadwrite(int mailboxId);

    /**
     * Makes a search for messages stored in the specified folder. An id of the list of searchresults is returned.
     * This operation <b>will</b> do a search in the mailserver. <br>
     * The arguments of this method can be dividen into two classes. The "selection" arguments which controls
     * what messages is to be retrieved - "types", "states" and "priorities". The "orders" which controls the
     * ordering of the result.
     * <p/>
     * <b>Ordering of result:</b>
     * The ordering of the result is dependent on the combination of the "ordering" arguments - that is "orders" and "timeOrder"
     * - and the selection arguments - "types", "states" and "priorities".
     * The <b>timeOrder</b> argument is the simpler of the two ordering arguments, just sorts
     * messages in either fifo or lifo fashion.<br>
     * The <b>orders</b> argument rules on what selection arguments the messages is to be ordered by. I.e a list consisting of an
     * combination of "type", "states" and "priorities". The actual ordering of the result is then dependent on the order the
     * selection arguments is given in.<br>
     * If <b>both</b> "orders" and "timeOrder" is given the orders argument will be the stronger of the two.<br>
     * <p/>
     * <dl><b>Example 1:</b>
     * <dd>orders=(types)</dd>
     * <dd>types=(email, fax, voice, video)></dd>
     * <dd>Result: </dd> The messages will be sorted according to the types argument, that is email messages first, then fax messages
     * and so on.
     * </dl>
     * <p/>
     * <dl><b>Example 2:</b>
     * <dd>orders=(states, types)</dd>
     * <dd>types=(email, fax, voice, video)</dd>
     * <dd>states=(new, read)</dd>
     * <dd>Result:</dd> The messages will be first be sorted according to the states argument, that is new messages before read
     * messages. Then the messages in each "state" will be sorted internally on types, i.e first email messages then
     * fax messages etc. So, first comes all "new" messages, which is internally sorted according to the ordering of
     * the types argument, and the all "read" messages, which also is internally sorted according to the ordering of
     * the types argument.
     * </dl>
     * <p/>
     * <dl><b>Example 3:</b>
     * <dd>orders=(states, types)</dd>
     * <dd>timeOrder=fifo</dd><br>
     * <dd>types=(email, fax, voice, video)</dd>
     * <dd>Result:</dd> The messages will be first be sorted according to the types argument, that is email messages first, then fax messages
     * and so on Then the messages of each "type" will be sorted internally in "fifo" manner.
     * </dl>
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the folderId is not found</li>
     * <li>the types argument contains a non-valid type. (see description of parameter types for valid values)</li>
     * <li>the states arument contains a non-valid state. (see description av parameter states for valid values)</li>
     * <li>the priorities argument in non-valid.(see description of parameter priorities for valid values)</li>
     * <li>the orders argument contains a non-valid value. (see description of parameter orders for valid values.)</li>
     * <li>the timeOrders argument contains a non-valid value. (see description of parameter timeOrder for valid values.)</li>
     * <li>an error occurs when retrieving messages from mailbox.
     * </ul><b>error.com.mobeon.platform.mailbox</b> is sent if:
     * <ul>
     * <li>there are problems with the mailbox</li></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Return value:</b>a list id to a message list with messages in the simulated mailbox that matches parameters</dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT  <br>
     * SSE_DATANOTFOUND_EVENT  <br>
     * SSE_SYSTEM_EVENT  <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetMessageList:folderId=" + folderId, "Invalid folderId");  <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetMessageList:folderId=" + folderId, e);  <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetMessageList:folderId=" + folderId, e);  <br>
     * </dd></dl>
     *
     * @param folderId   the folder identity
     * @param types      a string containing the type of messages to get, separated by comma. Valid values: "voice", "video", "fax" and "email".
     * @param states     a string containing the state for the messages to get, separated by comma. Valid values: "new", "read", "deleted and "saved".
     * @param priorities a string containing the priority for the messages to get, separated by comma. Valid values: "urgent" and "nonurgent".
     * @param orders     a string containing the order for the messages to get, separated by comma. Valid values: "priority", "type" and "state".
     * @param timeOrder  the time order for the messages to get. Valid values: "fifo" and "lifo".
     * @return an id for the list of searchresults (messageListId)
     */
    public int mailboxGetMessageList(int folderId, String types, String states, String priorities, String orders, String timeOrder);

    /**
     * Makes a search for messages in the list that is defined by the specified messageListId.
     * This operation will <b>not</b> do a search in the mailserver.
     * <p/>
     * <b>Ordering of result:</b>
     * The ordering of the result follows same principle as in method <code>mailboxGetMessageList</code>.
     * See {@link PlatformAccess#mailboxGetMessageList(int, String, String, String, String, String)}.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the folderId is not found</li>
     * <li>the types argument contains a non-valid type. (see description of parameter types for valid values)</li>
     * <li>the states arument contains a non-valid state. (see description av parameter states for valid values)</li>
     * <li>the priorities argument in non-valid.(see description of parameter priorities for valid values)</li>
     * <li>the orders argument contains a non-valid value. (see description of parameter orders for valid values.)</li>
     * <li>the timeOrders argument contains a non-valid value. (see description of parameter timeOrder for valid values.)</li>
     * </ul>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Return value:</b>a list id to a message list with messages in the simulated mailbox that matches parameters</dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT           <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetMessageSubList:messageListId=" + messageListId, "Invalid messageListId"); <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetMessageSubList:messageListId=" + messageListId, e); <br>
     * </dd></dl>
     *
     * @param messageListId Identity of message list to retrieve messages from.
     * @param types         a string containing the type of messages to get, separated by comma. Valid values: "voice", "video", "fax" and "email".
     * @param states        a string containing the state for the messages to get, separated by comma. Valid values: "new", "read", "deleted and "saved".
     * @param priorities    a string containing the priority for the messages to get, separated by comma. Valid values: "urgent" and "nonurgent".
     * @param orders        a string containing the order for the messages to get, separated by comma. Valid values: "priority", "type" and "state".
     * @param timeOrder     the time order for the messages to get. Valid values: "fifo" and "lifo".
     * @return an id for the list of searchresults (messageListId)
     */
    public int mailboxGetMessageSubList(int messageListId, String types, String states, String priorities, String orders, String timeOrder);

    /**
     * Retrieves the ids for the messages in the list specified by messageListId.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the messageListId is not found</li>
     * <p>
     * The event message will contain the item not found, that is: "messageListId".
     * </ul>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Return value:</b>Returns an array with the ID-numbers of the messages
     * in the message list.</dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetMessages:messageListId=" + messageListId, "Invalid messageListId"); <br>
     * </dd></dl>
     *
     * @param messageListId id for the search result list
     * @return a list with the message identities (messageId)
     */
    public int[] mailboxGetMessages(int messageListId);

    /**
     * <b>Not Implemented</b>. Always returns <code>0</code>.
     * <p>
     * When implemented, the behavior should be something like the following:
     * <p>
     * Start fetching of the messages stored in the specified folder. Asynchroneous function.
     * <p/>Except for the asynchroneous behaviour, this method behavior regarding selecting and ordering messages is
     * identical to method <code>mailboxGetMessageList</code>.
     * See {@link PlatformAccess#mailboxGetMessageList(int, String, String, String, String, String)}.
     * <p/>
     * Priority: 3
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the folderId is not found</li>
     * <li>the types argument contains a non-valid type. (see description of parameter types for valid values)</li>
     * <li>the states arument contains a non-valid state. (see description av parameter states for valid values)</li>
     * <li>the priorities argument in non-valid.(see description of parameter priorities for valid values)</li>
     * <li>the orders argument contains a non-valid value. (see description of parameter orders for valid values.)</li>
     * <li>the timeOrders argument contains a non-valid value. (see description of parameter timeOrder for valid values.)</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Return value:</b>Always return the value "1" </dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT           <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetMessageListAsync:folderId=" + folderId, "Invalid folderId"); <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetMessageListAsync:folderId=" + folderId, e); <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetMessageListAsync:folderId=" + folderId, e); <br>
     * </dd></dl>
     *
     * @param folderId   the folder identity
     * @param types      a string containing the type of messages to get, separated by comma. Valid values: "voice", "video", "fax" and "email".
     * @param states     a string containing the state for the messages to get, separated by comma. Valid values: "new", "read", "deleted" and "saved".
     * @param priorities a string containing the priority for the messages to get, separated by comma. Valid values: "urgent" and "nonurgent".
     * @param orders     a string containing the order for the messages to get, separated by comma. Valid values: "priority", "type" and "state".
     * @param timeOrder  the time order for the messages to get. Valid values: "fifo" and "lifo".
     * @return a transactionId to be used when to wait or check for the result
     */
    public int mailboxGetMessageListAsync(int folderId, String types, String states, String priorities, String orders, String timeOrder);

    /**
     * <b>Not Implemented</b>. Always returns <code>0</code>.
     * <p>
     * When implemented, the behavior should be something like the following:
     * <p>
     * Waits for the asynchroneous function <code>mailboxGetMessageListAsync</code> to finish.
     * Retrieves the ids for the messages that was retrieved when the search specified with the transactionId was made.
     * <p/>
     * Priority: 3
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the transactionId is not found</li>
     * <p>
     * The event message will contain the item not found, that is: "transactionId".
     * </ul>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Return value:</b>Returns an array with the ID-numbers of the messages
     * in the message list associated with the transactionId.</dd>
     * <p/>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT  <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetMessageList:transactionId=" + transactionId, "Invalid transactionId"); <br>
     * </dd></dl>
     *
     * @param transactionId the identification of the asynchrouneous request
     * @return a list with the message identities (messageId)
     */
    public int[] mailboxGetMessageList(int transactionId);

    /**
     * Returns the amount of messages stored in the mailbox specified with usageId.
     * The usageId is fetched with method
     * {@link PlatformAccess#mailboxUsage(int)}.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the usageId is not found</li>
     * <p>
     * The event message will contain the item not found, that is: "usageId".
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Return value:</b> Returns the number of messages in the simulated inbox.</dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetMessageUsage:mailboxId=" + mailboxId, "Invalid mailboxId"); <br>
     * </dd></dl>
     *
     * @param usageId the usage identity. Is fetched with method {@link PlatformAccess#mailboxUsage(int)}.
     * @return the number of messages
     */
    public int mailboxGetMessageUsage(int usageId);

    /**
     * Returns the number of messages in the specified list that fulfills the specified criterias.
     * This operation will <b>not</b> do a search in the mailserver.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the messageListId is not found</li>
     * <li>the types argument contains a non-valid type. (see description of parameter types for valid values)</li>
     * <li>the states arument contains a non-valid state. (see description av parameter states for valid values)</li>
     * <li>the priorities argument in non-valid.(see description of parameter priorities for valid values)</li>
     * </ul> </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Return value:</b> Returns the number of messages in the message list meeting the criteria of the parameters. </dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT           <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "mailboxGetNumberOfMessages:messageListId=" + messageListId, "Invalid messageListId"); <br>
     * </dd></dl>
     *
     * @param messageListId id for the search result list
     * @param types         a string containing the type of messages to get, separated by comma. Valid values: "voice", "video", "fax" and "email".
     * @param states        a string containing the state for the messages to get, separated by comma. Valid values: "new", "read", "deleted" and "saved".
     * @param priorities    a string containing the priority for the messages to get, separated by comma. Valid values: "urgent" and "nonurgent".
     * @return the number of messages
     */
    public int mailboxGetNumberOfMessages(int messageListId, String types, String states, String priorities);

    /**
     * Adds a media object to the specified storable message.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the storableMessageId is not found</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> Increases the attachment count property of the message with the
     * specified ID and replaces the MediaObject with the one passed in the mediaObject parameter<br>
     * <i>Note: The simulation engine does not support multiple media objects per message!</i></dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageAddMediaObject:storableMessageId=" + storableMessageId, "Invalid storableMessageId");
     * </dd></dl>
     *
     * @param storableMessageId the identity of the storable message
     * @param mediaObject       the media object to add
     * @param description       content description to be in the message
     * @param fileName          filename on the mediaobject to be set in the message
     * @param language          the language of the message according to <code>RFC2068</code>
     */
    public void messageAddMediaObject(String phoneNumber, int storableMessageId, IMediaObject mediaObject, String description, String fileName, String language);

    /**
     * Copies the specified message to the specified folder on the top level of the subscribers mailbox.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the mailboxId is not found</li>
     * <li>the messageId is not found</li>
     * <p>
     * The event message will contain the item that was not found, that is "mailboxId" or "messageId".
     * </ul>
     * <b>error.com.mobeon.platform.mailbox</b> is sent if:
     * <ul>
     * <li>there are problems with the mailbox, such as folder not found</li>
     * <p>
     * The event message will contain the item that was not found, that is "folderName".
     * </ul>
     *</ul>
     *</ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT           <br>
     * <p/>
     * throw new PlatformAccessException(EventType.MAILBOX, "messageCopyToFolder:mailboxId=" + mailboxId + ", messageId=" + messageId + ", folderName=" + folderName, e);
     * throw new PlatformAccessException(EventType.MAILBOX, "messageCopyToFolder:folderName=" + folderName, e);
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageCopyToFolder:messageId=" + messageId, "Invalid messageId");
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageCopyToFolder:mailboxId=" + mailboxId, "Invalid mailboxId");
     * <p/>
     * </dd></dl>
     *
     * @param mailboxId  the mailbox identity
     * @param messageId  the identity of the message
     * @param folderName the name of the folder where the message shall be copied
     */
    public void messageCopyToFolder(int mailboxId, int messageId, String folderName);

    /**
     * Copies the specified message to the specified folder.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the mailboxId is not found</li>
     * <li>the folderId is not found</li>
     * <li>the messageId is not found</li>
     * <p>
     * The event message will contain the item that was not found, that is "mailboxId", "folderId", "messageId" or "folderName".
     * </ul>
     * </ul>
     * <b>error.com.mobeon.platform.mailbox</b> is sent if:
     * <ul>
     * <li>there are problems with the mailbox</li></ul>
     * <p/>
     * Priority: 2
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd><
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT           <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageCopyToFolder:mailboxId=" + mailboxId + ", messageId=" + messageId + ", e);<br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageCopyToFolder:messageId=" + messageId, "Invalid messageId");<br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageCopyToFolder:folderId=" + folderId, "Invalid folderId");
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageCopyToFolder:mailboxId=" + mailboxId, "Invalid mailboxId");
     * <p/>
     * </dd></dl>
     *
     * @param mailboxId  the mailbox identity
     * @param folderId   the identity of the folder where folderName exist
     * @param messageId  the identity of the message that shall be copied
     * @param folderName the name of the folder where the message shall be copied
     */
    public void messageCopyToFolder(int mailboxId, int folderId, int messageId, String folderName);

    /**
     * Creates a new storable message.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>an error occurs when creating the message.</li>
     * <p>
     * </ul>
     * <b>error.com.mobeon.platform.mailbox</b> is sent if:
     * <ul>
     * <li>there are problems with the mailbox</li>
     * </ul>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>Creates a new message which can later be committed to the store
     * using {@link #messageStore(int) messageStore}.</dd>
     * <dd><b>Return value:</b> The ID of the created message.</dd>
     * <dd><b>Events: <br></b>
     * SSE_SYSTEM_EVENT <br>
     * <p/>
     * MailboxManager.java:            throw new PlatformAccessException(EventType.SYSTEMERROR, "messageCreateNew", e);
     * </dd></dl>
     *
     * @return the identity of the storable message (storableMessageId)
     */
    public int messageCreateNew();

    /**
     * Returns the identity of a new storable message with the original message attached.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the messageId is not found</li>
     * </ul>
     * <b>error.com.mobeon.platform.mailbox</b> is sent if:
     * <ul>
     * <li>there are problems with the mailbox</li>
     * </ul>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> Creates a copy of the message with the specified messageID which
     * can later be committed to the store using {@link #messageStore(int) messageStore}.
     * The message copy is marked as "forwarded".<br></dd>
     * <dd><b>Return value:</b> The ID of the new message copy.</dd>
     * <i>Note: The simulation engine does not support multiple media objects per message nor attaching a message to another message!</i>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT<br>
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageForward:messageId=" + messageId, e);<br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageForward:messageId=" + messageId, "Invalid messageId");
     * </dd></dl>
     *
     * @param messageId the identity of the message original message
     * @return the identity of the storable message (storableMessageId)
     */
    public int messageForward(int messageId);

    /**
     * Returns a list with identities to the content of the specified message. The list is empty if no content exist.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the messageId is not found</li>
     * </ul>
     * <b>error.com.mobeon.platform.mailbox</b> is sent if:
     * <ul>
     * <li>there are problems with the mailbox</li>
     * </ul>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None</dd>
     * <dd><b>Return value:</b> Returns an array containing a number of instances of the id
     * of the content of the message with the specified id.</dd>
     * The Simulation engine only supports one media content per message, but the number
     * The Simulation engine only supports one media content per message, but the number
     * of attachments can be specified. Hence the array will contain the same number of
     * elements as the attachment count property of the message, but all the elements will
     * contain the same ID.
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT  <br>
     * SSE_SYSTEM_EVENT <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageGetContent:messageId=" + messageId, e); <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageGetContent:messageId=" + messageId, "Invalid messageId");
     * </dd></dl>
     *
     * @param messageId the identity of the message
     * @return a list with the message content identities (messageContentId)
     */
    public int[] messageGetContent(int messageId);

    /**
     * Returns the media object of the specified message content.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the messageContentId is not found</li>
     * <p>
     * </ul>
     * <p>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li>the specified message content does not contain a media object</li>
     * </ul>
     * <b>error.com.mobeon.platform.mailbox</b> is sent if:
     * <ul>
     * <li>there are problems with the mailbox</li>
     * </ul>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Return value:</b> The MediaObject contained in the message with the specified ID</dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT            <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "messageGetMediaObject:messageContentId=" + messageContentId, "No mediaobject found in the messagecontent");<br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageGetMediaObject:messageContentId=" + messageContentId, e); <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageGetMediaObject:messageContentId=" + messageContentId, "Invalid messageContentId");
     * </dd></dl>
     *
     * @param messageContentId the identity of the message content
     * @return the media object
     */
    public IMediaObject messageGetMediaObject(int messageContentId);

    /**
     * Returns the media properties of the specified message content.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the messageContentId is not found</li>
     * </ul>
     * <b>error.com.mobeon.platform.mailbox</b> is sent if:
     * <ul>
     * <li>there are problems with the mailbox</li>
     * </ul>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Return value:</b> Returns the Media property of the message.</dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT  <br>
     * SSE_SYSTEM_EVENT <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageGetMediaProperties:messageContentId=" + messageContentId, e); <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageGetMediaProperties:messageContentId=" + messageContentId, "Invalid messageContentId");
     * </dd></dl>
     *
     * @param messageContentId the identity of the message content
     * @return the media properties. For example "video/quicktime; name=message.mov", "text/plain; format=flowed; charset=iso-8859-1".
     *
     */
    public String messageGetMediaProperties(int messageContentId);

    /**
     * Returns the requested property for the specified <b>stored</b> message.
     * <p/>
     * <dl>Valid <b>property names</b> and their corresponding possible values are:
     * <dd><b>"sender"</b></dd><br>The sender of the message.<br>
     * Possible property value: The sender in the form described in document IWD - END USER MESSAGE FORMAT.
     * <dd><b>"recipients"</b></dd><br>The recipients of the message.<br>
     * Possible property value: List of recipients.
     * <dd><b>"secondaryrecipients"</b></dd><br>The secondary recipients of the message.<br>
     * Possible property value: List of secondary recipients.
     * <dd><b>"subject"</b></dd><br>The subject of the message.<br>
     * Possible property value: Any text string.
     * <dd><b>"replytoaddr"</b></dd><br>The adress a reply message will be sent to.<br>
     * Possible property value: Any text string.
     * <dd><b>"type"</b></dd><br>The message type.<br>
     * Possible property values are: "voice", "video", "language", "deliverydate", "urgent", "confidential"
     * <dd><b>"state"</b></dd><br>The message state.<br>
     * Possible property values are: "new", "read", "saved", "deleted"
     * <dd><b>"language"</b></dd><br>The message language.<br>
     * Possible property value: Any valid language according to ISO-639-1
     * <dd><b>"receiveddate"</b></dd><br>The date and time when the
     * receiver shall receive or has received the message.<br>
     * Possible property value: Any date conforming to RFC 1123.
     * <dd><b>"urgent"</b></dd><br>Indicates if the message urgent.<br>
     * Possible property values are "true" or "false".
     * <dd><b>"confidential"</b></dd><br>Indicates if message is confidential.<br>
     * Possible property values: "true" or "false".
     * <dd><b>"forwarded"</b></dd><br>Indicates if message is forwarded.<br>
     * Possible property values: "true" or "false".
     * <dd><b>"deliveryreport"</b></dd><br>Indicates if message is a deliveryreport.<br>
     * Possible property values: "true" or "false".
     * <dd><b>"deliverystatus"</b></dd><br>Indicates if message is a deliveryreport.<br>
     * Possible property values: "false", "store-failed", "print-failed"
     * </dl>
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the messageId is not found</li>
     * <li>the propertyName is not found</li>
     * <p>The event message will contain the item that was not found, that is "messageId" or "propertyName".</p>
     * </ul></ul>
     * <p>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Return value:</b> Returns a property of the specified message which can be non atomic. Check FS_Mailbox5.FS.MAS0001.pdf for valid properties<br>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageGetStoredProperty:propertyName=" + propertyName, "Invalid propertyName " + propertyName); <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "messageGetStoredProperty:propertyName=" + propertyName, "No value found"); <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageGetStoredProperty:messageId=" + messageId, "Invalid messageId");
     * </dd></dl>
     *
     * @param messageId    the identity of the stored message to get the property from
     * @param propertyName the name of the requested property. See above for valid property names.
     * @return string-representation of the requested property. If only one value it is located in the
     *         first element of the array.
     */
    public String[] messageGetStoredProperty(int messageId, String propertyName);

    /**
     * Returns the requested property for the specified <b>storable</b> message.
     * <p>
     * <dl>Valid <b>property names</b> and their corresponding valid values are:
     * <dd><b>"sender"</b></dd><br>The sender of the message.<br>
     * Valid property value: The sender in the form described in document IWD - END USER MESSAGE FORMAT.
     * <dd><b>"recipients"</b></dd><br>The recipients of the message.<br>
     * Valid property value: List of recipients.
     * <dd><b>"secondaryrecipients"</b></dd><br>The secondary recipients of the message.<br>
     * Valid property value: List of secondary recipients.
     * <dd><b>"subject"</b></dd><br>The subject of the message.<br>
     * Valid property value: Any text string.
     * <dd><b>"replytoaddr"</b></dd><br>The adress a reply of the message will be sent to.<br>
     * Valid property value: Any text string.
     * <dd><b>"type"</b></dd><br>The message type.<br>
     * Valid property values are "voice", "video", "language", "deliverydate", "urgent", "confidential"
     * <dd><b>"language"</b></dd><br>The message language.<br>
     * Valid property value: Any valid language according to ISO-639-1
     * <dd><b>"deliverydate"</b></dd><br>The date and time when the receiver shall receive or has received the message.<br>
     * Valid property value: Any date conforming to RFC 1123.
     * <dd><b>"urgent"</b></dd><br>Indicates if the message urgent.<br>
     * Valid values: "true" or "false".
     * <dd><b>"confidential"</b></dd><br>Indicates if message is confidential.<br>
     * Valid property value: "true" or "false".
     * </dl>
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the storableMessageId is not found</li>
     * <li>the propertyName is not found</li>
     * <p>The event message will contain the item that was not found, that is "messageId" or "propertyName".</p>
     * </ul></ul>
     * <p>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Return value:</b> Returns a property of the specified message which can be non atomic. Check FS-Mailbox for valid properties<dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageGetStorableProperty:storableMessageId=" + storableMessageId, "Invalid storableMessageId"); <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageGetStorableProperty:propertyName=" + propertyName, "Invalid propertyName " + propertyName);     <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "messageGetStorableProperty:propertyName=" + propertyName, "No value found");
     * <p/>
     * </dd></dl>
     *
     * @param storableMessageId the identity of the storable message to get the property from
     * @param propertyName      the name of the requested property. Valid values: "sender", "recipients",
     *                          "secondaryrecipients", "subject", "replytoaddr", "type", "language", "deliverydate", "urgent", "confidential".
     * @return string-representation of the requested property. If only one value it is located in
     *         the first element of the array. See description above for valid values for each property.
     */
    public String[] messageGetStorableProperty(int storableMessageId, String propertyName);

    /**
     * Returns the size of the specified MessageContent.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the messageContentId is not found</li>
     * </ul>
     * <b>error.com.mobeon.platform.mailbox</b> is sent if:
     * <ul>
     * <li>there are problems with the mailbox</li>
     * </ul>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None.</dd>
     * <dd><b>Return value:</b> Returns the value of system setting SSE_MailBox_MessageSize</dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageContentSize:messageContentId=" + messageContentId, e); <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageContentSize:messageContentId=" + messageContentId, "Invalid messageContentId");
     * </dd></dl>
     *
     * @param messageContentId the identity of the message content
     * @return returns the size of the specified content in bytes.
     */
    public int messageContentSize(int messageContentId);

    /**
     * Returns the length of the specified MessageContent. Length can be retrieved in milliseconds or number of pages.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the messageContentId is not found.
     * The event message will contain the id not found as follows: "messageContentLength:messageContentId=" + messageContentId</li>
     * <li>The type is invalid, that is non of "milleseconds" or "pages". The event message will be as follows:
     * "messageContentLength:type=" + type + "%Invalid length type " + type</li>
     * </ul>
     * <b>error.com.mobeon.platform.mailbox</b> is sent if:
     * <ul>
     * <li>some error occurs when retrieving size from mailbox.
     * The event message will be as follows:"messageContentLength:messageContentId=" + messageContentId</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None.</dd>
     * <dd><b>Return value:</b> Return the values of system setting  SSE_Mailbox_ContentLength_Page for type pages and SSE_Mailbox_ContentLength_ms
     * for type milliseconds</dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageContentLength:type=" + type, "Invalid length type " + type);<br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageContentLength:messageContentId=" + messageContentId, e);<br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageContentLength:messageContentId=" + messageContentId, "Invalid messageContentId");
     * </dd></dl>
     *
     * @param messageContentId the identity of the message content
     * @param type the type (or unit) of the media length. Valid values: "milliseconds" and "pages".
     * @return length number of milliseconds or number of pages. If no length could be found in the content -1 is returned.
     */
    public int messageContentLength(int messageContentId, String type);

    /**
     * Returns the spoken name of the sender of the specified message.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the messageId is not found</li>
     * </ul>
     * <p>
     * <b>error.com.mobeon.platform.datanotfound is sent if:</b>
     * <ul>
     * <li>there is no spoken name attached to the message</li>
     * </ul>
     * <b>error.com.mobeon.platform.mailbox</b> is sent if:
     * <ul>
     * <li>there are problems with the mailbox</li>
     * </ul>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None.</dd>
     * <dd><b>Return value:</b> An audio media object. The same object is returned regardless of the messageId parameter.</dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "messageGetSpokenNameOfSender:messageId=" + messageId, "No spokenName found in the message"); <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageGetSpokenNameOfSender:messageId=" + messageId, e); <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageGetSpokenNameOfSender:messageId=" + messageId, "Invalid messageId"); <br>
     * </dd></dl>
     *
     * @param messageId the identity of the message
     * @return the spoken name
     */
    public IMediaObject messageGetSpokenNameOfSender(int messageId);

    /**
     * Prints the specified message to the specified destination.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the messageId is not found</li>
     * </ul>
     * </ul>
     * <b>error.com.mobeon.platform.mailbox</b> is sent if:
     * <ul>
     * <li>there are problems with the mailbox</li></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messagePrint:messageId=" + messageId, e); <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messagePrint:messageId=" + messageId, "Invalid messageId");
     * </dd></dl>
     *
     * @param messageId   the identity of the message
     * @param destination the destination for the message, e.g. fax number
     * @param sender      sender of the message
     */
    public void messagePrint(int messageId, String destination, String sender);

    /**
     * Sets the specified property in the specified <b>storable</b> message.
     * <p>
     * <dl>Valid <b>property names</b> and their corresponding valid values are:
     * <dd><b>"sender"</b></dd><br>The sender of the message.<br>
     * Valid property value: The sender in the form described in document IWD - END USER MESSAGE FORMAT.
     * <dd><b>"recipients"</b></dd><br>When "recipients" is supplied, the value of "recipients" is added to
     * the message as is, and is to be used when you want to add recepients which are not subscribers.
     * A "recipient" is a mail address, e.g. "kalle@acme.com"<br>
     * Valid property value: List of recipients.
     * <dd><b>"secondaryrecipients"</b></dd><br>The secondary recipients of the message.<br>
     * Valid property value: List of secondary recipients.
     * <dd><b>"recipientsSubscriberId"</b></dd><br>When "recipientsSubscriberId" is supplied, the platform
     * will add the subscriber as recipient to the message by examining the profile for the subscriber,
     * and is to be used when you want to add recipents which are subscribers. It is an error by
     * the application to supply "subscribers" who are not subscribers in the system.
     * A "recipientsSubscriberId" is a phone number.<br>
     * Valid property value: List of recipientsSubscriberId.
     * <dd><b>"subject"</b></dd><br>The subject of the message.<br>
     * Valid property value: Any text string.
     * <dd><b>"replytoaddr"</b></dd><br>The adress a reply of the message will be sent to.<br>
     * Valid property value: Any text string.
     * <dd><b>"type"</b></dd><br>The message type.<br>
     * Valid property values are "voice", "video", "language", "deliverydate", "urgent", "confidential"
     * <dd><b>"language"</b></dd><br>The message language.<br>
     * Valid property value: Any valid language according to ISO-639-1
     * <dd><b>"deliverydate"</b></dd><br>The date and time when the receiver shall receive or has received the message.<br>
     * Valid property value: Any date conforming to RFC 1123.
     * <dd><b>"urgent"</b></dd><br>Indicates if the message urgent.<br>
     * Valid values: "true" or "false".
     * <dd><b>"confidential"</b></dd><br>Indicates if message is confidential.<br>
     * Valid property value: "true" or "false".
     * </dl>
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the storableMessageId is not found</li>
     * <li>the propertyName is not found or null</li>
     * <li>the propertyValue is null</li>
     * <p>The event message will contain the item that was not found, that is "messageId" or "propertyName".</p>
     * </ul>


     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li>the propertyName is "recipientsSubscriberId" but the subscriber is not found</li>
     * <p>The event message will contain the subscriber that was not found.</p>
     * </ul>

     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>Sets the specified property in the specified <b>storable</b>  message.</dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT <br>
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageSetStorableProperty:storableMessageId=" + storableMessageId, "Invalid storableMessageId"); <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageSetStorableProperty:storableMessageId=" + storableMessageId, e); <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageSetStorableProperty:storableMessageId=" + storableMessageId,
     * <p/>
     *
     * @param storableMessageId the identity of the storable message to set the property to
     * @param propertyName      the name of the property.
     * @param propertyValue     the value(s) for the property. If single-value the value is the first element in the array.
     *                          See description of parameter propertyName for valid values for each property.
     */
    public void messageSetStorableProperty(int storableMessageId, String propertyName, String[] propertyValue);

    /**
     * Sets the specified property in the specified <b>stored</b> message.
     * <p>
     * As the message is <b>stored</b>, the only property that is legal to change is the "state" property.
     * See method
     * {@link PlatformAccess#messageGetStoredProperty(int, String)} for properties available to read.<br>
     * <dl>Valid values for the <b>state</b> property are:
     * <dd><b>"new", "read", "saved", "deleted"</b></dd>
     * </dl>
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the storableMessageId is not found</li>
     * <li>the propertyName is not found</li>
     * <p>The event message will contain the item that was not found, that is "messageId" or "propertyName".</p>
     * </ul><b>error.com.mobeon.platform.mailbox</b> is sent if:
     * <ul>
     * <li>there are problems with the mailbox</li>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageSetStoredProperty:messageId=" + messageId, "Invalid messageId");<br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageSetStoredProperty:messageId=" + messageId, e);<br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageSetStoredProperty:messageId=" + messageId, "Invalid propertyName " + propertyName);
     * </dd></dl>
     *
     * @param messageId     the identity of the stored message to set the property to
     * @param propertyName  the name of the property. Valid value is "state".
     * @param propertyValue the value(s) for the property. If single-value the value is the first element in the array.
     */
    public void messageSetStoredProperty(int messageId, String propertyName, String[] propertyValue);

    /**
     * Sets the spoken name of the sender.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the messageId is not found</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None.</dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageSetSpokenNameOfSender:storableMessageId=" + storableMessageId, "Invalid storableMessageId");
     * </dd></dl>
     *
     * @param storableMessageId the identity of the message
     * @param spokenName        the spoken name
     * @param description       content description to be in the message
     * @param fileName          filename on the mediaobject to be set in the message
     * @param language          the language of the message according to <code>RFC2068</code>
     */
    public void messageSetSpokenNameOfSender(int storableMessageId, IMediaObject spokenName, String description, String fileName, String language);

    /**
     * Stores the specified <b>storable</b> message in the recipients mailbox.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the storableMessageId is not found</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>Commits the message and any changes to it to the simulated inbox.</dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageStore:storableMessageId=" + storableMessageId, e); <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageStore:storableMessageId=" + storableMessageId, "Invalid storableMessageId");
     * </dd></dl>
     *
     * @param storableMessageId the identity of the message
     *
     * @return a list of recipients for which the message delivery failed.
     */
    public String[] messageStore(int storableMessageId);

    /**
     * Renews date when message was "issued" to now.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the messageId is not found</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None</dd>
     * <dd><b>Events: <br></b>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageRenewIssuedDate:messageId=" + messageId, e); <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "messageRenewIssuedDate:messageId=" + messageId, "Invalid messageId");
     * </dd></dl>
     *
     * @param messageId the identity of the stored message to renew date on
     */
    public void messageSetExpiryDate(int messageId, String expiryDate);

    /**
     * Adds an empty distribution list with the specified identity to the specified subscriber.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li>the phoneNumber is not a subscriber</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_PROFILEWRITE_EVENT <br>
     * SSE_DATANOTFOUND_EVENT     <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * EventType.SSE_DATANOTFOUND_EVENT, "subscriberAddDistributionList:phoneNumber=" + phoneNumber, "phoneNumber not found");
     * </dd></dl>
     *
     * @param phoneNumber    the phone number of the subscriber
     * @param distListNumber the identity for the distribution list
     */
    public void subscriberAddDistributionList(String phoneNumber, String distListNumber);

    /**
     * Deletes the specified distribution list.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li>the phoneNumber is not a subscriber</li>
     * <li>the distListNumber is not found</li>
     * <p>
     * The event message will contain the item that was not found, that is "phoneNumber" or "distListNumber".
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_PROFILEWRITE_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * EventType.SSE_DATANOTFOUND_EVENT, "subscriberDeleteDistributionList:distListNumber=" + distListNumber, e); <br>
     * EventType.SSE_DATANOTFOUND_EVENT, "subscriberDeleteDistributionList:phoneNumber=" + phoneNumber, "phoneNumber not found");
     * </dd> </dl>
     *
     * @param phoneNumber    the phone number of the subscriber
     * @param distListNumber the identity for the distribution list
     */
    public void subscriberDeleteDistributionList(String phoneNumber, String distListNumber);

    public boolean subscriberAutoprovision(String phoneNumber);

    public boolean subscriberAutoprovision(String phoneNumber, String subscriberProfile);

    public void subscriberRescheduleAutodeletionIfNecessary(String phoneNumber);

    public void subscriberScheduleAutodeletion(String userAgent);

    /**
     * Checks if there is a subscriber in the system with the specified phone number.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.profileread</b> is sent if:
     * <ul>
     * <li>an error occurs when reading the profile for phoneNumber</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None.</dd>
     * <dd><b>Return value:</b> Returns true or false depending on whether the number is marked
     * as a subscriber or non-subscriber in the SSE Scenario settings.</dd>
     * <dd><b>Events: <br></b>
     * SSE_SYSTEM_EVENT
     * </dd></dl>
     *
     * @param phoneNumber the phone number to check
     * @return true if phoneNumber is a subscriber and false if phoneNumber is not a subscriber
     */
    public boolean subscriberExist(String phoneNumber);

    /**
     * <b>Not Implemented</b>. Always returns <code>0</code>.
     * <p>
     * When implemented, the behavior should be something like the following:
     * <p>
     * Checks if there is a subscriber in the system with the specified phone number. Asynchroneous function.
     * To be used in conjunction with method {@link PlatformAccess#subscriberExist(int)}.
     * <p/>
     * Priority: 3
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Return value:</b>An ID number that can be used to fetch the result of the operation
     * with {@link #subscriberExist(int) subscriberExist}.</dd>
     * <dd><b>Events: <br></b>
     * SSE_SYSTEM_EVENT
     * </dd></dl>
     *
     * @param phoneNumber the phone number to check
     * @return a transactionId to be used when to wait or check for the result
     */
    public int subscriberExistAsync(String phoneNumber);

    /**
     * <b>Not Implemented</b>. Always returns <code>false</code>.
     *
     * When implemented, the behavior should be something like the following:
     * <p>
     * Waits for the asynchroneous function {@link PlatformAccess#subscriberExistAsync(String)} to finish.
     * <p/>
     * Priority: 3
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None.</dd>
     * <dd><b>Return value:</b> Returns true or false depending on whether the number specified in
     * the previous call to the {@link #subscriberExistAsync(String) subscriberExistAsync} method
     * is marked as a subscriber or non-subscriber in the SSE Scenario settings.</dd>
     * <dd><b>Events: <br></b>
     * SSE_SYSTEM_EVENT
     * </dd></dl>
     *
     * @param transactionId the identification of the asynchrouneous request
     * @return true if phoneNumber is a subscriber and false if phoneNumber is not a subscriber
     */
    public boolean subscriberExist(int transactionId);

    /**
     * Returns the value for the specified string attribute. The lookups are made in Terminal Subscription, Subscriber,
     * COS and Community objects.
     * <p>
     * Valid property names for argument <code>attrName</code> are listed in <code>FS-Profile Manager</code>.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li><code>phoneNumber</code> is not a subscriber</li>
     * <li>an invalid value is defined for attrName</li>
     * <p>
     * The event message will contain the item that was errenous, that is
     * "phoneNumber" or "attrName".
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value: </b>The value for the setting with the specified name in the Active CoS which can be non atomic</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_DATANOTFOUND_EVENT    <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetStringAttribute:attrName=" + attrName, e); <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetStringAttribute:phoneNumber=" + phoneNumber, "phoneNumber not found");
     * </dd></dl>
     *
     * @param phoneNumber the phone number of the subscriber
     * @param attrName    the name of the attribute. See <code>FS-Profile Manager</code> for valid attribute names.
     * @return An array conmtaining the the values for the specified attribute. See <code>FS-Profile Manager</code> for valid attribute values.
     */
    public String[] subscriberGetStringAttribute(String phoneNumber, String attrName);

    /**
     * Returns the value for the specified integer attribute. See description for subscriberGetStringAttribute
     * <p/>
     * The valid attribute names are listed in <code>FS-Profile Manager</code>.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li><code>phoneNumber</code> is not a subscriber</li>
     * <li>an invalid value is defined for <code>attrName</code></li>
     * <p>
     * The event message will contain the item that was errenous, that is
     * "phoneNumber" or "attrName".
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value: </b>The value for the setting with the specified name in the Active CoS which can be non atomic</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_DATANOTFOUND_EVENT    <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetIntegerAttribute:attrName=" + attrName, e); <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetIntegerAttribute:phoneNumber=" + phoneNumber, "phoneNumber not found");
     * </dd></dl>
     *
     * @param phoneNumber the phone number of the subscriber
     * @param attrName    the name of the attribute. See <code>FS-Profile Manager</code> for valid attribute names.
     * @return An array containing the values for the specified attribute. See <code>FS-Profile Manager</code> for valid attribute values.
     */
    public int[] subscriberGetIntegerAttribute(String phoneNumber, String attrName);

    /**
     * Returns the value for the specified boolean attribute. See description for subscriberGetStringAttribute.
     * <p/>
     * The valid attribute names are listed in <code>FS-Profile Manager</code>.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li><code>phoneNumber</code> is not a subscriber</li>
     * <li>an invalid value is defined for <code>attrName</code></li>
     * <p>
     * The event message will contain the item that was errenous, that is
     * "phoneNumber" or "attrName".
     * </ul></ul>
     * <p>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value: </b>The value for the setting with the specified name in the Active CoS which can be non atomic</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_DATANOTFOUND_EVENT    <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetBooleanAttribute:attrName=" + attrName, e); <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetBooleanAttribute:phoneNumber=" + phoneNumber, "phoneNumber not found");
     * </dd></dl>
     *
     * @param phoneNumber the phone number of the subscriber
     * @param attrName    the name of the attribute. See <code>FS-Profile Manager</code> for valid attribute names.
     * @return An array containing the values for the specified attribute. See <code>FS-Profile Manager</code> for valid attribute values.
     */
    public boolean[] subscriberGetBooleanAttribute(String phoneNumber, String attrName);


    /**
     * Returns the value for the specified string attribute from the COS that the subscriber belongs to.
     * <p/>
     * The valid attribute names are listed in <code>FS-Profile Manager</code>.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li><code>phoneNumber</code> is not a subscriber</li>
     * <li>an invalid value is defined for <code>attrName</code></li>
     * <p>
     * The event message will contain the item that was errenous, that is
     * "phoneNumber" or "attrName".
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value: </b>The value for the setting with the specified name in the Active CoS.</dd>
     * <dd><b>Events: <br></b>
     * SSE_DATANOTFOUND_EVENT<br>
     * <p/>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetCosStringAttribute:attrName=" + attrName, e); <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetCosStringAttribute:phoneNumber=" + phoneNumber, "phoneNumber not found");
     * </dd></dl>
     *
     * @param phoneNumber the phone number of the subscriber
     * @param attrName    the name of the attribute. See <code>FS-Profile Manager</code> for valid attribute names.
     * @return An array containing the values for the specified attribute. See <code>FS-Profile Manager</code> for valid attribute values.
     */
    String[] subscriberGetCosStringAttribute(String phoneNumber, String attrName);

    /**
     * Returns the value for the specified integer attribute from the COS that the subscriber belongs to.
     * <p/>
     * The valid attribute names are listed in <code>FS-Profile Manager</code>.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li><code>phoneNumber</code> is not a subscriber</li>
     * <li>an invalid value is defined for <code>attrName</code></li>
     * <p>
     * The event message will contain the item that was errenous, that is
     * "phoneNumber" or "attrName".
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value: </b>The value for the setting with the specified name in the Active CoS.</dd>
     * <dd><b>Events: <br></b>
     * SSE_DATANOTFOUND_EVENT<br>
     * <p/>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetCosIntegerAttribute:attrName=" + attrName, e); <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetCosIntegerAttribute:phoneNumber=" + phoneNumber, "phoneNumber not found");
     * </dd></dl>
     *
     * @param phoneNumber the phone number of the subscriber
     * @param attrName    the name of the attribute. See <code>FS-Profile Manager</code> for valid attribute names.
     * @return An array containing the values for the specified attribute. See <code>FS-Profile Manager</code> for valid attribute values.
     */
    int[] subscriberGetCosIntegerAttribute(String phoneNumber, String attrName);

    /**
     * Returns the value for the specified boolean attribute from the COS that the subscriber belongs to.
     * <p/>
     * The valid attribute names are listed in <code>FS-Profile Manager</code>.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li><code>phoneNumber</code> is not a subscriber</li>
     * <li>an invalid value is defined for <code>attrName</code></li>
     * <p>
     * The event message will contain the item that was errenous, that is
     * "phoneNumber" or "attrName".
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value: </b>The value for the setting with the specified name in the Active CoS.</dd>
     * <dd><b>Events: <br></b>
     * SSE_DATANOTFOUND_EVENT<br>
     * <p/>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetCosBooleanAttribute:attrName=" + attrName, e); <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetCosBooleanAttribute:phoneNumber=" + phoneNumber, "phoneNumber not found");
     * </dd></dl>
     *
     * @param phoneNumber the phone number of the subscriber
     * @param attrName    the name of the attribute. See <code>FS-Profile Manager</code> for valid attribute names.
     * @return An array containing the values for the specified attribute. See <code>FS-Profile Manager</code> for valid attribute values.
     */
    boolean[] subscriberGetCosBooleanAttribute(String phoneNumber, String attrName);

    /**
     * Returns a list with the distribution list numbers that exist for the specified subscriber. The list is empty if
     * the subscriber does not have any distribution lists.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li><code>phoneNumber</code> is not a subscriber</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None</dd>
     * <dd><b>Return value:</b> A string array containing:<br>
     * {"07", "47", "11"}</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_DATANOTFOUND_EVENT    <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetDistributionListIds:attrName=" + attrName, e); <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetDistributionListIds:phoneNumber=" + phoneNumber, "phoneNumber not found");
     * </dd></dl>
     *
     * @param phoneNumber the phone number of the subscriber
     * @return An array containing the distribution list numbers
     */
    public String[] subscriberGetDistributionListIds(String phoneNumber);

    /**
     * Returns the specified greeting.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li><code>phoneNumber</code> is not a subscriber. The event message will contain the phoneNumber:
     * "subscriberGetGreeting:phoneNumber=" + phoneNumber
     * </li>
     * <li>no <code>greeting</code> is recorded that meets the specified parameters. The event
     * message will contain the following: "subscriberGetGreeting:greetingType=" + greetingType</li>
     * </ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li><code>greetingType</code> or <code>mediaType</code> is null.
     * The event message will contain the following: "subscriberGetGreeting:greetingType=" + greetingType</li>
     * </ul>
     * <b>error.com.mobeon.platform.mailbox. </b> is sent if:
     * <ul>
     * <li>An error occurs when retrieving the greeting. The event message will contain the following:
     * "subscriberGetGreeting:greetingType=" + greetingType</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value:</b> An audio or video media object according to parameter <code>mediaType</code>. The same object is returned regardless of other parameter values.</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_MAILBOX_EVENT     <br>
     * SSE_DATANOTFOUND_EVENT    <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "subscriberGetGreeting:greetingType=" + greetingType, e); <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetGreeting:greetingType=" + greetingType, e); <br>
     * throw new PlatformAccessException(EventType.SSE_MAILBOX_EVENT, "subscriberGetGreeting:greetingType=" + greetingType, e); <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetGreeting:phoneNumber=" + phoneNumber, "phoneNumber not found");
     * </dd></dl>
     *
     * @param phoneNumber  the phone number of the subscriber
     * @param greetingType the type of greeting, Valid values: "allcalls", "cdg", "temporary", "noAnswer", "busy",
     *                     "outOfHours", "ExternalAbsence".
     * @param mediaType    the type of media, Valid values: "voice", "video".
     * @param cdgNumber    the called dependent number. Only valid if greetingType="cdg".
     * @return the specified greeting
     */
    public IMediaObject subscriberGetGreeting(String phoneNumber, String greetingType, String mediaType, String cdgNumber);

    /**
     * <b>Not Implemented</b>. Always returns <code>0</code>.
     * <p>
     * When implemented, the behavior should be something like the following:
     * <p>
     * Starts fetching of the specified greeting. Asynchroneous function. To be used together with
     * function {@link PlatformAccess#subscriberGetGreeting(int)}.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li><code>phoneNumber</code> is not a subscriber</li>
     * <li>no <code>greeting</code> is recorded that meets the specified parameters</li>
     * </ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li><code>greetingType</code> or <code>mediaType</code> is null</li>
     * </ul></ul>
     * <p/>
     * Priority: 3
     * <p/>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Return value:</b>An ID number that can be used to fetch the result of the operation
     * with {@link #subscriberGetGreeting(int) subscriberGetGreeting}.
     * The result will be an audio or video media object according to parameter <code>mediaType</code>. The same object is returned regardless of other parameter values.
     * </dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_MAILBOX_EVENT     <br>
     * SSE_DATANOTFOUND_EVENT    <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "subscriberGetGreetingAsync:greetingType=" + greetingType, e); <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetGreetingAsync:greetingType=" + greetingType, e); <br>
     * throw new PlatformAccessException(EventType.SSE_MAILBOX_EVENT, "subscriberGetGreetingAsync:greetingType=" + greetingType, e); <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetGreetingAsync:phoneNumber=" + phoneNumber, "phoneNumber not found");
     * </dd></dl>
     *
     * @param phoneNumber  the phone number of the subscriber
     * @param greetingType the type of greeting, Valid values: "allcalls", "cdg", "temporary", "noAnswer", "busy",
     *                     "outOfHours", "ExternalAbsence".
     * @param mediaType    the type of media, Valid values: "voice", "video".
     * @param cdgNumber    the called dependent number. Only valid if greetingType="cdg".
     * @return a transactionId to be used when to wait or check for the result
     */
    public int subscriberGetGreetingAsync(String phoneNumber, String greetingType, String mediaType, String cdgNumber);

    /**
     * <b>Not Implemented</b>. Always returns <code>null</code>.
     * <p>
     * When implemented, the behavior should be something like the following:
     * <p>
     * Waits for the asynchroneous function {@link PlatformAccess#subscriberGetGreetingAsync(String, String, String, String)}
     * to finish.
     * <br>
     * If phoneNumber is not a subscriber or if no greeting is recorded the event error.com.mobeon.platform.SSE_PROFILEREAD_EVENT
     * is sent. The event message will contain the item that was not found, that is "phoneNumber" or "greeting".
     * <p/>
     * Priority: 3
     * <p/>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None.</dd>
     * <dd><b>Return value:</b> An audio or video media object. The type of  object returned depends on the parameter values
     * passed in the previous call to the {@link #subscriberGetGreetingAsync(String, String, String, String) subscriberGetGreetingAsync} method</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "subscriberGetGreeting:greetingType=" + greetingType, e); <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetGreeting:greetingType=" + greetingType, e); <br>
     * throw new PlatformAccessException(EventType.SSE_MAILBOX_EVENT, "subscriberGetGreeting:greetingType=" + greetingType, e);<br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetGreeting:phoneNumber=" + phoneNumber, "phoneNumber not found");
     * </dd></dl>
     *
     * @param transactionId the identification of the asynchrouneous request
     * @return the specified greeting
     */
    public IMediaObject subscriberGetGreeting(int transactionId);

    /**
     * Returns the identity of the subscribers default mailbox.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li>phoneNumber is not a subscriber</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Return value:</b> Always returns the number "1".</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_MAILBOX_EVENT         <br>
     * SSE_DATANOTFOUND_EVENT        <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SSE_MAILBOX_EVENT, "subscriberGetMailbox:phoneNumber=" + phoneNumber, ex);
     * </dd></dl>
     *
     * @param phoneNumber the phone number of the subscriber
     * @return the mailbox identity (mailboxId)
     */
    public int subscriberGetMailbox(String phoneNumber);

    /**
     * Returns the identity of the subscribers specified mailbox.
     * <b>Not fully implemented</b>, only <code>phoneNumber</code> is currentlry used.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li>phoneNumber is not a subscriber</li>
     * </ul></ul>
     * <p/>
     * Priority: 3
     * <p/>
     * <p/>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Return value:</b> Always returns the number "1".</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_MAILBOX_EVENT         <br>
     * SSE_DATANOTFOUND_EVENT        <br>
     * SSE_SYSTEM_EVENT
     * </dd></dl>
     *
     * @param phoneNumber the phone number of the subscriber
     * @param mailHost    the mailHost where the mailbox is located
     * @param accountId   the account identifier
     * @param accountPwd  the account password
     * @return the mailbox identity (mailboxId)
     * @deprecated Deprecated
     */
    public int subscriberGetMailbox(String phoneNumber, String mailHost, String accountId, String accountPwd);

    /**
     * Returns the identity of the subscribers operator.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>PlatformAccess cannot access the subscriber database</li>
     * </ul></ul>
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "subscriberGetOperatorName:phoneNumber=" + phoneNumber, ex);
     * </dd></dl>
     *
     * @param phoneNumber the phone number of the subscriber
     * @return the operator's identity or null if the subscriber is not found
     */
    public String subscriberGetOperatorName(String phoneNumber);

    /**
     * Returns the identity of the subscribers operator.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>PlatformAccess cannot access the subscriber database</li>
     * </ul></ul>
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "subscriberGetOperatorName:phoneNumber=" + phoneNumber, ex);
     * </dd></dl>
     *
     * @param phoneNumber the phone number of the subscriber
     * @return the operator's identity or null if the subscriber is not found
     */
    /**
     * Returns the value for the specified string attribute.
     * The lookup is made in Subscriber objects.
     * <p/>
     * Valid property names for argument <code>attributeName</code> are listed in <code>FS-Profile Manager</code>.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>PlatformAccess cannot access the subscriber database</li>
     * </ul></ul>
     * <p/>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetStringAttribute:attrName=" + attrName, e); <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetStringAttribute:phoneNumber=" + phoneNumber, "phoneNumber not found");
     * </dd></dl>
     *
     * @param operatorName to which the subscriber belongs.
     * @param phoneNumber of the subscriber
     * @param attributeName to lookup. See <code>FS-Profile Manager</code> for valid attribute names.
     * @return An array containing the values for the specified attribute.
     */
    public String operatorGetSubscriberSingleStringAttribute(String operatorName, String phoneNumber, String attributeName);

    /**
     * Returns the spoken name of the specified <code>mediaType</code>,
     * for the subsciber with the specified <code>phoneNumber</code>.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li>phoneNumber is not a subscriber</li>
     * <li>no spoken name is recorded of the specified type</li>
     * <p>
     * The event message will contain the item that was not found, that is "phoneNumber" or "mediaType".
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value:</b> An audio or video media object according to parameter <code>mediaType</code>. The same object is returned regardless of the phoneNumber parameter.</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT<br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "subscriberGetSpokenName:mediaType=" + mediaType, e);<br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetSpokenName:mediaType=" + mediaType, e);   <br>
     * throw new PlatformAccessException(EventType.SSE_MAILBOX_EVENT, "subscriberGetSpokenName:mediaType=" + mediaType, e);<br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberGetSpokenName:phoneNumber=" + phoneNumber, "phoneNumber not found");
     * </dd></dl>
     *
     * @param phoneNumber the phone number of the subscriber
     * @param mediaType   the type of media, Valid values: "voice", "video".
     * @return the specified spoken name
     */
    public IMediaObject subscriberGetSpokenName(String phoneNumber, String mediaType);

    /**
     * <b>Not Implemented</b>. Always returns <code>0</code>.
     * <p>
     * When implemented, the behavior should be something like the following:
     * <p>
     * Starts fetching of the spoken name. Asynchroneous function. Used together with method
     * {@link PlatformAccess#subscriberGetSpokenName(int)}.
     * <p/>
     * If phoneNumber is not a subscriber or if no spoken name is recorded the event error.com.mobeon.platform.datanotfound
     * is sent.
     * <p/>
     * Priority: 3
     * <p/>
     * <p/>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Return value:</b>An ID number that can be used to fetch the result of the operation
     * with {@link #subscriberGetSpokenName(int) subscriberGetSpokenName}.</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT<br>
     * SSE_SYSTEM_EVENT
     * </dd></dl>
     *
     * @param phoneNumber the phone number of the subscriber
     * @return a transactionId to be used when to wait or check for the result
     */
    public int subscriberGetSpokenNameAsync(String phoneNumber);

    /**
     * <b>Not Implemented</b>. Always returns <code>null</code>.
     * <p>
     * When implemented, the behavior should be something like the following:
     * <p>
     * Waits for the asynchroneous function {@link PlatformAccess#subscriberGetSpokenNameAsync(String)}
     * to finish. Returns the spoken name.
     * <p/>
     * Priority: 3
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None.</dd>
     * <dd><b>Return value:</b> An audio or video media object according to parameter <code>mediaType</code> passed to the previus call to {@link #subscriberGetSpokenNameAsync(String) subscriberGetSpokenNameAsync} method
     * The same object is returned regardless of the phoneNumber
     * passed in the previous call to the {@link #subscriberGetSpokenNameAsync(String) subscriberGetSpokenNameAsync} method</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT<br>
     * SSE_SYSTEM_EVENT
     * </dd></dl>
     *
     * @param transactionId the identification of the asynchrouneous request
     * @return the specified spoken name
     */
    public IMediaObject subscriberGetSpokenName(int transactionId);

    /**
     * Stores the specified value for the specified string attribute.
     * The valid attribute names are listed in <code>FS-Profile Manager</code>.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li><code>phoneNumber</code> is not a subscriber</li>
     * <li><code>attrName</code> is not valid</li>
     * </ul>
     * <b>error.com.mobeon.platform.profilewrite</b> is sent if:
     * <ul>
     * <li>some error occured when setting the attribute</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>Sets the value for the setting with the specified name in the Active CoS.<br>
     * If no setting with the specified name could be found, the call to this method has no effect.</dd>
     * <dd><b>Return value: </b>None.</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT   <br>
     * SSE_PROFILEWRITE_EVENT <br>
     * SSE_DATANOTFOUND_EVENT     <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SSE_PROFILEWRITE_EVENT, "subscriberSetStringAttribute:attrName=" + attrName, e); <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberSetStringAttribute:phoneNumber=" + phoneNumber, "phoneNumber not found");
     * </dd></dl>
     *
     * @param phoneNumber the phone number of the subscriber
     * @param attrName    the name of the attribute. See <code>FS-Profile Manager</code> for valid attribute names.
     * @param attrValues  the values for the attribute If single value the first element in the array is used. If null, the attribute is deleted.
     *                    See <code>FS-Profile Manager</code> for valid attribute values.
     */
    public void subscriberSetStringAttribute(String phoneNumber, String attrName, String[] attrValues);

    public void subscriberSetStringAttribute(String phoneNumber, String attrName, String[] attrValues, Modification.Operation op);

    /**
     * Stores the specified value for the specified integer attribute.
     * The valid attribute names are listed in <code>FS-Profile Manager</code>.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li><code>phoneNumber</code> is not a subscriber</li>
     * <li><code>attrName</code> is not valid</li>
     * </ul>
     * <b>error.com.mobeon.platform.profilewrite</b> is sent if:
     * <ul>
     * <li>some error occured when setting the attribute</li>
     * </ul>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>Sets the value for the setting with the specified name in the Active CoS.</dd>
     * <dd><b>Return value: </b>None.</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT             <br>
     * SSE_PROFILEWRITE_EVENT                <br>
     * SSE_DATANOTFOUND_EVENT                    <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SSE_PROFILEWRITE_EVENT, "subscriberSetIntegerAttribute:attrName=" + attrName, e);<br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberSetIntegerAttribute:phoneNumber=" + phoneNumber, "phoneNumber not found");
     * </dd></dl>
     *
     * @param phoneNumber the phone number of the subscriber
     * @param attrName    the name of the attribute. See <code>FS-Profile Manager</code> for valid attribute names.
     * @param attrValues  the values for the attribute If single value the first element in the array is used. If null the attribute is deleted.
     *                    See <code>FS-Profile Manager</code> for valid attribute values.
     */
    public void subscriberSetIntegerAttribute(String phoneNumber, String attrName, int[] attrValues);

    /**
     * Stores the specified value for the specified boolean attribute.
     * <p>
     * The valid attribute names are listed in <code>FS-Profile Manager</code>.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li><code>phoneNumber</code> is not a subscriber</li>
     * <li><code>attrName</code> is not valid</li>
     * </ul>
     * <b>error.com.mobeon.platform.profilewrite</b> is sent if:
     * <ul>
     * <li>some error occurs when setting the attribute</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>Sets the value for the setting with the specified name in the Active CoS.<br>
     * If no setting with the specified name could be found, the call to this method has no effect.</dd>
     * <dd><b>Return value: </b>None.</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_PROFILEWRITE_EVENT    <br>
     * SSE_DATANOTFOUND_EVENT        <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SSE_PROFILEWRITE_EVENT, "subscriberSetBooleanAttribute:attrName=" + attrName, e);       <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberSetBooleanAttribute:phoneNumber=" + phoneNumber, "phoneNumber not found");
     * </dd></dl>
     *
     * @param phoneNumber the phone number of the subscriber
     * @param attrName    the name of the attribute. See <code>FS-Profile Manager</code> for valid attribute names.
     * @param attrValues  the values for the attribute If single value the first element in the array is used. If null, the attribute is deleted.
     *                    See <code>FS-Profile Manager</code> for valid attribute values.
     */
    public void subscriberSetBooleanAttribute(String phoneNumber, String attrName, boolean[] attrValues);

    /**
     * Stores the specified greeting.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li><code>phoneNumber</code> is not a subscriber</li>
     * </ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li><code>greetingType</code> null or invalid</li>
     * <li><code>mediaType</code> is null or invalid</li>
     * </ul>
     * <b>error.com.mobeon.platform.mailbox</b> is sent if:
     * <ul>
     * <li>some error occurs when setting the greeting</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_MAILBOX_EVENT     <br>
     * SSE_DATANOTFOUND_EVENT    <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "subscriberSetGreeting:greetingType=" + greetingType, e);<br>
     * throw new PlatformAccessException(EventType.SSE_MAILBOX_EVENT, "subscriberSetGreeting:greetingType=" + greetingType, e);        <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberSetGreeting:phoneNumber=" + phoneNumber, "phoneNumber not found");
     * </dd></dl>
     *
     * @param phoneNumber  the phone number of the subscriber
     * @param greetingType the type of greeting, Valid values: "allcalls", "cdg", "temporary", "noAnswer", "busy",
     *                     "outOfHours", "ExternalAbsence".
     * @param mediaType    the type of media, Valid values: "voice", "video".
     * @param cdgNumber    the called dependent number. Only valid if greetingType="cdg".
     * @param greeting     the greeting, if null the greeting is deleted
     */
    public void subscriberSetGreeting(String phoneNumber, String greetingType, String mediaType, String cdgNumber, IMediaObject greeting);

    /**
     * Stores the spoken name for the subscriber with the specified phoneNumber.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li><code>phoneNumber</code> is not a subscriber</li>
     * </ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li><code>mediaType</code> is null or not valid</li>
     * </ul>
     * <b>error.com.mobeon.platform.mailbox</b> is sent if:
     * <ul>
     * <li>some error occurs when setting the greeting</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_MAILBOX_EVENT <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "subscriberSetSpokenName:mediaType=" + mediaType, e); <br>
     * throw new PlatformAccessException(EventType.SSE_MAILBOX_EVENT, "subscriberSetSpokenName:mediaType=" + mediaType, e); <br>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "subscriberSetSpokenName:phoneNumber=" + phoneNumber, "phoneNumber not found");
     * </dd></dl>
     *
     * @param phoneNumber the phone number of the subscriber
     * @param mediaType   the type of media, an be one of "voice", "video".
     * @param spokenName  the spokenname, if null the spokenname is deleted.
     */
    public void subscriberSetSpokenName(String phoneNumber, String mediaType, IMediaObject spokenName);
    
    /**
     * Removes the subscriber from the MCD client cache.
     * 
     * @param phoneNumber the phone number of the subscriber
     * @return true if the subscriber was successfully removed from the cache
     *          (also returns true if the cache is dissabled or the profile was not present in the cache).
     *          False if there is an error.
     */
    public boolean subscriberRemoveFromCache(String phoneNumber);

    /**
     * Creates a new subscriber in the user directory.
     * <p/>
     * For valid attibutenames and corresponding values, see <code>IWD - Provisioning Interface (CAI)</code>.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the number of <code>attrNames</code> is different from the numbers of <code>attrValues</code></li>
     * <li>an error occurs when creating the subscriber</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_PROFILEWRITE_EVENT   <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "subscriberCreate:attrNames.length != attrValues.length"); <br>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "subscriberCreate:", e);
     * </dd></dl>
     *
     * @param attrNames  list of attribute names for the subscriber, the attrValues with the same index corresponds to this name.
     *                   Valid attribute names are defined in <code>IWD - Provisioning Interface (CAI)</code>.
     * @param attrValues list of attribute values for the subscriber, the attrNames with the same index corresponds to this name.
     *                   Valid values for each attribute is defined in <code>IWD - Provisioning Interface (CAI)</code>.
     * @param adminUid   uid for an useradmin in the user directory.
     * @param cosName    name on Class of service (CoS) to use for the new
     *                   subscriber (optional, use null if not included).
     */
    //public void subscriberCreate(String[] attrNames, String[] attrValues, String adminUid, String cosName);

    /**
     * Deletes a subscriber from the user directory.
     *
     *
     * <b> THIS METHOD IS NOT IMPLEMENTED CORRECTLY </b>
     *
     *
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>an error occurs when deleting the subscriber</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT <br>
     * SSE_PROFILEWRITE_EVENT   <br>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SYSTEMERROR, "subscriberDelete:", e);
     * </dd></dl>
     *
     * @param telephoneNumber identifies the subscriber to delete.
     * @param adminUid        uid for an useradmin in the user directory.
     */
    //public void subscriberDelete(String telephoneNumber, String adminUid);

    /**
     * Analyzes a number according to rule and optional region code.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.numberanalysis</b> is sent if:
     * <ul>
     * <li>the number analysis detects a number length error or that the number is blocked</li>
     * <p>
     * The event message shall contain one of the strings "NOMATCH, NORULE, BLOCKED", "MIN=x", "MAX=y"
     * or "EXACTLY=z" depending on the situation.
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None.</dd>
     * <dd><b>Return value:</b> Returns the value passed in the phoneNumber parameter.<br>
     * The rule and informationContainingRegionCode parameters are ignored.</dd>
     * <p/>
     * <dd><b>Events: <br></b>
     * SSE_NUMBERANALYSIS_EVENT <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.NUMBERANALYSIS, msg, e.getMessage());
     * <p>The msg shall contain the following strings depending of the error of the number analysis:
     * 111111: systemAnalyzeNumber:NOMATCH%Any text <br>
     * 222222: systemAnalyzeNumber:NORULE%Any text      <br>
     * 333333: systemAnalyzeNumber:BLOCKED%Any text         <br>
     * lentgh less then 4 digits: systemAnalyzeNumber:MIN=4%Any text<br>
     * length greater then 12 digits: systemAnalyzeNumber:MAX=12%Any text<br>
     * 444444: systemAnalyzeNumber:EXACTLY=8%Any text
     * </dd></dl>
     *
     * @param rule        the rule to use for the analysis
     * @param phoneNumber the phone number to analyze
     * @param informationContainingRegionCode
     *                    Optional. A phone number containing region code that shall be used in the analysis. Use null if not included.
     * @return the analyzed number
     */
    public String systemAnalyzeNumber(String rule, String phoneNumber, String informationContainingRegionCode);

    /**
     * Returns the value for the specified configuration parameter.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li>the group is unknown</li>
     * <li>parameter does not exist</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None</dd>
     * <dd><b>Return value:</b> The value of the setting with the attribute
     * id=parameterName in the SystemSettings.xml file belonging to the simulated call flow.
     * <br>The section parameter is ignored since the Simulation Engine does not support hierarchical
     * settings</dd>
     * <dd><b>Events: <br></b>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT
     * <p/>
     * throw new PlatformAccessException(EventType.SSE_DATANOTFOUND_EVENT, "systemGetConfigurationParameter:parameterName=" + parameterName, e);
     * </dd></dl>
     *
     * @param group         the group where to find the configuration parameter. Specified as <code>"topgroup"."subgroup"."subsubgroup"...</code>
     * @param parameterName the name of the configuration parameter to fetch
     * @return the value of the parameter
     */
    public String systemGetConfigurationParameter(String group, String parameterName);

    /**
     * Returns the value for the specified configuration parameter.
     * This method can get a parameter that is part of a group even if they are grouped together with the same name.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li>the group is unknown</li>
     * <li>parameter does not exist</li>
     * </ul>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> None</dd>
     * <dd><b>Events: <br></b>
     * SSE_DATANOTFOUND_EVENT <br>
     * SSE_SYSTEM_EVENT
     * </dd>
     * </dl>
     *
     * @param group         the group where to find the configuration parameter. Specified as <code>"topgroup"."subgroup"."subsubgroup"...</code>
     * @param parameterName the name of the configuration parameter to fetch
     * @param groupIdName   the name on a parameter in the group to use as key when searching for the parameter.
     * @param groupIdValue  the value on the parameter specified by groupIdName.
     * @return the value of the parameter
     */
    //This method doesn't seem to be used anymore. Let's see what happens if it removed.
  /*  public String systemGetConfigurationGroupParameter(String group, String parameterName,
                                                       String groupIdName, String groupIdValue);*/

    /**
     * Returns a list of IMediaObjects that represents the media content identity.
     * <p>
     * For an explanation of Media Content's, Qualifiers and Conditions, see <code>FS-Media Content Manager</code> and
     * <code>FD-Media Content Manager</code>.
     * <p>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the type or identity does not exist</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None.</dd>
     * <dd><b>Return value:</b> If the type is Prompt, an array with the MediaObjects associated with the prompt will be returned.<br>
     * In any other case, an array containing the same single MediaObject is returned regardless of the values of the parameters.</dd>
     * <dd><b>Events: <br></b>
     * SSE_SYSTEM_EVENT   <br>
     * SSE_DATANOTFOUND_EVENT
     * </dd></dl>
     *
     * @param mediaContentResourceType the type of Media Content Resource. Valid values: "Prompt", "SWA" or "FunGreeting"
     * @param mediaContentIdentity     the media content identifier
     * @return a list of IMediaObjects
     */
    public IMediaObject[] systemGetMediaContent(String mediaContentResourceType, String mediaContentIdentity);

    /**
     * Returns a list of IMediaObjects that represents the media content identity.
     * <p/>
     * The <code>qualifiers</code> argument has two purposes:
     * <ul>
     * <li>To select the media content,
     * which condition matches the qualifier list</li>
     * <li>If the content has qualifiers as part of the actual media: To be converted to their
     * Media Object representation and be part of the returned list of media.</li>
     * </ul>  <br>
     * For an explanation of Media Content's, Qualifiers and Conditions, <code>see FS-Media Content Manager and</code>
     * <code>FD-Media Content Manager</code>.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the type or identity does not exist. The event message will contain the item
     * that was not found, that is "mediaContentResourceType" and "mediaContentIdentity"</li>
     * <li>an invalid qualifier is used</li>
     * <li>no resource is selected. The event message will contain the string "No MediaContentResource is selected"</li>
     * <li>some error occurs when creating the media objects.</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None.</dd>
     * <dd><b>Return value:</b> If the type is Prompt, an array with the MediaObjects associated with the prompt will be returned.<br>
     * In any other case, an array containing the same single MediaObject is returned regardless of the values of the parameters.</dd>
     * <dd><b>Events: <br></b>
     * SSE_SYSTEM_EVENT
     * </dd></dl>
     *
     * @param mediaContentResourceType the type of Media Content Resource. Valid values: "Prompt", "SWA" or "FunGreeting"
     * @param mediaContentIdentity     the media content identifier
     * @param qualifier                a list of qualifiers to the media content. Optional.
     * @return a list of IMediaObjects
     */
    public IMediaObject[] systemGetMediaContent(String mediaContentResourceType, String mediaContentIdentity, IMediaQualifier[] qualifier);

    /**
     * Returns a list of MediaContent identities for all media content of the specified type, that
     * matches the passed list of qualifiers.
     * The list is empty if no media content is found.
     * <p/>
     * For an explanation of Media Content's, Qualifiers and Conditions, see <code>FS-Media Content Manager</code> and
     * <code>FD-Media Content Manager</code>.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the type does not exist. The event message will contain the item
     * that was not found, that is "mediaContentResourceType=" + missing type</li>
     * <li>no media content resource is selected. A resource must have been selected.
     * The event message will contain the string "No MediaContentResource is selected" and the
     * type, that is "mediaContentResourceType=" + type</li>
     * <li>some error occurs when retrieving contentIds. The event message will contain the type
     * searched for, that is "mediaContentResourceType=" + type</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None.</dd>
     * <dd><b>Return value:</b> If the type is Prompt, an array with the IDs of all Prompts is returned.
     * If any other MediaContentResourceType is specifed, an array with a single element containing
     * the number "1" is returned.<br>
     * The qualifier parameter is ignored since it's only used for resource types SWA and FunGreeting
     * which are not supported during simulation.</dd>
     * <dd><b>Events: <br></b>
     * SSE_SYSTEM_EVENT
     * </dd></dl>
     *
     * @param mediaContentResourceType the type of Media Content Resource. Valid values: "Prompt", "SWA" or "FunGreeting".
     * @param qualifier                a list of qualifiers to the media content. Optional, if null all contents
     *                                 of the specified type is returned.
     * @return a list of MediaContent identities
     */
    public String[] systemGetMediaContentIds(String mediaContentResourceType, IMediaQualifier[] qualifier);

    /**
     * Returns a list with the subscribers that matches the specified attribute and value. For example "uid", "161074".
     * The list is empty if no subscribers are found. A system parameter limits the maximum number of hits.
     * <p/>
     * Valid attributes is defined in <code>IWD - Provisioning Interface (CAI)</code>.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.profileread</b> is sent if:
     * <ul>
     * <li>the attribute is invalid</li>
     * <li>some error occurs when reading from profile</li>
     * <p>The event message will contain the invalid attribute and value as:
     * "systemGetSubscribers:attribute=" + attribute + ", value" + value
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value:</b> An array containing the following strings:<br>
     * {"46701111111", "46702222222", "46703333333"}</dd>
     * <dd><b>Events: <br></b>
     * SSE_PROFILEREAD_EVENT  <br>
     * SSE_DATANOTFOUND_EVENT     <br>
     * SSE_SYSTEM_EVENT
     * </dd></dl>
     *
     * @param attribute the attribute to use when specifying the search. See <code>IWD - Provisioning Interface (CAI)</code>
     *                  for valid attribute names.
     * @param value     the value to use when specifying the search.
     * @return a list of phone numbers to the matching subscribers
     */
    public String[] systemGetSubscribers(String attribute, String value);

    /**
     * <b>Not Implemented</b>. Always returns <code>0</code>.
     * <p>
     * When implemented, the behavior should be something like the following:
     * <p>
     * Checks if the asynchroneous request with the specified id is finished.
     * <p/>
     * Priority: 3
     * <p/>
     * <p/>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None.</dd>
     * <dd><b>Return value:</b> True. (All operations are performed synchronously)</dd>
     * <dd><b>Events: <br></b>
     * </dd></dl>
     *
     * @param transactionId the identification of the asynchrouneous request
     * @return true if transaction is finished and false if transaction is not finished
     */
    public boolean systemIsAsyncFinished(int transactionId);

    /**
     * Sends a service request. This function shall be used in conjunction with systemGetServiceResponseParameter
     * and systemGetServiceResponseHeaderParameter.
     * <br>
     * If a response has been required by setting ReportIndication to true, there will always be a response available to the
     * application. If a response was successfully received from the server, this response is available. If no
     * response was received, a response will be internally generated by the platform. This response will have statuscode 421.
     * <br>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>the parameters are invalid (for example different length of the parameterNames and parameterValues arrays)</li>
     * <li>there are internal errors</li>
     * <p>The event message will describe the problem.
     * </ul>
     * </ul>
     * <dl><b>Simulation details:</b> Not implemented
     * </dl>
     * @param hostName if non-null, the service request will be sent to this host. If null, the platform will select a host.
     * @param serviceName of the service, e.g. ExternalSubscriberInformation.
     * @param ValidityTime The time in seconds this request is valid. The function will wait this time for the response
     * from the server.
     * @param ReportIndication If the server should send a response on this request.
     * @param parameterNames An array of all parameter names. The related value of each parameter is in the same array
     * position in parameter parameterValues. Use an empty array if there are no parameters to supply.
     * @param parameterValues An array of all parameter values. The related name of each parameter is in the same array
     * position in parameter parameterNames. Use an empty array if there are no parameters to supply.
     *
     */
    public void systemSendServiceRequest(String hostName,
                                        String serviceName,
                                        int ValidityTime,
                                        boolean ReportIndication,
                                        String [] parameterNames,
                                        String [] parameterValues);

    /**
     * Returns the value for a service response parameter from the response. This function
     * shall be used in conjunction with systemSendServiceRequest.
     *
     * <br>
     * <b>Events:</b>
     * <ul>
     *
     * <li>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>this function is invoked and there was no response for the request.</li>
     * <li>the parameter is null</li>
     * <li>there are internal errors</li>
     *</ul>
     *</ul>
     * </li>
     * <li>
     * <b>error.com.mobeon.platform.datanotfound</b> is sent if:
     * <ul>
     * <li>the requested parameter does not exist in the response.</li>
     * </ul>
     * </li>
     *
     * <p>The event message will describe the problem.
     * </ul>
     * <dl><b>Simulation details:</b> Not implemented
     * </dl>
     * @param parameterName The name of the parameter.
     * @return the value of the parameter
     */

    public String systemGetServiceResponseParameter(String parameterName);

    /**
     * Returns the value for a header parameter, from the last received service response. This function
     * shall be used in conjunction with systemSendServiceRequest.
     *
     *
     * <br>
     * <b>Events:</b>
     * <ul>
     *
     * <li>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>this function is invoked before a service response has been received.</li>
     * <li>there is an attempt to retrieve a non-predefined header parameter</li>
     * <li>the parameter is null</li>
     * <li>there are internal errors</li>
     * </ul>
     * </li>
     * <br>
     * <p>The event message will describe the problem.
     * </ul>
     * <dl><b>Simulation details:</b> Not implemented
     * </dl>
     * @param parameterName The name of the header parameter. The following are predefined parameter names in XMP:
     * TransactionId, ClientId, StatusCode, StatusText
     * @return the value of the parameter
     */
    public String systemGetServiceResponseHeaderParameter(String parameterName);


    /**
     * Sets the Media Resources that shall be used for the call.
     * Media Resources for all available types (e.g. Prompt, SWA or Fun Greeting) shall be allocated.
     * If this method is used before that the call has been accepted the media resources are set based on both the
     * filter and on the incoming call parameters.
     * <p>
     * If the call is a voice call the voiceVariant is used. If the call is a video call the videoVariant is used.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>no resource is found with the specified language and variant.
     * The event message will contain the string "MediaContentResources are empty".</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect:</b> Set the system settings SSE_LANG, SSE_VOICE_VARIANT, SSE_VIDEO_VARIANT  according to parameters.</dd>
     * <dd><b>Events: <br></b>
     * SSE_SYSTEM_EVENT
     * </dd></dl>
     *
     * @param language     the language to be used for the prompts according to <code>RFC2068</code>
     * @param voiceVariant the variant to be used for the voiceprompts
     * @param videoVariant the variant to be used for the videoprompts
     */
    public void systemSetMediaResources(String language, String voiceVariant, String videoVariant);

    /**
     * Set the Media Resource for a specified type.
    * <dl><b>Simulation details:</b>  Not implemented
     * </dl>
     * @param mediaContentResourceType   the media Content Resource Type:  PROMPT, FUNGREETING or SWA
     * @param language                   the language to be used for the prompts according to <code>RFC2068</code>
     * @param voiceVariant               the variant to be used for the voiceprompts
     * @param videoVariant               the variant to be used for the videoprompts
     */
    public void systemSetMediaResource(String mediaContentResourceType,
                                       String language,
                                       String voiceVariant,
                                       String videoVariant);
    /**
     * Sets the early Media Resources that shall be used for the call.
     * See also description for {@link PlatformAccess#systemSetMediaResources(String, String, String)}.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>no resource is found with the specified language and variant.
     * The event message will contain the string "MediaContentResources are empty".</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Events: <br></b>
     * SSE_SYSTEM_EVENT
     * </dd></dl>
     *
     * @param language     the language to be used for the prompts according to <code>RFC2068</code>
     * @param voiceVariant the variant to be used for the voiceprompts
     * @param videoVariant the variant to be used for the videoprompts
     */
    public void systemSetEarlyMediaResource(String language, String voiceVariant, String videoVariant);
    
    /**
     * Sets the early Media Resources that shall be used for the call.
     * Also allows call flow to add extra headers to the SIP 183 Session Progress response for early media.
     * See also {@link PlatformAccess#systemSetEarlyMediaResource(String, String, String)} 
     * <p/>
     * 
     * The type of headers that can be added is limited to proprietary SIP headers
     * in order to reduce the risk of interfering with normal SIP signalling. See Rules: section below <br/> <br/>
     * <p/>
     * 
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>no resource is found with the specified language and variant.
     * The event message will contain the string "MediaContentResources are empty".</li>
     * <li>extraProprietaryHeadersNames or extraProprietaryHeadersValues are not valid.
     * The event message will contain error details. See below for validity rules.</li>
     * </ul></ul>
     * <p/>
     * 
     * <b>Rules: </b>
     * <ul>
     * <li>Only accepts header names starting with "X-", "x-", "P-" or "p-". </li>
     * <li>The following standard "P-" headers processed by the SIP stack are not accepted: 
     * P-Asserted-Identity, P-Preferred-Identity and P-Charging-Vector.</li>
     * <li>P-Early-Media must use a valid value [RFC 5009]  (eg: sendrecv, sendonly, etc.)<br>
     * P-Early-Media value could be overwritten by MAS/Call Manager when 'pEarlyMediaHeaderInSipResponse' feature enabled.</li>
     * <li>There is no extra validation done for the values of the other proprietary headers. </li>
     * 
     * <li>'extraProprietaryHeadersNames' and 'extraProprietaryHeadersValues' cannot be null
     * and must have the same amount of values.</li>
     * </ul>
     * 
     * 
     * <pre>
     * Usage example: In libMas.js
     * 
     * function System_SetEarlyMediaWithExtraHeaders() {
     *  var language = mas.systemGetConfigurationParameter('vva.media', 'defaultlanguage');
     *  var variantVoice = mas.systemGetConfigurationParameter('vva.media', 'variantvoice');
     *  var variantVideo = mas.systemGetConfigurationParameter('vva.media', 'variantvideo');
     *  
     *  var headerNames  = [];
     *  var headerValues = [];
     *  
     *  headerNames.push('X-New-Header');
     *  headerValues.push('value for first header');
     *          
     *  mas.systemSetEarlyMediaResource(language, variantVoice, variantVideo, headerNames, headerValues);
     * }
     * 
     * 
     * </pre>
     * 
     * @param language {@link #systemSetEarlyMediaResource(String, String, String)}
     * @param voiceVariant {@link #systemSetEarlyMediaResource(String, String, String)}
     * @param videoVariant {@link #systemSetEarlyMediaResource(String, String, String)}
     * @param extraProprietaryHeadersNames Names for extra SIP Headers. MUST start with "P-" or "X-". 
     *        P-Asserted-Identity, P-Preferred-Identity and P-Charging-Vector are excluded.
     * @param extraProprietaryHeadersValues Values for extra SIP Headers. 
     */
    public void systemSetEarlyMediaResource(String language, String voiceVariant, String videoVariant,
                                            String[] extraProprietaryHeadersNames, String[] extraProprietaryHeadersValues);

    /**
     * Indicates if the searches shall be limited to the user directory in the local sub/domain
     * for example when using iMux.
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Events: <br></b>
     * </dd></dl>
     *
     * @param limit true if limit scope, false if not
     */
    public void systemSetPartitionRestriction(boolean limit);


    /**
     * Sets a property with the specified value. The properties are defined in the vxml standard, chapter (6.3.2 - 6.3.6).
     * <p/>
     * These following <b>additional properties</b> exists in the Platform:
     * <ul>
     * <li>com.mobeon.platform.transfer_local_pi</li>
     * <li>com.mobeon.platform.transfer_maxtime</li>
     * <li>com.mobeon.platform.transfer_connecttimeout</li>
     * <li>com.mobeon.platform.transfer_ani</li>
     * <li>com.mobeon.platform.audio_offset</li>
     * <li>com.mobeon.platform.record_maxtime</li>
     * </ul>
     * <p/>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Events: <br></b>
     * </dd></dl>
     *
     * @param name  Name on the property.
     * @param value Value on the property.
     */
    public void systemSetProperty(String name, String value);

    /**
     * Returns the value for the specified servicerequest parameter.
     * The parameters names are defined in the <code>XMP IWD</code>.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>no service request exist in session.</li>
     * </ul>
     * </ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Return value:</b> empty string</dd>
     * <dd><b>Events: <br></b>
     * </dd></dl>
     *
     * @param name the name on the requested parameter. See <code>XMP IWD</code> for valid names.
     * @return value for the requested parameter. See <code>XMP IWD</code> for valid parameter values.
     * If the parameter does non exist, null is returned.
     */
    public String systemGetServiceRequestParameter(String name);

    /**
     * Sends a SIP message indicating "message waiting". (Note this method is generically designed and intended for
     * other uses in the future.)
     *
     * The method should be called from CCXML. The method is asynchronous, and the result will
     * be delivered as a "com.mobeon.platform.sipmessageresponseevent" event.
     * <br><b>Events:</b>
     * <ul>
     * <li><b>com.mobeon.platform.sipmessageresponse</b> The event is sent when a final response has
     *  arrived on SIP. A provisional response (1xx) will not cause generation of this event.
     * See the SIP RFC for details on final and provisional responses. This event will have the following properties:
     * <br>
     * <ul>
     * <li>responsecode This is the SIP response code describing the outcome of the request. Valid values are defined
     *                  in the SIP RFC, 3261.
     * <li> responsetext This is a human-readable text describing the response. It could be used for logging purposes.
     * <li> retryafter In case responsecode identifies a temporary error, retryafter is a suggestion how long
     *                to wait before trying again. Unit: milliseconds. The retryafter property sometimes has the value
     *                "ECMA undefined".
     * </ul>
     * </ul>
     *
     * <dl><b>Simulation details:</b> None
     * </dl>
     *
     * @param messageName The string "messagewaiting"
     * @param paramNames An array of parameter names. The value corresponding to each parameter name
     * is found at the same position in the paramValues array. For example, paramNames[3] corresponds to paramValues[3].
     * Note that if only one parameter of the same type is supplied (example: voicemessagenew is supplied but not
     * voicemessageold), the value of the other parameter will be given the value 0 in the SIP request.
     * </br> Available parameters:
     * <ul>
     *      <li>"sendto" - The recipient of the notify request. A E.164 phone number. (Mandatory)
     *      <li>"messageaccount" - The account to which this request corresponds. A E.164 phone number. (Mandatory)
     *      <li>"messageswaiting" - Indicate if there are any new messages.
     *       Must be either "yes" or "no". If this parameter is unset the default behaviour
     *      is to set the messages waiting indicator to "yes" if any of the given xxxmessagenew
     *      parameters is not 0 (zero) otherwise is will be set to "no". (Optional.)
     *      <li>"voicemessagenew" - The number of new voice messages. (Optional, default = "0")
     *      <li>"voicemessageold" - The number of old voice messages. (Optional, default = "0")
     *      <li>"faxmessagenew" - The number of new fax messages. (Optional, default = "0")
     *      <li>"faxmessageold" - The number of old fax messages. (Optional, default = "0")
     *      <li>"videomessagenew" - The number of new video messages. (Optional, default = "0")
     *      <li>"videomessageold" - The number of old video messages. (Optional, default = "0")
     *      <li>"emailmessagenew" - The number of new email messages. (Optional, default = "0")
     *      <li>"emailmessageold" - The number of old email messages. (Optional, default = "0")
     *      <li>"outboundcallserverhost" - The SIP server host to contact. (Optional, if omitted
     *           the platform will contact a default SIP server)
     *      <li>"outboundcallserverport" - The SIP server port to contact. (Optional, if omitted the platform will contact the
     *          outboundcallserverhostname on default port.)

     * </ul>
     * @param paramValues An array of parameter values. The name corresponding to each parameter value
     *  found at the same position in the paramNames array.
     * <br>
     */
    public void systemSendSIPMessage(String messageName, String[] paramNames, String[] paramValues) throws IllegalArgumentException;

    /**
     * Sends a service response. StatusCode and StatusText can be set. Also see <code>XMP IWD</code>.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>some error occurs when sending the response</li>
     * <li>the number of param names differs from the number of values.
     * The event message will contain "systemSendServiceResponse:paramName.length != paramValue.length"</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Events: <br></b>
     * SSE_SYSTEM_EVENT
     * </dd></dl>
     *
     * @param statusCode service response code. See <code>XMP IWD</code>.
     * @param statusText service response text. See <code>XMP IWD</code>.
     * @param paramName  a list with service response parameter names  (optional may be null). See <code>XMP IWD</code>..
     * @param paramValue a list with service response parameter values (optional may be null). See <code>XMP IWD</code>.
     */
    public void systemSendServiceResponse(int statusCode, String statusText, String[] paramName, String[] paramValue);

    /**
     * Sends a traffic event with the specified data.
     * <p>See <code>FS Traffic Event Sender</code> for details.
     * <p/>
     * <b>Events:</b>
     * <ul>
     * <b>error.com.mobeon.platform.system</b> is sent if:
     * <ul>
     * <li>some error occurs when sending the event.
     * The event message will contain "trafficEventSend:eventName=" + eventName</li>
     * <li>the number of param names differs from the number of values.
     * The event message will contain "trafficEventSend:propertyName.length != propertyValue.length"</li>
     * </ul></ul>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Events: <br></b>
     * SSE_SYSTEM_EVENT
     * </dd></dl>
     *
     * @param eventName        the name to set for the traffic event. See <code>FS Traffic Event Sender</code>
     * @param propertyName     a list with the property names for the event, the propertyValue with the same index corresponds to this name.
     *                         See <code>FS Traffic Event Sender</code>.
     * @param propertyValue    a list with the property values for the event, the propertyName with the same index corresponds to this value.
     *                         See <code>FS Traffic Event Sender</code>.
     * @param restrictEndUsers true if endusers shall not receive this event
     */
    public void trafficEventSend(String eventName, String[] propertyName, String[] propertyValue, boolean restrictEndUsers);

    /**
     * Closes the external resources that are used in this PlatformAccess. Shall be called when the call has ended.
     * <p/>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Events: <br></b></dl>
     */
    public void close();

    /**
     * Sends a message to the log file.
     * <p/>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>Log the string in  <code>msg</code> </dd>
     * <dd><b>Events: <br></b></dl>
     *
     * @param severity loglevel 0-fatal, 1-error, 2-warn, 3-info, 4-debug
     * @param msg      The message written fo log.
     */
    public void systemLog(int severity, String msg);

    /**
     * <b>Not Implemented</b>. Always returns <code>0</code>.
     * <p>
     * When implemented, the behavior should be something like the following:
     * <p>
     * Sends DTMF.
     * <p/>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Events: <br></b>
     * SSE_SYSTEM_EVENT
     * </dd></dl>
     *
     * @param utterance     A string of in the alphabet ?0 ? 9, A,B,C,D, *, #,+?.
     *                      The ?+? character inserts a pause of pause_interval length
     * @param paus_interval The length of the pause induced by ?+? in milliseconds
     */
    public void systemSendDTMF(String utterance, int paus_interval);


    /**
     * Checks if runtime environment is simulator or production platform.
     * <p/>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Returns: </b>always return true</dd>
     * </dd></dl>
     *
     * @return always return false
     */
    public boolean systemIsSimulated();

    /**
     * New for VCP.
     *
     * Retrieves a cached media. If not present it will be fetched and added to the cache.
     * <p/>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Events: <br></b></dl>
     *
     * @param url url for the media.
     * @return the media object, null if not found.
     */
    public IMediaObject getCachedMedia(String url);

    /**
     * New for VCP.
     * <p/>
     * Retrieves the base URI for the application as a string.
     * <p/>
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Events: <br></b>
     * </dd></dl>
     *
     * @return Base URI string for the application (ex: file:/opt/moip/mas/applications/vva001/)
     */
    public String systemGetApplicationPath();


    /**
     * Returns path to the currently set mediacontent directory (depends on language).
     * <p/>
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None</dd>
     * <dd><b>Returns: </b>empty string</dd>
     * </dd>
     * </dl>
     *
     * @return the path, ex: /opt/moip/mas/applications/mediacontentpackages/mcp0001.1/
     */
    public String systemGetMediaContentPath();

    /**
     * Checks if append is supported
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None.</dd>
     * <dd><b>Return value:</b> Returns true or false depending on if append is supported
     * or not.</dd>
     *
     * @param resourceType the type of Media Content Resource. Valid values: "Prompt", "SWA" or "FunGreeting"
     * @return true if append is supported for two media object using the same content type as resource type.
     */
    public boolean systemIsAppendSupported(String resourceType);

    /**
     * Executes a GetUserInfo request towards the configured CRP node. 
     *
     * @param aPhoneNumber     A String containing the phone number of the potential subscriber
     *                         for which the request towards the CRP node is to be done.
     * @param applicationName  The name of the application making this call.
     * @return a String array containing two values:
     *         the first value contains the StatusCode in the GetUserInfo response or "" if the request
     *         could not be made;
     *         the second value contains the SPID in the GetUserInfo response if the request could be
     *         made and one was present, or "" if the request could not be made or if it did not contain
     *         an SPID attribute. 
     */
    public String[] crpDoGetUserInfo(String aPhoneNumber, String applicationName);
    
    /**
     * Executes a BlindAuthenticationRegistration request towards the configured CRP node. 
     *
     * @param aPhoneNumber     A String containing the phone number of the potential subscriber
     *                         for which the request towards the CRP node is to be done.
     * @param applicationName  The name of the application making this call.
     * @return a String array containing two values:
     *         the first value contains the StatusCode in the BlindAuthenticationRegistration response
     *         or "" if the request could not be made;
     *         the second value contains the SPID in the BlindAuthenticationRegistration response if the
     *         request could be made and one was present, or "" if the request could not be made or if
     *         it did not contain an SPID attribute. 
     */
    public String[] crpDoBlindAuthenticationRegistration(String aPhoneNumber, String applicationName);
/*
 *             attributes[0] = crpInterface.getBlindAuthRegResStatusCode();
 *           attributes[1] = crpInterface.getBlindAuthRegResResSpid();
 */

    /**
     * check if PIN is valid
     * @return true if the PIN is valid
     */
    public boolean checkPin(String aPhoneNumber, String aPin, String applicationName);

    /**
     * is PinSet
     */
    public abstract boolean isPinSet(String aPhoneNumber, String applicationName);

    /**
     * change the subscribers password
     */
    public abstract void changePassword(String userName, String oldPassword, String newPassword, String reason, String applicationName)    throws AuthenticationException;

    /**
     * generate a password for the subscriber and send SMS is requried
     */
    public abstract void generatePassword(String userName, boolean sendSMS, String reason, String applicationName)   throws AuthenticationException;

    /**
     * register the subscriber with the system
     */
    public abstract void registerUser(String username, String applicationName)    throws AuthenticationException;

    /**
     * Send a map-register-ss to the HLR to set unconditional divert
     */
    public void setUnconditionalDivertInHlr(String aPhoneNumber);

    /**
     * Send a map-register-ss to the HLR to set conditional divert
     */
    public void setConditionalDivertInHlr(String aPhoneNumber);

    /**
     * Send a map-erase-ss to the HLR to cancel unconditional divert
     */
    public void cancelUnconditionalDivertInHlr(String aPhoneNumber);

    /**
     * Send a map-erase-ss to the HLR to cancel conditional diverts
     */
    public void cancelConditionalDivertInHlr(String aPhoneNumber);

    /**
     * Send a map-interrogate-ss to the HLR to get the divert status
     * @return a String containing the status  (String of either "conditional" or "unconditional")
     */
    public Boolean getDivertStatusInHlr(String aPhoneNumber, String divertType);


    /**
     * @deprecated
     * Send an ATI request to get the MSC number and match with number analysis table for roaming status
     * @return true if roaming
     */
    public Boolean isSubscriberRoaming(String aPhoneNumber);
    
    /**
     * Check if subscriber with specified phone number is logged in.
     * <dl><b>Simulation details:</b>
     * <dd><b>Effect: </b>None.</dd>
     * <dd><b>Return value:</b> Returns true or false depending on whether the subscriber is logged in
     * or not.</dd>
     * @param telephoneNumber The subscriber's telephone number.
     * @param validityPeriodInMin the validity period (in minutes) of the .login file beyond which the subscriber is considered logged out.
     * @return true if the subscriber with specified phone number is logged in
     */
    public Boolean isSubscriberLoggedIn(String telephoneNumber, int validityPeriodInMin);
    
    /**
     * Send an ATI or SRI-for-SM request to get the MSC number and match with number analysis table for roaming status
     * @return 0 for not roaming, 1 for roaming, -1 for unknown status
     */
    public int getSubscriberRoamingStatus(String aPhoneNumber);
    
    /**
	 * Description Creates an active SIP subscription to "message-information" event for the given mailbox obn behalf of userAgentNumber
	 * */
    public boolean systemMWIInitialSubscribe(String maiboxId, String userAgentNumber,String expires,String dialogInfo);

    /**
	 * Description Creates an active SIP subscription to "message-information" event for the given mailbox obn behalf of userAgentNumber
	 * */
    public boolean systemMWISubsequentSubscribe(String maiboxId, String userAgentNumber,String expires,String dialogInfo);


    /**
	 * Description Creates an active SIP subscription to "message-information" event for the given mailbox obn behalf of userAgentNumber
	 * */
    public boolean systemRemoveSolicitedSubscription(String maiboxId, String userAgentNumber);



    /**
     * Return true if the specified phone number is allowed to subscribe to SIP MWI notifications for the given mailbox.
	 * @param userAgentNumber The phone number to check.
	 * @param mailboxId The mailbox id the User Agents wishes to subscribe to
	 * @return true if the user agent is allowed to subscribe to the designated mailbox     *
     */
    public boolean phoneNumberCanSubscribeToMWIForMailbox(String userAgentNumber,String mailboxId);


    /**
     * Get the broadcast announcement names to be played to the subscriber
     * @param subscriberPhoneNumber the phone number of the subscriber
     * @return an array of broadcast announcement names
     */
    public String[] getBroadcastAnnouncements(String subscriberPhoneNumber);

    /**
     * Get the messageId of the broadcast announcement to be played to the subscriber
     * @param broadcastAnnouncementName The name of the broadcast announcement
     * @param mediaType    the type of media, Valid values: "voice", "video".
     * @param brand The brand of the subscriber retrieving the broadcast announcement
     * @param language The language of the subscriber retrieving the broadcast announcement
     * @return the messageId corresponding to this broadcast announcement
     */
    public int getBroadcastMessageId(String broadcastAnnouncementName, String mediaType, String brand, String language);

    /**
     * Set the broadcast as being played by the subscriber
     */
    public void setBroadcastPlayed(String subscriberPhoneNumber, String broadcastName);


    /**
     * Get an attribute of a broadcast
     * @return string value of the attribute value corresponding to the attributeName
     */
    public String[] broadcastGetStringAttribute(String broadcastName, String attributeName);

    /**
     * Get an attribute of a broadcast
     * @return integer value of the attribute value corresponding to the attributeName
     */
    public int[] broadcastGetIntegerAttribute(String broadcastName, String attributeName);

    /**
     * Get an attribute of a broadcast
     * @return boolean value of the attribute value corresponding to the attributeName
     */
    public boolean[] broadcastGetBooleanAttribute(String broadcastName, String attributeName);

    /**
     * Normalize a phone number but remove the tel and the tel:+ part
     * @param phoneNumber the phone number
     * @return a phone number which has been normalized without the uri part and +
     */
    public String getNormalizedPhoneNumber(String phoneNumber);

    /**
     * Get event file names for a Mfs Event
     * @param telephoneNumber the phone number
     * @param eventName eventName for which files will be retuned
     * @param order order fifo or lifo
     * @return an array of file names.
     */
    public String[] getMfsEventFiles(String telephoneNumber, final String eventName, String order) throws TrafficEventSenderException;
    /**
     * Get event from event file
     * @param phoneNumber the phone number
     * @param name fileName for which events will be retuned
     * @return an array of events.
     */
    public TrafficEvent[] retrieveMfsEvents(String phoneNumber, String name) throws TrafficEventSenderException;

    /**
     * Remove event file
     * @param telephoneNumber the phone number
     * @param fileName filename to be deleted
     */
    public void removeMfsEventFile(String telephoneNumber, String fileName) throws TrafficEventSenderException;

    /**
     * Checks if MFS storage is available. The storage is unavailable on a replicated site during Geo-Redundancy failover.
     * @param originator The A number
     * @param recipient The B number
     * @return true if storage is possible in the Geo Redundant system
     */
    public boolean isStorageOperationsAvailable(String originator, String recipient);

    /**
     * Checks if there is an MCD available for update. In a Geo-Redundant system, if the master MCD fails, a manual
     * switchover to the standby MCD is done. During that stage, MCD is read-only and not available for updates.
     * @param telephoneNumber the phone number
     * @return true if profile updates are possible in MCD
     */
    public boolean isProfileUpdatePossible(String telephoneNumber);
    
    /**
     * Returns the number of payload files stored for a given subscriber.
     * @param telephoneNumber The phone number of the subscriber.
     * @param type The type of payload file to count.
     * @return the number of payload files stored for a given subscriber.
     */
    public int getPayloadFileCount(String telephoneNumber, String type);
}
