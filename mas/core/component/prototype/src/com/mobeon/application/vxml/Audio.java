package com.mobeon.application.vxml;

import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Expression;

/**
 * Created by IntelliJ IDEA.
 * User: MPAZE
 * Date: 2005-feb-17
 * Time: 18:41:38
 */
public class Audio  implements ExecutableContentGroupElement, AudioGroup {
    private String src;
    private Expression expr;


    public Set getExecutableContent() {
        return executableContent;
    }

    public void setExecutableContent(Set executableContent) {
        this.executableContent = executableContent;
    }

    ExecutableContentGroupElement.Set executableContent = new ExecutableContentGroupElement.Set();




    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }


    public Expression getExpr() {
        return expr;
    }
    public void setExpression(Expression _expr) {
        expr = _expr;
    }
}
