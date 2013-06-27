package de.telekom.cldii.statemachine;

import static de.telekom.cldii.statemachine.StateMachineConstants.GESTURE_HOOKUP;
import static de.telekom.cldii.statemachine.StateMachineConstants.ID_AND_DURATION_SEPERATOR;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATEMACHINE_NEXTSTATE;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATEMACHINE_WILL_CHANGE_NEWSTATEID;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATEMACHINE_WILL_CHANGE_STATE;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_MANUAL_SLEEP;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_SLEEP;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_START;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_STOP;

import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import de.telekom.cldii.ApplicationConstants;
import de.telekom.cldii.config.ITextToSpeechManager;
import de.telekom.cldii.statemachine.states.StateModelMailDetails;
import de.telekom.cldii.statemachine.states.StateModelNewsDetails;
import de.telekom.cldii.statemachine.states.StateModelSms;
import de.telekom.cldii.util.SignalTonePlayer;
import de.telekom.cldii.view.AbstractActivity;
import de.telekom.cldii.view.main.MainActivity;

/**
 * A state machine framework for individual processes. Load a StateModel
 * containing your process description using
 * loadStateModel(stateModel,forceFlag). If you want to overwrite the current
 * StateModel use the force flag.
 * 
 * @author Sebastian Stallenberger, Jambit GmbH
 * 
 */
public class StateMachine implements OnUtteranceCompletedListener {

    /**
     * Activate/deactivate statemachine log.
     */
    private final boolean loggingEnabled = ApplicationConstants.STATEMACHINE_LOGGING;

    /**
     * TAG for {@link Log} methods
     */
    private static final String TAG = "StateMachine";

    /**
     * The application context
     */
    private Context context;

    /**
     * The {@link StateModel} if it is loaded
     */
    private StateModel stateModel;

    /**
     * The Id of the state before change to sleep mode (so we can switch back to
     * it later)
     */
    private String preSleepStateId;

    /**
     * The Id of the current state
     */
    private String currentStateId;

    /**
     * The {@link StateContent} of the current state
     */
    private StateContent currentState;

    /**
     * The start state of the loaded {@link StateModel}
     */
    private String startState;

    /**
     * Is a state doing something?
     * 
     * @default false
     */
    private Boolean isInState = false;

    // /**
    // * The {@link BroadcastReceiver} to receive state change broadcasts etc.
    // */
    // private BroadcastReceiver broadcastReceiver;

    /**
     * For repeat counting.
     */
    private int repeatCounter;

    /**
     * Thread for waiting between state changes
     */
    private Thread sleepThread = null;

    /**
     * tts manager for accessing the tts engine.
     */
    private ITextToSpeechManager ttsManager;

    /**
     * Constructor for the {@link StateMachine}. Registers a
     * {@link BroadcastReceiver}.
     * 
     * @param context
     *            The application context
     */
    public StateMachine(final Context context, ITextToSpeechManager ttsManager) {
        this.context = context;
        this.ttsManager = ttsManager;
    }

    // /**
    // * Destructor for the {@link StateMachine}. Unregisters the
    // * {@link BroadcastReceiver}.
    // */
    // @Override
    // protected final void finalize() throws Throwable {
    // super.finalize();
    // }

    /**
     * Loads a {@link StateModel} into the {@link StateMachine}.
     * 
     * @param stateModel
     *            The {@link StateModel} object
     * @param forceFlag
     *            Flag to set if existing {@link StateModel} will be overwritten
     *            or not
     */
    public final void loadStateModel(final StateModel stateModel, final boolean forceFlag) {
        if (stateModelIsOk(stateModel)) {
            if (this.stateModel == null) {
                this.stateModel = stateModel;
                if (loggingEnabled) {
                    Log.i(TAG, "State Model set.");
                }
            } else {
                if (forceFlag) {
                    if (loggingEnabled) {
                        Log.i(TAG, "I will stop the State.. ehm myself! (loadStateModel force)");
                    }
                    stop();
                    this.stateModel = stateModel;
                    if (loggingEnabled) {
                        Log.i(TAG, "State Model is already existing, but I changed it.");
                    }
                } else {
                    if (loggingEnabled) {
                        Log.i(TAG, "State Model is already existing. I didn't change it.");
                    }
                }
            }
            this.stateModel.setStateMachine(this);
        }
    }

