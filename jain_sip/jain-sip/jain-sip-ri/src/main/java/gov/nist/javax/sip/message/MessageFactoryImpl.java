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
package gov.nist.javax.sip.message;

import java.text.ParseException;
import javax.sip.header.*;
import java.util.List;
import gov.nist.javax.sip.header.*;
import javax.sip.message.*;
import javax.sip.address.*;
import gov.nist.javax.sip.parser.*;

/**
 *  Message Factory implementation 
 *
 * @version 1.2 $Revision: 1.7 $ $Date: 2006/07/18 10:45:00 $
 * @since 1.1
 *
 * @author M. Ranganathan   <br/>
 * @author Olivier Deruelle <br/>
 * 
 */
public class MessageFactoryImpl implements MessageFactory {

	/**
	 * Creates a new instance of MessageFactoryImpl
	 */
	public MessageFactoryImpl() {
	}

	/**
	 * Creates a new Request message of type specified by the method paramater,
	 * containing the URI of the Request, the mandatory headers of the message
	 * with a body in the form of a Java object and the body content type.
	 *
	 * @param requestURI - the new URI object of the requestURI value of this Message.
	 * @param method - the new string of the method value of this Message.
	 * @param callId - the new CallIdHeader object of the callId value of this Message.
	 * @param cSeq - the new CSeqHeader object of the cSeq value of this Message.
	 * @param from - the new FromHeader object of the from value of this Message.
	 * @param to - the new ToHeader object of the to value of this Message.
	 * @param via - the new List object of the ViaHeaders of this Message.
	 * @param content - the new Object of the body content value of this Message.
	 * @param contentType - the new ContentTypeHeader object of the content type 
	 * value of this Message.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the method or the body.
	 */
	public Request createRequest(
		javax.sip.address.URI requestURI,
		String method,
		CallIdHeader callId,
		CSeqHeader cSeq,
		FromHeader from,
		ToHeader to,
		List via,
		MaxForwardsHeader maxForwards,
		ContentTypeHeader contentType,
		Object content)
		throws ParseException {
		if (requestURI == null
			|| method == null
			|| callId == null
			|| cSeq == null
			|| from == null
			|| to == null
			|| via == null
			|| maxForwards == null
			|| content == null
			|| contentType == null)
			throw new NullPointerException("Null parameters");

		SIPRequest sipRequest = new SIPRequest();
		sipRequest.setRequestURI(requestURI);
		sipRequest.setMethod(method);
		sipRequest.setCallId(callId);
		sipRequest.setCSeq(cSeq);
		sipRequest.setFrom(from);
		sipRequest.setTo(to);
		sipRequest.setVia(via);
		sipRequest.setMaxForwards(maxForwards);
		sipRequest.setContent(content, contentType);

		return sipRequest;
	}

	/**
	 * Creates a new Request message of type specified by the method paramater,
	 * containing the URI of the Request, the mandatory headers of the message
	 * with a body in the form of a byte array and body content type.
	 *
	 * @param requestURI - the new URI object of the 
	 * requestURI value of this Message.
	 * @param method - the new string of the method value of this Message.
	 * @param callId - the new CallIdHeader object of the callId value of this Message.
	 * @param cSeq - the new CSeqHeader object of the cSeq value of this Message.
	 * @param from - the new FromHeader object of the from value of this Message.
	 * @param to - the new ToHeader object of the to value of this Message.
	 * @param via - the new List object of the ViaHeaders of this Message.
	 * @param content - the new byte array of the body content value of this Message.
	 * @param contentType - the new ContentTypeHeader object of the content type 
	 * value of this Message.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the method or the body.
	 */
	public Request createRequest(
		URI requestURI,
		String method,
		CallIdHeader callId,
		CSeqHeader cSeq,
		FromHeader from,
		ToHeader to,
		List via,
		MaxForwardsHeader maxForwards,
		byte[] content,
		ContentTypeHeader contentType)
		throws ParseException {
		if (requestURI == null
			|| method == null
			|| callId == null
			|| cSeq == null
			|| from == null
			|| to == null
			|| via == null
			|| maxForwards == null
			|| content == null
			|| contentType == null)
			throw new ParseException(
				"JAIN-SIP Exception, some parameters are missing"
					+ ", unable to create the request",
				0);

		SIPRequest sipRequest = new SIPRequest();
		sipRequest.setRequestURI(requestURI);
		sipRequest.setMethod(method);
		sipRequest.setCallId(callId);
		sipRequest.setCSeq(cSeq);
		sipRequest.setFrom(from);
		sipRequest.setTo(to);
		sipRequest.setVia(via);
		sipRequest.setMaxForwards(maxForwards);
		sipRequest.setHeader((ContentType) contentType);
		sipRequest.setMessageContent(content);
		return sipRequest;
	}

