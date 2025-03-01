/*
 * Conditions Of Use 
 * 
 * This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government.
 * Pursuant to title 15 Untied States Code Section 105, works of NIST
 * employees are not subject to copyright protection in the United States
 * and are considered to be in the public domain.  As a result, a formal
 * license is not needed to use the software.
 * 
 * This software is provided by NIST as a service and is expressly
 * provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
 * OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
 * AND DATA ACCURACY.  NIST does not warrant or make any representations
 * regarding the use of the software or the results thereof, including but
 * not limited to the correctness, accuracy, reliability or usefulness of
 * the software.
 * 
 * Permission to use this software is contingent upon your acceptance
 * of the terms of this agreement
 *  
 * .
 * 
 */
/*******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).       *
 *******************************************************************************/
package gov.nist.javax.sip.header;

import gov.nist.core.*;
import gov.nist.javax.sip.address.*;
import java.text.ParseException;

import javax.sip.header.ToHeader;

/**
 * To SIP Header.
 * 
 * @version 1.2 $Revision: 1.8 $ $Date: 2006/11/01 02:23:02 $
 * 
 * @author M. Ranganathan <br/>
 * @author Olivier Deruelle <br/>
 * 
 * 
 * 
 */

public final class To extends AddressParametersHeader implements
		javax.sip.header.ToHeader {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -4057413800584586316L;

	/**
	 * default Constructor.
	 */
	public To() {
		super(TO,true);
	}

	/**
	 * Generate a TO header from a FROM header
	 */
	public To(From from) {
		super(TO);
		setAddress(from.address);
		setParameters(from.parameters);
	}

	/**
	 * Encode the header into a String.
	 * 
	 * @since 1.0
	 * @return String
	 */
	public String encode() {
		return headerName + COLON + SP + encodeBody() + NEWLINE;
	}

	/**
	 * Encode the header content into a String.
	 * 
	 * @return String
	 */
	protected String encodeBody() {
		if (address == null)
			return null;
		String retval = "";
		if (address.getAddressType() == AddressImpl.ADDRESS_SPEC) {
			retval += LESS_THAN;
		}
		retval += address.encode();
		if (address.getAddressType() == AddressImpl.ADDRESS_SPEC) {
			retval += GREATER_THAN;
		}

		if (!parameters.isEmpty()) {
				retval += SEMICOLON + parameters.encode();
			
		}
		return retval;
	}

	/**
	 * Conveniance accessor function to get the hostPort field from the address.
	 * Warning -- this assumes that the embedded URI is a SipURL.
	 * 
	 * @return hostport field
	 */
	public HostPort getHostPort() {
		if (address == null)
			return null;
		return address.getHostPort();
	}

	/**
	 * Get the display name from the address.
	 * 
	 * @return Display name
	 */
	public String getDisplayName() {
		if (address == null)
			return null;
		return address.getDisplayName();
	}

	/**
	 * Get the tag parameter from the address parm list.
	 * 
	 * @return tag field
	 */
	public String getTag() {
		if (parameters == null)
			return null;
		return getParameter(ParameterNames.TAG);

	}

	/**
	 * Boolean function
	 * 
	 * @return true if the Tag exist
	 */
	public boolean hasTag() {
		if (parameters == null)
			return false;
		return hasParameter(ParameterNames.TAG);

	}

	/**
	 * remove Tag member
	 */
	public void removeTag() {
			if (parameters != null)
				parameters.delete(ParameterNames.TAG);
	
	}

	/**
	 * Set the tag member. This should be set to null for the initial request in
	 * a dialog.
	 * 
	 * @param t
	 *            tag String to set.
	 */
	public void setTag(String t) throws ParseException {
			if (t == null)
				throw new NullPointerException("null tag ");
			else if (t.trim().equals(""))
				throw new ParseException("bad tag", 0);
			this.setParameter(ParameterNames.TAG, t);
		
	}

	/**
	 * Get the user@host port string.
	 */
	public String getUserAtHostPort() {
		if (address == null)
			return null;
		return address.getUserAtHostPort();
	}

	public boolean equals(Object other) {
		return (other instanceof ToHeader) && super.equals(other);
	}
}
