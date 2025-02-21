package com.mobeon.application.util;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: snikkt
 * Date: Feb 2, 2005
 * Time: 1:03:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class HashTupleSet implements TupleSet,Cloneable {
    Map map;
    List ordered = new ArrayList();
    Comparator c;

    public HashTupleSet() {
        map = new HashMap();
    }

    public HashTupleSet(Comparator c) {
        map = new TreeMap(c);
    }

    public Object get(Object tuple) {
        return map.get(tuple);
    }

    public boolean isReal(Object tuple) {
        return true;
    }

    public int size() {
        return map.size();
    }

    public void clear() {
        map.clear();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public Object[] toArray() {
        return map.values().toArray();
    }

    public boolean add(Object o) {
        boolean result = true;
        if(map.containsKey(o))
            result = false;
        map.put(o,o);
        return result;
    }

    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    public boolean remove(Object o) {
        return remove(o);
    }

    public boolean addAll(Collection c) {
        boolean result = true;
        for (Iterator iterator = c.iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            if(map.put(o,o) == null)
                result = false;
        }
        return result;
    }

    public boolean containsAll(Collection c) {
        boolean result = true;
        for (Iterator iterator = c.iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            result &= map.containsKey(o);
        }
        return result;
    }

    public boolean removeAll(Collection c) {
        boolean result = false;
        for (Iterator iterator = c.iterator(); iterator.hasNext();) {
            Object o = iterator.next();
            result |= map.remove(o) != null;
        }
        return result;
    }

    public boolean retainAll(Collection c) {
        boolean result = false;
        Iterator i = map.values().iterator();
        Set retain = new HashSet(c);
        while (i.hasNext()) {
            Object o = i.next();
            if(!retain.contains(o)) {
                result |= true;
                i.remove();
            }
        }
        return result;
    }

    public Iterator iterator() {
        return map.values().iterator();
    }

    public Object[] toArray(Object a[]) {
        return map.values().toArray();
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
}
