package com.mobeon.masp.execution_engine.util;

import com.mobeon.masp.execution_engine.runtime.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

/**
 * Simple stack implementation based on an {@link ArrayList}
 * <p/>
 * <b>Threading: </b>This class is <em>not</em> safe to concurrently access from several threads
 *
 * @author Mikael Andersson
 */
public class Stack<E> implements Cloneable {

    /**
     * List which all storage is delegated to
     */
    protected ArrayList<E> list = new ArrayList<E>(15);

    public Stack() {
    }

    public Stack(ArrayList<E> list) {
        this.list.addAll(list);
    }

    /**
     * Gets the size of the stack
     * @return Size of the stack
     */
    public int size() {
        return list.size();
    }

    /**
     * Places a new entry on the top of the stack
     * @param entry Entry to place on stack
     */
    public void push(E entry) {
        list.add(entry);
    }

    /**
     * Removes the top entry from the stack and returns it.
     * @return The removed entry
     */
    public E pop() {
        if (size() > 0)
            return list.remove(list.size() - 1);
        else
            return null;
    }

    /**
     * Gets the entry on the top of the stack without deleting it.
     * @return The entry on the top of the stack
     */
    public E peek() {
        return list.get(list.size() - 1);
    }

    public E peek(int i) {
        int size = list.size();
        int index = size - i;
        if(index > 0)
            return list.get(list.size() - i -1);
        else
            return null;

    }
    /**
     * Trims the stack to the specifed new size.
     * @param newSize The desired new size
     */
    public void prune(int newSize) {
        while (list.size() > newSize) list.remove(list.size() - 1);
    }

    public Stack<E> clone() {
        return new Stack<E>(list);
    }

    public List<E> toList() {
        return Collections.unmodifiableList(list);
    }
}
