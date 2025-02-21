package com.mobeon.application.vxml;

import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Cond;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 10:28:49 PM
 */
public interface IfAttributedElement
{
    // todo> refactor. condition should be a reference to a Script instance.

    public Cond getCondition();
    public void setCondition(Cond condition);
}
