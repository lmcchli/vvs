/**
 * Copyright (c) 2007 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.email.request;

import java.util.Properties;

import com.abcxyz.messaging.common.oam.LogAgent;
import com.abcxyz.services.moip.ntf.coremgmt.oam.NtfCmnLogger;
import com.mobeon.common.email.EmailResultHandler;
import com.mobeon.ntf.out.email.EmailOut;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.NotificationConfigConstants;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;

import java.util.Date;

/**
 * Contains a request for Notification by Email / MMS sender
 */
public class EmailRequest extends Request {

  private MimeContainer container;
  private static LogAgent log = NtfCmnLogger.getLogAgent(EmailOut.class);
  private String fromDefault = Config.getDefaultFromAddress();
  /**
   * Creates a new <code>EmailRequest</code> instance.
   *
   * @param msg a <code>MimeContainer</code> (lightweight mime message)
   * @param validity an <code>int</code> value
   * @param resultHandler an <code>EmailResultHandler</code> used to report feedback
   * @param id an id for the request
   */
  public EmailRequest(MimeContainer msg, 
                      int validity, 
                      EmailResultHandler resultHandler, 
                      int id) {    
    super(msg, validity, resultHandler, id); 
    container = msg;
  }

  /**
   * Constructs the actual MIME-message used for the
   *
   * @param session a <code>Session</code> value to be used for the MimeMessage
   * @return a new <code>MimeMessage</code> 
   * @exception jakarta.mail.MessagingException if an error occurs
   */
  public synchronized MimeMessage getEmailMessage(Session session) 
  throws jakarta.mail.MessagingException {
      Properties prop = session.getProperties();
      MimeMessage msgTosend = 
          new MimeMessage(Session.getInstance(prop));

      msgTosend.setHeader("X-Ipms-EmailNotification", "On");
      
      String charSet = container.getCharset();
      if (charSet == null || charSet.isEmpty())
      {
          charSet = null;
      }

      String tmp = container.getHeader(MimeContainer.SUBJECT);
      if (tmp != null) {
          if(charSet != null) {
              msgTosend.setSubject(tmp,charSet);
          } else
          {
              msgTosend.setSubject(tmp); 
          }                  
      }
      
      if (container.getContentID() != null) {
          msgTosend.setContentID(container.getContentID());
      }

      msgTosend.setSentDate(new Date());

      tmp = null;
      tmp = container.getHeader(MimeContainer.TEXT);
      if (tmp != null) {
          if(charSet != null) {
              msgTosend.setText(tmp, charSet);
          } else
          {
              msgTosend.setText(tmp);
          }
          msgTosend.setHeader("Content-Type", container.getTextContentType());
          msgTosend.setHeader("Content-Transfer-Encoding", "quoted-printable");
      }
      tmp = null;
      tmp = container.getHeader(MimeContainer.CC);
      if (tmp != null) {
          msgTosend.addRecipients(Message.RecipientType.CC, tmp);
      }
      tmp = null;
      tmp = container.getHeader(MimeContainer.BCC);
      if (tmp != null) {
          msgTosend.addRecipients(Message.RecipientType.BCC, tmp);
      }
      tmp = null;
      tmp = container.getHeader(MimeContainer.REPLYTO);
      if (tmp != null) {
          msgTosend.setReplyTo(InternetAddress.parse(tmp));
      }
      tmp = null;
      tmp = container.getHeader(MimeContainer.TO);
      if (tmp == null) {
          return null; // No send without to!
      }
      msgTosend.addRecipients(Message.RecipientType.TO, tmp);
      tmp = null;

      // get the from.
      InternetAddress fromAddress = null;
      tmp = container.getHeader(MimeContainer.FROM);
      
      if (tmp != null) {
          try {
              // check if the from is not null.
              fromAddress = new InternetAddress(tmp);  
       
          } catch ( AddressException e) {
              log.debug ("Exeception with from email: "+e);
          }
      }
      
      if (fromAddress == null) {
          try {
              fromAddress = new InternetAddress(fromDefault);
              msgTosend.setFrom(fromAddress);
          }  catch ( AddressException e1) {
              log.error ("Exception translating default from : "+ e1 +" fix " + NotificationConfigConstants.EMAIL_FROM_DEFAULT + " in the ntf configuration file");
              return null;
          }   
      } else {
          // set the from   
          msgTosend.setFrom(fromAddress);
      }
      
      // X-headers 
      CustomHeader [] customHeaders = container.getCustomHeaders();
      if (customHeaders != null) {
          for (int i = 0; i < customHeaders.length; i++) {
              String name = customHeaders[i].getHeader();
              String value = customHeaders[i].getValue();
              if (name != null && value != null) {
                  msgTosend.setHeader(name, value);
              }
          } 
      }
      // Ok, set mutlipart or content/content type if not null
      if (container.getMultiPart() != null) {
          msgTosend.setContent(container.getMultiPart());
      } else if (container.getContent() != null & container.getContentContentType() != null) {
          msgTosend.setContent(container.getContent(), container.getContentContentType());
      }

      return msgTosend;
  }

  /**
   * Does this container have an RcpToAddress
   *
   * @return true if container uses RcpToAddress
   */
  public boolean useRcpToAddress() {
    return (container.getRcpToAddr() != null);
  }

  /**
   * Return the containers RcpToAddress
   *
   * @return a <code>String</code> representation of RcpToAddress
   */
  public String getRcpTo() {
    return container.getRcpToAddr();
  }

  /**
   * Gets the envelope from address, default is empty
   *
   * @return a <code>String</code> value representing EnvelopeFrom address
   */
  public String getEnvelopeFrom() {
      
      InternetAddress fromAddress=null;
      
     // take the envelop if from is null
      String tmp = container.getHeader(MimeContainer.ENVELOPEFROM);
      if (tmp != null) {
          try {
                  // validating the address
                  fromAddress = new InternetAddress(tmp);  
              } catch ( AddressException e1) {
                  log.error ("Exception translating email address envelopfrom "+e1);
              }
          
      }
      // if the address is null, take the default in the config file.
      if (fromAddress == null) {   
          try {
              fromAddress = new InternetAddress(fromDefault);
          }  catch ( AddressException e1) {
              log.error ("Exception translating default from : "+ e1 +" fix " + NotificationConfigConstants.EMAIL_FROM_DEFAULT + " in the ntf configuration file");
              return null;
          }
      }
      return fromAddress.toString();
  }
}
