package com.mobeon.application.vxml;

import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Expression;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:16:49 PM
 *
 <xsd:element name="exit">
     <xsd:complexType>
         <xsd:attribute name="expr" type="Script.datatype"/>
         <xsd:attributeGroup ref="Namelist.attrib"/>
     </xsd:complexType>
 </xsd:element>

 */
public class Exit
        implements NameListAttributedElement, ExecutableContentGroupElement

{
    private Expression expression;


    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    private NameListAttributedElement.List nameList = new NameListAttributedElement.List();

    public NameListAttributedElement.List getNameList()
    {
        return nameList;
    }
}
