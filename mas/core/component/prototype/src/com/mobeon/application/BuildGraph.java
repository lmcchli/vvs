/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.application;

import com.mobeon.application.vxml.*;
import com.mobeon.application.vxml.Error;

import com.mobeon.application.vxml.Filled;
import com.mobeon.application.vxml.datatypes.Duration;

import com.mobeon.application.vxml.grammar.*;

import com.mobeon.application.graph.*;
import com.mobeon.application.graph.Node;
import com.mobeon.application.util.Expression;
import com.mobeon.executor.DocumentManager;
import com.mobeon.util.ErrorCodes;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.Object;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: MPAZE
 * Date: 2005-feb-17
 * Time: 17:31:41
 */


public class BuildGraph {
    Logger logger = Logger.getLogger("mobeon.com");
    Node tail;
    Node root;
    private Stack endIfStack = new Stack();
    private static final Class cs = BuildGraph.class;

    private Node invokeCompile(Object elem) {

        Class[] types = new Class[1];
        types[0] = elem.getClass();

        try {

            Method method = cs.getDeclaredMethod("compile", types);
            Object[] param = new Object[1];
            param[0] = elem;
            Node node = (Node) method.invoke(this, param);

            return node;
        } catch (NoSuchMethodException e) {
            logger.error("No Compile method for class" + elem.getClass(), e);
        } catch (IllegalAccessException e) {
            logger.error(e);
        } catch (InvocationTargetException e) {
            logger.error(e);
            logger.debug("Failed to locate compile method for class " + elem.getClass());
        }
        return null;
    }


    public Node compileDocument(VXML n) {

        invokeCompile(n);
        return root;
    }

    public Node compile(VXML vxml) {
        VXMLNode vxmlNode = new VXMLNode();
        vxmlNode.setBase(vxml.getBase());
        vxmlNode.setApplication(vxml.getApplication());
        DocumentManager.getInstance().addDocument(vxml.getBase(), vxmlNode);
        root = vxmlNode;
        tail = vxmlNode;
        // No parent since root of document
        VXML.ContentSet set = vxml.getContent();

        Node child = null;
        for (int i = 0; i < set.size(); i++) {
            VXMLContentElement elem = set.get(i);
            child = invokeCompile(elem);
            child.setParent(root);
            vxmlNode.addChild(child);
        }

        ArrayList children = vxmlNode.getChildren();
        // Attach the prompts as siblings
        for (int i = 0; i < children.size() - 1; i++){
            ((Node)children.get(i)).setNextSibling((Node)children.get(i + 1));
        }

        return addEndScopeNode();
    }

    private Node compile(Catch c) {
        CatchNode cn = new CatchNode();
        tail.setNext(cn);
        tail = cn;
        cn.setEvent(c.getEvents());
        ExecutableContentGroupElement.Set set = c.getExecutableContent();

        Node child = null;
        boolean firstIteration = true;
        for (int i = 0; i < set.size(); i++) {
            child = invokeCompile(set.get(i));
            child.setParent(cn);
            if (firstIteration) {
                cn.setExceptionHandlerNode(child);
                firstIteration=false;
            }
        }
        // Set tail to null, in order to terminate the catch clause.
        tail.setNext(null);

        tail = cn;
        return cn;
    }

    private Node compile(Log log) {
        LogNode ln = new LogNode();
        tail.setNext(ln);
        tail = ln;
         ExecutableContentGroupElement.Set set = log.getExecutbleContent();

        Node child = null;
        for (int i = 0; i < set.size(); i++) {
            child = invokeCompile(set.get(i));
            child.setParent(ln);
        }

        return ln;

    }


    private Node compile(RePrompt reprompt) {
        RepromptNode rn = new RepromptNode();

        tail.setNext(rn);
        tail = rn;

        return rn;
    }
    private Node compile(Filled filled) {


        FilledNode fn = new FilledNode();

        tail.setNext(fn);
        tail = fn;

        // todo: implement filled outside of field - nameList != null

        if (filled.getNameList() != null) {
            if(filled.getMode() == null || filled.getMode().equals("all")){ // mode = all is default


            } else if (filled.getMode().equals("any")) {

            } else {
                logger.error("Filled node with illegal mode [" + filled.getMode() + "] \n\tShuting down...");
                System.exit(ErrorCodes.VXML_ERROR);
            }


            logger.error("Filled with namelist not supported yet");
        }
        fn.setMode(filled.getMode());
        ExecutableContentGroupElement.Set set = filled.getExecutableContent();
        Node child;
        for (int i = 0; i < set.size(); i++) {
            logger.debug("Filled: invokeCompiler");
            child = invokeCompile(set.get(i));
            child.setParent(fn);
        }

        addEndScopeNode();


        return fn;
    }

