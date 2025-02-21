package com.mobeon.application;

/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */

import org.w3.x2001.vxml.impl.*;
import org.w3.x2001.vxml.*;
import org.apache.xmlbeans.*;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.Logger;

import com.mobeon.application.vxml.Transformer;
import com.mobeon.application.vxml.VXML;
import com.mobeon.application.vxml.LateBinder;
import com.mobeon.executor.Traverser;
import com.mobeon.executor.DocumentManager;
import com.mobeon.frontend.DTMFSignal;
import com.mobeon.event.MASEventDispatcher;
import com.mobeon.util.ErrorCodes;

import java.io.*;
import java.util.ArrayList;

public class CallFlowCompiler {

    static Logger logger = Logger.getLogger("mobeon.com");

    /**
     *
     * @param args : <vxmldir> <vxmlStartDoc> <properties>
     */
    public static void main(String[] args) {


        PropertyConfigurator.configure(args[2]);
        File file = null;


        try {
            logger.debug("Transforming " + args[0]);
            VXML[] vxml_arr = vxml_arr = Transformer.transformPath(new File(args[0]));
           // if(args.length != 4711) System.exit(4711); // todo: dummy exit - remove
            BuildGraph bg = new BuildGraph();

            for (int i = 0; i < vxml_arr.length; i++) {
                bg.compileDocument(vxml_arr[i]);
            }
            com.mobeon.application.graph.Node root = DocumentManager.getInstance().getDocument(args[1]);
            Traverser t = new Traverser(root);
            t.setControlSignalQ(new DTMFSignal(null,new MASEventDispatcher(), 0));
            logger.debug("\n\nExecuting application\n\n");
            t.run();
        } catch (IOException e) {
            logger.error("Cannot open " + args[0]);
            System.exit(ErrorCodes.GENERAL_ERROR);
        }


    }
}
