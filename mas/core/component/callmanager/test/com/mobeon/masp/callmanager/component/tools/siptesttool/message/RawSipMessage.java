/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tools.siptesttool.message;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;

/**
 * TODO: Document
 *
 * Special handling of the following parameters:
 * <ul>
 * <li>{@link com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessageParameter.BRANCH} -
 * Will be generated to the magic cookie concatenated with a random integer
 * if not set.
 * </li>
 * <li>{@link com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessageParameter.CALL_ID} -
 * Will be generated to a random integer if not set.
 * </li>
 * <li>{@link com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessageParameter.LEN} - Will be calculated if not set.
 * </li>
 * <li>{@link com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessageParameter.LOCAL_PORT} -
 * Will be set to "5060" if not set.
 * </li>
 * <li>{@link com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessageParameter.LOCAL_TAG} -
 * Will be generated to a random integer if not set.
 * </li>
 * <li>{@link com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessageParameter.REMOTE_PORT} -
 * Will be set to "5060" if not set.
 * </li>
 * <li>{@link com.mobeon.masp.callmanager.component.tools.siptesttool.message.RawSipMessageParameter.TRANSPORT} -
 * Will be set to "UDP" if not set.
 * </li>
 * </ul>
 *
 * @author Malin Nyfeldt
 */
public class RawSipMessage {

    /** A logger instance. */
    private final ILogger log = ILoggerFactory.getILogger(getClass());

    // Default values
    private static final String DEFAULT_SIP_PORT =  "5060";
    private static final String DEFAULT_TRANSPORT = "UDP";


    private final String originalMessage;
    private final HashMap<RawSipMessageParameter, String> parameters;

    // Generator of cryptographically random numbers.
    // Used for generation of branch, call-id and tag
    private final SecureRandom random;

    private AtomicReference<String> parsedMessage = new AtomicReference<String>();
    private AtomicBoolean isParsed = new AtomicBoolean();


    // PATTERNS
    // Via header related
    private static Pattern branchPattern =
            Pattern.compile("Via:\\s*.*;branch=(\\S+)\\s*");
    private static Pattern viaHeaderPattern =
            Pattern.compile("(Via:.+)\\r\\n");

    // Call-ID header related
    private static Pattern callIdHeaderPattern =
            Pattern.compile("(Call-ID:.+)\\r\\n");
    private static Pattern callIdPattern =
            Pattern.compile("Call-ID:\\s*(\\S+)\\s*");

    // Contact header related
    private static Pattern contactPattern =
            Pattern.compile("Contact:\\s*<sip:(\\S+)@\\S+\\s*");

    // CSeq header related
    private static Pattern cSeqHeaderPattern =
            Pattern.compile("(CSeq:.+)\\r\\n");
    private static Pattern cSeqPattern =
        Pattern.compile("CSeq:\\s*(\\d+)\\s([a-zA-Z]+)\\s*");
    private static Pattern rSeqPattern =
        Pattern.compile("RSeq:\\s*(\\d+)\\s*");

    // From header related
    private static Pattern fromHeaderPattern =
            Pattern.compile("(From:.+)\\r\\n");
    private static Pattern fromTagPattern =
            Pattern.compile("From:\\s*.*;tag=(\\S+)\\s*");

    // To header related
    private static Pattern toHeaderPattern =
            Pattern.compile("(To:.+)\\r\\n");
    private static Pattern toTagPattern =
            Pattern.compile("To:\\s*.*;tag=(\\S+)\\s*");

    private static Pattern responseCodePattern =
            Pattern.compile("SIP/2.0\\s(\\d+)\\s.*\\s*");


    // TODO: This constructor is used to create a final message with no parameters
    public RawSipMessage(String message) throws NoSuchAlgorithmException {
        if (message == null)
            throw new NullPointerException("Message must not be null");

        this.originalMessage = message;
        this.parsedMessage.set(message);
        this.parameters = new HashMap<RawSipMessageParameter, String>();
        isParsed.set(true);

        random = SecureRandom.getInstance("SHA1PRNG");
    }

    // TODO: This constructor is used to create a message that is parameterized
    public RawSipMessage(String message, HashMap<RawSipMessageParameter, String> parameters)
            throws NoSuchAlgorithmException {
        if (message == null)
            throw new NullPointerException("Message must not be null");

        this.originalMessage = message;
        this.parameters = (parameters != null) ? parameters : new HashMap<RawSipMessageParameter, String>();
        isParsed.set(false);

        random = SecureRandom.getInstance("SHA1PRNG");
    }

    // TODO: Overrides any parameter set at construction
    public void setParameter(RawSipMessageParameter parameter, String value) {
        isParsed.set(false);
        parameters.put(parameter, value);
    }