	/**
	 * Creates a new Request message of type specified by the method paramater,
	 * containing the URI of the Request, the mandatory headers of the message.
	 * This new Request does not contain a body.
	 *
	 * @param requestURI - the new URI object of the requestURI value of this Message.
	 * @param method - the new string of the method value of this Message.
	 * @param callId - the new CallIdHeader object of the callId value of this Message.
	 * @param cSeq - the new CSeqHeader object of the cSeq value of this Message.
	 * @param from - the new FromHeader object of the from value of this Message.
	 * @param to - the new ToHeader object of the to value of this Message.
	 * @param via - the new List object of the ViaHeaders of this Message.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the method.
	 */
	public Request createRequest(
		URI requestURI,
		String method,
		CallIdHeader callId,
		CSeqHeader cSeq,
		FromHeader from,
		ToHeader to,
		List via,
		MaxForwardsHeader maxForwards)
		throws ParseException {
		if (requestURI == null
			|| method == null
			|| callId == null
			|| cSeq == null
			|| from == null
			|| to == null
			|| via == null
			|| maxForwards == null)
			throw new ParseException(
				"JAIN-SIP Exception, some parameters are missing"
					+ ", unable to create the request",
				0);

		SIPRequest sipRequest = new SIPRequest();
		sipRequest.setRequestURI(requestURI);
		sipRequest.setMethod(method);
		sipRequest.setCallId(callId);
		sipRequest.setCSeq(cSeq);
		sipRequest.setFrom(from);
		sipRequest.setTo(to);
		sipRequest.setVia(via);
		sipRequest.setMaxForwards(maxForwards);

		return sipRequest;
	}

	// Standard Response Creation methods

	/**
	 * Creates a new Response message of type specified by the statusCode 
	 * paramater, containing the mandatory headers of the message with a body 
	 * in the form of a Java object and the body content type.
	 *
	 * @param statusCode - the new integer of the statusCode value of this Message.
	 * @param callId - the new CallIdHeader object of the callId value of this Message.
	 * @param cSeq - the new CSeqHeader object of the cSeq value of this Message.
	 * @param from - the new FromHeader object of the from value of this Message.
	 * @param to - the new ToHeader object of the to value of this Message.
	 * @param via - the new List object of the ViaHeaders of this Message.
	 * @param content - the new Object of the body content value of this Message.
	 * @param contentType - the new ContentTypeHeader object of the content type 
	 * value of this Message.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the statusCode or the body.
	 */
	public Response createResponse(
		int statusCode,
		CallIdHeader callId,
		CSeqHeader cSeq,
		FromHeader from,
		ToHeader to,
		List via,
		MaxForwardsHeader maxForwards,
		Object content,
		ContentTypeHeader contentType)
		throws ParseException {
		if (callId == null
			|| cSeq == null
			|| from == null
			|| to == null
			|| via == null
			|| maxForwards == null
			|| content == null
			|| contentType == null)
			throw new NullPointerException(" unable to create the response");

		SIPResponse sipResponse = new SIPResponse();
		StatusLine statusLine = new StatusLine();
		statusLine.setStatusCode(statusCode);
		String reasonPhrase = SIPResponse.getReasonPhrase(statusCode);
		if (reasonPhrase == null)
			throw new ParseException(statusCode + " Unkown  ", 0);
		statusLine.setReasonPhrase(reasonPhrase);
		sipResponse.setStatusLine(statusLine);
		sipResponse.setCallId(callId);
		sipResponse.setCSeq(cSeq);
		sipResponse.setFrom(from);
		sipResponse.setTo(to);
		sipResponse.setVia(via);
		sipResponse.setMaxForwards(maxForwards);
		sipResponse.setContent(content, contentType);

		return sipResponse;
	}

