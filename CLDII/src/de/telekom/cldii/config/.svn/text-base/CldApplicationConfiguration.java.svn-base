package de.telekom.cldii.config;

import de.telekom.cldii.service.UpdateInterval;

/**
 * Provides productive configuration for the cld application.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public class CldApplicationConfiguration implements IApplicationConfiguration {

	@Override
	public int getNumberOfImageDownloadThreads() {
		return 3;
	}

	@Override
	public int getNumberOfNewsDownloadThreads() {
		return 5;
	}

	@Override
	public long getTimeForUpdateInterval(UpdateInterval updateInterval) {
		long millisecondsInMinute = 60000;
		
		switch (updateInterval) {
		case FIVE_MINUTES:
			return millisecondsInMinute * 5;

		case TEN_MINUTES:
			return millisecondsInMinute * 10;
			
		case FIFTEEN_MINUTES:
			return millisecondsInMinute * 15;
			
		case THIRTY_MINUTES:
			return millisecondsInMinute * 30;
			
		case FOURTYFIVE_MINUTES:
			return millisecondsInMinute * 45;
			
		case ONE_HOUR:
			return millisecondsInMinute * 60;
			
		case THREE_HOURS:
			return millisecondsInMinute * 180;
		}
		return Long.MAX_VALUE;
	}
	
	@Override
	public String getNewsDatabaseName() {
		return "CLDIINewsDB.sqlite";
	}
	
	@Override
	public int getNewsDatabaseVersion() {
		return 4;
	}
}
