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
* Product of NIST/ITL Advanced Networking Technologies Division (ANTD).        *
*******************************************************************************/
package gov.nist.core;

import org.apache.log4j.Logger;

/**
*  Handle Internal error failures and print a stack trace (for debugging).
*
*@version 1.2
*
*@author M. Ranganathan   <br/>
*
*
*
*/
public class InternalErrorHandler {
	
    private static final Logger log = Logger.getLogger(InternalErrorHandler.class);

    public static void handleException(Exception ex) throws RuntimeException {
        log.error("Unexpected internal error " + ex.getMessage(), ex);
        throw new RuntimeException("Unexpected internal error "  +
                ex.getMessage(), ex);

    }
    /**
     * Handle an unexpected exception.
     */
    public static void handleException(Exception ex, LogWriter logWriter) {
        log.error("Unexpected internal error " + ex.getMessage(), ex);
//        logWriter.logError("UNEXPECTED INTERNAL ERROR FIXME " +  ex.getMessage());
//        logWriter.logException(ex);
        throw new RuntimeException("Unexpected internal error FIXME!! "  + ex.getMessage(), ex);

    }
    /**
     * Handle an unexpected condition (and print the error code).
     */

    public static void handleException(String emsg) {
        log.error(emsg,new Exception());
//        new Exception().printStackTrace();
//        System.err.println("Unexepcted INTERNAL ERROR FIXME!!");
//        System.err.println(emsg);
        throw new RuntimeException(emsg);

    }

    public static void handleException(String emsg, LogWriter logWriter) {
        log.error(emsg, new Exception());
//        logWriter.logStackTrace();
//        logWriter.logError("Unexepcted INTERNAL ERROR FIXME!!");
//        logWriter.logFatalError(emsg);
        throw new RuntimeException(emsg);

    }
}
