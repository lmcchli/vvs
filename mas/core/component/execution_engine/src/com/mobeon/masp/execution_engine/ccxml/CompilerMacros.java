/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Ops;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.xml.CompilerElement;

import java.net.URI;

public class CompilerMacros {

    public static void evaluateConnectionId_P(CompilerElement element, Product parent, URI uri, int lineNumber) {
        String connectionId = element.attributeValue(Constants.CCXML.CONNECTION_ID);
        if(CompilerTools.isValidStringAttribute(connectionId)) {
            parent.add(Ops.evaluateECMA_P(connectionId, uri, lineNumber));
        } else {
            parent.add(Ops.eventVar_P(Constants.CCXML.CONNECTION_ID));
        }
    }

    public static void evaluateStringAttribute_P(
            String attributeName, CompilerElement element, Product parent, String defaultValue, boolean evaluateDefault, URI uri, int lineNumber) {
        String attribute = element.attributeValue(attributeName);
        if(CompilerTools.isValidStringAttribute(attribute)) {
            parent.add(Ops.evaluateECMA_P(attribute, uri, lineNumber));
        } else {
            if(evaluateDefault) {
                parent.add(Ops.evaluateECMA_P(defaultValue, uri, lineNumber));
            } else {
                parent.add(Ops.text_P(defaultValue));
            }
        }
    }

    public static void stringAttribute_P(String attributeName, CompilerElement element, Product parent) {
        String attribute = element.attributeValue(attributeName);
        if(CompilerTools.isValidStringAttribute(attribute)) {
            parent.add(Ops.text_P(attribute));
        } else {
            parent.add(Ops.text_P(null));
        }
    }

    public static boolean evaluateRequiredStringAttribute_P(String attributeName, CompilerElement element, Product parent, URI uri, int lineNumber) {
        String attribute = element.attributeValue(attributeName);
        if(CompilerTools.isValidStringAttribute(attribute)) {
            parent.add(Ops.evaluateECMA_P(attribute, uri, lineNumber));
            return true;
        } else {
            parent.add(Ops.sendEvent(Constants.Event.ERROR_SEMANTIC,
                    "Required attribute " + attributeName + " was missing in " + element.getName(), DebugInfo.getInstance(element)));
            return false;
        }
    }

    public static String invalidAttrMessage(String tagName, String attrName, String value) {
        return "Attribute " + attrName + " had invalid value " + value + " in <" + tagName + ">";
    }

    public static String bothAreDefinedMessage(CompilerElement element, String attrName1, String attrName2){
        return "Both " + attrName1 + " and " + attrName2 + " can not be defined for <" + element.getName() + ">";
    }
}
