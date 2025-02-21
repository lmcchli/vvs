/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager;

import java.util.HashMap;

/**
 * A container of RedirectingParty related information.
 * It extends {@link com.mobeon.masp.callmanager.CallPartyDefinitions}
 * with information specific for a RedirectingParty.
 *
 * As a container, it provides only setters and getters.
 *
 * This class is thread-safe.
 *
 * @author Malin Flodin
 */
public class RedirectingParty extends CallPartyDefinitions {

    private static final HashMap<RedirectingReason, String> REDIR_CAUSE_NAMES =
            new HashMap<RedirectingReason, String>();

    /**
     * Redirecting reasons according to Q.732.2-5
     */
    public static enum RedirectingReason {
        UNKNOWN,
        USER_BUSY,
        NO_REPLY,
        UNCONDITIONAL,
        DEFLECTION_DURING_ALERTING,
        DEFLECTION_IMMEDIATE_RESPONSE,
        MOBILE_SUBSCRIBER_NOT_REACHABLE
    }

    private PresentationIndicator presentationIndicator;
    private RedirectingReason redirectingReason = RedirectingReason.UNKNOWN;
    private String redirectingReasonText = "unknown";

    static {
        REDIR_CAUSE_NAMES.put(RedirectingReason.UNKNOWN, "unknown");
        REDIR_CAUSE_NAMES.put(RedirectingReason.USER_BUSY, "user busy");
        REDIR_CAUSE_NAMES.put(RedirectingReason.NO_REPLY, "no reply");
        REDIR_CAUSE_NAMES.put(RedirectingReason.UNCONDITIONAL, "unconditional");
        REDIR_CAUSE_NAMES.put(RedirectingReason.DEFLECTION_DURING_ALERTING, "deflection during alerting");
        REDIR_CAUSE_NAMES.put(RedirectingReason.DEFLECTION_IMMEDIATE_RESPONSE, "deflection immediate response");
        REDIR_CAUSE_NAMES.put(RedirectingReason.MOBILE_SUBSCRIBER_NOT_REACHABLE, "mobile subscriber not reachable");
    }



    /**
     * SIP cause code to Redirecting reason mapping according to
     * RFC4458:
     *  +---------------------------------+-------+
     *  | Redirecting Reason              | Value |
     *  +---------------------------------+-------+
     *  | Unknown/Not available           | 404   |
     *  | User busy                       | 486   |
     *  | No reply                        | 408   |
     *  | Unconditional                   | 302   |
     *  | Deflection during alerting      | 487   |
     *  | Deflection immediate response   | 480   |
     *  | Mobile subscriber not reachable | 503   |
     *  +---------------------------------+-------+
     *
     * Diversion header reason to Redirecting reason mapping according to
     *  draft-levy-sip-diversion-08.txt:
     *  "user-busy"      => RedirectingReason.USER_BUSY
     *  "no-answer"      => RedirectingReason.NO_REPLY
     *  "unconditional"  => RedirectingReason.UNCONDITIONAL
     *  "deflection"     => RedirectingReason.DEFLECTION_IMMEDIATE_RESPONSE
     *  "unavailable"    => RedirectingReason.MOBILE_SUBSCRIBER_NOT_REACHABLE
     *  "away"           => RedirectingReason.UNKNOWN
     *  "do-not-disturb" => RedirectingParty.RedirectingReason.UNKNOWN
     *  "follow-me"      => RedirectingParty.RedirectingReason.UNKNOWN
     *  "out-of-service" => RedirectingParty.RedirectingReason.UNKNOWN
     *  "time-of-day"    => RedirectingParty.RedirectingReason.UNKNOWN
     *  "unavailable"    => RedirectingParty.RedirectingReason.UNKNOWN
     *  "unknown"        => RedirectingParty.RedirectingReason.UNKNOWN
     */
    private static final
    HashMap<String, RedirectingParty.RedirectingReason> CAUSE_MAPPING =
            new HashMap<String, RedirectingParty.RedirectingReason>();

