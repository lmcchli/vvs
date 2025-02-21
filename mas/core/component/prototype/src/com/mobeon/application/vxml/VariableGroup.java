package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 4:29:10 PM
 *
   <xsd:group name="variable">
        <xsd:choice>
            <xsd:element ref="block"/>
            <xsd:element ref="field"/>
            <xsd:element ref="var"/>
        </xsd:choice>
    </xsd:group>

 */
public interface VariableGroup
        extends FormContentElement
{
}
