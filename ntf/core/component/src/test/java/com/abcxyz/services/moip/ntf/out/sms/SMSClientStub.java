/**
 * Copyright (c) 2009 Abcxyz
 * All Rights Reserved
 */

package com.abcxyz.services.moip.ntf.out.sms;

import java.util.ArrayList;

import com.mobeon.ntf.event.PhoneOnEvent;
import com.mobeon.common.smscom.SMSAddress;
import com.mobeon.common.sms.SMSClient;
import com.mobeon.common.sms.SMSInfoResultHandler;
import com.mobeon.common.sms.SMSResultHandler;
import com.mobeon.common.sms.request.*;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.event.EventRouter;
import com.mobeon.ntf.slamdown.SlamdownPayload;
import com.mobeon.ntf.util.time.NtfTime;

/**
 * Test purposes only.
 *
 * This SMSClient stubs simulates the SMS-C by responding to the calling party in two phases.
 *
 * First phase is to respond (with the <smsClientResponse> the calling entity
 * that the request will be processed by the SMSClient.
 * For example, it could be set to SEND_FAILED to simulate an SMS-C unable to find an SMS-Unit.
 *
 * Second phase is the response (with the <smsUnitResponse>) from the SMS-C to the calling entity
 * concerning the SMS delivery itself (sent only if the first phase was SEND_OK).
 */
public class SMSClientStub extends SMSClient {

    private static int smsClientResponse = SEND_OK;
    private static int smsUnitResponse = Constants.FEEDBACK_STATUS_OK;
    private static int numberOfRequests = 0;
    private static String[] smsClientStubCallers;
    private int sendMultiCalled;
    private static String expectedSmsMessage = null;
    private static ArrayList<Request> requestsHistory = new ArrayList<Request>();

    public SMSClientStub() {
        super(null, null);
    }

    /**
     * Set the SMSClientResponse (i.e. if SMS-C will handle the incoming SMS request)
     * @param smsCResponse
     *  SMSClient.SEND_OK
     *  SMSClient.SEND_FAILED
     *  SMSClient.SEND_FAILED_TEMPORARY
     */
    public void setSmsClientResponse(int smsCResponse) {
        smsClientResponse = smsCResponse;
        numberOfRequests = 0;
        smsClientStubCallers = null;
    }

    /**
     * Set the SMSUnitResponse (i.e. if SMS-C will handle the incoming SMS request)
     * @param smsResponse:
     *  Constants.FEEDBACK_STATUS_OK
     *  Constants.FEEDBACK_STATUS_RETRY
     *  Constants.FEEDBACK_STATUS_FAILED
     *  Constants.FEEDBACK_STATUS_EXPIRED
     */
    public void setSmsUnitResponse(int smsUResponse) {
        smsUnitResponse = smsUResponse;
        numberOfRequests = 0;
        smsClientStubCallers = null;
    }

    /**
     * Reset the counters before using the SmsClientStub
     */
    public void reset() {
        smsClientResponse = SEND_OK;
        smsUnitResponse = Constants.FEEDBACK_STATUS_OK;
        numberOfRequests = 0;
        smsClientStubCallers = null;
        expectedSmsMessage = null;
    }

    /**
     * Used to get the number of requests received by SMS-c.
     * NTF considers a subscriber successfully notified when notified on 1 interface.
     * Therefore, further notifications will be discarded by NTF, preventing test cases
     * to assert the number of requests processed/received.
     * This method fixes this NTF limitation.
     * @return
     */
    public int getNumberOfRequests() {
        return numberOfRequests;
    }

    public int getSendMultiCalled() {
    	return sendMultiCalled;
    }

    public String[] getCallers() {
        return smsClientStubCallers;
    }

    public String getExpectedSmsMessage() {
        return expectedSmsMessage;
    }

    @Override
    public int sendMulti(MultiRequest request) {
        MultiRequest copiedRequest = new MultiRequest();
        Request firstRequest = request.getNextRequest();
        while (firstRequest != null) {
            requestsHistory.add(firstRequest);
            firstRequest = request.getNextRequest();
            copiedRequest.addRequest(firstRequest);
            request.requestDone();
    	}
    	sendMultiCalled ++;
        return sendSmsResponse(copiedRequest);
    }

    @Override
    public int sendSMS(SMSAddress from, SMSAddress to, int validity, SMSResultHandler rh, int id, String message, byte[] byteContent, int replacePosition, int delay) {
        SMSRequest request = new SMSRequest(from, to, validity, rh, id, message, byteContent, replacePosition, delay);
        return sendSmsResponse(request);
    }

    @Override
    public int sendCancel(SMSAddress from, SMSAddress to, int replacePosition) {
        CancelRequest request = new CancelRequest(from, to, replacePosition);
        return sendSmsResponse(request);
    }

    public int sendMWI(SMSAddress from, SMSAddress to, int validity, SMSResultHandler rh, int id, int count, String message) {
        MWIRequest request = new MWIRequest(from, to, validity, rh, id, count, message);
        return sendSmsResponse(request);
    }

