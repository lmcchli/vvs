package com.mobeon.application.vxml;

import com.mobeon.application.vxml.datatypes.Accept;

/**
 * User: kalle
 * Date: Feb 8, 2005
 * Time: 2:51:09 PM
 <xsd:attributeGroup name="Accept.attrib">
     <xsd:annotation>
         <xsd:documentation>Accept attribute: menu, option (2.2)</xsd:documentation>
     </xsd:annotation>
     <xsd:attribute name="accept" type="Accept.datatype" default="exact"/>
 </xsd:attributeGroup>

 */
public interface AcceptAttributedElement
{
    public void setAccept(Accept accept);
    public Accept getAccept();
}
