import jakarta.mail.Flags;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 */
public class SimpleStoredMessage {
    private MimeMessage mimeMessage;
    private Flags flags;
    private String internalDateString;
    private String interalDateEnvelopeString;

    private long uid;
    private String envelope;
    private String bodyStructure;
    private long size;

    public SimpleStoredMessage(MimeMessage mimeMessage, long size, long uid)
            throws MessagingException {
        this(mimeMessage, mimeMessage.getFlags(), size, uid);
    }

    public SimpleStoredMessage(MimeMessage mimeMessage, Flags flags, long size, long uid) {
        this.mimeMessage = mimeMessage;
        this.flags = flags;
        this.size = size;
        this.uid = uid;

        Date internalDate = null;
        try {
            internalDate = mimeMessage.getSentDate();
        } catch (MessagingException me) {
            internalDate = new Date();
        }
        if (internalDate == null) {
            internalDate = new Date();
        }

        internalDateString = new SimpleDateFormat("dd-MMM-yyyy hh:mm:ss Z").format(internalDate);
        interalDateEnvelopeString = new MailDateFormat().format(internalDate);

        makeEnvelope();
        makeBodyStructure();
    }

    public MimeMessage getMimeMessage() {
        return mimeMessage;
    }

    public Flags getFlags() {
        return flags;
    }

    public String getInternalDate() {
        return internalDateString;
    }

    public long getUid() {
        return uid;
    }

    /**
     * Provides the Envelope structure information for this message.
     * This is a parsed representation of the rfc822 envelope information.
     *
     * @return String according to syntax in rfc 2060.
     */
    public String getEnvelope() {
        return envelope;
    }

    /**
     * Returns the Body Structure information for this message.
     * This is a representation of the MIME structure of the message.
     *
     * @return String according to syntax in rfc 2060.
     */
    public String getBodyStructure() {
        return bodyStructure;
    }

    /**
     * @return size of the mail
     */
    public long getSize() {
        return size;
    }

    private void makeEnvelope() {
        StringBuffer buf = new StringBuffer();
        buf.append("(");

        buf.append("\"");
        buf.append(interalDateEnvelopeString);
        buf.append("\"");

        buf.append(" ");

        try {
            String subject = mimeMessage.getSubject();
            if (subject != null) {
                buf.append("\"");
                buf.append(subject);
                buf.append("\"");
            }
        } catch (MessagingException e) {
            System.out.println("Exception in makeEnvelope (subject) " + e);
        }

        buf.append(" ");

        // 3. From
        String[] from = null;
        try {
            from = mimeMessage.getHeader("From");
            if (from != null && from.length > 0) {
                buf.append("(");
                for (int i = 0; i < from.length; i++) {
                    buf.append(parseAddress(from[i]));
                }
                buf.append(")");
            } else {
                buf.append("NIL");
            }
        } catch (MessagingException e) {
            System.out.println("Exception in makeEnvelope (from) " + e);
        }

        buf.append(" ");

        // 4. Sender
        try {
            String[] sender = mimeMessage.getHeader("Sender");
            if (sender != null && sender.length > 0) {
                buf.append("(");
                for (int i = 0; i < sender.length; i++) {
                    buf.append(parseAddress(sender[i]));
                }
                buf.append(")");
            } else {
                if (from != null && from.length > 0) {
                    buf.append("(");
                    buf.append(parseAddress(from[0])); //first From address
                    buf.append(")");
                } else {
                    buf.append("NIL");
                }
            }
        } catch (MessagingException e) {
            System.out.println("Exception in makeEnvelope (sender) " + e);
        }

        buf.append(" ");

        // 5. Reply-To
        try {
            String[] sender = mimeMessage.getHeader("In Reply To");
            if (sender != null && sender.length > 0) {
                buf.append("(");
                for (int i = 0; i < sender.length; i++) {
                    buf.append(parseAddress(sender[i]));
                }
                buf.append(")");
            } else {
                if (from != null && from.length > 0) {
                    buf.append("(");
                    buf.append(parseAddress(from[0])); //first From address
                    buf.append(")");
                } else {
                    buf.append("NIL");
                }
            }
        } catch (MessagingException e) {
            System.out.println("Exception in makeEnvelope (sender) " + e);
        }

        buf.append(" ");

        // 6. To
        try {
            String[] to = mimeMessage.getHeader("To");
            if (to != null && to.length > 0) {
                buf.append("(");
                for (int i = 0; i < to.length; i++) {
                    buf.append(parseAddress(to[i]));
                }
                buf.append(")");
            } else {
                buf.append("NIL");
            }
        } catch (MessagingException e) {
            System.out.println("Exception in makeEnvelope (to) " + e);
        }

        buf.append(" ");

        // 7. Cc
        buf.append("NIL");

        buf.append(" ");

        // 8. Bcc
        buf.append("NIL");

        buf.append(" ");

        // 9. In-Reply-To
        buf.append("NIL");

        buf.append(" ");

        // 10. Message-Id
        try {
            String messageId = mimeMessage.getMessageID();
            if (messageId != null) {
                buf.append("\"");
                buf.append(messageId);
                buf.append("\"");
            }
        } catch (MessagingException e) {
            System.out.println("Exception in makeEnvelope (messageId) " + e);
        }

        buf.append(")");

        envelope = buf.toString();
    }

