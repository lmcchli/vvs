/*
 * Copyright (c) 2006, Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mailbox.*;

import static com.mobeon.masp.mailbox.MailboxMessageType.*;
import static com.mobeon.masp.mailbox.javamail.ConfidentialHeaderUtil.HEADERNAME_SENSITIVITY;
import static com.mobeon.masp.mailbox.javamail.ConfidentialHeaderUtil.HEADERNAME_X_SENSITIVITY;
import static com.mobeon.masp.mailbox.javamail.ContentDispositionHeaderUtil.HEADERNAME_CONTENT_DISPOSITION;
import static com.mobeon.masp.mailbox.javamail.ContentDispositionHeaderUtil.ORIGINATOR_SPOKEN_NAME_PATTERN;
import static com.mobeon.masp.mailbox.javamail.JavamailFlags.*;
import static com.mobeon.masp.mailbox.javamail.LanguageHeaderUtil.HEADERNAME_LANGUAGE;
import static com.mobeon.masp.mailbox.javamail.UrgentHeaderUtil.*;
import static com.mobeon.masp.mailbox.util.ContentTypePatterns.*;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.common.message_sender.SmtpOptions;
import com.mobeon.common.util.FaxNumber;
import com.mobeon.common.util.FaxPrintStatus;
import com.mobeon.masp.util.javamail.MessageUtil;

import jakarta.mail.Address;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.text.*;

/**
 */
public class JavamailMessageAdapter extends BaseStoredMessage<JavamailContext> implements IStoredMessage {

    private static final ILogger log = ILoggerFactory.getILogger(JavamailMessageAdapter.class);

    private Message adaptedMessage;
    private MimePart spokenNameOfSenderPart;
    private JavamailFolderAdapter folderAdapter;

    private static final AtomicReference<FetchProfile> atomicStoredMessageProfile = new AtomicReference<FetchProfile>();
    private static final MailDateFormat MAIL_DATE_FORMAT = new MailDateFormat();
    private static final ParsePosition PARSE_POSITION_ZERO = new ParsePosition(0);
    private static final String HEADERNAME_RECEIVED = "Received";

    JavamailMessageAdapter(Message message, JavamailContext context, JavamailFolderAdapter folderAdapter) {
        super(context);
        this.adaptedMessage = message;
        this.folderAdapter = folderAdapter;
        if(adaptedMessage.getFolder() != folderAdapter.folder) {
            throw new IllegalArgumentException("Adapted message's Folder must be the same as the folderAdapter's adapted Folder!");
        }
    }

    public static FetchProfile getStoredMessageProfile(JavamailContext context) {
        if(atomicStoredMessageProfile.get() == null) {
            FetchProfile storedMessageProfile = new FetchProfile();
            storedMessageProfile.add(FetchProfile.Item.ENVELOPE);
            storedMessageProfile.add(FetchProfile.Item.CONTENT_INFO);
            storedMessageProfile.add(FetchProfile.Item.FLAGS);
            storedMessageProfile.add(HEADERNAME_IMPORTANCE);
            storedMessageProfile.add(HEADERNAME_PRIORITY);
            storedMessageProfile.add(HEADERNAME_X_PRIORITY);
            storedMessageProfile.add(HEADERNAME_X_SENSITIVITY);
            storedMessageProfile.add(HEADERNAME_SENSITIVITY);
            storedMessageProfile.add(HEADERNAME_LANGUAGE);
            storedMessageProfile.add(HEADERNAME_RECEIVED);

            for(String field : context.getConfig().getAdditionalPropertyMap().values()) {
                storedMessageProfile.add(field);
            }
            atomicStoredMessageProfile.set(storedMessageProfile);
        }
        return atomicStoredMessageProfile.get();
    }

