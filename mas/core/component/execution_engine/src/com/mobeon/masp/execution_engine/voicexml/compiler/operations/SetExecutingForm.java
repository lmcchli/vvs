package com.mobeon.masp.execution_engine.voicexml.compiler.operations;

import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.voicexml.runtime.VXMLExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.compiler.base.VXMLOperationBase;

/**
 * Created by IntelliJ IDEA.
 * User: ermkese
 * Date: Jan 5, 2006
 * Time: 2:40:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class SetExecutingForm extends VXMLOperationBase {

    Product form;

    public SetExecutingForm(Product form){
        this.form = form;
    }
    public void execute(VXMLExecutionContext context) throws InterruptedException {
        context.setExecutingForm(form);

    }


    public String arguments(){
        return form.getName();
    }
}
