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
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.javax.sip.header;

import gov.nist.javax.sip.address.*;

/**
 * RequestLine of SIP Request. 
 *
 * @version 1.2 $Revision: 1.5 $ $Date: 2006/07/13 09:01:26 $
 * @author M. Ranganathan
 */
public class RequestLine extends SIPObject {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -3286426172326043129L;

	/** uri field. Note that this can be a SIP URI or a generic URI 
	* like tel URI.
	 */
	protected GenericURI uri;

	/** method field.
	 */
	protected String method;

	/** sipVersion field
	 */
	protected String sipVersion;

	/** Default constructor
	 */
	public RequestLine() {
		sipVersion = "SIP/2.0";
	}

	/** Set the SIP version.
	*@param sipVersion -- the SIP version to set.
	*/
	public void setSIPVersion(String sipVersion) {
		this.sipVersion = sipVersion;
	}

	/** Encode the request line as a String.
	*
	 * @return requestLine encoded as a string.
	 */
	public String encode() {
		StringBuffer encoding = new StringBuffer();
		if (method != null) {
			encoding.append(method);
			encoding.append(SP);
		}
		if (uri != null) {
			encoding.append(uri.encode());
			encoding.append(SP);
		}
		encoding.append(sipVersion + NEWLINE);
		return encoding.toString();
	}

	/** get the Request-URI.
	 *
	     * @return the request URI 
	     */
	public GenericURI getUri() {
		return uri;
	}

	/** Constructor given the request URI and the method.
	*/
	public RequestLine(GenericURI requestURI, String method) {
		this.uri = requestURI;
		this.method = method;
		this.sipVersion = "SIP/2.0";
	}

	/**
	     * Get the Method
	 *
	     * @return method string.
	     */
	public String getMethod() {
		return method;
	}

	/**
	     * Get the SIP version.
	 *
	     * @return String
	     */
	public String getSipVersion() {
		return sipVersion;
	}

	/**
	     * Set the uri member.
	     * @param uri URI to set.
	     */
	public void setUri(GenericURI uri) {
		this.uri = uri;
	}

	/**
	     * Set the method member
	 *
	     * @param method String to set
	     */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	     * Set the sipVersion member
	 *
	     * @param s String to set
	     */
	public void setSipVersion(String s) {
		sipVersion = s;
	}

	/**
	* Get the major verrsion number.
	*
	*@return String major version number
	*/
	public String getVersionMajor() {
		if (sipVersion == null)
			return null;
		String major = null;
		boolean slash = false;
		for (int i = 0; i < sipVersion.length(); i++) {
			if (sipVersion.charAt(i) == '.')
				break;
			if (slash) {
				if (major == null)
					major = "" + sipVersion.charAt(i);
				else
					major += sipVersion.charAt(i);
			}
			if (sipVersion.charAt(i) == '/')
				slash = true;
		}
		return major;
	}

	/**
	 * Get the minor version number.
	*
	*@return String minor version number
	*
	*/
	public String getVersionMinor() {
		if (sipVersion == null)
			return null;
		String minor = null;
		boolean dot = false;
		for (int i = 0; i < sipVersion.length(); i++) {
			if (dot) {
				if (minor == null)
					minor = "" + sipVersion.charAt(i);
				else
					minor += sipVersion.charAt(i);
			}
			if (sipVersion.charAt(i) == '.')
				dot = true;
		}
		return minor;
	}

	/**
	* Compare for equality.
	*
	*@param other object to compare with. We assume that all fields 
	* are set.
	*/
	public boolean equals(Object other) {
		boolean retval;
		if (!other.getClass().equals(this.getClass())) {
			return false;
		}
		RequestLine that = (RequestLine) other;
		try {
			retval =
				this.method.equals(that.method)
					&& this.uri.equals(that.uri)
					&& this.sipVersion.equals(that.sipVersion);
		} catch (NullPointerException ex) {
			retval = false;
		}
		return retval;
	}

	public Object clone() {
		RequestLine retval = (RequestLine) super.clone();
		if (this.uri != null)
			retval.uri = (GenericURI) this.uri.clone();
		return retval;
	}
}
/*
 * $Log: RequestLine.java,v $
 * Revision 1.5  2006/07/13 09:01:26  mranga
 * Issue number:
 * Obtained from:
 * Submitted by:  jeroen van bemmel
 * Reviewed by:   mranga
 * Moved some changes from jain-sip-1.2 to java.net
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 * Revision 1.3  2006/06/19 06:47:26  mranga
 * javadoc fixups
 *
 * Revision 1.2  2006/06/16 15:26:28  mranga
 * Added NIST disclaimer to all public domain files. Clean up some javadoc. Fixed a leak
 *
 * Revision 1.1.1.1  2005/10/04 17:12:35  mranga
 *
 * Import
 *
 *
 * Revision 1.3  2005/04/16 20:38:50  dmuresan
 * Canonical clone() implementations for the GenericObject and GenericObjectList hierarchies
 *
 * Revision 1.2  2004/01/22 13:26:29  sverker
 * Issue number:
 * Obtained from:
 * Submitted by:  sverker
 * Reviewed by:   mranga
 *
 * Major reformat of code to conform with style guide. Resolved compiler and javadoc warnings. Added CVS tags.
 *
 * CVS: ----------------------------------------------------------------------
 * CVS: Issue number:
 * CVS:   If this change addresses one or more issues,
 * CVS:   then enter the issue number(s) here.
 * CVS: Obtained from:
 * CVS:   If this change has been taken from another system,
 * CVS:   then name the system in this line, otherwise delete it.
 * CVS: Submitted by:
 * CVS:   If this code has been contributed to the project by someone else; i.e.,
 * CVS:   they sent us a patch or a set of diffs, then include their name/email
 * CVS:   address here. If this is your work then delete this line.
 * CVS: Reviewed by:
 * CVS:   If we are doing pre-commit code reviews and someone else has
 * CVS:   reviewed your changes, include their name(s) here.
 * CVS:   If you have not had it reviewed then delete this line.
 *
 */
