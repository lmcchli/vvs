/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.configuration;

import junit.framework.*;

import com.mobeon.masp.execution_engine.configuration.ExtensionValidator;

public class ExtensionValidatorTest extends ValidatorCase {
    public ExtensionValidatorTest(String string) {
        super(string, new ExtensionValidator());
    }

    public static Test suite() {
        return new TestSuite(ExtensionValidatorTest.class);
    }

    public void testIsValid() throws Exception {
        String[] validExts = {"test","123asd_-"};
        String[] invalidExts = {"test.test","\\sd"};
        validateValidation(validExts, true);
        validateValidation(invalidExts, false);
    }

}