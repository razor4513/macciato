/**
 * Defines the data provider of the Email module
 */
package de.telekom.cldii.data.mail;

import java.util.Comparator;
import java.util.List;

import de.telekom.cldii.data.IDataProvider;

/**
 * Defines the data provider of the Email module
 * 
 * @author Christoph Huebner
 * 
 */
public interface IMailDataProvider extends IDataProvider {

    /**
     * @return all mails of all accounts
     */
    public List<ICompactMail> getIncommingMails();

    /**
     * @return all mails of all accounts sorted by Comparator
     */
    public List<ICompactMail> getIncommingMails(Comparator<ICompactMail> comparator);

    /**
     * @return the number of unread mails
     */
    public int getUnreadMails();

    /**
     * @param mailId
     *            the id of the mail
     * @return mail for the given id or null if mail could not be found
     */
    public ICompactMail getCompactMail(String mailId);

    /**
     * @param mailId
     *            the id of the mail
     * @param listener
     *            listener to be notified if the loading is finished
     */
    public void getFullMail(String mailId, IMailLoadingListener listener);

    /**
     * @return the name of the action sent by broadcast if new mails arrive
     */
    public String getNewMailIntentAction();

    /**
     * @return the name of the action sent as broadcast if mails have been
     *         remotly deleted
     */
    public String getRemovedMailIntentAction();

    /**
     * @return the names of all accounts.
     */
    public List<String> getAccountNames();

    /**
     * Callback listener for the asynchronuos loading of mails.
     * 
     * @author Anton Wolf, jambit GmbH
     */
    public interface IMailLoadingListener {

        /**
         * Callback method called when the loading is finished.
         * 
         * @param loadedMail
         *            the loaded mail or null if loading failed
         */
        public void onMailLoaded(IFullMail loadedMail);
    }
}
