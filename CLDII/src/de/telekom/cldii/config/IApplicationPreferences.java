package de.telekom.cldii.config;

import android.content.Context;

/**
 * Changeable application preferences.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public interface IApplicationPreferences {

    /**
     * Called on creation of the preferences.
     * 
     * @param context
     */
    public void onCreate(Context context);

    /**
     * @return true if application sample data has been created
     */
    public boolean isInitialApplicationDataCreated();

    /**
     * Sets the application sample data as created or not created.
     */
    public void setInitialApplicationDataCreated(boolean initialApplicationDataCreated);

    /**
     * Sets the mail update interval in milliseconds. 0 means no automatic
     * update.
     */
    public void setMailUpdateInterval(long updateInterval);

    /**
     * @return the mail update interval in milliseconds.
     */
    public long getMailUpdateInterval();

    /**
     * Sets the last mail check to the current time
     */
    public void setMailLastUpdateMillis();

    /**
     * Returns the time in milliseconds of last mail check
     */
    public long getMailLastUpdateMillis();

    /**
     * Sets the tutorial to be shown on application startup.
     */
    public void setShowTutorialOnStartup(boolean showTutorial);

    /**
     * @return true if the tutorial should be shown on startup.
     */
    public boolean getShowTutorialOnStartup();
}
