package de.telekom.cldii.config;

/**
 * The preference manager provides access to the preferences.
 * Preferences are (in contrast to configuration) changable at runtime.
 * 
 * @author awolf
 */
public interface IPreferenceManager {

	/**
	 * @return the application preferences
	 */
	public IApplicationPreferences getApplicationPreferences();
}
