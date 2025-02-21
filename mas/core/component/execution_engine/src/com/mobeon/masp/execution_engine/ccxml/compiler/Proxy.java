/**
 * COPYRIGHT (c) Abcxyz Canada Inc., 2010.
 * All Rights Reserved.
 *
 * The copyright to the computer program(s) herein is the property of
 * Abcxyz Canada Inc.  The program(s) may be used and/or copied only with the
 * written permission from Abcxyz Canada Inc. or in accordance with the terms
 * and conditions stipulated in the agreement/contract under which the
 * program(s) have been supplied.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler;

import java.util.List;

import org.dom4j.Node;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ccxml.CompilerMacros;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.NodeCompilerBase;
import com.mobeon.masp.execution_engine.compiler.Ops;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.compiler.Compiler.CompilerPass;
import com.mobeon.masp.execution_engine.xml.CompilerElement;

public class Proxy extends NodeCompilerBase {

	@Override
	public Product compile(Module module, CompilerPass compilerPass,
			Product parent, CompilerElement element, List<Node> content) {
		parent.add(Ops.logElement(element));
		CompilerMacros.evaluateRequiredStringAttribute_P(Constants.CCXML.PORT,element,parent, module.getDocumentURI(), element.getLine());
		CompilerMacros.evaluateRequiredStringAttribute_P(Constants.CCXML.SERVER,element,parent, module.getDocumentURI(), element.getLine());
		CompilerMacros.evaluateConnectionId_P(element, parent, module.getDocumentURI(), element.getLine());
		parent.add(Ops.connectionProxy_T3());
		return parent;
	}

}










