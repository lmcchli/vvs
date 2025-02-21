/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.products;

import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Executable;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.voicexml.runtime.form_interpretation.FIAObjects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author David Looberger
 */
public class FormPredicate extends PredicateImpl {
    private List<String> formItemNames = new ArrayList<String>();
    private Map<String, List<Executable>> formItems = new HashMap<String, List<Executable>>();
    private Product formItemProduct;
    private FIAObjects fiaObject;

    public FormPredicate(Product parent, String localName, DebugInfo debugInfo) {
        super(parent, localName, debugInfo);
    }

    public void addFormItemName(String name) {
        formItemNames.add(name);
    }

    public void addForItemNames(List<String> names) {
        formItemNames.addAll(names);
    }

    public List<String> getFormItemNames() {
        return formItemNames;
    }

    public List<Executable> getFormItemContents(String name){
        return formItems.get(name);
    }

    public FIAObjects getFiaObject() {
        return fiaObject;
    }

    public void setFiaObject(FIAObjects fiaObject) {
        this.fiaObject = fiaObject;
    }

    /**
     /* Add the actual contents for a form item, for example
     /* what is inside <block>. This could be used for example
     /* when a <goto> wants to execute its contents directly.
     */
    public void putFormItemContents(String name, List<Executable> l){
        formItems.put(name, l);
    }

    /**
     * Return the Product which contains execution of all form items
     * @return the product
     */
    public Product getFormItemProduct(){
        return formItemProduct;
    }

    public void setFormItemProduct(Product p){
        formItemProduct = p;
    }

    public void appendExtraSections(StringAccumulator buf, int indent, String lineSep) {
        super.appendExtraSections(buf,indent,lineSep);
        buf.append(lineSep);
        String section = "<catches>";
        List<Executable> itemList = new ArrayList<Executable>();
        itemList.add(fiaObject.getCatches());
        appendSection(buf, section, itemList, lineSep, indent);

        buf.append(lineSep);

        section = "<formitems>";
        itemList = new ArrayList<Executable>();
        for (String formItemName : fiaObject.getFormItemOrder()) {
            itemList.add(fiaObject.getFormItems().get(formItemName));            
        }
        appendSection(buf, section, itemList, lineSep, indent);
        buf.append(lineSep);

        itemList.clear();
        section = "<filled>";
        itemList.add(fiaObject.getFilled());
        appendSection(buf, section, itemList, lineSep, indent);
        buf.append(lineSep);
    }

    public Class getCanonicalClass() {
        return FormPredicate.class;
    }
}
