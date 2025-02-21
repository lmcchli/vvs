/**
 * 
 */
package com.mobeon.masp.mailbox.mfs;


import static com.mobeon.masp.mediaobject.MediaLength.LengthUnit.MILLISECONDS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Vector;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.ContentDisposition;

import com.abcxyz.messaging.common.message.CodingFailureException;
import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.Container2;
import com.abcxyz.messaging.common.message.MessageStreamingResult;
import com.abcxyz.messaging.common.message.MsgBodyPart;
import com.abcxyz.messaging.common.message.NameValuePairs;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.ICommonMessagingAccess;
import com.mobeon.common.cmnaccess.MsgAccessingException;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mailbox.BaseStorableMessage;
import com.mobeon.masp.mailbox.IMessageContent;
import com.mobeon.masp.mailbox.MailboxException;
import com.mobeon.masp.mailbox.MessageContentProperties;
import com.mobeon.masp.mailbox.util.ContentDispositionHeaderUtil;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaProperties;

public class MfsStorableMessage extends BaseStorableMessage<MfsContext> {

    private static final String ADDITIONAL_PROPERTY = "addlproperty_";
    private static ILogger log = ILoggerFactory.getILogger(MfsStorableMessage.class);

	// Interface to access MFS.
	private ICommonMessagingAccess mfs;
	
	/**
	 * @param mfs Not used
	 */
	MfsStorableMessage(MfsContext context, ICommonMessagingAccess mfs) {
		super(context);
		//this.mfs = mfs;
		this.mfs = CommonMessagingAccess.getInstance();
		
	}
	
	/* (non-Javadoc)
	 * @see com.mobeon.masp.mailbox.BaseStorableMessage#storeWork(java.lang.String)
	 */
	protected void storeWork(String host) throws MailboxException {
		//
		// Prepare MFS Container 1
		//
		Container1 c1 = createContainer1();
		
		//
		// Prepare MFS Container 2
		//
		Container2 c2 = createContainer2();

		//
		// Prepare MFS Container 3
		//
		MsgBodyPart[] parts = createMsgBodyParts();
		
		//
		// Prepare state file. 
		// Record headers used in search filter.
		//
		StateAttributes stateAttributes = createStateAttributes();
		
		//
		// Store containers in MFS
		//
        try {
            int status = mfs.storeMessage(c1, c2, parts, stateAttributes);
            if (status >= MessageStreamingResult.contentDropped) {
                throw new MailboxException("Detected MFS error " + Integer.toString(status) + ".");
            }
        } catch (MsgAccessingException e) {
            throw new MailboxException("Cannot store message to MFS", e.getFailedRecipients(), e);
        } catch (Exception e) {
            throw new MailboxException("Cannot store message to MFS", e);
        }
	}
	
	/**
	 * Creates and builds container 1 from this storable message for use with MFS storage.
	 * 
	 * @return Container 1 for MFS
	 * @throws MailboxException Thrown on error
	 */
	private Container1 createContainer1() throws MailboxException {
		Container1 c1 = new Container1();
		try {
			c1.setMsgClass(MfsUtil.toMfsMessageType(type));
			
			if (sender != null) {
				c1.setFrom(sender);
			}
			
			for (String recipient : recipients) {
				c1.setTo(recipient);
			}
			
			for (String recipient : secondaryRecipients) {
				c1.setCc(recipient);
			}
			
			c1.setSubject(subject);
			if (deliveryDate != null) {
				c1.setTimeOfDelivery(deliveryDate.getTime());
			}
			c1.setDateTime(new Date().getTime());

			String priority = Integer.toString(MfsUtil.toMfsPriority(urgent));
			c1.setPriority(priority);
			
			// Move confidential header to C2 based on MFS SEC
			//c1.setPrivacy(MfsUtil.toMfsConfidentialState(confidential));
			
			c1.setSenderVisibility(senderVisibility);
			
		} catch (CodingFailureException e) {
			throw new MailboxException("Cannot create MFS Container 1", e);
		}
		
		return c1;
	}
	
	/**
	 * Create and builds container 2 from this storable message for use with MFS storage.
	 * 
	 * @return Container for MFS
	 */
	private Container2 createContainer2() {
		Container2 c2 = new Container2();
		NameValuePairs headers = new NameValuePairs();
	
		if (replyToAddress != null) {
			headers.setValue(MoipMessageEntities.REPLY_TO_HEADER, replyToAddress);
		}
		headers.setValue(MoipMessageEntities.LANGUAGE_HEADER, language);
		// Add confidential header in C2 based on MFS SEC
		headers.setValue(MoipMessageEntities.CONFIDENTIALITY_HEADER, MfsUtil.toMfsConfidentialState(confidential));
		c2.setMsgHeaders(headers);
		
		return c2;
	}
	
	/**
	 * Creates and builds an array of message body parts for use with MFS.
	 * 
	 * @return Array of body parts.
	 * @throws MailboxException Thrown on error
	 */
	private MsgBodyPart[] createMsgBodyParts() throws MailboxException {
		Vector<MsgBodyPart> parts = new Vector<MsgBodyPart>();
		try {
			if (content != null) {
		        for (IMessageContent messageContent : content) {
		        	MsgBodyPart messageBodyPart = createBodyPart(messageContent, false);
		            parts.add(messageBodyPart);
		        }
			}
			
			if (spokenNameOfSender != null) {
	        	MsgBodyPart messageBodyPart = createBodyPart(spokenNameOfSender, true);
	            parts.add(messageBodyPart);				
			}
		} catch (MessagingException e) {
			throw new MailboxException("Cannot create MFS message body part", e); 
		}
		
		return parts.toArray(new MsgBodyPart[parts.size()]);
	}
	
