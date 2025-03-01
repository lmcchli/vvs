/*
* Conditions Of Use 
* 
* This software was developed by employees of the National Institute of
* Standards and Technology (NIST), an agency of the Federal Government.
* Pursuant to title 15 Untied States Code Section 105, works of NIST
* employees are not subject to copyright protection in the United States
* and are considered to be in the public domain.  As a result, a formal
* license is not needed to use the software.
* 
* This software is provided by NIST as a service and is expressly
* provided "AS IS."  NIST MAKES NO WARRANTY OF ANY KIND, EXPRESS, IMPLIED
* OR STATUTORY, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTY OF
* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, NON-INFRINGEMENT
* AND DATA ACCURACY.  NIST does not warrant or make any representations
* regarding the use of the software or the results thereof, including but
* not limited to the correctness, accuracy, reliability or usefulness of
* the software.
* 
* Permission to use this software is contingent upon your acceptance
* of the terms of this agreement
*  
* .
* 
*/
/******************************************************************************
 * Product of NIST/ITL Advanced Networking Technologies Division (ANTD).      *
 ******************************************************************************/
package gov.nist.javax.sip.header;
import java.util.ListIterator;
import java.util.LinkedList;
import java.util.Iterator;
import java.lang.reflect.*;
import gov.nist.core.*;

/**
 * Root class for all the collection objects in this list:
 * a wrapper class on the GenericObjectList class for lists of objects
 * that can appear in SIPObjects.
 * IMPORTANT NOTE: SIPObjectList cannot derive from SIPObject.
 *
 * @version 1.2 $Revision: 1.5 $ $Date: 2006/07/13 09:01:10 $
 *
 * @author M. Ranganathan   <br/>
 *
 * 
 */
public class SIPObjectList extends GenericObjectList {

	

	/**
	 * Construct a SIPObject List given a list name.
	 * @param lname String to set
	 */
	public SIPObjectList(String lname) {
		super(lname);
	}

	/**
	 * Construct a SIPObject List given a list name and a class for
	 * the objects that go into the list.
	 * @param lname String to set
	 * @param cname Class to set
	 */
	public SIPObjectList(String lname, Class cname) {
		super(lname, cname);
	}

	/**
	 * Construct a SIPObject List given a list name and a class for
	 * the objects that go into the list.
	 * @param lname String to set
	 * @param cname String to set
	 */
	public SIPObjectList(String lname, String cname) {
		super(lname, cname);
	}

	/**
	 * Construct an empty SIPObjectList.
	 */
	public SIPObjectList() {
		super();
	}

	
	

	/**
	 * Do a merge of the GenericObjects contained in this list with the
	 * GenericObjects in the mergeList. Note that this does an inplace
	 * modification of the given list. This does an object by object
	 * merge of the given objects.
	 *
	 *@param mergeList is the list of Generic objects that we want to do
	 * an object by object merge with. Note that no new objects are
	 * added to this list.
	 *
	 */

	public void mergeObjects(GenericObjectList mergeList) {
		if (!mergeList.getMyClass().equals(this.getMyClass()))
			throw new IllegalArgumentException("class mismatch");
		Iterator it1 = this.listIterator();
		Iterator it2 = mergeList.listIterator();
		while (it1.hasNext()) {
			GenericObject outerObj = (GenericObject) it1.next();
			while (it2.hasNext()) {
				Object innerObj = it2.next();
				outerObj.merge(innerObj);
			}
		}
	}

	/**
	 * Append a given list to the end of this list.
	 * @param otherList SIPObjectList to set
	 */
	public void concatenate(SIPObjectList otherList) {
		super.concatenate(otherList);
	}

	/**
	 * Append or prepend a given list to this list.
	 * @param otherList SIPObjectList to set
	 * @param topFlag boolean to set
	 */
	public void concatenate(SIPObjectList otherList, boolean topFlag) {
		super.concatenate(otherList, topFlag);
	}

	/**
	 * Get the first object of this list.
	 * @return GenericObject
	 */
	public GenericObject first() {
		return (SIPObject) super.first();
	}

	/**
	 * Get the class of the supported objects of this list.
	 * @return Class
	 */
	public Class getMyClass() {
		return super.getMyClass();
	}

	/**
	 * Get the next object of this list (assumes that first() has been
	 * called prior to calling this method.)
	 * @return GenericObject
	 */
	public GenericObject next() {
		return (SIPObject) super.next();
	}

	/**
	 * Get the next object of this list.
	 * @param li ListIterator to set
	 * @return GenericObject
	 */
	public GenericObject next(ListIterator li) {
		return (SIPObject) super.next(li);
	}

	
	

	/**
	 * Convert to a string given an indentation(for pretty printing).
	 * This is useful for debugging the system in lieu of a debugger.
	 *
	 * @param indent int to set
	 * @return an indentation
	 */
	public String debugDump(int indent) {
		return super.debugDump(indent);
	}

	/**
	 * Set the class of the supported objects of this list.
	 *
	 * @param cl Class to set
	 */
	public void setMyClass(Class cl) {
		super.setMyClass(cl);
	}

	


}
