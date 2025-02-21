package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

import java.util.List;


/**
 * An abstract executable unit executed by {@link com.mobeon.masp.execution_engine.runtime.Engine}.
 *
 * @author Mikael Andersson
 */
public interface Executable {
    /**
     * Performs the embedded behaviour of this Executable through
     * manipulating the supplied {@link ExecutionContext}.
     *
     * @param context The Executables current context
     * @throws InterruptedException
     */
    public void execute(ExecutionContext context) throws InterruptedException;

    public String toMnemonic();

    public String toMnemonic(int indent);

    public void appendMnemonic(
    ExecutableBase.StringAccumulator accumulator, int count, boolean recurse, String lineSep, String entrySep);

    public void appendMnemonic(ExecutableBase.StringAccumulator sa,int indent);

    /**
     * Performs all necessary processing so that <code>toMnemonic</code> will be efficient
     */
    public void freeze();

}
