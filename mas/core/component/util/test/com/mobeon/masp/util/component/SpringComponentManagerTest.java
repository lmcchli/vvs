/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.util.component;

import java.util.Date;
import java.util.Vector;

import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.jmock.MockObjectTestCase;
import com.mobeon.masp.util.Tools;

public class SpringComponentManagerTest extends MockObjectTestCase {
    SpringComponentManager springComponentManager;

    public SpringComponentManagerTest(String name) {
        super(name);
    }

    public void setup() {
        SpringComponentManager.initialApplicationContext(new ClassPathXmlApplicationContext("test:/lib/TestComponentConfig.xml"));
        springComponentManager = new SpringComponentManager();
    }

    public void testCreate() throws Exception {
        setup();
        Date date = null;
        try {
            date = (Date) springComponentManager.create("TestBean");
        } catch (Exception e) {
            die("Create failed with an exception: "+e);
        }
        if(date == null)
            die("TestBean reference is still null after create !");

        try {
            date = (Date) springComponentManager.create("NoDemoBean");
        } catch (Exception e) {
            assertTrue(e instanceof NoSuchBeanDefinitionException);
        }
        tearDown();
    }

    public void testCreateWithType() throws Exception {
        setup();
        Date date = null;
        Vector v = null;
        try {
            date = (Date) springComponentManager.create("TestBean", Date.class);
        } catch (Exception e) {
            die("Create failed with an exception: "+e);
        }
        if(date == null)
            die("TestBean reference is still null after create !");
        try {
            v = (Vector) springComponentManager.create("TestBean", Vector.class);
        } catch (Exception e) {
            if(! (e instanceof BeanNotOfRequiredTypeException)) {
                die("Expected BeanNotOfRequiredTypeException, but got "+e);
            }
        }
        tearDown();
    }

    public static void die(String reason) {
        if(Tools.isTrueProperty(System.getProperty("com.mobeon.junit.unimplemented.ignore"))) {
            String reasonLc = reason.toLowerCase();
            if( ! ( reasonLc.contains("not implemented") ||
                    reasonLc.contains("not fully implemented")))
                fail(reason);
        } else {
            fail(reason);
        }
    }

}