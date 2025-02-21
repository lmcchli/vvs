package com.mobeon.application.vxml;

import java.util.ArrayList;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:33:15 PM
 * <p/>
 * <xsd:element name="vxml">
 * <xsd:complexType>
 * <xsd:choice maxOccurs="unbounded">
 * <xsd:group ref="event.handler"/>
 * <xsd:element ref="form"/>
 * <xsd:element ref="link"/>
 * <xsd:element ref="menu"/>
 * <xsd:element ref="meta"/>
 * <xsd:element ref="metadata"/>
 * <xsd:element ref="property"/>
 * <xsd:element ref="script"/>
 * <xsd:element ref="var"/>
 * </xsd:choice>
 * <xsd:attribute name="application" type="URIValidator.datatype"/>
 * <xsd:attribute ref="xml:base"/>
 * <xsd:attribute ref="xml:lang"/>
 * <xsd:attribute name="version" type="xsd:string" use="required"/>
 * </xsd:complexType>
 * </xsd:element>
 */
public class VXML {
    private String application; // todo> not string
    private String base;
    private String lang;

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }


    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    private String version;

    private ContentSet content = new ContentSet();

    public ContentSet getContent() {
        return content;
    }

    public class ContentSet {
        private ArrayList set = new ArrayList();

        public void add(VXMLContentElement member) {
            if (!set.contains(member))
                set.add(member);
        }

        public int size() {
            return set.size();
        }

        public VXMLContentElement get(int index) {
            return (VXMLContentElement) set.get(index);
        }
    }
}
