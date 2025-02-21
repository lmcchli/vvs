package com.mobeon.application.vxml;

import com.mobeon.application.vxml.datatypes.Duration;
import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Cond;
import com.mobeon.ecma.ECMAExecutor;

import java.util.ArrayList;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:27:11 PM
 *
 <xsd:element name="object">
     <xsd:complexType mixed="true">
         <xsd:choice minOccurs="0" maxOccurs="unbounded">
             <xsd:group ref="audio"/>
             <xsd:group ref="event.handler"/>
             <xsd:element ref="filled"/>
             <xsd:element ref="param"/>
             <xsd:element ref="property"/>
             <xsd:element ref="prompt"/>
         </xsd:choice>
         <xsd:attributeGroup ref="Form-item.attribs"/>
         <xsd:attributeGroup ref="Cache.attribs"/>
         <xsd:attribute name="classid" type="URIValidator.datatype"/>
         <xsd:attribute name="codebase" type="URIValidator.datatype"/>
         <xsd:attribute name="data" type="URIValidator.datatype"/>
         <xsd:attribute name="type" type="xsd:string"/>
         <xsd:attribute name="codetype" type="xsd:string"/>
         <xsd:attribute name="archive" type="URIValidator.datatype"/>
     </xsd:complexType>
 </xsd:element>
 
 */
public class Object
        implements FormItemAttributedElement,
                   CacheAttributedElement
                   
{
    private String classId; // todo not string
    private String codeBase; // todo not string
    private String data; // todo not string
    private String type;
    private String codeType;
    private String archive; // todo not string

    private String name;
    private Expression expression;
    private Cond cond;
    private String fetchHint;
    private Duration fetchTimeOut;
    private int maxAge;
    private int maxStale;

    public String getClassId()
    {
        return classId;
    }

    public void setClassId(String classId)
    {
        this.classId = classId;
    }

    public String getCodeBase()
    {
        return codeBase;
    }

    public void setCodeBase(String codeBase)
    {
        this.codeBase = codeBase;
    }

    public String getData()
    {
        return data;
    }

    public void setData(String data)
    {
        this.data = data;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getCodeType()
    {
        return codeType;
    }

    public void setCodeType(String codeType)
    {
        this.codeType = codeType;
    }

    public String getArchive()
    {
        return archive;
    }

    public void setArchive(String archive)
    {
        this.archive = archive;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Expression getExpression()
    {
        return expression;
    }

    public void setExpression(Expression expression)
    {
        this.expression = expression;
    }

    public boolean isCond(ECMAExecutor exec)
    {
        return cond.isCond(exec);
    }

    public void setCond(Cond condition)
    {
        this.cond = condition;
    }

    public String getFetchHint()
    {
        return fetchHint;
    }

    public void setFetchHint(String fetchHint)
    {
        this.fetchHint = fetchHint;
    }

    public Duration getFetchTimeOut()
    {
        return fetchTimeOut;
    }

    public void setFetchTimeOut(Duration fetchTimeOut)
    {
        this.fetchTimeOut = fetchTimeOut;
    }

    public int getMaxAge()
    {
        return maxAge;
    }

    public void setMaxAge(int maxAge)
    {
        this.maxAge = maxAge;
    }

    public int getMaxStale()
    {
        return maxStale;
    }

    public void setMaxStale(int maxStale)
    {
        this.maxStale = maxStale;
    }

    private ContentSet content = new ContentSet();

    public ContentSet getContent()
    {
        return content;
    }

    public class ContentSet
    {
        private ArrayList set = new ArrayList();

        public void add(ObjectContentElement member)
        {
            if (!set.contains(member))
                set.add(member);
        }

        public int size()
        {
            return set.size();
        }

        public ObjectContentElement get(int index)
        {
            return (ObjectContentElement)set.get(index);
        }

    }
}
