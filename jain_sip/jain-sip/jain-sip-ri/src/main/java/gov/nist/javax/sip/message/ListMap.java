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
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD)         *
 *******************************************************************************/
package gov.nist.javax.sip.message;

import gov.nist.javax.sip.header.*;
import gov.nist.javax.sip.header.ims.*;
import gov.nist.core.InternalErrorHandler;

import java.util.Hashtable;

/**
 * A map of which of the standard headers may appear as a list
 * 
 * @version 1.2 $Revision: 1.9 $ $Date: 2006/10/12 11:57:55 $
 * @since 1.1
 */
class ListMap {
	// A table that indicates whether a header has a list representation or
	// not (to catch adding of the non-list form when a list exists.)
	// Entries in this table allow you to look up the list form of a header
	// (provided it has a list form). Note that under JAVA-5 we have
	// typed collections which would render such a list obsolete. However,
	// we are not using java 5.
	private static Hashtable<Class, Class> headerListTable;

	private static boolean initialized;
	static {
		initializeListMap();
	}

	static private void initializeListMap() {
		/*
		 * Build a table mapping between objects that have a list form and the
		 * class of such objects.
		 */
		headerListTable = new Hashtable<Class, Class>();
		headerListTable.put(ExtensionHeaderImpl.class, ExtensionHeaderList.class);

		headerListTable.put(Contact.class, ContactList.class);

		headerListTable.put(ContentEncoding.class, ContentEncodingList.class);

		headerListTable.put(Via.class, ViaList.class);

		headerListTable.put(WWWAuthenticate.class, WWWAuthenticateList.class);

		headerListTable.put(Accept.class, AcceptList.class);

		headerListTable.put(AcceptEncoding.class, AcceptEncodingList.class);

		headerListTable.put(AcceptLanguage.class, AcceptLanguageList.class);

		headerListTable.put(ProxyRequire.class, ProxyRequireList.class);

		headerListTable.put(Route.class, RouteList.class);

		headerListTable.put(Require.class, RequireList.class);

		headerListTable.put(Warning.class, WarningList.class);

		headerListTable.put(Unsupported.class, UnsupportedList.class);

		headerListTable.put(AlertInfo.class, AlertInfoList.class);

		headerListTable.put(CallInfo.class, CallInfoList.class);

		headerListTable.put(ProxyAuthenticate.class,ProxyAuthenticateList.class);

		headerListTable.put(ProxyAuthorization.class, ProxyAuthorizationList.class);

		headerListTable.put(Authorization.class, AuthorizationList.class);

		headerListTable.put(Allow.class, AllowList.class);

		headerListTable.put(RecordRoute.class, RecordRouteList.class);

		headerListTable.put(ContentLanguage.class, ContentLanguageList.class);

		headerListTable.put(ErrorInfo.class, ErrorInfoList.class);

		headerListTable.put(Supported.class, SupportedList.class);

		headerListTable.put(InReplyTo.class,InReplyToList.class);
		
		// IMS headers.
		
                headerListTable.put(PAssertedIdentity.class, PAssertedIdentityList.class);

		headerListTable.put(AssociatedURI.class, AssociatedURIList.class);
		
		headerListTable.put(MediaAuthorization.class, MediaAuthorizationList.class);
		
		headerListTable.put(Path.class, PathList.class);
		
		headerListTable.put(Privacy.class,PrivacyList.class);
		
		headerListTable.put(ServiceRoute.class, ServiceRouteList.class);
		
		headerListTable.put(VisitedNetworkID.class, VisitedNetworkIDList.class);

                // History-Info header (RFC4244)
                headerListTable.put(HistoryInfo.class, HistoryInfoList.class);

                initialized = true;

	}

	/**
	 * return true if this has an associated list object.
	 */
	static protected boolean hasList(SIPHeader sipHeader) {
		if (sipHeader instanceof SIPHeaderList)
			return false;
		else {
			Class headerClass = sipHeader.getClass();
			return headerListTable.get(headerClass) != null;
		}
	}

	/**
	 * Return true if this has an associated list object.
	 */
	static protected boolean hasList(Class sipHdrClass) {
		if (!initialized)
			initializeListMap();
		return headerListTable.get(sipHdrClass) != null;
	}

	/**
	 * Get the associated list class.
	 */
	static protected Class getListClass(Class sipHdrClass) {
		if (!initialized)
			initializeListMap();
		return headerListTable.get(sipHdrClass);
	}

	/**
	 * Return a list object for this header if it has an associated list object.
	 */
	static protected SIPHeaderList getList(SIPHeader sipHeader) {
		if (!initialized)
			initializeListMap();
		try {
			Class headerClass = sipHeader.getClass();
			Class listClass = headerListTable.get(headerClass);
			SIPHeaderList shl = (SIPHeaderList) listClass.newInstance();
			shl.setHeaderName(sipHeader.getName());
			return shl;
		} catch (InstantiationException ex) {
                    InternalErrorHandler.handleException(ex);
		} catch (IllegalAccessException ex) {
                    InternalErrorHandler.handleException(ex);
		}
		return null;
	}

}

