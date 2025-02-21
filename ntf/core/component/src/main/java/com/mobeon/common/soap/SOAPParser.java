/*
 * SOAPParser.java
 * Created on March 28, 2004, 7:55 PM
 */
package com.mobeon.common.soap;
import java.io.*;
import java.util.*;

/**
 * This class parses the SOAP Response from a SOAP enabled server. The parse method must
 * be called first and the parsed result is stored in a SOAPEnvelope which can be
 * fetched with the getSOAPEnvelope method
 * 
 * @author  ermmaha
 */
public class SOAPParser {
    
    private String _envelopeDelimiter;
    private String _headerDelimiter;
    private String _bodyDelimiter;
    private SOAPEnvelope _envelope;
    
    public SOAPParser() {
        _envelopeDelimiter = "env:Envelope";
        _headerDelimiter = "env:Header";
        _bodyDelimiter = "env:Body";
    }
    
    public void setHeaderDelimiter(String headerDelimiter) {
        _headerDelimiter = headerDelimiter;
    }
    
    public void setBodyDelimiter(String bodyDelimiter) {
        _bodyDelimiter = bodyDelimiter;
    }
    
    /**
     * Parses the SOAP Response from the MM7 server
     * @param String data
     * @throws SOAPParseException If some invalid SOAP data was encountered
     */
    public void parse(String data) throws SOAPParseException {
        if(!checkEnvelope(data)) {
            throw new SOAPParseException("No SOAP envelope found");
        }
        
        _envelope = new SOAPEnvelope();
        
        String tmp = getHeader(data);
        if(tmp != null) {
            SOAPHeader header = getSOAPHeader(tmp);
            if(header != null) {
                _envelope.setHeader(header);
            }
        } else {
            throw new SOAPParseException("No SOAP header found");
        }
        
        tmp = getBody(data);
        if(tmp != null) {
            SOAPBody body = getSOAPBody(tmp);
            if(body != null) {
                _envelope.setBody(body);
            }
        }
        else {
            throw new SOAPParseException("No SOAP body found");
        }
    }
    
    /**
     * Returns the parsed SOAPEnvelope
     * @return The SOAPEnvelope object
     */
    public SOAPEnvelope getSOAPEnvelope() {
        return _envelope;
    }
    
    private boolean checkEnvelope(String data) {
        int pos = data.indexOf(_envelopeDelimiter);
        return (pos == -1) ? false : true;
    }
    
    /**
     * Retrieves the data inside the env:Header tags
     * @param String
     * @return String
     */
    private String getHeader(String data) {
        int startpos = data.indexOf(_headerDelimiter); //Lower or uppercase ?
        int endPos = data.lastIndexOf(("/" + _headerDelimiter));
        if(startpos == -1 || endPos == -1 || (endPos <= startpos)) return null;
        
        return data.substring(startpos + _headerDelimiter.length() + 1, endPos - 1);
    }
    
    /**
     * Retrieves the data in the env:Body tags
     * @param String
     * @return String
     */
    private String getBody(String data) {
        int startpos = data.indexOf(_bodyDelimiter); //Lower or uppercase ?
        int endPos = data.lastIndexOf(("/" + _bodyDelimiter));
        if(startpos == -1 || endPos == -1 || (endPos <= startpos)) return null;
        
        return data.substring(startpos + _bodyDelimiter.length() + 1, endPos - 1);
    }
    
    /**
     * Parses the header data into a SOAPHeader object
     * @param String
     * @return SOAPHeader
     * @throws SOAPParseException
     */
    private SOAPHeader getSOAPHeader(String data) throws SOAPParseException {
        
        SOAPHeader header = null;
        boolean firstTag = true;
        
        StringTokenizer z = new StringTokenizer(data, "<");
        while(z.hasMoreTokens()) {
            String tmp = z.nextToken();
            tmp = tmp.trim();
            if(tmp.length() > 0 && !tmp.startsWith("/")) {
                SOAPTag tag = new SOAPTag();
                setTagFromTokenString(tmp, tag);
                if(firstTag) {
                    
                    //The first tag should be the header, create the SOAPHeader
                    String nameSpace = tag.getAttribute(0);
                    if(nameSpace != null) {
                        header = new SOAPHeader(tag.getName(), nameSpace, tag.getValue());
                    } else {
                        //No name space on the header ?
                        header = new SOAPHeader(tag.getName(), "", tag.getValue());
                    }
                    firstTag = false;
                }
                //Add these tags too ?
            }
        }
        
        if(header == null) throw new SOAPParseException("Invalid SOAP tag in env:Header");
        
        return header;
    }
    
