/*
 * Copyright (c) 2010 Abcxyz. All Rights Reserved.
 */
package com.mobeon.masp.callmanager.callhandling.events;

import javax.sip.message.Response;

/**
 * A reject event contains all information regarding a call reject.
 * It is used internally in the Call Manager to carry information regarding
 * the event until the event is handled.
 * <p>
 * The RejectEventTypes enum maps the CallManager's client code with javax.sip.message.Response class.
 * <p>
 * This class is thread-safe and immutable.
 */
public class RejectEvent extends CallCommandEvent {

    /** RejectEventTypes */
    public enum RejectEventTypes {

        _302_FORBIDDEN           (Response.MOVED_TEMPORARILY,    "The call is redirected by service."),
        _400_BAD_REQUEST           (Response.BAD_REQUEST,           "The request could not be understood due to malformed syntax."),
        _403_FORBIDDEN           (Response.FORBIDDEN,           "The call is rejected by service."),
        _488_NOT_ACCEPTABLE_HERE (Response.NOT_ACCEPTABLE_HERE, "The call is not accepted by service."),
        _503_SERVICE_UNAVAILABLE (Response.SERVICE_UNAVAILABLE, "The server is temporarily unable to process the request due to a temporary overloading or maintenance of the server.");

        private int code;
        private String message;

        RejectEventTypes(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }

    /** Private members */
    private RejectEventTypes rejectEventType = RejectEventTypes._403_FORBIDDEN;
    private String message = null;

    public RejectEvent() {
    }

    public RejectEvent(RejectEventTypes rejectEventType) {
    	if(rejectEventType != null){
    		this.rejectEventType = rejectEventType;
    	}
    }

    public RejectEvent(RejectEventTypes rejectEventType, String message) {
    	if(rejectEventType != null){
    		this.rejectEventType = rejectEventType;
    	}
        if(message != null && !message.equals("")){
        	this.message = message;
        }
    }

    public RejectEvent(String reason) {
    	this(null,reason);
	}

	public RejectEventTypes getRejectEventType() {
        return this.rejectEventType;
    }

    public int getCode() {
        return this.rejectEventType.getCode();
    }

    public String getMessage() {
        return (this.message == null ? this.rejectEventType.getMessage() : this.message);
    }
    
    public String toString() {
        return "RejectEvent (" + this.rejectEventType.getCode() + " " + getMessage() + ")";
    }

    public static RejectEventTypes mapToRejectEventType(String rejectEventTypeName) {
    	RejectEventTypes type = RejectEventTypes._403_FORBIDDEN;
    	try{
    		type = RejectEventTypes.valueOf(rejectEventTypeName);
    	}
    	catch(Exception e){}
    	return type;
    }

    /**
     * Added for backward compatibility
     * @deprecated
     * @return
     */
	public String getReason() {
		return getMessage();
	}
    
}
