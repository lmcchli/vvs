/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.content;

import com.mobeon.masp.util.content.PageCounter;

import java.io.Reader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Counts how many times a string representing a page break occurrs
 * in the content character stream.
 * @author Håkan Stolt
 */
public class PageBreakingStringCounter implements PageCounter {


    /**
     * String defining the page breaking string.
     */
    private String pageBreaker;

    /**
     * Default contructor. Page break String is not initialized.
     */
    public PageBreakingStringCounter() {
    }

    /**
     * Page break String is not initialized.
     * @throws IllegalArgumentException if pageBreaker is null or empty.
     */
    public PageBreakingStringCounter(String pageBreaker) {
        setPageBreaker(pageBreaker);
    }

    /**
     * Gets the string defining a page break..
     * @return page break string. (Can be null)
     */
    public String getPageBreaker() {
        return pageBreaker;
    }

    /**
     * Sets the String defining a page break.
     * @param pageBreaker
     * @throws IllegalArgumentException if pageBreaker is null or empty.
     */
    public void setPageBreaker(String pageBreaker) {
        if(pageBreaker == null || pageBreaker.length() == 0) throw new IllegalArgumentException("pageBreaker cannot be null or empty!");
        this.pageBreaker = pageBreaker;
    }

    /**
     * Counts how many times a page breaking string occurrs in the character stream.
     * @param contentReader content character stream reader.
     * @return page count. Returns zero if pages not could be counted.
     * @throws IOException If an I/O error occurs
     * @throws IllegalArgumentException if contentReader is null.
     * @throws IllegalStateException if pageBreaker not is initialized.
     */
    public long countPages(Reader contentReader) throws IOException {
        if(contentReader == null) throw new IllegalArgumentException("contentReader cannot be null!");
        if(pageBreaker == null) throw new IllegalStateException("pageBreaker must be initialized!");

        StringBuilder buffer = new StringBuilder();
        BufferedReader br = new BufferedReader(contentReader);
        String line = br.readLine();
        while(line != null) {
            buffer.append(line);
            line = br.readLine();
        }
        long pages = 0;
        int i = buffer.indexOf(pageBreaker);
        while(i>-1) {
            pages++;
            i = buffer.indexOf(pageBreaker,i+pageBreaker.length());
        }
        return pages;

    }

    @Override
    public String toString() {
        return PageBreakingStringCounter.class.getName()+":pageBreaker=\""+pageBreaker+"\"";
    }
}

