package com.mobeon.application.vxml;

/**
 * User: kalle
 * Date: Feb 8, 2005
 * Time: 1:29:27 PM
 *

 <xsd:attributeGroup name="GrammarScope.attrib">
     <xsd:annotation>
         <xsd:documentation>Attributes common to form and menu</xsd:documentation>
     </xsd:annotation>
     <xsd:attribute name="scope" type="GrammarScope.datatype" default="dialog"/>
 </xsd:attributeGroup>

 */
public interface GrammarScopeAttributedElement
{
    public GrammarScopeAttributedElement.Scope getScope();
    public void setScope(GrammarScopeAttributedElement.Scope scope);

    /**
    <xsd:simpleType name="GrammarScope.datatype">
        <xsd:annotation>
           <xsd:documentation>dialog or document</xsd:documentation>
        </xsd:annotation>
        <xsd:restriction base="xsd:NMTOKEN">
            <xsd:enumeration value="document"/>
            <xsd:enumeration value="dialog"/>
        </xsd:restriction>
    </xsd:simpleType>
     */
    public final class Scope
    {
        public static final Scope DOCUMENT = new Scope("document");
        public static final Scope DIALOG = new Scope("dialog");

        private String scope;
        private Scope(String scope)
        {
            this.scope = scope;
        }

        public String toString()
        {
            return scope;
        }


    }

}
