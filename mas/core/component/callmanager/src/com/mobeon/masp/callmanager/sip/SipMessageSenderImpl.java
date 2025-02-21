/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip;

import gov.nist.javax.sip.header.Via;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.sip.message.SipResponse;
import com.mobeon.masp.callmanager.sip.message.SipRequest;

import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.Dialog;
import javax.sip.ClientTransaction;
import javax.sip.InvalidArgumentException;
import javax.sip.message.Request;
import javax.sip.message.Response;

/**
 * This class handles sending of SIP requests and responses.
 *
 * @author Malin Flodin
 */
public class SipMessageSenderImpl implements SipMessageSender {

    private final ILogger log = ILoggerFactory.getILogger(getClass());

    private final SipStackWrapper sipStackWrapper;

    public SipMessageSenderImpl(
            SipStackWrapper sipStackWrapper) {
        this.sipStackWrapper = sipStackWrapper;
    }

    public ClientTransaction sendRequest(SipRequest sipRequest) throws SipException {

        // Update the via header
        updateViaHeader(sipRequest);

        // Create the client transaction.
        ClientTransaction transaction = sipStackWrapper.getNewClientTransaction(sipRequest.getRequest());

        // Send the request
        transaction.sendRequest();

        try {
            if (log.isInfoEnabled()) log.info("SIP " + sipRequest.getRequest().getMethod() + " request sent.");
            if (log.isDebugEnabled()) log.debug("SIP Call id: "+sipRequest.getCallId()+" SIP:  "+sipRequest.getRequest().getMethod() + " request sent.");
        } catch(Exception e)  {}

        return transaction;
    }

    public void sendRequestWithinDialog(Dialog dialog, SipRequest sipRequest) throws SipException {

        Request request = sipRequest.getRequest();

        // Update the via header
        updateViaHeader(sipRequest);

        if (request.getMethod().equals(Request.ACK)) {
            dialog.sendAck(request);
        } else {
            ClientTransaction transaction = sipStackWrapper.getNewClientTransaction(request);
            dialog.sendRequest(transaction);
        }

        try {
            if (log.isInfoEnabled()) log.info("SIP " + sipRequest.getRequest().getMethod() + " request sent.");
            if(log.isDebugEnabled()) log.debug("SIP Call id: "+sipRequest.getCallId()+" SIP:  "+sipRequest.getRequest().getMethod() + " request sent Within Dialog.");
        }
        catch(Exception e)  {}
    }

    public void sendRequestStatelessly(SipRequest sipRequest) throws SipException {
        if (log.isInfoEnabled())
            log.info("Sending SIP " + sipRequest.getRequest().getMethod() + " request statelessly.");

        updateViaHeader(sipRequest);

        if(log.isDebugEnabled())
        {
            try{
                log.debug("SIP Call id: "+sipRequest.getCallId()+" SIP:  "+sipRequest.getRequest().getMethod() + " request sent statelessly.");
            }
            catch(Exception e)  {}
        }        
        
        sipStackWrapper.getSipProvider().sendRequest(sipRequest.getRequest());
    }
    
    public void sendResponse(SipResponse sipResponse)
            throws SipException, InvalidArgumentException {
        Response response = sipResponse.getResponse();
        ServerTransaction transaction = sipResponse.getServerTransaction();
        if (transaction != null) {
            // Send response statefully
            transaction.sendResponse(response);
            if (log.isInfoEnabled())
                log.info("SIP " + response.getStatusCode() + " response is sent for a SIP " + sipResponse.getMethod() + " request.");
            
            if(log.isDebugEnabled() && !sipResponse.getMethod().equals(Request.OPTIONS))
            {
                try{
                    log.debug("SIP Call id: "+sipResponse.getCallId()+" SIP:  "+response.getStatusCode() + " response is sent for a SIP " + sipResponse.getMethod() + " request.");
                }
                catch(Exception e)  {}
            }        
            
        } else {
            // Send response statelessly
            sipResponse.getSipProvider().sendResponse(response);
            if (log.isInfoEnabled())
                log.info("SIP " + response.getStatusCode() + " response is sent statelessly for a SIP " + sipResponse.getMethod() + " request.");
            
            if(log.isDebugEnabled() && !sipResponse.getMethod().equals(Request.OPTIONS))
            {
                try{
                    log.debug("SIP Call id: "+sipResponse.getCallId()+" SIP:  "+response.getStatusCode() + " response is sent statelessly for a SIP " + sipResponse.getMethod() + " request.");
                }
                catch(Exception e)  {}
            }        

        }
    }

    public void sendReliableProvisionalResponse(
            Dialog dialog, SipResponse sipResponse) throws SipException {

        Response response = sipResponse.getResponse();
        dialog.sendReliableProvisionalResponse(response);

        if (log.isInfoEnabled()) log.info(
                "SIP " + response.getStatusCode() +
                        " response is sent reliably for a SIP " +
                        sipResponse.getMethod() + " request.");
        
        
        if(log.isDebugEnabled())
        {
            try{
                log.debug("SIP Call id: "+sipResponse.getCallId()+" SIP:  "+response.getStatusCode() + " response is sent reliably for a SIP " + sipResponse.getMethod() + " request.");
            }
            catch(Exception e)  {}
        }        
        
    }

    /**
     * This method updates VIA header of outgoing SipRequests when MAS is used as an end point, ie not in Proxy/B2BUA mode.  
     * @param sipRequest SipRequest to be sent out by MAS
     */
    private void updateViaHeader(SipRequest sipRequest) {

        Via viaHeader = (Via) sipRequest.getRequest().getHeader(Via.NAME);
        String viaOverride = null;
        String viaHost = null;
        String viaPort = null;

        try {
            if (viaHeader == null) {
                return;
            }

            // Update the via header only when MAS is used as an end point, not a Proxy/B2BUA.
/*            if (ConfigurationReader.getInstance().getConfig().getApplicationProxyMode()) {
                log.debug("No VIA header update to perform when in Proxy/B2BUA mode");
                return;
            }
*/
            viaOverride = ConfigurationReader.getInstance().getConfig().getViaOverride();
            if (viaOverride == null || viaOverride.isEmpty()) {
                return;
            }

            if(viaOverride.contains(":")){
                viaHost = viaOverride.split(":")[0];
                viaPort = viaOverride.split(":")[1];
            } else {
                viaHost = viaOverride;
            }

            log.debug("VIA header as been changed from : " + viaHeader);

            // Set VIA host
            log.debug("VIA header to set viaHost: " + viaHost);
            viaHeader.setHost(viaHost);

            // Set VIA port
            if (viaPort != null) {
                log.debug("VIA header to set viaPort: " + viaPort);
                viaHeader.setPort(Integer.parseInt(viaPort));
            }

            log.debug("VIA header as been changed to : " + viaHeader);

        } catch (Throwable e) {
            log.error("Error while trying to update VIA header: " + viaHeader, e);
        }
    }
}
