/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf.text;

import com.abcxyz.messaging.common.message.MultiNameValuePairs;
import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.oe.common.geosystems.GeoSystems;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.NotifierMfsException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.NotifierMfsException.NotifierMfsExceptionCause;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.ANotifierNotificationInfo;
import com.abcxyz.services.moip.common.cmnaccess.MoipMessageEntities;
import com.abcxyz.messaging.oe.lib.OEManager;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.slamdown.CallerInfo;
import com.mobeon.ntf.text.TemplateMessageGenerationException.TemplateExceptionCause;
import com.mobeon.ntf.mail.AMessageDepositInfo.ProcessContentType;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.util.Logger;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import jakarta.mail.MessagingException;


/**
 * Template implements a single text template used by TextCreator.
 */
public class Template implements Constants {
    private static Logger log = Logger.getLogger(Template.class);

    /**Initial StringBuffer size (size of largest text produced so far)*/
    private TextTagObject END = new EndTag();
    private int stringBufferSize = 160;
    private boolean countSpecific = false;
    private boolean countSpecificCphr = false;
    private Properties phr;
    private String cphr;
    private TextTagObject cphrChain;
    private String content;

    private static String VVATAG = "__TAG=";


     /**
     * Constructor. Creates a Template with a pre-compiled TextTagObject
     * chain for the template of the new count specific format with file
     * extension (.phr).
     * @param phr - the phrases for this template.
     * @param cphr - count specific string.
     * @param contentIsPhrase - Whether or not the <code>content</code> parameter represents a phrase
     * to parse (instead of the index of a phrase to parse).
     **/
    Template(Properties phr, String content, String cphr, String lang, boolean contentIsPhrase) {
       this.content = content;
       this.phr = phr;
       this.cphr = cphr;
       cphrChain = END;
       countSpecificCphr = true;
       String line="";
       String tag="";
       String notifContent;

       if (!contentIsPhrase)
           notifContent = getTemplateContent(content);
       else
           notifContent = content;
       if(notifContent != null) {
           TemplateTokenizerCphr strtoken = new TemplateTokenizerCphr(notifContent);
           log.logMessage("Creating template " + content, Logger.L_VERBOSE);
           while((line=strtoken.getNextLine()) != null){
               TemplateTokenizerCphr linetoken = new TemplateTokenizerCphr(line);
               while((tag=linetoken.getNext()) != null) {
                   log.logMessage("Make tag of " + tag, Logger.L_VERBOSE);
                   cphrChain = makeTagCphr(tag, cphrChain, phr, lang);
               }
           }
       }
    }
    
    /**
     * Constructor. Creates a Template with a pre-compiled TextTagObject
     * chain for the template of the new count specific format with file
     * extension (.phr).
     * @param phr - the phrases for this template.
     * @param cphr - count specific string.
     **/
    Template(Properties phr, String content, String cphr, String lang)
    {
        this(phr, content, cphr, lang, false);
    }



    /**
     * makeChain parses a template string and creates a chain of TextTagObjects
     * that will generate the desired string.
     *@param templateStrings - all phrases in the phrase file for the preferred
     * language.
     *@param mainTemplate - the main template string to parse
     *@return the first tag object in the created template chain.
     */
    private TextTagObject makeChain(Properties templateStrings,
                                    String mainTemplate,
                                    String lang) {
        log.logMessage("Making new template chain from \"" + mainTemplate + "\"",
                       Logger.L_VERBOSE);
        if (mainTemplate == null) { return null; }

        TemplateTokenizer tt = new TemplateTokenizer(mainTemplate);
        String tag;
        TextTagObject chain = END;

        while ((tag = tt.getNext()) != null) {
            chain = makeTagCphr(tag, chain, templateStrings, lang);
        }
        return chain;
    }



    /**
     * makeTag creates a single tag object for a token from the main template
     * string.
     *@param tagString - the token string corresponding to this tag.
     *@param nextTagObject - the next tag object in the chain (shall be appended
     * to the tag object created by this function).
     *@param templateStrings - all phrases in the phrase file for the preferred
     * language.
     *@return a new tag object, linked to the next tag object.
     */
    private TextTagObject makeTagCphr(String tagString,
                                      TextTagObject nextTagObject,
                                      Properties templateStrings,
                                      String lang) {

        if (tagString.indexOf("__SUBJECT") != -1) {
            return new SubjectTag(nextTagObject);
        } else if (tagString.indexOf("__FROM") != -1 || tagString.indexOf("__FROM=ctx:") != -1) {
            return new FromTag(nextTagObject, templateStrings, tagString);
        } else if (tagString.indexOf("__SIZE") != -1) {
            return new SizeTag(nextTagObject, templateStrings);
        } else if (tagString.indexOf("__CONVERTED_PHONE") != -1) {
            return new ConvertedPhoneTag(nextTagObject, templateStrings);
        } else if (tagString.indexOf("__PHONE") != -1) {
            return new PhoneTag(nextTagObject);
        } else if (tagString.indexOf("__NUM_ATTACHMENTS") != -1) {
            return new AttachmentsTag(nextTagObject);
        } else if (tagString.indexOf("__EMAIL_TEXT") != -1) {
            return new TextTag(nextTagObject);
        } else if (tagString.indexOf("__STATUS") != -1) {
            return new StatusTag(nextTagObject, templateStrings);
        } else if (tagString.indexOf("__TCOUNT") != -1 || tagString.indexOf("__TCOUNT=") != -1) {
            return new NumNewTotTag(nextTagObject, tagString);
        } else if (tagString.indexOf("__VCOUNT") != -1 || tagString.indexOf("__VCOUNT=") != -1) {
            return new NumNewVoiceTag(nextTagObject, tagString);
        } else if (tagString.indexOf("__UVCOUNT") != -1) {
            return new NumNewUrgVoiceTag(nextTagObject);
        } else if (tagString.indexOf("__CVCOUNT") != -1) {
            return new NumNewConfVoiceTag(nextTagObject);
        } else if (tagString.indexOf("__VACOUNT") != -1 || tagString.indexOf("__VACOUNT=") != -1) {
            return new NumAllVoiceTag(nextTagObject, tagString);
        } else if (tagString.indexOf("__FCOUNT") != -1 || tagString.indexOf("__FCOUNT=") != -1) {
            return new NumNewFaxTag(nextTagObject, tagString);
        } else if (tagString.indexOf("__ECOUNT") != -1 || tagString.indexOf("__ECOUNT=") != -1) {
            return new NumNewEmailTag(nextTagObject, tagString);
        } else if (tagString.indexOf("__MCOUNT") != -1 || tagString.indexOf("__MCOUNT=") != -1) {
            return new NumNewVideoTag(nextTagObject, tagString);
        } else if (tagString.indexOf("__UMCOUNT") != -1) {
            return new NumNewUrgVideoTag(nextTagObject);
        } else if (tagString.indexOf("__CMCOUNT") != -1) {
            return new NumNewConfVideoTag(nextTagObject);
        } else if (tagString.indexOf("__QUOTA_TEXT") != -1) {
            return new QuotaTextTag(nextTagObject, templateStrings);
        } else if (tagString.indexOf("__TIME") != -1) {
            return new TimeTag(nextTagObject, templateStrings);
        } else if (tagString.indexOf("__TYPE") != -1) {
            return new DepositTypeTag(nextTagObject, templateStrings);
        } else if (tagString.indexOf("__PRIORITY") != -1) {
            return new PriorityTag(nextTagObject, templateStrings);
        } else if (tagString.indexOf("__CONFIDENTIAL") != -1) {
            return new ConfidentialTag(nextTagObject, templateStrings);
        } else if (tagString.indexOf("__HANDLE_CONFIDENTIAL") != -1) {
            return new HandleConfidentialTag(nextTagObject, templateStrings);
        } else if (tagString.indexOf("__APPLEIMAPADDRESSINFO") != -1) {
            return new ImapAddressInfoTag(nextTagObject, templateStrings);
        } else if (tagString.indexOf("__APPLEIMAPPORTINFO") != -1) {
            return new ImapPortInfoTag(nextTagObject, templateStrings);
        } else if (tagString.indexOf("__IMAPUSERINFO") != -1) {
            return new ImapUserInfoTag(nextTagObject, templateStrings);
        } else if (tagString.indexOf("__IMAPPASSWORDINFO") != -1) {
            return new ImapPasswordInfoTag(nextTagObject, templateStrings);
        }else if (tagString.indexOf("__DATE=") != -1) {
            return new FormattedDateTag(nextTagObject,
                                        tagString.substring(7, tagString.length() ).replaceAll("_"," "),
                                        templateStrings,
                                        lang);
        } else if (tagString.indexOf("__DATE") != -1) {
            return new DateTag(nextTagObject);
        } else if (tagString.indexOf("__COUNT") != -1 || tagString.indexOf("__COUNT=") != -1) {
            return new CountTag(nextTagObject, tagString);
        } else if (tagString.indexOf("__UID") != -1) {
            return new UidTag(nextTagObject,templateStrings);
        } else if (tagString.indexOf("__VVM_PREFIX") != -1) {
        	return new VvmPrefixTag(nextTagObject,templateStrings);
        } else if (tagString.indexOf(VVATAG) != -1) {
        	return new VvaSmsTag(nextTagObject, templateStrings, tagString);
        }

        // Ok, this saves some performance.
        //  Keep the unicode stuff outside "ordinary text " \u002a " like this."
        else if (tagString.matches("__\\\\u([0-9a-fA-F]+)")) {
            return new ConstTag(nextTagObject,
                       Character.toString(((char) Integer.parseInt(tagString.substring(4,tagString.length()), 16))));
        }
        else if (tagString.indexOf("__UNICODE=") != -1) { // or use \u1234 inline
          if (tagString.substring(10,tagString.length()).matches("([0-9a-fA-F]+)")) {
            return new ConstTag(nextTagObject,
                       Character.toString(((char) Integer.parseInt(tagString.substring(10,tagString.length()), 16))));
          } else {
            return new ConstTag(nextTagObject, "#");
          }
        }
        else if (tagString.indexOf("__QUOTE") != -1) {
            return new ConstTag(nextTagObject, "\"");
        }
        else if (tagString.indexOf("__CTAG_") != -1) {
            return new CTag(nextTagObject, tagString.substring(2), templateStrings);
        }
        else if (tagString.indexOf("__PAYLOAD") != -1) {
            return new PayloadTag(nextTagObject);
        }
        else if(tagString.matches("\\(.*,.*,.*,.*\\)")) {
            return new CondTag(nextTagObject, tagString, false);
        }
        else if(tagString.matches("\\(.*,.*,.*,.*,.*,.*\\)")) {
            return new CondTag(nextTagObject, tagString, false);
        }
        else if(tagString.matches("\\(.*,.*,.*,.*,.*,.*,.*,.*\\)")) {
            return new CondTag(nextTagObject, tagString, false);
        }
        else if(tagString.matches("\\(.*\\)")) {
            return new CondTag(nextTagObject, tagString, true);
        }
        else {
            return new ConstTag(nextTagObject, tagString);
        }
    }
    
