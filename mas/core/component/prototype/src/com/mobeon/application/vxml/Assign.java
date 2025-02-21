package com.mobeon.application.vxml;

import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Expression;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:11:43 PM
 *
 * <xsd:element name="assign">
        <xsd:complexType>
            <xsd:attribute name="name" type="VariableName.datatype" use="required"/>
            <xsd:attribute name="expr" type="Script.datatype" use="required"/>
        </xsd:complexType>
    </xsd:element>
 */
public class Assign
        implements ExecutableContentGroupElement
{
    private String name;
    private Expression expression;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Expression getExpression()
    {
        return expression;
    }

    public void setExpression(Expression expression)
    {
        this.expression = expression;
    }
}