    private Node compile(Form form) {
        FormNode fn = new FormNode();
        fn.setName(form.getId());
        tail.setNext(fn);
        tail = fn;
        Form.ContentSet set = form.getContent();
        Node child;
        for (int i = 0; i < set.size(); i++) {
            child = invokeCompile(set.get(i));
            if (child != null) {
                child.setParent(fn);
                if (child instanceof BlockNode ||
                        child instanceof FieldNode) {  // todo: Should subdialog be added?
                    fn.addChild(child);
                }
            }
        }
        addEndScopeNode();
        return fn;
    }

    private Node compile(Block block) {
        BlockNode bn = new BlockNode();
        bn.setName(block.getName());
        tail.setNext(bn);
        tail = bn;
        ExecutableContentGroupElement.Set set = block.getExecutableContent();
        Node child;
        for (int i = 0; i < set.size(); i++) {
            logger.debug("Block: invokeCompiler");
            child = invokeCompile(set.get(i));
            child.setParent(bn);
            logger.debug("Block: Adding child " + child.getClass() + " to the block");
            bn.addChild(child);
        }
        // Attach the children as siblings
        ArrayList children = bn.getChildren();
        for (int i = 0; i < children.size() - 1; i++) {
            logger.debug("Block: Attaching siblings");
            ((Node) children.get(i)).setNextSibling((Node) children.get(i + 1));
        }
        Node endscope = addEndScopeNode();
        ((Node) children.get(children.size() - 1)).setNextSibling(endscope);
        return bn;

    }

    private Node compile(SubDialog subdialog)  {
        SubdialogNode sdn = new SubdialogNode();
        tail.setNext(sdn);
        tail = sdn;
        sdn.setName(subdialog.getName());
        sdn.setCond(subdialog.getCond());
        sdn.setExpr(subdialog.getExpression());
        sdn.setSrc(subdialog.getSrc());
        sdn.setSrcExpression(subdialog.getSrcExpression());

        SubDialog.ContentSet set = subdialog.getContent();
        Node child;
        for (int i = 0; i < set.size(); i++) {
            logger.debug("Subdialog: invokeCompiler");
            child = invokeCompile(set.get(i));
            child.setParent(sdn);
            if (child instanceof ParamNode)  {
                ParamNode pn = (ParamNode) child;
                sdn.addParam(pn.getName(),pn.getExpr());
            }
            else {
                logger.debug("Block: Adding child " + child.getClass() + " to the block");
                sdn.addChild(child);
            }
        }
        return sdn;
    }

    private Node compile(Script script) {
        ScriptNode sn = new ScriptNode();

        sn.setBody(script.getBody());
        sn.setSrc(script.getSrc());
        String src = null;
        if ((src = sn.getSrc()) != null) {
            sn.loadSrc(src);
        }


        tail.setNext(sn);
        tail = sn;

        return sn;
    }

    private Node compile(Prompt prompt) {
        PromptNode pn = new PromptNode();

        tail.setNext(pn);
        tail = pn;
        if (prompt.getCount() > 0)
            pn.setCount(prompt.getCount());
        Prompt.Set set = prompt.getExcutableContent();
        for (int i = 0; i < set.size(); i++) {
            Node node = invokeCompile(set.get(i));
            node.setParent(pn);
        }
        return pn;
    }

    private Node compile(Audio audio) {
        AudioNode an = new AudioNode();

        tail.setNext(an);
        tail = an;
        an.setSrc(audio.getSrc());
        an.setExpr(audio.getExpr());
        Audio.Set set = audio.getExecutableContent();
        for (int i = 0; i < set.size(); i++) {
            Node node = invokeCompile(set.get(i));
            node.setParent(an);
        }

        return an;
    }

