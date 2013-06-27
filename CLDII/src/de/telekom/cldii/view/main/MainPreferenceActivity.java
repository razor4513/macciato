package de.telekom.cldii.view.main;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import de.telekom.cldii.R;

public class MainPreferenceActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.mainpreferences);
    }
}
