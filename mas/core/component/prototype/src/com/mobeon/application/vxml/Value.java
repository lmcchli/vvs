package com.mobeon.application.vxml;

import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Expression;

/**
 * Created by IntelliJ IDEA.
 * User: MPAZE
 * Date: 2005-feb-16
 * Time: 17:20:53
 */
public class Value  implements ExecutableContentGroupElement, SubDialogContentElement{
    Expression expr = null;

    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }
}
