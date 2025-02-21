/**
 * Copyright (c) 2003 Mobeon AB
 * All Rights Reserved
 */

package com.mobeon.common.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;


/**
 * GroupedProperties implements functionality similar to Properties, but the
 * property file contains several groups of independent properties, each with
 * its own name. Individual properties are accessed with a combination of group
 * name and property name. An example of a GroupedProperties file defining two
 * groups with two and one property respectively:
 *
 * <CODE>
 * [ Group1 ]
 * Property1=prop1
 * Property2=prop2
 *
 * [ Group2 ]
 * Property1=prop1in2
 * </CODE>
 */
public class GroupedProperties {
    
    private Hashtable groups= null;

    /* Used to get an empty Enumerator for nonexisting groups */
    private Properties emptyProps= new Properties();
    
    /**
     * Constructor for an empty GroupedProperties. The only way to fill it is
     * with the "load" method.
     */
    public GroupedProperties() {
	groups= new Hashtable();
    }
    

    /**
     * loads GroupedProperties values from an InputStream. The format of this
     * stream is the same as that for Properties with one extension; lines that
     * start and end with brackets (ignoring any leading or trailing
     * whitespace), identify a new property group. The name of the group is the
     * text between the brackets, minus leading and trailing whitespace.
     * <P>
     * Any lines preceding the first group identification line are ignored.
     * If the same group identifier is used several times in a file, only the
     * last one is used.
     */
    public void load(InputStream is) throws IOException {
	String line;
	String group= null;
	String props= new String();
	BufferedReader in= new BufferedReader(new InputStreamReader(is));

	while (true) {
	    line= in.readLine();
	    if (line == null) {
		addGroup(group, props);
		return;
	    }
	    line= line.trim();
	    
	    //Skip empty lines and comments
	    if (line.length() > 0 && !line.startsWith("#") && !line.startsWith("!")) {
		if (line.startsWith("[") && line.endsWith("]")) {
		    //New group, store the previous group
		    addGroup(group, props);
		    props= new String();
		    group= line.substring(1, line.length() - 1).trim();
		} else {
		    props+= line + "\n";
		}
	    }
	}
    }

    
    /**
     * Returns the number of groups in this GroupedProperties.
     * @return the size (number of groups) in this GroupedProperties.
     */
    public int size() {
	return groups.size();
    }


    /**
     * Returns an Enumeration of all the group names in this GroupedProperties.
     *@return an Enumeration with all the group names in this
     * GroupedProperties.
     */
    public Enumeration groupNames() {
	return groups.keys();
    }


    /**
     * Returns an enumeration of all the keys in a property group.
     *@param groupName the name of the property group.
     *@return an enumeration of all the keys in the property group specified by
     * groupName.
     */
    public Enumeration propertyNames(String groupName) {
	if (groups.containsKey(groupName)) {
	    return ((Properties)groups.get(groupName)).propertyNames();
	} else {
	    return emptyProps.propertyNames();
	}
    }


    /**
     * Adds a new property group.
     *@param group the name of the new group.
     *@param propsLines all lines specifying the properties in the group,
     * concatenated into one String.
     */
    private void addGroup(String group, String propsLines) throws IOException {
	if (group == null) return;

	Properties props= new Properties();
	groups.put(group, props);
	props.load(new ByteArrayInputStream(propsLines.getBytes()));
    }


    /**
     * Searches for the property group with the specified name.
     * The method returns null if the group is not found.
     *@param group name of the group to search.
     *@return the property group with the specified name.
     */
    public Properties getProperties(String group) {
	if (groups == null) return null;

	return (Properties)(groups.get(group));

    }

    /**
     * Searches for the property with the specified key in the specified property group.
     * The method returns null if the property is not found.
     *@param group name of the group to search.
     *@param propName name of the property to search in the group.
     *@return the value in the property group with the specified property name.
     */
    public String getProperty(String group, String propName) {
	if (groups == null) return null;

	Properties props= (Properties)(groups.get(group));
	if (props == null) return null;
	
	return props.getProperty(propName);
    }
}
