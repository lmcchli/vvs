package com.mobeon.application.vxml;


import org.w3.x2001.vxml.*;
import org.w3.x2001.vxml.Speak;
import org.w3.x2001.vxml.impl.FormDocumentImpl;
import org.w3.x2001.vxml.impl.BooleanDatatypeImpl;
import org.w3.x2001.vxml.impl.DurationDatatypeImpl;
import org.apache.xmlbeans.*;
import org.apache.xmlbeans.impl.values.XmlAnyUriImpl;
import org.apache.log4j.Logger;


import javax.xml.namespace.QName;
import java.util.Set;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import com.mobeon.application.util.FileTraverser;
import com.mobeon.application.util.Cond;
import com.mobeon.application.vxml.grammar.Grammar;
import com.mobeon.application.vxml.grammar.XMLGrammar;
import com.mobeon.application.util.Expression;
import com.mobeon.util.ErrorCodes;


/**
 * Created by IntelliJ IDEA.
 * User: MPAZE
 * Date: 2005-feb-09
 * Time: 17:00:27
 */
public class Transformer {

    public static final VXML getStartDocument(VXML[] docs) {
        // todo: make sanity check - only one root application allowed
        for (int i = 0; i < docs.length; i++) {
            if (docs[i].getApplication() == null) {
                return docs[i];
            }
        }
        return null;
    }

    private static final Logger logger = Logger.getLogger("com.mobeon");

    public static final VXML[] transformPath(File path)
            throws IOException {
        XmlOptions validateOptions = new XmlOptions();
        ArrayList errorList = new ArrayList();
        validateOptions.setErrorListener(errorList);

        LateBinder lateBinder = new LateBinder(); // todo: actually call the methods when resolving uri-elements and late bound references.
        File[] f = FileTraverser.findFiles(path, new String[]{".vxml"}, true); // todo: handle files more specific.
        VXML[] ret = new VXML[f.length];
        for (int i = 0; i < f.length; i++) {
            // String uri = f[i].getAbsolutePath(); // todo
            String uri = f[i].getName(); // todo
            logger.debug("Begin " + uri);

            VxmlDocument vBean = null;
            try {
                logger.debug("#### " + f[i].getCanonicalPath());
                vBean = VxmlDocument.Factory.parse(f[i]);
                vBean.getVxml().setBase(uri); // lets hang loose
                if (!vBean.validate(validateOptions)) {
                    logger.error("Invalid VXML");

                    for (int idx = 0; idx < errorList.size(); idx++) {
                        XmlError error = (XmlError) errorList.get(idx);

                        System.out.println("\n");
                        System.out.println("Message: " + error.getMessage() + "\n");
                        System.out.println("Location of invalid XML: " +
                                error.getCursorLocation().xmlText() + "\n");
                    }

                    System.exit(ErrorCodes.VXML_ERROR);
                }
            } catch (XmlException e) {
                logger.error("Cannot parse " + f[i].getCanonicalPath() + "\n\n" + e);  //To change body of catch statement use File | Settings | File Templates.
                System.exit(ErrorCodes.VXML_ERROR);
            }
            ret[i] = transformElement(vBean.getVxml(), uri, lateBinder);

            logger.debug("Finished " + uri);
        }
        return ret;
    }

