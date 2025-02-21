package com.mobeon.application.vxml;

import com.mobeon.ecma.ECMAExecutor;
import com.mobeon.application.util.Cond;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 4:19:05 PM
 *
 * Additional constraints: must evaluate to true or false
 */
public interface ConditionAttributedElement
{
    public boolean isCond(ECMAExecutor exec);
    public void setCond(Cond condition);
}
