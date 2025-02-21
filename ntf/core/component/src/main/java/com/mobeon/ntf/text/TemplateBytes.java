package com.mobeon.ntf.text;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.Vector;

import jakarta.mail.MessagingException;

import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.oe.common.geosystems.GeoSystems;
import com.abcxyz.messaging.oe.lib.OEManager;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.NotifierMfsException;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.mfs.NotifierMfsException.NotifierMfsExceptionCause;
import com.abcxyz.messaging.vvs.ntf.notifier.plugin.framework.send.ANotifierNotificationInfo;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.slamdown.CallerInfo;
import com.mobeon.ntf.text.TemplateMessageGenerationException.TemplateExceptionCause;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.util.Logger;

/**
 * A class which gives the same functionality as Template, but returning byte
 * arrays instead of Strings. The reason for this is that now, certain template
 * files (*.cphr) will no longer have ASCII-English characters, but might now
 * contain foreign or arbitrary character sets.
 */
public class TemplateBytes implements Constants {
    
    /** Logger */
    private static Logger log = Logger.getLogger();
    
    /** Byte arrays representing all tags to look for, not including special tags (such as CondTags and
     * Const tags). */
    private static final String TAG_SUBJECT = "__SUBJECT",
            TAG_FROM = "__FROM",
            TAG_FROM_EQ = "__FROM=ctx:",
            TAG_SIZE = "__SIZE",
            TAG_APPLEIMAPADDRESSINFO = "__APPLEIMAPADDRESSINFO",
            TAG_APPLEIMAPPORTINFO = "__APPLEIMAPPORTINFO",
            TAG_IMAPUSERINFO = "__IMAPUSERINFO",
            TAG_IMAPPASSWORDINFO = "__IMAPPASSWORDINFO",
            TAG_CONVERTED_PHONE = "__CONVERTED_PHONE",
            TAG_PHONE = "__PHONE",
            TAG_NUM_ATTACHMENTS = "__NUM_ATTACHMENTS",
            TAG_EMAIL_TEXT = "__EMAIL_TEXT",
            TAG_STATUS = "__STATUS",
            TAG_TCOUNT = "__TCOUNT",
            TAG_TCOUNT_EQ = "__TCOUNT=",
            TAG_VCOUNT = "__VCOUNT",
            TAG_VCOUNT_EQ = "__VCOUNT=",
            TAG_UVCOUNT = "__UVCOUNT",
            TAG_CVCOUNT = "__CVCOUNT",
            //TAG_VNCOUNT = "__VNCOUNT",
            //TAG_VSCOUNT = "__VSCOUNT",
            //TAG_VRCOUNT = "__VRCOUNT",
            TAG_VACOUNT = "__VACOUNT",
            TAG_VACOUNT_EQ = "__VACOUNT=",
            TAG_FCOUNT = "__FCOUNT",
            TAG_FCOUNT_EQ = "__FCOUNT=",
            //TAG_FNCOUNT = "__FNCOUNT",
            //TAG_FSCOUNT = "__FSCOUNT",
            //TAG_FRCOUNT = "__FRCOUNT",
            //TAG_FACOUNT = "__FACOUNT",
            TAG_ECOUNT = "__ECOUNT",
            TAG_ECOUNT_EQ = "__ECOUNT=",
            //TAG_ENCOUNT = "__ENCOUNT",
            //TAG_ESCOUNT = "__ESCOUNT",
            //TAG_ERCOUNT = "__ERCOUNT",
            //TAG_EACOUNT = "__EACOUNT",
            TAG_MCOUNT = "__MCOUNT",
            TAG_MCOUNT_EQ = "__MCOUNT=",
            TAG_UMCOUNT = "__UMCOUNT",
            TAG_CMCOUNT = "__CMCOUNT",
            //TAG_MNCOUNT = "__MNCOUNT",
            //TAG_MSCOUNT = "__MSCOUNT",
            //TAG_MRCOUNT = "__MRCOUNT",
            //TAG_MACOUNT = "__MACOUNT",
            //TAG_ANCOUNT = "__ANCOUNT",
            //TAG_ASCOUNT = "__ASCOUNT",
            //TAG_ARCOUNT = "__ARCOUNT",
            //TAG_AACOUNT = "__AACOUNT",
            TAG_QUOTA_TEXT = "__QUOTA_TEXT",
            TAG_TIME = "__TIME",
            TAG_TYPE = "__TYPE",
            TAG_PRIORITY = "__PRIORITY",
            TAG_CONFIDENTIAL = "__CONFIDENTIAL",
            TAG_HANDLE_CONFIDENTIAL = "__HANDLE_CONFIDENTIAL",
            TAG_DATE_EQ = "__DATE=",
            TAG_DATE = "__DATE",
            TAG_COUNT = "__COUNT",
            TAG_COUNT_EQ = "__COUNT=",
            TAG_UID = "__UID",
            TAG_VVM_PREFIX = "__VVM_PREFIX",
            TAG_VVA_TAG = "__TAG=",
            TAG_C_TAG = "__CTAG_", // "CTAG" stands 
            TAG_QUOTE = "__QUOTE",
            TAG_COND = "(*,*,*,*)",
            TAG_COND_2 = "(*,*,*,*,*,*)",
            TAG_COND_3 = "(*)",
            TAG_COND_4 = "(*,*,*,*,*,*,*,*)",
            TAG_UNICODE_NEWLINE = "\\u000a",
            TAG_PAYLOAD = "__PAYLOAD",
            TAG_HEX_EQ = "__HEX=";

    /** A tag which has no next tag (chain end). */
    private final ByteTagObject END_TAG = new EndTag();
    
    /** The content. */
    public final String content;
    
    /** The language, cosName, brand and charsetname of this Template. */
    public final String lang, cosName, charsetname, brand;
    
    /** Phrases specific to this template. */
    private Hashtable<String, byte[]> phrases;
    
    /** The prefix to all phrases for this template. */
    private String phrasePrefix;
    
    /** The chain of tags to parse. */
    private ByteTagObject chain = END_TAG;
    
    /** Empty byte array. */
    public static final byte[] EMPTY = {};
    
    /**
     * Default constructor.
     * @param content The content of the bytes to parse (String).
     * @param brand The brand of the subscriber.
     * @param lang The language of the message.
     * @param cosName The name of the COS.
     * @param charsetname The name of the charset.
     */
    public TemplateBytes(String content, String brand, String lang, String cosName, String charsetname)
    {
        this.content = content;
        this.lang = lang;
        this.cosName = cosName;
        this.charsetname = charsetname;
        this.brand = brand;
        phrases = null;
        
        if (brand != null)
        {
            phrases = Phrases.getBPhrases(brand + "_" + lang, cosName, charsetname);
            phrasePrefix = Phrases.getBytePrefix(brand + "_" + lang, cosName, charsetname);
            
            if (phrases == null || phrases.isEmpty()) // No phrases found for brand + lang + cos + charset, trying just brand + lang + charset
            {
                phrases = Phrases.getBPhrases(brand + "_" + lang, null, charsetname);
                phrasePrefix = Phrases.getBytePrefix(brand + "_" + lang, null, charsetname);
            }
            
            if (phrases == null || phrases.isEmpty()) // No phrases found for brand + lang + charset, trying just lang + charset
            {
                phrases = Phrases.getBPhrases(lang, null, charsetname);
                phrasePrefix = Phrases.getBytePrefix(lang, null, charsetname);
            }
        }
        else
        {
            phrases = Phrases.getBPhrases(lang, cosName, charsetname);
            phrasePrefix = Phrases.getBytePrefix(lang, cosName, charsetname);
            
            if (phrases == null || phrases.isEmpty()) // No phrases found for lang + cos + charset, trying just lang + charset
            {
                phrases = Phrases.getBPhrases(lang, null, charsetname);
                phrasePrefix = Phrases.getBytePrefix(lang, null, charsetname);
            }
        }
        
        byte[] token;
        
        byte[] phrase = getContentPhrase(content);
        // Case: phrase is found, indexed by content
        if (phrase != null && !phrases.isEmpty())
        {
            TemplateByteTokenizer tbt = new TemplateByteTokenizer(phrase);
            log.logMessage("Creating byte template " + content, Logger.L_VERBOSE);
            
            while((token=tbt.getNextBackward()) != null){
                log.logMessage("Make tag of " + new String(token), Logger.L_VERBOSE);
                ByteTagObject prev = makeTag(token, chain);
                if (prev != null) chain = prev;
            }
        }
        // Case: no phrase found
        else
        {
            chain = null;
        }
    }
    
