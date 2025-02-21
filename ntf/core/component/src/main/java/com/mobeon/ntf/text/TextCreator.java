/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.text;

import com.abcxyz.messaging.common.message.Container1;
import com.abcxyz.messaging.common.message.Container2;
import com.abcxyz.messaging.common.message.MsgBodyPart;
import com.abcxyz.messaging.mfs.data.MessageInfo;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.mfs.statefile.StateAttributes;
import com.abcxyz.messaging.mrd.operation.SendMessageReq;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.ANotifierNotificationInfo;
import com.abcxyz.services.moip.ntf.event.NtfEvent;
import com.abcxyz.services.moip.ntf.event.NtfEventGenerator;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.email.request.CustomHeader;
import com.mobeon.ntf.mail.AMessageDepositInfo.ProcessContentType;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.slamdown.CallerInfo;

import java.util.*;



/**
 * TextCreator creates a text message from a set of general template
 * strings and a notification email. NTF can be configured to handle different
 * languages, each with its own set of template strings.
 * <P>A subscriber can select the language for notifications. He can also select
 * between a few notification types, each with a operator-defined main
 * notification template. The main templates are not fixed, in fact there is
 * no knowledge about the main templates coded into NTF. The existence of a
 * notification type is a matter between the user interface where notifications
 * are selected, and the phrase configuration files.</P>
 * <P>The main template string consists of plain text with embedded tags where
 * information from the email is substituted.
 * <H4>Implementation</H4>
 * <P>A template is identified by a combination of language and notification
 * content. The template string is split into a number of
 tokens, and a small
 * object that handles one token is created for each token. All such objects for
 * a template string are linked together in a chain of references, and the head
 * of the chain is cached. When the same combination of language and notification
 * type is needed at a later time, the pre-parsed template can be used
 * immediately.
 */
public class TextCreator {
    private static Logger log = Logger.getLogger(TextCreator.class);
    
    private static CommonMessagingAccess commonMessagingAccess = null;
    
    /**Initial StringBuffer size (size of largest text produced so far)*/
    private static int stringBufferSize = 160;
    private static Hashtable<String, Template> /* of Template, keyed by content+lang */ cTemplates
    /*cTemplate include templates of the new format*/;
    /** Hashtable of TemplateBytes keyed by content+lang. */
    private static Hashtable<String, TemplateBytes> bTemplates;
    private static TextCreator instance = null;

    public final static int CC = 0;
    public final static int BCC = 1;
    public final static int REPLYTO = 2;
    public final static int SUBJECT = 3;
    public final static int FROM = 4;
    public final static int FROMENVELOPE = 5;


    static { //"Constructor" for static data
        cTemplates = new Hashtable<String, Template>();
        bTemplates = new Hashtable<String, TemplateBytes>();
        Phrases.refresh();
        commonMessagingAccess = CommonMessagingAccess.getInstance();
    }

    String cphr = null;

    /**
     * Constructor
     */
    protected TextCreator() {
    }


    /**
     *clears any templates and phrases.
     *Used in basic test.
     */
    public void reset() {
        cphr = null;
        cTemplates = new Hashtable<String, Template>();
        bTemplates = new Hashtable<String, TemplateBytes>();
    }

    /**
     * Accessor for the single instance
     *@return the single text creator.
     */
    public static TextCreator get() {
        if (instance == null) {
            instance = new TextCreator();        }
        return instance;
    }

    public static String stripCosPrefix(String cos) {
        String prefix = "cos:";
        if (cos != null && !cos.isEmpty() && cos.startsWith(prefix)) {
            String[] strs = cos.split(":");
            if (strs.length == 1) { 
                return "";            
            } else {
                return strs[1];                
            }
        }        
        return cos;
    }
    
