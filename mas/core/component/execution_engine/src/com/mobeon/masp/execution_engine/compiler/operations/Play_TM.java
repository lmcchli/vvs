/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.OperationBase;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.runtime.Value;
import com.mobeon.masp.execution_engine.runtime.ValueVisitor;
import com.mobeon.masp.execution_engine.runtime.values.ValueVisitorImpl;
import com.mobeon.masp.util.Tools;
import com.mobeon.masp.execution_engine.voicexml.runtime.ExecutionContextImpl;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;

import java.util.List;

/**
 * @author Mikael Andersson
 */
public class Play_TM extends OperationBase {
    private static final ILogger log = ILoggerFactory.getILogger(Play_TM.class);

    private final ValueVisitor playVisitor;

    public class PlayVisitor extends ValueVisitorImpl {
        public Object visitText(ExecutionContext ex, String text) {
            if (ex instanceof ExecutionContextImpl) {
                Long offset = Tools.parseCSS2Time(((ExecutionContextImpl) ex).getProperties().getProperty(Constants.PlatformProperties.PLATFORM_AUDIO_OFFSET));
                if (offset == null) {
                    offset = 0L;
                }
                ((ExecutionContextImpl) ex).getCall().play(text, offset);
            }
            return super.visitText(ex, text);
        }

        public Object visitMediaObject(ExecutionContext ex, IMediaObject obj) {
            if (ex instanceof ExecutionContextImpl) {
                Long offset = Tools.parseCSS2Time(((ExecutionContextImpl) ex).getProperties().getProperty(Constants.PlatformProperties.PLATFORM_AUDIO_OFFSET));
                if (offset == null) {
                    offset = 0L;
                }
                ((ExecutionContextImpl) ex).getCall().play(obj, offset);
            }
            return super.visitMediaObject(ex, obj);
        }
    }

    public Play_TM() {
        playVisitor = new PlayVisitor();
    }

    public void execute(ExecutionContext ex) throws InterruptedException {
        List<Value> values = ex.getValueStack().popToMark();

        StringBuffer accumulated = new StringBuffer();
        for (int i = values.size() - 1; i >= 0; i--) {
            Value value = values.get(i);
            value.accept(ex, playVisitor);
        }

        if (log.isInfoEnabled())
            log.info(accumulated);
    }

    public String arguments() {
        return "";
    }

}
