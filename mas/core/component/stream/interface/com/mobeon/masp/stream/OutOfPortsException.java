package com.mobeon.masp.stream;

public class OutOfPortsException extends StackException{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public OutOfPortsException(String message) {
        super(message);
    }

    public OutOfPortsException(String message, Throwable exception) {
        super(message, exception);
    }
}
