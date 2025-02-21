/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

/**
 * FolderAlreadyExistsException Tester.
 *
 * @author qhast
 */
public class FolderAlreadyExistsExceptionTest extends TestCase
{
    private FolderAlreadyExistsException folderAlreadyExistsException;

    public FolderAlreadyExistsExceptionTest(String name)
    {
        super(name);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        folderAlreadyExistsException = new FolderAlreadyExistsException("xFolder");
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testGetFolderName() throws Exception
    {
        assertEquals("xFolder",folderAlreadyExistsException.getFolderName());
    }

     public void testConstructWithIllegalFolderName() throws Exception
    {
        try {
            FolderAlreadyExistsException fe = new FolderAlreadyExistsException("");
            fail("Should throw IllegalArgumentException when constructing with empty folderName!");
        } catch(IllegalArgumentException e) {
            //OK
        }

        try {
            FolderAlreadyExistsException fe = new FolderAlreadyExistsException(null);
            fail("Should throw IllegalArgumentException when constructing with folderName null!");
        } catch(IllegalArgumentException e) {
            //OK
        }


    }

    public static Test suite()
    {
        return new TestSuite(FolderAlreadyExistsExceptionTest.class);
    }
}
