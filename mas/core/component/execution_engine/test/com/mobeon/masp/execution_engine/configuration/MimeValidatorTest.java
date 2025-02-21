/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.configuration;

import junit.framework.*;

import com.mobeon.masp.execution_engine.configuration.MimeValidator;
import com.mobeon.masp.execution_engine.compiler.Constants;

public class MimeValidatorTest extends ValidatorCase {

    public MimeValidatorTest(String string) {
        super(string, new MimeValidator());
    }

    public static Test suite() {
        return new TestSuite(MimeValidatorTest.class);
    }

    public void testIsValid() throws Exception {
        String[] validMime = {"text/plain", Constants.MimeType.CCXML_MIMETYPE};
        String[] invalidMime = {" text/plain","text\\plain" };
        validateValidation(validMime,true);
        validateValidation(invalidMime,false);
    }
}