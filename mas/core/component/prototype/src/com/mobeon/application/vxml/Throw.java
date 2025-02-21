package com.mobeon.application.vxml;

import com.mobeon.application.util.Expression;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:31:56 PM
 *
 <xsd:element name="throw">
     <xsd:complexType>
         <xsd:attributeGroup ref="Throw.attribs"/>
     </xsd:complexType>
 </xsd:element>

 */
public class Throw
        implements ExecutableContentGroupElement,
                   ThrowAttributedElement
{
    private String event;
    private Expression eventExpression;
    private String message;
    private Expression messageExpression;

    public Expression getMessageExpression()
    {
        return messageExpression;
    }

    public void setMessageExpression(Expression messageExpression)
    {
        this.messageExpression = messageExpression;
    }

    public ThrowableEvent getEvent()
    {
        return null;
    }

    public String getEventString()
    {
        return event;
    }

    public void setEvent(ThrowableEvent event) {

    }

    public void setEventString(String event)
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
}
