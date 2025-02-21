/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.ccxml.compiler.operations;

import com.mobeon.masp.execution_engine.ccxml.BridgeParty;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.ccxml.Dialog;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLRuntimeCase;
import com.mobeon.masp.execution_engine.ccxml.runtime.Id;
import com.mobeon.masp.execution_engine.ccxml.runtime.IdGeneratorImpl;
import com.mobeon.masp.execution_engine.ccxml.runtime.event.DialogStartEvent;
import com.mobeon.masp.execution_engine.compiler.Constants;

public abstract class CCXMLOperationCase extends CCXMLRuntimeCase {

    public CCXMLOperationCase(String name) {
        super(name);
    }

    protected DialogStartEvent createDialogStartEvent(Id<BridgeParty> id, String src, String mimeType, CCXMLExecutionContext executionContext, Connection conn) {
        Dialog d = new Dialog(id, src, mimeType, executionContext);
       DialogStartEvent dse = new DialogStartEvent(d, conn);
        return dse;
    }

    protected DialogStartEvent createDialogStartEvent(Id<BridgeParty> id, String src, String mimeType, CCXMLExecutionContext executionContext) {
        return createDialogStartEvent(id,src,mimeType,executionContext,null);
    }
    protected DialogStartEvent createDialogStartEvent() {
        return createDialogStartEvent(IdGeneratorImpl.PARTY_GENERATOR.generateId(), "src", Constants.MimeType.VOICEXML_MIMETYPE, getExecutionContext(),null);
    }


}
