/**
 * Copyright (c) Abcxyz. All Rights Reserved.
 */
package com.abcxyz.messaging.vvs.ntf.sms.smpp.param.framework;


/**
 * The SMPPSMSInfo interface defines methods that the SMPP Parameter plug-in can invoke to get information
 * about the current SMPP message.
 * <p>
 * The SMPP Parameter plug-in can use the information to customize values for SMPP PDU parameters.
 */
public interface SMPPSMSInfo {
    
	/**
	 * Retrieves whether there are more message fragments to be sent. 
	 * There are more fragments to be sent if there is still some content in the message text which has not been sent yet.
	 * @return true if there are more message fragments to be sent; false otherwise
	 */
	public boolean hasMoreFragments();

	/**
	 * Retrieves the number of new messages in the subscriber's mailbox.
	 * @return the message count
	 */
	public int getCount();

	/**
	 * Retrieves the alert value for the message.
	 * The alert value indicates if the mobile should alert the user when the notification arrives.
	 * @return the alert value of the message
	 */
	public int getAlert();

	/**
	 * Retrieves the value for the SMPP service_type parameter.
	 * @return value of the SMPP service_type parameter
	 */
	public String getServiceType();

	/**
	 * Retrieves whether there is a request to set the delivery pending flag for delivery failure.
	 * @return true if the SMPP set_dpf parameter should be 1 indicating that there is a request to set the delivery pending flag for delivery failure;<BR> 
	 *         false if the SMPP set_dpf parameter should be 0 indicating that there is no request to set the delivery pending flag for delivery failure
	 */
	public boolean getSetDpf();

	/**
	 * Retrieves the length of the user data header.
	 * @return the length of the user data header
	 */
	public int getUDHLength();

	/**
	 * Retrieves the user data header in a byte array.
	 * @return the user data header in a byte array
	 */
	public byte[] getUDHBytes();

    /**
     * Retrieves the user data which contains the message text.
     * @param udhData the byte array containing the user data header
     * @return the user data in a byte array
     */
    public byte[] getUserData(byte[] udhData);

	/**
	 * Retrieves the integer value representing the type of number of the source address.
	 * @return the integer value representing the type of number of the source address
	 */
	public int getSourceTON();

	/**
	 * Retrieves the integer value representing the numbering plan indicator of the source address.
	 * @return the integer value representing the numbering plan indicator of the source address
	 */
	public int getSourceNPI();

	/**
	 * Retrieves the number of the source address.
	 * @return the number of the source address
	 */
	public String getSourceNumber();

	/**
	 * Normalises the given number according to the normalisation rules found under the given context in the configuration (formattingrules.conf).
	 * @param number the number that is to be normalised
	 * @param context the context to use when determining the rules (regular expressions) to apply to the number in order to normalise it
	 * @param useDefaultContext boolean which specifies whether the configured default context is to be used in the event that
	 *            the given context does not exist in the configuration file
	 * @return the normalised number
	 */
	public String normalize(String number, String context, boolean useDefaultContext);
}
