package com.mobeon.application.vxml;

import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Expression;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:32:46 PM
 *
 <xsd:element name="var">
     <xsd:complexType>
         <xsd:attribute name="name" type="RestrictedVariableName.datatype" use="required"/>
         <xsd:attributeGroup ref="Expr.attrib"/>
     </xsd:complexType>
 </xsd:element>

 */
public class Var
        implements VariableGroup,
                   ExecutableContentGroupElement,
                   ExpressionAttributedElement,
                   VXMLContentElement
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
