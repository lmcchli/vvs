/**
 * COPYRIGHT (C) ABCXYZ INTERNET APPLICATIONS INC.
 *
 * THIS SOFTWARE IS FURNISHED UNDER A LICENSE ONLY AND IS
 * PROPRIETARY TO ABCXYZ INTERNET APPLICATIONS INC. IT MAY NOT BE COPIED
 * EXCEPT WITH THE PRIOR WRITTEN PERMISSION OF ABCXYZ INTERNET APPLICATIONS
 * INC.  ANY COPY MUST INCLUDE THE ABOVE COPYRIGHT NOTICE AS
 * WELL AS THIS PARAGRAPH.  THIS SOFTWARE OR ANY OTHER COPIES
 * THEREOF, MAY NOT BE PROVIDED OR OTHERWISE MADE AVAILABLE
 * TO ANY OTHER PERSON OR ENTITY.
 * TITLE TO AND OWNERSHIP OF THIS SOFTWARE SHALL AT ALL
 * TIMES REMAIN WITH ABCXYZ INTERNET APPLICATIONS INC.
 *
 */
package com.mobeon.common.cmnaccess;

public class FormatUtil {

	/**
	 * Removing the first and last quote if exist.
	 * For example this will "U"T"F-8" be translate into U"T"F-8 
	 * 
	 * @param stringToParse
	 * @return
	 */
	public static String removeQuote(String stringToParse){
		int startAt = 0;
		int endAt = stringToParse.length();
		boolean toDoSubstring = false;
		
		if(stringToParse.charAt(startAt) == '\"'){
			startAt ++;
			toDoSubstring = true;
		}
		
		if(stringToParse.charAt(endAt - 1) == '\"'){
			endAt --;
			toDoSubstring = true;
		}
		
		if(toDoSubstring){
			return stringToParse.substring(startAt, endAt);
		}else{
			return stringToParse;
		}
	}
}
