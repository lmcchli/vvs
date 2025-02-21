/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import jakarta.activation.MimeType;
import java.util.List;

/**
 * A <code>MediaResourceFilter</code> filter matches the properties
 * of a {@link IMediaContentResource} to a specified {@link MediaContentResourceProperties}.
 * <p/>
 * See the filter method for the filter functionality.
 */
public class MediaResourceFilter {

    /**
     * The {@link com.mobeon.common.logging.ILogger} logger used for logging purposes.
     */
    protected static final ILogger LOGGER = ILoggerFactory.getILogger(MediaResourceFilter.class);

    /**
     * If a property in the passed MediaContentResourceProperties is null,
     * the IMediaContentResource will always match on that resource.
     * <p/>
     * Otherwise:
     * This filter does the following comparisons on each property in the
     * passed {@link MediaContentResourceProperties} and
     * {@link IMediaContentResource} (all must be true):
     * <ul>
     * <li>Language: If the language property in the passed
     * filterProperties is not null
     * and not empty after whitespace trimmed:
     * the the language in the resource must be equal
     * (ignore-case comparison).</li>
     * <p/>
     * <li>Type:    If the type property in the passed
     * filterProperties is not null,
     * and not empty after whitespace trimmed:
     * the type in the resource must be equal to it.
     * (ignore-case comparison).</li>
     * <p/>
     * <p/>
     * <li>Variant: If the passed filterProperties has voice variant or
     * video variant the resource must have the voice OR the video variant.
     * (ignore-case comparison).</li>
     * <p/>
     * <li>Voice:   If the passed filterProperties is
     * voice the resource must be voice.
     * Further, if above is true and if the voice variant of
     * the filterProperties is non-null, the voice variant of
     * the resource must be equal. (ignore-case comparison).</li>
     * <p/>
     * <li>Codecs:  If the filterProperties has codecs the same codecs must
     * be in the resource.
     * </ul>
     *
     * @param filterProperties The {@link MediaContentResourceProperties} that the
     *                         resource is to match.
     * @param resource         The {@link  IMediaContentResource} that is to match
     *                         the passed properties.
     * @return true if the resouce matches the criterias listed above.
     */
    public boolean filter(MediaContentResourceProperties filterProperties,
                          IMediaContentResource resource) {
        // Always match if
        if (filterProperties == null) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Passed filter is null so return true");
            }
            return true;
        }
        IMediaContentResourceProperties resourceProperties =
                resource.getMediaContentResourceProperties();
        // language
        String language = filterProperties.getLanguage();
        if (!languageValid(language,  resourceProperties)) {
            return false;
        }
        // type
        String type = filterProperties.getType();
        if (type != null && type.trim().length() > 0) {
            if (!type.trim().equalsIgnoreCase(
                    resourceProperties.
                            getType().trim())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Resource type " + resourceProperties.getType()
                            + " did not match specified " + type);
                }
                return false;
            }
        }
        // Variant
        if (!variantValid(filterProperties.getVoiceVariant(),
                filterProperties.getVideoVariant(), resourceProperties)) {
            return false;
        }
        // Codecs
        if (!compareCodecs(filterProperties.getMediaCodecs(), resourceProperties)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Resource codecs did not match given codecs");
            }
            return false;
        }
        return true;
    }

    /**
     * Validates that the language given matches the language in
     * the given resource properties.
     * @param language              The language to match.
     * @param resourceProperties    The properties of the resource.
     * @return  true if language is specified, and in that case
     *          matches language in resource.
     */
    private boolean languageValid(String language,  IMediaContentResourceProperties resourceProperties) {
        if (language != null && language.trim().length() > 0) {
            if (!language.trim().equalsIgnoreCase(
                    resourceProperties.
                            getLanguage().trim())) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Resource language " + resourceProperties.getLanguage()
                            + " did not match specified " + language);
                }
                return false;
            }
        }
        return true;
    }
    private boolean variantValid(String voiceVariant, String videoVariant,
                                 IMediaContentResourceProperties resourceProperties) {

        boolean videoNull = true;
        boolean videoValid = false;
        if (videoVariant != null && videoVariant.trim().length() > 0) {
          videoNull = false;
          if ((resourceProperties.getVideoVariant() != null) &&
                (videoVariant.trim().equalsIgnoreCase(
                    resourceProperties.getVideoVariant().trim()))) {
                videoValid = true;
            } else {
              if (LOGGER.isDebugEnabled()) {
                  LOGGER.debug("Resource video variant " + resourceProperties.getVideoVariant()
                          + " did not match specified " + videoVariant);
              }
          }
        }
        boolean voiceNull = true;
        boolean voiceValid = false;
        if (voiceVariant != null && voiceVariant.trim().length() > 0) {
            voiceNull = false;

            if ((resourceProperties.getVoiceVariant() != null) &&
                (voiceVariant.trim().equalsIgnoreCase(
                    resourceProperties.getVoiceVariant().trim()))) {
                voiceValid = true;
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Resource voice variant " + resourceProperties.getVoiceVariant()
                            + " did not match specified " + voiceVariant);
                }
            }
        }
        if (voiceNull && videoNull) {
            return true;
        } else if (voiceNull && !videoNull) {
            // video variant determines
            return videoValid;
        } else if (videoNull && !voiceNull) {
            // voice variant determines
            return voiceValid;
        } else {
            // both voice and video is given
            return voiceValid || videoValid;
        }

    }
    /**
     * Compares the codecs in the passed list to the
     * codecs of the resource properites.
     * <p/>
     * IF the filterCodecs holds any codecs the number of codecs
     * in the resource must be
     * equal and each codec in the filter must be present in the
     * resource.
     *
     * @param filterCodecs       The criteria-list of codecs.
     * @param resourceProperties The resource that must have the
     *                           codecs.
     * @return true If the resource have same number of codecs
     *         as the specified list and each codec in the
     *         list is present in the resource.
     */
    private boolean compareCodecs(
            List<MimeType> filterCodecs,
            IMediaContentResourceProperties resourceProperties) {
        if (filterCodecs.size() > 0) {
            if (filterCodecs.size() != resourceProperties.getMediaCodecs().size()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Resource codecs did not match given codecs as the number " +
                            "of codecs is not same. Resource has "
                            + resourceProperties.getMediaCodecs().size() +
                            ", filter has " + filterCodecs.size());
                }
                return false;
            } else {
                for (MimeType mimeType : filterCodecs) {
                    if (!resourceProperties.hasMatchingCodec(mimeType)) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Resource has not the required codec " + mimeType.toString());
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
