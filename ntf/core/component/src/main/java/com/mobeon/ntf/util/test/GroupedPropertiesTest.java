 /**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.ntf.util.test;

import com.mobeon.ntf.util.GroupedProperties;
import java.io.FileInputStream;
import java.util.*;
import junit.framework.*;

public class GroupedPropertiesTest extends TestCase {

    private GroupedProperties gp;
    private Properties p;
    private Properties p2;

    public GroupedPropertiesTest(String name) {
	super(name);
    }

    /*
     *
     */
    public void testGroupedProperties() throws Exception {
	gp= new GroupedProperties();
	assertEquals(0, gp.size());
	assertNull(gp.getProperty("Group1", "Property1"));
	gp.load(new FileInputStream("groupedproperties"));
	assertEquals(2, gp.size());
	assertEquals("prop1", gp.getProperty("Group1", "Property1"));
	assertEquals("prop2", gp.getProperty("Group1", "Property2"));
	assertEquals("prop1in2", gp.getProperty("Group2", "Property1"));
	assertNull(gp.getProperty("Group3", "Property1"));
	assertNull(gp.getProperty("Group1", "Property3"));

	Properties p= gp.getProperties("Group1");
	assertNotNull(p);

	String group;
	Hashtable found= new Hashtable();
	for (Enumeration e= gp.groupNames(); e.hasMoreElements() ;) {
	    group= (String)(e.nextElement());
	    found.put(group, "hej");
	}
	assertEquals(2, found.size());
	assertTrue(found.containsKey("Group1"));
	assertTrue(found.containsKey("Group2"));
	assertTrue(!found.containsKey("Group3"));

	found= new Hashtable();
	String prop;
	for (Enumeration e= gp.propertyNames("Group1"); e.hasMoreElements() ;) {
	    prop= (String)(e.nextElement());
	    found.put(prop, "hej");
	}
	assertEquals(2, found.size());
	assertTrue(found.containsKey("Property1"));
	assertTrue(found.containsKey("Property2"));
	assertTrue(!found.containsKey("Property3"));

    }
}
