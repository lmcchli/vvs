/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler;

import com.mobeon.masp.execution_engine.compiler.*;
import com.mobeon.masp.execution_engine.compiler.products.PredicateImpl;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.values.ECMAObjectValue;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author David Looberger
 */
public class PromptImpl extends PredicateImpl implements Predicate {
    private int count = 1;
    private String bargein = null;
    private String bargeintype = null;
    private String lang = null;
    private String base = null;
    private String cond = null;
    private String timeout = null;

    private static final List<Executable> ops;
    private static final ILogger logger = ILoggerFactory.getILogger(PromptImpl.class);
    private boolean childToInputItem = false;


    static {
        ops = new ArrayList<Executable>();
        ops.add(Ops.queuePlayableObject_TM());
    }


    public PromptImpl(Product parent, String localName, DebugInfo debugInfo) {
        super(parent, localName, debugInfo);
    }

    public PromptImpl(Product parent, DebugInfo debugInfo) {
        super(parent, debugInfo);
    }

    public String getTimeout() {
        return timeout;
    }

    public void setTimeout(String timeout) {
        this.timeout = timeout;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getBargein() {
        return bargein;
    }

    public void setBargein(String bargein) {
        this.bargein = bargein;
    }

    public String getBargeintype() {
        return bargeintype;
    }

    public void setBargeintype(String bargeintype) {
        this.bargeintype = bargeintype;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getCond() {
        return cond;
    }

    public void setCond(String cond) {
        this.cond = cond;
    }

    public void execute(ExecutionContext ex) throws InterruptedException {
        if (ex instanceof VXMLExecutionContext) {
            if (logger.isDebugEnabled()) logger.debug("In PromptImpl. (Queueing prompt)");
            VXMLExecutionContext vxmlExecutionContext = (VXMLExecutionContext) ex;
            // Propagate properties, for use in e.g. Audio
            propagateProperties(vxmlExecutionContext);
            if (isChildToInputItem())
                ex.getValueStack().push(new ECMAObjectValue(Boolean.TRUE));
            else
                ex.getValueStack().push(new ECMAObjectValue(Boolean.FALSE));

            ex.getValueStack().pushMark();
            ex.anonymousCall(ops);
            super.execute(ex);
        }

    }


    private void propagateProperties(VXMLExecutionContext ex) {
        if (bargein != null) {
            ex.setProperty("bargein", bargein);
        }
        if (bargeintype != null) {
            ex.setProperty("bargeintype", bargeintype);
        }

        if (timeout != null) {
            ex.setProperty("timeout", timeout);
        }
        if (lang != null) {
            ex.setProperty("xml:lang", lang);
        }
        if (base != null) {
            ex.setProperty("xml:base", base);
        }
    }

    public void setIsChildToInputItem(boolean isInputItemChild) {
        this.childToInputItem = isInputItemChild;
    }

    public boolean isChildToInputItem() {
        return childToInputItem;
    }
}
