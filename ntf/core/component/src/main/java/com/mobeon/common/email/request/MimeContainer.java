/**
 * Copyright (c) 2007 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.email.request;

import java.util.Properties;
import java.io.FileOutputStream;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.Message;
import jakarta.mail.Session;


/**
 * A mime container contains the parts currently used for
 * creating a MIME message. The reason for using this obscure
 * class is that MimeMessage is too heavyweight, especially
 * when we need to reuse session objects. 
 *
 */
public class MimeContainer {

  static final int TO = 0;
  static final int CC = 1;
  static final int BCC = 2;
  static final int FROM = 3;
  static final int REPLYTO = 4;
  static final int SUBJECT = 5;
  static final int TEXT = 6;
  static final int ENVELOPEFROM = 7;
  
  private Object content = null;
  private String textCType = "text/plain; charset=\"" + "us-ascii" + "\""; // default
  private String contentCType;
  private String charSet = "us-ascii"; //default
  private String cID = null;
  private String rcpToAddr = null;
  private String [] headers = new String [8];
  private CustomHeader [] xHeaders = null;

  private MimeMultipart multiPart = null;


  /**
   * Creates a new <code>MimeContainer</code> instance.
   *
   * @param to a <code>String</code> value valid TO-address
   * @param cc a <code>String</code> value CC address or null if unused
   * @param bcc a <code>String</code> value BCC address or null if unused
   * @param from a <code>String</code> value FROM address or null if unused
   * @param replyto a <code>String</code> value REPLYTO address or null if unused
   * @param subject a <code>String</code> value subject to be used
   * @param text a <code>String</code> value text body to be used (or null if for example multipart)
   * @param envelopeFrom a <code>String</code> value EnvelopeFrom address to be used 
   * (or null, gives an emty RCP-TO address)
   */
  public MimeContainer(String to,
                       String cc,
                       String bcc,
                       String from,
                       String replyto,
                       String subject,
                       String text,
                       String envelopeFrom) { 
    headers[TO] = to;
    headers[CC] = cc;
    headers[BCC] = bcc;
    headers[FROM] = from;
    headers[REPLYTO] = replyto;
    headers[SUBJECT] = subject;
    headers[TEXT] = text;
    headers[ENVELOPEFROM] = envelopeFrom;
  }
  
  /**
   * Creates a new <code>MimeContainer</code> instance.
   *
   * @param to a <code>String</code> value valid TO-address
   * @param cc a <code>String</code> value CC address or null if unused
   * @param bcc a <code>String</code> value BCC address or null if unused
   * @param from a <code>String</code> value FROM address or null if unused
   * @param replyto a <code>String</code> value REPLYTO address or null if unused
   * @param subject a <code>String</code> value subject to be used
   * @param text a <code>String</code> value text body to be used (or null if for example multipart)
   * @param envelopeFrom a <code>String</code> value EnvelopeFrom address to be used    
   * (or null, gives an emty RCP-TO address)
   * @param charSet <code>String</code> Character set to encode text fields in.
   */
  public MimeContainer(String to,
                       String cc,
                       String bcc,
                       String from,
                       String replyto,
                       String subject,
                       String text,
                       String envelopeFrom,
                       String charSet) { 
    headers[TO] = to;
    headers[CC] = cc;
    headers[BCC] = bcc;
    headers[FROM] = from;
    headers[REPLYTO] = replyto;
    headers[SUBJECT] = subject;
    headers[TEXT] = text;
    headers[ENVELOPEFROM] = envelopeFrom;
    setCharset(charSet);
  }

  /**
   * Get a specific header value 
   * (TO, CC, BCC, FROM, REPLYTO, TEXT, ENVELOPEFROM)
   *
   * @param index an <code>int</code> value (0-7) above
   * @return a <code>String</code> value
   */
  public String getHeader(int index) {
    if (index > 7) {
      return null;
    }
    return headers[index];
  }

  /**
   * Gets the array of Custom headers (i.e. X-An-Example, X-Another-Header)
   *
   * @return a <code>CustomHeader[]</code> array
   */
  public CustomHeader [] getCustomHeaders() {
    return xHeaders;
  }

  /**
   * Set the custom headers for this container
   *
   * @param headers a <code>CustomHeader[]</code> array
   */
  public void setCustomHeaders(CustomHeader [] headers) {
    xHeaders = headers;
  }

  /**
   * Set content (can be used instead of setText)
   *
   * @param content an <code>Object</code> value, content to include in email
   * @param contentType <code>String</code> value, the content type of the content.
   */
  public void setContent(Object content,String contentType) {
    this.content = content;
    contentCType=contentType;
  }

  /**
   * Return the content (can be null if only text part set)
   *
   * @return an <code>Object</code> value
   */
  public Object getContent() {
    return content;
  }
  
  
  /**
   * Gets the Character set to encode in.
   *
   * @return a <code>String</code> value
   */
  public String getCharset() {
      return charSet;
  }

  /**
   * Gets the content Type of any included content
   * Note MultiPart type is included in the multiPart itself
   *
   * @return a <code>String</code> value
   */
  public String getContentContentType() {
    return contentCType;
  }
  
  /**
   * Gets the content Type of Text Content.
   * Included for encoding of text included in email.
   *
   * @return a <code>String</code> value
   */
  public String getTextContentType() {
    return textCType;
  }
  
  
  /**
   * Gets the content Id
   *
   * @return a <code>String</code> value
   */
  public String getContentID() {
      return cID;
  }
  
