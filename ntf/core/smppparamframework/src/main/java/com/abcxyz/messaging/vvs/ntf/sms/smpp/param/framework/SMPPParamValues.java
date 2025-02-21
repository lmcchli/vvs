/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework;

import java.util.List;

/**
 * The SMPPParamValues interface defines methods which are invoked by the NTF component to get values for SMPP mandatory and optional parameters
 * for submit_sm and data_sm PDUs.
 * <p>
 * To avoid forward compatibility issues, the SMPP Parameter plug-in should not implement this interface directly.  
 * Instead, the SMPP Parameter plug-in should extend the SMPP Parameter Framework {@link SMPPParamValuesDefaultImpl} class.
 * When new methods are added to the SMPPParamValues interface, the SMPPParamValuesDefaultImpl will provide default implementations for these new methods and
 * existing SMPP Parameter plug-ins will simply inherit these default implementations.  Thus, existing SMPP Parameter plug-ins will still compile
 * with the new SMPP Parameter framework.
 * <p>
 * If the SMPP Parameter plug-in needs to use values other than those that are used by the SMPPParamValuesDefaultImpl class, 
 * it will have to override the methods.
 * <p>
 * The methods for retrieving mandatory parameter values in this interface have a default value which is passed as a method argument by NTF.  
 * This default value is the value that NTF would use as the SMPP parameter value.
 * If the SMPP Parameter plug-in does not want to alter the value to be used, it should inherit the default implementation for the parameter
 * from the SMPPParamValuesDefaultImpl class.  
 * The SMPPParamValuesDefaultImpl methods to retrieve mandatory parameter values simply return the default value unchanged back to NTF. 
 * <p>
 * In order for the SMPP Parameter plug-in to be loaded by the NTF component, the SMPP Parameter plug-in class MUST have the following package and class name:<p>
 * <code>com.abcxyz.messaging.vvs.ntf.sms.smpp.param.plugin.SMPPParamValuesCustomImpl</code>
 */
public interface SMPPParamValues {
    
	/**
	 * Retrieves the service_type parameter value.
	 * @param pduEnum the PDU for which the parameter value is being retrieved
	 * @param defaultValue the default value that was set to be used by NTF
	 * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
	 * @return value of the service_type parameter
	 */
	public String getServiceType(SMPPPduEnum pduEnum, String defaultValue, SMPPSMSInfo smsInfo);

	/**
	 * Retrieves the source_addr_ton parameter value.
     * @param pduEnum the PDU for which the parameter value is being retrieved
     * @param defaultValue the default value that was set to be used by NTF
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return value of the source_addr_ton parameter
	 */
	public int getSourceAddrTon(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo);

	/**
	 * Retrieves the source_addr_npi parameter value.
     * @param pduEnum the PDU for which the parameter value is being retrieved
     * @param defaultValue the default value that was set to be used by NTF
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return value of the service_type parameter
	 */
	public int getSourceAddrNpi(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo);

	/**
	 * Retrieves the source_addr parameter value.
     * @param pduEnum the PDU for which the parameter value is being retrieved
     * @param defaultValue the default value that was set to be used by NTF
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return value of the source_addr parameter
	 */
	public String getSourceAddr(SMPPPduEnum pduEnum, String defaultValue, SMPPSMSInfo smsInfo);

	/**
	 * Retrieves the dest_addr_ton parameter value.
     * @param pduEnum the PDU for which the parameter value is being retrieved
     * @param defaultValue the default value that was set to be used by NTF
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return value of the dest_addr_ton parameter
	 */
	public int getDestAddrTon(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo);

	/**
	 * Retrieves the dest_addr_npi parameter value.
     * @param pduEnum the PDU for which the parameter value is being retrieved
     * @param defaultValue the default value that was set to be used by NTF
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
	 * @return value of the dest_addr_npi parameter.
	 */
	public int getDestAddrNpi(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo);

	/**
	 * Retrieves the destination_addr parameter value.
	 * <p>
	 * Destination numbers going to the SMSC from NTF (out-bound) can be changed with this method.
	 * If the implementation of this method does not return the defaultValue but instead returns a modified version of the defaultValue,
	 * the {@link SMPPParamValues#getDestinationAddrInbound(String, SMPPSMSInfo)} method should be implemented
	 * to reverse the change.
     * @param pduEnum the PDU for which the parameter value is being retrieved
     * @param defaultValue the default value that was set to be used by NTF
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return value of the destination_addr parameter.
	 */
	public String getDestinationAddrOutbound(SMPPPduEnum pduEnum, String defaultValue, SMPPSMSInfo smsInfo);