    public String getCallId() {
        String callId = null;

        // Get a Matcher for the call id based on the parsed sip message.
        Matcher matcher = callIdPattern.matcher(getParsedMessage());

        if (matcher.find())
            if (matcher.end() >= 1)
                callId = matcher.group(1);

        if (log.isDebugEnabled()) log.debug(
                "Retreived call-ID: " + callId);

        return callId;
    }

    public String getCallIdHeader() {
        String callIdHeader = null;

        // Get a Matcher for the call id header based on the parsed sip message.
        Matcher matcher = callIdHeaderPattern.matcher(getParsedMessage());

        if (matcher.find())
            if (matcher.end() >= 1)
                callIdHeader = matcher.group(1);

        if (log.isDebugEnabled()) log.debug(
                "Retreived Call-ID header: " + callIdHeader);

        return callIdHeader;
    }

    public String getCSeqHeader() {
        String cseqHeader = null;

        // Get a Matcher for the CSeq header based on the parsed sip message.
        Matcher matcher = cSeqHeaderPattern.matcher(getParsedMessage());

        if (matcher.find())
            if (matcher.end() >= 1)
                cseqHeader = matcher.group(1);

        if (log.isDebugEnabled()) log.debug(
                "Retreived CSeq header: " + cseqHeader);

        return cseqHeader;
    }

    public String getFromHeader() {
        String fromHeader = null;

        // Get a Matcher for the from header based on the parsed sip message.
        Matcher matcher = fromHeaderPattern.matcher(getParsedMessage());

        if (matcher.find())
            if (matcher.end() >= 1)
                fromHeader = matcher.group(1);

        if (log.isDebugEnabled()) log.debug(
                "Retreived From header: " + fromHeader);

        return fromHeader;
    }

    public String getFromTag() {
        String tag = null;

        // Get a Matcher for the to tag based on the parsed sip message.
        Matcher matcher = fromTagPattern.matcher(getParsedMessage());

        if (matcher.find())
            if (matcher.end() >= 1)
                tag = matcher.group(1);

        if (log.isDebugEnabled()) log.debug(
                "Retreived from tag: " + tag);

        return tag;
    }

    public String getToHeader() {
        String toHeader = null;

        // Get a Matcher for the to header based on the parsed sip message.
        Matcher matcher = toHeaderPattern.matcher(getParsedMessage());

        if (matcher.find())
            if (matcher.end() >= 1)
                toHeader = matcher.group(1);

        if (log.isDebugEnabled()) log.debug(
                "Retreived To header: " + toHeader);

        return toHeader;
    }

    public String getToTag() {
        String tag = null;

        // Get a Matcher for the to tag based on the parsed sip message.
        Matcher matcher = toTagPattern.matcher(getParsedMessage());

        if (matcher.find())
            if (matcher.end() >= 1)
                tag = matcher.group(1);

        if (log.isDebugEnabled()) log.debug(
                "Retreived to tag: " + tag);

        return tag;
    }

    public String getViaHeader() {
        String viaHeader = null;

        // Get a Matcher for the via header based on the parsed sip message.
        Matcher matcher = viaHeaderPattern.matcher(getParsedMessage());

        if (matcher.find())
            if (matcher.end() >= 1)
                viaHeader = matcher.group(1);

        if (log.isDebugEnabled()) log.debug(
                "Retreived Via header: " + viaHeader);

        return viaHeader;
    }

    public String getBranchId() {
        String branchId = null;

        // Get a Matcher for the branch id based on the parsed sip message.
        Matcher matcher = branchPattern.matcher(getParsedMessage());

        if (matcher.find())
            if (matcher.end() >= 1)
                branchId = matcher.group(1);

        if (log.isDebugEnabled()) log.debug(
                "Retreived branchId: " + branchId);

        return branchId;
    }

    public String getContactUser() {
        String user = null;

        // Get a Matcher for the call id based on the parsed sip message.
        Matcher matcher = contactPattern.matcher(getParsedMessage());

        if (matcher.find())
            if (matcher.end() >= 1)
                user = matcher.group(1);

        if (log.isDebugEnabled()) log.debug(
                "Retreived contact user: " + user);

        return user;
    }

    public boolean isResponse() {
        return getParsedMessage().startsWith("SIP/2.0 ");
    }

    public boolean isRequest() {
        return !isResponse();
    }

    public String getParsedMessage() {
        if (!isParsed.get())
          parseMessage();
        return parsedMessage.get();
    }

    public String getMethod() {
        String method = null;
        Matcher matcher = cSeqPattern.matcher(getParsedMessage());

        if (matcher.find())
            if (matcher.end() >= 2)
                method = matcher.group(2);

        if (log.isDebugEnabled()) log.debug(
                "Retreived method: " + method);

        return method;
    }

