package de.telekom.cldii.service.newsupdate;

import static de.telekom.cldii.ApplicationConstants.CATEGORY_CLEAN_FLAG;
import static de.telekom.cldii.ApplicationConstants.CATEGORY_UPDATED;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import de.telekom.cldii.R;
import de.telekom.cldii.config.IConfigurationManager;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.news.INewsDataProvider;
import de.telekom.cldii.data.news.NewsFeed;
import de.telekom.cldii.service.UpdateInterval;
import de.telekom.cldii.service.imagedownload.NewsImageDownloadService;
import de.telekom.cldii.view.news.NewsCategoriesActivity;
import de.telekom.cldii.view.util.NetworkCheck;

/**
 * Service for retrieving news from the internet and forwarding them to the
 * {@link INewsDataProvider}.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public class NewsUpdateService extends Service implements NewsUpdateTask.NewsFeedUpdateTaskListener {

    private static final String TAG = "NewsUpdateService";
    private static final int NEWS_DOWNLOAD_NOTIFICATION_ID = 41;

    private List<NewsFeed> pendingNewsFeeds;
    private List<NewsFeed> currentlyUpdatingNewsFeeds;
    private List<NewsUpdateTask> updateTasks;
    private boolean updateNotificationShown;

    @Override
    public void onCreate() {
        super.onCreate();
        pendingNewsFeeds = new ArrayList<NewsFeed>();
        currentlyUpdatingNewsFeeds = new ArrayList<NewsFeed>();
        updateTasks = new ArrayList<NewsUpdateTask>();
        updateNotificationShown = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (NewsUpdateTask updateTask : updateTasks) {
            updateTask.cancel(true);
        }
        hideUpdateNotification();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // this service does not support binding
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (getNewsUpdateServiceMode(intent)) {
        case UPDATE_ALL_CATEGORIES:
            Log.v(TAG, "Enqueue update for all categories");
            List<NewsFeed> allNewsFeeds = getDataProviderManager().getNewsDataProvider().getAllFeeds();
            addNewsFeedsForUpdate(allNewsFeeds);
            // update the last updated time for all categories
            getDataProviderManager().getNewsDataProvider().updateLastUpdatedTime(null);
            break;

        case UPDATE_ALL_OUTDATED_CATEGORIES:
            Log.v(TAG, "Enqueue update for all outdated categories");
            List<NewsFeed> allOutdatedNewsFeeds = getDataProviderManager().getNewsDataProvider().getAllOutdatedFeeds(
                    getConfigurationManager());
            addNewsFeedsForUpdate(allOutdatedNewsFeeds);
            // update the last updated time for all outdated categories
            getDataProviderManager().getNewsDataProvider().updateOutdatedLastUpdatedTime(allOutdatedNewsFeeds);
            break;

        case UPDATE_CATEGORY_ID:
            long categoryId = getUpdateCategoryWithId(intent);
            Log.v(TAG, "Enqueue update for category with id " + categoryId);
            List<NewsFeed> newsFeedsForCategory = getDataProviderManager().getNewsDataProvider().getFeedsForCategoryId(
                    categoryId);
            addNewsFeedsForUpdate(newsFeedsForCategory);
            // update the last updated time of categories of a given category id
            getDataProviderManager().getNewsDataProvider().updateLastUpdatedTime(categoryId);
            break;

        case UPDATE_CATEGORIES_WITH_INTERVAL:
            UpdateInterval updateInterval = getUpdateCategoriesWithInterval(intent);
            Log.v(TAG, "Enqueue update for categories with update interval " + updateInterval);
            List<NewsFeed> newsFeedsForInterval = getDataProviderManager().getNewsDataProvider() 
                    .getFeedsByIntervalType(updateInterval);
            addNewsFeedsForUpdate(newsFeedsForInterval);
            // update the last updated time of categories of a given interval type
            getDataProviderManager().getNewsDataProvider().updateLastUpdatedTime(updateInterval);
            break;

        default:
            Log.w(TAG, "Service called with invalid parameters");
            break;
        }
        if (NetworkCheck.isOnline(this))
            startUpdateTasks();
        else
            Log.i(TAG, "No Internet Connectivity!");
        return START_STICKY;
    }

    /**
     * Checks if there are free threads for starting the update of pending news
     * feeds.
     */
    private void startUpdateTasks() {
        Log.v(TAG, "Pending news feeds: " + pendingNewsFeeds.size() + " Updating news feeds: "
                + currentlyUpdatingNewsFeeds.size());
        Log.v(TAG, "Starting update threads");
        int numberOfMaxThreads = getConfigurationManager().getConfiguration().getNumberOfNewsDownloadThreads();
        for (int newThreadNr = 0; (newThreadNr < numberOfMaxThreads - updateTasks.size())
                && (pendingNewsFeeds.size() > 0); newThreadNr++) {
            NewsFeed newsFeed = pendingNewsFeeds.remove(pendingNewsFeeds.size() - 1);
            currentlyUpdatingNewsFeeds.add(newsFeed);
            NewsUpdateTask updateTask = new NewsUpdateTask(this, getDataProviderManager());
            updateTasks.add(updateTask);
            updateTask.execute(newsFeed);
        }
        if (updateTasks.size() > 0) {
            showUpdateNotification();
        } else {
            finishUpdateTasks();
        }
        Log.v(TAG, "Pending news feeds: " + pendingNewsFeeds.size() + " Updating news feeds: "
                + currentlyUpdatingNewsFeeds.size());
    }

    /**
     * Clean up after all pending news feeds have been updated.
     */
    private void finishUpdateTasks() {
        // Cleanup old news
        int count = 0;
        count += getDataProviderManager().getNewsDataProvider().removeNewsItemsByTime();
        count += getDataProviderManager().getNewsDataProvider().removeNewsItemsByCount();
        count += getDataProviderManager().getNewsDataProvider().removeUnusedImages();
        if (count > 0) {
            // Notify on cleanup
            Intent intent = new Intent();
            intent.setAction(CATEGORY_UPDATED);
            intent.putExtra(CATEGORY_CLEAN_FLAG, true);
            sendBroadcast(intent);
        }

        hideUpdateNotification();
        Intent serviceIntent = new Intent(getApplicationContext(), NewsImageDownloadService.class);
        startService(serviceIntent);
    }

    /**
     * Adds News feeds for updating. If a news feed is already currently
     * updating it is ignored.
     * 
     * @param newsFeedsForUpdate
     *            news feeds to be updated
     */
    private void addNewsFeedsForUpdate(List<NewsFeed> newsFeedsForUpdate) {
        Log.v(TAG, "Adding " + newsFeedsForUpdate.size() + " news feeds for update");
        for (NewsFeed newsFeedForUpdate : newsFeedsForUpdate) {
            if (newsFeedForUpdate.isActivated() && !currentlyUpdatingNewsFeeds.contains(newsFeedForUpdate)
                    && !pendingNewsFeeds.contains(newsFeedForUpdate)) {
                pendingNewsFeeds.add(newsFeedForUpdate);
            }
        }
        Log.v(TAG, "Pending news feeds: " + pendingNewsFeeds.size() + " Updating news feeds: "
                + currentlyUpdatingNewsFeeds.size());
    }

    @Override
    public void taskFinished(NewsUpdateTask finishedTask) {
        updateTasks.remove(finishedTask);
        startUpdateTasks();
    }

    @Override
    public void newsFeedUpdated(NewsFeed updatedNewsFeed) {
        currentlyUpdatingNewsFeeds.remove(updatedNewsFeed);
    }

    /**
     * Shows a notification on the notification bar.
     */
    private void showUpdateNotification() {
        if (!updateNotificationShown) {
            // open news categories if notification is pressed
            Intent openNewsCategoriesIntent = new Intent(this, NewsCategoriesActivity.class);
            final PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                    openNewsCategoriesIntent, 0);

            Notification notification = new Notification(R.drawable.app_icon, getString(R.string.update_newsitems),
                    System.currentTimeMillis());
            notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;
            notification.contentIntent = pendingIntent;
            notification.contentView = new RemoteViews(getApplicationContext().getPackageName(),
                    R.layout.download_progress);
            notification.contentView.setImageViewResource(R.id.status_icon, R.drawable.app_icon);
            notification.contentView.setTextViewText(R.id.status_text, getString(R.string.update_newsitems));
            notification.contentView.setProgressBar(R.id.status_progress, 0, 0, true);

            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(
                    NOTIFICATION_SERVICE);
            notificationManager.notify(NEWS_DOWNLOAD_NOTIFICATION_ID, notification);
            updateNotificationShown = true;
        }
    }

    /**
     * Hides the notification from the notification bar.
     */
    private void hideUpdateNotification() {
        if (updateNotificationShown) {
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(
                    NOTIFICATION_SERVICE);
            notificationManager.cancel(NEWS_DOWNLOAD_NOTIFICATION_ID);
            updateNotificationShown = false;
        }
    }

    /**
     * @return the data provider manager
     */
    private IDataProviderManager getDataProviderManager() {
        return (IDataProviderManager) getApplication();
    }

    /**
     * @return the configuration manager
     */
    private IConfigurationManager getConfigurationManager() {
        return (IConfigurationManager) getApplication();
    }

    // Configuration handling for starting the service

    /**
     * Execution modes of the news update service.
     */
    private enum NewsUpdateServiceMode {
        UPDATE_ALL_CATEGORIES, UPDATE_ALL_OUTDATED_CATEGORIES, UPDATE_CATEGORY_ID, UPDATE_CATEGORIES_WITH_INTERVAL, UNDEFINED
    }

    /**
     * Factory to retrieve a configured intent ready for starting a
     * {@link NewsUpdateService}.
     */
    public static Intent updateAllCategoriesIntent(Context context) {
        Intent newsUpdateServiceIntent = new Intent(context, NewsUpdateService.class);
        newsUpdateServiceIntent.putExtra(NewsUpdateServiceMode.UPDATE_ALL_CATEGORIES.toString(), true);
        return newsUpdateServiceIntent;
    }

    /**
     * Factory to retrieve a configured intent ready for starting a
     * {@link NewsUpdateService}.
     */
    public static Intent updateAllOutdatedCategoriesIntent(Context context) {
        Intent newsUpdateServiceIntent = new Intent(context, NewsUpdateService.class);
        newsUpdateServiceIntent.putExtra(NewsUpdateServiceMode.UPDATE_ALL_OUTDATED_CATEGORIES.toString(), true);
        return newsUpdateServiceIntent;
    }

    /**
     * Factory to retrieve a configured intent ready for starting a
     * {@link NewsUpdateService}.
     */
    public static Intent updateCategoryWithIdIntent(Context context, long categoryId) {
        Intent newsUpdateServiceIntent = new Intent(context, NewsUpdateService.class);
        newsUpdateServiceIntent.putExtra(NewsUpdateServiceMode.UPDATE_CATEGORY_ID.toString(), categoryId);
        return newsUpdateServiceIntent;
    }

    /**
     * Factory to retrieve a configured intent ready for starting a
     * {@link NewsUpdateService}.
     */
    public static Intent updateCategoriesWithIntervalIntent(Context context, UpdateInterval updateInterval) {
        Intent newsUpdateServiceIntent = new Intent(context, NewsUpdateService.class);
        newsUpdateServiceIntent.putExtra(NewsUpdateServiceMode.UPDATE_CATEGORIES_WITH_INTERVAL.toString(),
                updateInterval.ordinal());
        return newsUpdateServiceIntent;
    }

    /**
     * Determines the execution mode of the service depending on the given
     * intent.
     */
    private NewsUpdateServiceMode getNewsUpdateServiceMode(Intent intent) {
        if (intent.hasExtra(NewsUpdateServiceMode.UPDATE_ALL_CATEGORIES.toString())) {
            return NewsUpdateServiceMode.UPDATE_ALL_CATEGORIES;
        }
        if (intent.hasExtra(NewsUpdateServiceMode.UPDATE_ALL_OUTDATED_CATEGORIES.toString())) {
            return NewsUpdateServiceMode.UPDATE_ALL_OUTDATED_CATEGORIES;
        }
        if (intent.hasExtra(NewsUpdateServiceMode.UPDATE_CATEGORY_ID.toString())) {
            return NewsUpdateServiceMode.UPDATE_CATEGORY_ID;
        }
        if (intent.hasExtra(NewsUpdateServiceMode.UPDATE_CATEGORIES_WITH_INTERVAL.toString())) {
            return NewsUpdateServiceMode.UPDATE_CATEGORIES_WITH_INTERVAL;
        }
        return NewsUpdateServiceMode.UNDEFINED;
    }

    /**
     * Extracts the category id from the given intent. Defaults to -1.
     */
    private long getUpdateCategoryWithId(Intent intent) {
        return intent.getLongExtra(NewsUpdateServiceMode.UPDATE_CATEGORY_ID.toString(), -1);
    }

    /**
     * Extracts the update interval from the given intent. Defaults to the first
     * update interval.
     */
    private UpdateInterval getUpdateCategoriesWithInterval(Intent intent) {
        int ordinalValue = intent.getIntExtra(NewsUpdateServiceMode.UPDATE_CATEGORIES_WITH_INTERVAL.toString(), 0);
        return UpdateInterval.values()[ordinalValue];
    }
}