  /**
   * Set contentId MIME header type if adding content.
   * Can be used instead of setText.
   *
   * @param  contentID an <code>Object</code> value
   */
  public void setContentID(String contentID)
  {
      cID=contentID;
  }
  
  /**
   * Address that overrides original to in transport.send
   *
   * @param to a <code>String</code> value
   */
  public void setRcpToAddr(String to) {
    rcpToAddr = to;
  }

  /**
   * Get the overriding recipient TO address
   *
   * @return a <code>String</code> value
   */
  public String getRcpToAddr() {
    return rcpToAddr;
  }

  /**
   * Set the MIME content type for text included in email i.e. text/plain
   * or possibly text/html.
   *
   * @param contentType a <code>String</code> value
   */
  public void setTextContentType(String contentType) {
    if (contentType.isEmpty()) {  
        return;
    }    
    
    contentType=contentType.toLowerCase();
    
    if (charSet == null | charSet.isEmpty() || !contentType.contains("text")) {
        textCType = contentType.trim();
    }
    else
    {
        textCType=contentType.trim()+"; charset=\""+textCType+"\"";
    }
  }
  
  /**
   * Set the MIME character set
   *
   * @param charSet a <code>String</code> value, set character set of text fields.
   */
  public void setCharset(String charSet) {      
      if (charSet != null && !charSet.isEmpty() ) {
          charSet=charSet.toLowerCase();
          if (textCType.contains("text")) {         
              if (textCType.contains("charset")) {
                  String regex="charset=\".*\"";
                  textCType = textCType.replaceFirst(regex,"charset=\""+charSet+"\"");
              } else {
                  if (!textCType.endsWith(";")) {
                      textCType= textCType + ";";    
                  }
                  textCType= textCType + " charset=\"" + charSet +"\"";
              }         
          } else
          {
              textCType.replaceFirst("charset=\".*\"","");
          }
      }
      this.charSet=charSet; 
  }

  
  /**
   * Get the multipart (or null if no multipart set)
   *
   * @return a <code>MimeMultipart</code> value
   */
  public MimeMultipart getMultiPart() {
    return multiPart;
  }

  /**
   * Sets a multipart object to this request
   *
   * @param p a <code>MimeMultipart</code> value
   */
  public void setMultiPart(MimeMultipart p) {
    multiPart = p;
  }


  // This should reflect handling in EmailRequest. 
  // Used for debug
  public void writeTo(FileOutputStream f) {
    // Construct the message in order to write it!
    Properties p = new Properties();
    p.put("smtp.mail.host", "smtp.foo.bar.com");
    
    MimeMessage msg = new MimeMessage(Session.getDefaultInstance(p));
    try {
      msg.setHeader("X-Ipms-EmailNotification", "On");
      String tmp = this.getHeader(MimeContainer.SUBJECT);
      if (tmp != null) {
        if (charSet != null) {
            msg.setSubject(tmp,charSet);            
        } else
        {
            msg.setSubject(tmp);
        }
      }
      tmp = null;
      tmp = this.getHeader(MimeContainer.TEXT);
      if (tmp != null) {
        if (charSet != null) {  
            msg.setText(tmp, charSet);
        } else {
            msg.setText(tmp);
        }
        msg.setHeader("Content-Type", textCType);
        msg.setHeader("Content-Transfer-Encoding", "quoted-printable");
        
      }
      tmp = null;
      tmp = this.getHeader(MimeContainer.CC);
      if (tmp != null) {
        msg.addRecipients(Message.RecipientType.CC, tmp);
      }
      tmp = null;
      tmp = this.getHeader(MimeContainer.BCC);
      if (tmp != null) {
        msg.addRecipients(Message.RecipientType.BCC, tmp);
      }
      tmp = null;
      tmp = this.getHeader(MimeContainer.REPLYTO);
      if (tmp != null) {
        msg.setReplyTo(InternetAddress.parse(tmp));
      }
      tmp = null;
      tmp = this.getHeader(MimeContainer.TO);
      if (tmp == null) {
        ;
      }
    
      msg.addRecipients(Message.RecipientType.TO, tmp);
      tmp = null;
      
      tmp = this.getHeader(MimeContainer.FROM);
      InternetAddress fromAddress;
      if (tmp != null) {
        fromAddress = new InternetAddress(tmp);  
      } else {
        fromAddress = new InternetAddress("ntf@moip.com");
      }
      msg.setFrom(fromAddress);
     
      // X-headers 
      CustomHeader [] customHeaders = this.getCustomHeaders();
      if (customHeaders != null) {
        for (int i = 0; i < customHeaders.length; i++) {
          String name = customHeaders[i].getHeader();
          String value = customHeaders[i].getValue();
          if (name != null && value != null) {
            msg.setHeader(name, value);
          }
        }
      }

      // Ok, set mutlipart or content/content type if not null
      if (multiPart != null) {
        msg.setContent(multiPart);
      } else if (content != null && contentCType != null) {
        msg.setContent(content, contentCType);
      }

    } catch (jakarta.mail.MessagingException e) {
      // Do nothing if creation falis
      ;
    }
    try {
      msg.writeTo(f);
    } catch (Exception e) {
      // IO, could not log.
      ;
    }
  }



}