    private Node compile(If _if) {
        IfNode in = new IfNode();

        tail.setNext(in);
        tail = in;

        // Insert the condition in the IfNode
        in.setCond(_if.getCondition());

        // Get the set of child nodes
        If.Set set = _if.getExecutableContent();
        // Create a common EndScope node, to be attached at the end of
        // each true/false subtree.
        EndIfNode endIf = new EndIfNode();

        // This is a shitme-baby, idiots implementation, late-at-night-hacking kind of solution
        // TODO: Should most likely be re-written in a more readable fashion

        for (int i = 0; i < set.size(); i++) {
            ExecutableContentGroupElement e = set.get(i);
            if (e instanceof ElseIf) {  // Found start of ElseIf branch
                logger.debug("ElseIf: Found an ELSEIF statement (cond: " + ((ElseIf) e).getCondition().getCond() + ")");
                IfNode parentIf = null;
                parentIf = (IfNode) tail; // Retrieve the If parent node
                IfNode inSub = new IfNode(); // Create the new sub-If node
                parentIf.setElse(inSub);    // And insert it as the else-child in the parent
                inSub.setCond(((ElseIf) e).getCondition()); // Get the condition for the elseif node
                tail = inSub;  // Relocate tail to be the new sub-if
                // Iterate the rest of the set until an Elseif or an Else is encountered
                i++;
                if (i < set.size()) {
                    ExecutableContentGroupElement esub = set.get(i);
                    boolean initialLoop = true;
                    while (i < set.size() && !(esub instanceof ElseIf || esub instanceof Else)) {
                        // If we found a new If, push the EndIf node to the stack for future usage.
                        if (esub instanceof If)
                            endIfStack.push(endIf);
                        Node child = invokeCompile(esub);
                        child.setParent(inSub);
                        // If in the first loop, add the node as child to the
                        // sub-if node. If NOT in the first loop, the tail structure
                        // will hold this shit together.
                        if (initialLoop) {
                            inSub.setBody(child);
                            initialLoop = false;
                        }
                        i++;
                        if (i < set.size()) {
                            esub = set.get(i);
                        }
                    }
                    i--; // Push back the ElseIf or Else node in the set
                }
                else {
                    // Empty set
                    inSub.setElse(endIf);
                }
                tail.setNext(endIf); // Hang in the endIf node at the end of this subtree
                tail = inSub; // Again, set the sub-If node as tail.
            } else if (e instanceof Else) {  // Found start of Else branch
                logger.debug("Else: Found an ELSE statement");
                IfNode inSub = (IfNode) tail;  // Retrieve the previous If node, and use it as tail
                tail = inSub;
                // Iterate the rest of the set.
                i++;
                if (i < set.size()) {
                    ExecutableContentGroupElement esub = set.get(i);
                    boolean initialLoop = true;
                    while (i < set.size()) {
                        // If we found a new If, push the EndIf node to the stack for future usage.
                        if (esub instanceof If)
                            endIfStack.push(endIf);
                        Node child = invokeCompile(esub);
                        child.setParent(inSub);
                        // If in the first loop, add the node as child to the
                        // sub-if node. If NOT in the first loop, the tail structure
                        // will hold this shit together.
                        if (initialLoop) {
                            inSub.setElse(child);
                            initialLoop = false;
                        }
                        i++;
                        if (i < set.size()) {
                            esub = set.get(i);
                        }
                    }
                    i--; // Push back the ElseIf or Else node in the set
                }
                else {
                    // Empty set
                    inSub.setElse(endIf);
                }
                tail.setNext(endIf);
                tail = inSub;
            } else {  // Found start of If branch
                ExecutableContentGroupElement esub = set.get(i);
                logger.debug("If: Found an If statement (cond : " + in.getCond().getCond() + ")");

                boolean initialLoop = true;
                // Iterate the rest of the set until an Elseif or an Else is encountered
                while (i < set.size() && !(esub instanceof ElseIf || esub instanceof Else)) {
                    Node child = invokeCompile(esub);
                    child.setParent(in);
                    // If in the first loop, add the node as child to the
                    // sub-if node. If NOT in the first loop, the tail structure
                    // will hold this shit together.
                    if (initialLoop) {
                        in.setBody(child);
                        initialLoop = false;
                    }
                    i++;
                    if (i < set.size()) {
                        esub = set.get(i);
                    }
                }
                tail.setNext(endIf);
                tail = in;
                i--; // Push back the ElseIf or Else node in the set
            }
        }

        // Tie in all endIfs
        Node parentEndIf = null;
        while (!endIfStack.empty() &&  (parentEndIf = (EndIfNode) endIfStack.pop()) != null){
            endIf.setNext(parentEndIf);
        }
        tail = endIf;
        return in; // Finally Done!
    }

