/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.xml.CompilerElement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
 
/**
 * @author Mikael Andersson
 */
public class Constants {
    public static final String CCXML_STATEVAR = "_state";
    public static final String CCXML_EVENTEVAR = "_evt";

    public static class VoiceXML {

        private static final Set<String> types = new HashSet<String>();
        private static final Set<String> inputItemSet = new HashSet<String>(10);
        private static final Set<String> formItemSet = new HashSet<String>(10);


        public static final String ALL = "all";
        public static final String ANY = "any";
        public static final String ASSIGN = "assign";
        public static final String MODAL = "modal";
        public static final String BEEP = "beep";
        public static final String MAXTIME = "maxtime";
        public static final String FINALSILENCE = "finalsilence";
        public static final String DTMFTERM = "dtmfterm";
        public static final String CATCH = "catch";
        public static final String SRC = "src";
        public static final String TYPE = "type";
        public static final String NAMELIST = "namelist";
        public static final String COND = "cond";
        public static final String NAME = "name";
        public static final String LABEL = "label";
        public static final String EXPR = "expr";
        public static final String VALUE = "value";
        public static final String ID = "id";
        public static final String BARGEIN = "bargein";
        public static final String BARGEINTYPE = "bargeintype";
        public static final String COUNT = "count";
        public static final String TIMEOUT = "timeout";
        public static final String XMLLANG = "xml:lang";
        public static final String XMLBASE = "xml:base";
        public static final String XMLNS = "xmlns";
        public static final String URI = "uri";
        public static final String XMLNS_XSI = "xmlns:xsi";
        public static final String FETCHHINT = "fetchhint";
        public static final String FETCHTIMEOUT = "fetchtimeout";
        public static final String MAXAGE = "maxage";
        public static final String DOCUMENTMAXAGE = "documentmaxage";
        public static final String MAXSTALE = "maxstale";
        public static final String EXPECTEDXMLNS = "http://www.w3.org/2001/vxml";
        public static final String EXPECTEDXMLNS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
        public static final String XSI_SCHEMALOCATION = "xsi:schemaLocation";
        public static final String LOCATION = "location";
        public static final String TAGNAME = "tagname";
        public static final String REASON = "reason";
        public static final String SCOPE = "scope";
        public static final String NOINPUT = "noinput";
        public static final String NOMATCH = "nomatch";
        public static final String ERROR = "error";
        public static final String MODE = "mode";
        public static final String DTMF = "dtmf";
        public static final String ROOT = "root";
        public static final String VERSION = "version";
        public static final String VERSION2_0 = "2.0";
        public static final String VERSION2_1 = "2.1";
        public static final String APPLICATION = "application";
        public static final String SLOT = "slot";
        public static final String EVENT = "event";
        public static final String XML = "xml";
        public static final String BASE = "base";
        public static final String SCHEMALOCATION = "schemaLocation";
        public static final String XSI = "xsi";
        public static final String NEXT = "next";
        public static final String NEXTITEM = "nextitem";
        public static final String EXPRITEM = "expritem";
        public static final String NAMEEXPR = "nameexpr";
        public static final String EVENTEXPR = "eventexpr";
        public static final String MESSAGE = "message";
        public static final String _MESSAGE = "_message";
        public static final String MESSAGEEXPR = "messageexpr";
        public static final String PROPERTY = "property";
        public static final String GRAMMAR = "grammar";
        public static final String FORM = "form";
        public static final String DTMFUTTERANCE_EVENT = "internal.dtmfutterance";
        public static final String ASRUTTERANCE_EVENT = "internal.asrutterance";
        public static final String VXML = "vxml";
        public static final String RECORD = "record";
        public static final String TERMCHAR = "termchar";
        public static final String SRCEXPR = "srcexpr";
        public static final String HELP = "help";
        public static final String FIELD = "field";
        public static final String SUBDIALOG = "subdialog";
        public static final String TRANSFER = "transfer";
        public static final String INITIAL = "initial";
        public static final String BLOCK = "block";
        public static final String OBJECT = "object";
        public static final String DOCUMENT = "document";
        public static final String DISCONNECT = "disconnect";
        public static final String CLEAR = "clear";
        public static final String FETCHAUDIO = "fetchaudio";
        public static final String CHARSET = "charset";
        public static final String DEST = "dest";
        public static final String DESTEXPR = "destexpr";
        public static final String BRIDGE = "bridge";
        public static final String CONNECTIONTIMEOUT = "connecttimeout";
        public static final String TRANSFERAUDIO = "transferaudio";
        public static final String TRANSFERAUDIOEXPR = "transferaudioexpr";
        public static final String AAI = "aai";
        public static final String AAIEXPR = "aaiexpr";
        public static final String DIALOG = "dialog";
        public static final String INTERDIGITTIMEOUT = "interdigittimeout";
        public static final String TERMTIMEOUT = "termtimeout";
        public static final String ENCTYPE = "enctype";
        public static final String METHOD = "method";
        public static final String DEFAULT_TERMCHAR = "#";
        public static final String DEFAULT_TERMTIMEOUT = "0";
        public static final String LOG = "log";
        public static final String PROMPT = "prompt";
        public static final String VAR = "var";
        public static final String IF = "if";
        public static final String AUDIO = "audio";
        public static final String FILLED = "filled";
        public static final String SCRIPT = "script";
        public static final String REPROMPT = "reprompt";
        public static final String CHOICE = "choice";
        public static final String LINK = "link";
        public static final String EXIT = "exit";
        public static final String MARK = "mark";
        public static final String THROW = "throw";
        public static final String GOTO = "goto";
        public static final String RETURN = "return";
        public static final String PARAM = "param";
        public static final String NEAR_END_DISCONNECT = "near_end_disconnect";


