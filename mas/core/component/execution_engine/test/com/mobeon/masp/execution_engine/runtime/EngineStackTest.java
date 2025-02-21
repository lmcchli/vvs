package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.masp.execution_engine.Case;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.Executable;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.Arrays;

/**
 * EngineStack Tester.
 *
 * @author Mikael Andersson
 * @since <pre>09/22/2005</pre>
 */
public class EngineStackTest extends Case
{
    private RuntimeFactory factory;

    public EngineStackTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        factory = RuntimeFactories.getInstance(Constants.MimeType.CCXML_MIMETYPE);
        super.setUp();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testStackOperations() throws Exception
    {
        EngineStack es = new EngineStack(100, factory.createData());
        for(int i=0;i<100;i++) {
            es.push(Arrays.asList(new Executable[i]));
        }
        es.prune(37);
        int length = -1;
        if((length = es.peek().ops.size()) != 36) die("peek() returned wrong entry att index 36. This entry should be at: "+length);
        if((length = es.pop().ops.size()) != 36) die("pop() returned wrong entry att index 36. This entry should be at: "+length);
        if((length = es.pop().ops.size()) != 35) die("pop() returned wrong entry att index 35. This entry should be at: "+length);
    }

    public void testStackResult() throws Exception
    {
        StackFrame result;
        RuntimeFactory factory = RuntimeFactories.getInstance(Constants.MimeType.CCXML_MIMETYPE);
        EngineStack es = new EngineStack(100,factory.createData());
        es.push(Arrays.asList(new Executable[0]));
        es.push(Arrays.asList(new Executable[1]));
        result = es.peek();
        es.pop();
        if(result == es.peek()) die("Enginestack returned the same frame for two different stack levels (peek,pop,peek)");
        es.push(Arrays.asList(new Executable[1]));
        result = es.pop();
        if(result == es.pop()) die("Enginestack returned the same frame for two different stack levels (pop,pop)");
    }

    public void testSize() throws Exception
    {
        EngineStack es = new EngineStack(100,factory.createData());
        if(es.size() != 0) die("New stack reported size() = "+es.size()+" it should report 0");
        es.push(Arrays.asList(new Executable[10]));
        if(es.size() != 1) die("Stack with one element reported size() = "+es.size()+" it should report 1");
        es.pop();
        if(es.size() != 0) die("Stack with zero elements after pop reported size() = "+es.size()+" it should report 0");
        for(int i=0;i<100;i++) {
            es.push(Arrays.asList(new Executable[i]));
        }
        if(es.size() != 100) die("Stack with 100 elements reported size() = "+es.size()+" it should report 100");
        try {
            es.push(Arrays.asList(new Executable[10]));
            die("More than 100 elements placed on stack, 100 is the limit in this test");
        }
        catch (EngineStackExhausted soe) {
            //Ignore this error, it's according to the class contract
        }
        es.prune(37);
        if(es.size() != 37) die("Stack with 37 elements after prune reported size() = "+es.size()+" it should report 37");
        es.prune(0);
        if(es.size() != 0) die("Stack with 0 elements after prune reported size() = "+es.size()+" it should report 37");
        if(es.pop() != null) die("pop() beyond end of stack didn't return null");

    }

    public static Test suite()
    {
        return new TestSuite(EngineStackTest.class);
    }
}
