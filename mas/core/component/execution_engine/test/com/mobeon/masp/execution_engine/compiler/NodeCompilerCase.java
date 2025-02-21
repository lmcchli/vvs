/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.util.test.MASTestSwitches;
import com.mobeon.masp.execution_engine.ccxml.compiler.DialogStartTest;
import com.mobeon.masp.execution_engine.compiler.products.ProductImpl;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.masp.execution_engine.xml.CompilerElementFactory;
import com.mobeon.masp.execution_engine.xml.XPP3CompilerReader;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.io.XPP3Reader;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;

/**
 * @author Mikael Andersson
 */
public class NodeCompilerCase extends CompilerCase {
    public NodeCompiler nc;
    public Module module;
    public List<Compiler.CompilerPass> compilerPass;
    public Product parent;
    public ArrayList<Node> content;
    public CompilerElement element;
    private Class nodeCompilerClass;
    private String elementName;

    public NodeCompilerCase(String name,Class nodeCompilerClass,String elementName) {
        super(name);
        this.nodeCompilerClass = nodeCompilerClass;
        this.elementName = elementName;
        MASTestSwitches.initForCompilerTest();

    }

    public void setUp() throws Exception {
        super.setUp();
        nc = (NodeCompiler)nodeCompilerClass.newInstance();
        module = new Module(new URI("file://just_some_uri"));
        content = new ArrayList<Node>();
        setupParent();
        setupElement();
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    public Product compile() {
        return nc.compile(module, Compiler.VXML_CODEGEN, parent, element, content);
    }

    public void setCompilerPasses(List<Compiler.CompilerPass> compilerPass){
        this.compilerPass = compilerPass;
    }

    private void setupElement() {
        element = new CompilerElement(new CompilerElementFactory(),new QName(elementName), 1, 1);
    }

    private void setupParent() {
        parent = new ProductImpl(null, null, DebugInfo.getInstance());
    }

    public void validateResultAndParent(Product result, Product parent) {
        if (result == null)
            fail("compile(...) must never return null");
        if (noOperations(result) && noConstructors(result) && noDestructors(result))
            fail("A product should contain operations,constructors or destructors. This product was empty !");
    }

    private boolean noOperations(Product result) {
        return result.freezeAndGetExecutables() == null || result.freezeAndGetExecutables().size() == 0;
    }

    private boolean noConstructors(Product result) {
        return result.freezeAndGetConstructors() == null || result.freezeAndGetConstructors().size() == 0;
    }

    private boolean noDestructors(Product result) {
        return result.freezeAndGetDestructors() == null || result.freezeAndGetDestructors().size() == 0;
    }

    public static Test suite() {
        return new TestSuite(DialogStartTest.class);
    }

    /**
     * Reads an xml document passed as a string using the XPP3LineNoReader.
     * The root element of the document is returned as a LineNoElement.
     *
     * E.g. readDocument("<form id='demo'></form>");
     *
     * @param document The XML document snippet to read
     */
    protected CompilerElement readDocument(String document) {
        Document doc = null;
        XPP3Reader reader = new XPP3CompilerReader();

        try {
            doc = reader.read(new StringReader(document));
        } catch (DocumentException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (XmlPullParserException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        if (doc == null) {
            return null;
        } else {
            return (CompilerElement) doc.getRootElement();
        }
    }
}
