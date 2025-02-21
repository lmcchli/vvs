package com.mobeon.masp.execution_engine.compiler;

/**
 * @author Mikael Andersson
 */
public interface EngineCallable extends Executable{
    void appendExtraSections(ExecutableBase.StringAccumulator sa, int indent, String lineSep);

}
