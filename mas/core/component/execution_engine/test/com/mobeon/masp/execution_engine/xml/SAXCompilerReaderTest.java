/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.xml;

import junit.framework.*;
import org.dom4j.Document;

import com.mobeon.masp.execution_engine.xml.SAXCompilerReader;
import com.mobeon.masp.execution_engine.Case;

import java.io.StringReader;

/**
 * Test-class for testing SAXLineNoReader and it's supporting classes.
 * <p>
 * The supporting classes also covered by this case is:
 * <ul>
 * <li>{@link CompilerElement}</li>
 * <li>{@link CompilerElementFactory}</li>
 * <li>{@link SAXCompilerHandler}</li>
 * </ul>
 *
 * @author Mikael Andersson
 */
public class SAXCompilerReaderTest extends Case {

    public static Test suite() {
        return new TestSuite(SAXCompilerReaderTest.class);
    }

    public SAXCompilerReaderTest(String name) {
        super(name);
    }

    /**
     * Tests that a SAXLineNoReader fills the created LineNoElements with
     * sensible line-number information. In essence, it validates the DOM4J
     * integration.
     *
     * @throws Exception
     */
    public void testLineNoReader() throws Exception {
        SAXCompilerReader r = new SAXCompilerReader();
        Document d = r.read(new StringReader("<test>\n\n\n\n <inner/>\n</test>"));
        CompilerElement test = (CompilerElement) d.getRootElement();
        validateLineNumber(test, "test", 1, 7);
        CompilerElement inner = (CompilerElement) test.element("inner");
        validateLineNumber(inner, "inner", 5, 10);
    }

    private void validateLineNumber(CompilerElement inner, String element, int expectedLine, int expectedColumn) {
        if (inner.getLine() != expectedLine)
            die("<" + element + "> element should start on line " + expectedLine + ", we claimed it started at line " + inner.getLine());
        if (inner.getColumn() != expectedColumn)
            die("<" + element + "> element should end on column " + expectedColumn + ", we claimed it ended at column " + inner.getColumn());
    }
}