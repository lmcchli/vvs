package com.mobeon.masp.execution_engine.voicexml.runtime;

import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

/**
 * @author Mikael Anderssonn
 */
public class Redirector {

    private final InputAggregator inputAggregator;
    private final VXMLExecutionContext vxmlExecutionContext;

    public Redirector(InputAggregator inputAggregator, VXMLExecutionContext vxmlExecutionContext) {
        this.inputAggregator = verify(inputAggregator);
        this.vxmlExecutionContext = verify(vxmlExecutionContext);
    }

    private static <T> T verify(T type) {
        if(type == null)
            throw new RuntimeException("Attempted to initialize redirector with null value !");
        return type;
    }

    public static InputAggregator InputAggregator(VXMLExecutionContext ex) {
        return ex.getInputAggregator();
    }

    public static InputAggregator InputAggregator(Redirector redirector) {
        return redirector.inputAggregator;
    }
    public static VXMLExecutionContext VXMLExecutionContext(Redirector redirector) {
        return redirector.vxmlExecutionContext;
    }

    public static PromptQueue PromptQueue(VXMLExecutionContext redirector) {
        return redirector.getPromptQueue();
    }
}
