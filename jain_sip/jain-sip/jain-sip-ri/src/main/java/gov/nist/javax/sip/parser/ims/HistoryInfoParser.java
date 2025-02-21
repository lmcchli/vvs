package gov.nist.javax.sip.parser.ims;

import gov.nist.javax.sip.parser.AddressParametersParser;
import gov.nist.javax.sip.parser.Lexer;
import gov.nist.javax.sip.parser.TokenTypes;
import gov.nist.javax.sip.parser.URLParser;
import gov.nist.javax.sip.header.SIPHeader;
import gov.nist.javax.sip.header.ims.HistoryInfoList;
import gov.nist.javax.sip.header.ims.HistoryInfo;
import gov.nist.javax.sip.address.AddressImpl;
import gov.nist.javax.sip.address.GenericURI;
import gov.nist.javax.sip.address.SipUri;

import javax.sip.address.URI;
import java.text.ParseException;
import java.util.Iterator;

/**
 * A parser for The SIP History-Info header.
 *
 */
public class HistoryInfoParser extends AddressParametersParser {

    public HistoryInfoParser(String historyInfo) {
        super(historyInfo);
    }

    protected HistoryInfoParser(Lexer lexer) {
        super(lexer);
        this.lexer = lexer;
    }

    public SIPHeader parse() throws ParseException {
        // past the header name and the colon.
        headerName(TokenTypes.HISTORY_INFO);
        HistoryInfoList historyInfoList = new HistoryInfoList();
        while (true) {
            HistoryInfo historyInfo = new HistoryInfo();
            
            // Add additional character to parse Reason header from non RFC compliant gateway
            // ie: "<sip:user@host?Reason=SIP;cause=486>;index=1" (it should be "<sip:user@host?Reason=SIP%3Bcause%3D486>;index=1")
            char [] additionalChar = {';', '='}; 
            this.lexer.setAdditionalUrlQueryHeaderCharacter(additionalChar);
            super.parse(historyInfo);
            this.lexer.setAdditionalUrlQueryHeaderCharacter(null);
            AddressImpl address = (AddressImpl) historyInfo.getAddress();
            URI uri = historyInfo.getAddress().getURI();
            /*
                * When the header field value contains a display name, the URI
                * including all URI parameters is enclosed in "<" and ">". If no "<"
                * and ">" are present, all parameters after the URI are header
                * parameters, not URI parameters.
                */
            if (address.getAddressType() == AddressImpl.ADDRESS_SPEC
                    && uri instanceof SipUri) {
                SipUri	 sipUri = (SipUri) uri;
                
                for (Iterator it = sipUri.getParameterNames(); it.hasNext();) {
                    String name = (String) it.next();
                    String val = sipUri.getParameter(name);
                    sipUri.removeParameter(name);
                    historyInfo.setParameter(name,val);
                }
            }
            historyInfoList.add(historyInfo);
            this.lexer.SPorHT();
            if (lexer.lookAhead(0) == ',') {
                this.lexer.match(',');
                this.lexer.SPorHT();
            } else if (lexer.lookAhead(0) == '\n' || lexer.lookAhead(0) == '\0')
                break;
            else
                throw createParseException("unexpected char");
        }
        return historyInfoList;
    }
    
    // quick test routine for debugging type assignment
    /**********************
    public static void main(String[] args) throws ParseException
    {
        // quick test for sips parsing
        String[] test = { "History-Info: <sip:09097892125@domain.com?Reason=SIP;cause=486&Privacy=history>;index=1",
                     "History-Info: <sip:479021@domain.com>;index=2",
                     "History-Info: <sip:479021@domain.com?Reason=SIP%3Bcause%3D486>;index=3",
                     "History-Info: <sip:479021@domain.com?Reason=SIP%3Bcause%3D486&Privacy=history>;index=4",
                     "History-Info: <sip:491721093020@domain.com;privacy=history;cause=302>;index=1,<sip:12345@domain.com>;index=1.1"};
        
        for ( int i = 0; i < test.length; i++)
        {
            HistoryInfoParser p  = new HistoryInfoParser(test[i]);
            char [] x = {';', '='};
                p.getLexer().setAdditionalUrlQueryHeaderCharacter(x);
                HistoryInfoList uriList = (HistoryInfoList)p.parse();
                p.getLexer().setAdditionalUrlQueryHeaderCharacter(null);
                
                System.out.println("uri type returned " + uriList.getClass().getName());
                
                HistoryInfo h = ((HistoryInfo)uriList.getFirst()); //only check the first value for this basic test
                
                System.out.println(test[i] + " Reason Header? " + h.getReasonHeader() 
                                    + " Privacy Header? " + (h.getPrivacyValues() != null ? h.getPrivacyValues()[0] : "null"));
                
                System.out.println("cause? " + ((SipUri)h.getAddress().getURI()).getParameter("cause"));
                System.out.println("Index? " + h.getIndex());
                
                System.out.println( uriList.encode());
        }
    }
    ************************/

}
