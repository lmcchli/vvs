/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.products;

import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Executable;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.compiler.ProductSupport;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductBase extends ProductSupport implements Product {
    private List<Executable> operationList = new ArrayList<Executable>();
    private List<Executable> destructorList = new ArrayList<Executable>();
    private List<Executable> constructorList = new ArrayList<Executable>();
    private final int depth;
    private boolean operationListFrozen;
    private boolean destructorListFrozen;
    private boolean constructorListFrozen;
    private GrammarScopeNode grammar = null;
    private final Object LOCK = new Object();

    //TODO: Make this field obsolete by fixing the real problems, which is that  
    //the compilation interface doesn't contain a type
    private Product parent;
    private String tagType = null;

    private final List<Product> sourceProducts = new ArrayList<Product>();


    public ProductBase(Product aParent, String localName, DebugInfo debugInfo) {
        super(debugInfo,localName);
        parent = aParent;
        if (aParent != null) {
            depth = aParent.getDepth() + 1;
        } else {
            depth = 0;
        }
    }

    public ProductBase(Product aParent, DebugInfo aDebugInfo) {
        super(aDebugInfo, null);
        parent = aParent;
        if (aParent != null) {
            depth = aParent.getDepth() + 1;
        } else {
            depth = 0;
        }
    }

    public boolean add(Executable o) {
        return operationList.add(o);
    }

    public void add(int index, Executable element) {
        operationList.add(index, element);
    }


    public void addDestructor(Executable executable) {
        destructorList.add(executable);
    }

    public void addConstructor(Executable executable) {
        constructorList.add(executable);
    }

    public void execute(ExecutionContext ex) throws InterruptedException {
        // Since a call() pushes onto the stack, we call()
        // first, and then execute the constructors.
        ex.call(freezeAndGetExecutables(), this);
        if (!constructorList.isEmpty()) {
            try {
                List<Executable> list = freezeAndGetConstructors();

                for (Executable e : list) {
                    e.execute(ex);
                    ex.getFrameData().constructorProgress++;
                }

            } catch (InterruptedException e) {
                ex.wasInterrupted();
            }
        }
    }

    public Executable get(int i) {
        return operationList.get(i);
    }

    public List<Executable> freezeAndGetExecutables() {
        synchronized (LOCK) {
            if (!operationListFrozen) {
                operationList = Collections.unmodifiableList(operationList);
                operationListFrozen = true;
            }
            return operationList;
        }
    }

    public int getDepth() {
        return depth;
    }

    public List<Executable> freezeAndGetDestructors() {
        synchronized (LOCK) {
            if (!destructorListFrozen) {
                destructorList = Collections.unmodifiableList(destructorList);
                destructorListFrozen = true;
            }
            return destructorList;
        }
    }

    public List<Executable> freezeAndGetConstructors() {
        synchronized (LOCK) {
            if (!constructorListFrozen) {
                constructorList = Collections.unmodifiableList(constructorList);
                constructorListFrozen = true;
            }
            return constructorList;
        }
    }

    public void appendExtraSections(StringAccumulator sa, int indent, String lineSep) {
        if (!constructorList.isEmpty()) {
            sa.append(lineSep);
            String section = "<ctor>";
            appendSection(sa, section, constructorList, lineSep, indent);
            sa.append(lineSep);
        }
        if (!operationList.isEmpty()) {
            sa.append(lineSep);
            appendSection(sa, "<main>", operationList, lineSep, indent);
            sa.append(lineSep);
        }
        if (!destructorList.isEmpty()) {
            sa.append(lineSep);
            String section = "<dtor>";
            appendSection(sa, section, destructorList, lineSep, indent);
            sa.append(lineSep);
        }
        if (!sourceProducts.isEmpty()) {
            sa.append(lineSep);
            appendSection(sa, "<declarations>", sourceProducts, lineSep, indent);
            sa.append(lineSep);
        }
    }

    public void addSource(Product product) {
        sourceProducts.add(product);
    }

    public List<Product> getSourceProducts() {
        return sourceProducts;
    }


    public Product getParent() {
        return parent;
    }

    public GrammarScopeNode getGrammar() {
        return grammar;
    }

    public void setGrammar(GrammarScopeNode grammar) {
        this.grammar = grammar;
    }

    public String identity() {
        return kind() + tag();
    }

    public Product clone() {
        try {
            return (Product) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public void setTagType(String aTagType) {
        tagType = aTagType;
    }

    public String getTagType() {
        if (tagType == null) {
            return "";
        } else {
            return tagType;
        }
    }
}
