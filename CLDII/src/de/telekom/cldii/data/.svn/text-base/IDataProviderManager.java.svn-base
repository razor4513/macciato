package de.telekom.cldii.data;

import de.telekom.cldii.data.contact.IContactDataProvider;
import de.telekom.cldii.data.mail.IMailDataProvider;
import de.telekom.cldii.data.news.INewsDataProvider;
import de.telekom.cldii.data.sms.ISmsDataProvider;

/**
 * Interface for accessing the different {@link IDataProvider} s of the different modules
 * 
 * @author Anton Wolf, jambit GmbH
 */
public interface IDataProviderManager {

	/**
	 * @return the news data provider
	 */
	public INewsDataProvider getNewsDataProvider();
	
	/**
	 * @return the mail data provider
	 */
	public IMailDataProvider getMailDataProvider();
	
	/**
	 * @return the sms data provider
	 */
	public ISmsDataProvider getSmsDataProvider();
	
	/**
	 * @return the contact data provider
	 */
	public IContactDataProvider getContactDataProvider();
}
