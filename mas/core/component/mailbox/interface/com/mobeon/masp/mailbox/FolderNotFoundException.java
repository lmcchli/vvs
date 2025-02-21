/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox;

/**
 * Thrown when a folder parent tries access a folder that not exists.
 * @author qhast
 * @see IFolder
 */
public class FolderNotFoundException extends MailboxException {

    private String folderName;

    public FolderNotFoundException(String folderName) {
        super("Folder named \""+folderName+"\" not found.");
        if(folderName == null || folderName.length()==0) throw new IllegalArgumentException("folderName cannot be null or empty!");
        this.folderName = folderName;
    }

    public String getFolderName() {
        return folderName;
    }
}
