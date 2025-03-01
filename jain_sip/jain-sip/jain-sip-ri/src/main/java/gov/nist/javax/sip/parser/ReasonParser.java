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

import gov.nist.javax.sip.header.*;
import gov.nist.core.*;
import java.text.ParseException;

/**
 * Parser for Reason header.
 *
 * @version 1.2
 *
 * @author Olivier Deruelle   <br/>
 * @author M. Ranganathan   <br/>
 *
 * 
 */
public class ReasonParser extends ParametersParser {

	/**
	 * Creates a new instance of ReasonParser 
	 * @param reason the header to parse
	 */
	public ReasonParser(String reason) {
		super(reason);
	}

	/**
	 * Constructor
	 * @param lexer the lexer to use to parse the header
	 */
	protected ReasonParser(Lexer lexer) {
		super(lexer);
	}

	/**
	 * parse the String message
	 * @return SIPHeader (ReasonParserList object)
	 */
	public SIPHeader parse() {
		ReasonList reasonList = new ReasonList();
		if (debug)
			dbg_enter("ReasonParser.parse");

		try {
			headerName(TokenTypes.REASON);
			this.lexer.SPorHT();
			while (lexer.lookAhead(0) != '\n') {
				Reason reason = new Reason();
				this.lexer.match(TokenTypes.ID);
				Token token = lexer.getNextToken();
				String value = token.getTokenValue();

				reason.setProtocol(value);
				super.parse(reason);
				reasonList.add(reason);
				if (lexer.lookAhead(0) == ',') {
					this.lexer.match(',');
					this.lexer.SPorHT();
				} else
					this.lexer.SPorHT();

			}
		} catch (ParseException ex) {
                    InternalErrorHandler.handleException(ex);
		} finally {
			if (debug)
				dbg_leave("ReasonParser.parse");
		}

		return reasonList;
	}

	/** Test program
	public static void main(String args[]) throws ParseException {
	    String r[] = {
	        "Reason: SIP ;cause=200 ;text=\"Call completed elsewhere\"\n",
	        "Reason: Q.850 ;cause=16 ;text=\"Terminated\"\n",
	        "Reason: SIP ;cause=600 ;text=\"Busy Everywhere\"\n",
	        "Reason: SIP ;cause=580 ;text=\"Precondition Failure\","+
	        "SIP ;cause=530 ;text=\"Pre Failure\"\n",
	        "Reason: SIP \n"
	    };
	    
	    for (int i = 0; i < r.length; i++ ) {
	        ReasonParser parser =
	        new ReasonParser(r[i]);
	        ReasonList rl= (ReasonList) parser.parse();
	        System.out.println("encoded = " + rl.encode());
	    }    
	}
	 */
}
/*
 * $Log: ReasonParser.java,v $
 * Revision 1.6  2006/07/13 09:02:12  mranga
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
