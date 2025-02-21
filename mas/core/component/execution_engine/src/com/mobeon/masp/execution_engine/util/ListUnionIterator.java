package com.mobeon.masp.execution_engine.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Iterator for iterating through a union of Lists
 *
 * @author Mikael Andersson
 */
public class ListUnionIterator<T> implements Iterator<T> {
    int position = 0;
    private Iterator<List<T>> listIterator;
    private Iterator<T> current;

    public ListUnionIterator(List<T> ... lists) {
        this.listIterator = Arrays.asList(lists).iterator();
    }

    public boolean hasNext() {
        if (null == current && listIterator.hasNext())
            current = listIterator.next().iterator();
        if (null == current)
            return false;
        boolean result = current.hasNext();
        while (!result && listIterator.hasNext()) {
            current = listIterator.next().iterator();
            if (result = current.hasNext())
                break;
        }
        return result;
    }

    public T next() {
        if (current != null)
            return current.next();
        else
            return null;
    }

    public void remove() {
        throw new UnsupportedOperationException("Remove not supported for ListUnionIterator");
    }
}