    /**
     * This is a version of {@link #generateText(UserMailbox, NotificationEmail, UserInfo, String, boolean, CallerInfo)}
     * which takes the same arguments plus a charsetname. This will generate the bytes required for the sms message
     * based on a byte template.
     * @param charsetname - the name of the char set to use.
     * @param inbox - the message counts for the current user.
     * @param email - all information about the notification.
     * @param user - information about the user that shall be notified.
     * @param content - the selected notification content.
     * @param generateDefault - true usually, but for tags like header-reply-to and other tags an TemplateMessageGenerationException 
     *                          is desired instead of the default "You have messages" text.
     *                          The calling code can then decide how to deal with the exception depending on the TemplateMessageGenerationException.TemplateExceptionCause.
     * @param caller - an alternate container for all information about the notification; used for slamdown.
     * @return a byte array with data from the email/caller and inbox information inserted.
     * @throws TemplateMessageGenerationException if text generation failed and generateDefault is false.
     */
    public byte[] generateBytes(String charsetname, UserMailbox inbox, NotificationEmail email, UserInfo user, String content,
            boolean generateDefault, CallerInfo caller) throws TemplateMessageGenerationException {
        return generateBytes(charsetname, inbox, email, user, content, generateDefault, caller, null);
    }
    /**
     * This is a version of {@link #generateText(UserMailbox, NotificationEmail, UserInfo, String, boolean, CallerInfo)}
     * which takes the same arguments plus a charsetname. This will generate the bytes required for the sms message
     * based on a byte template.
     * @param charsetname - the name of the char set to use.
     * @param inbox - the message counts for the current user.
     * @param user - information about the user that shall be notified.
     * @param content - the selected notification content.
     * @param generateDefault - true usually, but for tags like header-reply-to and other tags an TemplateMessageGenerationException 
     *                          is desired instead of the default "You have messages" text.
     *                          The calling code can then decide how to deal with the exception depending on the TemplateMessageGenerationException.TemplateExceptionCause.
     * @param notifInfo - an alternate container for all information about the notification; used for generic notification types.
     * @return a byte array with data from the notifInfo and inbox information inserted.
     * @throws TemplateMessageGenerationException if text generation failed and generateDefault is false.
     */
    public byte[] generateBytes(String charsetname, UserMailbox inbox, UserInfo user, String content,
            boolean generateDefault, ANotifierNotificationInfo notifInfo) throws TemplateMessageGenerationException {
        return generateBytes(charsetname, inbox, null, user, content, generateDefault, null, notifInfo);
    }
    
    /**
     * This is a version of {@link #generateText(UserMailbox, NotificationEmail, UserInfo, String, boolean, CallerInfo)}
     * which takes the same arguments plus a charsetname. This will generate the bytes required for the sms message
     * based on a byte template.
     * </p>
     * TODO Currently, the information about the notification can be passed to this method in 3 possible containers:
     * NotificationEmail, CallerInfo and ANotifierNotificationInfo.
     * Ideally, only an ANotifierNotificationInfo should be passed to this method.
     * 
     * @param charsetname - the name of the char set to use.
     * @param inbox - the message counts for the current user.
     * @param email - all information about the notification.
     * @param user - information about the user that shall be notified.
     * @param content - the selected notification content.
     * @param generateDefault - true usually, but for tags like header-reply-to and other tags an TemplateMessageGenerationException 
     *                          is desired instead of the default "You have messages" text.
     *                          The calling code can then decide how to deal with the exception depending on the TemplateMessageGenerationException.TemplateExceptionCause.
     * @param caller - an alternate container for all information about the notification; used for slamdown.
     * @param notifInfo - an alternate container for all information about the notification; used for generic notification types.
     * @return a byte array with data from the email and inbox information inserted.
     * @throws TemplateMessageGenerationException if text generation failed and generateDefault is false.
     */
    public byte[] generateBytes(String charsetname, UserMailbox inbox, NotificationEmail email, UserInfo user, String content,
            boolean generateDefault, CallerInfo caller, ANotifierNotificationInfo notifInfo) throws TemplateMessageGenerationException {
        if(content != null && !content.equals("")) {
            content = content.toLowerCase();
        }

        String cosName = stripCosPrefix(user.getCosName());

        String userLanguage = user.getPreferredLanguage();

        if (userLanguage == null || userLanguage.equals("") ) {
            userLanguage = Config.getDefaultLanguage();
        } 

        String brand = user.getBrand();

        if (brand == "") brand = null;

        log.logMessage("Generating byte array payload for " + content + ": charset=" + charsetname + ", ln=" + userLanguage + ", brand=" + brand + ", cos=" + cosName, Logger.L_DEBUG);

        TemplateBytes tpl = getTemplateBytes(content, brand, userLanguage, cosName, charsetname );

        return tpl.generateBytes(inbox, email, user, generateDefault, caller, notifInfo);
    }

