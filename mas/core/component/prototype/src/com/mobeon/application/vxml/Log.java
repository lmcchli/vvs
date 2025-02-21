package com.mobeon.application.vxml;

import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Expression;

import java.util.ArrayList;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:24:34 PM
 * <p/>
 * <xsd:element name="log">
 * <xsd:complexType mixed="true">
 * <xsd:choice minOccurs="0" maxOccurs="unbounded">
 * <xsd:element ref="value"/>
 * </xsd:choice>
 * <xsd:attribute name="label" type="xsd:string"/>
 * <xsd:attribute name="expr" type="Script.datatype"/>
 * </xsd:complexType>
 * </xsd:element>
 */
public class Log implements ExecutableContentGroupElement {
    private String label;
    private Expression expression;


    public Set getExecutbleContent() {
        return executbleContent;
    }

    public void setExecutbleContent(Set executbleContent) {
        this.executbleContent = executbleContent;
    }

    ExecutableContentGroupElement.Set executbleContent = new ExecutableContentGroupElement.Set();

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }


}
