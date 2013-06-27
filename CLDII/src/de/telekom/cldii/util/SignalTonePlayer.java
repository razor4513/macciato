package de.telekom.cldii.util;

import android.content.Context;
import android.media.MediaPlayer;
import de.telekom.cldii.statemachine.StateModel;

/**
 * Has methods to play different sounds over {@link MediaPlayer}.
 * 
 * @author Sebastian Stallenberger, jambit GmbH
 * 
 */
public class SignalTonePlayer {

    /**
     * If you give this constant to the {@link speakAndNextState} method of
     * {@link StateModel} as textToRead, it will play the listend sound instead
     * of speaking.
     */
    public static final String SIGNAL_TONE_LISTEND = "SIGNAL_TONE_LISTEND";

    /**
     * Play a list-end sound.
     * 
     * @param context
     */
    public static void playListendSound(final Context context) {

        // Thread thread = new Thread(new Runnable() {
        // public void run() {
        // MediaPlayer mp = MediaPlayer.create(context, R.raw.listend);
        // mp.start();
        // }
        // });
        // thread.start();
    }
}
