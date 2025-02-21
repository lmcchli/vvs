/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.xml;

import org.dom4j.io.XPP3Reader;
import org.dom4j.*;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import com.mobeon.masp.util.Ignore;

import java.io.IOException;

public class XPP3CompilerReader extends XPP3Reader {
    private CompilerElementFactory compilerElementFactory;

    public XPP3CompilerReader() {
         compilerElementFactory = new CompilerElementFactory(this);
    }


    /**
     * Fix for bug in DOM4J 1.6.1 where ENTITY_REFS is not added to the
     * tree at all. Here we simply att them as their replacement text.
     * Copyright for this method (except for the changes introduced)
     * is given at the bottom of this file.
     * @return
     * @throws DocumentException
     * @throws IOException
     * @throws XmlPullParserException
     */
    protected Document parseDocument() throws DocumentException, IOException,
            XmlPullParserException {
        DocumentFactory df = getDocumentFactory();
        Document document = df.createDocument();
        Element parent = null;
        XmlPullParser pp = getXPPParser();
        pp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);

        while (true) {
            int type = pp.nextToken();

            switch (type) {
                case XmlPullParser.PROCESSING_INSTRUCTION: {
                    String text = pp.getText();
                    int loc = text.indexOf(" ");

                    if (loc >= 0) {
                        String target = text.substring(0, loc);
                        String txt = text.substring(loc + 1);
                        document.addProcessingInstruction(target, txt);
                    } else {
                        document.addProcessingInstruction(text, "");
                    }

                    break;
                }

                case XmlPullParser.COMMENT: {
                    if (parent != null) {
                        parent.addComment(pp.getText());
                    } else {
                        document.addComment(pp.getText());
                    }

                    break;
                }

                case XmlPullParser.CDSECT: {
                    if (parent != null) {
                        parent.addCDATA(pp.getText());
                    } else {
                        String msg = "Cannot have text content outside of the "
                                + "root document";
                        throw new DocumentException(msg);
                    }

                    break;
                }

                case XmlPullParser.END_DOCUMENT:
                    return document;

                case XmlPullParser.START_TAG: {
                    QName qname = (pp.getPrefix() == null) ? df.createQName(pp
                            .getName(), pp.getNamespace()) : df.createQName(pp
                            .getName(), pp.getPrefix(), pp.getNamespace());
                    Element newElement = df.createElement(qname);
                    int nsStart = pp.getNamespaceCount(pp.getDepth() - 1);
                    int nsEnd = pp.getNamespaceCount(pp.getDepth());

                    for (int i = nsStart; i < nsEnd; i++) {
                        if (pp.getNamespacePrefix(i) != null) {
                            newElement.addNamespace(pp.getNamespacePrefix(i),
                                    pp.getNamespaceUri(i));
                        }
                    }

                    for (int i = 0; i < pp.getAttributeCount(); i++) {
                        QName qa = (pp.getAttributePrefix(i) == null) ? df
                                .createQName(pp.getAttributeName(i)) : df
                                .createQName(pp.getAttributeName(i), pp
                                        .getAttributePrefix(i), pp
                                        .getAttributeNamespace(i));
                        newElement.addAttribute(qa, pp.getAttributeValue(i));
                    }

                    if (parent != null) {
                        parent.add(newElement);
                    } else {
                        document.add(newElement);
                    }

                    parent = newElement;

                    break;
                }

                case XmlPullParser.END_TAG: {
                    if (parent != null) {
                        parent = parent.getParent();
                    }

                    break;
                }

                case XmlPullParser.ENTITY_REF:
                case XmlPullParser.TEXT: {
                    String text = pp.getText();

                    if (parent != null) {
                        parent.addText(text);
                    } else {
                        String msg = "Cannot have text content outside of the "
                                + "root document";
                        throw new DocumentException(msg);
                    }

                    break;
                }

                default:
                    break;
            }
        }
    }

    public DocumentFactory getDocumentFactory() {
        return compilerElementFactory;
    }
}

/**
 * Licence pertinent to the parseDocument method ONLY !
 *
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright statements and
 * notices. Redistributions must also contain a copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. The name "DOM4J" must not be used to endorse or promote products derived
 * from this Software without prior written permission of MetaStuff, Ltd. For
 * written permission, please contact dom4j-info@metastuff.com.
 *
 * 4. Products derived from this Software may not be called "DOM4J" nor may
 * "DOM4J" appear in their names without prior written permission of MetaStuff,
 * Ltd. DOM4J is a registered trademark of MetaStuff, Ltd.
 *
 * 5. Due credit should be given to the DOM4J Project - http://www.dom4j.org
 *
 * THIS SOFTWARE IS PROVIDED BY METASTUFF, LTD. AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL METASTUFF, LTD. OR ITS CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 */
