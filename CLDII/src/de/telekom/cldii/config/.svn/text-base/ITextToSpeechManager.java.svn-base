package de.telekom.cldii.config;

import android.speech.tts.TextToSpeech;

/**
 * Provides access to the text to speech engine.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public interface ITextToSpeechManager {

    /**
     * Set text to speech engine ready for initialization after checking if
     * required packages are installed.
     * 
     * @param isReady
     */
    public void setTextToSpeechReadyForInitialization(boolean isReady);

    /**
     * @return the application wide text to speech instance
     */
    public TextToSpeech getTextToSpeech();

    /**
     * @return Returns true if TTS is initializing, false otherwise
     */
    public boolean ttsIsInitializing();
}
