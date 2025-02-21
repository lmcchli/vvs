/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.runtime.ExecutionResult;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.Node;

import java.util.List;

/** Compiler for the &lt;disconnect&gt; tag
 * @author David Looberger
 */
public class Disconnect extends NodeCompilerBase  {
    static ILogger logger = ILoggerFactory.getILogger(Disconnect.class);

    public Product compile(Module module, Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        if (logger.isDebugEnabled()) logger.debug("Compiling Disconnect");

        Predicate disconnect = createPredicate(parent, null, element);
        disconnect.add(Ops.logElement(element));

        disconnect.setTagType(Constants.VoiceXML.DISCONNECT);

        String namelist = element.attributeValue(Constants.VoiceXML.NAMELIST);
        // TODO: What to do with the namelist attribute?

        disconnect.add(Ops.enterFinalProcessingState(true));
        // Play all queued prompts, send dialog.disconnect and await the connection.disconnect.hangup event
        disconnect.add(Ops.initiatedDisconnect());
        disconnect.add(Ops.playQueuedPrompts());
        disconnect.add(
                new AtomicExecutable(
                        Ops.sendDialogEvent(Constants.Event.DIALOGDISCONNECT, "<disconnect> tag in VoiceXML document"),
                        Ops.changeExecutionResult(ExecutionResult.EVENT_WAIT)
                )
        );
        // The <disconnect> tag has no children, hence, it is only placed as a child to its parent without
        // recursion

        parent.add(disconnect);

        return disconnect;
    }

}
