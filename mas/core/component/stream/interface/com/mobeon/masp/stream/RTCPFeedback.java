/**
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.stream;

import java.util.HashMap;
import java.util.Vector;

/**
 * @author Stefan Berglund
 */
public final class RTCPFeedback {
    public enum RTCPFeedbackAttributes {
        FIR
    }

    private final HashMap<RTCPFeedbackAttributes, Vector<String>> rtcpFeedback =
            new HashMap<RTCPFeedbackAttributes, Vector<String>>();

    public void addFIRValue(String value) {
        Vector<String> vec = rtcpFeedback.get(RTCPFeedbackAttributes.FIR);
        if (vec == null) {
            vec = new Vector<String>(); 
            rtcpFeedback.put(RTCPFeedbackAttributes.FIR, vec);
        }
        vec.add(value);
    }

    public Boolean hasFIR() {
        return rtcpFeedback.containsKey(RTCPFeedbackAttributes.FIR);
    }
}
