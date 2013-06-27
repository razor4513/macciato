/**
 * Implementation of the NewsDataService.
 */
package de.telekom.cldii.data.news.impl;

import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.CATEGORIES_ICON;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.CATEGORIES_ID;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.CATEGORIES_INTERVAL;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.CATEGORIES_LASTUPDATE;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.CATEGORIES_NAME;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.FEEDS_ACTIVATED;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.FEEDS_CID;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.FEEDS_ID;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.FEEDS_NAME;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.FEEDS_URL;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.IMAGES_CONTENT;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.IMAGES_DATE;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.IMAGES_ID;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.IMAGES_URL;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.NEWS_CONTENT;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.NEWS_DATE;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.NEWS_FID;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.NEWS_GUID;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.NEWS_ID;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.NEWS_IMAGE;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.NEWS_READ;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.NEWS_SUMMARY;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.NEWS_TITLE;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.NEWS_URL;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.TABLE_CATEGORIES;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.TABLE_FEEDS;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.TABLE_IMAGES;
import static de.telekom.cldii.data.news.impl.NewsSQLiteConstants.TABLE_NEWS;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import de.telekom.cldii.ApplicationConstants;
import de.telekom.cldii.config.IConfigurationManager;
import de.telekom.cldii.data.news.INewsDataProvider;
import de.telekom.cldii.data.news.NewsCategory;
import de.telekom.cldii.data.news.NewsFeed;
import de.telekom.cldii.data.news.NewsItem;
import de.telekom.cldii.service.UpdateInterval;
import de.telekom.cldii.service.imagedownload.RSSImageRetriever;
import de.telekom.cldii.service.imagedownload.RSSImageRetrieverItem;
import de.telekom.cldii.widget.grid.GridViewContent;

/**
 * Implementation of the NewsDataService.
 * 
 * @author Christoph HŸbner
 * 
 */
public class SQLiteNewsDataProvider implements INewsDataProvider {
    // Tag as identifier for Log methods
    private final String TAG = "SQLiteNewsDataProvider";
    private SQLiteDatabase writableDatabase;
    public static final int THREE_DAYS_MILLIS = 259200000;
    private final Object synchronizer = new Object();
    private Context context;

    private Map<Long, Long> lastCategoryItemDate = new HashMap<Long, Long>();

    // / instance variables
    private SQLiteNewsDataProvider.NewsDBHelper dbHelper = null;

    public SQLiteNewsDataProvider() {
    };

    @Override
    public void onCreate(Context context, IConfigurationManager configurationManager) {
        this.context = context;
        dbHelper = new SQLiteNewsDataProvider.NewsDBHelper(context, configurationManager);
        this.writableDatabase = dbHelper.getWritableDatabase();
        this.writableDatabase.execSQL("PRAGMA foreign_keys = ON;");
        Log.d(TAG, SQLiteNewsDataProvider.class + " created");

        storeLastCategoryItemDate();
    }

    // / Implementation of INewsDataService interface

    @Override
    public int getNumberOfCategories() {
        Cursor query = this.writableDatabase.rawQuery("SELECT COUNT(*) FROM " + TABLE_CATEGORIES, null);

        query.moveToFirst();
        int count = query.getInt(0);
        query.close();

        return count;
    }

    @Override
    public List<NewsCategory> getNewsCategoriesOrderedByName() {
        Cursor query = this.writableDatabase.query(TABLE_CATEGORIES, new String[] { CATEGORIES_ID, CATEGORIES_NAME },
                null, null, null, null, CATEGORIES_NAME + " ASC");

        List<NewsCategory> newsCategoryList = new ArrayList<NewsCategory>();
        while (query.moveToNext()) {
            // get the data for the category item from database and add it to
            // the page
            int categoryId = query.getInt(query.getColumnIndex(CATEGORIES_ID));

            int unreadNews = getNumberOfUnreadNewsItemsForCategoryId(categoryId);
            NewsCategory newsCategory = new NewsCategory(categoryId, query.getString(query
                    .getColumnIndex(CATEGORIES_NAME)), null, null, unreadNews);
            newsCategoryList.add(newsCategory);
        }
        query.close();

        return newsCategoryList;
    }

    @Override
    public Object getNewsItemColumnForNewsId(long newsId, String column) {
        Cursor query = this.writableDatabase.query(TABLE_NEWS, new String[] { column }, NEWS_ID + "=" + newsId, null,
                null, null, null);

        Object result = null;
        if (query.getCount() > 0) {
            query.moveToFirst();
            if (column.equals(NEWS_IMAGE)) {
                String url = query.getString(query.getColumnIndex(column));
                query.close();
                Cursor imageQuery = this.writableDatabase.query(TABLE_IMAGES, new String[] { IMAGES_CONTENT },
                        IMAGES_URL + "=?", new String[] { url }, null, null, null);

                if (imageQuery.getCount() > 0) {
                    imageQuery.moveToFirst();
                    byte[] image = imageQuery.getBlob(imageQuery.getColumnIndex(IMAGES_CONTENT));
                    imageQuery.close();
                    
                    if (image != null) {
                    	BitmapFactory.Options options = new BitmapFactory.Options();
                    	options.inSampleSize = 2;
                        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);
                        result = bitmap;
                    }
                } else {
                    Log.w(TAG, "Bitmap couldn't be found: " + url);
                }
            } else {
                result = query.getString(query.getColumnIndex(column));
                query.close();
            }
        }
        
