package de.telekom.cldii.statemachine.states;

import static de.telekom.cldii.statemachine.StateMachineConstants.GESTURE_SWIPE_LEFTTORIGHT;
import static de.telekom.cldii.statemachine.StateMachineConstants.GESTURE_SWIPE_RIGHTTOLEFT;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_LISTEND;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_SLEEP;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_SPEAK_EMAIL;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_SPEAK_NEWS;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_SPEAK_PHONE;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_SPEAK_SMS;
import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_START;
import static de.telekom.cldii.statemachine.StateMachineConstants.TIME_TO_WAIT_BIG;
import android.content.Context;
import android.content.res.TypedArray;
import de.telekom.cldii.R;
import de.telekom.cldii.statemachine.StateContent;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.util.SignalTonePlayer;
import de.telekom.cldii.view.main.MainActivity;

public class StateModelMainMenu extends StateModel {

    /**
     * TAG for Log methods
     */
    protected static final String TAG = "StateModelMainMenu";

    public StateModelMainMenu(final Context context) {
        super(context);

        // STATE START -------------
        StateContent start = new StateContent(context) {

            @Override
            public void executeInState() {
                super.executeInState();

                // Landmarking prompt
                getStateMachine().speakAndNextState(context.getString(R.string.section_main), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };
        start.setNextId(STATE_SPEAK_PHONE);
        addStateContent(STATE_START, start);

        // STATE STATE_SPEAK_PHONE -------------
        StateContent stateContent = new StateContent(context) {

            @Override
            public void reactOnGesture(String gestureName) {
                super.reactOnGesture(gestureName);

                if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {
                    // PREVIOUS
                    getStateMachine().changeState(getStateId(), STATE_SPEAK_NEWS);
                } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                    // NEXT
                    getStateMachine().changeState(getStateId(), STATE_SPEAK_SMS);
                }

            }

            @Override
            public void reactOnTap() {
                super.reactOnTap();

                ((MainActivity) context).phoneButtonClicked();
            }

            @Override
            public void executeInState() {
                super.executeInState();

                getStateMachine().speakAndNextState(context.getString(R.string.tts_phone), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId(STATE_SPEAK_SMS);
        addStateContent(STATE_SPEAK_PHONE, stateContent);

        // STATE STATE_SPEAK_SMS -------------
        stateContent = new StateContent(context) {

            @Override
            public void reactOnGesture(String gestureName) {
                super.reactOnGesture(gestureName);

                if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {
                    // PREVIOUS
                    getStateMachine().changeState(getStateId(), STATE_SPEAK_PHONE);
                } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                    // NEXT
                    getStateMachine().changeState(getStateId(), STATE_SPEAK_EMAIL);
                }

            }

            @Override
            public void reactOnTap() {
                super.reactOnTap();

                ((MainActivity) context).smsButtonClicked();
            }

            @Override
            public void executeInState() {
                super.executeInState();

                getStateMachine()
                        .speakAndNextState(context.getString(R.string.tts_sms), getStateId(), TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId(STATE_SPEAK_EMAIL);
        addStateContent(STATE_SPEAK_SMS, stateContent);

        // STATE STATE_SPEAK_EMAIL -------------
        stateContent = new StateContent(context) {

            @Override
            public void reactOnGesture(String gestureName) {
                super.reactOnGesture(gestureName);

                if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {
                    // PREVIOUS
                    getStateMachine().changeState(getStateId(), STATE_SPEAK_SMS);
                } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                    // NEXT
                    getStateMachine().changeState(getStateId(), STATE_SPEAK_NEWS);
                }
            }

            @Override
            public void reactOnTap() {
                super.reactOnTap();

                ((MainActivity) context).emailButtonClicked();
            }

            @Override
            public void executeInState() {
                super.executeInState();

                getStateMachine().speakAndNextState(context.getString(R.string.tts_email), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId(STATE_SPEAK_NEWS);
        addStateContent(STATE_SPEAK_EMAIL, stateContent);

        // STATE STATE_SPEAK_NEWS -------------
        stateContent = new StateContent(context) {

            @Override
            public void reactOnGesture(String gestureName) {
                super.reactOnGesture(gestureName);

                if (gestureName.equals(GESTURE_SWIPE_LEFTTORIGHT)) {
                    // PREVIOUS
                    getStateMachine().changeState(getStateId(), STATE_SPEAK_EMAIL);
                } else if (gestureName.equals(GESTURE_SWIPE_RIGHTTOLEFT)) {
                    // NEXT
                    getStateMachine().changeState(getStateId(), STATE_SPEAK_PHONE);
                }

            }

            @Override
            public void reactOnTap() {
                super.reactOnTap();

                ((MainActivity) context).newsButtonClicked();
            }

            @Override
            public void executeInState() {
                super.executeInState();

                getStateMachine().speakAndNextState(context.getString(R.string.tts_news), getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };
        stateContent.setNextId(STATE_LISTEND);
        addStateContent(STATE_SPEAK_NEWS, stateContent);

        // STATE STATE_LISTEND -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                if (getStateMachine().getRepeatCounter() == 0) {
                    this.setNextId(STATE_SPEAK_PHONE);
                    getStateMachine().increaseRepeatCounter();
                } else {
                    this.setNextId(STATE_SLEEP);
                }

                super.executeInState();
                getStateMachine().speakAndNextState(SignalTonePlayer.SIGNAL_TONE_LISTEND, getStateId(),
                        TIME_TO_WAIT_BIG);
            }
        };
        addStateContent(STATE_LISTEND, stateContent);

        // STATE STATE_SLEEP -------------
        stateContent = new StateContent(context) {

            @Override
            public void executeInState() {
                TypedArray a = context.obtainStyledAttributes(new int[] { R.attr.gestureOverlayMouthCrossed });
                int attributeResourceId = a.getResourceId(0, 0);
                setGestureOverlayIconId(attributeResourceId);
                a.recycle();
                super.executeInState();
            }

            @Override
            public void reactOnTap() {
                super.reactOnTap();
                getStateMachine().resetRepeatCounter();
                getStateMachine().start();
            }
        };
        stateContent.setGestureOverlayStringId(R.string.gesture_overlay_silence);
        addStateContent(STATE_SLEEP, stateContent);
    }

}
