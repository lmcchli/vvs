/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

/**
 * Thrown when a folder parent tries to add a folder that already exists.
 * @author qhast
 * @see IFolderParent
 */
public class FolderAlreadyExistsException extends MailboxException {

    private String folderName;

    public FolderAlreadyExistsException(String folderName) {
        super("Folder named "+folderName+" already exists.");
        if(folderName == null || folderName.length()==0) throw new IllegalArgumentException("folderName cannot be null or empty!");
        this.folderName = folderName;
    }

    public String getFolderName() {
        return folderName;
    }
}
