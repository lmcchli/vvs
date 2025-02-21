/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.components;

import com.mobeon.masp.execution_engine.compiler.IApplicationCompiler;

/**
 * Component interface for a class providing the {@link IApplicationCompiler} service
 * within the com.mobeon.masp.execution_engine framework.
 * <p/>
 * <b>Note:</b> A class <em>must</em> implement this interface to be viable as a component
 * of the {@link IApplicationCompiler} type.
 *
 * @author Mikael Andersson
 */
public interface ApplicationCompilerComponent extends IApplicationCompiler {
}
