package com.mobeon.masp.execution_engine.util;

/**
 * User: QMIAN
 * Date: 2005-sep-08
 * Time: 11:42:08
 */
public class IntStack {

    int[] stack = null;
    int index = 0;

    public IntStack(int capacity) {
        stack = new int[capacity];
    }

    public void push(int i) {
        if(index + 1 > stack.length) {
            throw new StackOverflowError("Stack exhausted");
        }
        stack[index] = i;
        index++;
    }

    public int peek() {
        return stack[index-1];
    }

    public int pop() {
        int result = stack[--index];
        stack[index]=0;
        return result;
    }

    public int set(int ptr) {
        int result = stack[index-1];
        stack[index-1] = ptr;
        return result;
    }

    public void prune(int i) {
        for(int j=index;j>i;j--) {
            stack[j-1]=0;
        }
        index = i;
    }
}
