/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.voicexml.CompilerMacros;
import com.mobeon.masp.execution_engine.ccxml.CompilerTools;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.products.PredicateImpl;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.Node;

import java.util.List;

/**
 * Compiler class for compiling &lt;prompt&gt; tags to the executable representation.
 * <p>
 *
 */
public class Prompt extends NodeCompilerBase {
    private static ILogger logger = ILoggerFactory.getILogger(Prompt.class);


    /**
     * Compile the &lt;form&gt; element into a predicate containg an optional condition
     * and add compiled the child elements to the predicate.
     * @param app
     * @param compilerPass
     * @param parent
     * @param element
     * @param content
     * @return the executable representation of the &lt;prompt&gt; element and its children.
     */
    public Product compile(Module app, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        if (logger.isDebugEnabled()) logger.debug("Compiling Prompt");
        //Product product = createProduct(parent, element);
        boolean compileError = false;
        String compileMessage = "";
        PromptImpl prompt = new PromptImpl(parent, null, DebugInfo.getInstance(element));
        prompt.add(Ops.logElement(element));

        // Prompts do not really have property scopes but this is an
        // implementation solution to the fact that the prompt may have
        // attributes overriding properties. The attributes are added in
        // this property scope: 
        prompt.addConstructor(Ops.addProperyScope(true));
        prompt.addDestructor(Ops.leaveProperyScope());

        // attributes of the <prompt>-tag check:
        // http://www.w3.org/TR/2004/REC-voicexml20-20040316/#dml4.1
        // for their meaning
        String bargein = element.attributeValue(Constants.VoiceXML.BARGEIN);
        if (bargein != null) {
            bargein = bargein.toLowerCase().trim();
            if (!CompilerTools.validateTrueOrFalse(bargein)) {
                compileMessage = CompilerMacros.invalidAttrMessage(element, Constants.VoiceXML.BARGEIN, bargein);
                compileError = true;
            }
        }
        prompt.setBargein(bargein);

        String bargeintype = element.attributeValue(Constants.VoiceXML.BARGEINTYPE);
        if (bargeintype != null) {
            bargeintype = bargeintype.trim();
            if (!validateBargeinType(bargeintype)) {
                compileMessage = CompilerMacros.invalidAttrMessage(element, Constants.VoiceXML.BARGEINTYPE, bargeintype);
                compileError = true;
            }
        }
        prompt.setBargeintype(bargeintype);

        String cond = element.attributeValue(Constants.VoiceXML.COND);
        if (cond != null) {
            prompt.setCond(cond);
            prompt.addToPredicate(Ops.evaluateECMA_P(cond, app.getDocumentURI(), element.getLine()));
        }

        String count = element.attributeValue(Constants.VoiceXML.COUNT);
        if (count == null) {
            count = "1";
        }
        prompt.setCount(Integer.parseInt(count));

        String timeout = element.attributeValue(Constants.VoiceXML.TIMEOUT);
        if (timeout != null && !Validate.validateTime(timeout, "^[0-9]+(s|ms)$")) {
            compileMessage = CompilerMacros.invalidAttrMessage(element, Constants.VoiceXML.TIMEOUT, timeout);
            compileError = true;
        }
        prompt.setTimeout(timeout);

        String lang = element.attributeValue(Constants.VoiceXML.XMLLANG);
        prompt.setLang(lang);

        String base = element.attributeValue(Constants.VoiceXML.XMLBASE);
        prompt.setBase(base);

        if (compileError) {
            compilationError(parent, element, app, compileMessage);
        }
        if (isFormItem(element.getParent().getName())) {
            PredicateImpl predicateImpl = (PredicateImpl) parent;
            predicateImpl.addPrompt(prompt);
        } else {
            // parent is something else that a form item, e.g. an <if>
            parent.add(Ops.queuePrompt(prompt));
        }

        // compileChildren(app, compilerPass, parent, prompt, element.content());
        // Do not add the prompt as child to the parent node. Prompt handling performed by the FIA,
        // and are played once the interpreter reaches the WAITING state, or the interpreter is about to exit
        Compiler.compile(app, compilerPass, prompt, element.content());

        prompt.setIsChildToInputItem(Constants.VoiceXML.isInputItemChild(element));
        return prompt;
    }

    private boolean isFormItem(String item) {
        if (item == null)
            return false;
        return item.equals("field") ||
                item.equals("record") ||
                item.equals("object") ||
                item.equals("subdialog") ||
                item.equals("transfer") ||
                item.equals("initial");
           //     item.equals("block");

    }

    private boolean validateBargeinType(String value) {
        if (value.equals("speech") ||
            value.equals("hotword") ||
            value.length() == 0) {
            return true;
        } else {
            return false;
        }
    }


}