    /**
     * Use the next state defined in current {@link StateContent} to call
     * changeState(...).
     */
    public final void nextState(String checkId, boolean force) {
        if (stateModel != null && currentState != null && currentStateId != null && !currentStateId.equals(STATE_STOP)) {
            if (loggingEnabled) {
                Log.i(TAG, "nextState");
            }
            if (force) {
                Log.e(TAG, "Forced nextState");
                changeState(currentStateId, currentState.getNextId());
            } else {
                // check if state who triggered this nextState command is the
                // current state (it should be the same)
                if (checkId.equals(currentStateId)) {
                    changeState(currentStateId, currentState.getNextId());
                } else {
                    Log.w(TAG, "Security warning: Unauthorized state change requested by state " + checkId
                            + ". (Should be " + currentStateId + ")");
                }
            }
        }
    }

    /**
     * Switch to the State with Id newStateId.
     * 
     * @param oldStateId
     * @param newStateId
     */
    public void changeState(final String oldStateId, final String newStateId) {
        interruptSleep();
        if (ttsManager != null && ttsManager.getTextToSpeech() != null) {
            ttsManager.getTextToSpeech().stop();
        }

        if (currentState != null) {
            currentState.directlyBeforeStateChange(oldStateId, newStateId);
        }

        // Send Broadcast that state will change
        Intent intent = new Intent(STATEMACHINE_WILL_CHANGE_STATE);
        intent.putExtra(STATEMACHINE_WILL_CHANGE_NEWSTATEID, newStateId);
        if (context != null) {
            context.sendBroadcast(intent);
        }

        if (stateModel != null && oldStateId != null && newStateId != null) {
            if (!stateModel.getStateMap().containsKey(newStateId)) {
                if (loggingEnabled) {
                    Log.e(TAG, "Unknown state: " + newStateId);
                }
            } else {
                if (!isInState) {
                    isInState = true;
                    currentStateId = newStateId;
                    if (stateModel.getStateMap() != null) {
                        currentState = stateModel.getStateMap().get(newStateId);
                    }

                    currentState.executeInState();
                    isInState = false;
                }
            }
        }

    }

    /**
     * Starts the {@link StateMachine} using the default start state
     * STATE_START. If the state model contains a state STATE_READGESTUREPROMPT
     * this model is the used state.
     */
    public final void start() {
        start(STATE_START);
    }

    /**
     * Starts the {@link StateMachine} using the parameter stateId as start
     * state.
     * 
     * @param stateId
     */
    public final void start(final String stateId) {
        if (stateModel != null) {
            this.startState = stateId;
            changeState(STATE_START, this.startState);
        } else {
            if (loggingEnabled) {
                Log.e(TAG, "No State Model loaded.");
            }
        }
    }

    /**
     * Stop the {@link StateMachine}. Stop TTS and set current
     * {@link StateModel}, current {@link StateContent} and current state id to
     * null.
     */
    public final void stop() {
        currentState = null;
        currentStateId = null;
        if (stateModel != null) {
            interruptSleep();
        }
        stateModel = null;
        if (ttsManager.getTextToSpeech() != null) {
            ttsManager.getTextToSpeech().stop();
        }

        resetRepeatCounter();
        if (loggingEnabled) {
            Log.i(TAG, "Statemachine stopped");
        }
    }

    public void onPause() {
        if (currentState != null) {
            preSleepStateId = currentState.getStateId();
            if (preSleepStateId.contains("OPTIONSMENU")) {
                if (getStateModel().getClass().equals(StateModelSms.class)) {
                    preSleepStateId = ((StateModelSms) getStateModel()).getCurrentItemBeforeOptionsMenuId();
                } else if (getStateModel().getClass().equals(StateModelNewsDetails.class)) {
                    preSleepStateId = ((StateModelNewsDetails) getStateModel()).getCurrentItemBeforeOptionsMenuId();
                } else if (getStateModel().getClass().equals(StateModelMailDetails.class)) {
                    preSleepStateId = ((StateModelMailDetails) getStateModel()).getCurrentItemBeforeOptionsMenuId();
                }
            }
        }
    }

    public void onResume() {
        // "CFL_"
    }

