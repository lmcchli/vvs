/*
 * Copyright (c) 2005 Your Corporation. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.products.FormPredicate;
import com.mobeon.masp.execution_engine.runtime.scoping.Scope;
import com.mobeon.masp.execution_engine.runtime.values.ECMAObjectValue;
import com.mobeon.masp.util.Tools;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author David Looberger
 */
public class AreAllNeededFormItemsDone_P extends VXMLOperationBase {
    private boolean hasParent = false;
    private List<String> nameList = null;
    private String mode = null;

    public AreAllNeededFormItemsDone_P(FormPredicate parent, String nameList, String mode) {
        hasParent = parent != null;
        if (nameList != null && nameList.length() > 0) {
            StringTokenizer strtok = new StringTokenizer(nameList, " ");
            this.nameList = new ArrayList<String>();
            while (strtok.hasMoreTokens()) {
                String name = strtok.nextToken();
                this.nameList.add(name);
            }
        }


        this.mode = mode;
        if (this.mode == null || this.mode.length() == 0) {
            this.mode = Constants.VoiceXML.ALL;
        }
    }


    /** Check if any of the form items have an unsatisfied guard condition. If so, place the value
     * FALSE on the value stack, TRUE otherwise.
     *
     * @param ex
     * @throws InterruptedException
     */
    public void execute(VXMLExecutionContext ex) throws InterruptedException {
        if (hasParent && nameList == null) {
            nameList = ex.getFIAState().getInputItemNames();
            if (nameList != null && nameList.size() == 0){
                ex.getValueStack().push(new ECMAObjectValue(false));
                return;
            }
        }

        boolean satisfied = false;
        if (this.mode.equals(Constants.VoiceXML.ALL)) {
            satisfied = true; // In the look set the value to false if an unset item is found
        }
        for (String name : this.nameList) {
            Scope scope = ex.getCurrentScope();
            // TODO: Should the requirement be that the var is declared in EXACTLY this scope?
            if (scope.isDeclaredInAnyScope(name) && (scope.getValue(name) != scope.getUndefined())) {
                if (this.mode.equals(Constants.VoiceXML.ANY)) {
                    satisfied = true;
                    break;
                }
            } else {
                if (this.mode.equals(Constants.VoiceXML.ALL))  {
                    satisfied = false;
                    break;
                }
            }
        }
        ex.getValueStack().push(new ECMAObjectValue(satisfied));
    }


    public String arguments() {
        StringBuffer sb = new StringBuffer();
        sb.append(hasParent).append(", ").append(mode).append(", ");
        sb.append("[ ");
        if(nameList != null) {
        Tools.commaSeparate(sb,nameList.toArray());
        } else {
            sb.append("<empty>");
        }
        sb.append(" ]");
        return sb.toString();
    }
}
