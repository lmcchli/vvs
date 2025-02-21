package com.mobeon.application.vxml;

import com.mobeon.application.util.Expression;

/**
 * User: kalle
 * Date: Feb 8, 2005
 * Time: 3:57:31 PM
 *
 <xsd:attributeGroup name="Throw.attribs">
     <xsd:annotation>
         <xsd:documentation>Attributes associated with event
     throwing </xsd:documentation>
     </xsd:annotation>
     <xsd:attribute name="event" type="EventName.datatype"/>
     <xsd:attribute name="eventexpr" type="Script.datatype"/>
     <xsd:attribute name="message" type="xsd:string"/>
     <xsd:attribute name="messageexpr" type="Script.datatype"/>
 </xsd:attributeGroup>

 */
public interface ThrowAttributedElement
{
    public ThrowableEvent getEvent();
    public void setEvent(ThrowableEvent event);

    public Expression getEventExpression();
    public void setEventExpression(Expression eventExpression);  // todo > script instance

    public String getMessage();
    public void setMessage(String message);

    public Expression getMessageExpression();
    public void setMessageExpression(Expression messageExpression); // todo > script instance
}
