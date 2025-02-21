package com.mobeon.masp.callmanager.sdp.attributes;

import java.util.Locale;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.callmanager.sdp.SdpNotSupportedException;
import com.mobeon.masp.callmanager.sip.header.SipWarning;

/**
 * This class represents the content of a media attribute "precondition" line of an SDP.
 * As defined in RFC3312: Integration of Resource Management and SIP.
 * <p>
 * This class is immutable. 
 */
public abstract class SdpPrecondition {

    private static final ILogger LOG = ILoggerFactory.getILogger(SdpPrecondition.class);
    
    private static final String PARSE_ERROR =
            "Could not parse precondition attribute value received in remote SDP. " +
                    "The call will not be setup.";

    private static final SdpNotSupportedException PARSE_EXCEPTION =
            new SdpNotSupportedException(
                    SipWarning.ATTRIBUTE_NOT_UNDERSTOOD, PARSE_ERROR);
    
    private final String preconditionType;
    private final StrengthTag strengthTag;
    private final StatusType statusType;
    private final DirectionTag directionTag;
    

    public static final String PRECONDITION_TYPE_QOS = "qos";
    
    /**
     * strength-tag       =  ("mandatory" | "optional" | "none"
     *                    =  | "failure" | "unknown")
     */
    public enum StrengthTag {
        MANDATORY ("mandatory"),
        OPTIONAL ("optional"),
        NONE ("none"),
        FAILURE ("failure"),
        UNKNOWN ("unknown");
        
        private String value;
        
        StrengthTag(String value) {
            this.value = value;
        }
        
        public String toString() {
            return value;
        }
    }
    
    /**
     * status-type        =  ("e2e" | "local" | "remote")
     */
    public enum StatusType {
        LOCAL ("local"),
        REMOTE ("remote"),
        E2E ("e2e");
        
        private String value;
        
        StatusType(String value) {
            this.value = value;
        }
        
        public String toString() {
            return value;
        }
    }
    
    /**
     * direction-tag      =  ("none" | "send" | "recv" | "sendrecv")
     */
    public enum DirectionTag {
        NONE ("none"),
        SEND ("send"),
        RECV ("recv"),
        SENDRECV ("sendrecv");
        
        private String value;
        
        DirectionTag(String value) {
            this.value = value;
        }
        
        public String toString() {
            return value;
        }
    }
    
    SdpPrecondition(String preconditionType, StrengthTag strengthTag, StatusType statusType, DirectionTag directionTag) {
        this.preconditionType = preconditionType;
        this.strengthTag = strengthTag;
        this.statusType = statusType;
        this.directionTag = directionTag;
    }
    
    public static SdpPreconditionCurr parseCurrentStatusAttribute(String curr)
            throws SdpNotSupportedException {
        /**
         *       current-status     =  "a=curr:" precondition-type
         *                              SP status-type SP direction-tag
         */
        SdpPreconditionCurr sdpPrecondition = null;
        
        if (LOG.isDebugEnabled())
            LOG.debug("Parsing curr attribute value: " + curr);
        
        if(curr != null) {
            String[] token = curr.split(" ");
            if(token.length != 3 || token[0].trim().isEmpty())
                throw PARSE_EXCEPTION;
            
            try {
                String preconditionType = token[0];
                StatusType statusType = StatusType.valueOf(token[1].toUpperCase(Locale.ENGLISH));
                DirectionTag directionTag = DirectionTag.valueOf(token[2].toUpperCase(Locale.ENGLISH));
                
                sdpPrecondition = new SdpPreconditionCurr(preconditionType, statusType, directionTag);
                
            } catch(IllegalArgumentException e) {
                throw PARSE_EXCEPTION;
            }
        }
        
        return sdpPrecondition;
    }
    
    public static SdpPreconditionDes parseDesiredStatusAttribute(String des)
            throws SdpNotSupportedException {
        /**
         *       desired-status     =  "a=des:" precondition-type
         *                             SP strength-tag SP status-type
         *                             SP direction-tag
         */
        SdpPreconditionDes sdpPrecondition = null;
        
        if (LOG.isDebugEnabled())
            LOG.debug("Parsing des attribute value: " + des);
        
        if(des != null) {        
            String[] token = des.split(" ");
            if(token.length != 4 || token[0].trim().isEmpty())
                throw PARSE_EXCEPTION;
            
            try {
                String preconditionType = token[0];
                StrengthTag strengthTag = StrengthTag.valueOf(token[1].toUpperCase(Locale.ENGLISH));
                StatusType statusType = StatusType.valueOf(token[2].toUpperCase(Locale.ENGLISH));
                DirectionTag directionTag = DirectionTag.valueOf(token[3].toUpperCase(Locale.ENGLISH));
                
                // validate that token not acceptable in precondition offer are rejected
                if(strengthTag == StrengthTag.FAILURE || strengthTag == StrengthTag.UNKNOWN || directionTag == DirectionTag.NONE)
                    throw PARSE_EXCEPTION;
                
                sdpPrecondition = new SdpPreconditionDes(preconditionType, strengthTag, statusType, directionTag);
                
            } catch(IllegalArgumentException e) {
                throw PARSE_EXCEPTION;
            }
        }
        
        return sdpPrecondition;
    }
    
    public static SdpPreconditionConf parseConfirmStatusAttribute(String conf)
            throws SdpNotSupportedException {
        /**
         *       confirm-status     =  "a=conf:" precondition-type
         *                             SP status-type SP direction-tag
         */
        SdpPreconditionConf sdpPrecondition = null;
        
        if (LOG.isDebugEnabled())
            LOG.debug("Parsing conf attribute value: " + conf);
        
        if(conf != null) {
            String[] token = conf.split(" ");
            if(token.length != 3 || token[0].trim().isEmpty())
                throw PARSE_EXCEPTION;
            
            try {
                String preconditionType = token[0];
                StatusType statusType = StatusType.valueOf(token[1].toUpperCase(Locale.ENGLISH));
                DirectionTag directionTag = DirectionTag.valueOf(token[2].toUpperCase(Locale.ENGLISH));
                
                sdpPrecondition = new SdpPreconditionConf(preconditionType, statusType, directionTag);
                
            } catch(IllegalArgumentException e) {
                throw PARSE_EXCEPTION;
            }
        }
        
        return sdpPrecondition;
    }
    
    public StrengthTag getStrengthTag() {
        return strengthTag;
    }

    
    public StatusType getStatusType() {
        return statusType;
    }

    
    public DirectionTag getDirectionTag() {
        return directionTag;
    }
    
    public String getPreconditionType() {
        return preconditionType;
    }
}
