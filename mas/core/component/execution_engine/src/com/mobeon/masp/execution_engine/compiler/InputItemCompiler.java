package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.voicexml.compiler.FormItemCompiler;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kenneth Selins
 *         <p/>
 *         The purpose of this class is to contain help methods for compiling input
 *         items. To be called by the actual input items compilers, e.g. Record.
 */
public abstract class InputItemCompiler extends FormItemCompiler {

    /**
     * Compiles the children of the input item. The method will compile
     * the children into two separate Products. An eventual <filled> element
     * will be compiled into one Product and all remaining elements
     * to one Product.
     * <p/>
     * containingContent: elements of the input item.
     */
    protected static void compileInputItemChildren(Module module,
                                                   Compiler.CompilerPass compilerPass,
                                                   Product filledProduct,
                                                   Product otherProduct,
                                                   List<Node> containingContent) {
        for (Object o : containingContent) {
            Node node = (Node) o;
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                CompilerElement element = (CompilerElement) node;
                if (element.getName().equals("filled")) {
                    compileInputItemChild(compilerPass, module, filledProduct, element, containingContent);
                }
            }
        }
        compileChildren(module, compilerPass, otherProduct, null, containingContent);
    }

    /**
     * Compiles the children of the input item. The method will compile
     * the children into two separate Products. An eventual <filled> element
     * will be compiled into one Product and all remaining elements
     * to one Product.
     * <p/>
     * containingContent: elements of the input item.
     */
    protected static void compileInputItemChildren(Module module, Compiler.CompilerPass compilerPass, Product filledProduct, Product otherProduct, Product parent, CompilerElement parentElement, List<Node> containingContent) {
        List<Node> params = null;
        Product catches = createProduct(parent, parentElement);
        Product properties = createProduct(parent, parentElement);

        int prevSize = 0;
        //The following loops looks weird, but what it does is that it only increments i if the array stays
        //the same size. Otherwise it repeats the loop so we won't skip any elements ( which could
        //happen otherwise )
        for (int i = 0; i < containingContent.size(); i += oneOrZero(prevSize, containingContent)) {
            prevSize = containingContent.size();
            Node node = containingContent.get(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                CompilerElement element = (CompilerElement) node;
                Product productForEntity = null;

                Constants.VoiceXML.Entity entity = Constants.VoiceXML.nameToEntity(element.getName());
                switch (entity) {
                    case FILLED:
                        productForEntity = filledProduct;
                        break;
                    case PROPERTY:
                        productForEntity = properties;
                        break;
                    case CATCH:
                        productForEntity = catches;
                        break;
                    case PARAM:
                        params = ensureParams(params);
                        params.add(element);
                        containingContent.remove(element);
                        break;
                }
                if(productForEntity != null)
                    compileInputItemChild(compilerPass, module, productForEntity, element, containingContent);
            }
        }
        if (parent instanceof InputItem) {
            InputItem inputItem = (InputItem) parent;
            inputItem.setCatches(catches);
            inputItem.setProperties(properties);
        } else {
            parent.add(catches);
            parent.add(properties);
        }
        Compiler.compile(module, compilerPass, otherProduct, containingContent);
        if (parentElement.getTagType().isTypeOf("subdialog")) {
            otherProduct.add(Ops.mark_P());
            if (params != null) {
                compilerPass.compile(module, otherProduct, params);
            }
        }
    }

    private static void compileInputItemChild(Compiler.CompilerPass compilerPass, Module module, Product filledProduct, CompilerElement element, List<Node> containingContent) {
        compilerPass.compile(module, filledProduct, element, containingContent);
        containingContent.remove(element);
    }

    private static int oneOrZero(int prevSize, List<Node> containingContent) {
        return prevSize == containingContent.size() ? 1 : 0;
    }

    private static List<Node> ensureParams(List<Node> params) {
        if (params == null)
            return new ArrayList<Node>(5);
        return params;
    }


}
