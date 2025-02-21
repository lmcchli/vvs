/*
 * Copyright (c) 2007 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.component.tools.siptesttool.utilities;

/**
 * This class contains constants made available by the SIP test tool to use in
 * "component tests".
 * @author Malin Nyfeldt
 */
public abstract class SipToolConstants {

    /** Requests */

    public static String inviteRequest =
            "INVITE sip:[called]@[remote_ip]:[remote_port] SIP/2.0\r\n" +
            "Via: SIP/2.0/[transport] [local_ip]:[local_port];branch=[branch]\r\n" +
            "From: sipp <sip:sipp@[local_ip]:[local_port]>;tag=[local_tag]\r\n" +
            "To: sut <sip:[called]@[remote_ip]:[remote_port]>\r\n" +
            "Call-ID: [call_id]\r\n" +
            "CSeq: [cseq] INVITE\r\n" +
            "Contact: sip:sipp@[local_ip]:[local_port]\r\n" +
            "Max-Forwards: 70\r\n" +
            "Content-Type: application/sdp\r\n" +
            "Content-Length: [len]\r\n" +
            "\r\n" +
            "v=0\r\n" +
            "o=user1 53655765 2353687637 IN IP4 [local_ip]\r\n" +
            "s=-\r\n" +
            "c=IN IP4 [media_ip]\r\n" +
            "t=0 0\r\n" +
            "m=audio [media_port] RTP/AVP 0 101\r\n" +
            "a=rtpmap:0 PCMU/8000\r\n" +
            "a=rtpmap:101 telephone-event/8000\r\n" +
            "a=fmtp:101 0-15\r\n";

    public static String inviteRequestIndicatingEarlyMedia =
        "INVITE sip:[called]@[remote_ip]:[remote_port] SIP/2.0\r\n" +
        "Via: SIP/2.0/[transport] [local_ip]:[local_port];branch=[branch]\r\n" +
        "From: sipp <sip:sipp@[local_ip]:[local_port]>;tag=[local_tag]\r\n" +
        "To: sut <sip:[called]@[remote_ip]:[remote_port]>\r\n" +
        "Call-ID: [call_id]\r\n" +
        "CSeq: [cseq] INVITE\r\n" +
        "Supported: 100rel\r\n" +
        "Allow: INVITE, OPTIONS, BYE, CANCEL, ACK, PRACK, NOTIFY\r\n" +
        "Contact: sip:sipp@[local_ip]:[local_port]\r\n" +
        "Max-Forwards: 70\r\n" +
        "Content-Type: application/sdp\r\n" +
        "Content-Length: [len]\r\n" +
        "\r\n" +
        "v=0\r\n" +
        "o=user1 53655765 2353687637 IN IP4 [local_ip]\r\n" +
        "s=-\r\n" +
        "c=IN IP4 [media_ip]\r\n" +
        "t=0 0\r\n" +
        "m=audio [media_port] RTP/AVP 0 101\r\n" +
        "a=rtpmap:0 PCMU/8000\r\n" +
        "a=rtpmap:101 telephone-event/8000\r\n" +
        "a=fmtp:101 0-15\r\n";

    public static String ackRequest =
            "ACK sip:[called]@[remote_ip]:[remote_port] SIP/2.0\r\n" +
            "Via: SIP/2.0/[transport] [local_ip]:[local_port];branch=[branch]\r\n" +
            "From: sipp <sip:sipp@[local_ip]:[local_port]>;tag=[local_tag]\r\n" +
            "To: sut <sip:[called]@[remote_ip]:[remote_port]>;tag=[remote_tag]\r\n" +
            "Call-ID: [call_id]\r\n" +
            "CSeq: [cseq] ACK\r\n" +
            "Contact: sip:sipp@[local_ip]:[local_port]\r\n" +
            "Max-Forwards: 70\r\n" +
            "Content-Length: 0\r\n" +
            "\r\n";

    public static String ackRequestSameTransaction =
        "ACK sip:[called]@[remote_ip]:[remote_port] SIP/2.0\r\n" +
        "[last_Via:]\r\n" +
        "[last_From:]\r\n" +
        "[last_To:]\r\n" +
        "[last_Call-ID:]\r\n" +
        "CSeq: [cseq] ACK\r\n" +
        "Contact: sip:sipp@[local_ip]:[local_port]\r\n" +
        "Max-Forwards: 70\r\n" +
        "Content-Length: 0\r\n" +
        "\r\n";

    public static String prackRequest =
        "PRACK sip:[called]@[remote_ip]:[remote_port] SIP/2.0\r\n" +
        "Via: SIP/2.0/[transport] [local_ip]:[local_port];branch=[branch]\r\n" +
        "From: sipp <sip:sipp@[local_ip]:[local_port]>;tag=[local_tag]\r\n" +
        "To: sut <sip:[called]@[remote_ip]:[remote_port]>;tag=[remote_tag]\r\n" +
        "Call-ID: [call_id]\r\n" +
        "CSeq: [cseq] PRACK\r\n" +
        "RAck: [response_rseq] [response_cseq] INVITE\r\n" +
        "Max-Forwards: 70\r\n" +
        "Content-Length: 0\r\n" +
        "\r\n";

