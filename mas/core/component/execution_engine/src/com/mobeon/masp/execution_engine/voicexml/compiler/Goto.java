package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.NodeCompilerBase;
import com.mobeon.masp.execution_engine.compiler.Ops;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.voicexml.CompilerMacros;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.net.URI;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Jan 2, 2006
 * Time: 2:35:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class Goto extends NodeCompilerBase {
    public Product compile(Module module, com.mobeon.masp.execution_engine.compiler.Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        Product gotoProduct = createProduct(parent, element);
        gotoProduct.add(Ops.logElement(element));

        // UnwindAndCall_TM_T up to the enclosing form.
        // Then start executing the target.

        Product form = module.getSpecialProduct(Module.FORM_BEING_COMPILED);
        if (form == null) {
            //TODO
        }
        unsupportedAttr(Constants.VoiceXML.FETCHAUDIO, element, module);
        unsupportedAttr(Constants.VoiceXML.FETCHHINT, element, module);
        unsupportedAttr(Constants.VoiceXML.MAXSTALE, element, module);

        CompilerMacros.stringAttribute_P(Constants.VoiceXML.MAXAGE, element, gotoProduct);
        CompilerMacros.stringAttribute_P(Constants.VoiceXML.FETCHTIMEOUT, element, gotoProduct);

        // There are 2 kinds of goto: to a form item or to another dialog.
        // Only one kind can be satisfied at a time.

        String next = element.attributeValue(Constants.VoiceXML.NEXT);
        String expr = element.attributeValue(Constants.VoiceXML.EXPR);
        String nextitem = element.attributeValue(Constants.VoiceXML.NEXTITEM);
        String expritem = element.attributeValue(Constants.VoiceXML.EXPRITEM);

        // Exactly one must be defined.
        if (numberOfSetAttributes(next, expr, nextitem, expritem) != 1) {
            compilationError(parent, element, module, Constants.Event.ERROR_BADFETCH,
                    "Invalid combination of attributes to <goto>");
            return parent;
        }

        if (next != null || expr != null) {
            compileGotoToAnotherDialog(expr, gotoProduct, next, form, module.getDocumentURI(), element.getLine());
        } else {
            compileGotoToAnotherFormItem(expritem, gotoProduct, nextitem, form, module.getDocumentURI(), element.getLine());
        }

        parent.add(gotoProduct);

        return gotoProduct;
    }

    private void compileGotoToAnotherDialog(String expr,
                                            Product gotoProduct,
                                            String next,
                                            Product form,
                                            URI uri,
                                            int lineNumber) {
        if (expr != null) {
            gotoProduct.add(Ops.evaluateECMA_P(expr, uri, lineNumber));
        } else {
            gotoProduct.add(Ops.text_P(next));
        }
        gotoProduct.add(Ops.analyzeGotoType_TP(form));
        boolean unwindSuppliedProduct = true;
        gotoProduct.add(Ops.unwindAndCall_TM_T(unwindSuppliedProduct));
    }

    private void compileGotoToAnotherFormItem(String expritem,
                                              Product gotoProduct,
                                              String nextitem,
                                              Product form,
                                              URI uri,
                                              int lineNumber) {
        if (expritem != null) {
            gotoProduct.add(Ops.evaluateECMA_P(expritem, uri, lineNumber));
        } else {
            gotoProduct.add(Ops.text_P(nextitem));
        }
        gotoProduct.add(Ops.analyzeFormItemGoto_TP());
        boolean unwindSuppliedProduct = false;
        gotoProduct.add(Ops.unwindAndCall_TM_T(unwindSuppliedProduct));
    }

    private int numberOfSetAttributes(String a1, String a2, String a3, String a4) {
        int total = 0;
        if (a1 != null) {
            total++;
        }
        if (a2 != null) {
            total++;
        }
        if (a3 != null) {
            total++;
        }
        if (a4 != null) {
            total++;
        }
        return total;
    }

}
