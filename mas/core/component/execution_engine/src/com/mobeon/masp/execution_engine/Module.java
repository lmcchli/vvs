package com.mobeon.masp.execution_engine;

import com.mobeon.masp.execution_engine.compiler.CompilerBookkeeping;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.compiler.Product;
import com.mobeon.masp.execution_engine.compiler.State;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEvent;
import com.mobeon.masp.execution_engine.runtime.event.SimpleEventImpl;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeTree;
import com.mobeon.masp.execution_engine.xml.CompilerElement;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Compiled representation of a VXML/CCXML document. One or more Modules build an {@link ApplicationImpl}.
 * @author Mikael Andersson 
 */
public class Module {
    private URI documentURI;
    private Product product;
    private List<SimpleEvent> compilationEvents = new ArrayList<SimpleEvent>();
    private Map<String,Product> productsByName = new HashMap<String, Product>();
    private Map<String, String> documentAttributes = new HashMap<String, String>();
    private State prototype;
    private ModuleCollection parent = null;
    private CompilerBookkeeping bookeeping;


    // Special products can be used by operations for book-keeping. Example:
    // A VXML implementation may use this to register which form that is the
    // first in a document.

    private Map<String,Product> specialProducts = new HashMap<String, Product>();
    private GrammarScopeTree dtmf_grammarTree = null;
    private GrammarScopeTree asr_grammarTree = null;
    public static final String FORM_BEING_COMPILED = "formBeingCompiled";
    public static String FIRST_DIALOG = "firstDialog";
    public static String DIALOG_PRODUCT = "dialogProduct";
    public static String VXML_PRODUCT = "vxmlProduct";
    public static final String DIALOG_TRAMPOLINE_PRODUCT = "executingProduct";
    public static final String CONTEXT_INITIALIZING_PRODUCT = "contextInitializingProduct";

    public Module(URI documentURI) {
        this.documentURI = documentURI;
    }

    public void postEvent(String event, CompilerElement element) {
        compilationEvents.add(new SimpleEventImpl(event, DebugInfo.getInstance(element)));
    }

    public ModuleCollection getParent() {
        return parent;
    }

    public CompilerBookkeeping getBookeeping() {
        return bookeeping;
    }

    public void setBookeeping(CompilerBookkeeping bookeeping) {
        this.bookeeping = bookeeping;
    }

    public void setParent(ModuleCollection parent) {
        this.parent = parent;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }

    public URI getDocumentURI() {
        return documentURI;
    }

    public void registerNamedProduct(Product product) {
        productsByName.put(product.getName(),product);
    }

    public Product getNamedProduct(String name) {
        return productsByName.get(name);
    }

    public List<SimpleEvent> getCompilationEvents() {
        return compilationEvents;
    }

    /**
     * Sets a document level attribute.
     * @param key The key
     * @param value The value
     */
    public void setDocumentAttribute(String key, String value){
        documentAttributes.put(key, value);
    }
    /**
     * Returns a document level attribute.
     * @param key
     * @return the attribute, or null if there was no such attribute.
     */
    public String getDocumentAttribute(String key){
        return documentAttributes.get(key);
    }

    public void setNodeStatePrototype(State prototype) {
        this.prototype = prototype;
    }

    public State getElementStatePrototype() {
        return prototype;
    }


    public void setSpecialProduct(String key, Product value) {
        specialProducts.put(key, value);
    }

    public Product getSpecialProduct(String key){
        return specialProducts.get(key);
    }

    public GrammarScopeTree getDTMFGrammarTree() {
        if(this.dtmf_grammarTree == null) {
            this.dtmf_grammarTree = new GrammarScopeTree();
        }
        return this.dtmf_grammarTree;
    }

    public GrammarScopeTree getASRGrammarTree() {
        if(this.asr_grammarTree == null) {
            this.asr_grammarTree = new GrammarScopeTree();
        }
        return this.asr_grammarTree;
    }

    public void setDTMFGrammarTree(GrammarScopeTree grammarTree) {
        this.dtmf_grammarTree = grammarTree;
    }
    public void setASRGrammarTree(GrammarScopeTree grammarTree) {
        this.asr_grammarTree = grammarTree;
    }
}
