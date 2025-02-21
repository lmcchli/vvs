/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application.util;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: snikkt
 * Date: Feb 2, 2005
 * Time: 2:39:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShadowHashTupleSet extends HashTupleSet {
    Set nonShadows = new HashSet();

    public ShadowHashTupleSet shadowClone() {
        ShadowHashTupleSet result = null;
        result = (ShadowHashTupleSet)clone();
        result.nonShadows = new HashSet();
        return result;
    }


    public boolean add(Object o) {
        nonShadows.add(o);
        return super.add(o);
    }

    public boolean addAll(Collection c) {
        nonShadows.addAll(c);
        return super.addAll(c);
    }

    public boolean remove(Object o) {
        nonShadows.remove(o);
        return super.remove(o);
    }

    public boolean removeAll(Collection c) {
        nonShadows.removeAll(c);
        return super.removeAll(c);
    }

    public boolean isReal(Object tuple) {
        return nonShadows.contains(tuple);
    }

    public class RealIterator implements Iterator {
        Iterator it;
        Object current;

        private RealIterator(Iterator it) {
            this.it = it;
            current = findNext();
        }

        public void remove() {
            if(current != null) {
                nonShadows.remove(current);
                it.remove();
                current = findNext();
            }
        }

        private Object findNext() {
            Object nextOne=null;
            while(it.hasNext()) {
                nextOne = it.next();
                if(nonShadows.contains(nextOne)) {
                    break;
                } else {
                    nextOne = null;
                }
            }
            return nextOne;
        }

        public boolean hasNext() {
            return current != null;
        }

        public Object next() {
            Object nextOne = current;
            current = findNext();
            return nextOne;
        }
    }

    public Iterator onlyRealIterator() {
        return new RealIterator(map.values().iterator());
    }

    public class AllIterator implements Iterator {
        Iterator it;
        Object current;

        private AllIterator(Iterator it) {
            this.it = it;
            findNext();
        }

        public void remove() {
            if(current != null) {
                nonShadows.remove(current);
            }
            it.remove();
            findNext();
        }

        private void findNext() {
            current = null;
            if(it.hasNext())
                current = next();
        }

        public boolean hasNext() {
            findNext();
            return current != null;
        }

        public Object next() {
            return current;
        }
    }

    public Iterator iterator() {
        return new AllIterator(super.iterator());
    }
}
