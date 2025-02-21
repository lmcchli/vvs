/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling;

import com.mobeon.masp.stream.IInboundMediaStream;
import com.mobeon.masp.stream.IOutboundMediaStream;
import com.mobeon.masp.callmanager.sip.events.SipRequestEvent;
import com.mobeon.masp.callmanager.sip.events.SipResponseEvent;

/**
 * This is an interface that are used for method usage between two calls.
 * This interface is implemented by a call and used when accessing another call.
 * Currently, the only situation when a call use methods in another call is when
 * the calls are joined or unjoined.
 *
 * <em>
 * IMPORTANT TO NOTE:
 *   Stream related events such as creating streams, deleting streams, join
 *   and unjoin operations in a call implementation (see {@link CallImpl}) are
 *   synchronized around {@link CallImpl#streamLock}.
 *
 *   Therefore, a call must not use any of the below methods of another call
 *   within a block synchronized on {@link CallImpl#streamLock} if that method
 *   itself indicates synchronization on {@link CallImpl#streamLock}.
 *   The reason for this is to avoid dead-lock.
 *
 *   Currently, {@link #unjoin} is the only method below that are synchronized
 *   on the {@link CallImpl#streamLock} and must therefore not be used by
 *   another call is a synchronized block.
 * </em>
 *
 * @author Malin Nyfeldt
 */
public interface CallToCall {

    /**
     * <em>
     * NOTE: This method is NOT synchronized and may therefore be used
     * within a synchronized block.
     * </em>
     * @return Returns the inbound stream of the call.
     * Null is returned if there is no inbound stream.
     */
    public IInboundMediaStream getInboundStream();

    /**
     * <em>
     * NOTE: This method is NOT synchronized and may therefore be used
     * within a synchronized block.
     * </em>
     * @return Returns the outbound stream of the call.
     * Null is returned if there is no outbound stream.
     */
    public IOutboundMediaStream getOutboundStream();

    /**
     * This method unjoins the initiating call from the called call half duplex.
     * The called call's inbound stream is unjoined from the other call's
     * outbound stream.
     * <p>
     * <em>
     * NOTE: This method IS IMPLEMENTED SYNCHRONIZED and shall therefore not
     * be called from a synchronized block in the call using this interface.
     * </em>
     *
     * @param anotherCall
     * @throws IllegalStateException if it was not possible to unjoin the calls.
     */
    public void unjoin(CallToCall anotherCall) throws IllegalStateException;

    /**
     * This method takes a SipRequestEvent containing a VFU request and
     * forwards it out on the call implementing this interface.
     * An actual forward is only done if the SipRequestEvent is a VFU request,
     * i.e. if the method is INFO which contains a media control message body.
     * <p>
     * If the VFU request cannot be created or sent, it is handled as an error
     * situation and the call is considered completed.
     * <p>
     * <em>
     * NOTE: This method is NOT synchronized and may therefore be used
     * within a synchronized block.
     * </em>
     *
     * @param sipRequestEvent MUST NOT be null
     * @return an identifier for the sent request
     */
    public String forwardVFURequest(SipRequestEvent sipRequestEvent);

    /**
     * This method checks if there is a pending VFU request that matches the VFU
     * response. If there is, the VFU response is sent on within the call.
     * <p>
     * If the VFU response cannot be created or sent, it is handled as an error
     * situation and the call is considered completed.
     * <p>
     * <em>
     * NOTE: This method is NOT synchronized and may therefore be used
     * within a synchronized block.
     * </em>
     *
     * @param sipResponseEvent MUST NOT be null.
     */
    public void forwardVFUResponse(SipResponseEvent sipResponseEvent);
}
