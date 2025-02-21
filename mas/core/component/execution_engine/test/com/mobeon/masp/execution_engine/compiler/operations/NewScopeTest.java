package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Ops;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * newScope Tester.
 *
 * @author <Authors name>
 * @since <pre>09/16/2005</pre>
 */
public class NewScopeTest extends OperationCase
{

    public NewScopeTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(NewScopeTest.class);
    }

    protected void doSetup() {
        mockExecutionContext.expects(once()).method("getScopeRegistry").will(returnValue(mockScopeRegistry.proxy()));
        op = Ops.newScope("firstAlias");
        expect_ScopeRegistry_createNewScope();

    }


    public void testExecute() throws Exception {
        doSetup();
        op.execute(getExecutionContext());
    }

    public void xxxtestExecuteTwoPrefixes() throws Exception {
        fail("not implemented");
        // Nothing regarding scope prefixes currently implemented.
    }
    public void testToMnemonic_TwoPrefixes() throws Exception {
        doSetup();
        op.execute(getExecutionContext());
        eq("NEW_SCOPE([firstAlias])").eval(op.toMnemonic());
    }
}