    /**
     * Retrieves the destination_addr parameter value in a format that is known to NTF.
     * <p>
     * Destination numbers coming into NTF from the SMSC (in-bound) can be normalized with this method. 
     * This method should be implemented to reverse the modifications made to the destination address by the 
     * {@link SMPPParamValues#getDestinationAddrOutbound(SMPPPduEnum, String, SMPPSMSInfo)} method.
     * @param number the number that was used as the destination_addr parameter value when sending to the SMSC
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return value of the destination_addr parameter in a format that is known to NTF
     */
    public String getDestinationAddrInbound(String number, SMPPSMSInfo smsInfo);

	/**
	 * Retrieves the esm_class parameter value.
     * @param pduEnum the PDU for which the parameter value is being retrieved
     * @param defaultValue the default value that was set to be used by NTF
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return value of the esm_class parameter
	 */
	public int getEsmClass(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo);

	/**
	 * Retrieves the protocol_id parameter value.
     * @param pduEnum the PDU for which the parameter value is being retrieved
     * @param defaultValue the default value that was set to be used by NTF
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return value of the protocol_id parameter
	 */
	public int getProtocolId(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo);

	/**
	 * Retrieves the priority_flag parameter value.
     * @param pduEnum the PDU for which the parameter value is being retrieved
     * @param defaultValue the default value that was set to be used by NTF
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return value of the priority_flag parameter
	 */
	public int getPriorityFlag(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo);

	/**
	 * Retrieves the schedule_delivery_time parameter value.
     * @param pduEnum the PDU for which the parameter value is being retrieved
     * @param defaultValue the default value that was set to be used by NTF
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return value of the schedule_delivery_time parameter
	 */
	public String getScheduleDeliveryTime(SMPPPduEnum pduEnum, String defaultValue, SMPPSMSInfo smsInfo);

	/**
	 * Retrieves the validity_period parameter value.
     * @param pduEnum the PDU for which the parameter value is being retrieved
     * @param defaultValue the default value that was set to be used by NTF
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return value of the validity_period parameter
	 */
	public String getValidityPeriod(SMPPPduEnum pduEnum, String defaultValue, SMPPSMSInfo smsInfo);

	/**
	 * Retrieves the registered_delivery parameter value.
     * @param pduEnum the PDU for which the parameter value is being retrieved
     * @param defaultValue the default value that was set to be used by NTF
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return value of the registered_delivery parameter
	 */
	public int getRegisteredDelivery(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo);

	/**
	 * Retrieves the replace_if_present_flag parameter value.
     * @param pduEnum the PDU for which the parameter value is being retrieved
     * @param defaultValue the default value that was set to be used by NTF
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return value of the replace_if_present_flag parameter
	 */
	public int getReplaceIfPresentFlag(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo);

	/**
	 * Retrieves the data_coding parameter value.
     * @param pduEnum the PDU for which the parameter value is being retrieved
     * @param defaultValue the default value that was set to be used by NTF
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return value of the data_coding parameter
	 */
	public int getDataCoding(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo);

	/**
	 * Retrieves the sm_default_msg_id parameter value.
     * @param pduEnum the PDU for which the parameter value is being retrieved
     * @param defaultValue the default value that was set to be used by NTF
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return value of the sm_default_msg_id parameter
	 */
	public int getSmDefaultMsgId(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo);

	/**
	 * Retrieves the sm_length parameter value.
     * @param pduEnum the PDU for which the parameter value is being retrieved
     * @param defaultValue the default value that was set to be used by NTF
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return value of the sm_length parameter
	 */
	public int getSmLength(SMPPPduEnum pduEnum, int defaultValue, SMPPSMSInfo smsInfo);

	/**
	 * Retrieves the short_message parameter value.
     * @param pduEnum the PDU for which the parameter value is being retrieved
     * @param defaultValue the default value that was set to be used by NTF
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return value of the short_message parameter
	 */
	public byte[] getShortMessage(SMPPPduEnum pduEnum, byte[] defaultValue, SMPPSMSInfo smsInfo);

	/**
	 * Retrieves the optional parameters, including vendor specific parameters, to be included in the SMPP PDU for the current message.
     * @param pduEnum the PDU for which the optional parameters are being retrieved
     * @param smsInfo the SMPPSMSInfo containing information about the current SMPP message
     * @return the optional parameters, including vendor specific parameters, to be included in the SMPP PDU for the current message
	 */
	public List<SMPPParamsTLV> getOptionalParametersTLVList(SMPPPduEnum pduEnum, SMPPSMSInfo smsInfo);

	/**
     * Returns whether validation of the parameter TLV values should be performed prior to writing the values to the data output stream.
     * This method is invoked by NTF to determine if it should do the validation.
     * @return true if validation of the parameter TLV values should be performed prior to writing the values to the data output stream; false otherwise
	 */
	public boolean isValidationDoneBeforeWritingTLV();
}
