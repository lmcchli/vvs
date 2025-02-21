package com.mobeon.application.vxml;

import com.mobeon.application.vxml.datatypes.Method;
import com.mobeon.application.vxml.datatypes.Duration;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:31:32 PM

 <xsd:element name="submit">
     <xsd:complexType>
         <xsd:attributeGroup ref="Cache.attribs"/>
         <xsd:attributeGroup ref="Next.attribs"/>
         <xsd:attribute name="fetchaudio" type="URIValidator.datatype"/>
         <xsd:attributeGroup ref="Submit.attribs"/>
     </xsd:complexType>
 </xsd:element>

 */
public class Submit
        implements ExecutableContentGroupElement,
                   SubmitAttributedElement,
                   CacheAttributedElement,
                   NextAttributedElement
{
    private String fetchAudio;

    private Method method = Method.GET;
    private String encodingType;
    private NameListAttributedElement.List nameList = new NameListAttributedElement.List();
    private String fetchHint;
    private Duration fetchTimeOut;
    private int maxAge;
    private int maxStale;
    private NextElement next;

    public List getNameList()
    {
        return nameList;
    }

    public String getFetchAudio()
    {
        return fetchAudio;
    }

    public void setFetchAudio(String fetchAudio)
    {
        this.fetchAudio = fetchAudio;
    }

    public Method getMethod()
    {
        return method;
    }

    public void setMethod(Method method)
    {
        this.method = method;
    }

    public String getEncodingType()
    {
        return encodingType;
    }

    public void setEncodingType(String encodingType)
    {
        this.encodingType = encodingType;
    }

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

    public NextElement getNext()
    {
        return next;
    }

    public void setNext(NextElement next)
    {
        this.next = next;
    }

}
