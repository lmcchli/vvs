/**
 * Copyright (c) 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.cimd2;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.*;

/**
 * DeliverStatusReportRespMessage knows the format of a CIMD2 deliver status
 * report response message.
 */
public class DeliverStatusReportRespMessage extends CIMD2RespMessage {

    /**
     * Constructor.
     *@param conn The CIMD2Com used for this message.
     */
    DeliverStatusReportRespMessage(CIMD2Com conn) {
        super(conn);
        operationCode = CIMD2_DELIVER_STATUS_REPORT_RESP;
    }
}
