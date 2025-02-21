package com.mobeon.masp.execution_engine.ccxml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.List;

/**
 * Compiles an &lt;eventprocessor&gt; to it's executable representation.
 * <p/>
 *
 * Pseudocode:
 * <pre>
 *  useStateVariable($statevariable)
 * </pre>
 * The &lt;transition&gt; children will also
 * each add the following code to this product.
 * <pre>
 * registerHandler($state,$event,$cond)
 * </pre>
 *
 * @author Mikael Andersson
 */
public class EventProcessor extends NodeCompilerBase {

    public Product compile(Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        Product product = createProduct(parent,element);
        product.add(Ops.logElement(element));
        String statevariable = element.attributeValue(Constants.CCXML.STATE_VARIABLE);
        if(statevariable == null) {
            statevariable = Constants.CCXML_STATEVAR;
            product.add(Ops.introduceECMAVariable(statevariable,product.getDebugInfo()));
        }
        product.add(Ops.useStateVariable(statevariable));
        compileChildren(module,compilerPass,parent,product,element.content());
        product.add(Ops.setEventsEnabled(true));
        return product;
    }
}
