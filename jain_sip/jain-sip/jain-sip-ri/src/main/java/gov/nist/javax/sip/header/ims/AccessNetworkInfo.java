/*
 * Conditions Of Use 
 * 
 * This software was developed by employees of the National Institute of
 * Standards and Technology (NIST), an agency of the Federal Government,
 * and others.
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
 * of the terms of this agreement.
 * 
 */
/*****************************************************************************
 * PRODUCT OF PT INOVAO - EST DEPARTMENT and Aveiro University - Portugal)   *
 *****************************************************************************/

package gov.nist.javax.sip.header.ims;

import java.text.ParseException;

import javax.sip.header.ExtensionHeader;

import gov.nist.javax.sip.header.ParametersHeader;

/**
 * Private-Header P-Access-Network-Info
 * 
 * @author Jose Miguel Freitas
 * 
 * @since 1.2
 */

public class AccessNetworkInfo extends ParametersHeader implements
		AccessNetworkInfoHeader, ExtensionHeader {

	private String accessType;

	// protected String extendAccessInfo;
	private Object extendAccessInfo;

	/**
	 * Public constructor.
	 */
	public AccessNetworkInfo() {
		super(AccessNetworkInfoHeader.NAME);
		parameters.setSeparator(SEMICOLON);
	}

	/**
	 * Constructor.
	 */
	public AccessNetworkInfo(String accessTypeVal) {
		this();
		setAccessType(accessTypeVal);
	}

	/**
	 * Set the accessTpe
	 * 
	 * @param accessTypeVal
	 *            access type
	 * @throws NullPointerException
	 */
	public void setAccessType(String accessTypeVal) {
		if (accessTypeVal == null)
			throw new NullPointerException(
					"JAIN-SIP Exception, "
							+ "P-Access-Network-Info, setAccessType(), the accessType parameter is null.");

		this.accessType = accessTypeVal;
	}

	/**
	 * @return String access type
	 */
	public String getAccessType() {
		return accessType;
	}

	/**
	 * 
	 * @param cgi -- String
	 *            CGI value
	 * @throws NullPointerException -- if null argument passed in
	 * @throws ParseException -- if bad argument passed in.
	 */
	public void setCGI3GPP(String cgi) throws ParseException {

		if (cgi == null)
			throw new NullPointerException(
					"JAIN-SIP Exception, "
							+ "P-Access-Network-Info, setCGI3GPP(), the cgi parameter is null.");

		setParameter(ParameterNamesIms.CGI_3GPP, cgi);

	}

	/**
	 * 
	 * @return String CGI value
	 */
	public String getCGI3GPP() {
		return getParameter(ParameterNamesIms.CGI_3GPP);
	}

	/**
	 * Set the UtranCellID field.
	 * 
	 * @param  utranCellID -- String
	 *            UTRAN Cell ID value
	 * @throws NullPointerException
	 * @throws ParseException
	 */
	public void setUtranCellID3GPP(String utranCellID) throws ParseException {

		if (utranCellID == null)
			throw new NullPointerException(
					"JAIN-SIP Exception, "
							+ "P-Access-Network-Info, setUtranCellID3GPP(), the utranCellID parameter is null.");

		setParameter(ParameterNamesIms.UTRAN_CELL_ID_3GPP, utranCellID);

	}

	/**
	 * 
	 * @return String UTRAN Cell ID value
	 */
	public String getUtranCellID3GPP() {
		return getParameter(ParameterNamesIms.UTRAN_CELL_ID_3GPP);
	}

	/**
	 * 
	 * @param dslLocation
	 *            DSL location value
	 * @throws NullPointerException
	 * @throws ParseException
	 */
	public void setDSLLocation(String dslLocation) throws ParseException {

		if (dslLocation == null)
			throw new NullPointerException(
					"JAIN-SIP Exception, "
							+ "P-Access-Network-Info, setDSLLocation(), the dslLocation parameter is null.");

		setParameter(ParameterNamesIms.DSL_LOCATION, dslLocation);

	}

	/**
	 * 
	 * @return String DSL location value
	 */
	public String getDSLLocation() {
		return getParameter(ParameterNamesIms.DSL_LOCATION);
	}

	/**
	 * 
	 * @param ci3Gpp2 -- String
	 *            CI 3GPP2 value
	 * @throws NullPointerException -- if arg is null
	 * @throws ParseException -- if arg is bad.
	 */
	public void setCI3GPP2(String ci3Gpp2) throws ParseException {
		if (ci3Gpp2 == null)
			throw new NullPointerException(
					"JAIN-SIP Exception, "
							+ "P-Access-Network-Info, setCI3GPP2(), the ci3Gpp2 parameter is null.");

		setParameter(ParameterNamesIms.CI_3GPP2, ci3Gpp2);
	}

	/**
	 * 
	 * @return String CI 3GPP2 value
	 */
	public String getCI3GPP2() {
		return getParameter(ParameterNamesIms.CI_3GPP2);
	}

	/**
	 * 
	 * @param name --
	 *            parameter name
	 * @param value --
	 *            value of parameter
	 */
	public void setParameter(String name, Object value) {
		/**
		 * todo ParametersHeader needs to be fix!? missing "throws
		 *       ParseException" in setParameter(String, Object)
		 */

		if (name.equalsIgnoreCase(ParameterNamesIms.CGI_3GPP)
				|| name.equalsIgnoreCase(ParameterNamesIms.UTRAN_CELL_ID_3GPP)
				|| name.equalsIgnoreCase(ParameterNamesIms.DSL_LOCATION)
				|| name.equalsIgnoreCase(ParameterNamesIms.CI_3GPP2)) {
			try {
				super.setQuotedParameter(name, value.toString());
			} catch (ParseException e) {

			}

		} else {
			// value can be token either than a quoted-string
			super.setParameter(name, value);

		}

	}

	/**
	 * extension-access-info = gen-value gen-value = token / host /
	 * quoted-string
	 * 
	 * @param extendAccessInfo -- 
	 *            extended Access Information
	 */
	public void setExtensionAccessInfo(Object extendAccessInfo)
			throws ParseException {

		if (extendAccessInfo == null)
			throw new NullPointerException(
					"JAIN-SIP Exception, "
							+ "P-Access-Network-Info, setExtendAccessInfo(), the extendAccessInfo parameter is null.");

		// or -> setParameter("", extendAccessInfo);

		this.extendAccessInfo = extendAccessInfo;

	}

	public Object getExtensionAccessInfo() {
		return this.extendAccessInfo;
	}

	protected String encodeBody() {

		StringBuffer encoding = new StringBuffer();

		if (getAccessType() != null)
			encoding.append(getAccessType());

		if (!parameters.isEmpty()) {
			encoding.append(SEMICOLON + this.parameters.encode());
		}
		// else if (getExtendAccessInfo() != null) // stack deve limitar, de
		// acordo com a especificação ?
		if (getExtensionAccessInfo() != null) {
			encoding.append(SEMICOLON
					+ getExtensionAccessInfo().toString());
		}

		return encoding.toString();

	}

	public void setValue(String value) throws ParseException {
		throw new ParseException(value, 0);

	}

}
