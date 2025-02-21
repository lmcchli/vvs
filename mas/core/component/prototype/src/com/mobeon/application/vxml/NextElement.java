package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 16, 2005
 * Time: 4:38:09 PM
 *
 * The kind of Element that can be jumped to from GoTo, et.c.
 * todo: implement in those elements that can be jumped to. vxml, form, et.c.
 *
 */
public interface NextElement
{
    public String getURI();
}
