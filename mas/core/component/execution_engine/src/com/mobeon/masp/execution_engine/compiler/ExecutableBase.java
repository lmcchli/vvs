package com.mobeon.masp.execution_engine.compiler;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Mikael Andersson
 */
public abstract class ExecutableBase implements Executable {    

    public String toMnemonic(int indent) {
        StringAccumulator sa = new StringAccumulator();
        appendMnemonic(sa, indent, true, "\n", "\n");
        return sa.toString();
    }

    public void appendMnemonic(StringAccumulator sa,int indent) {
        appendMnemonic(sa, indent, true, "\n", "\n");
    }

    public String toMnemonic() {
        StringAccumulator sa = new StringAccumulator();
        appendMnemonic(sa, 0, true, "\n", "\n");
        return sa.toString();
    }

    public static final String INDENT_STRING = "  ";

    public static void indent(StringAccumulator buf, int n) {
        if (n == -1)
            return;
        for (int i = 0; i < n; i++) {
            buf.append(INDENT_STRING);
        }
    }

    public String toString() {
        StringAccumulator sa = new StringAccumulator();
        appendMnemonic(sa, -1, false, " ", ", ");
        return sa.toString();
    }

    public static void appendMnemonics(StringAccumulator sa, List<? extends Executable> operations,int indent) {
        appendMnemonics(sa, operations, indent, true, "\n", "\n");
    }

    public static void appendMnemonics(StringAccumulator sa, List<? extends Executable> operations, int indent, boolean recurse, String lineSep, String entrySep) {
        sa.append(lineSep);
        if (recurse) {
            int size = operations.size();
            for (int i = 0; i < size; i++) {
                Executable executable = operations.get(i);
                sa.visit(executable);
                executable.appendMnemonic(sa, indent, true, "\n", "\n");
                if (i < size + 1)
                    sa.append(entrySep);
                else
                    sa.append(lineSep);
            }
        } else {
            indent(sa, indent);
            sa.append("...");
            sa.append(lineSep);
        }
    }

    protected String classToMnemonic(Class canonicalClass) {
        String mnemonic = canonicalClass.getCanonicalName();
        int lastDot = mnemonic.lastIndexOf('.');
        return mnemonic.substring(lastDot+1);
    }

    public static void appendMnemmonicPrologue(StringAccumulator sa, String productKind, String productTag, String name, String arguments) {
        sa.append(productKind);
        sa.append("(");
        if (name != null) {
            sa.append("id='").append(name).append("'");
        }
        if (arguments != null) {
            if(name != null)
                sa.append(", ");
            sa.append(arguments);
        }
        sa.append(")");
        if(productTag != null)
            sa.append(productTag);
    }

    public static class StringAccumulator {

        private StringBuilder buf = new StringBuilder();
        private Set<Executable> visited = new HashSet<Executable>();

        public void visit(Executable e) {
            if(visited.contains(e)) {
                throw new RuntimeException("Loop detected in compiled graph, the Excutable "+e.toString()+" occurs several times");
            }
            visited.add(e);
        }

        public StringAccumulator append(String s) {
            buf.append(s);
            return this;
        }

        public String toString() {
            return buf.toString();
        }
    }

     public abstract void appendMnemonic(
        StringAccumulator sa, int indent, boolean recurse, String lineSep, String entrySep);
}
