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
package gov.nist.javax.sip.stack;

import java.io.*;
import java.net.*;
import org.apache.log4j.Logger;

class IOHandler {

    private static final Logger log = Logger.getLogger(IOHandler.class);

    /**
     * A private function to write things out. This needs to be syncrhonized as
     * writes can occur from multiple threads. We write in chunks to allow the
     * other side to synchronize for large sized writes.
     */
    private static void writeChunks(OutputStream outputStream, byte[] bytes, int length)
            throws IOException {
        // Chunk size is 16K - this hack is for large
        // writes over slow connections.
        synchronized (outputStream) {
            // outputStream.write(bytes,0,length);
            int chunksize = 8 * 1024;
            for (int p = 0; p < length; p += chunksize) {
                int chunk = p + chunksize < length ? chunksize : length - p;
                outputStream.write(bytes, p, chunk);
            }
        }
        outputStream.flush();
    }


    /**
     * Send an array of bytes.
     *
     * @throws IOException --
     *             if there is an IO exception sending message.
     */
    public static void sendBytes(Socket socket, byte[] bytes)
            throws IOException {

        int length = bytes.length;
        if (socket != null) {
            if (log.isDebugEnabled()) {
                log.debug("sendBytes: Writing " + length +
                        " bytes to socket, remote=" +
                        socket.getInetAddress().getHostAddress() + ":" +
                        socket.getPort() + " local=" +
                        socket.getLocalAddress().getHostAddress() + ":" +
                        socket.getLocalPort());
            }

            OutputStream outputStream = socket.getOutputStream();
            writeChunks(outputStream, bytes, length);
        } else {
            String errMsg = "sendBytes: Cannot write, socket=null";
            log.warn(errMsg);
            throw new IOException(errMsg);
        }
    }


}
