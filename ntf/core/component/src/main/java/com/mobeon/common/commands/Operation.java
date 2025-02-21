/*
 * Operation.java
 *
 * Created on den 26 augusti 2004, 13:58
 */

package com.mobeon.common.commands;


/**
 * This class represents a single operation within a {@link Command}.
 *
 */
public class Operation implements Cloneable
{

    // ----------------------------------------
    // Private Data
    // ----------------------------------------
    /** Opcode for this operation. */
    private short opcode;
    /** Parameter to operation. */
    private String param;
    /** Operation name, used for presentation. */
    private String opname;

    /**
     * Creates an operation with given info.
     * @param opcode Identifies the operation
     * @param param Parameter to the operaton
     * @param name Descriptive name for the operation, e.g. "WAITON". The
     *    name is used in the configuration file.
     */
    public Operation(short opcode, String name, String param)
    {
        this.opcode = opcode;
        this.opname = name;
        this.param = param;
    }

    /**
     * Creates an operation with zero opcode and no name.
     * Call this when the operation is to be restored
     * with unpack.
     */
    public Operation()
    {
        // Nothing to do
    }

    /**
     * Get a readable representation of this object.
     * The format is name[code](param)
     * @return Information about object
     */
    public String toString()
    {
        return opname + "[" + opcode + "](" + param + ")";
    }

    /**
     * Check if two operations are equal.
     * Two Operations are equal if they have the same opcode and parameter.
     * Note thet the operation name is not included in the comparision.
     * @param other Object to compare with
     * @return true if this is equal to other, false otherwise.
     */
    public boolean equals(Object other)
    {
        if (!(other instanceof Operation)) return false;
        Operation otherOp = (Operation) other;
        if (otherOp.param != param) {
            // Not equal if only one is null
            if ((otherOp.param == null) || (param == null)) return false;
            // Not equal if none are null and not equals()
            if (!otherOp.param.equals(param)) return false;
        }
        if (otherOp.opcode != opcode) return false;
        // No comparision for opname, its only informational
        return true;
    }

    /**
     * Hash for this object.
     * Implemented to fit with equal so the operation name is not
     * included when calculating the hashcode.
     * @return Hashcode for this object.
     */
    public int hashCode()
    {
        if (param != null) return opcode ^ param.hashCode();
        return opcode;
    }

    /**
     * Make a copy of this operation.
     * @return Copy of the object
     */
    public Object clone()
    {
        try {
            return super.clone();
        } catch (CloneNotSupportedException willNotHappen) {
            return null;
        }
    }


    /**
     * Restore operation  from byte array.
     * The name is retreived from the String array of names.
     * @param data Byte Array to restore from
     * @param index Place to read in data
     * @param names Mapping from opcodes to names
     * @return Number of bytes read from byte array
     */
    public int restore(byte[] data, int index, String[] names)
    {
        this.opcode = CommandUtil.unpackShort(data, index);
        StringBuffer paramBuff = new StringBuffer();
        int strBytes = CommandUtil.unpackString(data, index + 2, paramBuff);
        this.opname = names[opcode];
        this.param = paramBuff.toString();
        return CommandUtil.shortPackSize() + strBytes;
    }


    /**
     * Pack relevant information into byte array.
     * Note that the name is not saved.
     * @param data Byte array to pack into
     * @param index Index in array where to start
     * @return Number of bytes used in byte array.
     */
    public int pack(byte[] data, int index)
    {
        int size = CommandUtil.packShort(data, index, opcode);
        int strSize = CommandUtil.packString(data, index + size, param);
        CHLogger.log(CHLogger.TRACE,
                     "Operation: Pack sizes = " + size + "/" + strSize);
        return size + strSize;
    }

    /**
     * How many bytes are needed to save this operation when packing.
     * @return Number of bytes needed in pack array
     */
    public int getPackSize()
    {
        return CommandUtil.shortPackSize() + CommandUtil.stringPackSize(param);
    }


    /**
     * Get the operation code.
     * @return Opcode for this operation.
     */
    public short getOpcode()
    {
        return opcode;
    }

    /**
     * Get operation parameter.
     * @return Parameter for this operation.
     */
    public String getParam()
    {
        return param;
    }

    /**
     * Getter for property opname.
     * @return Value of property opname.
     */
    public java.lang.String getOpname()
    {
        return opname;
    }

}
