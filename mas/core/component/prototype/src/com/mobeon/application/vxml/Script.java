package com.mobeon.application.vxml;

import com.mobeon.application.vxml.datatypes.Duration;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:30:40 PM
 *
 <xsd:element name="script">
     <xsd:complexType mixed="true">
         <xsd:attribute name="src" type="URIValidator.datatype"/>
         <xsd:attribute name="charset" type="xsd:string"/>
         <xsd:attributeGroup ref="Cache.attribs"/>
     </xsd:complexType>
 </xsd:element>

 */

// TODO: If src is file ref., "load" the source into the bean.
// TODO: If src is a URL including a protocol ref, throw exception (or handle the error in other fashion)
public class Script
        implements ExecutableContentGroupElement,
                   FormContentElement,
                   CacheAttributedElement,
                   VXMLContentElement
{
    private String src;
    private String charachterSet;
    private String body;
    private String fetchHint;
    private Duration fetchTimeOut;
    private int maxAge;
    private int maxStale;

    public String getSrc()
    {
        return src;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setSrc(String src)
    {
        this.src = src;
    }

    public String getCharachterSet()
    {
        return charachterSet;
    }

    public void setCharachterSet(String charachterSet)
    {
        this.charachterSet = charachterSet;
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


}