    public static final VXML transformElement(VxmlDocument.Vxml vBean, String uri, LateBinder lateBinder) {
        VXML vxml = new VXML();

        if (!vBean.validate()) {
            logger.error("VXML node not valid W3C vocieXML");
        }


        logger.debug("Transforming a VXML node with uri " + uri);

        VXML.ContentSet cs = vxml.getContent();
        XmlCursor cursor = vBean.newCursor();
        vxml.setApplication(vBean.getApplication());
        vxml.setBase(vBean.getBase());
        vxml.setLang(vBean.getLang());


        cursor.toFirstChild();

        do {
            if (cursor.currentTokenType().intValue() == XmlCursor.TokenType.START.intValue()) {
                XmlObject obj = cursor.getObject();

                if (obj instanceof FormDocument.Form)
                    cs.add(transformElement((FormDocument.Form) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof ErrorDocument)
                    cs.add(transformElement((ErrorDocument) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof LinkDocument.Link)
                    cs.add(transformElement((LinkDocument.Link) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof HelpDocument)
                    cs.add(transformElement((HelpDocument) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof NoinputDocument)
                    cs.add(transformElement((NoinputDocument) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof NomatchDocument)
                    cs.add(transformElement((NomatchDocument) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof PropertyDocument.Property)
                    cs.add(transformElement((PropertyDocument.Property) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof ScriptDocument.Script)
                    cs.add(transformElement((ScriptDocument.Script) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof VarDocument.Var)
                    cs.add(transformElement((VarDocument.Var) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof CatchDocument.Catch)
                    cs.add(transformElement((CatchDocument.Catch) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else
                    logger.error("VXML node with illegal node type" + obj.getClass());
            }
        } while (cursor.toNextSibling());
        return vxml;
    }

    public static final Var transformElement(VarDocument.Var vBean, String uri, LateBinder lateBinder) {

        Var var = new Var();

        var.setExpression(newExpression(vBean.getExpr()));
        var.setName(vBean.getName());
        logger.debug("Transforming a Var node [" + var.getName() + "]");
        return var;
    }

       public static final Param transformElement(ParamDocument.Param vBean, String uri, LateBinder lateBinder) {

        Param par = new Param();

        par.setExpression(newExpression(vBean.getExpr()));
        par.setName(vBean.getName());
        logger.debug("Transforming a Param node [" + par.getName() + "]");
        return par;
    }

    public static final GoTo transformElement(GotoDocument.Goto gBean, String uri, LateBinder lateBinder) {

        GoTo _goto = new GoTo();

        _goto.setExpr(newExpression(gBean.getExpr()));
        String s;

        String e;

        if ((e = gBean.getExpr()) != null) {
            _goto.setExpr(new Expression(e));
        } else if ((e = gBean.getExpritem()) != null) {
            _goto.setExpr(new Expression(e));
        }

        if ((s = gBean.getNext()) != null) {
            _goto.setNext(s);
        } else if ((s = gBean.getNextitem()) != null) {
            _goto.setNext(s);
        }


        logger.debug("Transforming a GoTo node [" + _goto.getNext() + "]");
        return _goto;
    }

    public static final SubDialog transformElement(SubdialogDocument.Subdialog sdBean, String uri, LateBinder lateBinder) {

        SubDialog subdialog = new SubDialog();
        String s;

        String e;
        subdialog.setCond(newCond(sdBean.getCond()));
        subdialog.setName(sdBean.getName());
        logger.debug("Transforming a SubDialog node [" + subdialog.getName() + "]");

        if ((e = sdBean.getExpr()) != null) {
            subdialog.setExpression(new Expression(e));
        }

        if ((s = sdBean.getSrc()) != null) {
            subdialog.setSrc(s);
        } else if ((s = sdBean.getSrcexpr()) != null) {
            subdialog.setSrcExpression(s);
        }

        XmlCursor cursor = sdBean.newCursor();
        SubDialog.ContentSet cs = subdialog.getContent();

        if (!cursor.toFirstChild()) {
            logger.debug("SubdialogNode " + sdBean.getName() + " empty");
            return subdialog;
        }

        do {
            if (cursor.currentTokenType().intValue() == XmlCursor.TokenType.START.intValue()) {
                XmlObject obj = cursor.getObject();
                if (obj instanceof org.w3.x2001.vxml.Audio)
                    cs.add(transformElement((org.w3.x2001.vxml.Audio) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof CatchDocument.Catch)
                    cs.add(transformElement((CatchDocument.Catch) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof ErrorDocument)
                    cs.add(transformElement((ErrorDocument) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof FilledDocument.Filled)
                    cs.add(transformElement((FilledDocument.Filled) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof ParamDocument.Param)
                    cs.add(transformElement((ParamDocument.Param) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof PropertyDocument.Property)
                    cs.add(transformElement((PropertyDocument.Property) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof Speak) // Prompt extends Speak
                    cs.add(transformElement((Speak) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof ValueDocument.Value)
                    cs.add(transformElement((ValueDocument.Value) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else
                    logger.error("Form node with illegal node type" + obj.getClass());
            }
        } while (cursor.toNextSibling());


        return subdialog;
    }

    public static final Script transformElement(ScriptDocument.Script sBean, String uri, LateBinder lateBinder) {
        logger.debug("Transforming a Script Node");

        Script script = new Script();

        XmlCursor cursor = sBean.newCursor();
        script.setSrc(sBean.getSrc());

        StringBuffer buff = new StringBuffer("");
        cursor.toNextToken();
        while (cursor.isText()) {
            buff.append(cursor.getChars());
            cursor.toNextToken();
        }
        script.setBody(buff.toString()); // todo: add script body function - no pun intended

        return script;
    }

    public static final Property transformElement(PropertyDocument.Property pBean, String uri, LateBinder lateBinder) {
        Property prop = new Property();

        return prop;
    }

    public static final NoMatch transformElement(NomatchDocument dBean, String uri, LateBinder lateBinder) {
        // todo: add support for GOTO and RETURN
        return new NoMatch();
    }

    public static final NoInput transformElement(NoinputDocument nBean, String uri, LateBinder lateBinder) {
        // todo: add support for GOTO  and RETURN
        return new NoInput();
    }

    public static final Help transformElement(HelpDocument hBean, String uri, LateBinder lateBinder) {
        // TODO: add support for GOTO , RETURN and THROW
        return new Help();
    }


    public static final RePrompt transformElement(RepromptDocument.Reprompt rBean) {
        RePrompt reprompt = new RePrompt();

        // reprompt has no children and no attributes
        return reprompt;
    }
    public static final Link transformElement(LinkDocument.Link lBean, String uri, LateBinder lateBinder) {
        Link link = new Link();

        link.setDtmf(lBean.getDtmf());
        link.setEvent(lBean.getEvent());
        link.setEventexpr(newExpression(lBean.getEventexpr()));
        link.setExpr(newExpression(lBean.getExpr()));
        link.setMessage(lBean.getMessage());
        link.setMessageexpr(newExpression(lBean.getMessageexpr()));
        link.setNext(lBean.getNext());
        
        return link;
    }

    public static final Block transformElement(BlockDocument.Block bBean, String uri, LateBinder lateBinder) {
        Block block = new Block();
        ExecutableContentGroupElement.Set set = block.getExecutableContent();
        block.setName(bBean.getName());

        logger.debug("Transformning a Block Node [" + block.getName() + "]");


        block.setCond(newCond(bBean.getCond()));


        block.setExpression(newExpression(bBean.getExpr()));
        XmlCursor cursor = bBean.newCursor();
//        if(!cursor.toFirstChild()) {
//            logger.debug("Empty Block found");
//            String s = cursor.getTextValue();
//            logger.debug("TDADA " + s.trim());
//            set.add(new Bread(s));
//        }
        cursor.toNextToken();
        XmlObject o = cursor.getObject();
        if (o instanceof RestrictedVariableNameDatatype) { // This should trigger "put in scope" action

            logger.debug("Transforming Block name" + bBean.getName() + " to scope");
            cursor.toNextToken();
        }
        do {
            if (cursor.isText()) {
                String ss = cursor.getChars();
                if (!ss.trim().equals("")) {
                    logger.debug("Adding text data [" + ss.trim() + "]");
                    set.add(new Bread(ss.trim()));
                }
            } else {
                XmlObject obj = cursor.getObject();
                if (obj != null) {
                    if (obj instanceof LogDocument.Log)
                        set.add(transformElement((LogDocument.Log) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                    else if (obj instanceof Speak)
                        set.add(transformElement((Speak) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                    else if (obj instanceof IfDocument.If)
                        set.add(transformElement((IfDocument.If) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                    else if (obj instanceof GotoDocument.Goto)
                        set.add(transformElement((GotoDocument.Goto) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                    else if (obj instanceof ScriptDocument.Script)
                        set.add(transformElement((ScriptDocument.Script) obj, null, null));
                    else if (obj instanceof ReturnDocument.Return)
                        set.add(transformElement((ReturnDocument.Return) obj, null, null));
                    else if (obj instanceof ThrowDocument.Throw)
                         set.add(transformElement((ThrowDocument.Throw) obj, null, null));
                    else
                        logger.error("Block node with illegal node type" + obj.getClass());
                }

                cursor.toEndToken();
            }

            cursor.toNextToken();
        } while (!cursor.isEnd());

        return block;
    }

    public static final If transformElement(IfDocument.If iBean, String uri, LateBinder lateBinder) {
        If IF = new If();
        logger.debug("Transforming If Node [" + iBean.getCond() + "]");

        XmlCursor cursor = iBean.newCursor();

        cursor.toNextToken(); // move over if start tag
        If.Set set = IF.getExecutableContent();
        IF.setCondition(newCond(iBean.getCond()));
        do {
            if (cursor.isText()) {
                String s = cursor.getChars().trim();
                if (!s.equals("")) {
                    logger.debug("Adding text data from If [" + s + "]");
                    set.add(new Bread(s));
                }
            } else {
                XmlObject obj = cursor.getObject();

                if (obj != null) {
                    if (obj instanceof Speak)
                        set.add(transformElement((Speak) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                    else if (obj instanceof IfDocument.If)
                        set.add(transformElement((IfDocument.If) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                    else if (obj instanceof ElseifDocument.Elseif)
                        set.add(transformElement((ElseifDocument.Elseif) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                    else if (obj instanceof ElseDocument.Else)
                        set.add(transformElement((ElseDocument.Else) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                    else if (obj instanceof GotoDocument.Goto)
                        set.add(transformElement((GotoDocument.Goto) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                     else if (obj instanceof ReturnDocument.Return)
                        set.add(transformElement((ReturnDocument.Return) obj, null, null));
                     else if (obj instanceof ThrowDocument.Throw)
                        set.add(transformElement((ThrowDocument.Throw) obj, null, null));
                     else if (obj instanceof ScriptDocument.Script)
                        set.add(transformElement((ScriptDocument.Script) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));


                    cursor.toEndToken();
                }
            }
            cursor.toNextToken();
        } while (!cursor.isEnd());

        return IF;
    }

    public static final Clear transformElement(ClearDocument.Clear cBean, String uri, LateBinder lateBinder) {
        Clear clear = new Clear();

        Clear.List list = clear.getNameList();

        List l = cBean.getNamelist();

        for (Iterator i = l.iterator(); i.hasNext();) {
            list.add((String) i.next());
        }
        return clear;
    }
    public static final Else transformElement(ElseDocument.Else eBean, String uri, LateBinder lateBinder) {
        Else _else = new Else();
        logger.debug("Transforming Else Node");

        return _else;
    }

    public static final ElseIf transformElement(ElseifDocument.Elseif eBean, String uri, LateBinder lateBinder) {
        ElseIf elseIf = new ElseIf();

        elseIf.setCondition(newCond(eBean.getCond()));
        logger.debug("Transforming ElseIf Node");

        return elseIf;
    }

    public static final Expression newExpression(String s) {
        if (s == null) return null;
        return new Expression(s);
    }

    public static final Cond newCond(String s) {
        if (s == null) return null;
        return new Cond(s);
    }

    public static final Log transformElement(LogDocument.Log lBean, String uri, LateBinder lateBinder) {
        Log log = new Log();

        Log.Set set = log.getExecutbleContent();

        log.setExpression(newExpression(lBean.getExpr()));
        log.setLabel(lBean.getLabel());
        logger.debug("Transforming object of type [Log] " + log.getLabel());
        XmlCursor cursor = lBean.newCursor();

        cursor.toNextToken();


        do {
            if (cursor.isText()) {
                String s = cursor.getChars();
                if (!s.trim().equals("")) {
                    logger.debug("Adding text data from Log[" + s.trim() + "]");
                    set.add(new Bread(s.trim()));
                }
            } else {
                XmlObject obj = cursor.getObject();
                if (obj != null) {
                    if (obj instanceof ValueDocument.Value) {
                        set.add(transformElement((ValueDocument.Value) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                        //while (!cursor.toNextToken().isEnd()) ;
                        cursor.toEndToken();
                    }
                }
            }

        } while (!cursor.toNextToken().isEnd());


        return log;

    }


    public static final Value transformElement(ValueDocument.Value vBean, String uri, LateBinder lateBinder) {
        Value value = new Value();
        logger.debug("Transforming object of type [Value] " + vBean.getExpr());

        value.setExpr(newExpression(vBean.getExpr()));
        return value;
    }

    public static final Throw transformElement(ThrowDocument.Throw tBean, String uri, LateBinder lateBinder) {
        Throw _throw = new Throw();
        logger.debug("Transforming object of type [Throw] ");

        _throw.setEventExpression(new Expression(tBean.getEventexpr()));
        _throw.setEventString(tBean.getEvent());
        _throw.setMessage(tBean.getMessage());
        _throw.setMessageExpression(new Expression(tBean.getMessageexpr()));
        return _throw;
    }

    public static final Exit transformElement(ExitDocument.Exit eBean, String uri, LateBinder lateBinder) {
        Exit exit = new Exit();

        exit.setExpression(newExpression(eBean.getExpr()));

        List list = eBean.getNamelist();
        if(list != null) {
            for (Iterator i = list.iterator(); i.hasNext();) {
                exit.getNameList().add((String) i.next());
            }
        }
        return exit;
    }

    public static final Catch transformElement(CatchDocument.Catch cBean, String uri, LateBinder lateBinder) {
        Catch c = new Catch();


        c.setCond(newCond(cBean.getCond()));
        c.setEvents(cBean.getEvent());
        BigInteger i = cBean.getCount();
        if (i == null) // count defaults to 1
            c.setCount(1);
        else
            c.setCount(i.intValue());
        ExecutableContentGroupElement.Set set = c.getExecutableContent();


        XmlCursor cursor = cBean.newCursor();
        cursor.toNextToken(); // move cursor over element
        while (true) {   // move cursor over attribs
            XmlObject obj = cursor.getObject();
            if (!(obj instanceof ScriptDatatype) && !(obj instanceof IntegerDatatype) && !(obj instanceof EventNamesDatatype)) {
                break;
            }

            cursor.toNextToken();
        }

        populateExecContentSet(set, cursor);


        return c;
    }


    public static final Field transformElement(FieldDocument.Field fBean, String uri, LateBinder lateBinder) {

        Field field = new Field();
        field.setCond(newCond(fBean.getCond()));
        field.setExpression(newExpression(fBean.getExpr()));
        field.setName(fBean.getName());
        field.setType(fBean.getType());
        logger.debug("Transforming Field object [" + field.getName() + "]");

        XmlCursor cursor = fBean.newCursor();


        Field.ContentSet set = field.getContent();

        cursor.toNextToken();
        XmlObject o = cursor.getObject();
        if (o instanceof RestrictedVariableNameDatatype) { // This should trigger "put in scope" action

            logger.debug("Transforming Field name" + fBean.getName() + " to scope");
            cursor.toNextToken();
        }
        do {

            if (cursor.isText()) {
                String ss = cursor.getChars();
                if (!ss.trim().equals("")) {
                    logger.debug("Adding text data to field node [" + ss.trim() + "]");
                    set.add(new Bread(ss.trim()));
                }
            } else {
                XmlObject obj = cursor.getObject();
                // todo: handle mixed Bread content
                if (obj != null) {
                    if (obj instanceof org.w3.x2001.vxml.Audio)
                        set.add(transformElement((org.w3.x2001.vxml.Audio) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                    else if (obj instanceof Speak) // Prompt extends Speak
                        set.add(transformElement((Speak) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                    else if (obj instanceof FilledDocument.Filled)
                        set.add(transformElement((FilledDocument.Filled) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                    else if (obj instanceof LinkDocument.Link) {
                        set.add(transformElement((LinkDocument.Link) obj, uri, lateBinder));

                    } else if (obj instanceof OptionDocument.Option) {
                        set.add(transformElement((OptionDocument.Option) obj, uri, lateBinder));

                    } else if (obj instanceof org.w3.x2001.vxml.MixedGrammar) {
                        set.add(transformElement((org.w3.x2001.vxml.MixedGrammar) obj, uri, lateBinder));

                    } else {
                        logger.error("Field node with illegal node type" + obj.getClass());
                    }
                    cursor.toEndToken();
                }
            }
            cursor.toNextToken();
        } while (!cursor.isEnd());


        return field;
    }

    public static final Record transformElement(RecordDocument.Record rBean, String uri, LateBinder lateBinder) {

        Record record = new Record();
        record.setCond(newCond(rBean.getCond()));
        record.setExpression(newExpression(rBean.getExpr()));
        record.setName(rBean.getName());
        record.setType(rBean.getType());
        List list = rBean.getBeep();
        if (list.size() > 0 && ((String) list.get(0)).equals("true"))
            record.setBeep(true);
        list = rBean.getDtmfterm();
        if (list.size() > 0 && ((String) list.get(0)).equals("false"))
            record.setDtmfTerm(false);
        String dur = rBean.getMaxtime();
        String duration = null;
        String measure = null;
        Pattern p = Pattern.compile("(\\d+)(ms|s)");
        Matcher m = p.matcher(dur);
        if (m.find()) {
            duration = m.group(1);
            measure = m.group(2);
            com.mobeon.application.vxml.datatypes.Duration d = null;
            if (measure.equals("s"))
                d = new com.mobeon.application.vxml.datatypes.Duration(Integer.parseInt(duration), com.mobeon.application.vxml.datatypes.Duration.SECONDS);
            else
                d = new com.mobeon.application.vxml.datatypes.Duration(Integer.parseInt(duration), com.mobeon.application.vxml.datatypes.Duration.MILLISECONDS);
            record.setMaxTime(d);
        }

        logger.debug("Transforming Record object [" + record.getName() + "]");

        XmlCursor cursor = rBean.newCursor();


        Record.ContentSet set = record.getContent();

        cursor.toNextToken();
        XmlObject o = cursor.getObject();
        if (o instanceof RestrictedVariableNameDatatype) { // This should trigger "put in scope" action

            logger.debug("Transforming Record name" + rBean.getName() + " to scope");
            cursor.toNextToken();
        }
        do {

            if (cursor.isText()) {
                String ss = cursor.getChars();
                if (!ss.trim().equals("")) {
                    logger.debug("Adding text data to record node [" + ss.trim() + "]");
                    set.add(new Bread(ss.trim()));
                }
            } else {
                XmlObject obj = cursor.getObject();
                // todo: handle mixed Bread content
                if (obj != null) {
                    if (obj instanceof org.w3.x2001.vxml.Audio)
                        set.add(transformElement((org.w3.x2001.vxml.Audio) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                    else if (obj instanceof Speak) // Prompt extends Speak
                        set.add(transformElement((Speak) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                    else if (obj instanceof FilledDocument.Filled)
                        set.add(transformElement((FilledDocument.Filled) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                    else if (obj instanceof BooleanDatatypeImpl) {
                        // NOP
                    } else if (obj instanceof DurationDatatypeImpl) {
                        // NOP
                    } else {
                        logger.error("Record node with illegal node type" + obj.getClass());
                    }
                    cursor.toEndToken();
                }
            }
            cursor.toNextToken();
        } while (!cursor.isEnd());


        return record;
    }

    public static final Option transformElement(OptionDocument.Option oBean, String uri, LateBinder lateBinder) {
        Option option = new Option();

        return option;
    }

    public static final Filled transformElement(FilledDocument.Filled fBean, String uri, LateBinder lateBinder) {
        Filled filled = new Filled();


        ExecutableContentGroupElement.Set set = filled.getExecutableContent();


        FilledModeDatatype.Enum _enum = fBean.getMode();

        if (_enum != null) // if null filled will correctly default to "any"
            filled.setMode(fBean.getMode().toString());

        List nameList = fBean.getNamelist();
        Filled.List l = filled.getNameList();

        if (nameList != null) {
            for (Iterator i = nameList.iterator(); i.hasNext();) {
                l.add((String) i.next());
            }
        }
        XmlCursor cursor = fBean.newCursor();
        cursor.toNextToken(); // move over the start tag

        while (true) {
            XmlObject obj = cursor.getObject();

            if (!(obj instanceof FilledModeDatatype) && !(obj instanceof RestrictedVariableNamesDatatype)) {
                break;
            }
            cursor.toNextToken();
        }

        populateExecContentSet(set, cursor);

        return filled;
    }

    public static final Grammar transformElement(org.w3.x2001.vxml.MixedGrammar gBean, String uri, LateBinder lateBinder) {

        logger.debug("Transforming grammar");

        XMLGrammar grammar = new XMLGrammar();
        // todo: support more generic grammars
        Rule[] rules = gBean.getRuleArray();


        for (int i = 0; i < rules.length; i++) {
            Rule r = rules[i];
            com.mobeon.application.vxml.grammar.Rule rule = new com.mobeon.application.vxml.grammar.Rule();
            OneOf[] oneOfs = r.getOneOfArray();
            com.mobeon.application.vxml.grammar.Rule.Set set = rule.getContent();
            for (int j = 0; j < oneOfs.length; j++) {
                com.mobeon.application.vxml.grammar.OneOf one = new com.mobeon.application.vxml.grammar.OneOf();
                Item[] items = oneOfs[i].getItemArray();
                for (int k = 0; k < items.length; k++) {
                    XmlCursor cursor = items[k].newCursor();
                    com.mobeon.application.vxml.grammar.Item ii = new com.mobeon.application.vxml.grammar.Item();
                    cursor.toNextToken();
                    if (cursor.isText()) {
                        String s = cursor.getChars();
                        ii.setBread(s.trim());
                    }
                    set.add(ii);
                }

            }
            grammar.setRule(rule); // todo: support more then one rule, now only first rule is used

        }
        return grammar;
    }


    public static final Form transformElement(FormDocument.Form fBean, String uri, LateBinder lateBinder) {
        Form form = new Form();

        Form.ContentSet cs = form.getContent();
        logger.debug("Tranforming Form Node [" + fBean.getId() + "] with uri " + uri);
        XmlCursor cursor = fBean.newCursor();
        form.setId(fBean.getId());

        if (!cursor.toFirstChild()) {
            logger.debug("Form " + fBean.getId() + " empty");
            return form;
        }

        do {
            if (cursor.currentTokenType().intValue() == XmlCursor.TokenType.START.intValue()) {
                XmlObject obj = cursor.getObject();
                if (obj instanceof BlockDocument.Block)
                    cs.add(transformElement((BlockDocument.Block) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof CatchDocument.Catch)
                    cs.add(transformElement((CatchDocument.Catch) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof ErrorDocument)
                    cs.add(transformElement((ErrorDocument) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof FieldDocument.Field)
                    cs.add(transformElement((FieldDocument.Field) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof FilledDocument.Filled)
                    cs.add(transformElement((FilledDocument.Filled) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof MixedGrammar)
                    cs.add(transformElement((org.w3.x2001.vxml.MixedGrammar) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof RecordDocument.Record)
                    cs.add(transformElement((RecordDocument.Record) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else if (obj instanceof SubdialogDocument.Subdialog)
                    cs.add(transformElement((SubdialogDocument.Subdialog) obj, uri /* todo: extend uri with child element name (if needed) */, lateBinder));
                else
                    logger.error("Form node with illegal node type" + obj.getClass());
            }
        } while (cursor.toNextSibling());

        for (int i = 0; i < cs.size(); i++) {
            FormContentElement e = cs.get(i);
            if (e instanceof Field && (i + 1) < cs.size()) { // A sibling must exist
                ((Field) e).setNextSibling(cs.get(i + 1));
            } else if (e instanceof Record && (i + 1) < cs.size()) { // A sibling must exist
                ((Record) e).setNextSibling(cs.get(i + 1));
            }
        }
        return form;
    }

    public static final Assign transformElement(AssignDocument.Assign aBean, String uri, LateBinder lateBinder) {
        Assign assign = new Assign();
        assign.setExpression(newExpression(aBean.getExpr()));
        assign.setName(aBean.getName());

        return assign;
    }

    public static final Return transformElement(ReturnDocument.Return rBean, String uri, LateBinder lateBinder) {
        Return _return = new Return();
        _return .setEvent(createThrowableEvent(rBean.getEvent()));
        if (rBean.getEventexpr() != null)
            _return.setEventExpression(new Expression(rBean.getEventexpr()));
        _return.setMessage(rBean.getMessage());
        if (rBean.getMessageexpr() != null)
            _return.setMessageExpression(new Expression(rBean.getMessageexpr()));

        List nameList = rBean.getNamelist();
        Return.List l = _return.getNameList();

        if (nameList != null) {
            for (Iterator i = nameList.iterator(); i.hasNext();) {
                l.add((String) i.next());
            }
        }
        return _return;
    }

    public static final Prompt transformElement(Speak sBean, String uri, LateBinder lateBinder) {
        logger.debug("Transforming a Prompt Node with uri: " + uri);

        Prompt prompt = new Prompt();

        Prompt.Set set = prompt.getExcutableContent();
        XmlCursor cursor = sBean.newCursor();
        cursor.toNextToken(); // move prompt start tag

        BigInteger in = sBean.getCount();

        if (in != null) {
            prompt.setCount(in.intValue());
        }
        do {
            if (cursor.isText()) {
                String s = cursor.getChars().trim();
                if (!s.equals("")) {
                    logger.debug("Adding text to a Prompt Node [" + s + "]");
                    set.add(new Bread(cursor.getChars()));
                }
            } else {
                XmlObject o = cursor.getObject();
                if (o instanceof org.w3.x2001.vxml.Audio) {

                    set.add(transformElement((org.w3.x2001.vxml.Audio) o, uri, lateBinder));
                } else {
                    logger.error("Prompt node with illegal node type" + o.getClass());
                }
                cursor.toEndToken();
            }


            cursor.toNextToken();
        } while (!cursor.isEnd());

        return prompt;
    }


    public static final Audio transformElement(org.w3.x2001.vxml.Audio aBean, String uri, LateBinder lateBinder) {
        Audio audio = new Audio();
        logger.debug("Transforming Audio Node with uri " + uri);
        audio.setSrc(aBean.getSrc());
        audio.setExpression(newExpression(aBean.getExpr()));

        XmlCursor cursor = aBean.newCursor();
        cursor.toNextToken(); // move over Audio start tag
        XmlObject srcAttribs = cursor.getObject();
        if (srcAttribs != null) {
            if (srcAttribs instanceof org.apache.xmlbeans.XmlAnyURI) {
                cursor.toNextToken();
            } else if (srcAttribs instanceof ScriptDatatype) {
                cursor.toNextToken();
            } else {
                logger.error("Audio element with unsupported attributes");
            }
        }


        Audio.Set set = audio.getExecutableContent();
        do {
            if (cursor.isText()) {
                String s = cursor.getChars().trim();
                if (!s.equals("")) {
                    logger.debug("Adding text to a Audio Node [" + s + "]");
                    set.add(new Bread(cursor.getChars()));
                }
            } else {
                XmlObject obj = cursor.getObject();
                if (obj != null) {
                    if (obj instanceof ValueDocument.Value) {
                        set.add(transformElement((ValueDocument.Value) obj, uri, lateBinder));

                    } else {
                        logger.error("Audio node with illegal node type" + obj.getClass());
                    }
                }
            }

            cursor.toNextToken();
        } while (!cursor.isEnd());

        return audio;
    }

    public static final Error transformElement(ErrorDocument eBean, String uri, LateBinder lateBinder) {
        Error error = new Error();
        // todo: add support for GOTO, RETURN and THROW
        return error;
    }

    private static final void populateExecContentSet(ExecutableContentGroupElement.Set set, XmlCursor cursor) {
        if (cursor.isEnd() || cursor.isEnddoc())
            return;

        logger.debug("In populateExecContentSet");
        while (!cursor.isEnd()) {
            if (cursor.isText()) {
                String s = cursor.getChars();
                s = s.trim();
                if (s != null && !s.equals("")) {
                    logger.debug("Adding text [" + s.trim() + "] to executableContentGroup");
                    set.add(new Bread(s));
                }

            } else {
                XmlObject obj = cursor.getObject();

                if (obj != null) {
                    if (obj instanceof LogDocument.Log)
                        set.add(transformElement((LogDocument.Log) obj, null, null));     // todo: Remove uri and lateBinder from tranformElement
                    else if (obj instanceof Speak)
                        set.add(transformElement((Speak) obj, null, null));
                    else if (obj instanceof IfDocument.If)
                        set.add(transformElement((IfDocument.If) obj, null, null));
                    else if (obj instanceof GotoDocument.Goto)
                        set.add(transformElement((GotoDocument.Goto) obj, null, null));
                    else if (obj instanceof ExitDocument.Exit)
                        set.add(transformElement((ExitDocument.Exit) obj, null, null));
                    else if (obj instanceof ValueDocument.Value)
                        set.add(transformElement((ValueDocument.Value) obj, null, null));
                    else if (obj instanceof ThrowDocument.Throw)
                        set.add(transformElement((ThrowDocument.Throw) obj, null, null));
                    else if (obj instanceof AssignDocument.Assign) {
                        set.add(transformElement((AssignDocument.Assign) obj, null, null));
                    }
                     else if (obj instanceof ReturnDocument.Return)
                        set.add(transformElement((ReturnDocument.Return) obj, null, null));else {
                        logger.error("Executble Content node with illegal node type" + obj.getClass());
                    }

                } else {
                    logger.debug("obj is null - can this realy happen?"); // todo: remove this else
                }

                cursor.toEndToken();
            }
            cursor.toNextToken();
        }

    }

    public static final ThrowableEvent createThrowableEvent(String eventType) {
        // TODO: Implement all other events
        if (eventType == null) {
            return null;
        }
        if (eventType.startsWith("error")) { return new Error(); }
        else if (eventType.equals("noinput")) { return new NoInput();}
        else if (eventType.equals("nomatch")) { return new NoMatch();}
        else if (eventType.equals("help")) { return new Help();}
        else {
            logger.error("Can not create event for event type " + eventType + " Returning NULL object");
            return null;
        }
    }
}