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

import javax.sip.header.*;

import java.util.Iterator;

/**
 * Keeps a list and a hashtable of via header functions.
 *
 * @version 1.2 $Revision: 1.5 $ $Date: 2006/07/13 09:01:24 $
 *
 * @author M. Ranganathan   <br/>
 *
 * 
 *
 */
public final class ViaList extends SIPHeaderList{
	
	public Object clone() {
		ViaList retval = new ViaList();		
		return retval.clonehlist(this.hlist);
	}
	/**
	 * Default Constructor.
	 */
	public ViaList() {
		super(Via.class, ViaHeader.NAME);
	}

	
	
}
