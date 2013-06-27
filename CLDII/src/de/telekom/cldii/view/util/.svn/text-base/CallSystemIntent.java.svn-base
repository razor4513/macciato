package de.telekom.cldii.view.util;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;

public class CallSystemIntent {
    public static void callEditContactIntent(final Activity activity, final long id, String type, final String data,
            int requestCode) {
        Intent intent = new Intent();
        if (id != -1 && android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.ECLAIR) {
            intent.setAction(Intent.ACTION_EDIT);
            intent.setData(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id));
        } else {
            intent.setAction(ContactsContract.Intents.SHOW_OR_CREATE_CONTACT);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.fromParts(type, data, null));
        }
        activity.startActivityForResult(intent, requestCode);
    }
}
