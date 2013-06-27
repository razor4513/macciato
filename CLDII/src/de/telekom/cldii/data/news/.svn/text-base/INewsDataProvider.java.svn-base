package de.telekom.cldii.data.news;

import java.util.Date;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import de.telekom.cldii.config.IConfigurationManager;
import de.telekom.cldii.data.IDataProvider;
import de.telekom.cldii.service.UpdateInterval;
import de.telekom.cldii.service.imagedownload.RSSImageRetrieverItem;
import de.telekom.cldii.widget.grid.GridViewContent;

/**
 * DataProvider for the news module.
 * 
 * @author Anton Wolf, jambit GmbH
 */
public interface INewsDataProvider extends IDataProvider {

    /**
     * Returns all available Categories as a list, ordered by Name
     * 
     * @return
     */
    public List<NewsCategory> getNewsCategoriesOrderedByName();

    /**
     * Returns a news category page, ordered by Name
     * 
     * @param pageNumber
     *            number of the page to return
     * @return the given page
     */
    public GridViewContent getNewsCategoryPageOrderedByName(int pageNumber);

    /**
     * Returns a all news category pages, ordered by Name
     * 
     * @return all news categories as pages between <code>fromPage</code> and
     *         <code>toPage</code>
     */
    public List<GridViewContent> getNewsCategoryPagesOrderedByName();

    /**
     * Returns a news item for a given news id
     * 
     * @param newsId
     *            the identifier of a news
     * @return the news item for the given news id
     */
    public NewsItem getNewsItemForNewsId(long newsId);

    /**
     * Returns a list of all news items for a new category ordered by Date
     * 
     * @param categoryId
     *            the identifier of the category
     * @return all news items as a list ordered by date
     */
    public List<NewsItem> getNewsItemsForNewsCategoryOrderedByDate(long categoryId);

    /**
     * Returns the image defined by a URL from database
     * 
     * @param newId
     *            the news item identifier for broadcast notification
     * @param url
     *            the URL acting as a image identifier
     * @return the according image belonging to the URL
     */
    public Drawable getImageForUrl(long newsId, String url);

    /**
     * Returns the name of a news category for a given id
     * 
     * @param id
     *            the identifier of the category
     * @return name of the category
     */
    public String getNewsCategoryNameById(long id);

    /**
     * Returns the properties of a news category for a given id
     * 
     * @param id
     *            the identifier of the category
     * @return the category properties
     */
    public NewsCategory getNewsCategoryById(long id);

    /**
     * Returns a list of news feeds for a given interval type
     * 
     * @param intervalType
     *            the interval type
     * @return list of feeds
     */
    public List<NewsFeed> getFeedsByIntervalType(UpdateInterval intervalType);

    /**
     * Returns a list of news feeds for a given category id
     * 
     * @param categoryId
     *            the identifier of the category
     * @return list of feeds of the given category
     */
    public List<NewsFeed> getFeedsForCategoryId(long categoryId);

    /**
     * Returns the number of unread news items for a category
     * 
     * @param categoryId
     *            the identifier of the category
     * @return number of unread news items
     */
    public int getNumberOfUnreadNewsItemsForCategoryId(long categoryId);

    /**
     * Returns the number of unread news items
     * 
     * @return number of unread news items
     */
    public int getNumberOfUnreadNewsItems();

    /**
     * Returns a list of all news feeds
     * 
     * @return list of all feeds
     */
    public List<NewsFeed> getAllFeeds();

    /**
     * Returns a list of all outdated news feeds
     * 
     * @return list of all outdated feeds
     */
    public List<NewsFeed> getAllOutdatedFeeds(IConfigurationManager configurationManager);

    /**
     * Returns an object, mostly string, for an lazy loading column
     * 
     * @param newsId
     *            the identifier of the news item
     * @param column
     *            the column of the lazy loading
     * @return the content of the lazy loading
     */
    public Object getNewsItemColumnForNewsId(long newsId, String column);

    /**
     * Returns a list of thumbnail URLs belonging to news items that are not
     * downloaded yet
     * 
     * @return list of thumbnail URLs
     */
    public List<RSSImageRetrieverItem> getAllPendingThumbnailUrls();

