import jakarta.mail.BodyPart;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles the Fetch IMAP command. Ex:
 * <p/>
 * A4 FETCH 1:2 (ENVELOPE INTERNALDATE RFC822.SIZE FLAGS BODY.PEEK[HEADER.FIELDS (X-Priority)])
 * A5 FETCH 1 (BODY.PEEK[1.MIME])
 * A6 FETCH 1 (BODY[1])
 *
 * @author emahagl
 */
public class FetchHandler {

    private CommandParser commandParser;
    private ImapResponse response;
    private User user;
    private int start = -1;
    private int end = -1;

    public FetchHandler(CommandParser commandParser, ImapResponse response, User user) {
        this.commandParser = commandParser;
        this.response = response;
        this.user = user;
        getMessageSet();
    }

    public void handleCommand() {
        if (start > - 1 && end > -1) {
            for (int i = start; i <= end; i++) {
                handleFetchResponse(i);
            }
        } else {
            handleFetchResponse(start);
        }
        response.taggedResponse("OK Completed");
    }

    private void handleFetchResponse(int id) {
        String fetchCommand = commandParser.getCommandToken(3);
        fetchCommand = fetchCommand.toLowerCase();

        if (fetchCommand.indexOf("envelope") > -1) {
            response.untaggedResponse(makeEnvelopeResponse(id, false).toString());
        } else if (fetchCommand.indexOf("bodystructure") > -1) {
            response.untaggedResponse(makeEnvelopeResponse(id, true).toString());
        } else if (fetchCommand.indexOf("body") > -1) {

            SimpleStoredMessage simpleStoredMessage = user.getMessage(id);
            MimeMessage mimeMessage = simpleStoredMessage.getMimeMessage();
            handleBodyFetch(id, mimeMessage, fetchCommand);
        }
    }

    // TODO
    // TODO handle Body fetch parameters in a better way...
    // TODO
    private void handleBodyFetch(int id, MimeMessage mimeMessage, String fetchCommand) {

        try {
            StringBuffer respBuf = new StringBuffer();
            MimeMultipart multipart = (MimeMultipart) mimeMessage.getContent();

            if (fetchCommand.indexOf("body[]") > -1) {
                // Fetch the whole message
                respBuf.append(id + " FETCH (BODY[] ");
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                mimeMessage.writeTo(bout);
                byte[] bytes = bout.toByteArray();
                addLiteral(bytes, respBuf);
                respBuf.append(")");
            } else if (fetchCommand.indexOf("body.peek[header") > -1) {
                respBuf.append(id + " FETCH (BODY[HEADER.FIELDS] ");
                byte[] bytes = MsSimUtil.getHeaderAsBytes(mimeMessage);
                addLiteral(bytes, respBuf);
                respBuf.append(")");
            } else if (fetchCommand.indexOf("body.peek[1.mime]") > -1) {
                respBuf.append(id + " FETCH (BODY[1.MIME] ");
                handleBodyPartHeader(multipart, 0, respBuf);
                respBuf.append(")");
            } else if (fetchCommand.indexOf("body.peek[1]") > -1) {
                respBuf.append(id + " FETCH (BODY[1] ");
                handleBodyPart(multipart, 0, respBuf);
                respBuf.append(")");
            } else if (fetchCommand.indexOf("body[1]") > -1) {
                respBuf.append(id + " FETCH (BODY[1] ");
                handleBodyPart(multipart, 0, respBuf);
                respBuf.append(")");
            } else if (fetchCommand.indexOf("body.peek[2.mime]") > -1) {
                respBuf.append(id + " FETCH (BODY[2.MIME] ");
                handleBodyPartHeader(multipart, 1, respBuf);
                respBuf.append(")");
            } else if (fetchCommand.indexOf("body.peek[2]") > -1) {
                respBuf.append(id + " FETCH (BODY[2] ");
                handleBodyPart(multipart, 1, respBuf);
                respBuf.append(")");
            } else if (fetchCommand.indexOf("body[2]") > -1) {
                respBuf.append(id + " FETCH (BODY[2] ");
                handleBodyPart(multipart, 1, respBuf);
                respBuf.append(")");
            }
            response.untaggedResponse(respBuf.toString());

        } catch (IOException e) {
            System.out.println("Exception in handleBodyFetch " + e);
        } catch (MessagingException e) {
            System.out.println("Exception in handleBodyFetch " + e);
        }
    }

