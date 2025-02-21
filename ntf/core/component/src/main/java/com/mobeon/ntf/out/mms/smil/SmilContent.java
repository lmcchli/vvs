package com.mobeon.ntf.out.mms.smil;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import com.abcxyz.messaging.common.util.converters.Amr3gpConverter;
import com.abcxyz.messaging.common.util.converters.ConverterException;
import com.mobeon.ntf.Config;
import com.mobeon.ntf.Constants;
import com.mobeon.ntf.mail.NotificationEmail;
import com.mobeon.ntf.mail.UserMailbox;
import com.mobeon.ntf.out.mediaconversion.ConversionResult;
import com.mobeon.ntf.out.mediaconversion.MCData;
import com.mobeon.ntf.out.mediaconversion.MediaConversionOut;
import com.mobeon.ntf.text.TextCreator;
import com.mobeon.ntf.userinfo.UserInfo;
import com.mobeon.ntf.util.FileName;
import com.mobeon.ntf.util.Logger;
import com.mobeon.ntf.util.NtfUtil;
import com.mobeon.ntf.util.PartParser;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.MimeType;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

/**
 * @deprecated
 *
 */
public class SmilContent implements Constants {
    /**
     * the mediaconversionout object to use
     */
    private static MediaConversionOut mediaOut;

    private String doc = null;
    /* Debug and error log **/
    private final static Logger log = Logger.getLogger(SmilContent.class);
    /**
     * Information about the user that shall be notified.
     */
    private UserInfo user = null;
    /**
     * Information about the notification.
     */
    private NotificationEmail email = null;
    /**
     * Cached information
     */
    private MimeMultipart multipart = null;
    /**
     * TextCreator needs it in order to generate a template for count.
     */
    private UserMailbox inbox = null;

    /**
     * Creates a new instance of SmilContent
     */
    protected SmilContent(UserInfo user, NotificationEmail email,
                          UserMailbox inbox,
                          String smildoc,
                          MimeMultipart template) {
        if (mediaOut == null) {
            mediaOut = new MediaConversionOut();
        }
        this.user = user;
        this.email = email;
        this.inbox = inbox;
        this.doc = smildoc;
        this.multipart = template;
    }

    /*
    * Creates a new MimeMultipart object containg bodyparts (new bodyparts
    * and bodyparts stored in the template) needed to send a smil based
    * Voice to MMS notification.<BR><BR>
    * New BodyParts can be created, depending of the content of the smildoc.
    * The new BodyParts contains user specific infromation such as: <BR><BR>
    *
    * If the smildoc contains a __MESSAGE__ tag it is replaced with message.amr
    * and a new bodypart is created and added to the MimeMultipart object. <BR>
    * Content-Type: audio/amr <BR>
    * Content-Transfer-Encoding: Base64 <BR>
    * Content-ID: message.amr <BR>
    * Content-Location: message.amr<BR>
    * ......Base64 data...... <BR><BR>
    *
    * If the smildoc contains a __FROM__ tag it is replaced with sender.txt
    * and a new bodypart is created and added to the MimeMultipart object. <BR>
    * Content-Type: text/plain <BR>
    * Content-Transfer-Encoding: quoted-printable <BR>
    * Content-ID: sender.txt <BR>
    * Content-Location: sender.txt<BR>
    * From: +467011122233 <BR><BR>
    *
    * If the smildoc contains a __DATE__ tag it is replaced with date.txt
    * and a new bodypart is created and added to the MimeMultipart object
    * with the following content<BR>
    * Content-Type: text/plain <BR>
    * Content-Transfer-Encoding: quoted-printable <BR>
    * Content-ID: date.txt <BR>
    * Content-Location: date.txt<BR>
    * Received: 2003-12-12 16:40 <BR><BR>
    *
    * If the smildoc contains a __COUNT__ tag it is replaced with count.txt
    * and a new bodypart is created and added to the MimeMultipart object
    * with the following content<BR>
    * Content-Type: text/plain <BR>
    * Content-Transfer-Encoding: quoted-printable <BR>
    * Content-ID: count.txt <BR>
    * Content-Location: count.txt<BR>
    * You have 6 new voice messages.<BR><BR>
    *
    * If the smildoc contains a __LENGTH__ tag it is replaced with the length
    * of the transcoded message. E.g.<BR>
    * &lt;par dur="__LENGTH__"&gt; is replaced with &lt;par dur="20s"&gt; <BR><BR>
    *
    * @param user information about the user that shall be notified.
    * @param email all information about the notification.
    * @param inbox TextCreator needs it in order to generate a text template.
    * @param smildoc the smil template.
    * @param template bodyparts that should be included in the notification.
    * @return a MimeMultipart containg the content needed to send a
    * smil based Voice to MMS notification.
    **/
    public MimeMultipart createContent() {
        try {

            if (replace("__FROM__", "sender.txt"))
                addSender();

            if (replace("__DATE__", "date.txt"))
                addDate();

            if (replace("__COUNT__", "count.txt"))
                addCount();

            addMessage();

            addSmilDoc();

            return multipart;
        } catch (MessagingException e) {
            log.logMessage("Messaging exception creating content, " + e.toString(), Logger.L_ERROR);
            return null;
        } catch (Exception e) {
            log.logMessage("Unknown exception creating content, " + NtfUtil.stackTrace(e).toString(), Logger.L_ERROR);
            return null;
        }
    }

