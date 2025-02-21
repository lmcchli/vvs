/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mediacontentmanager;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Junit test for class {@link MediaResourceFilter}.
 *
 * @author Mats Egland
 */
public class MediaResourceFilterTest extends MockObjectTestCase {

    /**
     * Tests the filter method with focus on the language property.
     *
     * <pre>
     * 1. Filter with null
     *  Conditions:
     *      A non-null MediaResourceFilter is created.
     *  Action:
     *      Call filter with filterProperties = null
     *  Result:
     *      true
     *
     *
     * 3. Filter on langugage
     *  Conditions:
     *      - A mocked IMediaContentResource returns a
     *        MediaContentResourceProperties with
     *        language=en.
     *  Action:
     *      Call method with a filterProperties:
     *          language=null
     *  Result:
     *      true
     * 
     * 3. Filter on langugage
     *  Conditions:
     *      - A mocked IMediaContentResource returns a
     *        MediaContentResourceProperties with
     *        language=en.
     *  Action:
     *      Call method with a filterProperties:
     *          language="en"
     *  Result:
     *      true
     *
     * 4. Filter on langugage
     *  Conditions:
     *      - A mocked IMediaContentResource returns a
     *        MediaContentResourceProperties with
     *        language=en.
     *  Action:
     *      Call method with a filterProperties:
     *          language="en_UK"
     *  Result:
     *      false
     * </pre>
     */
    public void testLanguageFilter() {
        // Conditions
        MediaContentResourceProperties resourceProperties =
                new MediaContentResourceProperties();
        resourceProperties.setLanguage("en");
        MediaContentResourceProperties filterProperties =
                new MediaContentResourceProperties();

        Mock mockMediaContentResource_en = mock(IMediaContentResource.class);
        mockMediaContentResource_en.stubs().method("getMediaContentResourceProperties").
                withNoArguments().will(returnValue(resourceProperties));
        MediaResourceFilter filter = new MediaResourceFilter();
        assertSame(resourceProperties,
                ((IMediaContentResource)mockMediaContentResource_en.proxy()).
                        getMediaContentResourceProperties());
        // 1
        boolean result;
        result = filter.filter(null,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertTrue(result);
        // 2
        result = filter.filter(filterProperties,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertTrue(result);
        // 3
        filterProperties.setLanguage("en");
        result = filter.filter(filterProperties,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertTrue(result);

        // 4
        filterProperties.setLanguage("en_UK");
        result = filter.filter(filterProperties,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertFalse(result);

    }
    public void testTypeFilter() {
        // Conditions
        MediaContentResourceProperties resourceProperties =
                new MediaContentResourceProperties();
        resourceProperties.setType(" prompt");

        Mock mockMediaContentResource_en = mock(IMediaContentResource.class);
        mockMediaContentResource_en.stubs().method("getMediaContentResourceProperties").
                withNoArguments().will(returnValue(resourceProperties));

        MediaResourceFilter filter = new MediaResourceFilter();
        MediaContentResourceProperties filterProperties =
                new MediaContentResourceProperties();

        // 1 Filter = null should always return true
        boolean result;
        result = filter.filter(null, (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertTrue(result);


        // Type = ""  returns true
        filterProperties.setType("");

        result = filter.filter(filterProperties,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertTrue(result);

        // Type = "  " returns true
        filterProperties.setType("   ");
        result = filter.filter(filterProperties,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertTrue(result);

        // filter type = "prompt" and resource type = " prompt" should return true
        filterProperties.setType("prompt");
        result = filter.filter(filterProperties,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertTrue(result);

        filterProperties.setType(" PrOmPt ");
        result = filter.filter(filterProperties,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertTrue(result);

        filterProperties.setType("easdfj");
        result = filter.filter(filterProperties,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertFalse(result);

    }
    public void testVariantFilter() {
        // Conditions
        MediaContentResourceProperties resourceProperties =
                new MediaContentResourceProperties();

        MediaContentResourceProperties filterProperties =
                new MediaContentResourceProperties();

        Mock mockMediaContentResource_en = mock(IMediaContentResource.class);
        mockMediaContentResource_en.stubs().method("getMediaContentResourceProperties").
                withNoArguments().will(returnValue(resourceProperties));
        MediaResourceFilter filter = new MediaResourceFilter();
        assertSame(resourceProperties,
                ((IMediaContentResource)mockMediaContentResource_en.proxy()).
                        getMediaContentResourceProperties());


        // Filter Variant = [(video)null, (voice)null] and Resource Variant = [null, null] will return true
        boolean result = filter.filter(filterProperties,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertTrue(result);

        // Filter Variant = [(video)null, ""] and Resource Variant = [null, null] will return true
        filterProperties.setVoiceVariant("");
        result = filter.filter(filterProperties,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertTrue(result);

        // Filter Variant = [(video)null, "   "] and Resource Variant = [null, null] will return true
        filterProperties.setVoiceVariant("   ");
        result = filter.filter(filterProperties,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertTrue(result);

        // Filter Variant = [(video)null, "male"] and Resource Variant = [null, null] will return false
        filterProperties.setVoiceVariant("male");
        result = filter.filter(filterProperties,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertFalse(result);

        // Filter Variant = [(video)null, "male"] and Resource Variant = [null, "male"] will return true
        filterProperties.setVoiceVariant("male");
        resourceProperties.setVoiceVariant("male");
        result = filter.filter(filterProperties,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertTrue(result);

        // Filter Variant = [(video)null, "male"] and Resource Variant = [null, " MaLE "] will return true
        resourceProperties.setVoiceVariant(" MaLE ");
        result = filter.filter(filterProperties,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertTrue(result);

        // Filter Variant = [(video)null, "male"] and Resource Variant = [null, " MaLEX "] will return true
        resourceProperties.setVoiceVariant(" MaLEX ");
        result = filter.filter(filterProperties,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertFalse(result);

        // Filter Variant = ["blue", "male"] and Resource Variant = [null, "male"] will return true
        resourceProperties.setVoiceVariant("male");
        filterProperties.setVideoVariant("blue");
        filterProperties.setVoiceVariant("male");
        result = filter.filter(filterProperties,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertTrue(result);

        // Filter Variant = ["blue", "male"] and Resource Variant = ["blue", null] will return true
        resourceProperties.setVoiceVariant(null);
        resourceProperties.setVideoVariant("blue");
        filterProperties.setVideoVariant("blue");
        filterProperties.setVoiceVariant("male");
        result = filter.filter(filterProperties,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertTrue(result);

        // Filter Variant = ["blue", "male"] and Resource Variant = [null, null] will return false
        resourceProperties.setVoiceVariant(null);
        resourceProperties.setVideoVariant(null);
        filterProperties.setVideoVariant("blue");
        filterProperties.setVoiceVariant("male");
        result = filter.filter(filterProperties,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertFalse(result);

        // Filter Variant = ["blue", "male"] and Resource Variant = ["male", "blue"] will return false
        resourceProperties.setVideoVariant("male");
        resourceProperties.setVoiceVariant("blue");
        filterProperties.setVideoVariant("blue");
        filterProperties.setVoiceVariant("male");
        result = filter.filter(filterProperties,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertFalse(result);

        // Filter Variant = ["blue", "male"] and Resource Variant = ["blue", "nisse"] will return true
        resourceProperties.setVideoVariant("blue");
        resourceProperties.setVoiceVariant("nisse");
        filterProperties.setVideoVariant("blue");
        filterProperties.setVoiceVariant("male");
        result = filter.filter(filterProperties,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertTrue(result);

        // Filter Variant = ["blue", "male"] and Resource Variant = ["blue", "male"] will return true
        resourceProperties.setVideoVariant("blue");
        resourceProperties.setVoiceVariant("male");
        filterProperties.setVideoVariant("blue");
        filterProperties.setVoiceVariant("male");
        result = filter.filter(filterProperties,
                (IMediaContentResource)mockMediaContentResource_en.proxy());
        assertTrue(result);

    }

    /**
     *
     * Tests the filter in general.
     */
    public void testFilter() {
        // todo
    }


}