    @Override
    public int sendMWISMS(SMSAddress from, SMSAddress to, int validity, SMSResultHandler rh, int id, int count, String message, int replacePosition) {
        SMSMWIRequest request = new SMSMWIRequest(from, to, validity, rh, id, count, message, replacePosition);
        return sendSmsResponse(request);
    }

    @Override
    public int sendPhoneOn(SMSAddress from, SMSAddress to, int validity, SMSResultHandler rh, int id, String message) {
        PhoneOnRequest request = new PhoneOnRequest(from, to, validity, rh, id, message);
        return sendSmsResponse(request);
    }

    @Override
    public int sendFormattedSMS(SMSAddress from, SMSAddress to, int validity, SMSInfoResultHandler rh, int id,
            String[] lines, int maxLinesPerSms, String header, String footer, String[] callers) {
        SlamdownPayload payload = new SlamdownPayload(header, lines, footer);
        FormattedSMSRequest request = new FormattedSMSRequest(from, to, validity, rh, id, maxLinesPerSms, callers, payload);
        smsClientStubCallers = callers;
        return sendSmsResponse(request);
    }

    @Override
    public int sendFlashSMS(SMSAddress from, SMSAddress to, int validity, SMSResultHandler rh, int id, String message) {
        SMSFlashRequest request = new SMSFlashRequest(from, to, validity, rh, id, message);
        return sendSmsResponse(request);
    }

    @Override
    public int sendVvm(Request request) {
        return sendSmsResponse(request);
    }
    
    public ArrayList<Request> getRequestsHistory() {
    	return requestsHistory;
    }
    
    public void setRequestsHistory(ArrayList<Request> aSmsRequestsHistory) {
    	requestsHistory = aSmsRequestsHistory;
    }

    /**
     * Inform back the calling entity based on the set configuration
     * @param request
     * @return
     */
    private int sendSmsResponse(Request request) {
        if(smsClientResponse == SEND_OK) {
            // Start a thread to generate the SMS-C final response to the calling entity
            new SmsUnitResponseSender(request);
            expectedSmsMessage = request.getMessage();
        }
        numberOfRequests++;
        return smsClientResponse;
    }

    /**
     * Thread to send an SmsUnitResonse
     */
    private class SmsUnitResponseSender extends Thread {

        private Request request;

        public SmsUnitResponseSender(Request request) {
            super("SmsUnitResponseSender");
            this.request = request;
            start();
        }

        public void run() {
            try {
                NtfTime.sleepUntil(1);
                this.sendSmsUnitResponse(request);
                return;
            } catch (Exception e) {
                ;
            }
        }

        private void sendSmsUnitResponse(Request request) {
            if (request instanceof MultiRequest) {
                // Process MultiRequest individually
                MultiRequest multiRequest = (MultiRequest)request;
                while( multiRequest.getCount() > 0 ) {
                    Request sendRequest = multiRequest.getNextRequest();
                    this.sendResponse(sendRequest);
                    multiRequest.requestDone();
                }
            } if (request instanceof PhoneOnRequest) {
                this.sendPhoneOnResponse(request);
            } else {
                this.sendResponse(request);
            }
        }

        private void sendResponse(Request sendRequest) {
            if (smsUnitResponse == Constants.FEEDBACK_STATUS_OK) {
                if (sendRequest.getResultHandler() instanceof SMSInfoResultHandler) {
                    ((SMSInfoResultHandler)sendRequest.getResultHandler()).allOk(sendRequest.getId(), 1);
                } else {
                    sendRequest.getResultHandler().ok(sendRequest.getId());
                }
            } else if (smsUnitResponse == Constants.FEEDBACK_STATUS_RETRY) {
                sendRequest.getResultHandler().retry(sendRequest.getId(), "retry");
            } else {
                sendRequest.getResultHandler().failed(sendRequest.getId(), "failed");
            }
        }

        private void sendPhoneOnResponse(Request sendRequest) {
            try {
                /**
                 * First, the SMSPhoneOnListener sends back a PhoneOnEvent.PHONEON_SENT_SUCCESSFULLY event
                 * simulating an OK response from the NTF SMSc client.
                 * Note that the SMSPhoneOnListener will invoke the EventRouter.
                 */
                sendRequest.getResultHandler().ok(sendRequest.getId());

                /**
                 * Second, the EventRouter sends back a PhoneOnEvent.PHONEON_OK
                 * simulating a PhoneOn response (DeliverySM response) from the SMSc. 
                 */
                Thread.sleep(500);
                PhoneOnEvent phoneOnEvent = new PhoneOnEvent(this, request.getToAddress().getNumber(), PhoneOnEvent.PHONEON_OK, "");
                EventRouter.get().phoneOn(phoneOnEvent);
            } catch (Exception e) { ; }
        }
    }
}
