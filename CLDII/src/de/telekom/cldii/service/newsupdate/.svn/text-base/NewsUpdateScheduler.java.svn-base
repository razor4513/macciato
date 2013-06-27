package de.telekom.cldii.service.newsupdate;

import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import de.telekom.cldii.config.IConfigurationManager;
import de.telekom.cldii.service.UpdateInterval;

/**
 * Class for scheduling calls to {@link NewsUpdateService}.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public class NewsUpdateScheduler {

	Context context;
	IConfigurationManager configurationManager;
    private List<PendingIntent> alarmIntents;
	
	public NewsUpdateScheduler(Context context, IConfigurationManager configurationManager) {
		this.context = context;
		this.configurationManager = configurationManager;
		this.alarmIntents = new ArrayList<PendingIntent>();
	}
	
    public void scheduleNewsUpdate() {
        UpdateInterval[] updateInterval = { UpdateInterval.FIVE_MINUTES,
                UpdateInterval.TEN_MINUTES, UpdateInterval.FIFTEEN_MINUTES,
                UpdateInterval.THIRTY_MINUTES,
                UpdateInterval.FOURTYFIVE_MINUTES, UpdateInterval.ONE_HOUR,
                UpdateInterval.THREE_HOURS };
        
        AlarmManager alarmManager = getAlarmManager();
        for (int i = 0; i < updateInterval.length; i++) {
        	
        	Intent updateServiceIntent = NewsUpdateService.updateCategoriesWithIntervalIntent(context, updateInterval[i]);
        	PendingIntent pendingIntent = PendingIntent.getService(context, i, updateServiceIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            
            alarmIntents.add(pendingIntent);
            long timeForUpdateInterval = configurationManager.getConfiguration().getTimeForUpdateInterval(updateInterval[i]);
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + timeForUpdateInterval, timeForUpdateInterval, pendingIntent);
        }
    }

    public void unscheduleNewsUpdate() {
        for (PendingIntent alarmIntent : alarmIntents) {
            getAlarmManager().cancel(alarmIntent);
        }
        alarmIntents.clear();
        context.stopService(new Intent(context, NewsUpdateService.class));
    }
    
    private AlarmManager getAlarmManager() {
    	return (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    }
}