    /**
     * Formats the content given and prepends 0's to it if needed.
     */
    private static String formatDecimalPlaces(String token, String content)
    {
        char x = 'x';
        int eqLoc = token.indexOf('=');
        // If no =, return content
        if (eqLoc == -1) return content;
        // Get number of decimal places
        int prependLength = 0;
        int max = 0;
        boolean maxSet = false;
        for (int i = eqLoc + 1; i < token.length(); i++)
        {
            char c = token.charAt(i);
            // If we have something other than an x after the =, ignore and return content.
            if (c != x) {
                maxSet = true;
                max *= 10;
                try {
                    int m = Integer.parseInt(""+c);
                    max += m;
                } catch (NumberFormatException nfe) {
                    log.logMessage("Could not parse count token: " + new String(token), Logger.L_ERROR);
                    return content;
                }
            }
            else {
                prependLength++;
            }
        }
        try {
            int value = Integer.parseInt(content);
            if (value > max && maxSet) {
                value = max;
            }
            content = ""+value;
        } catch (NumberFormatException nfe) {
            log.logMessage("Could not parse content into int: " + content, Logger.L_ERROR);
            return content;
        }
        prependLength -= content.length();
        // Get array of wanted 0's
        if (prependLength <= 0) return content;
        String prepend = "";
        for (int i = 0; i < prependLength; i++)
        {
            prepend += "0";
        }
        // Return result
        return prepend + content;
    }

    /**
     * Returns a formatted phone number from the formatting rules.
     * @param number The number to format.
     * @param context The context of the format.
     */
    private static String formatPhoneNumber(String number, String context)
    {
        if (context.length() < 1) return number;

        String formattedNumber = CommonMessagingAccess.getInstance().normalize(number, context, false);
        if (formattedNumber == null) {
            return number;
        }
        return formattedNumber;
    }

  /**
   * Used internally in created tags that can not contain
   * TAG specific information (recurse).
   */
  protected String getTemplateContentTrimmed (String content, Properties property, String fallback) {

    String line ="";
    String tag = "";
    int strBufSize = 32;
    StringBuffer sb = new StringBuffer(strBufSize);
    String myContent = getTemplateContent(content);

    boolean tagAppended = false;

    if (myContent != null) {
      TemplateTokenizerCphr strtoken = new TemplateTokenizerCphr(myContent);
      while((line=strtoken.getNextLine()) != null){
        TemplateTokenizerCphr linetoken = new TemplateTokenizerCphr(line);
        while((tag=linetoken.getNext()) != null) {
          log.logMessage("getTemplateContentTrimmed for tag " + tag, Logger.L_DEBUG);
          if (tag.startsWith("__")) {
              if (tag.matches("__\\\\u([0-9a-fA-F]+)")) {
                  tag =  Character.toString(((char) Integer.parseInt(tag.substring(4,tag.length()), 16)));
              } else if (tag.indexOf("__UNICODE=") != -1) {
                  tag =  Character.toString(((char) Integer.parseInt(tag.substring(10,tag.length()), 16)));
              } else
              {
                  log.logMessage("getTemplateContentTrimmed, tag content: [" + myContent + "] does not support TAG," + tag + " ignoring! ", Logger.L_ERROR);
                  tag = "";
              }
          }

          if (tag.length() > 0) {
              sb.insert(0, tag); //insert at beginning as the tags are read in reverse order.
              //sb.append(tag);
              tagAppended = true;
          }
        }
      }
    }
    String tmp = null;
    if (!tagAppended) {
      if (property != null && content != null) {
        tmp = property.getProperty(content.toLowerCase()); // All props in lower
        if (tmp == null) {
          tmp = fallback;
        }
      } else {
        tmp = fallback;
      }
      return tmp;
    }
    return sb.toString();
  }

    /**
     * getTemplateContent returns a phrase for a notification content
     * @param content the notification content which is c for count,
     * s for subject and h for header
     * @return a String with a phrase
     */
    private String getTemplateContent(String content) {
        String line="";
        StringBuffer strbuf = new StringBuffer();
        if (cphr == null) {
          log.logMessage("Failed to read phrase with " + content, Logger.L_ERROR);
          return null;
        }
        StringTokenizer cphrTokenizer = new StringTokenizer(cphr, "\n");

        while(cphrTokenizer.hasMoreTokens()) {

          line = cphrTokenizer.nextToken();
            // All are handled in the same way
          if (content.matches(".*") &&
                   line.matches("[. ]*(?i)" + content.trim() + "[. ]*=[. ]*\\{.*")) {
            strbuf.append(line.substring(line.indexOf("{"), line.length())); // from { to eol
            while(cphrTokenizer.hasMoreTokens() && (line.indexOf("}") == -1)) {
              line = cphrTokenizer.nextToken();
              strbuf.append(line).append("\n");
              }
            return strbuf.toString();
          }
        }

        if( phr != null && phr.getProperty(content) != null ) {
            return phr.getProperty(content);
        }

        log.logMessage("Failed to read phrase with " + content, Logger.L_VERBOSE);
        return null;
    }

