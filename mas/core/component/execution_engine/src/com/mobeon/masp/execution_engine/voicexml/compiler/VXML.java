/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.voicexml.CompilerMacros;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeNode;
import com.mobeon.masp.execution_engine.voicexml.runtime.ShadowVarBase;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.Text;

import java.util.List;

/**
 * User: QMIAN
 * Date: 2005-sep-08
 * Time: 09:23:33
 * <p/>
 * Handles the VXML tag.
 * Sets values of atributes into the Module, and defines scopes.
 */

public class VXML extends NodeCompilerBase {
    static ILogger logger = ILoggerFactory.getILogger(VXML.class);

    public Product compile(Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        if (logger.isDebugEnabled()) logger.debug("Compiling VXML");
        Product vxml = createProduct(parent, element);
        module.setProduct(vxml);
        vxml.add(Ops.logElement(element));

        // Handle grammar
        GrammarScopeNode grammar = CompilerMacros.getDTMFGrammar(module, element);
        if (grammar != null) {
            vxml.add(Ops.registerDTMFGrammar(grammar));
        }
        grammar = CompilerMacros.getASRGrammar(module, element);
        if (grammar != null) {
            vxml.add(Ops.registerASRGrammar(grammar));
        }

        // The context must be initialized before the first form/menu
        // is started. We will put that stuff into the "context initializing
        // product". This product holds <var>, <script>, and other non-menu
        // stuff.
        Product contextInitializingProduct = createProduct(vxml, element);

        // This product holds form and menu:
        Product dialogProduct = createProduct(vxml, element);
        vxml.add(contextInitializingProduct);

        // Examine attributes of <vxml>

        if (! checkRequiredAttributes(module, parent, element)) {
            compilationError(parent, element, module, "Combination of attributes for <vxml> was invalid");
            return parent;
        }
        xmlLang(element, module);
        xmlBase(element, module);
        schemaLocation(element, module);
        contextInitializingProduct.add(Ops.setExecutingModule(module));

        // TODO! can events really be enabled at every VXML? likely not
        contextInitializingProduct.add(Ops.setEventsEnabled(true));
        doScopes(element, contextInitializingProduct, module);

        compile(module, compilerPass, element.content(), contextInitializingProduct, dialogProduct);

        // At this point we put a product that will execute what it finds in
        // the executionContext. On the first start of this Module, there
        // will be no such product, and execution will continue with the first
        // dialog.
        Product executingProduct = createProduct(parent, element);
        contextInitializingProduct.add(executingProduct);
        executingProduct.add(Ops.executeDialogTrampoline());

        // After the end of context initializing, we start executing
        // the first menu, so "hang it in there" :)
        contextInitializingProduct.add(dialogProduct);

        // Register special products which can be used by implementation of
        // goto:

        module.setSpecialProduct(Module.DIALOG_PRODUCT,
                dialogProduct);
        module.setSpecialProduct(Module.VXML_PRODUCT,
                vxml);
        module.setSpecialProduct(Module.DIALOG_TRAMPOLINE_PRODUCT,
                executingProduct);
        module.setSpecialProduct(Module.CONTEXT_INITIALIZING_PRODUCT,
                contextInitializingProduct);

        // Fire a dialog.exit event if we come to this point and are about
        // to fall off the end of the (voicexml) world...

        // Play any remaining, queued prompt
        vxml.add(Ops.playQueuedPrompts());
        vxml.add(Ops.sendDialogEvent(Constants.Event.DIALOG_EXIT, "End of VXML document"));
        vxml.add(Ops.engineShutdown(true));

        return vxml;
    }

    private void schemaLocation(CompilerElement element, Module app) {
        String schemaLocation =
                element.attributeValue(Constants.VoiceXML.SCHEMALOCATION);
        if (CompilerMacros.checkVXMLAttrib(schemaLocation)) {
            app.setDocumentAttribute(Constants.VoiceXML.XSI_SCHEMALOCATION,
                    schemaLocation);
        }
    }

