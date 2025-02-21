/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.ldap;

import javax.naming.spi.InitialContextFactory;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import java.util.Hashtable;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

import org.jmock.core.VerifyingTestCase;
import org.jmock.core.Verifiable;
import junit.framework.Assert;

/**
 * Documentation
 *
 * @author mande
 */
public class MockLdapCtxFactory implements InitialContextFactory, Verifiable {
    private static DirContext dirContext;
    private static List<NamingException> exception;
    private static Hashtable<?, ?> environment;
    private static MockLdapCtxFactory factory;
    private static int numCreatedContexts = 0;

    public MockLdapCtxFactory() {
        factory = this;
    }

    /**
     * Sets the DirContext to return from the factory. Can be used to retrieve a mocked DirContext when creating
     * a new InitialDirContext.
     * @param dirContext
     */
    public static void setDirContext(DirContext dirContext) {
        MockLdapCtxFactory.dirContext = dirContext;
    }

    /**
     * Retrieves the environment from the latest creation of an InitialDirContext
     * @return the environment from the latest creation of an InitialDirContext
     */
    public static Hashtable<?, ?> getEnvironment() {
        return environment;
    }

    /**
     * Makes the creating of a DirContext throw a NamingException
     * @param exception the NamingException to throw
     */
    public static void throwInitialException(NamingException... exception) {
        MockLdapCtxFactory.exception = new ArrayList<NamingException>(Arrays.asList(exception));
    }

    /**
     * Register this factory as a <code>Verifiable</code> object so that verification of thrown exceptions can be made.
     * This has to be done after this factory has been created by JNDI, and I'm not sure when this happens.
     * In ProfileManagerImplTest and SubscriberTest this seems to happen before the setUp method, but this is not the
     * case for BaseContextTest. *//* Todo: Check why
     * @param testCase the test case to register to
     */
    public static void registerToVerify(VerifyingTestCase testCase) {
        if (factory != null) {
            testCase.registerToVerify(factory);
        }
    }
    /**
     * Creates an Initial Context for beginning name resolution.
     * Special requirements of this context are supplied
     * using <code>environment</code>.
     * <p/>
     * The environment parameter is owned by the caller.
     * The implementation will not modify the object or keep a reference
     * to it, although it may keep a reference to a clone or copy.
     *
     * @param environment The possibly null environment
     *                    specifying information to be used in the creation
     *                    of the initial context.
     * @return A non-null initial context object that implements the Context
     *         interface.
     * @throws javax.naming.NamingException If cannot create an initial context.
     */
    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException {
        MockLdapCtxFactory.environment = environment;
        if (MockLdapCtxFactory.exception == null) {
            numCreatedContexts++;
            return dirContext;
        } else {
            NamingException exception = MockLdapCtxFactory.exception.remove(0);
            if (MockLdapCtxFactory.exception.size() == 0) {
                MockLdapCtxFactory.exception = null;
            }
            throw exception;
        }
    }

    public void verify() {
        try {
            Assert.assertNull("expected exceptions was not thrown", MockLdapCtxFactory.exception);
        } finally {
            // So other test cases will work
            MockLdapCtxFactory.exception = null;
        }
    }

    public static int getNumCreatedContexts() {
        return numCreatedContexts;
    }

    public static void setNumCreatedContexts(int numCreatedContexts) {
        MockLdapCtxFactory.numCreatedContexts = numCreatedContexts;
    }
}