        private static final Map<String, Entity> elements = new HashMap<String,Entity>(100);

        static {
            types.add("boolean");
            types.add("currency");
            types.add("date");
            types.add("digits");
            types.add("number");
            types.add("phone");
            types.add("time");

            inputItemSet.add("field");
            inputItemSet.add("record");
            inputItemSet.add("subdialog");
            inputItemSet.add("initial");
            inputItemSet.add("object");
            inputItemSet.add("transfer");

            formItemSet.addAll(inputItemSet);
            formItemSet.add("block");

            elements.put(VXML,Entity.VXML);
            elements.put(ASSIGN,Entity.ASSIGN);
            elements.put(BLOCK,Entity.BLOCK);
            elements.put(FORM,Entity.FORM);
            elements.put(LOG,Entity.LOG);
            elements.put(PROMPT,Entity.PROMPT);
            elements.put(VAR,Entity.VAR);
            elements.put(IF,Entity.IF);
            elements.put(AUDIO,Entity.AUDIO);
            elements.put(FIELD,Entity.FIELD);
            elements.put(FILLED,Entity.FILLED);
            elements.put(INITIAL,Entity.INITIAL);
            elements.put(RECORD,Entity.RECORD);
            elements.put(SCRIPT,Entity.SCRIPT);
            elements.put(REPROMPT,Entity.REPROMPT);
            elements.put(CATCH,Entity.CATCH);
            elements.put(TRANSFER,Entity.TRANSFER);
            elements.put(CHOICE,Entity.CHOICE);
            elements.put(LINK,Entity.LINK);
            elements.put(EXIT,Entity.EXIT);
            elements.put(OBJECT,Entity.OBJECT);
            elements.put(MARK,Entity.MARK);
            elements.put(THROW,Entity.THROW);
            elements.put(GOTO,Entity.GOTO);
            elements.put(VALUE,Entity.VALUE);
            elements.put(SUBDIALOG,Entity.SUBDIALOG);
            elements.put(PARAM,Entity.PARAM);
            elements.put(RETURN,Entity.RETURN);
            elements.put(PROPERTY,Entity.PROPERTY);
            elements.put(DISCONNECT,Entity.DISCONNECT);
            elements.put(CLEAR,Entity.CLEAR);
        }


        public static boolean isValidFieldType(String type) {
            return types.contains(type);
        }

