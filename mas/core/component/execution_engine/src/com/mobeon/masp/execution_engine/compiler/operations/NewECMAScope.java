package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.scoping.ScopeRegistry;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Apr 12, 2006
 * Time: 5:58:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewECMAScope extends OperationBase  {

    private String name;

    public NewECMAScope(String name){
        this.name = name;
    }
    public String arguments() {
         return "[" + name + "]";
    }

    public void execute(ExecutionContext context) throws InterruptedException {
        ScopeRegistry registry = context.getScopeRegistry();
        registry.createNewECMAScope(name);
    }
}
