/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.configuration;

import org.jmock.MockObjectTestCase;
import com.mobeon.masp.execution_engine.Case;

public abstract class ValidatorCase extends Case {
    protected Validator validator = null;

    public ValidatorCase(String string, Validator validator) {
        super(string);
        this.validator = validator;
    }

    protected void validateValidation(String[] values, boolean expect) {
        for(String ext:values) {
            if(expect != validator.isValid(ext))
                die("Expected "+ext+" to validate as "+Boolean.toString(expect)+ " but it didn't");
        }
    }
}