    public String getCSeq() {
        String method = null;
        Matcher matcher = cSeqPattern.matcher(getParsedMessage());

        if (matcher.find())
            if (matcher.end() >= 1)
                method = matcher.group(1);

        if (log.isDebugEnabled()) log.debug(
                "Retreived method: " + method);

        return method;
    }

    public String getRSeq() {
        String method = null;
        Matcher matcher = rSeqPattern.matcher(getParsedMessage());

        if (matcher.find())
            if (matcher.end() >= 1)
                method = matcher.group(1);

        if (log.isDebugEnabled()) log.debug(
                "Retreived method: " + method);

        return method;
    }

    public String getResponseCode() {
        int index = getParsedMessage().indexOf("\r\n");
        String firstLine = getParsedMessage().substring(0, index);

        String code = null;

        // Get a Matcher for the to tag based on the parsed sip message.
        Matcher matcher = responseCodePattern.matcher(firstLine);

        if (matcher.find())
            if (matcher.end() >= 1)
                code = matcher.group(1);

        if (log.isDebugEnabled()) log.debug(
                "Retreived response code: " + code);

        return code;
    }


    /**
     * Parses the message string and replaces the parameters with
     * corresponding value.
     */
    public void parseMessage() {
        if (!isParsed.get()) {
            String result = originalMessage;

            // First replace special parameters
            result = replaceBranch(result);
            result = replaceCallId(result);
            result = replaceLocalTag(result);
            result = replaceSipPorts(result);
            result = replaceTransport(result);

            // Now replace parameters set for this message
            result = replaceParameters(result);

            // Finally, calculate the length of the message
            result = replaceLength(result);

            parsedMessage.set(result);
            isParsed.set(true);
        }
    }

    /**
     * @return Returns the message string where the parameters have been
     * replaced with corresponding value.
     */
    public String toString() {
        return getParsedMessage();
    }

    private String replaceLength(String input) {
        String output = input;

        if (!parameters.containsKey(RawSipMessageParameter.LEN)) {
            int index = input.indexOf("\r\n\r\n");
            int length = input.substring(index + 4).length();
            output = output.replaceAll(
                    "\\[" + RawSipMessageParameter.LEN.getName() + "\\]",
                    Integer.toString(length));
        }

        return output;
    }


    private String replaceBranch(String input) {
        String output = input;

        if (!parameters.containsKey(RawSipMessageParameter.BRANCH))
            output = output.replaceAll(
                    "\\[" + RawSipMessageParameter.BRANCH.getName() + "\\]",
                    "z9hG4bK" + generateRandomNumber());

        return output;
    }

    private String replaceCallId(String input) {
        String output = input;

        if (!parameters.containsKey(RawSipMessageParameter.CALL_ID))
            output = output.replaceAll(
                    "\\[" + RawSipMessageParameter.CALL_ID.getName() + "\\]",
                    generateRandomNumber());

        return output;
    }

    private String replaceLocalTag(String input) {
        String output = input;

        if (!parameters.containsKey(RawSipMessageParameter.LOCAL_TAG))
            output = output.replaceAll(
                    "\\[" + RawSipMessageParameter.LOCAL_TAG.getName() + "\\]",
                    generateRandomNumber());

        return output;
    }

    private String replaceParameters(String input) {
        String output = input;

        for (RawSipMessageParameter parameter : parameters.keySet()) {
            output = output.replaceAll(
                    "\\[" + parameter.getName() + "\\]",
                    parameters.get(parameter));
        }

        return output;
    }

    private String replaceSipPorts(String input) {
        String output = input;

        if (!parameters.containsKey(RawSipMessageParameter.REMOTE_PORT))
            output = output.replaceAll(
                    "\\[" + RawSipMessageParameter.REMOTE_PORT.getName() + "\\]",
                    DEFAULT_SIP_PORT);

        if (!parameters.containsKey(RawSipMessageParameter.LOCAL_PORT))
            output = output.replaceAll(
                    "\\[" + RawSipMessageParameter.LOCAL_PORT.getName() + "\\]",
                    DEFAULT_SIP_PORT);

        return output;
    }

    private String replaceTransport(String input) {
        String output = input;

        if (!parameters.containsKey(RawSipMessageParameter.TRANSPORT))
            output = output.replaceAll(
                    "\\[" + RawSipMessageParameter.TRANSPORT.getName() + "\\]",
                    DEFAULT_TRANSPORT);

        return output;
    }

    /**
     * Generates a new random number.
     * @return a 32-bit cryptographically random positiv integer as a String.
     */
    private String generateRandomNumber() {
        int tag = random.nextInt();
        tag = (tag < 0) ? 0 - tag : tag; // generate a positive number
        return Integer.toString(tag);
    }

}


