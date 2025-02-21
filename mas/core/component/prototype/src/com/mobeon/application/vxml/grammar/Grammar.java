package com.mobeon.application.vxml.grammar;

import com.mobeon.application.vxml.InputElement;
import com.mobeon.application.vxml.FormContentElement;
import com.mobeon.application.vxml.FieldContentElement;

/**
 * User: kalle
 * Date: Feb 17, 2005
 * Time: 8:05:00 PM
 */
public interface Grammar
        extends InputElement, FormContentElement, FieldContentElement
{
    public Rule getRule();
}
