package com.mobeon.application.util;

import com.mobeon.ecma.ECMAExecutor;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: MPAZE
 * Date: 2005-feb-14
 * Time: 16:20:07
 */
public class Expression {
    Logger logger = Logger.getLogger("mobeon.com");

    private String expr;
    public Expression(String _expr) {
        expr = _expr;
    }

    public java.lang.Object eval(ECMAExecutor exec) {
        if (expr != null) {
            return exec.exec(expr);
        }
        else {
            logger.error("The Expression object contains a null expression!");
            return null;
        }
    }
}
