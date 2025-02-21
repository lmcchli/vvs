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
package gov.nist.javax.sip.stack;

import java.io.IOException;
import gov.nist.core.net.NetworkLayer;
import gov.nist.javax.sip.*;
import gov.nist.javax.sip.message.SIPResponse;
import gov.nist.javax.sip.message.SIPMessage;

import javax.sip.address.Hop;

/*
* Enhancements contributed by Jeff Keyser.
*/

/**
 * This is the Stack abstraction for the active object that waits for messages
 * to appear on the wire and processes these messages by calling the
 * MessageFactory interface to create a ServerRequest or ServerResponse object.
 * The main job of the message processor is to instantiate message channels for
 * the given transport.
 * 
 * @version 1.2 $Revision: 1.10 $ $Date: 2006/07/13 09:00:58 $
 * 
 * @author M. Ranganathan <br/>
 * 
 */
public abstract class MessageProcessor implements Runnable {

    /**
     * The Listening Point to which I am assigned.
     */
    private final ListeningPointImpl listeningPoint;

    /**
     * Constructor
     */
    public MessageProcessor(ListeningPointImpl lip) {
        if (lip == null)
            throw new NullPointerException(
                    "Listening point is null in message processor: " + this);

        this.listeningPoint = lip;
    }

    public ListeningPointImpl getListeningPoint() {
        return listeningPoint;
    }

    //////////////////////////////////////////////////////////////////////////
    // Abstract methods
    //////////////////////////////////////////////////////////////////////////

    /**
     * Start our thread.
     */
    public abstract void start() throws IOException;

    /**
     * Stop method.
     */
    public abstract void stop();

    /**
     * Return true if there are pending messages to be processed (which prevents
     * the message channel from being closed).
     */
    public abstract boolean inUse();

    /**
     * Run method.
     */
    public abstract void run();

    // Note! Should handle that messageChannel is dead/closed
    public abstract void sendResponse(MessageChannel messageChannel,
                                      Hop hop, SIPResponse response) throws IOException;

    // Note! Should handle that messageChannel is dead/closed
    public abstract void sendMessage(Hop hop,
                            Hop outboundProxy,
                            SIPMessage message) throws IOException;

    //////////////////////////////////////////////////////////////////////////
    // Helper methods
    //////////////////////////////////////////////////////////////////////////

    protected NetworkLayer getNetworkLayer() {
        return getListeningPoint().getSipStack().getNetworkLayer();
    }


}
