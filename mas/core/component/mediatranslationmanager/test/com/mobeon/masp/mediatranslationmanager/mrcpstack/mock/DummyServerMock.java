package com.mobeon.masp.mediatranslationmanager.mrcpstack.mock;

import com.mobeon.masp.mediatranslationmanager.mrcpstack.messages.*;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;

import java.io.ByteArrayInputStream;

public class DummyServerMock extends ServerMock {
    protected static ILogger logger = ILoggerFactory.getILogger(DummyServerMock.class);

    public boolean stepSimulation() {
        String message = inputStream.getBuffer();
        if (logger.isInfoEnabled()) logger.info("handleMessage : [" + message + "]");

        if ("StopServerMock".equals(message)) {
            if (logger.isInfoEnabled()) logger.info("Aborting simulation step");
            return false;
        }

        RtspRequest request = null;
        RtspResponse response = new RtspResponse(200, "OK");

        try {
            request = (RtspRequest) MessageParser.parse(new ByteArrayInputStream(message.getBytes()));
            if (logger.isDebugEnabled()) logger.debug("Got request: [" + request.getMessage() + "]");
        } catch (Exception e) {
            e.printStackTrace();
        }

        assert(request != null);

        if (request.getCommand().equals("OPTIONS")) {
            // Never mind wat to answer ...
        } else if (request.getCommand().equals("DESCRIBE")) {
            // Never mind wat to answer ...
        } else if (request.getCommand().equals("ANNOUNCE")) {
            // ANNOUNCE means MRCP
            MrcpRequest mrcpRequest = (MrcpRequest)request.getMrcpMessage();
            MrcpResponse mrcpResponse;
            // Default response is COMPLETE
            String mrcpStatus = "COMPLETE";

            // For SPEAK and RECOGNIZE we expect to get IN-PROGRESS
            if (mrcpRequest.getName().equals("SPEAK")) {
                mrcpStatus = "IN-PROGRESS";
            } else if (mrcpRequest.getName().equals("RECOGNIZE")) {
               mrcpStatus = "IN-PROGRESS";
            }

            // Every thing is fine ...
            mrcpResponse = new MrcpResponse(mrcpRequest.getRequestId(), 200, mrcpStatus);
            if (mrcpStatus.equals("COMPLETE")) {
                mrcpResponse.setHeaderField("Completion-Cause", "000 success");
            }
            response.setMrcpMessage(mrcpResponse);
        } else if (request.getCommand().equals("SETUP")) {
            request.getHeaderField("Session");
            response.setHeaderField("Session", request.getHeaderField("Session"));
        } else if (request.getCommand().equals("TEARDOWN")) {
            request.getHeaderField("Session");
            response.setHeaderField("Session", request.getHeaderField("Session"));
        }
        String responseMessage = response.getMessage();
        if (logger.isDebugEnabled()) logger.debug("returning response message: [" + responseMessage + "]");
        outputStream.setBuffer(responseMessage);
        return true;
    }
}