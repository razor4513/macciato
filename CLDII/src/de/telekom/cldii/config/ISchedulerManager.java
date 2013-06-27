package de.telekom.cldii.config;

import de.telekom.cldii.service.mail.MailUpdateScheduler;
import de.telekom.cldii.service.newsupdate.NewsUpdateScheduler;

/**
 * Provides access to the schedulers.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public interface ISchedulerManager {

	/**
	 * @return the news update scheduler
	 */
	public NewsUpdateScheduler getNewsUpdateScheduler();
	
	/**
	 * @return the mail update scheduler
	 */
	public MailUpdateScheduler getMailUpdateScheduler();
}
