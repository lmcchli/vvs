package com.mobeon.application.vxml;

import com.mobeon.application.vxml.datatypes.Duration;
import com.mobeon.application.vxml.datatypes.Method;
import com.mobeon.application.util.Expression;
import com.mobeon.application.util.Cond;
import com.mobeon.application.util.Cond;
import com.mobeon.ecma.ECMAExecutor;

import java.util.ArrayList;

import org.w3.x2001.vxml.FetchhintDatatype;

/**
 * User: kalle
 * Date: Feb 7, 2005
 * Time: 3:31:06 PM
 *
 <xsd:element name="subdialog">
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
         <xsd:attribute name="src" type="URIValidator.datatype"/>
         <xsd:attribute name="srcexpr" type="Script.datatype"/>
         <xsd:attributeGroup ref="Cache.attribs"/>
         <xsd:attribute name="fetchaudio" type="URIValidator.datatype"/>
         <xsd:attributeGroup ref="Submit.attribs"/>
     </xsd:complexType>
 </xsd:element>
 
 */
public class SubDialog
        implements FormContentElement,
                   FormItemAttributedElement,
                   CacheAttributedElement,
                   SubmitAttributedElement
{
    private ContentSet content = new ContentSet();

    public ContentSet getContent()
    {
        return content;
    }

    public class ContentSet
    {
        private ArrayList set = new ArrayList();

        public void add(SubDialogContentElement member)
        {
            if (!set.contains(member))
                set.add(member);
        }

        public int size()
        {
            return set.size();
        }

        public SubDialogContentElement get(int index)
        {
            return (SubDialogContentElement)set.get(index);
        }
    }

    private String name;
    private Expression expression;
    private Cond cond;
    
    private String fetchHint;
    private Duration fetchTimeOut;
        
    private String src;
    private String srcExpression;
    private String fetchAudio;

    private Method method = Method.GET;
    private String encodingType;
    private NameListAttributedElement.List nameList = new NameListAttributedElement.List();

    private int maxAge;
    private int maxStale;

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

    public Cond getCond() {
        return cond;
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

    public String getSrc()
    {
        return src;
    }

    public void setSrc(String src)
    {
        this.src = src;
    }

    public String getSrcExpression()
    {
        return srcExpression;
    }

    public void setSrcExpression(String srcExpression)
    {
        this.srcExpression = srcExpression;
    }

    public String getFetchAudio()
    {
        return fetchAudio;
    }

    public void setFetchAudio(String fetchAudio)
    {
        this.fetchAudio = fetchAudio;
    }

    public Method getMethod()
    {
        return method;
    }

    public void setMethod(Method method)
    {
        this.method = method;
    }

    public String getEncodingType()
    {
        return encodingType;
    }

    public void setEncodingType(String encodingType)
    {
        this.encodingType = encodingType;
    }

    public List getNameList()
    {
        return nameList;
    }



}
