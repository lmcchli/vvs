/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.runtime;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.runtime.scoping.ScopeChangedSubscriber;
import com.mobeon.masp.execution_engine.util.TestEventGenerator;
import com.mobeon.masp.execution_engine.util.TestEvent;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.*;

/**
 * @author David Looberger
 */
public class PropertyStack implements ScopeChangedSubscriber {

    public static final Map<String, String> properties;

    static {
        Map<String, String> props = new HashMap<String, String>();
        // Generic Speech Recognizer Props
        props.put("confidencelevel", "0.5");
        props.put("sensitivity", "0.5");
        props.put("speedvsaccuracy", "0.5");
        props.put("completetimeout", "1.0s"); // Platform specific
        props.put("incompletetimeout", "2.0s"); // Platform specific, should be larger than completetimeout

        // Generic DTMF recognizer props
        props.put("interdigittimeout", "1s"); // Platform specific
        props.put("termtimeout", "0s");
        props.put("termchar", "#");

        // Prompt and Collect Props
        props.put("bargein", "true");
        props.put("bargeintype", "speech"); // Platform specific
        props.put("timeout", "5s");

        // Fetching Props
        props.put("audiofetchhint", "prefetch");
        props.put("audiomaxage", "0s"); // Platform specific, unconditionally request a fresh copy
        props.put("audiomaxstale", "0s");  // Platform specific, , unconditionally request a fresh copy
        props.put("documentfetchhint", "safe");
        props.put(Constants.VoiceXML.DOCUMENTMAXAGE, "600s"); // Platform specific, ten minutes
        props.put("documentmaxstale", "600s"); // Platform specific, ten minutes
        props.put("grammarfetchhint", "prefetch");
        props.put("grammarmaxage", "600s"); // Platform specific, ten minutes
        props.put("grammarmaxstale", "600s"); // Platform specific, ten minutes
        props.put("objectfetchhint", "prefetch");
        props.put("objectmaxage", "600s"); // Platform specific, ten minutes
        props.put("objectmaxstale", "600s"); // Platform specific, ten minutes
        props.put("scriptfetchhint", "prefetch");
        props.put("scriptmaxage", "600s"); // Platform specific, ten minutes
        props.put("scriptmaxstale", "600s"); // Platform specific, ten minutes
        props.put("fetchaudio", ""); // No audio URI
        props.put("fetchaudiodelay", "2s"); // Platform specific
        props.put("fetchaudiominimum", "5s");  // Platform specific
        props.put("fetchtimeout", "5s");  // Platform specific

        // Miscellaneous Props
        props.put("inputmodes", "dtmf voice"); // Set to dtmf to disable ASR
        props.put("universals", "none");
        props.put("maxnbest", "1");

        // Platform properties
        props.put(Constants.PlatformProperties.PLATFORM_AUDIO_OFFSET, "0s"); // Default offset when playing prompts
        props.put(Constants.PlatformProperties.PLATFORM_RECORD_MAXTIME, "180s"); // Max recording time
        props.put(Constants.PlatformProperties.PLATFORM_TRANSFER_MAXTIME, "0s");
        props.put(Constants.PlatformProperties.PLATFORM_TRANSFER_CONNECTTIMEOUT, "30s");
        props.put(Constants.PlatformProperties.PLATFORM_TRANSFER_ANI, "");
        props.put(Constants.PlatformProperties.PLATFORM_TRANSFER_LOCAL_PI,
                Constants.PlatformProperties.DEFAULT_PLATFORM_TRANSFER_LOCAL_PI);
        props.put(Constants.SYSTEM.SHUTDOWNGRACETIME, "30000ms");
        props.put(Constants.PlatformProperties.PLATFORM_RECORD_FINALSILENCE, "3s"); 

        properties = Collections.unmodifiableMap(props);
    }

    Stack<Hashtable<String, String>> propStack;
    Hashtable<String, String> top;
    int level = 0;
    private static final ILogger log = ILoggerFactory.getILogger(PropertyStack.class);


    public PropertyStack() {
        this.propStack = new Stack<Hashtable<String, String>>();
        this.top = new Hashtable<String, String>(properties);
        this.propStack.add(top);
    }


    public String getProperty(String prop) {
        String property = top.get(prop);
        if (log.isDebugEnabled()) {
            log.debug("Getting property " + prop + " from depth " + level + ", value is " + property);
        }
        return property;
    }

    public void putProperty(String prop, String value) {
        if (log.isDebugEnabled()) {
            log.debug("Assigning the property " + prop + " the value " + value +
                    ", old value was " + getProperty(prop)+ ", depth is " + level);
        }
        if(TestEventGenerator.isActive()){
            TestEventGenerator.generateEvent(TestEvent.PROPERTYSTACK_PUT_PROPERTY,prop,value);
            TestEventGenerator.generateEvent(TestEvent.PROPERTYSTACK_PUT_PROPERTY_OLD,prop,getProperty(prop));
        }
        top.put(prop, value);
    }

    public void enteredScope(Scope newScope) {
        Hashtable<String, String> newProps = new Hashtable<String, String>(top);
        propStack.push(newProps);
        level++;
        top = propStack.peek();
        if (log.isDebugEnabled()) log.debug("Entered property scope, depth is now " + level);
    }

    public void leftScope(Scope oldScope) {
        if (propStack.size() > 1) {
            level--;
            propStack.pop();
            top = propStack.peek();
        }
        if (log.isDebugEnabled()) log.debug("Left property scope, depth is now " + level);
    }

    public int getDepth() {
        return level;
    }
}
