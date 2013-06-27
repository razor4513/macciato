/**
 * Implementation of the SmsDataService.
 */
package de.telekom.cldii.data.sms.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import de.telekom.cldii.config.IConfigurationManager;
import de.telekom.cldii.data.sms.ISmsDataProvider;
import de.telekom.cldii.data.sms.SmsItem;

/**
 * Implementation of the SmsDataService.
 * 
 * @author Sebastian Stallenberger, jambit GmbH
 * 
 */
public class SmsDataProvider implements ISmsDataProvider {
    private Context context;

    // set constants external
    private static final String SMS_CONTENTURI = "content://sms/inbox";
    private static final String SMS_CONTENTDELETEURI = "content://sms";
    private static final String SMS_COLUMN_ID = "_id";
    private static final String SMS_COLUMN_ADDRESS = "address";
    private static final String SMS_COLUMN_BODY = "body";
    private static final String SMS_COLUMN_PERSON = "person";
    private static final String SMS_COLUMN_DATE = "date";
    private static final String SMS_COLUMN_READ = "read";

    private static final String TAG = "SmsDataProvider";

    private List<SmsItem> smsItemList = new ArrayList<SmsItem>();

    @Override
    public void onCreate(Context context, IConfigurationManager configurationManager) {
        this.context = context;
    }

    @Override
    public int getUnreadSmsCount() {
        Uri uri = Uri.parse(SMS_CONTENTURI);
        Cursor cursor = context.getContentResolver().query(uri, new String[] { "COUNT(*)" }, SMS_COLUMN_READ + "=0",
                null, null);
        int count = 0;
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    @Override
    public List<SmsItem> getSmsItemsOrderedByDate() {
        if (smsItemList.isEmpty()) {
            String[] projection = new String[] { SMS_COLUMN_ID, SMS_COLUMN_ADDRESS, SMS_COLUMN_BODY, SMS_COLUMN_PERSON,
                    SMS_COLUMN_DATE, SMS_COLUMN_READ, "status", "type", "reply_path_present" };

            Uri uri = Uri.parse(SMS_CONTENTURI);

            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, SMS_COLUMN_DATE + " DESC");

            while (cursor.moveToNext()) {
                int smsId = cursor.getInt(cursor.getColumnIndex(SMS_COLUMN_ID));
                String senderPhoneNumber = cursor.getString(cursor.getColumnIndex(SMS_COLUMN_ADDRESS));
                String content = cursor.getString(cursor.getColumnIndex(SMS_COLUMN_BODY));
                int contactId = cursor.getInt(cursor.getColumnIndex(SMS_COLUMN_PERSON));
                long dateMilliseconds = cursor.getLong(cursor.getColumnIndex(SMS_COLUMN_DATE));
                int isRead = cursor.getInt(cursor.getColumnIndex(SMS_COLUMN_READ));

                Date date = new Date(dateMilliseconds);
                String filteredSenderPhoneNumber = senderPhoneNumber.replaceAll("[^\\+0-9]", "");
                boolean isDialable = PhoneNumberUtils.isGlobalPhoneNumber(filteredSenderPhoneNumber);
                SmsItem smsItem = new SmsItem(smsId, contactId, (isDialable ? filteredSenderPhoneNumber
                        : senderPhoneNumber), date, content, isRead == 1 ? true : false, isDialable);
                smsItemList.add(smsItem);
            }
            cursor.close();
        }

        return smsItemList;
    }

    public boolean setSmsUnread(long id) {
        Uri uri = Uri.parse(SMS_CONTENTURI);
        ContentValues cv = new ContentValues(1);
        cv.put(SMS_COLUMN_READ, 0);

        if (context.getContentResolver().update(uri, cv, SMS_COLUMN_ID + "=?", new String[] { String.valueOf(id) }) == 1) {
            Log.d(TAG, "sms " + id + " set unread.");

            return true;
        }

        return false;
    }

    public boolean setSmsRead(long id) {
        Uri uri = Uri.parse(SMS_CONTENTURI);
        ContentValues cv = new ContentValues(1);
        cv.put(SMS_COLUMN_READ, 1);

        if (context.getContentResolver().update(uri, cv, SMS_COLUMN_ID + "=?", new String[] { String.valueOf(id) }) == 1) {
            Log.d(TAG, "sms " + id + " set read.");

            return true;
        }

        return false;
    }

    @Override
    public boolean deleteSms(long id) {
        Uri uri = Uri.parse(SMS_CONTENTDELETEURI);

        if (context.getContentResolver().delete(uri, SMS_COLUMN_ID + "=?", new String[] { String.valueOf(id) }) == 1) {
            Log.d(TAG, "sms " + id + " deleted.");

            return true;
        }

        return false;
    }

    @Override
    public void onLowMemoryWarning() {
        clearCache();
    }

    @Override
    public void onResume() {
        clearCache();
        getSmsItemsOrderedByDate();
    }

    @Override
    public void clearCache() {
        smsItemList.clear();
    }
}
