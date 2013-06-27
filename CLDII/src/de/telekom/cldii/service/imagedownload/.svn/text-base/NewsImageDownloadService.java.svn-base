package de.telekom.cldii.service.imagedownload;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import de.telekom.cldii.R;
import de.telekom.cldii.config.IConfigurationManager;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.view.news.NewsCategoriesActivity;

/**
 * Service for loading news item images and storing it into the database
 * 
 * @author Jun Chen, jambit GmbH
 * 
 */

public class NewsImageDownloadService extends Service implements
        RSSImageRetrieverOnCompleteListener<AsyncTask<?, ?, ?>> {
    private final String TAG = "NewsImageDownloadService";
    private static final int IMAGE_DOWNLOAD_NOTIFICATION_ID = 42;

    private Notification notification;
    private NotificationManager notificationManager;
    private int maxProgress;
    private int progress = 0;
    private List<AsyncTask<?, ?, ?>> imagesAsyncTasksList;

    private long startTime;
    
    @Override
    public IBinder onBind(Intent intent) {
    	// this service does not support binding
    	return null;
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "Service created...");
        startTime = System.currentTimeMillis();
        imagesAsyncTasksList = new ArrayList<AsyncTask<?, ?, ?>>();
    }

    @Override
    public void onDestroy() {
        // Cleanup
        for (AsyncTask<?, ?, ?> asyncTask : imagesAsyncTasksList) {
            asyncTask.cancel(true);
        }
        imagesAsyncTasksList.clear();

        if (notificationManager != null)
            notificationManager.cancel(IMAGE_DOWNLOAD_NOTIFICATION_ID);

        Log.v(TAG, "Service destroyed...");
        Log.i(TAG, "Service Duration: " + ((double) (System.currentTimeMillis() - startTime) / 1000d) + "ms");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Service explicitly started...");
        // Cleanup
        for (AsyncTask<?, ?, ?> asyncTask : imagesAsyncTasksList) {
            asyncTask.cancel(true);
        }
        imagesAsyncTasksList.clear();

        List<RSSImageRetrieverItem> thumbnailUrls = getDataProviderManager().getNewsDataProvider()
                .getAllPendingThumbnailUrls();
        this.maxProgress = thumbnailUrls.size();
        Log.d(TAG, maxProgress + " images pending... ");

        int intervalThumbnails = (int) Math.ceil((double) maxProgress
                / (double) getConfigurationManager().getConfiguration().getNumberOfImageDownloadThreads());

        Log.d(TAG, "It should be divided into AsyncTask of each " + intervalThumbnails + " images!");

        if (maxProgress > 0) {
            // DataManager.getInstance().showNotification(getString(R.string.update_newsimages));
            initNotification(maxProgress);

            int intervalThumbnailsCounter = intervalThumbnails;
            for (int i = 0; i < getConfigurationManager().getConfiguration().getNumberOfImageDownloadThreads(); i++) {
                List<RSSImageRetrieverItem> intervalItems = new ArrayList<RSSImageRetrieverItem>();
                for (int j = i * intervalThumbnails; j < Math.min(intervalThumbnailsCounter, thumbnailUrls.size()); j++) {
                    intervalItems.add(thumbnailUrls.get(j));
                }
                intervalThumbnailsCounter += intervalThumbnails;

                Log.d(TAG, "Thumbnails in Interval " + (i + 1) + ": " + intervalItems.size());

                if (intervalItems.size() > 0) {
                    imagesAsyncTasksList.add(new RSSImageRetriever(this, getDataProviderManager(), getApplicationContext()).execute(intervalItems
                            .toArray(new RSSImageRetrieverItem[] {})));
                }
            }
        }
        return START_STICKY;
    }

    private void initNotification(int maxProgress) {
        // configure the intent
        Intent notificationintent = new Intent(this, NewsCategoriesActivity.class);
        final PendingIntent pendingIntent = PendingIntent
                .getActivity(getApplicationContext(), 0, notificationintent, 0);

        // configure the notification
        notification = new Notification(R.drawable.app_icon, getString(R.string.update_newsimages), System.currentTimeMillis());
        notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
        notification.contentIntent = pendingIntent;
        notification.contentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.download_progress);
        notification.contentView.setImageViewResource(R.id.status_icon, R.drawable.app_icon);
        notification.contentView.setTextViewText(R.id.status_text, getString(R.string.update_newsimages));
        notification.contentView.setProgressBar(R.id.status_progress, maxProgress, progress, false);

        notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(IMAGE_DOWNLOAD_NOTIFICATION_ID, notification);
    }

    @Override
    public void onRssImageRetrieverProgress() {
        progress++;
        notification.contentView.setProgressBar(R.id.status_progress, maxProgress, progress, false);
        // inform the progress bar of updates in progress
        notificationManager.notify(IMAGE_DOWNLOAD_NOTIFICATION_ID, notification);
        if (maxProgress == progress)
            notificationManager.cancel(IMAGE_DOWNLOAD_NOTIFICATION_ID);
    }

    @Override
    public void onRssImageRetrieverExecutionComplete(AsyncTask<?, ?, ?> result) {
        if (imagesAsyncTasksList.remove(result)) {
            int count = imagesAsyncTasksList.size();
            Log.d(TAG, "RSSImageRetriever Done!");
            if (count == 0) {
                // DataManager.getInstance().showNotification(getString(R.string.update_newsimages_complete));
                // Timer timer = new Timer();
                // timer.schedule(new Task(), 2000);

                // Stop after task finished
                this.stopSelf();
            }
        } else
            Log.e(TAG, "RSSRetriever could not be removed!");
    }

    public IDataProviderManager getDataProviderManager() {
    	return (IDataProviderManager) getApplication();
    }
    
    public IConfigurationManager getConfigurationManager() {
    	return (IConfigurationManager) getApplication();
    }
}
