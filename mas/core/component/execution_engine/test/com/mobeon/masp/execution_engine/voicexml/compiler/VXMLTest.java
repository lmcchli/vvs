/*
 * Copyright (c) 2005 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;
import com.mobeon.masp.execution_engine.compiler.Compiler;
import com.mobeon.masp.execution_engine.compiler.*;
import junit.framework.Test;
import junit.framework.TestSuite;


public class VXMLTest extends NodeCompilerCase {
    public VXMLTest(String name) {
        super(name, VXML.class, "vxml");
    }

    /**
     * Check that compilation of a <vxml> where "version" is set to 2.0 works.
     * @throws Exception
     */
    public void testCompileVersion2() throws Exception {
        commonSetUp();
        String code = "<vxml version=\"2.0\" xmlns=\"http://www.w3.org/2001/vxml\"></vxml>";
        element = readDocument(code);
        Product result = compile();
        assertTrue( "2.0".
                equals(module.getDocumentAttribute(Constants.VoiceXML.VERSION)));
        validateResultAndParent(result,parent);
    }

    /**
     * Check that compilation of a <vxml> where "version" is set to 2.1 works.
     * @throws Exception
     */
    public void testCompileVersion2_1() throws Exception {
        commonSetUp();
        String code = "<vxml version=\"2.0\" xmlns=\"http://www.w3.org/2001/vxml\"></vxml>";
        element = readDocument(code);
        Product result = compile();
        assertTrue( "2.0".
                equals(module.getDocumentAttribute(Constants.VoiceXML.VERSION)));
        validateResultAndParent(result,parent);
    }

    /**
     * Check that compilation of a <vxml> where "version" is set to 1.0
     * does not work.
     */

    public void testVersion1_0(){
        commonSetUp();
        String code = "<vxml version=\"1.0\" xmlns=\"http://www.w3.org/2001/vxml\"></vxml>";
        element = readDocument(code);
        Product result = compile();
        assertTrue( "1.0".
                equals(module.getDocumentAttribute(Constants.VoiceXML.VERSION)));
        validateResultAndParent(result,parent);
        validateOperations(result,
                           Ops.sendEvent(Constants.Event.ERROR_SEMANTIC,
                                   "", result.getDebugInfo()));
        validateDestructors(result);
    }

    /**
     * Test that "application scope" is created as expected.
     */
    public void checkApplicationScopeCreation(){
        commonSetUp();
        String code = "<vxml version=\"2.0\" xmlns=\"http://www.w3.org/2001/vxml\"></vxml>";
        element = readDocument(code);
        Product result = compile();
        assertTrue( "2.0".
                equals(module.getDocumentAttribute(Constants.VoiceXML.VERSION)));
        assertNull(module.getDocumentAttribute(Constants.VoiceXML.APPLICATION));
        validateResultAndParent(result,parent);
        validateDestructors(result,Ops.closeScope());
    }

    /**
     * Test that "document scope" is created as expected.
     */
    public void checkDocumentScopeCreation(){
        commonSetUp();
        String code = "<vxml version=\"2.0\" xmlns=\"http://www.w3.org/2001/vxml\"" +
        " application=\"the_application.vxml\"></vxml>";
        element = readDocument(code);
        Product result = compile();
        assertTrue("the_application.vxml".
                equals(module.getDocumentAttribute(Constants.VoiceXML.APPLICATION)));
        validateOperations(result,
                           Ops.newScope(Constants.Scope.DOCUMENT_SCOPE),
                           Ops.sendDialogEvent(Constants.Event.DIALOG_EXIT, "End of VXML document"),
                           Ops.engineShutdown(true));
        validateDestructors(result,Ops.closeScope());
    }

    /**
     * Test that "xml:lang" is inserted into the Module if defined
     * in the <vxml> tag.
     */
    public void testDefinedXmlLang(){
        commonSetUp();
        String code = "<vxml version=\"2.0\" xmlns=\"http://www.w3.org/2001/vxml\"" +
        " xml:lang=\"en\"></vxml>";
        element = readDocument(code);
        Product result = compile();
        assertTrue("en".
                equals(module.getDocumentAttribute(Constants.VoiceXML.XMLLANG)));
    }

    /**
     * Test that default value of "xml:lang" is inserted into the Module
     * if undefined in the <vxml> tag.
     */
    public void testUndefinedXmlLang(){
        commonSetUp();
        String code = "<vxml version=\"2.0\" xmlns=\"http://www.w3.org/2001/vxml\"" +
        "></vxml>";
        element = readDocument(code);
        Product result = compile();
        assertTrue("en".
                equals(module.getDocumentAttribute(Constants.VoiceXML.XMLLANG)));
    }

    /**
     * Test that xml:base is inserted into the Module
     * if defined.
     */
    public void testXmlBaseDefined(){
        commonSetUp();
        String code = "<vxml version=\"2.0\" xmlns=\"http://www.w3.org/2001/vxml\"" +
        " xml:base=\"file:///kaka/olle\"></vxml>";
        element = readDocument(code);
        Product result = compile();
        assertTrue("file:///kaka/olle".
                equals(module.getDocumentAttribute(Constants.VoiceXML.XMLBASE)));
    }

    /**
     * Test that xml:base is null in the Module if not defined.
     */
    public void testXmlBaseUndefined(){
        commonSetUp();
        String code = "<vxml version=\"2.0\" xmlns=\"http://www.w3.org/2001/vxml\"" +
        "></vxml>";
        element = readDocument(code);
        Product result = compile();
        assertNull(module.getDocumentAttribute(Constants.VoiceXML.XMLBASE));
    }

    /**
     * Test that xmlns is inserted into the Module
     * if defined.
     */
    public void testXmlnsCorrect(){
        commonSetUp();
        String code = "<vxml version=\"2.0\" xmlns=\"http://www.w3.org/2001/vxml\"" +
        "></vxml>";
        element = readDocument(code);
        Product result = compile();
        assertTrue("http://www.w3.org/2001/vxml".
                equals(module.getDocumentAttribute(Constants.VoiceXML.XMLNS)));
    }

    /**
     * Test that if xmlns is wrong, there will be a compilation error.
     */
    public void testXmlnsIncorrect(){
        commonSetUp();
        String code = "<vxml version=\"2.0\" xmlns=\"http://mobeon.com/vxml\"" +
        "></vxml>";
        element = readDocument(code);
        Product result = compile();
        validateOperations(result,
                           Ops.sendEvent(Constants.Event.ERROR_SEMANTIC, "", result.getDebugInfo()));
        validateDestructors(result);
    }

    /**
     * Test that if xmlns is undefined, there will be a compilation error.
     */
    public void testXmlnsUndefined(){
        commonSetUp();
        String code = "<vxml version=\"2.0\" " +
        "></vxml>";
        element = readDocument(code);
        Product result = compile();
        validateOperations(result,
                           Ops.sendEvent(Constants.Event.ERROR_SEMANTIC, "", result.getDebugInfo()));
        validateDestructors(result);
    }

    /**
     * Test that xmlns:xsi is inserted into the Module
     * if defined.
     */
    public void testXmlnsXsiCorrect(){
        commonSetUp();
        String code = "<vxml version=\"2.0\" xmlns=\"http://www.w3.org/2001/vxml\"" +
        " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"></vxml>";
        element = readDocument(code);
        Product result = compile();
        assertTrue("http://www.w3.org/2001/XMLSchema-instance".
                equals(module.getDocumentAttribute(Constants.VoiceXML.XMLNS_XSI)));
    }

    /**
     * Test that there will be a compilation error if xmlns:xsi
     * has an unexpected value.
     */
    public void testXmlnsXsiIncorrect(){
        commonSetUp();
        String code = "<vxml version=\"2.0\" xmlns=\"http://www.w3.org/2001/vxml\"" +
        " xmlns:xsi=\"http://mobeon.com/2001/XMLSchema-instance\"></vxml>";
        element = readDocument(code);
        Product result = compile();
        validateOperations(result,
                           Ops.sendEvent(Constants.Event.ERROR_SEMANTIC, "", result.getDebugInfo()));
        validateDestructors(result);
    }

    /**
     * Test that if xmlns:xsi is undefined, everything works fine.
     */
    public void testXmlnsXsiUndefined(){
        commonSetUp();
        String code = "<vxml version=\"2.0\" xmlns=\"http://www.w3.org/2001/vxml\"" +
        " ></vxml>";
        element = readDocument(code);
        Product result = compile();
        assertNull(module.getDocumentAttribute(Constants.VoiceXML.XMLNS_XSI));
    }

    /**
     * Test that xsi:schemaLocation is inserted into the Module
     * if defined.
     */
    public void testSchemaLocationDefined(){
        commonSetUp();
        String code = "<vxml version=\"2.0\" xmlns=\"http://www.w3.org/2001/vxml\"" +
        " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
        " xsi:schemaLocation=\"http://www.w3.org/TR/voicexml21/vxml.xsd\"></vxml>";
        element = readDocument(code);
        Product result = compile();
        assertTrue("http://www.w3.org/TR/voicexml21/vxml.xsd".
                equals(module.getDocumentAttribute(Constants.VoiceXML.XSI_SCHEMALOCATION)));
    }

    /**
     * Test that compilation works ok if xsi:schemaLocation is undefined.
     */
    public void testSchemaLocationUndefined(){
        commonSetUp();
        String code = "<vxml version=\"2.0\" xmlns=\"http://www.w3.org/2001/vxml\"" +
        " ></vxml>";
        element = readDocument(code);
        Product result = compile();
        assertNull(module.getDocumentAttribute(Constants.VoiceXML.XSI_SCHEMALOCATION));
    }


    private void commonSetUp() {
        setCompilerPasses(Compiler.VXML_PASSES);
    }

    public static Test suite() {
        return new TestSuite(VXMLTest.class);
    }
}