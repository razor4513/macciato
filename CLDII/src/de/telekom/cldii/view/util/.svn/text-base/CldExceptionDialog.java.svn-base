package de.telekom.cldii.view.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import de.telekom.cldii.CldApplication;
import de.telekom.cldii.R;

/**
 * This activity displays a dialog notifying the user of a problem
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */
public class CldExceptionDialog extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(((CldApplication) getApplication()).getThemeResId());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.exception_title);
        builder.setMessage(R.string.exception_message);
        builder.setPositiveButton("OK", new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.create().show();

    }

}