    private TemplateBytes getTemplateBytes(String content, String brand, String language, String cosName, String charsetname) {
        TemplateBytes template = null;
        String key = Phrases.getBytePrefix((brand == null ? "" : brand + "_") + language, cosName, charsetname) + ":CONTENT=" + content;
        template = bTemplates.get(key);
        if (template != null)
        {
            log.logMessage("Found stored TemplateBytes in cache ", Logger.L_DEBUG);
            return template;
        }
        template = new TemplateBytes(content, brand, language, cosName, charsetname);
        log.logMessage("Storing new TemplateBytes in cache ", Logger.L_DEBUG);
        bTemplates.put(key, template);
        return template;
    }


    /**
     * Generates a text message using a compiled and cached version of the template string.
     *@param inbox - the message counts for the current user.
     *@param email - all information about the notification.
     *@param user - information about the user that shall be notified.
     *@param content - the selected notification content.
     * @param generateDefault - true usually, but for tags like header-reply-to and other tags an TemplateMessageGenerationException 
     *                          is desired instead of the default "You have messages" text.
     *                          The calling code can then decide how to deal with the exception depending on the TemplateMessageGenerationException.TemplateExceptionCause.
     *@param caller - an alternate container for all information about the notification; used for slamdown
     *@return a text message with data from the email/caller and inbox information inserted.
     * @throws TemplateMessageGenerationException if text generation failed and generateDefault is false.
     */
    public String generateText(UserMailbox inbox,
            NotificationEmail email,
            UserInfo user,
            String content,
            boolean generateDefault,
            CallerInfo caller) throws TemplateMessageGenerationException {
        return generateText(inbox, email, user, content, generateDefault, caller, null);
    }

    /**
     * Generates a text message using a compiled and cached version of the template string.
     * 
     * @param inbox - the message counts for the current user.
     * @param user - information about the user that shall be notified.
     * @param content - the selected notification content.
     * @param generateDefault - true usually, but for tags like header-reply-to and other tags an TemplateMessageGenerationException 
     *                          is desired instead of the default "You have messages" text.
     *                          The calling code can then decide how to deal with the exception depending on the TemplateMessageGenerationException.TemplateExceptionCause.
     * @param notifInfo - an alternate container for all information about the notification; used for generic notification types.
     * @return a text message with data from the notifInfo and inbox inserted.
     * @throws TemplateMessageGenerationException if text generation failed and generateDefault is false.
     */
    public String generateText(UserMailbox inbox,
            UserInfo user,
            String content,
            boolean generateDefault,
            ANotifierNotificationInfo notifInfo) throws TemplateMessageGenerationException {
        return generateText(inbox, null, user, content, generateDefault, null, notifInfo);
    }
    
