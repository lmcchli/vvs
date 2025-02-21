package com.mobeon.masp.mediacontentmanager;

import junit.framework.TestCase;

import jakarta.activation.MimeType;

/**
 * @author mmawi
 */
public class MediaContentResourcePropertiesTest extends TestCase {

    MediaContentResourceProperties mediaContentResourceProperties;

    protected void setUp() throws Exception {
        super.setUp();
        mediaContentResourceProperties = new MediaContentResourceProperties();
        mediaContentResourceProperties.setLanguage("sv");
        mediaContentResourceProperties.setVideoVariant(null);
        mediaContentResourceProperties.setVoiceVariant("voice");
        mediaContentResourceProperties.setType("prompt");
    }

    public void testEqualsNoCodecs() throws Exception {
        MediaContentResourceProperties otherProperties = new MediaContentResourceProperties();

        // Test equals
        otherProperties.setLanguage("sv");
        otherProperties.setVideoVariant(null);
        otherProperties.setVoiceVariant("voice");
        otherProperties.setType("prompt");
        assertTrue("Should be equal: " + mediaContentResourceProperties.toString()
                + "; " + otherProperties.toString(),
                mediaContentResourceProperties.equals(otherProperties));

        // Test not equal language
        otherProperties.setLanguage("en");
        assertFalse("Should not be equal: " + mediaContentResourceProperties.toString()
                + "; " + otherProperties.toString(),
                mediaContentResourceProperties.equals(otherProperties));

        // Test not equal voiceVariant
        otherProperties.setLanguage("sv");
        otherProperties.setVoiceVariant("test");
        assertFalse("Should not be equal: " + mediaContentResourceProperties.toString()
                + "; " + otherProperties.toString(),
                mediaContentResourceProperties.equals(otherProperties));

        // Test not equal voiceVariant
        otherProperties.setVoiceVariant(null);
        assertFalse("Should not be equal: " + mediaContentResourceProperties.toString()
                + "; " + otherProperties.toString(),
                mediaContentResourceProperties.equals(otherProperties));

        // Test not equal videoVariant
        otherProperties.setVoiceVariant("voice");
        otherProperties.setVideoVariant("test");
        assertFalse("Should not be equal: " + mediaContentResourceProperties.toString()
                + "; " + otherProperties.toString(),
                mediaContentResourceProperties.equals(otherProperties));

        // Test not equal type
        otherProperties.setVideoVariant(null);
        otherProperties.setType("fungreeting");
        assertFalse("Should not be equal: " + mediaContentResourceProperties.toString()
                + "; " + otherProperties.toString(),
                mediaContentResourceProperties.equals(otherProperties));
    }

    public void testEqualsWithCodecs() throws Exception {
        MediaContentResourceProperties otherProperties = new MediaContentResourceProperties();

        mediaContentResourceProperties.addCodec(new MimeType("audio/pcmu"));

        otherProperties.setLanguage("sv");
        otherProperties.setVideoVariant(null);
        otherProperties.setVoiceVariant("voice");
        otherProperties.setType("prompt");
        assertFalse("Should not be equal: " + mediaContentResourceProperties.toString()
                + "; " + otherProperties.toString(),
                mediaContentResourceProperties.equals(otherProperties));

        otherProperties.addCodec(new MimeType("audio/pcmu"));
        assertTrue("Should be equal: " + mediaContentResourceProperties.toString()
                + "; " + otherProperties.toString(),
                mediaContentResourceProperties.equals(otherProperties));

        otherProperties.addCodec(new MimeType("video/h263"));
        assertFalse("Should not be equal: " + mediaContentResourceProperties.toString()
                + "; " + otherProperties.toString(),
                mediaContentResourceProperties.equals(otherProperties));
    }

    public void testOtherClass() throws Exception {
        String otherProperties = "Language=sv, type=prompt, videoVariant=null, voiceVariant=voice, codecs=[]";

        assertFalse("Should not be equal: " + mediaContentResourceProperties.toString()
                + "; " + otherProperties,
                mediaContentResourceProperties.equals(otherProperties));
    }
}
