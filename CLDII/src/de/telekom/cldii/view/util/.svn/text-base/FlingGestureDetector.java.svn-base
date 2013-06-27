package de.telekom.cldii.view.util;

import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

/**
 * An extended {@link SimpleOnGestureListener} with implementation for fling
 * gesture
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */
public class FlingGestureDetector extends SimpleOnGestureListener {
    private final OnFlingListener listener;
    private final int SWIPE_MIN_DISTANCE = 100;
    private final int SWIPE_MAX_OFF_PATH = 450;
    private final int SWIPE_THRESHOLD_VELOCITY = 150;

    public FlingGestureDetector(OnFlingListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (e1 == null || e2 == null)
            return false;
        android.util.Log.v("Fling", "Fling pathX: " + (e1.getX() - e2.getX()) + "/" + (e2.getX() - e1.getX()) + " = "
                + (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) + "/" + (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE)
                + " | pathY: " + (e1.getY() - e2.getY()) + " | speedX: " + velocityX + " = "
                + (Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY));

        try {
            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                return false;
            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                listener.onLeftFling();
                return true;
            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                listener.onRightFling();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
