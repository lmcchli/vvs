/*
 * Command.java
 *
 * Created on den 26 augusti 2004, 14:40
 */

package com.mobeon.common.commands;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A Command contains the next state and a list of operations.
 * A command can save itself into a byte array and restore copy
 * from the array.
 */
public class Command implements Cloneable
{

    // --------------------------------------------
    // Private Data
    // --------------------------------------------
    /** Next state to go to. */
    private int nextState;
    /** Our operations. */
    private LinkedList<Object> ops;

    private static final int PACKSIZE_COMMAND_ONLY = 8;

    /** Creates a new instance of Command
     * The command has a next state and a list
     * of operations to be performed. The given list
     * is copied so it may be changed after the call.
     * @param nextState Next state to go to after this command
     * @param ops The operations to be done while going to next state
     */
    public Command(int nextState, List<Operation> ops)
    {
        this.nextState = nextState;
        this.ops = new LinkedList<Object>();
        for (Iterator<Operation> it = ops.iterator(); it.hasNext();) {
            Operation op = it.next();
            this.ops.add(op.clone());
        }
    }

    /**
     * Create a command without operations, nextstate = 0.
     * This is to have a command that can be restored from
     * a byte array with {@link #restore}.
     */
    public Command()
    {
        ops = new LinkedList<Object>();
    }

    /**
     * Get a readable representation of this object.
     * @return String representation
     */
    public String toString()
    {
        return "Nextstate: " + nextState + ", Ops:" + ops;
    }

    /**
     * Make a copy of this object.
     * @return A deep copy.
     */
    public Object clone()
    {
        try {
            Command other = (Command) super.clone();
            if (this.ops != null) {
                other.ops = new LinkedList<Object>();
                for (Iterator<Object> it = ops.iterator(); it.hasNext();) {
                    Operation op = (Operation) it.next();
                    other.ops.add(op.clone());
                }
            }
            return other;
        } catch (CloneNotSupportedException willNotHappen) {
            return null;
        }
    }

    /**
     * Restore data from byte array.
     * @param data Byte array to restore from
     * @param index Position to start restoring at
     * @param opNames Mapping from opcodes to operation names
     * @return Number of bytes used.
     */
    public int restore(byte[] data, int index, String[] opNames)
    {
        int currIndex = index;
        nextState = CommandUtil.unpackInt(data, currIndex);
        currIndex += 4;
        int noOfOperations = CommandUtil.unpackInt(data, currIndex);
        currIndex += 4;
        CHLogger.log(CHLogger.TRACE,
                     "Command - Restoring " + noOfOperations + " operations");
        for (int i = 0; i < noOfOperations; i++) {
            Operation op = new Operation();
            currIndex += op.restore(data, currIndex, opNames);
            ops.add(op);
        }
        return currIndex - index;
    }

    /**
     * Pack data about this command into byte array.
     * @param data Byte array to restore to
     * @param index Position to start at
     * @return Number of bytes written
     */
    public int pack(byte[] data, int index)
    {
        int currIndex = index;
        CommandUtil.packInt(data, currIndex, nextState);
        currIndex += 4;
        CommandUtil.packInt(data, currIndex, ops.size());
        currIndex += 4;
        for (Iterator<Object> it = ops.iterator(); it.hasNext();) {
            Operation op = (Operation) it.next();
            currIndex += op.pack(data, currIndex);
        }
        return currIndex - index;
    }


    /**
     * How many bytes are needed to save this command when packing.
     * @return Number of bytes needed in pack array
     */
    public int getPackSize()
    {
        int opSize = 0;
        for (Iterator<Object> it = ops.iterator(); it.hasNext();) {
            Operation op = (Operation) it.next();
            opSize += op.getPackSize();
        }
        return PACKSIZE_COMMAND_ONLY + opSize;
    }


    /**
     * Get the next operation to perform for this command.
     * The operation list is not changed by this operation.
     * If there are no operations to be performed in the
     * list null is returned.
     * @return First operation in list.
     */
    public Operation getCurrentOperation()
    {
        if (ops.size() == 0) return null;
        return (Operation) ops.getFirst();
    }

    /**
     * Mark the first operation as done.
     * This means that the first operation in the list will be
     * removed and a new operation will be first. If there are no
     * operations in the list nothing is done.
     */
    public void operationDone()
    {
        if (ops.size() > 0) {
            ops.removeFirst();
        }
    }

    public void operationDoneBefore (int oprCode) {

    	Operation opr = (Operation)ops.getFirst();
    	try {
        	while (opr != null && opr.getOpcode() != oprCode) {
        		ops.removeFirst();
        		opr = (Operation)ops.getFirst();
        	}
    	} catch (NoSuchElementException e) {

    	}

    }

    /**
     * How many operations are still to be done.
     * @return Number of operations left
     */
    public int getOperationCount()
    {
        return ops.size();
    }

    /**
     * Get the next state.
     * @return next state
     */
    public int getNextState()
    {
        return nextState;
    }

    /**
     * Get operations, the returned list is not modifiable
     */
    public List<Object> getOperations()
    {
        return java.util.Collections.unmodifiableList(ops);
    }
}
