/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.graph;

import com.mobeon.executor.Traverser;
import com.mobeon.ecma.ECMAExecutor;
import com.mobeon.frontend.Stream;
import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Expression;

import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: QDALO
 * Date: 2005-feb-22
 * Time: 18:23:41
 * To change this template use File | Settings | File Templates.
 */
public class AudioNode extends Node {
    private Node next;
    private String src;
    private Expression expr;
    private boolean interruptable = true;
    private String docBase;

    public AudioNode() {
        docBase = System.getProperty("PROMPT_DIR");
        if (docBase == null) {
            docBase = "";
        }
    }

    public Node execute(Traverser traverser) {
        logger.debug("Executing Audio");
        Stream stream = traverser.getOutputStream();

        String media[] = null;

        if (expr != null) {
            ECMAExecutor ecma = traverser.getEcmaExecutor();
            media = evalExpr(ecma);

        }
        else if (src != null) {
            media = splitFiles(src);
        }
        else {
            logger.error("Audio tag has neither src or expr!");
            return next;
        }
        if (stream == null) {
            logger.error("No output stream! Can not play prompt:");
            for (int i =0 ; i < media.length; i++) {
                logger.error(media[i]);
            }
            return next;
        }
        try {
            stream.playPrompt(media, interruptable);
        } catch (InterruptedException e) { } // Don't bother with the exception

        return next;
    }

    public void setNext(Node child) {
        this.next = child;
    }

    public Node getNext() {
        return next;
    }

    private String[] evalExpr(ECMAExecutor ecma) {
        String result = (String) expr.eval(ecma);

        return splitFiles(result);
    }

    private String[] splitFiles(String files) {
        // Split the space separated list into a String[]
        StringTokenizer st = new StringTokenizer(files);
        String ret[] = new String[st.countTokens()];
        int i = 0;
        while (st.hasMoreTokens()) {
            String file = st.nextToken();
            if (!(file.startsWith("/") || file.startsWith("c:")) && docBase.length() > 0)
                file = docBase + "/" + file;
            ret[i++] = file;
        }
        return ret;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }
}
