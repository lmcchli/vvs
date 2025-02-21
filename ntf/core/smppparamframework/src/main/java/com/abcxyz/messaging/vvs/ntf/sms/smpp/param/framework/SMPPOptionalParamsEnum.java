/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework;

import static com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework.SMPPOptionalParamConstants.*;

/**
 * The SMPPOptionalParamsEnum contains enum constants for all optional parameters in the SMPP Protocol Specification v3.4. 
 * Each enum constant includes the following information for the optional parameter it represents:<p>
 * - Tag value<p>
 * - Minimum allowed value length<p>
 * - Maximum allowed value length<p>
 * - Value range<p>
 * - Value type
 */
public enum SMPPOptionalParamsEnum {
    
	/**
	 * Represents the additional_status_info_text parameter.<p>
	 * Tag = 0x001D<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 256 octets<p>
	 * Range = N/A<p>
	 * Type = C-Octet String
	 */
	ADDITIONAL_STATUS_INFO_TEXT(SMPPTAG_ADDITIONAL_STATUS_INFO_TEXT, 1, 256, null, C_OCTET_STRING_TYPE),

	/**
	 * Represents the alert_on_message_delivery parameter.<p>
	 * Tag = 0x130C<p>
	 * Min allowed length = 0 octet<p>
	 * Max allowed length = 0 octet<p>
	 * Range = N/A<p>
	 * Type = N/A
	 */
	ALERT_ON_MESSAGE_DELIVERY(SMPPTAG_ALERT_ON_MESSAGE_DELIVERY, 0, 0, new int[][] { { 0, 0 } }, INT_TYPE),

	/**
     * Represents the callback_num parameter.<p>
	 * Tag = 0x0381<p>
	 * Min allowed length = 4 octets<p>
	 * Max allowed length = 19 octets<p>
	 * Range = N/A<p>
	 * Type = Octet String
	 */
	CALLBACK_NUM(SMPPTAG_CALLBACK_NUM, 4, 19, null, OCTET_STRING_TYPE),

	/**
     * Represents the callback_num_atag parameter.<p>
	 * Tag = 0x0303<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 65 octets<p>
	 * Range = N/A<p>
	 * Type = Octet String
	 */
	CALLBACK_NUM_ATAG(SMPPTAG_CALLBACK_NUM_ATAG, 1, 65, null, OCTET_STRING_TYPE),

	/**
     * Represents the callback_num_pres_ind parameter.<p>
	 * Tag = 0x0302<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = N/A<p>
	 * Type = Bit Mask
	 */
	CALLBACK_NUM_PRES_IND(SMPPTAG_CALLBACK_NUM_PRES_IND, 1, 1, null, BIT_MASK_TYPE),

	/**
     * Represents the delivery_failure_reason parameter.<p>
	 * Tag = 0x0425<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = [0-3]<p>
	 * Type = Int
	 */
	DELIVERY_FAILURE_REASON(SMPPTAG_DELIVERY_FAILURE_REASON, 1, 1, new int[][] { { 0, 3 } }, INT_TYPE),

	/**
     * Represents the dest_addr_subunit parameter.<p>
	 * Tag = 0x0005<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = [0-4]<p>
	 * Type = Int
	 */
	DEST_ADDR_SUBUNIT(SMPPTAG_DEST_ADDR_SUBUNIT, 1, 1, new int[][] { { 0, 4 } }, INT_TYPE),

	/**
     * Represents the dest_bearer_type parameter.<p>
	 * Tag = 0x0007<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = [0-8]<p>
	 * Type = Int
	 */
	DEST_BEARER_TYPE(SMPPTAG_DEST_BEARER_TYPE, 1, 1, new int[][] { { 0, 8 } }, INT_TYPE),

	/**
     * Represents the dest_network_type parameter.<p>
	 * Tag = 0x0006<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = [0-8]<p>
	 * Type = Int
	 */
	DEST_NETWORK_TYPE(SMPPTAG_DEST_NETWORK_TYPE, 1, 1, new int[][] { { 0, 8 } }, INT_TYPE),

