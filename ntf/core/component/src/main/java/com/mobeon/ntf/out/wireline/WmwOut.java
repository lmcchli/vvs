/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf.out.wireline;

import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.out.FeedbackHandler;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.userinfo.WmwFilterInfo;
import com.mobeon.ntf.util.Logger;

import java.util.*;

/**
 * MwmOut uses a instance of SS7GatewayHandler in order to get
 * and release a SS7Unit, which is used for sending the XMP request.
 */
public class WmwOut extends Thread implements SS7GatewayResultcodes, Constants, SS7ResponseHandler {

    private final static Logger log = Logger.getLogger(WmwOut.class);
    /** The object that fetches a SS7Unit object to use for communication with the SS7server*/
    private SS7GatewayHandler center;
    /** Global log handler*/

    /** A global transaction counter*/
    private int transactionId = 1;
    private Hashtable<String, SS7User> pendingRequests = null;

    private class SS7User {
        public UserInfo user = null;
        public FeedbackHandler ng = null;
        SS7User(UserInfo user, FeedbackHandler ng) {
            this.user = user;
            this.ng = ng;
        }
    }

    /**
     * Constructor
     * mwmnotification.
     */
    public WmwOut() {
        center = new SS7GatewayHandler(this);
        pendingRequests = new Hashtable<String, SS7User>();
        this.setName("SS7Manager");
    }

    /**
     * sendNotification is used to request that an wireline MWI notification is
     * created and sent to the user.
     *@param ng the FeedbackHandler that contains the notification contents
     * and collects responses for all receivers of the email.
     *@param user the information about the receiver.
     *@param info MMS-specific information derived from the users filters.
     */
    public int sendNotification(UserInfo user,
                                WmwFilterInfo info,
                                FeedbackHandler ng,
                                NotificationEmail email,
                                UserMailbox inbox) {
        int count = 0;
        try {
            String[] numbers = info.getNumbers();

            if (numbers == null || numbers.length == 0) {
                //WMW Off
                numbers = user.getFilter().getNotifNumbers("WMW",email);
            }

            for (int i = 0; i < numbers.length; i++) {
                if (forwardWMW(user, ng, email, inbox, info, numbers[i], numbers[i]))
                    count++;
            }
        } catch (Exception e) {
            log.logMessage("Notification out, could not send notification to recipient. Message: " + e, Logger.L_ERROR);
        }
        return count;
    }

    public int handleMWIOff(UserInfo user, FeedbackHandler ng, NotificationEmail email) {
        return sendNotification(user, null, ng, email, null);
    }

    private boolean forwardWMW(UserInfo user,
                               FeedbackHandler ng,
                               NotificationEmail email,
                               UserMailbox inbox,
                               WmwFilterInfo info,
                               String billingNumber,
                               String notifNumber) {

        boolean ok;
        int newVoiceCount = -1;
        int newFaxCount = -1;
        int newEmailCount = -1;
        int newTotalCount = -1;
        int type = -1;

        if (inbox != null && info != null) {
            if (info.hasCount()) {
                newVoiceCount = inbox.getNewVoiceCount();
                newFaxCount = inbox.getNewFaxCount();
                newEmailCount = inbox.getNewEmailCount();
            }
            newTotalCount = inbox.getNewTotalCount();
        }
        
        SS7Unit unit = center.getUnit();

        if (unit == null) { return false; }
        
        type = email.getEmailType();
        

        pendingRequests.put(""+transactionId, new SS7User(user, ng));

        ok = unit.wmwRequest(""+transactionId++,
                             billingNumber,
                             notifNumber,
                             type,
                             newVoiceCount,
                             newFaxCount,
                             newEmailCount,
                             newTotalCount,
                             ""+Config.getTypeOfNumber(),
                             ""+Config.getNumberingPlanIndicator(),
                             12,
                             true);

        if(!ok) log.logMessage("sendNotification: SS7request failed for " + unit.getName() , Logger.L_DEBUG);
        else log.logMessage("sendNotification: SS7request for " + unit.getName() + " was successful", Logger.L_DEBUG);

        center.releaseUnit(unit);
        return ok;
    }