    private boolean checkRequiredAttributes(Module app,
                                            Product parent,
                                            CompilerElement element) {
        String version = element.attributeValue(Constants.VoiceXML.VERSION);
        app.setDocumentAttribute(Constants.VoiceXML.VERSION, version);
        if (! checkVersion(version)) {
            return false;
        }
        String xmlns = null;
        Namespace namespace = element.getNamespace();
        if (namespace != null) {
            xmlns = namespace.getURI();
        }
        app.setDocumentAttribute(Constants.VoiceXML.XMLNS, xmlns);
        if (! checkXmlns(xmlns)) {
            return false;
        }
        String xmlnsXsi = null;
        List additionalNamespaces = element.additionalNamespaces();
        for (Object o : additionalNamespaces) {
            Namespace additionalNamespace = (Namespace) o;
            if (Constants.VoiceXML.XSI.equals(additionalNamespace.getPrefix())) {
                xmlnsXsi = additionalNamespace.getURI();
                break;
            }
        }
        app.setDocumentAttribute(Constants.VoiceXML.XMLNS_XSI, xmlnsXsi);
        if (! checkXmlnsXsi(xmlnsXsi)) {
            return false;
        }
        return true;
    }

    private boolean checkXmlns(String xmlns) {
        if (xmlns == null) {
            return false;
        }
        return xmlns.equals(Constants.VoiceXML.EXPECTEDXMLNS);
    }

    private void doScopes(CompilerElement element, Product product, Module app) {

        // Add scope depending on whether we are the application root document.

        String application = element.attributeValue(Constants.Scope.APPLICATION_SCOPE);
        if (CompilerMacros.checkVXMLAttrib(application)) {
            CompilerMacros.addScope(product, Constants.Scope.DOCUMENT_SCOPE);
            app.setDocumentAttribute(Constants.VoiceXML.APPLICATION, application);
        } else {
            // TODO: removed this temporarily: , Constants.DOCUMENT_SCOPE );
            CompilerMacros.addScope(product, Constants.Scope.APPLICATION_SCOPE);
            product.add(Ops.declareLastResult("lastresult$"));
        }
    }

    private void xmlBase(CompilerElement element, Module app) {
        String xmlBase = element.attributeValue(
                new QName(Constants.VoiceXML.BASE,
                        element.getNamespaceForPrefix(Constants.VoiceXML.XML)));
        if (CompilerMacros.checkVXMLAttrib(xmlBase)) {
            app.setDocumentAttribute(Constants.VoiceXML.XMLBASE, xmlBase);
        }
    }

    private void xmlLang(CompilerElement element, Module app) {
        String xmlLang = element.attributeValue(Constants.VoiceXML.XMLLANG);
        if (CompilerMacros.checkVXMLAttrib(xmlLang)) {
            app.setDocumentAttribute(Constants.VoiceXML.XMLLANG, xmlLang);
        } else {
            // Not found! Defaulting to English!
            //TODO: configure this?
            app.setDocumentAttribute(Constants.VoiceXML.XMLLANG, "en");

        }
    }

    private boolean checkVersion(String version) {
        if (version == null) {
            return false;
        }
        return version.equals(Constants.VoiceXML.VERSION2_0) ||
                version.equals(Constants.VoiceXML.VERSION2_1);
    }

    private boolean checkXmlnsXsi(String value) {
        // null means that xmlns:nsi is not defined, and that is ok.
        if (value == null) {
            return true;
        }
        return value.equals(Constants.VoiceXML.EXPECTEDXMLNS_XSI);
    }

    /**
     * This function compiles the children of <vxml> into two products
     * to enable that the "context" can be initalized first, for example
     * all <var> declarations.
     *
     * @param module
     * @param compilerPass
     * @param containingContent
     * @param contextInitializingProduct
     * @param menuProduct
     */
    private void compile(Module module,
                         Compiler.CompilerPass compilerPass,
                         List<Node> containingContent,
                         Product contextInitializingProduct,
                         Product menuProduct) {

        for (Object o : containingContent) {
            Node node = (Node) o;
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                CompilerElement element = (CompilerElement) node;
                if (element.getName().equals("menu")) {
                    compilerPass.compile(module, menuProduct, element, containingContent);
                } else if (element.getName().equals("form")) {
                    compilerPass.compile(module, menuProduct, element, containingContent);
                } else {
                    compilerPass.compile(module, contextInitializingProduct, element, containingContent);
                }
            } else if (node.getNodeType() == Node.TEXT_NODE) {
                Text text = (Text) node;
                compilerPass.compile(module, contextInitializingProduct, text, containingContent);
            }
        }
    }
}
