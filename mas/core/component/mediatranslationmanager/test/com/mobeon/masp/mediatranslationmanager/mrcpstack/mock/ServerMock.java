package com.mobeon.masp.mediatranslationmanager.mrcpstack.mock;

public abstract class ServerMock {
    protected InputStreamMock outputStream;
    protected OutputStreamMock inputStream;

    public String handleMessage(String sessionId, String message) {
        throw new IllegalAccessError("No implementation, this method should not have been called.");
    }

    boolean stepSimulation() {
        throw new IllegalAccessError("No implementation, this method should not have been called.");
    }

    /**
     * Connecting client input to server output
     * @param outputStream
     */
    public void setOutput(InputStreamMock outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * Connecting client output to server input
     * @param inputStream
     */
    public void setInput(OutputStreamMock inputStream) {
        this.inputStream = inputStream;
    }
}