	/**
	 * Creates a new Response message of type specified by the statusCode 
	 * paramater, containing the mandatory headers of the message with a body 
	 * in the form of a byte array and the body content type.
	 *
	 * @param statusCode - the new integer of the statusCode value of this Message.
	 * @param callId - the new CallIdHeader object of the callId value of this Message.
	 * @param cSeq - the new CSeqHeader object of the cSeq value of this Message.
	 * @param from - the new FromHeader object of the from value of this Message.
	 * @param to - the new ToHeader object of the to value of this Message.
	 * @param via - the new List object of the ViaHeaders of this Message.
	 * @param content - the new byte array of the body content value of this Message.
	 * @param contentType - the new ContentTypeHeader object of the content type 
	 * value of this Message.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the statusCode or the body.
	 */
	public Response createResponse(
		int statusCode,
		CallIdHeader callId,
		CSeqHeader cSeq,
		FromHeader from,
		ToHeader to,
		List via,
		MaxForwardsHeader maxForwards,
		byte[] content,
		ContentTypeHeader contentType)
		throws ParseException {
		if (callId == null
			|| cSeq == null
			|| from == null
			|| to == null
			|| via == null
			|| maxForwards == null
			|| content == null
			|| contentType == null)
			throw new NullPointerException("Null params ");

		SIPResponse sipResponse = new SIPResponse();
		sipResponse.setStatusCode(statusCode);
		sipResponse.setCallId(callId);
		sipResponse.setCSeq(cSeq);
		sipResponse.setFrom(from);
		sipResponse.setTo(to);
		sipResponse.setVia(via);
		sipResponse.setMaxForwards(maxForwards);
		sipResponse.setHeader((ContentType) contentType);
		sipResponse.setMessageContent(content);

		return sipResponse;
	}

	/**
	 * Creates a new Response message of type specified by the statusCode 
	 * paramater, containing the mandatory headers of the message. This new 
	 * Response does not contain a body.
	 *
	 * @param statusCode - the new integer of the statusCode value of this Message.
	 * @param callId - the new CallIdHeader object of the callId value of this Message.
	 * @param cSeq - the new CSeqHeader object of the cSeq value of this Message.
	 * @param from - the new FromHeader object of the from value of this Message.
	 * @param to - the new ToHeader object of the to value of this Message.
	 * @param via - the new List object of the ViaHeaders of this Message.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the statusCode.
	 */
	public Response createResponse(
		int statusCode,
		CallIdHeader callId,
		CSeqHeader cSeq,
		FromHeader from,
		ToHeader to,
		List via,
		MaxForwardsHeader maxForwards)
		throws ParseException {
		if (callId == null
			|| cSeq == null
			|| from == null
			|| to == null
			|| via == null
			|| maxForwards == null)
			throw new ParseException(
				"JAIN-SIP Exception, some parameters are missing"
					+ ", unable to create the response",
				0);

		SIPResponse sipResponse = new SIPResponse();
		sipResponse.setStatusCode(statusCode);
		sipResponse.setCallId(callId);
		sipResponse.setCSeq(cSeq);
		sipResponse.setFrom(from);
		sipResponse.setTo(to);
		sipResponse.setVia(via);
		sipResponse.setMaxForwards(maxForwards);

		return sipResponse;
	}

	// Response Creation methods based on a Request    

	/**
	 * Creates a new Response message of type specified by the statusCode 
	 * paramater, based on a specific Request with a new body in the form of a 
	 * Java object and the body content type.
	 *
	 * @param statusCode - the new integer of the statusCode value of this Message.
	 * @param request - the received Reqest object upon which to base the Response.
	 * @param content - the new Object of the body content value of this Message.
	 * @param contentType - the new ContentTypeHeader object of the content type 
	 * value of this Message.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the statusCode or the body.
	 */
	public Response createResponse(
		int statusCode,
		Request request,
		ContentTypeHeader contentType,
		Object content)
		throws ParseException {
		if (request == null || content == null || contentType == null)
			throw new NullPointerException("null parameters");

		SIPRequest sipRequest = (SIPRequest) request;
		SIPResponse sipResponse = sipRequest.createResponse(statusCode);
		sipResponse.setContent(content, contentType);

		return sipResponse;
	}

