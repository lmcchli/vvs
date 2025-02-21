package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:28:12 PM
 * <p/>
 * <xsd:element name="prompt" type="speak">
 * <xsd:annotation>
 * <xsd:documentation>prompt element uses SSML speak type</xsd:documentation>
 * </xsd:annotation>
 * </xsd:element>
 */
public class Prompt
        extends Speak
        implements ExecutableContentGroupElement,
        FieldContentElement,
        InitalContentElement,
        MenuContentElement,
        ObjectContentElement,
        RecordContentElement,
        SubDialogContentElement,
        TransferContentElement,
        FormContentElement {
    // todo> Speak not implemented

    public Set getExcutableContent() {
        return excutableContent;
    }

    public void setExcutableContent(Set excutableContent) {
        this.excutableContent = excutableContent;
    }

    ExecutableContentGroupElement.Set excutableContent = new ExecutableContentGroupElement.Set();


}