    /**
     * Generates a text message using a compiled and cached version of the template string.
     * </p>
     * TODO Currently, the information about the notification can be passed to this method in 3 possible containers:
     * NotificationEmail, CallerInfo and ANotifierNotificationInfo.
     * Ideally, only an ANotifierNotificationInfo should be passed to this method.
     * 
     * @param inbox - the message counts for the current user.
     * @param email - all information about the notification.
     * @param user - information about the user that shall be notified.
     * @param content - the selected notification content.
     * @param generateDefault - true usually, but for tags like header-reply-to and other tags an TemplateMessageGenerationException 
     *                          is desired instead of the default "You have messages" text.
     *                          The calling code can then decide how to deal with the exception depending on the TemplateMessageGenerationException.TemplateExceptionCause.
     * @param caller - an alternate container for all information about the notification; used for slamdown.
     * @param notifInfo - an alternate container for all information about the notification; used for generic notification types.
     * @return a text message with data inserted.
     * @throws TemplateMessageGenerationException if text generation failed and generateDefault is false.
     */
    public String generateText(UserMailbox inbox,
            NotificationEmail email,
            UserInfo user,
            String content,
            boolean generateDefault,
            CallerInfo caller,
            ANotifierNotificationInfo notifInfo) throws TemplateMessageGenerationException {
        if(content != null && !content.equals("")) {
            content = content.toLowerCase();
        }

        String cosName = stripCosPrefix(user.getCosName());

        String userLanguage = user.getPreferredLanguage();

        if (userLanguage == null || userLanguage.equals("") ) {
            userLanguage = Config.getDefaultLanguage();
        } 

        String brand = user.getBrand();
        if (brand != null && !brand.equals("")) {
            //if brand is set prepend it to language
            userLanguage = brand + "_" + userLanguage;
        }       

        log.logMessage("Generating text for " + content + ":" + userLanguage + ", cos=" + cosName, Logger.L_DEBUG);

        Template tpl = getTemplate(userLanguage, content, cosName);

        if (tpl == null) {
            if (content.indexOf("emailnotification-header") == -1) {
                log.logMessage("Template \"" + content + "\" is missing in "
                        + userLanguage + ".cphr", Logger.L_VERBOSE);
            }
            if (generateDefault) {
                tpl = getTemplate(userLanguage, "general", cosName);
            } else {
                throw new TemplateMessageGenerationException("Could not find template: " + content);
            }
        }
        // Try Config.getDefaultLanguage()
        if (tpl == null) {
            tpl = getTemplate(Config.getDefaultLanguage(), content, cosName);
        }
        if (tpl == null) {
            tpl = getTemplate(Config.getDefaultLanguage(), "general", cosName);
        }
        // Try hardcoded english as fallback
        if (tpl == null) {
            tpl = getTemplate("en", content, cosName);
        }
        if (tpl == null) {
            tpl = getTemplate("en", "general", cosName);
        }
        // Generate the text
        if (tpl != null) {
            String text = tpl.generateText(inbox, email, user, generateDefault, caller, notifInfo);
            log.logMessage("Generated text is: " + text, Logger.L_DEBUG);
            return text;
        } else {
            return ("New message");
        }
    }



    private Template getTemplate(String language, String notifCont, String cosName) {

        Template template = null;

        if( cosName != null ) {
            template = cTemplates.get(notifCont + ":" +language + ":" + cosName ); //cos c_template
            if (template != null)
                log.logMessage("Found (cos specific) c_template", Logger.L_DEBUG);
            /*
  if( template == null ) {
    template = (Template) (cTemplates.get(notifCont + ":"
                                          + language)); // lang c_template
  if (template != null)
    log.logMessage("Found (fallback) language c_template", log.L_DEBUG);
  }
             */
            if( template == null ) {
                if(cphr != null){//cphr will always be null because it is never initialised
                    template = new Template(Phrases.getTemplateStrings(language, cosName ),
                            notifCont,
                            cphr,
                            language);
                    cTemplates.put(notifCont + ":" + language + ":" + cosName, template);
                    log.logMessage("Stored new (cos specific) c_template in cache ", Logger.L_DEBUG);
                }
                else if (cphr == null && Phrases.isCphrPhraseFound(language, notifCont, cosName)) {
                    template = new Template(Phrases.getTemplateStrings(language),
                            notifCont,
                            Phrases.getCphrTemplateStrings(language, cosName),
                            language);
                    cTemplates.put(notifCont + ":" + language + ":" + cosName, template);
                    log.logMessage("Stored new (cos specific) c_template in cache ", Logger.L_DEBUG);
                }
                else if (cphr == null && ( Phrases.isCphrPhraseFound(language, notifCont, null)
                        || Phrases.getTemplateStrings(language).getProperty(notifCont) != null) ) {
                    template = new Template(Phrases.getTemplateStrings(language),
                            notifCont,
                            Phrases.getCphrTemplateStrings(language, null),
                            language);
                    cTemplates.put(notifCont + ":" + language, template);
                    log.logMessage("Stored new (non-cos specific) c_template in cache ", Logger.L_DEBUG);
                }
            }
            return template; // null if lang template not found
        } else {
            template = cTemplates.get(notifCont + ":" +language);
            if( template == null ) {
                if(cphr != null){
                    template = new Template(Phrases.getTemplateStrings(language),
                            notifCont,
                            cphr,
                            language);
                    log.logMessage("Stored new template in cache " + template, Logger.L_DEBUG);
                    cTemplates.put(notifCont + ":" + language, template);
                }
                else if (cphr == null && (Phrases.isCphrPhraseFound(language, notifCont, null)
                        || Phrases.getTemplateStrings(language).getProperty(notifCont) != null)) {
                    template = new Template(Phrases.getTemplateStrings(language),
                            notifCont,
                            Phrases.getCphrTemplateStrings(language, cosName),
                            language);
                    log.logMessage("Stored new template in cache " + template, Logger.L_DEBUG);
                    cTemplates.put(notifCont + ":" + language, template);
                }
            }
            return template;
        }
    }