        public static boolean isInputItemChild(CompilerElement element) {
            TagType tagType = element.getTagType();
            return tagType.isChildTypeOf(inputItemSet);
        }

        public static boolean isFormItemChild(CompilerElement element) {
            TagType tagType = element.getTagType();
            return tagType.isChildTypeOf(formItemSet);
        }

        public static Entity nameToEntity(String name) {
            return elements.get(name);
        }

        @SuppressWarnings({"InnerClassFieldHidesOuterClassField"})
        public enum Entity {
            VXML,
            ASSIGN,
            BLOCK,
            FORM,
            LOG,
            PROMPT,
            VAR,
            IF,
            AUDIO,
            FIELD,
            FILLED,
            INITIAL,
            RECORD,
            SCRIPT,
            REPROMPT,
            CATCH,
            TRANSFER,
            CHOICE,
            LINK,
            EXIT,
            OBJECT,
            MARK,
            THROW,
            GOTO,
            VALUE,
            SUBDIALOG,
            PARAM,
            RETURN,
            PROPERTY,
            DISCONNECT,
            CLEAR,
        }
    }

    public static class PlatformProperties {
        public static final String PLATFORM_RECORD_MAXTIME = "com.mobeon.platform.record_maxtime";
        public static final String PLATFORM_AUDIO_OFFSET = "com.mobeon.platform.audio_offset";
        public static final String PLATFORM_TRANSFER_MAXTIME = "com.mobeon.platform.transfer_maxtime";
        public static final String PLATFORM_TRANSFER_CONNECTTIMEOUT = "com.mobeon.platform.transfer_connecttimeout";
        public static final String PLATFORM_TRANSFER_ANI = "com.mobeon.platform.transfer_ani";
        public static final String PLATFORM_TRANSFER_LOCAL_PI = "com.mobeon.platform.transfer_local_pi";
        public static final String PLATFORM_TRANSFER_SERVER_ADDRESS = "com.mobeon.platform.transfer_serveraddress";
        public static final String PLATFORM_TRANSFER_SERVER_PORT = "com.mobeon.platform.transfer_serverport";
        public static final String TRANSFER_PROPERTIES = "com.mobeon.platform.transfer_properties";
        public static final String PLATFORM_RECORD_TYPE = "com.mobeon.platform.record_type";
        public static final String DEFAULT_PLATFORM_TRANSFER_LOCAL_PI = "1";
        public static final String PLATFORM_RECORD_FINALSILENCE = "com.mobeon.platform.record_finalsilence";
    }

