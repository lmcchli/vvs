package com.mobeon.common.soap.test;

import com.mobeon.common.soap.*;
import junit.framework.*;
import java.io.*;
import java.util.List;

public class TestSOAP extends TestCase {
    
    private static String BASE_DIR = "/vobs/ipms/ntf/src/com/mobeon/common/";
    private static String Namespace =
        "xmlns:mm7=\"http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-5-MM7-1-3\"";
    
    private SOAPHeader _testHeader;
    private SOAPBody _testBody;
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new TestSOAP("TestSOAP"));
        return suite;
    }
    
    public TestSOAP(String name) {
        super(name);
    }
    
    protected void runTest() {
        try {
            testSOAPTags();
            //System.out.println("\n*** Testing SOAPHeader ***");
            testSOAPHeader();
            //System.out.println("\n*** Testing SOAPBody ***");
            testSOAPBody();
            //System.out.println("\n*** Testing SOAPEnvelope ***");
            //testSOAPEnvelope();
            testParser();
        }
        catch(Exception ex) {
            System.out.println("Error in runTest: "+ getStackTrace(ex));
        }
    }
    
    private void testSOAPTags() {
        SOAPTag test = new SOAPTag("test");
        test.addAttribute("attr1");
        test.addAttribute("attr2");
        test.setValue("value");
        assertEquals("<test attr1 attr2>value</test>", test.toString());

        //Add a linked tag
        test.addTag(new SOAPTag("test2", "value2"));
        
        //Add closed tag
        SOAPTag test3 = new SOAPTag("test3");
        test3.addAttribute("attr3=\"value3\"");
        test.addTag(test3);
        
        assertEquals("<test attr1 attr2><test2>value2</test2><test3 attr3=\"value3\"/></test>",
            test.toString());
        
        //test the getTag methods
        SOAPTag toTest = test.getTag("test2");
        assertEquals("<test2>value2</test2>", toTest.toString());
        
        //test hasTag method
        assertTrue("test tag has no tag ???", test.hasTag());
        assertFalse("test3 tag has tag ???", test3.hasTag());
    }
    
    private void testSOAPHeader() {
        _testHeader = new SOAPHeader("mm7:TransactionID", Namespace, "00088900");
        //System.out.println(""+_testHeader);
        String toTest = "<env:Header><mm7:TransactionID xmlns:mm7=\"http://www.3gpp.org/ftp/Specs/archive/23_series/23.140/schema/REL-5-MM7-1-3\">"+"00088900</mm7:TransactionID></env:Header>";
        assertEquals(toTest, _testHeader.toString());
    }
    
    private void testSOAPBody() {
        _testBody = new SOAPBody("SubmitReq", Namespace);
        _testBody.addElement("MM7Version", new SOAPTag("MM7Version", "5.6.0"));
        SOAPTag tag = new SOAPTag("TimeStamp", new java.util.Date().toString());
        _testBody.addElement(tag.getName(), tag);
        
        SOAPTag sender = new SOAPTag("SenderIdentification", new SOAPTag("VASPID", "TNN"));
        sender.addTag(new SOAPTag("VASID", "News"));
        _testBody.addElement(sender.getName(), sender);
        
        SOAPTag to = new SOAPTag();
        to.setName("To");
        to.addTag(new SOAPTag("Number", "22244455"));
        to.addTag(new SOAPTag("RFC2822Address", "22244455@tst.com"));
        SOAPTag recipients = new SOAPTag("Recipients", to);
        
        SOAPTag cc = new SOAPTag("Cc");
        cc.addTag(new SOAPTag("Number", "22244455"));
        recipients.addTag(cc);
        
        _testBody.addElement(recipients.getName(), recipients);
        
        //Test the content tag which is empty and should be closed
        SOAPTag content = new SOAPTag("Content");
        content.addAttribute("href=\"cid:SaturnPics\"");
        _testBody.addElement(content.getName(), content);
        
        //System.out.println(""+_testBody);
        assertEquals("SubmitReq", _testBody.getSOAPMethod());
        
        tag = _testBody.getElement("MM7Version");
        assertEquals("5.6.0", tag.getValue());
    }
    
    private void testSOAPEnvelope() {
        SOAPEnvelope envelope = new SOAPEnvelope();
        envelope.setHeader(_testHeader);
        
        envelope.setBody(_testBody);        
        System.out.println(""+envelope);
    }
    
    private void testParser() {
        SOAPParser parser = new SOAPParser();
        
        String toTest = loadResponseFile(BASE_DIR + "soap/test/RES/Submit.RES");
        try {
            parser.parse(toTest);
        }
        catch(Exception ex) { System.out.println("Error in testParser(1) "+ex);}
        
        //toTest = loadResponseFile(BASE_DIR + "soap/test/RES/FAULT_SPEC.RES");
        toTest = loadResponseFile(BASE_DIR + "soap/test/RES/SMSNOW_FAULT.RES");
        try {
            parser.parse(toTest);
            SOAPEnvelope env = parser.getSOAPEnvelope();
            //System.out.println("\nPRINTING PARSED ENVELOPE");
            //System.out.println(env);
            String method = env.getBody().getSOAPMethod();
            assertEquals("RSErrorRsp", method);
        }
        catch(Exception ex) { System.out.println("Error in testParser(2) "+getStackTrace(ex));}
        
        
        //Test an error response file
        toTest = loadResponseFile(BASE_DIR + "soap/test/RES/Submit.ERROR_RES");
        try {
            parser.parse(toTest);
        }
        catch(Exception ex) { System.out.println("Error in testParser(3) "+getStackTrace(ex));}
        
    }
    
    private String loadResponseFile(String filename) {
        StringBuffer strBuf = new StringBuffer();
        try {
            FileInputStream fis = new FileInputStream(filename);
            byte[] buf = new byte[4 * 1024];  // 4K buffer
            int bytesRead = 0;
            while ((bytesRead = fis.read(buf)) != -1) {
                strBuf.append(new String(buf, 0, bytesRead));
            }
            fis.close();
        }
        catch(Exception ex){ System.out.println("Error in loadResponseFile "+ex); }
        return strBuf.toString();
    }
    

    //Move to some future base class
    public static String getStackTrace(Throwable t){
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        PrintWriter w=new PrintWriter(bytes,true);
        t.printStackTrace(w);
        String trace=bytes.toString();
        try{bytes.close();}catch(IOException e){}
        return trace;
    }
    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}