/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.execution_engine.platformaccess;

import com.mobeon.masp.callmanager.Call;
import com.mobeon.masp.callmanager.CallMediaTypes;
import com.mobeon.masp.callmanager.InboundCall;
import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.compiler.Constants;
import com.mobeon.masp.execution_engine.compiler.DebugInfo;
import com.mobeon.masp.execution_engine.ccxml.runtime.CCXMLExecutionContext;
import com.mobeon.masp.execution_engine.ccxml.runtime.Disconnecter;
import com.mobeon.masp.execution_engine.ccxml.compiler.operations.ConnectionCallBase;
import com.mobeon.masp.execution_engine.ccxml.Connection;
import com.mobeon.masp.execution_engine.platformaccess.util.MediaUtil;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediacontentmanager.*;
import com.mobeon.masp.mediaobject.ContentTypeMapper;
import com.mobeon.masp.mediaobject.IMediaObject;
import com.mobeon.masp.mediaobject.MediaMimeTypes;

import jakarta.activation.MimeType;
import java.util.*;

/**
 * Takes care of the mediacontent related functions in the PlatformAccess interface.
 *
 * @author ermmaha
 */
public class MediaManager {
    private static final String PROMPT = "prompt";
    private static final String FUNGREETING = "fungreeting";
    private static final String SWA = "swa";
    private static final String CALLMEDIA_TYPES_ARRAY_KEY = "callmediatypesarray";
    private static final String SELECTEDCALL_MEDIATYPES_KEY = "selectedcallmediatypes";

    private static ILogger log = ILoggerFactory.getILogger(MediaManager.class);
    private static Map<String, String> mediaPathMap;

    private ExecutionContext executionContext;
    private IMediaContentManager iMediaContentManager;

    private Map<String, IMediaContentResource> selectedResources =
            new HashMap<String, IMediaContentResource>();
    private Map<String, IMediaContentResourceProperties> resourceProperties =
            new HashMap<String, IMediaContentResourceProperties>();

    /**
     * Constructor.
     *
     * @param iMediaContentManager
     * @param executionContext
     */
    MediaManager(IMediaContentManager iMediaContentManager, ExecutionContext executionContext) {
        this.iMediaContentManager = iMediaContentManager;
        this.executionContext = executionContext;
    }

    /**
     * Returns a list of MediaContent identities for the specified Media Content Resource Type.
     *
     * @param mediaContentResourceType the type of Media Content Resource. (for example "prompt", "swa" or "fungreeting")
     * @param qualifiers
     * @return array with id's (array of mediaContentIdentity) for the mediacontents
     */
    String[] systemGetMediaContentIds(String mediaContentResourceType, IMediaQualifier[] qualifiers) {
        if (log.isDebugEnabled()) {
            log.debug("In systemGetMediaContentIds: mediaContentResourceType=" +
                    mediaContentResourceType + " qualifier=" + qualifiers);
        }

        if (!isValidResourceType(mediaContentResourceType)) {
            throw new PlatformAccessException(EventType.SYSTEMERROR,
                    "systemGetMediaContentIds:mediaContentResourceType=" + mediaContentResourceType, "Invalid mediaContentResourceType");
        }

        IMediaContentResource resource = getSelectedResource(mediaContentResourceType);
        if (resource != null) {
            try {
                List<String> ids = resource.getMediaContentIDs(qualifiers);
                if (log.isDebugEnabled()) {
                    log.debug("In systemGetMediaContentIds: mediaContentResourceType=" + mediaContentResourceType +
                            ", ids " + ids);
                }
                return ids.toArray(new String[ids.size()]);
            } catch (MediaContentManagerException e) {
                throw new PlatformAccessException(
                        EventType.SYSTEMERROR,
                        "systemGetMediaContentIds:mediaContentResourceType=" + mediaContentResourceType, e);
            }
        }
        throw new PlatformAccessException(EventType.SYSTEMERROR,
                "systemGetMediaContentIds:mediaContentResourceType=" + mediaContentResourceType, "No MediaContentResource is selected");
    }

    /**
     * Returns a list of IMediaObjects that represents the media content identity.
     *
     * @param mediaContentResourceType the type of Media Content Resource. (One of "prompt", "swa" or "fungreeting")
     * @param mediaContentId           the media content identifier
     * @return a list of IMediaObjects
     */
    IMediaObject[] systemGetMediaContent(String mediaContentResourceType, String mediaContentId) {
        return systemGetMediaContent(mediaContentResourceType, mediaContentId, null);
    }