    void parseMessage() throws MessagingException, IOException {

        if (log.isDebugEnabled()) log.debug("Parsing message " + this);

        MessageUtil.enableStorageFlagKeeping(adaptedMessage);

        Message storedMessage = adaptedMessage;

        receivedDate = parseReceivedDate();

        if ((DELIVERY_REPORT_PATTERN.matcher(adaptedMessage.getContentType()).matches()) ||
            ((DELIVERY_REPORT_MULTIPART_PATTERN.matcher(adaptedMessage.getContentType()).matches()) &&
             (DELIVERY_REPORT_REPORT_TYPE_PATTERN.matcher(adaptedMessage.getContentType()).matches()))) {
            if (log.isDebugEnabled()) log.debug(this + " is a Delivery report.");
            deliveryStatus = DeliveryStatus.STORE_FAILED;
            String finalRecipient = null;
            Multipart multipart = (Multipart) adaptedMessage.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                String bodyPartContentType = bodyPart.getContentType();
                if (DELIVERY_STATUS_PART_PATTERN.matcher(bodyPartContentType).matches()) {
                    Object bodyPartContent = bodyPart.getContent();
                    if (bodyPartContent instanceof InputStream) {
                        BufferedReader br = new BufferedReader(new InputStreamReader((InputStream) bodyPartContent));
                        String deliveryStatusField = br.readLine();
                        while (deliveryStatusField != null && finalRecipient == null) {
                            if (log.isDebugEnabled() && deliveryStatusField.length() > 0) {
                                log.debug(this + ": Delivery Status Field: " + deliveryStatusField);
                            }
                            if (FINAL_RECIPIENT_FIELD_PATTERN.matcher(deliveryStatusField).matches()) {
                                finalRecipient = deliveryStatusField.replaceFirst(FINAL_RECIPIENT_FIELD_HEAD_PATTERN_STRING, "").trim();
                                if (finalRecipient.startsWith("FAX=")) {
                                    deliveryStatus = DeliveryStatus.PRINT_FAILED;
                                }
                            }
                            deliveryStatusField = br.readLine();
                        }
                        if (log.isDebugEnabled()) log.debug(this + ": Final-Recipient=" + finalRecipient);
                    }
                } else if (RFC822_MESSAGE_PATTERN.matcher(bodyPartContentType).matches()) {
                    Object bodyPartContent = bodyPart.getContent();
                    if (bodyPartContent instanceof Message) {
                        storedMessage = (Message) bodyPartContent;
                        if (finalRecipient != null && finalRecipient.startsWith("FAX=")) {
                            storedMessage = findPrintedOriginalMessage(storedMessage);
                        }
                    } else {
                        log.debug("Bounced message RFC822 BodyPart is not an instance of " + Message.class.getName());
                    }
                }
            }
            if (log.isDebugEnabled()) log.debug(this + ": Reports " + deliveryStatus);
        } else {
            deliveryStatus = null;
        }

        parseBodyPart(storedMessage, storedMessage);

        if (spokenNameOfSenderPart != null) {
            spokenNameOfSender = new JavamailPartAdapter(spokenNameOfSenderPart, getContext(),folderAdapter);
        } else {
            if (log.isDebugEnabled()) log.debug("No Spoken Name of Sender found for " + this);
        }

        subject = storedMessage.getSubject();
        sender = extractAddressString(storedMessage.getFrom());
        replyToAddress = extractAddressString(storedMessage.getReplyTo());
        recipients = extractAddressStrings(storedMessage.getRecipients(Message.RecipientType.TO));
        secondaryRecipients = extractAddressStrings(storedMessage.getRecipients(Message.RecipientType.CC));
        state = readState(adaptedMessage);
        type = readType(storedMessage);
        urgent = UrgentHeaderUtil.isUrgent(storedMessage);
        confidential = ConfidentialHeaderUtil.isConfidential(storedMessage);
        language = parseLanguage(storedMessage);

