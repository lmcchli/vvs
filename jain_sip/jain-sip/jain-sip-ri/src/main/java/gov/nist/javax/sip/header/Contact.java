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
/*
 * Bug reports contributed by Joao Paulo, Stephen Jones, 
 * John Zeng and Alstair Cole.
 *
 */
/*******************************************************************************
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import gov.nist.core.*;
import gov.nist.javax.sip.address.*;

import java.text.ParseException;
import javax.sip.InvalidArgumentException;
import javax.sip.header.ContactHeader;

/**
 * Contact Item. 
 *
 * @see gov.nist.javax.sip.header.ContactList
 *
 * @author M. Ranganathan  <br/>
 * @version 1.2 $Revision: 1.8 $ $Date: 2006/11/12 21:52:38 $
 * @since 1.1
 *
 *
 */
public final  class Contact
	extends AddressParametersHeader
	implements javax.sip.header.ContactHeader {
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1677294871695706288L;
	public static final String ACTION = ParameterNames.ACTION;
	public static final String PROXY = ParameterNames.PROXY;
	public static final String REDIRECT = ParameterNames.REDIRECT;
	public static final String EXPIRES = ParameterNames.EXPIRES;
	public static final String Q = ParameterNames.Q;

	// This must be private or the toString will go for a loop! 
	private ContactList contactList;

	/** wildCardFlag field.
	 */
	protected boolean wildCardFlag;

	/** Default constructor.
	 */
	public Contact() {
		super(NAME);
	}

	/** Set a parameter.
	*/
	public void setParameter(String name, String value) throws ParseException {
		NameValue nv = parameters.getNameValue(name);
		if (nv != null) {
			nv.setValue(value);
		} else {
			nv = new NameValue(name, value);
			if (name.equalsIgnoreCase("methods"))
				nv.setQuotedValue();
			this.parameters.set(nv);
		}
	}

	/**
	 * Encode body of the header into a cannonical String.
	 * @return string encoding of the header value.
	 */
	protected String encodeBody() {
		StringBuffer encoding = new StringBuffer();
		if (wildCardFlag) {
			return encoding.append("*").toString();
		}
		// Bug report by Joao Paulo
		if (address.getAddressType() == AddressImpl.NAME_ADDR) {
			encoding.append(address.encode());
		} else {
			// Encoding in canonical form must have <> around address.
			encoding.append("<").append(address.encode()).append(">");
		}
		if (!parameters.isEmpty()) {
			encoding.append(SEMICOLON).append(parameters.encode());
		}

		return encoding.toString();
	}

	/** get the Contact list.
	 * @return ContactList
	 */
	public ContactList getContactList() {
		return contactList;
	}

	/** get the WildCardFlag field
	 * @return boolean
	 */
	public boolean getWildCardFlag() {
		return wildCardFlag;
	}

	/** get the address field.
	 * @return Address
	 */
	public javax.sip.address.Address getAddress() {
		// JAIN-SIP stores the wild card as an address!
		return address;
	}

	/** get the parameters List
	 * @return NameValueList
	 */
	public NameValueList getContactParms() {
		return parameters;
	}

	/** get Expires parameter.
	 * @return the Expires parameter.
	 */
	public int getExpires() {
		return getParameterAsInt(EXPIRES);
	}

	/** Set the expiry time in seconds.
	*@param expiryDeltaSeconds exipry time.
	*/

	public void setExpires(int expiryDeltaSeconds) {
		Integer deltaSeconds = new Integer(expiryDeltaSeconds);
		this.parameters.set(EXPIRES, deltaSeconds);
	}

	/** get the Q-value
	 * @return float
	 */
	public float getQValue() {
		return getParameterAsFloat(Q);
	}

	/** set the Contact List
	 * @param cl ContactList to set
	 */
	public void setContactList(ContactList cl) {
		contactList = cl;
	}

	/**
	 * Set the wildCardFlag member
	 * @param w boolean to set
	 */
	public void setWildCardFlag(boolean w) {
		this.wildCardFlag = true;
		this.address = new AddressImpl();
		this.address.setWildCardFlag();
	}

	/**
	 * Set the address member
	 *
	 * @param address Address to set
	 */
	public void setAddress(javax.sip.address.Address address) {
		// Canonical form must have <> around the address.
		if (address == null)
			throw new NullPointerException("null address");
		this.address = (AddressImpl) address;
		this.wildCardFlag = false;
	}

	/**
	 * set the Q-value parameter
	 * @param qValue float to set
	 */
	public void setQValue(float qValue) throws InvalidArgumentException {
		if (qValue != -1 && (qValue < 0 || qValue > 1))
			throw new InvalidArgumentException(
				"JAIN-SIP Exception, Contact, setQValue(), "
					+ "the qValue is not between 0 and 1");
		this.parameters.set(Q, new Float(qValue));
	}
	
	public Object clone() {
		Contact retval = (Contact) super.clone();
		if (this.contactList != null)
			retval.contactList = (ContactList) this.contactList.clone();
		return retval;
	}

    /* (non-Javadoc)
     * @see javax.sip.header.ContactHeader#setWildCard()
     */
    public void setWildCard() {
       this.setWildCardFlag(true);
        
    }

    /* (non-Javadoc)
     * @see javax.sip.header.ContactHeader#isWildCard()
     */
    public boolean isWildCard() {
        
        return this.address.isWildcard();
    }

	public boolean equals(Object other) {
		return (other instanceof ContactHeader) && super.equals(other);
	}
    
}
