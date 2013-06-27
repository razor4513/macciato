package de.telekom.cldii.service.newsupdate;

import java.util.List;

import org.apache.http.client.methods.HttpGet;

import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.util.Log;
import de.telekom.cldii.ApplicationConstants;
import de.telekom.cldii.data.IDataProviderManager;
import de.telekom.cldii.data.news.NewsFeed;
import de.telekom.cldii.util.rssparser.RSSFault;
import de.telekom.cldii.util.rssparser.RSSFeed;
import de.telekom.cldii.util.rssparser.RSSItem;
import de.telekom.cldii.util.rssparser.RSSReader;
import de.telekom.cldii.util.rssparser.RSSReaderException;

/**
 * Asynchronous task to update a single news feed item.
 * 
 * @author Jun Chen, jambit GmbH
 * @author Anton Wolf, jambit GmbH
 */
public class NewsUpdateTask extends AsyncTask<NewsFeed, Double, Void> {

    private static final String TAG = "NewsFeedUpdateTask";

    private NewsFeedUpdateTaskListener listener;
    private IDataProviderManager dataProviderManager;

    public NewsUpdateTask(NewsFeedUpdateTaskListener listener, IDataProviderManager dataProviderManager) {
        this.listener = listener;
        this.dataProviderManager = dataProviderManager;
    }

    @Override
    protected Void doInBackground(NewsFeed... newsFeeds) {
        for (int i = 0; i < newsFeeds.length && !isCancelled(); i++) {
            try {
                new HttpGet(newsFeeds[i].getUrl());

                Log.v(TAG, "Loading [" + newsFeeds[i].getUrl() + "]... ");
                RSSReader reader = new RSSReader();
                try {
                    RSSFeed feed = reader.load(newsFeeds[i].getUrl());
                    Log.v(TAG, "[" + newsFeeds[i].getUrl() + "] " + feed.getItems().size() + " Elements loaded!");

                    if (newsFeeds[i].getName() == null) {
                        // Add feed name if it's missing
                        dataProviderManager.getNewsDataProvider().storeFeed(newsFeeds[i].getFeedId(), feed.getTitle());

                        Log.v(TAG, "Missing Feed Name [" + feed.getTitle() + "] added!");
                    }
                    List<RSSItem> feedList = feed.getItems();

                    for (int j = 0; j < Math.min(feedList.size(), ApplicationConstants.NEWSCATEGORY_LIMIT)
                            && !isCancelled(); j++) {
                        RSSItem feedItem = feedList.get(j);
                        String feedUrl = feedItem.getLink().toString();
                        String thumbnailUrl = (feedItem.getThumbnails().size() == 0 ? null : feedItem.getThumbnails()
                                .get(feedItem.getThumbnails().size() - 1).getUrl().toString());

                        dataProviderManager.getNewsDataProvider().storeNewsItem(newsFeeds[i].getParentCategoryId(),
                                newsFeeds[i].getFeedId(), feedItem.getTitle(), feedItem.getDescription(),
                                feedItem.getPubDate(), feedItem.getContent(),
                                (feedItem.getGuid() != null ? feedItem.getGuid() : feedUrl), feedUrl, thumbnailUrl,
                                feedItem.getContentImages());
                    }
                    reader.close();
                    Log.i(TAG, "[" + feed.getTitle() + "] finished!");
                } catch (IllegalStateException e) {
                    Log.w(TAG, "Database closed!");
                } catch (RSSReaderException e) {
                    e.printStackTrace();
                } catch (RSSFault e) {
                    Log.d(TAG, e.getMessage());
                } catch (SQLiteException e) {
                    Log.d(TAG, "Database Problems! App exited?");
                } finally {
                    if (reader != null)
                        reader.close();
                }
                listener.newsFeedUpdated(newsFeeds[i]);
            } catch (IllegalArgumentException e1) {
                Log.w(TAG, newsFeeds[i].getUrl() + " invalid!");
            }

        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        listener.taskFinished(this);
    }

    /**
     * Callback interface to get updates of the execution state.
     * 
     * @author awolf
     */
    public interface NewsFeedUpdateTaskListener {

        /**
         * Called when the task is finished.
         * 
         * @param finishedTask
         *            the finished task
         */
        public void taskFinished(NewsUpdateTask finishedTask);

        /**
         * Called when the task has finished updating a newsfeed.
         * 
         * @param updatedNewsFeed
         *            the updated news feed
         */
        public void newsFeedUpdated(NewsFeed updatedNewsFeed);
    }
}
