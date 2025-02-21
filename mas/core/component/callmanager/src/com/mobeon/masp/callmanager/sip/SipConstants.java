/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.sip;

import com.mobeon.masp.callmanager.configuration.ConfigurationReader;
import com.mobeon.masp.callmanager.sip.header.SipContentType;

import javax.sip.message.Request;
import java.util.HashSet;
import java.util.Collection;

/**
 * A container for SIP related constants.
 *
 * @author Malin Flodin
 */
public abstract class SipConstants {

    // Supported content types
    private static final HashSet<SipContentType> SUPPORTED_CT = new HashSet<SipContentType>();

    // Supported methods
    private static final HashSet<String> SUPPORTED_METHODS = new HashSet<String>();

    // Known but unsupported methods
    private static final HashSet<String> UNSUPPORTED_METHODS = new HashSet<String>();

    // Supported versions
    private static final HashSet<String> SUPPORTED_VERSIONS = new HashSet<String>();

    // Supported extensions
    private static final HashSet<String> SUPPORTED_EXTENSIONS = new HashSet<String>();

    // Additional SIP headers
    public static final String HDR_DIVERSION        = "Diversion";
    public static final String HDR_EXPERIENCED_OPERATIONAL_STATUS = "Experienced-Operational-Status";
    public static final String HDR_PRIVACY          = "Privacy";
    public static final String HDR_REMOTE_PARTY_ID  = "Remote-Party-ID";

    // Content types and subtypes
    public static final String CT_APPLICATION       = "application";
    public static final String CT_MULTIPART         = "multipart";
    public static final String CST_MEDIA_CONTROL    = "media_control+xml";
    public static final String CST_SIMPLE_MESSAGE_SUMMARY = "simple-message-summary";
    public static final String CST_SDP              = "sdp";
    public static final String CST_GTD              = "gtd";

    // GTD parameters
    public static final String GTD_CGN = "CGN";
    public static final String GTD_RNI = "RNI";

    // Header parameters
    public static final String PARAM_BOUNDARY       = "boundary";
    public static final String PARAM_CHARSET        = "charset";

    // Charsets
    public static final String CHARSET_UTF8         = "utf-8";

    // Disposition handling
    public static final String DISPOSITION_OPTIONAL = "optional";

    // Encodings
    public static final String ENCODING_NONE        = "identity";

    // Extensions
    public static final String EXTENSION_100REL     = "100rel";
    public static final String EXTENSION_PRECONDITION = "precondition";
    public static final String EXTENSION_HISTINFO   = "histinfo";

    // Transports
    public static final String TRANSPORT_UDP        = "udp";

    // Privacy
    public static final String PRIVACY_RESTRICTED   = "id";
    public static final String PRIVACY_ALLOWED      = "none";
    public static final String REMOTE_PARTY_PRIVACY_RESTRICTED  = "full";
    public static final String REMOTE_PARTY_PRIVACY_ALLOWED     = "off";

    // Q.850
    public static final String Q850                 = "q.850";
    public static final String Q850_LOCATION        = "eri-location";

    // General internet
    public static final int MAX_PORT = 65535; 

    // Line endings
    public static final String CRLF                 = "\r\n";
    public static final String LF                   = "\n";

    // Redirection counters
    public static final Integer LOOPBACK_PREVENTION_COUNTER = 5;

    // Event type for MWI notification over SIP
    public static final String MWI_EVENT_TYPE = "message-summary";

    public static final int PRECONDITION_FAILURE = 580;