    /**
     * Checks if the passed {@link StateModel} is valid.
     * 
     * @param stateModel
     *            A {@link StateModel}
     * @return True if {@link StateModel} is valid, false if not
     */
    private Boolean stateModelIsOk(final StateModel stateModel) {
        Boolean returnBool = true;
        if (stateModel == null) {
            returnBool = false;
            if (loggingEnabled) {
                Log.e(TAG, "State Model doesn't exist.");
            }
        } else {
            if (stateModel.getStateMap() == null) {
                returnBool = false;
                if (loggingEnabled) {
                    Log.e(TAG, "State Model has no StateMap.");
                }
            } else {
                if (!stateModel.getStateMap().containsKey(STATE_START)) {
                    returnBool = false;
                    if (loggingEnabled) {
                        Log.e(TAG, "State Model has no State 'STATE_START'.");
                    }
                } else if (!stateModel.getStateMap().containsKey(STATE_SLEEP)) {
                    returnBool = false;
                    if (loggingEnabled) {
                        Log.e(TAG, "State Model has no State 'STATE_SLEEP'.");
                    }
                }
            }
        }

        return returnBool;
    }

    /**
     * Calls the reactOnTap method of the current state.
     */
    public final void reactOnTap() {
        if (currentState != null) {
            currentState.reactOnTap();
        }

    }

    /**
     * Calls the reactOnLongTap method of the current state.
     */
    public final void reactOnLongTap() {
        if (currentState != null) {
            preSleepStateId = currentState.getStateId();
            if (loggingEnabled) {
                Log.i(TAG, "Saved preSleepStateId: " + preSleepStateId);
            }
            changeState(currentState.getStateId(), STATE_MANUAL_SLEEP);
        }

    }

    /**
     * Calls the reactOnGesture method of the current state. If gestureName is
     * "Dach" it changes to main menu.
     */
    public void reactOnGesture(String gestureName) {
        if (gestureName.equals(GESTURE_HOOKUP)) {
            stop();

            goBackToMainMenu();

            if (loggingEnabled) {
                Log.d(TAG, "GestureMode: Back to HOME");
            }
        } else {
            if (currentState != null) {
                currentState.reactOnGesture(gestureName);
            }
        }
    }

    /**
     * Returns the id of the current state.
     * 
     * @return currentStateId
     */
    public final String getCurrentStateId() {
        return currentStateId;
    }

    /**
     * Set the current state id.
     * 
     * @param currentStateId
     *            The current state id
     */
    public final void setCurrentStateId(final String currentStateId) {
        this.currentStateId = currentStateId;
    }

    /**
     * Get the current {@link StateContent}.
     * 
     * @return current {@link StateContent}
     */
    public final StateContent getCurrentState() {
        return currentState;
    }

    /**
     * Set the current state.
     * 
     * @param currentState
     *            {@link StateContent}
     */
    public final void setCurrentState(final StateContent currentState) {
        this.currentState = currentState;
    }

    /**
     * Get the {@link StateModel}.
     * 
     * @return Loaded {@link StateModel}.
     */
    public final StateModel getStateModel() {
        return stateModel;
    }

    /**
     * Set the {@link StateModel}.
     * 
     * @param stateObject
     *            {@link StateModel}
     */
    public final void setStateModel(final StateModel stateObject) {
        this.stateModel = stateObject;
    }

    /**
     * Get the start state.
     * 
     * @return State id of the start state.
     */
    public final String getStartState() {
        return startState;
    }

    /**
     * Set the start state.
     * 
     * @param startState
     *            State id
     */
    public final void setStartState(final String startState) {
        this.startState = startState;
    }

    /**
     * Get the value of the repeat counter.
     * 
     * @return Repeat counter value
     */
    public final int getRepeatCounter() {
        return repeatCounter;
    }

    /**
     * Returns state id of state before sleep.
     * 
     * @return
     */
    public String getPreSleepStateId() {
        return preSleepStateId;
    }

    /**
     * Set state id of state before sleep.
     * @param preSleepStateId
     */
    public void setPreSleepStateId(String preSleepStateId) {
        this.preSleepStateId = preSleepStateId;
    }

    /**
     * Increase repeat counter by one.
     */
    public final void increaseRepeatCounter() {
        this.repeatCounter++;
    }

    /**
     * Reset repeat counter to 0.
     */
    public final void resetRepeatCounter() {
        this.repeatCounter = 0;
    }

