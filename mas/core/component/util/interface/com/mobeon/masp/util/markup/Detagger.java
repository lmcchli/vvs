/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.markup;

/**
 * A markup text to plain text converter.
 * Detagger is a utility that can remove markup tags from a text.
 * @author qhast
 */
public class Detagger {


    /**
     * Makes a brutal removal of markup tags.
     * It simple replaces all &lt;characters&gt; with a empty string.
     * @param source markup text.
     * @return a plain text with no markup tags.
     */
    public static String removeMarkup(String source) {
        return source.replaceAll("(<[^>]+>)|(&\\w+;)","");
    }

    /**
     * Makes a nice removal of HTML markup tags.
     * For example words in sentences and paragraphs are kept separated.
     * <p>
     * <i>Currently this method calls {@link #removeMarkup(String)} </i> 
     * @param source
     * @return a plain text with no markup tags.
     */
    public static String removeHtmlMarkup(String source) {
        return removeMarkup(source); //todo improve implementation.
    }




}
