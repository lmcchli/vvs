/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.common.configuration;

import com.mobeon.common.logging.ILoggerFactory;
import junit.framework.TestCase;

public class TestSchemaValidator extends TestCase {
    static {
        ILoggerFactory.configureAndWatch("log4j2conf.xml");
    }

    private SchemaValidator schemaValidator;

    public TestSchemaValidator() {
        super();
    }

    public void setUp() throws Exception {
        super.setUp();
        schemaValidator = new SchemaValidator();
    }

    public void testValidate() {
        String configurationFile = "src/test/java/com/mobeon/common/configuration/cfg/mas.cfg";
        String configurationFile2 = "src/test/java/com/mobeon/common/configuration/cfg/mas2.cfg";
        String configurationFile3 = "src/test/java/com/mobeon/common/configuration/cfg/failed.cfg";
        assertTrue(schemaValidator.validate(configurationFile));
        assertTrue(schemaValidator.validate(configurationFile2));
        assertTrue(schemaValidator.validate(configurationFile3));
    }
}