    private void handleBodyPartHeader(MimeMultipart multipart, int bodyId, StringBuffer respBuf)
            throws MessagingException {
        BodyPart bodyPart = multipart.getBodyPart(bodyId);
        byte[] bytes = MsSimUtil.getHeaderAsBytes(bodyPart);
        addLiteral(bytes, respBuf);
    }

    private void handleBodyPart(MimeMultipart multipart, int bodyId, StringBuffer respBuf)
            throws MessagingException {
        BodyPart bodyPart = multipart.getBodyPart(bodyId);
        byte[] bytes = MsSimUtil.getBodyAsBytes(bodyPart);
        addLiteral(bytes, respBuf);
    }

    private void addLiteral(byte[] bytes, StringBuffer response) {
        response.append('{');
        response.append(bytes.length);
        response.append('}');
        response.append("\r\n");

        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            response.append((char) b);
        }
    }

    private StringBuffer makeEnvelopeResponse(int id, boolean bodyStructureOnly) {
        SimpleStoredMessage simpleStoredMessage = user.getMessage(id);
        StringBuffer buf = new StringBuffer();
        buf.append(id);
        buf.append(" FETCH ");
        buf.append("(");
        if (!bodyStructureOnly) {
            buf.append("FLAGS");
            buf.append(" ");
            buf.append(MessageFlags.format(simpleStoredMessage.getFlags()));
            buf.append(" ");
            buf.append("INTERNALDATE");
            buf.append(" ");
            buf.append("\"");
            buf.append(simpleStoredMessage.getInternalDate());
            buf.append("\"");
            buf.append(" ");
            buf.append("RFC822.SIZE " + simpleStoredMessage.getSize());
            buf.append(" ");
            buf.append("ENVELOPE");
            buf.append(" ");
            buf.append(simpleStoredMessage.getEnvelope());

            buf.append(" ");
        }
        buf.append("BODYSTRUCTURE");
        buf.append(" ");
        buf.append(simpleStoredMessage.getBodyStructure());

        buf.append(")");
        return buf;
    }

    private void getMessageSet() {
        String messageSet = commandParser.getCommandToken(2);
        if (messageSet.length() > 1 && messageSet.indexOf(":") > -1) {
            try {
                String s1 = messageSet.substring(0, 1);
                String s2 = messageSet.substring(2);
                start = Integer.parseInt(s1);
                end = Integer.parseInt(s2);
            } catch (NumberFormatException e) {
                System.out.println("Error in getMessageSet " + e);
            }
        } else {
            try {
                start = Integer.parseInt(messageSet);
            } catch (NumberFormatException e) {
                System.out.println("Error in getMessageSet " + e);
            }
        }
    }

    /**
     * Models the Fetch command. Ex:
     * <p/>
     * (ENVELOPE INTERNALDATE RFC822.SIZE FLAGS BODY.PEEK[HEADER.FIELDS (X-Priority)])
     * (BODY.PEEK[1.MIME])
     * (BODY[1])
     */
    private static class FetchRequest {
        boolean flags;
        boolean uid;
        boolean internalDate;
        boolean size;
        boolean envelope;
        boolean body;
        boolean bodyStructure;

        private boolean setSeen = false;

        private Set<BodyFetchElement> bodyElements = new HashSet<BodyFetchElement>();

        public Set<BodyFetchElement> getBodyElements() {
            return bodyElements;
        }

        public boolean isSetSeen() {
            return setSeen;
        }

        public void add(BodyFetchElement element, boolean peek) {
            if (!peek) {
                setSeen = true;
            }
            bodyElements.add(element);
        }
    }

    private class BodyFetchElement {
        private String name;
        private String sectionIdentifier;
        private String partial;

        public BodyFetchElement(String name, String sectionIdentifier) {
            this(name, sectionIdentifier, null);
        }

        public BodyFetchElement(String name, String sectionIdentifier, String partial) {
            this.name = name;
            this.sectionIdentifier = sectionIdentifier;
            this.partial = partial;
        }

        public String getParameters() {
            return sectionIdentifier;
        }

        public String getResponseName() {
            return name;
        }

        public String getPartial() {
            return partial;
        }
    }
}
