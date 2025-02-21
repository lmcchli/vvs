/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tools.siptesttool.message;

/**
 * Contains all parameters possible to use in a SIP message created for a
 * test case used in the SIP test tool.
 * The reason for listing all parameters here is to not invent new ones
 * identical to previously existing ones.
 *
 * @author Malin Nyfeldt
 */
public enum RawSipMessageParameter {
    BRANCH("branch"),
    CALLED("called"),
    CALLING("calling"),
    CALL_ID("call_id"),
    CONTACT_TRANSPORT("contact_transport"),
    CSEQ("cseq"),
    LEN("len"),
    LOCAL_IP("local_ip"),
    LOCAL_PORT("local_port"),
    LOCAL_TAG("local_tag"),
    MEDIA_IP("media_ip"),
    MEDIA_PORT("media_port"),
    REMOTE_IP("remote_ip"),
    REMOTE_PORT("remote_port"),
    REMOTE_TAG("remote_tag"),
    TRANSPORT("transport"),
    LAST_CALL_ID("last_Call-ID:"),
    LAST_CSEQ("last_CSeq:"),
    LAST_FROM("last_From:"),
    LAST_TO("last_To:"),
    LAST_VIA("last_Via:"),
    RESPONSE_CSEQ("response_cseq"),
    RESPONSE_RSEQ("response_rseq");

    private final String name;

    RawSipMessageParameter(String name) {
       this.name = name;
   }

    public String getName() {
        return name;
    }
}
