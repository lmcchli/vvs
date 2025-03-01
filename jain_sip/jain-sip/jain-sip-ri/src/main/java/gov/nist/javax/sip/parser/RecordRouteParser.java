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
package gov.nist.javax.sip.parser;

import java.text.ParseException;
import gov.nist.javax.sip.header.*;

/**
 * Parser for a list of route headers.
 *
 * @version 1.2 $Revision: 1.6 $ $Date: 2006/07/13 09:02:02 $
 *
 * @author Olivier Deruelle   <br/>
 * @author M. Ranganathan   <br/>
 * 
 */
public class RecordRouteParser extends AddressParametersParser {

	/**
	 * Constructor
	 * @param recordRoute message to parse to set
	 */
	public RecordRouteParser(String recordRoute) {
		super(recordRoute);
	}

	protected RecordRouteParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the String message and generate the RecordRoute List Object
	 * @return SIPHeader the RecordRoute List object
	 * @throws ParseException if errors occur during the parsing
	 */
	public SIPHeader parse() throws ParseException {
		RecordRouteList recordRouteList = new RecordRouteList();

		if (debug)
			dbg_enter("RecordRouteParser.parse");

		try {
			this.lexer.match(TokenTypes.RECORD_ROUTE);
			this.lexer.SPorHT();
			this.lexer.match(':');
			this.lexer.SPorHT();
			while (true) {
				RecordRoute recordRoute = new RecordRoute();
				super.parse(recordRoute);
				recordRouteList.add(recordRoute);
				this.lexer.SPorHT();
				if (lexer.lookAhead(0) == ',') {
					this.lexer.match(',');
					this.lexer.SPorHT();
				} else if (lexer.lookAhead(0) == '\n')
					break;
				else
					throw createParseException("unexpected char");
			}
			return recordRouteList;
		} finally {
			if (debug)
				dbg_leave("RecordRouteParser.parse");
		}

	}

	/**
	        public static void main(String args[]) throws ParseException {
			String rou[] = {
				"Record-Route: <sip:bob@biloxi.com;maddr=10.1.1.1>,"+
	                        "<sip:bob@biloxi.com;maddr=10.2.1.1>\n",
	                        
				"Record-Route: <sip:UserB@there.com;maddr=ss2.wcom.com>\n",
	                        
	                        "Record-Route: <sip:+1-650-555-2222@iftgw.there.com;"+
	                        "maddr=ss1.wcom.com>\n",
	                        
	                        "Record-Route: <sip:UserB@there.com;maddr=ss2.wcom.com>,"+
	                        "<sip:UserB@there.com;maddr=ss1.wcom.com>\n"  
	                };
				
			for (int i = 0; i < rou.length; i++ ) {
			    RecordRouteParser rp = 
				  new RecordRouteParser(rou[i]);
			    RecordRouteList recordRouteList = (RecordRouteList) rp.parse();
			    System.out.println("encoded = " +recordRouteList.encode());
			}
				
		}
	*/
}
/*
 * $Log: RecordRouteParser.java,v $
 * Revision 1.6  2006/07/13 09:02:02  mranga
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
 * Revision 1.3  2006/06/19 06:47:27  mranga
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
 * Revision 1.4  2004/01/22 13:26:31  sverker
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
