package com.mobeon.masp.execution_engine.externaldocument;

import com.mobeon.masp.execution_engine.Module;
import com.mobeon.masp.execution_engine.ModuleCollection;
import com.mobeon.masp.execution_engine.compiler.ApplicationCompilerImpl;
import com.mobeon.masp.execution_engine.compiler.IApplicationCompiler;
import com.mobeon.masp.execution_engine.externaldocument.http.HttpDocumentResponse;
import com.mobeon.masp.execution_engine.externaldocument.http.HttpMediaResponse;
import com.mobeon.masp.execution_engine.externaldocument.http.HttpResourceHandlerFactory;
import com.mobeon.masp.execution_engine.externaldocument.http.IHttpResourceHandler;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;
import com.mobeon.masp.util.TimeValueParser;
import com.mobeon.masp.util.Tools;
import org.dom4j.Document;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Class used to get resources from an external interface. Contains two caches (one for documents and another for media)
 * that is updated automatically.
 *
 * @author ermmaha
 */
public class ResourceLocator {
    private static final String HTTP = "http";
    private static final ILogger log = ILoggerFactory.getILogger(ResourceLocator.class);
    private static final Object fetchMediaLock = new Object();
    private static final Object fetchDocumentLock = new Object();
    /**
     * Cache for documents
     */
    private static ResourceCache documentCache = new ResourceCache("DocumentCache");
    /**
     * Cache for media (30min timeout, maxsize 50)
     */
    private static ResourceCache mediaCache = new ResourceCache("MediaCache", 30 * 60 * 1000, 50);

    private IApplicationCompiler applicationCompiler = new ApplicationCompilerImpl();
    private HttpResourceHandlerFactory httpResourceHandlerFactory = new HttpResourceHandlerFactory();
    private IMediaObjectFactory mediaObjectFactory;

    public ResourceLocator() {
    }

    public void setApplicationCompiler(IApplicationCompiler applicationCompiler) {
        this.applicationCompiler = applicationCompiler;
    }

    public void setMediaObjectFactory(IMediaObjectFactory mediaObjectFactory) {
        this.mediaObjectFactory = mediaObjectFactory;
    }

    public void setHttpResourceHandlerFactory(HttpResourceHandlerFactory httpResourceHandlerFactory) {
        this.httpResourceHandlerFactory = httpResourceHandlerFactory;
    }

    /**
     * Retrieves a external media object. It looks in the cache first and if it exist there (and not aged) it is
     * retrieved from the cache. If not, the media file is retrieved from an external interface and is put in the cache
     * and is returned.
     *
     * @param url
     * @param maxAgeStr    if null no age is checked and media is not updated if present in cache
     * @param fetchtimeout string with timeout value when fetching data
     * @return the media object
     */
    public IMediaObject getMedia(String url, String maxAgeStr, String fetchtimeout) {
        if (log.isDebugEnabled()) log.debug("Retrieving media for " + url);
        boolean hasAged = false;
        Resource resource = mediaCache.get(url);
        if (resource != null) {
            if (log.isDebugEnabled()) log.debug("Got cached media, checking maxage (" + maxAgeStr + ")");
            if (maxAgeStr != null) {
                long maxage = Tools.toMillis(TimeValueParser.getTime(maxAgeStr));
                if (System.currentTimeMillis() <= resource.getAge() + maxage) {
                    if (log.isDebugEnabled()) log.debug("Age on media not larger than maxage returning it");
                    return (IMediaObject) resource.getResource();
                } else {
                    if (log.isDebugEnabled()) log.debug("Media has aged, refreshing from server");
                    hasAged = true;
                }
            } else {
                return (IMediaObject) resource.getResource();
            }
        }

        URI documentURI;
        try {
            documentURI = new URI(url);
        } catch (URISyntaxException e) {
            log.error("Failed to fetch media " + url, e);
            return null;
        }

        synchronized (fetchMediaLock) {
            resource = mediaCache.get(url);
            if (!hasAged && resource != null) {
                if (log.isDebugEnabled()) log.debug("Cached media created while waiting on lock.");
            } else {
                // Even if the resource has aged the lastmodified time can be sent in the http request
                // and let the server decide if the content is still valid by answering with a 304.
                long lastModifiedAge = 0;
                if (hasAged && resource != null) {
                    lastModifiedAge = resource.getAge();
                }
                HttpMediaResponse response = fetchMedia(documentURI, lastModifiedAge, fetchtimeout);
                if (response != null) {
                    if (response.getResponseCode() == 200) {
                        if (response.getMediaObject() != null) {
                            if (log.isDebugEnabled()) log.debug("Server responded with 200, updating cache.");
                            resource = new Resource(response.getMediaObject(), response.getLastModified());
                            mediaCache.put(url, resource);
                        }
                    } else if (response.getResponseCode() == 304) {
                        if (log.isDebugEnabled()) log.debug("Server responded with 304, media still valid.");
                    } else {
                        // error 500, 403 etc
                        log.error("Server responded with " + response.getResponseCode() + " media could not be fetched");
                    }
                }
            }
        }
        return resource != null ? (IMediaObject) resource.getResource() : null;
    }

