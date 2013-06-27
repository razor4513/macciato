package de.telekom.cldii.view.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import de.telekom.cldii.R;
import de.telekom.cldii.config.IPreferenceManager;
import de.telekom.cldii.config.ITextToSpeechManager;
import de.telekom.cldii.view.main.MainActivity;
import de.telekom.cldii.view.tutorial.TutorialActivity;

/**
 * Activity to check if text to speech engine is installed.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public class CheckTextToSpeechActivity extends Activity {

    private static final String TAG = "CheckTextToSpeechActivity";

    private boolean isCheckingTts = false;

    @Override
    protected void onStart() {
        super.onStart();
        if (!this.isCheckingTts) {
            Log.v(TAG, "Checking Text to Speech engine...");
            // Check if a TTS engine is installed
            Intent checkIntent = new Intent();
            checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            startActivityForResult(checkIntent, R.id.resultcode_checkifttsinstalled);
            this.isCheckingTts = true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        /*String info1 = data.getExtras().getStringArrayList("availableVoices").get(0) + "";
        String info2 = data.getExtras().get("dataFiles").toString();
        String info3 = data.getExtras().get("dataFilesInfo").toString();
        String info4 = data.getExtras().keySet().toString();
        Log.v(TAG, "info 1: " + info1 + " | " + info2 + " | " + info3 + " | " + info4);*/
        if (requestCode == R.id.resultcode_checkifttsinstalled) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                this.isCheckingTts = false;
                //getTextToSpeechManager().getTextToSpeech().setEngineByPackageName(enginePackageName)
                getTextToSpeechManager().setTextToSpeechReadyForInitialization(true);
                Intent intent;
                if (getPreferenceManager().getApplicationPreferences().getShowTutorialOnStartup()) {
                    intent = new Intent(this, TutorialActivity.class);
                } else {
                    intent = new Intent(this, MainActivity.class);
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();
                overridePendingTransition(0, 0);
                startActivity(intent);
                overridePendingTransition(0, android.R.anim.fade_in);
            } else {
                // missing data, install it
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setMessage(getString(R.string.need_tts)).setCancelable(false)
                        .setPositiveButton(getString(R.string.confirm_yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent installIntent = new Intent();
                                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                                startActivity(installIntent);
                                isCheckingTts = false;
                            }
                        }).setNegativeButton(getString(R.string.confirm_no), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                finish();
                                isCheckingTts = false;
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }

        }
    }

    private ITextToSpeechManager getTextToSpeechManager() {
        return (ITextToSpeechManager) getApplication();
    }

    private IPreferenceManager getPreferenceManager() {
        return (IPreferenceManager) getApplication();
    }

}
