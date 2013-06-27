package de.telekom.cldii.view;

import static de.telekom.cldii.statemachine.StateMachineConstants.CHANGE_GESTURE_OVERLAY;
import static de.telekom.cldii.statemachine.StateMachineConstants.CHANGE_GESTURE_OVERLAY_ICON_ID;
import static de.telekom.cldii.statemachine.StateMachineConstants.CHANGE_GESTURE_OVERLAY_STRING_ID;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_MANUAL_SLEEP;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_START;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.gesture.Prediction;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import de.telekom.cldii.ApplicationConstants;
import de.telekom.cldii.CldApplication;
import de.telekom.cldii.R;
import de.telekom.cldii.config.IActivityLifecycleListener;
import de.telekom.cldii.config.IConfigurationManager;
import de.telekom.cldii.config.ISchedulerManager;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.statemachine.IStateMachineManager;
import de.telekom.cldii.statemachine.StateContent;
import de.telekom.cldii.statemachine.StateMachine;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.statemachine.states.StateModelPhoneAddressbook;
import de.telekom.cldii.view.mail.MailDetailsActivity;
import de.telekom.cldii.view.mail.MailListActivity;
import de.telekom.cldii.view.main.MainActivity;
import de.telekom.cldii.view.news.NewsDetailsActivity;
import de.telekom.cldii.view.news.NewsListActivity;
import de.telekom.cldii.view.phone.PhoneAddressbookListActivity;
import de.telekom.cldii.view.sms.SmsDetailsActivity;
import de.telekom.cldii.view.sms.SmsListActivity;

/**
 * Abstract activity to be inherited from by all CLDII activities.
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */

public abstract class AbstractActivity extends Activity implements OnGesturePerformedListener, OnTouchListener {

    /**
     * Activate/deactivate statemachine log.
     */
    private final boolean loggingEnabled = ApplicationConstants.STATEMACHINE_LOGGING;

    /**
     * TAG for Log methods
     */
    private static final String TAG = "AbstractActivity";
    private static boolean activityInGestureMode;

    private Toast prompt;
    private View modeButton;
    private TextView topBarName;
    private GestureOverlayView gestureOverlay;
    protected GestureLibrary gestureLibrary;
    protected Menu optionMenu;
    private PowerManager.WakeLock wakeLock;

