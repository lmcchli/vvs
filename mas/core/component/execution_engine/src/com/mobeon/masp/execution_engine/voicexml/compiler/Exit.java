/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import org.dom4j.Node;

import java.util.ArrayList;
import java.util.List;

public class Exit extends NodeCompilerBase {
    private static List<Executable> exitOperations = new ArrayList<Executable>();

    static {
        // If we reach this section where no more form items are elegible for selection,
        // according to 2.2.1 of the VoiceXML specification,
        // an implicit <exit/> is implied..
        // Play any remaining, queued prompt
        exitOperations.add(Ops.setIsExiting());
        exitOperations.add(Ops.playQueuedPrompts());
        exitOperations.add(Ops.sendDialogEvent(Constants.Event.DIALOG_EXIT, "Was about to enter waiting state/No field to select"));
        exitOperations.add(Ops.engineShutdown(true));
    }

    public static List<Executable> getExitOperations() {
        return exitOperations;
    }

    public Product compile(Module module, com.mobeon.masp.execution_engine.compiler.Compiler.CompilerPass compilerPass, Product parent, CompilerElement element, List<Node> content) {
        Product exit = createProduct(parent, element);
        exit.add(Ops.logElement(element));


        unsupportedAttr(Constants.VoiceXML.EXPR, element, module);
        unsupportedAttr(Constants.VoiceXML.NAMELIST, element, module);

        // TODO: Implement, with send event maybe?
        // TODO: Japp, så blir det. DialogExit blir ett lämpligt namn.
        // TODO: Stoppa in returparametrarna i det också.
        exit.add(Ops.setIsExiting());
        // Play any remaining, queued prompt
        exit.add(Ops.playQueuedPrompts());
        exit.add(Ops.sendDialogEvent(Constants.Event.DIALOG_EXIT, "<exit> tag in VoiceXML document"));
        exit.add(Ops.engineShutdown(true));

        parent.add(exit);
        return exit;
    }
}
