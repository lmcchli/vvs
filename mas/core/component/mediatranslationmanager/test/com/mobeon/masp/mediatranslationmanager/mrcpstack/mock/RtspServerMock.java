package com.mobeon.masp.mediatranslationmanager.mrcpstack.mock;

import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.MessageParser;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.MrcpMessage;
import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.RtspMessage;

import java.io.ByteArrayInputStream;
import java.util.LinkedList;
import java.util.List;

public class RtspServerMock extends ServerMock {
    protected static ILogger logger = ILoggerFactory.getILogger(RtspServerMock.class);
    List<ServerMockAction> simulationSteps = new LinkedList<ServerMockAction>();
    String sessionId = "mockedRTSP";
//    boolean isPending = false;

    public void sendServerEvent(MrcpMessage message) {throw new Error("Bajs!");}

    public boolean stepSimulation() {
        if (simulationSteps.isEmpty()) return false;
        ServerMockAction step = simulationSteps.remove(0);
        switch (step.action) {
            case PEND:
                pend(step.message);
                break;

            case SEND:
                send(step.message);
                Thread.yield();
                break;

            case SLEEP:
                sleep(step.delay);
                break;
        }
        return true;
    }

    private void pend(RtspMessage expected) {
        // TODO: Should not wait for ever!
        RtspMessage request;
        if (logger.isInfoEnabled()) logger.info("Pending for a message ...");
        String message = inputStream.getBuffer();
        if (logger.isInfoEnabled()) logger.info("processing : [" + message + "]");

        try {
            request = MessageParser.parse(new ByteArrayInputStream(message.getBytes()));
        } catch (Exception e) {
            throw new IllegalArgumentException("Parse failed: ", e);
        }

        if (request == null) {
            throw new IllegalArgumentException("Parse failed: [" + message + "]");
        }

        // check if the retrieved message matches the expected
        if (!expected.getMessage().equals(request.getMessage())) {
            logger.error("Expected: [" + expected.getMessage() + "]");
            logger.error("Actual:   [" + request.getMessage() + "]");
            throw new IllegalArgumentException("Unexpected request");
        }
    }

    private void send(RtspMessage message) {
        if (logger.isDebugEnabled()) logger.debug("Sending a message: [" + message.getMessage() + "]");
        outputStream.setBuffer(message.getMessage());
   }

    private void sleep(int delay) {
        if (logger.isDebugEnabled()) logger.debug("Sleeping for a while ...");
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a request message to the list of expected requests
     * @param message
     */
    public void setExpectedMessage(RtspMessage message) {
        simulationSteps.add(new ServerMockAction(message, true));
    }

    /**
     * Adds a message (response/event) to the list of outgoing messages
     * @param message
     */
    public void setSentMessage(RtspMessage message) {
        simulationSteps.add(new ServerMockAction(message, false));
    }

    public void setDelay(int delay) {
        simulationSteps.add(new ServerMockAction(delay)) ;
    }

//    public boolean pendOnClientResponse() {
//        isPending = true;
//        int times = 0;
//        while (isPending) {
//            try {
//                times++;
//                Thread.sleep(100);
//                if (times > 100) return false;
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        return true;
//    }
}
