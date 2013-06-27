package de.telekom.cldii.config;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Implementation for the {@link IApplicationPreferences} that stores the preferences in the shared preferences.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public class CldApplicationPreferences implements IApplicationPreferences {

	//file name
	private static final String PREFERENCE_FILE_NAME = "CldApplicationPreferences";
	
	//keys
	private static final String INITIAL_APP_DATA_CREATED = "initialApplicationDataCreated";
	private static final String MAIL_UPDATE_INTERVAL = "mailUpdateInterval";
	private static final String MAIL_LAST_UPDATE = "mailLastUpdate";
	private static final String SHOW_TUTORIAL_ON_STARTUP = "showTutorialOnStartup";
	
	private Context context;
	
	@Override
	public void onCreate(Context context) {
		this.context = context;
	}
	
	@Override
	public boolean isInitialApplicationDataCreated() {
		SharedPreferences settings = context.getSharedPreferences(PREFERENCE_FILE_NAME, 0);
	    boolean initialApplicationDataCreated = settings.getBoolean(INITIAL_APP_DATA_CREATED, false);

		return initialApplicationDataCreated;
	}

	@Override
	public void setInitialApplicationDataCreated(boolean initialApplicationDataCreated) {
		SharedPreferences settings = context.getSharedPreferences(PREFERENCE_FILE_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putBoolean(INITIAL_APP_DATA_CREATED, initialApplicationDataCreated);
	    editor.commit();
	}
	
	@Override
	public long getMailUpdateInterval() {
		SharedPreferences settings = context.getSharedPreferences(PREFERENCE_FILE_NAME, 0);
	    return settings.getLong(MAIL_UPDATE_INTERVAL, 15 * 60000);
	}
	
	@Override
	public void setMailUpdateInterval(long updateInterval) {
		SharedPreferences settings = context.getSharedPreferences(PREFERENCE_FILE_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putLong(MAIL_UPDATE_INTERVAL, updateInterval);
	    editor.commit();
	}

	@Override
	public long getMailLastUpdateMillis() {
	    SharedPreferences settings = context.getSharedPreferences(PREFERENCE_FILE_NAME, 0);
	    return settings.getLong(MAIL_LAST_UPDATE, 0);
	}
	
	@Override
	public void setMailLastUpdateMillis() {
        SharedPreferences settings = context.getSharedPreferences(PREFERENCE_FILE_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(MAIL_LAST_UPDATE, System.currentTimeMillis());
        editor.commit();
	}
	
	@Override
	public void setShowTutorialOnStartup(boolean showTutorial) {
		SharedPreferences settings = context.getSharedPreferences(PREFERENCE_FILE_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
	    editor.putBoolean(SHOW_TUTORIAL_ON_STARTUP, showTutorial);
	    editor.commit();
	}
	
	@Override
	public boolean getShowTutorialOnStartup() {
		SharedPreferences settings = context.getSharedPreferences(PREFERENCE_FILE_NAME, 0);
	    boolean initialApplicationDataCreated = settings.getBoolean(SHOW_TUTORIAL_ON_STARTUP, true);

		return initialApplicationDataCreated;
	}
}
