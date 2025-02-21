/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime;

import com.mobeon.masp.execution_engine.compiler.Executable;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.util.Prototype;

import java.util.List;

/**
 * Stack frame implementation used by {@link Engine} through {@link EngineStack}.
 * <p/>
 * <b>Threading: </b>This class is not designed to be used by more
 * than one thread at a time.
 *
 * @author Mikael Andersson
 */
public class StackFrame implements Prototype<StackFrame> {
    /**
     * Array of operations to be executed within this StackFrame.
     */
    public List<Executable> ops;
    public Executable singleOp;

    /**
     * Execution pointer indexing into the <code>ops[]</code> array.
     */
    public int ptr = -1;

    /**
     * Domain specific data for VoiceXML or CCXML
     */
    public Data frameData;

    /**
     * Call target that lead to the creation of this frame. This member
     * may be null if the frame wasn't the result of a call.
     */
    public Product call;

    /**
     * Constructs a StackFrame object using the supplied {@link Data} class
     * as a prototype for the extraData field.
     *
     * @param extraData Prototype for extraData field.
     */
    public StackFrame(Data extraData) {
        this.frameData = (Data) extraData.duplicate();
    }

    private StackFrame() {
    }

    private StackFrame(Data data, List<Executable> ops,Executable singleOp, int ptr, Product call) {
        this.frameData = data;
        this.ops = ops;
        this.ptr = ptr;
        this.call = call;
        this.singleOp = singleOp;
    }

    /**
     * Checks if there are more operations to execute in
     * this frame.
     *
     * @return True if there are more operations to execute.
     */
    public boolean hasNext() {
        return (singleOp == null) ? ptr < ops.size() : ptr < 1;
    }

    public StackFrame copy(StackFrame frame) {
        //ops is supposed to be immutable, so we can copy the ref
        this.ops = frame.ops;
        this.singleOp = frame.singleOp;
        this.ptr = frame.ptr;
        //Call is also immutable
        this.call = frame.call;
        //Extradata is mutable, so we must deep-copy it.
        this.frameData.copy(frame.frameData);
        return this;
    }


    public String toString() {
        if (ops != null
                && ptr >= 0
                && ptr < ops.size()) {
            return "StackFrame{" +
                    "op=" + ops.get(ptr) +
                    ", extraData=" + frameData +
                    ", call=" + call +
                    '}';
        } else {
            return "StackFrame{" +
                    "op=<undef>" +
                    ", extraData=" + frameData +
                    ", call=" + call +
                    '}';
        }
    }

    public StackFrame duplicate() {
        return new StackFrame(frameData.duplicate(), ops,singleOp, ptr, call);
    }

    public StackFrame clone() {
        return new StackFrame().copy(this);
    }
}
