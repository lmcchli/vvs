/*
 * Copyright (c) 2006 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import junit.framework.*;
import org.jmock.*;

import com.mobeon.masp.execution_engine.compiler.TagType;

public class TagTypeTest extends MockObjectTestCase {

    public static Test suite() {
        return new TestSuite(TagTypeTest.class);
    }

    public void testIsTypeOf() throws Exception {
        TagType root = new TagType();
        if(!root.isTypeOf())
            fail("Empty type must be type of empty type");
        root.refine("vxml");
        root.refine("form");
        if(!root.isTypeOf("vxml","form"))
            fail("vxml->form should be type of "+root);
        TagType type = root.clone();
        type.refine("input");
        if(root.isTypeOf("vxml","form","input"))
            fail("Clone should ensure separation between root and type, expected vxml->form, but got "+root);
        if(!type.isTypeOf("form","input"))
            fail("form->input should be type of "+type);
        if(!type.isLeafTypeOf("form"))
            fail("form should be trunk type of "+type);
    }
}