    static {
        // Initialise supported content types
        SUPPORTED_CT.add(new SipContentType(CT_APPLICATION, CST_SDP));
        SUPPORTED_CT.add(new SipContentType(CT_APPLICATION, CST_MEDIA_CONTROL));
        SUPPORTED_CT.add(new SipContentType(CT_APPLICATION, CST_GTD));

        // Initialise supported methods
        SUPPORTED_METHODS.add(Request.INVITE);
        SUPPORTED_METHODS.add(Request.ACK);
        SUPPORTED_METHODS.add(Request.BYE);
        SUPPORTED_METHODS.add(Request.CANCEL);
        SUPPORTED_METHODS.add(Request.OPTIONS);
        SUPPORTED_METHODS.add(Request.INFO);
        SUPPORTED_METHODS.add(Request.PRACK);
        SUPPORTED_METHODS.add(Request.SUBSCRIBE);
        SUPPORTED_METHODS.add(Request.UPDATE);

        // Initialise supported versions
        SUPPORTED_VERSIONS.add("sip/2.0");

        // Initialise known but not supported methods
        UNSUPPORTED_METHODS.add(Request.REGISTER);

        // Initialise supported extension
        SUPPORTED_EXTENSIONS.add(EXTENSION_100REL);
        SUPPORTED_EXTENSIONS.add(EXTENSION_PRECONDITION);
    }

    public static Collection<SipContentType> getSupportedContentTypes() {
        return SUPPORTED_CT;
    }

    public static String getSupportedEncodings() {
        return "none";
    }

    public static String getSupportedLanguages() {
        return "all";
    }

    /**
     * Returns a collection of the supported methods. The following methods are
     * supported: INVITE, ACK, BYE, CANCEL, OPTIONS, INFO
     * @return  Returns a collection of the supported methods.
     */
    public static Collection<String> getSupportedMethods() {
        return SUPPORTED_METHODS;
    }

    /**
     * Returns a collection of the supported versions.
     * The following versions are supported: "SIP/2.0"
     * @return  Returns a collection of the supported versions.
     */
    public static Collection<String> getSupportedVersions() {
        return SUPPORTED_VERSIONS;
    }

    /**
     * Returns whether the given <param>extension</param> is supported or not.
     * Refer to {@code SUPPORTED_EXTENSIONS} for the list of supported extensions.
     * @param extension incoming extension 
     * @return True if the given <param>extension</param> is supported, false otherwise.
     */
    public static boolean isExtensionSupported(String extension) {
        boolean config = ConfigurationReader.getInstance().getConfig().isPreconditionEnabled();
        boolean result = SUPPORTED_EXTENSIONS.contains(extension);

        // Backward compatibility - PRECONDITION was not supported by MAS prior to PreSessionEstablishment feature
        if (!config && EXTENSION_PRECONDITION.equalsIgnoreCase(extension)) {
            result = false;
        }
        return result;
    }

    /**
     * Checks whether the given method is known but not supported.
     * Refer to {@code UNSUPPORTED_METHODS} for the list of unsupported methods.
     * @param method incoming sip method
     * @return True if the method is known but not supported, false otherwise.
     */
    public static boolean isMethodKnownButUnsupported(String method) {
        return UNSUPPORTED_METHODS.contains(method);
    }

    /**
     * Checks whether the given method is supported.
     * Refer to {@code SUPPORTED_METHODS} for the list of supported methods.
     * @param method incoming sip method
     * @return True if the method is supported, false otherwise.
     */
    public static boolean isMethodSupported(String method) {
        boolean config = ConfigurationReader.getInstance().getConfig().isSessionEstablishmentEnabled();
        boolean result = SUPPORTED_METHODS.contains(method);

        // Backward compatibility - SIP UPDATE was not supported by MAS prior to PreSessionEstablishment feature
        if (!config && Request.UPDATE.equalsIgnoreCase(method)) {
            result = false;
        }
        return result;
    }

    /**
     * Returns whether the given SIP version is supported or not.
     * Currently only the version "SIP/2.0" is supported.
     * @param   sipVersion
     * @return  True if <param>sipVersion</param> is supported, false otherwise.
     */
    public static boolean isSipVersionSupported(String sipVersion) {
        return SUPPORTED_VERSIONS.contains(sipVersion.toLowerCase());
    }

}


