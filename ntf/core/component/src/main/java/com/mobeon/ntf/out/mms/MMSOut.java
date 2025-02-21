/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.mms;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;

import com.abcxyz.messaging.mfs.exception.MsgStoreException;
import com.abcxyz.messaging.transcoderfacade.Transcoder;
import com.abcxyz.messaging.vvs.ntf.notifier.NotifierUtil;
import com.mobeon.common.cmnaccess.CommonMessagingAccess;
import com.mobeon.common.cmnaccess.oam.MoipOamManager;
import com.mobeon.common.content.BinaryContent;
import com.mobeon.common.content.ContentPart;
import com.mobeon.common.content.MMSMediaContent;
import com.mobeon.common.email.request.CustomHeader;
import com.mobeon.common.email.request.MimeContainer;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.NotificationConfigConstants;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.management.ManagementCounter;
import com.mobeon.ntf.management.ManagementInfo;
import com.mobeon.ntf.meragent.MerAgent;
import com.mobeon.ntf.out.FeedbackHandler;
import com.mobeon.ntf.out.smtp.SMTPOut;
import com.mobeon.ntf.out.smtp.SMTPOutFactory;
import com.mobeon.ntf.text.TemplateMessageGenerationException;
import com.mobeon.ntf.text.TextCreator;
import com.mobeon.ntf.userinfo.MmsFilterInfo;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.util.NtfUtil;
import com.abcxyz.services.mms.sdk.vasp.api.MessageClass;
import com.abcxyz.services.mms.sdk.vasp.api.MultiMediaApplication;
import com.abcxyz.services.mms.sdk.vasp.api.MultiMediaController;
import com.abcxyz.services.mms.sdk.vasp.api.MultiMediaMessage;
import com.abcxyz.services.mms.sdk.vasp.api.MultiMediaSubmitResponse;
import com.abcxyz.services.mms.sdk.vasp.api.Number;
import com.abcxyz.services.mms.sdk.vasp.api.Priority;
import com.abcxyz.services.moip.ntf.coremgmt.fallback.FallbackHandler;
import com.abcxyz.services.moip.ntf.coremgmt.fallback.FallbackInfo;

/**
 * MMSOut is NTFs interface for sending MMS notifications.
 */
public class MMSOut implements Constants {
    
    private final static Logger log = Logger.getLogger(MMSOut.class); 
    private Hashtable<String, MMSCenter> centers; // MMSCenters, keyed by host name
    
    private static int _mmsid = 1;

    private static int _mmsThreadID;
    private int _noOfMMSThreads;

    /* Counters for management information */
    private static ManagementCounter successCounter =
        ManagementInfo.get().getCounter("MultimediaMessage", ManagementCounter.CounterType.SUCCESS);
    private static ManagementCounter failCounter =
        ManagementInfo.get().getCounter("MultimediaMessage", ManagementCounter.CounterType.FAIL);

    private Transcoder transcoder = new Transcoder(new MoipOamManager(), "MMSOut");

    private int autoIndex = 1;

    /**
     * Constructor.
     */
    public MMSOut() {
        centers = new Hashtable<String, MMSCenter>(10, 0.5F);
    }

    /**
     * sendNotification sends a MMS notification to a user.
     *@param ng the FeedbackHandler that contains the notification contents
     * and collects responses for all receivers of the email.
     *@param user the information about the receiver.
     *@param info MMS-specific information derived from the users filters.
     */
    public synchronized int sendNotification(UserInfo user,
                                             UserMailbox inbox,
                                             MmsFilterInfo info,
                                             FeedbackHandler ng,
                                             NotificationEmail email) {

        MMSCenter center = getAutoMMSCenter();
        if (center == null) {
            ng.retry(user, NTF_MMS, "cannot find a valid MMS Center ");
            return 0;
        }

        if(_noOfMMSThreads < Config.getMMSMaxConnection()) {

            MMSHandler handler = new MMSHandler(user, ng, email, inbox, center, info);
            updateThreadCount(true); //Increase MMS thread count by one
            handler.start();
            return info.getNumbers().length;

        } else {
            ng.retry(user, NTF_MMS, "No more MMS connections available");
            return 0;
        }
    }