    /**
     * Marks a news item from given news identifier as read
     * 
     * @param identifier
     *            of the news item
     * @return true if only the given news item was set, false otherwise
     */
    public boolean markIsRead(long newsId);

    /**
     * Stores a feed item
     * 
     * @param feedId
     *            the identifier of the feed
     * @param feedName
     *            the name of the feed
     */
    public void storeFeed(int feedId, String feedName);

    /**
     * Stores a news item
     * 
     * @param feedId
     *            the id of the feed, the new item is belonging to
     * @param title
     *            the title of the news item
     * @param description
     *            the description of the news item
     * @param publishDate
     *            the publish date of the news item
     * @param content
     *            the content of the news item
     * @param url
     *            the url of the news item
     * @param thumbnailUrl
     *            the url of the thumbnail of the news item
     * @param contentImageUrls
     *            the urls of the content images
     * @return the news id of the inserted news item
     */
    public Long storeNewsItem(long categoryId, int feedId, String title, String description, Date publishDate,
            String content, String guid, String url, String thumbnailUrl, List<String> contentImageUrls);

    /**
     * Stores a news item content image
     * 
     * @param imageUrl
     *            URL of the content image
     * @param imageBlob
     *            Blob of the content image
     */
    public void storeNewsItemContentImage(String imageUrl, byte[] imageBlob);

    /**
     * Stores the thumbnail of a news item
     * 
     * @param newsId
     *            the identifier of the news item
     * @param imageBlob
     *            blob of the thumbnail
     */
    public void storeNewsItemThumbnail(String newsId, byte[] imageBlob);

    /**
     * Deletes a category and its feeds (including news items) from the database
     * 
     * @param categoryId
     *            the identifier of the category
     */
    public void deleteCategory(long categoryId);

    /**
     * Removes news items older than 3 days old
     * 
     * @return count of removed items
     */
    public int removeNewsItemsByTime();

    /**
     * Removes the oldest news items of a category to limit the item count to
     * the a given number
     * 
     * @param count
     *            the number to limit the item count to
     * @return count of removed items
     */
    public int removeNewsItemsByCount();

    /**
     * Removes images from the database that aren't used by any news anymore
     * 
     * @return count of removed items
     */
    public int removeUnusedImages();

    /**
     * Updates the last updated time of a given category or interval type
     * 
     * @param criteria
     *            the identifier of the category or interval type. Use null if
     *            you want to update all feeds
     * @return true if successful, false otherwise
     */
    public boolean updateLastUpdatedTime(Object criteria);

    /**
     * Updates the last updated time of a given list of news feeds
     * 
     * @param outdatedNewsfeeds
     *            list of outdated news feeds
     */
    public void updateOutdatedLastUpdatedTime(List<NewsFeed> outdatedNewsfeeds);

    /**
     * Updates a existing category of a given category id
     * 
     * @param categoryId
     *            the identifier of the category
     * @param name
     *            name of the category
     * @param interval
     *            update interval of the category
     * @param iconBitmap
     *            category icon as a bitmap
     * 
     * @return true if successful, false otherwise
     */
    public boolean updateNewsCategory(long categoryId, String name, UpdateInterval interval, Bitmap iconBitmap);

    /**
     * Changes the activation status of the news feed for updates
     * 
     * @param feedId
     *            the identifier of the news feed
     * @param activated
     *            status to be set
     * @return true when successful, false otherwise
     */
    public boolean changeNewsFeedActivationStatus(long feedId, boolean activated);

    /**
     * Creates a new category with a given name and interval type
     * 
     * @param name
     *            name of the new category
     * @param interval
     *            interval for automatic updates
     * @param iconBitmap
     *            category icon as a bitmap
     * @return category id of the new category
     */
    public Long createNewCategory(String name, UpdateInterval interval, Bitmap iconBitmap);

    /**
     * Creates a new Feed wit a given URL
     * 
     * @param url
     *            URL of the new feed
     * @param categoryId
     *            the categoryId which the new feed belongs to
     * @return id of the new news feed
     */
    public Long createNewNewsFeed(String url, long categoryId);

    /**
     * Deletes the news feed given by its feed id including all news items and
     * its images
     * 
     * @param feedId
     *            the identifier of the news feed that is to be deleted
     */
    public void removeNewsFeed(long feedId);

    /**
     * @return the number of categories
     */
    int getNumberOfCategories();
}
