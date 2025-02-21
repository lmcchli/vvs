/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import org.apache.log4j.Priority;
import org.apache.log4j.Level;

/**
 * Created by IntelliJ IDEA.
 * User: MPAZE
 * Date: 2005-feb-23
 * Time: 15:29:27
 */
public class BreadNode extends Node {
   private Node next = null;
   
    private String data;

    public Node execute(Traverser traverser) {

        logger.debug("Executing");
        if (next instanceof ValueNode) {
            Node nextnode = next;
            ValueNode.ValueNodeReturn vrn = ((ValueNode) next).eval(traverser);
            String value = data;
            if (vrn != null)  {
                value += vrn.getValue();
                nextnode = vrn.getNext();
                handleBread(value);
                return nextnode;
            }
        }
        else {
            handleBread(data);
        }
        return next;
    }

    private void handleBread(String data) {
        // TODO: TTS data - log it for now        
       logger.info(data);
    }

    public void setNext(Node _child) {
        next = _child;
    }

    public Node getNext() {
        return next;
    }


    public void setData(String _data) {
        data = _data;
    }

    public String getData() {
        return data;
    }
}
