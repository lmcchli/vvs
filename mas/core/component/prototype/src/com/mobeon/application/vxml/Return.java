package com.mobeon.application.vxml;

import com.mobeon.application.util.Expression;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:30:17 PM
 *
 <xsd:element name="return">
     <xsd:complexType>
         <xsd:attributeGroup ref="Namelist.attrib"/>
         <xsd:attributeGroup ref="Throw.attribs"/>
     </xsd:complexType>
 </xsd:element>

 */
public class Return
        implements ExecutableContentGroupElement,
                   NameListAttributedElement,
                   ThrowAttributedElement
{
    private NameListAttributedElement.List nameList = new NameListAttributedElement.List();

    public NameListAttributedElement.List getNameList()
    {
        return nameList;
    }

    private ThrowableEvent event;
    private Expression eventExpression;
    private String message;
    private Expression messageExpression;

    public ThrowableEvent getEvent()
    {
        return event;
    }

    public void setEvent(ThrowableEvent event)
    {
        this.event = event;
    }

    public Expression getEventExpression()
    {
        return eventExpression;
    }

    public void setEventExpression(Expression eventExpression)
    {
        this.eventExpression = eventExpression;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public Expression getMessageExpression()
    {
        return messageExpression;
    }

    public void setMessageExpression(Expression messageExpression)
    {
        this.messageExpression = messageExpression;
    }
}

