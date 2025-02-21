package com.mobeon.application.vxml;

import com.mobeon.application.vxml.datatypes.Method;

/**
 * User: kalle
 * Date: Feb 8, 2005
 * Time: 4:47:43 PM
 *
 <xsd:attributeGroup name="Method.attrib">
        <xsd:annotation>
            <xsd:documentation>Atttibute for data transport method</xsd:documentation>
        </xsd:annotation>
        <xsd:attribute name="method" type="Method.datatype" default="get"/>
    </xsd:attributeGroup>
 */
public interface MethodAttributedElement
{
    public Method getMethod();
    public void setMethod(Method method);
}