    IntentFilter filter = new IntentFilter(CHANGE_GESTURE_OVERLAY);
    private final BroadcastReceiver gestureModeIconChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(CHANGE_GESTURE_OVERLAY_ICON_ID)) {
                int newDrawableId = intent.getIntExtra(CHANGE_GESTURE_OVERLAY_ICON_ID, 0);
                if (newDrawableId != 0) {
                    changeGestureModeIcon(newDrawableId);
                }
            }

            if (intent.hasExtra(CHANGE_GESTURE_OVERLAY_STRING_ID)) {
                int newStringId = intent.getIntExtra(CHANGE_GESTURE_OVERLAY_STRING_ID, 0);
                if (newStringId != 0) {
                    changeGestureModeString(newStringId);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTheme(((CldApplication) getApplication()).getThemeResId());
        super.setContentView(R.layout.global_layout);

        // initialize objects and listeners
        initListeners();
        initGestureLibrary();

        // TTS is initialized in the main activity

        this.topBarName = (TextView) findViewById(R.id.topBarName);

        registerReceiver(gestureModeIconChangeReceiver, filter);

        if (this.wakeLock == null) {
            this.wakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(
                    PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }

        getStateMachineManager().getStateMachine().onPause();
        getStateMachineManager().getStateMachine().stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(gestureModeIconChangeReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        getActivityLifecycleListener().onActivityStopped(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getActivityLifecycleListener().onActivityStarted(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (((CldApplication) getApplication()).isStartingViaAppIcon()) {
            AbstractActivity.setActivityInGestureMode(false);
            ((CldApplication) getApplication()).resetStartedViaAppIcon();
        } else if (((CldApplication) getApplication()).isResumingAfterCall()) {
            // set statemachine to sleep on incoming call
            startStateMachine(STATE_MANUAL_SLEEP);
            StateContent currentState = getStateMachineManager().getStateMachine().getCurrentState();
            String preSleepStateId = getStateMachineManager().getStateMachine().getPreSleepStateId();
            if (preSleepStateId != null) {
                if (preSleepStateId.contains("CFL_") || preSleepStateId.contains("NFC_")) {
                    getStateMachineManager().getStateMachine().setPreSleepStateId(STATE_START);
                    // currentState.setNextId(STATE_START);
                }
            }

            Log.d(TAG, "onResume() starts StateMachine after call in STATE_SLEEP");
            ((CldApplication) getApplication()).resetResumingAfterCall();
        }

        // recognize the current mode and switch to it when necessary
        if (isActivityInGestureMode()) {
            if (wakeLock != null && !wakeLock.isHeld()) {   
                wakeLock.acquire();
            }

            String beforePauseStateId = getStateMachineManager().getStateMachine().getCurrentStateId();

            if (beforePauseStateId == null) {
                startStateMachine();
                Log.d(TAG, "onResume() starts StateMachine from start");
            }

            if (gestureOverlay == null || !gestureOverlay.isEventsInterceptionEnabled()) {
                showGestureOverlay();
            }
        } else {
            if (wakeLock.isHeld()) {
                wakeLock.release();
            }
            if ((gestureOverlay != null && gestureOverlay.isEventsInterceptionEnabled())) {
                hideGestureOverlay();
            }
        }
    }

    /**
     * This method overrides the standard {@link setContentView()} of
     * {@link Activity} and inflates a layout into a predefined frame layout
     * instead of the whole screen
     * 
     * @param id
     *            Resource id of a layout to be inflated
     */
    @Override
    public void setContentView(int id) {
        // redirect content into designated contentView
        RelativeLayout globalContent = (RelativeLayout) findViewById(R.id.global_content);
        FrameLayout contentLayout = (FrameLayout) globalContent.findViewById(R.id.contentLayout);
        View content = findViewById(id);

        if (content == null) {
            // create view
            content = getLayoutInflater().inflate(id, contentLayout, false);
        }
        contentLayout.addView(content);
    }

    /**
     * Inflates a given layout into the bottom bar and sets it visible
     * 
     * @param id
     *            Resource id of a layout to be inflated
     */
    public void setBottomView(int id) {
        // redirect content into designated contentView
        RelativeLayout globalContent = (RelativeLayout) findViewById(R.id.global_content);
        FrameLayout bottomBarLayout = (FrameLayout) globalContent.findViewById(R.id.bottomBarLayout);
        View content = findViewById(id);

        if (content == null) {
            // create view
            content = getLayoutInflater().inflate(id, bottomBarLayout, false);
        }
        bottomBarLayout.addView(content);
        bottomBarLayout.setVisibility(View.VISIBLE);
    }

    /**
     * Sets the section name in the center top bar
     * 
     * @param name
     *            name to be displayed
     */
    public void setTopBarName(String name) {
        this.topBarName.setText(name);
    }

    /**
     * Sets the menu items background.
     */
    protected void setMenuBackground() {
        /** Step 1. setting the custom LayoutInflater.Factory instance. */
        getLayoutInflater().setFactory(new LayoutInflater.Factory() {

            public View onCreateView(final String name, final Context context, final AttributeSet attributeSet) {

                if (name.equalsIgnoreCase("com.android.internal.view.menu.IconMenuItemView")) {
                    try {
                        final LayoutInflater f = getLayoutInflater();
                        final View view = f.createView(name, null, attributeSet);

                        new Handler().post(new Runnable() {
                            public void run() {
                                TypedArray styled = obtainStyledAttributes(new int[] { R.attr.news_listitem,
                                        R.attr.blackWhiteColor });
                                view.setBackgroundDrawable(styled.getDrawable(0));
                                ((TextView) view).setTextColor(styled.getColor(1, 0));
                                styled.recycle();
                            }
                        });
                        return view;
                    } catch (final Exception e) {
                        Log.w(TAG, "No menu modification possible.");
                    }
                }
                return null;
            }

        });
    }

    /**
     * This method initializes all button listeners
     */
    private void initListeners() {
        // definition of the button for mode switching
        modeButton = findViewById(R.id.modeButton);
        modeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                modeButtonClicked();
            }
        });

        // definition of the button for returning to the main activity
        View homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                homeButtonClicked();
            }
        });
    }

    /**
     * This method initializes a gesture overlay and displays it
     */
    private void showGestureOverlay() {
        if (wakeLock != null && !wakeLock.isHeld()) {   
            wakeLock.acquire();
        }

        ((TextView) modeButton.findViewById(R.id.modeButtonText)).setText(getString(R.string.button_guimode));
        ((ImageView) modeButton.findViewById(R.id.modeButtonIcon)).setImageResource(R.drawable.d_tbar_right_eye);

        // lazy initialize the gesture overlay
        if (gestureOverlay == null) {
            View background = findViewById(R.id.gestureOverlay);
            gestureOverlay = (GestureOverlayView) background;
            gestureOverlay.addOnGesturePerformedListener(this);
            gestureOverlay.setOnTouchListener(this);
        }
        findViewById(R.id.gestureOverlayBackground).setVisibility(View.VISIBLE);
        gestureOverlay.setEventsInterceptionEnabled(true);

        getSchedulerManager().getNewsUpdateScheduler().unscheduleNewsUpdate();
        // set the current mode in the entire app
        setActivityInGestureMode(true);
    }

    /**
     * This method removes a displayed gesture overlay
     */
    private void hideGestureOverlay() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }

        if (loggingEnabled) {
            Log.d(TAG, "I will stop the StateMachine! (hideGestureOverlay)");
        }

        getStateMachineManager().getStateMachine().stop();

        ((TextView) modeButton.findViewById(R.id.modeButtonText)).setText(getString(R.string.button_gesturemode));
        ((ImageView) modeButton.findViewById(R.id.modeButtonIcon)).setImageResource(R.drawable.d_tbar_right_mouth);

        gestureOverlay.setEventsInterceptionEnabled(false);

        getSchedulerManager().getNewsUpdateScheduler().scheduleNewsUpdate();
        setActivityInGestureMode(false);
        // no home button necessary in the main activity
        if (!(this instanceof MainActivity)) {
            showHomeButton();
        }
        findViewById(R.id.gestureOverlayBackground).setVisibility(View.GONE);
    }

    /**
     * This method is called by pressing the mode button
     */
    private void modeButtonClicked() {
        if (!isActivityInGestureMode()) {
            showGestureOverlay();
            showPrompt(getString(R.string.prompt_speakmode));
            startStateMachine();
        } else {
            hideGestureOverlay();
            showPrompt(getString(R.string.prompt_guimode));
        }
    }

    /**
     * This method is called by pressing the home button
     */
    public void homeButtonClicked() {
        // return to main menu
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * This method uses the standard {@link Toast} API to display a message or
     * TTS to read it
     * 
     * @param message
     *            The string message to be shown
     */
    protected void showPrompt(String message) {
        // check if the prompt of the same type already exist
        if (prompt == null || prompt.getView().getId() != -1) {
            if (prompt != null)
                prompt.cancel();
            // create a Toast
            prompt = Toast.makeText(getBaseContext(), message, Toast.LENGTH_SHORT);
            // prompt.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        } else {
            // modify existing Toast
            prompt.cancel();
            prompt.setText(message);
        }
        // display prompt
        prompt.show();
    }

    /**
     * This method displays a customized {@link Toast} API message with a
     * default icon
     * 
     * @param message
     *            A string message to be shown
     */
    protected void showCustomPrompt(String message) {
        // show a custom prompt with a default image
        showCustomPrompt(message, R.drawable.app_icon);
    }

    /**
     * This method displays a customized {@link Toast} API message with a given
     * icon
     * 
     * @param message
     *            A string message to be shown
     * @param resImageId
     *            A drawable resource Id
     */
    protected void showCustomPrompt(String message, int resImageId) {
        // check if the prompt of the same type already exist
        if (prompt == null || prompt.getView().getId() == -1) {
            if (prompt != null)
                prompt.cancel();
            // create a custom toast
            LayoutInflater inflater = getLayoutInflater();
            View layout = inflater.inflate(R.layout.global_customprompt,
                    (ViewGroup) findViewById(R.id.customprompt_layout_root));

            prompt = new Toast(getBaseContext());
            // prompt.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            prompt.setDuration(Toast.LENGTH_SHORT);
            prompt.setView(layout);
        } else {
            prompt.cancel();
        }
        // set or modify prompt text message
        TextView textView = ((TextView) prompt.getView().findViewById(R.id.customprompt_text));
        textView.setCompoundDrawablesWithIntrinsicBounds(resImageId, 0, 0, 0);
        textView.setText(message);
        textView.setGravity(Gravity.CENTER);
        // display prompt
        prompt.show();
    }

    /**
     * This method is used to hide the home button
     */
    protected void hideHomeButton() {
        // hide home button in main layout and gesture mode
        View homeButton = findViewById(R.id.homeButton);
        ((TextView) homeButton.findViewById(R.id.homeButtonText)).setText("");

        TypedArray styled = obtainStyledAttributes(new int[] { R.attr.tbarLeftButtonHidden });
        homeButton.setBackgroundDrawable(styled.getDrawable(0));
        styled.recycle();
        ((ImageView) homeButton.findViewById(R.id.homeButtonIcon)).setImageDrawable(null);
        homeButton.setClickable(false);
    }

    /**
     * This method is used to hide the home button
     */
    protected void hideSpeakButton() {
        // hide speak button in main layout and gesture mode
        View speakButton = findViewById(R.id.modeButton);
        ((TextView) speakButton.findViewById(R.id.modeButtonText)).setText("");

        TypedArray styled = obtainStyledAttributes(new int[] { R.attr.tbarRightButtonHidden });
        speakButton.setBackgroundDrawable(styled.getDrawable(0));
        styled.recycle();
        ((ImageView) speakButton.findViewById(R.id.modeButtonIcon)).setImageDrawable(null);
        speakButton.setClickable(false);
    }

    /**
     * This method is used to show the home button
     */
    protected void showHomeButton() {
        // show home button
        View homeButton = findViewById(R.id.homeButton);
        ((TextView) homeButton.findViewById(R.id.homeButtonText)).setText(getString(R.string.button_home));

        TypedArray styled = obtainStyledAttributes(new int[] { R.attr.tbarLeftHome });
        ((ImageView) homeButton.findViewById(R.id.homeButtonIcon)).setImageDrawable(styled.getDrawable(0));
        styled.recycle();
        homeButton.setClickable(true);
    }

    /**
     * This method handles key event, meaning the hardware buttons especially
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (event.getKeyCode()) {
        case KeyEvent.KEYCODE_MENU:
            if (isActivityInGestureMode()) {
                Intent intent = new Intent(this, GesturesTutorialActivity.class);

                if (this instanceof NewsListActivity || this instanceof SmsListActivity
                        || this instanceof PhoneAddressbookListActivity || this instanceof MailListActivity
                        || this instanceof NewsDetailsActivity || this instanceof SmsDetailsActivity
                        || this instanceof MailDetailsActivity)
                    intent.putExtra(GesturesTutorialActivity.EXTRAS_SWIPE, true);
                if (this instanceof SmsListActivity || this instanceof SmsDetailsActivity)
                    intent.putExtra(GesturesTutorialActivity.EXTRAS_CIRCLE, true);
                startActivity(intent);
                return true;
            }
            break;
        case KeyEvent.KEYCODE_BACK:
            if (isTaskRoot()) {
                if (isActivityInGestureMode()) {
                    hideGestureOverlay();
                }
                setResult(RESULT_OK);
            }

            finish();
            break;
        default:
            // Log.v(TAG, "Unrecognized or unhandled KeyEvent" +
            // event.getKeyCode());
        }

        return false;
    }

    /**
     * This is a call-back method of the OnGesturePerformedListener interface to
     * react on gesture input
     */
    @Override
    public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
        if (gestureLibrary != null) {
            // iterate through the gesture library and make a prediction
            ArrayList<Prediction> predictions = gestureLibrary.recognize(gesture);
            for (Prediction p : predictions) {
                if (p.score > 1) {
                    onGesturePerformed(p);
                    break;
                }
            }
        }
    }

    /**
     * Subclasses must implement this method to load a raw gesture file to
     * {@code gestureLibrary}
     */
    protected abstract void initGestureLibrary();

    /**
     * This method is called with a recognized prediction as its value
     * 
     * @param prediction
     *            A predicted gesture
     */
    public void onGesturePerformed(Prediction prediction) {
        Log.i(TAG, prediction.name);
        getStateMachineManager().getStateMachine().reactOnGesture(prediction.name);
    }

    /**
     * This call-back method of {@link OnTouchListener} interface to handle
     * touch events
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return false;
    }

    /**
     * This method is called when a long-press is recognized in the gesture
     * overlay
     */
    protected void onLongPress() {
        Log.i(TAG, "Long-Tap");
        getStateMachineManager().getStateMachine().reactOnLongTap();
    }

    /**
     * This method is called when a single tap is recognized in the gesture
     * overlay
     */
    protected boolean onSingleTap() {
        Log.i(TAG, "Tap");
        getStateMachineManager().getStateMachine().reactOnTap();
        return false;
    }

    GestureDetector gestureDetector = new GestureDetector(new SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return AbstractActivity.this.onSingleTap();
        }

        @Override
        public void onLongPress(MotionEvent e) {
            AbstractActivity.this.onLongPress();
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

    });

    /**
     * Actions to start the {@link StateMachine} (normally load
     * {@link StateModel}, run start method of {@link StateMachine} etc.)
     * 
     */
    private void startStateMachine() {
        getStateMachineManager().getStateMachine().stop();
        StateModel newStateModel = getStateModel();
        if (newStateModel != null) {
            getStateMachineManager().getStateMachine().loadStateModel(newStateModel, true);
            getStateMachineManager().getStateMachine().start();
        }
    }

    /**
     * Actions to start the {@link StateMachine} (normally load
     * {@link StateModel}, run start method of {@link StateMachine} etc.)
     * 
     * @param startStateId
     *            The id of the start state
     */
    private void startStateMachine(String startStateId) {
        getStateMachineManager().getStateMachine().stop();
        StateModel newStateModel = getStateModel();
        if (newStateModel != null) {
            getStateMachineManager().getStateMachine().loadStateModel(newStateModel, true);
            getStateMachineManager().getStateMachine().start(startStateId);
        }
    }

    public abstract StateModel getStateModel();

    public void changeGestureModeIcon(int iconDrawableId) {
        ImageView gestureOverlayImageView = (ImageView) findViewById(R.id.gestureOverlayImageView);
        Drawable gestureIcon = getResources().getDrawable(iconDrawableId);
        if (gestureOverlayImageView != null && gestureIcon != null) {
            gestureOverlayImageView.setImageDrawable(gestureIcon);
        }
    }

    public void changeGestureModeString(int stringId) {
        TextView gestureOverlayTextView = (TextView) findViewById(R.id.gestureOverlayTextView);
        String gestureString = getResources().getString(stringId);
        if (gestureOverlayTextView != null && gestureString != null) {
            gestureOverlayTextView.setText(gestureString);
        }
    }

    /**
     * @return the activityInGestureMode
     */
    protected static boolean isActivityInGestureMode() {
        return activityInGestureMode;
    }

    /**
     * @param activityInGestureMode
     *            the activityInGestureMode to set
     */
    public static void setActivityInGestureMode(boolean activityInGestureMode) {
        AbstractActivity.activityInGestureMode = activityInGestureMode;
    }

    public IDataProviderManager getDataProviderManager() {
        return (IDataProviderManager) getApplication();
    }

    public ISchedulerManager getSchedulerManager() {
        return (ISchedulerManager) getApplication();
    }

    public IConfigurationManager getConfigurationManager() {
        return (IConfigurationManager) getApplication();
    }

    public IActivityLifecycleListener getActivityLifecycleListener() {
        return (IActivityLifecycleListener) getApplication();
    }

    public IStateMachineManager getStateMachineManager() {
        return (IStateMachineManager) getApplication();
    }
}
