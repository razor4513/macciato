/**
 * Defines the data provider of the SMS module
 */
package de.telekom.cldii.data.sms;

import java.util.List;

import de.telekom.cldii.data.IDataProvider;

/**
 * Defines the data provider of the SMS module
 * 
 * @author Sebastian Stallenberger, jambit GmbH
 * 
 */
public interface ISmsDataProvider extends IDataProvider {

    /**
     * Returns all available sms as a list ordered by Date.
     * 
     * @return list of SmsItems ordered by date.
     */
    List<SmsItem> getSmsItemsOrderedByDate();

    /**
     * Sets the sms with id unread.
     * 
     * @param id
     *            the sms id
     * @return success
     */
    boolean setSmsUnread(long id);

    /**
     * Sets the sms with id read.
     * 
     * @param id
     *            the sms id
     * @return success
     */
    boolean setSmsRead(long id);

    /**
     * Deletes the sms with id.
     * 
     * @param id
     *            the sms id
     * @return success
     */
    boolean deleteSms(long id);

    /**
     * Returns the count of unread SMS
     */
    public int getUnreadSmsCount();
    
    /**
     * Clears the sms list cache.
     */
    public void clearCache();
}
