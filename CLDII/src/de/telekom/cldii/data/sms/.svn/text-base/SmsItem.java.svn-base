package de.telekom.cldii.data.sms;

import java.util.Date;

/**
 * Represents a single sms item
 * 
 * @author Sebastian Stallenberger, jambit GmbH
 */
public class SmsItem {
    // the identifier of this sms item (api: "_id")
    private int smsId;

    // the contact id of this sms item (api: "person")
    private int contactId;

    // the sender phone number of this sms item (api: "address")
    private String senderPhoneNumber;

    // the date of this sms item (api: "date")
    private Date date;

    // the content of this sms item (api: "body")
    private String content;

    // the read state of this sms item (api: "read")
    private boolean isRead;
    
    private boolean hasDialableNumber;

    public SmsItem(int smsId, int contactId, String senderPhoneNumber, Date date, String content, boolean isRead, boolean hasDialableNumber) {
        this.smsId = smsId;
        this.contactId = contactId;
        this.senderPhoneNumber = senderPhoneNumber;
        this.date = date;
        this.content = content;
        this.isRead = isRead;
        this.hasDialableNumber = hasDialableNumber;
    }

    public int getSmsId() {
        return smsId;
    }

    public void setSmsId(int smsId) {
        this.smsId = smsId;
    }

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public String getSenderPhoneNumber() {
        return senderPhoneNumber;
    }

    public void setSenderPhoneNumber(String senderPhoneNumber) {
        this.senderPhoneNumber = senderPhoneNumber;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    public boolean isHasDialableNumber() {
        return hasDialableNumber;
    }

    public void setHasDialableNumber(boolean hasDialableNumber) {
        this.hasDialableNumber = hasDialableNumber;
    }
}
