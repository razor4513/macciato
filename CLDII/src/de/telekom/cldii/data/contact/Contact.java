/**
 * 
 */
package de.telekom.cldii.data.contact;

import android.provider.ContactsContract;

/**
 * Encapsulates the contact data from the address book
 * 
 * @author Christoph Huebner
 * 
 */
public class Contact {

    public enum PhoneType {
        HOME, WORK, MOBILE, HOME_FAX, WORK_FAX, WORK_MOBILE, OTHER
    }
    
    /**
     * Converts the Android phone type into our shortened enum format.
     * 
     * @param type
     * @return
     */
    public static PhoneType convertType(int type) {

        switch (type) {
        case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
            return PhoneType.HOME;
        case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
            return PhoneType.MOBILE;
        case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
        case ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN:
            return PhoneType.WORK;
        case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
            return PhoneType.WORK_MOBILE;
        case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
        case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX:
            return PhoneType.HOME_FAX;
        case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
            return PhoneType.WORK_FAX;
        case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
        case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER:
        case ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK:
        case ContactsContract.CommonDataKinds.Phone.TYPE_CAR:
        case ContactsContract.CommonDataKinds.Phone.TYPE_ISDN:
        case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
        case ContactsContract.CommonDataKinds.Phone.TYPE_RADIO:
        case ContactsContract.CommonDataKinds.Phone.TYPE_TELEX:
        case ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD:
        case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER:
        case ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT:
        case ContactsContract.CommonDataKinds.Phone.TYPE_MMS:
        default:
            return PhoneType.OTHER;
        }
    }

    /**
     * Stores the contact id
     */
    private String id;

    /**
     * Stores the lookup key
     */
    private String lookupKey;

    /**
     * Stores the display name = combination of forename and lastname
     */
    private String displayName;

    /**
     * Defines, if there are phone numbers for this contact in the database
     */
    private boolean hasPhone;

    public boolean hasPhone() {
        return hasPhone;
    }

    public void setHasPhone(boolean hasPhone) {
        this.hasPhone = hasPhone;
    }

    /**
     * Defines, if this contact's email and phone fields were not synchronized
     * with the db
     */
    private boolean needsSyncForEmailAndPhone;

    public boolean needsSyncForEmailAndPhone() {
        return needsSyncForEmailAndPhone;
    }

    public void setNeedsSyncForEmailAndPhone(boolean needsSyncForEmailAndPhone) {
        this.needsSyncForEmailAndPhone = needsSyncForEmailAndPhone;
    }

    /**
     * Defines, if this contact is a favorite contact
     */
    private boolean isFavorite;

    @SuppressWarnings("unused")
    private Contact() {
    }

    public Contact(String contactId, String lookupKey) {
        this.id = contactId;
        this.lookupKey = lookupKey;
    }

    /**
     * Returns the contact id
     * 
     * @return the contact id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the lookup key
     * 
     * @return the lookup key
     */
    public String getLookupKey() {
        return lookupKey;
    }

    /**
     * Returns the display name (e.g combination of forename and lastname)
     * 
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name of a contact object
     * 
     * @param dName
     */
    public void setDisplayName(String dName) {
        this.displayName = dName;
    }

    /**
     * Defines, if this contact is a Favorite
     * 
     * @param isFavorite
     *            if <code>true</code> this is a favorite
     */
    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    /**
     * Returns <code>true</code>, if this is a Favorite contact
     * 
     * @return <code>true</code>, if this is a Favorite contact
     */
    public boolean isFavorite() {
        return this.isFavorite;
    }

    /**
     * Represents an Email adress
     * 
     * @author Christoph HŸbner
     */
    public class Email {

        /**
         * Stores the email address
         */
        private String address;

        /**
         * Stores the type of the email address
         */
        private String type;

        /**
         * if <code>true</code>, this email address is the default address
         */
        private boolean isDefault = false;

