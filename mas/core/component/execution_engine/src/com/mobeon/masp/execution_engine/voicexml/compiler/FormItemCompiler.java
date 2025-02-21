package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.NodeCompilerBase;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.compiler.products.FormPredicate;

/**
 * @author Kenneth Selin
 */
public abstract class FormItemCompiler extends NodeCompilerBase {

    /**
     * All form items must be able to register their contents into the
     * containing Form.
     * @param module the module being compiled
     * @param name the name of the form item
     * @param p the form item product
     */
    protected static void  registerContentsInContainingForm(Module module,
                                                    String name,
                                                    Product p){
        if(isCompilingFormPredicate(module, name, p)) {
            Product formProduct = module.getSpecialProduct(
                Module.FORM_BEING_COMPILED);
            FormPredicate form = (FormPredicate) formProduct;
            form.putFormItemContents(name, p.freezeAndGetExecutables());
        };
    }

    protected  static boolean isCompilingFormPredicate(Module module, String name, Product p) {
        Product formProduct = module.getSpecialProduct(
                Module.FORM_BEING_COMPILED);
        return formProduct instanceof FormPredicate;
    }

}
