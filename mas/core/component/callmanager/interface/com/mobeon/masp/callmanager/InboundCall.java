/*
* Copyright (c) 2005 Mobeon AB. All Rights Reserved.
*/
package com.mobeon.masp.callmanager;

import com.mobeon.masp.callmanager.configuration.RemotePartyAddress;

/**
 * Provides call management related to an active inbound call.
 * <p>
 * If media types additional to those configured in the Call Manager must be
 * supported to play on the outbound media stream of the call these should be
 * given by the Call Manager client. The media types are given by the Call
 * Manager client by setting the "callmediatypesarray" variable in the session
 * object related to this call.
 * <ul>
 * <li>
 * Early media: <br>
 * For the early media scenario, the Call Manager client must call the
 * {@link #negotiateEarlyMediaTypes()} method before playing media on the call.
 * Tne Call Manager then negotiates with peer and stores the media type to use
 * in the session parameter "selectedcallmediatypes". When the call is available
 * for early media play, an
 * {@link com.mobeon.masp.callmanager.events.EarlyMediaAvailableEvent}
 * is sent to indicate that the media type can be retrieved from the session
 * object and that it now is ok to play media on the call.
 * </li>
 * <li>
 * NO early media:
 * If early media shall not be used, the media negotiation is done when the
 * client accepts the call. The selected media type is stored in the
 * "selectedcallmediatypes" variable in the session object. When the call has
 * been connected and is available for media play, a
 * {@link com.mobeon.masp.callmanager.events.ConnectedEvent} is sent. When this
 * event is received, the media type can be retrieved from the session object
 * and it is ok to play media on the call.
 * </li>
 * </ul>
 * When the media types are selected from the session object, the Call Manager
 * first checks to see if the session parameter "selectedcallmediatypes" is set.
 * If it is set, that call media types is used in negotiation. This is done
 * since all calls in one session must require the same media types for an
 * outbound stream.
 * <br>
 * If the "selectedcallmediatypes" are not set, Call Manager uses
 * "callmediatypesarray" as described above. If that is not set either, Call
 * Manager assumes that the client has no special requests on media types
 * that must be supported to play on the outbound stream.
 * <p>
 * This class is also a container of properties related to an inbound call.
 * As a container, it provides setters and getters.
 * All getters return null if values have not been previously set.
 *
 * @author Malin Flodin
 */
public interface InboundCall extends Call {

    /**
     * @return  Returns the redirecting party. Null if there is no redirecting 
     *          party for the call.
     */
    public RedirectingParty getRedirectingParty();

    /**
     * This method is used to accept a peer inbound call request.
     * When the call is connected, a
     * {@link com.mobeon.masp.callmanager.events.ConnectedEvent} is generated.
     * <p>
     * If the media negotiation fails, a
     * {@link com.mobeon.masp.callmanager.events.FailedEvent} is generated.
     * <p>
     * If an error occurs while accepting the inbound call, an
     * {@link com.mobeon.masp.callmanager.events.ErrorEvent} is generated.
     * <p>
     * A {@link com.mobeon.masp.callmanager.events.NotAllowedEvent} is
     * generated if used on an inbound call that already is accepted.
     */
    public void accept();

    /**
     * This method is used to proxy an inbound call request to a given UAS.
     * When the call is connected, a
     * {@link com.mobeon.masp.callmanager.events.ProxiedEvent} is generated.
     * <p>
     * If an error occurs while proxying the inbound call, a
     * {@link com.mobeon.masp.callmanager.events.ErrorEvent} is generated.
     * <p>
     * A {@link com.mobeon.masp.callmanager.events.NotAllowedEvent} is
     * generated if used on an inbound call that is already proxying.
     */
    public void proxy(RemotePartyAddress opcoId);

    /**
     * This method is used to reject a call request.
     * A call can only be rejected if it has not yet been accepted.
     * A {@link com.mobeon.masp.callmanager.events.NotAllowedEvent} is
     * generated if used when not allowed.
     */
    public void reject(String rejectEventTypeName, String reason);

    /**
     * This method is used to indicate that early media shall be played for the
     * call and that which media type to use must be negotiated before the call
     * is connected.
     * This method is used if early media shall be played before the call is
     * connected, and it MUST be called before accepting the call.
     * <p>
     * When one media type has been selected to be used, it is stored in the
     * session parameter "selectedcallmediatypes" and an
     * {@link com.mobeon.masp.callmanager.events.EarlyMediaAvailableEvent}
     * is generated indicating that it now is ok to either play media or accept
     * the call.
     */
    public void negotiateEarlyMediaTypes();

    /**
     * This method is used to disconnect the call.
     * Always generates a
     * {@link com.mobeon.masp.callmanager.events.DisconnectedEvent} when
     * completed.
     */
    public void disconnect();

    /**
     * This method is used to redirect a call request to the specified destination
     * A call can only be rejected if it has not yet been accepted.
     * A {@link com.mobeon.masp.callmanager.events.NotAllowedEvent} is
     * generated if used when not allowed.
     * @param destination SIP URI or TEL URI representing the redirect destination
     * @param redirectCode valid SIP 3XX redirect code
     */
    public void redirect(RedirectDestination destination, RedirectStatusCode redirectCode);
    
    
    public static enum RedirectStatusCode {
        
        _300_MULTIPLE_CHOICE    (300 ,"Multiple Choice"),
        _301_MOVED_PERMANENTLY  (301, "Moved Permanently"),
        _302_MOVED_TEMPORARILY  (302, "Moved Temporarily"),
        _305_USE_PROXY           (305 , "Use Proxy"),
        _380_ALTERNATIVE_SERVICE (380,"Alternative Service");

        private final int code;
        private final String reason;
        
        private RedirectStatusCode(int code, String reason){
            this.code = code ;
            this.reason = reason;
        }


        public int getCode() {
            return code;
        }


        public String getReason() {
            return reason;
        }

        
    };
}
