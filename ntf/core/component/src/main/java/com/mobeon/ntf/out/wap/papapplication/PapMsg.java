/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.wap.papapplication;

import com.mobeon.ntf.out.wap.papapi.*;
import com.mobeon.ntf.text.TemplateMessageGenerationException;
import com.mobeon.ntf.text.TextCreator;
import com.mobeon.ntf.util.Logger;

/**
 * The class creates and maintains PAP messages in XML fromat. These messages are
 * needed by a PI to be sent to a PAP gateway.
 */
public class PapMsg
{
   private final static Logger logger = Logger.getLogger(PapMsg.class);
   private boolean debug = false;

   // IPMS Logger used for logging

   /**
    * The current PAP message text
    */
    //Changed by ermahen 6/7-2001 to make it things more easy to read.
    //private static final String MESSAGE_BOUNDARY = "asdlfkjiurwghasfwwwwxxxpuu";
    private static final String MESSAGE_BOUNDARY = "msgpart";
    private static final String MIME_CONTENT_TYPE = "multipart/related; boundary=" + MESSAGE_BOUNDARY + "; type=\"application/xml\"";
    private static final String PUSHCTRL_CONTENT_TYPE = "application/xml";
    private static final String PUSHCONT_CONTENT_TYPE = "text/vnd.wap.si";
    private String mimeBoundary;
    private String mimeContentType;
    private String pushControlType;
    private String pushControlPublicID;
    private String pushContentPublicID;
    private String pushControlSystemID;
    private String pushContentSystemID;

    private static final String PUSH_CONTROL_PUBLIC_ID = "<!DOCTYPE pap PUBLIC \"-//WAPFORUM//DTD PAP 1.0//EN\"";
    private static final String PUSH_CONTROL_SYSTEM_ID = " \"http://www.wapforum.org/DTD/pap_1.0.dtd\">";
    
    private static final String PUSH_CONTENT_PUBLIC_ID = "<!DOCTYPE si PUBLIC \"-//WAPFORUM//DTD SI 1.0//EN\"";
    private static final String PUSH_CONTENT_SYSTEM_ID = " \"http://www.wapforum.org/DTD/SI.dtd\">";

    // TO DO. This should be in locale file to provide multi-language support
    private static final String NOTIFICATION_MESSAGE = "You have new messages";

    private String pushContentType;
    private String lineSeparator;
    private static final String DEFAULT_LINE_FEED = "\r\n";
    private String msgText;


   /**
    * main is only used as a test engine for PapMsg
    * @roseuid 3A9FC47E0340
    */
   public static void main(String args[])
   {
 //    Person person = WapPersonFactory.getWapPerson("5166778010", 1);
//     PapMsg myMsg = new PapMsg();
//     myMsg.setDebug(true);
//     String msg = myMsg.createPushMsg((WapPerson)person);
//     System.out.println("PAP message = \n" + msg);
//     System.out.println(msg);
//     System.exit(0);
   }

   /**
    * @roseuid 3A9FC47E034A
    */
   public String createPushMsg(WapPerson person)
   {
    String ctrlEntity = createPushControlEntity(person);
    String contentEntity = createPushContentEntity(person);
    msgText = assemblePushMessage(ctrlEntity, contentEntity);
    return msgText;
   }

   /**
    * @roseuid 3A9FC47E0354
    */
   public PapMsg()
   {
    mimeBoundary = MESSAGE_BOUNDARY;
    mimeContentType = MIME_CONTENT_TYPE;
    pushControlType = PUSHCTRL_CONTENT_TYPE;
    pushContentType = PUSHCONT_CONTENT_TYPE;
    pushControlSystemID = PUSH_CONTROL_SYSTEM_ID;
    pushContentSystemID = PUSH_CONTENT_SYSTEM_ID;
    pushControlPublicID = PUSH_CONTROL_PUBLIC_ID;
    pushContentPublicID = PUSH_CONTENT_PUBLIC_ID;
    lineSeparator = System.getProperty("line.separator", DEFAULT_LINE_FEED);
    msgText = "";
   }


