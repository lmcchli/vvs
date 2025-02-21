package com.mobeon.masp.execution_engine.voicexml.runtime;

import com.mobeon.masp.execution_engine.compiler.Constants;

/**
 * User: QMIAN
 * Date: 2006-okt-27
 * Time: 10:24:12
 */
public class PropertyStackTest extends VoiceXMLRuntimeCase {
    public PropertyStackTest(String name) {
        super(name);
    }

    public void testInitialScope() throws Exception {
        PropertyStack stack = new PropertyStack();
        retrieveAndValidateAudioOffset(stack);
        stack.enteredScope(null);
        retrieveAndValidateAudioOffset(stack);
        stack.enteredScope(null);
        stack.enteredScope(null);
        retrieveAndValidateAudioOffset(stack);
        stack.leftScope(null);
        stack.leftScope(null);
        retrieveAndValidateAudioOffset(stack);
    }

    private void retrieveAndValidateAudioOffset(PropertyStack stack) {
        String offset = stack.getProperty(Constants.PlatformProperties.PLATFORM_AUDIO_OFFSET);
        if (!"0s".equals(offset))
            die("Platform audio offset is not 0s as default ! It was " + offset);
    }
}