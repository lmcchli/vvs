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
 ******************************************************************************/
package gov.nist.javax.sip.header;

import gov.nist.core.*;
import gov.nist.javax.sip.address.*;
import java.text.ParseException;

import javax.sip.header.FromHeader;

/**
 * From SIP Header.
 *
 * @version 1.2 $Revision: 1.6 $ $Date: 2006/07/13 09:01:23 $
 * @since 1.1
 *
 * @author M. Ranganathan   <br/>
 *
 * 
 */
public final class From
	extends AddressParametersHeader
	implements javax.sip.header.FromHeader {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -6312727234330643892L;

	/** Default constructor
	 */
	public From() {
		super(NAME);
	}

	/** Generate a FROM header from a TO header
	 */
	public From(To to) {
		super(NAME);
		address = to.address;
		parameters = to.parameters;
	}

	/**
	 * Encode the header into a String.
	 *
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
		return address.getHostPort();
	}

	/**
	 * Get the display name from the address.
	 * @return Display name
	 */
	public String getDisplayName() {
		return address.getDisplayName();
	}

	/**
	 * Get the tag parameter from the address parm list.
	 * @return tag field
	 */
	public String getTag() {
		if (parameters == null)
			return null;
		return getParameter(ParameterNames.TAG);
	}

	/** Boolean function
	 * @return true if the Tag exist
	 */
	public boolean hasTag() {
		return hasParameter(ParameterNames.TAG);
	}

	/** remove Tag member
	 */
	public void removeTag() {
		parameters.delete(ParameterNames.TAG);
	}

	/**
	 * Set the address member
	 * @param address Address to set
	 */
	public void setAddress(javax.sip.address.Address address) {
		this.address = (AddressImpl) address;
	}

	/**
	 * Set the tag member
	 * @param t tag to set. From tags are mandatory.
	 */
	public void setTag(String t) throws ParseException {
		if (t == null)
			throw new NullPointerException("null tag ");
		else if (t.trim().equals(""))
			throw new ParseException("bad tag", 0);
		this.setParameter(ParameterNames.TAG, t);
	}

	/** Get the user@host port string.
	 */
	public String getUserAtHostPort() {
		return address.getUserAtHostPort();
	}
	
	public boolean equals(Object other) {
		return (other instanceof FromHeader) && super.equals(other);
	}
	
}