   /**
    * Create the wappush-address using device-address as client-specifier
    * For more information about the WAP Client Device Address Format
    * please see: WAP PPG Service, Version 16-August-1999, clause 7
    * @roseuid 3A9FC47E0355
    */
   private String createPushDeviceAddress(WapPerson person)
   {
    String ClientMSISDN = person.getWapDeviceID();
    String client_specifier = "+" + person.getNotifNumber() + "/TYPE=PLMN";
    String wappush_client_address = "wappush=" + client_specifier;

    String wappush_address = wappush_client_address + "@" + person.getHostName();
    logger.logMessage("wappush-address = " + wappush_address, logger.L_DEBUG);
    return wappush_address;
   }

   // package method to be used for test purposes
   void setDebug(boolean debug)
   {
    this.debug = debug;
   }

   /**
    * @roseuid 3A9FC47E035E
    */
   private String createPushControlEntity(WapPerson person)
   {
       //changed by ermahen 6/7-2001
    String controlMessage = "<?xml version=\"1.0\"?>" + pushControlPublicID + pushControlSystemID; 
    controlMessage +=  "<pap><push-message push-id=\"" + person.getPushID() +
	"\"><address address-value=\"" + createPushDeviceAddress(person)  +
	"\"></address></push-message></pap>";
    return controlMessage;
   }

   /**
    * @roseuid 3A9FC47E0368
    */
   private String createPushContentEntity(WapPerson person)
   {
     //changed by ermahen
    String contentMessage = "<?xml version=\"1.0\"?>" + pushContentPublicID + pushContentSystemID;
    //           contentMessage += "<si>" + "<indication " + "href=\""+ person.getPushData()+"\"  si-id=\""+ person.getPushID()  +"\" " + "action=\"signal-high\">"+NOTIFICATION_MESSAGE  + "</indication>" + "</si>";
           try {
            contentMessage += "<si>"
                   + "<indication "
                   + "href=\""
                   + person.getPushData()
                   +"\"  si-id=\""
                   + person.getPushID()
                   +"\" action=\"signal-high\">"
                   + TextCreator.get().generateText(person.getInbox(),
                                                    person.getEmail(),
                                                    person.getUser(),
                                                    "WapPushText",
                                                    true,
                                                    null)
                   + "</indication>"
                   + "</si>";
        } catch (TemplateMessageGenerationException e) {
            logger.logMessage("TemplateMessageGenerationException received in createPushContentEntity.", Logger.L_ERROR);
        }
    return contentMessage;
   }

   /**
    * @roseuid 3A9FC47E0372
    */
   private String assemblePushMessage(String controlEntity, String contentEntity)
   {
       String message = "Content-Type: multipart/related; boundary=" +
	   mimeBoundary  + ";\"application/xml\"\r\n\r\n";
       message += "--" + mimeBoundary;
       message += "\r\nContent-Type: application/xml\r\n\r\n";
       message += controlEntity +"\r\n\r\n";
       message += "--" + mimeBoundary +"\r\n";
       message += "Content-Type: text/vnd.wap.si\r\n\r\n";
       message += contentEntity + "\r\n\r\n";

       return message;
   }

   /**
    * Access method for the mimeBoundary property.
    *
    * @return   the current value of the mimeBoundary property
    * @roseuid 3A9FC47E0386
    */
   public String getMimeBoundary()
   {
    return mimeBoundary;
   }

   /**
    * Sets the value of the mimeBoundary property.
    *
    * @param aMimeBoundary the new value of the mimeBoundary property
    * @roseuid 3A9FC47E0387
    */
   public void setMimeBoundary(String aMimeBoundary)
   {
    mimeBoundary = aMimeBoundary;
   }

   /**
    * Access method for the mimeContentType property.
    *
    * @return   the current value of the mimeContentType property
    * @roseuid 3A9FC47E0390
    */
   public String getMimeContentType()
   {
    return mimeContentType;
   }

