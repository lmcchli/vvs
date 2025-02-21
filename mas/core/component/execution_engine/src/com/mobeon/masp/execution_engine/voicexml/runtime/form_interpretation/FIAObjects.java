/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation;

import com.mobeon.masp.execution_engine.compiler.Product;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * The class handles the different form items used during the FIA, e.g. input items, catches, vars, filled etc.
 * One FIAObject exists per form, and is shared by all executing sessions. Session specific information, such as
 * which form items have been executed etc. is maintained in the {@link com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAState} object.
 * @author David Looberger
 */
public class FIAObjects {

    protected Hashtable<String, Product> formItems = null;
    protected List<String> formItemOrder = null;
    protected Product catches = null;
    protected Product varsAndScripts = null;
    protected Product filled = null;
    protected Product scripts = null;
    protected String id = null;
    private Product theForm = null;

    public FIAObjects() {
        formItems = new Hashtable<String, Product>();
        formItemOrder = new ArrayList<String>();
    }

    public Product getForm() {
        return theForm;
    }

    public void setForm(Product theForm) {
        this.theForm = theForm;
    }

    public void addItem(String name, Product product) {
        formItems.put(name, product);
        formItemOrder.add(name);
    }

    public Hashtable<String, Product> getFormItems() {
        return formItems;
    }

    public List<String> getFormItemOrder() {
        return formItemOrder;
    }

    public Product getFilled() {
        return filled;
    }

    public void setFilled(Product filled) {
        this.filled = filled;
    }

    public Product getVarsAndScripts() {
        return varsAndScripts;
    }

    public void setVarsAndScripts(Product vars) {
        this.varsAndScripts = vars;
    }

    public Product getCatches() {
        return catches;
    }

    public void setCatches(Product catches) {
        this.catches = catches;
    }

    /**
     * @deprecated
     */
    public Product getScripts() {
        return scripts;
    }

    /**
     * @deprecated
     * @param scripts
     */
    public void setScripts(Product scripts) {
        this.scripts = scripts;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
