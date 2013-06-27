package de.telekom.cldii.view.util;

import android.os.Bundle;
import de.telekom.cldii.CldApplication;
import de.telekom.cldii.R;
import de.telekom.cldii.statemachine.StateModel;
import de.telekom.cldii.view.AbstractActivity;

/**
 * Displays the legal notice for the application.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public class LegalNoticeActivity extends AbstractActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setTheme(((CldApplication) getApplication()).getThemeResId());
        setContentView(R.layout.legal_notice);
        setTopBarName(getResources().getString(R.string.impressum_title));
        hideSpeakButton();
        hideHomeButton();
    }

    @Override
    protected void initGestureLibrary() {
        // gesture mode not supproted in this activity
    }

    @Override
    public StateModel getStateModel() {
        // gesture mode not supproted in this activity
        return null;
    }

}