	/**
     * Represents the dest_subaddress parameter.<p>
	 * Tag = 0x0203<p>
	 * Min allowed length = 2 octets<p>
	 * Max allowed length = 23 octets<p>
	 * Range = N/A<p>
	 * Type = Octet String
	 */
	DEST_SUBADDRESS(SMPPTAG_DEST_SUBADDRESS, 2, 23, null, OCTET_STRING_TYPE),

	/**
     * Represents the dest_telematics_id parameter.<p>
	 * Tag = 0x0008<p>
	 * Min allowed length = 2 octets<p>
	 * Max allowed length = 2 octets<p>
	 * Range = N/A<p>
	 * Type = Int
	 */
	DEST_TELEMATICS_ID(SMPPTAG_DEST_TELEMATICS_ID, 2, 2, null, INT_TYPE),

	/**
     * Represents the destination_port parameter.<p>
	 * Tag = 0x020B<p>
	 * Min allowed length = 2 octets<p>
	 * Max allowed length = 2 octets<p>
	 * Range = N/A<p>
	 * Type = Int
	 */
	DESTINATION_PORT(SMPPTAG_DESTINATION_PORT, 2, 2, null, INT_TYPE),

	/**
     * Represents the display_time parameter.<p>
	 * Tag = 0x1201<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = [1-2]<p>
	 * Type = Int
	 */
	DISPLAY_TIME(SMPPTAG_DISPLAY_TIME, 1, 1, new int[][] { { 1, 2 } }, INT_TYPE),

	/**
     * Represents the dpf_result parameter.<p>
	 * Tag = 0x0420<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = [0-1]<p>
	 * Type = Int
	 */
	DPF_RESULT(SMPPTAG_DPF_RESULT, 1, 1, new int[][] { { 0, 1 } }, INT_TYPE),

	/**
     * Represents the its_reply_type parameter.<p>
	 * Tag = 0x1380<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = [0-8]<p>
	 * Type = Int
	 */
	ITS_REPLY_TYPE(SMPPTAG_ITS_REPLY_TYPE, 1, 1, new int[][] { { 0, 8 } }, INT_TYPE),

	/**
     * Represents the its_session_info parameter.<p>
	 * Tag = 0x1383<p>
	 * Min allowed length = 2 octets<p>
	 * Max allowed length = 2 octets<p>
	 * Range = N/A<p>
	 * Type = Octet String
	 */
	ITS_SESSION_INFO(SMPPTAG_ITS_SESSION_INFO, 2, 2, null, OCTET_STRING_TYPE),

	/**
     * Represents the language_indicator parameter.<p>
	 * Tag = 0x020D<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = N/A<p>
	 * Type = Int
	 */
	LANGUAGE_INDICATOR(SMPPTAG_LANGUAGE_INDICATOR, 1, 1, null, INT_TYPE),

	/**
     * Represents the message_payload parameter.<p>
	 * Tag = 0x0424<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 64000 octets<p>
	 * Range = N/A<p>
	 * Type = Octet String
	 */
	MESSAGE_PAYLOAD(SMPPTAG_MESSAGE_PAYLOAD, 1, 64000, null, OCTET_STRING_TYPE),

	/**
     * Represents the more_messages_to_send parameter.<p>
	 * Tag = 0x0426<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = [0-1]<p>
	 * Type = Int
	 */
	MORE_MESSAGES_TO_SEND(SMPPTAG_MORE_MESSAGES_TO_SEND, 1, 1, new int[][] { { 0, 1 } }, INT_TYPE),

	/**
     * Represents the ms_availability_status parameter.<p>
	 * Tag = 0x0422<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = [0-2]<p>
	 * Type = Int
	 */
	MS_AVAILABILITY_STATUS(SMPPTAG_MS_AVAILABILITY_STATUS, 1, 1, new int[][] { { 0, 2 } }, INT_TYPE),

