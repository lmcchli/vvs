package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:37:21 PM
 *
 <xsd:group name="event.handler">
       <xsd:choice>
           <xsd:element ref="catch"/>
           <xsd:element ref="help"/>
           <xsd:element ref="noinput"/>
           <xsd:element ref="nomatch"/>
           <xsd:element ref="error"/>
       </xsd:choice>
   </xsd:group>

 */
public interface EventHandlerGroup
        extends FieldContentElement,
                FormContentElement,
                InitalContentElement,
                MenuContentElement,
                RecordContentElement,
                SubDialogContentElement,
                TransferContentElement,
                VXMLContentElement
{
}