	/**
	 * Creates a new Response message of type specified by the statusCode 
	 * paramater, based on a specific Request with a new body in the form of a 
	 * byte array and the body content type.
	 *
	 * @param statusCode - the new integer of the statusCode value of this Message.
	 * @param request - the received Reqest object upon which to base the Response.
	 * @param content - the new byte array of the body content value of this Message.
	 * @param contentType - the new ContentTypeHeader object of the content type 
	 * value of this Message.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the statusCode or the body.
	 */
	public Response createResponse(
		int statusCode,
		Request request,
		ContentTypeHeader contentType,
		byte[] content)
		throws ParseException {
		if (request == null || content == null || contentType == null)
			throw new NullPointerException("null Parameters");

		SIPRequest sipRequest = (SIPRequest) request;
		SIPResponse sipResponse = sipRequest.createResponse(statusCode);
		sipResponse.setHeader((ContentType) contentType);
		sipResponse.setMessageContent(content);

		return sipResponse;
	}

	/**
	 * Creates a new Response message of type specified by the statusCode 
	 * paramater, based on a specific Request message. This new Response does 
	 * not contain a body.
	 *
	 * @param statusCode - the new integer of the statusCode value of this Message.
	 * @param request - the received Reqest object upon which to base the Response.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the statusCode.
	 */
	public Response createResponse(int statusCode, Request request)
		throws ParseException {
		if (request == null)
			throw new NullPointerException("null parameters");

		// if (LogWriter.needsLogging)
		//	LogWriter.logMessage("createResponse " + request);

		SIPRequest sipRequest = (SIPRequest) request;
		SIPResponse sipResponse = sipRequest.createResponse(statusCode);
		// Remove the content from the message (Bug report from
		// Antonis Karydas.
		sipResponse.removeContent();
		sipResponse.removeHeader(ContentTypeHeader.NAME);

		return sipResponse;
	}

	/** Creates a new Request message of type specified by the method paramater,
	 * containing the URI of the Request, the mandatory headers of the message
	 * with a body in the form of a byte array and body content type.
	 *
	 * @param requestURI - the new URI object of the requestURI value 
	 * 		of this Message.
	 * @param method - the new string of the method value of this Message.
	 * @param callId - the new CallIdHeader object of the callId value 
	 * 		of this Message.
	 * @param cSeq - the new CSeqHeader object of the cSeq value of 
	 * 		this Message.
	 * @param from - the new FromHeader object of the from value of 
	 *		this Message.
	 * @param to - the new ToHeader object of the to value of this Message.
	 * @param via - the new List object of the ViaHeaders of this Message.
	 * @param contentType - the new ContentTypeHeader object of the content type
	 * value of this Message.
	 * @param content - the new byte array of the body content 
	 * value of this Message.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the method or the body.
	 */
	public Request createRequest(
		javax.sip.address.URI requestURI,
		String method,
		CallIdHeader callId,
		CSeqHeader cSeq,
		FromHeader from,
		ToHeader to,
		List via,
		MaxForwardsHeader maxForwards,
		ContentTypeHeader contentType,
		byte[] content)
		throws ParseException {
		if (requestURI == null
			|| method == null
			|| callId == null
			|| cSeq == null
			|| from == null
			|| to == null
			|| via == null
			|| maxForwards == null
			|| content == null
			|| contentType == null)
			throw new NullPointerException("missing parameters");

		SIPRequest sipRequest = new SIPRequest();
		sipRequest.setRequestURI(requestURI);
		sipRequest.setMethod(method);
		sipRequest.setCallId(callId);
		sipRequest.setCSeq(cSeq);
		sipRequest.setFrom(from);
		sipRequest.setTo(to);
		sipRequest.setVia(via);
		sipRequest.setMaxForwards(maxForwards);
		sipRequest.setContent(content, contentType);
		return sipRequest;
	}