   /**
    * Sets the value of the mimeContentType property.
    *
    * @param aMimeContentType the new value of the mimeContentType property
    * @roseuid 3A9FC47E0391
    */
   public void setMimeContentType(String aMimeContentType)
   {
    mimeContentType = aMimeContentType;
   }

   /**
    * Access method for the pushControlType property.
    *
    * @return   the current value of the pushControlType property
    * @roseuid 3A9FC47E039A
    */
   public String getPushControlType()
   {
    return pushControlType;
   }

   /**
    * Sets the value of the pushControlType property.
    *
    * @param aPushControlType the new value of the pushControlType property
    * @roseuid 3A9FC47E039B
    */
   public void setPushControlType(String aPushControlType)
   {
    pushControlType = aPushControlType;
   }

   /**
    * Access method for the pushContenetType property.
    *
    * @return   the current value of the pushContenetType property
    * @roseuid 3A9FC47E03A5
    */
   public String getPushContentType()
   {
    return pushContentType;
   }

   /**
    * Sets the value of the pushContenetType property.
    *
    * @param aPushContenetType the new value of the pushContenetType property
    * @roseuid 3A9FC47E03AE
    */
   public void setPushContenetType(String aPushContentType)
   {
    pushContentType = aPushContentType;
   }

   /**
    * Access method for the pushControlPublicID property.
    *
    * @return   the current value of the pushControlPublicID property
    * @roseuid 3A9FC47E03B8
    */
   public String getPushControlPublicID()
   {
    return pushControlPublicID;
   }

   /**
    * Sets the value of the pushControlPublicID property.
    *
    * @param aPushControlPublicID the new value of the pushControlPublicID property
    * @roseuid 3A9FC47E03B9
    */
   public void setPushControlPublicID(String aPushControlPublicID)
   {
    pushControlPublicID = aPushControlPublicID;
   }

   /**
    * Access method for the pushContentPublicID property.
    *
    * @return   the current value of the pushContentPublicID property
    * @roseuid 3A9FC47E03C2
    */
   public String getPushContentPublicID()
   {
    return pushContentPublicID;
   }

   /**
    * Sets the value of the pushContentPublicID property.
    *
    * @param aPushContentPublicID the new value of the pushContentPublicID property
    * @roseuid 3A9FC47E03C3
    */
   public void setPushContentPublicID(String aPushContentPublicID)
   {
    pushContentPublicID = aPushContentPublicID;
   }

   /**
    * Access method for the pushControlSystemID property.
    *
    * @return   the current value of the pushControlSystemID property
    * @roseuid 3A9FC47E03CC
    */
   public String getPushControlSystemID()
   {
    return pushControlSystemID;
   }

   /**
    * Sets the value of the pushControlSystemID property.
    *
    * @param aPushControlSystemID the new value of the pushControlSystemID property
    * @roseuid 3A9FC47E03CD
    */
   public void setPushControlSystemID(String aPushControlSystemID)
   {
    pushControlSystemID = aPushControlSystemID;
   }

   /**
    * Access method for the pushContentSystemID property.
    *
    * @return   the current value of the pushContentSystemID property
    * @roseuid 3A9FC47E03D6
    */
   public String getPushContentSystemID()
   {
    return pushContentSystemID;
   }

   /**
    * Sets the value of the pushContentSystemID property.
    *
    * @param aPushContentSystemID the new value of the pushContentSystemID property
    * @roseuid 3A9FC47E03E0
    */
   public void setPushContentSystemID(String aPushContentSystemID)
   {
    pushContentSystemID = aPushContentSystemID;
   }

   /**
    * Access method for the msgText property.
    *
    * @return   the current value of the msgText property
    * @roseuid 3A9FC47F0002
    */
   public String getMsgText()
   {
    return msgText;
   }

   /**
    * Sets the value of the msgText property.
    *
    * @param aMsgText the new value of the msgText property
    * @roseuid 3A9FC47F0003
    */
   public void setMsgText(String aMsgText)
   {
    msgText = aMsgText;
   }
}