    /**
     * Get one OK MMS center in a round robin fashion based on the instances list
     * returned from the componentservices.cfg
     * @return MMSCenter
     */
    private synchronized MMSCenter getAutoMMSCenter() {
        int multiMediaSize = 0;
        Map<String, Map<String, String>> multiMediaInstances = Config.getExternalEnablers(NotificationConfigConstants.MULTIMEDIA_MESSAGE_TABLE);
        if (multiMediaInstances == null || multiMediaInstances.isEmpty()) {
            log.logMessage("No MMSc found in config", Logger.L_DEBUG);
            return null;
        }

        multiMediaSize = multiMediaInstances.size();
        if (autoIndex > multiMediaSize) {
            autoIndex = 1;
        }
        int startIndex = autoIndex;
        boolean first = true;

        /**
         * scan through the instance list in the round robin fashion to get one mms center that is OK
         */
        while (first || startIndex != autoIndex) {
            String componentName = null;
            Iterator<String> it = multiMediaInstances.keySet().iterator();
            for (int i=0; i<autoIndex; i++) {
                componentName = it.next();
            }
            autoIndex++;

            MMSCenter unit = getCenter(componentName);
            if ((unit != null) && unit.isOk()) {
                return unit;
            }

            if (autoIndex > multiMediaSize) {
                autoIndex = 1;
            }
            first = false;
        }
        return null;
    }

    /**
     *getCenter finds an MMSCenter, creating it if necessary.
     *@param name the name of the wanted MMSCenter
     *@return the MMSCenter for name.
     */
    private MMSCenter getCenter(String name) {
        if(name == null) return null;

        MMSCenter c = centers.get(name);
        if (c == null) {
            log.logMessage("Creating new MMSCenter: " + name, Logger.L_VERBOSE);
            c = new MMSCenter(name);
            if (!c.isOk()) {
                //Could not find info about this MMSc
                log.logMessage("MMSPersonFactory.getCenter could not find MMS-C" + name, Logger.L_DEBUG);
                return null;
            }
            centers.put(name, c);
        }
        return c;
    }

    /**
     * @throws TemplateMessageGenerationException
     * @throws InterruptedException 
     */
    private void sendSMTPNotification(UserInfo user, FeedbackHandler ng,
            NotificationEmail email, UserMailbox inbox, MMSCenter center, MmsFilterInfo info) throws TemplateMessageGenerationException,InterruptedException {
        
        String charSet=Config.getMimeTextCharSet(user.getPreferredLanguage());
        
        SMTPOut out = SMTPOutFactory.getInstance().createSMTPOut(center);

        Vector<String> rcto_to = getRctpTo(info);
        Vector<String> receivers = getMMSReceivers(info);
        String sender = null;
        sender = determineSender(email);

        if ( rcto_to == null || receivers == null) {
            log.logMessage("No recipients defined for MMS notification" , Logger.L_ERROR);
            return;
        }

        MMSMediaContent mmsContent = processMessage(user, email, inbox, ng, receivers,charSet);
        if (mmsContent == null ) {
            //all error handling already done
            return;
        }

        for(int i = 0; i < rcto_to.size(); i++) {

            String receiver = receivers.get(i);
            // set subject to specific language
            String subject = TextCreator.get().generateText(inbox, email, user, "mms_subject", true, null);
            if (subject!=null ) {
                subject = subject + email.getSenderPhoneNumber();
            } else {
                subject = email.getSubject();
            }

            MimeContainer msg = new MimeContainer(receiver, null, null, sender,
                    null, subject, "", null, charSet);
            if(Config.getLogLevel() == Logger.L_DEBUG) {
                log.logMessage("Sending MMS to " + receiver + " from " + sender, Logger.L_DEBUG);
                log.logMessage("Subject :"+ subject, Logger.L_DEBUG);
            }
            
            CustomHeader[] xHeaders = null;
            if(email.isUrgent()) {
                xHeaders = new CustomHeader[7];
                xHeaders[6] = new CustomHeader("X-Mms-Priority", "High");
            }
            else{
                xHeaders = new CustomHeader[6];            
            }
            
            xHeaders[0] = new CustomHeader("Date",email.getMessageReceivedDate().toString());
            xHeaders[1] = new CustomHeader("X-Mms-Transaction-ID", getMmsTransactionId());
            xHeaders[2] = new CustomHeader("X-Mms-MMS-Version", Config.getMMSVersion());
            xHeaders[3] = new CustomHeader("X-Mms-Message-Type", "m-send-req");
            xHeaders[4] = new CustomHeader("X-Mms-Expiry", getMmsExpiry(user, email));
            xHeaders[5] = new CustomHeader("X-Mms-Message-Class", "Personal");
            
            msg.setCustomHeaders(xHeaders);
            if(!email.isConfidential()){
                msg.setMultiPart(mmsContent.getAllContentsAsMimeMultiPart());
            }
            else{
                msg.setMultiPart(mmsContent.getOptionalPartAsMimeMultiPart());
            }

            msg.setRcpToAddr(rcto_to.get(i));
            
         
        
            if(Config.getWriteMMSToTempFolder()){
                //for DEBUG MMS message, write a copy to a file to check formatting.
                //LIMITED to 10. so remember to remove them if you want new ones.
                String folder = Config.getTempDir();
                               
                File dir = new File(folder);
                File[] files = dir.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.matches("^\\+{0,1}[0-9]{1,}.*\\.msg$");
                    }
                });
                
