package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

import java.util.HashMap;
import java.util.Map;

/**
 * @author David Looberger
 */
public class SetPromptProperties extends VXMLOperationBase {
    Map<String,String> props;
    public SetPromptProperties(Map<String, String> promptProps) {
        super();
        props = new HashMap<String, String>();
        props.putAll(promptProps);
    }

    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        if (props != null) {
            for (String prop : props.keySet()) {
                if (props.get(prop) != null)
                ex.getProperties().putProperty(prop,  props.get(prop));
            }
        }
    }

    public String arguments() {
        return "";
    }
}
