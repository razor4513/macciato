package de.telekom.cldii.view.tutorial;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import de.telekom.cldii.R;
import de.telekom.cldii.config.IPreferenceManager;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.view.AbstractActivity;
import de.telekom.cldii.view.main.MainActivity;

/**
 * Activity for showing the tutorial screens.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public class TutorialActivity extends AbstractActivity {

    private static final String TUTORIAL_SCREEN_KEY = "TUTORIAL_SCREEN_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTopBarName(getString(R.string.tutorial_title));
        hideHomeButton();
        hideSpeakButton();

        Intent startingIntent = getIntent();
        int screenNr = 0;
        if (startingIntent.getExtras() != null) {
            screenNr = startingIntent.getExtras().getInt(TUTORIAL_SCREEN_KEY);
        }
        TutorialScreen tutorialScreen = TutorialScreen.values()[screenNr];

        switch (tutorialScreen) {
        case SUMMARY:
            buildSummaryScreen();
            break;
        case STVO:
            buildStvoScreen();
            break;
        case SPEAK_MODE:
            buildSpeakModeScreen();
            break;
        case GESTURES:
            buildGesturesScreen();
            break;
        case NIGHT_MODE:
            buildNightModeScreen();
            break;
        case TTS:
            buildTtsScreen();
            break;
        }
    }

    private void buildSummaryScreen() {
        setContentView(R.layout.tutorial_summary);

        Button continueButton = (Button) findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tutorialIntent = new Intent(TutorialActivity.this, TutorialActivity.class);
                tutorialIntent.putExtra(TUTORIAL_SCREEN_KEY, TutorialScreen.STVO.ordinal());
                startActivity(tutorialIntent);
                overridePendingTransition(0, android.R.anim.fade_in);
                finish();
            }
        });
    }

    private void buildStvoScreen() {
        setContentView(R.layout.tutorial_stvo);

        Button continueButton = (Button) findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tutorialIntent = new Intent(TutorialActivity.this, TutorialActivity.class);
                tutorialIntent.putExtra(TUTORIAL_SCREEN_KEY, TutorialScreen.SPEAK_MODE.ordinal());
                startActivity(tutorialIntent);
                overridePendingTransition(0, android.R.anim.fade_in);
                finish();
            }
        });
        Button backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tutorialIntent = new Intent(TutorialActivity.this, TutorialActivity.class);
                tutorialIntent.putExtra(TUTORIAL_SCREEN_KEY, TutorialScreen.SUMMARY.ordinal());
                startActivity(tutorialIntent);
                overridePendingTransition(0, android.R.anim.fade_out);
                finish();
            }
        });
    }

    private void buildSpeakModeScreen() {
        setContentView(R.layout.tutorial_speakmode);

        Button continueButton = (Button) findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tutorialIntent = new Intent(TutorialActivity.this, TutorialActivity.class);
                tutorialIntent.putExtra(TUTORIAL_SCREEN_KEY, TutorialScreen.GESTURES.ordinal());
                startActivity(tutorialIntent);
                overridePendingTransition(0, android.R.anim.fade_in);
                finish();
            }
        });
        Button backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tutorialIntent = new Intent(TutorialActivity.this, TutorialActivity.class);
                tutorialIntent.putExtra(TUTORIAL_SCREEN_KEY, TutorialScreen.STVO.ordinal());
                startActivity(tutorialIntent);
                overridePendingTransition(0, android.R.anim.fade_out);
                finish();
            }
        });
    }

    private void buildGesturesScreen() {
        setContentView(R.layout.tutorial_gestures);

        Button continueButton = (Button) findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tutorialIntent = new Intent(TutorialActivity.this, TutorialActivity.class);
                tutorialIntent.putExtra(TUTORIAL_SCREEN_KEY, TutorialScreen.NIGHT_MODE.ordinal());
                startActivity(tutorialIntent);
                overridePendingTransition(0, android.R.anim.fade_in);
                finish();
            }
        });
        Button backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tutorialIntent = new Intent(TutorialActivity.this, TutorialActivity.class);
                tutorialIntent.putExtra(TUTORIAL_SCREEN_KEY, TutorialScreen.SPEAK_MODE.ordinal());
                startActivity(tutorialIntent);
                overridePendingTransition(0, android.R.anim.fade_out);
                finish();
            }
        });
    }

    private void buildNightModeScreen() {
        setContentView(R.layout.tutorial_nightmode);

        Button continueButton = (Button) findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tutorialIntent = new Intent(TutorialActivity.this, TutorialActivity.class);
                tutorialIntent.putExtra(TUTORIAL_SCREEN_KEY, TutorialScreen.TTS.ordinal());
                startActivity(tutorialIntent);
                overridePendingTransition(0, android.R.anim.fade_in);
                finish();
            }
        });
        Button backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tutorialIntent = new Intent(TutorialActivity.this, TutorialActivity.class);
                tutorialIntent.putExtra(TUTORIAL_SCREEN_KEY, TutorialScreen.GESTURES.ordinal());
                startActivity(tutorialIntent);
                overridePendingTransition(0, android.R.anim.fade_out);
                finish();
            }
        });

    }

    private void buildTtsScreen() {
        setContentView(R.layout.tutorial_tts);

        Button continueButton = (Button) findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(TutorialActivity.this, MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainIntent);
                finish();
            }
        });
        Button backButton = (Button) findViewById(R.id.back_button);
        backButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tutorialIntent = new Intent(TutorialActivity.this, TutorialActivity.class);
                tutorialIntent.putExtra(TUTORIAL_SCREEN_KEY, TutorialScreen.NIGHT_MODE.ordinal());
                startActivity(tutorialIntent);
                overridePendingTransition(0, android.R.anim.fade_out);
                finish();
            }
        });
        View marketLink = findViewById(R.id.market_link);
        marketLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("http://www.t-mobile-favoriten.de/go/autoread"));
                startActivity(intent);
            }
        });
        CheckBox dontShowTutorialCheckBox = (CheckBox) findViewById(R.id.dont_show_again_checkbox);
        dontShowTutorialCheckBox.setChecked(!getPreferenceManager().getApplicationPreferences()
                .getShowTutorialOnStartup());
        dontShowTutorialCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean dontShowTutorialAgain) {
                getPreferenceManager().getApplicationPreferences().setShowTutorialOnStartup(!dontShowTutorialAgain);
            }
        });

    }

    private enum TutorialScreen {
        SUMMARY, STVO, SPEAK_MODE, GESTURES, NIGHT_MODE, TTS;
    }

    private IPreferenceManager getPreferenceManager() {
        return (IPreferenceManager) getApplication();
    }

    @Override
    protected void initGestureLibrary() {
        // TODO Auto-generated method stub

    }

    @Override
    public StateModel getStateModel() {
        // TODO Auto-generated method stub
        return null;
    }

}
