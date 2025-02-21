/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */
package com.mobeon.ntf.out.wireline;

/****************************************************************
 * The SS7GatewayResultcodes is a simple interface that just carries result information from
 */


public interface SS7GatewayResultcodes {
    
    static public final int completed           = 200;
    static public final int serviceInitiated    = 202;        
    static public final int numberBlocked       = 401;
    static public final int busy                = 402;
    static public final int allLinesBusy        = 403;        
    static public final int noAnswer            = 404;
    static public final int requestTimedOut     = 408;
    static public final int serviceNotAvailable = 421;    
    static public final int syntaxError         = 500;
    static public final int requestUnrecogniced = 501;
    static public final int limitExceeded       = 502;
    static public final int numberDoesNotExist  = 511;
    static public final int nonValidNumber      = 512;
    static public final int unknownError        = 503;
}
