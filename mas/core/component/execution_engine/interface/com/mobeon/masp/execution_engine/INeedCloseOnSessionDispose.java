package com.mobeon.masp.execution_engine;

/**
 * Tag interface for session data that need an explicit
 * call to close when the session is disposed of.
 *
 * @author Mikael Andersson
 */
public interface INeedCloseOnSessionDispose {
    public void close();
}
