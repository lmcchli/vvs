package com.mobeon.masp.execution_engine.runapp.mock;

import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.masp.mediatranslationmanager.MediaTranslationManager;
import com.mobeon.masp.mediatranslationmanager.SpeechRecognizer;
import com.mobeon.masp.mediatranslationmanager.TextToSpeech;

import java.util.*;
import java.util.Collection;

/**
 * @author David Looberger
 */
public class MediaTranslationManagerMock implements MediaTranslationManager {

    public static boolean illegalExceptionAtOpen = false;
    public static MediaTranslationManagerMock instance;
    private final HashMap<Object,SpeechRecognizer> recognizerMap = new HashMap<Object, SpeechRecognizer>();

    public MediaTranslationManagerMock() {
        instance = this;
    }


    public SpeechRecognizer getSpeechRecognizer(ISession session, Map<String, String> grammar) {
        return createSession(session);
    }

    public SpeechRecognizer getSpeechRecognizer(ISession session, SpeechRecognizer prototype) {
        return createSession(session);
    }

    public SpeechRecognizer getSpeechRecognizer(ISession session) {

        return createSession(session);
    }

    private SpeechRecognizer createSession(Object key) {
        if(illegalExceptionAtOpen){
            String message = "Unknown ASR engine protocol;";
            throw new IllegalStateException(message);
        } else {
            SpeechRecognizer result = recognizerMap.get(key);
            if(result == null) {
                recognizerMap.put(key,result = new SpeechRecognizerMock());
            }
            return result;
        }
    }

    public TextToSpeech getTextToSpeech(ISession session) {
        return null;
    }

    public Collection<String> getTextToSpeechLanguages() {
        List<String> languages = new ArrayList<String>();
        languages.add("en");
        languages.add("sv");
        return languages;
    }

    public static MediaTranslationManagerMock instance() {
        return instance;
    }

}
