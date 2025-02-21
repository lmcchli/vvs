/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.compiler.products.BinaryExecutable;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.masp.execution_engine.xml.CompilerElementFactory;
import org.dom4j.QName;

public class IfTest extends NodeCompilerCase {

    private CompilerElement newElement;
    public IfTest(){
        super("If", If.class, "if");
    }

    public void setUp() throws Exception {
        super.setUp();
        setCompilerPasses(Compiler.VXML_PASSES);
    }

    /**
     * Test compilation of:
     * if without else/elseif
     * @throws Exception
     */
    public void testCompileSimpleIf() throws Exception {
        setIf("i=3");

        addAssign("flavor_code", "'a'");

        Product result = compile();
        assertTrue(result instanceof BinaryExecutable);
        BinaryExecutable binaryExecutable = (BinaryExecutable) result;
//        List<Executable> trueOperationList = binaryExecutable.getTrueOperationList();
  //      assertTrue(trueOperationList.size() > 0);
        assertTrue(checkFalseDepth(binaryExecutable, 0));
    }

    /**
     * Checks that there are expectedDepth BinaryProducts on the false-side of
     * binaryProduct.
     * @param binaryExecutable
     * @param expectedDepth
     * @return
     */
    private boolean checkFalseDepth(BinaryExecutable binaryExecutable, int expectedDepth) {
        //List<Executable> falseList = binaryExecutable.getFalseOperationList();
        BinaryExecutable currentBinaryExecutable;

        /*for(int i=0;i<expectedDepth;i++){
            Executable currentExecutable = falseList.get(0);
            if(! (currentExecutable instanceof BinaryExecutable)){
                return false;
            }
            currentBinaryExecutable = (BinaryExecutable) currentExecutable;
            falseList = currentBinaryExecutable.getFalseOperationList();
        } */
        // If there are further Products, there can not be a BinaryProduct on
        // the false-side.
        /*if(falseList.size() > 0){
            Executable currentExecutable = falseList.get(0);
            if(currentExecutable instanceof BinaryExecutable){
                return false;
            }
        } */
        return true;

    }

    private void addAssign(String variableName, String value) {
        createNewElement("assign");
        addAttribute("name", variableName);
        addAttribute("expr", value);
        element.add(newElement);
    }

    private void addAssignToNewElement(String variableName, String value) {
        CompilerElement el = new CompilerElement(new CompilerElementFactory(),new QName("assign"), 1, 1);
        el.addAttribute("name", variableName);
        el.addAttribute("expr", value);
        newElement.add(el);
    }

    private void addAssignToElement(String variableName, String value) {
        CompilerElement el = new CompilerElement(new CompilerElementFactory(),new QName("assign"), 1, 1);
        el.addAttribute("name", variableName);
        el.addAttribute("expr", value);
        element.add(el);
    }

    /**
     * Test compilation of:
     * test if-else
     * @throws Exception
     */
    public void testCompileIfElse() throws Exception {
        setIf("i=3");

        addAssign("flavor_code", "'a'");

        createNewElement("else");
        element.add(newElement);

        addAssign("flavor_code2", "'b'");


        Product result = compile();
        assertTrue(result instanceof BinaryExecutable);
        BinaryExecutable binaryExecutable = (BinaryExecutable) result;
//        List<Executable> trueOperationList = binaryExecutable.getTrueOperationList();
//        assertTrue(trueOperationList.size() > 0);
        assertTrue(checkFalseDepth(binaryExecutable, 1));
    }

    /**
     * Test compilation of:
     * if-elseif
     * @throws Exception
     */
    public void testCompileIfElseIf() throws Exception {
        setIf("i=3");

        addAssign("flavor_code", "'a'");

        createNewElement("elseif");
        newElement.addAttribute("cond", "b>4");
        element.add(newElement);

        addAssign("flavor_code2", "'b'");

        Product result = compile();
        assertTrue(result instanceof BinaryExecutable);
        BinaryExecutable binaryExecutable = (BinaryExecutable) result;
//        List<Executable> trueOperationList = binaryExecutable.getTrueOperationList();
//        assertTrue(trueOperationList.size() > 0);
        assertTrue(checkFalseDepth(binaryExecutable, 1));
    }