    /**
     * toString creates a printable representation of all templates in a
     * TextCreator.
     *@return a printable representation of all templates in a TextCreator.
     */
    public String toString() {
        String s = "{TextCreator";
        String key;

        for (Enumeration<String> e = cTemplates.keys(); e.hasMoreElements();) {
            key = e.nextElement();
            s += (cTemplates.get(key)).toString();
        }
        for (Enumeration<String> e = bTemplates.keys(); e.hasMoreElements();) {
            key = e.nextElement();
            s += (bTemplates.get(key)).toString();
        }
        return s + "}";
    }

    /**
     * A test user used by the main program
     */
    private static class UserX extends TestUser {
        String ph = "+4670000000";
        public String getUsersDate(Date d) {return "2005-01-01";}
        public String getUsersTime(Date d) {return "14:20";}
    }

    /**
     * Prints an error message to stdout, used by the main program.
     * @param message - error message
     */
    private static void error(String message) {
        System.out.println("\nError: " + message);
        System.out.println("Usage: generatetext -c <content> -l <language> -v <voice> -f <fax> -e <e-mail> -m <video>");
        System.out.println("       -c Notification content such as c for count, s for subject or h for header");
        System.out.println("       -l User preferred language");
        System.out.println("       -v Number of voice mail in INBOX");
        System.out.println("       -f Number of fax mail in INBOX");
        System.out.println("       -e Number of e-mail in INBOX");
        System.out.println("       -m Number of video mail in INBOX\n");
    }

    /**
     * Makes an array of custom-headers defined for the users prefered language.
     * Note: the names need to be defined as header-x-<nr><Header name>={"Header Content"}
     * in the <language.cphr> file.
     *
     * @param inbox - the message counts for the current user.
     * @param email - all information about the notification.
     * @param user - information about the user that shall be notified.
     */
    public CustomHeader [] makeCustomHeaders(String content,
            UserMailbox inbox,
            NotificationEmail email,
            UserInfo user) {

        String userLanguage = null;

        if ((user.getBrand() != "" && user.getBrand() != null) && (user.getPreferredLanguage() != null && user.getPreferredLanguage() != ""))
            userLanguage = user.getBrand() + "_" + user.getPreferredLanguage();
        else
            userLanguage = user.getPreferredLanguage();

        // First get all identifiers matching "header-x-0-" from file
        String s1 = Phrases.getCphrTemplateStrings(userLanguage, user.getCosName());
        // Note: s could now be cos specific templates.. try null too.
        String s2 = Phrases.getCphrTemplateStrings(userLanguage, null);
        // If these are not the same, concatenate
        String s = null;
        if (s1.equals(s2)) {
            s = s1;
        } else {
            s = s1 + "\n" + s2;
        }

        /* 
        if (s == null) {
            return null;
        }
        */

        String [] lines = s.split("\n");
        Vector<CustomHeader> xHeaders = new Vector<CustomHeader>();
        String headerTemplate = null;
        int size = 0;

        if (content.matches("e[0-9]*")) {
            headerTemplate = "emailnotification-header-x-" + content.substring(1);
        } else if ((content.trim().matches("[a-z]")) || (content.trim().matches("[A-Z]"))) {
            headerTemplate = "emailnotification-header-x-" + content;
        }

        for (int i = 0; i < lines.length; i++) {
            if (lines[i].matches(headerTemplate + ".*=[. ]*\\{.*")) {
                String tmp = lines[i].substring(0, lines[i].indexOf("=")).trim();
                String generated = null;
                try {
                    generated = TextCreator.get().generateText(inbox, email, user, tmp, false, null);
                } catch (TemplateMessageGenerationException e) { }
                if (generated != null) { // badly generated ones will be ignored
                    xHeaders.add(new CustomHeader(tmp.substring(28, tmp.length()), generated));
                }
            }
        }

        //add priority to custom headers
        if(email.isUrgent() && (!content.equalsIgnoreCase("c"))){
            xHeaders.add(new CustomHeader("X-Priority","1"));
            xHeaders.add(new CustomHeader("X-MSMail-Priority","High"));
            xHeaders.add(new CustomHeader("Importance","High"));
        }

        if (xHeaders.size() > 0) {
            return xHeaders.toArray(new CustomHeader[size]);
        } else {
            return null;
        }
    }


