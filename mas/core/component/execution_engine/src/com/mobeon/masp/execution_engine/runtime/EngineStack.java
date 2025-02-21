/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.masp.execution_engine.compiler.Executable;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.util.Tools;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Fixed-size runtime stack used by {@link Engine}.
 * All entries in this stack are instances of {@link StackFrame}.
 * <p/>
 * Stack operations <code>peek</code>, <code>pop</code> always return
 * the same StackFrame object at the same stack size. As a result of
 * this, it's a bad idea to hold on to a StackFrame anywhere outside
 * of the Engine. Also <code>set</code> always return the same StackFrame
 * regardless of current stack size.
 * <p/>
 * <b>Notes:</b>
 * <ul>
 * <li> This class it not safe for concurrent access by multiple threads</li>
 * <li> Will throw {@link StackOverflowError} if capacity is surpassed</li>
 * </ul>
 *
 * @author Mikael Andersson
 */
public class EngineStack {
    StackFrame[] stack = null;
    StackFrame result;
    final Data prototype;
    int count = 0;
    int capacity = 0;

    /**
     * Creates an EngineStack instance with fixed size and preallocates
     * all entries.
     *
     * @param capacity Maximum number of entries this stack will support
     */
    public EngineStack(int capacity, Data prototype) {
        this.prototype = prototype;
        stack = new StackFrame[capacity];
        this.capacity = stack.length;
        Tools.fillArray(stack, new StackFrame(prototype));
        for (int i = 0; i < stack.length; i++) {
            stack[i] = new StackFrame(prototype);
        }
        result = new StackFrame(prototype);
    }

    /**
     * Places a new {@link StackFrame} on the stack and assigns
     * the <code>Object[]</code> array to its <code>ops</code>
     * member.
     * The <code>ptr</code> member of the frame is initialized to 0
     *
     * @param ops Sequence of {@link Executable}
     */
    public void push(List<Executable> ops) {
        assertSize();
        setFrame(stack[count], ops,null, -1, null);
        count++;
    }

    public void push(List<Executable> ops, Product calledProduct) {
        assertSize();
        setFrame(stack[count], ops,null, -1, calledProduct);
        count++;
    }

    public void push(Product calledProduct) {
        assertSize();
        setFrame(stack[count], null,calledProduct, -1, calledProduct);
        count++;
    }

    private void assertSize() {
        if (count + 1 > stack.length) {
            throw new EngineStackExhausted("Runtime stack exhausted");
        }
    }

    /**
     * Gets the top entry from the stack without removing it from the stack.
     * <p/>
     * <b>Note:</b>
     * <ul><li>All invocations at the same <code>Stack.size()</code>
     * will return the same {@link StackFrame} object. The contents however,
     * may differ</li></ul>
     *
     * @return The {@link StackFrame} on the top of the stack
     */
    public StackFrame peek() {
        if (count > 0) {
            return stack[count - 1];
        } else
            return null;
    }

    /**
     * Gets the n:th entry from the stack without removing it from the stack.
     * <p/>
     * <b>Note:</b>
     * <ul><li>All invocations at the same <code>Stack.size()</code>
     * will return the same {@link StackFrame} object. The contents however,
     * may differ</li></ul>
     *
     * @param index The entry we want to look at.
     * @return The {@link StackFrame} at the specified index, or <code>null</code>
     *         if the index was invalid.
     */
    public StackFrame peek(int index) {
        if (index >= 0 && index < this.count) {
            return stack[index];
        } else
            return null;
    }


    /**
     * Removes the top entry from the stack and returns it.
     * <p/>
     * If the stack is empty, <code>null</code> is returned.
     * <b>Note:</b>
     * <ul><li>All invocations at the same <code>Stack.size()</code>
     * will return the same {@link StackFrame} object. The contents however,
     * may differ</li></ul>
     *
     * @return The {@link StackFrame} on the top of the stack
     */
    public StackFrame pop() {
        if (count == 0)
            return null;
        return stack[--count];
    }

    /**
     * Copies contents from {@link StackFrame} <code>frame</frame> to
     * the <code>result</code> member-variable.
     *
     * @param frame Source frame
     * @return Returns the <code>this.result</code> member. This is only
     *         a convenience.
     */
    protected StackFrame returnResult(StackFrame frame) {
        result.ops = frame.ops;
        result.ptr = frame.ptr;
        result.frameData.copy(frame.frameData);
        return result;
    }

    protected void setFrame(StackFrame frame, List<Executable> ops,Executable singleOp, int ptr, Product call) {
        frame.ops = ops;
        frame.ptr = ptr;
        frame.call = call;
        frame.singleOp = singleOp;
        frame.frameData.clear();
    }


    /**
     * Efficiently prunes/truncates the stack to the specified size.
     *
     * @param size The desired size of the stack efter <code>prune</code>
     */
    public void prune(int size) {
        count = size;
    }

    /**
     * Returns the size of the stack
     *
     * @return Depth of stack
     */
    public int size() {
        return count;
    }

    /**
     * Try to resize the stack to the desired capacity.
     * Attempts to shrink the stack to a capacity smaller than it's current
     * size will be ignored.
     *
     * @param stackSize The desired capacity of the stack after resize
     * @return The actual stack capacity after resize
     */
    public int resize(int stackSize) {
        if (stackSize < capacity) {
            return capacity;
        } else {
            StackFrame[] newStack = new StackFrame[stackSize];
            this.capacity = stackSize;
            System.arraycopy(stack, 0, newStack, 0, stack.length);
            for (int i = stack.length; i < newStack.length; i++) {
                newStack[i] = new StackFrame(prototype);
            }
            stack = newStack;
            return stack.length;
        }
    }

    /**
     * Returns current capacity of the stack
     * @return The stack capacity
     */
    public int capacity() {
        return capacity;
    }
    public List<StackFrame> asList() {
        StackFrame[] list = new StackFrame[size()];
        System.arraycopy(stack, 0, list, 0, size());
        return Collections.unmodifiableList(Arrays.asList(list));
    }

    public void clearFrames() {
        for (int i = 0; i < stack.length; i++) {
            stack[i].ops = null;
            stack[i].call = null;
            stack[i].ptr = -1;
        }
    }
}