    /**
     * Adds a bodypart to the multipart object containg the parsed and modified smildocument.
     */
    private void addSmilDoc() throws MessagingException, Exception {
        MimeBodyPart smil_part = new MimeBodyPart();
        smil_part.setText(doc);
        smil_part.removeHeader("Content-Type");
        smil_part.setHeader("Content-Type", "application/smil");
        smil_part.setHeader("Content-ID", "notification.smil");
        smil_part.setHeader("Content-Location", "notification.smil");
        multipart.addBodyPart(smil_part, 0);
    }

    /**
     * Adds a bodypart to the multipart object containg date information.
     * This information is generated by TextCreator using the tag <B>mms_date<B/>.
     */
    private void addDate() throws MessagingException, Exception {
        String date = TextCreator.get().generateText(inbox, email, user, "mms_date", true, null);
        if (date != null) {
            MimeBodyPart smil_part = new MimeBodyPart();
            smil_part.setText(date);
            smil_part.setHeader("Content-ID", "date.txt");
            smil_part.setHeader("Content-Location", "date.txt");
            multipart.addBodyPart(smil_part);
        }
    }

    /**
     * Adds a bodypart to the multipart object containg message
     * count information.
     * This information is generated by TextCreator using the tag <B>mms_count<B/>.
     */
    private void addCount() throws MessagingException, Exception {
        String date = TextCreator.get().generateText(inbox, email, user, "c", true, null);
        if (date != null) {
            MimeBodyPart smil_part = new MimeBodyPart();
            smil_part.setText(date);
            smil_part.setHeader("Content-ID", "count.txt");
            smil_part.setHeader("Content-Location", "count.txt");
            multipart.addBodyPart(smil_part);
        }
    }


    /**
     * Adds a bodypart to the multipart object containg senders phonenumber. If senders
     * phonenumber is missing the general MoIP number is used.
     * This information is generated by TextCreator using the tag <B>mms_sender<B/>.
     */
    private void addSender() throws MessagingException, Exception {
        String sender = TextCreator.get().generateText(inbox, email, user, "mms_sender", true, null);
        if (sender != null) {
            MimeBodyPart smil_part = new MimeBodyPart();
            smil_part.setText(sender);
            smil_part.setHeader("Content-ID", "sender.txt");
            smil_part.setHeader("Content-Location", "sender.txt");
            multipart.addBodyPart(smil_part);
        }
    }

