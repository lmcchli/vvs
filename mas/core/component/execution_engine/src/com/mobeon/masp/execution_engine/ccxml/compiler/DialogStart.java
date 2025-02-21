/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ccxml.CompilerTools;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.List;

/**
 * Pseudocode with <code>prepareddialogid</code> attribute
 * <p/>
  * <pre>
 *  evaluateECMA_P($preparedialogid)
 *  createStartVXMLByDialogID_T()
 *  sendEvent_T()
 * </pre>
 * Pseudocode with <code>src</code> attribute
 * <pre>
 * evaluateECMA_P($src)
 * (if $type)
 *   evaluateECMA_P($type)
 * (else)
 *   push_T("application/xml+vxml")
 * textArray_P($namelist)
 * createStartVXMLBySrcTypeNamelist_T3P
 * sendEvent_T
 * </pre>
 *
 * @author Mikael Andersson
 */
public class DialogStart extends NodeCompilerBase {
    public Product compile(Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        Product product = createProduct(parent, element);
        product.add(Ops.logElement(element));

        unsupportedAttr(Constants.CCXML.CONFERENCE_ID, element, module);
        unsupportedAttr(Constants.CCXML.DUPLEX, element, module);

        final String prepareddialogid = element.attributeValue(Constants.CCXML.PREPARED_DIALOG_ID);
        final String src = element.attributeValue(Constants.CCXML.SRC);
        final String type = element.attributeValue(Constants.CCXML.TYPE);
        final String namelist = element.attributeValue(Constants.CCXML.NAMELIST);
        final String dialogId = element.attributeValue(Constants.CCXML.DIALOG_ID);
        final String connectionID = element.attributeValue(Constants.CCXML.CONNECTION_ID);

        boolean hasPreparedDialogId =   ( null != prepareddialogid );
        boolean hasSrc =                ( null != src );
        if (
                hasPreparedDialogId &&
                        ! hasSrc) {
            product.add(Ops.evaluateECMA_P(prepareddialogid,module.getDocumentURI(), element.getLine()));
            product.add(Ops.createDialogStartByDialogID_T());
            product.add(Ops.sendEvent_T());
        } else if (
                hasSrc &&
                        ! hasPreparedDialogId) {
            product.add(Ops.evaluateECMA_P(src, module.getDocumentURI(), element.getLine()));
            if (null != type) {
                product.add(Ops.evaluateECMA_P(type, module.getDocumentURI(), element.getLine()));
            } else {
                product.add(Ops.text_P("application/xml+vxml"));
            }
            if (null != namelist) {
                product.add(Ops.textArray_P(namelist.split("[ ]+")));
            } else {
                product.add(Ops.textArray_P());
            }
            if(null != connectionID){
                product.add(Ops.evaluateECMA_P(connectionID, module.getDocumentURI(), element.getLine()));
            } else {
                product.add(Ops.text_P(null));
            }
            product.add(Ops.createDialogStartBySrcTypeNamelist_T4P());
            if(CompilerTools.isValidStringAttribute(dialogId)) {
                product.add(Ops.storeDialogId(dialogId));
            }
            product.add(Ops.sendEvent_T());
        } else {
            String message = "Invalid combination of attributes for dialogstart";
            compilationError(product,element,module, message);
        }
        compileChildren(module, compilerPass, parent, product, element.content());
        return product;
    }

}
