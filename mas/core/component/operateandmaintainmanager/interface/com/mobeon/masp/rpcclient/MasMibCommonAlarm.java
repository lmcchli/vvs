/*
* COPYRIGHT Abcxyz Communication Inc. Montreal 2009
* The copyright to the computer program(s) herein is the property
* of ABCXYZ Communication Inc. Canada. The program(s) may be used
* and/or copied only with the written permission from ABCXYZ
* Communication Inc. or in accordance with the terms and conditions
* stipulated in the agreement/contact under which the program(s)
* have been supplied.
*---------------------------------------------------------------------
* Created on 21-Apr-2009
*/
package com.mobeon.masp.rpcclient;

import java.io.Serializable;

/**
 * MasMibCommonAlarm holds information for MAS common alarms.
 * @see {@link com.mobeon.common.cmnaccess.oam.CommonAlarmStatus}
 * @author egeobli
 */
public class MasMibCommonAlarm implements Serializable {
	
	public String name = "";
	public String id = "";
	public int status = 0;

	private static final long serialVersionUID = 1L;
	
	public String toString() {
		String result = "MasMibCommonAlarm has: name = " + name + ", id = " + id + " status = " + status;
		return result;
	}
}