    /**
     * Adds a bodypart to the multipart object containg the transcoded message.
     */
    private void addMessage() throws MessagingException, Exception {
        if (email.getDepositType() == depositType.VOICE) {
            if (replace("__MESSAGE__", "message.amr")) {
                MimeBodyPart smil_part = new MimeBodyPart();

                Part voicePart = email.getVoiceAttachmentPart();

                PartParser.Result result = PartParser.parse(voicePart);

                if (shouldEncode(result, true)) {
                    log.logMessage("Encoding voice message to amr", Logger.L_DEBUG);
                    ConversionResult conversionResult = mediaOut.convertVoice(voicePart);
                    if (conversionResult == null || conversionResult.getResultCode() != ConversionResult.OK) {
                        throw new MessagingException("Could not transcode message to AMR format");
                    }
                    if (conversionResult.getLength() < 0) {
                        throw new MessagingException("Could not transcode message to AMR format. " +
                                "Could not determine size.");
                    }
                    replace("__LENGTH__", "" + conversionResult.getLength());
                    DataSource src = new ByteArrayDataSource(conversionResult.getInputStream(), conversionResult.getContentType());
                    smil_part.setDataHandler(new DataHandler(src));
                } else {

                	log.logMessage("Begin convert 3gp voice message to amr", Logger.L_DEBUG);
                    replace("__LENGTH__", "" + result.getDuration()*1000);
                    InputStream is = voicePart.getInputStream();
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    Amr3gpConverter converter = new Amr3gpConverter();
                    boolean success = false;
                    try {
                    	converter.convert3gpToAmr(is, os);
                        DataSource src = new ByteArrayDataSource(os.toByteArray(), "audio/amr");
                        smil_part.setDataHandler(new DataHandler(src));
                        success = true;
                    } catch( ConverterException e) {
                    	log.logMessage("Fail converting from 3gp to amr. ConverterException:"+e.getMessage(), Logger.L_DEBUG);

                    }
                    //if the conversion fails, send the original 3gp
                    if (!success) {
                        smil_part.setDataHandler(new DataHandler(voicePart.getDataHandler().getDataSource()));
                    }

                    os.close();
                    is.close();

                }
                smil_part.setHeader("Content-Type", "audio/amr");
                smil_part.setHeader("Content-ID", "message.amr");
                smil_part.setHeader("Content-Location", "message.amr");
                multipart.addBodyPart(smil_part);
            }
        } else if (email.getDepositType() == depositType.VIDEO) {
            if (replace("__MESSAGE__", "message.3gp")) {
                MimeBodyPart smil_part = new MimeBodyPart();

                Part videoPart = email.getVoiceAttachmentPart();

                PartParser.Result result = PartParser.parse(videoPart);

                if (shouldEncode(result, false)) {
                    log.logMessage("Encoding video message to 3gp", Logger.L_DEBUG);
                    MCData mcData = mediaOut.convertVideo(videoPart, Config.getMMSMaxVideoLength());
                    if (mcData == null || mcData.getStatus() != 2) {
                        throw new MessagingException("Could not transcode message to 3gp format");
                    }
                    if (mcData.getLength() < 0) {
                        throw new MessagingException("Could not transcode message to 3gp format. " +
                                "Could not determine size.");
                    }
                    replace("__LENGTH__", "" + mcData.getLength());
                    smil_part.setDataHandler(new DataHandler(mcData.getAttachment()));
                } else {
                    replace("__LENGTH__", "" + result.getDuration());
                    smil_part.setDataHandler(videoPart.getDataHandler());
                }

                smil_part.setHeader("Content-Type", "video/3gp");
                smil_part.setHeader("Content-ID", "message.3gp");
                smil_part.setHeader("Content-Location", "message.3gp");
                multipart.addBodyPart(smil_part);
            }
        }
    }

    /**
     * Replaces all occurrences of oldText with newText within smil document.
     */
    private boolean replace(String oldText, String newText) throws Exception {
        boolean modified = false;
        if (doc == null || oldText == null || newText == null) return false;
        int start = doc.indexOf(oldText);
        while (start > 0) {
            String tmp = doc.substring(0, start);
            tmp += newText;
            tmp += doc.substring(start + oldText.length());
            doc = tmp;
            modified = true;
            start = doc.indexOf(oldText);
        }
        return modified;
    }

    private boolean shouldEncode(PartParser.Result result, boolean voice) {
        MimeType mimeType = result.getContentType();
        log.logMessage("In shouldEncode " + mimeType, Logger.L_DEBUG);
        if (voice) {
            if (mimeType != null) {
                return !mimeType.toString().equalsIgnoreCase("audio/3gpp");
            } else {
                FileName fileName = result.getFilename();
                if (fileName != null) {
                    return !fileName.getExtension().equals("3gp");
                }
            }
        } else {
            if (mimeType != null) {
                return !mimeType.toString().equalsIgnoreCase("video/3gpp");
            } else {
                FileName fileName = result.getFilename();
                if (fileName != null) {
                    return !fileName.getExtension().equals("3gp");
                }
            }
        }
        return false;
    }

    /**
     * @param dispString
     * @return the attachment part from the mail
     *//*
    private Part getAttachmentPart(String dispString) {
        try {
            MimeMessage mm = email.getMimeMessage();
            if (mm == null) return null;
            Multipart mp = (Multipart) (mm.getContent());
            if (mp == null) return null;
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart body = mp.getBodyPart(i);
                String[] disp = body.getHeader("content-disposition");
                for (int q = 0; q < disp.length; q++) {
                    if (disp[q].toLowerCase().indexOf(dispString) != -1) {
                        return body;
                    }
                }
            }
            return null;
        } catch (IOException e) {
            log.logMessage("getAttachmentPart(): Could not read message: " +
                    e + " " + NtfUtil.stackTrace(e), Logger.L_VERBOSE);
            return null;
        } catch (MessagingException e) {
            log.logMessage("getAttachmentPart(): Could not read message: " +
                    e + " " + NtfUtil.stackTrace(e), Logger.L_VERBOSE);
            return null;
        }
    }*/
}
