/**
 * Copyright (c) 2007 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.email;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.ContentFormatUtil;
import com.mobeon.common.cmnaccess.oam.MoipOamManager;
import com.mobeon.common.content.BinaryContent;
import com.mobeon.common.email.EmailClient;
import com.mobeon.common.email.EmailClientFactory;
import com.mobeon.common.email.EmailUnit;
import com.mobeon.common.email.request.*;
import com.mobeon.common.email.ConnectionStateListener;
import com.mobeon.ntf.text.TemplateMessageGenerationException;
import com.mobeon.ntf.text.TextCreator;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NotificationConfigConstants;
import com.mobeon.ntf.Constants.depositType;
import com.mobeon.ntf.mail.EmailListHandler;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.out.FeedbackHandler;
import com.mobeon.ntf.userinfo.EmailFilterInfo;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.management.ManagementInfo;
import com.mobeon.ntf.management.ManagementStatus;
import com.mobeon.ntf.meragent.MerAgent;
import com.abcxyz.messaging.common.message.MSA;
import com.abcxyz.messaging.common.message.Message;
import com.abcxyz.messaging.common.message.MsgBodyPart;
import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.transcoderfacade.Transcoder;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;

import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.ParseException;


/**
 * The EmailOut is the entry point for sending Email notifications.
 * Normally this class forwards requests to a handler thread from a pool,
 * holding the calling thread only for a short time. If too many handlers are
 * already running, the calling thread will hang until a handler is released.
 */
