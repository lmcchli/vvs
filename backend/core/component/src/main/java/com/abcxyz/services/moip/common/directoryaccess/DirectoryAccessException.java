package com.abcxyz.services.moip.common.directoryaccess;

public class DirectoryAccessException extends Exception{

	private static final long serialVersionUID = 19981010017L;

    public DirectoryAccessException(String message) {
        super(message);
    }

    public DirectoryAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DirectoryAccessException(Throwable cause) {
        super(cause);
    }
}
