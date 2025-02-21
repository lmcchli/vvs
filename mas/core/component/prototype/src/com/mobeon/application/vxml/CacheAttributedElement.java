package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 8, 2005
 * Time: 1:45:50 PM
 * 
 <xsd:attributeGroup name="Cache.attribs">
     <xsd:annotation>
         <xsd:documentation>Cache attributes to control caching behavior</xsd:documentation>
     </xsd:annotation>
     <xsd:attributeGroup ref="Fetchhint.attrib"/>
     <xsd:attributeGroup ref="Fetchtimeout.attrib"/>
     <xsd:attributeGroup ref="Maxage.attrib"/>
     <xsd:attributeGroup ref="Maxstale.attrib"/>
 </xsd:attributeGroup>
 
 */
public interface CacheAttributedElement
        extends FetchHintAttributedElement,
                FetchTimeOutAttributedElement,
                MaxAgeAttributedElement,
                MaxStaleAttributedElement
{
}