        /**
         * Constructor
         * 
         * @param a
         *            email address string
         * @param t
         *            type string
         */
        public Email(String a, String t) {
            this.address = a;
            this.type = t;
        }

        /**
         * Returns the email address string
         * 
         * @return email address as string
         */
        public String getAddress() {
            return address;
        }

        /**
         * Sets the email address as string
         * 
         * @param address
         *            the email address string
         */
        public void setAddress(String address) {
            this.address = address;
        }

        /**
         * Returns the type of the email address
         * 
         * @return
         */
        public String getType() {
            return type;
        }

        /**
         * Sets the type of the email address
         * 
         * @param t
         *            type of the email address
         */
        public void setType(String t) {
            this.type = t;
        }

        /**
         * Determines, if this email address is a default address
         * 
         * @return <code>true</code>, if this email address is the default email
         *         address
         */
        public boolean isDefault() {
            return this.isDefault;
        }

        /**
         * Sets this entry as the default address
         * 
         * @param isDefault
         */
        public void setDefault(boolean isDefault) {
            this.isDefault = isDefault;
        }

    }

    /**
     * Represents a phone number entry
     * 
     * @author Christoph HŸbner
     * 
     */
    public class Phone implements Comparable<Phone> {

        /**
         * Stores the preformatted number as string
         */
        private String number;

        /**
         * Stores the raw number as a string
         */
        private String rawNumber;

        /**
         * Stores the type of the number
         */
        private PhoneType type;

        /**
         * Stores the raw type (= db type)
         */
        private int rawType;

        /**
         * if <code>true</code>, this number is the default number
         */
        private boolean isDefault = false;

        /**
         * Constructor
         * 
         * @param number
         *            the phone number
         * @param type
         *            the type of the phone number
         * @paran isDefault flag indicating, if this is a default number
         */
        public Phone(String number, String rawNumber, int type, boolean isDefault) {
            this.number = number;
            this.rawNumber = rawNumber;
            this.isDefault = isDefault;
            this.rawType = type;
            this.setType(type);
        }

        /**
         * Returns the number
         * 
         * @return the phone number
         */
        public String getNumber() {
            return number;
        }

        /**
         * Returns the raw number (= number from the db)
         * 
         * @return the phone number
         */
        public String getRawNumber() {
            return rawNumber;
        }

        /**
         * Returns the type of the phone number
         * 
         * @return type of the phone number
         */
        public PhoneType getType() {
            return type;
        }

        /**
         * Returns the raw (=db) type of the phone number
         * 
         * @return type of the phone number
         */
        public int getRawType() {
            return rawType;
        }

        /**
         * Sets the type of the phone number
         * 
         * @param type
         */
        private void setType(int type) {

            this.type = convertType(type);
        }

        

        /**
         * Determines, if this email address is a default phone number
         * 
         * @return <code>true</code>, if this email address is the default phone
         *         number
         */
        public boolean isDefault() {
            return this.isDefault;
        }

        /**
         * Sets this entry as the default phone number
         * 
         * @param isDefault
         */
        public void setDefault(boolean isDefault) {
            this.isDefault = isDefault;
        }

        /**
         * Helper method for sorting types
         * 
         * @return priority (lower = higher prio)
         */
        private int getOrderPrio() {

            if (isDefault()) {
                return 0;
            }

            switch (type) {
            case HOME:
                return 3;
            case MOBILE:
                return 1;
            case WORK:
                return 4;
            case WORK_MOBILE:
                return 2;
            default:
                return Integer.MAX_VALUE;

            }
        }

        @Override
        public int compareTo(Phone another) {
            if (another == null) {
                return -1;
            }

            if (this.getOrderPrio() == another.getOrderPrio()) {
                return 0;
            }

            if (this.getOrderPrio() < another.getOrderPrio()) {
                return -1;
            }

            return 1;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Phone)) {
                return super.equals(o);
            }
            // Two Phone object are equal if they have the same raw number
            return this.rawNumber.equals(((Phone) o).rawNumber);
        }

    }

}
