package com.mobeon.masp.execution_engine.compiler;

/**
 * <b>Threading: </b>This class is safe to use concurrently from any number of threads, in any way.
 * @author Mikael Andersson
 */

public interface CompilerDispatcher {

    /**
     * Selects the appropriate {@link NodeCompiler} based on the supplied
     * XML node name.
     * @param nodeName Node name to find a compiler for
     * @return The selected NodeCompiler
     */
    public NodeCompiler dispatch(String nodeName);

    /**
     * Returns an appropriate {@link TextCompiler}
     * @return TextCompiler instance
     */
    public TextCompiler dispatchText();
}