    /**
     * Based on content a cc/bcc/reply-to header template is chosen.
     * For c, s, h (one letter content templates)
     * templates like header-cc-to-c are used
     * For e0-e9 templates are header-cc-0 to header-cc-9.
     * If none is found null is returned.
     *
     * @param inbox - the message counts for the current user.
     * @param email - all information about the notification.
     * @param user - information about the user that shall be notified.
     * @param content - the selected notification content.
     * @param headerType - the type of header text to find (CC, BCC, REPLYTO, SUBJECT)
     * @return one or several bcc/reply-to/cc-addresses (comma-separated) or a subject text
     */
    public synchronized String makeHeader(UserMailbox inbox,
            NotificationEmail email,
            UserInfo user,
            String content,
            int headerType) {
        String name = "";
       
        
        switch(headerType) {
            case CC:
                name="emailnotification-header-cc-";
                email.setProcessContentType(ProcessContentType.CC);
                break;
            case BCC:
                name="emailnotification-header-bcc-";
                email.setProcessContentType(ProcessContentType.BCC);
                break;
            case REPLYTO:
                email.setProcessContentType(ProcessContentType.REPLYTO);
                name="emailnotification-header-reply-to-";
                break;
            case FROM:
                name="emailnotification-header-from-";
                email.setProcessContentType(ProcessContentType.FROM);
                break;
            case SUBJECT:
                name="emailnotification-header-subject-";
                email.setProcessContentType(ProcessContentType.SUBJECT);
                break;
            case FROMENVELOPE:
                email.setProcessContentType(ProcessContentType.ENVELOPEFROM);
                name="emailnotification-header-from-envelope-";
                break;

            default:
                return null;
        }
        content = content.toLowerCase().trim();

        try {
            String headerTemplate = null;
            if (content.matches("e[0-9]*")) {
                headerTemplate = name + content.substring(1);
                return TextCreator.get().generateText(inbox, email,
                        user, headerTemplate, false, null);
            }
            else if ((content.matches("[a-z]")) ||
                    (content.matches("[A-Z]")) ||
                    (content.matches("(?i)" + Config.getQuotaTemplate())) ||
                    (content.matches("(?i)mailquotahighlevelexceeded")) ||
                    (content.matches("(?i)mailquotatypeexceeded")) ||
                    (content.matches("(?i)mailquotatypehighlevelexceeded")) ||
                    (content.matches("(?i)slamdown")) ||
                    (content.matches("(?i)faxprintfail"))) {

                headerTemplate = name + content;
                return TextCreator.get().generateText(inbox, email, user,
                        headerTemplate, false, null);
            }
            else {
                return null;
            }
        } catch(TemplateMessageGenerationException e) {
            return null;
        }
    }