	/**
     * Represents the ms_msg_wait_facilities parameter.<p>
	 * Tag = 0x0030<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = N/A<p>
	 * Type = Bit Mask
	 */
	MS_MSG_WAIT_FACILITIES(SMPPTAG_MS_MSG_WAIT_FACILITIES, 1, 1, null, BIT_MASK_TYPE),

	/**
     * Represents the ms_validity parameter.<p>
	 * Tag = 0x1204<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = [0-3]<p>
	 * Type = Int
	 */
	MS_VALIDITY(SMPPTAG_MS_VALIDITY, 1, 1, new int[][] { { 0, 3 } }, INT_TYPE),

	/**
     * Represents the network_error_code parameter.<p>
	 * Tag = 0x0423<p>
	 * Min allowed length = 3 octets<p>
	 * Max allowed length = 3 octets<p>
	 * Range = N/A<p>
	 * Type = Octet String
	 */
	NETWORK_ERROR_CODE(SMPPTAG_NETWORK_ERROR_CODE, 3, 3, null, OCTET_STRING_TYPE),

	/**
     * Represents the number_of_messages parameter.<p>
	 * Tag = 0x0304<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = [0-99]<p>
	 * Type = Int
	 */
	NUMBER_OF_MESSAGES(SMPPTAG_NUMBER_OF_MESSAGES, 1, 1, new int[][] { { 0, 99 } }, INT_TYPE),

	/**
     * Represents the payload_type parameter.<p>
	 * Tag = 0x0019<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = [0-1]<p>
	 * Type = Int
	 */
	PAYLOAD_TYPE(SMPPTAG_PAYLOAD_TYPE, 1, 1, new int[][] { { 0, 1 } }, INT_TYPE),

	/**
     * Represents the privacy_indicator parameter.<p>
	 * Tag = 0x0201<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = [0-3]<p>
	 * Type = Int
	 */
	PRIVACY_INDICATOR(SMPPTAG_PRIVACY_INDICATOR, 1, 1, new int[][] { { 0, 3 } }, INT_TYPE),

	/**
     * Represents the qos_time_to_live parameter.<p>
	 * Tag = 0x0017<p>
	 * Min allowed length = 4 octets<p>
	 * Max allowed length = 4 octets<p>
	 * Range = N/A<p>
	 * Type = Int
	 */
	QOS_TIME_TO_LIVE(SMPPTAG_QOS_TIME_TO_LIVE, 4, 4, null, INT_TYPE),

	/**
     * Represents the receipted_message_id parameter.<p>
	 * Tag = 0x001E<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 65 octets<p>
	 * Range = N/A<p>
	 * Type = Octet String
	 */
	RECEIPTED_MESSAGE_ID(SMPPTAG_RECEIPTED_MESSAGE_ID, 1, 65, null, C_OCTET_STRING_TYPE),

	/**
     * Represents the sar_msg_ref_num parameter.<p>
	 * Tag = 0x020C<p>
	 * Min allowed length = 2 octets<p>
	 * Max allowed length = 2 octets<p>
	 * Range = N/A<p>
	 * Type = Int
	 */
	SAR_MSG_REF_NUM(SMPPTAG_SAR_MSG_REF_NUM, 2, 2, null, INT_TYPE),

	/**
     * Represents the sar_segment_seqnum parameter.<p>
	 * Tag = 0x020F<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = [1-255]<p>
	 * Type = Int
	 */
	SAR_SEGMENT_SEQNUM(SMPPTAG_SAR_SEGMENT_SEQNUM, 1, 1, new int[][] { { 1, 255 } }, INT_TYPE),

	/**
     * Represents the sar_total_segments parameter.<p>
	 * Tag = 0x020E<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = [1-255]<p>
	 * Type = Int
	 */
	SAR_TOTAL_SEGMENTS(SMPPTAG_SAR_TOTAL_SEGMENTS, 1, 1, new int[][] { { 1, 255 } }, INT_TYPE),

	/**
     * Represents the SC_interface_version parameter.<p>
	 * Tag = 0x0210<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = [0-52]<p>
	 * Type = Int
	 */
	SC_INTERFACE_VERSION(SMPPTAG_SC_INTERFACE_VERSION, 1, 1, new int[][] { { 0, 52 } }, INT_TYPE),

