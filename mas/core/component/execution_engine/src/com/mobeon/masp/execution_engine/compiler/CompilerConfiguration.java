/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import java.util.List;

public class CompilerConfiguration {
    public final List<Compiler.CompilerPass> compilerPasses;
    public final State prototype;
    public final CompilerBookkeeping bookkeeping;
    public CompilerConfiguration(List<Compiler.CompilerPass> compilerPasses, State prototype, CompilerBookkeeping bookkeeping) {
        this.bookkeeping = bookkeeping;
        this.compilerPasses = compilerPasses;
        this.prototype = prototype;
    }
}