    /**
     * Returns a list of IMediaObjects that represents the media content identity.
     *
     * @param mediaContentResourceType the type of Media Content Resource. (One of "prompt", "swa" or "fungreeting")
     * @param mediaContentId           the media content identifier
     * @param qualifiers               a list of qualifiers to the media content
     * @return a list of IMediaObjects
     */
    IMediaObject[] systemGetMediaContent(String mediaContentResourceType, String mediaContentId,
                                         IMediaQualifier[] qualifiers) {
        if (log.isDebugEnabled()) {
            log.debug("In systemGetMediaContent: mediaContentResourceType=" +
                    mediaContentResourceType + ", mediaContentId=" + mediaContentId +
                    " qualifier=" + qualifiers);
        }

        if (!isValidResourceType(mediaContentResourceType)) {
            throw new PlatformAccessException(EventType.SYSTEMERROR,
                    "systemGetMediaContent:mediaContentResourceType=" + mediaContentResourceType, "Invalid mediaContentResourceType");
        }

        IMediaContentResource resource = getSelectedResource(mediaContentResourceType);
        if (resource != null) {
            try {
                IMediaObject[] im = resource.getMediaContent(mediaContentId, qualifiers);
                if (log.isDebugEnabled()) {
                    log.debug("In systemGetMediaContent, mediaobjects=" + im);
                }
                return im;
            } catch (IllegalArgumentException e) {
                throw new PlatformAccessException(
                        EventType.SYSTEMERROR, "systemGetMediaContent:mediaContentId=" + mediaContentId, e);
            } catch (MediaContentManagerException e) {
                throw new PlatformAccessException(
                        EventType.SYSTEMERROR, "systemGetMediaContent:mediaContentResourceType=" + mediaContentResourceType +
                        "mediaContentId=" + mediaContentId, e);
            }
        }
        throw new PlatformAccessException(EventType.SYSTEMERROR,
                "systemGetMediaContent:mediaContentId=" + mediaContentId, "No MediaContentResource is selected");
    }

    /** TODO
     * Returns the MediaContentResourceProperties for the given mediaContentResourceType
     *
     * @param mediaContentResourceType the type of Media Content Resource. (One of "prompt", "swa" or "fungreeting")
     * @return the mediaContentResourceProperties
     */
    MimeType systemGetMediaContentResourceContentType(
    		String mediaContentResourceType, ContentTypeMapper contentTypeMapper) {

    	if (log.isDebugEnabled()) {
            log.debug("In systemGetMediaContentResourceProperties: mediaContentResourceType=" +
                    mediaContentResourceType);
        }

        if (!isValidResourceType(mediaContentResourceType)) {
            throw new PlatformAccessException(EventType.SYSTEMERROR,
                    "systemGetMediaContentResourceProperties:mediaContentResourceType=" + mediaContentResourceType, "Invalid mediaContentResourceType");
        }

        IMediaContentResource resource = getSelectedResource(mediaContentResourceType);
        if (resource != null) {
        	IMediaContentResourceProperties properties = resource.getMediaContentResourceProperties();
        	if (log.isDebugEnabled()) {
        		log.debug("In systemGetMediaContentResourceProperties, properties=" + properties);
        	}
        	List<MimeType> codecs = properties.getMediaCodecs();
        	MimeType contentType = contentTypeMapper.mapToContentType(new MediaMimeTypes(codecs));
        	if (contentType != null)
        		return contentType;	
        	else { 
                throw new PlatformAccessException(EventType.SYSTEMERROR,
                        "systemGetMediaContentResourceProperties:mediaContentResourceType=" + mediaContentResourceType, "No content type mapping found");
        	}        		
        }

        throw new PlatformAccessException(EventType.SYSTEMERROR,
                "systemGetMediaContentResourceProperties:mediaContentResourceType=" + mediaContentResourceType, "No MediaContentResource is selected");
    }

    
    /**
     * Set the Media Resources that shall be used for the call.
     *
     * @param language     the language to be used for the prompts
     * @param voiceVariant the variant to be used for the voiceprompts
     * @param videoVariant the variant to be used for the videoprompts
     */
    void systemSetMediaResources(String language, String voiceVariant, String videoVariant) {
        if (log.isDebugEnabled()) {
            log.debug("In systemSetMediaResources: " +
                    "Setting media resource for <" + PROMPT + ">");
        }

        systemSetMediaResource(PROMPT, language, voiceVariant, videoVariant);
    }

