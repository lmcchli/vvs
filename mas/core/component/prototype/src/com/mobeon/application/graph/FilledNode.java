package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: MPAZE
 * Date: 2005-mar-01
 * Time: 17:11:33
 */
public class FilledNode extends Node {
    private Node next;
    private List  namelist = null;
    private String mode = "all";


    public void addToNameList(String name) {
        if(namelist == null) namelist = new ArrayList();
        namelist.add(name);
    }

    public void setMode(String _mode){
        if (_mode == null || !(mode.equalsIgnoreCase("all") || mode.equalsIgnoreCase("any"))) {
            logger.error("FilledNode node with illegal mode");
            return;
        }

    }
    public void setNext(Node next) {
        this.next = next;
    }


    public Node getNext() {
        return next;
    }

     // todo: does return next solve the filled node - is it a compile time problem only
    public  Node execute(Traverser traverser) {

         logger.debug("Execute filled Node");
         /*

        if(namelist == null) { // in a field
            return next;
        }
        if (mode.equalsIgnoreCase("any")) {

        }   else if (mode.equalsIgnoreCase("all")) {

        }  */
       traverser.newScope(false);
       return next;
    }
}