    public static NtfEvent createNtfEvent() {
        final SendMessageReq req = new SendMessageReq();

        req.version.value = "1.0";
        req.operatorID.value = "rcpt12";
        req.transID.value = "trans12";
        req.destMsgClass.value = "im";
        req.destRcptID.value = "tel:+123456";
        req.rMsa.value = "msid:1111222233334444";
        req.rMsgID.value = "29dkjd";
        req.oMsa.value = "eid:1234";
        req.oMsgID.value = "dkdfdfd";
        req.eventType.value = "Deliv";
        req.eventID.value = "id";

        final HashMap<String, String> extra = new HashMap<String, String>();
        extra.put("srv-type", "foo");
        extra.put("BarFoo", "bar");

        req.extraValue = extra;
        req.eventID.value = "myid";

        // construct NTF event, the event is created based on message type
        MessageInfo msgInfo = null;
//                new MessageInfo(new MSA(req.oMsa.value),
//                        new MSA(req.rMsa.value), req.oMsgID.value,
//                        req.rMsgID.value);

        NtfEvent event = null;
        try {
            Container1 c1 = new Container1();
            
            c1.setFrom("eid:1234");
            c1.setTo("msid:1111222233334444");
            c1.appendHeaderValue("Priority", "0");

            MsgBodyPart[] c3Parts = new MsgBodyPart[2];
            c3Parts[0] =  new MsgBodyPart();
            c3Parts[1] =  new MsgBodyPart();

            StateAttributes attributes = new StateAttributes();

            msgInfo = commonMessagingAccess.storeMessageTest(c1, new Container2(), c3Parts, attributes);

            // convert to NTF event
            event = NtfEventGenerator.generateEvent("me", msgInfo, req.extraValue, req.eventID.value);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return event;
    }    

    /**
     * The main program prints a text to stdout generated from the phrase files
     * (*.phr and *.cphr) in the phrase files directory. The directory is
     * configured in the parameter PhraseTableLocale in file notification.cfg
     *
     * @param args command line arguments. The arguments are
     * -c Notification content
     * -l User preferred language
     * -v Number of voice mail in INBOX
     * -f Number of fax mail in INBOX
     * -e Number of e-mail in INBOX
     * -m Number of video mail in INBOX
     */
    public static void main(String[] args) {
        UserX user = new UserX();
        user.setCosName("testcos");
        NotificationEmail email = null;
        //NtfEvent ntfEvent = new NtfEvent("");
        //email = new NotificationEmail(ntfEvent);
        //NotificationEmail email = new NotificationEmail(9999, "Content-type:text/plain\nFrom:userx@host.domain\nX-priority:3\nSubject:testemail\n\ntemplate test");
        //NotificationEmail email = new NotificationEmail(9999, "Content-Type: multipart/voice-message; BOUNDARY=\"-222222222-222222222-2222222222=:22222\"\r\nFrom:userx@host.domain\nX-priority:3\nSubject:testemail\n\n---222222222-222222222-2222222222=:22222\r\nContent-Type: AUDIO/wav\r\nContent-Transfer-Encoding: BASE64\r\nContent-Description: Abcxyz voice Message\r\nContent-Duration: 42\r\nContent-Disposition: inline; voice=Voice-Message; filename=\"message.wav\"\r\n\r\nUklGRhRjAgBXQVZFZm10IBAAAAAHAAEAQB8AAEAfAAABAAgAZGF0YfBiAgBf\r\n bHZ1e/jr5u30cm7u7+rvaXd8cfJ7c+74fvp5fu5+aGxwb/ns7vB0b/rt5e52\r\n\r\n---222222222-222222222-2222222222=:22222--\r\n  ");
        
        NtfEvent ntfEvent = TextCreator.createNtfEvent();
        email = new NotificationEmail(ntfEvent);
        try {
            email.init();
        } catch (MsgStoreException e1) {
            e1.printStackTrace();
            return;
        }


        UserMailbox inbox = null;

        if (args.length < 14) {
            error("Missing arguments");
            return;
        }
        else if (args.length > 14) {
            error("Too many arguments");
            return;
        }

        // default values
        int voice = 0;
        int fax = 0;
        int mail = 0;
        int video = 0;
        String ntfHome = null;
        String content = "c";
        String language = "en";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-d")) {
                if(args[i+1] != null)
                    ntfHome = new String(args[i+1]);
            }
            if (args[i].equals("-c")) {
                if(args[i+1] != null)
                    content = new String(args[i+1]);
            }
            if (args[i].equals("-l")) {
                if(args[i+1] != null)
                    language = new String(args[i+1]);
            }
            if (args[i].equals("-v")) {
                if(args[i+1] != null) {
                    try {
                        voice = new Integer(args[i+1]).intValue();
                    }
                    catch(Exception e) {error("Unsupported value for voice: "+args[i+1]); return;}
                    if(voice < 0) {
                        error("Unsupported value for voice: " + voice);
                        return;
                    }
                }
            }
            if (args[i].equals("-f")) {
                if(args[i+1] != null) {
                    try {
                        fax = new Integer(args[i+1]).intValue();
                    }
                    catch(Exception e) {error("Unsupported value for fax: "+args[i+1]); return;}
                    if(fax < 0) {
                        error("Unsupported value for fax: " + fax);
                        return;
                    }
                }
            }
            if (args[i].equals("-e")) {
                if(args[i+1] != null) {
                    try {
                        mail = new Integer(args[i+1]).intValue();
                    }
                    catch(Exception e) {error("Unsupported value for e-mail: "+args[i+1]); return;}
                    if(mail < 0) {
                        error("Unsupported value for e-mail: " + mail);
                        return;
                    }
                }
            }
            if (args[i].equals("-m")) {
                if(args[i+1] != null) {
                    try {
                        video = new Integer(args[i+1]).intValue();
                    }
                    catch(Exception e) {error("Unsupported value for video: "+args[i+1]); return;}
                    if(video < 0) {
                        error("Unsupported value for video: " + video);
                        return;
                    }
                }
            }
        }