                if (files.length >= 10) {
                    log.logMessage("10 message limit for mms message in folder: " + Config.getTempDir() + " reached.", Logger.L_DEBUG);
                } else {
                    String filename = folder +
                            File.separator + NotifierUtil.get().getNormalizedTelephoneNumber(user.getTelephoneNumber()) +
                            "_" + email.getMessageId() +
                            ".msg";

                    try {
                        msg.writeTo(new FileOutputStream(filename));
                        log.logMessage("Wrote copy of MMS message for " + user.getTelephoneNumber() + " to " + filename + " remember to remove it!", Logger.L_ERROR);
                    } catch (FileNotFoundException e) {
                        log.logMessage("Unable to write a copy of msg to temporary folder: " + Config.getTempDir(), Logger.L_DEBUG);
                    }
                }
            }
            out.handleMail(msg, ng, user);
        }

        return;
    }

    private static String getAudioContentTypeForPreferedCodec() {
        String preferedCodec = Config.getMMSPreferedAudioCodec();

        if (preferedCodec.equalsIgnoreCase("amr")) {
            return("audio/amr");
        } else if (preferedCodec.equalsIgnoreCase("pcm")) {
            return("audio/wav");
        }
        // Default is "native" i.e. don't transcode
        return null;
    }

    private static String getVideoContentTypeForPreferedCodec() {
        String preferedCodec = Config.getMMSPreferedAudioCodec();

        if (preferedCodec.equalsIgnoreCase("amr")) {
            return("video/3gpp");
        } else if (preferedCodec.equalsIgnoreCase("pcm")) {
            return("video/quicktime");
        }
        // Default is "native" i.e. don't transcode
        return null;
    }

    private static String getTargetContentType( String sourceContentType ) {
        if (sourceContentType.startsWith("audio")) {
            return (getAudioContentTypeForPreferedCodec());
        } else if (sourceContentType.startsWith("video")) {
            return (getVideoContentTypeForPreferedCodec());
        }
        // if not "audio" or "video", no need to transcode
        return null;
    }

    private BinaryContent transcode(BinaryContent message) {
        String targetContentType = getTargetContentType(message.getContentType().toLowerCase());

        if ((targetContentType == null) ||
                (targetContentType.equalsIgnoreCase(message.getContentType()))) {
            // Config says don't transcode or content is already encoded using configured codec
            // return non-transcoded message
            return message;
        }

        byte[] transcodedBytes = transcoder.convertByteArray(message.getBytes(), message.getContentType(), targetContentType);

        if (transcodedBytes != null) {
            String transcodedFileName = BinaryContent.makeNewFileName(message.getPartName(), targetContentType);
            return new BinaryContent(transcodedFileName, targetContentType, transcodedBytes);
        }
        // Transcoding failed - return null
        return null;
    }

    private MMSMediaContent processMessage(UserInfo user, NotificationEmail email, UserMailbox inbox,
                                        FeedbackHandler ng, Collection<String> recipients,String charSet) {
        //do all error handling here
        MMSMediaContent mmsContent = null;
        try {
            mmsContent = buildMMSMediaContent(user, email, inbox,charSet);
        } catch (Exception e) {
            log.logMessage("Exception in buildMMSMediaContent(): " + e + " Stacktrace: " + NtfUtil.stackTrace(e),
                           Logger.L_ERROR);
            ng.failed(user, Constants.NTF_MMS, "Exception while processing MMS content");
            handleError(email, recipients);
            return null;
        }

        if (mmsContent == null ) {
            ng.failed(user, Constants.NTF_MMS, "Transcoding Error while processing MMS notif");
            handleError(email, recipients);
        } else {
            if (isMsgTooBig(mmsContent)) {
                ng.failed(user, Constants.NTF_MMS, "MMS content is too large");
                handleMsgTooBig(email, mmsContent, recipients);
            } else { //message is the right size
                return mmsContent;
            }
        }
        return null;
    }

    private MMSMediaContent buildMMSMediaContent(UserInfo user, NotificationEmail email, UserMailbox inbox,String charSet)
            throws IOException, MessagingException, MsgStoreException, TemplateMessageGenerationException {
        MMSMediaContent mmsContent = new MMSMediaContent();
        BodyPart voicePart = (BodyPart) email.getAttachmentPart(email.getDepositType());
        byte[] data = BinaryContent.readBytes(voicePart.getInputStream());
      
        BinaryContent message = new BinaryContent(voicePart.getFileName(), voicePart.getContentType(), data);
        if(!email.isConfidential()){
            long start = System.currentTimeMillis();
            BinaryContent transcodedMessage = transcode(message);
            long end = System.currentTimeMillis();

            if(transcodedMessage == null) {
                // Transcoding failed
                log.logMessage("Could not transcode content from type " + message.getContentType()
                        + " to type " + getTargetContentType(message.getContentType())
                        + ". Transcoder returned null.", Logger.L_ERROR );
                // Cannot continue processing - return null
                return null;
            }
            if(message != transcodedMessage) {
                // transcoding DID occur - notify mer
                int mSize = retrieveMessageSize(voicePart, email);
                String telephoneNumber = CommonMessagingAccess.getInstance().denormalizeNumber(user.getTelephoneNumber());
                MerAgent mer = MerAgent.get();
                mer.trascodingCompleted(telephoneNumber, message.getContentType(),
                        transcodedMessage.getContentType() , mSize, (end - start));
                // Continue processing using transcoded content
                message = transcodedMessage;
            }
        }
        mmsContent.setMessageContent(message);
        String mainMimeType = getMainMimeType(message.getContentType());

        //check if we support smil before doing all this
        if(Config.shouldUseSmil()) {
            String language = user.getPreferredLanguage();
            String cos = TextCreator.stripCosPrefix(user.getCosName());
            String brand = user.getBrand();
            log.logMessage("Try to find template for language: " + language + " cos: " + cos
                    + " type: " + mainMimeType + " brand: " + brand, Logger.L_DEBUG);
            File dir = getBestMatchTemplateDir(language,  brand,  cos, mainMimeType);
            if (dir != null) {
                log.logMessage("Found best matching template in dir: " + dir.getAbsolutePath(), Logger.L_DEBUG);
                //read all the template files
                File[] files = dir.listFiles();
                for(File f : files) {
                    if (!f.isDirectory()) {
                        if (f.getName().endsWith(".smil")) {
                            //read file
                            String smil = readSmil(f);
                            if (smil != null) {
                                mmsContent.setSmil(smil);
                            }
                        } else {
                            BinaryContent content = BinaryContent.readBinaryContent(f);
                            mmsContent.addOptionalPart(content);
                        }
                    }
                }
            } else {
                log.logMessage("No template found for language: " + language + " cos: " + cos
                        + " type: " + mainMimeType + " brand: " + brand, Logger.L_ERROR);
            }
            if(mmsContent.hasSmil()) {
                //replace all the template stuff
                if(!email.isConfidential()){
                    mmsContent.replaceInSmil("__MESSAGE__", message.getPartName());
                }
                else{ // avoid putting an empty message.amr attachment 
                    mmsContent.replaceInSmil("<audio src=\"__MESSAGE__\"/>", "");
                }
                if (mainMimeType.equals("audio")) {
                    //  I think we should remove duration from smil as it plays media for the actual duration if not present, test this
                    int messageDuration = 300;
                    try {
                        messageDuration = retrieveMessageSize(voicePart, email);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //if you use this make sure you don't put the units in the template file, ms was too low a granularity anyways
                    mmsContent.replaceInSmil("__LENGTH__", messageDuration + "s");
                }
                String sender = TextCreator.get().generateText(inbox, email, user, "mms_sender", true, null);
                mmsContent.addPartIfDefinedInSmil( "__FROM__", sender, "sender.txt",charSet);
                String date = TextCreator.get().generateText(inbox, email, user, "mms_date", true, null);
                mmsContent.addPartIfDefinedInSmil("__DATE__", date, "date.txt",charSet);
                String count = TextCreator.get().generateText(inbox, email, user, "c", true, null);
                mmsContent.addPartIfDefinedInSmil("__COUNT__", count, "count.txt",charSet);
            }
        }
        return mmsContent;
    }

    private String readSmil(File f) {
        try {
            FileInputStream is = new FileInputStream(f);
            byte[] bytes = BinaryContent.readBytes(is);
            return new String(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            log.logMessage("Error: (" + e.getMessage() + ") trying to read smil in file " + f.getAbsolutePath() , Logger.L_ERROR);
        }
        return null;
    }
    /**
     * returns the main mime type without the sub type
     * ex: audio/mp3 returns audio
     */
    private static String getMainMimeType(String fullMimeType) {
        return fullMimeType.split("/")[0];
    }

    private static File getBestMatchTemplateDir(String language, String languageExtention, String cosName, String messageType) {
        String path = null;
        File f = null;
        String defaultLangauge = Config.getDefaultLanguage();
        String root = Config.getPhraseDirectory() + "/";
        if (language == null || language.equalsIgnoreCase("")) {
            language = defaultLangauge;
        }

        if (languageExtention != null && languageExtention != "") {
            path = root + languageExtention + "_" + language + "-x-" + cosName + "/" + messageType;
            f = new File(path);
            if (f.exists()) return f;
        }

        if (languageExtention != null && languageExtention != "") {
            //if brand is set try brand + lang
            path = root + languageExtention + "_" + language + "/" + messageType;
            f = new File(path);
            if (f.exists()) return f;
        } else {
            //if brand is not set try lang + cos to find dir
            path = root + language + "-x-" + cosName + "/" + messageType;
            f = new File(path);
            if (f.exists()) return f;
        }

        path = root + language + "/" + messageType;
        f = new File(path);
        if (f.exists()) return f;

        if (language.equals(defaultLangauge)) return null;

        path = root + defaultLangauge + "/" + messageType;
        f = new File(path);
        if (f.exists()) return f;

        return null; //no match
    }

    private boolean isMsgTooBig(MMSMediaContent mmsContent) {
        long maxMsgSize = Config.getMmsMaxMsgSize(); //read max size in kBytes from config
        if (maxMsgSize <= 0) {
            return false;
        }

        long msgSize = mmsContent.getSizeOfAllContents();

        if (msgSize > (maxMsgSize * 1000) ) { //convert to kBytes
            return true;
        }
        return false;
    }

    private void handleMsgTooBig(NotificationEmail email, MMSMediaContent mmsContent, Collection<String> receivers) {
        long msgSize = mmsContent.getSizeOfAllContents();
        log.logMessage("Message size (" + msgSize + ") is greater than configured MMS Max Message Size ("
                    + Config.getMmsMaxMsgSize() * 1000 + "). Cannot send MMS notification to: " + receivers, Logger.L_ERROR);
        log.logMessage("Use fallback mechanism to send SMS error notification", Logger.L_ERROR);
        failCounter.incr(receivers.size());

        FallbackInfo fallbackInfo = new FallbackInfo("mms-msg-too-big");
        FallbackHandler.get().fallback(Constants.NTF_MMS, email.getNtfEvent(), fallbackInfo);
    }

    private void handleError(NotificationEmail email, Collection<String> receivers) {
        log.logMessage("An error occured while processing an MMS notification. Cannot send MMS notification to: "
                + receivers, Logger.L_ERROR);
        log.logMessage("Use fallback mechanism to send SMS error notification", Logger.L_ERROR);

        failCounter.incr(receivers.size());
        FallbackInfo fallbackInfo = new FallbackInfo("mms-msg-error");
        FallbackHandler.get().fallback(Constants.NTF_MMS, email.getNtfEvent(), fallbackInfo);
    }


    /**
     * @return Vector<String> a vector of numbers that should receive a MMS notification.
     */
    private Vector<String> getRctpTo(MmsFilterInfo info){
        Vector<String> to = new Vector<String>();
        if (Config.shouldUseMMSPostmaster()) {
            to.add(Config.getMMSPostMaster());
            return to;
        }

        String[] numbers = info.getNumbers();


        for( int i=0;i<numbers.length;i++ ) {
        	String rctp=numbers[i];
        	StringBuilder toRcptBuilder=new StringBuilder();
        	if ( rctp.length() == 0 || rctp.equalsIgnoreCase("?") )
        		toRcptBuilder.append(Config.getNumberToMessagingSystem()).append("/TYPE=PLMN").append("@").append(Config.getMMSSystemDomain());
        	else
        		toRcptBuilder.append("+").append(rctp).append("/TYPE=PLMN@").append( Config.getMMSSystemDomain());
            to.add( toRcptBuilder.toString() );
        }

        return to;

    }

    /**
     * @return Vector<String> a vector of numbers that should receive a MMS notification.
     */
    private Vector<String> getMMSReceivers(MmsFilterInfo info){
        Vector<String> to = new Vector<String>();

        String[] numbers = info.getNumbers();
        for( int i = 0;i<numbers.length;i++ ) {
            to.add("+" + numbers[i] + "/TYPE=PLMN" );
        }

        return to;
    }

     /**
      * Creates a From header according to RFC822
      * and the Multimedia Messaging Service Encapsulation
      * protocol (version 1.1).
      * If senders number can not be determined,
      * the general systemnumber will be used.
      *@return RFC822 header containg senders number or the number
      * to the messagingsystem.
      */
    private String determineSender(NotificationEmail email) {
    	if(email.getDepositType() == depositType.EMAIL)
    		return email.getSenderPhoneNumber();

    	String from = email.getSenderPhoneNumber();
    	if ( from.length() == 0 || from.equalsIgnoreCase("?") )
    		return Config.getNumberToMessagingSystem() + "/TYPE=PLMN" +
    		"@" + Config.getMMSSystemDomain();

    	Pattern p = Pattern.compile("^\\d+$");
    	Matcher m = p.matcher(from);
    	if( m.matches() ) {
    		// Only numbers in the from string, use the from String
    		//return "+" + from  + "/TYPE=PLMN"+ "@" + Config.getMMSSystemDomain();
    		return from + "/TYPE=PLMN"+ "@" + Config.getMMSSystemDomain();
    	} else {
    		// The from string contains something other than numbers, return the messaging system number
    		return Config.getNumberToMessagingSystem() + "/TYPE=PLMN" +
    		"@" + Config.getMMSSystemDomain() ;
    	}

    }

    /**
     * @return an updated MmsTransactionId used for SMTP mail
     */
    private synchronized String getMmsTransactionId(){
        return "--" + (_mmsid++) + "--";
    }

    //Same for SMTP and MM7
    private String getMmsExpiry(UserInfo user, NotificationEmail email){
        return user.getUsersDate(email.getMessageReceivedDate());
    }

    //MM7 STARTS HERE

    /**
     * Uses the MM7 api to send a MM7 SubmitRequest to a MM7 enabled MMS-C
     */
    private void sendMM7Notification(UserInfo user,
                                    FeedbackHandler ng,
                                    NotificationEmail email,
                                    UserMailbox inbox,
                                    MMSCenter center,
                                    MmsFilterInfo info) {

        try {
            String url = "http://" + center.getHost() + ":" + center.getPort() + "/" + center.getUri();
            if(Config.getLogLevel() == Logger.L_DEBUG) log.logMessage("MM7 URL " + url, Logger.L_DEBUG);
            String[] numbers = info.getNumbers();
            if(numbers == null || numbers.length == 0) {
                log.logMessage("No recipients defined for MMS notification" , Logger.L_ERROR);
                return;
            }
            
            String charSet=Config.getMimeTextCharSet(user.getPreferredLanguage());
            MMSMediaContent mmsContent = processMessage(user, email, inbox, ng, Arrays.asList(numbers), charSet);
            if (mmsContent == null ) {
                //all error handling already done
                return;
            }

            MultiMediaApplication app = new MultiMediaApplication();
            app.setApplicationName(Config.getMmscVasId());
            app.setApplicationProviderName(Config.getMmscVaspId());
            //I think these are optional
            String userName = Config.getMmscUser();
            if (userName != null && !userName.equals("")) {
                app.setUserName(userName);
                app.setPassword(Config.getMmscPassword());
            }

            MultiMediaMessage msg = new MultiMediaMessage();
            String subject = TextCreator.get().generateText(inbox, email, user, "mms_subject", true, null);
            subject = subject + email.getSenderPhoneNumber();
            //msg.setSubject(email.getSubject());
            msg.setSubject(subject);
            //don't set expiry date
            msg.setMessageClass(MessageClass.PERSONAL);
            addRecipients(msg, info);
            setMM7SenderAddress(msg, email);
            
            if(email.isUrgent()){
                msg.setPriority(Priority.HIGH);
            }
            
            if (mmsContent.hasSmil()) {
                msg.setSmil(mmsContent.getSmilAsMimeBodyPart());
                //add optional parts if any
                for(ContentPart p : mmsContent.getOptionalParts()) {
                    //msg.addBodyPart(p.getMimeBodyPart()); - this does not work is something wrong in the mime body parts? or is this a bug in the vasp sdk
                    //cmpmgr used the addAttachment method
                    msg.addAttachment(new ByteArrayInputStream(p.getBytes()), p.getPartName(), p.getContentType());
                }
            }
             if(!email.isConfidential()){ 
                //add actual vms message
                BinaryContent message = mmsContent.getMessageContent();
                //MimeBodyPart body = new MimeBodyPart();
                //body.setContent(message.getBytes(), message.getContentType()); -- this does not work either seems to be a bug in the vasp sdk
                //msg.addBodyPart(body);
                msg.addAttachment(new ByteArrayInputStream(message.getBytes()), message.getPartName(), message.getContentType());
             }
            MultiMediaController mmc = new MultiMediaController();
            MultiMediaSubmitResponse response = mmc.submitMsg(new URL(url), app, msg);

            if(response.isSuccess()) {
                ng.ok(user, NTF_MMS);
                successCounter.incr();
                if(Config.getLogLevel() == Logger.L_DEBUG) {
                    log.logMessage("Successfully submited message to MMC via MM7", Logger.L_DEBUG);
                }
                return;
            } else {
                ng.failed(user, NTF_MMS, "MM7 Submit failed");
                failCounter.incr();
                log.logMessage("MM7 Error: " + response.getResponseStatus().getStatusCode()
                            + " - " + response.getResponseStatus().getStatusText(), Logger.L_ERROR);
                return;
            }
        } catch(java.net.MalformedURLException urlx) {
            failCounter.incr();
            ng.retry(user, NTF_MMS, "MalformedURLException in sendMM7Notification(): " + urlx);
            return;
        } catch(MessagingException mex) {
            ng.failed(user, NTF_MMS, "MessagingException in sendMM7Notification(): " + mex);
            failCounter.incr();
            return;
        } catch(IOException iox) {
            ng.failed(user, NTF_MMS, "IOException in sendMM7Notification(): " + iox);
            failCounter.incr();
            ManagementInfo.get().getStatus(NotificationConfigConstants.MULTIMEDIA_MESSAGE_TABLE, center.getName()).down();
            return;
        } catch (TemplateMessageGenerationException e) {
            log.logMessage("TemplateMessageGenerationException received in sendMM7Notification.", Logger.L_ERROR);
        }
    }



    private void addRecipients(MultiMediaMessage msg, MmsFilterInfo info) {
        for(String recipient :  info.getNumbers()) {
            msg.addDestinationTO(recipient);
        }
    }

    /**
     * Corresponds to determineSender in SMTP
     */
    private void setMM7SenderAddress(MultiMediaMessage msg, NotificationEmail email) {
        //only set from if sender is visible
        String from = email.getSenderPhoneNumber();
        if(!email.getSenderVisibile() || from.length() == 0 || from.equalsIgnoreCase("?")) {
            log.logMessage("Use Number to Messaging system as From", Logger.L_DEBUG);
            from = Config.getNumberToMessagingSystem();
        }
        msg.setFrom(new Number(from));
    }

    /**
     * Updates the MMS thread count variable
     * @param increase boolean true if increasing the count, false if decreasing the count
     */
    private synchronized void updateThreadCount(boolean increase) {
        if(increase) _noOfMMSThreads++;
        else _noOfMMSThreads--;

        if(Config.getLogLevel() == Logger.L_DEBUG) {
            log.logMessage("No of MMS threads active:" + _noOfMMSThreads, Logger.L_DEBUG);
        }
    }
    
    /**
     * Utility method to retrieve a message size.
     * <p>
     * If the message is audio, it tries to retrieve the message size from the body part.
     * When the content duration is not available in the body part, it retrieves the size
     * from the email.
     * </p>
     * <p>
     * For all the other cases, the message size is retrieved from the email.
     * </p>
     *  
     * @param msgPart MIME part of the message.
     * @param email E-Mail message.
     * @return Size of the message.
     * 
     * @throws MessagingException
     * @throws MsgStoreException
     * @throws IOException
     */
    private static int retrieveMessageSize(BodyPart msgPart, NotificationEmail email) 
    throws MessagingException, MsgStoreException, IOException {
        
        int size = 0;
        String length = null;
        if (msgPart.getContentType().startsWith("audio")) {
            String[] headers = msgPart.getHeader("Content-Duration");
            if (headers.length > 0) {
                length = headers[0];
            }
        }
        
        if (length == null) {
            length = email.getMessageLength();
        }
        
        try {
            size = Integer.parseInt(length);
        } catch (NumberFormatException e) {
            log.logMessage("MessagingException in retrieveMessageSize(): " + e, Logger.L_DEBUG);
        }
        return size;
    }

    /**
     * Threaded class that sends a MMS notification in one thread and then
     * finishes.
     */
    class MMSHandler extends Thread {

        private UserInfo _user;
        private FeedbackHandler _ng;
        private UserMailbox _inbox;
        private NotificationEmail _email;
        private MMSCenter _center;
        private MmsFilterInfo _info;

        public MMSHandler(UserInfo user,
                          FeedbackHandler ng,
                          NotificationEmail email,
                          UserMailbox inbox,
                          MMSCenter center,
                          MmsFilterInfo info) {

            super("MMSHandler-" + _mmsThreadID++);
            //log.logMessage("**IN MMSHandler UID" + ng.getEmail().getMessageUID() +
            //    " user " + user.getTelephoneNumber(), log.L_DEBUG);
            _user = user;
            _ng = ng;
            _email = email;
            _inbox = inbox;
            _center = center;
            _info = info;
       }

        public void run() {
            if(_center.getProtocol().equalsIgnoreCase("http")) {
                //This message could be removed
                if(Config.getLogLevel() == Logger.L_DEBUG) {
                    log.logMessage("Sending MMS to a MM7 server...", Logger.L_DEBUG);
                }
                sendMM7Notification(_user, _ng, _email, _inbox, _center, _info);

            } else {
                //This message could be removed
                if(Config.getLogLevel() == Logger.L_DEBUG) {
                    log.logMessage("Sending MMS to a SMTP server...", Logger.L_DEBUG);
                }
                try {
                    sendSMTPNotification(_user, _ng, _email,_inbox, _center, _info);
                } catch (TemplateMessageGenerationException e) {
                    log.logMessage("TemplateMessageGenerationException received in sendSMTPNotification.", Logger.L_ERROR);
                } catch (InterruptedException i) {
                    log.logMessage("Failed to send MM3 notification, shutting down", Logger.L_ERROR);
                    return;
                } catch (Throwable t) {
                    log.logMessage("Failed to send MM3 notification, due to exception: " + NtfUtil.stackTrace(t), Logger.L_ERROR);
                }
            }
            updateThreadCount(false);
            if(Config.getLogLevel() == Logger.L_DEBUG) {
                log.logMessage("MMS thread " + getName() + " is finished", Logger.L_DEBUG);
            }
        }
    }

}

