/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.xmp;

public interface XmpConstants {

  //Result codes
  
  /** Everything was OK. */
  public static final int OK = 200;

  /** Not OK, timed out. */
  public static final int TIMEOUT = 408;

  /** Not OK, Service not available. */
  public static final int NO_SERVICE = 421;

  /** Not OK, some partial failure, but you can retry at a later time. */
  public static final int PARTIAL_LATER = 440;
  
  /** Not OK, but you can retry at a later time. */
  public static final int LATER = 450;

  /** Not OK, and retries will also fail. */
  public static final int NEVER = 551;

  /** Not OK, Parameter error, client error */
  public static final int CLIENT_ERROR = 501;

  /** Not OK, Resource exceeded. */
  public static final int NO_RESOURCE = 502; // 402

  /** Not OK, partial failure. */
  public static final int PARTIAL_FAILURE = 540;
  
  /** Not OK, subscriber not found. */
  public static final int SUBSCRIBER_NOT_FOUND = 550;

  /** Not OK, object not found. */
  public static final int OBJECT_NOT_FOUND = 552;
}
