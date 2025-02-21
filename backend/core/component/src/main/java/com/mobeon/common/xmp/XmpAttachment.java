package com.mobeon.common.xmp;

import java.io.*;
import jakarta.activation.DataSource;

/**
 * A simple DataSource for holding attachment data
 * This class implements a DataSource from an InputStream
 *
 * @author Mac
 */
public class XmpAttachment implements DataSource {
    private byte[] data;        // data
    private String type;        // content-type

    public XmpAttachment(InputStream is, String type) {
        this.type = type;
        try { 
            ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
            int ch;  
            byte[] tmp=new byte[1024]; //Read/write in batches to minimize systemcalls
            while((ch=is.read(tmp)) !=-1){
                os.write(tmp,0,ch);
            }
            
            data = os.toByteArray();

        } catch (IOException ioex) {
           System.out.println("Failed to make attachment, " + ioex.toString() );
        }
    }
    
    public XmpAttachment(byte[] data, String type) {
        this.type = type;
        this.data = data;
    }

    /**
     * Return an InputStream for the data.
     * A new stream must be returned each time.
     * @return a newly allocated InputStream to read the data from.
     */
    public InputStream getInputStream() throws IOException {
        if (data == null)
            throw new IOException("no data");
        return new ByteArrayInputStream(data);
    }

    public String getContentType() {
        return type;
    }

    public String getName() {
        return "noname";
    }
    
    public int getSize(){
        return (data!=null)?data.length:-1;
    }
    
    public byte[] getData() {
        return data;
    }
    
    public void print(OutputStream out) {
        try {
            out.write("XmpAttachment".getBytes());
            out.write(("ContentType=" + type + "\n").getBytes());
            out.write(data);
        } catch (IOException ioe) {
            // nothing
        }
        
    }
    
    public java.io.OutputStream getOutputStream() throws java.io.IOException {
        throw new IOException("No support for this");
    }
    
}