    /**
     * generateText generates a text message.
     * @param inbox - The message counts for the current user.
     * @param email - All information about the notification.
     * @param user - Information about the user that shall be notified.
     * @param generateDefault - true usually, but for tags like header-reply-to and other tags an TemplateMessageGenerationException 
     *                          is desired instead of the default "You have messages" text.
     *                          The calling code can then decide how to deal with the exception depending on the TemplateMessageGenerationException.TemplateExceptionCause.
     * @param caller - an alternate container for all information about the notification; used for slamdown.
     * @param notifInfo - an alternate container for all information about the notification; used for generic notification types.
     * @return a text message with data from the email inserted.
     * @throws TemplateMessageGenerationException if text generation failed and generateDefault is false.
     */
    public String generateText(UserMailbox inbox,
                               NotificationEmail email,
                               UserInfo user,
                               boolean generateDefault,
                               CallerInfo caller,
                               ANotifierNotificationInfo notifInfo) throws TemplateMessageGenerationException {
        StringBuffer sb = new StringBuffer(stringBufferSize);
        log.logMessage("Generating text for " + content + ":" + user.getPreferredLanguage(),
                Logger.L_DEBUG);

        TextTagObject chain = cphrChain;

        if (chain != null) {
            try {
                chain.appendValue(sb, email, user, inbox, caller, notifInfo);
                if (sb.length() > stringBufferSize) {
                    stringBufferSize = sb.length();
                }
                return sb.toString();

            } catch (TemplateMessageGenerationException e) {
                //Failed to produce a string, e.g. because some message count was available
                log.logMessage("Failed to produce proper text using template " + content + ": " + e.getMessage(), Logger.L_DEBUG);
                if(!generateDefault) {
                    throw e;
                }
            }
        }
        
        //Return a generic notification message if the template can not be
        //found or the correct text can not be generated.
        if (generateDefault && phr != null) {
            return getTemplateContentTrimmed("general", phr, "New Message"); // current template
        } else {
            String errorMsg = "Failed to produce proper text using template " + content;
            log.logMessage(errorMsg, Logger.L_DEBUG);
            throw new TemplateMessageGenerationException(errorMsg);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("\n{Template " + content);
        if (cphrChain != null) {
          sb.append(cphrChain.toString());
        }

        return sb.toString() + "}";
    }

    private String getCounter(String line) {
        if (line.matches(".*\\(.*\\).*")) {
            return line.substring(line.indexOf("(")+1, line.indexOf(")"));
        }
        return null;
    }

    private String getValue(String line) {
        if (line.matches(".*\\).*$")) {
            return line.substring(line.indexOf(")")+1).trim();
        }
        else if (line.matches(".*\\\".*"))
            return line.trim();
        return null;
    }

    private String convertAmPmMarker(String msgdate, String am, String pm) {
           if(msgdate == null)
               return null;

           String before;
           String after;
           String result;
           int amindex=msgdate.toLowerCase().indexOf("am");
           int pmindex=msgdate.toLowerCase().indexOf("pm");

           if (amindex >= 0 && am != null) {
               before = msgdate.substring(0, amindex);
               after = msgdate.substring(amindex+2);
               result = before + am + after;
           }
           else if (pmindex >= 0 && pm != null) {
               before = msgdate.substring(0, pmindex);
               after = msgdate.substring(pmindex+2);
               result = before + pm + after;
           }
           else
               result = msgdate;
           return result;
        }

    private final class Conversion {
        public String[] matchPrefix = new String[0];
        public String[] newPrefix = new String[0];
        public String[] matchSuffix = new String[0];
        public String[] newSuffix = new String[0];

        /**
         * Constructor. Creates a Conversion with a
         *@param convSpec - A string specifying the conversion, on the format
         * /prefix1>newprefix1,suffix1>newsuffix1/prefix2>newprefix2,suffix2>newsuffix2/...
         **/
        Conversion(String convSpec) {
            log.logMessage("Making new conversion from \"" + convSpec + "\"", Logger.L_DEBUG);
            Vector<String> mpv = new Vector<String>();
            Vector<String> npv = new Vector<String>();
            Vector<String> msv = new Vector<String>();
            Vector<String> nsv = new Vector<String>();
            String mp;
            String np;
            String ms;
            String ns;
            int pos = 0;
            int slashPos;
            int commaPos;
            int toPos;
            if (convSpec == null) { convSpec = ""; }
            while (pos < convSpec.length()) {
                mp = "";
                np = "";
                ms = "";
                ns = "";
                slashPos = convSpec.indexOf("/", pos);
                if (slashPos < 0) { slashPos = convSpec.length(); }
                //Here pos is after the last consumer character and slashpos is
                //after the last character of the next conversion
                log.logMessage("Conversion " + convSpec.substring(pos, slashPos), Logger.L_DEBUG);
                if (slashPos == pos) { //Empty string before the next slash
                    pos++;
                } else {
                    commaPos = convSpec.indexOf(",", pos);
                    if (commaPos < 0 || commaPos > slashPos) { //No comma, prefix only
                        commaPos = slashPos;
                    }
                    if (commaPos > pos) { //There is a prefix
                        log.logMessage("  Prefix " + convSpec.substring(pos, commaPos), Logger.L_DEBUG);
                        toPos = convSpec.indexOf(">", pos);
                        if (toPos < 0 || toPos >= commaPos)  { //No > in conversion spec.
                            log.logMessage("Bad conversion specification \"" + convSpec
                                           + "\", a \">\" is missing in prefix.", Logger.L_ERROR);
                            return;
                        }
                        log.logMessage("    From " + convSpec.substring(pos, toPos), Logger.L_DEBUG);
                        log.logMessage("    To " + convSpec.substring(toPos + 1, commaPos), Logger.L_DEBUG);
                        if (toPos > pos) { mp = convSpec.substring(pos, toPos); }
                        if (commaPos > toPos) { np = convSpec.substring(toPos + 1, commaPos); }
                    }
                    if (commaPos + 1 < slashPos) { //There is a suffix (not empty)
                        log.logMessage("  Suffix " + convSpec.substring(commaPos + 1, slashPos), Logger.L_DEBUG);
                        toPos = convSpec.indexOf(">", commaPos + 1);
                        if (toPos < 0 || toPos >= slashPos)  { //No > in conversion spec.
                            log.logMessage("Bad conversion specification \"" + convSpec
                                           + "\", a \">\" is missing in suffix.", Logger.L_ERROR);
                            return;
                        }
                        log.logMessage("    From " + convSpec.substring(commaPos + 1, toPos), Logger.L_DEBUG);
                        log.logMessage("    To " + convSpec.substring(toPos + 1, slashPos), Logger.L_DEBUG);
                        if (toPos > commaPos) { ms = convSpec.substring(commaPos + 1, toPos); }
                        if (slashPos > toPos + 1) { ns = convSpec.substring(toPos + 1, slashPos); }
                    }
                    mpv.add(mp);
                    npv.add(np);
                    msv.add(ms);
                    nsv.add(ns);
                    pos = slashPos + 1;
                }
            }
            matchPrefix = mpv.toArray(matchPrefix);
            newPrefix = npv.toArray(newPrefix);
            matchSuffix = msv.toArray(matchSuffix);
            newSuffix = nsv.toArray(newSuffix);
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("{Conversion /");
            for (int i = 0 ; i < matchPrefix.length; i++) {
                sb.append(matchPrefix[i]).append(">").append(newPrefix[i]).append(",")
                    .append(matchSuffix[i]).append(">").append(newSuffix[i]).append("/");
            }
            return sb.toString() + "}";
        }
    }


    /**
     * Specifies the methods that must be available from all text tag
     * objects
     */
    private class TextTagObject {
        /**Next tag object in the template*/
        protected TextTagObject next;

        /**
         *@param next - the next tag object in the chain.
         */
        protected TextTagObject(TextTagObject next) {
            this.next = next;
        }



        /**
         * Appends the value corresponding to a template tag to a StringBuffer.
         * </p>
         * TODO Currently, the information about the notification can be passed to this method in 3 possible containers:
         * NotificationEmail, CallerInfo and ANotifierNotificationInfo.
         * Ideally, only an ANotifierNotificationInfo should be passed to this method.
         * 
         * @param sb The StringBuffer where the value is appended.
         * @param email The NotificationEmail from which the tag object may extract information needed by a specific tag implementation.
         * @param user information about the user that shall be notified.
         * @param inbox info from the users inbox.
         * @param caller CallerInfo from which the tag object may extract information needed by a specific tag implementation.
         * @param notifInfo ANotifierNotificationInfo from which the tag object may extract information needed by a specific tag implementation.
         * @throws TemplateMessageGenerationException upon error
         */
        public void appendValue(StringBuffer sb, NotificationEmail email,
                                UserInfo user, UserMailbox inbox, CallerInfo caller, ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
        }

        /**
         * Generates a printable string showing the configuration of a tag
         * object and all its successors.
         *@return a printable string showing the configuration of a tag
         * object and all its successors.
         */
        public String toString() {
            return "";
        }
    }


    /**
     * EndTag appends nothing, it just ends the tag chain.
     */
    private final class EndTag extends TextTagObject {

        EndTag() {
            super(null);
            log.logMessage("Making EndTag", Logger.L_VERBOSE);
        }
    }


    /**
     * ConstTag appends a text that is independent from information about a
     * specific notification.
     */
    private final class ConstTag extends TextTagObject {
        private String value;

        ConstTag(TextTagObject next, String value) {
            super(next);
            log.logMessage("Making ConstTag \"" + value + "\"", Logger.L_VERBOSE);
            this.value = value;
        }

        public void appendValue(StringBuffer sb, NotificationEmail email,
                                UserInfo user, UserMailbox inbox, CallerInfo caller, ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            next.appendValue(sb.append(value), email, user, inbox, caller, notifInfo);
        }

        public String toString() {
            return "{\"" + value + "\"}" + next;
        }
    }


    /**
     * SubjectTag appends the subject of the latest message
     */
    private final class SubjectTag extends TextTagObject {

        SubjectTag(TextTagObject next) {
            super(next);
            log.logMessage("Making SubjectTag ", Logger.L_VERBOSE);
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller, 
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            if(email != null) {
                sb.append(email.getSubject());
            } else {
                log.logMessage("SubjectTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
            }
            next.appendValue(sb, email, user, inbox, caller, notifInfo);
        }

        public String toString() {
            return "{subject}" + next;
        }
    }
   
    /**
     * PayloadTag appends the message payload
     */
    private final class PayloadTag extends TextTagObject {

        PayloadTag(TextTagObject next) {
            super(next);
            log.logMessage("Making PayloadTag ", Logger.L_VERBOSE);
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            
            if(notifInfo != null) {
                try {
                    sb.append(notifInfo.getMessagePayloadAsString());
                } catch (NotifierMfsException nmfse) {
                    String message = "Cannot generate payload for PAYLOAD tag.";
                    NotifierMfsExceptionCause nmfseCause = nmfse.getNotifierMfsExceptionCause();
                    switch (nmfseCause) {
                        case FILE_DOES_NOT_EXIST:
                            throw new TemplateMessageGenerationException(message,
                                    TemplateExceptionCause.CAUSE_PAYLOAD_FILE_DOES_NOT_EXIST);
                        case FILE_NOT_ACCESSIBLE:
                            throw new TemplateMessageGenerationException(message,
                                    TemplateExceptionCause.CAUSE_PAYLOAD_FILE_NOT_ACCESSIBLE);
                        default:
                            throw new TemplateMessageGenerationException(message);
                    }
                } catch (Exception e) {
                    throw new TemplateMessageGenerationException("Cannot generate payload for PAYLOAD tag: " + e.getMessage());
                }      
            } else {
                log.logMessage("PayloadTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
            }
            next.appendValue(sb, email, user, inbox, caller, notifInfo);
        }
            

        public String toString() {
            return "{payload}" + next;
        }
    }


    /**
     * FromTag appends the sender of the latest message.
     */
    private final class FromTag extends TextTagObject {
        private String unknown;
        private String unknownMail;
        private String prefix;
        private final String tag;
        private final Properties templateStrings;

        FromTag(TextTagObject next, Properties templateStrings, String tag) {
            super(next);
            this.tag = tag;
            this.templateStrings = templateStrings;
            log.logMessage("Making FromTag ", Logger.L_VERBOSE);
            unknown = getTemplateContentTrimmed("unknownsender", templateStrings,
                                                getTemplateContentTrimmed("numwithheldtext", templateStrings, "#"));
            prefix = getTemplateContentTrimmed("fromnumberprefix", templateStrings, "");
            
            unknownMail = getTemplateContentTrimmed("unknown_email_sender", templateStrings,
                    getTemplateContentTrimmed("numwithheldtext", templateStrings, "#"));
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            log.logMessage("FromTag::appendValue: Entering method", Logger.L_DEBUG);
            String from = null;
            if( caller != null ) {
               from = caller.getNumber();
               if (from != null && from.length() > 0) {
                   log.logMessage("FromTag::appendValue: Got 'from' from caller.getNumber()", Logger.L_DEBUG);
                   if (Config.denormalizeFromTag()) {
                       from = CommonMessagingAccess.getInstance().denormalizeNumber(from);
                   } else {
                       log.logMessage("FromTag::appendValue: Calling normalizeAddressField()", Logger.L_DEBUG);
                       try {
                           from = CommonMessagingAccess.getInstance().normalizeAddressField(from);
                            
                           //remove the URI if any
                           int start = from.indexOf(":") + 1;
                           if(start>0)
                              from=from.substring(start);
                           
                           int end=from.indexOf(">");
                           if (end != -1)
                              from=from.substring(0, end);
                           
                           from=from.trim();
                           log.logMessage("FromTag::appendValue: normalized non-URI address=" + from, Logger.L_DEBUG);
                           }
                       
                           catch (Exception e) {
                              log.logMessage("FromTag::appendValue: Exception caught when trying to normalize FROM address", Logger.L_DEBUG);
                              // Fallback to denormalization
                              from = CommonMessagingAccess.getInstance().denormalizeNumber(from);
                           }
                   }
                   from = prefix + from;
               }

               int maxDigitsInNumber = 0;
               String truncatedNumberIndication = "";
               if(!caller.getIsSendingAsGenericNotif()) {
                   if(caller.isInternal()) {
                       maxDigitsInNumber = Config.getSlamdownMaxDigitsInNumber();
                       truncatedNumberIndication = Config.getSlamdownTruncatedNumberIndication();
                   } else {
                       maxDigitsInNumber = Config.getMcnMaxDigitsInNumber();
                       truncatedNumberIndication = Config.getMcnTruncatedNumberIndication();
                   }
               }

               if (maxDigitsInNumber != 0 && from != null && from.length() > maxDigitsInNumber) {
                    if ("".equals(truncatedNumberIndication)) {
                        from = from.substring(from.length() - maxDigitsInNumber);
                    } else {
                        from = truncatedNumberIndication.substring(0, 1)
                            + from.substring(from.length() - maxDigitsInNumber + 1);
                    }
                }
               
            } else if(notifInfo != null) {
                if (notifInfo.getSenderVisibility()) {
                    from = notifInfo.getSenderPhoneNumber();
                    if (from != null && from.length() > 0) {
                        log.logMessage("FromTag::appendValue: Got 'from' from notifInfo.getSenderPhoneNumber()", Logger.L_DEBUG);
                        if (Config.denormalizeFromTag()) {
                        	from = CommonMessagingAccess.getInstance().denormalizeNumber(from);
                        } else {
                            log.logMessage("FromTag::appendValue: Calling normalizeAddressField()", Logger.L_DEBUG);
                            try {
                                from = CommonMessagingAccess.getInstance().normalizeAddressField(from);
                                
                                //remove the URI if any
                                int start = from.indexOf(":") + 1;
                                if(start>0)
                                   from=from.substring(start);
                                
                                int end=from.indexOf(">");
                                if (end != -1)
                                   from=from.substring(0, end);
                                
                                from=from.trim();
                                log.logMessage("FromTag::appendValue: normalized non-URI address=" + from, Logger.L_DEBUG);
                                }
                                catch (Exception e) {
                                   log.logMessage("FromTag::appendValue: Exception caught when trying to normalize FROM address", Logger.L_DEBUG);
                                   // Fallback to denormalization
                                   from = CommonMessagingAccess.getInstance().denormalizeNumber(from);
                                }
                        }
                        from = prefix + from;
                    } else {
                        if ((email.getProcessContentType() == ProcessContentType.ENVELOPEFROM) || 
                            (email.getProcessContentType() == ProcessContentType.FROM) ){
                            from = unknownMail;
                        } else {
                            from = unknown;
                        }
                    }

                } else {         
                    String displayName = notifInfo.getSenderDisplayName();
                    if (displayName != null && displayName.length() > 0 ){
                        displayName = displayName.toLowerCase();
                        if ( "unknown".equals(displayName)  ){                            
                           if ((email.getProcessContentType() == ProcessContentType.ENVELOPEFROM) || 
                               (email.getProcessContentType() == ProcessContentType.FROM) ){
                               from = unknownMail;
                           } else {
                               from = unknown;
                           }
                        } else {
                            String tmpStr = null;
                            if ((email.getProcessContentType() == ProcessContentType.ENVELOPEFROM) || 
                                (email.getProcessContentType() == ProcessContentType.FROM) ){
                                    tmpStr = unknownMail;
                                } else {
                                    tmpStr = unknown;
                                }
                            from = getTemplateContentTrimmed(displayName, templateStrings, tmpStr);
                        }
                    } else {                        
                        if ((email.getProcessContentType() == ProcessContentType.ENVELOPEFROM) || 
                                (email.getProcessContentType() == ProcessContentType.FROM) ){
                                from = unknownMail;
                            } else {
                                from = unknown;
                            }
                    }
                                       
                    log.logMessage("Tag FROM set to " + from + " since sender-visibility is 0.", Logger.L_DEBUG);
                }
            }  else {

                if (email.getSenderVisibile()) {

                    switch (email.getDepositType()) {
                        case VIDEO:
                        case VOICE:
                        case FAX:
                        case FAX_RECEPT_MAIL_TYPE:
                            if (Config.denormalizeFromTag()) {
                                log.logMessage("FromTag::appendValue: Getting 'from' from email.getSenderPhoneNumber()", Logger.L_DEBUG);
                                from = email.getSenderPhoneNumber();
                                if (from != null && from.length() > 0) {
                                    log.logMessage("FromTag::appendValue: Calling denormalizeNumber()", Logger.L_DEBUG);
                                    from = CommonMessagingAccess.getInstance().denormalizeNumber(from);
                                    from = prefix + from;
                                }
                            } else {
                                // Getting FROM from getSender instead of getSenderPhoneNumber because the latter was already denormalized
                                log.logMessage("FromTag::appendValue: Getting 'from' from email.getSender()", Logger.L_DEBUG);
                                from = email.getSender();
                                if (from != null && from.length() > 0) {
                                    log.logMessage("FromTag::appendValue: Calling normalizeAddressField()", Logger.L_DEBUG);
                                    try {
                                        from = CommonMessagingAccess.getInstance().normalizeAddressField(from);
                                        
                                        //remove the URI if any
                                        int start = from.indexOf(":") + 1;
                                        if(start>0)
                                           from=from.substring(start);
                                        
                                        int end=from.indexOf(">");
                                        if (end != -1)
                                           from=from.substring(0, end);
                                        
                                        from=from.trim();
                                        log.logMessage("FromTag::appendValue: normalized non-URI address=" + from, Logger.L_DEBUG);
                                        }
                                        catch (Exception e) {
                                           log.logMessage("FromTag::appendValue: Exception caught when trying to normalize FROM address", Logger.L_DEBUG);
                                           // Fallback to denormalization
                                           from = CommonMessagingAccess.getInstance().denormalizeNumber(from);
                                        }
                                    from = prefix + from;
                                }
                            }
                            break;
                        case EMAIL:
                        default:
                            from = email.getSender();
                            break;
                    }
                    if (from == null || "".equals(from)) {
                        
                        if ((email.getProcessContentType() == ProcessContentType.ENVELOPEFROM) || 
                                (email.getProcessContentType() == ProcessContentType.FROM) ){
                                from = unknownMail;
                            } else {
                                from = unknown;
                            }
                    }

                } else {
         
                    String displayName = email.getSenderDisplayName();
                    if (displayName != null && displayName.length() > 0 ){
                        displayName = displayName.toLowerCase();
                        if ( "unknown".equals(displayName)  ){
                            
                            // if processing the contentType envelopp, we have to put a different
                            if ((email.getProcessContentType() == ProcessContentType.ENVELOPEFROM) || 
                               (email.getProcessContentType() == ProcessContentType.FROM) ){
                                from = unknownMail;
                            } else {
                                from = unknown;
                            }
                        } else {
                            // if processing the contentType envelopp, we have to put a different
                            if ((email.getProcessContentType() == ProcessContentType.ENVELOPEFROM) || 
                                (email.getProcessContentType() == ProcessContentType.FROM) ) {
                                from = getTemplateContentTrimmed(displayName, templateStrings, unknownMail);
                            } else {
                                from = getTemplateContentTrimmed(displayName, templateStrings, unknown);
                            }
                            
                        }
                    } else {
                        // if processing the contentType envelopp, we have to put a different
                        if ((email.getProcessContentType() == ProcessContentType.ENVELOPEFROM) ||
                            (email.getProcessContentType() == ProcessContentType.FROM)) {    
                            from = unknownMail;
                        } else {
                            from = unknown;
                        }
                    }
                                       
                    log.logMessage("Tag FROM set to " + from + " since sender-visibility is 0.", Logger.L_DEBUG);
                }
            }
            String result = from;
            if (tag.contains("=ctx:")) {
                result = Template.formatPhoneNumber(result, tag.substring(tag.indexOf("=ctx:") + 5));
            }
            next.appendValue(sb.append(result), email, user, inbox, caller, notifInfo);
        }

        public String toString() {
            return "{from|\"" + unknown + "\"}" + next;
        }
    }


    /**
     * SizeTag appends the size of the latest message.
     * It is possible to configure the format of the size string inserted, by
     * defining the subtemplates FaxSize, VoiceSize and EmailSize. They are
     * strings with the tag SIZE embedded. If a subtemplate is missing,
     * only the size is inserted.
     * <P>Note that the size for fax and voice messages already have a format,
     * created by MVAS, so the FaxSize and VoiceSize should normally be omitted
     * from the language files.</P>
     */
    private final class SizeTag extends TextTagObject {
        private final String sizeTag = "SIZE";
        private final int ixFax = 0;
        private final int ixVoice = 1;
        private final int ixEmail = 2;
        private final int ixVideo = 3;
        private final String[] msgTypes = {"F", "V", "E", "M"};

        //String before __SIZE__ per message type
        private String[] beforeN = new String[ixVideo + 1];
        //String after __SIZE__ per message type
        private String[] afterN = new String[ixVideo + 1];
        //If there is a __SIZE__ per message type
        private boolean[] showN = {false, false, false, false};

        SizeTag(TextTagObject next, Properties templateStrings) {
            super(next);
            String tpl;
            log.logMessage("Making SizeTag ", Logger.L_VERBOSE);
            int tagPos;

            for (int type = ixFax; type <= ixVideo; type++) {
                tpl = getTemplateContentTrimmed(msgTypes[type] + "SIZE_TEXT", templateStrings, sizeTag);
                tagPos = tpl.indexOf(sizeTag);
                if (tagPos >= 0) {
                    showN[type] = true;
                    beforeN[type] = tpl.substring(0, tagPos);
                    afterN[type] = tpl.substring(tagPos + sizeTag.length());
                } else { //The size shall not be included
                    beforeN[type] = tpl;
                    afterN[type] = "";
                }
            }
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            if(email != null) {
                int ix;
                Object size;
                try {
                    switch (email.getDepositType()) {
                        case VOICE:
                            ix = ixVoice;
                            size = email.getMessageLength();
                            break;
                        case FAX:
                            ix = ixFax;
                            size = email.getMessageLength();
                            break;
                        case EMAIL:
                            ix = ixEmail;
                            size = new Integer(email.getMessageSizeInKbytes());
                            break;
                        case VIDEO:
                            ix = ixVideo;
                            size = email.getMessageLength();
                            break;
                        default:
                            log.logMessage("Unknown deposit type: "
                                    + email.getDepositType(), Logger.L_ERROR);
                            ix = ixEmail;
                            size = new Integer(email.getMessageSizeInKbytes());
                            break;
                    }
                    if (showN[ix]) {
                        next.appendValue(sb.append(beforeN[ix]).append(size).
                                append(afterN[ix]), email, user, inbox, caller, notifInfo);
                    } else {
                        next.appendValue(sb.append(beforeN[ix]).append(afterN[ix]),
                                email, user, inbox, caller, notifInfo);
                    }
                } catch (MsgStoreException mse) {
                    // Throw a TemplateMessageGenerationException to keep default behaviour regarding exception handling
                    throw new TemplateMessageGenerationException("SizeTag.appendValue exception" + mse);
                } catch (IOException ioe) {
                    // Throw a TemplateMessageGenerationException to keep default behaviour regarding exception handling
                    throw new TemplateMessageGenerationException("SizeTag.appendValue exception" + ioe);
                } catch (MessagingException me) {
                    // Throw a TemplateMessageGenerationException to keep default behaviour regarding exception handling
                    throw new TemplateMessageGenerationException("SizeTag.appendValue exception" + me);
                }

            } else {
                log.logMessage("SizeTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                next.appendValue(sb, email, user, inbox, caller, notifInfo);
            }
        }

        public String toString() {
            return "{size}" + next;
        }
    }


    /**
     * AttachmentsTag appends the number of attachments
     */
    private final class AttachmentsTag extends TextTagObject {

        AttachmentsTag(TextTagObject next) {
            super(next);
            log.logMessage("Making AttachmentsTag ", Logger.L_VERBOSE);
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            try {
                if(email != null) {
                    sb.append(email.getNoOfAttachments());
                } else {
                    log.logMessage("AttachmentsTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                }
                next.appendValue(sb, email, user, inbox, caller, notifInfo);
            } catch (MsgStoreException mse) {
                // Throw a TemplateMessageGenerationException to keep default behaviour regarding exception handling
                throw new TemplateMessageGenerationException("SizeTag.appendValue exception" + mse);
            }
        }

        public String toString() {
            return "{num_attachments}" + next;
        }
    }


    /**
     * TextTag appends the message text
     */
    private final class TextTag extends TextTagObject {

        TextTag(TextTagObject next) {
            super(next);
            log.logMessage("Making TextTag ", Logger.L_VERBOSE);
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            try {
                if(email != null) {
                    String text = email.getMessageText();
                    if( text != null ) {
                        text = text.trim();
                    }
                    sb.append(text);
                } else {
                    log.logMessage("TextTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);                        
                }
                next.appendValue(sb, email, user, inbox, caller, notifInfo);
            } catch (MsgStoreException mse) {
                // Throw a TemplateMessageGenerationException to keep default behaviour regarding exception handling
                throw new TemplateMessageGenerationException("TextTag.appendValue exception" + mse);
            } 
        }

        public String toString() {
            return "{message text}" + next;
        }
    }


    /**
     * StatusTag appends the message urgency
     */
    private final class StatusTag extends TextTagObject {
        private String normalText;
        private String urgentText;

        StatusTag(TextTagObject next, Properties templateStrings) {
            super(next);
            log.logMessage("Making StatusTag ", Logger.L_VERBOSE);
            normalText = getTemplateContentTrimmed("normal", templateStrings, "#");
            urgentText = getTemplateContentTrimmed("urgent", templateStrings, "#");
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            if(caller != null) {
                log.logMessage("StatusTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
            } else {
                boolean isUrgent = false;
                if(notifInfo != null) {
                    isUrgent = notifInfo.getIsUrgent();
                } else {
                    isUrgent = email.isUrgent();
                }
                if (isUrgent) {
                    sb.append(urgentText);
                } else {
                    sb.append(normalText);
                }
            }
            next.appendValue(sb, email, user, inbox, caller, notifInfo);
        }

        public String toString() {
            return "{\"" + normalText + "\"|\"" + urgentText + "\"}"
                + next;
        }
    }


    /**
     * QuotaTextTag appends a warning about full mailbox, but only when it is
     * full
     */
    private final class QuotaTextTag extends TextTagObject {
        private String quotaText;
        private String  quotahighleveltext;
        private String  voicequotatext;
        private String voicequotahighleveltext;
        private String videoquotatext;
        private String videoquotahighleveltext;
        private String faxquotatext;
        private String faxquotahighleveltext;

        QuotaTextTag(TextTagObject next, Properties templateStrings) {
            super(next);
            log.logMessage("Making QuotaTextTag ", Logger.L_VERBOSE);
            quotaText = getTemplateContentTrimmed("quotatext", templateStrings, "#");
            quotahighleveltext = getTemplateContentTrimmed("quotahighleveltext", templateStrings, "#");
            voicequotatext = getTemplateContentTrimmed("voicequotatext", templateStrings, "#");
            voicequotahighleveltext = getTemplateContentTrimmed("voicequotahighleveltext", templateStrings, "#");
            videoquotatext = getTemplateContentTrimmed("videoquotatext", templateStrings, "#");
            videoquotahighleveltext = getTemplateContentTrimmed("videoquotahighleveltext", templateStrings, "#");
            faxquotatext = getTemplateContentTrimmed("faxquotatext", templateStrings, "#");
            faxquotahighleveltext = getTemplateContentTrimmed("faxquotahighleveltext", templateStrings, "#");

        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
        	if(inbox != null){
        	    if(email != null) {
        	        if(email.getQuotaExceeded())
        	        {
        	            sb.append(quotaText);
        	        }
        	        else if (email.getQuotaAlmostExceeded())
        	        {
        	            sb.append(quotahighleveltext);
        	        }
        	        if(email.getVoiceQuotaExceeded())
        	        {
        	            sb.append(voicequotatext);
        	        }
        	        else if (email.getVoiceQuotaAlmostExceeded())
        	        {
        	            sb.append(voicequotahighleveltext);
        	        }
        	        if(email.getVideoQuotaExceeded())
        	        {
        	            sb.append(videoquotatext);
        	        }
        	        else if (email.getVideoQuotaAlmostExceeded())
        	        {
        	            sb.append(videoquotahighleveltext);
        	        }
        	        if(email.getFaxQuotaExceeded())
        	        {
        	            sb.append(faxquotatext);
        	        }
        	        else if (email.getFaxQuotaAlmostExceeded())
        	        {
        	            sb.append(faxquotahighleveltext);
        	        }
        	    } else {
                    log.logMessage("QuotaTextTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                }
        	} 

            next.appendValue(sb, email, user, inbox, caller, notifInfo);
        }

        public String toString() {
            return "{\"" + quotaText + "\"}" + next;
        }
    }

     /**
     * DateTag appends the message date
     */
    private final class DateTag extends TextTagObject {

        DateTag(TextTagObject next) {
            super(next);
            log.logMessage("Making DateTag ", Logger.L_VERBOSE);
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
        throws TemplateMessageGenerationException {
            String messageTime = "";
            if( caller != null ) {
                messageTime = user.getUsersDate(caller.getCallTime());
            } else if(notifInfo != null) {
                messageTime = user.getUsersDate(notifInfo.getDate());
            } else {
                messageTime = user.getUsersDate(email.getMessageReceivedDate());
            }
            next.appendValue(sb.append(messageTime),email, user, inbox, caller, notifInfo);
        }

        public String toString() {
            return "{date}" + next;
        }
    }


    /**
     * Appends the message date, formatted in a non-standard way.
     */
    private final class FormattedDateTag extends TextTagObject {
        DateFormat fmt;
        private String _am;
        private String _pm;
        private String _format = null;

        FormattedDateTag(TextTagObject next, String format, Properties templateStrings, String language) {
            super(next);
            this._am = getTemplateContentTrimmed("am", templateStrings, "AM"); // fallback added
            this._pm = getTemplateContentTrimmed("pm", templateStrings, "PM");
            this._format = format;
            log.logMessage("Making FormattedDateTag ", Logger.L_VERBOSE);
            Locale loc = new Locale(language, "");
            try {
                fmt = new SimpleDateFormat(format, loc);
                fmt.format(new Date());
            } catch (IllegalArgumentException e) {
                log.logMessage("Bad date format DATE=" + format + ": "
                               + e.getMessage(), Logger.L_ERROR);
                fmt = new SimpleDateFormat("HHmm", loc);
            }
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {

            String messageDate = "";
            if( caller != null ) {
                messageDate = fmt.format(caller.getCallTime());
            } else if(notifInfo != null) {
                messageDate = fmt.format(notifInfo.getDate());
            } else {
                messageDate = fmt.format(email.getMessageReceivedDate());
            }
            if (_format != null && _format.matches(".*a.*"))
                    messageDate = convertAmPmMarker(messageDate,_am, _pm);
            next.appendValue(sb.append(messageDate),email, user, inbox, caller, notifInfo);
        }

        public String toString() {
            return "{date=" + fmt + "}" + next;
        }
    }


    /**
     * TimeTag appends the message time
     */
    private final class TimeTag extends TextTagObject {

        private String _am;
        private String _pm;

        TimeTag(TextTagObject next, Properties templateStrings) {
            super(next);
            this._am = getTemplateContentTrimmed("am", templateStrings, "AM");
            this._pm = getTemplateContentTrimmed("pm", templateStrings, "PM");

            log.logMessage("Making TimeTag ", Logger.L_VERBOSE);
            this.next = next;
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
                throws TemplateMessageGenerationException {
            String messageTime = "";
            if( caller != null ) {
                messageTime = user.getUsersTime(caller.getCallTime());
            } else if(notifInfo != null) {
                messageTime = user.getUsersTime(notifInfo.getDate());
            } else {
                messageTime = user.getUsersTime(email.getMessageReceivedDate());
            }
            String usrPreferredTimeFormat = user.getPreferredTimeFormat();
            if (usrPreferredTimeFormat != null && "12".equals(usrPreferredTimeFormat))
                messageTime = convertAmPmMarker(messageTime, _am, _pm);

            next.appendValue(sb.append(messageTime),email, user, inbox, caller, notifInfo);

        }

        public String toString() {
            return "{time}" + next;
        }
    }



    /**
     * PhoneTag appends the users telephonenumber
     */
    private final class PhoneTag extends TextTagObject {

        PhoneTag(TextTagObject next) {
            super(next);
            log.logMessage("Making PhoneTag ", Logger.L_VERBOSE);
            this.next = next;
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            
            if(caller != null) {
                log.logMessage("PhoneTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
            } else {
                String phone = null;
                if(notifInfo != null) {
                    phone = notifInfo.getReceiverPhoneNumber();
                } else if(email != null) {
                    phone = email.getReceiverPhoneNumber();
                }
                phone = CommonMessagingAccess.getInstance().denormalizeNumber(phone);
                sb.append(phone);
            }
            next.appendValue(sb, email, user, inbox, caller, notifInfo);
        }

        public String toString() {
            return "{phone}" + next;
        }
    }


    /**
     * ConvertedPhoneTag appends the users telephonenumber, but with a
     * configurable conversion applied.
     */
    private final class ConvertedPhoneTag extends TextTagObject {

        private Conversion c;

        ConvertedPhoneTag(TextTagObject next, Properties phr) {
            super(next);
            log.logMessage("Making ConvertedPhoneTag ", Logger.L_VERBOSE);
            c = new Conversion(getTemplateContentTrimmed("converted_phone", phr, ""));
            this.next = next;
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {

            if(caller != null) {
                log.logMessage("ConvertedPhoneTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
            } else {
                String phone = null;
                if(notifInfo != null) {
                    phone = notifInfo.getReceiverPhoneNumber();
                } else if(email != null) {
                    phone = email.getReceiverPhoneNumber();
                }
                phone = CommonMessagingAccess.getInstance().denormalizeNumber(phone);

                int i;
                for (i = 0; i < c.matchPrefix.length; i++) {
                    if (phone.startsWith(c.matchPrefix[i])
                            && phone.endsWith(c.matchSuffix[i])) {
                        phone = c.newPrefix[i]
                                + phone.substring(c.matchPrefix[i].length(), phone.length()
                                        - c.matchSuffix[i].length())
                                        + c.newSuffix[i];
                        break;
                    }
                }
                sb.append(phone);
            }
            next.appendValue(sb, email, user, inbox, caller, notifInfo);
        }

        public String toString() {
            return "{converted phone" + c + "}" + next;
        }
    }


    /**
     * DepositTypeTag appends the message type
     */
    private final class DepositTypeTag extends TextTagObject {
        private Properties templateStrings;
        private String emailText;
        private String voiceText;
        private String faxText;
        private String videoText;

        DepositTypeTag(TextTagObject next, Properties templateStrings) {
            super(next);
            log.logMessage("Making DepositTypeTag ", Logger.L_VERBOSE);
            this.templateStrings = templateStrings;
            emailText = getTemplateContentTrimmed("email", templateStrings, "#");
            voiceText = getTemplateContentTrimmed("voice", templateStrings, "#");
            faxText = getTemplateContentTrimmed("fax", templateStrings, "#");
            videoText = getTemplateContentTrimmed("video", templateStrings, "#");
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            
            if(caller != null) {
                log.logMessage("DepositTypeTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                next.appendValue(sb, email, user, inbox, caller, notifInfo);
            } else if(notifInfo != null) {
                String notifTypeText = getTemplateContentTrimmed(notifInfo.getNotificationType(), templateStrings, notifInfo.getNotificationType());
                next.appendValue(sb.append(notifTypeText), email, user, inbox, caller, notifInfo);
            } else {
                switch (email.getDepositType()) {
                    case SLAMDOWN:
                        if(email.getSlamdownCallType() == NTF_VIDEO) {
                            next.appendValue(sb.append(videoText), email, user, inbox, caller, notifInfo);
                        } else {
                            next.appendValue(sb.append(voiceText), email, user, inbox, caller, notifInfo);
                        }
                        break;
                    case VOICE:
                        next.appendValue(sb.append(voiceText), email, user, inbox, caller, notifInfo);
                        break;
                    case FAX:
                    case FAX_RECEPT_MAIL_TYPE:
                        next.appendValue(sb.append(faxText), email, user, inbox, caller, notifInfo);
                        break;
                    case VIDEO:
                        next.appendValue(sb.append(videoText), email, user, inbox, caller, notifInfo);
                        break;
                    case EMAIL:
                    default:
                        next.appendValue(sb.append(emailText), email, user, inbox, caller, notifInfo);
                        break;
                }
            }
        }

        public String toString() {
            return "{\"" + voiceText + "\"|\"" + faxText + "\"|\""
                + emailText + "\"|\"" + videoText + "\"}" + next;
        }
    }

    
    /**
     * PriorityTag appends the message priority
     */
    private final class PriorityTag extends TextTagObject {
        private String urgent_message;
        private String non_urgent_message;
        
        PriorityTag(TextTagObject next, Properties templateStrings) {
            super(next);
            log.logMessage("Making PriorityTag ", Logger.L_VERBOSE);  
            urgent_message = getTemplateContentTrimmed("urgent_message", templateStrings, "#");
            non_urgent_message = getTemplateContentTrimmed("non_urgent_message", templateStrings, "#");
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            try {
                if(caller != null) {
                    log.logMessage("StatusTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                } else {
                    boolean isUrgent = false;
                    if(notifInfo != null) {
                        isUrgent = notifInfo.getIsUrgent();
                    } else if(email != null){
                        isUrgent = email.isUrgent();
                    }
                    if (isUrgent) {
                        sb.append(urgent_message);
                    } else {
                        sb.append(non_urgent_message);
                    }
                }
                next.appendValue(sb, email, user, inbox, caller, notifInfo);                
            } catch (Exception e) {
                // Do not throw exception (such as TemplateMessageGenerationException).
                next.appendValue(sb.append("-1"), email, user, inbox, caller, notifInfo);
            }
        }

        public String toString() {
            return "{\"" + non_urgent_message + "\"|\"" + urgent_message + "\"}"
                    + next;
        }
    }
    
    
    /**
     * HandleConfidentialTag appends the confidential message handle
     */
    private final class HandleConfidentialTag extends TextTagObject {
        private String handle_confidential_message;
        private String handle_non_confidential_message;
        
        HandleConfidentialTag(TextTagObject next, Properties templateStrings) {
            super(next);
            log.logMessage("Making HandleConfidentialTag ", Logger.L_VERBOSE);  
            handle_confidential_message = getTemplateContentTrimmed("handle_confidential_message", templateStrings, "#");
            handle_non_confidential_message = getTemplateContentTrimmed("handle_non_confidential_message", templateStrings, "#");
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            try {
                if(email != null) {
                    if (email.isConfidential()) {
                        sb.append(handle_confidential_message);
                    } else {
                        sb.append(handle_non_confidential_message);
                    }
                } else {
                    log.logMessage("HandleConfidentialTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                }
                next.appendValue(sb, email, user, inbox, caller, notifInfo);
            } catch (Exception e) {
                // Do not throw exception (such as TemplateMessageGenerationException).
                next.appendValue(sb.append("-1"), email, user, inbox, caller, notifInfo);
            }
        }

        public String toString() {
            return "{\"" + handle_non_confidential_message + "\"|\"" + handle_confidential_message + "\"}"
                    + next;
        }
    }
    

    /**
     * ConfidentialTag appends the message priority
     */
    private final class ConfidentialTag extends TextTagObject {
        private String confidential_message;
        private String non_confidential_message;
        
        ConfidentialTag(TextTagObject next, Properties templateStrings) {
            super(next);
            log.logMessage("Making ConfidentialTag ", Logger.L_VERBOSE);  
            confidential_message = getTemplateContentTrimmed("confidential_message", templateStrings, "#");
            non_confidential_message = getTemplateContentTrimmed("non_confidential_message", templateStrings, "#");
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            try {
                if(email != null) {
                    if (email.isConfidential()) {
                        sb.append(confidential_message);
                    } else {
                        sb.append(non_confidential_message);
                    }
                } else {
                    log.logMessage("StatusTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                }
                next.appendValue(sb, email, user, inbox, caller, notifInfo);
            } catch (Exception e) {
                // Do not throw exception (such as TemplateMessageGenerationException).
                next.appendValue(sb.append("-1"), email, user, inbox, caller, notifInfo);
            }
        }

        public String toString() {
            return "{\"" + non_confidential_message + "\"|\"" + confidential_message + "\"}"
                    + next;
        }
    }
    
    /**
     * ImapAddressInfoTag appends the IMAP server info
     */
    private final class ImapAddressInfoTag extends TextTagObject {
        
        ImapAddressInfoTag(TextTagObject next, Properties templateStrings) {
            super(next);
            log.logMessage("Making ImapAddressInfoTag ", Logger.L_VERBOSE);  
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            try {
                if(caller != null) {
                    log.logMessage("ImapAddressInfoTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                    next.appendValue(sb, email, user, inbox, caller, notifInfo);
                } else {        
                    GeoSystems geoSystems = OEManager.getGeoSystems();
                    String homeSystemID = null;
                    String imapAddress = null;
                    if (user != null){
                        //Get subscriber's home system ID
                        homeSystemID = user.getHomeSystemID();
                    }                  
                    log.logMessage("Template: subscriber " + user.getTelephoneNumber() + ", systemHomeId: " + ( homeSystemID == null ? "doesn't exist":homeSystemID ), Logger.L_VERBOSE);
                    
                    if (homeSystemID != null && !homeSystemID.isEmpty()){
                        imapAddress = geoSystems.getImapServerAddr(homeSystemID);
                    }
                    if (imapAddress == null || imapAddress.isEmpty()) {
                        //Try getting imap info from NTF config file
                        imapAddress = Config.getAppleImapServerAddress();
                        if (imapAddress == null || imapAddress.isEmpty()){
                            // No value found in ntf config, do not append ImapAddress and port
                            log.logMessage("Template: No valid ImapAddress and port found in NTF, discard IMAPADDRESSINFO tag", Logger.L_VERBOSE);
                        }else{
                            //Found imap value in NTF. Append.
                            log.logMessage("Template: NTF ImapAddress is " + imapAddress , Logger.L_VERBOSE);
                            sb.append(imapAddress);
                        }
                    } else {
                        // append imap server and port
                        log.logMessage("Template: GEO ImapAddress is " + imapAddress , Logger.L_VERBOSE);
                        sb.append(imapAddress);
                    }
                }
                next.appendValue(sb, email, user, inbox, caller, notifInfo);                
            } catch (Exception e) {
                // Do not throw exception (such as TemplateMessageGenerationException).
                next.appendValue(sb.append("-1"), email, user, inbox, caller, notifInfo);
            }
        }

        public String toString() {
            return "{imapadressinfo}" + next;
        }
    }
    
    
    /**
     * ImapPortInfoTag appends the IMAP port
     */
    private final class ImapPortInfoTag extends TextTagObject {
        
        ImapPortInfoTag(TextTagObject next, Properties templateStrings) {
            super(next);
            log.logMessage("Making ImapPortInfoTag ", Logger.L_VERBOSE);  
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            try {
                if(caller != null) {
                    log.logMessage("ImapPortInfoTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                    next.appendValue(sb, email, user, inbox, caller, notifInfo);
                } else {        
                    GeoSystems geoSystems = OEManager.getGeoSystems();
                    String homeSystemID = null;
                    String imapPort = null;
                    if (user != null){
                        //Get subscriber's home system ID
                        homeSystemID = user.getHomeSystemID();
                    }                  
                    log.logMessage("Template: subscriber " + user.getTelephoneNumber() + ", systemHomeId: " + ( homeSystemID == null ? "doesn't exist":homeSystemID ), Logger.L_VERBOSE);
                    
                    if (homeSystemID != null && !homeSystemID.isEmpty()){
                        imapPort = geoSystems.getImapServerPort(homeSystemID);                       
                    }
                    if (imapPort == null || imapPort.isEmpty()) {
                        //Try getting imap info from NTF config file
                        imapPort = Config.getAppleImapServerPort();
                        if (imapPort == null || imapPort.isEmpty()){
                            // No value found in ntf config, do not append imap port
                            log.logMessage("Template: No valid imap port found in NTF, discard IMAPPORTINFO tag", Logger.L_VERBOSE);
                        }else{
                            //Found imap value in NTF. Append.
                            log.logMessage("Template: NTF imapPort is " + imapPort , Logger.L_VERBOSE);
                            sb.append(imapPort);
                        }
                    } else {
                        // append imap port
                        log.logMessage("Template: GEO imapPort is " + imapPort , Logger.L_VERBOSE);
                        sb.append(imapPort);
                    }
                }
                next.appendValue(sb, email, user, inbox, caller, notifInfo);                
            } catch (Exception e) {
                // Do not throw exception (such as TemplateMessageGenerationException).
                next.appendValue(sb.append("-1"), email, user, inbox, caller, notifInfo);
            }
        }

        public String toString() {
            return "{imapportinfo}" + next;
        }
    }

    
    /**
     * ImapUserInfoTag appends the IMAP user info
     */
    private final class ImapUserInfoTag extends TextTagObject {      
        ImapUserInfoTag(TextTagObject next, Properties templateStrings) {
            super(next);
            log.logMessage("Making ImapUserInfoTag ", Logger.L_VERBOSE);  
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            try {
                if(caller != null) {
                    log.logMessage("ImapUserInfoTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                    next.appendValue(sb, email, user, inbox, caller, notifInfo);
                } else {
                    //get user info
                    String userInfo=null;
                    if(user!=null){
                        userInfo = CommonMessagingAccess.getInstance().denormalizeNumber(user.getTelephoneNumber());
                    }
                    if(userInfo!=null && !userInfo.isEmpty()){
                        log.logMessage("Template: imapUserInfo is " + userInfo , Logger.L_VERBOSE);
                        sb.append(userInfo);
                    }
                }
                next.appendValue(sb, email, user, inbox, caller, notifInfo);                
            } catch (Exception e) {
                // Do not throw exception (such as TemplateMessageGenerationException).
                next.appendValue(sb.append("-1"), email, user, inbox, caller, notifInfo);
            }
        }

        public String toString() {
            return "{imapuserinfo}" + next;
        }
    }
    
    
    /**
     * ImapPasswordInfoTag appends the IMAP password
     */
    private final class ImapPasswordInfoTag extends TextTagObject {
        ImapPasswordInfoTag(TextTagObject next, Properties templateStrings) {
            super(next);
            log.logMessage("Making ImapPasswordInfoTag ", Logger.L_VERBOSE);  
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            try {
                if(caller != null) {
                    log.logMessage("ImapPasswordInfoTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                    next.appendValue(sb, email, user, inbox, caller, notifInfo);
                } else {
                    String imapPassword = null;
                    if (user != null){
                        //Get vvm client password
                        imapPassword = user.getImapPassword();
                    }
                    if( imapPassword != null){
                        log.logMessage("Template: imapPassword is " + imapPassword , Logger.L_VERBOSE);
                        sb.append(imapPassword);
                    }
                    log.logMessage("Template: subscriber " + user.getTelephoneNumber() + ", imapPassword: " + ( imapPassword == null ? "doesn't exist":imapPassword ), Logger.L_VERBOSE);
                    
                }
                next.appendValue(sb, email, user, inbox, caller, notifInfo);                
            } catch (Exception e) {
                // Do not throw exception (such as TemplateMessageGenerationException).
                next.appendValue(sb.append("-1"), email, user, inbox, caller, notifInfo);
            }
        }

        public String toString() {
            return "{imappasswordinfo}" + next;
        }
    }
    
    
    
    
    /**
     * NumNewTotTag appends the total new message count
     */
    private final class NumNewTotTag extends TextTagObject {
        
        private final String token;

        NumNewTotTag(TextTagObject next, String token) {
            super(next);
            this.token = token;
            log.logMessage("Making NumNewTotTag ", Logger.L_VERBOSE);
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            if (inbox != null) {
                next.appendValue(sb.append(formatDecimalPlaces(token, "" + inbox.getNewTotalCount())),
                                 email, user, inbox, caller, notifInfo);
            } else {
                next.appendValue(sb.append("-1"),
                                 email, user, inbox, caller, notifInfo);
            }
        }

        public String toString() {
            return "{tot_count}" + next;
        }
    }

    /**
     * NumNewUrgVoiceTag appends the new urgent voice message count
     */
    private final class NumNewUrgVoiceTag extends TextTagObject {

        NumNewUrgVoiceTag(TextTagObject next) {
            super(next);
            log.logMessage("Making NumNewUrgVoiceTag ", Logger.L_VERBOSE);
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            try {
                if (inbox != null) {
                    next.appendValue(sb.append(inbox.getNewUrgentVoiceCount()), email, user, inbox, caller, notifInfo);
                } else {
                    next.appendValue(sb.append("-1"), email, user, inbox, caller, notifInfo);
                }
            } catch (Exception e) {
                // Do not throw exception (such as TemplateMessageGenerationException).
                next.appendValue(sb.append("-1"), email, user, inbox, caller, notifInfo);
            }
        }

        public String toString() {
            return "{urg_voice_count}" + next;
        }
    }
    
    /**
     * NumNewConfVoiceTag appends the new confidential voice message count
     */
    private final class NumNewConfVoiceTag extends TextTagObject {

        NumNewConfVoiceTag(TextTagObject next) {
            super(next);
            log.logMessage("Making NumNewConfVoiceTag ", Logger.L_VERBOSE);
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            try {
                if (inbox != null) {
                    next.appendValue(sb.append(inbox.getNewConfidentialVoiceCount()), email, user, inbox, caller, notifInfo);
                } else {
                    next.appendValue(sb.append("-1"), email, user, inbox, caller, notifInfo);
                }
            } catch (Exception e) {
                // Do not throw exception (such as TemplateMessageGenerationException).
                next.appendValue(sb.append("-1"), email, user, inbox, caller, notifInfo);
            }
        }

        public String toString() {
            return "{conf_voice_count}" + next;
        }
    }
    
    /**
     * NumNewVoiceTag appends the new voice message count
     */
    private final class NumNewVoiceTag extends TextTagObject {
        
        private final String token;

        NumNewVoiceTag(TextTagObject next, String token) {
            super(next);
            this.token = token;
            log.logMessage("Making NumNewVoiceTag ", Logger.L_VERBOSE);
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            try {
                if (inbox != null) {
                    next.appendValue(sb.append(formatDecimalPlaces(token, "" + inbox.getNewVoiceCount())), email, user, inbox, caller, notifInfo);
                } else {
                    next.appendValue(sb.append("-1"), email, user, inbox, caller, notifInfo);
                }
            } catch (Exception e) {
                // Do not throw exception (such as TemplateMessageGenerationException).
                next.appendValue(sb.append("-1"), email, user, inbox, caller, notifInfo);
            }
        }

        public String toString() {
            return "{voice_count}" + next;
        }
    }
    
    
    /**
     * NumAllVoiceTag appends the all voice message count
     */
    private final class NumAllVoiceTag extends TextTagObject {
        
        private final String token;

        NumAllVoiceTag(TextTagObject next, String token) {
            super(next);
            this.token = token;
            log.logMessage("Making NumAllVoiceTag ", Logger.L_VERBOSE);
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            try {
                if (inbox != null) {
                    next.appendValue(sb.append(formatDecimalPlaces(token, "" + inbox.getVoiceTotalCount())), email, user, inbox, caller, notifInfo);
                } else {
                    next.appendValue(sb.append("-1"), email, user, inbox, caller, notifInfo);
                }
            } catch (Exception e) {
                // Do not throw exception (such as TemplateMessageGenerationException).
                next.appendValue(sb.append("-1"), email, user, inbox, caller, notifInfo);
            }
        }

        public String toString() {
            return "{voice_count}" + next;
        }
    }


    /**
     * NumNewFaxTag appends the new fax message count
     */
    private final class NumNewFaxTag extends TextTagObject {
        
        private final String token;

        NumNewFaxTag(TextTagObject next, String token) {
            super(next);
            this.token = token;
            log.logMessage("Making NumNewFaxTag ", Logger.L_VERBOSE);
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            if (inbox != null) {
                next.appendValue(sb.append(formatDecimalPlaces(token, "" + inbox.getNewFaxCount())), email, user, inbox, caller, notifInfo);
            } else {
                next.appendValue(sb.append("-1"),
                                 email, user, inbox, caller, notifInfo);
            }
        }

        public String toString() {
            return "{fax_count}" + next;
        }
    }


    /**
     * NumNewEmailTag appends the new email message count
     */
    private final class NumNewEmailTag extends TextTagObject {
        
        private final String token;

        NumNewEmailTag(TextTagObject next, String token) {
            super(next);
            this.token = token;
            log.logMessage("Making NumNewEmailTag ", Logger.L_VERBOSE);
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            if (inbox != null) {
                next.appendValue(sb.append(formatDecimalPlaces(token, "" + (inbox.getNewEmailCount()))),
                                 email, user, inbox, caller, notifInfo);
            } else {
                next.appendValue(sb.append("-1"), email, user, inbox, caller, notifInfo);
            }
        }

        public String toString() {
            return "{email_count}" + next;
        }
    }

    /**
     * NumNewUrgVideoTag appends the new urgent video message count
     */
    private final class NumNewUrgVideoTag extends TextTagObject {

        NumNewUrgVideoTag(TextTagObject next) {
            super(next);
            log.logMessage("Making NumNewUrgVideoTag ", Logger.L_VERBOSE);
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            next.appendValue(sb.append(inbox == null ? -1 : inbox.getNewUrgentVideoCount()),
                             email, user, inbox, caller, notifInfo);
        }

        public String toString() {
            return "{urg_video_count}" + next;
        }
    }
    
    /**
     * NumNewConfVideoTag appends the new confidential video message count
     */
    private final class NumNewConfVideoTag extends TextTagObject {

        NumNewConfVideoTag(TextTagObject next) {
            super(next);
            log.logMessage("Making NumNewConfVideoTag ", Logger.L_VERBOSE);
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            next.appendValue(sb.append(inbox == null ? -1 : inbox.getNewConfidentialVideoCount()),
                             email, user, inbox, caller, notifInfo);
        }

        public String toString() {
            return "{conf_video_count}" + next;
        }
    }
    
    /**
     * NumNewVideoTag appends the new video message count
     */
    private final class NumNewVideoTag extends TextTagObject {
        
        private final String token;

        NumNewVideoTag(TextTagObject next, String token) {
            super(next);
            this.token = token;
            log.logMessage("Making NumNewVideoTag ", Logger.L_VERBOSE);
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            next.appendValue(sb.append(formatDecimalPlaces(token, "" + (inbox == null ? -1 : inbox.getNewVideoCount()))),
                             email, user, inbox, caller, notifInfo);
        }

        public String toString() {
            return "{video_count}" + next;
        }
    }

    /**
         * CountTag appends the call count
         */
        private final class CountTag extends TextTagObject {
            
            private final String token;

            CountTag(TextTagObject next, String token) {
                super(next);
                this.token = token;
                Logger.getLogger().logMessage("Making CountTag ", Logger.L_VERBOSE);
            }

            public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
                throws TemplateMessageGenerationException {
                if( caller != null ) {
                    int c = caller.getVoiceCount();
                    int maxCallsPerCaller = 0;
                    
                    if(!caller.getIsSendingAsGenericNotif()) {
                        if (user.getCosName().equalsIgnoreCase(Constants.DUMMY_MCN_COS)) {
                            // Mcn case
                            maxCallsPerCaller = Config.getMcnMaxCallsPerCaller();
                        } else {
                            // Slamdown case
                            maxCallsPerCaller = Config.getSlamdownMaxCallsPerCaller();
                        }
                    }
                    if (maxCallsPerCaller > 0) {
                        c = Math.min(c, maxCallsPerCaller);
                    }
                    sb.append(formatDecimalPlaces(token, ""+c));
                } else {
                    log.logMessage("CountTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                }
                next.appendValue(sb, email, user, inbox, caller, notifInfo);
            }

            public String toString() {
                return "{count}" + next;
            }
        }

    /**
     * CondTag appends a condition for
     */
    private final class CondTag extends TextTagObject {
        /*voice condition*/
        private boolean[] _vcond = new boolean[11];
        /*urgent voice condition*/
        private boolean[] _uvcond = new boolean[11];
        /*confidential voice condition*/
        private boolean[] _cvcond = new boolean[11];
        /*fax condition*/
        private boolean[] _fcond = new boolean[11];
        /*email condition*/
        private boolean[] _econd = new boolean[11];
        /*video condition*/
        private boolean[] _mcond = new boolean[11];
        /*urgent video condition*/
        private boolean[] _umcond = new boolean[11];
        /*confidential video condition*/
        private boolean[] _cmcond = new boolean[11];
        /* Total condition*/
        private boolean[] _tcond = new boolean[11];

        private boolean totalCount = false;

        CondTag(TextTagObject next, String conditions, boolean totalCount) {
            super(next);
            this.totalCount = totalCount;
            readConditions(conditions);
            log.logMessage("Making CondTag ", Logger.L_VERBOSE);
        }

        public void appendValue(StringBuffer sb,
                                NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            if(inbox != null) {
                if ( !totalCount &&
                        ((inbox.getNewVoiceCount() == MSG_COUNT_ERR) ||
                                (inbox.getNewFaxCount() == MSG_COUNT_ERR) ||
                                (inbox.getNewEmailCount() == MSG_COUNT_ERR) ||
                                (inbox.getNewVideoCount() == MSG_COUNT_ERR) ||
                                (inbox.getNewUrgentVoiceCount() == MSG_COUNT_ERR) ||
                                (inbox.getNewUrgentVideoCount() == MSG_COUNT_ERR) ||
                                (inbox.getNewConfidentialVoiceCount() == MSG_COUNT_ERR) ||
                                (inbox.getNewConfidentialVideoCount() == MSG_COUNT_ERR)) ) {
                    throw(new TemplateMessageGenerationException("Failed to produce proper text"));
                } 
                if( totalCount && inbox.getNewTotalCount() == MSG_COUNT_ERR ) {
                    throw(new TemplateMessageGenerationException("Failed to produce proper text"));
                }
                if( totalCount ) {
                    if( _tcond[ Math.min(inbox.getNewTotalCount(), 10)] ) {
                        next.appendValue(sb,email, user, inbox, caller, notifInfo);
                    }  else {
                        next.next.appendValue(sb,email,user,inbox,caller, notifInfo);
                    }
                } else {
                    if(_vcond[Math.min(inbox.getNewVoiceCount(),10)] &&
                            _uvcond[Math.min(inbox.getNewUrgentVoiceCount(),10)] &&
                            _cvcond[Math.min(inbox.getNewConfidentialVoiceCount(),10)] &&
                            _fcond[Math.min(inbox.getNewFaxCount(),10)] &&
                            _econd[Math.min(inbox.getNewEmailCount(),10)] &&
                            _mcond[Math.min(inbox.getNewVideoCount(),10)] &&
                            _umcond[Math.min(inbox.getNewUrgentVideoCount(),10)] &&
                            _cmcond[Math.min(inbox.getNewConfidentialVideoCount(),10)])
                        next.appendValue(sb,email, user, inbox, caller, notifInfo);
                    else
                        next.next.appendValue(sb,email,user,inbox,caller, notifInfo);
                }
            } else {
                log.logMessage("CondTag not supported since subscriber inbox is null; skipping this tag.", Logger.L_VERBOSE);
            }
        }

        private void readConditions(String conditions) {
            setCondition(null,"default");
            if (conditions == null)
                return;
            if (conditions.indexOf("(") != -1 &&
                conditions.indexOf(")") != -1){
                conditions = conditions.substring(conditions.indexOf("(")+1,
                                                  conditions.indexOf(")"));
            }
            if( totalCount == false ) { //condition with voice, fax, email and video
                StringTokenizer strtoken = new StringTokenizer(conditions,",");
                int size = strtoken.countTokens();
                if (size==4) {
                    setCondition(strtoken.nextToken(), "v") /*for voice*/;
                    setCondition("*"                 , "uv")/*for urgent voice*/;
                    setCondition("*"                 , "cv")/*for confidential voice*/;
                    setCondition(strtoken.nextToken(), "f") /*for fax*/;
                    setCondition(strtoken.nextToken(), "e") /*for email*/;
                    setCondition(strtoken.nextToken(), "m") /*for video*/;
                    setCondition("*"                 , "um") /*for urgent video*/;
                    setCondition("*"                 , "cm")/*for confidential video*/;
                } else if (size==6) {
                    setCondition(strtoken.nextToken(), "v") /*for voice*/;
                    setCondition(strtoken.nextToken(), "uv") /*for urgent voice*/;
                    setCondition("*"                 , "cv")/*for confidential voice*/;
                    setCondition(strtoken.nextToken(), "f") /*for fax*/;
                    setCondition(strtoken.nextToken(), "e") /*for email*/;
                    setCondition(strtoken.nextToken(), "m") /*for video*/;
                    setCondition(strtoken.nextToken(), "um") /*for urgent video*/;
                    setCondition("*"                 , "cm")/*for confidential video*/;
                } else if (size==8) {
                    setCondition(strtoken.nextToken(), "v") /*for voice*/;
                    setCondition(strtoken.nextToken(), "uv") /*for urgent voice*/;
                    setCondition(strtoken.nextToken(), "cv") /*for confidential voice*/;
                    setCondition(strtoken.nextToken(), "f") /*for fax*/;
                    setCondition(strtoken.nextToken(), "e") /*for email*/;
                    setCondition(strtoken.nextToken(), "m") /*for video*/;
                    setCondition(strtoken.nextToken(), "um") /*for urgent video*/;
                    setCondition(strtoken.nextToken(), "cm") /*for confidential video*/;
                }
                else
                    return;

            } else { // condition with totalcount only
                setCondition(conditions, "t");
            }
        }

        private void setCondition(String condition, String type) {
            log.logMessage("Conditions in CondTag (" +
                 condition + ")", Logger.L_VERBOSE);
            if(type.equalsIgnoreCase("default")) {
                for(int ix=0;ix<11;ix++){
                    _vcond[ix]=false;
                    _uvcond[ix]=false;
                    _cvcond[ix]=false;
                    _fcond[ix]=false;
                    _econd[ix]=false;
                    _mcond[ix]=false;
                    _umcond[ix]=false;
                    _cmcond[ix]=false;
                    _tcond[ix]=false;
                }
                 log.logMessage("Setting default conditions in CondTag ", Logger.L_VERBOSE);
            }
            else if(type.equalsIgnoreCase("v")) {
                for(int ix=0;ix<11;ix++){
                    _vcond[ix]=getBooleanValue(condition, ix);
                }
                 log.logMessage("Setting voice conditions", Logger.L_VERBOSE);
            }
            else if(type.equalsIgnoreCase("uv")) {
                for(int ix=0;ix<11;ix++){
                    _uvcond[ix]=getBooleanValue(condition, ix);
                }
                 log.logMessage("Setting urgent voice conditions", Logger.L_VERBOSE);
            }
            else if(type.equalsIgnoreCase("cv")) {
                for(int ix=0;ix<11;ix++){
                    _cvcond[ix]=getBooleanValue(condition, ix);
                }
                 log.logMessage("Setting confidential voice conditions", Logger.L_VERBOSE);
            }
            else if(type.equalsIgnoreCase("f")) {
                for(int ix=0;ix<11;ix++){
                    _fcond[ix]=getBooleanValue(condition, ix);
                }
                log.logMessage("Setting fax conditions", Logger.L_VERBOSE);
            }
            else if(type.equalsIgnoreCase("e")) {
                for(int ix=0;ix<11;ix++){
                    _econd[ix]=getBooleanValue(condition, ix);
                }
                log.logMessage("Setting email conditions", Logger.L_VERBOSE);
            }
            else if(type.equalsIgnoreCase("m")) {
                for(int ix=0;ix<11;ix++){
                    _mcond[ix]=getBooleanValue(condition, ix);
                }
                log.logMessage("Setting video conditions", Logger.L_VERBOSE);
            }
            else if(type.equalsIgnoreCase("um")) {
                for(int ix=0;ix<11;ix++){
                    _umcond[ix]=getBooleanValue(condition, ix);
                }
                log.logMessage("Setting urgent video conditions", Logger.L_VERBOSE);
            }
            else if(type.equalsIgnoreCase("cm")) {
                for(int ix=0;ix<11;ix++){
                    _cmcond[ix]=getBooleanValue(condition, ix);
                }
                log.logMessage("Setting confidential video conditions", Logger.L_VERBOSE);
            }
            else if(type.equalsIgnoreCase("t")) {
                for(int ix=0;ix<11;ix++){
                    _tcond[ix]=getBooleanValue(condition, ix);
                }
                log.logMessage("Setting video conditions", Logger.L_VERBOSE);
            }
        }

        private boolean getBooleanValue(String condition, int number) {
            if(condition.length()==1) {
                if(condition.matches("\\*"))
                    return true;
                else if (condition.matches("\\d")){
                    char ch = condition.charAt(0);
                    int ival = Character.digit(ch,10);
                    return (ival==number) ? true : false;
                }
            }
            else if(condition.length() == 2) {
                if(condition.matches("-[0-9]")) {
                    char ch = condition.charAt(1);
                    int ival = Character.digit(ch,10);
                    return (number<=ival) ? true : false;
                }
                else if(condition.matches("[0-9]-")) {
                    char ch = condition.charAt(0);
                    int ival = Character.digit(ch,10);
                    return (number>=ival) ? true : false;
                }
            }
            else if(condition.length() == 3) {
                if(condition.matches("[0-9]-[0-9]")) {
                    char ch = condition.charAt(0);
                    int ivalFrom = Character.digit(ch,10);
                    ch = condition.charAt(2);
                    int ivalTo = Character.digit(ch,10);
                    return (number>=ivalFrom && number<=ivalTo) ? true : false;
                }
            }
            return false;
        }

        public String toString() {
            return "{condition}" + next;
        }
    }

    /**
     * VvmIdTag Tag retrieves the ID field as respecting the OMTP and IMAP standards.
     * @TODO - The id will need to be changed for the correct one once system decides what that is.
     */
    private final class UidTag extends TextTagObject {

    	UidTag(TextTagObject next, Properties templateStrings) {
            super(next);
            log.logMessage("Making VvmIdTag ", Logger.L_VERBOSE);
        }

        public void appendValue(StringBuffer sb, NotificationEmail email, UserInfo user, UserMailbox inbox, CallerInfo caller, ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            if (email != null) {
            	sb.append(email.getUID());
            } else {
                log.logMessage("UidTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
            }
            next.appendValue(sb, email, user, inbox, caller, notifInfo);
        }

        public String toString() {
            return "{uid}" + next;
        }
    }

    /**
     * VvmIdTag Tag retrieves the ID field as respecting the OMTP and IMAP standards.
     * @TODO - The id will need to be changed for the correct one once system decides what that is.
     */
    private final class VvmPrefixTag extends TextTagObject {

    	VvmPrefixTag(TextTagObject next, Properties templateStrings) {
            super(next);
            log.logMessage("Making VvmPrefixTag ", Logger.L_VERBOSE);
        }

        public void appendValue(StringBuffer sb, NotificationEmail email, UserInfo user, UserMailbox inbox, CallerInfo caller, ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            next.appendValue(sb.append(user.getVvmClientPrefix()), email, user, inbox, caller, notifInfo);
        }

        public String toString() {
            return "{vvm prefix}" + next;
        }
    }

    /**
     * VvaSmsTag Tag will search the properties list which accompanies the email to see if a property has been defined
     */
    private final class VvaSmsTag extends TextTagObject {
    	private String tagname = null;

    	VvaSmsTag(TextTagObject next, Properties templateStrings, String tagString) {
            super(next);

            tagname = tagString.substring(VVATAG.length(), tagString.length() ).replaceAll("_"," ");
            log.logMessage("Making VvaSmsTag [" + tagString + "] becomes " + tagname, Logger.L_VERBOSE);
        }

        public void appendValue(StringBuffer sb, NotificationEmail email, UserInfo user, UserMailbox inbox, CallerInfo caller, ANotifierNotificationInfo notifInfo)
                throws TemplateMessageGenerationException {
            if(caller != null) {
                //log.logMessage("VvaSmsTag (TAG) not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                String value = null;
                value = caller.getSlamdownInfoProperty(tagname);
                log.logMessage("VvaSmsTag.appendValue(): caller.getSlamdownInfoProperty for tagname: " + tagname + " returned: " + value, Logger.L_VERBOSE);
                sb.append(value);
            } else {
                String value = null;
                if(notifInfo != null) {
                    value = notifInfo.getProperty(tagname);
                } else if(email != null) {
                    value = email.getNtfEvent().getProperty(tagname);
                    if(value == null){
                        value = email.getAdditionalProperty(tagname);
                    }
                }
                log.logMessage("VvaSmsTag.appendValue(): Retrieving event property: " + tagname + "=" + value, Logger.L_VERBOSE);
                sb.append(value);
            }
            next.appendValue(sb, email, user, inbox, caller, notifInfo);
        }

        public String toString() {
            return "" + next;
        }

    }
    
    /**
     * CTag appends the C tag content to the rest of the message.
     */
    private final class CTag extends TextTagObject
    {
        private String tag;
        private Properties prop;
        
        CTag(TextTagObject next, String tag, Properties prop)
        {
            super(next);
            this.tag = tag;
            this.prop = prop;
            log.logMessage("Making CTag", Logger.L_VERBOSE);
        }

        @Override
        public void appendValue(StringBuffer sb, NotificationEmail email, UserInfo user, UserMailbox inbox, CallerInfo caller, ANotifierNotificationInfo notifInfo)
                throws TemplateMessageGenerationException {
                next.appendValue(sb.append(getTemplateContentTrimmed(tag, prop, "#")), email, user, inbox, caller, notifInfo);
        }
        
        public String toString() { return "{CTag=" + tag + "}" + next; }
        
    }

}
