/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.util.javamail;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.builder.StubBuilder;

import jakarta.mail.Part;
import jakarta.mail.MessagingException;

import com.mobeon.common.logging.ILoggerFactory;

/**
 * PartParser Tester.
 *
 * @author qhast
 */
public class PartParserTest extends MockObjectTestCase
{
    private Mock partMock;
    private StubBuilder getContentTypeStubBuilder;
    private StubBuilder getHeaderContentDurationStubBuilder;
    private StubBuilder getHeaderContentDescriptionStubBuilder;
    private StubBuilder getHeaderContentDispositionStubBuilder;

    public PartParserTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        ILoggerFactory.configureAndWatch("test/log4jconf.xml");

        partMock = mock(Part.class);

        getContentTypeStubBuilder = partMock.stubs().method("getContentType").withNoArguments();
        getContentTypeStubBuilder.will(returnValue("audio/wave"));

        getHeaderContentDescriptionStubBuilder = partMock.stubs().method("getHeader").with(eq(PartParser.HEADER_NAME_DESCRIPTION));
        getHeaderContentDescriptionStubBuilder.will(returnValue(new String[]{"Default Description"}));

        getHeaderContentDispositionStubBuilder = partMock.stubs().method("getHeader").with(eq(PartParser.HEADER_NAME_DISPOSITION));
        getHeaderContentDispositionStubBuilder.will(returnValue(new String[]{"inline;filename=default.wav"}));

        getHeaderContentDurationStubBuilder = partMock.stubs().method("getHeader").with(eq(PartParser.HEADER_NAME_DURATION));
        getHeaderContentDurationStubBuilder.will(returnValue(new String[]{"123"}));


    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testClassCall() throws Exception {
        new PartParser();
    }

    public void testGetDescription() throws Exception
    {

        PartParser.Result result = PartParser.parse((Part) partMock.proxy());
        assertEquals("Default Description",result.getDescription());

        getHeaderContentDescriptionStubBuilder.will(returnValue(null));
        result = PartParser.parse((Part) partMock.proxy());
        assertEquals(null,result.getDescription());

        getHeaderContentDescriptionStubBuilder.will(throwException(new MessagingException()));
        result = PartParser.parse((Part) partMock.proxy());
        assertEquals(null,result.getDescription());

    }

    public void testGetFilename() throws Exception
    {
        PartParser.Result result = PartParser.parse((Part) partMock.proxy());
        assertEquals("default.wav",result.getFilename().getFullname());

        getHeaderContentDispositionStubBuilder.will(returnValue(new String[]{"inline"}));
        result = PartParser.parse((Part) partMock.proxy());
        assertEquals(null,result.getFilename());

        getContentTypeStubBuilder.will(returnValue("audio/wave;name=ct.wave"));
        result = PartParser.parse((Part) partMock.proxy());
        assertEquals("ct.wave",result.getFilename().getFullname());

        getHeaderContentDispositionStubBuilder.will(throwException(new MessagingException()));
        result = PartParser.parse((Part) partMock.proxy());
        assertEquals(null,result.getFilename());

    }

    public void testGetContentType() throws Exception
    {
        PartParser.Result result = PartParser.parse((Part) partMock.proxy());
        assertEquals("audio/wave",result.getContentType().getBaseType());
    }

    public void testGetDuration() throws Exception
    {
        PartParser.Result result = PartParser.parse((Part) partMock.proxy());
        assertEquals(123,(long)result.getDuration());

        getHeaderContentDurationStubBuilder.will(returnValue(null));
        result = PartParser.parse((Part) partMock.proxy());
        assertEquals(null,result.getDuration());

        getHeaderContentDurationStubBuilder.will(returnValue(new String[]{"nonumber"}));
        result = PartParser.parse((Part) partMock.proxy());
        assertEquals(null,result.getDuration());

        getHeaderContentDurationStubBuilder.will(throwException(new MessagingException()));
        result = PartParser.parse((Part) partMock.proxy());
        assertEquals(null,result.getDuration());
    }

    public static Test suite()
    {
        return new TestSuite(PartParserTest.class);
    }
}
