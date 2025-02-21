package com.mobeon.application.graph.util;

import com.mobeon.application.graph.Node;
import com.mobeon.executor.DocumentManager;

/**
 * Created by IntelliJ IDEA.
 * User: MPAZE
 * Date: 2005-mar-23
 * Time: 15:08:49
 */
public class URIValidator {

     public static  boolean isDocumentURI(String URI){

        if (URI.startsWith("file://") ||
            URI.startsWith("http://") ||
            URI.indexOf(".vxml") > 0)
            return true;
        else
            return false;
    }

    public static  boolean isDialogURI(String URI) {
        if (URI.startsWith("#"))
            return true;
        else
            return false;
    }

    public static  String getDocumentPart(String URI) {
        int idx = URI.indexOf("#");
        if (idx >= 0) {
            return URI.substring(0,idx);
        }
        else
            return URI;
    }

    public static  String getDialogPart(String URI) {
        int idx = URI.indexOf("#");
        if (idx >= 0) {
            return URI.substring(idx + 1);
        }
        else
            return null;
    }

    public static  Node getDocumentRoot(String URI) {
        return DocumentManager.getInstance().getDocument(URI);
    }
}