    /**
     * Set the Media Resource for a specified type: PROMPT, FUNGREETING or SWA
     * @param mediaContentResourceType
     * @param language
     * @param voiceVariant
     * @param videoVariant
     */
    void systemSetMediaResource(String mediaContentResourceType,
                                String language,
                                String voiceVariant,
                                String videoVariant) {

            if (log.isDebugEnabled()) {
            log.debug("In systemSetMediaResource: " +
                    "mediaContentResourceType=" + mediaContentResourceType +
                    ", language=" + language +
                    ", voiceVariant=" + voiceVariant +
                    ", videoVariant=" + videoVariant);
        }

        if (!isValidResourceType(mediaContentResourceType)) {
            String message = "Invalid MediaContentResourceType: " + mediaContentResourceType;
            if (log.isInfoEnabled()) {
                log.info("In systemSetMediaResource: " + message);
            }
            throw new PlatformAccessException(EventType.SYSTEMERROR,
                    "systemSetMediaResource", message);
        }

        // step 10 in sequence diagram fig.6 in MAS-FD
        List<IMediaContentResource> resources = getMediaContentResources(
                mediaContentResourceType, language, voiceVariant, videoVariant);

        if (resources.isEmpty()) {
            if (log.isDebugEnabled())
                log.debug("In systemSetMediaResource: resources are empty ");
            throw new PlatformAccessException(EventType.SYSTEMERROR,
                    "systemSetMediaResource:", "MediaContentResources are empty");
        }

        MediaContentResourceProperties properties = new MediaContentResourceProperties();
        properties.setLanguage(language);
        properties.setVoiceVariant(voiceVariant);
        properties.setVideoVariant(videoVariant);
        properties.setType(mediaContentResourceType);
        resourceProperties.put(mediaContentResourceType.toLowerCase(), properties);

        selectedResources.put(mediaContentResourceType.toLowerCase(), null);

        retrieveMediaContentResource(mediaContentResourceType, resources);
    }

    /**
     * Set the early Media Resources that shall be used for the call.
     *
     * @param language     the language to be used for the prompts
     * @param voiceVariant the variant to be used for the voiceprompts
     * @param videoVariant the variant to be used for the videoprompts
     */
    void systemSetEarlyMediaResource(String language, String voiceVariant, String videoVariant) {
        if (log.isDebugEnabled()) {
            log.debug("In systemSetEarlyMediaResource: language=" + language +
                    ", voiceVariant=" + voiceVariant + ", videoVariant=" + videoVariant);
        }

        // step 10 in sequence diagram fig.6 in MAS-FD
        List<IMediaContentResource> resources = getMediaContentResources(
                PROMPT, language, voiceVariant, videoVariant);

        if (resources.isEmpty()) {
            if (log.isDebugEnabled()) log.debug("In systemSetEarlyMediaResource: resources are empty ");
            throw new PlatformAccessException(EventType.SYSTEMERROR,
                    "systemSetEarlyMediaResource:", "MediaContentResources are empty");
        }

        // step 11 in sequence diagram fig.6 in MAS-FD
        ArrayList<CallMediaTypes> callMediaTypes = getCallMediaTypes(resources);

        if (log.isDebugEnabled()) log.debug("In systemSetEarlyMediaResource: adding callmediatypesarray to session ");

        executionContext.getSession().setData(CALLMEDIA_TYPES_ARRAY_KEY, callMediaTypes.toArray(new CallMediaTypes[callMediaTypes.size()]));

        MediaContentResourceProperties properties = new MediaContentResourceProperties();
        properties.setLanguage(language);
        properties.setVoiceVariant(voiceVariant);
        properties.setVideoVariant(videoVariant);
        properties.setType(PROMPT);
        resourceProperties.put(PROMPT.toLowerCase(), properties);

        Call call = executionContext.getCurrentConnection().getCall();

        if (call instanceof InboundCall) {
            if(executionContext instanceof CCXMLExecutionContext){
                superviseResponse();
                InboundCall inboundCall = (InboundCall) call;
                inboundCall.negotiateEarlyMediaTypes();
            } else {
                executionContext.getEventHub().fireContextEvent(Constants.Event.ERROR_SEMANTIC,
                        "systemSetEarlyMediaResource is not allowed in VoiceXML", DebugInfo.getInstance());
            }
        }
    }

