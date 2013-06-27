package de.telekom.cldii.view.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import de.telekom.cldii.R;

/**
 * This class is used to handle all uncaught exceptions thrown in Cld. It writes
 * a stracktrace textfile onto the sd card and displaying a dialog notifying the
 * user
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */
public class CldExceptionHandler implements UncaughtExceptionHandler {
    private final String TAG = "CldExceptionHandler";
    private UncaughtExceptionHandler uncaughtExceptionHandler;
    private final String localPath = "/sdcard/cldii";

    private Application application;

    public CldExceptionHandler(Application application) {
        this.application = application;
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();

        String timestamp = String.valueOf(new Date().getTime());
        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        String filename = "stacktrace_" + timestamp + ".txt";

        if (localPath != null) {
            writeToFile(stacktrace, filename);
        }
        Intent intent = new Intent(application.getBaseContext(), CldExceptionDialog.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        PendingIntent myActivity = PendingIntent.getActivity(application.getBaseContext(),
                R.id.requestCode_restart_application, intent, PendingIntent.FLAG_ONE_SHOT);

        AlarmManager alarmManager = (AlarmManager) application.getBaseContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, 5000, myActivity);
        System.exit(2);

        uncaughtExceptionHandler.uncaughtException(t, e);
    }

    private void writeToFile(String stacktrace, String filename) {
        try {
            File directory = new File(localPath);
            if (!directory.exists() || directory.mkdir()) {
                String path = localPath + "/" + filename;
                BufferedWriter bos = new BufferedWriter(new FileWriter(path));
                bos.write(stacktrace);
                bos.flush();
                bos.close();
                Log.i(TAG, "Stacktrace was written to " + path);
            } else {
                Log.w(TAG, "No SD card found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