	/** Creates a new Response message of type specified by the statusCode
	 * paramater, containing the mandatory headers of the message with a body
	 * in the form of a Java object and the body content type.
	 *
	 * @param statusCode the new integer of the 
	 * 	statusCode value of this Message.
	 * @param callId the new CallIdHeader object of the 
	 * 		callId value of this Message.
	 * @param cSeq the new CSeqHeader object of the cSeq value of this Message.
	 * @param from the new FromHeader object of the from value of this Message.
	 * @param to the new ToHeader object of the to value of this Message.
	 * @param via the new List object of the ViaHeaders of this Message.
	 * @param contentType the new ContentTypeHeader object of the content type
	 * value of this Message.
	 * @param content the new Object of the body content value of this Message.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the statusCode or the body.
	 */
	public Response createResponse(
		int statusCode,
		CallIdHeader callId,
		CSeqHeader cSeq,
		FromHeader from,
		ToHeader to,
		List via,
		MaxForwardsHeader maxForwards,
		ContentTypeHeader contentType,
		Object content)
		throws ParseException {
		if (callId == null
			|| cSeq == null
			|| from == null
			|| to == null
			|| via == null
			|| maxForwards == null
			|| content == null
			|| contentType == null)
			throw new NullPointerException("missing parameters");
		SIPResponse sipResponse = new SIPResponse();
		StatusLine statusLine = new StatusLine();
		statusLine.setStatusCode(statusCode);
		String reason = SIPResponse.getReasonPhrase(statusCode);
		if (reason == null)
			throw new ParseException(statusCode + " Unknown", 0);
		statusLine.setReasonPhrase(reason);
		sipResponse.setStatusLine(statusLine);
		sipResponse.setCallId(callId);
		sipResponse.setCSeq(cSeq);
		sipResponse.setFrom(from);
		sipResponse.setTo(to);
		sipResponse.setVia(via);
		sipResponse.setContent(content, contentType);
		return sipResponse;

	}

	/** Creates a new Response message of type specified by the statusCode
	 * paramater, containing the mandatory headers of the message with a body
	 * in the form of a byte array and the body content type.
	 *
	 * @param statusCode the new integer of the statusCode value of this Message.
	 * @param callId the new CallIdHeader object of the callId value of this Message.
	 * @param cSeq the new CSeqHeader object of the cSeq value of this Message.
	 * @param from the new FromHeader object of the from value of this Message.
	 * @param to the new ToHeader object of the to value of this Message.
	 * @param via the new List object of the ViaHeaders of this Message.
	 * @param contentType the new ContentTypeHeader object of the content type
	 * value of this Message.
	 * @param content the new byte array of the body content value of this Message.
	 * @throws ParseException which signals that an error has been reached
	 * unexpectedly while parsing the statusCode or the body.
	 */
	public Response createResponse(
		int statusCode,
		CallIdHeader callId,
		CSeqHeader cSeq,
		FromHeader from,
		ToHeader to,
		List via,
		MaxForwardsHeader maxForwards,
		ContentTypeHeader contentType,
		byte[] content)
		throws ParseException {
		if (callId == null
			|| cSeq == null
			|| from == null
			|| to == null
			|| via == null
			|| maxForwards == null
			|| content == null
			|| contentType == null)
			throw new NullPointerException("missing parameters");
		SIPResponse sipResponse = new SIPResponse();
		StatusLine statusLine = new StatusLine();
		statusLine.setStatusCode(statusCode);
		String reason = SIPResponse.getReasonPhrase(statusCode);
		if (reason == null)
			throw new ParseException(statusCode + " : Unknown", 0);
		statusLine.setReasonPhrase(reason);
		sipResponse.setStatusLine(statusLine);
		sipResponse.setCallId(callId);
		sipResponse.setCSeq(cSeq);
		sipResponse.setFrom(from);
		sipResponse.setTo(to);
		sipResponse.setVia(via);
		sipResponse.setContent(content, contentType);
		return sipResponse;
	}

	/**
	 * Create a request from a string. Conveniance method for UACs
	 * that want to create an outgoing request from a string. Only the
	 * headers of the request should be included in the String that is
	 * supplied to this method.
	 *
	 * @param requestString -- string from which to create the message
	 *		null string returns an empty message.
	 */
	public javax.sip.message.Request createRequest(String requestString)
		throws java.text.ParseException {
		if (requestString == null)
			return new SIPRequest();

		StringMsgParser smp = new StringMsgParser();

		SIPMessage sipMessage = smp.parseSIPMessage(requestString);

		if (!(sipMessage instanceof SIPRequest))
			throw new ParseException(requestString, 0);

		return (SIPRequest) sipMessage;
	}
	
	/** Create a response from a string 
	 * 
	 * @param responseString -- string from which to create the message
	 *		null string returns an empty message. 
	 *
	 */
	public Response createResponse (String responseString ) 
	throws java.text.ParseException {
	    if (responseString == null)
			return new SIPResponse();

		StringMsgParser smp = new StringMsgParser();

		SIPMessage sipMessage = smp.parseSIPMessage(responseString);

		if (!(sipMessage instanceof SIPResponse))
			throw new ParseException(responseString, 0);

		return (SIPResponse) sipMessage;
	}
}