    private void superviseResponse() {
        String messageForFiredEvent = "Expected event for systemSetEarlyMediaResource did not arrive in time";
        String[] eventNames = {Constants.Event.MOBEON_PlATFORM_EARLYMEDIARESOURCEAVAILABLE,
                Constants.Event.MOBEON_PlATFORM_EARLYMEDIARESOURCEFAILED,
                Constants.Event.CONNECTION_DISCONNECTED,
                Constants.Event.CONNECTION_DISCONNECT_HANGUP,
                Constants.Event.ERROR_CONNECTION,
                Constants.Event.CONNECTION_FAILED,
                Constants.Event.ERROR_NOTALLOWED};
        CCXMLExecutionContext ex = (CCXMLExecutionContext) executionContext;
        Connection currentConnection = executionContext.getCurrentConnection();
        ex.waitForEvent(Constants.Event.ERROR_CONNECTION, messageForFiredEvent,currentConnection.getCallManagerWaitTimeout(), new Disconnecter(currentConnection),currentConnection, eventNames);
    }

    private List<IMediaContentResource> getMediaContentResources(String type,
                                                                 String language,
                                                                 String voiceVariant,
                                                                 String videoVariant) {
        MediaContentResourceProperties resourceProperties = new MediaContentResourceProperties();
        resourceProperties.setLanguage(language);
        resourceProperties.setVoiceVariant(voiceVariant);
        resourceProperties.setVideoVariant(videoVariant);
        resourceProperties.setType(type);

        return iMediaContentManager.getMediaContentResource(resourceProperties);
    }

    private void retrieveMediaContentResource(String mediaContentResourceType,
                                              List<IMediaContentResource> resources) {

        // If the call manager already has set "selectedcallmediatypes" we must look in the list we got from MCM and
        // select the media resource that has the same encodings as the "selectedcallmediatypes"

        CallMediaTypes selectedCallMediaTypes;
        if ((selectedCallMediaTypes = (CallMediaTypes) executionContext.getSession().getData(SELECTEDCALL_MEDIATYPES_KEY)) == null) {
            if (log.isDebugEnabled())
                log.debug("In retrieveMediaContentResource: " +
                        SELECTEDCALL_MEDIATYPES_KEY + " is null, adding callmediatypesarray to session ");

            ArrayList<CallMediaTypes> callMediaTypes = getCallMediaTypes(resources);
            executionContext.getSession().setData(CALLMEDIA_TYPES_ARRAY_KEY, callMediaTypes.toArray(new CallMediaTypes[callMediaTypes.size()]));

        } else {

            if (log.isDebugEnabled())
                log.debug("In retrieveMediaContentResource: " +
                        SELECTEDCALL_MEDIATYPES_KEY + " is NOT null, selecting mediatype...");

            IMediaContentResource resource = selectMediaContentResource(selectedCallMediaTypes, resources);
            if (resource != null) {
                selectedResources.put(mediaContentResourceType.toLowerCase(), resource);
            }
        }
    }

    private ArrayList<CallMediaTypes> getCallMediaTypes(List<IMediaContentResource> resources) {
        ArrayList<CallMediaTypes> callMediaTypes = new ArrayList<CallMediaTypes>();
        Iterator<IMediaContentResource> it = resources.iterator();
        while (it.hasNext()) {
            IMediaContentResource resource = it.next();

            if (log.isDebugEnabled()) {
                IMediaContentResourceProperties prop = resource.getMediaContentResourceProperties();
                log.debug("In getCallMediaTypes: " +
                        "adding resource with properties " + prop + " to callMediaTypes list");
            }

            MediaMimeTypes mediaMimeTypes = new MediaMimeTypes(resource.getMediaContentResourceProperties().getMediaCodecs());
            CallMediaTypes cTypes = new CallMediaTypes(mediaMimeTypes, resource);
            callMediaTypes.add(cTypes);
        }
        return callMediaTypes;
    }

