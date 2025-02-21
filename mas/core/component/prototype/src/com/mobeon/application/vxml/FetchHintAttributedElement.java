package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 8, 2005
 * Time: 1:47:24 PM
 *
 * <xsd:attributeGroup name="Fetchhint.attrib">
        <xsd:annotation>
            <xsd:documentation>Used in Cache.attribs </xsd:documentation>
        </xsd:annotation>
        <xsd:attribute name="fetchhint" type="Fetchhint.datatype"/>
    </xsd:attributeGroup>

 */
public interface FetchHintAttributedElement
{
    public String getFetchHint();
    public void setFetchHint(String fetchHint);
}
