package com.mobeon.application.vxml;

import com.mobeon.application.vxml.grammar.Grammar;

import java.util.ArrayList;

/**
 * User: kalle
 * Date: Feb 8, 2005
 * Time: 12:26:04 PM
 *
  <xsd:group name="input">
        <xsd:annotation>
            <xsd:documentation>input using adapted SRGS grammars</xsd:documentation>
        </xsd:annotation>

        <xsd:choice>
            <xsd:element name="grammar" type="mixed-grammar"/>
        </xsd:choice>

    </xsd:group>
 
 */
public interface InputAttributedElement
        extends FieldContentElement,
                FormContentElement,
                RecordContentElement,
                TransferContentElement
{
    public InputElement getInput();
    public void setInput (InputElement input);
}
