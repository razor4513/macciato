package de.telekom.cldii.view.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import de.telekom.cldii.R;

/**
 * Notifies the user that an internet connection is needed, if no connection is found.
 * 
 * @author Sebastian Stallenberger, jambit GmbH
 */
public class NetworkCheck {
	
    public static void notifyUserIfConnectionIsNotAvailable(Context activityContext) {
        if (!isOnline(activityContext)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activityContext);
            builder.setMessage(
                    R.string.dialog_you_need_a_internet_connection)
                    .setCancelable(true)
                    .setPositiveButton(activityContext.getString(R.string.confirm_ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            builder.create().show();
        }
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }
}
