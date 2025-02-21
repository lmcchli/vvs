package com.mobeon.application.vxml;

import com.mobeon.ecma.ECMAExecutor;
import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Expression;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:13:45 PM
 *
 *     <xsd:element name="block">
        <xsd:complexType mixed="true">
            <xsd:group ref="executable.content" minOccurs="0" maxOccurs="unbounded"/>
            <xsd:attributeGroup ref="Form-item.attribs"/>
        </xsd:complexType>
    </xsd:element>

 *
 */
public class Block
        implements FormItemAttributedElement,
                   VariableGroup
{
    private String name;
    private Expression expression;
    private Cond cond;

    private ExecutableContentGroupElement.Set executableContent = new ExecutableContentGroupElement.Set();

    public ExecutableContentGroupElement.Set getExecutableContent()
    {
        return executableContent;
    }

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

    public boolean isCond(ECMAExecutor exec)
    {
        return cond.isCond(exec);
    }

    public void setCond(Cond condition)
    {
        this.cond = condition;
    }
}