    private SOAPBody getSOAPBody(String data) throws SOAPParseException {
        SOAPBody body = null;
        boolean firstTag = true;
        
        StringTokenizer z = new StringTokenizer(data, "<");
        while(z.hasMoreTokens()) {
            String tmp = z.nextToken();
            tmp = tmp.trim();
            if(tmp.length() > 0 && !tmp.startsWith("/")) {
                SOAPTag tag = new SOAPTag();
                setTagFromTokenString(tmp, tag);

                if(firstTag) {
                    //The first tag should be the method, create the SOAPBody
                    //Don't add the method tag to the element list
                    String nameSpace = tag.getAttribute(0);
                    if(nameSpace != null) {
                        body = new SOAPBody(tag.getName(), nameSpace);
                    } else {
                        //No name space on the body ?
                        body = new SOAPBody(tag.getName(), "");
                    }
                    firstTag = false;
                } else {
                    body.addElement(tag.getName(), tag);
                }
            }
        }
        
        if(body == null) throw new SOAPParseException("Invalid SOAP tag in env:Body");
        
        return body;
    }
    
    private void setTagFromTokenString(String tmp, SOAPTag tag) throws SOAPParseException {
        
        int endpos = tmp.indexOf(">");
        if(endpos == -1) throw new SOAPParseException("Invalid SOAP tag near "+tmp);
        
        String name = tmp.substring(0, endpos);
        int spacepos = name.indexOf(" ");
        if(spacepos != -1) {
            //name = tmp.substring(0, spacepos);
            boolean nameSet = false;
            StringTokenizer z = new StringTokenizer(name);
            while(z.hasMoreTokens()) {
                String nameattr = z.nextToken();
                if(!nameSet) {
                    tag.setName(nameattr);
                    nameSet = true;
                } else {
                    tag.addAttribute(nameattr);
                }
            }
        } else {
            tag.setName(name);
        }
        
        String value = tmp.substring(endpos + 1, tmp.length());
        tag.setValue(value.trim());
    }
}

        /*
        SOAPTagReader read = new SOAPTagReader(new StringReader(soapData));
        SOAPTag tag = null;
        while((tag = read.readTag()) != null) {
            ;
        }*/
/**
 * Decorates a Reader object with funcionallity to read tags ( <data> )
 * DOES NOT WORK YET
 *
 */
/*
class SOAPTagReader extends Reader {
    Reader _in;
 
    public SOAPTagReader(Reader in) {
        super(in);
        _in = in;
    }
 
    public SOAPTag readTag() throws IOException {
        boolean startCharFound = false;
        boolean endCharFound = false;
        StringBuffer tmp = new StringBuffer();
        int c = 0;
        while((c = _in.read()) != -1) {
            // <
            if(c == 60) {
                startCharFound = true;
            }
            else if(c == 62) { // >
                endCharFound = true;
            }
            tmp.append((char)c);
            if(startCharFound && endCharFound) {
                startCharFound = false;
                endCharFound = false;
                String tagStr = tmp.toString();
                tmp = new StringBuffer();
                return makeTag(tagStr);
            }
        }
        return null;
    }
 
    public int read(char[] cbuf, int off, int len) throws IOException {
        return _in.read(cbuf, off, len);
    }
 
    public void close() throws IOException{
        _in.close();
        _in = null;
    }
 
    private SOAPTag makeTag(String tagStr) {
        System.out.println("tagStr:"+tagStr);
        SOAPTag tag = new SOAPTag();
        return tag;
    }
}
 */