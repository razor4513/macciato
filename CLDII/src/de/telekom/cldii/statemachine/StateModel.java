package de.telekom.cldii.statemachine;

import static de.telekom.cldii.statemachine.StateMachineConstants.STATE_MANUAL_SLEEP;

import java.util.HashMap;

import de.telekom.cldii.R;

import android.content.Context;
import android.content.res.TypedArray;

/**
 * {@link StateModel} class for holding {@link StateContent}s in a
 * {@link HashMap}. Needs at least states START and SLEEP.
 * 
 * @author Sebastian Stallenberger, Jambit GmbH
 * 
 */
public class StateModel {

    /**
     * A {@link HashMap} containing all states of the {@link StateModel}. Key is
     * the state id and value the StateContent.
     */
    private HashMap<String, StateContent> stateMap;

    /**
     * The application context.
     */
    protected Context context;

    /**
     * The state machine where the model is executed
     */
    protected StateMachine stateMachine;

    /**
     * Constructor for a StateModel.
     * 
     * @param context
     *            Application context
     */
    public StateModel(final Context context) {
        this.stateMap = new HashMap<String, StateContent>();
        this.context = context;

        // STATE STATE_MANUAL_SLEEP -------------
        StateContent stateContent = new StateContent(context) {

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
                getStateMachine().start(getStateMachine().getPreSleepStateId());
            }
        };
        stateContent.setGestureOverlayStringId(R.string.gesture_overlay_silence);
        addStateContent(STATE_MANUAL_SLEEP, stateContent);
    }

    /**
     * Adds a {@link StateContent} to the {@link StateModel} {@link HashMap}.
     * 
     * @param stateId
     *            The state id
     * @param stateContent
     *            The {@link StateContent}
     */
    public void addStateContent(String stateId, StateContent stateContent) {
        stateContent.setStateId(stateId);
        stateMap.put(stateId, stateContent);
    }

    /**
     * Return the {@link HashMap} containing all {@link StateContent}s of the
     * {@link StateModel}.
     * 
     * @return {@link HashMap}
     */
    public HashMap<String, StateContent> getStateMap() {
        return stateMap;
    }

    /**
     * Clear the state {@link HashMap}.
     */
    public void removeAllContents() {
        stateMap.clear();
    }

    protected void setStateMachine(StateMachine stateMachine) {
        this.stateMachine = stateMachine;
    }

    protected StateMachine getStateMachine() {
        return this.stateMachine;
    }
}
