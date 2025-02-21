/*
 * Copyright (c) 2006 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.profilemanager.pool;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

import javax.naming.Name;
import javax.naming.directory.*;

/**
 * Testcase for the PooledDirContext class
 *
 * @author ermmaha
 */
public class PooledDirContextTest extends MockObjectTestCase {

    protected Mock jmockDirContext;
    protected PooledDirContext pooledDirContext;

    protected Mock jmockName;
    protected Mock jmockAttributes;
    protected ModificationItem[] modificationItems;

    public PooledDirContextTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        jmockDirContext = mock(DirContext.class);
        pooledDirContext = new PooledDirContext((DirContext) jmockDirContext.proxy(), "ldap://ldap.url:389");

        jmockName = mock(Name.class);
        jmockAttributes = mock(Attributes.class);

        Mock jmockAttribute = mock(Attribute.class);
        modificationItems = new ModificationItem[]{new ModificationItem(1, (Attribute) jmockAttribute.proxy())};
    }

    /**
     * Test the release method
     *
     * @throws Exception if testcase fails.
     */
    public void testRelease() throws Exception {
        jmockDirContext.expects(once()).method("close");
        pooledDirContext.release();
    }

    /**
     * Test the getAttributes methods
     *
     * @throws Exception if testcase fails.
     */
    public void testGetAttributes() throws Exception {
        String name = "name";
        jmockDirContext.expects(once()).method("getAttributes").with(eq(name));
        pooledDirContext.getAttributes(name);

        String[] values = new String[]{"value"};
        jmockDirContext.expects(once()).method("getAttributes").with(eq(name), eq(values));
        pooledDirContext.getAttributes(name, values);

        jmockDirContext.expects(once()).method("getAttributes").with(eq(jmockName.proxy()));
        pooledDirContext.getAttributes((Name) jmockName.proxy());

        jmockDirContext.expects(once()).method("getAttributes").with(eq(jmockName.proxy()), eq(values));
        pooledDirContext.getAttributes((Name) jmockName.proxy(), values);
    }

    /**
     * Test the modifyAttributes methods
     *
     * @throws Exception if testcase fails.
     */
    public void testModifyAttributes() throws Exception {
        String name = "name";
        int op = 2;

        jmockDirContext.expects(once()).method("modifyAttributes").with(eq(name), eq(op), eq(jmockAttributes.proxy()));
        pooledDirContext.modifyAttributes(name, op, (Attributes) jmockAttributes.proxy());

        jmockDirContext.expects(once()).method("modifyAttributes").with(eq(jmockName.proxy()), eq(op), eq(jmockAttributes.proxy()));
        pooledDirContext.modifyAttributes((Name) jmockName.proxy(), op, (Attributes) jmockAttributes.proxy());

        jmockDirContext.expects(once()).method("modifyAttributes").with(eq(name), eq(modificationItems));
        pooledDirContext.modifyAttributes(name, modificationItems);

        jmockDirContext.expects(once()).method("modifyAttributes").with(eq(jmockName.proxy()), eq(modificationItems));
        pooledDirContext.modifyAttributes((Name) jmockName.proxy(), modificationItems);
    }

    /**
     * Test the bind methods
     *
     * @throws Exception if testcase fails.
     */
    public void testBind() throws Exception {
        String name = "name";
        Object obj = new Object();

        jmockDirContext.expects(once()).method("bind").with(eq(name), eq(obj), eq(jmockAttributes.proxy()));
        pooledDirContext.bind(name, obj, (Attributes) jmockAttributes.proxy());

        jmockDirContext.expects(once()).method("bind").with(eq(jmockName.proxy()), eq(obj), eq(jmockAttributes.proxy()));
        pooledDirContext.bind((Name) jmockName.proxy(), obj, (Attributes) jmockAttributes.proxy());

        jmockDirContext.expects(once()).method("bind").with(eq(name), eq(obj));
        pooledDirContext.bind(name, obj);

        jmockDirContext.expects(once()).method("bind").with(eq(jmockName.proxy()), eq(obj));
        pooledDirContext.bind((Name) jmockName.proxy(), obj);
    }

    /**
     * Test the rebind methods
     *
     * @throws Exception if testcase fails.
     */
    public void testRebind() throws Exception {
        String name = "name";
        Object obj = new Object();

        jmockDirContext.expects(once()).method("rebind").with(eq(name), eq(obj), eq(jmockAttributes.proxy()));
        pooledDirContext.rebind(name, obj, (Attributes) jmockAttributes.proxy());

        jmockDirContext.expects(once()).method("rebind").with(eq(jmockName.proxy()), eq(obj), eq(jmockAttributes.proxy()));
        pooledDirContext.rebind((Name) jmockName.proxy(), obj, (Attributes) jmockAttributes.proxy());

        jmockDirContext.expects(once()).method("rebind").with(eq(name), eq(obj));
        pooledDirContext.rebind(name, obj);

        jmockDirContext.expects(once()).method("rebind").with(eq(jmockName.proxy()), eq(obj));
        pooledDirContext.rebind((Name) jmockName.proxy(), obj);
    }

    /**
     * Test the createSubcontext methods
     *
     * @throws Exception if testcase fails.
     */
    public void testCreateSubcontext() throws Exception {
        String name = "name";
        jmockDirContext.expects(once()).method("createSubcontext").with(eq(name), eq(jmockAttributes.proxy()));
        pooledDirContext.createSubcontext(name, (Attributes) jmockAttributes.proxy());

        jmockDirContext.expects(once()).method("createSubcontext").with(eq(jmockName.proxy()), eq(jmockAttributes.proxy()));
        pooledDirContext.createSubcontext((Name) jmockName.proxy(), (Attributes) jmockAttributes.proxy());

        jmockDirContext.expects(once()).method("createSubcontext").with(eq(name));
        pooledDirContext.createSubcontext(name);

        jmockDirContext.expects(once()).method("createSubcontext").with(eq(jmockName.proxy()));
        pooledDirContext.createSubcontext((Name) jmockName.proxy());
    }

    /**
     * Test the getSchema* methods
     *
     * @throws Exception if testcase fails.
     */
    public void testGetSchema() throws Exception {
        String name = "name";
        jmockDirContext.expects(once()).method("getSchema").with(eq(name));
        pooledDirContext.getSchema(name);

        jmockDirContext.expects(once()).method("getSchema").with(eq(jmockName.proxy()));
        pooledDirContext.getSchema((Name) jmockName.proxy());

        jmockDirContext.expects(once()).method("getSchemaClassDefinition").with(eq(name));
        pooledDirContext.getSchemaClassDefinition(name);

        jmockDirContext.expects(once()).method("getSchemaClassDefinition").with(eq(jmockName.proxy()));
        pooledDirContext.getSchemaClassDefinition((Name) jmockName.proxy());
    }

    /**
     * Test the search methods
     *
     * @throws Exception if testcase fails.
     */
    public void testSearch() throws Exception {
        String name = "name";
        jmockDirContext.expects(once()).method("search").with(eq(name), eq(jmockAttributes.proxy()));
        pooledDirContext.search(name, (Attributes) jmockAttributes.proxy());

        jmockDirContext.expects(once()).method("search").with(eq(jmockName.proxy()), eq(jmockAttributes.proxy()));
        pooledDirContext.search((Name) jmockName.proxy(), (Attributes) jmockAttributes.proxy());

        String[] attributesToReturn = new String[]{"values"};
        jmockDirContext.expects(once()).method("search").with(eq(name), eq(jmockAttributes.proxy()), eq(attributesToReturn));
        pooledDirContext.search(name, (Attributes) jmockAttributes.proxy(), attributesToReturn);

        jmockDirContext.expects(once()).method("search").with(eq(jmockName.proxy()), eq(jmockAttributes.proxy()), eq(attributesToReturn));
        pooledDirContext.search((Name) jmockName.proxy(), (Attributes) jmockAttributes.proxy(), attributesToReturn);

        String filter = "filter";
        SearchControls searchControls = new SearchControls();
        jmockDirContext.expects(once()).method("search").with(eq(name), eq(filter), eq(searchControls));
        pooledDirContext.search(name, filter, searchControls);

        jmockDirContext.expects(once()).method("search").with(eq(jmockName.proxy()), eq(filter), eq(searchControls));
        pooledDirContext.search((Name) jmockName.proxy(), filter, searchControls);

        String filterExpr = "filterExpr";
        Object[] filterArgs = new Object[]{"args"};
        jmockDirContext.expects(once()).method("search").with(eq(name), eq(filterExpr), eq(filterArgs), eq(searchControls));
        pooledDirContext.search(name, filterExpr, filterArgs, searchControls);

        jmockDirContext.expects(once()).method("search").with(eq(jmockName.proxy()), eq(filterExpr), eq(filterArgs), eq(searchControls));
        pooledDirContext.search((Name) jmockName.proxy(), filterExpr, filterArgs, searchControls);
    }

    /**
     * Test the unbind methods
     *
     * @throws Exception if testcase fails.
     */
    public void testUnbind() throws Exception {
        String name = "name";

        jmockDirContext.expects(once()).method("unbind").with(eq(name));
        pooledDirContext.unbind(name);

        jmockDirContext.expects(once()).method("unbind").with(eq(jmockName.proxy()));
        pooledDirContext.unbind((Name) jmockName.proxy());
    }

    /**
     * Test the rename methods
     *
     * @throws Exception if testcase fails.
     */
    public void testRename() throws Exception {
        String name = "name";
        String oldName = "oldName";

        jmockDirContext.expects(once()).method("rename").with(eq(name), eq(oldName));
        pooledDirContext.rename(name, oldName);

        jmockDirContext.expects(once()).method("rename").with(eq(jmockName.proxy()), eq(jmockName.proxy()));
        pooledDirContext.rename((Name) jmockName.proxy(), (Name) jmockName.proxy());
    }

    /**
     * Test the list methods
     *
     * @throws Exception if testcase fails.
     */
    public void testList() throws Exception {
        String name = "name";

        jmockDirContext.expects(once()).method("list").with(eq(name));
        pooledDirContext.list(name);

        jmockDirContext.expects(once()).method("list").with(eq(jmockName.proxy()));
        pooledDirContext.list((Name) jmockName.proxy());
    }

    /**
     * Test the listBindings methods
     *
     * @throws Exception if testcase fails.
     */
    public void testListBindings() throws Exception {
        String name = "name";

        jmockDirContext.expects(once()).method("listBindings").with(eq(name));
        pooledDirContext.listBindings(name);

        jmockDirContext.expects(once()).method("listBindings").with(eq(jmockName.proxy()));
        pooledDirContext.listBindings((Name) jmockName.proxy());
    }

    /**
     * Test the destroySubcontext methods
     *
     * @throws Exception if testcase fails.
     */
    public void testDestroySubcontext() throws Exception {
        String name = "name";

        jmockDirContext.expects(once()).method("destroySubcontext").with(eq(name));
        pooledDirContext.destroySubcontext(name);

        jmockDirContext.expects(once()).method("destroySubcontext").with(eq(jmockName.proxy()));
        pooledDirContext.destroySubcontext((Name) jmockName.proxy());
    }

    /**
     * Test the lookupLink methods
     *
     * @throws Exception if testcase fails.
     */
    public void testLookupLink() throws Exception {
        String name = "name";

        jmockDirContext.expects(once()).method("lookupLink").with(eq(name));
        pooledDirContext.lookupLink(name);

        jmockDirContext.expects(once()).method("lookupLink").with(eq(jmockName.proxy()));
        pooledDirContext.lookupLink((Name) jmockName.proxy());
    }

    /**
     * Test the getNameParser methods
     *
     * @throws Exception if testcase fails.
     */
    public void testGetNameParser() throws Exception {
        String name = "name";

        jmockDirContext.expects(once()).method("getNameParser").with(eq(name));
        pooledDirContext.getNameParser(name);

        jmockDirContext.expects(once()).method("getNameParser").with(eq(jmockName.proxy()));
        pooledDirContext.getNameParser((Name) jmockName.proxy());
    }

    /**
     * Test the composeName methods
     *
     * @throws Exception if testcase fails.
     */
    public void testComposeName() throws Exception {
        String name = "name";
        String prefix = "prefix";
        Mock jmockPrefix = mock(Name.class);

        jmockDirContext.expects(once()).method("composeName").with(eq(name), eq(prefix));
        pooledDirContext.composeName(name, prefix);

        jmockDirContext.expects(once()).method("composeName").with(eq(jmockName.proxy()), eq(jmockPrefix.proxy()));
        pooledDirContext.composeName((Name) jmockName.proxy(), (Name) jmockPrefix.proxy());
    }

    /**
     * Test the *Environment methods
     *
     * @throws Exception if testcase fails.
     */
    public void testEnvironmentMethods() throws Exception {
        String propName = "propName";
        Object propVal = new Object();

        jmockDirContext.expects(once()).method("addToEnvironment").with(eq(propName), eq(propVal));
        pooledDirContext.addToEnvironment(propName, propVal);

        jmockDirContext.expects(once()).method("removeFromEnvironment").with(eq(propName));
        pooledDirContext.removeFromEnvironment(propName);

        jmockDirContext.expects(once()).method("getEnvironment");
        pooledDirContext.getEnvironment();

        jmockDirContext.expects(once()).method("getNameInNamespace");
        pooledDirContext.getNameInNamespace();
    }

    public static Test suite() {
        return new TestSuite(PooledDirContextTest.class);
    }
}
