package com.mobeon.ntf.text;

/**
 * TemplateByteTokenizer splits an array of bytes into multiple subarrays
 * based on specific delimiters. TemplateByteTokenizer supports both forward
 * and backward tokenizing.
 */
public class TemplateByteTokenizer {
    
    /** The current byte observed when looking forward. */
    private int cursorForward;
    
    /** The current byte observed when looking backward. */
    private int cursorBackward;
    
    /** The byte array to tokenize. */
    private final byte[] array;
    
    private static final byte NL = (byte)'\n';
    private static final byte CR = (byte)'\r';
    private static final byte QU = (byte)'\"';
    private static final byte SP = (byte)' ';
    private static final byte TAB = (byte)'\t';
    private static final byte CBoR = (byte)'{';
    private static final byte CBoL = (byte)'}';
    private static final byte BoR = (byte)'(';
    private static final byte BoL = (byte)')';
    
    /**
     * Creates a TemplateByteTokenizer from the given array.
     * @param array 
     */
    public TemplateByteTokenizer(byte[] array)
    {
        this.array = array;
        cursorForward = 0;
        cursorBackward = array.length - 1;
    }
    
    /**
     * Returns the next token in a foward tokenization.
     */
    public byte[] getNextForward()
    {

        int near = 0, far = array.length - 1; // Near and far are the indices for the array to return (near < far)
        if (cursorForward >= array.length) return null;
        // search
        boolean quote = false; // "
        boolean tag = false; // VCOUNT
        boolean brace = false; // (
        for (;cursorForward < array.length; cursorForward++)
        {
            byte b = array[cursorForward];
            if (quote && b == QU)
            {
                far = cursorForward - 1;
                break;
            }
            if (tag && (b == SP || b == TAB || b == QU || b == CBoL))
            {
                far = cursorForward - 1;
                break;
            }
            if (brace && b == BoL)
            {
                far = cursorForward;
                break;
            }
            if (!quote && !tag && !brace && b == QU)
            {
                near = cursorForward + 1;
                quote = true;
                continue;
            }
            if (!quote && !tag && !brace && b == BoR)
            {
                near = cursorForward;
                brace = true;
                continue;
            }
            if (!quote && !tag && !brace && ByteArrayUtils.byteMatch(b))
            {
                near = cursorForward;
                tag = true;
                continue;
            }
        }
        cursorForward++; // Advance cursor to avoid re-reading last char read
        byte[] token = new byte[far - near + 1 + (tag ? 2 : 0)];
        if (tag)
        {
            token[0] = '_';
            token[1] = '_';
        }
        for (int i = (tag ? 2 : 0); i < token.length; i++)
        {
            token[i] = array[near + i - (tag ? 2 : 0)];
        }
        return token;
    }
    
    /**
     * Returns the next token in a backward tokenization.
     */
    public byte[] getNextBackward()
    {
        int near = 0, far = array.length - 1; // Near and far are the indices for the array to return (near < far)
        if (cursorBackward < 0) return null;
        // search
        boolean quote = false; // "
        boolean tag = false; // VCOUNT
        boolean brace = false; // (
        for (;cursorBackward >= 0; cursorBackward--)
        {
            byte b = array[cursorBackward];
            if (quote && b == QU)
            {
                near = cursorBackward + 1;
                break;
            }
            if (tag && (b == SP || b == TAB || b == QU || b == CBoR))
            {
                near = cursorBackward + 1;
                break;
            }
            if (brace && b == BoR)
            {
                near = cursorBackward;
                break;
            }
            if (!quote && !tag && !brace && b == QU)
            {
                far = cursorBackward - 1;
                quote = true;
                continue;
            }
            if (!quote && !tag && !brace && b == BoL)
            {
                far = cursorBackward;
                brace = true;
                continue;
            }
            if (!quote && !tag && !brace && ByteArrayUtils.byteMatch(b))
            {
                far = cursorBackward;
                tag = true;
                continue;
            }
        }
        cursorBackward--; // Advance cursor to avoid re-reading last char read
        byte[] token = new byte[far - near + 1 + (tag ? 2 : 0)];
        if (tag)
        {
            token[0] = '_';
            token[1] = '_';
        }
        for (int i = (tag ? 2 : 0); i < token.length; i++)
        {
            token[i] = array[near + i - (tag ? 2 : 0)];
        }
        return token;
    }

}
