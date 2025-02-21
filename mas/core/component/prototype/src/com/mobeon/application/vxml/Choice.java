package com.mobeon.application.vxml;

import com.mobeon.application.vxml.datatypes.Accept;
import com.mobeon.application.vxml.datatypes.Duration;
import com.mobeon.application.util.Expression;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:14:26 PM
 *
    <xsd:element name="choice">
        <xsd:complexType mixed="true">
            <xsd:group ref="input" minOccurs="0" maxOccurs="unbounded"/>
            <xsd:attributeGroup ref="Cache.attribs"/>
            <xsd:attributeGroup ref="Throw.attribs"/>
	        <xsd:attribute name="accept" type="Accept.datatype"/>

            <xsd:attribute name="dtmf" type="DTMFSequence.datatype"/>
            <xsd:attribute name="fetchaudio" type="URIValidator.datatype"/>
            <xsd:attributeGroup ref="Next.attribs"/>
        </xsd:complexType>
    </xsd:element>

 *
 */
public class Choice
        implements MenuContentElement,
                   CacheAttributedElement,
                   ThrowAttributedElement,
                   NextAttributedElement
{
    private InputGroup.Set input = new InputGroup.Set();

    public InputGroup.Set getInput()
    {
        return input;
    }

    private String fetchHint;
    private Duration fetchTimeOut;
    private int maxAge;
    private int maxStale;

    private ThrowableEvent event;
    private Expression eventExpression;
    private String message;
    private Expression messageExpression;

    private NextElement next;

    public String getFetchHint()
    {
        return fetchHint;
    }

    public void setFetchHint(String fetchHint)
    {
        this.fetchHint = fetchHint;
    }

    public Duration getFetchTimeOut()
    {
        return fetchTimeOut;
    }

    public void setFetchTimeOut(Duration fetchTimeOut)
    {
        this.fetchTimeOut = fetchTimeOut;
    }

    public int getMaxAge()
    {
        return maxAge;
    }

    public void setMaxAge(int maxAge)
    {
        this.maxAge = maxAge;
    }

    public int getMaxStale()
    {
        return maxStale;
    }

    public void setMaxStale(int maxStale)
    {
        this.maxStale = maxStale;
    }

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

    public NextElement getNext()
    {
        return next;
    }

    public void setNext(NextElement next)
    {
        this.next = next;
    }

    private Accept accept;
    private String dtmf;
    private String fetchAudio;

    public Accept getAccept()
    {
        return accept;
    }

    public void setAccept(Accept accept)
    {
        this.accept = accept;
    }

    public String getDtmf()
    {
        return dtmf;
    }

    public void setDtmf(String dtmf)
    {
        this.dtmf = dtmf;
    }

    public String getFetchAudio()
    {
        return fetchAudio;
    }

    public void setFetchAudio(String fetchAudio)
    {
        this.fetchAudio = fetchAudio;
    }

}