    @Override
    public void onUtteranceCompleted(String utteranceId) {
        if (utteranceId.length() >= STATEMACHINE_NEXTSTATE.length()) {
            if (STATEMACHINE_NEXTSTATE.equals(utteranceId.substring(0, STATEMACHINE_NEXTSTATE.length()))) {
                String subString = utteranceId.substring(STATEMACHINE_NEXTSTATE.length());
                if (subString.contains(ID_AND_DURATION_SEPERATOR)) {
                    String[] idDurationArray = subString.split(ID_AND_DURATION_SEPERATOR);
                    if (idDurationArray.length == 2) {
                        waitBeforeNextState(Integer.valueOf(idDurationArray[1]), idDurationArray[0]);
                    }
                }
            }
        }
    }

    /**
     * 1. If the textToRead is the SIGNAL_TONE_LISTEND of
     * {@link SignalTonePlayer}, it will play the listend sound and then use the
     * waitBeforeNextState method of {@link AbstractActivity}.
     * 
     * 2. Reads a text (add it to the {@link TextToSpeech} queue). Passes a
     * combination of STATEMACHINE_NEXTSTATE, the senderStateId, a separator
     * (ID_AND_DURATION_SEPERATOR) and timeToWaitAfterSpeak as utteranceId.
     * 
     * @param textToRead
     *            Text to read.
     * @param senderStateId
     *            The state Id of the executing state.
     * @param timeToWaitAfterSpeak
     *            Time to wait after speaking or playing sound.
     */
    public void speakAndNextState(final String textToRead, final String senderStateId, final int timeToWaitAfterSpeak) {
        if (ttsManager != null) {
            if (textToRead.equals(SignalTonePlayer.SIGNAL_TONE_LISTEND)) {
                // SignalTonePlayer.playListendSound(context);
                waitBeforeNextState(timeToWaitAfterSpeak, senderStateId);
            } else {
                if (ttsManager.getTextToSpeech() == null) {
                    Log.w(TAG, "TTS Error: ttsManager.getTextToSpeech() == null");
                    ttsManager.setTextToSpeechReadyForInitialization(true);
                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            for (int i = 0; i < 10; i++) {
                                if (!ttsManager.ttsIsInitializing()) {
                                    break;
                                }
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            if (!ttsManager.ttsIsInitializing()) {
                                speakText(textToRead, senderStateId, timeToWaitAfterSpeak);
                            }
                        }
                    }).start();
                } else {
                    speakText(textToRead, senderStateId, timeToWaitAfterSpeak);
                }
            }
        } else {
            Log.w(TAG, "TTS Error: ttsManager == null");
        }
    }

    private void speakText(String textToRead, String senderStateId, int timeToWaitAfterSpeak) {

        if (sleepThread != null && sleepThread.isAlive()) {
            sleepThread.interrupt();
        }

        HashMap<String, String> ttsParams = new HashMap<String, String>();
        ttsParams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, STATEMACHINE_NEXTSTATE + senderStateId
                + ID_AND_DURATION_SEPERATOR + timeToWaitAfterSpeak);

        ttsManager.getTextToSpeech().speak(textToRead, TextToSpeech.QUEUE_FLUSH, ttsParams);
    }

    /**
     * Takes duration in milliseconds and waits this time. Then it tells the
     * StateMachine to change to next state.
     * 
     * @param duration
     *            The time to wait
     */
    protected void waitBeforeNextState(final int duration, final String senderStateId) {

        if (sleepThread != null && sleepThread.isAlive()) {
            sleepThread.interrupt();
        }

        sleepThread = new Thread("SleepThread") {

            @Override
            public void run() {
                Looper.prepare();
                try {
                    if (loggingEnabled) {
                        Log.v(TAG, "Sleep" + duration);
                    }
                    if (loggingEnabled) {
                        Log.v(TAG, Thread.currentThread().getName());
                    }
                    sleep(duration);
                    if (loggingEnabled) {
                        Log.v(TAG, "Trigger next State");
                    }
                    nextState(senderStateId, false);
                } catch (InterruptedException e) {
                }
            }

        };

        sleepThread.start();
    }

    /**
     * Called from StateMachine if it was stopped.
     */
    private void interruptSleep() {
        if (sleepThread != null && sleepThread.isAlive()) {
            sleepThread.interrupt();
            sleepThread = null;
        }
    }

    public void goBackToMainMenu() {
        // Back to main menu
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // NEW_TASK flag required to start an activity from outside an Activity
        // context.
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