    /**
     * Retrieves a external document. It looks in the cache first and if it exist there (and not aged) it is
     * retrieved from the cache. If not, the document file is retrieved from an external interface compiled and put in
     * the cache and is returned.
     *
     * @param url
     * @param maxage
     * @param fetchtimeout string with timeout value when fetching data
     * @param collection
     * @return the document object
     */
    public Module getDocument(String url, String maxage, String fetchtimeout, ModuleCollection collection) {
        if (log.isDebugEnabled()) log.debug("Retrieving document for " + url);
        if (url.startsWith(HTTP)) {
            return getExternalDocument(url, maxage, fetchtimeout, collection);
        }
        return collection.get(url);
    }

    private Module getExternalDocument(String url, String maxAgeStr, String fetchTimeoutStr, ModuleCollection collection) {
        boolean hasAged = false;
        Resource resource = documentCache.get(url);
        if (resource != null) {
            if (log.isDebugEnabled()) log.debug("Got cached document, checking maxage (" + maxAgeStr + ")");
            if (maxAgeStr != null) {
                long maxage = Tools.toMillis(TimeValueParser.getTime(maxAgeStr));
                if (System.currentTimeMillis() <= resource.getAge() + maxage) {
                    if (log.isDebugEnabled()) log.debug("Age on document not larger than maxage returning it");
                    return (Module) resource.getResource();
                } else {
                    if (log.isDebugEnabled()) log.debug("Document has aged, refreshing from server");
                    hasAged = true;
                }
            } else {
                return (Module) resource.getResource();
            }
        }

        URI documentURI;
        try {
            documentURI = new URI(url);
        } catch (URISyntaxException e) {
            log.error("Failed to fetch document " + url, e);
            return null;
        }

        synchronized (fetchDocumentLock) {
            resource = documentCache.get(url);
            if (!hasAged && resource != null) {
                if (log.isDebugEnabled()) log.debug("Cached document created while waiting on lock.");
            } else {
                long lastModifiedAge = 0;
                if (hasAged && resource != null) {
                    lastModifiedAge = resource.getAge();
                }
                HttpDocumentResponse response = fetchDocument(documentURI, lastModifiedAge, fetchTimeoutStr);
                if (response != null) {
                    if (response.getResponseCode() == 200) {
                        if (response.getDocument() != null) {
                            Module module = compileDocument(documentURI, response.getDocument(), collection);
                            resource = new Resource(module, response.getLastModified());
                            documentCache.put(url, resource);
                        }
                    } else if (response.getResponseCode() == 304) {
                        if (log.isDebugEnabled()) log.debug("Server responded with 304, document still valid.");
                    } else {
                        log.error("Server responded with " + response.getResponseCode() + " document could not be fetched");
                    }
                }
            }
        }
        return resource != null ? (Module) resource.getResource() : null;
    }

    private HttpMediaResponse fetchMedia(URI documentURI, long ifmodifiedSince, String fetchtimeout) {
        if (log.isDebugEnabled()) log.debug("fetching media " + documentURI + ", fetchtimeout=" + fetchtimeout);
        IHttpResourceHandler httpResourceHandler = httpResourceHandlerFactory.create();
        httpResourceHandler.setIfModifiedSince(ifmodifiedSince);
        setTimeoutValues(fetchtimeout, httpResourceHandler);
        return httpResourceHandler.requestMedia(documentURI, mediaObjectFactory);
    }

    private HttpDocumentResponse fetchDocument(URI documentURI, long ifmodifiedSince, String fetchtimeout) {
        if (log.isDebugEnabled()) log.debug("fetching document " + documentURI + ", fetchtimeout=" + fetchtimeout);
        IHttpResourceHandler httpResourceHandler = httpResourceHandlerFactory.create();
        httpResourceHandler.setIfModifiedSince(ifmodifiedSince);
        setTimeoutValues(fetchtimeout, httpResourceHandler);
        return httpResourceHandler.requestDocument(documentURI);
    }

    private void setTimeoutValues(String fetchtimeout, IHttpResourceHandler httpResourceHandler) {
        if (fetchtimeout != null) {
            long timeOut = Tools.toMillis(TimeValueParser.getTime(fetchtimeout));
            httpResourceHandler.setConnectTimeout((int) timeOut);
            httpResourceHandler.setReadTimeout((int) timeOut);
        }
    }

    /**
     * Compile the requested document into a {@link Module}.
     *
     * @param documentURI - The document to compile
     * @param collection  - The module collection used for retrieving application attributes etc.
     * @return The compiled document as an {@link Module}, or null in case of an error.
     */
    private Module compileDocument(URI documentURI, Document document, ModuleCollection collection) {
        if (log.isDebugEnabled()) log.debug("Compiling document " + documentURI);
        return applicationCompiler.compileDocument(document, documentURI, collection);
    }
}
