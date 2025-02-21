package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 4:59:31 PM
 *
 * this class represents bread text in executable content
 *
 */
public class Bread
        implements ExecutableContentGroupElement , FieldContentElement, RecordContentElement
{
    private String text;
    public Bread(String str) {
        setText(str);
    }
    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }
}
