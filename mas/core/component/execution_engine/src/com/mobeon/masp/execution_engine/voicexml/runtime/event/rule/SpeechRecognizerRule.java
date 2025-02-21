/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.runtime.event.rule;

import com.mobeon.common.eventnotifier.Event;
import com.mobeon.masp.execution_engine.runtime.event.rule.EventRuleBase;
import com.mobeon.masp.mediatranslationmanager.RecognitionCompleteEvent;
import com.mobeon.masp.mediatranslationmanager.RecognitionFailedEvent;
import com.mobeon.masp.mediatranslationmanager.RecognitionNoInputEvent;
import com.mobeon.masp.mediatranslationmanager.RecognitionNoMatchEvent;

public class SpeechRecognizerRule extends EventRuleBase {

    public boolean isValid(Event e) {
        if (e instanceof RecognitionCompleteEvent ||
                e instanceof RecognitionNoMatchEvent ||
                e instanceof RecognitionNoInputEvent ||
                e instanceof RecognitionFailedEvent) {
            return logIfValid(true, e);
        }
        return false;
    }

    public String toString() {
        return "isRecognitionEvent()";
    }
}