    /**
     * Parses a String email address to an IMAP address string.
     */
    String parseAddress(String address) {
        int comma = address.indexOf(",");
        StringBuffer buf = new StringBuffer();
        if (comma == -1) { //single address
            buf.append("(");
            InternetAddress netAddr = null;
            try {
                netAddr = new InternetAddress(address);
            } catch (AddressException ae) {
                return null;
            }
            String personal = netAddr.getPersonal();
            if (personal != null && (!personal.equals(""))) {
                if (personal.startsWith("\"")) {
                    buf.append(personal);
                } else {
                    buf.append("\"" + personal + "\"");
                }
            } else {
                buf.append("NIL");
            }
            buf.append(" ");
            buf.append("NIL");
            buf.append(" ");
            try {
                MailAddress mailAddr = new MailAddress(netAddr.getAddress());
                buf.append("\"" + mailAddr.getUser() + "\"");
                buf.append(" ");
                buf.append("\"" + mailAddr.getHost() + "\"");
            } catch (Exception pe) {
                buf.append("NIL" + " " + "NIL");
            }
            buf.append(")");
        } else {
            buf.append(parseAddress(address.substring(0, comma)));
            buf.append(" ");
            buf.append(parseAddress(address.substring(comma + 1)));
        }
        return buf.toString();
    }

    private void makeBodyStructure() {
        StringBuffer buf = new StringBuffer();
        buf.append("((");

        try {
            MimeMultipart multipart = (MimeMultipart) mimeMessage.getContent();
            MimeBodyPart bodyPart1 = (MimeBodyPart) multipart.getBodyPart(0);

            ContentType contentType = new ContentType(bodyPart1.getContentType());

            buf.append("\"");
            buf.append(contentType.getPrimaryType().toUpperCase());
            buf.append("\"");
            buf.append(" ");
            buf.append("\"");
            buf.append(contentType.getSubType().toUpperCase());
            buf.append("\"");
            buf.append(" ");

            String param = contentType.getParameter("name");
            if (param != null) {
                buf.append("(");
                buf.append("\"NAME\"");
                buf.append(" ");
                buf.append("\"" + param + "\"");
                buf.append(")");
            }

            // 4. body id
            buf.append(" ");
            buf.append("NIL");
            buf.append(" ");

            // 5. Content-Description
            String contentDescription = bodyPart1.getDescription();
            if (contentDescription != null) {
                buf.append("\"");
                buf.append(contentDescription);
                buf.append("\"");
            } else {
                buf.append("NIL");
            }

            // 6. encoding
            buf.append(" ");
            String encoding = bodyPart1.getEncoding();
            if (encoding != null) {
                buf.append("\"");
                buf.append(encoding.toUpperCase());
                buf.append("\"");
            } else {
                buf.append("NIL");
            }
            // 7. size
            buf.append(" ");
            buf.append(bodyPart1.getSize());
            buf.append(" ");

            buf.append("NIL"); // ???
            buf.append(" ");

            // Inline...
            // ToDo hardcoded now
            buf.append("(\"INLINE\" (\"FILENAME\" \"message.wav\" \"VOICE\" \"Voice-Message\")) (\"EN\"))(\"AUDIO\" \"WAV\" (\"NAME\" \"spoken.wav\") NIL \"Originators spoken name\" \"BASE64\" 25042 NIL (\"INLINE\" (\"FILENAME\" \"spoken.wav\" \"VOICE\" \"Originator-Spoken-Name\")) (\"EN\")) \"VOICE-MESSAGE\" (\"BOUNDARY\" \"----=_Part_0_8608446.1204022525376\") NIL (\"EN\")");

        } catch (IOException e) {
            System.out.println("Exception in makeBodyStructure " + e);
        } catch (MessagingException e) {
            System.out.println("Exception in makeBodyStructure " + e);
        }

        buf.append(")");

        bodyStructure = buf.toString();
    }
}
