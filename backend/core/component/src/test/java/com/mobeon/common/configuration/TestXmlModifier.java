//
package com.mobeon.common.configuration;
import junit.framework.TestCase;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.File;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.PrintWriter;

public final class TestXmlModifier extends TestCase {

    private static final String emptyDoc =
    "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<empty/>\n";

    public TestXmlModifier() {
    }

    public void testEmptyTransform() throws Exception {
        String origXml = emptyDoc;
        XmlModifier modifier = new XmlModifier();
        StringWriter result = new StringWriter();
        modifier.transform(
            new StreamSource(new StringReader(origXml)),
            new StreamResult(result));
        assertEquals(origXml, result.toString());
    }

    public void testInvalidXPath() throws Exception {
        String origXml = emptyDoc;
        XmlModifier modifier = new XmlModifier();
        modifier.setattr("/$$[]", "someKey=Kalle Kula");
        StringWriter result = new StringWriter();
        try {
            modifier.transform(
                new StreamSource(new StringReader(origXml)),
                new StreamResult(result));
            fail("We expect a TransformerException");
        } catch (TransformerException e) {}
    }

    public void testInvalidXml() throws Exception {
        String origXml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
            "<top></notop>\n";
        XmlModifier modifier = new XmlModifier();
        StringWriter result = new StringWriter();
        try {
            modifier.transform(
                new StreamSource(new StringReader(origXml)),
                new StreamResult(result));
            fail("We expect a TransformerException");
        } catch (TransformerException e) {}
    }

    public void testSetAttrBasic() throws Exception {
        String origXml = emptyDoc;
        String expectedXml = 
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
            "<empty someKey=\"Kalle Kula\"/>\n";
        XmlModifier modifier = new XmlModifier();
        modifier.setattr("//empty", "someKey=Kalle Kula");
        StringWriter result = new StringWriter();
        modifier.transform(
            new StreamSource(new StringReader(origXml)),
            new StreamResult(result));
        assertEquals(expectedXml, result.toString());
    }

    public void testSetAttrNoMatchingNode() throws Exception {
        String origXml = emptyDoc;
        XmlModifier modifier = new XmlModifier();
        modifier.setattr("//Empty", "someKey=Kalle Kula");
        StringWriter result = new StringWriter();
        modifier.transform(
            new StreamSource(new StringReader(origXml)),
            new StreamResult(result));
        assertEquals(origXml, result.toString());
    }

    public void testSetAttrMultiple() throws Exception {
        String origXml = emptyDoc;
        String expectedXml = 
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
            "<empty someKey=\"Kalle Kula\" a=\"b\" b=\"c\" c=\"d\"/>\n";
        XmlModifier modifier = new XmlModifier();
        modifier.setattr("//empty", 
                         "someKey=Kalle Kula", "a=b", "b=c", "c=d");
        StringWriter result = new StringWriter();
        modifier.transform(
            new StreamSource(new StringReader(origXml)),
            new StreamResult(result));
        assertEquals(expectedXml, result.toString());
    }

    public void testSetEmptyAttr() throws Exception {
        String origXml = emptyDoc;
        String expectedXml = 
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
            "<empty a=\"\" b=\"\"/>\n";
        XmlModifier modifier = new XmlModifier();
        modifier.setattr("//empty", "a", "b=");
        StringWriter result = new StringWriter();
        modifier.transform(
            new StreamSource(new StringReader(origXml)),
            new StreamResult(result));
        assertEquals(expectedXml, result.toString());
    }

    public void testSetAttrManyNodes() throws Exception {
        String origXml = 
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
            "<top>\n"+
            "    <one/>\n"+
            "    <two/>\n"+
            "</top>\n";
        String expectedXml = 
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
            "<top all=\"yes\">\n"+
            "    <one a=\"b\" b=\"c\"/>\n"+
            "    <two a=\"b\" b=\"c\"/>\n"+
            "</top>\n";
        XmlModifier modifier = new XmlModifier();
        modifier.setattr("*", "all=yes");
        // The "*" is overridden. This is expected since the
        // attributes are set multiple times for "/top/*" nodes.
        modifier.setattr("/top/*", "a=b", "b=c");
        StringWriter result = new StringWriter();
        modifier.transform(
            new StreamSource(new StringReader(origXml)),
            new StreamResult(result));
        assertEquals(expectedXml, result.toString());
    }