    /**
     * Creates a TemplateBytes object with a phrase to parse, without the need to search
     * in <code>Phrases</code> for a phrase.
     * @param phrase The phrase to parse.
     * @param lang The language of the message.
     * @param cosName The name of the COS.
     * @param charsetname The name of the charset.
     */
    public TemplateBytes(byte[] phrase, String lang, String cosName, String charsetname)
    {
        this.content = null;
        this.lang = lang;
        this.cosName = cosName;
        this.charsetname = charsetname;
        this.brand = null;
        phrasePrefix = Phrases.getBytePrefix(lang, cosName, charsetname);
        
        byte[] token;
        
        // Case: phrase is found, indexed by content
        if (phrase != null)
        {
            TemplateByteTokenizer tbt = new TemplateByteTokenizer(phrase);
            log.logMessage("Creating byte template " + content, Logger.L_VERBOSE);
            
            while((token=tbt.getNextBackward()) != null){
                log.logMessage("Make tag of " + new String(token), Logger.L_VERBOSE);
                ByteTagObject prev = makeTag(token, chain);
                if (prev != null) chain = prev;
            }
        }
        // Case: no phrase found
        else
        {
            chain = null;
        }
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
    public byte[] generateBytes(UserMailbox inbox,
                               NotificationEmail email,
                               UserInfo user,
                               boolean generateDefault,
                               CallerInfo caller,
                               ANotifierNotificationInfo notifInfo) throws TemplateMessageGenerationException
    {
        ByteTagObject chain = this.chain;
        if (chain != null) {
            try {
                byte[] array = chain.getBytes(email, user, inbox, caller, notifInfo);
                return array;
            } catch (TemplateMessageGenerationException e) {
                //Failed to produce a string, e.g. because some message count was available
                log.logMessage("Failed to produce proper text using template " + content + ": " + e.getMessage(), Logger.L_DEBUG);
                if(!generateDefault) {
                    throw e;
                }
            }
        } 
        
        if(generateDefault) {
            return getTemplateContentTrimmed("general", ("New Message").getBytes()); 
        } else {
            String errorMsg = "Failed to produce proper text using template " + content;
            log.logMessage(errorMsg, Logger.L_DEBUG);
            throw new TemplateMessageGenerationException(errorMsg);
        }
    }
    
    /**
     * Returns a ByteTagObject represented by the given token.
     * @param token The token to create a ByteTagObject from.
     * @param nextTag The next tag (i.e. the chain).
     */
    private ByteTagObject makeTag(byte[] token, ByteTagObject nextTag)
    {
        if (ByteArrayUtils.matches(token, TAG_SUBJECT) != -1)
        {
            return new SubjectTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_FROM) != -1 || ByteArrayUtils.matches(token, TAG_FROM_EQ) != -1)
        {
            return new FromTag(nextTag, token);
        }
        if (ByteArrayUtils.matches(token, TAG_SIZE) != -1)
        {
            return new SizeTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_CONVERTED_PHONE) != -1)
        {
            return new ConvertedPhoneTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_PHONE) != -1)
        {
            return new PhoneTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_APPLEIMAPADDRESSINFO) != -1)
        {
            return new ImapAddressInfoTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_APPLEIMAPPORTINFO) != -1)
        {
            return new ImapPortInfoTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_IMAPUSERINFO) != -1)
        {
            return new ImapUserInfoTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_IMAPPASSWORDINFO) != -1)
        {
            return new ImapPasswordInfoTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_NUM_ATTACHMENTS) != -1)
        {
            return new AttachmentsTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_EMAIL_TEXT) != -1)
        {
            return new EmailTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_STATUS) != -1)
        {
            return new StatusTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_TCOUNT) != -1 || ByteArrayUtils.matches(token, TAG_TCOUNT_EQ) != -1)
        {
            return new NumNewTotTag(nextTag, token);
        }
        if (ByteArrayUtils.matches(token, TAG_VCOUNT) != -1 || ByteArrayUtils.matches(token, TAG_VCOUNT_EQ) != -1)
        {
            return new NumNewVoiceTag(nextTag, token);
        }
        if (ByteArrayUtils.matches(token, TAG_UVCOUNT) != -1)
        {
            return new NumNewUrgVoiceTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_CVCOUNT) != -1)
        {
            return new NumNewConfVoiceTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_VACOUNT) != -1 || ByteArrayUtils.matches(token, TAG_VACOUNT_EQ) != -1)
        {
            return new NumAllVoiceTag(nextTag, token);
        }
        if (ByteArrayUtils.matches(token, TAG_FCOUNT) != -1 || ByteArrayUtils.matches(token, TAG_FCOUNT_EQ) != -1)
        {
            return new NumNewFaxTag(nextTag, token);
        }
        if (ByteArrayUtils.matches(token, TAG_ECOUNT) != -1 || ByteArrayUtils.matches(token, TAG_ECOUNT_EQ) != -1)
        {
            return new NumNewEmailTag(nextTag, token);
        }
        if (ByteArrayUtils.matches(token, TAG_MCOUNT) != -1 || ByteArrayUtils.matches(token, TAG_MCOUNT_EQ) != -1)
        {
            return new NumNewVideoTag(nextTag, token);
        }
        if (ByteArrayUtils.matches(token, TAG_UMCOUNT) != -1)
        {
            return new NumNewUrgVideoTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_CMCOUNT) != -1)
        {
            return new NumNewConfVideoTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_QUOTA_TEXT) != -1)
        {
            return new QuotaTextTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_TIME) != -1)
        {
            return new TimeTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_TYPE) != -1)
        {
            return new DepositTypeTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_PRIORITY) != -1)
        {
            return new PriorityTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_CONFIDENTIAL) != -1)
        {
            return new ConfidentialTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_HANDLE_CONFIDENTIAL) != -1)
        {
            return new HandleConfidentialTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_DATE_EQ) != -1)
        {
            String format = new String(ByteArrayUtils.subArray(token, 7));
            return new FormattedDateTag(nextTag, format, this.lang);
        }
        if (ByteArrayUtils.matches(token, TAG_DATE) != -1)
        {
            return new DateTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_COUNT) != -1 || ByteArrayUtils.matches(token, TAG_COUNT_EQ) != -1)
        {
            return new CountTag(nextTag, token);
        }
        if (ByteArrayUtils.matches(token, TAG_UID) != -1)
        {
            return new UidTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_VVM_PREFIX) != -1)
        {
            return new VvmPrefixTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_VVA_TAG) != -1)
        {
            return new VvaSmsTag(nextTag, token);
        }
        if (ByteArrayUtils.matches(token, TAG_QUOTE) != -1)
        {
            return new BasicTag((byte)'\"', nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_COND) != -1)
        {
            return new CondTag(nextTag, new String(token), false);
        }
        if (ByteArrayUtils.matches(token, TAG_COND_2) != -1)
        {
            return new CondTag(nextTag, new String(token), false);
        }
        if (ByteArrayUtils.matches(token, TAG_COND_3) != -1)
        {
            return new CondTag(nextTag, new String(token), true);
        }
        if (ByteArrayUtils.matches(token, TAG_COND_4) != -1)
        {
            return new CondTag(nextTag, new String(token), true);
        }
        if (ByteArrayUtils.matches(token, TAG_UNICODE_NEWLINE) != -1)
        {
            return new NewlineTag(nextTag);
        }
        if (ByteArrayUtils.matches(token, TAG_HEX_EQ) != -1)
        {
            return new HexTag(nextTag, token);
        }
        if (ByteArrayUtils.matches(token, TAG_C_TAG) != -1)
        {
            return new CTag(nextTag, ByteArrayUtils.subArray(token, 2));
        }
        if (ByteArrayUtils.matches(token, TAG_PAYLOAD) != -1)
        {
            return new PayloadTag(nextTag);
        }
        // If all else fails, return a BasicTag
        return new BasicTag(token, nextTag);
    }
    
    /**
     * Converts the substrings "am" and "pm" from the string <code>msgdate</code>
     * with their byte equivalents <code>am</code> and <code>pm</code>, and returns
     * the result in the form of an array of bytes.
     */
    public static byte[] convertAmPmMarker(String msgdate, byte[] am, byte[] pm) {
        if(msgdate == null)
            return null;

        byte[] msgArray = msgdate.getBytes();
        int amindex = ByteArrayUtils.matches(msgArray, "am");
        int pmindex = ByteArrayUtils.matches(msgArray, "pm");

        byte[] result = new byte[msgArray.length + (amindex != -1 ? am.length - 2 : 0) + (pmindex != -1 ? pm.length - 2 : 0)];
        
        if (amindex != -1 && pmindex != -1 && amindex < pmindex)
            pmindex += am.length - 2;
        else if (amindex != -1 && pmindex != -1 && amindex > pmindex)
            amindex += pm.length - 2;
        
        for (int i = 0, msgIndex = 0; i < result.length; i++)
        {
            if (i == amindex)
            {
                for (int j = 0; j < am.length; j++)
                {
                    result[i] = am[j];
                    i++;
                }
                i--;
                msgIndex += 2;
            }
            else if (i == pmindex)
            {
                for (int j = 0; j < pm.length; j++)
                {
                    result[i] = pm[j];
                    i++;
                }
                i--;
                msgIndex += 2;
            }
            else
            {
                result[i] = msgArray[msgIndex];
                msgIndex++;
            }
        }
        
        return result;
     }
    
    /**
     * Returns the phrase (byte[]) associated with the given content.
     */
    private byte[] getContentPhrase(String content) {
        String key = phrasePrefix + content;
        return phrases.get(key);
    }
    
    /**
     * Formats the content given and prepends 0's to it if needed, and set maximum value.
     */
    private static byte[] formatCountValue(byte[] token, int value)
    {
        log.logMessage("formatCountValue received token:" + new String(token) + "  value:" + value , Logger.L_DEBUG);
        byte x = (byte) 'x';
        // If no =, return content
        int eqLoc = ByteArrayUtils.matches(token, "=");
        if (eqLoc == -1) {
            log.logMessage("formatCountValue: token does not contain '='.  Returning value unchanged: " + value , Logger.L_DEBUG);
            return ByteArrayUtils.intToByteArray(value);
        }
        // Get number of decimal places and max value
        int prependLength = 0;
        int max = 0;
        boolean maxSet = false;
        for (int i = eqLoc + 1; i < token.length; i++)
        {
            if (token[i] != x) {
                maxSet = true;
                max *= 10;
                char c = (char) (token[i]);
                try {
                    int m = Integer.parseInt(""+c);
                    max += m;
                } catch (NumberFormatException nfe) {
                    log.logMessage("Could not parse count token: " + new String(token) + ".  Returning value unchanged: " + value, Logger.L_ERROR);
                    return ByteArrayUtils.intToByteArray(value);
                }
            } else {
                prependLength++;
            }
        }
        // Set to max value
        if (value > max && maxSet) {
            value = max;
        }
        // Get number of 0's to append
        // Since value will always have at least one digit, there is always a minimum of 1 decimal place.
        int valueDecimalPlaces = 1;
        if(value > 9) {
            // Value has at least 2 digits; one digit already accounted for, so divide by 10 to remove one digit. 
            int v = value / 10;
            while (v > 0) {
                v /= 10;
                valueDecimalPlaces++;
            }
        }
        prependLength -= valueDecimalPlaces;
        // Get array of wanted 0's
        if (prependLength > 0) {
            byte[] prepend = new byte[prependLength];
            for (int i = 0; i < prepend.length; i++)
            {
                prepend[i] = (byte)'0';
            }
            // Since 0's are being prepended, if value is negative, convert value to be zero
            if(value < 0) {
                value = 0;
            }
            byte[] bytes = ByteArrayUtils.append(prepend, ByteArrayUtils.intToByteArray(value));
            log.logMessage("formatCountValue: Value " + value + " formatted to: " + new String(bytes), Logger.L_DEBUG);
            return bytes;
        } else {
            byte[] bytes = ByteArrayUtils.intToByteArray(value);
            log.logMessage("formatCountValue: Value " + value + " formatted to: " + new String(bytes), Logger.L_DEBUG);
            return bytes;
        }
    }

    /**
     * Returns a formatted phone number from the formatting rules.
     * @param numberInBytes The number to format.
     * @param contextInBytes The context of the format.
     */
    private static byte[] formatPhoneNumber(byte[] numberInBytes, byte[] contextInBytes)
    {
        if (contextInBytes.length < 1) return numberInBytes;
        String number = new String(numberInBytes);
        String context = new String(contextInBytes);
        String result = CommonMessagingAccess.getInstance().normalize(number, context, false);
        if (result == null) {
            return numberInBytes;
        }
        return result.getBytes();
    }

    /**
     * Used internally in created tags that can not contain
     * count specific information (recurse).
     */
    public byte[] getTemplateContentTrimmed(String content, byte[] fallback) {
        byte[] myContent = getContentPhrase(content);
        byte[] token;
        byte[] temp = {};
        if (myContent != null)
        {
            TemplateByteTokenizer tbt = new TemplateByteTokenizer(myContent);
            log.logMessage("Creating byte template " + content, Logger.L_VERBOSE);
            
            while((token=tbt.getNextBackward()) != null){
                log.logMessage("Make tag of " + new String(token), Logger.L_VERBOSE);
                temp = ByteArrayUtils.append(temp, token);
            }
            // temp could be a hex tag
            if (ByteArrayUtils.matches(temp, TAG_HEX_EQ) != -1)
            {
                HexTag tag = new HexTag(new EndTag(), temp);
                try {
                    return tag.getBytes(null, null, null, null, null);
                } catch (TemplateMessageGenerationException bte) {
                    return temp;
                }
            }
        }
        if (ByteArrayUtils.arrayEquals(temp, ByteArrayUtils.ARRAY_EMPTY))
        {
            return fallback;
        }
        return temp;
    }

   
    /**
     * Specifies the methods that must be available from all byte tag
     * objects.
     */
    private class ByteTagObject {
        
        /** Next tag object in the template. */
        protected ByteTagObject next;

        /**
         * Default constructor for a ByteTagObject.
         * @param next - the next tag object in the chain.
         */
        protected ByteTagObject(ByteTagObject next) {
            this.next = next;
        }

        /**
         * Returns the bytes for this ByteTag, as well as the bytes for each tag after it.
         */
        @SuppressWarnings("unused")
        public byte[] getBytes(NotificationEmail email, UserInfo user, UserMailbox inbox, CallerInfo caller, ANotifierNotificationInfo notifInfo) throws TemplateMessageGenerationException
        {
            return ByteArrayUtils.ARRAY_EMPTY;
        }
    }
    
    /**
     * EndTag appends nothing, it just ends the tag chain.
     */
    private final class EndTag extends ByteTagObject {
        EndTag() {
            super(null);
            log.logMessage("Making EndTag", Logger.L_VERBOSE);
        }
    }

    /**
     * SubjectTag appends the subject of the latest message
     */
    private final class SubjectTag extends ByteTagObject {

        SubjectTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making SubjectTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            if(email != null) {
                return ByteArrayUtils.append(email.getSubject().getBytes(), nextArray);
            } else {
                log.logMessage("SubjectTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                return nextArray;
            }
        }

        public String toString() {
            return "{subject}" + next;
        }
    }
    
    /**
     * PayloadTag appends a payload message from a payload file
     */
    private final class PayloadTag extends ByteTagObject {

        PayloadTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making PayloadTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            // Get next array
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);

            if(notifInfo != null) {
                try {
                    return ByteArrayUtils.append(notifInfo.getMessagePayloadAsBytes(), nextArray);
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
                return nextArray;
            }
        }

        public String toString() {
            return "{payload}" + next;
        }
    }
    
    /**
     * CTag appends the custom tag content to the rest of the message.
     */
    private final class CTag extends ByteTagObject
    {
        private String tag;
        
        CTag(ByteTagObject next, byte[] bTag)
        {
            super(next);
            tag = new String(bTag);
            log.logMessage("Making CTag", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            return ByteArrayUtils.append(phrases.get(tag), nextArray);
        }
        
        public String toString() { return "{CTag=" + tag + "}" + next; }
        
    }

    /**
     * DepositTypeTag appends the message type
     */
    private final class DepositTypeTag extends ByteTagObject {
        private byte[] emailText;
        private byte[] voiceText;
        private byte[] faxText;
        private byte[] videoText;

        DepositTypeTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making DepositTypeTag ", Logger.L_VERBOSE);
            emailText = getTemplateContentTrimmed("email", "#".getBytes());
            voiceText = getTemplateContentTrimmed("voice", "#".getBytes());
            faxText = getTemplateContentTrimmed("fax", "#".getBytes());
            videoText = getTemplateContentTrimmed("video", "#".getBytes());
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            if(caller != null) {
                log.logMessage("DepositTypeTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                return nextArray;
            } else if(notifInfo != null) {
                return ByteArrayUtils.append(getTemplateContentTrimmed(notifInfo.getNotificationType(), notifInfo.getNotificationType().getBytes()), nextArray);
            } else {
                switch (email.getDepositType()) {
                    case SLAMDOWN:
                        if(email.getSlamdownCallType() == NTF_VIDEO) {
                            return ByteArrayUtils.append(videoText, nextArray);
                        } else {
                            return ByteArrayUtils.append(voiceText, nextArray);
                        }
                    case VOICE:
                        return ByteArrayUtils.append(voiceText, nextArray);
                    case FAX:
                    case FAX_RECEPT_MAIL_TYPE:
                        return ByteArrayUtils.append(faxText, nextArray);
                    case VIDEO:
                        return ByteArrayUtils.append(videoText, nextArray);
                    case EMAIL:
                    default:
                        return ByteArrayUtils.append(emailText, nextArray);
                }
            }
        }

        public String toString() {
            return "{\"" + voiceText + "\"|\"" + faxText + "\"|\""
                + emailText + "\"|\"" + videoText + "\"}" + next;
        }
    }
    
    
    /**
     * PriorityTag appends the message priority.
     */
    private final class PriorityTag extends ByteTagObject {
        private byte[] non_urgent_message;
        private byte[] urgent_message;

        PriorityTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making PriorityTag ", Logger.L_VERBOSE);
            non_urgent_message = getTemplateContentTrimmed("non_urgent_message", "".getBytes());
            urgent_message = getTemplateContentTrimmed("urgent_message", "#".getBytes());
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            if(caller != null) {
                log.logMessage("StatusTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                return nextArray;
            } else {
                boolean isUrgent = false;
                if(notifInfo != null) {
                    isUrgent = notifInfo.getIsUrgent();
                } else if(email != null){
                    isUrgent = email.isUrgent();
                }
                if (isUrgent) {
                    return ByteArrayUtils.append(urgent_message, nextArray);
                } else {
                    return ByteArrayUtils.append(non_urgent_message, nextArray);
                }
            }
        }

        public String toString() {
            return "{\"" + new String(non_urgent_message) + "\"|\"" + new String(urgent_message) + "\"}"
                + next;
        }
    }

    
    /**
     * ConfidentialTag appends the message priority.
     */
    private final class ConfidentialTag extends ByteTagObject {
        private byte[] non_confidential_message;
        private byte[] confidential_message;

        ConfidentialTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making ConfidentialTag ", Logger.L_VERBOSE);
            non_confidential_message = getTemplateContentTrimmed("non_confidential_message", "".getBytes());
            confidential_message = getTemplateContentTrimmed("confidential_message", "#".getBytes());
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            if(email != null) {
                if (email.isConfidential()) {                
                    return ByteArrayUtils.append(confidential_message, nextArray);
                } else {
                    return ByteArrayUtils.append(non_confidential_message, nextArray);
                }
            } else {
                log.logMessage("StatusTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                return nextArray;
            }
        }

        public String toString() {
            return "{\"" + new String(non_confidential_message) + "\"|\"" + new String(confidential_message) + "\"}"
                + next;
        }
    }
    
    /**
     * HandleConfidentialTag appends the confidential message handle.
     */
    private final class HandleConfidentialTag extends ByteTagObject {
        private byte[] handle_non_confidential_message;
        private byte[] handle_confidential_message;

        HandleConfidentialTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making HandleConfidentialTag ", Logger.L_VERBOSE);
            handle_non_confidential_message = getTemplateContentTrimmed("handle_non_confidential_message", "".getBytes());
            handle_confidential_message = getTemplateContentTrimmed("handle_confidential_message", "#".getBytes());
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            if(email != null) {
                if (email.isConfidential()) {                
                    return ByteArrayUtils.append(handle_confidential_message, nextArray);
                } else {
                    return ByteArrayUtils.append(handle_non_confidential_message, nextArray);
                }
            } else {
                log.logMessage("HandleConfidentialTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                return nextArray;
            }
        }

        public String toString() {
            return "{\"" + new String(handle_non_confidential_message) + "\"|\"" + new String(handle_confidential_message) + "\"}"
                + next;
        }
    }
    
    
    /**
     * QuotaTextTag appends a warning about full mailbox, but only when it is
     * full or almost full.
     */
    private final class QuotaTextTag extends ByteTagObject {
        private byte[] quotaText;
        private byte[] quotahighleveltext;
        private byte[] voicequotatext;
        private byte[] voicequotahighleveltext;
        private byte[] videoquotatext;
        private byte[] videoquotahighleveltext;
        private byte[] faxquotatext;
        private byte[] faxquotahighleveltext;

        QuotaTextTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making QuotaTextTag ", Logger.L_VERBOSE);
            quotaText = getTemplateContentTrimmed("quotatext", "#".getBytes());
            quotahighleveltext = getTemplateContentTrimmed("quotahighleveltext", "#".getBytes());
            voicequotatext = getTemplateContentTrimmed("voicequotatext", "#".getBytes());
            voicequotahighleveltext = getTemplateContentTrimmed("voicequotahighleveltext", "#".getBytes());
            videoquotatext = getTemplateContentTrimmed("videoquotatext", "#".getBytes());
            videoquotahighleveltext = getTemplateContentTrimmed("videoquotahighleveltext", "#".getBytes());
            faxquotatext = getTemplateContentTrimmed("faxquotatext", "#".getBytes());
            faxquotahighleveltext = getTemplateContentTrimmed("faxquotahighleveltext", "#".getBytes());

        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            byte[] message = {};
            if(inbox != null){
                if(email != null) {
                    if(email.getQuotaExceeded())
                    {
                        message = ByteArrayUtils.append(message, quotaText);
                    }
                    else if (email.getQuotaAlmostExceeded())
                    {
                        message = ByteArrayUtils.append(message, quotahighleveltext);
                    }
                    if(email.getVoiceQuotaExceeded())
                    {
                        message = ByteArrayUtils.append(message, voicequotatext);
                    }
                    else if (email.getVoiceQuotaAlmostExceeded())
                    {
                        message = ByteArrayUtils.append(message, voicequotahighleveltext);
                    }
                    if(email.getVideoQuotaExceeded())
                    {
                        message = ByteArrayUtils.append(message, videoquotatext);
                    }
                    else if (email.getVideoQuotaAlmostExceeded())
                    {
                        message = ByteArrayUtils.append(message, videoquotahighleveltext);
                    }
                    if(email.getFaxQuotaExceeded())
                    {
                        message = ByteArrayUtils.append(message, faxquotatext);
                    }
                    else if (email.getFaxQuotaAlmostExceeded())
                    {
                        message = ByteArrayUtils.append(message, faxquotahighleveltext);
                    }
                } else {
                    log.logMessage("QuotaTextTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                }
            }

            return ByteArrayUtils.append(message, nextArray);
        }

        public String toString() {
            return "{\"" + quotaText + "\"}" + next;
        }
    }

    /**
     * AttachmentsTag appends the number of attachments.
     */
    private final class AttachmentsTag extends ByteTagObject {

        AttachmentsTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making AttachmentsTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            try {
                byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
                if(email != null) {
                    return ByteArrayUtils.append((""+email.getNoOfAttachments()).getBytes(), nextArray);
                } else {
                    log.logMessage("AttachmentsTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                    return nextArray;
                }
            } catch (MsgStoreException mse) {
                // Throw a TextTagException to keep default behaviour regarding exception handling
                throw new TemplateMessageGenerationException("SizeTag.appendValue exception" + mse);
            }
        }

        public String toString() {
            return "{num_attachments}" + next;
        }
    }

    /**
     * PhoneTag appends the users telephonenumber.
     */
    private final class PhoneTag extends ByteTagObject {

        PhoneTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making PhoneTag ", Logger.L_VERBOSE);
            this.next = next;
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            if(caller != null) {
                log.logMessage("PhoneTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                return nextArray;
            } else {
                String phone = null;
                if(notifInfo != null) {
                    phone = notifInfo.getReceiverPhoneNumber();
                } else if(email != null) {
                    phone = email.getReceiverPhoneNumber();
                }
                phone = CommonMessagingAccess.getInstance().denormalizeNumber(phone);
                return ByteArrayUtils.append(phone.getBytes(), nextArray);
            }
        }

        public String toString() {
            return "{phone}" + next;
        }
    }

    /**
     * ConvertedPhoneTag appends the users telephonenumber, but with a
     * configurable conversion applied. Note: converted phone tags must
     * be encoded using ASCII characters.
     */
    private final class ConvertedPhoneTag extends ByteTagObject {

        private Conversion c;

        ConvertedPhoneTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making ConvertedPhoneTag ", Logger.L_VERBOSE);
            c = new Conversion(new String(getTemplateContentTrimmed("converted_phone", "".getBytes())));
            this.next = next;
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {

            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            if(caller != null) {
                log.logMessage("ConvertedPhoneTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                return nextArray;
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
                return ByteArrayUtils.append(phone.getBytes(), nextArray);
            }
        }

        public String toString() {
            return "{converted phone" + c + "}" + next;
        }
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
     * SizeTag appends the size of the latest message.
     * It is possible to configure the format of the size string inserted, by
     * defining the subtemplates FaxSize, VoiceSize and EmailSize. They are
     * strings with the tag SIZE embedded. If a subtemplate is missing,
     * only the size is inserted.
     * <P>Note that the size for fax and voice messages already have a format,
     * created by MVAS, so the FaxSize and VoiceSize should normally be omitted
     * from the language files.</P>
     */
    private final class SizeTag extends ByteTagObject { 
        private final byte[] sizeTag = "SIZE".getBytes();
        private final int ixFax = 0;
        private final int ixVoice = 1;
        private final int ixEmail = 2;
        private final int ixVideo = 3;
        private final String[] msgTypes = {"F", "V", "E", "M"};

        //String before __SIZE__ per message type
        private byte[][] beforeN = new byte[ixVideo + 1][];
        //String after __SIZE__ per message type
        private byte[][] afterN = new byte[ixVideo + 1][];
        //If there is a __SIZE__ per message type
        private boolean[] showN = {false, false, false, false};

        SizeTag(ByteTagObject next) {
            super(next);
            byte[] tpl;
            log.logMessage("Making SizeTag ", Logger.L_VERBOSE);
            int tagPos;

            for (int type = ixFax; type <= ixVideo; type++) {
                tpl = getTemplateContentTrimmed(msgTypes[type] + "SIZE_TEXT", sizeTag);
                tagPos = ByteArrayUtils.contains(tpl, sizeTag);
                if (tagPos >= 0) {
                    showN[type] = true;
                    beforeN[type] = ByteArrayUtils.subArray(tpl, 0, tagPos);
                    afterN[type] = ByteArrayUtils.subArray(tpl, tagPos + sizeTag.length);
                } else { //The size shall not be included
                    beforeN[type] = tpl;
                    afterN[type] = "".getBytes();
                }
            }
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
                                        throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            if(email != null) {
                int ix;
                String sizeStr = null;
                int sizeInt = 0;
                try {
                    switch (email.getDepositType()) {
                        case VOICE:
                            ix = ixVoice;
                            sizeStr = email.getMessageLength();
                            break;
                        case FAX:
                            ix = ixFax;
                            sizeStr = email.getMessageLength();
                            break;
                        case EMAIL:
                            ix = ixEmail;
                            sizeInt = new Integer(email.getMessageSizeInKbytes());
                            break;
                        case VIDEO:
                            ix = ixVideo;
                            sizeStr = email.getMessageLength();
                            break;
                        default:
                            log.logMessage("Unknown deposit type: "
                                    + email.getDepositType(), Logger.L_ERROR);
                            ix = ixEmail;
                            sizeInt = new Integer(email.getMessageSizeInKbytes());
                            break;
                    }
                    if (showN[ix]) {
                        if (sizeStr == null)
                        {
                            byte[] result = ByteArrayUtils.append(ByteArrayUtils.append(beforeN[ix], (""+sizeInt).getBytes()), afterN[ix]);
                            return ByteArrayUtils.append(result, nextArray);
                        }
                        else
                        {
                            byte[] result = ByteArrayUtils.append(ByteArrayUtils.append(beforeN[ix], (sizeStr).getBytes()), afterN[ix]);
                            return ByteArrayUtils.append(result, nextArray);
                        }
                    } else {
                        byte[] result = ByteArrayUtils.append(beforeN[ix], afterN[ix]);
                        return ByteArrayUtils.append(result, nextArray);
                    }
                } catch (MsgStoreException mse) {
                    // Throw a TextTagException to keep default behaviour regarding exception handling
                    throw new TemplateMessageGenerationException("SizeTag.appendValue exception" + mse);
                } catch (IOException ioe) {
                    // Throw a TextTagException to keep default behaviour regarding exception handling
                    throw new TemplateMessageGenerationException("SizeTag.appendValue exception" + ioe);
                } catch (MessagingException me) {
                    // Throw a TextTagException to keep default behaviour regarding exception handling
                    throw new TemplateMessageGenerationException("SizeTag.appendValue exception" + me);
                }
            } else {
                log.logMessage("SizeTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                return nextArray;
            }
        }

        public String toString() {
            return "{size}" + next;
        }
    }
    
    private final class ImapAddressInfoTag extends ByteTagObject {
        ImapAddressInfoTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making ImapAddressInfoTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email, UserInfo user, UserMailbox inbox, CallerInfo caller, ANotifierNotificationInfo notifInfo) throws TemplateMessageGenerationException
        {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            if(caller != null) {
                log.logMessage("ImapAddressInfoTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                return nextArray;
            } else {
                GeoSystems geoSystems = OEManager.getGeoSystems();
                String homeSystemID = null;
                String ImapAddress = null;
                if (user != null){
                    //Get subscriber's home system ID
                    homeSystemID = user.getHomeSystemID();
                }
                log.logMessage("Template: subscriber " + user.getTelephoneNumber() + ", systemHomeId: " + ( homeSystemID == null ? "doesn't exist":homeSystemID ), Logger.L_VERBOSE);
                
                if (homeSystemID != null && !homeSystemID.isEmpty()){
                    ImapAddress = geoSystems.getImapServerAddr(homeSystemID);
                }
                if ( ImapAddress == null || ImapAddress.isEmpty()) {
                    //Try getting imap info from NTF config file
                    ImapAddress = Config.getAppleImapServerAddress();
                    if (ImapAddress == null || ImapAddress.isEmpty()){
                        // No value found in ntf config, do not append ImapAddress and port
                        log.logMessage("Template: No valid ImapAddress and port found in NTF, discard IMAPSERVERINFO tag", Logger.L_VERBOSE);
                        return nextArray;
                    }else{
                        //Found imap value in NTF. Append.
                        log.logMessage("Template: NTF ImapAddress is " + ImapAddress , Logger.L_VERBOSE);
                        return ByteArrayUtils.append(ImapAddress, nextArray);
                    }
                } else {
                    // append imap server and port
                    log.logMessage("Template: GEO ImapAddress is " + ImapAddress, Logger.L_VERBOSE);
                    return ByteArrayUtils.append(ImapAddress, nextArray);
                }

            }
        }
        
        public String toString() {
            return "{imapaddressinfo}" + next;
        }
    }

    private final class ImapPortInfoTag extends ByteTagObject {
        ImapPortInfoTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making ImapPortInfoTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email, UserInfo user, UserMailbox inbox, CallerInfo caller, ANotifierNotificationInfo notifInfo) throws TemplateMessageGenerationException
        {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            if(caller != null) {
                log.logMessage("ImapPortInfoTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                return nextArray;
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
                if (imapPort == null || imapPort.isEmpty() ) {
                    //Try getting imap info from NTF config file
                    imapPort = Config.getAppleImapServerPort();
                    if (imapPort == null || imapPort.isEmpty()){
                        // No value found in ntf config, do not append imap port
                        log.logMessage("Template: No valid imap port found in NTF, discard IMAPPORTINFO tag", Logger.L_VERBOSE);
                        return nextArray;
                    }else{
                        //Found imap value in NTF. Append.
                        log.logMessage("Template: NTF imapPort is " + imapPort , Logger.L_VERBOSE);
                        return ByteArrayUtils.append(imapPort, nextArray);
                    }
                } else {
                    // append imap port
                    log.logMessage("Template: GEO imapPort is " + imapPort , Logger.L_VERBOSE);
                    return ByteArrayUtils.append(imapPort, nextArray);
                }

            }
        }
        
        public String toString() {
            return "{imapportinfo}" + next;
        }
    }
    
    
    /**
     * ImapUserInfoTag appends the IMAP user info.
     */
    private final class ImapUserInfoTag extends ByteTagObject {
        ImapUserInfoTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making ImapUserInfoTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            if(caller != null) {
                log.logMessage("ImapUserInfoTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                return nextArray;
            } else {
                //get user info 
                String userInfo=null;
                if(user!=null){
                    userInfo = CommonMessagingAccess.getInstance().denormalizeNumber(user.getTelephoneNumber());
                    log.logMessage("Template: User userInfo is " + userInfo , Logger.L_VERBOSE);
                }
                
                if(userInfo==null || userInfo.isEmpty()){
                 // No value found , do not append imap user info
                    log.logMessage("Template: No valid imap user found, discard IMAPUSERINFO tag", Logger.L_VERBOSE);
                    return nextArray;
                }
                
                else{
                    // append imap user
                    log.logMessage("Template: imap user is " + userInfo , Logger.L_VERBOSE);
                    return ByteArrayUtils.append(userInfo, nextArray);
                }
            }
        }

        public String toString() {
            return "{imapuserinfo}" + next;
        }
    }

    
    /**
     * ImapPasswordInfoTag appends the IMAP password.
     */
    private final class ImapPasswordInfoTag extends ByteTagObject {
        ImapPasswordInfoTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making ImapPasswordInfoTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            if(caller != null) {
                log.logMessage("ImapPasswordInfoTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                return nextArray;
            } else {
                String imapPassword = null;
                if (user != null){
                    //Get vvm client password
                    imapPassword = user.getImapPassword();
                }
                if( imapPassword == null || imapPassword.isEmpty()){
                    // No value found , do not append imap password info
                    log.logMessage("Template: No valid imap password found, discard IMAPUSERINFO tag", Logger.L_VERBOSE);
                    return nextArray;
                }
                else{
                 // append imap password
                    log.logMessage("Template: imap password is " + imapPassword , Logger.L_VERBOSE);
                    return ByteArrayUtils.append(imapPassword, nextArray);
                }
            }
        }

        public String toString() {
            return "{imappasswordinfo}" + next;
        }
    }

    
    
    /**
     * The most basic tag. Simply appends its content to the end of the array.
     */
    private final class BasicTag extends ByteTagObject {
        private final byte[] content;
        BasicTag(byte[] content, ByteTagObject nextTag)
        {
            super(nextTag);
            this.content = content;
        }
        BasicTag(byte content, ByteTagObject nextTag)
        {
            super(nextTag);
            this.content = new byte[1];
            this.content[0] = content;
        }
        @Override
        public byte[] getBytes(NotificationEmail email, UserInfo user, UserMailbox inbox, CallerInfo caller, ANotifierNotificationInfo notifInfo) throws TemplateMessageGenerationException
        {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            return ByteArrayUtils.append(content, nextArray);
        }
    }
    
    /**
     * The most basic tag. Simply appends its content to the end of the array.
     */
    private final class HexTag extends ByteTagObject {
        private final String hexCode;
        HexTag(ByteTagObject nextTag, byte[] token)
        {
            super(nextTag);
            this.hexCode = new String(ByteArrayUtils.subArray(token, TAG_HEX_EQ.length()));
        }
        @Override
        public byte[] getBytes(NotificationEmail email, UserInfo user, UserMailbox inbox, CallerInfo caller, ANotifierNotificationInfo notifInfo) throws TemplateMessageGenerationException
        {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            byte[] translated = ByteArrayUtils.stringToByteArrayNoSpaces(hexCode);
            if (ByteArrayUtils.arrayEquals(translated, ByteArrayUtils.ARRAY_EMPTY))
                log.logMessage("HexTag returned empty array on code " + hexCode, Logger.L_DEBUG);
            return ByteArrayUtils.append(translated, nextArray);
        }
    }

    /**
     * CondTag appends a condition for
     */
    private final class CondTag extends ByteTagObject {
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

        CondTag(ByteTagObject next, String conditions, boolean totalCount) {
            super(next);
            this.totalCount = totalCount;
            readConditions(conditions);
            log.logMessage("Making CondTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            byte[] nextArray = {};
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
                } if( totalCount && inbox.getNewTotalCount() == MSG_COUNT_ERR ) {
                    throw(new TemplateMessageGenerationException("Failed to produce proper text"));
                }
                if( totalCount ) {
                    if( _tcond[ Math.min(inbox.getNewTotalCount(), 10)] ) {
                        nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
                    }  else {
                        nextArray = next.next.getBytes(email, user, inbox, caller, notifInfo);
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
                        nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
                    else
                        nextArray = next.next.getBytes(email, user, inbox, caller, notifInfo);
                }
            } else {
                log.logMessage("CondTag not supported since subscriber inbox is null; skipping this tag.", Logger.L_VERBOSE);
            }

            return nextArray;
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
                if(size==4){
                    setCondition(strtoken.nextToken(), "v") /*for voice*/;
                    setCondition("*"                 , "uv") /*for urgent voice*/;
                    setCondition("*"                 , "cv") /*for confidential voice*/;
                    setCondition(strtoken.nextToken(), "f") /*for fax*/;
                    setCondition(strtoken.nextToken(), "e") /*for email*/;
                    setCondition(strtoken.nextToken(), "m") /*for video*/;
                    setCondition("*"                 , "um") /*for urgent video*/;
                    setCondition("*"                 , "cm") /*for confidential video*/;
                }
                else if(size==6){
                    setCondition(strtoken.nextToken(), "v") /*for voice*/;
                    setCondition(strtoken.nextToken(), "uv") /*for urgent voice*/;
                    setCondition("*"                 , "cv") /*for confidential voice*/;
                    setCondition(strtoken.nextToken(), "f") /*for fax*/;
                    setCondition(strtoken.nextToken(), "e") /*for email*/;
                    setCondition(strtoken.nextToken(), "m") /*for video*/;
                    setCondition(strtoken.nextToken(), "um") /*for urgent video*/;
                    setCondition("*"                 , "cm") /*for confidential video*/;
                }
                else if(size==8){
                    setCondition(strtoken.nextToken(), "v") /*for voice*/;
                    setCondition(strtoken.nextToken(), "uv") /*for urgent voice*/;
                    setCondition(strtoken.nextToken(), "cv") /*for confidential voice*/;
                    setCondition(strtoken.nextToken(), "f") /*for fax*/;
                    setCondition(strtoken.nextToken(), "e") /*for email*/;
                    setCondition(strtoken.nextToken(), "m") /*for video*/;
                    setCondition(strtoken.nextToken(), "um") /*for urgent video*/;
                    setCondition(strtoken.nextToken(), "cm") /*for confidential video*/;
                }
                else{
                    return;
                }
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
     * VvaSmsTag Tag will search the properties list which accompanies the email to see if a property has been defined
     */
    private final class VvaSmsTag extends ByteTagObject {
        private byte[] tagname = null;

        VvaSmsTag(ByteTagObject next, byte[] tag) {
            super(next);

            tagname = ByteArrayUtils.replaceAll(ByteArrayUtils.subArray(tag, TAG_VVA_TAG.length(), tag.length), (byte)'_', (byte)' ');
            log.logMessage("Making VvaSmsTag [" + new String(tag) + "] becomes " + new String(tagname), Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email, UserInfo user, UserMailbox inbox, CallerInfo caller, ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            if(caller != null) {
                //log.logMessage("VvaSmsTag (TAG) not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                //return nextArray;
                String value = null;
                value = caller.getSlamdownInfoProperty(new String(tagname));
                if(value == null || value.equals("")){
                    log.logMessage("VvaSmsTag.getBytes(): caller.getSlamdownInfoProperty for tagname: " + new String(tagname) + " returned null or an empty String; skipping this tag.", Logger.L_VERBOSE);
                    return nextArray;
                } else {
                    String stringTagname = new String(tagname);
                    if((stringTagname != null) && (stringTagname.length() > 4) && ("hex-".equalsIgnoreCase(stringTagname.substring(0, 4)))){
                        //Special case: if tagname starts with "hex-", interpret as hexadecimal (same as for HEX Tag but with variable content)
                        log.logMessage("VvaSmsTag.getBytes(): caller.getSlamdownInfoProperty for tagname: " + new String(tagname) + " returned: " + value + " that will be interpreted as hexadecimal", Logger.L_VERBOSE);
                        byte[] hexValue = ByteArrayUtils.stringToByteArrayNoSpaces(value);
                        if(ByteArrayUtils.arrayEquals(hexValue, ByteArrayUtils.ARRAY_EMPTY)) {
                            log.logMessage("VvaSmsTag.getBytes(): ByteArrayUtils.stringToByteArrayNoSpaces returned an empty array; skipping this tag (" + new String(tagname) + ").", Logger.L_VERBOSE);
                            return nextArray;
                        } else {
                            return ByteArrayUtils.append(hexValue, nextArray);
                        }
                    } else {
                        log.logMessage("VvaSmsTag.getBytes(): caller.getSlamdownInfoProperty for tagname: " + new String(tagname) + " returned: " + value, Logger.L_VERBOSE);
                        return ByteArrayUtils.append(value.getBytes(), nextArray);
                    }
                }
            } else {
                String value = null;
                if(notifInfo != null) {
                    value = notifInfo.getProperty(new String(tagname));
                } else if(email != null) {
                    value = email.getNtfEvent().getProperty(new String(tagname));
                    if(value == null){
                        value = email.getAdditionalProperty(new String(tagname));
                    }
                }
                if(value == null || value.equals("")) {
                    log.logMessage("VvaSmsTag.getBytes(): Can't find value for event property: " + new String(tagname) + "; skipping this tag.", Logger.L_VERBOSE);
                    return nextArray;
                } else {
                    String stringTagname = new String(tagname);
                    if((stringTagname != null) && (stringTagname.length() > 4) && ("hex-".equalsIgnoreCase(stringTagname.substring(0, 4)))){
                        //Special case: if tagname starts with "hex-", interpret as hexadecimal (same as for HEX Tag but with variable content)
                        log.logMessage("VvaSmsTag.getBytes(): Retrieving event property: " + new String(tagname) + "=" + value + ". This value will be interpreted as hexadecimal", Logger.L_VERBOSE);
                        byte[] hexValue = ByteArrayUtils.stringToByteArrayNoSpaces(value);
                        if(ByteArrayUtils.arrayEquals(hexValue, ByteArrayUtils.ARRAY_EMPTY)) {
                            log.logMessage("VvaSmsTag.getBytes(): ByteArrayUtils.stringToByteArrayNoSpaces returned an empty array; skipping this tag (" + new String(tagname) + ").", Logger.L_VERBOSE);
                            return nextArray;
                        } else {
                            return ByteArrayUtils.append(hexValue, nextArray);
                        }
                    } else {
                        log.logMessage("VvaSmsTag.getBytes(): Retrieving event property: " + new String(tagname) + "=" + value, Logger.L_VERBOSE);
                        return ByteArrayUtils.append(value.getBytes(), nextArray);
                    }
                }
            }
        }

        public String toString() {
            return "" + next;
        }

    }

    /**
     * VvmIdTag Tag retrieves the ID field as respecting the OMTP and IMAP standards.
     */
    private final class VvmPrefixTag extends ByteTagObject {

        VvmPrefixTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making VvmPrefixTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email, UserInfo user, UserMailbox inbox, CallerInfo caller, ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            return ByteArrayUtils.append(user.getVvmClientPrefix(), nextArray);
        }

        public String toString() {
            return "{vvm prefix}" + next;
        }
    }

    /**
     * VvmIdTag Tag retrieves the ID field as respecting the OMTP and IMAP standards.
     * @TODO - The id will need to be changed for the correct one once system decides what that is.
     */
    private final class UidTag extends ByteTagObject {

        UidTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making UidTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email, UserInfo user, UserMailbox inbox, CallerInfo caller, ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            if (email != null) {
                byte[] msg = email.getUID().getBytes();
                return ByteArrayUtils.append(msg, nextArray);
            } else {
                log.logMessage("UidTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                return nextArray;
            }
        }

        public String toString() {
            return "{uid}" + next;
        }
    }

    /**
     * CountTag appends the call count.
     */
    private final class CountTag extends ByteTagObject {
        
        private final byte[] token;

        CountTag(ByteTagObject next, byte[] token) {
            super(next);
            this.token = token;
            Logger.getLogger().logMessage("Making CountTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
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
                return ByteArrayUtils.append(formatCountValue(token, c), nextArray);
            } else {
                log.logMessage("CountTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                return nextArray;
            }
        }

        public String toString() {
            return "{count}" + next;
        }
    }
    
    /**
     * FromTag appends the sender of the latest message.
     */
    private final class FromTag extends ByteTagObject {
        private byte[] unknown;
        private byte[] prefix;
        private byte[] HASH = {'#'};
        private final byte[] token;

        FromTag(ByteTagObject next, byte[] token) {
            super(next);
            this.token = token;
            log.logMessage("Making FromTag ", Logger.L_VERBOSE);
            unknown = getTemplateContentTrimmed("unknownsender", getTemplateContentTrimmed("numwithheldtext", HASH));
            prefix = getTemplateContentTrimmed("fromnumberprefix", ByteArrayUtils.ARRAY_EMPTY);
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                           UserInfo user,
                           UserMailbox inbox,
                           CallerInfo caller,
                           ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            String from = null; // Treat from as String for now.
            boolean prependPrefix = false;
            byte[] fromByteForm = null;
            if( caller != null ) {
               from = caller.getNumber();
               if (from != null && from.length() > 0) {
                   from = CommonMessagingAccess.getInstance().denormalizeNumber(from);
                   prependPrefix = true;
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

               // The truncation here should be done on the from number with prefix
               if (maxDigitsInNumber != 0 && from != null && from.length() > maxDigitsInNumber) {
                   // Get byte from and append prefix
                   fromByteForm = from.getBytes();
                   if (prependPrefix) {
                       fromByteForm = ByteArrayUtils.append(prefix, fromByteForm);
                       prependPrefix = false; // We already appended the prefix
                   }
                   // Set from to null here since we already got the bytes
                   from = null;
                   // Truncate with bytes
                   if ("".equals(truncatedNumberIndication)) {
                       fromByteForm = ByteArrayUtils.subArray(fromByteForm, fromByteForm.length - maxDigitsInNumber);
                   } else {
                       fromByteForm = ByteArrayUtils.append(truncatedNumberIndication.substring(0, 1).getBytes(),
                               ByteArrayUtils.subArray(fromByteForm, fromByteForm.length - maxDigitsInNumber + 1));
                   }
               }
            } else if(notifInfo != null) {
                if (notifInfo.getSenderVisibility()) {
                    from = notifInfo.getSenderPhoneNumber();
                    if (from != null && from.length() > 0) {
                        from = CommonMessagingAccess.getInstance().denormalizeNumber(from);
                        prependPrefix = true;
                    } else {
                        fromByteForm = unknown;
                    }

                } else {         
                    String displayName = notifInfo.getSenderDisplayName();
                    if (displayName != null && displayName.length() > 0 ){
                        displayName = displayName.toLowerCase();
                        if ( "unknown".equals(displayName)  ){                
                            fromByteForm = unknown;
                        } else {
                            fromByteForm = getTemplateContentTrimmed(displayName, unknown);
                        }
                    } else {
                        fromByteForm = unknown;
                    }

                    log.logMessage("Tag FROM set to " + new String(fromByteForm) + " since sender-visibility is 0.", Logger.L_DEBUG);
                }
            }  else {

                if (email.getSenderVisibile()) {

                    switch (email.getDepositType()) {
                        case VIDEO:
                        case VOICE:
                        case FAX:
                        case FAX_RECEPT_MAIL_TYPE:
                            from = email.getSenderPhoneNumber();
                            if (from != null && from.length() > 0) {
                                from = CommonMessagingAccess.getInstance().denormalizeNumber(from);
                                prependPrefix = true;
                            }
                            break;
                        case EMAIL:
                        default:
                            from = email.getSender();
                            break;
                    }
                    if (from == null || "".equals(from)) {
                        fromByteForm = unknown;
                    }

                } else {
                    
                    String displayName = email.getSenderDisplayName();
                    if (displayName != null && displayName.length() > 0 ){
                        displayName = displayName.toLowerCase();
                        if ( "unknown".equals(displayName)  ){                
                            fromByteForm = unknown;
                        } else {
                            fromByteForm = getTemplateContentTrimmed(displayName, unknown);
                        }
                    } else {
                        fromByteForm = unknown;
                    }
                    
                    log.logMessage("Tag FROM set to " + new String(fromByteForm) + " since sender-visibility is 0.", Logger.L_DEBUG);
                    
                }
            }
            if (from != null)
            {
                fromByteForm = from.getBytes();
            }
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);

            // Format with formatting rules.
            int match = ByteArrayUtils.matches(token, "=ctx:");
            if (match != -1) {
                fromByteForm = formatPhoneNumber(fromByteForm, ByteArrayUtils.subArray(token, match + 5));
            }
            if (prependPrefix)
            {
                fromByteForm = ByteArrayUtils.append(prefix, fromByteForm);
            }
            return ByteArrayUtils.append(fromByteForm, nextArray);
        }

        public String toString() {
            return "{from|\"" + new String(unknown) + "\"}" + next;
        }
    }

    /**
     * TimeTag appends the message time.
     */
    private final class TimeTag extends ByteTagObject {
        private byte[] _am;
        private byte[] _pm;

        TimeTag(ByteTagObject next) {
            super(next);
            this._am = getTemplateContentTrimmed("am", "AM".getBytes());
            this._pm = getTemplateContentTrimmed("pm", "PM".getBytes());
            log.logMessage("Making TimeTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
                throws TemplateMessageGenerationException {
            String messageTime = "";
            byte[] byteTime;
            if( caller != null ) {
                messageTime = user.getUsersTime(caller.getCallTime());
            } else if(notifInfo != null) {
                messageTime = user.getUsersTime(notifInfo.getDate());
            } else {
                messageTime = user.getUsersTime(email.getMessageReceivedDate());
            }
            String usrPreferredTimeFormat = user.getPreferredTimeFormat();
            if (usrPreferredTimeFormat != null && "12".equals(usrPreferredTimeFormat))
                byteTime = convertAmPmMarker(messageTime, _am, _pm);
            else
                byteTime = messageTime.getBytes();

            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            return ByteArrayUtils.append(byteTime, nextArray);

        }

        public String toString() {
            return "{time}" + next;
        }
    }
    
    /**
     * NumNewVoiceTag appends the new voice message count.
     */
    private final class NumNewVoiceTag extends ByteTagObject {
        
        private final byte[] token;

        NumNewVoiceTag(ByteTagObject next, byte[] token) {
            super(next);
            this.token = token;
            log.logMessage("Making NumNewVoiceTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
                throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            try {
                if (inbox != null) {
                    return ByteArrayUtils.append(formatCountValue(token, inbox.getNewVoiceCount()), nextArray);
                } else {
                    return ByteArrayUtils.append(("-1").getBytes(), nextArray);
                }
            } catch (Exception e) {
                // Do not throw exception (such as TemplateMessageGenerationException).
                return ByteArrayUtils.append(("-1").getBytes(), nextArray);
            }
        }

        public String toString() {
            return "{vcount}" + next;
        }
    }

    /**
     * NumNewUrgVoiceTag appends the new urgent voice message count.
     */
    private final class NumNewUrgVoiceTag extends ByteTagObject {

        NumNewUrgVoiceTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making NumNewUrgVoiceTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
                throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            try {
                if (inbox != null) {
                    return ByteArrayUtils.append((""+inbox.getNewUrgentVoiceCount()).getBytes(), nextArray);
                } else {
                    return ByteArrayUtils.append(("-1").getBytes(), nextArray);
                }
            } catch (Exception e) {
                // Do not throw exception (such as TemplateMessageGenerationException).
                return ByteArrayUtils.append(("-1").getBytes(), nextArray);
            }
        }

        public String toString() {
            return "{uvcount}" + next;
        }
    }
    
    /**
     * NumNewConfVoiceTag appends the new confidential voice message count.
     */
    private final class NumNewConfVoiceTag extends ByteTagObject {

        NumNewConfVoiceTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making NumNewConfVoiceTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
                throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            try {
                if (inbox != null) {
                    return ByteArrayUtils.append((""+inbox.getNewConfidentialVoiceCount()).getBytes(), nextArray);
                } else {
                    return ByteArrayUtils.append(("-1").getBytes(), nextArray);
                }
            } catch (Exception e) {
                // Do not throw exception (such as TemplateMessageGenerationException).
                return ByteArrayUtils.append(("-1").getBytes(), nextArray);
            }
        }

        public String toString() {
            return "{cvcount}" + next;
        }
    }
    
    /**
     * NumAllVoiceTag appends all voice message count.
     */
    private final class NumAllVoiceTag extends ByteTagObject {
        
        private final byte[] token;

        NumAllVoiceTag(ByteTagObject next, byte[] token) {
            super(next);
            this.token = token;
            log.logMessage("Making NumAllVoiceTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
                throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            try {
                if (inbox != null) {
                    return ByteArrayUtils.append(formatCountValue(token, inbox.getVoiceTotalCount()), nextArray);
                } else {
                    return ByteArrayUtils.append(("-1").getBytes(), nextArray);
                }
            } catch (Exception e) {
                // Do not throw exception (such as TemplateMessageGenerationException).
                return ByteArrayUtils.append(("-1").getBytes(), nextArray);
            }
        }

        public String toString() {
            return "{vacount}" + next;
        }
    }
    
    /**
     * NumNewFaxTag appends the new fax message count.
     */
    private final class NumNewFaxTag extends ByteTagObject {
        
        private final byte[] token;

        NumNewFaxTag(ByteTagObject next, byte[] token) {
            super(next);
            this.token = token;
            log.logMessage("Making NumNewFaxTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
                throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            try {
                if (inbox != null) {
                    return ByteArrayUtils.append(formatCountValue(token, inbox.getNewFaxCount()), nextArray);
                } else {
                    return ByteArrayUtils.append(("-1").getBytes(), nextArray);
                }
            } catch (Exception e) {
                // Do not throw exception (such as TemplateMessageGenerationException).
                return ByteArrayUtils.append(("-1").getBytes(), nextArray);
            }
        }

        public String toString() {
            return "{fcount}" + next;
        }
    }
    
    /**
     * NumNewEmailTag appends the new email message count.
     */
    private final class NumNewEmailTag extends ByteTagObject {
        
        private final byte[] token;

        NumNewEmailTag(ByteTagObject next, byte[] token) {
            super(next);
            this.token = token;
            log.logMessage("Making NumNewEmailTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
                throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            try {
                if (inbox != null) {
                    return ByteArrayUtils.append(formatCountValue(token, inbox.getNewEmailCount()), nextArray);
                } else {
                    return ByteArrayUtils.append(("-1").getBytes(), nextArray);
                }
            } catch (Exception e) {
                // Do not throw exception (such as TemplateMessageGenerationException).
                return ByteArrayUtils.append(("-1").getBytes(), nextArray);
            }
        }

        public String toString() {
            return "{ecount}" + next;
        }
    }
    
    /**
     * NumNewVideoTag appends the new video message count.
     */
    private final class NumNewVideoTag extends ByteTagObject {
        
        private final byte[] token;

        NumNewVideoTag(ByteTagObject next, byte[] token) {
            super(next);
            this.token = token;
            log.logMessage("Making NumNewVideoTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
                throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            try {
                if (inbox != null) {
                    return ByteArrayUtils.append(formatCountValue(token, inbox.getNewVideoCount()), nextArray);
                } else {
                    return ByteArrayUtils.append(("-1").getBytes(), nextArray);
                }
            } catch (Exception e) {
                // Do not throw exception (such as TemplateMessageGenerationException).
                return ByteArrayUtils.append(("-1").getBytes(), nextArray);
            }
        }

        public String toString() {
            return "{mcount}" + next;
        }
    }
    
    /**
     * NumNewUrgVideoTag appends the new urgent video message count.
     */
    private final class NumNewUrgVideoTag extends ByteTagObject {

        NumNewUrgVideoTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making NumNewUrgVideoTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
                throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            try {
                if (inbox != null) {
                    return ByteArrayUtils.append((""+inbox.getNewUrgentVideoCount()).getBytes(), nextArray);
                } else {
                    return ByteArrayUtils.append(("-1").getBytes(), nextArray);
                }
            } catch (Exception e) {
                // Do not throw exception (such as TemplateMessageGenerationException).
                return ByteArrayUtils.append(("-1").getBytes(), nextArray);
            }
        }

        public String toString() {
            return "{umcount}" + next;
        }
    }
    
    /**
     * NumNewConfVideoTag appends the new confidential video message count.
     */
    private final class NumNewConfVideoTag extends ByteTagObject {

        NumNewConfVideoTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making NumNewConfVideoTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
                throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            try {
                if (inbox != null) {
                    return ByteArrayUtils.append((""+inbox.getNewConfidentialVideoCount()).getBytes(), nextArray);
                } else {
                    return ByteArrayUtils.append(("-1").getBytes(), nextArray);
                }
            } catch (Exception e) {
                // Do not throw exception (such as TemplateMessageGenerationException).
                return ByteArrayUtils.append(("-1").getBytes(), nextArray);
            }
        }

        public String toString() {
            return "{cmcount}" + next;
        }
    }
    
    /**
     * NumNewTotTag appends the new message count.
     */
    private final class NumNewTotTag extends ByteTagObject {
        
        final byte[] token;

        NumNewTotTag(ByteTagObject next, byte[] token) {
            super(next);
            this.token = token; 
            log.logMessage("Making NumNewTotTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
                throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            try {
                if (inbox != null) {
                    return ByteArrayUtils.append(formatCountValue(token, inbox.getNewTotalCount()), nextArray);
                } else {
                    return ByteArrayUtils.append(("-1").getBytes(), nextArray);
                }
            } catch (Exception e) {
                // Do not throw exception (such as TemplateMessageGenerationException).
                return ByteArrayUtils.append(("-1").getBytes(), nextArray);
            }
        }

        public String toString() {
            return "{tcount}" + next;
        }
    }

    /**
     * StatusTag appends the message urgency.
     */
    private final class StatusTag extends ByteTagObject {
        private byte[] normal;
        private byte[] urgent;

        StatusTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making StatusTag ", Logger.L_VERBOSE);
            normal = getTemplateContentTrimmed("normal", "#".getBytes());
            urgent = getTemplateContentTrimmed("urgent", "#".getBytes());
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
            if(caller != null) {
                log.logMessage("StatusTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                return nextArray;
            } else {
                boolean isUrgent = false;
                if(notifInfo != null) {
                    isUrgent = notifInfo.getIsUrgent();
                } else {
                    isUrgent = email.isUrgent();
                }
                if (isUrgent) {
                    return ByteArrayUtils.append(urgent, nextArray);
                } else {
                    return ByteArrayUtils.append(normal, nextArray);
                }
            }
        }

        public String toString() {
            return "{\"" + new String(normal) + "\"|\"" + new String(urgent) + "\"}"
                + next;
        }
    }
    
    /**
     * EmailTag appends the email message text.
     */
    private final class EmailTag extends ByteTagObject {

        EmailTag(ByteTagObject next) {
            super(next);
            log.logMessage("Making EmailTag ", Logger.L_VERBOSE);
        }

        @Override
        public byte[] getBytes(NotificationEmail email,
                                UserInfo user,
                                UserMailbox inbox,
                                CallerInfo caller,
                                ANotifierNotificationInfo notifInfo)
            throws TemplateMessageGenerationException {
            try {
                byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
                if(email != null) {
                    String text = email.getMessageText();
                    if( text != null ) {
                        text = text.trim();
                    }
                    return ByteArrayUtils.append((text == null ? "" : text).getBytes(), nextArray);
                } else {
                    log.logMessage("TextTag not supported for notification type; skipping this tag.", Logger.L_VERBOSE);
                    return nextArray;
                }
            } catch (MsgStoreException mse) {
                // Throw a TextTagException to keep default behaviour regarding exception handling
                throw new TemplateMessageGenerationException("EmailTag.appendValue exception" + mse);
            }
        }

        public String toString() {
            return "{message text}" + next;
        }
    }

   /**
    * DateTag appends the message date.
    */
   private final class DateTag extends ByteTagObject {

       DateTag(ByteTagObject next) {
           super(next);
           log.logMessage("Making DateTag ", Logger.L_VERBOSE);
       }

       @Override
       public byte[] getBytes(NotificationEmail email,
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
           byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
           return ByteArrayUtils.append(messageTime.getBytes(), nextArray);
       }

       public String toString() {
           return "{date}" + next;
       }
   }
   
   /**
    * Appends the message date, formatted in a non-standard way.
    */
   private final class FormattedDateTag extends ByteTagObject {
       DateFormat fmt;
       private byte[] _am;
       private byte[] _pm;
       private String _format = null;

       FormattedDateTag(ByteTagObject next, String format, String language) {
           super(next);
           this._am = getTemplateContentTrimmed("am", "AM".getBytes()); // fallback added
           this._pm = getTemplateContentTrimmed("pm", "PM".getBytes());
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

       @Override
       public byte[] getBytes(NotificationEmail email,
                               UserInfo user,
                               UserMailbox inbox,
                               CallerInfo caller,
                               ANotifierNotificationInfo notifInfo)
           throws TemplateMessageGenerationException {

           String messageDate = "";
           byte[] byteDate;
           if( caller != null ) {
               messageDate = fmt.format(caller.getCallTime());
           } else if(notifInfo != null) {
               messageDate = fmt.format(notifInfo.getDate());
           } else {
               messageDate = fmt.format(email.getMessageReceivedDate());
           }
           if (_format != null && _format.matches(".*a.*"))
               byteDate = convertAmPmMarker(messageDate, _am, _pm);
           else
               byteDate = messageDate.getBytes();

           byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
           return ByteArrayUtils.append(byteDate, nextArray);
       }

       public String toString() {
           return "{date=" + fmt + "}" + next;
       }
   }
   
   /**
    * Appends an ASCII newline character.
    */
   private final class NewlineTag extends ByteTagObject {
       NewlineTag(ByteTagObject next) {
           super(next);
           log.logMessage("Making NewlineTag ", Logger.L_VERBOSE);
       }

       @Override
       public byte[] getBytes(NotificationEmail email,
                               UserInfo user,
                               UserMailbox inbox,
                               CallerInfo caller,
                               ANotifierNotificationInfo notifInfo)
           throws TemplateMessageGenerationException {
           byte[] nextArray = next.getBytes(email, user, inbox, caller, notifInfo);
           return ByteArrayUtils.append("\n", nextArray);
       }

       public String toString() {
           return "{newline (\\n)}" + next;
       }
   }
    
}