        Map<String, String> additionalPropertyMap = getContext().getConfig().getAdditionalPropertyMap();
        for (String name : additionalPropertyMap.keySet()) {
            String field = additionalPropertyMap.get(name);
            String[]headers = storedMessage.getHeader(field);
            if (headers != null && headers.length > 0) {
                additionalProperties.put(name, headers[0]);
            }
        }

    }

    private Date parseReceivedDate() throws MessagingException {
        String[] headers = adaptedMessage.getHeader(HEADERNAME_RECEIVED);
        if (headers != null) {
            if (headers.length > 0) {
                HeaderTokenizer headerTokenizer = new HeaderTokenizer(headers[0]);
                // Find ";" token
                //noinspection StatementWithEmptyBody
                while (headerTokenizer.next().getType() != ';');
                String remainder = headerTokenizer.getRemainder();
                return MAIL_DATE_FORMAT.parse(remainder, PARSE_POSITION_ZERO);
            }
        } else {
            log.debug("getHeader(\"Received\") returns null");
        }
        return null;
    }


    static String parseLanguage(Message message) throws MessagingException {

        StringBuffer sb = new StringBuffer();
        String[] langHeaders = message.getHeader(HEADERNAME_LANGUAGE);
        if (langHeaders != null) {
            for (int i = 0; i < langHeaders.length; i++) {
                sb.append(langHeaders[i]);
                if (i + 1 < langHeaders.length) {
                    sb.append(", ");
                }
            }
        }
        return sb.toString();
    }

    static String extractAddressString(Address[] addresses) {
        String[] tmp = extractAddressStrings(addresses);
        if (tmp.length > 0) {
            return tmp[0];
        } else {
            return null;
        }
    }


    static String[] extractAddressStrings(Address[] addresses) {
        if (addresses == null) {
            return new String[0];
        }
        String[] addressStrings = new String[addresses.length];
        for (int i = 0; i < addresses.length; i++) {
            if (addresses[i] instanceof InternetAddress) {

                InternetAddress inetAddr = (InternetAddress) addresses[i];

                StringBuilder sb = new StringBuilder();
                if (inetAddr.getPersonal() != null) {
                    sb.append(inetAddr.getPersonal());
                }
                sb.append(" <");
                if (inetAddr.getAddress() != null && com.mobeon.masp.mailbox.Address.EMAILADDRESS_PATTERN.matcher(inetAddr.getAddress()).matches()) {
                    sb.append(inetAddr.getAddress());
                }
                sb.append(">");
                addressStrings[i] = sb.toString().trim();
                if (log.isDebugEnabled()) log.debug("'" + addresses[i] + "' was parsed to '" + addressStrings[i] + "'");
            } else {
                if (log.isDebugEnabled()) log.debug(addresses[i] + " is not an Internet email address!");
                addressStrings[i] = addresses[i].toString();
            }
        }
        return addressStrings;
    }


    /**
     * Returns the spoken name of sender.
     *
     * @return the spoken name of sender as a media object. (If exists)
     * @throws com.mobeon.masp.mailbox.SpokenNameNotFoundException
     *          if no spoken name is set.
     * @throws com.mobeon.masp.mailbox.MailboxException
     *
     */
    public IMediaObject getSpokenNameOfSender() throws MailboxException {
        if (log.isInfoEnabled()) log.info("getSpokenNameOfSender()");
        if (log.isDebugEnabled()) log.debug("Getting spokenNameOfSender for " + this);
        IMediaObject result = null;
        if (spokenNameOfSender != null) {
            try {
                getContext().getMailboxLock().lock();
                result = spokenNameOfSender.getMediaObject();
            } finally {
                getContext().getMailboxLock().unlock();
            }
        }
        if (log.isInfoEnabled()) log.info("getSpokenNameOfSender() returns " + result);
        return result;
    }

    private void parseBodyPart(Part part, Message topMessage) throws MessagingException, IOException {

        if (log.isDebugEnabled()) log.debug("Parsing body part " + this);

        if (part instanceof Message) {
            MessageUtil.enableStorageFlagKeeping((Message) part);
        }

        if (content == null) {
            content = new ArrayList<IMessageContent>();
        }

        if (MULTIPART_MESSAGE_PATTERN.matcher(part.getContentType()).matches()) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                parseBodyPart(multipart.getBodyPart(i), topMessage);
            }
        } else if ((RFC822_MESSAGE_PATTERN.matcher(part.getContentType()).matches()) &&
                  !(RFC822_HEADERS_PATTERN.matcher(part.getContentType()).matches())) {
            Part message = (Part) part.getContent();
            forward = true;
            parseBodyPart(message, topMessage);
        } else {
            String[] contentDisposition = part.getHeader(HEADERNAME_CONTENT_DISPOSITION);
            if (contentDisposition != null && contentDisposition.length > 0 && ORIGINATOR_SPOKEN_NAME_PATTERN.matcher(contentDisposition[0]).matches()) {
                //Is a spoken name bodypart
                if (part instanceof MimeBodyPart) {
                    MimeBodyPart mimeBodyPart = (MimeBodyPart) part;
                    if (mimeBodyPart.getParent().getParent() == topMessage) {
                        //Is a bodypart of the top message.
                        spokenNameOfSenderPart = mimeBodyPart;
                    }
                }
            } else {
                JavamailPartAdapter partAdapter = new JavamailPartAdapter((MimePart)part, getContext(),folderAdapter);
                content.add(partAdapter);
            }
        }
    }

    /**
     * Prints the message att the givemn destination.
     *
     * @param destination address.
     * @param sender      address
     * @throws com.mobeon.masp.mailbox.MailboxException
     *
     */
    public void print(String sender, FaxNumber destination, boolean autoprint) throws MailboxException {
        if (log.isInfoEnabled()) log.info("print(destination=" + destination + ",sender=" + sender + ")");
        if (log.isDebugEnabled())
            log.debug("Print " + this + " to \"" + destination + "\" from \"" + sender + "\"");
        if (destination == null || destination.toString().length() == 0)
            throw new IllegalArgumentException("<destination> cannot be null or empty!");
        if (sender == null || sender.length() == 0)
            throw new IllegalArgumentException("<sender> cannot be null or empty!");

        JavamailForwardMessage forwardMessage = new JavamailForwardMessage(getContext(), adaptedMessage);
        forwardMessage.setRecipients("FAX=" + destination + "@mfc");
        forwardMessage.setSender(sender);
        forwardMessage.setReplyToAddress(getContext().getMailboxProfile().getEmailAddress());
        forwardMessage.setSubject(getSubject());
        forwardMessage.setType(getType());
        forwardMessage.setSmtpOptions(new SmtpOptions());
        forwardMessage.getSmtpOptions().setEnvelopeFrom(sender);
        forwardMessage.store();
        if (log.isInfoEnabled()) log.info("print(String,String) returns void");
    }
    
    /**
     * Dummy implementation
     */
    public FaxPrintStatus getFaxPrintStatus() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a storeable message with this message as content.
     *
     * @return storable message.
     * @throws com.mobeon.masp.mailbox.MailboxException
     *
     */
    public JavamailForwardMessage forward() throws MailboxException {
        if (log.isInfoEnabled()) log.info("forward()");
        if (log.isDebugEnabled()) log.debug("Forward " + this);
        JavamailForwardMessage javamailForwardMessage = new JavamailForwardMessage(getContext(), adaptedMessage);
        if (log.isInfoEnabled()) log.info("forward() returns " + javamailForwardMessage);
        return javamailForwardMessage;
    }

    /**
     * Copy this message into the specified Folder.
     * This operation appends this Message to the destination Folder.
     *
     * @param folder target folder.
     * @throws com.mobeon.masp.mailbox.MailboxException
     *          if a problem occurs.
     */
    public void copy(IFolder folder) throws MailboxException {
        if (log.isInfoEnabled()) log.info("copy(" + folder + ")");
        if (log.isDebugEnabled()) log.debug("Copy " + this + " to " + folder);
        if (folder instanceof JavamailFolderAdapter) {
            try {
                JavamailFolderAdapter jfa = (JavamailFolderAdapter) folder;
                getContext().getMailboxLock().lock();
                folderAdapter.open();
                adaptedMessage.getFolder().copyMessages(new Message[]{adaptedMessage}, jfa.folder);
            } catch (MessagingException e) {

                MailboxException e2 = new MailboxException("Could not copy " + this + " to " + folder + ": " + e.getMessage()+". URL: "+getURL());
                log.debug(e2.getMessage(), e);
                throw e2;
            } finally {
                getContext().getMailboxLock().unlock();
            }
        } else {
            ClassCastException e = new ClassCastException("Could not cast " + folder.getClass()
                    .getName() + " to an " + JavamailFolderAdapter.class.getName());
            log.error(e.getMessage());
            throw e;
        }
        if (log.isInfoEnabled()) log.info("copy(IFolder) returns void");
    }

    /**
     * Save any changes made to this message into the message-store.
     *
     * @throws com.mobeon.masp.mailbox.MailboxException
     *
     */
    public void saveChanges() throws MailboxException {
        if (log.isInfoEnabled()) log.info("saveChanges()");
        if (log.isDebugEnabled()) log.debug("Saving changes for " + this);
        try {
            getContext().getMailboxLock().lock();
            saveState();
            //adaptedMessage.saveChanges(); //Not supported for org.eclipse.angus.mail.imap.IMAPMessage todo undersök
        } catch (MessagingException e) {
            throw new MailboxException("Could not save message changes. URL: "+getURL(), e);
        } finally {
            getContext().getMailboxLock().unlock();
        }
        if (log.isInfoEnabled()) log.info("saveChanges() returns void");
    }

	public void saveChangesForRecycle(StoredMessageState prioryState, int maxNumberMsgUndelete, int daysToExpire)  throws MailboxException {
		throw new MailboxException("method not implemented in JavamailMessageAdapter");
	}

    private URLName getURL() {
        return folderAdapter.folder.getStore().getURLName();
    }


    private void saveState() throws MessagingException {
        setFlags(adaptedMessage, state);
    }

    private static void setFlags(Message message, StoredMessageState state) throws MessagingException {

        Flags setFlags = new Flags();
        Flags notSetFlags = new Flags();

        switch (state) {
            case NEW:
                //message.setFlags(NEW_NOT_SET_FLAGS,false);
                notSetFlags = NEW_NOT_SET_FLAGS;
                break;
            case DELETED:
                //message.setFlags(DELETED_SET_FLAGS,true);
                setFlags = DELETED_SET_FLAGS;
                break;
            case SAVED:
                //message.setFlags(SAVED_NOT_SET_FLAGS,false);
                //message.setFlags(SAVED_SET_FLAGS,true);
                setFlags = SAVED_SET_FLAGS;
                notSetFlags = SAVED_NOT_SET_FLAGS;
                break;
            case READ:
                //message.setFlags(READ_NOT_SET_FLAGS,false);
                //message.setFlags(READ_SET_FLAGS,true);
                setFlags = READ_SET_FLAGS;
                notSetFlags = READ_NOT_SET_FLAGS;
                break;
        }

        //Unset flags
        Iterator<Flags.Flag> currentSystemFlags = Arrays.asList(message.getFlags().getSystemFlags()).iterator();
        boolean unsetFlagsDone = false;
        while (currentSystemFlags.hasNext() && !unsetFlagsDone) {
            if (notSetFlags.contains(currentSystemFlags.next())) {
                message.setFlags(notSetFlags, false);
                unsetFlagsDone = true;
            }
        }
        Iterator<String> currentUserFlags = Arrays.asList(message.getFlags().getUserFlags()).iterator();
        while (currentUserFlags.hasNext() && !unsetFlagsDone) {
            if (notSetFlags.contains(currentUserFlags.next())) {
                message.setFlags(notSetFlags, false);
                unsetFlagsDone = true;
            }
        }

        //Set flags
        Iterator<Flags.Flag> systemFlags = Arrays.asList(setFlags.getSystemFlags()).iterator();
        boolean setFlagsDone = false;
        while (systemFlags.hasNext() && !setFlagsDone) {
            if (!message.getFlags().contains(systemFlags.next())) {
                message.setFlags(setFlags, true);
                setFlagsDone = true;
            }
        }
        Iterator<String>userFlags = Arrays.asList(setFlags.getUserFlags()).iterator();
        while (userFlags.hasNext() && !setFlagsDone) {
            if (!message.getFlags().contains(userFlags.next())) {
                message.setFlags(setFlags, true);
                setFlagsDone = true;
            }
        }


    }


    /**
     * Renews date when message was "issued" to now.
     * Implementation should have an internal date used as date when the message was issued.
     * Application may want to renew issued date to extend the message rentention time.
     * Initially issued date is equal or near equal to received date.
     * Different to issued date received date can not be changed.
     *
     * @throws com.mobeon.masp.mailbox.MailboxException
     *          if a problem occurs.
     */
    public void messageSetExpiryDate(String expiryDate) throws MailboxException {

    }

    public static StoredMessageState readState(Message message) throws MessagingException {

        Flags flags = message.getFlags();
        if (flags.contains(DELETED_SET_FLAGS)) {
            return StoredMessageState.DELETED;
        } else {
            if (flags.contains(SAVED_SET_FLAGS)) {
                return StoredMessageState.SAVED;
            } else {
                if (flags.contains(READ_SET_FLAGS)) {
                    return StoredMessageState.READ;
                } else {
                    return StoredMessageState.NEW;
                }
            }
        }
    }

    public static MailboxMessageType readType(Message message) throws MessagingException {
        String contentType = message.getContentType();
        MailboxMessageType result;
        if (VOICE_MESSAGE_PATTERN.matcher(contentType).matches()) {
            result = VOICE;
        } else if (VIDEO_MESSAGE_PATTERN.matcher(contentType).matches()) {
            result = VIDEO;
        } else if (FAX_MESSAGE_PATTERN.matcher(contentType).matches()) {
            result = FAX;
        } else {
            result = EMAIL;
        }
        return result;
    }

    public String toString() {

        Folder folder = adaptedMessage.getFolder();
        if(folder != null) {
            StringBuffer sb = new StringBuffer();
            Store store = folder.getStore();
            sb.append(store.getURLName());
            sb.append("/");
            sb.append(folder);
            sb.append("/Message[").append(adaptedMessage.getMessageNumber()).append("]");
            return sb.toString();
        } else {
            return super.toString();
        }

    }


    private static final String FINAL_RECIPIENT_FIELD_HEAD_PATTERN_STRING = "[Ff][Ii][Nn][Aa][Ll]-[Rr][Ee][Cc][Ii][Pp][Ii][Ee][Nn][Tt]:.*;";

    private static final Pattern FINAL_RECIPIENT_FIELD_PATTERN =
            Pattern.compile(FINAL_RECIPIENT_FIELD_HEAD_PATTERN_STRING + ".*", Pattern.DOTALL);


    private static Message findPrintedOriginalMessage(Message message) throws MessagingException, IOException {
        MessageUtil.enableStorageFlagKeeping(message);
        if (MULTIPART_MESSAGE_PATTERN.matcher(message.getContentType()).matches()) {
            Multipart multipart = (Multipart) message.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (RFC822_MESSAGE_PATTERN.matcher(bodyPart.getContentType()).matches()) {
                    return (Message) bodyPart.getContent();
                }
            }
        }
        return message;
    }

    @Override
    public void setMessageAccessPoint(String accessPoint) {
    }

	@Override
	public String getBroadcastLanguage() {
		// TODO Auto-generated method stub
		return null;
	}

}
