package com.mobeon.common.cmnaccess;

/**
 * class holds route result data
 */
public class RouteResult {
	/**
	 * @see RouteLookupResult for the possible route results
	 */
	public String result;
	
	/**
	 * reason is available only when result is "failed"
	 * @see Reason for list of failure reasons
	 */
	public String reason;
	
	/**
	 * @see MessageClass for known message classes, may have more that are not listed
	 */
	public String destMsgClass;
	
	/**
	 * destination recipient id
	 */
    public String destRcptId;

}
