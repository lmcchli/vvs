/*
 * COPYRIGHT Abcxyz Communication Inc. Montreal 2009
 * The copyright to the computer program(s) herein is the property
 * of ABCXYZ Communication Inc. Canada. The program(s) may be used
 * and/or copied only with the written permission from ABCXYZ
 * Communication Inc. or in accordance with the terms and conditions
 * stipulated in the agreement/contact under which the program(s)
 * have been supplied.
 *---------------------------------------------------------------------
 * Created on 22-apr-2009
 */
package com.mobeon.masp.execution_engine.runtime.xmlhttprequest;

import java.lang.reflect.InvocationTargetException;

import org.w3c.dom.NamedNodeMap;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * 
 */
public class JsNamedNodeMap extends ScriptableObject {
    
    /**
     * 
     */
    private static final long serialVersionUID = 6640739958523956955L;
    private NamedNodeMap wrappedMap;
    
    public JsNamedNodeMap() {
        // just to make Rhino happy.
    }
    
    @Override
    public String getClassName() {
        return "NamedNodeMap";
    }
    
    public static void register(ScriptableObject scope) {
        try {
            ScriptableObject.defineClass(scope, JsNamedNodeMap.class);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /** * @return Returns the wrappedMap.
     */
    public NamedNodeMap getWrappedMap() {
        return wrappedMap;
    }

    /**
     * @param wrappedMap The wrappedMap to set.
     */
    public void setWrappedMap(NamedNodeMap wrappedMap) {
        this.wrappedMap = wrappedMap;
    }
    
    // Rhino won't let us use a constructor.
    void initialize(NamedNodeMap map) {
        wrappedMap = map;
    }
    
    public static JsNamedNodeMap wrapMap(Scriptable scope, NamedNodeMap map) {
        Context cx = Context.enter();
        JsNamedNodeMap newObject = (JsNamedNodeMap)cx.newObject(scope, "NamedNodeMap");
        newObject.initialize(map);
        return newObject;
    }

    // CHECKSTYLE:OFF
    
    public int jsGet_length() {
        return wrappedMap.getLength();
    }
    
    public Object jsFunction_getNamedItem(String name) {
        return JsSimpleDomNode.wrapNode(getParentScope(), wrappedMap.getNamedItem(name));
    }
    
    public Object jsFunction_getNamedItemNS(String uri, String local) {
        return JsSimpleDomNode.wrapNode(getParentScope(), wrappedMap.getNamedItemNS(uri, local));
    }

    public Object jsFunction_item(int index) {
        return JsSimpleDomNode.wrapNode(getParentScope(), wrappedMap.item(index));
    }
    
    // don't implement the 'modify' APIs.
}