    private Node compile(Bread bread) {
        BreadNode bn = new BreadNode();

        bn.setData(bread.getText());
        // todo: handle them evil value nodes somehow
        tail.setNext(bn);
        tail = bn;
        return bn;
    }

    private Node compile(Var var) {
        VarNode vn = new VarNode(var.getName(), var.getExpression());
        tail.setNext(vn);
        tail = vn;
        return vn;
    }


    private Node compile(Throw _throw) {
        ThrowNode tn = new ThrowNode();
        tail.setNext(tn);
        tail = tn;
        tn.setEvent(_throw.getEventString());
        return tn;
    }

    private Node compile(Link link) {
        LinkNode ln = new LinkNode();

        return ln;
    }
    private Node compile(Value value) {
        ValueNode vn = new ValueNode();
        Expression e = null;
        if ((e = value.getExpr()) != null)
            vn.setExpr(e);
        tail.setNext(vn);
        tail = vn;
        return vn;
    }

      private Node compile(Param param) {
        ParamNode pn = new ParamNode();
        pn.setName(param.getName());
        pn.setExpr(param.getExpression());
        // Do not hang in the params at the end of teh three,
        // place them in the list of params in SubdialogNode and ObjectNode
        // instead.
        return pn;
    }

    private Node compile(Return _return) {
        ReturnNode rn = new ReturnNode();
        tail.setNext(rn);
        tail = rn;

        rn.setEvent(_return.getEvent());
        rn.setEventExpr(_return.getEventExpression());
        rn.setMessage(_return.getMessage());
        rn.setMessageExpr(_return.getMessageExpression());
        NameListAttributedElement.List l= _return.getNameList();
        for (int i = 0; i < l.size(); i++){
            rn.addName((String) l.get(i));
        }

        return rn;
    }
    private Node compile(Clear clear) {
        ClearNode cn = new ClearNode();
        cn.setList(clear.getNameList().getList());
        tail.setNext(cn);
        tail = cn;
        return cn;
    }

    private Node compile(Exit exit) {
        ExitNode en = new ExitNode();
        en.setExpr(exit.getExpression());


        tail.setNext(en);
        tail = en;
        Exit.List list = exit.getNameList();
        List l = en.getNamelist();
        for (int i = 0; i < list.size(); i++) {
            l.add(list.get(i));
        }

        return en;
    }

    private Node compile(GoTo _goto) {
        GotoNode gn = new GotoNode();
        if (_goto.getNext() != null)
            gn.setURI(_goto.getNext());
        if (_goto.getExpr() != null)
            gn.setExpr(_goto.getExpr());
        tail.setNext(gn);
        tail = gn;
        return gn;
    }

    private Node compile(Assign assign) {
        AssignNode an = new AssignNode();

        an.setExpr(assign.getExpression());
        an.setName(assign.getName());
        tail.setNext(an);
        tail = an;

        return an;
    }
    private Node compile(Field field) {
        FieldNode fn = new FieldNode();
        fn.setName(field.getName());
        fn.setExpr(field.getExpression());
        if (field.getCond() != null)
            fn.setCond(field.getCond());
        fn.setModal(field.isModal());
        fn.setSlotname(field.getSlot());
        fn.setType(field.getType());
        tail.setNext(fn);
        tail = fn;


        Field.ContentSet set = field.getContent();
        Node child = null;
        ArrayList prompts = null;

        for (int i = 0; i < set.size(); i++) {
            child = invokeCompile(set.get(i));
            child.setParent(fn);

            if (child instanceof PromptNode) {
                ((PromptNode) child).setInField(true);
                fn.addPrompt((PromptNode) child);
            }
        }

        prompts = fn.getPrompts();
        ValueRetrievalNode vrn = null;
        PromptNode prompt = null;
        if (prompts.size() > 0) {
            vrn = new ValueRetrievalNode();
            vrn.setVarName(field.getName());
            prompt = (PromptNode) prompts.get(0); // todo: which of the prompts to fecth timeout from?
            vrn.setTimeout(prompt.getTimeout());
            tail.setNext(vrn);
            tail = vrn;
        }
        // Add the EndField node to the tail
        Node endField = new EndField();
        tail.setNext(endField);
        tail = tail.getNext();


        // Attach the prompts as siblings
        for (int i = 0; i < prompts.size() - 1; i++) {
            ((Node) prompts.get(i)).setNextSibling((Node) prompts.get(i + 1));
        }
        if (vrn != null)
            ((Node) prompts.get(prompts.size() - 1)).setNextSibling(vrn);

        FormContentElement elem;
        Node sibling = null;
        if ((elem = field.getNextSibling()) != null) {
            sibling = invokeCompile(elem);
        }


        if (sibling != null) {
            fn.setNextSibling(sibling);
            //tail.setNext(sibling);
            //tail = sibling;
        }

        return fn;
    }

