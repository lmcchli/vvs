package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 8, 2005
 * Time: 1:57:47 PM
 *
 <xsd:attributeGroup name="Maxstale.attrib">
     <xsd:annotation>
         <xsd:documentation>Used in Cache attribs</xsd:documentation>
     </xsd:annotation>
     <xsd:attribute name="maxstale" type="Integer.datatype"/>
 </xsd:attributeGroup>

 */
public interface MaxStaleAttributedElement
{
    public int getMaxStale();
    public void setMaxStale(int maxStale);
}
