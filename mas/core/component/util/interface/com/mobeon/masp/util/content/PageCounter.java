/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.content;

import java.io.Reader;
import java.io.IOException;

/**
 * Implementations of this interface are able to count "pages" in content data
 * representing semantically has "pages". E.g. image data may represent a five pages fax.
 * <br>
 * This is done by examining the characters representing content in a specific format.
 * Each implementation "knows" how to parse page count in the specific format.
 *
 * @author Håkan Stolt
 */
public interface PageCounter {

    /**
     *
     * @param contentReader content character stream reader.
     * @return page count. Returns zero if pages not could be counted.
     * @throws IOException If an I/O error occurs
     */
    public long countPages(Reader contentReader) throws IOException ;

}