	/**
	 * Creates and build a message body part for use with MFS.
	 * @param messageContent Message part content.
	 * @param spokenNameOfSender If true the spoken name of sender media file is available.
	 * @return Message part.
	 * @throws MessagingException Thrown if content disposition cannot be set for this part.
	 * @throws MailboxException Thrown on error.
	 */
	private MsgBodyPart createBodyPart(IMessageContent messageContent, boolean spokenNameOfSender) 
	throws MessagingException, MailboxException {

    	MsgBodyPart messageBodyPart = new MsgBodyPart();
        IMediaObject mediaObject = messageContent.getMediaObject();
        MediaProperties mediaProperties = mediaObject.getMediaProperties();

        MessageContentProperties contentProperties = messageContent.getContentProperties();

        // Content-Duration
        if (contentProperties.getDuration() != null) {
            log.debug("Content-Duration (from contentProperties): " + contentProperties.getDuration());
            messageBodyPart.addPartHeader(Constants.CONTENT_DURATION, contentProperties.getDuration());
        } else if (mediaProperties.hasLengthInUnit(MILLISECONDS)) {
            long milliseconds = mediaProperties.getLengthInUnit(MILLISECONDS);
            long seconds = Math.round(((double) milliseconds) / 1000); //Convert milliseconds to seconds.
            log.debug("Content-Duration (from mediaProperties): " + String.valueOf(seconds));
            messageBodyPart.addPartHeader(Constants.CONTENT_DURATION, String.valueOf(seconds));
        }

        // Content-Description
        messageBodyPart.addPartHeader(Constants.CONTENT_DESCRIPTION, contentProperties.getDescription());

        // Content-Language
        messageBodyPart.addPartHeader(Constants.CONTENT_LANGUAGE, contentProperties.getLanguage());

        // Content-Disposition
        ContentDisposition contentDisposition =
                ContentDispositionHeaderUtil.createContentDisposition(spokenNameOfSender, contentProperties, mediaProperties);
        messageBodyPart.addPartHeader(Constants.CONTENT_DISPOSITION, contentDisposition.toString());

        // Add content
        try {
	        InputStream is = mediaObject.getInputStream();
	        ByteArrayOutputStream data = new ByteArrayOutputStream();
	        int count = 0;
	        byte[] buffer = new byte[1024];
	        while ((count = is.read(buffer)) != -1) {
	        	data.write(buffer, 0, count);
	        }
	        messageBodyPart.setContent(data.toByteArray());
	        String contentType = mediaProperties.getContentType().toString();
	        messageBodyPart.setContentType(contentType);
        } catch (IOException e) {
        	throw new MailboxException("Cannot create content for message part.", e);
        }

        return messageBodyPart;
    }
	
	/**
	 * Creates a list of attributes that will be set in the MFS state file. 
	 * 
	 * These attributes
	 * will be used as criteria search to filter messages on retrieval operations.
	 * 
	 * @return State attributes.
	 */
	private StateAttributes createStateAttributes() {
		StateAttributes stateAttributes = new StateAttributes();

		stateAttributes.setAttribute(StateAttributes.GLOBAL_MSG_STATE, Constants.NEW);
		if (replyToAddress != null) {
			stateAttributes.setAttribute(MoipMessageEntities.REPLY_TO_HEADER, replyToAddress);
		}
		if (language != null) {
			stateAttributes.setAttribute(MoipMessageEntities.LANGUAGE_HEADER, language);
		}
		//Add confidential header in state file since it's not in C1 anymore
		if (confidential != null){
		    stateAttributes.setAttribute(MoipMessageEntities.CONFIDENTIALITY_HEADER, MfsUtil.toMfsConfidentialState(confidential));
		}
		
		// Add all additional properties (custom properties that NTF will be able to access in state file)
		for (String key : additionalProperties.keySet()) {
		    stateAttributes.setAttribute(key, getAdditionalProperty(key));
		}
		
		return stateAttributes;
	}

    
	// Override get and setAdditionalProperty methods to bypass usage of getAdditionalPropertyName in order to allow
	// any property of any name (of String type) to be store in the voicemail State File
	public String getAdditionalProperty(String name) {
        if (log.isInfoEnabled()) log.info("MfsStorableMessage::getAdditionalProperty(name=" + name + ")");
        String value = additionalProperties.get(name);
        if (log.isInfoEnabled()) log.info("MfsStorableMessage::getAdditionalProperty(String) returns \"" + value + "\"");
        return value;
    }

    public void setAdditionalProperty(String name, String value) {
        if (log.isInfoEnabled()) log.info("MfsStorableMessage::setAdditionalProperty(name=" + name + ",value=" + value + ")");
        if (value == null) {
            if (log.isInfoEnabled()) log.info("MfsStorableMessage::setAdditionalProperty value is null; not storing property name=" + name);
        } else {
            additionalProperties.put(ADDITIONAL_PROPERTY + name, value);
            if (log.isInfoEnabled()) log.info("MfsStorableMessage::setAdditionalProperty(String,String) returns void");
        }
    }

    
	@Override
	public String getBroadcastLanguage() {
		return broadcastAnnouncementLanguage;
	}
}
