package com.mobeon.masp.execution_engine.runtime.wrapper;

import com.mobeon.masp.execution_engine.voicexml.grammar.ASRGrammar;
import com.mobeon.masp.execution_engine.voicexml.grammar.GrammarScopeNode;
import com.mobeon.masp.execution_engine.session.ISession;
import com.mobeon.common.logging.ILogger;
import com.mobeon.common.logging.ILoggerFactory;
import com.mobeon.masp.mediatranslationmanager.MediaTranslationManager;
import com.mobeon.masp.mediatranslationmanager.SpeechRecognizer;
import com.mobeon.masp.stream.IInboundMediaStream;

import java.util.Map;
import java.util.HashMap;

/**
 * A wrapper class for the MediaTranslatorManager component classes
 *
 * @author David Looberger
 */
public class MediaTranslator {
    private final ILogger log = ILoggerFactory.getILogger(MediaTranslator.class);
    private SpeechRecognizer asr = null;

    public MediaTranslationManager getMediaTranslationManager() {
        return mediaTranslationManager;
    }

    private MediaTranslationManager mediaTranslationManager;
    private GrammarScopeNode lastUsedGrammarNode = null;

    public MediaTranslator(MediaTranslationManager mediaTranslationManager) {
        this.mediaTranslationManager = mediaTranslationManager;
    }
    public void recognize(IInboundMediaStream stream) {
        if(asr != null)
            asr.recognize(stream);
    }
    public void prepare(final ISession session,final GrammarScopeNode initialGnode) {

        if (lastUsedGrammarNode != initialGnode) {
            GrammarScopeNode gnode = initialGnode;

            Map<String, String> grammars = new HashMap<String, String>();
            ASRGrammar grammar = (ASRGrammar) gnode.getGrammar();
            if (grammar != null) {
                grammars.put(grammar.getGrammar_id(), grammar.getSRGSContent());
            }
            while (gnode.getParent() != null) {
                gnode = gnode.getParent();
                grammar = (ASRGrammar) initialGnode.getGrammar();
                if (grammar != null) {
                    grammars.put(grammar.getGrammar_id(), grammar.getSRGSContent());
                }
            }
            asr = mediaTranslationManager.getSpeechRecognizer(session,grammars);
            asr.prepare();
            lastUsedGrammarNode = initialGnode;
        } else {
            asr.prepare();
        }
    }

    public void cancel() {
        if(asr != null) {
            asr.cancel();
        }
    }
}
