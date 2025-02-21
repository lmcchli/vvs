/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

import com.mobeon.common.cmnaccess.ICommonMessagingAccess;

/**
 * A folder parent has the capability to keep folder children.
 * A folder parent self may not be a child to anaother folder parent.
 */
public interface IFolderParent {

    /**
     * Tries to Find an existing folder. If the folder not exists an exception is thrown.
     *
     * @param name Folder name.
     * @return the requested folder. (if exists)
     * @throws FolderNotFoundException if the requested folder not exists.
     * @throws MailboxException        if problems occur.
     */
    public IFolder getFolder(String name)
            throws MailboxException;
    
    
    /**
     * TODO : For test only to be able to mock the ICommonMessagingAccess
     * @param name
     * @return
     * @deprecated
     */
    public IFolder getFolder(String name, ICommonMessagingAccess mfs)
    throws MailboxException;

    /**
     * Tries to add a {@link IFolder} to the folder parent.
     * If a folder with the same name already exist an exception is thrown.
     * @param name Folder name.
     * @return a new folder with the requested name. (if not already exists)
     * @throws FolderAlreadyExistsException if the requested folder already exists.
     * @throws MailboxException             if problems occur.
     */
    public IFolder addFolder(String name)
            throws MailboxException;


    /**
     * Deletes the folder and deletes all subfolders.
     * All messages within the folder are removed.
     * If the folder not exists an exception is thrown.
     * @param name Folder name.
     * @throws FolderNotFoundException if the requested folder not exists.
     * @throws MailboxException        if problems occur.
     */
    public void deleteFolder(String name)
            throws MailboxException;


    /**
    * Sets this mailbox to readonly to prevent update of LasAccesstime flag in MUR.
    * @throws MailboxException if a problem occur.
    */
     public void setReadonly() throws MailboxException;

    /**
    * Sets this mailbox to readwrite.
    * @throws MailboxException if a problem occur.
    */
     public void setReadwrite() throws MailboxException;

}

