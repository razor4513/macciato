package de.telekom.cldii.view;

import android.app.Activity;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import de.telekom.cldii.CldApplication;
import de.telekom.cldii.R;

/**
 * This activity displays an overlay over the gesture overlay describing all
 * possible gestures
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */
public class GesturesTutorialActivity extends Activity {

    private enum GestureTypes {
        LONG_TAP, SWIPE, ROOF, TAP, CIRCLE
    }

    public static final String EXTRAS_LONG_TAP = "EXTRAS_LONG_TAP";
    public static final String EXTRAS_SWIPE = "EXTRAS_SWIPE";
    public static final String EXTRAS_ROOF = "EXTRAS_ROOF";
    public static final String EXTRAS_TAP = "EXTRAS_TAP";
    public static final String EXTRAS_CIRCLE = "EXTRAS_CIRCLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(((CldApplication) getApplication()).getThemeResId());
        setContentView(R.layout.global_gesturetutorial);

        // Initialize default setting
        changeGestureIcon(GestureTypes.SWIPE, false);
        changeGestureIcon(GestureTypes.CIRCLE, false);

        findViewById(R.id.closeButton).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        String[] extrasArray = { EXTRAS_LONG_TAP, EXTRAS_SWIPE, EXTRAS_ROOF, EXTRAS_TAP, EXTRAS_CIRCLE };
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            for (int i = 0; i < extrasArray.length; i++) {
                if (extras.containsKey(extrasArray[i]))
                    changeGestureIcon(GestureTypes.values()[i], extras.getBoolean(extrasArray[i]));
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (event.getKeyCode()) {
        case KeyEvent.KEYCODE_MENU:
        case KeyEvent.KEYCODE_BACK:
            finish();
            break;
        default:
            break;
        }
        return false;
    }

    /*
     * Private method to de-/activate a gesture icon by making it a little
     * transparent
     */
    private void changeGestureIcon(GestureTypes type, boolean isActivated) {
        ImageView gestureView = null;
        TextView gestureTextView = null;
        TextView gestureEventTextView = null;
        switch (type) {
        case LONG_TAP:
            gestureView = (ImageView) findViewById(R.id.longPressIcon);
            gestureTextView = (TextView) findViewById(R.id.longPress);
            gestureEventTextView = (TextView) findViewById(R.id.longPressEvent);
            break;
        case SWIPE:
            gestureView = (ImageView) findViewById(R.id.swipeIcon);
            gestureTextView = (TextView) findViewById(R.id.swipe);
            gestureEventTextView = (TextView) findViewById(R.id.swipeEvent);
            break;
        case ROOF:
            gestureView = (ImageView) findViewById(R.id.roofIcon);
            gestureTextView = (TextView) findViewById(R.id.roof);
            gestureEventTextView = (TextView) findViewById(R.id.roofEvent);
            break;
        case TAP:
            gestureView = (ImageView) findViewById(R.id.tapIcon);
            gestureTextView = (TextView) findViewById(R.id.tap);
            gestureEventTextView = (TextView) findViewById(R.id.tapEvent);
            break;
        case CIRCLE:
            gestureView = (ImageView) findViewById(R.id.circleIcon);
            gestureTextView = (TextView) findViewById(R.id.circle);
            gestureEventTextView = (TextView) findViewById(R.id.circleEvent);
            break;
        }
        if (gestureView != null) {
            if (isActivated) {
                gestureView.setAlpha(255);
                TypedArray styled = obtainStyledAttributes(new int[] { R.attr.header_title, R.attr.grid_plates });
                gestureTextView.setTextColor(styled.getColor(0, 0));
                gestureEventTextView.setTextColor(styled.getColor(1, 0));
                styled.recycle();
            } else {
                gestureView.setAlpha(77);
                gestureTextView.setTextColor(getResources().getColor(R.color.gesture_deactivated));
                gestureEventTextView.setTextColor(getResources().getColor(R.color.gesture_deactivated));
            }
        }
    }
}
