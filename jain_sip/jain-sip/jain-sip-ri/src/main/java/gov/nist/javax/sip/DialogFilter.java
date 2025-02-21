/*
 * Conditions Of Use 
 * 
 * This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 Untied States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 * 
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof, including but
 * not limited to the correctness, accuracy, reliability or usefulness of
 * the software.
 * 
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement
 *  
 * .
 * 
 */
/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      *
 ******************************************************************************/
package gov.nist.javax.sip;

import javax.sip.*;
import javax.sip.header.EventHeader;
import javax.sip.message.*;

import gov.nist.core.InternalErrorHandler;
import gov.nist.javax.sip.stack.*;
import gov.nist.javax.sip.message.*;
import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.address.*;

import java.io.IOException;

import org.apache.log4j.Logger;

/*
 * Bug fix Contributions by Lamine Brahimi, Andreas Bystrom, Bill Roome, John
 * Martin, Daniel Machin Vasquez-Illa, Antonis Karydas, Joe Provino, Bruce
 * Evangelder, Jeroen van Bemmel.
 */
/**
 * An adapter class from the JAIN implementation objects to the NIST-SIP stack.
 * The primary purpose of this class is to do early rejection of bad messages
 * and deliver meaningful messages to the application. This class is essentially
 * a Dialog filter. It checks for and rejects requests and responses which may
 * be filtered out because of sequence number, Dialog not found, etc. Note that
 * this is not part of the JAIN-SIP spec (it does not implement a JAIN-SIP
 * interface). This is part of the glue that ties together the NIST-SIP stack
 * and event model with the JAIN-SIP stack. This is strictly an implementation
 * class.
 *
 * @version 1.2 $Revision: 1.5 $ $Date: 2006/09/26 22:22:59 $
 *
 * @author M. Ranganathan
 */
public class DialogFilter {

    private static final Logger log = Logger.getLogger(DialogFilter.class);

