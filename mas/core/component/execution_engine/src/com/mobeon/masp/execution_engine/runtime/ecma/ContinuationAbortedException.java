/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.runtime.ecma;

import org.mozilla.javascript.EvaluatorException;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;

public class ContinuationAbortedException extends EvaluatorException {
    private ResumableCall resumable;

    public ContinuationAbortedException(String detail, String fileName, int lineNo, String sourceLine, int columnNo, ResumableCall resumable) {
        super(detail, fileName, lineNo, sourceLine, columnNo);
        this.resumable = resumable;
    }

    public ContinuationAbortedException(String detail, String fileName, int lineNo, ResumableCall resumable) {
        super(detail, fileName, lineNo);
        this.resumable = resumable;
    }

    public ContinuationAbortedException(String detail, ResumableCall resumable) {
        super(detail);
        this.resumable = resumable;
    }

    public ResumableCall getResumable() {
        return resumable;
    }
}
