package com.mobeon.masp.execution_engine.xml;

import com.mobeon.masp.execution_engine.compiler.State;
import com.mobeon.masp.execution_engine.compiler.TagType;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.QName;
import org.dom4j.tree.DefaultElement;

import java.util.List;

/**
 * JDOM {@link Element} implementation containing line-number information.
 * Line numbers will only work if a {@link SAXCompilerReader} is used to read
 * the XML-file or -stream.
 *
 * @author Mikael Andersson
 */
public class CompilerElement extends DefaultElement {

    protected int line = 0;
    protected int column = 0;
    private State state;
    private DocumentFactory factory;
    private TagType tagType;


    public void setParent(Element element) {
        super.setParent(element);

        ensureTagType(element);
    }

    private void ensureTagType(Element element) {
        if (tagType == null || element != getParent()) {
            if (element != null && element instanceof CompilerElement) {
                TagType parentType = ((CompilerElement) element).getTagType();
                if (parentType != null)
                    tagType = parentType.clone();
            }

            if (tagType == null)
                tagType = new TagType();

            tagType.refine(getName());
        }
    }

    public CompilerElement(CompilerElementFactory factory, QName qName, int line, int column) {
        super(qName);
        this.line = line;
        this.column = column;
        this.factory = factory;
    }

    public int getColumn() {
        return column;
    }

    public int getLine() {
        return line;
    }

    public String getTagHead() {
        String head = this.asXML();
        if (head.indexOf('\n') != -1)
            head = head.substring(0, head.indexOf('\n'));
        head = head.replaceFirst("xmlns=\"[:.a-zA-Z0-9 /]*\" ", "");
        return head;
    }

    public State getState(State prototype) {
        if (state == null && prototype != null) {
            CompilerElement node = this;
            while ((node = (CompilerElement) node.getParent()) != null) {
                State parentState = node.getState(null);
                if (parentState != null) {
                    state = parentState.clone();
                    break;
                }
            }
            if (state == null)
                state = prototype.clone();
        }
        return state;
    }

    public TagType getTagType() {
        ensureTagType(getParent());
        return tagType;
    }

    @SuppressWarnings("unchecked")
    public List<Node> content() {
        return (List<Node>) super.content();
    }

    /**
     * Returns the factory that creates elements of this class.
     *
     * @return A LineNoElement factory.
     */
    protected DocumentFactory getDocumentFactory() {
        DocumentFactory fact = super.getDocumentFactory();
        if (! (fact instanceof CompilerElementFactory))
            return factory;
        else
            return fact;
    }
}
