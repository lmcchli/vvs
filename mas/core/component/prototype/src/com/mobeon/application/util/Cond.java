package com.mobeon.application.util;

import org.apache.log4j.Logger;
import com.mobeon.ecma.ECMAExecutor;

/**
 * Created by IntelliJ IDEA.
 * User: MPAZE
 * Date: 2005-feb-14
 * Time: 15:41:53
 */
public class Cond {
    Logger logger = Logger.getLogger("mobeon.com");
    private String expr = null;
    private boolean value = true;

    public Cond(String _expr) {

        expr = _expr;
        if(expr == null) value = true;
        logger.debug("Creating condition [" + expr + "]");
    }


    public boolean isCond(ECMAExecutor exec) {
        if (expr == null)
            return false;
        Boolean ret =  (Boolean) exec.exec(expr);

        return ret.booleanValue();
    }

    public String getCond() {
        return expr;
    }
}
