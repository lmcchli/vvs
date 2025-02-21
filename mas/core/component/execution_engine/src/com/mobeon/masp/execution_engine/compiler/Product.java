/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeNode;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;

import java.util.List;

/**
 * @author Mikael Andersson
 */
public interface Product extends EngineCallable,Cloneable {
    boolean add(Executable o);

    int getDepth();

    void addDestructor(Executable executable);

    void addConstructor(Executable executable);

    List<Executable> freezeAndGetConstructors();

    List<Executable> freezeAndGetExecutables();

    List<Executable> freezeAndGetDestructors();

    void setName(String id);

    String getName();

    void setGrammar(GrammarScopeNode grammar);

    GrammarScopeNode getGrammar();

    DebugInfo getDebugInfo();

    String identity();

    Class<?> getCanonicalClass();

    void setTagType(String tagType);

    String getTagType();

    Product getParent();

    Product clone();

    void addSource(Product product);

    List<Product> getSourceProducts();

    Id<Product> getId();
}