    private Node compile(Record record) {
        RecordNode rn = new RecordNode();
        rn.setName(record.getName());
        rn.setExpr(record.getExpression());
        if (record.getCond() != null)
            rn.setCond(record.getCond());
        rn.setModal(record.isModal());
        rn.setBeep(record.isBeep());
        rn.setDtmfterm(record.isDtmfTerm());
        int factor = 1;
        if (record.getMaxTime() != null) {
            if (record.getMaxTime().getMesurement() == Duration.SECONDS)
                factor = 1000;
            rn.setMaxtime(record.getMaxTime().getLength() * factor);
        }
        if (record.getFinalSilence() != null) {
            if (record.getFinalSilence().getMesurement() == Duration.SECONDS)
                factor = 1000;
            rn.setFinalsilencetime(record.getFinalSilence().getLength() * factor);
        }
        tail.setNext(rn);
        tail = rn;


        Record.ContentSet set = record.getContent();
        Node child = null;
        ArrayList prompts = null;

        for (int i = 0; i < set.size(); i++) {
            child = invokeCompile(set.get(i));
            child.setParent(rn);

            if (child instanceof PromptNode) {
                ((PromptNode) child).setInField(true);
                rn.addPrompt((PromptNode) child);
            }
        }

        prompts = rn.getPrompts();

        RecordMsgNode rmn = new RecordMsgNode();
        rmn.setVarName(record.getName());
        rmn.setMaxtimeout(rn.getMaxtime());
        rmn.setMimeType(rn.getMimetype());
        rmn.setDtmfterm(rn.isDtmfterm());
        tail.setNext(rmn);
        tail = rmn;
        // Add the EndField node to the tail
        Node endRecord = new EndRecord();
        tail.setNext(endRecord);
        tail = tail.getNext();


        // Attach the prompts as siblings
        for (int i = 0; i < prompts.size() - 1; i++) {
            ((Node) prompts.get(i)).setNextSibling((Node) prompts.get(i + 1));
        }
        ((Node) prompts.get(prompts.size() - 1)).setNextSibling(rmn);


        FormContentElement elem;
        Node sibling = null;
        if ((elem = record.getNextSibling()) != null) {
            sibling = invokeCompile(elem);
        }


        if (sibling != null) {
            tail.setNext(sibling);
            tail = sibling;
        }

        return rn;
    }


    private Node compile(XMLGrammar grammar) {
        GrammarNode gn = new GrammarNode();
        Rule rule = grammar.getRule();
        for (int i = 0; i < rule.content.size(); i++) {
            RuleContent rc = rule.content.get(i);
            if (rc instanceof Item) {
                gn.addRule(((Item) rc).getBread());
            }
        }
        Node parent = tail.getParent();
        while (parent != null &&
                (!(parent instanceof FieldNode) &&
                !(parent instanceof FormNode))) {
            parent = parent.getParent();
        }
        Node child = parent.getNext();
        parent.setNext(gn);
        gn.setNext(child);
        gn.setParent(parent);
        return gn;
    }

    private Node addEndScopeNode() {
        EndScopeNode endS = new EndScopeNode();
        tail.setNext(endS);
        tail = endS;

        return endS;
    }
}



