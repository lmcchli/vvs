package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 8, 2005
 * Time: 1:59:01 PM
 *
 <xsd:attributeGroup name="Next.attrib">
     <xsd:annotation>
         <xsd:documentation>URIValidator to transition to</xsd:documentation>
     </xsd:annotation>
     <xsd:attribute name="next" type="URIValidator.datatype"/>
 </xsd:attributeGroup>

 */
public interface NextAttributedElement
{
    public NextElement getNext();  // todo not string, early bound to form or document.
    public void setNext(NextElement next);
}