    public static class CCXML {
        public static final String CCXML = "ccxml";
        public static final String DIALOG_ID = "dialogid";
        public static final String CONNECTION_ID = "connectionid";
        public static final String CONFERENCE_ID = "conferenceid";
        public static final String PREPARED_DIALOG_ID = "prepareddialogid";
        public static final String TYPE = VoiceXML.TYPE;
        public static final String NAMELIST = VoiceXML.NAMELIST;
        public static final String EVENT = VoiceXML.EVENT;
        public static final String SRC = VoiceXML.SRC;
        public static final String STATE = "state";
        public static final String COND = VoiceXML.COND;
        public static final String NAME = VoiceXML.NAME;
        public static final String LABEL = VoiceXML.LABEL;
        public static final String EXPR = VoiceXML.EXPR;
        public static final String STATE_VARIABLE = "statevariable";
        public static final String REASON = "reason";
        public static final String CALLER_ID = "callerid";
        public static final String DESTINATION = "dest";
        public static final String DUPLEX = "duplex";
        public static final String IMMEDIATE = "immediate";
        public static final String HINTS = "hints";
        public static final String AAI = "aai";
        public static final String USE = "use";
        public static final String TIMEOUT = "timeout";
        public static final String DATA = "data";
        public static final String TARGET = "target";
        public static final String TARGETTYPE = "targettype";
        public static final String DELAY = "delay";
        public static final String ID1 = "id1";
        public static final String ID2 = "id2";
        public static final String SENDID = "sendid";
        public static final String MAXTIME = "maxtime";
        public static final String URI = "URI";
        public static final String CONNECTTIMEOUT = "connecttimeout";
        public static final String TRANSFER_LOCAL_PI = "transfer_local_pi";
        public static final String TRANSFER_ANI = "transfer_ani";
        public static final String VALUES = "values";
        public static final String PI = "pi";
        public static final String _CALLTYPE = "_calltype";
        public static final String CALLTYPE = "calltype";
        public static final String _EARLYMEDIA = "_earlymedia";
        public static final String INFO = "info";
        public static final String OUTBOUNDCALLSERVERHOST = "outboundcallserverhost";
        public static final String OUTBOUNDCALLSERVERPORT = "outboundcallserverport";   
        public static final String SERVER = "server";
        public static final String PORT = "port";
        public static final String REJECT_EVENT_TYPE   = "rejecteventtype";
        public static final String DIVERSION_MAILBOX   = "mailbox";
        public static final String DIVERSION_HOST_IP   = "hostip";
        public static final String DIVERSION_REASON    = "reason";
        public static final String DIVERSION_COUNTER   = "counter";
        public static final String DIVERSION_LIMIT     = "limit";
        public static final String DIVERSION_PRIVACY   = "privacy";
        public static final String DIVERSION_SCREEN    = "screen";
        public static final String DIVERSION_EXTENSION = "extension";
        public static final String CALLER_INFO_FROM_DISPLAY_NAME = "callerinfofromdisplayname";
        public static final String CALLER_INFO_FROM_USER = "callerinfofromuser";
        public static final String CALLER_INFO_PAI_DISPLAY_NAME_FIRST_VALUE = "callerinfopaidisplaynamefirstvalue";
        public static final String CALLER_INFO_PAI_DISPLAY_NAME_SECOND_VALUE = "callerinfopaidisplaynamesecondvalue";
        public static final String CALLER_INFO_PAI_FIRST_VALUE = "callerinfopaifirstvalue";
        public static final String CALLER_INFO_PAI_SECOND_VALUE = "callerinfopaisecondvalue";
    }

    public static class Scope {
        public static final String APPLICATION_SCOPE = "application";
        public static final String DOCUMENT_SCOPE = "document";
        public static final String DIALOG_SCOPE = "dialog";
        public static final String ANONYMOUS_SCOPE = null;
    }

    public static class MimeType {
        public static final String VOICEXML_MIMETYPE = "application/xml+vxml";
        public static final String CCXML_MIMETYPE = "application/xml+ccxml";
        public static final String DEFAULT_MEDIA_MIME = "audio/pcmu";
    }

    public static enum VoiceXMLState {
        STATE_UNKNOWN (){ public String toString(){return "unknown";} },
        STATE_WAITING  (){ public String toString(){return "mobeon.waiting";}},
        STATE_TRANSITIONING  (){ public String toString(){return "mobeon.transitioning";}};
    }

    public static class Prefix {

        public static final String CONNECTION = "connection";
        public static final String ERROR = "error";
    }