    /** Reads the response from a Xmp request and
     * cancels the notification from the
     * delayline if a the request was successful or a
     * normal/serious error occured. Such as codes 401, 421,
     * 500 and 501.
     * Valid statuscodes are:
     * <DL>
     * <DT><B>200</B><DD> Completed
     * <DT><B>202</B><DD> Service inititated
     * <DT><B>401</B><DD> Number blocked
     * <DT><B>402</B><DD> Busy
     * <DT><B>403</B><DD> All lines busy
     * <DT><B>404</B><DD> No answer
     * <DT><B>421</B><DD> Requested service not available
     * <DT><B>500</B><DD> Syntax error
     * <DT><B>501</B><DD> Number does not exist
     * <DT><B>502</B><DD> Not a valid number
     * <DT><B>503</B><DD> Unknown error
     * </DL>
     * @param code Status code for the preformed Xmp request.
     * @param id the transaction id from the Xmp request.
     */
    public void handleResponse(String code, String id){
        log.logMessage("handleResponse: Recived a response " + code + " for transid " + id , Logger.L_DEBUG);
        int resultCode = 0;
        try{
            resultCode = Integer.parseInt(code);
        }catch(NumberFormatException e){
            log.logMessage("handleResponse: Could not parse responsecode " + code, Logger.L_VERBOSE);
            return;
        }
        if(pendingRequests.containsKey(id)){
            UserInfo user = pendingRequests.get(id).user;
            FeedbackHandler ng = pendingRequests.get(id).ng;
            pendingRequests.remove(id);
            switch (resultCode) {
                case syntaxError:
                case requestUnrecogniced:
                    log.logMessage("handleResponse: Syntax Error for transid: " +
                    id , Logger.L_VERBOSE);
                    ng.failed(user, NTF_MWI, null);
                    break;
                case numberBlocked:
                    log.logMessage("handleResponse: Phonenumber is blocked for transid: " +
                    id , Logger.L_VERBOSE);
                    ng.failed(user, NTF_MWI, null);
                    break;
                case serviceNotAvailable:
                    log.logMessage("handleResponse: MWIService is not avalable for transid: " +
                    id , Logger.L_VERBOSE);
                    ng.failed(user, NTF_MWI, null);
                    break;
                case requestTimedOut:
                    log.logMessage("handleResponse: Request timed out. " +
                    id , Logger.L_VERBOSE);
                    ng.failed(user, NTF_MWI, null);
                    break;
                case numberDoesNotExist:
                    log.logMessage("handleResponse: Phonenumber does not exist for transid: " +
                    id , Logger.L_VERBOSE);
                    ng.failed(user, NTF_MWI, null);
                    break;
                case nonValidNumber:
                    log.logMessage("handleResponse: Not a valid phonenumber for transid: " +
                    id , Logger.L_VERBOSE);
                    ng.failed(user, NTF_MWI, null);
                    break;
                case completed:
                case serviceInitiated:
                    log.logMessage("handleResponse: Successful MWI-notification for transid " +
                    id , Logger.L_VERBOSE);
                    ng.ok(user, NTF_MWI);
                    break;
                case busy:
                case allLinesBusy:
                    ng.failed(user, NTF_MWI, null);
                    break;
                case noAnswer:
                    log.logMessage("handleResponse: Subscriber does not answer. Keep notification in delayline" , Logger.L_VERBOSE);
                    ng.failed(user, NTF_MWI, null);
                    break;
                case unknownError:
                    log.logMessage("handleResponse: Unknown error! " + id , Logger.L_VERBOSE);
                    ng.failed(user, NTF_MWI, null);
                    break;
                case limitExceeded:
                    log.logMessage("handleResponse: Resource limit exceeded!" , Logger.L_VERBOSE);
                    ng.failed(user, NTF_MWI, null);
                    break;
                default:
                    log.logMessage("handleResponse: Unknown response: " + id , Logger.L_VERBOSE);
                    ng.failed(user, NTF_MWI, null);
                    break;
            }
        }
    }
}
