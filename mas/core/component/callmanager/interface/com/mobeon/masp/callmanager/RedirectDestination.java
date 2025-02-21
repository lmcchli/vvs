package com.mobeon.masp.callmanager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;




/**
 * Represents a valid redirect destination for redirecting an InboundCall
 * @author lmcraby
 * @since MIO 2.0.1
 *
 */
public class RedirectDestination extends CallPartyDefinitions{

    private RedirectDestination() {
        super();

    }


    /**
     * Parses a string representing a valid redirect destination URI into a RedirectionDestination
     * Valid URI scheme are tel, sip fax and modem
     * @param uri a string representing a valid destination URI
     * @return the valid redirection destination
     * @throws URISyntaxException if the specified string is not a valid destination URI
     */
    public static RedirectDestination parseRedirectDestination(String uri) throws URISyntaxException{
        
        RedirectDestination party = new RedirectDestination();
        URI aURI = new URI(uri);
   
        if (isSchemeSupported(aURI)) {
                    party .setUri(uri);
         } else {
               throw new URISyntaxException(uri, "Unsupported sheme only support " + supportedURIScheme.toString());
         }
        return party;
    }
    
    public static boolean isSchemeSupported(URI uri){
        String scheme = uri.getScheme();
        return supportedURIScheme.contains(scheme);
    }
    
    public static Set<String> getSupportedURIScheme() {
        return Collections.unmodifiableSet(supportedURIScheme);
    }
        
    private static final Set<String> supportedURIScheme = new HashSet<String>(4);
    static {
        supportedURIScheme.add("tel");
        supportedURIScheme.add("fax");
        supportedURIScheme.add("modem");
        supportedURIScheme.add("sip");
    }
    
   

}