	/**
     * Represents the set_dpf parameter.<p>
	 * Tag = 0x0421<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = [0-1]<p>
	 * Type = Int
	 */
	SET_DPF(SMPPTAG_SET_DPF, 1, 1, new int[][] { { 0, 1 } }, INT_TYPE),

	/**
     * Represents the sms_signal parameter.<p>
	 * Tag = 0x1203<p>
	 * Min allowed length = 2 octets<p>
	 * Max allowed length = 2 octets<p>
	 * Range = N/A<p>
	 * Type = Int
	 */
	SMS_SIGNAL(SMPPTAG_SMS_SIGNAL, 2, 2, null, INT_TYPE),

	/**
     * Represents the source_addr_subunit parameter.<p>
	 * Tag = 0x000D<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = [0-4]<p>
	 * Type = Int
	 */
	SOURCE_ADDR_SUBUNIT(SMPPTAG_SOURCE_ADDR_SUBUNIT, 1, 1, new int[][] { { 0, 4 } }, INT_TYPE),

	/**
     * Represents the source_bearer_type parameter.<p>
	 * Tag = 0x000F<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = [0-8]<p>
	 * Type = Int
	 */
	SOURCE_BEARER_TYPE(SMPPTAG_SOURCE_BEARER_TYPE, 1, 1, new int[][] { { 0, 8 } }, INT_TYPE),

	/**
     * Represents the source_network_type parameter.<p>
	 * Tag = 0x000E<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = [0-8]<p>
	 * Type = Int
	 */
	SOURCE_NETWORK_TYPE(SMPPTAG_SOURCE_NETWORK_TYPE, 1, 1, new int[][] { { 0, 8 } }, INT_TYPE),

	/**
     * Represents the source_port parameter.<p>
	 * Tag = 0x020A<p>
	 * Min allowed length = 2 octets<p>
	 * Max allowed length = 2 octets<p>
	 * Range = N/A<p>
	 * Type = Int
	 */
	SOURCE_PORT(SMPPTAG_SOURCE_PORT, 2, 2, null, INT_TYPE),

	/**
     * Represents the source_subaddress parameter.<p>
	 * Tag = 0x0202<p>
	 * Min allowed length = 2 octets<p>
	 * Max allowed length = 23 octets<p>
	 * Range = N/A<p>
	 * Type = Octet String
	 */
	SOURCE_SUBADDRESS(SMPPTAG_SOURCE_SUBADDRESS, 2, 23, null, OCTET_STRING_TYPE),

	/**
     * Represents the user_message_reference parameter.<p>
	 * Tag = 0x0204<p>
	 * Min allowed length = 2 octets<p>
	 * Max allowed length = 2 octets<p>
	 * Range = N/A<p>
	 * Type = Int
	 */
	USER_MESSAGE_REFERENCE(SMPPTAG_USER_MESSAGE_REFERENCE, 2, 2, null, INT_TYPE),

	/**
     * Represents the user_response_code parameter.<p>
	 * Tag = 0x0205<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = N/A<p>
	 * Type = Int
	 */
	USER_RESPONSE_CODE(SMPPTAG_USER_RESPONSE_CODE, 1, 1, null, INT_TYPE),

	/**
     * Represents the ussd_service_op parameter.<p>
	 * Tag = 0x0501<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = N/A<p>
	 * Type = Octet String
	 */
	USSD_SERVICE_OP(SMPPTAG_USSD_SERVICE_OP, 1, 1, null, OCTET_STRING_TYPE),

	/**
     * Represents the message_state parameter.<p>
	 * Tag = 0x0427<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = [0-8]<p>
	 * Type = Int
	 */
	MESSAGE_STATE(SMPPTAG_MESSAGE_STATE, 1, 1, new int[][] { { 0, 8 } }, INT_TYPE),

