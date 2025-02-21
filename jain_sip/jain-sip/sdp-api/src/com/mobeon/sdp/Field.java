/*
 * Field.java
 *
 */

package com.mobeon.sdp;

import java.io.*;

/** A Field represents a single line of information within a SDP
 * session description.
 *
 * @author Malin Flodin
 */
public interface Field extends Serializable, Cloneable {

    /** Returns the type character for the field.
     * @return the type character for the field.
     */
    public char getTypeChar();
    
    
    /** Returns a clone of this field.
     * @return  a clone of this field.
     */
    public Object clone();
    
}