    public void testSetAttrMultipleSteps() throws Exception {
        String origXml = 
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
            "<top>\n"+
            "    <one/>\n"+
            "    <two/>\n"+
            "</top>\n";
        String expectedXml = 
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n"+
            "<top all=\"yes\">\n"+
            "    <one all=\"yes\" a=\"b\" b=\"c\"/>\n"+
            "    <two all=\"yes\" a=\"b\" b=\"c\"/>\n"+
            "</top>\n";

        // We don't want the "*" to be overridden so make a two-step
        // transform.

        XmlModifier modifier = new XmlModifier();
        modifier.setattr("*", "all=yes");
        StringWriter result = new StringWriter();
        modifier.transform(
            new StreamSource(new StringReader(origXml)),
            new StreamResult(result));

        origXml = result.toString();
        result = new StringWriter();
        modifier.setattr("/top/*", "a=b", "b=c");
        modifier.transform(
            new StreamSource(new StringReader(origXml)),
            new StreamResult(result));

        //System.out.println("[" + result + "]");
        assertEquals(expectedXml, result.toString());
    }

    public void testFileOperations() throws Exception {

        XmlModifier modifier = new XmlModifier();
        try {
            modifier.transform("/tmp/NonExistingFileWeHope.XML");
            fail("Expected IOException");
        } catch (IOException e) {}

        // Create a temoraty file
        File tmpfile = File.createTempFile("TestXmlModifier", ".xml");
        String tmppath = tmpfile.getPath();
        PrintWriter writer = new PrintWriter(tmpfile);
        writer.print(emptyDoc);
        writer.close();

        // Verify that a backup file was created
        modifier = new XmlModifier();
        modifier.transform(tmppath);
        File bakfile = new File(tmppath + ".save");
        assertTrue(bakfile.isFile());
        assertTrue(bakfile.canRead());

        // Verify repeated operation
        modifier.transform(tmppath);

        // Clean-up
        tmpfile.delete();
        bakfile.delete();
        
    }
    
    public void testLogmanagerXmlModify()  throws Exception {
        String origXml = 
            "<?xml version=\"1.0\" encoding=\"UTF-8}\"?>\n\r" +
            "<!DOCTYPE log4j:configuration SYSTEM \"log4j.dtd\">\n\r" +
            "<log4j:configuration xmlns:log4j=\"http://jakarta.apache.org/log4j/\">\n\r" +
            "<appender name=\"STDOUT\" class=\"org.apache.log4j.ConsoleAppender\">\n\r" +
            "<layout class=\"org.apache.log4j.PatternLayout\">\n\r" +
            "<param name=\"ConversionPattern\\n\r" +
            "value=\"%d{ISO8601} %c{1} %t %5p [SID:%X{session}] - %m%n\"/>\n\r" +
            "</layout>\n\r" +
            "</appender>\n\r" + 
            "<appender name=\"PLUGIN\" class=\"com.webspherious.log4jmonitor.log4j.IntelliJAppender\">\n\r" +
            "</appender>\n\r" +
            "<root>\n\r" +
            "<priority value =\"debug\" />\n\r" +
            "<appender-ref ref=\"STDOUT\" />\n\r" +
            "</root>\n\r" +
            "</log4j:configuration>\n\r"
            ;
        String expectedXml = 
            "<?xml version=\"1.0\" encoding=\"UTF-8}\"?>\n\r" +
            "<!DOCTYPE log4j:configuration SYSTEM \"log4j.dtd\">\n\r" +
            "<log4j:configuration xmlns:log4j=\"http://jakarta.apache.org/log4j/\">\n\r" +
            "<appender name=\"STDOUT\" class=\"org.apache.log4j.ConsoleAppender\">\n\r" +
            "<layout class=\"org.apache.log4j.PatternLayout\">\n\r" +
            "<param name=\"ConversionPattern\\n\r" +
            "value=\"%d{ISO8601} %c{1} %t %5p [SID:%X{session}] - %m%n\"/>\n\r" +
            "</layout>\n\r" +
            "</appender>\n\r" + 
            "<appender name=\"PLUGIN\" class=\"com.webspherious.log4jmonitor.log4j.IntelliJAppender\">\n\r" +
            "</appender>\n\r" +
            "<root>\n\r" +
            "<priority value =\"warn\" />\n\r" +
            "<appender-ref ref=\"STDOUT\" />\n\r" +
            "</root>\n\r" +
            "</log4j:configuration>\n\r"
            ;
        XmlModifier modifier = new XmlModifier();
        modifier.setattr("//priority", "value=warn");
        StringWriter result = new StringWriter();
        modifier.transform(
            new StreamSource(new StringReader(origXml)),
            new StreamResult(result));
        assertEquals(expectedXml, result.toString());
    }

    
    
}
