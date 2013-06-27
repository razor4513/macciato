package de.telekom.cldii.statemachine;

import static de.telekom.cldii.statemachine.StateMachineConstants.CHANGE_GESTURE_OVERLAY;
import static de.telekom.cldii.statemachine.StateMachineConstants.CHANGE_GESTURE_OVERLAY_ICON_ID;
import static de.telekom.cldii.statemachine.StateMachineConstants.CHANGE_GESTURE_OVERLAY_STRING_ID;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.util.Log;
import de.telekom.cldii.ApplicationConstants;
import de.telekom.cldii.R;

public abstract class StateContent {
    /**
     * Activate/deactivate statemachine log.
     */
    private final boolean loggingEnabled = ApplicationConstants.STATEMACHINE_LOGGING;

    /**
     * TAG for Logging.
     */
    private static final String TAG = "StateContent";
    /**
     * Next state id.
     */
    private String nextId;
    /**
     * State id of the StateContent.
     */
    private String stateId;
    /**
     * Default gesture overlay day icon id.
     */
    private int gestureOverlayDayIconId;
    /**
     * Default gesture overlay {@link String} id.
     */
    private int gestureOverlayStringId = R.string.gesture_overlay_defaulttext;

    /**
     * The submitted activity context.
     */
    private Context activityContext;

    public StateContent(Context context) {
        this.activityContext = context;
    }

    /**
     * Called by {@link StateMachine} when a tap was recognized.
     */
    public void reactOnTap() {
        if (loggingEnabled) {
            Log.i(TAG, "reactOnTap in state " + stateId);
        }
    }

    /**
     * Called by {@link StateMachine} when a gesture was recognized.
     * 
     * @param name
     */
    public void reactOnGesture(String name) {
        if (loggingEnabled) {
            Log.i(TAG, "reactOnGesture " + name + " in state " + stateId);
        }

    }

    /**
     * Executed when entering a new state.
     */
    public void executeInState() {
        if (getGestureOverlayIconId() == 0) {
            TypedArray a = activityContext.obtainStyledAttributes(new int[] { R.attr.gestureOverlaySpeechBalloon });
            int attributeResourceId = a.getResourceId(0, 0);
            setGestureOverlayIconId(attributeResourceId);
            a.recycle();
        }

        if (loggingEnabled) {
            Log.i(TAG, "executeInState in state " + stateId);
        }
        if (loggingEnabled) {
            Log.i(TAG, "Set gesture overlay icon to id: " + gestureOverlayDayIconId);
        }
        // Send Broadcast to change the gesture overlay icon
        Intent intent = new Intent(CHANGE_GESTURE_OVERLAY);
        intent.putExtra(CHANGE_GESTURE_OVERLAY_ICON_ID, gestureOverlayDayIconId);
        intent.putExtra(CHANGE_GESTURE_OVERLAY_STRING_ID, gestureOverlayStringId);
        if (activityContext != null) {
            activityContext.sendBroadcast(intent);
        }
    }

    /**
     * Called by {@link StateMachine} directly before a state is changed.
     * 
     * @param oldStateId
     * @param newStateId
     */
    public void directlyBeforeStateChange(String oldStateId, String newStateId) {
        if (loggingEnabled) {
            Log.i(TAG, "onStateChange in state " + stateId);
        }
    }

    public String getNextId() {
        return nextId;
    }

    public void setNextId(String childId) {
        this.nextId = childId;
    }

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public int getGestureOverlayIconId() {
        return gestureOverlayDayIconId;
    }

    public void setGestureOverlayIconId(int gestureOverlayIconId) {
        this.gestureOverlayDayIconId = gestureOverlayIconId;
    }

    public int getGestureOverlayStringId() {
        return gestureOverlayStringId;
    }

    public void setGestureOverlayStringId(int gestureOverlayStringId) {
        this.gestureOverlayStringId = gestureOverlayStringId;
    }
}
