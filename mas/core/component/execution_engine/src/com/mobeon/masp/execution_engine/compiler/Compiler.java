/*
 * Copyright (c) 2006 Mobeon AB All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.compiler;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ModuleCollection;
import com.mobeon.masp.execution_engine.ccxml.compiler.CCXMLCompilerDispatcher;
import com.mobeon.masp.execution_engine.voicexml.VoiceXMLCompilerDispatcher;
import com.mobeon.masp.execution_engine.voicexml.CompilerMacros;
import com.mobeon.masp.execution_engine.voicexml.compiler.VXMLCompilerBookkeeping;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeTree;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeNode;
import com.mobeon.masp.execution_engine.voicexml.grammar.Grammar;
import com.mobeon.masp.execution_engine.xml.CompilerElement;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.Text;
import org.dom4j.tree.DefaultCDATA;
import org.dom4j.tree.DefaultText;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class Compiler {
    private static ILogger log = ILoggerFactory.getILogger(Compiler.class);

    public static final CompilerPass VXML_CODEGEN =
            new CodeGeneratingPass(VoiceXMLCompilerDispatcher.getInstance());
    public static final CompilerPass CCXML_CODEGEN =
            new CodeGeneratingPass(CCXMLCompilerDispatcher.getInstance());

    public static final List<CompilerPass> VXML_PASSES =
            Collections.unmodifiableList(Arrays.asList(new CompilerPass[]{new VoiceXMLPrecompiler(), VXML_CODEGEN}
            ));
    public static final List<CompilerPass> CCXML_PASSES =
            Collections.unmodifiableList(Arrays.asList(new CompilerPass[]{CCXML_CODEGEN}));

    public static final CompilerConfiguration VXML_CONFIG = new CompilerConfiguration(VXML_PASSES, new State(), new VXMLCompilerBookkeeping());
    public static final CompilerConfiguration CCXML_CONFIG = new CompilerConfiguration(CCXML_PASSES, new State(), new CompilerBookkeeping());


    public static interface CompilerPass {
        public List<Node> compile(Module module, Product parent, List<Node> list);

        public Product compile(Module module, Product parent, CompilerElement element, List<Node> containingContent);

        public void compile(Module module, Product parent, Text text, List<Node> containingContent);
    }

    public static class CodeGeneratingPass implements CompilerPass {

        CompilerDispatcher compilerDispatcher;

        public CodeGeneratingPass(CompilerDispatcher cd) {
            this.compilerDispatcher = cd;
        }

        public Product compile(Module module, Product parent, CompilerElement element, List<Node> containingContent) {
            return compilerDispatcher.dispatch(element.getName()).compile(module, this, parent, element, containingContent);
        }

        public void compile(Module module, Product parent, Text text, List<Node> containingContent) {
            compilerDispatcher.dispatchText().compile(module, parent, text);
        }

        public List<Node> compile(Module module, Product parent, List<Node> list) {
            for (Object o : list) {
                Node node = (Node) o;
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    CompilerElement element = (CompilerElement) node;
                    compile(module, parent, element, list);
                } else if (node.getNodeType() == Node.TEXT_NODE) {
                    Text text = (Text) node;
                    compile(module, parent, text, list);
                } else if (node.getNodeType() == Node.CDATA_SECTION_NODE) {
                    DefaultCDATA cdata = (DefaultCDATA) node;
                    String str = ((DefaultCDATA) node).getText();
                    Text text = new DefaultText(str);
                    compile(module, parent, text, list);
                }
            }
            return list;
        }
    }

    public static class VoiceXMLPrecompiler implements CompilerPass {

        private void transformCatchShorthands(Node node) {
            Element elem = (Element) node;
            String node_name = elem.getName();
            if (node_name.equals(Constants.VoiceXML.NOINPUT)) {
                elem.setName(Constants.VoiceXML.CATCH);
                elem.addAttribute(Constants.VoiceXML.EVENT, Constants.VoiceXML.NOINPUT);

            } else if (node_name.equals(Constants.VoiceXML.HELP)) {
                elem.setName(Constants.VoiceXML.CATCH);
                elem.addAttribute(Constants.VoiceXML.EVENT, Constants.VoiceXML.HELP);

            } else if (node_name.equals(Constants.VoiceXML.NOMATCH)) {
                elem.setName(Constants.VoiceXML.CATCH);
                elem.addAttribute(Constants.VoiceXML.EVENT, Constants.VoiceXML.NOMATCH);

            } else if (node_name.equals(Constants.VoiceXML.ERROR)) {
                elem.setName(Constants.VoiceXML.CATCH);
                elem.addAttribute(Constants.VoiceXML.EVENT, Constants.VoiceXML.ERROR);
            }
        }

        public Product compile(Module module, Product vars, CompilerElement element, List<Node> containingContent) {
            //TODO: Tom implementation
            return null;
        }

        public void compile(Module module, Product parent, Text text, List<Node> containingContent) {
            //TODO: Tom implementation
        }

        // scan for grammar record and catch short hand elements
        private ArrayList<CompilerElement> traverseNodes(List<Node> nodes, Module module) {

            ArrayList<CompilerElement> grammarNodes = null;

            for (Node node : nodes) {

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String node_name = node.getName();
                    if (node_name == null) continue;
                    //needs to handle record with termchar set to true special
                    if (node_name.equals(Constants.VoiceXML.RECORD)) {
                        Element record = (Element) node;

                        if (record.attributeValue(Constants.VoiceXML.DTMFTERM) != null &&
                                record.attributeValue(Constants.VoiceXML.DTMFTERM).equals("true")) {

                            GrammarScopeTree tree = module.getDTMFGrammarTree();
                            tree.setAlwaysMatchGrammar(record);
                        }
                    } else if (node_name.equals(Constants.VoiceXML.GRAMMAR)) {

                        if (grammarNodes == null)
                            grammarNodes = new ArrayList<CompilerElement>();
                        grammarNodes.add((CompilerElement) node);


                    }
                    // handle them catch shorthand nodes
                    transformCatchShorthands(node);
                }

            } // end for

            return grammarNodes;
        }

        private Element findParent(List<Node> nodes) {
            if (nodes.size() == 0) {
                return null;
            }
            for (Node n : nodes) {
                Element parent = n.getParent();
                if (parent  != null) {
                    return parent;
                }
            }
            return null;
        }

        /**
         * @param module
         * @param parent
         * @param nodes
         * @return
         */

        public List<Node> compile(Module module, Product parent, List<Node> nodes) {
            if (nodes == null) return null;


            ArrayList<CompilerElement> grammarNodes = null;
            Element parentElem = null;


            parentElem = findParent(nodes);
            // first scan for grammars and catch short hands
            grammarNodes = traverseNodes(nodes, module);

            // handle grammars
            GrammarScopeNode dtmf_curr = null;
            GrammarScopeNode asr_curr = null;
            // If a vxml tag lacks a grammar tag a virtual grammar node is created.
            if (grammarNodes == null && parentElem != null && Constants.VoiceXML.VXML.equals(parentElem.getName())) {
                //dtmf_curr = module.getGrammarTree().getCurrent(); // do not need to back track from the root
                module.getDTMFGrammarTree().createVirtualDocRootGrammar(parentElem, module.getDocumentURI()); // TODO optimize this
                module.getASRGrammarTree().createVirtualDocRootGrammar(parentElem, module.getDocumentURI());
                if (log.isDebugEnabled())
                    log.debug("Creating Virtual document root Grammar for " + module.getDocumentURI().toString());
            } else if (grammarNodes != null) {
                nodes.removeAll(grammarNodes);
                GrammarScopeTree dtmf_tree = module.getDTMFGrammarTree();
                GrammarScopeTree asr_tree = module.getASRGrammarTree();

                dtmf_curr = dtmf_tree.getCurrent();
                asr_curr = asr_tree.getCurrent();

                dtmf_tree.handleGrammars(CompilerMacros.getGrammarsPerMode(grammarNodes, Grammar.InputMode.DTMF), parentElem, module.getDocumentURI());
                asr_tree.handleGrammars(CompilerMacros.getGrammarsPerMode(grammarNodes, Grammar.InputMode.VOICE), parentElem, module.getDocumentURI());
                // if parent is a vxml node and we do not have a ParentGrammarScopeNode needs
                // to be created as a virutal parent
            }
            handleApplicationGrammar(module, parentElem);

            if(log.isDebugEnabled())
                log.debug("Module " + CompilerMacros.getFileName(module.getDocumentURI()) + " grammar:" + module.getDTMFGrammarTree().toString());

            // recursive call
            for (Node node : nodes) {
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    CompilerElement celem = (CompilerElement) node;
                    List<Node> list = celem.content();
                    compile(module, parent, list);
                }
            }
            // set the back current ref
            if (dtmf_curr != null)
                module.getDTMFGrammarTree().setCurrent(dtmf_curr);
            if (asr_curr != null) {
                module.getASRGrammarTree().setCurrent(asr_curr);
            }

            return nodes;
        }

        private void handleApplicationGrammar(Module module, Element parent) {
            // only need to do stuff if parent is a vxml root element
            if (parent == null || !Constants.VoiceXML.VXML.equals(parent.getName())) return;


            VXMLCompilerBookkeeping bookkeeping = (VXMLCompilerBookkeeping) module.getBookeeping();

            String uri = parent.attributeValue(Constants.VoiceXML.APPLICATION);

            if (uri == null) { // module is an application module
                GrammarScopeNode dtmf_gnode = module.getDTMFGrammarTree().getDocumentScopeGrammars();
                GrammarScopeNode asr_gnode = module.getASRGrammarTree().getDocumentScopeGrammars();

                bookkeeping.addApplicationModule(module.getDocumentURI().toString(), module);
                List<Module> list = bookkeeping.getUnresolvedLeafModules(module.getDocumentURI().toASCIIString());
                if (dtmf_gnode != null) { // no app grammar to handle
                    // register root app for later compiled documents
                    if (list != null) {
                        for (Module m : list) {
                            m.getDTMFGrammarTree().hangInApplicationGrammar(dtmf_gnode);
                        }
                    }
                }
                if (asr_gnode != null) {
                    if (list != null) {
                        for (Module m : list) {
                            m.getASRGrammarTree().hangInApplicationGrammar(dtmf_gnode);
                        }
                    }
                }

            } else { // module is a leaf document
                URI base = module.getDocumentURI();
                URI newURI = base.resolve(uri);
                Module root_doc = bookkeeping.getApplicationModule(newURI.toString()); // returns null
                if (root_doc == null) { // root doc not compiled yet
                    bookkeeping.addUnresolvedLeafModule(uri, module);
                    return;
                } else {
                    GrammarScopeNode dtmf_gnode = root_doc.getDTMFGrammarTree().getDocumentScopeGrammars();
                    GrammarScopeNode asr_gnode = root_doc.getASRGrammarTree().getDocumentScopeGrammars();
                    if (dtmf_gnode != null)  // no application grammar exist
                        module.getDTMFGrammarTree().hangInApplicationGrammar(dtmf_gnode);
                    if(asr_gnode != null)
                        module.getASRGrammarTree().hangInApplicationGrammar(asr_gnode);
                }
            }
        }


    }

    public static void compile(Module module, CompilerPass compilerPass, Product parent, List<Node> list) {
        compilerPass.compile(module, parent, list);
    }

    public static Module compileDocument(CompilerConfiguration compilerConfig, Document doc, URI documentURI, ModuleCollection collection) {
        Module module = new Module(documentURI);
        module.setParent(collection);
        CompilerElement root = (CompilerElement) doc.getRootElement();
        List<Node> list = new ArrayList<Node>();
        list.add(root);
        module.setNodeStatePrototype(compilerConfig.prototype);
        module.setBookeeping(compilerConfig.bookkeeping);

        for (CompilerPass pass : compilerConfig.compilerPasses) {
            pass.compile(module, null, list);
        }
        return module;
    }
}