    public static class Event {
        public static final String CONTEXT = "context";        
        public static final String PREFIX_CONNECTION_DISCONNECT = Prefix.CONNECTION + ".disconnect";
        public static final String CONNECTION_DISCONNECT_HANGUP = PREFIX_CONNECTION_DISCONNECT + ".hangup";
        public static final String ERROR_SEMANTIC = Prefix.ERROR + ".semantic";
        public static final String ERROR_BADFETCH = Prefix.ERROR + ".badfetch";
        public static final String ERROR_NOTALLOWED = Prefix.ERROR + ".notallowed";
        public static final String ERROR_CONNECTION = Prefix.ERROR + ".connection";
        public static final String ERROR_UNSUPPORTED = Prefix.ERROR + ".unsupported";
        public static final String ERROR_NORESOURCE = Prefix.ERROR + ".noresource";
        public static final String ERROR_REQUEST_TIMEOUT = Prefix.ERROR + ".requesttimeout";                
        public static final String CONNECTION_ALERTING = Prefix.CONNECTION + ".alerting";
        public static final String CONNECTION_FAILED = Prefix.CONNECTION + ".failed";
        public static final String CONNECTION_SIGNAL = Prefix.CONNECTION + ".signal";
        public static final String CONNECTION_CONNECTED = Prefix.CONNECTION + ".connected";
        public static final String CONNECTION_PROGRESSING = Prefix.CONNECTION + ".progressing";
        public static final String CONNECTION_DISCONNECTED = Prefix.CONNECTION + ".disconnected";
        public static final String CONNECTION_DISCONNECT = Prefix.CONNECTION + ".disconnect";
        public static final String CONNECTION_PROXIED = Prefix.CONNECTION + ".proxied";
        public static final String CONNECTION_REDIRECTED = Prefix.CONNECTION +".redirected";
        public static final String DIALOG_EXIT = "dialog.exit";
        public static final String DIALOG_STARTED = "dialog.started";
        public static final String TERMINATE_TRANSFER = "dialog.terminatetransfer";
        public static final String DIALOGDISCONNECT = "dialog.disconnect";
        public static final String CCXML_KILL = "ccxml.kill";
        public static final String CCXML_LOADED = "ccxml.loaded";
        public static final String PLAY_FINISHED = "internal.play.finished";
        public static final String PLAY_FINISHED_HANGUP = PLAY_FINISHED + ".hangup";
        public static final String PLAY_FAILED = "internal.play.failed";
        public static final String PLAY_FAILED_HANGUP = PLAY_FAILED + ".hangup";
        public static final String RECORD_FINISHED = "internal.record.finished";
        public static final String RECORD_FINISHED_HANGUP = RECORD_FINISHED + ".hangup";
        public static final String RECORD_FAILED = "internal.record.failed";
        public static final String RECORD_FAILED_HANGUP = RECORD_FAILED + ".hangup";
        public static final String CCXML_KILL_UNCONDITIONAL = CCXML_KILL + ".unconditional";
        public static final String UNSUPPORTED_OBJECTNAME = "error.unsupported.objectname";
        public static final String CCXML_EXIT = "ccxml.exit";
        public static final String ERROR_DIALOG_NOT_STARTED = "error.dialog.notstarted";
        public static final String DIALOG_TRANSFER = "dialog.transfer";
        public static final String DIALOG_TRANSFER_COMPLETE = "dialog.vxml.transfer.complete";
        public static final String ERROR_SEND_TARGETTYPE_INVALID = "error.send.targettypeinvalid";
        public static final String TELEPHONE_DISCONNECT_TRANSFER = "telephone.disconnect.transfer";
        public static final String ERROR_CONFERENCE_JOIN = "error.conference.join";
        public static final String CONFERENCE_JOINED = "conference.joined";
        public static final String MOBEON_PlATFORM_EARLYMEDIARESOURCEAVAILABLE = "com.mobeon.platform.earlymediaresourceavailable";
        public static final String MOBEON_PlATFORM_EARLYMEDIARESOURCEFAILED = "com.mobeon.platform.earlymediaresourcefailed";
        public static final String MOBEON_PLATFORM_SIPMESSAGERESPONSEEVENT = "com.mobeon.platform.sipmessageresponse";
        public static final String CONFERENCE_UNJOINED = "conference.unjoined";
        public static final String ERROR_CONFERENCE_UNJOIN = "error.confererence.unjoin";
        public static final String DTMF_WAKEUP_EVENT = "internal.dtmfwakeupevent";
		public static final String SUBSCRIBE_RECEIVED = "subscribe.received";
        
    }

    public static class SpecialProducts {
    } 

    public static class CallProperties {
        public static final String VOICE = "voice";
        public static final String VIDEO = "video";
    }
    public class SRGS {
        public static final String URI = "uri";
        public static final String ONEOF = "one-of";
        public static final String ITEM = "item";
        public static final String RULEREF = "ruleref";
        public static final String REPEAT = "repeat";
    }

    /**
     * System properties, that control the behaviour of EE
     */
    public class SYSTEM {
        public static final String TRACE_ENABLED = "TraceEnabled"; // set to true or false
        public static final String SHUTDOWNGRACETIME = "ShutdownGraceTime";

    }
}
