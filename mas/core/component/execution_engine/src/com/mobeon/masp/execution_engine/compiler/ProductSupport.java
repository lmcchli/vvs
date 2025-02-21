package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.util.test.MASTestSwitches;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;
import com.mobeon.masp.execution_engine.ccxml.runtime.IdGeneratorImpl;
import com.mobeon.masp.execution_engine.util.EETestSwitches;

import java.util.List;

/**
 * @author Mikael Andersson
 */
public abstract class ProductSupport extends ExecutableBase implements EngineCallable {

    private String name;
    private final DebugInfo debugInfo;

    private Id<Product> id = IdGeneratorImpl.PRODUCT_GENERATOR.generateId();

    public ProductSupport(DebugInfo info, String localName) {
        debugInfo = info;
        name = localName;
    }

    public void appendMnemonic(
            StringAccumulator sa, int indent, boolean recurse, String lineSep, String entrySep) {
        indent(sa, indent);
        appendMnemmonicPrologue(sa, kind(), tag(), name, null);
        sa.append(" {");
        if (recurse) {
            indent++;
            appendExtraSections(sa, indent, lineSep);
            indent--;
        } else {
            sa.append(lineSep);
            sa.append("...");
            sa.append(lineSep);
        }
        indent(sa, indent);
        sa.append("}");
    }


    public static void appendSection(StringAccumulator sa, String section, List<? extends Executable> list, String lineSep, int count) {
        indent(sa, count);
        sa.append(section);
        sa.append("{");
        appendMnemonics(sa, list, count + 1);
        indent(sa, count);
        sa.append("}");
    }

    public String kind() {
        return classToMnemonic(getCanonicalClass());
    }

    public String tag() {
        return "<" + "#" + EETestSwitches.toProductTestId(id) + "," + debugInfo.getTagName() + "@<" +
               debugInfo.getLocation() + ">>";
    }

    public Class<?> getCanonicalClass() {
        return getClass();
    }

    public String getName() {
        return name;
    }

    public void setName(String aName) {
        name = aName;
    }

    public DebugInfo getDebugInfo() {
        return debugInfo;
    }

    public void freeze() {
    }

    public Id<Product> getId() {
        return id;
    }
}
