/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf.out.mediaconversion;

import com.mobeon.common.externalcomponentregister.IServiceName;
import com.mobeon.common.xmp.XmpAttachment;
import com.mobeon.common.xmp.client.XmpClient;
import com.mobeon.common.xmp.client.XmpProtocol;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.mail.EmailListHandler;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.util.Logger;

import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * MediaConversionOut is NTFs interface to convert mediafiles.
 */
public class MediaConversionOut implements Constants {
    /**
     * Global log handler
     */
    private final static Logger log = Logger.getLogger(MediaConversionOut.class); 
    private XmpClient client;

    /**
     * Constructor
     */
    public MediaConversionOut() {
        client = XmpClient.get();
    }

    /**
     * Converts a video attachment to a 3gp video file.
     *
     * @param part      the email to extract the videoattachment from.
     * @param maxLength max allowed length on the message
     * @return MCData containing status and converted file or null
     *         if the MediaConversion server couldn't be reached. .
     */
    public MCData convertVideo(Part part, int maxLength) {
        Properties props = new Properties();
        props.setProperty("fromformat", "video/mov");
        props.setProperty("toformat", "video/3gp");
        if (maxLength > 0) {
            maxLength *= 1000;
        }
        props.setProperty("length", "" + maxLength);

        int transId = client.nextTransId();

        String request = XmpProtocol.makeRequest(transId, IServiceName.MEDIA_CONVERSION, props);
        InputStream is = null;
        try {
            is = part.getInputStream();
        } catch (IOException e) {
            log.logMessage("Failed to get video message from email " + e, Logger.L_ERROR);
        } catch (MessagingException e) {
            log.logMessage("Failed to get video message from email " + e, Logger.L_ERROR);
        }

        String contentType;
        try {
            contentType = part.getContentType();
        } catch (MessagingException e) {
            log.logMessage("Failed to get video content type from email " + e, Logger.L_ERROR);
            return null;
        }

        XmpAttachment attachment = new XmpAttachment(is, contentType);
        if (attachment.getSize() <= 0 || attachment.getContentType() == null) {
            log.logMessage("Failed to read videostream from email ", Logger.L_ERROR);
            return null;
        }

        XmpAttachment[] attachments = new XmpAttachment[1];
        attachments[0] = attachment;
        MCResultHandler resultHandler = new MCResultHandler();
        boolean sendResult = client.sendRequest(transId, request, IServiceName.MEDIA_CONVERSION, resultHandler, attachments);
        if (!sendResult) {
            log.logMessage("Failed to send video message to MediaConversion", Logger.L_VERBOSE);
            return null;
        } else {
            resultHandler.waitForResult();
            MCData mcData = resultHandler.getMCData();
            return mcData;
        }
    }

    /**
     * Converts a voice attachment to a amr voice file.
     *
     * @param part the Part to extract the voiceattachment from.
     * @return MCData containing status and converted file or null
     *         if the MediaConversion server couldn't be reached. .
     */
/*    public MCData convertVoice(Part part) {
        Properties props = new Properties();
        props.setProperty("fromformat", "audio/wav");
        props.setProperty("toformat", "audio/amr");

        int transId = client.nextTransId();

        String request = XmpProtocol.makeRequest(transId, IServiceName.MEDIA_CONVERSION, props);
        InputStream is;
        try {
            is = part.getInputStream();
        } catch (IOException e) {
            log.logMessage("Failed to get voice message from email " + e, Logger.L_ERROR);
            return null;
        } catch (MessagingException e) {
            log.logMessage("Failed to get voice message from email " + e, Logger.L_ERROR);
            return null;
        }

        String contentType;
        try {
            contentType = part.getContentType();
        } catch (MessagingException e) {
            log.logMessage("Failed to get voice content type from email " + e, Logger.L_ERROR);
            return null;
        }

        XmpAttachment attachment = new XmpAttachment(is, contentType);
        if (attachment.getSize() <= 0 || attachment.getContentType() == null) {
            log.logMessage("Failed to read voicestream from email ", Logger.L_ERROR);
            return null;
        }

        XmpAttachment[] attachments = new XmpAttachment[1];
        attachments[0] = attachment;
        MCResultHandler resultHandler = MCResultHandlerFactory.getInstance().createMCResultHandler();
        boolean sendResult = client.sendRequest(transId, request, IServiceName.MEDIA_CONVERSION, resultHandler, attachments);
        if (!sendResult) {
            log.logMessage("Failed to send voice message to MediaConversion", Logger.L_VERBOSE);
            return null;
        } else {
            resultHandler.waitForResult();
            MCData mcData = resultHandler.getMCData();
            return mcData;
        }
    }*/

    /**
     * Converts a voice attachment to a amr voice file.
     *
     * @param part the Part to extract the voice attachment from.
     * @return ConversionResult containing status and converted file or null
     *         if the MediaConversion server failed.
     */
    public ConversionResult convertVoice(Part part) {
    	WavToAmrConverter converter = WavToAmrConverter.get();
    	try {
			ConversionResult result = converter.convert(part.getInputStream(), -1);
			if(result.getResultCode() == ConversionResult.OK){
				MerAgent.get().mediaConversionCompleted("unknown", "audio/wav", "audio/amr", result.getLength(), result.getConversionTime());
			}
			return result;
		} catch (IOException e) {
			log.logMessage("MediaConversionOut: IOException while converting voice: " + e.getMessage(), Logger.L_ERROR);
			e.printStackTrace();
		} catch (MessagingException e) {
			log.logMessage("MediaConversionOut: MessagingException while converting voice: " + e.getMessage(), Logger.L_ERROR);
			e.printStackTrace();
		}
		return null;

    }
}