        if (ntfHome == null) {
            ntfHome = System.getProperty("ntfHome");
        }
        
        if(ntfHome != null)
            Config.updateCfg(ntfHome + "/cfg/notification.cfg");
        else
            Config.updateCfg("/opt/ntf/cfg/notification.cfg");

        TextCreator tc = TextCreator.get();
        user.setPreferredLanguage(language);
        inbox = new UserMailbox(voice, fax, mail, video, 0,0,0,0,false);

        if (content.matches("[ .]*e[0-9].*")) {
            try {
                System.out.println("Generated text: " + tc.generateText(inbox, email, user, content, false, null));
            } catch (TemplateMessageGenerationException e) {
                e.printStackTrace();
            }

            String tmp = tc.makeHeader(inbox, email, user, content, SUBJECT);
            if (tmp != null) {
                System.out.println("SUBJECT:    " + tmp);
            }
            tmp = tc.makeHeader(inbox, email, user, content, CC);
            if (tmp != null) {
                System.out.println("CC:         " + tmp);
            }
            tmp = tc.makeHeader(inbox, email, user, content, BCC);
            if (tmp != null) {
                System.out.println("BCC:        " + tmp);
            }
            tmp = tc.makeHeader(inbox, email, user, content, REPLYTO);
            if (tmp != null) {
                System.out.println("REPLY-TO:   " + tmp);
            }
            tmp = tc.makeHeader(inbox, email, user, content, FROM);
            if (tmp != null) {
                System.out.println("FROM:       " + tmp);
            }

            CustomHeader [] userDefinedHeaders = tc.makeCustomHeaders(content, inbox, email, user);
            if (userDefinedHeaders != null) {
                for (int i = 0; i < userDefinedHeaders.length; i++) {
                    if (userDefinedHeaders[i] != null) {
                        System.out.println(userDefinedHeaders[i].getHeader() +
                                ":" + userDefinedHeaders[i].getValue());
                    }
                }
            }
        } else {
            try {
                System.out.println("Generated text: " + tc.generateText(inbox, email, user, content, true, null));
            } catch (TemplateMessageGenerationException e) {
                e.printStackTrace();
            }
        }
        System.exit(0);
        return;
    }

}
