package de.telekom.cldii.data.contact;

import java.util.List;

import android.graphics.Bitmap;
import de.telekom.cldii.data.IDataProvider;
import de.telekom.cldii.data.contact.Contact.Email;
import de.telekom.cldii.data.contact.Contact.Phone;

/**
 * Data provider for the phone module.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public interface IContactDataProvider extends IDataProvider {

    /**
     * Returns all phone contacts of the Android phone as a list
     * 
     * @return all Android contacts
     */
    public List<Contact> getPhoneContacts();

    /**
     * Returns all favorite contacts of the Android phone as a list
     * 
     * @return all Android contacts
     */
    public List<Contact> getFavoriteContacts();

    /**
     * Retrieve the contact image thumbnail
     * 
     * @param contact
     *            the contact to get the photo for
     * @return the image of the contact
     */
    public Bitmap getContactPhoto(Contact contact);

    /**
     * Retrieve the phone numbers for a contact
     * 
     * @param contact
     *            a contact
     * @return the phone numbers of the contact
     */
    public List<Phone> getPhoneForContact(Contact contact);

    /**
     * Retrieve the default number of a contact
     * 
     * @param contact
     *            a contact
     * @return the default number of the contact
     */
    public String getDefaultPhoneForContact(Contact contact);

    /**
     * Retrieve the email addresses for a contact
     * 
     * @param contact
     *            a contact
     * @return the email addresses of the contact
     */
    public List<Email> getEmailForContact(Contact contact);

    /**
     * Returns the contact object for the given id
     * 
     * @param contactId
     *            identifier
     * @return Contact object
     */
    public Contact getContactForId(long contactId);

    /**
     * returns the first contact object for the given phone number.
     * 
     * @param phoneNumber
     *            The phone number string
     * @return Contact
     */
    public Contact getContactForPhoneNumber(String phoneNumber);

    /**
     * returns the first contact object for the given email address.
     * 
     * @param emailAddress
     *            The email address string
     * @return Contact
     */
    public Contact getContactForEmail(String emailAddress);

    /**
     * Change the favorite state of a contact
     * 
     * @param contact
     * @param isFavorite
     * @return success
     */
    public boolean changeFavoriteState(Contact contact, boolean isFavorite);

    /**
     * Returns a list of phone numbers with the according contact names
     * 
     * @return all phone numbers with the according contact name
     */
    public List<PhoneNamePair> getPhoneNumbers();

    /**
     * Returns a list of email addresses with the according contact names
     * 
     * @return all email addresses with the according contact name
     */
    public List<EmailNamePair> getEmailAddresses();

    /**
     * Returns the list of beginning characters of all contact names
     * 
     * @return list of beginning characters of all contact names
     */
    public List<String> getIndexChars();

    /**
     * Returns all contacts with a display name beginning with the given
     * indexChar
     * 
     * @param indexChar
     *            a index char
     * @return the list of contacts with a name beginning with the given char
     */
    public List<Contact> getContactsForIndexChar(String indexChar);

    /**
     * Sets a phone number as primary number for a Contact
     * 
     * @param contact
     *            the Contact
     * @param phone
     *            the phone number
     * @return <code>true</code> if the number was successfully set as primary
     *         number
     */
    public boolean setDefaultPhoneForContact(Contact contact, Phone phone);

    /**
     * Check, if the ContactDataProvider is synchronizing Sms at the moment.
     * 
     * @return
     */
    public boolean isSmsSyncRunning();

    /**
     * Check, if the ContactDataProvider is synchronizing BasicInfo at the
     * moment.
     * 
     * @return
     */
    public boolean isBasicInfoSyncRunning();

    /**
     * Check, if the ContactDataProvider is synchronizing Email at the moment.
     * 
     * @return
     */
    public boolean isEmailSyncRunning();
}