    /**
     * Test compilation of:
     * if-elseif-else
     * @throws Exception
     */
    public void testCompileIfElseifElse() throws Exception {

        setIf("i=3");

        addAssign("flavor_code", "'a'");

        createNewElement("elseif");
        newElement.addAttribute("cond", "b>4");
        element.add(newElement);
        addAssign("flavor_code2", "'b'");

        createNewElement("else");
        element.add(newElement);
        addAssign("flavor_code3", "'c'");

        Product result = compile();
        assertTrue(result instanceof BinaryExecutable);
        BinaryExecutable binaryExecutable = (BinaryExecutable) result;
//        List<Executable> trueOperationList = binaryExecutable.getTrueOperationList();
//        assertTrue(trueOperationList.size() > 0);
        assertTrue(checkFalseDepth(binaryExecutable, 2));
    }

    /**
     * Test compilation of:
     * if-elseif-elseif-else
     * @throws Exception
     */
    public void testCompileIfElseifElseifElse() throws Exception {

        setIf("i=3");

        addAssign("flavor_code", "'a'");

        createNewElement("elseif");
        newElement.addAttribute("cond", "b>4");
        element.add(newElement);
        addAssign("flavor_code2", "'b'");

        createNewElement("elseif");
        newElement.addAttribute("cond", "e>8");
        element.add(newElement);
        addAssign("flavor_code3", "'c'");

        createNewElement("else");
        element.add(newElement);
        addAssign("flavor_code4", "'d'");

        Product result = compile();
        assertTrue(result instanceof BinaryExecutable);
        BinaryExecutable binaryExecutable = (BinaryExecutable) result;
//        List<Executable> trueOperationList = binaryExecutable.getTrueOperationList();
//        assertTrue(trueOperationList.size() > 0);
        assertTrue(checkFalseDepth(binaryExecutable, 3));
    }

    /**
     * Test compilation of:
     * if with no contents
     * @throws Exception
     */
    public void testCompileIfWithNoContents() throws Exception {
        setIf("i=3");
        Product result = compile();
        assertTrue(result instanceof BinaryExecutable);
        BinaryExecutable binaryExecutable = (BinaryExecutable) result;
//        List<Executable> trueOperationList = binaryExecutable.getTrueOperationList();
//        assertTrue(trueOperationList.size() == 0);
        assertTrue(checkFalseDepth(binaryExecutable, 0));
    }

    /**
     * Test compilation of:
     * test if-else
     * @throws Exception
     */
    public void testCompileIfElseWithNoContents() throws Exception {
        setIf("i=3");

        addAssign("flavor_code", "'a'");

        createNewElement("else");
        element.add(newElement);

        Product result = compile();
        assertTrue(result instanceof BinaryExecutable);
        BinaryExecutable binaryExecutable = (BinaryExecutable) result;
//        List<Executable> trueOperationList = binaryExecutable.getTrueOperationList();
//        assertTrue(trueOperationList.size() > 0);
        assertTrue(checkFalseDepth(binaryExecutable, 1));
    }

    /**
     * Test compilation of:
     * if-else-elseif where each "executable content" consists
     * of several elements.
     * @throws Exception
     */
    public void testCompileIfElseIfElseComplexContents() throws Exception {
        setIf("i=3");

        addAssignToElement("flavor_code", "'a'");
        addAssignToElement("flavor_code2", "'b'");

        createNewElement("elseif");
        newElement.addAttribute("cond", "e>8");
        addAssignToNewElement("flavor_code3", "'c'");
        addAssignToNewElement("flavor_code4", "'d'");
        element.add(newElement);

        createNewElement("else");
        addAssignToNewElement("flavor_code5", "'e'");
        addAssignToNewElement("flavor_code6", "'f'");
        element.add(newElement);

        Product result = compile();
        assertTrue(result instanceof BinaryExecutable);
        BinaryExecutable binaryExecutable = (BinaryExecutable) result;
//        List<Executable> trueOperationList = binaryExecutable.getTrueOperationList();
//        assertTrue(trueOperationList.size() > 0);
        assertTrue(checkFalseDepth(binaryExecutable, 2));
    }

    private void addAttribute(String attributeName, String attributeValue) {

        newElement.addAttribute(attributeName, attributeValue);
    }


    /**
     * Help function to set the if-statement.
     * @param theCondition
     */

    private void setIf(String theCondition){
        element.addAttribute("cond", theCondition);
    }

    private void createNewElement(String elementName) {
        newElement = new CompilerElement(new CompilerElementFactory(),new QName(elementName), 1, 1);
    }

    // TODO:
    // test if with wrong kind of contents
    // test else with wrong kind of contents
    // test elseif with wrong kind of contents


}