    static {
        CAUSE_MAPPING.put("user-busy",
                RedirectingReason.USER_BUSY);
        CAUSE_MAPPING.put("no-answer",
                RedirectingReason.NO_REPLY);
        CAUSE_MAPPING.put("unconditional",
                RedirectingReason.UNCONDITIONAL);
        CAUSE_MAPPING.put("deflection",
                RedirectingReason.DEFLECTION_IMMEDIATE_RESPONSE);
        CAUSE_MAPPING.put("unavailable",
                RedirectingReason.MOBILE_SUBSCRIBER_NOT_REACHABLE);
        CAUSE_MAPPING.put("302",
                RedirectingParty.RedirectingReason.UNCONDITIONAL);
        CAUSE_MAPPING.put("404",
                RedirectingParty.RedirectingReason.UNKNOWN);
        CAUSE_MAPPING.put("408",
                RedirectingParty.RedirectingReason.NO_REPLY);
        CAUSE_MAPPING.put("480",
                RedirectingParty.RedirectingReason.DEFLECTION_IMMEDIATE_RESPONSE);
        CAUSE_MAPPING.put("486",
                RedirectingParty.RedirectingReason.USER_BUSY);
        CAUSE_MAPPING.put("487",
                RedirectingParty.RedirectingReason.DEFLECTION_DURING_ALERTING);
        CAUSE_MAPPING.put("503",
                RedirectingParty.RedirectingReason.MOBILE_SUBSCRIBER_NOT_REACHABLE);
    }



    public RedirectingParty() {
        super();
        presentationIndicator = PresentationIndicator.UNKNOWN;
        redirectingReason = RedirectingReason.UNKNOWN;
    }

    public synchronized PresentationIndicator getPresentationIndicator() {
        return presentationIndicator;
    }

    /**
     * Sets the Presentation Indicator for the party.
     * The Presentation Indicator shows if presentation of the number is
     * allowed, restricted or if the number is unavailable.
     *
     * @param pi The presentation indicator. Possible values are described
     * in {@link
     * com.mobeon.masp.callmanager.CallPartyDefinitions.PresentationIndicator}
     */
    public synchronized void setPresentationIndicator(PresentationIndicator pi) {
        this.presentationIndicator = pi;
    }


    /**
     * Get the redirecting reason, see {@link RedirectingReason}.
     * @return RedirectinReason
     */
    public synchronized RedirectingReason getRedirectingReason() {
        return redirectingReason;
    }


    /**
     * Get the redirecting reason text. This is the source of the redirecting reason
     * information. This is the same text as set in the setRedirectingReason(String) method.
     * It can for example be "user-busy", "follow-me" (when RedirectingParty is retrieved from
     * the Diversion header) or eg. "302" or "408" (when RedirectionParty is retrieved from
     * the History-Info header). If the RedirectingReason is unknown, this reason text can be
     * examined in order to get more exact information regarding the redirecting reason.
     * @return String describing the reason for redirection.
     */
    public synchronized String getRedirectingReasonText() {
        return redirectingReasonText;
    }

    /**
     * Sets the reason for the redirection.
     *
     * @param redirectingReason The reason for the redirection . Possible
     * values are described in {@link RedirectingReason}
     */
    public synchronized void setRedirectingReason(RedirectingReason redirectingReason) {
        this.redirectingReason = redirectingReason;
        this.redirectingReasonText = REDIR_CAUSE_NAMES.get(redirectingReason);
        if (redirectingReasonText == null)
            redirectingReasonText ="unknown";
    }

    /**
     * Sets the reason for the redirection. The given string is mapped to
     * a RedirectingReason according to {@link CAUSE_MAPPING}.
     *
     * @param redirectingReason
     */
    public synchronized void setRedirectingReason(String redirectingReason) {
        if (redirectingReason == null) {
            this.redirectingReasonText = "unknown";
            this.redirectingReason = RedirectingReason.UNKNOWN;
        } else {
            this.redirectingReasonText = redirectingReason;
            this.redirectingReason = CAUSE_MAPPING.get(redirectingReason);
            if (this.redirectingReason == null)
                this.redirectingReason = RedirectingReason.UNKNOWN;
        }

    }


    public String toString() {
        return super.toString() +  ", <PI = " +
                presentationIndicatorToString(getPresentationIndicator()) +
                ">, <Cause = " +
                redirectionCauseToString(getRedirectingReason()) + ">";
    }

    private String redirectionCauseToString(RedirectingReason redirectingReason) {
        String cause = null;
        if (redirectingReason != null) {
            cause = REDIR_CAUSE_NAMES.get(redirectingReason) +
                    " (" + redirectingReasonText + ")";
        }
        return cause;
    }
}
