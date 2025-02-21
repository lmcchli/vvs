package com.mobeon.application.vxml;

import com.mobeon.application.util.Expression;

import java.lang.*;
import java.lang.Object;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:28:35 PM
 *
 <xsd:element name="param">
     <xsd:complexType>
         <xsd:attribute name="name" type="xsd:NMTOKEN" use="required"/>
         <xsd:attribute name="expr" type="Script.datatype"/>
         <xsd:attribute name="value" type="xsd:string"/>
         <xsd:attribute name="valuetype" type="Valuetype.datatype" default="data"/>
         <xsd:attribute name="type" type="xsd:string"/>
     </xsd:complexType>
 </xsd:element>
 
 */
public class Param
        implements ObjectContentElement,
                   SubDialogContentElement
{
    private String name;
    private Expression expression;
    private java.lang.Object value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