    /**
     * Returns the selected IMediaContentResource depending on the the mediaContentResourceType parameter.
     * If the type is "prompt" then selectedResourcePrompt is first retrived and then saved.
     *
     * @param mediaContentResourceType
     * @return the selected IMediaContentResource object
     */
    private IMediaContentResource getSelectedResource(String mediaContentResourceType) {
        IMediaContentResource resource =
                selectedResources.get(mediaContentResourceType.toLowerCase());

        if (resource == null) {

            // No resource has been selected yet for this type. Fetch all matching
            // resources from MCM using the properties for the resource type.

            IMediaContentResourceProperties properties =
                    resourceProperties.get(mediaContentResourceType.toLowerCase());
            if (properties == null && !mediaContentResourceType.equalsIgnoreCase(PROMPT)) {
                if (log.isDebugEnabled())
                    log.debug("In getSelectedResource: " +
                            "No resource properties set for media content resource type <" +
                            mediaContentResourceType + ">, using properties for <"
                            + PROMPT + "> instead.");

                properties = resourceProperties.get(PROMPT);
            }
            if (properties == null) {
                if (log.isDebugEnabled())
                    log.debug("In getSelectedResource: " +
                            "No properties set for resource type <" + PROMPT + ">. " +
                            "Cannot determine which resource to use.");

                return null;
            } else {
                List<IMediaContentResource> resources = getMediaContentResources(
                                mediaContentResourceType,
                                properties.getLanguage(),
                                properties.getVoiceVariant(),
                                properties.getVideoVariant());

                CallMediaTypes callMediaTypes = (CallMediaTypes)
                        executionContext.getSession().getData(SELECTEDCALL_MEDIATYPES_KEY);

                if (callMediaTypes != null) {
                    resource = selectMediaContentResource(callMediaTypes, resources);
                    if (resource != null) {
                        selectedResources.put(mediaContentResourceType.toLowerCase(), resource);
                    }
                } else {
                    if (log.isDebugEnabled())
                        log.debug("In getSelectedResource: " +
                                "No CallMediaTypes found in the session");

                }
            }
        }
        return resource;
    }

    /**
     * @param callMediaTypes The Call Media Types to look for
     * @param resources A list of Media Content Resources
     * @return A Media Content Resource matching the given Call Media Types
     */
    private IMediaContentResource selectMediaContentResource(CallMediaTypes callMediaTypes,
                                                             List<IMediaContentResource> resources) {
        if (log.isDebugEnabled()) {
            log.debug("In selectMediaContentResource: " +
                    "Trying to select a MediaContentResource with " +
                    callMediaTypes.toString());
        }

        // Look for a resource matching the selected call media types.

        for (IMediaContentResource resource : resources) {
            MediaMimeTypes mediaMimeTypes = new MediaMimeTypes(
                    resource.getMediaContentResourceProperties().getMediaCodecs());
            if (callMediaTypes.getOutboundMediaTypes().compareTo(mediaMimeTypes)) {
                if (log.isDebugEnabled()) {
                    log.debug(("In selectMediaContentResource: " +
                            "The MediaContentResource <" + resource.getID() +
                            "> matched the selected call media types."));
                }
                return resource;
            }
        }

        // No resource matched the selected call media types.
        // If this is a video call, look for a match on audio codec only.

        if (callMediaTypes.getOutboundMediaTypes().getNumberOfMimeTypes() > 1) {
            MimeType audioCallMediaType = callMediaTypes.getMimeType(CallMediaTypes.CallMediaType.AUDIO);
            for (IMediaContentResource resource : resources) {
                if (resource.getMediaContentResourceProperties().hasMatchingCodec(audioCallMediaType)) {
                    if (log.isDebugEnabled()) {
                        log.debug(("In selectMediaContentResource: " +
                                "The MediaContentResource <" + resource.getID() +
                                "> matched the audio media type."));
                    }
                    return resource;
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("In selectMediaContentResource: " +
                    "No MediaContentResource matches the selected call media types.");
        }
        return null;
    }

    private boolean isValidResourceType(String mediaContentResourceType) {
        if (mediaContentResourceType == null) return false;
        return (mediaContentResourceType.equalsIgnoreCase(PROMPT) ||
                mediaContentResourceType.equalsIgnoreCase(FUNGREETING) ||
                mediaContentResourceType.equalsIgnoreCase(SWA));
    }

    String systemGetMediaContentPath(String baseURI) {
        if (mediaPathMap == null) {
            mediaPathMap = MediaUtil.getMediaPathMap(baseURI);
            if (log.isDebugEnabled()) {
                log.debug("mediaPathMap=" + mediaPathMap);
            }
        }

        IMediaContentResourceProperties prop = resourceProperties.get(PROMPT);
        String lang = prop.getLanguage();

        String path = mediaPathMap.get(lang);

        return path != null ? path : ""; 
    }
}
