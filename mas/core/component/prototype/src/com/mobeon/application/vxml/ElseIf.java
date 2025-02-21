package com.mobeon.application.vxml;

import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Cond;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:15:54 PM
 *
 <xsd:element name="elseif">
     <xsd:complexType>
         <xsd:attributeGroup ref="If.attribs"/>
     </xsd:complexType>
 </xsd:element>
 
 */
public class ElseIf
        implements IfAttributedElement, ExecutableContentGroupElement
{
    private Cond condition;

    public Set getExecutableContent() {
        return executableContent;
    }

    public void setExecutableContent(Set executableContent) {
        this.executableContent = executableContent;
    }

    private ExecutableContentGroupElement.Set executableContent = new ExecutableContentGroupElement.Set();

    public Cond getCondition()
    {
        return condition;
    }

    public void setCondition(Cond condition)
    {
        this.condition = condition;
    }
}
