/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.out.wap.papapi;


/**
 * The interface class intercepts the pushNotificationRequest from the
 * NotificationManager and forwords the request to WapPushControl.
 * @author Ahmad Mahmoudi
 * @version 1.0
 */
public interface IWapPush
{

  /**
   * Push Access Protocol status Codes as described in
   * Wireless Application Protocol, Push Access Protocol Specification
   * Version 08-Nov-1999
   */
  public static final int PAP_OK = 1000;
  public static final int PAP_ACCEPTED_FOR_PROCESSING = 1001;
  public static final int PAP_BAD_REQUEST = 2000;
  public static final int PAP_FORBIDDEN = 2001;
  public static final int PAP_ADDRESS_ERROR = 2002;
  public static final int PAP_ADDRESS_NOT_FOUND = 2003;
  public static final int PAP_PUSHID_NOT_FOUND = 2004;
  public static final int PAP_CAPABILITIES_MISMATCH = 2005;
  public static final int PAP_REQUIRED_CAPABILITY_NOT_SUPPORTED = 2006;
  public static final int PAP_DUPLICATE_PUSHID = 2007;
  public static final int PAP_INTERNAL_SERVER_ERROR = 3000;
  public static final int PAP_NOT_IMPLEMENTED = 3001;
  public static final int PAP_VERSION_NOT_SUPPORTED = 3002;
  public static final int PAP_NOT_POSSIBLE = 3003;
  public static final int PAP_CAPABILITY_MATCHING_NOT_SUPPORTED = 3004;
  public static final int PAP_MULTIPLE_ADDRESS_NOT_SUPPORTED = 3005;
  public static final int PAP_TRANSFORMATION_FAILURE = 3006;
  public static final int PAP_SPECIFIED_DELIVERY_METHOD_NOT_POSSIBLE = 3007;
  public static final int PAP_CAPABILITIES_NOT_AVAILABLE = 3008;
  public static final int PAP_REQUIRED_NETWORK_NOT_AVAILABLE = 3009;
  public static final int PAP_REQUIRED_BEARER_NOT_AVAILABLE = 3010;
  public static final int PAP_SERVICE_FAILURE = 4000;
  public static final int PAP_SERVICE_UNAVAILABLE = 4001;
  public static final int PAP_MOBILE_CLIENT_ABORTED = 5000;

  /**
   * Notification Filter Manager or any entity to send a wap push message to a WAP
   * PPG server calls this method. The person to be receive the push message and the
   * PPG server to send to are passed as method parameters.
   * The return value is a 4 digit number between 1xxx and 5xxx.
   * 1xxx means Success
   * 2xxx means Client Error
   * 3xxx means Server Error
   * 4xxx means Server Failure
   * 5xxx means Mobile Device Abort
   * For more info on result code please refer to WAP Push Access Protocol
   * Specification at http://www.wapforum.org/what/technical.htm
   * @roseuid 3A846CAB00B0
   */
    public boolean pushNotifyRequest();//-ermahen
}
