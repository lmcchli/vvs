package com.mobeon.masp.execution_engine.voicexml.compiler.operations;
/**
 * @author Kenneth Selin
 */

import com.mobeon.masp.execution_engine.runtime.ExecutionContext;
import com.mobeon.masp.execution_engine.voicexml.runtime.VoiceXMLRuntimeCase;
import com.mobeon.masp.mediaobject.*;
import com.mobeon.masp.mediaobject.factory.IMediaObjectFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class Record_PTest extends VoiceXMLRuntimeCase {
    Record_P record_p;

    class MediaObjectMock implements IMediaObject {
        public void append(ByteBuffer byteBuffer) throws MediaObjectException {
        }

        public IMediaObjectIterator iterator() {
            return null;
        }

        public void setImmutable() {
        }

        public boolean isImmutable() {
            return true;
        }

        public MediaProperties getMediaProperties() {
            return null;
        }

        public String getFileFormat() {
            return null;
        }

        public void setFileFormat(String fileFormat) {
        }

        public long getSize() {
            return 0;
        }

        public String getContentType() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public InputStream getInputStream() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public MediaObjectNativeAccess getNativeAccess() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getName() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public OutputStream getOutputStream() throws IOException {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public Record_PTest(String name) {
        super(name);
    }

    public void testExecute() throws Exception {
        setupExecute();
        op.execute(getExecutionContext());
    }

    /**
     * Help method to be used by test cases/methods. Sets up everything necessary
     * to execute this.op, for example execution context.
     */
    private void setupExecute() {
        op = new Record_P("*");
        IMediaObject mediaObject = new MediaObjectMock();
        expect_ValueStack_push(mediaObject);
    }
}