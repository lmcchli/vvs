/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework;

import java.util.List;

/**
 * The SMPPPduEnum enumerates all possible Protocol Data Units (PDUs) from the SMPP Protocol Specification v3.4.
 */
public enum SMPPPduEnum {
	/** Represents the bind_transmitter PDU */
	BIND_TRANSMITTER,

	/** Represents the bind_transmitter_resp PDU */
	BIND_TRANSMITTER_RESP,

	/** Represents the bind_receiver PDU */
	BIND_RECEIVER,

	/** Represents the bind_receiver_resp PDU */
	BIND_RECEIVER_RESP,

	/** Represents the bind_transceiver PDU */
	BIND_TRANSCEIVER,

	/** Represents the bind_transceiver_resp PDU */
	BIND_TRANSCEIVER_RESP,

	/** Represents the unbind PDU */
	UNBIND,

	/** Represents the unbind_resp PDU */
	UNBIND_RESP,

	/** Represents the generic_nack PDU */
	GENERIC_NACK,

	/** Represents the submit_sm PDU */
	SUBMIT_SM,

	/** Represents the submit_sm_resp PDU */
	SUBMIT_SM_RESP,

	/** Represents the submit_multi PDU */
	SUBMIT_MULTI,

	/** Represents the submit_multi_resp PDU */
	SUBMIT_MULTI_RESP,

	/** Represents the deliver_sm PDU */
	DELIVER_SM,

	/** Represents the deliver_sm_resp PDU */
	DELIVER_SM_RESP,

	/** Represents the data_sm PDU */
	DATA_SM,

	/** Represents the data_sm_resp PDU */
	DATA_SM_RESP,

	/** Represents the query_sm PDU */
	QUERY_SM,

	/** Represents the query_sm_resp PDU */
	QUERY_SM_RESP,

	/** Represents the cancel_sm PDU */
	CANCEL_SM,

	/** Represents the cancel_sm_resp PDU */
	CANCEL_SM_RESP,

	/** Represents the replace_sm PDU */
	REPLACE_SM,

	/** Represents the replace_sm_resp PDU */
	REPLACE_SM_RESP,

	/** Represents the enquire_link PDU */
	ENQUIRE_LINK,

	/** Represents the enquire_link_resp PDU */
	ENQUIRE_LINK_RESP,

	/** Represents the alert_notification PDU */
	ALERT_NOTIFICATION;

}
