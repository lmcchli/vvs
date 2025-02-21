package com.mobeon.application.vxml;

import com.mobeon.application.vxml.datatypes.Duration;

/**
 * User: kalle
 * Date: Feb 8, 2005
 * Time: 1:48:33 PM
 *
 <xsd:attributeGroup name="Fetchtimeout.attrib">
     <xsd:annotation>
         <xsd:documentation>Used in Cache.attribs</xsd:documentation>
     </xsd:annotation>
     <xsd:attribute name="fetchtimeout" type="Duration.datatype"/>
 </xsd:attributeGroup>

 */
public interface FetchTimeOutAttributedElement
{
    public void setFetchTimeOut(Duration duration);
    public Duration getFetchTimeOut();
}