    public static String byeRequestForInboundCall =
            "BYE sip:[called]@[remote_ip]:[remote_port] SIP/2.0\r\n" +
            "Via: SIP/2.0/[transport] [local_ip]:[local_port];branch=[branch]\r\n" +
            "From: sipp <sip:sipp@[local_ip]:[local_port]>;tag=[local_tag]\r\n" +
            "To: sut <sip:[called]@[remote_ip]:[remote_port]>;tag=[remote_tag]\r\n" +
            "Call-ID: [call_id]\r\n" +
            "CSeq: [cseq] BYE\r\n" +
            "Contact: sip:sipp@[local_ip]:[local_port]\n" +
            "Max-Forwards: 70\r\n" +
            "Content-Length: 0\r\n" +
            "\r\n";

    public static String byeRequestForOutboundCall =
            "BYE sip:[called]@[remote_ip]:[remote_port] SIP/2.0\r\n" +
            "Via: SIP/2.0/[transport] [local_ip]:[local_port];branch=[branch]\r\n" +
            "From: sipp <sip:[calling]@[local_ip]:[local_port]>;tag=[local_tag]\r\n" +
            "To: sut <sip:[called]@[remote_ip]:[remote_port]>;tag=[remote_tag]\r\n" +
            "Call-ID: [call_id]\r\n" +
            "CSeq: [cseq] BYE\r\n" +
            "Contact: sip:sipp@[local_ip]:[local_port]\n" +
            "Max-Forwards: 70\r\n" +
            "Content-Length: 0\r\n" +
            "\r\n";


    public static String optionsRequestForInboundCall =
            "OPTIONS sip:[called]@[remote_ip]:[remote_port] SIP/2.0\r\n" +
            "Via: SIP/2.0/[transport] [local_ip]:[local_port];branch=[branch]\r\n" +
            "From: sipp <sip:sipp@[local_ip]:[local_port]>;tag=[local_tag]\r\n" +
            "To: sut <sip:[called]@[remote_ip]:[remote_port]>;tag=[remote_tag]\r\n" +
            "Call-ID: [call_id]\r\n" +
            "CSeq: [cseq] OPTIONS\r\n" +
            "Contact: sip:sipp@[local_ip]:[local_port]\n" +
            "Max-Forwards: 70\r\n" +
            "Content-Length: 0\r\n" +
            "\r\n";

    /** Responses */

    public static String okResponseForBye =
            "SIP/2.0 200 OK\r\n" +
            "[last_Via:]\r\n" +
            "[last_From:]\r\n" +
            "[last_To:]\r\n" +
            "[last_Call-ID:]\r\n" +
            "[last_CSeq:]\r\n" +
            "Contact: <sip:[local_ip]:[local_port]>\r\n" +
            "Content-Length: 0\r\n" +
            "\r\n";

    public static String tryingResponse =
            "SIP/2.0 100 Trying\r\n" +
            "[last_Via:]\r\n" +
            "[last_From:]\r\n" +
            "[last_To:];tag=[local_tag]\r\n" +
            "[last_Call-ID:]\r\n" +
            "[last_CSeq:]\r\n" +
            "Contact: <sip:[local_ip]:[local_port];transport=[contact_transport]>\r\n" +
            "Content-Length: 0\r\n" +
            "\r\n";

    public static String ringingResponse =
            "SIP/2.0 180 Ringing\r\n" +
            "[last_Via:]\r\n" +
            "[last_From:]\r\n" +
            "[last_To:];tag=[local_tag]\r\n" +
            "[last_Call-ID:]\r\n" +
            "[last_CSeq:]\r\n" +
            "Contact: <sip:[local_ip]:[local_port];transport=[contact_transport]>\r\n" +
            "Content-Length: 0\r\n" +
            "\r\n";

    public static String okResponseForInvite =
            "SIP/2.0 200 OK\r\n" +
            "[last_Via:]\r\n" +
            "[last_From:]\r\n" +
            "[last_To:];tag=[local_tag]\r\n" +
            "[last_Call-ID:]\r\n" +
            "[last_CSeq:]\r\n" +
            "Contact: <sip:[local_ip]:[local_port];transport=[contact_transport]>\r\n" +
            "Content-Type: application/sdp\r\n" +
            "Content-Length: [len]\r\n" +
            "\r\n" +
            "v=0\r\n" +
            "o=user1 53655765 2353687637 IN IP4 [local_ip]\r\n" +
            "s=-\r\n" +
            "c=IN IP4 [media_ip]\r\n" +
            "t=0 0\r\n" +
            "m=audio [media_port] RTP/AVP 0 101\r\n" +
            "a=rtpmap:0 PCMU/8000\r\n" +
            "a=rtpmap:101 telephone-event/8000\r\n" +
            "a=fmtp:101 0-15\r\n";
}
