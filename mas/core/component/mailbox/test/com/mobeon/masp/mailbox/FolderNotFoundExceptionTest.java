/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * FolderNotFoundException Tester.
 *
 * @author qhast
 */
public class FolderNotFoundExceptionTest extends TestCase
{
    private FolderNotFoundException folderNotFoundException;

    public FolderNotFoundExceptionTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        folderNotFoundException = new FolderNotFoundException("xFolder");
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testGetFolderName() throws Exception
    {
        assertEquals("xFolder",folderNotFoundException.getFolderName());
    }

    public void testConstructWithIllegalFolderName() throws Exception
    {
        try {
            FolderNotFoundException fe = new FolderNotFoundException("");
            fail("Should throw IllegalArgumentException when constructing with empty folderName!");
        } catch(IllegalArgumentException e) {
            //OK
        }

        try {
            FolderNotFoundException fe = new FolderNotFoundException(null);
            fail("Should throw IllegalArgumentException when constructing with folderName null!");
        } catch(IllegalArgumentException e) {
            //OK
        }


    }

    public static Test suite()
    {
        return new TestSuite(FolderNotFoundExceptionTest.class);
    }
}