	/**
     * Represents the source_telematics_id parameter.<p>
	 * Tag = 0x0010<p>
	 * Min allowed length = 1 octet<p>
	 * Max allowed length = 1 octet<p>
	 * Range = N/A<p>
	 * Type = Int
	 */
	SOURCE_TELEMATICS_ID(SMPPTAG_SOURCE_TELEMATICS_ID, 1, 1, null, INT_TYPE);

	private int tag = 0;
	private int length = 0;
	private int minValueSize = 0;
	private int maxValueSize = 0;
	private int valueType = 0;
	private boolean isValueSizeVariable = false;
	private boolean isValueRangeAvailable = false;
	private int[][] valueRange = null;

	/**
	 * Constructs a SMPPOptionalParamsEnum constant.
	 * @param tag the tag value of the optional parameter.
	 * @param minValueSize the minimum size (in octets) that the value container can be.
	 * @param maxValueSize the maximum size (in octets) that the value container can be.
	 * @param valueRange the acceptable range of values.
	 * @param type the integer representing the value type (integer, octet string, c-octet string or bit mask).
	 */
	SMPPOptionalParamsEnum(int tag, int minValueSize, int maxValueSize, int[][] valueRange, int type) {
		this.tag = tag;
		this.minValueSize = minValueSize;
		this.maxValueSize = maxValueSize;
		this.valueRange = valueRange;
		this.valueType = type;

		if (minValueSize != maxValueSize) {
			this.isValueSizeVariable = true;
		} else {
			this.length = minValueSize;
		}

		if (this.valueRange != null) {
			this.isValueRangeAvailable = true;
		}
	}

	/**
	 * Retrieves the tag value of the optional parameter
	 * @return the tag value of the optional parameter
	 */
	public int getTag() {
		return this.tag;
	}

	/**
	 * Retrieves the fixed value container length/size (in octets) for this optional parameter.
	 * @return the fixed value container length/size (in octets) for this optional parameter; 0 if the value length/size is variable
	 */
	public int getLength() {
		return this.length;
	}

	/**
	 * Retrieves the maximum allowed size (in octets) of the value container
	 * @return the maximum allowed size (in octets) of the value container
	 */
	public int getMaxValueSize() {
		return this.maxValueSize;
	}

	/**
	 * Retrieves the minimum allowed size (in octets) of the value container.
	 * @return the minimum allowed size (in octets) of the value container
	 */
	public int getMinValueSize() {
		return this.minValueSize;
	}

	/**
	 * Retrieves a two dimensional array containing the ranges of acceptable values.
	 * <p>
	 * Each array in the array of arrays represents a range and has 2 elements.  
	 * The first element is the lower limit of the range.
	 * The second element is the upper limit of the range.
	 * @return a two dimensional array containing the ranges of acceptable values
	 */
	public int[][] getValueRange() {
		return this.valueRange;
	}

	/**
	 * Retrieves the integer representing the value type for this optional parameter.
	 * <p>
	 * This method can return the following integer values:<p>
	 * - {@link SMPPOptionalParamConstants#INT_TYPE}<p>
	 * - {@link SMPPOptionalParamConstants#OCTET_STRING_TYPE}<p>
	 * - {@link SMPPOptionalParamConstants#C_OCTET_STRING_TYPE}<p>
	 * - {@link SMPPOptionalParamConstants#BIT_MASK_TYPE}<p>
	 * @return the integer representing the value type for this optional parameter
	 */
	public int getValueType() {
		return this.valueType;
	}

	/**Retrieves whether the size of the value can be variable.
	 * <p>
	 * The size is variable if the value's minimum allowed size is not equal to the value's maximum allowed size.
	 * The size is not variable (fixed) if the value's minimum allowed size is equal to the value's maximum allowed size.
	 * @return true if the size of the value can be variable; false if the size of the value is fixed
	 */
	public boolean isValueSizeVariable() {
		return this.isValueSizeVariable;
	}

	/**
	 * Retrieves whether there is a defined range for the value of this optional parameter.
	 * @return true if there is a defined range for the value of this optional parameter; false otherwise
	 */
	public boolean isValueRangeAvailable() {
		return this.isValueRangeAvailable;
	}
}