        return result;
    }

    @Override
    public List<NewsItem> getNewsItemsForNewsCategoryOrderedByDate(long categoryId) {
        String sql = "SELECT news." + NEWS_ID + ", news." + NEWS_TITLE + ", news." + NEWS_DATE + ", news."
                + NEWS_SUMMARY + ", news." + NEWS_URL + ", news." + NEWS_CONTENT + ", images." + IMAGES_CONTENT
                + ", news." + NEWS_READ + ", feeds." + FEEDS_NAME + " FROM " + TABLE_FEEDS + " feeds INNER JOIN "
                + TABLE_NEWS + " news ON feeds." + FEEDS_ID + "=news." + NEWS_FID + " LEFT OUTER JOIN " + TABLE_IMAGES
                + " images ON news." + NEWS_IMAGE + "=images." + IMAGES_URL + " WHERE feeds." + FEEDS_CID + "="
                + categoryId + " AND " + TABLE_FEEDS + "." + FEEDS_ACTIVATED + "=1" + " ORDER BY news." + NEWS_DATE
                + " DESC";

        Cursor query = this.writableDatabase.rawQuery(sql, null);

        List<NewsItem> newsList = new ArrayList<NewsItem>();
        while (query.moveToNext()) {
            NewsItem newsItem = new NewsItem();

            newsItem.setNewsId(query.getInt(query.getColumnIndex(NEWS_ID)));
            newsItem.setTitle(query.getString(query.getColumnIndex(NEWS_TITLE)));
            newsItem.setIsRead(query.getInt(query.getColumnIndex(NEWS_READ)) != 0);
            String feedName = query.getString(query.getColumnIndex(FEEDS_NAME));
            newsItem.setParentFeedName((feedName == null ? "No Name" : feedName));
            newsItem.setSummary(query.getString(query.getColumnIndex(NEWS_SUMMARY)));
            newsItem.setUrl(query.getString(query.getColumnIndex(NEWS_URL)));
            newsItem.setContent(query.getString(query.getColumnIndex(NEWS_CONTENT)));

            Date newDate = new Date(Long.valueOf(query.getString(query.getColumnIndex(NewsSQLiteConstants.NEWS_DATE))));
            newsItem.setCreationDate(newDate);
            // byte[] image =
            // query.getBlob(query.getColumnIndex(IMAGES_CONTENT));
            // if (image != null)
            // newsItem.setImage(new
            // BitmapDrawable(BitmapFactory.decodeByteArray(image, 0,
            // image.length)));

            newsList.add(newsItem);
        }
        query.close();

        return newsList;
    }

    @Override
    public NewsItem getNewsItemForNewsId(long newsId) {
        String sql = "SELECT news." + NEWS_ID + ", news." + NEWS_TITLE + ", news." + NEWS_DATE + ", news."
                + NEWS_SUMMARY + ", news." + NEWS_URL + ", news." + NEWS_CONTENT + ", images." + IMAGES_CONTENT
                + ", news." + NEWS_READ + ", feeds." + FEEDS_NAME + " FROM " + TABLE_FEEDS + " feeds INNER JOIN "
                + TABLE_NEWS + " news ON feeds." + FEEDS_ID + "=news." + NEWS_FID + " LEFT OUTER JOIN " + TABLE_IMAGES
                + " images ON news." + NEWS_IMAGE + "=images." + IMAGES_URL + " WHERE news." + NEWS_ID + "=" + newsId
                + " AND " + TABLE_FEEDS + "." + FEEDS_ACTIVATED + "=1";

        Cursor query = this.writableDatabase.rawQuery(sql, null);
        NewsItem newsItem = null;

        if (query.getCount() > 0) {
            newsItem = new NewsItem();
            query.moveToFirst();

            newsItem.setNewsId(query.getInt(query.getColumnIndex(NEWS_ID)));
            newsItem.setTitle(query.getString(query.getColumnIndex(NEWS_TITLE)));
            newsItem.setIsRead(query.getInt(query.getColumnIndex(NEWS_READ)) != 0);
            String feedName = query.getString(query.getColumnIndex(FEEDS_NAME));
            newsItem.setParentFeedName((feedName == null ? "No Name" : feedName));
            newsItem.setSummary(query.getString(query.getColumnIndex(NEWS_SUMMARY)));
            newsItem.setUrl(query.getString(query.getColumnIndex(NEWS_URL)));
            newsItem.setContent(query.getString(query.getColumnIndex(NEWS_CONTENT)));

            Date newDate = new Date(Long.valueOf(query.getString(query.getColumnIndex(NewsSQLiteConstants.NEWS_DATE))));
            newsItem.setCreationDate(newDate);
            byte[] image = query.getBlob(query.getColumnIndex(IMAGES_CONTENT));
            if (image != null)
                newsItem.setImage(new BitmapDrawable(BitmapFactory.decodeByteArray(image, 0, image.length)));
        }
        query.close();

        return newsItem;
    }

    @Override
    public Drawable getImageForUrl(long newsId, String url) {
        String[] columns = { IMAGES_ID, IMAGES_CONTENT };
        Cursor query = this.writableDatabase.query(TABLE_IMAGES, columns, IMAGES_URL + "='" + url + "'", null, null,
                null, null);

        Drawable imageDrawable = null;
        byte[] image;
        query.moveToFirst();
        if (query.getCount() > 0 && (image = query.getBlob(query.getColumnIndex(IMAGES_CONTENT))) != null) {
            imageDrawable = new BitmapDrawable(BitmapFactory.decodeByteArray(image, 0, image.length));
        } else {
            Log.w(TAG, "Image " + url + " not found! Fetching from Web...");
            // imageDrawable =
            // context.getResources().getDrawable(R.drawable.empty_pixel);
            RSSImageRetrieverItem imageItem = new RSSImageRetrieverItem(String.valueOf(newsId), null, url, false);
            new RSSImageRetriever(ApplicationConstants.IMAGE_LOADED).execute(imageItem);
        }

        query.close();
        return imageDrawable;
    }

    @Override
    public GridViewContent getNewsCategoryPageOrderedByName(int pageNumber) {
        String limit = (pageNumber * GridViewContent.PAGE_SIZE) + ", " + GridViewContent.PAGE_SIZE;

        GridViewContent pageContent = new GridViewContent();
        if (this.writableDatabase != null) {
            Cursor query = this.writableDatabase.query(TABLE_CATEGORIES, new String[] { CATEGORIES_ID, CATEGORIES_NAME,
                    CATEGORIES_ICON }, null, null, null, null, CATEGORIES_NAME + " ASC", limit);

            while (query.moveToNext()) {
                // get the data for the category item from database and add it
                // to the page
                byte[] blob = query.getBlob(query.getColumnIndex(CATEGORIES_ICON));
                Bitmap bitmap = null;
                if (blob != null && blob.length > 0) {
                    bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.length);
                }
                int categoryId = query.getInt(query.getColumnIndex(CATEGORIES_ID));
                int unreadNews = getNumberOfUnreadNewsItemsForCategoryId(categoryId);
                NewsCategory newsCategory = new NewsCategory(query.getInt(query.getColumnIndex(CATEGORIES_ID)),
                        query.getString(query.getColumnIndex(CATEGORIES_NAME)), bitmap, null, unreadNews);
                pageContent.addCellContent(newsCategory);
            }
            query.close();
        }

        return pageContent;
    }

    @Override
    public List<GridViewContent> getNewsCategoryPagesOrderedByName() {
        Cursor query = this.writableDatabase.query(TABLE_CATEGORIES, new String[] { CATEGORIES_ID, CATEGORIES_NAME },
                null, null, null, null, CATEGORIES_NAME + " ASC");

        List<GridViewContent> pageContentList = new ArrayList<GridViewContent>();
        GridViewContent pageContent = new GridViewContent();
        int itemCounter = 0;
        // use the itemCounter to separate the news categories into pages
        while (query.moveToNext()) {
            // get the data for the category item from database and add it to
            // the page
            int categoryId = query.getInt(query.getColumnIndex(CATEGORIES_ID));

            int unreadNews = getNumberOfUnreadNewsItemsForCategoryId(categoryId);
            NewsCategory newsCategory = new NewsCategory(query.getInt(query.getColumnIndex(CATEGORIES_ID)),
                    query.getString(query.getColumnIndex(CATEGORIES_NAME)), null, null, unreadNews);
            pageContent.addCellContent(newsCategory);

            // create a new page when a page is full
            if ((itemCounter / GridViewContent.PAGE_SIZE) == 0) {
                pageContentList.add(pageContent);
                pageContent = new GridViewContent();
                itemCounter++;
            }
        }
        query.close();

        return pageContentList;
    }

    @Override
    public String getNewsCategoryNameById(long categoryId) {
        String categoryName = "";
        if (this.writableDatabase != null) {
            String[] columns = { CATEGORIES_NAME };

            Cursor query = this.writableDatabase.query(TABLE_CATEGORIES, columns, CATEGORIES_ID + "=" + categoryId,
                    null, null, null, null);

            if (query != null && query.getCount() > 0) {
                query.moveToFirst();
                categoryName = query.getString(query.getColumnIndex(CATEGORIES_NAME));
            }
            query.close();
        }

        return categoryName;
    }

    @Override
    public NewsCategory getNewsCategoryById(long id) {
        Cursor query = this.writableDatabase.query(TABLE_CATEGORIES, new String[] { CATEGORIES_NAME,
                CATEGORIES_INTERVAL, CATEGORIES_ICON }, CATEGORIES_ID + "=" + id, null, null, null, null);

        NewsCategory newsCategory = null;
        if (query.getCount() > 0) {
            query.moveToFirst();
            String categoryName = query.getString(query.getColumnIndex(CATEGORIES_NAME));
            byte[] iconBlob = query.getBlob(query.getColumnIndex(CATEGORIES_ICON));
            Bitmap categoryIcon = null;
            if (iconBlob != null)
                categoryIcon = BitmapFactory.decodeByteArray(iconBlob, 0, iconBlob.length);
            UpdateInterval updateInterval = UpdateInterval.values()[query.getInt(query
                    .getColumnIndex(CATEGORIES_INTERVAL))];
            newsCategory = new NewsCategory((int) id, categoryName, categoryIcon, updateInterval, 0);

        }
        query.close();
        return newsCategory;
    }

    @Override
    public List<NewsFeed> getFeedsByIntervalType(UpdateInterval intervalType) {
        String sql = "SELECT feeds." + FEEDS_ID + ", feeds." + FEEDS_NAME + ", feeds." + FEEDS_URL + ", feeds."
                + FEEDS_ACTIVATED + ", feeds." + FEEDS_CID + " FROM " + TABLE_CATEGORIES + " category, " + TABLE_FEEDS
                + " feeds WHERE category." + CATEGORIES_INTERVAL + "=" + intervalType.ordinal() + " AND category."
                + CATEGORIES_ID + "=" + "feeds." + FEEDS_CID;

        Cursor query = this.writableDatabase.rawQuery(sql, null);

        List<NewsFeed> newsFeeds = new ArrayList<NewsFeed>();
        while (query.moveToNext()) {
            NewsFeed newsFeed = new NewsFeed();
            newsFeed.setFeedId(query.getInt(query.getColumnIndex(FEEDS_ID)));
            newsFeed.setParentCategoryId(query.getLong(query.getColumnIndex(FEEDS_CID)));
            newsFeed.setName(query.getString(query.getColumnIndex(FEEDS_NAME)));
            newsFeed.setUrl(query.getString(query.getColumnIndex(FEEDS_URL)));
            newsFeed.setIsActivated(query.getInt(query.getColumnIndex(FEEDS_ACTIVATED)) == 1);
            newsFeeds.add(newsFeed);
        }
        query.close();

        return newsFeeds;
    }

    @Override
    public List<NewsFeed> getFeedsForCategoryId(long categoryId) {
        Cursor query = this.writableDatabase.query(TABLE_FEEDS, null, FEEDS_CID + "=" + categoryId, null, null, null,
                null);

        List<NewsFeed> newsFeeds = new ArrayList<NewsFeed>();
        while (query.moveToNext()) {
            NewsFeed newsFeed = new NewsFeed();
            newsFeed.setFeedId(query.getInt(query.getColumnIndex(FEEDS_ID)));
            newsFeed.setParentCategoryId(query.getLong(query.getColumnIndex(FEEDS_CID)));
            newsFeed.setName(query.getString(query.getColumnIndex(FEEDS_NAME)));
            newsFeed.setUrl(query.getString(query.getColumnIndex(FEEDS_URL)));
            newsFeed.setIsActivated(query.getInt(query.getColumnIndex(FEEDS_ACTIVATED)) == 1);
            newsFeeds.add(newsFeed);
        }
        query.close();

        return newsFeeds;
    }

    @Override
    public int getNumberOfUnreadNewsItemsForCategoryId(long categoryId) {
        String sql = "SELECT COUNT(*) FROM " + TABLE_FEEDS + " feeds, " + TABLE_NEWS + " news WHERE feeds." + FEEDS_CID
                + "=" + categoryId + " AND feeds." + FEEDS_ID + "=news." + NEWS_FID + " AND " + NEWS_READ + "=0 AND "
                + TABLE_FEEDS + "." + FEEDS_ACTIVATED + "=1";

        Cursor query = this.writableDatabase.rawQuery(sql, null);

        int count = 0;
        if (query.getCount() > 0) {
            query.moveToFirst();
            count = query.getInt(0);
        }
        query.close();
        return count;
    }

    @Override
    public int getNumberOfUnreadNewsItems() {
        String sql = "SELECT COUNT(*) FROM " + TABLE_NEWS + ", " + TABLE_FEEDS + " WHERE " + TABLE_NEWS + "."
                + NEWS_READ + "=0 AND " + TABLE_FEEDS + "." + FEEDS_ACTIVATED + "=1 AND " + TABLE_NEWS + "." + NEWS_FID
                + " = " + TABLE_FEEDS + "." + FEEDS_ID;
        int count = 0;

        try {
            Cursor query = this.writableDatabase.rawQuery(sql, null);
            if (query.getCount() > 0) {
                query.moveToFirst();
                count = query.getInt(0);
            }
            query.close();
        } catch (IllegalStateException e) {
            Log.w(TAG, "Datebase closed!");
        }

        return count;
    }

    @Override
    public List<NewsFeed> getAllFeeds() {
        List<NewsFeed> newsFeeds = new ArrayList<NewsFeed>();

        Cursor query = this.writableDatabase.query(TABLE_FEEDS, null, null, null, null, null, null);

        while (query.moveToNext()) {
            NewsFeed newsFeed = new NewsFeed();
            newsFeed.setFeedId(query.getInt(query.getColumnIndex(FEEDS_ID)));
            newsFeed.setParentCategoryId(query.getLong(query.getColumnIndex(FEEDS_CID)));
            newsFeed.setName(query.getString(query.getColumnIndex(FEEDS_NAME)));
            newsFeed.setUrl(query.getString(query.getColumnIndex(FEEDS_URL)));
            newsFeed.setIsActivated(query.getInt(query.getColumnIndex(FEEDS_ACTIVATED)) == 1);
            newsFeeds.add(newsFeed);
        }
        query.close();

        return newsFeeds;
    }

    @Override
    public List<NewsFeed> getAllOutdatedFeeds(IConfigurationManager configurationManager) {
        // get all lastUpdate times
        Cursor catQuery = this.writableDatabase.query(TABLE_CATEGORIES, new String[] { CATEGORIES_ID,
                CATEGORIES_LASTUPDATE, CATEGORIES_INTERVAL }, null, null, null, null, null);
        Map<Long, Boolean> outdatedCategories = new HashMap<Long, Boolean>();
        while (catQuery.moveToNext()) {
            long lastUpdate = catQuery.getLong(catQuery.getColumnIndex(CATEGORIES_LASTUPDATE));
            Log.d(TAG,
                    "categoryId: "
                            + catQuery.getLong(catQuery.getColumnIndex(CATEGORIES_ID))
                            + " | "
                            + (System.currentTimeMillis() - lastUpdate)
                            + " ms difference | Needed: "
                            + (configurationManager.getConfiguration().getTimeForUpdateInterval(UpdateInterval.values()[catQuery
                                    .getInt(catQuery.getColumnIndex(CATEGORIES_INTERVAL))])));
            boolean outdated = ((System.currentTimeMillis() - lastUpdate) > (configurationManager.getConfiguration()
                    .getTimeForUpdateInterval(UpdateInterval.values()[catQuery.getInt(catQuery
                            .getColumnIndex(CATEGORIES_INTERVAL))])));
            outdatedCategories.put(catQuery.getLong(catQuery.getColumnIndex(CATEGORIES_ID)), outdated);
        }

        catQuery.close();

        // get news feeds
        List<NewsFeed> newsFeeds = new ArrayList<NewsFeed>();

        Cursor query = this.writableDatabase.query(TABLE_FEEDS, null, null, null, null, null, null);

        while (query.moveToNext()) {
            if (outdatedCategories.get(query.getLong(query.getColumnIndex(FEEDS_CID)))) {
                NewsFeed newsFeed = new NewsFeed();
                newsFeed.setFeedId(query.getInt(query.getColumnIndex(FEEDS_ID)));
                newsFeed.setParentCategoryId(query.getLong(query.getColumnIndex(FEEDS_CID)));
                newsFeed.setName(query.getString(query.getColumnIndex(FEEDS_NAME)));
                newsFeed.setUrl(query.getString(query.getColumnIndex(FEEDS_URL)));
                newsFeed.setIsActivated(query.getInt(query.getColumnIndex(FEEDS_ACTIVATED)) == 1);
                newsFeeds.add(newsFeed);
            }
        }
        query.close();

        return newsFeeds;

    }

    @Override
    public List<RSSImageRetrieverItem> getAllPendingThumbnailUrls() {
        List<RSSImageRetrieverItem> thumbnailUrlList = new ArrayList<RSSImageRetrieverItem>();
        String[] columns = { TABLE_NEWS + "." + NEWS_ID + " AS " + NEWS_ID,
                TABLE_NEWS + "." + NEWS_IMAGE + " AS " + NEWS_IMAGE, TABLE_FEEDS + "." + FEEDS_CID + " AS " + FEEDS_CID };
        String selection = TABLE_NEWS + "." + NEWS_FID + "=" + TABLE_FEEDS + "." + FEEDS_ID
                + " AND NOT EXISTS (SELECT " + TABLE_IMAGES + "." + IMAGES_URL + " FROM " + TABLE_IMAGES + " WHERE "
                + TABLE_IMAGES + "." + IMAGES_URL + "=" + TABLE_NEWS + "." + NEWS_IMAGE + ") AND " + TABLE_NEWS + "."
                + NEWS_IMAGE + " IS NOT NULL GROUP BY " + TABLE_NEWS + "." + NEWS_IMAGE;

        Cursor query = this.writableDatabase.query(TABLE_NEWS + ", " + TABLE_FEEDS, columns, selection, null, null,
                null, null);

        while (query.moveToNext()) {
            RSSImageRetrieverItem imageItem = new RSSImageRetrieverItem(
                    query.getString(query.getColumnIndex(FEEDS_CID)), query.getString(query.getColumnIndex(NEWS_ID)),
                    query.getString(query.getColumnIndex(NEWS_IMAGE)), true);
            thumbnailUrlList.add(imageItem);
        }
        query.close();

        return thumbnailUrlList;
    }

    // / Implementation of the IRssStorageService interface

    @Override
    public void storeFeed(int feedId, String feedName) {
        synchronized (synchronizer) {
            ContentValues feedValues = new ContentValues(1);
            feedValues.put(FEEDS_NAME, feedName);
            this.writableDatabase.update(TABLE_FEEDS, feedValues, FEEDS_ID + "=" + feedId, null);
        }
    }

    @Override
    public Long storeNewsItem(long categoryId, int feedId, String title, String description, Date publishDate,
            String content, String guid, String url, String thumbnailUrl, List<String> contentImageUrls) {
        long newsId = 0;
        if (publishDate != null
                && publishDate.getTime() > (System.currentTimeMillis() - THREE_DAYS_MILLIS)
                && (lastCategoryItemDate.get(categoryId) == null || publishDate.getTime() > lastCategoryItemDate
                        .get(categoryId))) {
            synchronized (synchronizer) {
                try {
                    ContentValues newsValues = new ContentValues(7);
                    newsValues.put(NEWS_FID, feedId);
                    newsValues.put(NEWS_TITLE, title);
                    newsValues.put(NEWS_SUMMARY, description);
                    newsValues.put(NEWS_IMAGE, thumbnailUrl);
                    newsValues.put(NEWS_DATE, publishDate.getTime());
                    newsValues.put(NEWS_CONTENT, content);
                    newsValues.put(NEWS_GUID, guid);
                    newsValues.put(NEWS_URL, url);
                    newsId = writableDatabase.insertOrThrow(TABLE_NEWS, "", newsValues);

                } catch (SQLiteConstraintException e) {
                    if (newsDateHasChanged(guid, publishDate)) {
                        String[] whereArgs = { guid };
                        this.writableDatabase.delete(TABLE_NEWS, NEWS_GUID + "=?", whereArgs);
                        Log.w(TAG, "[" + title + "] updated");
                        newsId = storeNewsItem(categoryId, feedId, title, description, publishDate, content, guid, url,
                                thumbnailUrl, contentImageUrls);
                    }
                }
            }
        }
        if (newsId > 0) {
            Intent intent = new Intent();
            intent.setAction(ApplicationConstants.NEWS_UPDATED);
            intent.putExtra(ApplicationConstants.EXTRAS_KEY_CATEGORYID, Long.valueOf(categoryId));
            context.sendBroadcast(intent);

            intent = new Intent();
            intent.setAction(ApplicationConstants.CATEGORY_UPDATED);
            intent.putExtra(ApplicationConstants.EXTRAS_KEY_CATEGORYID, Long.valueOf(categoryId));
            intent.putExtra(ApplicationConstants.EXTRAS_KEY_NEWSID, newsId);
            context.sendBroadcast(intent);
        }
        return newsId;
    }

    @Override
    public void storeNewsItemThumbnail(String imageUrl, byte[] thumbnail) {
        synchronized (synchronizer) {
            try {
                ContentValues thumbnailValues = new ContentValues(2);
                thumbnailValues.put(IMAGES_URL, imageUrl);
                thumbnailValues.put(IMAGES_CONTENT, thumbnail);
                this.writableDatabase.insertOrThrow(TABLE_IMAGES, null, thumbnailValues);
            } catch (SQLiteConstraintException e) {
                Log.w(TAG, imageUrl + " already exist! No insertion into the database performed!");
            }

        }
    }

    @Override
    public void storeNewsItemContentImage(String imageUrl, byte[] contentImageBlob) {
        synchronized (synchronizer) {
            try {
                ContentValues contentImage = new ContentValues(1);
                contentImage.put(IMAGES_CONTENT, contentImageBlob);
                contentImage.put(IMAGES_URL, imageUrl);
                this.writableDatabase.insertOrThrow(TABLE_IMAGES, null, contentImage);
            } catch (SQLiteConstraintException e) {
                Log.w(TAG, imageUrl + " already exist! No insertion into the database performed!");
            }
        }
    }

    @Override
    public void deleteCategory(long categoryId) {
        Cursor query = this.writableDatabase.query(TABLE_FEEDS, new String[] { FEEDS_ID },
                FEEDS_CID + "=" + categoryId, null, null, null, null);

        List<Long> feedIds = new ArrayList<Long>();
        while (query.moveToNext()) {
            feedIds.add(query.getLong(query.getColumnIndex(FEEDS_ID)));
        }
        query.close();

        if (feedIds.size() > 0) {
            // Delete Images
            String where = "EXISTS (SELECT " + NEWS_IMAGE + " FROM " + TABLE_NEWS + " WHERE " + TABLE_IMAGES + "."
                    + IMAGES_URL + "=" + TABLE_NEWS + "." + NEWS_IMAGE + " AND " + TABLE_NEWS + "." + NEWS_FID + "="
                    + feedIds.get(0);
            ;
            for (int i = 1; i < feedIds.size(); i++) {
                where += " OR " + TABLE_NEWS + "." + NEWS_FID + "=" + feedIds.get(i);
            }
            where += ")";
            Log.d(TAG, this.writableDatabase.delete(TABLE_IMAGES, where, null) + " images deleted!");

            // Delete News
            where = NEWS_FID + "=" + feedIds.get(0);
            for (int i = 1; i < feedIds.size(); i++) {
                where += " OR " + NEWS_FID + "=" + feedIds.get(i);
            }
            Log.d(TAG, this.writableDatabase.delete(TABLE_NEWS, where, null) + " news items deleted!");

            // Delete Feeds
            where = FEEDS_ID + "=" + feedIds.get(0);
            for (int i = 1; i < feedIds.size(); i++) {
                where += " OR " + FEEDS_ID + "=" + feedIds.get(i);
            }
            Log.d(TAG, this.writableDatabase.delete(TABLE_FEEDS, where, null) + " feeds deleted!");
        }
        // Delete Category
        Log.d(TAG, this.writableDatabase.delete(TABLE_CATEGORIES, CATEGORIES_ID + "=" + categoryId, null)
                + " category deleted!");
    }

    @Override
    public void removeNewsFeed(long feedId) {
        // Delete Images
        String where = "EXISTS (SELECT " + NEWS_IMAGE + " FROM " + TABLE_NEWS + " WHERE " + TABLE_IMAGES + "."
                + IMAGES_URL + "=" + TABLE_NEWS + "." + NEWS_IMAGE + " AND " + TABLE_NEWS + "." + NEWS_FID + "="
                + feedId + ")";
        Log.d(TAG, this.writableDatabase.delete(TABLE_IMAGES, where, null) + " images deleted!");

        // Delete News
        Log.d(TAG, this.writableDatabase.delete(TABLE_NEWS, NEWS_FID + "=" + feedId, null) + " news items deleted!");

        // Delete Feeds
        Log.d(TAG, this.writableDatabase.delete(TABLE_FEEDS, FEEDS_ID + "=" + feedId, null) + " feeds deleted!");
    }

    @Override
    public boolean markIsRead(long newsId) {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(NEWS_READ, 1);

        int count = this.writableDatabase.update(TABLE_NEWS, contentValues, NEWS_ID + "=" + newsId, null);
        return (count == 1);
    }

    @Override
    public boolean changeNewsFeedActivationStatus(long feedId, boolean activated) {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(FEEDS_ACTIVATED, (activated ? 1 : 0));

        int count = this.writableDatabase.update(TABLE_FEEDS, contentValues, FEEDS_ID + "=" + feedId, null);
        return (count == 1);
    }

    @Override
    public Long createNewCategory(String name, UpdateInterval interval, Bitmap iconBitmap) {
        ContentValues categoryValues = getCategoryContentValues(name, interval, iconBitmap);
        if (categoryValues != null)
            return this.writableDatabase.insert(TABLE_CATEGORIES, null, categoryValues);
        return null;
    }

    private ContentValues getCategoryContentValues(String name, UpdateInterval interval, Bitmap iconBitmap) {
        if (name.length() > 0) {
            ContentValues categoryValues = new ContentValues(3);
            categoryValues.put(CATEGORIES_NAME, name);
            categoryValues.put(CATEGORIES_INTERVAL, interval.ordinal());
            byte[] iconBlob = null;
            if (iconBitmap != null) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                iconBitmap.compress(CompressFormat.PNG, 100, out);
                iconBlob = out.toByteArray();
                categoryValues.put(CATEGORIES_ICON, iconBlob);
            }
            return categoryValues;
        }
        return null;
    }

    @Override
    public boolean updateNewsCategory(long categoryId, String name, UpdateInterval interval, Bitmap iconBitmap) {
        ContentValues categoryValues = getCategoryContentValues(name, interval, iconBitmap);
        if (categoryValues != null)
            return this.writableDatabase.update(TABLE_CATEGORIES, categoryValues, CATEGORIES_ID + "=" + categoryId,
                    null) == 1;
        return false;
    }

    @Override
    public Long createNewNewsFeed(String url, long categoryId) {
        ContentValues feedValues = new ContentValues(2);
        feedValues.put(FEEDS_URL, url);
        feedValues.put(FEEDS_CID, categoryId);
        return this.writableDatabase.insert(TABLE_FEEDS, null, feedValues);
    }

    @Override
    public void updateOutdatedLastUpdatedTime(List<NewsFeed> outdatedNewsfeeds) {
        List<Long> outdatedCategories = new ArrayList<Long>();

        for (NewsFeed feed : outdatedNewsfeeds) {
            if (!outdatedCategories.contains(feed.getParentCategoryId())) {
                outdatedCategories.add(feed.getParentCategoryId());
            }
        }
        for (long categoryId : outdatedCategories)
            updateLastUpdatedTime(categoryId);
    }

    @Override
    public boolean updateLastUpdatedTime(Object criteria) {
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(CATEGORIES_LASTUPDATE, System.currentTimeMillis());
        String where = null;
        if (criteria != null) {
            if (criteria instanceof Long)
                where = CATEGORIES_ID + "=" + criteria;
            else
                where = CATEGORIES_INTERVAL + "=" + ((UpdateInterval) criteria).ordinal();
        }
        return (this.writableDatabase.update(TABLE_CATEGORIES, contentValues, where, null) == 1);
    }

    @Override
    public int removeUnusedImages() {
        int count = this.writableDatabase.delete(TABLE_IMAGES, IMAGES_DATE + "<"
                + (System.currentTimeMillis() - THREE_DAYS_MILLIS), null);
        Log.d(TAG, count + " unused images removed!");
        return count;
    }

    @Override
    public int removeNewsItemsByCount() {
        storeLastCategoryItemDate();
        int count = 0;
        Iterator<Entry<Long, Long>> iterator = lastCategoryItemDate.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, Long> entry = iterator.next();
            String where = TABLE_NEWS + "." + NEWS_DATE + "<" + entry.getValue() + " AND EXISTS (SELECT * FROM "
                    + TABLE_FEEDS + " WHERE " + TABLE_FEEDS + "." + FEEDS_CID + "=" + entry.getKey() + " AND "
                    + TABLE_FEEDS + "." + FEEDS_ID + "=" + TABLE_NEWS + "." + NEWS_FID + ")";
            int localCount = this.writableDatabase.delete(TABLE_NEWS, where, null);
            count += localCount;
            Log.d(TAG, localCount + " old category " + entry.getKey() + " news items removed by count!");
        }
        return count;
    }

    @Override
    public int removeNewsItemsByTime() {
        long deleteBeforeTime = System.currentTimeMillis() - THREE_DAYS_MILLIS;
        try {
            String where = NEWS_DATE + "<" + deleteBeforeTime + " OR NOT EXISTS (SELECT " + FEEDS_ID + " FROM "
                    + TABLE_FEEDS + " WHERE " + TABLE_FEEDS + "." + FEEDS_ID + "=" + TABLE_NEWS + "." + NEWS_FID + ")";
            int rows = this.writableDatabase.delete(TABLE_NEWS, where, null);
            Log.d(TAG, rows + " old news items removed by time!");
            return rows;
        } catch (Exception e) {
            return 0;
        }
    }

    // / Private helper methods

    /**
     * Private method to check if a existing news changed by checking the dates
     * 
     * @return
     */
    private boolean newsDateHasChanged(String guid, Date newDate) {
        String[] whereArgs = { guid };
        Cursor query = this.writableDatabase.query(TABLE_NEWS, new String[] { NEWS_DATE }, NEWS_GUID + "=?", whereArgs,
                null, null, null);
        if (query.getCount() > 0) {
            query.moveToFirst();
            Date oldDate = new Date(Long.valueOf(query.getString(query.getColumnIndex(NEWS_DATE))));
            query.close();
            if (newDate.getTime() > oldDate.getTime())
                Log.w(TAG, newDate.toLocaleString() + " - " + oldDate.toLocaleString());
            return newDate.getTime() > oldDate.getTime();
        }
        query.close();
        return false;
    }

    /**
     * A helper method to keep track of the last news item's date of an category
     * defined by the limit
     */
    private void storeLastCategoryItemDate() {
        Cursor categories = this.writableDatabase.query(TABLE_CATEGORIES, new String[] { CATEGORIES_ID }, null, null,
                null, null, null);

        int count = ApplicationConstants.NEWSCATEGORY_LIMIT;
        if (categories.getCount() > 0) {
            while (categories.moveToNext()) {
                long categoryId = categories.getLong(0);
                String where = "feeds." + FEEDS_CID + "=" + categoryId + " AND " + "feeds." + FEEDS_ID + "=" + "news."
                        + NEWS_FID + " AND " + TABLE_FEEDS + "." + FEEDS_ACTIVATED + "=1";
                Cursor query = this.writableDatabase.query(TABLE_FEEDS + " feeds, " + TABLE_NEWS + " news",
                        new String[] { NEWS_DATE, NEWS_TITLE }, where, null, null, null, NEWS_DATE + " DESC",
                        String.valueOf(count));
                long lastDate = 0;
                if (query.getCount() > 0 && !(query.getCount() < 50)) {
                    query.moveToLast();
                    lastDate = query.getLong(0);
                }
                this.lastCategoryItemDate.put(categoryId, lastDate);
                query.close();
            }
        }
        categories.close();
    }

    /**
     * Inner helper class for accessing the News SQLite Database
     * 
     * @author Christoph Huebner
     * 
     */
    protected class NewsDBHelper extends SQLiteOpenHelper {

        /**
         * Creates a new helper instance with a given application context
         * 
         * @param applicationContext
         */
        public NewsDBHelper(Context applicationContext, IConfigurationManager configurationManager) {
            super(applicationContext, configurationManager.getConfiguration().getNewsDatabaseName(), null,
                    configurationManager.getConfiguration().getNewsDatabaseVersion());
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createTables(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO: This is only valid for the first version! We have to
            // implement "real" update mechanisms
        }

        /**
         * Clears the database: Recreates all tables
         * 
         * @param db
         *            a database instance
         */
        public void clearDatabase(SQLiteDatabase db) {
            dropTables(db);
            createTables(db);
        }

        /**
         * Create and fill the needed tables for the news module
         * 
         * @param db
         *            A database
         */
        private void createTables(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORIES + " (" + CATEGORIES_ID
                    + " integer primary key autoincrement, " + CATEGORIES_NAME + " text not null, " + CATEGORIES_ICON
                    + " blob, " + CATEGORIES_INTERVAL + " integer not null, " + CATEGORIES_LASTUPDATE + " date);");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_FEEDS + " (" + FEEDS_ID
                    + " integer primary key autoincrement, " + FEEDS_NAME + " text, " + FEEDS_URL + " text not null, "
                    + FEEDS_ACTIVATED + " integer default 1, " + FEEDS_CID + " integer, " + "FOREIGN KEY (" + FEEDS_CID
                    + ") REFERENCES " + TABLE_CATEGORIES + " (" + CATEGORIES_ID + ") ON DELETE CASCADE);");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NEWS + " (" + NEWS_ID
                    + " integer primary key autoincrement, " + NEWS_READ + " integer default 0, " + NEWS_TITLE
                    + " text, " + NEWS_SUMMARY + " text, " + NEWS_DATE + " date, " + NEWS_CONTENT + " text, "
                    + NEWS_GUID + " text not null unique, " + NEWS_URL + " text not null unique, " + NEWS_IMAGE
                    + " text, " + NEWS_FID + " integer, " + "FOREIGN KEY (" + NEWS_FID + ") REFERENCES " + TABLE_FEEDS
                    + " (" + FEEDS_ID + ") ON DELETE CASCADE);");

            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_IMAGES + " (" + IMAGES_ID
                    + " integer primary key autoincrement, " + IMAGES_URL + " text not null unique, " + IMAGES_CONTENT
                    + " blob, " + IMAGES_DATE + " date default (datetime('now')));");

        }

        /**
         * Drop the news tables
         * 
         * @param db
         */
        private void dropTables(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NEWS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FEEDS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
        }

    }

    @Override
    public void onLowMemoryWarning() {

    }

    @Override
    public void onResume() {

    }

}
