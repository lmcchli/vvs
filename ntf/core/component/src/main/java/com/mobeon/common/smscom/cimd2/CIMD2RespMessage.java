/**
 * Copyright (c) 2003 2004 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.smscom.cimd2;

import com.mobeon.common.smscom.Logger;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;


/****************************************************************
 * CIMD2RespMessage knows the format of a CIMD2 response message with only error
 * parameters. It extracts the error parameters from the response
 * buffer. Response messages with more parameters must override the parseBody
 * method, including the handling of error parameters.
 ****************************************************************/
public class CIMD2RespMessage extends CIMD2Message {

    protected int _errorCode = CIMD2_NO_ERROR;
    protected String _errorText = null;

    /**
     * Constructor.
     * @param conn The CIMD2Connection where this message shall be sent.
     */
    CIMD2RespMessage(CIMD2Com conn) {
        super(conn);
    }


    /****************************************************************
     * parseBody parses the error parameters of a response message from the
     * buffer read from the SMS-C.
     */
    public void parseBody() {
        int par;

        pos = HEADER_SIZE;
        _errorCode = CIMD2_NO_ERROR;
        _errorText = null;

        while (buffer[pos] != CIMD2_ETX) {
            par = getInt(CIMD2_COLON);
            switch (par) {
            case CIMD2_ERROR_CODE:
                _errorCode = getInt(CIMD2_TAB);
                break;
            case CIMD2_ERROR_TEXT:
                _errorText = getTTS();
                break;
            default:
                if (conn.willLog(Logger.LOG_DEBUG)) {
                    conn.getLogger().logString("CIMD2: unexpected parameter in response: " + par,
                                               Logger.LOG_DEBUG);
                }
                skipPast(CIMD2_TAB);
            }
        }
    }

    /**
     * getBuffer creates a buffer with a general response message without any
     * parameters. Responses that need parameters must override this method.
     * @return the created buffer
     */
    public byte[] getBuffer() {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(10);
        DataOutputStream dos = new DataOutputStream(bos);

        try {
            writeHeader(dos);
            writeTrailer(dos);
        } catch (IOException e) {
            //Since we write to a buffer with known size, this should never happen
            return null;
        }

        return bos.toByteArray();
    }

    /**
     * Get the errorCode .
     *@return the errorCode
     */
    public int getErrorCode() { return _errorCode; }
    /**
     * Set the errorCode.
     *@param errorCode - the new errorCode
     */
    public void setErrorCode(int errorCode) { _errorCode = errorCode; }

    /**
     * Get the errorText .
     *@return the errorText
     */
    public String getErrorText() { return _errorText; }
    /**
     * Set the errorText.
     *@param errorText - the new errorText
     */
    public void setErrorText(String errorText) { _errorText = errorText; }
}
