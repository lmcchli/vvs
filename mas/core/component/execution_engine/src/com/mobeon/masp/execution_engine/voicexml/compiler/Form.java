/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ccxml.CompilerTools;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.products.FormPredicate;
import com.mobeon.masp.execution_engine.voicexml.CompilerMacros;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeNode;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAObjects;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.Node;
import org.dom4j.Text;

import java.util.List;

/**
 * @author David Looberger
 */
public class Form extends NodeCompilerBase {
    static ILogger logger = ILoggerFactory.getILogger(Form.class);

    public Product compile(Module module, com.mobeon.masp.execution_engine.compiler.Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        if (logger.isDebugEnabled()) logger.debug("Compiling Form (FIA)");

        FIAObjects fia = new FIAObjects();

        String scope = element.attributeValue(Constants.VoiceXML.SCOPE);
        if (scope == null)
            scope = Constants.Scope.DIALOG_SCOPE;

        String id = element.attributeValue(Constants.VoiceXML.ID);

        // Predicate form = createPredicate(parent, null, element);
        FormPredicate form = new FormPredicate(parent, id, DebugInfo.getInstance(element));
        form.add(Ops.logElement(element));

        fia.setId(form.getName());
        fia.setForm(form);

        // Add new scope
        CompilerMacros.addScope(form, scope);

        GrammarScopeNode grammar = CompilerMacros.getDTMFGrammar(module, element);
        if (grammar != null) {
            form.add(Ops.registerDTMFGrammar(grammar));
        }
        grammar = CompilerMacros.getASRGrammar(module, element);
        if (grammar != null) {
            form.add(Ops.registerASRGrammar(grammar));
        }

        if (CompilerTools.isValidStringAttribute(id)) {
            form.setName(id);
            module.registerNamedProduct(form);
        }

        module.setSpecialProduct(Module.FORM_BEING_COMPILED,
                form);

        // FormPredicate formItemProducts = new FormPredicate(form, null, DebugInfo.getInstance(element));
        Product formItemProducts = createProduct(form, element);
        formItemProducts.setName("FORMITEMS");

        form.setFormItemProduct(form);


        if (parent != null)
            parent.add(form);

        // Compile the children
        compile(module, compilerPass, element, element.content(), form, formItemProducts, fia);

        form.setFiaObject(fia);

        form.addConstructor(Ops.setFIAState(fia));
        // Add an operation that will set this Form as executing Form in the
        // executionContext when this form gets invoked.
        form.addConstructor(Ops.setExecutingForm(form));
        form.add(Ops.initializeCatchFIA());
        form.add(Ops.initializeVarFIA());
        form.add(Ops.initializeFormItemsFIA());

        // Select the form item to execute
        form.add(Ops.selectPhaseFIA(fia.getId()));
        // Register property scope and properties in order for
        // properties to be available for the prompts at queue time.
        form.add(Ops.collectPhaseRegisterPropsFIA(fia.getId()));
        // Determine and play prompts
        form.add(Ops.collectPhaseDeterminePromptsFIA(fia.getId()));
        // Collect utterance and execute form items
        form.add(Ops.collectPhaseCollectUtteranceFIA(fia.getId()));
        // Process the input by executing the form filled items
        form.add(Ops.processPhaseFilledActionsFIA(fia.getId()));
        // Once the current item has been properly processed, remove it from the unfinished item list
        form.add(Ops.setItemFinished());
        // Re-run FIA if still unfinished items
        form.add(Ops.rerunFIAIfUnfinishedItems(form));

        // If we reach this section where no more form items are elegible for selection,
        // according to 2.1.1 of the VoiceXML specification,
        // an implicit <exit/> is implied.

        // Play any remaining, queued prompt
        form.add(Ops.playQueuedPrompts());

        form.add(Ops.sendDialogEvent(Constants.Event.DIALOG_EXIT, "End of FORM"));
        form.add(Ops.engineShutdown(true));

        if (module.getSpecialProduct(Module.FIRST_DIALOG) == null) {
            // This form is the first dialog in this document.
            module.setSpecialProduct(Module.FIRST_DIALOG, form);
        }
        return form;
    }

    /**
     * Used to compile the child elements of the &lt;form&gt; node, used in order to arrange the
     * children according to the FIA.
     * <br>
     * This method re-arrange the children in the following order:
     * - All &lt;var&gt; nodes, in document order
     * - All &lt;script&gt; nodes, in document order
     * - All form items in document order
     * <br>
     *
     * @param module
     * @param containingContent
     * @param formItems
     * @param fia
     */
    private void compile(Module module, Compiler.CompilerPass compilerPass, CompilerElement element, List<Node> containingContent, FormPredicate form,  Product formItems,  FIAObjects fia) {
        // Create "sub-products" which will allow for re-ordering of child elements according to FIA
         // Give them names to simplify mnemonic printout
         Product catches = createProduct(form,element);
         catches.setName("CATCH");
         fia.setCatches(catches);
         Product varsandscripts = createProduct(form, element);
         varsandscripts.setName("VARSANDSCRIPTS");
         fia.setVarsAndScripts(varsandscripts);
        /* Product scripts = createProduct(form, element);
         scripts.setName("SCRIPTS");
         fia.setScripts(scripts);*/
         Product filledItems = createProduct(form, element);
         filledItems.setName("FORM_FILLED");
         fia.setFilled(filledItems);



        for (Object o : containingContent) {
            Node node = (Node) o;
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                CompilerElement el = (CompilerElement) node;
                if (el.getName().equals("var")) {
                    compilerPass.compile(module, varsandscripts, el, containingContent);
                } else if (el.getName().equals("catch")) {
                    compilerPass.compile(module, catches, el, containingContent);
                } else if (el.getName().equals("script")) {
                    compilerPass.compile(module, varsandscripts, el, containingContent);
                } else if (el.getName().equals(("filled"))) {
                    compilerPass.compile(module, filledItems, el, containingContent);
                } else if (isFormItem(el.getName())) {
                    Product formItem = compilerPass.compile(module, formItems, el, containingContent);
                    String name = formItem.getName();
                    fia.addItem(name, formItem);
                }
                else {
                    compilerPass.compile(module, form, el, containingContent);
                }

            } else if (node.getNodeType() == Node.TEXT_NODE) {
                Text text = (Text) node;
                compilerPass.compile(module, formItems, text, containingContent);
            }
        }
    }

    private boolean isFormItem(String item) {
        if (item == null)
            return false;
        return item.equals("field") ||
                item.equals("record") ||
                item.equals("object") ||
                item.equals("subdialog") ||
                item.equals("transfer") ||
                item.equals("initial") ||
                item.equals("block");
    }

}
