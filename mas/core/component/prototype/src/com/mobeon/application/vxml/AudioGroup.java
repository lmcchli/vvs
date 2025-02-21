package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 8, 2005
 * Time: 12:17:06 AM
 *
 <xsd:group name="audio">
        <xsd:choice>
           <xsd:element ref="enumerate"/>
           <xsd:element ref="value"/>
           <xsd:element ref="audio"/>
        </xsd:choice>
    </xsd:group>
 */
public interface AudioGroup
        extends FieldContentElement,
                InitalContentElement,
                MenuContentElement,
                ObjectContentElement,
                RecordContentElement,
                SubDialogContentElement
{
}
