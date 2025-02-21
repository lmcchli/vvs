/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

/**
 * @author Mikael Andersson
 */
public abstract class OperationBase extends ExecutableBase implements Operation {
    private String mnemonic;
    private String arguments;

    public static void appendSimpleMnemonic(StringAccumulator sa, int indent, String mnemonic) {
        indent(sa,indent);
        sa.append(mnemonic);
    }

    public abstract String arguments();

    public void freeze() {
        if(mnemonic == null) {
            mnemonic = classToMnemonic(getClass());
            arguments = arguments().replaceAll("\n","\\\\n");
        }
    }

    public void appendMnemonic(
        StringAccumulator accumulator, int indent, boolean recurse, String lineSep, String endSep) {
        freeze();
        appendSimpleMnemonic(accumulator, indent,mnemonic+"("+arguments+")");
     }


    public String arrayToString(String[] array) {
        StringBuffer args = new StringBuffer();
        args.append("{ ");
        for(int i=0;i<array.length;i++) {
            args.append(textArgument(array[i]));
            if(i+1<array.length)
                args.append(", ");
        }
        args.append(" }");
        return args.toString();
    }

    public String toString() {
        return toMnemonic(0);
    }

    protected String textArgument(String name) {
        return "'"+name+"'";
    }

    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        if(obj.getClass() == this.getClass()) {
            return toString().equals(obj.toString());
        }
        return super.equals(obj);
    }
}
