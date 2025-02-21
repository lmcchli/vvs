/*
 * Copyright (c) 2005 Mobeon AB. All Rights Reserved.
 */
package com.mobeon.masp.mailbox.javamail;

/**
 * @author Håkan Stolt
 */
public class JavamailBehavior {

    private boolean closeNonSelectedFolders = true;

    public boolean getCloseNonSelectedFolders() {
        return closeNonSelectedFolders;
    }

    public void setCloseNonSelectedFolders(boolean closeNonSelectedFolders) {
        this.closeNonSelectedFolders = closeNonSelectedFolders;
    }

}
