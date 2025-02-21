package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 8, 2005
 * Time: 1:56:39 PM
 *
 <xsd:attributeGroup name="Maxage.attrib">
     <xsd:annotation>
         <xsd:documentation>Used in Cache.attribs</xsd:documentation>
     </xsd:annotation>
     <xsd:attribute name="maxage" type="Integer.datatype"/>
 </xsd:attributeGroup>

 */
public interface MaxAgeAttributedElement
{
    public int getMaxAge();
    public void setMaxAge(int maxAge);    
}
