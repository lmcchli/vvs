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
package gov.nist.javax.sip;

import java.security.MessageDigest;
import java.util.HashSet;

/**
 * A few utilities that are used in various places by the stack. This is used to
 * convert byte arrays to hex strings etc. Generate tags and branch identifiers
 * and odds and ends.
 * 
 * @author mranga
 * @version 1.2 $Revision: 1.15 $ $Date: 2006/07/25 09:28:40 $
 */
public class Utils {

	private static MessageDigest digester;
	static {
		try {
			digester = MessageDigest.getInstance("MD5");
		} catch (Exception ex) {
			throw new RuntimeException("Could not intialize Digester ", ex);
		}
	}

	private static java.util.Random rand = new java.util.Random();

	private static long counter = 0;

	private static int callIDCounter;

	/**
	 * to hex converter
	 */
	private static final char[] toHex = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	/**
	 * convert an array of bytes to an hexadecimal string
	 * 
	 * @return a string
	 * @param b
	 *            bytes array to convert to a hexadecimal string
	 */

	public static String toHexString(byte b[]) {
		int pos = 0;
		char[] c = new char[b.length * 2];
		for (int i = 0; i < b.length; i++) {
			c[pos++] = toHex[(b[i] >> 4) & 0x0F];
			c[pos++] = toHex[b[i] & 0x0f];
		}
		return new String(c);
	}

	/**
	 * Put quotes around a string and return it.
	 * 
	 * @return a quoted string
	 * @param str
	 *            string to be quoted
	 */
	public static String getQuotedString(String str) {
		return '"' + str + '"';
	}

	/**
	 * Squeeze out all white space from a string and return the reduced string.
	 * 
	 * @param input
	 *            input string to sqeeze.
	 * @return String a reduced string.
	 */
	protected static String reduceString(String input) {
		String newString = input.toLowerCase();
		int len = newString.length();
		String retval = "";
		for (int i = 0; i < len; i++) {
			if (newString.charAt(i) == ' ' || newString.charAt(i) == '\t')
				continue;
			else
				retval += newString.charAt(i);
		}
		return retval;
	}

	/**
	 * Generate a call identifier. This is useful when we want to generate a
	 * call identifier in advance of generating a message.
	 */
	public static synchronized String generateCallIdentifier(String address) {
		
			String date = new Long(System.currentTimeMillis() + callIDCounter++
					+ rand.nextLong()).toString();
			byte cid[] = digester.digest(date.getBytes());

			String cidString = Utils.toHexString(cid);
			return cidString + "@" + address;
	
	}

	/**
	 * Generate a tag for a FROM header or TO header. Just return a random 4
	 * digit integer (should be enough to avoid any clashes!) Tags only need to
	 * be unique within a call.
	 * 
	 * @return a string that can be used as a tag parameter.
	 * 
	 * synchronized: needed for access to 'rand', else risk to generate same tag
	 * twice
	 */
	public static synchronized String generateTag() {
		
			return Integer.toHexString(rand.nextInt());
	
	}

	/**
	 * Generate a cryptographically random identifier that can be used to
	 * generate a branch identifier.
	 * 
	 * @return a cryptographically random gloablly unique string that can be
	 *         used as a branch identifier.
	 */
	public static synchronized String generateBranchId() {
		// 
	
		
			long num = rand.nextLong() + Utils.counter++  + System.currentTimeMillis();

			byte bid[] = digester.digest(Long.toString(num).getBytes());
			// prepend with a magic cookie to indicate we are bis09 compatible.
			return SIPConstants.BRANCH_MAGIC_COOKIE + Utils.toHexString(bid);
		

	}

	public static void main(String[] args) {

		HashSet branchIds = new HashSet();
		for (int b = 0; b < 100000; b++) {
			String bid = generateBranchId();
			if (branchIds.contains(bid)) {
				throw new RuntimeException("Duplicate Branch ID");
			} else {
				branchIds.add(bid);
			}
		}
		System.out.println("Done!!");

	}

}
