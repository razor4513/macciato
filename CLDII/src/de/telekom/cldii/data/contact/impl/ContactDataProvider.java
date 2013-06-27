package de.telekom.cldii.data.contact.impl;

import static de.telekom.cldii.ApplicationConstants.BASICINFOSYNCED;
import static de.telekom.cldii.ApplicationConstants.CONTACTS_SYNCED_ACTION;
import static de.telekom.cldii.ApplicationConstants.EMAILSSYNCED;
import static de.telekom.cldii.ApplicationConstants.SMSSYNCED;
import static de.telekom.cldii.ApplicationConstants.STARTSYNCING;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import de.telekom.cldii.config.IConfigurationManager;
import de.telekom.cldii.data.contact.Contact;
import de.telekom.cldii.data.contact.Contact.Email;
import de.telekom.cldii.data.contact.Contact.Phone;
import de.telekom.cldii.data.contact.EmailNamePair;
import de.telekom.cldii.data.contact.IContactDataProvider;
import de.telekom.cldii.data.contact.PhoneNamePair;

/**
 * Provides access to the Android Contacts
 * 
 * @author Christoph Huebner
 */
public class ContactDataProvider implements IContactDataProvider {

    private class ContactComparator implements Comparator<Contact> {

        @Override
        public int compare(Contact lhs, Contact rhs) {
            return lhs.getDisplayName().compareTo(rhs.getDisplayName());
        }

    }

    private class ContactContentObserver extends ContentObserver {

        public ContactContentObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            Log.d("contactSync", "Contact content changed.");

