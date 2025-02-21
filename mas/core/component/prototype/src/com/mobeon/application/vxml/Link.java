package com.mobeon.application.vxml;

import com.mobeon.application.util.Expression;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:24:09 PM

 <xsd:element name="link">
     <xsd:complexType>
         <xsd:group ref="input" minOccurs="0" maxOccurs="unbounded"/>
         <xsd:attributeGroup ref="Cache.attribs"/>
         <xsd:attributeGroup ref="Next.attribs"/>
         <xsd:attributeGroup ref="Throw.attribs"/>
         <xsd:attribute name="fetchaudio" type="URIValidator.datatype"/>
         <xsd:attribute name="dtmf" type="DTMFSequence.datatype"/>
     </xsd:complexType>
 </xsd:element>
 
 */


/*
   next="URI"
    expr="ECMAScript_Expression"
    event="event"
    eventexpr="ECMAScript_Expression"
    dtmf="DTMF_sequence"
    fetchaudio="URI"

    fetchhint="safe"
    fetchtimeout="time_interval"
    maxage="integer"
    maxstale="integer"
    message="string"
    messageexpr="ECMAScript_Expression">
*/
public class Link
        implements FieldContentElement,
                   FormContentElement,
                   InitalContentElement,
                   VXMLContentElement
{

    String next = null;
    Expression expr = null;
    String event = null;
    Expression eventexpr = null;
    String dtmf = null;
    String message = null;
    Expression messageexpr = null;


    public Expression getMessageexpr() {
        return messageexpr;
    }

    public void setMessageexpr(Expression messageexpr) {
        this.messageexpr = messageexpr;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public Expression getExpr() {
        return expr;
    }

    public void setExpr(Expression expr) {
        this.expr = expr;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Expression getEventexpr() {
        return eventexpr;
    }

    public void setEventexpr(Expression eventexpr) {
        this.eventexpr = eventexpr;
    }

    public String getDtmf() {
        return dtmf;
    }

    public void setDtmf(String dtmf) {
        this.dtmf = dtmf;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }



}
