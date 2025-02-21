/*
 * COPYRIGHT Abcxyz Communication Inc. Montreal 2009
 * The copyright to the computer program(s) herein is the property
 * of ABCXYZ Communication Inc. Canada. The program(s) may be used
 * and/or copied only with the written permission from ABCXYZ
 * Communication Inc. or in accordance with the terms and conditions
 * stipulated in the agreement/contact under which the program(s)
 * have been supplied.
 *---------------------------------------------------------------------
 * Created on 22-apr-2009
 */
package com.mobeon.masp.execution_engine.runtime.xmlhttprequest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringBufferInputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.NativeFunction;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.xml.XMLObject;


public class XMLHttpRequest extends ScriptableObject {

    /**
     * 
     */
    private static final long serialVersionUID = -4039947159343053330L;
    private String url;
    private String httpMethod;
    private HttpURLConnection urlConnection = null;

    private int httpStatus;
    private String httpStatusText;

    private Map<String, String> requestHeaders;

    private String userName;
    private String password;

    private String responseText;
    private XMLObject responseXML;

    private int readyState;
    private NativeFunction readyStateChangeFunction;

    private boolean asyncFlag;
    private Thread asyncThread;

    private int readTimeout = 0;           //no timeout by default
    
    public XMLHttpRequest() {
    }

    public void jsConstructor() {
    }

    public String getClassName() {
        return "XMLHttpRequest";
    }

    public void jsFunction_setRequestHeader(String headerName, String value) {
        if (readyState > 1) {
            throw new IllegalStateException("request already in progress");
        }

        if (requestHeaders == null) {
            requestHeaders = new HashMap<String, String>();
        }

        requestHeaders.put(headerName, value);
    }

    public Map<String, List<String>> jsFunction_getAllResponseHeaders() {
        if (readyState < 3) {
            throw new IllegalStateException(
                    "must call send before getting response headers");
        }
        return urlConnection.getHeaderFields();
    }

    public String jsFunction_getResponseHeader(String headerName) {
        return jsFunction_getAllResponseHeaders().get(headerName).toString();
    }

    public void jsFunction_open(String httpMethod, String url,
            boolean asyncFlag, String userName, String password) {

        if (readyState != 0) {
            throw new IllegalStateException("already open");
        }

        this.httpMethod = httpMethod;

        if (url.startsWith("http")) {
            this.url = url;
        } else {
            throw new IllegalArgumentException("URL protocol must be http: "
                    + url);
        }

        this.asyncFlag = asyncFlag;

        if ("undefined".equals(userName) || "".equals(userName)) {
            this.userName = null;
        } else {
            this.userName = userName;
        }
        if ("undefined".equals(password) || "".equals(password)) {
            this.password = null;
        } else {
            this.password = password;
        }
        if (this.userName != null) {
            setAuthenticator();
        }

        setReadyState(1);
    }

