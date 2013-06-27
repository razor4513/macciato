package de.telekom.cldii.service.mail;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import de.telekom.cldii.config.IPreferenceManager;

/**
 * Scheduler for scheduling mail updates.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public class MailUpdateScheduler {

	private Context context;
	private IPreferenceManager preferenceManager;
	private PendingIntent pendingUpdateIntent;
	
	public MailUpdateScheduler(Context context, IPreferenceManager preferenceManager) {
		this.context = context;
		this.preferenceManager = preferenceManager;
	}
	
	/**
	 * Schedules mail update with the time interval given in preferences.
	 */
	public void scheduleMailUpdate() {
		Intent updateServiceIntent = new Intent(context, MailUpdateService.class);
		pendingUpdateIntent = PendingIntent.getService(context, 0, updateServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
		long timeForUpdateInterval = preferenceManager.getApplicationPreferences().getMailUpdateInterval();
		if (timeForUpdateInterval > 0) {
			getAlarmManager().setRepeating(AlarmManager.RTC, System.currentTimeMillis() + timeForUpdateInterval, timeForUpdateInterval, pendingUpdateIntent);
		} else {
			pendingUpdateIntent = null;
		}
	}
	
	/**
	 * Unschedules a previously scheduled update.
	 */
	public void unscheduleMailUpdate() {
		if (pendingUpdateIntent != null) {
			getAlarmManager().cancel(pendingUpdateIntent);
			pendingUpdateIntent = null;
		}
	}
	
    private AlarmManager getAlarmManager() {
    	return (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }
}