public class EmailOut implements Constants,
                                 ConnectionStateListener {

  private static LogAgent log = NtfCmnLogger.getLogAgent(EmailOut.class);
  private static EmailOut instance = null;
  private EmailClient emailClient = null;
  private EmailListener emailListener = null;
  private Transcoder transcoder = new Transcoder(new MoipOamManager(), "EmailOut");
  private boolean init = true;


  /**
   * The constructor initializes the Email MTA table. Clients do now use it,
   * the single instance is retrieved with get instead.
   */
  private EmailOut() {
    EmailConfigWrapper configWrapper = new EmailConfigWrapper();
    emailClient = EmailClientFactory.getInstance().createEmailClient(log, configWrapper);
    emailClient.setConnectionStateListener(this);
    emailListener = new EmailListener();
    updateCacheFromConfig();
  }

  /**
   * Returns the single instance of EmailOut, creating it if necessary.
   * @return the EmailOut.
   */
  public static EmailOut get() {
    if (instance == null) {
      instance = new EmailOut();
    }
    return instance;
  }



  /**
   * handleEmail acquires an EmailClient thread that handles the job of creating
   * and sending the Email Notification message(s).
   * @param user - information about the user receiving the notification.
   * @param info - Email-specific information from notification filters.
   * @param ng - the entity outside handling the response to a
   * complete Email message.
   * @param inbox - the different message counts in the users mailbox. If inbox
   * is null, no message count will be done.
   * @param validity - how long the message is valid.
   * @return the number of acknowledgements that will be sent for Email.
 * @throws TemplateMessageGenerationException 
 * @throws InterruptedException 
     */
    public int handleEmail(UserInfo user,
            EmailFilterInfo info,
            FeedbackHandler ng,
            NotificationEmail email,
            UserMailbox inbox,
            int validity) throws TemplateMessageGenerationException, InterruptedException {
      boolean sendToMer = true;

      TextCreator tc = TextCreator.get();

      depositType type = email.getDepositType();

      int id = emailClient.getNextId();
      
      String charSet=Config.getMimeTextCharSet(user.getPreferredLanguage());

      String  notifContent = info.getNotifContent();
      log.debug("EmailOut.handleEmail(): Using notification template[" + notifContent + "]");

      String telephoneNumber = CommonMessagingAccess.getInstance().denormalizeNumber(user.getTelephoneNumber());
      String textMsg = makeTextMessage(notifContent, inbox, email, user, true);
      String subject = tc.makeHeader(inbox, email, user, notifContent, TextCreator.SUBJECT);
      String cc = tc.makeHeader(inbox, email, user, notifContent, TextCreator.CC);
      String bcc = tc.makeHeader(inbox, email, user, notifContent, TextCreator.BCC);
      String replyTo = tc.makeHeader(inbox, email, user, notifContent, TextCreator.REPLYTO);
      String from = tc.makeHeader(inbox, email, user, notifContent, TextCreator.FROM);
      String fromEnvelope = tc.makeHeader(inbox, email, user, notifContent, TextCreator.FROMENVELOPE);
      if (fromEnvelope == null || fromEnvelope.equals("")) {
        fromEnvelope = " "; // This gives us an empty return-path
      }
      // Make a "MimeContainer"
      MimeContainer mc =
    	  new MimeContainer(info.getAddresses(),
                          cc,
                          bcc,
                          from,
                          replyTo,
                          (subject != null) ? subject : "Email Notification",
                          textMsg,
                          fromEnvelope,
                          charSet);
      
      if(info.isFwdMsg() && (!email.isConfidential())) {
    	  //Email Forward Scenario i.e. forward message as an attachment
    	  if (type == depositType.VOICE || type == depositType.VIDEO || type == depositType.FAX) {
    	      log.debug("EmailOut.handleEmail(): Forward email with message type[" +
    	              type + "] as an attachment");
    		  MimeMultipart m = new MimeMultipart();
    		  MimeBodyPart textPart = new MimeBodyPart();
    		  MimeBodyPart contentPart = new MimeBodyPart();

    		  try {
    			  BodyPart part = (BodyPart) email.getAttachmentPart(type);
    			  String origFileName = part.getFileName();
    			  String contentType = part.getContentType();
    			  byte[] data = BinaryContent.readBytes(part.getInputStream());
    			  BinaryContent inputContent = new BinaryContent(origFileName, contentType, data);
    			  BinaryContent transContent = transcode(telephoneNumber, type, email.getMessageLength(), inputContent);

    			  if (transContent == null) {
    			      addErrorMsg(m, inbox, email, user,charSet);
    			  } else {
    			      byte[] transData = transContent.getBytes();
    			          			    
    			      if (!isMsgTooBig(transData.length)) {
    			          
    			          textPart.setText(textMsg,charSet);
    			          textPart.setHeader("Content-Type","text/plain; charset=\"" + charSet + "\"" );
    			          textPart.setHeader("Content-Transfer-Encoding", "quoted-printable");
    			          m.addBodyPart(textPart, 0);   
    			          
    			          contentPart.setFileName(transContent.getPartName());
    			          contentPart.setContent(transData, transContent.getContentType());
    			          m.addBodyPart(contentPart, 1);
    			          //write MDR
    			          MerAgent mer = MerAgent.get();
    			          mer.msgAutoFwdByEmail(telephoneNumber);
                                  sendToMer = false;
    			      } else {
    			          //msg is too big, get error template for message too big for email forward
    			          String tooBigMsg = null;
    			          try {
    			              tooBigMsg = makeTextMessage("email-msg-too-big", inbox, email, user, false);
    			          } catch (TemplateMessageGenerationException e) { 
    			              log.error("Exception occured generating template for message too big for email forward: ", e);
    			          }
    			          if (tooBigMsg != null) {
    			              textPart.setText(tooBigMsg);
    			              textPart.setText(textMsg,charSet);
                              textPart.setHeader("Content-Type","text/plain; charset=\"" + charSet + "\"" );
                              textPart.setHeader("Content-Transfer-Encoding", "quoted-printable");
    			              m.addBodyPart(textPart);
    			          }
    			      }
    			  }
    			  mc.setMultiPart(m);
    		  } catch (Exception e) {
    			  //these exceptions should be very rare
    			  //it's probably more important that we log them correctly than try to tell the user what happened,
    			  //so use generic error template
    			  log.error("Exception occured trying to create email attachment for forwarding : ",e);
    			  addErrorMsg(m, inbox, email, user,charSet);
    			  mc.setMultiPart(m);
    		  }
    	  }
      }

      CustomHeader [] userDefinedHeaders = tc.makeCustomHeaders(info.getNotifContent(), inbox, email, user);
      if (userDefinedHeaders != null) {
        /* Add customHeaders */
        mc.setCustomHeaders(userDefinedHeaders);
      }
      if (Config.isDiscardSmsWhenCountIs0() // Use same switch discardSMS ??
          && inbox != null
          && inbox.isCountFetched()
          && inbox.getNewTotalCount() == 0 ) {

        // if the receieved mail is newer than 10 minutes we should make a retry
        // since tha mail could end up in the users inbox later due to heavy load.
        Date emailDate = email.getMessageReceivedDate();
        Date now = new Date();
        long diff = now.getTime() - emailDate.getTime();
        if( diff < (1000*60*10)) {
          log.debug("No mail in users inbox and the mail is new, retrying later. ");

          ng.retry(user,
                   NTF_EML,
                   "No mail in users inbox and the mail is new, retrying later. " );
        } else {
          log.debug("Notification expired since count was 0. ");
          ng.expired(user, NTF_EML);
        }
      } else {
        log.debug("EmailOut.handleEmail(): Built email with message type[" + type +
                "] attachment. Attempting to send to MTA");
        emailListener.add(id, user, ng, NTF_EML);
        int result = emailClient.sendEmailMessage(mc, validity, Config.getImapTimeout(), emailListener, id);
        if( result != EmailClient.SEND_OK ) {
          emailListener.retry(id, "Failed to send request");
        }else {
        	ng.ok(user, NTF_EML, sendToMer);
        }
      }
      if (ng != null) {
        return 1;
      } else {
        return 0;
      }
    }

    private BinaryContent transcode(String mailboxId, depositType type, String msgLength, BinaryContent inputContent) {
        BinaryContent transContent = inputContent;

        if (type != depositType.FAX) {
            long start = System.currentTimeMillis();
            transContent = transcode(type, inputContent);
            long end = System.currentTimeMillis();

            if (transContent != null && transContent != inputContent) {
                int mSize = 0;
                try {
                    mSize = Integer.parseInt(msgLength);
                } catch (NumberFormatException e) {}
                MerAgent mer = MerAgent.get();
                mer.trascodingCompleted(mailboxId, inputContent.getContentType(), transContent.getContentType() , mSize, (end - start));
            }
        }

        return transContent;
    }



    private boolean isMsgTooBig(long msgSize) {
    	long maxMsgSize = Config.getEmailForwardMaximumSize(); //read max size in kBytes from config
    	if (maxMsgSize <= 0) {
    		return false;
    	}

    	if (msgSize > (maxMsgSize * 1000) ) {
    		return true;
    	}
    	return false;
    }

    private BinaryContent transcode(depositType type, BinaryContent content) {
        boolean isTranscodingOn = false;
        String outputMimeType = content.getContentType();

        if ( type == depositType.VOICE ) {
            outputMimeType  = Config.getEmailForwardOutputAudioMimeType();
            isTranscodingOn = Config.isEmailForwardTranscodeAudioOn();
        } else if ( type == depositType.VIDEO ) {
            outputMimeType  = Config.getEmailForwardOutputVideoMimeType();
            isTranscodingOn = Config.isEmailForwardTranscodeVideoOn();
        }

        if (!outputMimeType.equalsIgnoreCase(content.getContentType()) && isTranscodingOn) {
            byte[] transData = transcoder.convertByteArray(content.getBytes(), content.getContentType(), outputMimeType);

            if (transData != null) {
                String newFileName = BinaryContent.makeNewFileName(content.getPartName(), outputMimeType);
                return new BinaryContent(newFileName, outputMimeType, transData);
            } else {
                log.error("Could not transcode content from type " + content.getContentType()
                        + " to type " + outputMimeType + ". Transcoder returned null." );
                return null;
            }
        }
    	return content;
    }

    private void addErrorMsg(MimeMultipart multiPart, UserMailbox inbox, NotificationEmail email, UserInfo user,String charSet) {
        String errorMsg = null;
        try {
            errorMsg = makeTextMessage("email-msg-fwd-err", inbox, email, user, false);
        } catch(TemplateMessageGenerationException e) { }
        
        if (errorMsg != null) {
            //only send error message if the template exists
            try {
                MimeBodyPart textPart = new MimeBodyPart();
                textPart.setText(errorMsg,charSet);
                textPart.setHeader("Content-Type","text/plain; charset=\"" + charSet + "\"" );
                textPart.setHeader("Content-Transfer-Encoding", "quoted-printable");
                multiPart.addBodyPart(textPart);

            } catch (MessagingException me) {
                //worse case scenario
                log.error("Exception occured trying to create error message for forwarding : ",me);
            }
        }
    }




  /**
   * Looks up the content in the phrases file to get the message to send.
 * @throws TemplateMessageGenerationException 
   */
  private String makeTextMessage(String content,
                                 UserMailbox inbox,
                                 NotificationEmail email,
                                 UserInfo user,
                                 boolean generateDefault) throws TemplateMessageGenerationException {
    String emailString = null;
    emailString = TextCreator.get().generateText(inbox, email, user, content, generateDefault, null);
    return emailString;
  }


  private void setMtaValues(ManagementStatus mStatus, String name) {
    String hostColonPort [] = name.split(":");
    if (hostColonPort != null && hostColonPort.length >=2 ) {
      int port = 0;
      mStatus.setHostName(hostColonPort[0]);
      try {
        port = Integer.parseInt(hostColonPort[1]);
      } catch (java.lang.NumberFormatException e) {
        port = 25;
      }
      mStatus.setPort(port);
      mStatus.setZone("Unspecified");
    }
  }

  // Implement emailconnectionlistener
  public void connectionDown(String name) {
    ManagementStatus mStatus = ManagementInfo.get().getStatus(NotificationConfigConstants.MAIL_TRANSFER_AGENT_TABLE, name);
    if (mStatus.isUp() | init ) {
      init = false;
      setMtaValues(mStatus, name);
      mStatus.down();
      log.error("No connection to Mail Transfer Agent" + name);
    }
  }
  public void connectionUp(String name) {
    ManagementStatus mStatus = ManagementInfo.get().getStatus(NotificationConfigConstants.MAIL_TRANSFER_AGENT_TABLE, name);
    if (!mStatus.isUp() | init ) {
      init = false;
      setMtaValues(mStatus, name);
    }
      mStatus.up();
  }


  /**
   * Method for updating cache of MailTransferAgents
   * with current MCR content. Called from timer.
   */
  public synchronized void updateCacheFromConfig() {
      log.debug("Updating cache of MailTransferAgents from Config.");

      Map<String, Map<String, String>> mailTransferAgents = Config.getExternalEnablers(NotificationConfigConstants.MAIL_TRANSFER_AGENT_TABLE);
      if (mailTransferAgents == null || mailTransferAgents.isEmpty()) {
          log.debug("No MailTransferAgent found in config");
          return;
      }

      // Replace Cache
      String [] newMTAs = new String[mailTransferAgents.size()];
      int correctEntries = 0;
      String host;
      String port;

      Iterator<String> it = mailTransferAgents.keySet().iterator();
      while (it.hasNext()) {
          String mailTransferAgent = it.next();
          host = mailTransferAgents.get(mailTransferAgent).get(NotificationConfigConstants.HOST_NAME);
          port = mailTransferAgents.get(mailTransferAgent).get(NotificationConfigConstants.PORT);
          if (host != null && port != null) {
              newMTAs[correctEntries] = host + ":" + port;
              correctEntries++;
          }
      }

      if (correctEntries == 0) {
          log.info("No correct MailTransferAgents found! Cache unchanged.");
      } else {
          if (emailClient.updateMailTransferAgents(newMTAs) == -1) {
              log.debug("Unable to update MailTransferAgents from MCR!");
          }
      }
  }

  private String getTypeAsString(int type) {
      if (type == Constants.NTF_VOICE) {
          return "voice";
      } else if (type == Constants.NTF_VIDEO) {
          return "video";
      } else if (type == Constants.NTF_FAX) {
          return "fax";
      } else {
          return "unknown";
      }
  }

  
  ////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // Below code are only for testing
  //
  ///////////////////////////////////////////////////////////////////////////////////////////////

  // e.g. outputMimeType = "audio/mp3", "audio/amr"...  
  public int transcodeTest(String outputMimeType) {

	  boolean sendToMer = false;
	  TextCreator tc = TextCreator.get();
	  depositType type = depositType.VOICE;
	  int id = 1;
	  String charSet = "us-ascii";
	  String  notifContent = "test notif content";
	  log.debug("EmailOutTest.handleEmail(): Using notification template[" + notifContent + "]");

	  String telephoneNumber = "5143457900";
	  String textMsg = "test message";
	  String subject = "test subject";
	  String cc = "test_cc@abcxyz.com";
	  String bcc = "test_bcc@abcxyz.com";
	  String replyTo = "test_replyto@abcxyz.com";
	  String from = "test_from@abcxyz.com";
	  String fromEnvelope = "";
	  if (fromEnvelope == null || fromEnvelope.equals("")) {
		  fromEnvelope = " "; // This gives us an empty return-path
	  }
	  // Make a "MimeContainer"
	  MimeContainer mc =
			  new MimeContainer("test_to@abcxyz.com",
					  cc,
					  bcc,
					  from,
					  replyTo,
					  (subject != null) ? subject : "Email Notification",
							  textMsg,
							  fromEnvelope,
							  charSet);

	  if (true) {
		  if (type == depositType.VOICE || type == depositType.VIDEO || type == depositType.FAX) {
			  log.debug("EmailOutTest.handleEmail(): Forward email with message type[" + type + "] as an attachment");
			  MimeMultipart m = new MimeMultipart();
			  MimeBodyPart textPart = new MimeBodyPart();
			  MimeBodyPart contentPart = new MimeBodyPart();
			  try {
				  BodyPart part = (BodyPart) getAttachmentPartTest(type);
				  String origFileName = part.getFileName();
				  String contentType = part.getContentType();
				  log.debug("content type got from part.getContentType(): " + part.getContentType());
				  byte[] data = BinaryContent.readBytes(part.getInputStream());
				  BinaryContent inputContent = new BinaryContent(origFileName, contentType, data);
				  BinaryContent transContent = transcodeTest(type, inputContent, outputMimeType);

				  log.debug("content type got from inputContent.getContentType(): " + inputContent.getContentType());
				  String file = "./msg_boy_content_email_out_test." + System.currentTimeMillis();
				  log.debug("going to save content data read from message body (before transcoding) to file: " + file);
				  try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
					  fos.write(data);
					  //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
				  } catch (Exception e) {
					  e.printStackTrace();
				  }

				  if (transContent == null) {
					  //addErrorMsg(m, inbox, email, user,charSet);
					  log.error("EmailOutTest.handleEmail(): transContent = null");
					  return -1;
				  } else {
					  byte[] transData = transContent.getBytes();

					  if (!isMsgTooBig(transData.length)) {

						  textPart.setText(textMsg,charSet);
						  textPart.setHeader("Content-Type","text/plain; charset=\"" + charSet + "\"" );
						  textPart.setHeader("Content-Transfer-Encoding", "quoted-printable");
						  m.addBodyPart(textPart, 0);   

						  contentPart.setFileName(transContent.getPartName());
						  contentPart.setContent(transData, transContent.getContentType());
						  m.addBodyPart(contentPart, 1);
						  //write MDR
						  //MerAgent mer = MerAgent.get();
						  //mer.msgAutoFwdByEmail(telephoneNumber);
						  sendToMer = false;
					  } else {
						  //msg is too big, get error template for message too big for email forward
						  log.error("msg too big error ");
						  return -1;
						  /*******
							String tooBigMsg = null;
							try {
								tooBigMsg = makeTextMessage("email-msg-too-big", inbox, email, user, false);
							} catch (TemplateMessageGenerationException e) { 
								log.error("Exception occured generating template for message too big for email forward: ", e);
							}
							if (tooBigMsg != null) {
								textPart.setText(tooBigMsg);
								textPart.setText(textMsg,charSet);
								textPart.setHeader("Content-Type","text/plain; charset=\"" + charSet + "\"" );
								textPart.setHeader("Content-Transfer-Encoding", "quoted-printable");
								m.addBodyPart(textPart);
							}
						   **********/
					  }
				  }
				  mc.setMultiPart(m);
			  } catch (Exception e) {
				  //these exceptions should be very rare
				  //it's probably more important that we log them correctly than try to tell the user what happened,
				  //so use generic error template
				  log.error("Exception occured trying to create email attachment for forwarding : ", e);
				  e.printStackTrace();
				  //addErrorMsg(m, inbox, email, user,charSet);
				  mc.setMultiPart(m);
			  }
		  }
	  }

	  //CustomHeader [] userDefinedHeaders = tc.makeCustomHeaders(info.getNotifContent(), inbox, email, user);
	  CustomHeader [] userDefinedHeaders = null;
	  if (userDefinedHeaders != null) {
		  /* Add customHeaders */
		  mc.setCustomHeaders(userDefinedHeaders);
	  }
	  String filename = "./transcoded_voice_" +  outputMimeType.substring(outputMimeType.length() - 3) + ".eml";
	  try {
		  mc.writeTo(new FileOutputStream(filename));
		  log.info("email file with transcoded content saved to " + filename);
	  } catch (Exception e) {
		  e.printStackTrace();
		  return -1;
	  }
	  return 1;
  }

  public Part getAttachmentPartTest(depositType depositType)
		  throws MsgStoreException, MessagingException, IOException {
	  Part part = null;
	  switch (depositType) {
	  case VOICE:
		  part = getVoiceAttachmentPartTest();
		  break;
	  case VIDEO:
	  case FAX:
	  default:
		  log.error("Only voice is supported for this test scenario");
				  throw new MessagingException("Only voice is supported for this test scenario");
	  }
	  return part;
  }

  private static final CommonMessagingAccess commonMessagingAccess = CommonMessagingAccess.getInstance();

  public Part getVoiceAttachmentPartTest() throws MsgStoreException, MessagingException, IOException {
	  MimeBodyPart mimeBodyPart = null;
	  //readMessage();
	  MessageInfo msgInfo = new MessageInfo(new MSA(testOmsa), new MSA(testRmsa), testOmsgid, testRmsgid);
	  Message message = commonMessagingAccess.readMessage(msgInfo, false);
	  Vector<MsgBodyPart> parts= message.getContainer3().getContents();
	  MsgBodyPart part = getVoiceMessageBodyTest(parts);
	  //setExternalPart(part);
	  mimeBodyPart = ContentFormatUtil.buildMimeBodyPart(part);
	  return mimeBodyPart;
  }


  private BinaryContent transcodeTest(depositType type, BinaryContent content, String outputMimeType) {
	  byte[] transData = transcoder.convertByteArray(content.getBytes(), content.getContentType(), outputMimeType);
	  if (transData != null) {
		  String newFileName = BinaryContent.makeNewFileName(content.getPartName(), outputMimeType);
		  return new BinaryContent(newFileName, outputMimeType, transData);
	  } else {
		  log.error("Could not transcode content from type " + content.getContentType()
		  + " to type " + outputMimeType + ". Transcoder returned null." );
		  return null;
	  }
  }

  private final static int  BYTES_TO_SKIP = 6;
  private MsgBodyPart getVoiceMessageBodyTest(Vector<MsgBodyPart> allParts)  {
	  log.debug("getVoiceMessageBody Start");
	  MsgBodyPart myMultipart = new MsgBodyPart(true);
	  myMultipart.setContentType("multipart/mixed");
	  String outputMimeType = "audio/amr"; // maybe set to "audio/amr-wb" later if WB content detected

	  Iterator<MsgBodyPart> allPartIterator = allParts.iterator();
	  Vector<MsgBodyPart> partsVector = new Vector<MsgBodyPart>();
	  MsgBodyPart onlyPart = new MsgBodyPart();
	  ByteArrayOutputStream baos = new ByteArrayOutputStream();
	  int totalDuration = 0;
	  boolean firstOne = true;
	  while (allPartIterator.hasNext()){
		  log.debug("getVoiceMessageBody getting next MsgBodyPart");
		  MsgBodyPart eachPart = allPartIterator.next();

		  //Check if that part is in the message or if it just another part.
		  String partDescription = getPartHeaderTest(eachPart, "Content-Description");
		  if(partDescription != null){
			  partDescription = partDescription.toLowerCase();
			  if(partDescription.contains("spoken") && partDescription.contains("name")){
				  log.debug("getVoiceMessageBody skipping part partDescription="+partDescription);
				  continue;
			  }
		  }

		  ContentType contentType;
		  try {
			  contentType = new ContentType(eachPart.getContentType());
			  byte myContent[] = eachPart.getContent();
			  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
			  // AMR-WB: determine outputMimeType based on codec
			  ////////////////////////////////////////////////////////////////////////////////////////////////////////////
			  if (log.isDebugEnabled()) {
				  log.debug("@@@getVoiceMessageBody contentType = " + contentType.toString() + " | " + contentType.getParameter("codec") +
						  " | " + contentType.getPrimaryType() + " | " + contentType.getSubType() + " | " + contentType.getParameterList());
			  }
			  jakarta.mail.internet.ParameterList pl = contentType.getParameterList();
			  java.util.Enumeration<String> paramNames = pl.getNames();
			  while (paramNames.hasMoreElements()) {
				  String name = paramNames.nextElement();
				  if (name == null) continue;
				  if (name.equalsIgnoreCase("codec")) {
					  String codec = contentType.getParameter(name);
					  if (codec != null && codec.equalsIgnoreCase("sawb")) { // codec is AMR-WB
						  outputMimeType = "audio/amr-wb";
						  break;
					  }
				  } else {
					  log.debug("VoiceMessageBody(): contentType param name = " + name);
				  }
			  }
			  //byte[] resultTranscoded = transcoder.convertByteArray(myContent, contentType.getBaseType(), "audio/amr");
			  byte[] resultTranscoded = transcoder.convertByteArray(myContent, contentType.toString(), outputMimeType);


			  if (resultTranscoded != null && resultTranscoded.length != 0){
				  if(firstOne){
					  try {
						  baos.write(resultTranscoded);
					  } catch (IOException e) {
						  log.debug("IO Exception", e);
						  e.printStackTrace();
					  }
					  firstOne = false;
				  }else{
					  if (outputMimeType.equals("audio/amr-wb"))
						  // In case of AMR-WB, the header is not "#!AMR<LF>", but "#!AMR-WB<LF>"
						  baos.write(resultTranscoded, BYTES_TO_SKIP + 3, resultTranscoded.length - BYTES_TO_SKIP - 3);
					  else 
						  baos.write(resultTranscoded, BYTES_TO_SKIP, resultTranscoded.length - BYTES_TO_SKIP);
				  }
				  //Only add to the duration if we have a content
				  String partDuration = getPartHeaderTest(eachPart, "Content-Duration");
				  int thisDuration = extractDurationTest(partDuration);
				  totalDuration += thisDuration;
				  log.debug("getVoiceMessageBody totalDuration="+totalDuration);
			  }

		  }
		  catch (ParseException e1) {
			  log.debug("getVoiceMessageBody ParseException ", e1);
			  e1.printStackTrace();
		  }
		  catch (Exception e){
			  log.debug("getVoiceMessageBody Exception ", e);
			  // if any exception happened, we might have  an empty baos
		  }

	  }
	  onlyPart.addPartHeader("Content-Description", "Voice message (" + String.valueOf(totalDuration) + " second(s))");

	  onlyPart.addPartHeader("Content-Duration", String.valueOf(totalDuration));
	  onlyPart.addPartHeader("Content-Disposition", "inline; filename=message.amr; voice=Voice-Message");
	  if (outputMimeType.equals("audio/amr-wb"))
		  onlyPart.setContentType("audio/amr-wb; codec=sawb");
	  else
		  onlyPart.setContentType("audio/amr");
	  onlyPart.setContent(baos.toByteArray());
	  onlyPart.setBoundaryPart();
	  log.debug("getVoiceMessageBody returning multipart"+onlyPart.toString());
	  return onlyPart;

  }

  private static String getPartHeaderTest(MsgBodyPart part, String header){
	  String value = part.getPartHeader(header);
	  //Need to lookup case insensitive
	  if(value == null){
		  HashMap<String, String> allHeaders = part.getPartHeaders().getAll();

		  for(String key : allHeaders.keySet()){
			  if(key.equalsIgnoreCase(header)){
				  value = allHeaders.get(key);
				  break;
			  }
		  }
	  }
	  return value;
  }

  private static int extractDurationTest(String firstPartDuration) {
	  if(firstPartDuration != null){
		  int indexOfColon = firstPartDuration.indexOf(":");
		  String onlyDurationNumber = firstPartDuration.substring(indexOfColon+1, firstPartDuration.length());
		  onlyDurationNumber =  onlyDurationNumber.trim();
		  return Integer.valueOf(onlyDurationNumber).intValue();
	  }
	  return 0;
  }

  static String testOmsa = null;
  static String testRmsa = null;
  static String testOmsgid = null;
  static String testRmsgid = null;

  /**
   * Usage for test this main:
   * java EmailOut <omsa> <rmsa> <omsgid> <rmsgid>
   * e.g.
   * java EmailOut msid:8f199e92608dd181 msid:dc81c89033b6e0df e572d9 1302db
   * @param args
   */
  public static void main (String args[]) {

	  try {
		  Config.loadCfg();
	  } catch (Exception ex) {
		  ex.printStackTrace();
	  }
	  EmailOut e = new EmailOut();
	  //e.updateCacheFromConfig();

	  log = new com.abcxyz.messaging.common.oam.impl.StdoutLogger();
	  log.setLogLevel(com.abcxyz.messaging.common.oam.LogLevel.DEBUG);

	  testOmsa = args[0];
	  testRmsa = args[1];
	  testOmsgid = args[2];
	  testRmsgid = args[3];
	  System.out.println("=================================================");
	  System.out.println("Transcode to MP3 -- case for NTF email forwarding");
	  System.out.println("=================================================");
	  e.transcodeTest("audio/mp3");
	  System.out.println("====================================================");
	  System.out.println("Transcode to AMR -- case for VVM IMAP msg retrieving");
	  System.out.println("====================================================");
	  e.transcodeTest("audio/amr");
	  System.out.println("====================================================");
	  System.out.println("Transcode to AMR-WB -- case for NTF mail with AMR-WB");
	  System.out.println("====================================================");
	  e.transcodeTest("audio/amr-wb");
	  System.exit(0);

  }

}