            startSynchronization();
        }

    }

    private class ContactSyncThread extends Thread {
        private boolean shouldStop = false;

        public void run() {
            if (syncLock.tryLock()) {
                // Got the lock
                try {
                    sendSyncBroadcast(STARTSYNCING);
                    isBasicInfoSyncRunning = true;
                    isSmsSyncRunning = true;
                    isEmailSyncRunning = true;

                    // Remember old contacts
                    Set<Contact> unsyncedContacts = new HashSet<Contact>(allContacts);

                    // contactsById.clear();
                    contactsByLookupKey.clear();

                    // Fetch contacts from the phone db
                    retrieveContactsFromDB(unsyncedContacts);

                    // Remove remaining unsynchronized contacts
                    for (Contact contact : unsyncedContacts) {
                        removeContact(contact);
                    }
                    unsyncedContacts = null;

                    // sort all contacts list
                    Collections.sort(allContacts, new ContactComparator());

                    // fill phoneContacts and favoriteContacts lists
                    favoriteContacts.clear();
                    phoneContacts.clear();
                    for (Contact contact : allContacts) {
                        if (contact.hasPhone()) {
                            phoneContacts.add(contact);
                            if (contact.isFavorite()) {
                                favoriteContacts.add(contact);
                            }
                        }
                    }

                    // delete photo cache (photos will be updated lazily)
                    contactPhotoCache.clear();

                    // fill char lookup table
                    contactsByIndexChar = new HashMap<String, List<Contact>>();
                    for (Contact c : phoneContacts) {
                        String name = c.getDisplayName().trim();
                        if (name == null || name.length() < 1)
                            continue;
                        String indexChar = name.toUpperCase().substring(0, 1);
                        if (!contactsByIndexChar.containsKey(indexChar)) {
                            contactsByIndexChar.put(indexChar, new ArrayList<Contact>());
                        }
                        contactsByIndexChar.get(indexChar).add(c);
                    }

                    // BROADCAST: BASIC-INFOS-SYNCHED, needed for Favorites
                    // view, Addressbook view, Email view, SMS view (latter ones
                    // only for photo update)
                    sendSyncBroadcast(BASICINFOSYNCED);
                    isBasicInfoSyncRunning = false;

                    // load email and phone infos
                    getAllPhoneNumbers();
                    // BROADCAST: PHONE-INFOS-SYNCHED, needed for SMS view
                    sendSyncBroadcast(SMSSYNCED);
                    isSmsSyncRunning = false;

                    // fill lookup table for searching contacts by email address
                    // and create list of email-name-pairs
                    getAllEmailAddresses();
                    // BROADCAST: EMAIL-INFOS-SYNCHED, needed for Email view
                    sendSyncBroadcast(EMAILSSYNCED);
                    isEmailSyncRunning = false;

                } finally {
                    // Make sure to unlock
                    syncLock.unlock();
                    if (shouldStop) {
                        Log.w(TAG, "syncThread stopped (not complete)");
                        isBasicInfoSyncRunning = false;
                        isSmsSyncRunning = false;
                        isEmailSyncRunning = false;
                        onThreadStop();
                    }
                }
            }
        }

        public void shouldStop() {
            shouldStop = true;
        }
    }

    private static final String TAG = "ContactDataProvider";
    private Context context;

    // list of all contacts
    private List<Contact> allContacts;
    // convenience caches to avoid all to frequent filtering of the contacts
    // cache
    private List<Contact> phoneContacts;
    private List<Contact> favoriteContacts;

    // caches holding more specific information about the contacts
    private Map<Contact, Bitmap> contactPhotoCache;
    private Map<Contact, List<Email>> contactEmailCache;
    private Map<Contact, List<Phone>> contactPhoneCache;

    // lookup tables for finding contact instances
    private Map<String, Contact> contactsByPhoneNumber;
    private Map<String, Contact> contactsByEmail;
    private Map<String, List<Contact>> contactsByIndexChar;
    private Map<String, Contact> contactsById;
    private Map<String, Contact> contactsByLookupKey;

    // phone and email lists for receiver fields in SMS and Email compose view
    private List<PhoneNamePair> phoneNumbers;
    private List<EmailNamePair> emailAddresses;

    // // SECTION SYNC

    // contact observer
    private ContactContentObserver contentObserver = new ContactContentObserver();

    private boolean contactReceiverRegistered;
    private Lock syncLock = new ReentrantLock();

    private boolean isBasicInfoSyncRunning = false;
    private boolean isSmsSyncRunning = false;
    private boolean isEmailSyncRunning = false;

    private ContactSyncThread syncThread;

    /**
     * Adds the given contact data to the caches. Avoids duplicates
     * 
     * @param id
     * @param lookupKey
     * @param displayName
     * @param hasPhone
     * @param isFavorite
     * @return a Contact object containing the given data
     */
    private Contact addContactDataToList(String id, String lookupKey, String displayName, boolean hasPhone,
            boolean isFavorite) {

        // Try to get existing contact
        Contact contact = this.getContactById(id);

        // Contact does not exist --> create new one
        if (contact == null) {
            contact = new Contact(id, lookupKey);

            // put new contact into index maps
            if (!contactsById.containsKey(id)) {
                contactsById.put(id, contact);
            }
            if (!contactsByLookupKey.containsKey(lookupKey)) {
                contactsByLookupKey.put(lookupKey, contact);
            }
        }

        // add contact to according lists
        if (!this.allContacts.contains(contact)) {
            allContacts.add(contact);
        }

        // set new data
        contact.setDisplayName(displayName);
        contact.setFavorite(isFavorite);
        contact.setHasPhone(hasPhone);

        // mark for update
        contact.setNeedsSyncForEmailAndPhone(true);

        return contact;
    }

    @Override
    public boolean changeFavoriteState(Contact contact, boolean isFavorite) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, contact.getId());

        ContentValues starredValue = new ContentValues(1);
        starredValue.put(ContactsContract.Contacts.STARRED, (isFavorite ? 1 : 0));

        if (contentResolver.update(uri, starredValue, null, null) == 1) {
            // change cache state on success
            contact.setFavorite(isFavorite);

            if (isFavorite) {
                if (!favoriteContacts.contains(contact)) {
                    favoriteContacts.add(contact);
                    Collections.sort(favoriteContacts, new ContactComparator());
                }
            } else {
                favoriteContacts.remove(contact);
            }
            return true;
        }

        return false;
    }

    /**
     * Checks, if a contact needs an update of its email and phone data and
     * triggers the update
     * 
     * @param contact
     */
    private void checkAndUpdatePhoneAndEmail(Contact contact) {
        if (contact != null && contact.needsSyncForEmailAndPhone()) {

            this.contactEmailCache.put(contact, this.getEmailAddresses(contact));
            this.contactPhoneCache.put(contact, this.getPhoneNumbers(contact));

            contact.setNeedsSyncForEmailAndPhone(false);
        }
    }

    /**
     * Lookup contact by a contact id.
     * 
     * @param contactId
     * @return returns matching contact or <code>null</code> if no match exists
     */
    private Contact getContactById(String contactId) {
        if (contactsById.containsKey(contactId)) {
            return (contactsById.get(contactId));

        }
        return null;
    }

    /**
     * Lookup contact by a lookup key.
     * 
     * @param lookupKey
     * @return returns matching contact or <code>null</code> if no match exists
     */
    private Contact getContactByLookupKey(String lookupKey) {
        if (contactsByLookupKey.containsKey(lookupKey)) {
            return (contactsByLookupKey.get(lookupKey));

        }
        return null;
    }

    @Override
    public Contact getContactForEmail(String emailAddress) {

        String key = emailAddress.toLowerCase();

        if (this.contactsByEmail.containsKey(key)) {
            return this.contactsByEmail.get(key);
        }

        return null;
    }

    // // SECTION ACCESS

    @Override
    public Contact getContactForId(long contactId) {
        return this.getContactById(String.valueOf(contactId));
    }

    @Override
    public Contact getContactForPhoneNumber(String phoneNumber) {

        if (this.contactsByPhoneNumber.containsKey(phoneNumber)) {
            return this.contactsByPhoneNumber.get(phoneNumber);
        }

        Contact contact = null;
        Cursor cursor = null;

        // 1. Try via lookup key
        String[] projection = new String[] { PhoneLookup.LOOKUP_KEY };
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        try {
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String lookupKey = cursor.getString(cursor.getColumnIndex(PhoneLookup.LOOKUP_KEY));
                    contact = getContactByLookupKey(lookupKey);
                    if (contact != null) {
                        break;
                    }
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
            cursor = null;
        }

        if (contact != null) {
            this.contactsByPhoneNumber.put(phoneNumber, contact);
            return contact;
        }

        // 2. Try via contact id
        String lPhoneNumber = PhoneNumberUtils.formatNumber(phoneNumber);

        projection = new String[] { CommonDataKinds.Phone.CONTACT_ID };
        uri = Uri.withAppendedPath(CommonDataKinds.Phone.CONTENT_FILTER_URI, lPhoneNumber.toString());
        try {
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String id = cursor.getString(cursor
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    contact = this.getContactById(id);
                    if (contact != null) {
                        break;
                    }
                }
            }
        } finally {
            if (cursor != null)
                cursor.close();
            cursor = null;
        }

        if (contact != null) {
            this.contactsByPhoneNumber.put(phoneNumber, contact);
        }

        return contact;
    }

    @Override
    public Bitmap getContactPhoto(Contact contact) {
        Bitmap contactImage = this.contactPhotoCache.get(contact);

        if (contactImage == null && !this.contactPhotoCache.containsKey(contact)) {
            ContentResolver contentResolver = context.getContentResolver();

            if (contact == null || contentResolver == null) {
                return null;
            }

            // working code with CONTACT id
            Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, Long.parseLong(contact.getId()));
            Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);

            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(photoUri, new String[] { Photo.PHOTO }, null, null, null);

                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        byte[] contactImageBlob = cursor.getBlob(0);
                        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                        bitmapOptions.inSampleSize = 1;

                        contactImage = (contactImageBlob == null) ? null : BitmapFactory.decodeByteArray(
                                contactImageBlob, 0, contactImageBlob.length);
                        if (contactImage != null) {
                            break;
                        }
                    }
                }
            } finally {
                if (cursor != null)
                    cursor.close();
                cursor = null;
            }

            // cache it by lookup key
            contactPhotoCache.put(contact, contactImage);
        }
        return contactImage;
    }

    @Override
    public List<Contact> getContactsForIndexChar(String indexChar) {
        indexChar = indexChar.trim().toUpperCase();
        if (this.contactsByIndexChar.containsKey(indexChar)) {
            return this.contactsByIndexChar.get(indexChar);
        }
        return new ArrayList<Contact>();
    }

    @Override
    public String getDefaultPhoneForContact(Contact contact) {
        checkAndUpdatePhoneAndEmail(contact);
        if (this.contactPhoneCache.containsKey(contact)) {
            List<Phone> phoneList = this.contactPhoneCache.get(contact);
            if (phoneList != null && phoneList.size() > 0) {
                return phoneList.get(0).getNumber();
            }
        }

        return null;
    }

    @Override
    public List<EmailNamePair> getEmailAddresses() {
        return emailAddresses;
    }

    /**
     * Returns all emails for a contact
     * 
     * @param contact
     *            a Contact
     * @return all emails as a list
     */
    private List<Email> getEmailAddresses(Contact contact) {
        List<Email> emails = new ArrayList<Email>();

        ContentResolver contentResolver = context.getContentResolver();

        if (contact == null || contentResolver == null) {
            return emails;
        }

        Cursor emailCursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?", new String[] { contact.getId() }, null);
        while (emailCursor.moveToNext()) {
            Email email = contact.new Email(emailCursor.getString(
                    emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)).toLowerCase(),
                    emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE)));
            emails.add(email);
        }
        emailCursor.close();

        return emails;
    }

    private void getAllEmailAddresses() {
        this.contactsByEmail.clear();
        this.emailAddresses.clear();

        ContentResolver contentResolver = context.getContentResolver();

        String[] projection = new String[] { ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                ContactsContract.CommonDataKinds.Email.DATA };
        Cursor emailCursor = contentResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, projection,
                null, null, null);
        while (emailCursor.moveToNext()) {

            String emailAddress = emailCursor.getString(
                    emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)).toLowerCase();
            String contactId = emailCursor.getString(emailCursor
                    .getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID));

            Contact contact = this.getContactById(contactId);
            if (contact != null) {
                EmailNamePair emailName = new EmailNamePair(emailAddress, contact.getDisplayName());

                if (!emailAddresses.contains(emailName)) {
                    emailAddresses.add(emailName);
                }

                if (!contactsByEmail.containsKey(emailAddress)) {
                    contactsByEmail.put(emailAddress, contact);
                }
            }
        }
        emailCursor.close();

        Collections.sort(this.emailAddresses);
    }

    private void getAllPhoneNumbers() {
        this.contactsByPhoneNumber.clear();
        this.phoneNumbers.clear();

        ContentResolver contentResolver = context.getContentResolver();

        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DATA, ContactsContract.CommonDataKinds.Phone.TYPE };
        Cursor phoneCursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection,
                null, null, null);
        while (phoneCursor.moveToNext()) {

            String phoneNumber = phoneCursor.getString(
                    phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DATA)).toLowerCase();
            String contactId = phoneCursor.getString(phoneCursor
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
            int type = phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

            Contact contact = this.getContactById(contactId);
            if (contact != null) {
                PhoneNamePair phoneName = new PhoneNamePair(phoneNumber, contact.getDisplayName(),
                        Contact.convertType(type));

                if (!phoneNumbers.contains(phoneName)) {
                    phoneNumbers.add(phoneName);
                }

                if (!contactsByPhoneNumber.containsKey(phoneNumber)) {
                    contactsByPhoneNumber.put(phoneNumber, contact);
                }
            }
        }
        phoneCursor.close();
        Collections.sort(this.phoneNumbers);
    }

    @Override
    public List<Email> getEmailForContact(Contact contact) {

        checkAndUpdatePhoneAndEmail(contact);

        if (!this.contactEmailCache.containsKey(contact)) {
            this.contactEmailCache.put(contact, getEmailAddresses(contact));
        }

        return this.contactEmailCache.get(contact);

    }

    @Override
    public List<Contact> getFavoriteContacts() {
        return this.favoriteContacts;
    }

    @Override
    public List<String> getIndexChars() {

        List<String> indexChars = new ArrayList<String>(this.contactsByIndexChar.keySet());
        Collections.sort(indexChars);

        return indexChars;
    }

    @Override
    public List<Contact> getPhoneContacts() {
        return this.phoneContacts;
    }

    @Override
    public List<Phone> getPhoneForContact(Contact contact) {
        checkAndUpdatePhoneAndEmail(contact);

        if (this.contactPhoneCache.containsKey(contact)) {
            return this.contactPhoneCache.get(contact);
        }

        return null;
    }

    @Override
    public List<PhoneNamePair> getPhoneNumbers() {
        return phoneNumbers;
    }

    /**
     * Returns all phone numbers for a Contact
     * 
     * @param contact
     *            a Contact
     * @return all phone numbers as a list
     */
    private List<Phone> getPhoneNumbers(Contact contact) {

        // Query DB only if necessary
        if (!contact.hasPhone())
            return null;

        List<Phone> phones = new ArrayList<Phone>();

        ContentResolver contentResolver = context.getContentResolver();

        if (contact == null || contentResolver == null) {
            return phones;
        }

        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { contact.getId() }, null);
        while (cursor.moveToNext()) {

            boolean isDefault = cursor.getInt(cursor
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY)) == 1;

            String number, rawNumber;
            number = rawNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            // preformat number
            if (PhoneNumberUtils.isGlobalPhoneNumber(number.replaceAll("[^\\+0-9]", ""))) {
                number = number.replaceAll("[^\\+0-9]", "");
            }

            Phone phoneToAdd = contact.new Phone(number, rawNumber, cursor.getInt(cursor
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)), isDefault);

            if (!phones.contains(phoneToAdd)) {
                phones.add(phoneToAdd);
            }

        }

        cursor.close();

        // Sort the phones list by using the comparator
        Collections.sort(phones);

        return phones;
    }

    public boolean isBasicInfoSyncRunning() {
        return isBasicInfoSyncRunning;
    }

    public boolean isSmsSyncRunning() {
        return isSmsSyncRunning;
    }

    public boolean isEmailSyncRunning() {
        return isEmailSyncRunning;
    }

    @Override
    public void onCreate(Context context, IConfigurationManager configurationManager) {
        this.context = context;

        this.contactPhotoCache = new HashMap<Contact, Bitmap>();
        this.contactEmailCache = new HashMap<Contact, List<Email>>();
        this.contactPhoneCache = new HashMap<Contact, List<Phone>>();

        this.contactsByPhoneNumber = new HashMap<String, Contact>();
        this.contactsByEmail = new HashMap<String, Contact>();
        this.contactsByIndexChar = new HashMap<String, List<Contact>>();
        this.contactsById = new HashMap<String, Contact>();
        this.contactsByLookupKey = new HashMap<String, Contact>();

        this.phoneNumbers = new ArrayList<PhoneNamePair>();
        this.emailAddresses = new ArrayList<EmailNamePair>();

        this.allContacts = new ArrayList<Contact>();
        this.phoneContacts = new ArrayList<Contact>();
        this.favoriteContacts = new ArrayList<Contact>();
    }

    // / private helper methods

    @Override
    public void onLowMemoryWarning() {
        this.stopSyncAndClearCaches();

        if (this.contactReceiverRegistered) {
            // unregisterReceiver(smsReceiver);
            context.getContentResolver().unregisterContentObserver(contentObserver);
            contactReceiverRegistered = false;
        }
    }

    @Override
    public void onResume() {
        startSynchronization();
        if (!this.contactReceiverRegistered) {
            context.getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI, true,
                    contentObserver);
            this.contactReceiverRegistered = true;
        }
    }

    /**
     * Removes this contact from any caches
     * 
     * @param contact
     */
    private void removeContact(Contact contact) {

        // remove from caching lists
        if (allContacts.contains(contact)) {
            allContacts.remove(contact);
        }
        if (phoneContacts.contains(contact)) {
            phoneContacts.remove(contact);
        }
        if (favoriteContacts.contains(contact)) {
            favoriteContacts.remove(contact);
        }

        // remove from lookup lists
        if (contactsById.containsKey(contact.getId())) {
            contactsById.remove(contact.getId());
        }
        if (contactsByLookupKey.containsKey(contact.getLookupKey())) {
            contactsByLookupKey.remove(contact.getLookupKey());
        }

        // remove from phone, email and photo cache
        if (contactPhotoCache.containsKey(contact)) {
            contactPhotoCache.remove(contact);
        }
        if (contactEmailCache.containsKey(contact)) {
            contactEmailCache.remove(contact);
        }
        if (contactPhoneCache.containsKey(contact)) {
            contactPhoneCache.remove(contact);
        }

        // remove from phone2contact list
        if (contactsByPhoneNumber.containsValue(contact)) {
            List<String> keys = new ArrayList<String>();

            // Retrieve keys (= phone numbers)
            Set<Entry<String, Contact>> set = contactsByPhoneNumber.entrySet();
            Iterator<Entry<String, Contact>> it = set.iterator();
            Entry<String, Contact> currentEntry;
            while (it.hasNext()) {
                currentEntry = it.next();
                if (currentEntry.getValue() == contact) {
                    keys.add(currentEntry.getKey());
                }
            }

            // Remove keys
            for (String key : keys) {
                contactsByPhoneNumber.remove(key);
            }
        }

        // remove from email2contact list
        if (contactsByEmail.containsValue(contact)) {
            List<String> keys = new ArrayList<String>();

            // Retrieve keys (= email addresses)
            Set<Entry<String, Contact>> set = contactsByEmail.entrySet();
            Iterator<Entry<String, Contact>> it = set.iterator();
            Entry<String, Contact> currentEntry;
            while (it.hasNext()) {
                currentEntry = it.next();
                if (currentEntry.getValue() == contact) {
                    keys.add(currentEntry.getKey());
                }
            }

            // Remove keys
            for (String key : keys) {
                contactsByEmail.remove(key);
            }
        }
    }

    /**
     * Loads certain contacts from the database
     * 
     * @param mode
     *            determines, which subset of contacts should be loaded
     * @param unsyncedContacts
     *            a list to track which contacts where not synchronized yet
     */
    private void retrieveContactsFromDB(Set<Contact> unsyncedContacts) {
        ContentResolver contentResolver = context.getContentResolver();

        if (contentResolver == null) {
            return;
        }

        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.HAS_PHONE_NUMBER, ContactsContract.Contacts.STARRED,
                ContactsContract.Contacts.DISPLAY_NAME };

        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";

        // use content uri and not lookup content uri, as this is faster
        Cursor cursor = contentResolver.query(uri, projection, null, null, sortOrder);

        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String lookupKey = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                boolean hasPhone = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0;
                boolean isFavorite = cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.STARRED)) > 0;

                if (displayName == null || displayName.length() == 0)
                    continue;

                Contact contact = this.addContactDataToList(id, lookupKey, displayName, hasPhone, isFavorite);

                // Remove contact from list of unsynchronized contacts
                if (unsyncedContacts != null & unsyncedContacts.contains(contact)) {
                    unsyncedContacts.remove(contact);
                }
            }
        }
        cursor.close();
    }

    private void sendSyncBroadcast(String syncBroadcasts) {
        Intent intent = new Intent();
        intent.setAction(CONTACTS_SYNCED_ACTION);
        intent.putExtra("type", syncBroadcasts);
        Log.i(TAG, "contactSyncSender " + syncBroadcasts);
        context.sendBroadcast(intent);
    }

    @Override
    public boolean setDefaultPhoneForContact(Contact contact, Phone phone) {
        List<Phone> contactPhoneNumbers = this.getPhoneForContact(contact);

        if (contactPhoneNumbers == null || !contactPhoneNumbers.contains(phone)) {
            return false;
        }

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        String where = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.CommonDataKinds.Phone.MIMETYPE
                + " = ? AND " + String.valueOf(ContactsContract.CommonDataKinds.Phone.TYPE) + " = ? AND "
                + ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?";

        String[] params = new String[] { contact.getId(), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                String.valueOf(phone.getRawType()), phone.getRawNumber() };

        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI).withSelection(where, params)
                .withValue(ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY, 1).build());

        try {
            context.getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);

            // success --> update cache
            for (Phone currentPhone : contactPhoneNumbers) {
                if (currentPhone.equals(phone)) {
                    currentPhone.setDefault(true);
                } else {
                    currentPhone.setDefault(false);
                }
            }
            Collections.sort(contactPhoneNumbers);

            return true;
        } catch (RemoteException e) {
            e.printStackTrace();
            return false;
        } catch (OperationApplicationException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Starts the synchronization with the data source of this provider
     */
    private void startSynchronization() {

        synchronized (this) {
            if (this.syncThread != null && isEmailSyncRunning) {
                Log.w("contactSync", "sync stops.");
                this.syncThread.shouldStop();
            } else {
                syncThread = new ContactSyncThread();
                Log.w("contactSync", "start new sync thread");
                syncThread.start();
            }

        }
    }

    /**
     * Clear all caches and lookup maps
     */
    private void stopSyncAndClearCaches() {

        // stop thread
        if (this.syncThread != null) {
            this.syncThread.shouldStop();
        }
        this.phoneNumbers.clear();
        this.emailAddresses.clear();
        this.allContacts.clear();
        this.favoriteContacts.clear();
        this.phoneContacts.clear();
        this.contactPhotoCache.clear();
        this.contactPhoneCache.clear();
        this.contactEmailCache.clear();
        this.contactsById.clear();
        this.contactsByLookupKey.clear();
        this.contactsByPhoneNumber.clear();
        this.contactsByIndexChar.clear();
        this.contactsByEmail.clear();
    }

    private void onThreadStop() {
        Log.w("contactSync", "sync stopped.");
        startSynchronization();
    }
}
