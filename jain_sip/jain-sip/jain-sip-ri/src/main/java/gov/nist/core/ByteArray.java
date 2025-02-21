package gov.nist.core;

public class ByteArray {

    private byte[] buf;

    public ByteArray() {
        buf = new byte[0];
    }

    public ByteArray(byte[] bytes) {
        buf = bytes;
    }

    public ByteArray(byte[] bytes, int start, int len) {
        buf = new byte[len];
        System.arraycopy(bytes,start,buf,0,len);
    }

    public void setBytes(byte[] bytes) {
        buf = bytes;
    }

    public byte[] getBytes() {
        return buf;
    }

    public void append(byte[] bytes) {
        byte[] tmp = new byte[buf.length + bytes.length];
        System.arraycopy(buf, 0, tmp, 0, buf.length);
        System.arraycopy(bytes, 0, tmp, buf.length, bytes.length);
        buf = tmp;
    }

    public void append(byte[] bytes, int start, int len) {
        byte[] tmp = new byte[buf.length + len];
        System.arraycopy(buf, 0, tmp, 0, buf.length);
        System.arraycopy(bytes, start, tmp, buf.length, len);
        buf = tmp;
    }

    public boolean equals(byte[] bytes) {
        return buf.equals(bytes);
    }

    public boolean isEqual(String s) {
        if (buf.length != s.length())
            return false;

        for (int i=0; i<buf.length; i++) {
            if ((char)buf[i] != s.charAt(i))
                return false;
        }
        return true;
    }

    public boolean startsWith(byte[] s) {

        if (buf.length < s.length)
            return false;

        for (int i=0; i<s.length; i++) {
            if (buf[i] != s[i])
                return false;
        }
        return true;

    }

    public boolean startsWith(String s) {

        if (buf.length < s.length())
            return false;

        for (int i=0; i<s.length(); i++) {
            if ((char)buf[i] != s.charAt(i))
                return false;
        }
        return true;

    }

    public boolean startsWith(byte[] s, int offset) {
        if (buf.length < s.length+offset)
            return false;

        for (int i=0; i < s.length; i++) {
            if (buf[i+offset] != s[i])
                return false;
        }
        return true;

    }

    public boolean startsWith(String s, int offset) {
        if (buf.length < s.length()+offset)
            return false;

        for (int i=0; i < s.length(); i++) {
            if ((char)buf[i+offset] != s.charAt(i))
                return false;
        }
        return true;

    }

    public String toString() {
        return new String(buf);
    }


    /**
     * Returns true if byte array contains nothing but characters with same
     * value as space (0x20) or less.
     *
     * @return true if only white space, false otherwise
     */
    public boolean isEmptyLine() {
        if (buf.length <= 0)
            return true;

        for (byte aBuf : buf) {
            if (aBuf > ' ')
                return false;
        }
        return false;

    }

}