    /**
     * Process a request. Check for various conditions in the dialog that can
     * result in the message being dropped. Possibly return errors for these
     * conditions.
     */
    public static void processRequest(SIPRequest sipRequest,
                                      SIPServerTransaction transaction) {

        ListeningPointImpl lip = transaction.getListeningPoint();

        if (log.isDebugEnabled())
            log.debug("PROCESSING INCOMING REQUEST " + sipRequest
                    + " listening point = "
                    + lip.getIPAddress() + ":"
                    + lip.getPort());

        SipStackImpl sipStack = (SipStackImpl) transaction.getSIPStack();

        SipProviderImpl sipProvider = lip.getSipProvider();
        if (sipProvider == null) {
            log.warn("Dropping message: No provider");
            return;
        }

        if (sipStack == null) {
            log.error("No sip stack!");
            return;
        }

        if (transaction != null ) {
            if (log.isDebugEnabled())
                log.debug("transaction state = " + transaction.getState());
        }

        String dialogId = sipRequest.getDialogId(true);
        SIPDialog dialog = sipStack.getDialog(dialogId);

        // Check if we got this request on the contact address of the dialog
        // If not the dialog does not belong to this request. We check this
        // condition if a contact address has been assigned to the dialog.
        // Forgive the sins of B2BUA's that like to record route ACK's
        if (dialog != null && sipProvider != dialog.getSipProvider()) {
            Contact contact = dialog.getMyContactHeader();
            if (contact != null) {
                SipUri contactUri = (SipUri) (contact.getAddress().getURI());
                String ipAddress = contactUri.getHost();
                int contactPort = contactUri.getPort();
                String contactTransport = contactUri.getTransportParam();
                if (contactTransport == null)
                    contactTransport = "udp";
                if (contactPort == -1) {
                    if (contactTransport.equals("udp")
                            || contactTransport.equals("tcp"))
                        contactPort = 5060;
                    else
                        contactPort = 5061;
                }
                // Check if the dialog contact is the same as the provider on
                // which we got the request. Otherwise, dont assign this
                // dialog to the request.
                if (ipAddress != null
                        && (!ipAddress.equals(lip.getIPAddress())
                        || contactPort != lip
                        .getPort())) {
                    if (log.isDebugEnabled()) {
                        log.debug("nulling dialog -- listening point mismatch!  "
                                + contactPort + "  lp port = "
                                + lip.getPort());
                    }
                    dialog = null;
                }

            }
        }

        /*
           * RFC 3261 8.2.2.2 Merged requests:
           * If the request has no tag in the To header field, the UAS core MUST
           * check the request against ongoing transactions. If the From tag,
           * Call-ID, and CSeq exactly match those associated with an ongoing
           * transaction, but the request does not match that transaction (based
           * on the matching rules in Section 17.2.3), the UAS core SHOULD
           * generate a 482 (Loop Detected) response and pass it to the server
           * transaction.
           */
        if ( sipRequest.getToTag() == null ) {
            SIPServerTransaction sipServerTransaction =
                    sipStack.findMergedTransaction(sipRequest);
            if  ( sipServerTransaction != null &&
                    ! sipServerTransaction.isMessagePartOfTransaction(sipRequest)) {
                SIPResponse response = sipRequest.createResponse(
                        Response.LOOP_DETECTED);
                response.setHeader(sipStack.createServerHeaderForStack());

                log.error("Loop detected while processing request");

                try {
                    sipProvider.sendResponse(response);
                } catch (SipException e) {
                    log.error("Error sending response");
                }
                return;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("dialogId = " + dialogId);
            log.debug("dialog = " + dialog);
        }

        /*
           * RFC 3261 Section 16.4 If the first value in the Route header field
           * indicates this proxy,the proxy MUST remove that value from the
           * request .
           */

        // If the message is being processed
        // by a Proxy, then the proxy will take care of stripping the
        // Route header. If the request is being processed by an
        // endpoint, then the stack strips off the route header.
        if (sipRequest.getHeader(Route.NAME) != null
                && transaction.getDialog() != null) {
            RouteList routes = sipRequest.getRouteHeaders();
            Route route = (Route) routes.getFirst();
            SipUri uri = (SipUri) route.getAddress().getURI();
            int port;
            if (uri.getHostPort().hasPort()) {
                port = uri.getHostPort().getPort();
            } else {
                if (lip.getTransport().equalsIgnoreCase("TLS"))
                    port = 5061;
                else
                    port = 5060;
            }
            String host = uri.getHost();
            if ((host.equals(lip.getIPAddress()) || host
                    .equalsIgnoreCase(lip.getSentBy()))
                    && port == lip.getPort()) {
                if (routes.size() == 1)
                    sipRequest.removeHeader(Route.NAME);
                else
                    routes.removeFirst();
            }
        }

        if (sipRequest.getMethod().equals(Request.UPDATE)) {
            // Got an UPDATE method and the user dialog does not exist and the
            // user
            // wants to be a User agent.
            if (sipProvider.isAutomaticDialogSupportEnabled() && dialog == null) {
                Response notExist = sipRequest.createResponse(
                        Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
                Server server = sipStack.createServerHeaderForStack();
                notExist.addHeader(server);
                try {
                    sipProvider.sendResponse(notExist);
                } catch (SipException e) {
                    log.error("Error sending response", e);
                }
                if (transaction != null) {
                    sipStack.removeTransaction(transaction);
                    transaction.releaseSem();
                }
                return;
            }
        } else if (sipRequest.getMethod().equals(Request.ACK)) {

            if (transaction != null && transaction.isInviteTransaction()) {
                // This is an ack for a 3xx-6xx response. Just let the tx laer
                // take care of it.

                if (log.isDebugEnabled()) {
                    log.debug("Processing ACK for INVITE Tx ");
                }


            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Processing ACK for dialog " + dialog);
                }

                if (dialog == null) {

                    if (log.isDebugEnabled()) {
                        log.debug("Dialog does not exist "
                                + sipRequest.getFirstLine()
                                + " isServerTransaction = " + true);
                    }
                    SIPServerTransaction st = sipStack
                            .getRetransmissionAlertTransaction(dialogId);
                    if (st != null && st.isRetransmissionAlertEnabled()) {
                        st.disableRetransmissionAlerts();

                    }
                    /*
                          * JvB: must never drop ACKs that dont match a transaction!
                          * One cannot be sure if it isn't an ACK for a 2xx response
                          *
                          */

                } else {
                    if (!dialog.handleAck(transaction)) {
                        return;
                    } else {
                        transaction.passToListener();
                        dialog.addTransaction(transaction);
                        dialog.addRoute(sipRequest);
                        transaction.setDialog(dialog, dialogId);
                        /*
                         * Note that ACK is a pseudo transaction. It is never
                         * added to the stack and you do not get transaction
                         * terminated events on ACK.
                         */

                        if (sipStack.deliverTerminatedEventForAck) {
                            sipStack.addTransaction(transaction);
                        } else {
                            transaction.setMapped(true);
                        }

                    }
                }
            }
        } else if (sipRequest.getMethod().equals(Request.PRACK)) {

            /*
                * RFC 3262: A matching PRACK is defined as one within the same
                * dialog as the response, and whose method, CSeq-num, and
                * response-num in the RAck header field match, respectively, the
                * method from the CSeq, the sequence number from the CSeq, and the
                * sequence number from the RSeq of the reliable provisional
                * response.
                */

            if (log.isDebugEnabled())
                log.debug("Processing PRACK for dialog " + dialog);

            if (dialog == null && sipProvider.isAutomaticDialogSupportEnabled()) {
                if (log.isDebugEnabled()) {
                    log.debug("Dialog does not exist "
                            + sipRequest.getFirstLine()
                            + " isServerTransaction = " + true);
                    log.debug("Sending 481 for PRACK - automatic dialog " +
                            "support is enabled -- cant find dialog!");
                }

                SIPResponse notExist = sipRequest.createResponse(
                        Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
                Server server = sipStack.createServerHeaderForStack();
                notExist.addHeader(server);
                try {
                    sipProvider.sendResponse(notExist);
                } catch (SipException e) {
                    log.error("error sending response", e);
                }
                if (transaction != null) {
                    sipStack.removeTransaction(transaction);
                    transaction.releaseSem();
                }
                return;

            } else {
                if (!dialog.handlePrack(sipRequest)) {
                    if (log.isDebugEnabled())
                        log.debug("Sending 481 for out of sequence PRACK.");

                    SIPResponse notExist = sipRequest.
                            createResponse(Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
                    Server server = sipStack.createServerHeaderForStack();
                    notExist.addHeader(server);
                    try {
                        sipProvider.sendResponse(notExist);
                    } catch (SipException e) {
                        log.error("error sending response", e);
                    }

                    if (transaction != null) {
                        sipStack.removeTransaction(transaction);
                        transaction.releaseSem();
                    }
                    return;
                } else {
                    try {
                        sipStack.addTransaction(transaction);
                        dialog.addTransaction(transaction);
                        dialog.addRoute(sipRequest);
                    } catch (Exception ex) {
                        InternalErrorHandler.handleException(ex);
                    }
                }
            }

        } else if (sipRequest.getMethod().equals(Request.BYE)) {
            // Check for correct sequence numbering of the BYE
            if (dialog != null && !dialog.isRequestConsumable(sipRequest)) {
                // If the sequence number is incorrent then send a
                // 500 response
                if (log.isDebugEnabled()) {
                    log.debug("Out of sequence SIP BYE request for " + dialogId);
                    log.debug("Sending 500 response for out of sequence message");
                }

                if (transaction != null) {
                    // If the stack knows about the tx, then remove it.
                    sipStack.removeTransaction(transaction);


                    // Send 500 response
                    SIPResponse sipResponse = sipRequest
                            .createResponse(Response.SERVER_INTERNAL_ERROR);
                    try {
                        transaction.sendResponseToProcessor(sipResponse,
                                sipRequest.getMessageChannel());
                    } catch (IOException ex) {
                        // Ignore.
                    }
                }
                return;

            } else if (dialog == null
                    && sipProvider.isAutomaticDialogSupportEnabled()) {
                // Drop bye's with 481 if dialog does not exist.
                // If dialog support is enabled then
                // there must be a dialog associated with the bye
                // No dialog could be found and requests on this
                // provider. Must act like a user agent -- so drop the request.
                // NOTE: if Automatic dialog support is not enabled,
                // then it is the application's responsibility to
                // take care of this error condition possibly.

                SIPResponse response = sipRequest.createResponse(
                        Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
                Server server = sipStack.createServerHeaderForStack();
                response.addHeader(server);
                if (log.isDebugEnabled())
                    log.debug("dropping request -- automatic dialog "
                            + "support enabled and dialog does not exist!");

                try {
                    transaction.sendResponse(response);
                } catch (SipException ex) {
                    log.error("Error in sending response", ex);
                }
                // If the stack knows about the tx, then remove it.
                if (transaction != null) {
                    sipStack.removeTransaction(transaction);
                    transaction.releaseSem();
                }
                return;

            }

            // note that the transaction may be null (which
            // happens when no dialog for the bye was found.
            // and automatic dialog support is disabled (i.e. the app wants
            // to manage its own dialog layer.
            if (transaction != null && dialog != null) {
                if (sipProvider == dialog.getSipProvider()) {
                    sipStack.addTransaction(transaction);
                    dialog.addTransaction(transaction);
                    transaction.setDialog(dialog, dialogId);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("BYE Tx = " + transaction + " isMapped ="
                        + transaction.isTransactionMapped());
            }

        } else if (sipRequest.getMethod().equals(Request.CANCEL)) {

            SIPServerTransaction st = (SIPServerTransaction) sipStack
                    .findCancelTransaction(sipRequest, true);

            if (log.isDebugEnabled()) {
                log.debug("Got a CANCEL, InviteServerTx = " + st
                        + " cancel Server Tx ID = " + transaction
                        + " isMapped = "
                        + transaction.isTransactionMapped());
            }

            // Processing incoming CANCEL.
            // Check if we can process the CANCEL request.
            if (sipRequest.getMethod().equals(Request.CANCEL)) {
                // If the CANCEL comes in too late, there's not
                // much that the Listener can do so just do the
                // default action and avoid bothering the listener.
                if (st != null
                        && st.getState() == SIPTransaction.TERMINATED_STATE) {
                    // If transaction already exists but it is
                    // too late to cancel the transaction then
                    // just respond OK to the CANCEL and bail.

                    if (log.isDebugEnabled())
                        log.debug("Too late to cancel Transaction");

                    // send OK and just ignore the CANCEL.
                    try {

                        transaction.sendResponse(sipRequest
                                .createResponse(Response.OK));
                    } catch (Exception ex) {
                        if (ex.getCause() != null
                                && ex.getCause() instanceof IOException) {
                            st.raiseIOExceptionEvent();
                        }
                    }
                    return;
                }
                if (log.isDebugEnabled())
                    log.debug("Cancel transaction = " + st);

            }
            if (transaction != null && st != null && st.getDialog() != null) {
                // Found an invite tx corresponding to the CANCEL.
                // Set up the client tx and pass up to listener.
                transaction.setDialog((SIPDialog) st.getDialog(), dialogId);
                // transaction = st;
            } else if (st == null
                    && sipProvider.isAutomaticDialogSupportEnabled()
                    && transaction != null) {
                // Could not find a invite tx corresponding to the CANCEL.
                // Automatic dialog support is enabled so I must behave like
                // an endpoint on this provider.
                // Send the error response for the cancel.
                SIPResponse response = sipRequest.createResponse(
                        Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
                Server server = sipStack.createServerHeaderForStack();
                response.addHeader(server);

                if (log.isDebugEnabled())
                    log.debug("dropping request -- automatic dialog support "
                            + "enabled and INVITE ST does not exist!");

                try {
                    sipProvider.sendResponse(response);
                } catch (SipException ex) {
                    InternalErrorHandler.handleException(ex);
                }
                if (transaction != null) {
                    sipStack.removeTransaction(transaction);
                    transaction.releaseSem();
                }
                return;

            }

            // INVITE was handled statefully so the CANCEL must also be
            // statefully handled.
            if (st != null) {
                try {
                    if (transaction != null) {
                        sipStack.addTransaction(transaction);
                        transaction.setPassToListener();
                        transaction.setInviteTransaction(st);
                        // Dont let the INVITE and CANCEL be concurrently
                        // processed.
                        st.acquireSem();

                    }

                } catch (Exception ex) {
                    InternalErrorHandler.handleException(ex);
                }
            }
        } else if (sipRequest.getMethod().equals(Request.INVITE)) {
            SIPTransaction lastTransaction = dialog == null ? null : dialog
                    .getInviteTransaction();

            /*
                * RFC 3261 Chapter 14. A UAS that receives a second INVITE before
                * it sends the final response to a first INVITE with a lower CSeq
                * sequence number on the same dialog MUST return a 500 (Server
                * Internal Error) response to the second INVITE and MUST include a
                * Retry-After header field with a randomly chosen value of between
                * 0 and 10 seconds.
                */

            if (dialog != null
                    && transaction != null
                    && lastTransaction != null
                    && sipRequest.getCSeq().getSeqNumber() > dialog
                    .getRemoteSeqNumber()
                    && lastTransaction instanceof SIPServerTransaction
                    && lastTransaction.isInviteTransaction()
                    && lastTransaction.getState() != TransactionState.COMPLETED
                    && lastTransaction.getState() != TransactionState.TERMINATED
                    && lastTransaction.getState() != TransactionState.CONFIRMED) {

                if (log.isDebugEnabled())
                    log.debug("Sending 500 response for out of sequence message");

                SIPResponse sipResponse = sipRequest
                        .createResponse(Response.SERVER_INTERNAL_ERROR);
                Server server = sipStack.createServerHeaderForStack();
                sipResponse.addHeader(server);
                RetryAfter retryAfter = new RetryAfter();
                try {
                    retryAfter.setRetryAfter((int) (10 * Math.random()));
                } catch (InvalidArgumentException ex) {
                    InternalErrorHandler.handleException(ex);
                }
                sipResponse.addHeader(retryAfter);
                try {
                    transaction.sendResponseToProcessor(sipResponse,
                            sipRequest.getMessageChannel());
                } catch (IOException ex) {
                    transaction.raiseIOExceptionEvent();
                }
                return;
            }

            /*
                * RFC 3261 Chapter 14. A UAS that receives an INVITE on a dialog
                * while an INVITE it had sent on that dialog is in progress MUST
                * return a 491 (Request Pending) response to the received INVITE.
                */

            lastTransaction = (dialog == null ? null : dialog
                    .getLastTransaction());

            if (dialog != null
                    && lastTransaction != null
                    && lastTransaction.isInviteTransaction()
                    && lastTransaction instanceof SIPClientTransaction
                    && lastTransaction.getState() != TransactionState.COMPLETED
                    && lastTransaction.getState() != TransactionState.TERMINATED) {

                if (dialog.getRemoteSeqNumber() + 1 == sipRequest.getCSeq()
                        .getSeqNumber()) {
                    dialog.setRemoteSequenceNumber(sipRequest.getCSeq()
                            .getSeqNumber());

                    if (log.isDebugEnabled())
                        log.debug("Sending 491 response for out of " +
                                "sequence message");

                    SIPResponse sipResponse = sipRequest
                            .createResponse(Response.REQUEST_PENDING);
                    Server server = sipStack.createServerHeaderForStack();
                    sipResponse.addHeader(server);
                    try {
                        transaction.sendResponseToProcessor(sipResponse,
                                sipRequest.getMessageChannel());
                    } catch (IOException ex) {
                        transaction.raiseIOExceptionEvent();
                    }
                    dialog.requestConsumed();
                } else {
                    if (log.isInfoEnabled())
                        log.info("Dropping message -- sequence number is too high!");
                }
                return;
            }
        }

        // Sequence numbers are supposed to be incremented
        // sequentially within a dialog for RFC 3261
        // Note BYE, CANCEL and ACK is handled above - so no check here.

        if (dialog != null && transaction != null
                && !sipRequest.getMethod().equals(Request.BYE)
                && !sipRequest.getMethod().equals(Request.CANCEL)
                && !sipRequest.getMethod().equals(Request.ACK)
                && !sipRequest.getMethod().equals(Request.PRACK)) {

            if (!dialog.isRequestConsumable(sipRequest)) {

                /*
                 * RFC 3261: "UAS Behavior" section (12.2.2): If the remote
                 * sequence number was not empty, but the sequence number of the
                 * request is lower than the remote sequence number, the request
                 * is out of order and MUST be rejected with a 500 (Server
                 * Internal Error) response.
                 */

                // Drop the request
                if (log.isDebugEnabled()) {
                    log.debug("Dropping out of sequence message "
                            + dialog.getRemoteSeqNumber() + " "
                            + sipRequest.getCSeq());
                }

                // send error when stricly higher, ignore when ==
                // (likely still processing, error would interrupt that)
                if (dialog.getRemoteSeqNumber() > sipRequest.getCSeq()
                        .getSeqNumber()) {

                    if (log.isDebugEnabled())
                        log.debug("Sending 500 response for out of sequence " +
                                "message");
                    SIPResponse sipResponse = sipRequest
                            .createResponse(Response.SERVER_INTERNAL_ERROR);
                    sipResponse.setReasonPhrase("Request out of order");
                    Server server = sipStack.createServerHeaderForStack();
                    sipResponse.addHeader(server);
                    try {
                        transaction.sendResponseToProcessor(sipResponse,
                                sipRequest.getMessageChannel());
                        sipStack.removeTransaction(transaction);
                        transaction.releaseSem();
                    } catch (IOException ex) {

                        transaction.raiseIOExceptionEvent();
                        sipStack.removeTransaction(transaction);

                    }
                }
                return;
            }

            if (sipProvider == dialog.getSipProvider()) {
                sipStack.addTransaction(transaction);
                // This will set the remote sequence number.
                dialog.addTransaction(transaction);
                dialog.addRoute(sipRequest);
                transaction.setDialog(dialog, dialogId);
            }
        }

        RequestEvent sipEvent;

        if (log.isDebugEnabled()) {
            log.debug(sipRequest.getMethod() + " transaction.isMapped = "
                    + transaction.isTransactionMapped());
        }

        /*
           * RFC 3265: Each event package MUST specify whether forked SUBSCRIBE
           * requests are allowed to install multiple subscriptions. If such
           * behavior is not allowed, the first potential dialog- establishing
           * message will create a dialog. All subsequent NOTIFY messages which
           * correspond to the SUBSCRIBE message (i.e., match "To", "From", "From"
           * header "tag" parameter, "Call-ID", "CSeq", "Event", and "Event"
           * header "id" parameter) but which do not match the dialog would be
           * rejected with a 481 response. Note that the 200-class response to the
           * SUBSCRIBE can arrive after a matching NOTIFY has been received; such
           * responses might not correlate to the same dialog established by the
           * NOTIFY. Except as required to complete the SUBSCRIBE transaction,
           * such non-matching 200-class responses are ignored.
           */

        if (dialog == null && sipRequest.getMethod().equals(Request.NOTIFY)) {

            SIPClientTransaction pendingSubscribeClientTx = sipStack
                    .findSubscribeTransaction(sipRequest, lip);

            if (log.isDebugEnabled()) {
                log.debug("PROCESSING NOTIFY  DIALOG == null "
                        + pendingSubscribeClientTx);
            }

            /*
             * RFC 3265: Upon receiving a NOTIFY request, the subscriber should
             * check that it matches at least one of its outstanding
             * subscriptions; if not, it MUST return a "481 Subscription does
             * not exist" response unless another 400- or 500-class response is
             * more appropriate.
             */
            if (sipProvider.isAutomaticDialogSupportEnabled()
                    && pendingSubscribeClientTx == null
                    && !sipStack.deliverUnsolicitedNotify) {
                /*
                 * This is the case of the UAC receiving a Stray NOTIFY for
                 * which it has not previously sent out a SUBSCRIBE and for
                 * which it does not have an established dialog.
                 */
                try {
                    if (log.isDebugEnabled())
                        log.debug("Could not find Subscription for Notify Tx.");

                    Response errorResponse = sipRequest.createResponse(
                            Response.CALL_OR_TRANSACTION_DOES_NOT_EXIST);
                    errorResponse
                            .setReasonPhrase("Subscription does not exist");
                    Server server = sipStack.createServerHeaderForStack();
                    errorResponse.addHeader(server);
                    sipProvider.sendResponse(errorResponse);
                    return;

                } catch (Exception ex) {
                    if (ex.getCause() != null
                            && ex.getCause() instanceof IOException) {
                        if (log.isInfoEnabled())
                            log.info("Exception while sending error " +
                                    "response statelessly", ex);
                    } else {
                        InternalErrorHandler.handleException(ex);
                    }
                }

            }

            // If the server transaction cannot be found or if it
            // aleady has a dialog attached to it then just assign the
            // notify to this dialog and pass it up.
            if (pendingSubscribeClientTx != null) {
                // The response to the pending subscribe tx can try to create
                // a dialog at the same time that the notify is trying to
                // create a dialog. Thus we cannot process both at the
                // same time.

                transaction.setPendingSubscribe(pendingSubscribeClientTx);
                // The transaction gets assigned to the dialog from the
                // outgoing subscribe. First see if anybody claimed the
                // default Dialog for the outgoing Subscribe request.
                SIPDialog subscriptionDialog =
                        pendingSubscribeClientTx.getDefaultDialog();

                // TODO -- refactor this. Can probably be written far cleaner.
                if (subscriptionDialog == null
                        || subscriptionDialog.getDialogId() == null
                        || !subscriptionDialog.getDialogId().equals(dialogId)) {
                    // Notify came in before you could assign a response to
                    // the subscribe.
                    // grab the default dialog and assign it to the tags in
                    // the notify.
                    if (subscriptionDialog != null
                            && subscriptionDialog.getDialogId() == null) {
                        subscriptionDialog.setDialogId(dialogId);

                    } else {
                        subscriptionDialog = pendingSubscribeClientTx
                                .getDialog(dialogId);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("PROCESSING NOTIFY Subscribe DIALOG "
                                + subscriptionDialog);
                    }

                    // The user could have createed a dialog before sending out
                    // the SUBSCRIBE on the subscribe tx.
                    if (subscriptionDialog == null
                            && (sipProvider.isAutomaticDialogSupportEnabled()
                            || pendingSubscribeClientTx
                            .getDefaultDialog() != null)) {
                        Event event = (Event) sipRequest
                                .getHeader(EventHeader.NAME);
                        if (sipStack.isEventForked(event.getEventType())) {

                            subscriptionDialog = SIPDialog.createFromNOTIFY(
                                    pendingSubscribeClientTx, transaction);

                        }

                    }
                    if (subscriptionDialog != null) {
                        transaction.setDialog(subscriptionDialog, dialogId);
                        subscriptionDialog.setState(DialogState.CONFIRMED
                                .getValue());
                        sipStack.putDialog(subscriptionDialog);
                        pendingSubscribeClientTx.setDialog(subscriptionDialog,
                                dialogId);
                        if (!transaction.isTransactionMapped()) {
                            transaction.getSIPStack().mapTransaction(transaction);
                            // Let the listener see it if it just got
                            // created.
                            // otherwise, we have already processed the tx
                            // so
                            // we dont want the listener to see it.
                            transaction.setPassToListener();
                            try {
                                transaction.getSIPStack().addTransaction(transaction);
                            } catch (Exception ex) {
                                log.warn("Internal error: Exception in " +
                                        "addTransaction", ex);
                            }
                        }
                    }
                } else {
                    // The subscription default dialog is our dialog.
                    // Found a subscrbe dialog for the NOTIFY
                    // So map the tx.
                    transaction.setDialog(subscriptionDialog, dialogId);
                    if (!transaction.isTransactionMapped()) {
                        transaction.getSIPStack().mapTransaction(transaction);
                        // Let the listener see it if it just got created.
                        // otherwise, we have already processed the tx so
                        // we dont want the listener to see it.
                        transaction.setPassToListener();
                        try {
                            transaction.getSIPStack().addTransaction(transaction);
                        } catch (Exception ex) {
                            log.warn("Internal error: Exception in " +
                                    "addTransaction", ex);
                        }
                    }
                    sipStack.putDialog(subscriptionDialog);
                    if (pendingSubscribeClientTx != null) {
                        subscriptionDialog
                                .addTransaction(pendingSubscribeClientTx);
                        pendingSubscribeClientTx.setDialog(subscriptionDialog,
                                dialogId);

                    }
                }
                if (transaction != null && transaction.isTransactionMapped()) {
                    // Shadow transaction has been created and the stack
                    // knows
                    // about it.
                    sipEvent = new RequestEvent(sipProvider,
                            transaction, subscriptionDialog, sipRequest);
                } else {
                    // Shadow transaction has been created but the stack
                    // does
                    // not know
                    // about it.
                    sipEvent = new RequestEvent(sipProvider,
                            null, subscriptionDialog, sipRequest);
                }

            } else {
                if (log.isDebugEnabled())
                    log.debug("could not find subscribe tx");

                // Got a notify out of the blue - just pass it up
                // for stateless handling by the application.
                sipEvent = new RequestEvent(sipProvider, transaction, null,
                        sipRequest);
            }

        } else {

            // For a dialog creating event - set the transaction to null.
            // The listener can create the dialog if needed.

            // MMANY: Adding event listener to transaction for re-INVITE
            // Otherwise, for example SIP timeout events for re-INVITE
            // transactions will not be sent to the stack listener.
            if ((transaction != null) && (transaction.isTransactionMapped()) &&
                    (sipRequest.getMethod().equals(Request.INVITE)) && (dialog != null) &&
                    (dialog.getState() == DialogState.CONFIRMED)) {
                // So we can handle timeouts.
                transaction.addEventListener(sipProvider);
                if (log.isDebugEnabled())
                    log.debug("Adding event listener to transaction. " +
                            "Transaction=" + transaction + ", listener=" + sipProvider);
            }

            if (transaction != null && (transaction.isTransactionMapped())) {
                sipEvent = new RequestEvent(sipProvider,
                        transaction, dialog, sipRequest);
            } else {
                sipEvent = new RequestEvent(sipProvider, null, dialog,
                        sipRequest);
            }
        }
        sipProvider.handleEvent(sipEvent, transaction);

    }

    /**
     * Process the response for a given transaction.
     *
     * @param response
     * @param dialog
     * @param transaction   MUST NOT be null.
     */
    public static void processResponseWhenTransactionExists(
            SIPResponse response,
            SIPDialog dialog,
            SIPClientTransaction transaction) {

        if (log.isDebugEnabled())
            log.debug("PROCESSING INCOMING RESPONSE" + response.encodeMessage());

        if (transaction == null)
            throw new NullPointerException("Transaction must not be null.");

        ListeningPointImpl lip = transaction.getListeningPoint();

        SipProviderImpl sipProvider = lip.getSipProvider();
        if (sipProvider == null) {
            log.warn("Dropping message:  no provider");
            return;
        }

        if (sipProvider.sipListener == null) {
            log.warn("No listener -- dropping response!");
            return;
        }

        if (log.isDebugEnabled())
            log.debug("Transaction = " + transaction);

        // Here if there is an assigned dialog
        ResponseEvent responseEvent = new javax.sip.ResponseEvent(
                sipProvider, transaction, dialog, response);
        // Set the Dialog for the response.
        if (response.getToTag() != null && dialog != null
                && response.getStatusCode() != 100) {
            // set the last response for the dialog.
            dialog.setLastResponse(transaction, response);
            transaction.setDialog(dialog, dialog.getDialogId());
        }

        sipProvider.handleEvent(responseEvent, transaction);

    }

    /**
     * Process the response when no transaction is available.
     *
     * @param response
     * @param sipProvider
     */
    public static void processResponse(SIPResponse response,
                                       SipProviderImpl sipProvider) {
        if (log.isDebugEnabled())
            log.debug("PROCESSING INCOMING RESPONSE: "
                    + response.encodeMessage());

        if (sipProvider == null) {
            log.warn("Dropping message:  no provider");
            return;
        }

        if (sipProvider.sipListener == null) {
            log.warn("Dropping message:  no sipListener registered!");
            return;
        }

        String dialogID = response.getDialogId(false);
        SIPDialog sipDialog =
                ((SipStackImpl)sipProvider.getSipStack()).getDialog(dialogID);

        if (log.isDebugEnabled())
            log.debug(
                    " sipDialog = " + sipDialog);

        // Have a dialog but could not find transaction.
        if (sipDialog != null /* && transaction == null*/ ) {
            if (response.getStatusCode() / 100 != 2) {
                if (log.isDebugEnabled())
                    log.debug("staus code != 200 ; statusCode = "
                            + response.getStatusCode());
                return;
            } else if (sipDialog.getState() == DialogState.TERMINATED) {
                if (log.isDebugEnabled())
                    log.debug("Dialog is terminated -- dropping response!");
                return;
            } else {
                // MMANY: Changed so that an ACK is always sent for the 200
                // OK response.

                // 200 retransmission for the final response.
                if (response.getCSeq().getMethod().equals(
                        sipDialog.getMethod())
                        && sipDialog.isAckSeen()) {
                    try {
                        // Found the dialog - resend the ACK and
                        // dont pass up the null transaction
                        if (log.isDebugEnabled())
                            log.debug("resending ACK");

                        sipDialog.resendAck(response.getCSeq().getSeqNumber());
                        return;
                    } catch (SipException ex) {
                        // What to do here ?? kill the dialog?
                    }
                }
            }
        }
        // Pass the response up to the application layer to handle
        // statelessly.

        if (log.isDebugEnabled())
            log.debug("sending response to TU for processing ");

        if (sipDialog != null && response.getStatusCode() != 100
                && response.getTo().getTag() != null) {
            sipDialog.setLastResponse(null, response);
        }

        ResponseEvent responseEvent = new javax.sip.ResponseEvent(
                sipProvider, null, sipDialog, response);
        sipProvider.handleEvent(responseEvent, null);
    }
}
