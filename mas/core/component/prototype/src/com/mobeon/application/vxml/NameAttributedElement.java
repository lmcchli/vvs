package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 4:18:48 PM
 *
 * Field variable name additional contraints: must be unique field name within form (2.3.1)
 */
public interface NameAttributedElement
{
    public String getName();
    public void setName(String name);
}
