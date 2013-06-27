package de.telekom.cldii.config;

import de.telekom.cldii.service.UpdateInterval;

/**
 * Access to configuration values.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public interface IApplicationConfiguration {

	/**
	 * @return number of threads used to download images asynchronuosly
	 */
	public int getNumberOfImageDownloadThreads();
	
	/**
	 * @return number of threads used to download news asynchronuosly
	 */
	public int getNumberOfNewsDownloadThreads();
	
	/**
	 * @return time in milliseconds for the given update interval
	 */
	public long getTimeForUpdateInterval(UpdateInterval updateInterval);
	
	/**
	 * @return the name of the news database
	 */
	public String getNewsDatabaseName();
	
	/**
	 * @return the version of the news database
	 */
	public int getNewsDatabaseVersion();
}
