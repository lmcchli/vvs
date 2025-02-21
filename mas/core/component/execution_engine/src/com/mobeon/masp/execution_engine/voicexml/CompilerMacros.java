/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ccxml.CompilerTools;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.Ops;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.voicexml.grammar.Grammar;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeNode;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeTree;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Element;

import java.net.URI;
import java.util.ArrayList;
import java.util.StringTokenizer;


public class CompilerMacros {

    /**
     * Sets an attribute value to the product. If not found a null text_P operation is set.
     *
     * @param attrName name on attribute
     * @param element  to get value from
     * @param product
     */
    public static void stringAttribute_P(String attrName, CompilerElement element, Product product) {
        String attribute = element.attributeValue(attrName);
        if (CompilerTools.isValidStringAttribute(attribute)) {
            product.add(Ops.text_P(attribute));
        } else {
            product.add(Ops.text_P(null));
        }
    }

    // depreciated?
    public static final void handleCondition(final Product product, final String cond, URI uri, int lineNumber) {
        if (cond == null) return;
        product.add(Ops.evaluateECMA_P(cond, uri, lineNumber));
        // TODO: Impklement branch
    }

    // depreciated?
    public static final void handleGuardCondition(final Product product, final String guard) {
        if (guard == null) return;
        //TODO: Implement branch

    }

    /**
     * Adds a scope and a scope destructor to a Product
     *
     * @param product   The product awaiting the new scope / destructor
     * @param scopeName Type of scope, one of application, document, dialog or "" for anonymous scope
     */
    public static final void addScope(final Product product, final String scopeName) {
        product.addConstructor(Ops.newScope(scopeName));
        product.addDestructor(Ops.closeScope());
    }

    /**
     * Adds an ECMA scope only, i.e. no property scope for example, and a
     * scope destructor to a Product
     *
     * @param product   The product awaiting the new scope / destructor
     * @param scopeName Type of scope, one of application, document, dialog or "" for anonymous scope
     */
    public static final void addECMAScope(final Product product, final String scopeName) {
        product.addConstructor(Ops.newECMAScope(scopeName));
        product.addDestructor(Ops.closeECMAScope());
    }


    /**
     * Checks "str" and returns true if str has a valid attribute value.
     *
     * @param str
     * @return
     */
    public static final boolean checkVXMLAttrib(final String str) {
        if (str == null || str.trim().equals("")) {
            return false;
        }
        return true;
    }

    public static GrammarScopeNode getDTMFGrammar(Module module, Element element) {
        GrammarScopeTree gtree = module.getDTMFGrammarTree();
        if (gtree == null) return null;
        GrammarScopeNode gsn;

        gsn = gtree.getGrammars(element);
        return gsn;

    }

    public static ArrayList<CompilerElement> getGrammarsPerMode(ArrayList<CompilerElement> list, Grammar.InputMode mode) {
        ArrayList<CompilerElement> ret = null;

        for (CompilerElement elem : list) {

            if (elem.attributeValue(Constants.VoiceXML.MODE).equals(mode.toString())) {
                if (ret == null) {
                    ret = new ArrayList<CompilerElement>();
                }
                ret.add(elem);
            }

        }
        return ret;
    }

    public static String getFileName(URI uri) {
        String file_name = uri.getPath();
        StringTokenizer st = new StringTokenizer(file_name, "/");
        while (st.hasMoreTokens()) {
            file_name = st.nextToken();
        }
        return file_name;
    }

    public static GrammarScopeNode getASRGrammar(Module module, CompilerElement element) {
        GrammarScopeTree gtree = module.getASRGrammarTree();
        if (gtree == null) return null;
        GrammarScopeNode gsn;

        gsn = gtree.getGrammars(element);
        return gsn;
    }

    public static String bothAreDefinedMessage(CompilerElement element, String attrName1, String attrName2) {
        return "Both " + attrName1 + " and " + attrName2 + " can not be defined for <" + element.getName() + ">";
    }

    public static String invalidAttrMessage(CompilerElement element, String attrName, String value) {
        return "Attribute " + attrName + " had invalid value " + value + " in <" + element.getName() + ">";
    }

}
