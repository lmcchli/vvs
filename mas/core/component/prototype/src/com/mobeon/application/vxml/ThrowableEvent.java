/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 8, 2005
 * Time: 4:20:39 PM
 *
 * 5.2.6 Event Types

There are pre-defined events, and application and platform-specific events. Events are also subdivided into plain events (things that happen normally), and error events (abnormal occurrences). The error naming convention allows for multiple levels of granularity.

A conforming browser may throw an event that extends a pre-defined event string so long as the event contains the specified pre-defined event string as a dot-separated exact initial substring of its event name. Applications that write catch handlers for the pre-defined events will be interoperable. Applications that write catch handlers for extended event names are not guaranteed interoperability. For example, if in loading a grammar file a syntax error is detected the platform must throw "error.badfetch". Throwing "error.badfetch.grammar.syntax" is an acceptable implementation.

Components of event names in italics are to be substituted with the relevant information; for example, in error.unsupported.element, element is substituted with the name of VoiceXML element which is not supported such as error.unsupported.transfer. All other event name components are fixed.

Further information about an event may be specified in the "_message" variable (see Section 5.2.2).

The pre-defined events are:

cancel
    The user has requested to cancel playing of the current prompt.
connection.disconnect.hangup
    The user has hung up.
connection.disconnect.transfer
    The user has been transferred unconditionally to another line and will not return.
exit
    The user has asked to exit.
help
    The user has asked for help.
noinput
    The user has not responded within the timeout interval.
nomatch
    The user input something, but it was not recognized.
maxspeechtimeout
    The user input was too long exceeding the 'maxspeechtimeout' property.

In addition to transfer errors (Section 2.3.7.3), the pre-defined errors are:

error.badfetch
    The interpreter context throws this event when a fetch of a document has failed and the interpreter context has reached a place in the document interpretation where the fetch result is required. Fetch failures result from unsupported scheme references, malformed URIs, client aborts, communication errors, timeouts, security violations, unsupported resource types, resource type mismatches, document parse errors, and a variety of errors represented by scheme-specific error codes.
    If the interpreter context has speculatively prefetched a document and that document turns out not to be needed, error.badfetch is not thrown.  Likewise if the fetch of an <audio> document fails and if there is a nested alternate <audio> document whose fetch then succeeds, or if there is nested alternate text, no error.badfetch occurs.
    When an interpreter context is transitioning to a new document, the interpreter context throws error.badfetch on an error until the interpreter is capable of executing the new document, but again only at the point in time where the new document is actually needed, not before. Whether or not variable initialization is considered part of executing the new document is platform-dependent.
error.badfetch.http.response_code
error.badfetch.protocol.response_code
    In the case of a fetch failure, the interpreter context must use a detailed event type telling which specific HTTP or other protocol-specific response code was encountered. The value of the response code for HTTP is defined in [RFC2616]. This allows applications to differentially treat a missing document from a prohibited document, for instance. The value of the response code for other protocols (such as HTTPS, RTSP, and so on) is dependent upon the protocol.
error.semantic
    A run-time error was found in the VoiceXML document, e.g. substring bounds error, or an undefined variable was referenced.
error.noauthorization
    Thrown when the application tries to perform an operation that is not authorized by the platform. Examples would include dialing an invalid telephone number or one which the user is not allowed to call, attempting to access a protected database via a platform-specific <object>, inappropriate access to builtin grammars, etc.
error.noresource
    A run-time error occurred because a requested platform resource was not available during execution.
error.unsupported.builtin
    The platform does not support a requested builtin type/grammar.
error.unsupported.format
    The requested resource has a format that is not supported by the platform, e.g. an unsupported grammar format, or media type.
error.unsupported.language
    The platform does not support the language for either speech synthesis or speech recognition.
error.unsupported.objectname
    The platform does not support a particular platform-specific object. Note that 'objectname' is a fixed string and is not substituted with the name of the unsupported object.
error.unsupported.element
    The platform does not support the given element, where element is a VoiceXML element defined in this specification. For instance, if a platform does not implement <transfer>, it must throw error.unsupported.transfer. This allows an author to use event handling to adapt to different platform capabilities.

Errors encountered during document loading, including transport errors (no document found, HTTP status code 404, and so on) and syntactic errors (no <vxml> element, etc) result in a badfetch error event raised in the calling document. Errors that occur after loading and before entering the initialization phase of the Form Interpretation Algorithm are handled in a platform-specific manner. Errors that occur after entering the FIA initialization phase, such as semantic errors, are raised in the new document. The handling of errors encountered during the loading of the first document in a session is platform-specific.

Application-specific and platform-specific event types should use the reversed Internet domain name convention to avoid naming conflicts. For example:

error.com.example.voiceplatform.noauth
    The user is not authorized to dial out on this platform.
org.example.voice.someapplication.toomanynoinputs
    The user is far too quiet.

Catches can catch specific events (cancel) or all those sharing a prefix (error.unsupported).
 *
 */
public interface ThrowableEvent
{
}
