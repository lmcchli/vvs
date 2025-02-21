package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:17:36 PM
 *
 <xsd:element name="filled">
     <xsd:complexType mixed="true">
         <xsd:group ref="executable.content" minOccurs="0" maxOccurs="unbounded"/>
       <xsd:attribute name="mode" type="FilledMode.datatype"/>
         <xsd:attributeGroup ref="RestrictedNamelist.attrib"/>
     </xsd:complexType>
 </xsd:element>
 
 */
public class Filled
        implements RestrictedNameListAttributedElement,
                   FieldContentElement,
                   FormContentElement,
                   ObjectContentElement,
                   RecordContentElement,
                   SubDialogContentElement,
                   TransferContentElement
{
    private ExecutableContentGroupElement.Set executableContent = new ExecutableContentGroupElement.Set();
    private RestrictedNameListAttributedElement.List nameList = new RestrictedNameListAttributedElement.List();
    private String mode = "any";

    public ExecutableContentGroupElement.Set getExecutableContent()
    {
        return executableContent;
    }

    public void setNameList(List nameList) {
        this.nameList = nameList;
    }


    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public RestrictedNameListAttributedElement.List getNameList()
    {
        return nameList;
    }
}
