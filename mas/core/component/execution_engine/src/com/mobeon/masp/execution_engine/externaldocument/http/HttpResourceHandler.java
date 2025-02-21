package com.mobeon.masp.execution_engine.externaldocument.http;

import com.mobeon.masp.execution_engine.xml.XPP3CompilerReader;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaObjectException;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.XPP3Reader;
import org.xmlpull.v1.XmlPullParserException;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A class used to fetch external resources via HTTP.
 *
 * @author ermmaha
 */
public class HttpResourceHandler implements IHttpResourceHandler {
    private static ILogger log = ILoggerFactory.getILogger(HttpResourceHandler.class);

    private static Map<String, String> staticRequestProperties = new HashMap<String, String>();

    static {
        staticRequestProperties.put("User-Agent", "MAS/4.1");
    }

    /**
     * Connect timeout value in milliseconds
     */
    private int connectTimeout = 10 * 1000;
    /**
     * Read timeout value in milliseconds
     */
    private int readTimeout = 10 * 1000;

    private long ifmodifiedsince;

    HttpResourceHandler() {
    }

    public void setIfModifiedSince(long ifmodifiedsince) {
        this.ifmodifiedsince = ifmodifiedsince;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public HttpDocumentResponse requestDocument(URI documentURI) {
        XPP3Reader r = new XPP3CompilerReader();
        try {
            HttpURLConnection conn = getConnection(documentURI);
            addStaticRequestProperties(conn); // set User-Agent, etc
            conn.setRequestProperty("Accept", "application/vxml+xml;application/ccxml+xml");
            if (ifmodifiedsince > 0) conn.setIfModifiedSince(ifmodifiedsince);

            if (log.isDebugEnabled()) debugURLConnection(conn);

            HttpDocumentResponse response = new HttpDocumentResponse();
            response.setResponseCode(conn.getResponseCode());
            response.setLastModified(conn.getLastModified());
            response.setExpires(conn.getExpiration());
            response.setDate(conn.getDate());

            if (conn.getResponseCode() == 200) {
                Document doc = r.read(conn.getInputStream());
                response.setDocument(doc);
            }
            return response;

        } catch (MalformedURLException e) {
            log.error("Error in requestDocument " + e);
        } catch (DocumentException e) {
            log.error("Error in requestDocument ", e);
        } catch (IOException e) {
            log.error("Error in requestDocument " + e);
        } catch (XmlPullParserException e) {
            log.error("Error in requestDocument ", e);
        }
        return null;
    }

    public HttpMediaResponse requestMedia(URI documentURI, IMediaObjectFactory mediaObjectFactory) {
        // ToDo remove hardcodings for audio/wav and wav
        try {
            HttpURLConnection conn = getConnection(documentURI);
            addStaticRequestProperties(conn); // set User-Agent, etc
            conn.setRequestProperty("Accept", "audio/wav"); // maybe x-wav here
            if (ifmodifiedsince > 0) conn.setIfModifiedSince(ifmodifiedsince);

            if (log.isDebugEnabled()) debugURLConnection(conn);

            HttpMediaResponse response = new HttpMediaResponse();
            response.setResponseCode(conn.getResponseCode());
            response.setLastModified(conn.getLastModified());
            response.setExpires(conn.getExpiration());
            response.setDate(conn.getDate());

            if (conn.getResponseCode() == 200) {
                String contentType = conn.getContentType();
                // The Tomcat server in MWS is configured to answer with x-wav for wav files. Removes this line when
                // MAS can handle audio/x-wav
                if (contentType.equals("audio/x-wav")) contentType = "audio/wav";

                IMediaObject mediaObject = mediaObjectFactory.create(
                        conn.getInputStream(), 0, new MimeType(contentType));
                // 8000 is default byte buffer size in MediaObjectFactory
                // Fileextension is not set in MediaObjectFactory, must set it here
                mediaObject.getMediaProperties().setFileExtension("wav");
                response.setMediaObject(mediaObject);
            }
            return response;
        } catch (MalformedURLException e) {
            log.error("Error in requestMedia " + e);
        } catch (IOException e) {
            log.error("Error in requestMedia " + e);
        } catch (MediaObjectException e) {
            log.error("Error in requestMedia ", e);
        } catch (MimeTypeParseException e) {
            log.error("Error in requestMedia ", e);
        }
        return null;
    }

    /**
     * Opens the connection. Set timeout values.
     *
     * @param documentURI
     * @return
     * @throws java.io.IOException
     */
    private HttpURLConnection getConnection(URI documentURI) throws IOException {
        URL docURL = documentURI.toURL();
        HttpURLConnection conn = (HttpURLConnection) docURL.openConnection();
        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);
        return conn;
    }

    /**
     * Sets the static http request headers that should be present in every request.
     *
     * @param conn
     */
    private void addStaticRequestProperties(URLConnection conn) {
        Iterator<String> it = staticRequestProperties.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            String value = staticRequestProperties.get(key);
            conn.setRequestProperty(key, value);
        }
    }

    private void debugURLConnection(URLConnection conn) {
        Map<String, List<String>> headers = conn.getHeaderFields();

        StringBuffer debugBuf = new StringBuffer();
        debugBuf.append("\n");
        Iterator<String> it = headers.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            List<String> header = headers.get(key);
            debugBuf.append(key);
            debugBuf.append("=");
            debugBuf.append(header);
            debugBuf.append("\n");
        }
        log.debug(debugBuf);
    }
}