    private void setAuthenticator() {
        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password
                        .toCharArray());
            }
        });
    }

    public void jsFunction_send(Object o) {
        final String content = (o == null) ? "" : o.toString();
        if (asyncFlag) {
            Runnable r = new Runnable() {
                public void run() {
                    doSend(content);
                }
            };
            this.asyncThread = new Thread(r);
            asyncThread.start();
        } else {
            doSend(content);
        }
    }

    public void jsFunction_abort() {
        if (asyncThread != null) {
            asyncThread.interrupt();
        }
    }

    /**
     * @return Returns the readyState.
     */
    public int jsGet_readyState() {
        return readyState;
    }

    /**
     * @return Returns the responseText.
     */
    public String jsGet_responseText() {
        if (readyState < 2) {
            throw new IllegalStateException("request not yet sent");
        }
        return responseText;
    }

    /**
     * @return Returns the responseXML as a DOM Document.
     */
    public XMLObject jsGet_responseXML() {
        if (responseXML == null && responseText != null) {
            convertResponse2DOM();
        }
        return responseXML;
    }

    private void convertResponse2DOM() {
        try {

//            DOMParser parser = new DOMParser();
//            StringReader sr = new StringReader(jsGet_responseText());
//            parser.parse(new InputSource(sr));
//            Document xmlDoc = parser.getDocument();
            
            StringBufferInputStream is = new StringBufferInputStream(jsGet_responseText());
            Context cx = Context.getCurrentContext();
//            ScriptableObject scope = cx.initStandardObjects();
            Scriptable scope = getParentScope();
//            scope = getTopLevelScope(scope);

            
//            Object ctorVal = ScriptableObject.getProperty(scope, "XML");
            
            this.responseXML = XmlUtils.getXml(cx, scope, is, "UTF-8");


//        } catch (SAXException e) {
//            throw new RuntimeException("ex: " + e, e);
        } catch (IOException e) {
            throw new RuntimeException("ex: " + e, e);
        }
    }

    /**
     * @return Returns the htto status.
     */
    public int jsGet_status() {
        return httpStatus;
    }

    /**
     * @return Returns the http status text.
     */
    public String jsGet_statusText() {
        return httpStatusText;
    }

    /**
     * @return Returns the onreadystatechange.
     */
    public Object jsGet_onreadystatechange() {
        return readyStateChangeFunction;
    }

    /**
     * @param onreadystatechange
     *            The onreadystatechange to set.
     */
    public void jsSet_onreadystatechange(NativeFunction function) {
        readyStateChangeFunction = function;
    }

    /**
     * @param timeout
     *            The read timeout to set.
     */    
    public void jsFunction_setReadTimeout(int timeout) {  	
    	readTimeout = timeout;
    }
    
    private void doSend(String content) {

        connect(content);

        setRequestHeaders();

        try {
            urlConnection.connect();
        } catch (IOException e) {
            throw new RuntimeException("ex: " + e, e);
        }
        
        sendRequest(content);

        if ("POST".equals(this.httpMethod) || "GET".equals(this.httpMethod)) {
            readResponse();
        }

        setReadyState(4);

    }
    
    private void connect(String content) {
        try {

            URL url = new URL(this.url);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(httpMethod);
            if ("POST".equals(this.httpMethod) || content.length() > 0) {
                urlConnection.setDoOutput(true);
            }
            if ("POST".equals(this.httpMethod) || "GET".equals(this.httpMethod)) {
                urlConnection.setDoInput(true);
            }
            urlConnection.setUseCaches(false);
            urlConnection.setReadTimeout(readTimeout);

        } catch (MalformedURLException e) {
            throw new RuntimeException("MalformedURLException: " + e, e);
        } catch (IOException e) {
            throw new RuntimeException("IOException: " + e, e);
        }
    }

    private void setRequestHeaders() {
        if (this.requestHeaders != null) {
            for (Iterator<String> i = requestHeaders.keySet().iterator(); i.hasNext();) {
                String header = (String) i.next();
                String value = (String) requestHeaders.get(header);
                urlConnection.setRequestProperty(header, value);
            }
        }
    }

    private void sendRequest(String content) {
        try {

            if ("POST".equals(this.httpMethod) || content.length() > 0) {
                OutputStreamWriter out = new OutputStreamWriter(urlConnection
                        .getOutputStream(), "ASCII");
                out.write(content);
                out.flush();
                out.close();
            }

            httpStatus = urlConnection.getResponseCode();
            httpStatusText = urlConnection.getResponseMessage();

        } catch (IOException e) {
            throw new RuntimeException("IOException: " + e, e);
        }

        setReadyState(2);
    }

    private void readResponse() {
        try {

        	InputStream is = null;
        	
        	if (httpStatus == 400 || httpStatus == 500 )
        	{
        		is = urlConnection.getErrorStream();    //keep reading the stream 		
        	} else {
        		is = urlConnection.getInputStream();
        	}
            
            StringBuffer sb = new StringBuffer();

            setReadyState(3);

            int i;
            while ((i = is.read()) != -1) {
                sb.append((char) i);
            }
            is.close();

            this.responseText = sb.toString();

        } catch (IOException e) {
            throw new RuntimeException("IOException: " + e, e);
        }
    }

    private void setReadyState(int state) {
        this.readyState = state;
        callOnreadyStateChange();
    }

    private void callOnreadyStateChange() {
        if (readyStateChangeFunction != null) {
            Context cx = Context.enter();
            try {
                Scriptable scope = cx.initStandardObjects();
                readyStateChangeFunction.call(cx, scope, this, new Object[] {});
            } finally {
                Context.exit();
            }
        }
    }

    public static void register(ScriptableObject scope) {
        try {
            ScriptableObject.defineClass(scope, XMLHttpRequest.class);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }    
}